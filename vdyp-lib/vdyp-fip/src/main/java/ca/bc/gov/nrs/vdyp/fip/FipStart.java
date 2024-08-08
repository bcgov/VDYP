package ca.bc.gov.nrs.vdyp.fip;

import static ca.bc.gov.nrs.vdyp.math.FloatMath.abs;
import static ca.bc.gov.nrs.vdyp.math.FloatMath.clamp;
import static ca.bc.gov.nrs.vdyp.math.FloatMath.exp;
import static ca.bc.gov.nrs.vdyp.math.FloatMath.log;
import static ca.bc.gov.nrs.vdyp.math.FloatMath.pow;
import static java.lang.Math.max;
import static java.lang.Math.min;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.math3.analysis.MultivariateMatrixFunction;
import org.apache.commons.math3.analysis.MultivariateVectorFunction;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresFactory;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresProblem;
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer;
import org.apache.commons.math3.linear.DiagonalMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.util.FastMath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.bc.gov.nrs.vdyp.application.LowValueException;
import ca.bc.gov.nrs.vdyp.application.ProcessingException;
import ca.bc.gov.nrs.vdyp.application.StandProcessingException;
import ca.bc.gov.nrs.vdyp.application.VdypApplicationIdentifier;
import ca.bc.gov.nrs.vdyp.application.VdypStartApplication;
import ca.bc.gov.nrs.vdyp.common.ControlKey;
import ca.bc.gov.nrs.vdyp.common.EstimationMethods;
import ca.bc.gov.nrs.vdyp.common.Utils;
import ca.bc.gov.nrs.vdyp.common.ValueOrMarker;
import ca.bc.gov.nrs.vdyp.common_calculators.BaseAreaTreeDensityDiameter;
import ca.bc.gov.nrs.vdyp.fip.model.FipLayer;
import ca.bc.gov.nrs.vdyp.fip.model.FipLayerPrimary;
import ca.bc.gov.nrs.vdyp.fip.model.FipPolygon;
import ca.bc.gov.nrs.vdyp.fip.model.FipSite;
import ca.bc.gov.nrs.vdyp.fip.model.FipSpecies;
import ca.bc.gov.nrs.vdyp.io.parse.coe.BecDefinitionParser;
import ca.bc.gov.nrs.vdyp.io.parse.common.ResourceParseException;
import ca.bc.gov.nrs.vdyp.io.parse.control.BaseControlParser;
import ca.bc.gov.nrs.vdyp.io.parse.streaming.StreamingParser;
import ca.bc.gov.nrs.vdyp.model.BaseVdypSpecies;
import ca.bc.gov.nrs.vdyp.model.BecDefinition;
import ca.bc.gov.nrs.vdyp.model.Coefficients;
import ca.bc.gov.nrs.vdyp.model.CompatibilityVariableMode;
import ca.bc.gov.nrs.vdyp.model.LayerType;
import ca.bc.gov.nrs.vdyp.model.MatrixMap;
import ca.bc.gov.nrs.vdyp.model.MatrixMap2;
import ca.bc.gov.nrs.vdyp.model.PolygonMode;
import ca.bc.gov.nrs.vdyp.model.Region;
import ca.bc.gov.nrs.vdyp.model.StockingClassFactor;
import ca.bc.gov.nrs.vdyp.model.UtilizationClass;
import ca.bc.gov.nrs.vdyp.model.UtilizationVector;
import ca.bc.gov.nrs.vdyp.model.VdypLayer;
import ca.bc.gov.nrs.vdyp.model.VdypPolygon;
import ca.bc.gov.nrs.vdyp.model.VdypSpecies;
import ca.bc.gov.nrs.vdyp.model.VdypUtilizationHolder;
import ca.bc.gov.nrs.vdyp.model.VolumeComputeMode;

public class FipStart extends VdypStartApplication<FipPolygon, FipLayer, FipSpecies, FipSite> {

	public static final Comparator<FipSpecies> PERCENT_GENUS_DESCENDING = Utils
			.compareUsing(FipSpecies::getPercentGenus).reversed();

	public static final Logger log = LoggerFactory.getLogger(FipStart.class);

	public static final float TOLERANCE = 2.0e-3f;

	public static void main(final String... args) throws IOException {

		try (var app = new FipStart();) {
			doMain(app, args);
		}
	}

	// FIP_SUB
	// TODO Fortran takes a vector of flags (FIPPASS) controlling which stages are
	// implemented. FIPSTART always uses the same vector so far now that's not
	// implemented.
	@Override
	public void process() throws ProcessingException {
		int polygonsRead = 0;
		int polygonsWritten = 0;
		try (
				var polyStream = this.<FipPolygon>getStreamingParser(ControlKey.FIP_INPUT_YIELD_POLY);
				var layerStream = this.<Map<LayerType, FipLayer>>getStreamingParser(ControlKey.FIP_INPUT_YIELD_LAYER);
				var speciesStream = this.<Collection<FipSpecies>>getStreamingParser(ControlKey.FIP_INPUT_YIELD_LX_SP0);
		) {
			log.atDebug().setMessage("Start Stand processing").log();

			while (polyStream.hasNext()) {

				// FIP_GET
				log.atInfo().setMessage("Getting polygon {}").addArgument(polygonsRead + 1).log();
				var polygon = getPolygon(polyStream, layerStream, speciesStream);
				try {

					var resultPoly = processPolygon(polygonsRead, polygon);
					if (resultPoly.isPresent()) {
						polygonsRead++;

						// Output
						vriWriter.writePolygonWithSpeciesAndUtilization(resultPoly.get());

						polygonsWritten++;
					}

					log.atInfo().setMessage("Read {} polygons and wrote {}").addArgument(polygonsRead)
							.addArgument(polygonsWritten);

				} catch (StandProcessingException ex) {
					// TODO include some sort of hook for different forms of user output
					// TODO Implement single stand mode that propagates the exception

					log.atWarn().setMessage("Polygon {} bypassed").addArgument(polygon.getPolygonIdentifier())
							.setCause(ex);
				}

			}
		} catch (IOException | ResourceParseException ex) {
			throw new ProcessingException("Error while reading or writing data.", ex);
		}
	}

	static final EnumSet<PolygonMode> ACCEPTABLE_MODES = EnumSet.of(PolygonMode.START, PolygonMode.YOUNG);

