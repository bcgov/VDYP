package ca.bc.gov.nrs.vdyp.application;

import static ca.bc.gov.nrs.vdyp.math.FloatMath.abs;
import static ca.bc.gov.nrs.vdyp.math.FloatMath.clamp;
import static ca.bc.gov.nrs.vdyp.math.FloatMath.exp;
import static ca.bc.gov.nrs.vdyp.math.FloatMath.floor;
import static ca.bc.gov.nrs.vdyp.math.FloatMath.log;
import static ca.bc.gov.nrs.vdyp.math.FloatMath.pow;
import static ca.bc.gov.nrs.vdyp.math.FloatMath.sqrt;
import static java.lang.Math.max;
import static java.lang.Math.min;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
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
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.bc.gov.nrs.vdyp.common.ControlKey;
import ca.bc.gov.nrs.vdyp.common.Utils;
import ca.bc.gov.nrs.vdyp.common.ValueOrMarker;
import ca.bc.gov.nrs.vdyp.common_calculators.BaseAreaTreeDensityDiameter;
import ca.bc.gov.nrs.vdyp.common_calculators.EMP;
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
import ca.bc.gov.nrs.vdyp.model.CompatibilityVariableMode;
import ca.bc.gov.nrs.vdyp.model.InputLayer;
import ca.bc.gov.nrs.vdyp.model.LayerType;
import ca.bc.gov.nrs.vdyp.model.MatrixMap;
import ca.bc.gov.nrs.vdyp.model.MatrixMap2;
import ca.bc.gov.nrs.vdyp.model.MatrixMap3;
import ca.bc.gov.nrs.vdyp.model.Region;
import ca.bc.gov.nrs.vdyp.model.UtilizationClass;
import ca.bc.gov.nrs.vdyp.model.VdypLayer;
import ca.bc.gov.nrs.vdyp.model.VdypSpecies;
import ca.bc.gov.nrs.vdyp.model.VdypUtilizationHolder;
import ca.bc.gov.nrs.vdyp.model.VolumeComputeMode;

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

	// TODO Should probably handle this with enums instead for clarity.
	private int[] debugModes = new int[25];

	public int getDebugMode(int index) {
		return debugModes[index];
	}

	public void setDebugMode(int index, int mode) {
		debugModes[index] = mode;
	}

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

	/**
	 * Accessor methods for utilization vectors, except for Lorey Height, on Layer and Species objects.
	 */
	protected static final Collection<PropertyDescriptor> UTILIZATION_VECTOR_ACCESSORS;

	/**
	 * Accessor methods for utilization vectors, except for Lorey Height and Quadratic Mean Diameter, on Layer and
	 * Species objects. These are properties where the values for the layer are the sum of those for its species.
	 */
	static final Collection<PropertyDescriptor> SUMMABLE_UTILIZATION_VECTOR_ACCESSORS;

	/**
	 * Accessor methods for utilization vectors, except for Lorey Height,and Volume on Layer and Species objects.
	 */
	protected static final Collection<PropertyDescriptor> NON_VOLUME_UTILIZATION_VECTOR_ACCESSORS;

	static {
		try {
			var bean = Introspector.getBeanInfo(VdypUtilizationHolder.class);
			UTILIZATION_VECTOR_ACCESSORS = Arrays.stream(bean.getPropertyDescriptors()) //
					.filter(p -> p.getName().endsWith("ByUtilization")) //
					.filter(p -> !p.getName().startsWith("loreyHeight")) //
					.filter(p -> p.getPropertyType() == Coefficients.class) //
					.toList();
		} catch (IntrospectionException e) {
			throw new IllegalStateException(e);
		}

		SUMMABLE_UTILIZATION_VECTOR_ACCESSORS = UTILIZATION_VECTOR_ACCESSORS.stream()
				.filter(x -> !x.getName().startsWith("quadraticMeanDiameter")).toList();

		NON_VOLUME_UTILIZATION_VECTOR_ACCESSORS = UTILIZATION_VECTOR_ACCESSORS.stream()
				.filter(x -> !x.getName().contains("Volume")).toList();
	}

	protected VriAdjustInputWriter vriWriter;

	protected Map<String, Object> controlMap = new HashMap<>();

	public EMP emp;

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

	protected void setControlMap(Map<String, Object> controlMap) {
		this.controlMap = controlMap;
		this.emp = new EMP(controlMap);
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

	public static final int UTIL_ALL = UtilizationClass.ALL.index;

	public static final int UTIL_LARGEST = UtilizationClass.OVER225.index;

	public static final int UTIL_SMALL = UtilizationClass.SMALL.index;

	public static final Collection<UtilizationClass> UTIL_CLASSES = List.of(
			UtilizationClass.U75TO125, UtilizationClass.U125TO175, UtilizationClass.U175TO225, UtilizationClass.OVER225
	);

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

	protected Map<String, Float>
			applyGroups(BaseVdypPolygon<?, ?, ?, ?> fipPolygon, Collection<VdypSpecies> vdypSpecies)
					throws ProcessingException {
		// Lookup volume group, Decay Group, and Breakage group for each species.

		Map<String, Float> targetPercentages = new HashMap<>(vdypSpecies.size());

		BecDefinition bec = Utils.getBec(fipPolygon.getBiogeoclimaticZone(), controlMap);
		var volumeGroupMap = getGroupMap(ControlKey.VOLUME_EQN_GROUPS);
		var decayGroupMap = getGroupMap(ControlKey.DECAY_GROUPS);
		var breakageGroupMap = getGroupMap(ControlKey.BREAKAGE_GROUPS);
		for (var vSpec : vdypSpecies) {
			// VGRPFIND
			var volumeGroup = volumeGroupMap.get(vSpec.getGenus(), bec.getVolumeBec().getAlias());
			// DGRPFIND
			var decayGroup = decayGroupMap.get(vSpec.getGenus(), bec.getDecayBec().getAlias());
			// BGRPFIND (Breakage uses decay BEC)
			var breakageGroup = breakageGroupMap.get(vSpec.getGenus(), bec.getDecayBec().getAlias());

			vSpec.setVolumeGroup(volumeGroup);
			vSpec.setDecayGroup(decayGroup);
			vSpec.setBreakageGroup(breakageGroup);

			targetPercentages.put(vSpec.getGenus(), vSpec.getPercentGenus());
		}

		return targetPercentages;
	}

	protected MatrixMap2<String, String, Integer> getGroupMap(ControlKey key) {
		return Utils.expectParsedControl(controlMap, key, ca.bc.gov.nrs.vdyp.model.MatrixMap2.class);
	}

	// YSMAL(0, X)
	/**
	 * Estimate small components for primary layer
	 *
	 * @throws ProcessingException
	 */
	public void estimateSmallComponents(P fPoly, VdypLayer layer) throws ProcessingException {
		float loreyHeightSum = 0f;
		float baseAreaSum = 0f;
		float treesPerHectareSum = 0f;
		float volumeSum = 0f;

		Region region = Utils.getBec(fPoly.getBiogeoclimaticZone(), controlMap).getRegion();

		for (VdypSpecies spec : layer.getSpecies().values()) {
			@SuppressWarnings("unused")
			float loreyHeightSpec = spec.getLoreyHeightByUtilization().getCoe(UTIL_ALL); // HLsp
			float baseAreaSpec = spec.getBaseAreaByUtilization().getCoe(UTIL_ALL); // BAsp
			@SuppressWarnings("unused")
			float quadMeanDiameterSpec = spec.getQuadraticMeanDiameterByUtilization().getCoe(UTIL_ALL); // DQsp

			// EMP080
			float smallComponentProbability = smallComponentProbability(layer, spec, region); // PROBsp

			// this WHOLE operation on Actual BA's, not 100% occupancy.
			float fractionAvailable = Utils.<Float>optSafe(fPoly.getPercentAvailable()).map(p -> p / 100f).orElse(1f);
			baseAreaSpec *= fractionAvailable;
			// EMP081
			float conditionalExpectedBaseArea = conditionalExpectedBaseArea(spec, baseAreaSpec, region); // BACONDsp
			conditionalExpectedBaseArea /= fractionAvailable;

			float baseAreaSpecSmall = smallComponentProbability * conditionalExpectedBaseArea;

			// EMP082
			float quadMeanDiameterSpecSmall = smallComponentQuadMeanDiameter(spec); // DQSMsp

			// EMP085
			float loreyHeightSpecSmall = smallComponentLoreyHeight(spec, quadMeanDiameterSpecSmall); // HLSMsp

			// EMP086
			float meanVolumeSmall = meanVolumeSmall(spec, quadMeanDiameterSpecSmall, loreyHeightSpecSmall); // VMEANSMs

			// TODO Apply Compatibility Variables, not needed for FIPSTART or VRISTART

			spec.getLoreyHeightByUtilization().setCoe(UTIL_SMALL, loreyHeightSpecSmall);
			float treesPerHectareSpecSmall = BaseAreaTreeDensityDiameter
					.treesPerHectare(baseAreaSpecSmall, quadMeanDiameterSpecSmall); // TPHSMsp
			spec.getBaseAreaByUtilization().setCoe(UTIL_SMALL, baseAreaSpecSmall);
			spec.getTreesPerHectareByUtilization().setCoe(UTIL_SMALL, treesPerHectareSpecSmall);
			spec.getQuadraticMeanDiameterByUtilization().setCoe(UTIL_SMALL, quadMeanDiameterSpecSmall);
			float wholeStemVolumeSpecSmall = treesPerHectareSpecSmall * meanVolumeSmall; // VOLWS(I,-1)
			spec.getWholeStemVolumeByUtilization().setCoe(UTIL_SMALL, wholeStemVolumeSpecSmall);

			loreyHeightSum += baseAreaSpecSmall * loreyHeightSpecSmall;
			baseAreaSum += baseAreaSpecSmall;
			treesPerHectareSum += treesPerHectareSpecSmall;
			volumeSum += wholeStemVolumeSpecSmall;
		}

		if (baseAreaSum > 0f) {
			layer.getLoreyHeightByUtilization().setCoe(UTIL_SMALL, loreyHeightSum / baseAreaSum);
		} else {
			layer.getLoreyHeightByUtilization().setCoe(UTIL_SMALL, 0f);
		}
		layer.getBaseAreaByUtilization().setCoe(UTIL_SMALL, baseAreaSum);
		layer.getTreesPerHectareByUtilization().setCoe(UTIL_SMALL, treesPerHectareSum);
		layer.getQuadraticMeanDiameterByUtilization()
				.setCoe(UTIL_SMALL, BaseAreaTreeDensityDiameter.quadMeanDiameter(baseAreaSum, treesPerHectareSum));
		layer.getWholeStemVolumeByUtilization().setCoe(UTIL_SMALL, volumeSum);
	}

	// EMP085
	private float smallComponentLoreyHeight(VdypSpecies spec, float quadMeanDiameterSpecSmall) {
		Coefficients coe = getCoeForSpecies(spec, ControlKey.SMALL_COMP_HL);

		// EQN 1 in IPSJF119.doc

		float a0 = coe.getCoe(1);
		float a1 = coe.getCoe(2);

		return 1.3f + (spec.getLoreyHeightByUtilization().getCoe(UTIL_ALL) - 1.3f) * exp(
				a0 * (pow(quadMeanDiameterSpecSmall, a1)
						- pow(spec.getQuadraticMeanDiameterByUtilization().getCoe(UTIL_ALL), a1))
		);
	}

	// EMP082
	private float smallComponentQuadMeanDiameter(VdypSpecies spec) {
		Coefficients coe = getCoeForSpecies(spec, ControlKey.SMALL_COMP_DQ);

		// EQN 5 in IPSJF118.doc

		float a0 = coe.getCoe(1);
		float a1 = coe.getCoe(2);

		float logit = //
				a0 + a1 * spec.getLoreyHeightByUtilization().getCoe(UTIL_ALL);

		return 4.0f + 3.5f * exp(logit) / (1.0f + exp(logit));
	}

	// EMP081
	private float conditionalExpectedBaseArea(VdypSpecies spec, float baseAreaSpec, Region region) {
		Coefficients coe = getCoeForSpecies(spec, ControlKey.SMALL_COMP_BA);

		// EQN 3 in IPSJF118.doc

		float a0 = coe.getCoe(1);
		float a1 = coe.getCoe(2);
		float a2 = coe.getCoe(3);
		float a3 = coe.getCoe(4);

		float coast = region == Region.COASTAL ? 1.0f : 0.0f;

		// FIXME due to a bug in VDYP7 it always treats this as interior. Replicating
		// that for now.
		coast = 0f;

		float arg = //
				(a0 + //
						a1 * coast + //
						a2 * spec.getBaseAreaByUtilization().getCoe(UTIL_ALL)//
				) * exp(a3 * spec.getLoreyHeightByUtilization().getCoe(UTIL_ALL));
		arg = max(arg, 0f);

		return arg;
	}

	// EMP080
	private float smallComponentProbability(VdypLayer layer, VdypSpecies spec, Region region) {
		Coefficients coe = getCoeForSpecies(spec, ControlKey.SMALL_COMP_PROBABILITY);

		// EQN 1 in IPSJF118.doc

		float a0 = coe.getCoe(1);
		float a1 = coe.getCoe(2);
		float a2 = coe.getCoe(3);
		float a3 = coe.getCoe(4);

		float coast = region == Region.COASTAL ? 1.0f : 0.0f;

		float logit = //
				a0 + //
						a1 * coast + //
						a2 * layer.getBreastHeightAge().orElse(0f) + //
						a3 * spec.getLoreyHeightByUtilization().getCoe(UTIL_ALL);

		return exp(logit) / (1.0f + exp(logit));
	}

	// EMP086
	private float meanVolumeSmall(VdypSpecies spec, float quadMeanDiameterSpecSmall, float loreyHeightSpecSmall) {
		Coefficients coe = getCoeForSpecies(spec, ControlKey.SMALL_COMP_WS_VOLUME);

		// EQN 1 in IPSJF119.doc

		float a0 = coe.getCoe(1);
		float a1 = coe.getCoe(2);
		float a2 = coe.getCoe(3);
		float a3 = coe.getCoe(4);

		return exp(
				a0 + a1 * log(quadMeanDiameterSpecSmall) + a2 * log(loreyHeightSpecSmall)
						+ a3 * quadMeanDiameterSpecSmall
		);
	}

	public void computeUtilizationComponentsPrimary(
			BecDefinition bec, VdypLayer vdypLayer, VolumeComputeMode volumeComputeMode,
			CompatibilityVariableMode compatibilityVariableMode
	) throws ProcessingException {
		log.atTrace().setMessage("computeUtilizationComponentsPrimary for {}, stand total age is {}")
				.addArgument(vdypLayer.getPolygonIdentifier()).addArgument(vdypLayer.getAgeTotal()).log();

		log.atDebug().setMessage("Primary layer for {} has {} species/genera: {}")
				.addArgument(vdypLayer::getPolygonIdentifier) //
				.addArgument(() -> vdypLayer.getSpecies().size()) //
				.addArgument(() -> vdypLayer.getSpecies().keySet().stream().collect(Collectors.joining(", "))) //
				.log();

		for (VdypSpecies spec : vdypLayer.getSpecies().values()) {
			float loreyHeightSpec = spec.getLoreyHeightByUtilization().getCoe(VdypStartApplication.UTIL_ALL);
			float baseAreaSpec = spec.getBaseAreaByUtilization().getCoe(VdypStartApplication.UTIL_ALL);
			float quadMeanDiameterSpec = spec.getQuadraticMeanDiameterByUtilization()
					.getCoe(VdypStartApplication.UTIL_ALL);
			float treesPerHectareSpec = spec.getTreesPerHectareByUtilization().getCoe(VdypStartApplication.UTIL_ALL);

			log.atDebug().setMessage("Working with species {}  LH: {}  DQ: {}  BA: {}  TPH: {}")
					.addArgument(spec.getClass()).addArgument(loreyHeightSpec).addArgument(quadMeanDiameterSpec)
					.addArgument(baseAreaSpec).addArgument(treesPerHectareSpec);

			if (volumeComputeMode == VolumeComputeMode.BY_UTIL_WITH_WHOLE_STEM_BY_SPEC) {
				log.atDebug().log("Estimating tree volume");

				// EMP090
				throw new UnsupportedOperationException("TODO"); // Not used yet

				// log.atDebug().setMessage("Species WS stand volume {}")
				// .addArgument(() -> spec.getWholeStemVolumeByUtilization().getCoe(UTIL_ALL));

			}
			float wholeStemVolumeSpec = spec.getWholeStemVolumeByUtilization().getCoe(VdypStartApplication.UTIL_ALL);

			var baseAreaUtil = Utils.utilizationVector();
			var quadMeanDiameterUtil = Utils.utilizationVector();
			var treesPerHectareUtil = Utils.utilizationVector();
			var wholeStemVolumeUtil = Utils.utilizationVector();
			var closeVolumeUtil = Utils.utilizationVector();
			var closeVolumeNetDecayUtil = Utils.utilizationVector();
			var closeVolumeNetDecayWasteUtil = Utils.utilizationVector();
			var closeVolumeNetDecayWasteBreakUtil = Utils.utilizationVector();

			baseAreaUtil.setCoe(VdypStartApplication.UTIL_ALL, baseAreaSpec); // BAU
			quadMeanDiameterUtil.setCoe(VdypStartApplication.UTIL_ALL, quadMeanDiameterSpec); // DQU
			treesPerHectareUtil.setCoe(VdypStartApplication.UTIL_ALL, treesPerHectareSpec); // TPHU
			wholeStemVolumeUtil.setCoe(VdypStartApplication.UTIL_ALL, wholeStemVolumeSpec); // WSU

			var adjustCloseUtil = Utils.utilizationVector(); // ADJVCU
			@SuppressWarnings("unused")
			var adjustDecayUtil = Utils.utilizationVector(); // ADJVD
			@SuppressWarnings("unused")
			var adjustDecayWasteUtil = Utils.utilizationVector(); // ADJVDW

			// EMP071
			emp.estimateQuadMeanDiameterByUtilization(bec, quadMeanDiameterUtil, spec);

			// EMP070
			emp.estimateBaseAreaByUtilization(bec, quadMeanDiameterUtil, baseAreaUtil, spec);

			// Calculate tree density components
			for (var uc : VdypStartApplication.UTIL_CLASSES) {
				treesPerHectareUtil.setCoe(
						uc.index,
						BaseAreaTreeDensityDiameter
								.treesPerHectare(baseAreaUtil.getCoe(uc.index), quadMeanDiameterUtil.getCoe(uc.index))
				);
			}

			// reconcile components with totals

			// YUC1R
			reconcileComponents(baseAreaUtil, treesPerHectareUtil, quadMeanDiameterUtil);

			if (compatibilityVariableMode != CompatibilityVariableMode.NONE) {
				throw new UnsupportedOperationException("TODO");
			}

			// Recalculate TPH's

			for (var uc : VdypStartApplication.UTIL_CLASSES) {
				treesPerHectareUtil.setCoe(
						uc.index,
						BaseAreaTreeDensityDiameter
								.treesPerHectare(baseAreaUtil.getCoe(uc.index), quadMeanDiameterUtil.getCoe(uc.index))
				);
			}

			// Since DQ's may have changed, MUST RECONCILE AGAIN
			// Seems this might only be needed when compatibilityVariableMode is not NONE?

			// YUC1R
			reconcileComponents(baseAreaUtil, treesPerHectareUtil, quadMeanDiameterUtil);

			if (volumeComputeMode == VolumeComputeMode.ZERO) {
				throw new UnsupportedOperationException("TODO");
			} else {

				// EMP091
				emp.estimateWholeStemVolume(
						UtilizationClass.ALL, adjustCloseUtil.getCoe(4), spec.getVolumeGroup(), loreyHeightSpec,
						quadMeanDiameterUtil, baseAreaUtil, wholeStemVolumeUtil
				);

				if (compatibilityVariableMode == CompatibilityVariableMode.ALL) {
					// apply compatibity variables to WS volume

					// Set the adjustment factors for next three volume types

					throw new UnsupportedOperationException("TODO");
				} else {
					// Do nothing as the adjustment vectors are already set to 0
				}

				// EMP092
				emp.estimateCloseUtilizationVolume(
						UtilizationClass.ALL, adjustCloseUtil, spec.getVolumeGroup(), loreyHeightSpec,
						quadMeanDiameterUtil, wholeStemVolumeUtil, closeVolumeUtil
				);

				// EMP093
				emp.estimateNetDecayVolume(
						spec.getGenus(), bec.getRegion(), UtilizationClass.ALL, adjustCloseUtil, spec.getDecayGroup(),
						loreyHeightSpec, vdypLayer.getBreastHeightAge().orElse(0f), quadMeanDiameterUtil,
						closeVolumeUtil, closeVolumeNetDecayUtil
				);

				// EMP094
				emp.estimateNetDecayAndWasteVolume(
						bec.getRegion(), UtilizationClass.ALL, adjustCloseUtil, spec.getGenus(), loreyHeightSpec,
						vdypLayer.getBreastHeightAge().orElse(0f), quadMeanDiameterUtil, closeVolumeUtil,
						closeVolumeNetDecayUtil, closeVolumeNetDecayWasteUtil
				);

				if (this.getId().isStart()) {
					// EMP095
					emp.estimateNetDecayWasteAndBreakageVolume(
							UtilizationClass.ALL, spec.getBreakageGroup(), quadMeanDiameterUtil, closeVolumeUtil,
							closeVolumeNetDecayWasteUtil, closeVolumeNetDecayWasteBreakUtil
					);
				}
			}

			spec.getBaseAreaByUtilization().pairwiseInPlace(baseAreaUtil, EMP.COPY_IF_BAND);
			spec.getTreesPerHectareByUtilization().pairwiseInPlace(treesPerHectareUtil, EMP.COPY_IF_BAND);
			spec.getQuadraticMeanDiameterByUtilization().pairwiseInPlace(quadMeanDiameterUtil, EMP.COPY_IF_BAND);

			spec.getWholeStemVolumeByUtilization().pairwiseInPlace(wholeStemVolumeUtil, EMP.COPY_IF_NOT_TOTAL);
			spec.getCloseUtilizationVolumeByUtilization().pairwiseInPlace(closeVolumeUtil, EMP.COPY_IF_NOT_TOTAL);
			spec.getCloseUtilizationVolumeNetOfDecayByUtilization()
					.pairwiseInPlace(closeVolumeNetDecayUtil, EMP.COPY_IF_NOT_TOTAL);
			spec.getCloseUtilizationVolumeNetOfDecayAndWasteByUtilization()
					.pairwiseInPlace(closeVolumeNetDecayWasteUtil, EMP.COPY_IF_NOT_TOTAL);
			spec.getCloseUtilizationVolumeNetOfDecayWasteAndBreakageByUtilization()
					.pairwiseInPlace(closeVolumeNetDecayWasteBreakUtil, EMP.COPY_IF_NOT_TOTAL);

		}
		computeLayerUtilizationComponentsFromSpecies(vdypLayer);

		for (VdypSpecies spec : vdypLayer.getSpecies().values()) {
			if (vdypLayer.getBaseAreaByUtilization().getCoe(UTIL_ALL) > 0f) {
				spec.setFractionGenus(
						spec.getBaseAreaByUtilization().getCoe(UTIL_ALL)
								/ vdypLayer.getBaseAreaByUtilization().getCoe(UTIL_ALL)
				);
			}
			log.atDebug().addArgument(spec.getGenus()).addArgument(spec.getFractionGenus())
					.setMessage("Species {} base area {}%").log();
		}

		log.atDebug().setMessage("Calculating Stand Lorey Height").log();

		vdypLayer.getLoreyHeightByUtilization().setCoe(UTIL_SMALL, 0f);
		vdypLayer.getLoreyHeightByUtilization().setCoe(UTIL_ALL, 0f);

		for (VdypSpecies spec : vdypLayer.getSpecies().values()) {
			log.atDebug() //
					.addArgument(spec.getGenus()) //
					.addArgument(() -> spec.getLoreyHeightByUtilization().getCoe(UTIL_ALL))
					.addArgument(() -> spec.getBaseAreaByUtilization().getCoe(UTIL_ALL))
					.addArgument(
							() -> spec.getLoreyHeightByUtilization().getCoe(UTIL_ALL)
									* spec.getBaseAreaByUtilization().getCoe(UTIL_ALL)
					)
					.setMessage(
							"For species {}, Species LH (7.5cm+): {}, Species BA (7.5cm+): {}, Weighted LH (7.5cm+): {}"
					).log();
			vdypLayer.getLoreyHeightByUtilization().scalarInPlace(
					UTIL_SMALL,
					x -> x + spec.getLoreyHeightByUtilization().getCoe(UTIL_SMALL)
							* spec.getBaseAreaByUtilization().getCoe(UTIL_SMALL)
			);
			vdypLayer.getLoreyHeightByUtilization().scalarInPlace(
					UTIL_ALL,
					x -> x + spec.getLoreyHeightByUtilization().getCoe(UTIL_ALL)
							* spec.getBaseAreaByUtilization().getCoe(UTIL_ALL)
			);
		}
		{
			float baSmall = vdypLayer.getBaseAreaByUtilization().getCoe(UTIL_SMALL);
			float baAll = vdypLayer.getBaseAreaByUtilization().getCoe(UTIL_ALL);

			if (baSmall > 0) {
				vdypLayer.getLoreyHeightByUtilization().scalarInPlace(UTIL_SMALL, x -> x / baSmall);
			}
			if (baAll > 0) {
				vdypLayer.getLoreyHeightByUtilization().scalarInPlace(UTIL_ALL, x -> x / baAll);
			}

		}

	}

	/**
	 * Implements the three reconciliation modes for layer 1 as described in ipsjf120.doc
	 *
	 * @param baseAreaUtil
	 * @param treesPerHectareUtil
	 * @param quadMeanDiameterUtil
	 * @throws ProcessingException
	 */
	// YUC1R
	public void reconcileComponents(
			Coefficients baseAreaUtil, Coefficients treesPerHectareUtil, Coefficients quadMeanDiameterUtil
	) throws ProcessingException {
		if (baseAreaUtil.getCoe(UTIL_ALL) == 0f) {
			UTIL_CLASSES.forEach(uc -> {
				treesPerHectareUtil.setCoe(uc.index, 0f);
				baseAreaUtil.setCoe(uc.index, 0f);
			});
			return;
		}

		@SuppressWarnings("unused")
		float tphSum = 0f;
		float baSum = 0f;
		for (var uc : UTIL_CLASSES) {
			tphSum += treesPerHectareUtil.getCoe(uc.index);
			baSum += baseAreaUtil.getCoe(uc.index);
		}

		if (abs(baSum - baseAreaUtil.getCoe(UTIL_ALL)) / baSum > 0.00003) {
			throw new ProcessingException("Computed base areas for 7.5+ components do not sum to expected total");
		}

		float dq0 = BaseAreaTreeDensityDiameter
				.quadMeanDiameter(baseAreaUtil.getCoe(UTIL_ALL), treesPerHectareUtil.getCoe(UTIL_ALL));

		if (dq0 < 7.5f) {
			throw new ProcessingException(
					"Quadratic mean diameter computed from total base area and trees per hectare is less than 7.5 cm"
			);
		}

		float tphSumHigh = (float) UTIL_CLASSES.stream()
				.mapToDouble(
						uc -> BaseAreaTreeDensityDiameter.treesPerHectare(baseAreaUtil.getCoe(uc.index), uc.lowBound)
				).sum();

		if (tphSumHigh < treesPerHectareUtil.getCoe(UTIL_ALL)) {
			reconcileComponentsMode1(baseAreaUtil, treesPerHectareUtil, quadMeanDiameterUtil, tphSumHigh);
		} else {
			reconcileComponentsMode2Check(baseAreaUtil, treesPerHectareUtil, quadMeanDiameterUtil);
		}

	}

	/**
	 * Sets the Layer's utilization components based on those of its species.
	 *
	 * @param vdypLayer
	 */
	protected void computeLayerUtilizationComponentsFromSpecies(VdypLayer vdypLayer) {
		// Layer utilization vectors other than quadratic mean diameter are the pairwise
		// sums of those of their species
		sumSpeciesUtilizationVectorsToLayer(vdypLayer);

		{
			var hlVector = new Coefficients(new float[] { 0f, 0f }, -1);
			vdypLayer.getSpecies().values().stream().forEach(spec -> {
				var ba = spec.getBaseAreaByUtilization();
				hlVector.pairwiseInPlace(spec.getLoreyHeightByUtilization(), (x, y, i) -> x + y * ba.getCoe(i));
			});
			var ba = vdypLayer.getBaseAreaByUtilization();
			hlVector.scalarInPlace((x, i) -> ba.getCoe(i) > 0 ? x / ba.getCoe(i) : x);
			vdypLayer.setLoreyHeightByUtilization(hlVector);
		}
		// Quadratic mean diameter for the layer is computed from the BA and TPH after
		// they have been found from the species
		{
			var utilVector = vdypLayer.getBaseAreaByUtilization().pairwise(
					vdypLayer.getTreesPerHectareByUtilization(), BaseAreaTreeDensityDiameter::quadMeanDiameter
			);
			vdypLayer.setQuadraticMeanDiameterByUtilization(utilVector);
		}
	}

	// TODO De-reflectify this when we want to make it work in GralVM
	void sumSpeciesUtilizationVectorsToLayer(VdypLayer vdypLayer) throws IllegalStateException {
		try {
			for (var accessors : SUMMABLE_UTILIZATION_VECTOR_ACCESSORS) {
				var utilVector = Utils.utilizationVector();
				for (var vdypSpecies : vdypLayer.getSpecies().values()) {
					var speciesVector = (Coefficients) accessors.getReadMethod().invoke(vdypSpecies);
					utilVector.pairwiseInPlace(speciesVector, (x, y) -> x + y);
				}
				accessors.getWriteMethod().invoke(vdypLayer, utilVector);
			}
		} catch (IllegalAccessException | InvocationTargetException ex) {
			throw new IllegalStateException(ex);
		}
	}

	// TODO De-reflectify this when we want to make it work in GralVM
	protected void scaleAllSummableUtilization(VdypUtilizationHolder holder, float factor)
			throws IllegalStateException {
		try {
			for (var accessors : SUMMABLE_UTILIZATION_VECTOR_ACCESSORS) {
				((Coefficients) accessors.getReadMethod().invoke(holder)).scalarInPlace(x -> x * factor);
			}
		} catch (IllegalAccessException | InvocationTargetException ex) {
			throw new IllegalStateException(ex);
		}
	}

	private static final List<UtilizationClass> MODE_1_RECONCILE_AVAILABILITY_CLASSES = List
			.of(UtilizationClass.OVER225, UtilizationClass.U175TO225, UtilizationClass.U125TO175);

	@SuppressWarnings("java:S3655")
	void reconcileComponentsMode1(
			Coefficients baseAreaUtil, Coefficients treesPerHectareUtil, Coefficients quadMeanDiameterUtil,
			float tphSumHigh
	) {
		// MODE 1

		// the high sum of TPH's is too low. Need MODE 1 reconciliation MUST set DQU's
		// to lowest allowable values AND must move BA from upper classes to lower
		// classes.

		float tphNeed = treesPerHectareUtil.getCoe(UTIL_ALL) - tphSumHigh;

		UTIL_CLASSES.forEach(uc -> quadMeanDiameterUtil.setCoe(uc.index, uc.lowBound));

		for (var uc : MODE_1_RECONCILE_AVAILABILITY_CLASSES) {
			float tphAvail = BaseAreaTreeDensityDiameter
					.treesPerHectare(baseAreaUtil.getCoe(uc.index), uc.previous().get().lowBound)
					- BaseAreaTreeDensityDiameter.treesPerHectare(baseAreaUtil.getCoe(uc.index), uc.lowBound);

			if (tphAvail < tphNeed) {
				baseAreaUtil.scalarInPlace(uc.previous().get().index, x -> x + baseAreaUtil.getCoe(uc.index));
				baseAreaUtil.setCoe(uc.index, 0f);
				tphNeed -= tphAvail;
			} else {
				float baseAreaMove = baseAreaUtil.getCoe(uc.index) * tphNeed / tphAvail;
				baseAreaUtil.scalarInPlace(uc.previous().get().index, x -> x + baseAreaMove);
				baseAreaUtil.scalarInPlace(uc.index, x -> x - baseAreaMove);
				break;
			}
		}
		UTIL_CLASSES.forEach(
				uc -> treesPerHectareUtil.setCoe(
						uc.index,
						BaseAreaTreeDensityDiameter
								.treesPerHectare(baseAreaUtil.getCoe(uc.index), quadMeanDiameterUtil.getCoe(uc.index))
				)
		);
	}

	void reconcileComponentsMode2Check(
			Coefficients baseAreaUtil, Coefficients treesPerHectareUtil, Coefficients quadMeanDiameterUtil
	) throws ProcessingException {
		// Before entering mode 2, check to see if reconciliation is already adequate

		float tphSum = (float) UTIL_CLASSES.stream().mapToDouble(uc -> treesPerHectareUtil.getCoe(uc.index)).sum();

		if (abs(tphSum - treesPerHectareUtil.getCoe(UTIL_ALL)) / tphSum > 0.00001) {
			reconcileComponentsMode2(baseAreaUtil, treesPerHectareUtil, quadMeanDiameterUtil);
			return;
		}
		for (var uc : UTIL_CLASSES) {
			if (baseAreaUtil.getCoe(uc.index) > 0f) {
				if (treesPerHectareUtil.getCoe(uc.index) <= 0f) {
					reconcileComponentsMode2(baseAreaUtil, treesPerHectareUtil, quadMeanDiameterUtil);
					return;
				}
				float dWant = BaseAreaTreeDensityDiameter
						.quadMeanDiameter(baseAreaUtil.getCoe(uc.index), treesPerHectareUtil.getCoe(uc.index));
				float dqI = quadMeanDiameterUtil.getCoe(uc.index);
				if (dqI >= uc.lowBound && dqI <= uc.highBound && abs(dWant - dqI) < 0.00001) {
					return;
				}
			}
		}

	}

	private void reconcileComponentsMode2(
			Coefficients baseAreaUtil, Coefficients treesPerHectareUtil, Coefficients quadMeanDiameterUtil
	) throws ProcessingException {
		int n = 0;
		float baseAreaFixed = 0f;
		float treesPerHectareFixed = 0f;
		var quadMeanDiameterLimit = new boolean[] { false, false, false, false, false };
		Coefficients dqTrial = Utils.utilizationVector();

		while (true) {
			n++;

			if (n > 4) {
				throw new ProcessingException("Mode 2 component reconciliation iterations exceeded 4");
			}

			float sum = (float) UTIL_CLASSES.stream().mapToDouble(uc -> {
				float baI = baseAreaUtil.getCoe(uc.index);
				float dqI = quadMeanDiameterUtil.getCoe(uc.index);
				if (baI != 0 && !quadMeanDiameterLimit[uc.index]) {
					return baI / (dqI * dqI);
				}
				return 0;
			}).sum();

			float baAll = baseAreaUtil.getCoe(UTIL_ALL) - baseAreaFixed;
			float tphAll = treesPerHectareUtil.getCoe(UTIL_ALL) - treesPerHectareFixed;

			if (baAll <= 0f || tphAll <= 0f) {
				reconcileComponentsMode3(baseAreaUtil, treesPerHectareUtil, quadMeanDiameterUtil);
				return;
			}

			float dqAll = BaseAreaTreeDensityDiameter.quadMeanDiameter(baAll, tphAll);

			float k = dqAll * dqAll / baAll * sum;
			float sqrtK = sqrt(k);

			for (var uc : UTIL_CLASSES) {
				if (!quadMeanDiameterLimit[uc.index] && baseAreaUtil.getCoe(uc.index) > 0f) {
					dqTrial.setCoe(uc.index, quadMeanDiameterUtil.getCoe(uc.index) * sqrtK);
				}
			}

			UtilizationClass violateClass = null;
			float violate = 0f;
			boolean violateLow = false;

			for (var uc : UTIL_CLASSES) {
				if (baseAreaUtil.getCoe(uc.index) > 0f && dqTrial.getCoe(uc.index) < uc.lowBound) {
					float vi = 1f - dqTrial.getCoe(uc.index) / uc.lowBound;
					if (vi > violate) {
						violate = vi;
						violateClass = uc;
						violateLow = true;

					}
				}
				if (dqTrial.getCoe(uc.index) > uc.highBound) {
					float vi = dqTrial.getCoe(uc.index) / uc.highBound - 1f;
					if (vi > violate) {
						violate = vi;
						violateClass = uc;
						violateLow = false;
					}
				}
			}
			if (violateClass == null)
				break;
			// Move the worst offending DQ to its limit
			dqTrial.setCoe(violateClass.index, violateLow ? violateClass.lowBound : violateClass.highBound);

			quadMeanDiameterLimit[violateClass.index] = true;
			baseAreaFixed += baseAreaUtil.getCoe(violateClass.index);
			treesPerHectareFixed += BaseAreaTreeDensityDiameter
					.treesPerHectare(baseAreaUtil.getCoe(violateClass.index), dqTrial.getCoe(violateClass.index));
		}

		// Make BA's agree with DQ's and TPH's
		for (var uc : UTIL_CLASSES) {
			quadMeanDiameterUtil.setCoe(uc.index, dqTrial.getCoe(uc.index));
			treesPerHectareUtil.setCoe(
					uc.index,
					BaseAreaTreeDensityDiameter
							.treesPerHectare(baseAreaUtil.getCoe(uc.index), quadMeanDiameterUtil.getCoe(uc.index))
			);
		}
		// RE VERIFY That sums are correct
		float baSum = (float) UTIL_CLASSES.stream().mapToDouble(uc -> baseAreaUtil.getCoe(uc.index)).sum();
		float tphSum = (float) UTIL_CLASSES.stream().mapToDouble(uc -> treesPerHectareUtil.getCoe(uc.index)).sum();
		if (abs(baSum - baseAreaUtil.getCoe(UTIL_ALL)) / baSum > 0.0002) {
			throw new ProcessingException("Failed to reconcile Base Area");
		}
		if (abs(tphSum - treesPerHectareUtil.getCoe(UTIL_ALL)) / tphSum > 0.0002) {
			throw new ProcessingException("Failed to reconcile Trees per Hectare");
		}
	}

	@SuppressWarnings("java:S3655")
	void reconcileComponentsMode3(
			Coefficients baseAreaUtil, Coefficients treesPerHectareUtil, Coefficients quadMeanDiameterUtil
	) {

		/*
		 * Reconciliation mode 3 NOT IN THE ORIGINAL DESIGN The primary motivation for this mode is an example where all
		 * trees were in a signle utilization class and had a DQ of 12.4 cm. BUT the true DQ for the stand was slightly
		 * over 12.5. In this case the best solution is to simply reassign all trees to the single most appropriate
		 * class.
		 *
		 * Note, "original design" means something pre-VDYP 7. This was added to the Fortran some time before the port
		 * to Java including the comment above.
		 */
		UTIL_CLASSES.forEach(uc -> {
			baseAreaUtil.setCoe(uc.index, 0f);
			treesPerHectareUtil.setCoe(uc.index, 0f);
			quadMeanDiameterUtil.setCoe(uc.index, uc.lowBound + 2.5f);
		});

		var ucToUpdate = UTIL_CLASSES.stream().filter(uc -> quadMeanDiameterUtil.getCoe(UTIL_ALL) < uc.highBound)
				.findFirst().get();

		baseAreaUtil.setCoe(ucToUpdate.index, baseAreaUtil.getCoe(UTIL_ALL));
		treesPerHectareUtil.setCoe(ucToUpdate.index, treesPerHectareUtil.getCoe(UTIL_ALL));
		quadMeanDiameterUtil.setCoe(ucToUpdate.index, quadMeanDiameterUtil.getCoe(UTIL_ALL));
	}

}
