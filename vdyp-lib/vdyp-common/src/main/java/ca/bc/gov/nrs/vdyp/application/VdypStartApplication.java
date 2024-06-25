package ca.bc.gov.nrs.vdyp.application;

import static ca.bc.gov.nrs.vdyp.math.FloatMath.clamp;
import static ca.bc.gov.nrs.vdyp.math.FloatMath.exp;
import static ca.bc.gov.nrs.vdyp.math.FloatMath.floor;
import static ca.bc.gov.nrs.vdyp.math.FloatMath.log;
import static ca.bc.gov.nrs.vdyp.math.FloatMath.pow;
import static java.lang.Math.max;
import static java.lang.Math.min;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.bc.gov.nrs.vdyp.common.ControlKey;
import ca.bc.gov.nrs.vdyp.common.Utils;
import ca.bc.gov.nrs.vdyp.common.ValueOrMarker;
import ca.bc.gov.nrs.vdyp.io.FileSystemFileResolver;
import ca.bc.gov.nrs.vdyp.io.parse.coe.UpperCoefficientParser;
import ca.bc.gov.nrs.vdyp.io.parse.common.ResourceParseException;
import ca.bc.gov.nrs.vdyp.io.parse.control.BaseControlParser;
import ca.bc.gov.nrs.vdyp.io.parse.streaming.StreamingParser;
import ca.bc.gov.nrs.vdyp.io.parse.streaming.StreamingParserFactory;
import ca.bc.gov.nrs.vdyp.io.write.VriAdjustInputWriter;
import ca.bc.gov.nrs.vdyp.math.FloatMath;
import ca.bc.gov.nrs.vdyp.model.BaseVdypLayer;
import ca.bc.gov.nrs.vdyp.model.BaseVdypPolygon;
import ca.bc.gov.nrs.vdyp.model.BaseVdypSite;
import ca.bc.gov.nrs.vdyp.model.BaseVdypSpecies;
import ca.bc.gov.nrs.vdyp.model.BecDefinition;
import ca.bc.gov.nrs.vdyp.model.Coefficients;
import ca.bc.gov.nrs.vdyp.model.InputLayer;
import ca.bc.gov.nrs.vdyp.model.LayerType;
import ca.bc.gov.nrs.vdyp.model.MatrixMap;
import ca.bc.gov.nrs.vdyp.model.MatrixMap2;
import ca.bc.gov.nrs.vdyp.model.MatrixMap3;
import ca.bc.gov.nrs.vdyp.model.Region;