	Optional<VdypPolygon> processPolygon(int polygonsRead, FipPolygon polygon)
			throws ProcessingException, LowValueException {
		VdypPolygon resultPoly;
		log.atInfo().setMessage("Read polygon {}, preparing to process").addArgument(polygon.getPolygonIdentifier())
				.log();

		// if (MODE .eq. -1) go to 100

		final var mode = polygon.getMode().orElse(PolygonMode.START);

		if (!ACCEPTABLE_MODES.contains(mode)) {
			log.atInfo().setMessage("Skipping polygon with mode {}").addArgument(mode).log();
			return Optional.empty();
		}

		// IP_IN = IP_IN+1
		// if (IP_IN .gt. MAXPOLY) go to 200

		// IPASS = 1
		// CALL FIP_CHK( IPASS, IER)
		// if (ier .gt. 0) go to 1000
		//
		// if (IPASS .le. 0) GO TO 120

		log.atInfo().setMessage("Checking validity of polygon {}:{}").addArgument(polygonsRead)
				.addArgument(polygon.getPolygonIdentifier()).log();
		checkPolygon(polygon);

		// CALL FIPCALCV( BAV, IER)
		// CALL FIPCALC1( BAV, BA_TOTL1, IER)

		Map<LayerType, VdypLayer> processedLayers = new EnumMap<>(LayerType.class);

		var fipLayers = polygon.getLayers();
		var fipVetLayer = Optional.ofNullable(fipLayers.get(LayerType.VETERAN));
		Optional<VdypLayer> resultVetLayer;
		if (fipVetLayer.isPresent()) {
			resultVetLayer = Optional.of(processLayerAsVeteran(polygon, fipVetLayer.get()));
		} else {
			resultVetLayer = Optional.empty();
		}
		resultVetLayer.ifPresent(layer -> processedLayers.put(LayerType.VETERAN, layer));

		FipLayerPrimary fipPrimeLayer = (FipLayerPrimary) fipLayers.get(LayerType.PRIMARY);
		assert fipPrimeLayer != null;
		var resultPrimeLayer = processLayerAsPrimary(
				polygon, fipPrimeLayer,
				resultVetLayer.map(VdypLayer::getBaseAreaByUtilization).map(coe -> coe.getAll()).orElse(0f)
		);
		processedLayers.put(LayerType.PRIMARY, resultPrimeLayer);

		resultPoly = createVdypPolygon(polygon, processedLayers);

		float baseAreaTotalPrime = resultPrimeLayer.getBaseAreaByUtilization().getAll(); // BA_TOTL1

		// if (FIPPASS(6) .eq. 0 .or. FIPPASS(6) .eq. 2) then
		if (true /* TODO */) {
			var minima = Utils.<Map<String, Float>>expectParsedControl(controlMap, ControlKey.MINIMA, Map.class);

			float minimumBaseArea = minima.get(BaseControlParser.MINIMUM_BASE_AREA);
			float minimumPredictedBaseArea = minima.get(BaseControlParser.MINIMUM_FULLY_STOCKED_AREA);
			if (baseAreaTotalPrime < minimumBaseArea) {
				throw new LowValueException("Base area", baseAreaTotalPrime, minimumBaseArea);
			}
			float predictedBaseArea = baseAreaTotalPrime * (100f / resultPoly.getPercentAvailable());
			if (predictedBaseArea < minimumPredictedBaseArea) {
				throw new LowValueException("Predicted base area", predictedBaseArea, minimumPredictedBaseArea);
			}
		}
		BecDefinition bec = BecDefinitionParser.getBecs(controlMap).get(polygon.getBiogeoclimaticZone())
				.orElseThrow(() -> new ProcessingException("Missing Bec " + polygon.getBiogeoclimaticZone()));
		// FIPSTK
		adjustForStocking(resultPoly.getLayers().get(LayerType.PRIMARY), fipPrimeLayer, bec);
		return Optional.of(resultPoly);
	}

	// FIPSTK
	void adjustForStocking(VdypLayer vdypLayer, FipLayerPrimary fipLayerPrimary, BecDefinition bec) {

		MatrixMap2<Character, Region, Optional<StockingClassFactor>> stockingClassMap = Utils
				.expectParsedControl(controlMap, ControlKey.STOCKING_CLASS_FACTORS, MatrixMap2.class);

		Region region = bec.getRegion();

		var factorEntry = fipLayerPrimary.getStockingClass()
				.flatMap(stockingClass -> MatrixMap.safeGet(stockingClassMap, stockingClass, region));

		if (!factorEntry.isPresent()) {
			return;
		}

		float factor = factorEntry.get().getFactor();

		scaleAllSummableUtilization(vdypLayer, factor);
		vdypLayer.getSpecies().values().forEach(spec -> scaleAllSummableUtilization(spec, factor));

		log.atInfo().addArgument(fipLayerPrimary.getStockingClass()).addArgument(factor).setMessage(
				"Foregoing Primary Layer has stocking class {} Yield values will be multiplied by {}  before being written to output file."
		);
	}

	VdypPolygon createVdypPolygon(FipPolygon fipPolygon, Map<LayerType, VdypLayer> processedLayers)
			throws ProcessingException {
		Optional<FipLayer> fipVetLayer = Utils.optSafe(fipPolygon.getLayers().get(LayerType.VETERAN));
		FipLayerPrimary fipPrimaryLayer = (FipLayerPrimary) fipPolygon.getLayers().get(LayerType.PRIMARY);

		float percentAvailable = estimatePercentForestLand(fipPolygon, fipVetLayer, fipPrimaryLayer);

		var vdypPolygon = VdypPolygon.build(builder -> builder.adapt(fipPolygon, x -> percentAvailable));
		vdypPolygon.setLayers(processedLayers);
		return vdypPolygon;
	}

	// FIPCALC1
	VdypLayer processLayerAsPrimary(FipPolygon fipPolygon, FipLayerPrimary fipLayer, float baseAreaOverstory)
			throws ProcessingException {

		// PRIMFIND
		var primarySpecies = findPrimarySpecies(fipLayer.getSpecies().values());

		// There's always at least one entry and we want the first.
		fipLayer.setPrimaryGenus(Optional.of(primarySpecies.iterator().next().getGenus()));

		// VDYP7 stores this in the common FIPL_1C/ITGL1 but only seems to use it
		// locally
		var itg = findItg(primarySpecies);

		BecDefinition bec = Utils.getBec(fipPolygon.getBiogeoclimaticZone(), controlMap);

		// GRPBA1FD
		int empiricalRelationshipParameterIndex = findEmpiricalRelationshipParameterIndex(
				primarySpecies.get(0).getGenus(), bec, itg
		);

		var result = VdypLayer.build(builder -> {
			builder.adapt(fipLayer);
			builder.inventoryTypeGroup(itg);
			builder.empiricalRelationshipParameterIndex(empiricalRelationshipParameterIndex);
		});

		var breastHeightAge = fipLayer.getSite()
				.flatMap(x -> Utils.mapBoth(x.getAgeTotal(), x.getYearsToBreastHeight(), (at, ytbh) -> at - ytbh))
				.orElse(0f);
		// EMP040
		var baseArea = estimatePrimaryBaseArea(
				fipLayer, bec, fipPolygon.getYieldFactor(), breastHeightAge, baseAreaOverstory
		); // BA_TOT

		result.getBaseAreaByUtilization().setAll(baseArea);

		var quadMeanDiameter = estimatePrimaryQuadMeanDiameter(fipLayer, bec, breastHeightAge, baseAreaOverstory);

		result.getQuadraticMeanDiameterByUtilization().setAll(quadMeanDiameter);

		var tphTotal = BaseAreaTreeDensityDiameter.treesPerHectare(baseArea, quadMeanDiameter);

		result.getTreesPerHectareByUtilization().setAll(tphTotal);

		// Copy over Species entries.
		// LVCOM/ISPL1=ISPV
		// LVCOM4/SP0L1=FIPSA/SP0V
		// LVCOM4/SP64DISTL1=FIPSA/VDISTRV
		// LVCOM1/PCLT1=FIPS/PCTVOLV
		var vdypSpecies = fipLayer.getSpecies().values().stream() //
				.map(fipSpec -> VdypSpecies.build(sb -> {
					sb.adapt(fipSpec);
					applyGroups(bec, fipSpec.getGenus(), sb);
					sb.adaptSiteFrom(fipSpec, (ib, fipSite) -> {
					});
				})) //
				.collect(Collectors.toMap(VdypSpecies::getGenus, Function.identity()));

		var vdypPrimarySpecies = vdypSpecies.get(primarySpecies.get(0).getGenus());

		Map<String, Float> targetPercentages = getTargetPercentages(vdypSpecies.values());

		var maxPass = fipLayer.getSpecies().size() > 1 ? 2 : 1;

		result.setSpecies(vdypSpecies);

		float leadHeight = fipLayer.getHeight().orElse(0f);
		for (var iPass = 1; iPass <= maxPass; iPass++) {
			findPrimaryHeightPass(bec, tphTotal, vdypSpecies, vdypPrimarySpecies, targetPercentages, leadHeight, iPass);

			// ROOTF01
			findRootsForDiameterAndBaseArea(result, fipLayer, bec, iPass + 1);
		}

		estimateSmallComponents(fipPolygon, result);

		// YUC1
		computeUtilizationComponentsPrimary(bec, result, VolumeComputeMode.BY_UTIL, CompatibilityVariableMode.NONE);

		return result;
	}

	void findPrimaryHeightPass(
			BecDefinition bec, float tphTotal, Map<String, VdypSpecies> vdypSpecies, VdypSpecies vdypPrimarySpecies,
			Map<String, Float> targetPercentages, float leadHeight, int iPass
	) throws ProcessingException {
		float primaryHeight;
		if (iPass == 2) {
			for (var vSpec : vdypSpecies.values()) {
				vSpec.setPercentGenus(targetPercentages.get(vSpec.getGenus()));
			}
		}
		// Estimate lorey height for primary species
		if (iPass == 1 && vdypSpecies.size() == 1) {
			primaryHeight = estimationMethods
					.primaryHeightFromLeadHeight(leadHeight, vdypPrimarySpecies.getGenus(), bec.getRegion(), tphTotal);
		} else if (iPass == 1) {
			primaryHeight = estimationMethods
					.primaryHeightFromLeadHeightInitial(leadHeight, vdypPrimarySpecies.getGenus(), bec.getRegion());
		} else {
			primaryHeight = estimationMethods.primaryHeightFromLeadHeight(
					leadHeight, vdypPrimarySpecies.getGenus(), bec.getRegion(),
					vdypPrimarySpecies.getTreesPerHectareByUtilization().getAll()
			);
		}
		vdypPrimarySpecies.getLoreyHeightByUtilization().setAll(primaryHeight);

		// Estimate lorey height for non-primary species
		for (var vspec : vdypSpecies.values()) {
			if (vspec == vdypPrimarySpecies)
				continue;

			// EMP053
			vspec.getLoreyHeightByUtilization().setAll(
					estimationMethods
							.estimateNonPrimaryLoreyHeight(vspec, vdypPrimarySpecies, bec, leadHeight, primaryHeight)
			);
		}
	}

	public static <T> List<T> utilizationArray(VdypLayer layer, Function<VdypUtilizationHolder, T> accessor) {
		return Stream.concat(Stream.of(layer), layer.getSpecies().values().stream()).map(accessor).toList();
	}