public abstract class VdypStartApplication<P extends BaseVdypPolygon<L, Optional<Float>, S, I>, L extends BaseVdypLayer<S, I> & InputLayer, S extends BaseVdypSpecies, I extends BaseVdypSite>
		extends VdypApplication implements Closeable {

	private static final Logger log = LoggerFactory.getLogger(VdypStartApplication.class);

	public static final int CONFIG_LOAD_ERROR = 1;
	public static final int PROCESSING_ERROR = 2;

	public static final float LOW_CROWN_CLOSURE = 10f;

	static final Map<String, Integer> ITG_PURE = Utils.constMap(map -> {
		map.put("AC", 36);
		map.put("AT", 42);
		map.put("B", 18);
		map.put("C", 9);
		map.put("D", 38);
		map.put("E", 40);
		map.put("F", 1);
		map.put("H", 12);
		map.put("L", 34);
		map.put("MB", 39);
		map.put("PA", 28);
		map.put("PL", 28);
		map.put("PW", 27);
		map.put("PY", 32);
		map.put("S", 21);
		map.put("Y", 9);
	});

	static final Set<String> HARDWOODS = Set.of("AC", "AT", "D", "E", "MB");

	protected static void doMain(VdypStartApplication<?, ?, ?, ?> app, final String... args) {
		var resolver = new FileSystemFileResolver();

		try {
			app.init(resolver, args);
		} catch (Exception ex) {
			log.error("Error during initialization", ex);
			System.exit(CONFIG_LOAD_ERROR);
		}

		try {
			app.process();
		} catch (Exception ex) {
			log.error("Error during processing", ex);
			System.exit(PROCESSING_ERROR);
		}
	}

	protected VriAdjustInputWriter vriWriter;

	protected Map<String, Object> controlMap = new HashMap<>();

	static final Comparator<BaseVdypSpecies> PERCENT_GENUS_DESCENDING = Utils
			.compareUsing(BaseVdypSpecies::getPercentGenus).reversed();

	/**
	 * When finding primary species these genera should be combined
	 */
	protected static final Collection<Collection<String>> PRIMARY_SPECIES_TO_COMBINE = Arrays
			.asList(Arrays.asList("PL", "PA"), Arrays.asList("C", "Y"));

	protected VdypStartApplication() {
		super();
	}

	/**
	 * Initialize application
	 *
	 * @param resolver
	 * @param controlFilePath
	 * @throws IOException
	 * @throws ResourceParseException
	 */
	public void init(FileSystemFileResolver resolver, String... controlFilePaths)
			throws IOException, ResourceParseException {

		// Load the control map

		if (controlFilePaths.length < 1) {
			throw new IllegalArgumentException("At least one control file must be specified.");
		}

		BaseControlParser parser = getControlFileParser();
		List<InputStream> resources = new ArrayList<>(controlFilePaths.length);
		try {
			for (String path : controlFilePaths) {
				resources.add(resolver.resolveForInput(path));
			}

			init(resolver, parser.parse(resources, resolver, controlMap));

		} finally {
			for (var resource : resources) {
				resource.close();
			}
		}
	}

	/**
	 * Initialize application
	 *
	 * @param controlMap
	 * @throws IOException
	 */
	public void init(FileSystemFileResolver resolver, Map<String, Object> controlMap) throws IOException {

		setControlMap(controlMap);
		closeVriWriter();
		vriWriter = new VriAdjustInputWriter(controlMap, resolver);
	}

	protected abstract BaseControlParser getControlFileParser();

	void closeVriWriter() throws IOException {
		if (vriWriter != null) {
			vriWriter.close();
			vriWriter = null;
		}
	}

	void setControlMap(Map<String, Object> controlMap) {
		this.controlMap = controlMap;
	}

	protected <T> StreamingParser<T> getStreamingParser(ControlKey key) throws ProcessingException {
		try {
			var factory = Utils
					.<StreamingParserFactory<T>>expectParsedControl(controlMap, key, StreamingParserFactory.class);

			return factory.get();
		} catch (IllegalStateException ex) {
			throw new ProcessingException(String.format("Data file %s not specified in control map.", key), ex);
		} catch (IOException ex) {
			throw new ProcessingException("Error while opening data file.", ex);
		}
	}

	public abstract void process() throws ProcessingException;

	@Override
	public void close() throws IOException {
		closeVriWriter();
	}

	protected Coefficients getCoeForSpecies(BaseVdypSpecies species, ControlKey controlKey) {
		var coeMap = Utils.<Map<String, Coefficients>>expectParsedControl(controlMap, controlKey, java.util.Map.class);
		return coeMap.get(species.getGenus());
	}

	protected L requireLayer(P polygon, LayerType type) throws ProcessingException {
		if (!polygon.getLayers().containsKey(type)) {
			throw validationError(
					"Polygon \"%s\" has no %s layer, or that layer has non-positive height or crown closure.",
					polygon.getPolygonIdentifier(), type
			);
		}

		return polygon.getLayers().get(type);
	}

	/**
	 * Get the sum of the percentages of the species in a layer. Throws an exception if this differs from the expected
	 * 100% by too much.
	 *
	 * @param layer
	 * @return
	 * @throws StandProcessingException
	 */
	protected float getPercentTotal(L layer) throws StandProcessingException {
		var percentTotal = (float) layer.getSpecies().values().stream()//
				.mapToDouble(BaseVdypSpecies::getPercentGenus)//
				.sum();
		if (Math.abs(percentTotal - 100f) > 0.01f) {
			throw validationError(
					"Polygon \"%s\" has %s layer where species entries have a percentage total that does not sum to 100%%.",
					layer.getPolygonIdentifier(), LayerType.PRIMARY
			);
		}
		return percentTotal;
	}

	protected abstract S copySpecies(S toCopy, Consumer<BaseVdypSpecies.Builder<S>> config);

	/**
	 * Returns the primary, and secondary if present species records as a one or two element list.
	 */
	protected List<S> findPrimarySpecies(Collection<S> allSpecies) {
		if (allSpecies.isEmpty()) {
			throw new IllegalArgumentException("Can not find primary species as there are no species");
		}
		var result = new ArrayList<S>(2);

		// Start with a deep copy of the species map so there are no side effects from
		// the manipulation this method does.
		var combined = new HashMap<String, S>(allSpecies.size());
		allSpecies.stream().forEach(spec -> combined.put(spec.getGenus(), copySpecies(spec, x -> {
		})));

		for (var combinationGroup : PRIMARY_SPECIES_TO_COMBINE) {
			var groupSpecies = combinationGroup.stream().map(combined::get).filter(Objects::nonNull).toList();
			if (groupSpecies.size() < 2) {
				continue;
			}
			var groupPrimary = copySpecies(
					// groupSpecies.size() is at least 2 so findFirest will not be empty
					groupSpecies.stream().sorted(PERCENT_GENUS_DESCENDING).findFirst().orElseThrow(), builder -> {
						var total = (float) groupSpecies.stream().mapToDouble(BaseVdypSpecies::getPercentGenus).sum();
						builder.percentGenus(total);
					}
			);
			combinationGroup.forEach(combined::remove);
			combined.put(groupPrimary.getGenus(), groupPrimary);
		}

		assert !combined.isEmpty();

		if (combined.size() == 1) {
			// There's only one
			result.addAll(combined.values());
		} else {
			var NDEBUG_22 = false;
			if (NDEBUG_22) {
				// TODO
				throw new UnsupportedOperationException();
			} else {
				combined.values().stream().sorted(PERCENT_GENUS_DESCENDING).limit(2).forEach(result::add);
			}
		}

		assert !result.isEmpty();
		assert result.size() <= 2;
		return result;
	}

	/**
	 * Find Inventory type group (ITG)
	 *
	 * @param primarySecondary
	 * @return
	 * @throws ProcessingException
	 */
	@SuppressWarnings(
		{ //
				"java:S1301", // Using switch instead of if for consistency
				"java:S3776" // Inherently a lot of branching in a consistent manner, breaking into more
								// functions would make it less comprehensible
		}
	)
	protected int findItg(List<S> primarySecondary) throws StandProcessingException {
		var primary = primarySecondary.get(0);

		if (primary.getPercentGenus() > 79.999) { // Copied from VDYP7
			return ITG_PURE.get(primary.getGenus());
		}
		assert primarySecondary.size() == 2;

		var secondary = primarySecondary.get(1);

		assert !primary.getGenus().equals(secondary.getGenus());

		switch (primary.getGenus()) {
		case "F":
			switch (secondary.getGenus()) {
			case "C", "Y":
				return 2;
			case "B", "H":
				return 3;
			case "S":
				return 4;
			case "PL", "PA":
				return 5;
			case "PY":
				return 6;
			case "L", "PW":
				return 7;
			default:
				return 8;
			}
		case "C", "Y":
			switch (secondary.getGenus()) {
			case "C", "Y":
				return 10;
			case "H", "B", "S":
				return 11;
			default:
				return 10;
			}
		case "B":
			switch (secondary.getGenus()) {
			case "C", "Y", "H":
				return 19;
			default:
				return 20;
			}
		case "H":
			switch (secondary.getGenus()) {
			case "C", "Y":
				return 14;
			case "B":
				return 15;
			case "S":
				return 16;
			default:
				if (HARDWOODS.contains(secondary.getGenus())) {
					return 17;
				}
				return 13;
			}
		case "S":
			switch (secondary.getGenus()) {
			case "C", "Y", "H":
				return 23;
			case "B":
				return 24;
			case "PL":
				return 25;
			default:
				if (HARDWOODS.contains(secondary.getGenus())) {
					return 26;
				}
				return 22;
			}
		case "PW":
			return 27;
		case "PL", "PA":
			switch (secondary.getGenus()) {
			case "PL", "PA":
				return 28;
			case "F", "PW", "L", "PY":
				return 29;
			default:
				if (HARDWOODS.contains(secondary.getGenus())) {
					return 31;
				}
				return 30;
			}
		case "PY":
			return 32;
		case "L":
			switch (secondary.getGenus()) {
			case "F":
				return 33;
			default:
				return 34;
			}
		case "AC":
			if (HARDWOODS.contains(secondary.getGenus())) {
				return 36;
			}
			return 35;
		case "D":
			if (HARDWOODS.contains(secondary.getGenus())) {
				return 38;
			}
			return 37;
		case "MB":
			return 39;
		case "E":
			return 40;
		case "AT":
			if (HARDWOODS.contains(secondary.getGenus())) {
				return 42;
			}
			return 41;
		default:
			throw new StandProcessingException("Unexpected primary species: " + primary.getGenus());
		}
	}

	public int findEmpiricalRelationshipParameterIndex(String specAlias, BecDefinition bec, int itg) {
		var groupMap = Utils.<MatrixMap2<String, String, Integer>>expectParsedControl(
				controlMap, ControlKey.DEFAULT_EQ_NUM, ca.bc.gov.nrs.vdyp.model.MatrixMap2.class
		);
		var modMap = Utils.<MatrixMap2<Integer, Integer, Optional<Integer>>>expectParsedControl(
				controlMap, ControlKey.EQN_MODIFIERS, ca.bc.gov.nrs.vdyp.model.MatrixMap2.class
		);
		var group = groupMap.get(specAlias, bec.getGrowthBec().getAlias());
		group = MatrixMap.safeGet(modMap, group, itg).orElse(group);
		return group;
	}

	protected VriAdjustInputWriter getVriWriter() {
		return vriWriter;
	}

	// EMP040
	protected float estimatePrimaryBaseArea(
			L layer, BecDefinition bec, float yieldFactor, float breastHeightAge, float baseAreaOverstory,
			float crownClosure
	) throws LowValueException {
		boolean lowCrownClosure = layer.getCrownClosure() < LOW_CROWN_CLOSURE;
		crownClosure = lowCrownClosure ? LOW_CROWN_CLOSURE : crownClosure;

		var coeMap = Utils.<MatrixMap2<String, String, Coefficients>>expectParsedControl(
				controlMap, ControlKey.COE_BA, ca.bc.gov.nrs.vdyp.model.MatrixMap2.class
		);
		var modMap = Utils.<MatrixMap2<String, Region, Float>>expectParsedControl(
				controlMap, ControlKey.BA_MODIFIERS, ca.bc.gov.nrs.vdyp.model.MatrixMap2.class
		);
		var upperBoundMap = Utils.<MatrixMap3<Region, String, Integer, Float>>expectParsedControl(
				controlMap, ControlKey.UPPER_BA_BY_CI_S0_P, MatrixMap3.class
		);

		var leadGenus = leadGenus(layer);

		var decayBecAlias = bec.getDecayBec().getAlias();
		Coefficients coe = weightedCoefficientSum(
				List.of(0, 1, 2, 3, 4, 5), 9, 0, layer.getSpecies().values(), BaseVdypSpecies::getFractionGenus,
				s -> coeMap.get(decayBecAlias, s.getGenus())
		);

		float ageToUse = clamp(breastHeightAge, 5f, 350f);
		float trAge = FloatMath.log(ageToUse);

		/* @formatter:off */
						//      A00 = exp(A(0)) * ( 1.0 +  A(1) * TR_AGE  )
						/* @formatter:on */
		var a00 = exp(coe.getCoe(0)) * (1f + coe.getCoe(1) * trAge);

		/* @formatter:off */
						//      AP  = exp( A(3)) + exp(A(4)) * TR_AGE
						/* @formatter:on */
		float ap = FloatMath.exp(coe.getCoe(3)) + exp(coe.getCoe(4)) * trAge;

		var baseArea = 0f;

		float height = getLayerHeight(layer).orElse(0f);
		if (height > coe.getCoe(2) - 3f) {
			/* @formatter:off */
							//  if (HD .le. A(2) - 3.0) then
							//      BAP = 0.0
							//      GO TO 90
							//  else if (HD .lt. A(2)+3.0) then
							//      FHD = (HD- (A(2)-3.00) )**2 / 12.0
							//  else
							//      FHD = HD-A(2)
							//  end if
							/* @formatter:on */
			var fHeight = height <= coe.getCoe(2) + 3f ? //
					pow(height - (coe.getCoe(2) - 3), 2) / 12f //
					: height - coe.getCoe(2);

			/* @formatter:off */
							//      BAP =  A00 * (CCUSE/100) ** ( A(7) + A(8)*log(HD) )   *
							//     &      FHD**AP * exp( A(5)*HD  + A(6) * BAV )
							/* @formatter:on */
			baseArea = a00 * FloatMath.pow(crownClosure / 100, coe.getCoe(7) + coe.getCoe(8) * FloatMath.log(height))
					* FloatMath.pow(fHeight, ap) * exp(coe.getCoe(5) * height + coe.getCoe(6) * baseAreaOverstory);

			baseArea *= modMap.get(leadGenus.getGenus(), bec.getRegion());

			// TODO
			var NDEBUG_1 = 0;
			if (NDEBUG_1 <= 0) {
				// See ISPSJF128
				var upperBound = upperBoundMap.get(bec.getRegion(), leadGenus.getGenus(), UpperCoefficientParser.BA);
				baseArea = min(baseArea, upperBound);
			}

			if (lowCrownClosure) {
				baseArea *= layer.getCrownClosure() / LOW_CROWN_CLOSURE;
			}

		}

		baseArea *= yieldFactor;

		// This is to prevent underflow errors in later calculations
		if (baseArea <= 0.05f) {
			throw new LowValueException("Estimated base area", baseArea, 0.05f);
		}
		return baseArea;
	}

	protected abstract float getYieldFactor(P polygon);

	protected abstract Optional<I> getPrimarySite(L layer);

	protected Optional<Float> getLayerHeight(L layer) {
		return getPrimarySite(layer).flatMap(BaseVdypSite::getHeight);
	}

	protected Optional<Float> getLayerAgeTotal(L layer) {
		return getPrimarySite(layer).flatMap(BaseVdypSite::getAgeTotal);
	}

	protected Optional<Float> getLayerYearstoBreastHhight(L layer) {
		return getPrimarySite(layer).flatMap(BaseVdypSite::getYearsToBreastHeight);
	}

	protected Optional<Float> getLayerBreastHeightAge(L layer) {
		// TODO implement accessor for VRI and FIP Site. InputSite interface?
		return getPrimarySite(layer).flatMap(
				site -> Utils.mapBoth(site.getAgeTotal(), site.getYearsToBreastHeight(), (at, ytbh) -> at - ytbh)
		);
	}

	protected float estimatePrimaryBaseArea(
			L layer, BecDefinition bec, float yieldFactor, float breastHeightAge, float baseAreaOverstory
	) throws LowValueException {
		return estimatePrimaryBaseArea(
				layer, bec, yieldFactor, breastHeightAge, baseAreaOverstory, layer.getCrownClosure()
		);
	}

	public S leadGenus(L fipLayer) {
		return fipLayer.getSpecies().values().stream()
				.sorted(Utils.compareUsing(BaseVdypSpecies::getFractionGenus).reversed()).findFirst().orElseThrow();
	}

	protected L getPrimaryLayer(P poly) throws StandProcessingException {
		L primaryLayer = poly.getLayers().get(LayerType.PRIMARY);
		if (primaryLayer == null) {
			throw new StandProcessingException("Polygon does not have a primary layer");
		}
		return primaryLayer;
	}

	protected Optional<L> getVeteranLayer(P poly) throws StandProcessingException {
		return Utils.optSafe(poly.getLayers().get(LayerType.VETERAN));
	}

	protected BecDefinition getBec(P poly) throws ProcessingException {
		return Utils.getBec(poly.getBiogeoclimaticZone(), controlMap);
	}

	protected static <E extends Throwable> void throwIfPresent(Optional<E> opt) throws E {
		if (opt.isPresent()) {
			throw opt.get();
		}
	}

	protected static StandProcessingException validationError(String template, Object... values) {

		return new StandProcessingException(String.format(template, values));
	}

	/**
	 * Create a coefficients object where its values are either a weighted sum of those for each of the given entities,
	 * or the value from one arbitrarily chose entity.
	 *
	 * @param <T>             The type of entity
	 * @param weighted        the indicies of the coefficients that should be weighted sums, those that are not included
	 *                        are assumed to be constant across all entities and one is choses arbitrarily.
	 * @param size            Size of the resulting coefficients object
	 * @param indexFrom       index from of the resulting coefficients object
	 * @param entities        the entities to do weighted sums over
	 * @param weight          the weight for a given entity
	 * @param getCoefficients the coefficients for a given entity
	 */
	public static <T> Coefficients weightedCoefficientSum(
			Collection<Integer> weighted, int size, int indexFrom, Collection<T> entities, ToDoubleFunction<T> weight,
			Function<T, Coefficients> getCoefficients
	) {
		Coefficients coe = Coefficients.empty(size, indexFrom);

		// Do the summation in double precision
		var coeWorking = new double[size];
		Arrays.fill(coeWorking, 0.0);

		for (var entity : entities) {
			var entityCoe = getCoefficients.apply(entity);
			double fraction = weight.applyAsDouble(entity);
			log.atInfo().addArgument(entity).addArgument(fraction).addArgument(entityCoe)
					.setMessage("For entity {} with fraction {} adding coefficients {}").log();
			for (int i : weighted) {
				coeWorking[i - indexFrom] += (entityCoe.getCoe(i)) * fraction;
			}
		}
		// Reduce back to float once done
		for (int i : weighted) {
			coe.setCoe(i, (float) coeWorking[i - indexFrom]);
		}

		// Pick one entity to fill in the fixed coefficients
		// Choice is arbitrary, they should all be the same
		var anyCoe = getCoefficients.apply(entities.iterator().next());

		for (int i = indexFrom; i < size + indexFrom; i++) {
			if (weighted.contains(i))
				continue;
			coe.setCoe(i, anyCoe.getCoe(i));
		}
		return coe;
	}

	/**
	 * Create a coefficients object where its values are either a weighted sum of those for each of the given entities,
	 * or the value from one arbitrarily chose entity.
	 *
	 * @param <T>             The type of entity
	 * @param size            Size of the resulting coefficients object
	 * @param indexFrom       index from of the resulting coefficients object
	 * @param entities        the entities to do weighted sums over
	 * @param weight          the weight for a given entity
	 * @param getCoefficients the coefficients for a given entity
	 */
	public static <T> Coefficients weightedCoefficientSum(
			int size, int indexFrom, Collection<T> entities, ToDoubleFunction<T> weight,
			Function<T, Coefficients> getCoefficients
	) {
		var weighted = IntStream.range(indexFrom, size + indexFrom).boxed().toList();
		return weightedCoefficientSum(weighted, size, indexFrom, entities, weight, getCoefficients);
	}

	// FIPLAND
	@SuppressWarnings("java:S3655")
	public float estimatePercentForestLand(P polygon, Optional<L> vetLayer, L primaryLayer) throws ProcessingException {
		if (polygon.getPercentAvailable().isPresent()) {
			return polygon.getPercentAvailable().get();
		}

		assert primaryLayer != null;

		final boolean veteran;
		{
			var resultOrIsVeteran = isVeteranForEstimatePercentForestLand(polygon, vetLayer);
			if (resultOrIsVeteran.isValue()) {
				return resultOrIsVeteran.getValue().orElseThrow();
			}

			veteran = resultOrIsVeteran.getMarker().orElseThrow();
		}

		float primaryAgeTotal = getLayerAgeTotal(primaryLayer).orElseThrow();

		float crownClosure = crownClosureForPercentForestLand(vetLayer, primaryLayer, veteran, primaryAgeTotal);

		/*
		 * assume that CC occurs at age 25 and that most land goes to 90% occupancy but that occupancy increases only 1%
		 * /yr with no increases after ages 25. });
		 */

		// Obtain the percent yield (in comparison with CC = 90%)

		float crownClosureTop = 90f;
		float breastHeightAge = primaryAgeTotal
				- getPrimarySite(primaryLayer).flatMap(BaseVdypSite::getYearsToBreastHeight).orElseThrow();

		float yieldFactor = getYieldFactor(polygon);

		var bec = Utils.getBec(polygon.getBiogeoclimaticZone(), controlMap);

		breastHeightAge = max(5.0f, breastHeightAge);
		// EMP040
		float baseAreaTop = estimatePrimaryBaseArea(
				primaryLayer, bec, yieldFactor, breastHeightAge, 0f, crownClosureTop
		);
		// EMP040
		float baseAreaHat = estimatePrimaryBaseArea(primaryLayer, bec, yieldFactor, breastHeightAge, 0f, crownClosure);

		float percentYield;
		if (baseAreaTop > 0f && baseAreaHat > 0f) {
			percentYield = min(100f, 100f * baseAreaHat / baseAreaTop);
		} else {
			percentYield = 90f;
		}

		float gainMax;
		if (primaryAgeTotal > 125f) {
			gainMax = 0f;
		} else if (primaryAgeTotal < 25f) {
			gainMax = max(90f - percentYield, 0);
		} else {
			gainMax = max(90f - percentYield, 0);
			gainMax = min(gainMax, 125 - primaryAgeTotal);
		}

		return floor(min(percentYield + gainMax, 100f));

	}

	protected static final ValueOrMarker.Builder<Float, Boolean> FLOAT_OR_BOOL = ValueOrMarker
			.builder(Float.class, Boolean.class);

	protected ValueOrMarker<Float, Boolean> isVeteranForEstimatePercentForestLand(P polygon, Optional<L> vetLayer) {
		boolean veteran = vetLayer//
				.filter(layer -> getLayerHeight(layer).orElse(0f) > 0f) //
				.filter(layer -> layer.getCrownClosure() > 0f)//
				.isPresent(); // LAYERV

		return FLOAT_OR_BOOL.marker(veteran);
	}

	private float crownClosureForPercentForestLand(
			Optional<L> vetLayer, L primaryLayer, boolean veteran, float primaryAgeTotal
	) {
		float crownClosure = primaryLayer.getCrownClosure();

		// Assume crown closure linear with age, to 25.
		if (primaryAgeTotal < 25f) {
			crownClosure *= 25f / primaryAgeTotal;
		}
		// define crown closure as the SUM of two layers
		if (veteran) {
			crownClosure += vetLayer.map(InputLayer::getCrownClosure).orElse(0f);
		}

		crownClosure = clamp(crownClosure, 0, 100);
		return crownClosure;
	}

	// EMP041
	protected float estimatePrimaryQuadMeanDiameter(
			L layer, BecDefinition bec, float breastHeightAge, float baseAreaOverstory
	) {
		var coeMap = Utils.<MatrixMap2<String, String, Coefficients>>expectParsedControl(
				controlMap, ControlKey.COE_DQ, MatrixMap2.class
		);
		var modMap = Utils.<MatrixMap2<String, Region, Float>>expectParsedControl(
				controlMap, ControlKey.DQ_MODIFIERS, MatrixMap2.class
		);
		var upperBoundMap = Utils.<MatrixMap3<Region, String, Integer, Float>>expectParsedControl(
				controlMap, ControlKey.UPPER_BA_BY_CI_S0_P, MatrixMap3.class
		);

		var leadGenus = leadGenus(layer);

		var decayBecAlias = bec.getDecayBec().getAlias();
		Coefficients coe = weightedCoefficientSum(
				List.of(0, 1, 2, 3, 4), 8, 0, layer.getSpecies().values(), BaseVdypSpecies::getFractionGenus,
				s -> coeMap.get(decayBecAlias, s.getGenus())
		);

		var trAge = log(clamp(breastHeightAge, 5f, 350f));
		var height = getLayerHeight(layer).orElse(0f);

		if (height <= coe.getCoe(5)) {
			return 7.6f;
		}

		/* @formatter:off */
		//    C0 = A(0)
		//    C1 = EXP(A(1)) + EXP(A(2)) * TR_AGE
		//    C2 = EXP(A(3)) + EXP(A(4)) * TR_AGE
		/* @formatter:on */
		var c0 = coe.get(0);
		var c1 = exp(coe.getCoe(1)) + exp(coe.getCoe(2)) * trAge;
		var c2 = exp(coe.getCoe(3)) + exp(coe.getCoe(4)) * trAge;

		/* @formatter:off */
		//      DQ = C0 + ( C1*(HD - A(5))**C2 )**2 * exp(A(7)*BAV)
		//     &        * (1.0 - A(6)*CC/100.0)
		/* @formatter:on */

		var quadMeanDiameter = c0 + pow(c1 * pow(height - coe.getCoe(5), c2), 2)
				* exp(coe.getCoe(7) * baseAreaOverstory) * (1f - coe.getCoe(6) * layer.getCrownClosure() / 100f);

		/* @formatter:off */
		//      DQ = DQ * DQMOD200(JLEAD, INDEX_IC)
		/* @formatter:on */
		quadMeanDiameter *= modMap.get(leadGenus.getGenus(), bec.getRegion());

		quadMeanDiameter = max(quadMeanDiameter, 7.6f);

		// TODO
		var NDEBUG_2 = 0;
		if (NDEBUG_2 <= 0) {
			// See ISPSJF129
			var upperBound = upperBoundMap.get(bec.getRegion(), leadGenus.getGenus(), UpperCoefficientParser.DQ);
			quadMeanDiameter = min(quadMeanDiameter, upperBound);
		}

		return quadMeanDiameter;
	}

}