	// ROOTF01
	void findRootsForDiameterAndBaseArea(VdypLayer result, FipLayerPrimary fipLayer, BecDefinition bec, int source)
			throws ProcessingException {

		var quadMeanDiameterTotal = result.getQuadraticMeanDiameterByUtilization().getAll(); // DQ_TOT
		var baseAreaTotal = result.getBaseAreaByUtilization().getAll(); // BA_TOT
		var treesPerHectareTotal = result.getTreesPerHectareByUtilization().getAll(); // TPH_TOT
		Map<String, Float> goal = new LinkedHashMap<>(); // GOAL
		Map<String, Float> xMap = new LinkedHashMap<>(); // X

		float treesPerHectareSum;

		assert result.getSpecies().size() > 0;

		if (result.getSpecies().size() == 1) {
			var spec = result.getSpecies().values().iterator().next();
			for (var accessors : NON_VOLUME_UTILIZATION_VECTOR_ACCESSORS) {

				try {
					UtilizationVector specVector = (UtilizationVector) accessors.getReadMethod().invoke(spec);
					UtilizationVector layerVector = (UtilizationVector) accessors.getReadMethod().invoke(result);
					specVector.setAll(layerVector.getAll());
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					throw new IllegalStateException(e);
				}
			}

			result.getLoreyHeightByUtilization().setAll(spec.getLoreyHeightByUtilization().getAll());
			spec.setPercentGenus(100f);
			treesPerHectareSum = treesPerHectareTotal;
		} else {
			// Multiple Species
			for (var spec : result.getSpecies().values()) {

				var limits = estimationMethods.getLimitsForHeightAndDiameter(spec.getGenus(), bec.getRegion());

				final float maxHeightMultiplier = fipLayer.getPrimaryGenus()
						.orElseThrow(() -> new IllegalStateException("primaryGenus has not been set"))
						.equals(spec.getGenus()) ? 1.5f : 1.0f;
				final float heightMax = limits.maxLoreyHeight() * maxHeightMultiplier;

				spec.getLoreyHeightByUtilization().scalarInPlace(UtilizationClass.ALL, x -> min(x, heightMax));
			}
			ToDoubleFunction<VdypSpecies> accessor;

			switch (source) {
			case 1:
				accessor = x -> x.getPercentGenus();
				break;
			case 2:
				accessor = x -> x.getPercentGenus() / x.getLoreyHeightByUtilization().getAll();
				break;
			case 3:
				accessor = x -> x.getBaseAreaByUtilization().getAll();
				break;
			default:
				throw new UnsupportedOperationException("Unknown source for root finding " + source);
			}

			var sumSourceArea = result.getSpecies().values().stream().mapToDouble(accessor).sum();

			// FRJ
			var fractionMap = result.getSpecies().values().stream().collect(
					Collectors.toMap(
							VdypSpecies::getGenus, spec -> (float) (accessor.applyAsDouble(spec) / sumSourceArea)
					)
			);

			// HL_TOT
			float loreyHeightTotal = (float) fractionMap.entrySet().stream().mapToDouble(
					e -> e.getValue() * result.getSpecies().get(e.getKey()).getLoreyHeightByUtilization().getCoe(0)
			).sum();
			// FRJ(ISP) = FRJ(J) // We aren't using the remapping between global species
			// index and index for the species within the layer, so we can probably assign
			// directly to the fraction attribute on the species object.
			fractionMap.entrySet().forEach(e -> result.getSpecies().get(e.getKey()).setFractionGenus(e.getValue()));

			double[] quadMeanDiameterBase = new double[result.getSpecies().size()]; // DQspbase

			{
				int i = 0;
				for (var spec : result.getSpecies().values()) {

					// EMP061
					var limits = estimationMethods.getLimitsForHeightAndDiameter(spec.getGenus(), bec.getRegion());

					var dqMin = limits.minDiameterHeight() * spec.getLoreyHeightByUtilization().getAll();
					var dqMax = max(
							limits.maxQuadMeanDiameter(),
							limits.maxDiameterHeight() * spec.getLoreyHeightByUtilization().getAll()
					);

					// EMP060
					float quadMeanDiameter = clamp(
							estimationMethods.estimateQuadMeanDiameterForSpecies(
									spec, result.getSpecies(), bec.getRegion(), quadMeanDiameterTotal, baseAreaTotal,
									treesPerHectareTotal, loreyHeightTotal
							), //
							dqMin, dqMax
					);

					quadMeanDiameterBase[i++] = quadMeanDiameter;
				}
			}
			// VDYP7 checks the number of species here, but this is already inside a branch
			// that must be more than 1
			// Fill in target and trial values

			Utils.eachButLast(result.getSpecies().values(), spec -> {
				goal.put(spec.getGenus(), spec.getPercentGenus());
				xMap.put(spec.getGenus(), spec.getPercentGenus());
			}, spec -> {
				goal.put(spec.getGenus(), quadMeanDiameterTotal);
				xMap.put(spec.getGenus(), 0f);
			});

			var xVec = xMap.values().stream().mapToDouble(v -> (double) v).toArray();
			var goalVec = goal.values().stream().mapToDouble(v -> (double) v).toArray();

			// SNQSOL
			var rootVec = this.findRoot(quadMeanDiameterBase, goalVec, xVec, result, TOLERANCE);

			var rootMap = new LinkedHashMap<String, Float>();
			{
				float percentSum = 0;
				var it = result.getSpecies().values().iterator();
				for (int i = 0; it.hasNext(); i++) {
					var spec = it.next();
					rootMap.put(spec.getGenus(), (float) rootVec.getEntry(i));
					if (it.hasNext()) {
						spec.setPercentGenus((float) rootVec.getEntry(i));
						percentSum += rootVec.getEntry(i);
					} else {
						spec.setPercentGenus(100 - percentSum);
					}
				}
			}

			float loreyHeightSum = 0;
			treesPerHectareSum = 0;

			{
				int i = 0;
				for (var spec : result.getSpecies().values()) {
					float dqBase = (float) quadMeanDiameterBase[i++];
					float dq = 7.5f + (dqBase - 7.5f) * exp((float) rootVec.getEntry(rootVec.getDimension() - 1) / 20f);
					assert dq >= 0;
					float ba = baseAreaTotal * spec.getPercentGenus() / 100f;
					assert ba >= 0;
					float tph = BaseAreaTreeDensityDiameter.treesPerHectare(ba, dq);
					assert tph >= 0;
					spec.getQuadraticMeanDiameterByUtilization().setAll(dq);
					spec.getBaseAreaByUtilization().setAll(ba);
					spec.getTreesPerHectareByUtilization().setAll(tph);
					treesPerHectareSum += tph;
					loreyHeightSum += spec.getLoreyHeightByUtilization().getAll() * ba;
				}
			}
			result.getLoreyHeightByUtilization().setAll(loreyHeightSum / baseAreaTotal);

		} // end of Multiple Species branch

		var volumeSum = 0f;

		for (var spec : result.getSpecies().values()) {
			// EMP090
			var wholeStemVolume = spec.getTreesPerHectareByUtilization().getAll()
					* EstimationMethods.estimateWholeStemVolumePerTree(
							controlMap, spec.getVolumeGroup(), spec.getLoreyHeightByUtilization().getAll(),
							spec.getQuadraticMeanDiameterByUtilization().getAll()
					);
			spec.getWholeStemVolumeByUtilization().setAll(wholeStemVolume);
			volumeSum += wholeStemVolume;
		}

		result.getWholeStemVolumeByUtilization().setAll(volumeSum);
		var treesPerHectareStart = result.getTreesPerHectareByUtilization().getAll();
		result.getTreesPerHectareByUtilization().setAll(treesPerHectareSum);
		result.getQuadraticMeanDiameterByUtilization().setAll(
				BaseAreaTreeDensityDiameter.quadMeanDiameter(
						result.getBaseAreaByUtilization().getAll(), result.getTreesPerHectareByUtilization().getAll()
				)
		);

		if (abs(treesPerHectareStart / result.getTreesPerHectareByUtilization().getAll() - 1f) > 0.002) {
			throw new ProcessingException("TODO");
		}

		if (result.getSpecies().size() > 1) {
			for (var spec : result.getSpecies().values()) {
				if (spec.getWholeStemVolumeByUtilization().getAll() / volumeSum - goal.get(spec.getGenus()) > 0.1) {
					throw new ProcessingException("TODO");
				}
			}
		}
	}

	VdypLayer processLayerAsVeteran(FipPolygon fipPolygon, FipLayer fipLayer) throws ProcessingException {

		var polygonIdentifier = fipLayer.getPolygonIdentifier();

		assert fipLayer.getLayerType().equals(LayerType.VETERAN) : "Layer must be VETERAN";
		assert fipPolygon.getPolygonIdentifier().equals(fipLayer.getPolygonIdentifier()) : String.format(
				"Polygon polygonIdentifier '%s' doesn't match that of layer '%s'", fipPolygon.getPolygonIdentifier(),
				fipLayer.getPolygonIdentifier()
		);

		var layer = LayerType.VETERAN;

		// find Primary genus (highest percentage) ISPPVET

		var primaryGenus = fipLayer.getSpecies().values().stream() //
				.max(Utils.compareUsing(FipSpecies::getPercentGenus)) //
				.orElseThrow(() -> new IllegalStateException("No primarty genus (SP0) found. This should not happen."))
				.getGenus();

		// ageTotal copy, LVCOM3/AGETOTLV copied from FIPL_V/AGETOT_LV
		var ageTotal = fipLayer.getAgeTotal();

		// yearsToBreastHeight copy, minimum 6.0, LVCOM3/YTBHLV copied from
		// FIPL_V/YTBH_L
		var yearsToBreastHeight = Math.max(fipLayer.getYearsToBreastHeight().orElse(0f), 6.0f);

		// height? copy LVCOM3/HDLV = FIPL_V/HT_LV
		var height = fipLayer.getHeight().orElse(0f);

		var crownClosure = fipLayer.getCrownClosure();

		var becId = fipPolygon.getBiogeoclimaticZone();
		var bec = Utils.getBec(becId, controlMap);
		var region = bec.getRegion();

		// Call EMP098 to get Veteran Basal Area, store in LVCOM1/BA array at positions
		// 0,0 and 0,4
		var estimatedBaseArea = estimateVeteranBaseArea(height, crownClosure, primaryGenus, region);
		var baseAreaByUtilization = Utils.utilizationVector(estimatedBaseArea);
		// Copy over Species entries.
		// LVCOM/ISPLV=ISPV
		// LVCOM4/SP0LV=FIPSA/SP0V
		// LVCOM4/SP64DISTLV=FIPSA/VDISTRV
		// LVCOM1/PCLTV=FIPS/PCTVOLV
		// LVCOM1/HL=FIPL_V/HT_LV
		var vdypSpecies = fipLayer.getSpecies().values().stream() //
				.map(fipSpec -> {
					var vs = VdypSpecies.build(sb -> {
						sb.adapt(fipSpec);
						applyGroups(bec, fipSpec.getGenus(), sb);
						sb.adaptSiteFrom(fipSpec, (ib, fipSite) -> {
							ib.ageTotal(ageTotal);
							ib.yearsToBreastHeight(yearsToBreastHeight);
						});
					});
					vs.setLoreyHeightByUtilization(new UtilizationVector(0f, height));
					return vs;
				}) //
				.collect(Collectors.toMap(VdypSpecies::getGenus, Function.identity()));

		/*
		 * From VDYP7
		 *
		 * At this point we SHOULD invoke a root finding procedure sets species percents and adjusts DQ by species.
		 * fills in main components, through whole-stem volume INSTEAD, I will assume %volumes apply to % BA's
		 */

		for (var vSpec : vdypSpecies.values()) {
			vSpec.getBaseAreaByUtilization()
					.setLarge(baseAreaByUtilization.getLarge() * vSpec.getPercentGenus() / 100f);
		}

		var vetDqMap = Utils.<MatrixMap2<String, Region, Coefficients>>expectParsedControl(
				controlMap, ControlKey.VETERAN_LAYER_DQ, MatrixMap2.class
		);

		for (var vSpec : vdypSpecies.values()) {
			var genus = vSpec.getGenus();
			var coe = vetDqMap.get(genus, region);
			var a0 = coe.getCoe(1);
			var a1 = coe.getCoe(2);
			var a2 = coe.getCoe(3);
			float hl = vSpec.getLoreyHeightByUtilization().getCoe(0);
			float dq = max(a0 + a1 * pow(hl, a2), 22.5f);
			vSpec.getQuadraticMeanDiameterByUtilization().setLarge(dq);
			vSpec.getTreesPerHectareByUtilization().setLarge(
					BaseAreaTreeDensityDiameter.treesPerHectare(vSpec.getBaseAreaByUtilization().getLarge(), dq)
			);
		}

		var vdypLayer = VdypLayer.build(builder -> {
			builder.polygonIdentifier(polygonIdentifier);
			builder.layerType(layer);

			builder.addSpecies(vdypSpecies.values());
		});

		vdypLayer.setBaseAreaByUtilization(baseAreaByUtilization);

		computeUtilizationComponentsVeteran(vdypLayer, bec);

		return vdypLayer;
	}

	// YUCV
	private void computeUtilizationComponentsVeteran(VdypLayer vdypLayer, BecDefinition bec)
			throws ProcessingException {
		log.trace(
				"computeUtilizationComponentsVeterany for {}, stand total age is {}", vdypLayer.getPolygonIdentifier(),
				vdypLayer.getAgeTotal()
		);

		var volumeAdjustMap = Utils.<Map<String, Coefficients>>expectParsedControl(
				controlMap, ControlKey.VETERAN_LAYER_VOLUME_ADJUST, Map.class
		);
		try {
			for (var vdypSpecies : vdypLayer.getSpecies().values()) {

				var treesPerHectareUtil = Utils.utilizationVector();
				var quadMeanDiameterUtil = Utils.utilizationVector();
				var baseAreaUtil = Utils.utilizationVector();
				var wholeStemVolumeUtil = Utils.utilizationVector();

				var closeUtilizationVolumeUtil = Utils.utilizationVector();
				var closeUtilizationNetOfDecayUtil = Utils.utilizationVector();
				var closeUtilizationNetOfDecayAndWasteUtil = Utils.utilizationVector();
				var closeUtilizationNetOfDecayWasteAndBreakageUtil = Utils.utilizationVector();

				var hlSp = vdypSpecies.getLoreyHeightByUtilization().getAll();
				{
					var baSp = vdypSpecies.getBaseAreaByUtilization().getLarge();
					var tphSp = vdypSpecies.getTreesPerHectareByUtilization().getLarge();
					var dqSp = vdypSpecies.getQuadraticMeanDiameterByUtilization().getLarge();

					treesPerHectareUtil.setAll(tphSp);
					quadMeanDiameterUtil.setAll(dqSp);
					baseAreaUtil.setAll(baSp);
					wholeStemVolumeUtil.setAll(0f);

					treesPerHectareUtil.setLarge(tphSp);
					quadMeanDiameterUtil.setLarge(dqSp);
					baseAreaUtil.setLarge(baSp);
					wholeStemVolumeUtil.setLarge(0f);
				}
				// AADJUSTV
				var volumeAdjustCoe = volumeAdjustMap.get(vdypSpecies.getGenus());

				var utilizationClass = UtilizationClass.OVER225; // IUC_VET

				// ADJ
				var adjust = new Coefficients(new float[] { 0f, 0f, 0f, 0f }, 1);

				// EMP091
				EstimationMethods.estimateWholeStemVolume(
						controlMap, utilizationClass, volumeAdjustCoe.getCoe(1), vdypSpecies.getVolumeGroup(), hlSp,
						quadMeanDiameterUtil, baseAreaUtil, wholeStemVolumeUtil
				);

				adjust.setCoe(4, volumeAdjustCoe.getCoe(2));
				// EMP092
				EstimationMethods.estimateCloseUtilizationVolume(
						controlMap, utilizationClass, adjust, vdypSpecies.getVolumeGroup(), hlSp, quadMeanDiameterUtil,
						wholeStemVolumeUtil, closeUtilizationVolumeUtil
				);

				adjust.setCoe(4, volumeAdjustCoe.getCoe(3));
				// EMP093
				EstimationMethods.estimateNetDecayVolume(
						controlMap, vdypSpecies.getGenus(), bec.getRegion(), utilizationClass, adjust,
						vdypSpecies.getDecayGroup(), vdypLayer.getBreastHeightAge().orElse(0f), quadMeanDiameterUtil,
						closeUtilizationVolumeUtil, closeUtilizationNetOfDecayUtil
				);

				adjust.setCoe(4, volumeAdjustCoe.getCoe(4));
				// EMP094
				final var netDecayCoeMap = Utils.<Map<String, Coefficients>>expectParsedControl(
						controlMap, ControlKey.VOLUME_NET_DECAY_WASTE, Map.class
				);
				final var wasteModifierMap = Utils.<MatrixMap2<String, Region, Float>>expectParsedControl(
						controlMap, ControlKey.WASTE_MODIFIERS, MatrixMap2.class
				);
				EstimationMethods.estimateNetDecayAndWasteVolume(
						bec.getRegion(), utilizationClass, adjust, vdypSpecies.getGenus(), hlSp, netDecayCoeMap,
						wasteModifierMap, quadMeanDiameterUtil, closeUtilizationVolumeUtil,
						closeUtilizationNetOfDecayUtil, closeUtilizationNetOfDecayAndWasteUtil
				);

				if (getId().isStart()) {
					// EMP095
					EstimationMethods.estimateNetDecayWasteAndBreakageVolume(
							controlMap, utilizationClass, vdypSpecies.getBreakageGroup(), quadMeanDiameterUtil,
							closeUtilizationVolumeUtil, closeUtilizationNetOfDecayAndWasteUtil,
							closeUtilizationNetOfDecayWasteAndBreakageUtil
					);
				}

				vdypSpecies.setBaseAreaByUtilization(baseAreaUtil);
				vdypSpecies.setTreesPerHectareByUtilization(treesPerHectareUtil);
				vdypSpecies.setQuadraticMeanDiameterByUtilization(quadMeanDiameterUtil);
				vdypSpecies.setWholeStemVolumeByUtilization(wholeStemVolumeUtil);
				vdypSpecies.setCloseUtilizationVolumeByUtilization(closeUtilizationVolumeUtil);
				vdypSpecies.setCloseUtilizationVolumeNetOfDecayByUtilization(closeUtilizationNetOfDecayUtil);
				vdypSpecies.setCloseUtilizationVolumeNetOfDecayAndWasteByUtilization(
						closeUtilizationNetOfDecayAndWasteUtil
				);
				vdypSpecies.setCloseUtilizationVolumeNetOfDecayWasteAndBreakageByUtilization(
						closeUtilizationNetOfDecayWasteAndBreakageUtil
				);

				for (var accessors : UTILIZATION_VECTOR_ACCESSORS) {
					UtilizationVector utilVector = (UtilizationVector) accessors.getReadMethod().invoke(vdypSpecies);

					// Set all components other than 4 to 0.0
					for (var uc : UtilizationClass.ALL_BUT_LARGEST) {
						utilVector.set(uc, 0f);
					}

					// Set component 0 to equal component 4.
					utilVector.setAll(utilVector.getLarge());

					accessors.getWriteMethod().invoke(vdypSpecies, utilVector);
				}
			}

			computeLayerUtilizationComponentsFromSpecies(vdypLayer);

		} catch (IllegalAccessException | InvocationTargetException ex) {
			throw new IllegalStateException(ex);
		}
	}

	// FIP_GET
	protected FipPolygon getPolygon(
			StreamingParser<FipPolygon> polyStream, StreamingParser<Map<LayerType, FipLayer>> layerStream,
			StreamingParser<Collection<FipSpecies>> speciesStream
	) throws ProcessingException, IOException, ResourceParseException {

		log.trace("Getting polygon");
		var polygon = polyStream.next();

		log.trace("Getting layers for polygon {}", polygon.getPolygonIdentifier());
		Map<LayerType, FipLayer> layers;
		try {
			layers = layerStream.next();
		} catch (NoSuchElementException ex) {
			throw validationError("Layers file has fewer records than polygon file.", ex);
		}

		log.trace("Getting species for polygon {}", polygon.getPolygonIdentifier());
		Collection<FipSpecies> species;
		try {
			species = speciesStream.next();
		} catch (NoSuchElementException ex) {
			throw validationError("Species file has fewer records than polygon file.", ex);
		}

		var speciesPerLayerMap = new HashMap<LayerType, Map<String, FipSpecies>>();

		// Validate that layers belong to the correct polygon
		for (var layer : layers.values()) {
			if (!layer.getPolygonIdentifier().equals(polygon.getPolygonIdentifier())) {
				throw validationError(
						"Record in layer file contains layer for polygon %s when expecting one for %s.",
						layer.getPolygonIdentifier(), polygon.getPolygonIdentifier()
				);
			}
			speciesPerLayerMap.put(layer.getLayerType(), new HashMap<>());
		}

		for (var spec : species) {
			var layer = layers.get(spec.getLayerType());
			// Validate that species belong to the correct polygon
			if (!spec.getPolygonIdentifier().equals(polygon.getPolygonIdentifier())) {
				throw validationError(
						"Record in species file contains species for polygon %s when expecting one for %s.",
						layer.getPolygonIdentifier(), polygon.getPolygonIdentifier()
				);
			}
			if (Objects.isNull(layer)) {
				throw validationError(
						"Species entry references layer %s of polygon %s but it is not present.", layer,
						polygon.getPolygonIdentifier()
				);
			}
			speciesPerLayerMap.get(spec.getLayerType()).put(spec.getGenus(), spec);
		}

		for (var entry : speciesPerLayerMap.entrySet()) {
			layers.get(entry.getKey()).setSpecies(entry.getValue());
		}

		polygon.setLayers(layers);

		return polygon;
	}

	private Optional<Float> heightMinimum(LayerType layer) {
		var minima = Utils.<Map<String, Float>>expectParsedControl(controlMap, ControlKey.MINIMA.name(), Map.class);
		switch (layer) {
		case PRIMARY:
			return Optional.of(minima.get(BaseControlParser.MINIMUM_HEIGHT));
		case VETERAN:
			return Optional.of(minima.get(BaseControlParser.MINIMUM_VETERAN_HEIGHT));
		default:
			return Optional.empty();
		}
	}

	// FIP_CHK
	void checkPolygon(FipPolygon polygon) throws ProcessingException {

		// Fortran did debug logging when a polygon is found to be invalid. Attaching
		// messages to exceptions fills that need.

		// TODO finding all the things that are wrong rather than failing on just the
		// first would be a good idea.

		var primaryLayer = requireLayer(polygon, LayerType.PRIMARY);

		// FIXME VDYP7 actually tests if total age - YTBH is less than 0.5 but gives an
		// error that total age is "less than" YTBH. Replicating that for now but
		// consider changing it.

		if (primaryLayer.getAgeTotal().orElse(0f) - primaryLayer.getYearsToBreastHeight().orElse(0f) < 0.5f) {
			var ageTotal = primaryLayer.getAgeTotal().map(Object::toString).orElse("N/A (0)");
			var ytbh = primaryLayer.getYearsToBreastHeight().map(Object::toString).orElse("N/A (0)");
			throw validationError(
					"Polygon %s has %s layer where total age (%s) is less than YTBH (%s).",
					polygon.getPolygonIdentifier(), LayerType.PRIMARY, ageTotal, ytbh
			);
		}

		// TODO This is the only validation step done to non-primary layers, VDYP7 had a
		// less well defined idea of a layer being present or not and so it may have
		// skipped validating other layers rather than validating them conditionally on
		// being present. Consider extending validation of other properties to other
		// layers.

		for (FipLayer layer : polygon.getLayers().values()) {
			var height = layer.getHeight().orElse(0f);

			throwIfPresent(
					heightMinimum(layer.getLayerType()).filter(minimum -> height < minimum).map(
							minimum -> validationError(
									"Polygon %s has %s layer where height %.1f is less than minimum %.1f.",
									polygon.getPolygonIdentifier(), layer.getLayerType(), layer.getHeightSafe(), minimum
							)
					)
			);
		}

		if (polygon.getMode().map(x -> x == PolygonMode.YOUNG).orElse(false)) {
			throw validationError(
					"Polygon %s is using unsupported mode %s.", polygon.getPolygonIdentifier(), PolygonMode.YOUNG
			);
		}

		if (primaryLayer.getYearsToBreastHeight().orElse(0f) < 0.5) {
			throw validationError(
					"Polygon %s has %s layer where years to breast height %.1f is less than minimum %.1f years.",
					polygon.getPolygonIdentifier(), LayerType.PRIMARY, primaryLayer.getYearsToBreastHeightSafe(), 0.5f
			);
		}

		if (primaryLayer.getSiteIndex().orElse(0f) < 0.5) {
			throw validationError(
					"Polygon %s has %s layer where site index %s is less than minimum %.1f years.",
					polygon.getPolygonIdentifier(), LayerType.PRIMARY,
					primaryLayer.getSiteIndex().map(x -> String.format("%.1f", x)).orElse("N/A"), 0.5f
			);
		}

		for (FipLayer layer : polygon.getLayers().values()) {
			var percentTotal = getPercentTotal(layer);
			// VDYP7 performs this step which should be negligible but might have a small
			// impact due to the 0.01 percent variation and floating point errors.
			if (layer.getLayerType() == LayerType.PRIMARY) {
				layer.getSpecies().values()
						.forEach(species -> species.setFractionGenus(species.getPercentGenus() / percentTotal));
			}
		}

	}

	// EMP098
	float estimateVeteranBaseArea(float height, float crownClosure, String genus, Region region) {
		var coefficients = Utils.<MatrixMap2<String, Region, Coefficients>>expectParsedControl(
				controlMap, ControlKey.VETERAN_BQ, MatrixMap2.class
		).getM(genus, region);

		// mismatched index is copied from VDYP7
		float a0 = coefficients.getCoe(1);
		float a1 = coefficients.getCoe(2);
		float a2 = coefficients.getCoe(3);

		float baseArea = a0 * pow(max(height - a1, 0.0f), a2);

		baseArea *= crownClosure / 4.0f;

		baseArea = max(baseArea, 0.01f);

		return baseArea;
	}

	/**
	 * estimate mean volume per tree For a species, for trees with dbh >= 7.5 CM Using eqn in jf117.doc
	 *
	 * @param volumeGroup
	 * @param loreyHeight
	 * @param quadMeanDiameter
	 * @return
	 */
	public float estimateMeanVolume(int volumeGroup, float loreyHeight, float quadMeanDiameter) {
		var coeMap = Utils.<Map<Integer, Coefficients>>expectParsedControl(
				controlMap, ControlKey.TOTAL_STAND_WHOLE_STEM_VOL, Map.class
		);

		var coe = coeMap.get(volumeGroup);

		if (coe == null) {
			throw new IllegalArgumentException("Coefficients not found for volume group " + volumeGroup);
		}

		float lvMean = //
				coe.getCoe(0) + //
						coe.getCoe(1) * log(quadMeanDiameter) + //
						coe.getCoe(2) * log(loreyHeight) + //
						coe.getCoe(3) * quadMeanDiameter + //
						coe.getCoe(4) / quadMeanDiameter + //
						coe.getCoe(5) * loreyHeight + //
						coe.getCoe(6) * quadMeanDiameter * quadMeanDiameter + //
						coe.getCoe(7) * quadMeanDiameter * loreyHeight + //
						coe.getCoe(8) * loreyHeight / quadMeanDiameter;

		return exp(lvMean);
	}

	double[] rootFinderFunction(double[] point, VdypLayer layer, double[] diameterBase) {

		var percentL1 = new double[point.length];
		double percentSum = 0;
		if (point.length > 1) {
			for (int i = 0; i < point.length - 1; i++) {
				percentL1[i] = point[i];
				percentSum += point[i];
			}
		}
		percentL1[point.length - 1] = 100d - percentSum;

		double volumeSum = 0d;
		double treesPerHectareSum = 0d;

		final var layerBa = layer.getBaseAreaByUtilization().getAll();

		// Iterate over the fixed order list with an index
		{
			var it = layer.getSpecies().entrySet().iterator();
			for (int j = 0; it.hasNext(); j++) {
				var spec = it.next().getValue();

				// These side effects are evil but that's how VDYP7 works.

				final float quadMeanDiameter = (float) (7.5
						+ (diameterBase[j] - 7.5) * FastMath.exp(point[point.length - 1] / 20d));
				spec.getQuadraticMeanDiameterByUtilization().setAll(quadMeanDiameter);

				final float baseArea = (float) (layerBa * percentL1[j] / 100d);
				spec.getBaseAreaByUtilization().setAll(baseArea);

				final float tph = BaseAreaTreeDensityDiameter.treesPerHectare(baseArea, quadMeanDiameter);
				spec.getTreesPerHectareByUtilization().setAll(tph);
				treesPerHectareSum += tph;

				final float loreyHeight = spec.getLoreyHeightByUtilization().getAll();

				final float meanVolume = estimateMeanVolume(spec.getVolumeGroup(), loreyHeight, quadMeanDiameter);
				final float wholeStemVolume = tph * meanVolume;

				spec.getWholeStemVolumeByUtilization().setAll(wholeStemVolume);
				volumeSum += wholeStemVolume;
			}
		}

		double dqFinal = BaseAreaTreeDensityDiameter
				.quadMeanDiameter(layer.getBaseAreaByUtilization().getAll(), (float) treesPerHectareSum);

		var y = new double[point.length];

		if (layer.getSpecies().size() > 1) {
			var it = layer.getSpecies().values().iterator();
			for (int i = 0; it.hasNext(); i++) {
				var spec = it.next();

				y[i] = 100d * spec.getWholeStemVolumeByUtilization().getAll() / volumeSum;
			}
		}
		y[y.length - 1] = dqFinal;
		return y;
	}

	@Override
	protected ValueOrMarker<Float, Boolean>
			isVeteranForEstimatePercentForestLand(FipPolygon polygon, Optional<FipLayer> vetLayer) {
		if (polygon.getMode().map(mode -> mode == PolygonMode.YOUNG).orElse(false)) {
			return FLOAT_OR_BOOL.value(100f);
		}
		return super.isVeteranForEstimatePercentForestLand(polygon, vetLayer);
	}

	/**
	 * Estimate the Jacobian Matrix of a function using forward difference
	 *
	 * @param x
	 * @param func
	 * @return
	 */
	double[][] estimateJacobian(double[] x, MultivariateVectorFunction func) {
		return estimateJacobian(x, func.value(x), func);
	}

	/**
	 * Estimate the Jacobian Matrix of a function using forward difference
	 *
	 * @param x
	 * @param y
	 * @param func
	 * @return
	 */
	double[][] estimateJacobian(double[] x, double[] y, MultivariateVectorFunction func) {
		// TODO
		final double machineEpsilon = 2.22e-16;
		final double functionEpsilon = 1.19e-07;

		double epsilon = FastMath.sqrt(FastMath.max(functionEpsilon, machineEpsilon));

		double[] x2 = Arrays.copyOf(x, x.length);

		double[][] result = new double[x.length][x.length];

		for (int j = 0; j < x.length; j++) {
			double temp = x[j];
			double h = epsilon * FastMath.abs(temp);
			if (h == 0) {
				h = epsilon;
			}
			x2[j] = temp + h;
			double[] y2 = func.value(x2);
			x2[j] = temp;
			for (int i = 0; i < x.length; i++) {
				result[i][j] = (y2[i] - y[i]) / h;
			}
		}
		return result;
	}

	RealMatrix identityMatrix(int n) {
		var diag = new double[n];
		Arrays.fill(diag, n);
		return new DiagonalMatrix(diag);

	}

	RealVector findRoot(double[] diameterBase, double[] goal, double[] x, VdypLayer layer, double tolerance) {
		MultivariateVectorFunction func = point -> rootFinderFunction(point, layer, diameterBase);

		MultivariateMatrixFunction jacFunc = point -> estimateJacobian(point, func);

		LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();

		optimizer.withCostRelativeTolerance(tolerance); // Not sure if this is the right tolerance

		LeastSquaresProblem leastSquaresProblem = LeastSquaresFactory.create(
				func, //
				jacFunc, //
				goal, //
				x, //
				identityMatrix(x.length), //
				null, //
				200, //
				1000 //
		);

		var result = optimizer.optimize(leastSquaresProblem);

		return result.getPoint();
	}

	@Override
	public VdypApplicationIdentifier getId() {
		return VdypApplicationIdentifier.FIP_START;
	}

	@Override
	protected BaseControlParser getControlFileParser() {
		return new FipControlParser();
	}

	@Override
	protected FipSpecies
			copySpecies(FipSpecies toCopy, Consumer<BaseVdypSpecies.Builder<FipSpecies, FipSite, ?>> config) {
		return FipSpecies.build(builder -> {
			builder.copy(toCopy);
		});
	}

	@Override
	protected Optional<FipSite> getPrimarySite(FipLayer layer) {
		return layer.getSite();
	}

	@Override
	protected float getYieldFactor(FipPolygon polygon) {
		return polygon.getYieldFactor();
		// TODO Make an InputPolygon interface that has this.
	}

}
