package ca.bc.gov.nrs.vdyp.forward;

import static ca.bc.gov.nrs.vdyp.forward.ForwardPass.PASS_1;
import static ca.bc.gov.nrs.vdyp.forward.ForwardPass.PASS_2;
import static ca.bc.gov.nrs.vdyp.forward.ForwardPass.PASS_3;
import static ca.bc.gov.nrs.vdyp.forward.ForwardPass.PASS_4;
import static ca.bc.gov.nrs.vdyp.forward.ForwardPass.PASS_5;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.LogManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.bc.gov.nrs.vdyp.application.VdypApplication;
import ca.bc.gov.nrs.vdyp.application.VdypApplicationIdentifier;
import ca.bc.gov.nrs.vdyp.io.FileSystemFileResolver;

public class VdypForwardApplication extends VdypApplication {

	static {
		try {
			LogManager.getLogManager().readConfiguration(
					ForwardProcessor.class.getClassLoader().getResourceAsStream("logging.properties")
			);
		} catch (SecurityException | IOException e) {
			System.err.println("Unable to configure logging system");
		}
	}

	private static final Logger logger = LoggerFactory.getLogger(VdypForwardApplication.class);

	public static final int CONFIG_LOAD_ERROR = 1; // TODO check what Fortran VDYP Forward would exit with.
	public static final int PROCESSING_ERROR = 2; // TODO check what Fortran VDYP Forward would exit with.

	public static final String DEFAULT_VDYP_CONTROL_FILE_NAME = "vdyp.ctr";

	private static Set<ForwardPass> vdypPassSet = new HashSet<>(Arrays.asList(PASS_1, PASS_2, PASS_3, PASS_4, PASS_5));

	@SuppressWarnings("java:S106")
	public static void main(final String... args) {

		var app = new VdypForwardApplication();

		app.logVersionInformation();

		List<String> controlFileNames = null;

		try {
			if (args.length == 0) {
				System.out.print("Enter name of VDYP control file (or RETURN for vdyp.ctr) or *name for both): ");

				controlFileNames = new ArrayList<>();
				try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
					String userResponse = br.readLine();
					if (userResponse.length() == 0) {
						controlFileNames.add(DEFAULT_VDYP_CONTROL_FILE_NAME);
					} else if (userResponse.startsWith("*")) {
						controlFileNames.add(DEFAULT_VDYP_CONTROL_FILE_NAME);

						userResponse = userResponse.substring(1);
						controlFileNames.addAll(Arrays.asList(userResponse.split("[[:space:]]+")));
					}
				}
			} else {
				controlFileNames = Arrays.asList(args);
			}
		} catch (Exception ex) {
			logger.error("Error during initialization", ex);
			System.exit(CONFIG_LOAD_ERROR);
		}

		try {
			ForwardProcessor processor = new ForwardProcessor();

			processor.run(new FileSystemFileResolver(), controlFileNames, vdypPassSet);

		} catch (Exception ex) {
			logger.error("Error during processing", ex);
			System.exit(PROCESSING_ERROR);
		}
	}

	private void logVersionInformation() {
		logger.info("{} {}", RESOURCE_SHORT_VERSION, RESOURCE_VERSION_DATE);
		logger.info("{} Ver:{} {}", RESOURCE_BINARY_NAME, RESOURCE_SHORT_VERSION, RESOURCE_VERSION_DATE);
		logger.info("VDYP7 Support Ver: {}", AVERSION);
	}

	@Override
	public VdypApplicationIdentifier getId() {
		return VdypApplicationIdentifier.VDYP_FORWARD;
	}

	// @formatter:off

	//	public static final float PI_40K = 0.78539816E-04f;
	//
	// 	static final Set<String> HARDWOODS = Set.of("AC", "AT", "D", "E", "MB");
	//
	//	static final Collection<UtilizationClass> UTIL_CLASSES = List.of(
	//			UtilizationClass.U75TO125, UtilizationClass.U125TO175, UtilizationClass.U175TO225, UtilizationClass.OVER225
	//	);
	//
	//	static final Map<String, Integer> ITG_PURE = Utils.constMap(map -> {
	//		map.put("AC", 36);
	//		map.put("AT", 42);
	//		map.put("B", 18);
	//		map.put("C", 9);
	//		map.put("D", 38);
	//		map.put("E", 40);
	//		map.put("F", 1);
	//		map.put("H", 12);
	//		map.put("L", 34);
	//		map.put("MB", 39);
	//		map.put("PA", 28);
	//		map.put("PL", 28);
	//		map.put("PW", 27);
	//		map.put("PY", 32);
	//		map.put("S", 21);
	//		map.put("Y", 9);
	//	});
	//
	//	private static final Comparator<FipSpecies> PERCENT_GENUS_DESCENDING = Utils
	//			.compareUsing(FipSpecies::getPercentGenus).reversed();
	//	public static final int UTIL_ALL = UtilizationClass.ALL.index;
	//	public static final int UTIL_LARGEST = UtilizationClass.OVER225.index;
	//	public static final int UTIL_SMALL = UtilizationClass.SMALL.index;
	//
	//	public static final float TOLERANCE = 2.0e-3f;
	//
	//	/**
	//	 * When finding primary species these genera should be combined
	//	 */
	//	static final Collection<Collection<String>> PRIMARY_SPECIES_TO_COMBINE = Arrays
	//			.asList(Arrays.asList("PL", "PA"), Arrays.asList("C", "Y"));
	//
	//	@SuppressWarnings("unchecked")
	//	private <T> StreamingParser<T> getStreamingParser(String key) throws ProcessingException {
	//		try {
	//			var factory = (StreamingParserFactory<T>) controlMap.get(key);
	//			if (factory == null) {
	//				throw new ProcessingException(String.format("Data file %s not specified in control map.", key));
	//			}
	//			return factory.get();
	//		} catch (IOException ex) {
	//			throw new ProcessingException("Error while opening data file.", ex);
	//		}
	//	}
	//
	//	void process() throws ProcessingException {
	//		try (
	//				var polyStream = this.<FipPolygon>getStreamingParser(FipPolygonParser.CONTROL_KEY);
	//				var layerStream = this.<Map<Layer, FipLayer>>getStreamingParser(FipLayerParser.CONTROL_KEY);
	//				var speciesStream = this.<Collection<FipSpecies>>getStreamingParser(FipSpeciesParser.CONTROL_KEY);
	//		) {
	//			log.atDebug().setMessage("Start Stand processing").log();
	//			int polygonsRead = 0;
	//			int polygonsWritten = 0;
	//
	//			while (polyStream.hasNext()) {
	//
	//				// FIP_GET
	//				log.atInfo().setMessage("Getting polygon {}").addArgument(polygonsRead + 1).log();
	//				var polygon = getPolygon(polyStream, layerStream, speciesStream);
	//				VdypPolygon resultPoly;
	//				try {
	//
	//					log.atInfo().setMessage("Read polygon {}, preparing to process")
	//							.addArgument(polygon.getPolygonIdentifier()).log();
	//
	//					// if (MODE .eq. -1) go to 100
	//
	//					final var mode = polygon.getModeFip().orElse(FipMode.DONT_PROCESS);
	//
	//					if (mode == FipMode.DONT_PROCESS) {
	//						log.atInfo().setMessage("Skipping polygon with mode {}").addArgument(mode).log();
	//						continue;
	//					}
	//
	//					// IP_IN = IP_IN+1
	//					// if (IP_IN .gt. MAXPOLY) go to 200
	//
	//					polygonsRead++; // Don't count polygons we aren't processing due to mode. This was the behavior
	//					// in VDYP7
	//
	//					// IPASS = 1
	//					// CALL FIP_CHK( IPASS, IER)
	//					// if (ier .gt. 0) go to 1000
	//					//
	//					// if (IPASS .le. 0) GO TO 120
	//
	//					log.atInfo().setMessage("Checking validity of polygon {}:{}").addArgument(polygonsRead)
	//							.addArgument(polygon.getPolygonIdentifier()).log();
	//					checkPolygon(polygon);
	//
	//					// CALL FIPCALCV( BAV, IER)
	//					// CALL FIPCALC1( BAV, BA_TOTL1, IER)
	//
	//					Map<Layer, VdypLayer> processedLayers = new EnumMap<>(Layer.class);
	//
	//					var fipLayers = polygon.getLayers();
	//					var fipVetLayer = Optional.ofNullable(fipLayers.get(Layer.VETERAN));
	//					Optional<VdypLayer> resultVetLayer;
	//					if (fipVetLayer.isPresent()) {
	//						resultVetLayer = Optional.of(processLayerAsVeteran(polygon, fipVetLayer.get()));
	//					} else {
	//						resultVetLayer = Optional.empty();
	//					}
	//					resultVetLayer.ifPresent(layer -> processedLayers.put(Layer.VETERAN, layer));
	//
	//					FipLayerPrimary fipPrimeLayer = (FipLayerPrimary) fipLayers.get(Layer.PRIMARY);
	//					assert fipPrimeLayer != null;
	//					var resultPrimeLayer = processLayerAsPrimary(
	//							polygon, fipPrimeLayer,
	//							resultVetLayer.map(VdypLayer::getBaseAreaByUtilization).map(coe -> coe.get(UTIL_ALL))
	//									.orElse(0f)
	//					);
	//					processedLayers.put(Layer.PRIMARY, resultPrimeLayer);
	//
	//					resultPoly = createVdypPolygon(polygon, processedLayers);
	//
	//					float baseAreaTotalPrime = resultPrimeLayer.getBaseAreaByUtilization().getCoe(UTIL_ALL); // BA_TOTL1
	//
	//					// if (FIPPASS(6) .eq. 0 .or. FIPPASS(6) .eq. 2) then
	//					if (true /* TODO */) {
	//						@SuppressWarnings("unchecked")
	//						var minima = (Map<String, Float>) controlMap.get(VDYPForwardControlParser.MINIMA);
	//						float minimumBaseArea = minima.get(VDYPForwardControlParser.MINIMUM_BASE_AREA);
	//						float minimumPredictedBaseArea = minima.get(VDYPForwardControlParser.MINIMUM_PREDICTED_BASE_AREA);
	//						if (baseAreaTotalPrime < minimumBaseArea) {
	//							throw new LowValueException("Base area", baseAreaTotalPrime, minimumBaseArea);
	//						}
	//						float predictedBaseArea = baseAreaTotalPrime * (100f / resultPoly.getPercentAvailable());
	//						if (predictedBaseArea < minimumPredictedBaseArea) {
	//							throw new LowValueException(
	//									"Predicted base area", predictedBaseArea, minimumPredictedBaseArea
	//							);
	//						}
	//					}
	//					BecDefinition bec = BecDefinitionParser.getBecs(controlMap).get(polygon.getBiogeoclimaticZone())
	//							.orElseThrow(
	//									() -> new ProcessingException("Missing Bec " + polygon.getBiogeoclimaticZone())
	//							);
	//					// FIPSTK
	//					adjustForStocking(resultPoly.getLayers().get(Layer.PRIMARY), fipPrimeLayer, bec);
	//				} catch (StandProcessingException ex) {
	//					// TODO include some sort of hook for different forms of user output
	//					// TODO Implement single stand mode that propagates the exception
	//
	//					log.atWarn().setMessage("Polygon {} bypassed").addArgument(polygon.getPolygonIdentifier())
	//							.setCause(ex);
	//				}
	//
	//			}
	//		} catch (IOException | ResourceParseException ex) {
	//			throw new ProcessingException("Error while reading or writing data.", ex);
	//		}
	//	}
	//
	//	// FIPSTK
	//	void adjustForStocking(VdypLayer vdypLayer, FipLayerPrimary fipLayerPrimary, BecDefinition bec) {
	//
	//		@SuppressWarnings("unchecked")
	//		var stockingClassMap = (MatrixMap2<Character, Region, Optional<StockingClassFactor>>) controlMap
	//				.get(StockingClassFactorParser.CONTROL_KEY);
	//
	//		Region region = bec.getRegion();
	//
	//		var factorEntry = fipLayerPrimary.getStockingClass()
	//				.flatMap(stockingClass -> stockingClassMap.get(stockingClass, region));
	//
	//		if (!factorEntry.isPresent()) {
	//			return;
	//		}
	//
	//		float factor = factorEntry.get().getFactor();
	//
	//		scaleAllSummableUtilization(vdypLayer, factor);
	//		vdypLayer.getSpecies().values().forEach(spec -> scaleAllSummableUtilization(spec, factor));
	//
	//		log.atInfo().addArgument(fipLayerPrimary.getStockingClass()).addArgument(factor).setMessage(
	//				"Foregoing Primary Layer has stocking class {} Yield values will be multiplied by {}  before being written to output file."
	//		);
	//	}
	//
	//	VdypPolygon createVdypPolygon(FipPolygon fipPolygon, Map<Layer, VdypLayer> processedLayers)
	//			throws ProcessingException {
	//		var fipVetLayer = fipPolygon.getLayers().get(Layer.VETERAN);
	//		var fipPrimaryLayer = (FipLayerPrimary) fipPolygon.getLayers().get(Layer.PRIMARY);
	//
	//		float percentAvailable = estimatePercentForestLand(fipPolygon, fipVetLayer, fipPrimaryLayer);
	//
	//		var vdypPolygon = new VdypPolygon(fipPolygon.getPolygonIdentifier(), percentAvailable);
	//		vdypPolygon.setLayers(processedLayers);
	//		return vdypPolygon;
	//	}
	//
	//	// FIPLAND
	//	float estimatePercentForestLand(FipPolygon fipPolygon, FipLayer fipVetLayer, FipLayerPrimary fipPrimaryLayer)
	//			throws ProcessingException {
	//		if (fipPolygon.getPercentAvailable().isPresent()) {
	//			return fipPolygon.getPercentAvailable().get();
	//		} else {
	//
	//			boolean veteran = fipVetLayer != null && fipVetLayer.getHeight() > 0f && fipVetLayer.getCrownClosure() > 0f; // LAYERV
	//
	//			if (jprogram == 1 && fipPolygon.getModeFip().map(mode -> mode == FipMode.FIPYOUNG).orElse(false)) {
	//				return 100f;
	//			}
	//			if (jprogram == 3) {
	//				veteran = fipVetLayer != null;
	//			}
	//
	//			assert fipPrimaryLayer != null;
	//
	//			float crownClosure = fipPrimaryLayer.getCrownClosure();
	//
	//			// Assume crown closure linear with age, to 25.
	//			if (fipPrimaryLayer.getAgeTotal() < 25f) {
	//				crownClosure *= 25f / fipPrimaryLayer.getAgeTotal();
	//			}
	//			// define crown closure as the SUM of two layers
	//			if (veteran) {
	//				crownClosure += fipVetLayer.getCrownClosure();
	//			}
	//			crownClosure = clamp(crownClosure, 0, 100);
	//
	//			/*
	//			 * assume that CC occurs at age 25 and that most land goes to 90% occupancy but
	//			 * that occupancy increases only 1% /yr with no increases after ages 25. });
	//			 */
	//
	//			// Obtain the percent yield (in comparison with CC = 90%)
	//
	//			float crownClosureTop = 90f;
	//			float breastHeightAge = fipPrimaryLayer.getAgeTotal() - fipPrimaryLayer.getYearsToBreastHeight();
	//
	//			float yieldFactor = fipPolygon.getYieldFactor();
	//
	//			var bec = BecDefinitionParser.getBecs(controlMap).get(fipPolygon.getBiogeoclimaticZone()).orElseThrow(
	//					() -> new ProcessingException("Could not find BEC " + fipPolygon.getBiogeoclimaticZone())
	//			);
	//
	//			breastHeightAge = max(5.0f, breastHeightAge);
	//			// EMP040
	//			float baseAreaTop = estimatePrimaryBaseArea(
	//					fipPrimaryLayer, bec, yieldFactor, breastHeightAge, 0f, crownClosureTop
	//			);
	//			// EMP040
	//			float baseAreaHat = estimatePrimaryBaseArea(
	//					fipPrimaryLayer, bec, yieldFactor, breastHeightAge, 0f, crownClosure
	//			);
	//
	//			float percentYield;
	//			if (baseAreaTop > 0f && baseAreaHat > 0f) {
	//				percentYield = min(100f, 100f * baseAreaHat / baseAreaTop);
	//			} else {
	//				percentYield = 90f;
	//			}
	//
	//			float gainMax;
	//			if (fipPrimaryLayer.getAgeTotal() > 125f) {
	//				gainMax = 0f;
	//			} else if (fipPrimaryLayer.getAgeTotal() < 25f) {
	//				gainMax = max(90f - percentYield, 0);
	//			} else {
	//				gainMax = max(90f - percentYield, 0);
	//				gainMax = min(gainMax, 125 - fipPrimaryLayer.getAgeTotal());
	//			}
	//
	//			return floor(min(percentYield + gainMax, 100f));
	//
	//		}
	//	}
	//
	//	// FIPCALC1
	//	VdypLayer processLayerAsPrimary(FipPolygon fipPolygon, FipLayerPrimary fipLayer, float baseAreaOverstory)
	//			throws ProcessingException {
	//
	//		var lookup = BecDefinitionParser.getBecs(controlMap);
	//		var primarySpecies = findPrimarySpecies(fipLayer.getSpecies());
	//		// VDYP7 stores this in the common FIPL_1C/ITGL1 but only seems to use it
	//		// locally
	//		var itg = findItg(primarySpecies);
	//
	//		BecDefinition bec = lookup.get(fipPolygon.getBiogeoclimaticZone()).orElseThrow(
	//				() -> new IllegalStateException("Could not find BEC " + fipPolygon.getBiogeoclimaticZone())
	//		);
	//
	//		var result = new VdypLayer(fipLayer.getPolygonIdentifier(), fipLayer.getLayer());
	//
	//		result.setAgeTotal(fipLayer.getAgeTotal());
	//		result.setYearsToBreastHeight(fipLayer.getYearsToBreastHeight());
	//		result.setBreastHeightAge(fipLayer.getAgeTotal() - fipLayer.getYearsToBreastHeight());
	//		result.setHeight(fipLayer.getHeight());
	//
	//		// EMP040
	//		var baseArea = estimatePrimaryBaseArea(
	//				fipLayer, bec, fipPolygon.getYieldFactor(), result.getBreastHeightAge(), baseAreaOverstory
	//		); // BA_TOT
	//
	//		result.getBaseAreaByUtilization().setCoe(UTIL_ALL, baseArea);
	//
	//		var quadMeanDiameter = estimatePrimaryQuadMeanDiameter(
	//				fipLayer, bec, result.getBreastHeightAge(), baseAreaOverstory
	//		);
	//
	//		result.getQuadraticMeanDiameterByUtilization().setCoe(UTIL_ALL, quadMeanDiameter);
	//
	//		var tphTotal = treesPerHectare(baseArea, quadMeanDiameter);
	//
	//		result.getTreesPerHectareByUtilization().setCoe(UTIL_ALL, tphTotal);
	//
	//		// Copy over Species entries.
	//		// LVCOM/ISPL1=ISPV
	//		// LVCOM4/SP0L1=FIPSA/SP0V
	//		// LVCOM4/SP64DISTL1=FIPSA/VDISTRV
	//		// LVCOM1/PCLT1=FIPS/PCTVOLV
	//		var vdypSpecies = fipLayer.getSpecies().values().stream() //
	//				.map(VdypSpecies::new) //
	//				.collect(Collectors.toMap(VdypSpecies::getGenus, Function.identity()));
	//
	//		var vdypPrimarySpecies = vdypSpecies.get(primarySpecies.get(0).getGenus());
	//
	//		// Lookup volume group, Decay Group, and Breakage group for each species.
	//
	//		var volumeGroupMap = getGroupMap(VolumeEquationGroupParser.CONTROL_KEY);
	//		var decayGroupMap = getGroupMap(DecayEquationGroupParser.CONTROL_KEY);
	//		var breakageGroupMap = getGroupMap(BreakageEquationGroupParser.CONTROL_KEY);
	//
	//		Map<String, Float> targetPercentages = new HashMap<>(vdypSpecies.size());
	//
	//		for (var vSpec : vdypSpecies.values()) {
	//			var volumeGroup = getGroup(fipPolygon, volumeGroupMap, vSpec);
	//			var decayGroup = getGroup(fipPolygon, decayGroupMap, vSpec);
	//			var breakageGroup = getGroup(fipPolygon, breakageGroupMap, vSpec);
	//
	//			vSpec.setVolumeGroup(volumeGroup);
	//			vSpec.setDecayGroup(decayGroup);
	//			vSpec.setBreakageGroup(breakageGroup);
	//
	//			targetPercentages.put(vSpec.getGenus(), vSpec.getPercentGenus());
	//		}
	//
	//		var maxPass = fipLayer.getSpecies().size() > 1 ? 2 : 1;
	//
	//		result.setSpecies(vdypSpecies);
	//
	//		float primaryHeight;
	//		float leadHeight = fipLayer.getHeight();
	//		for (var iPass = 1; iPass <= maxPass; iPass++) {
	//			if (iPass == 2) {
	//				for (var vSpec : vdypSpecies.values()) {
	//					vSpec.setPercentGenus(targetPercentages.get(vSpec.getGenus()));
	//				}
	//			}
	//			// Estimate lorey height for primary species
	//			if (iPass == 1 && vdypSpecies.size() == 1) {
	//				primaryHeight = primaryHeightFromLeadHeight(
	//						leadHeight, vdypPrimarySpecies.getGenus(), bec.getRegion(), tphTotal
	//				);
	//			} else if (iPass == 1) {
	//				primaryHeight = primaryHeightFromLeadHeightInitial(
	//						leadHeight, vdypPrimarySpecies.getGenus(), bec.getRegion()
	//				);
	//			} else {
	//				primaryHeight = primaryHeightFromLeadHeight(
	//						leadHeight, vdypPrimarySpecies.getGenus(), bec.getRegion(),
	//						vdypPrimarySpecies.getTreesPerHectareByUtilization().getCoe(UTIL_ALL)
	//				);
	//			}
	//			vdypPrimarySpecies.getLoreyHeightByUtilization().setCoe(UTIL_ALL, primaryHeight);
	//
	//			// Estimate lorey height for non-primary species
	//			for (var vspec : vdypSpecies.values()) {
	//				if (vspec == vdypPrimarySpecies)
	//					continue;
	//
	//				// EMP053
	//				vspec.getLoreyHeightByUtilization().setCoe(
	//						UTIL_ALL,
	//						estimateNonPrimaryLoreyHeight(vspec, vdypPrimarySpecies, bec, leadHeight, primaryHeight)
	//				);
	//			}
	//
	//			// ROOTF01
	//			findRootsForDiameterAndBaseArea(result, fipLayer, bec, iPass + 1);
	//		}
	//
	//		estimateSmallComponents(fipPolygon, result);
	//
	//		// YUC1
	//		computeUtilizationComponentsPrimary(bec, result, VolumeComputeMode.BY_UTIL, CompatibilityVariableMode.NONE);
	//
	//		return result;
	//	}
	//
	//	// ROOTF01
	//	void findRootsForDiameterAndBaseArea(VdypLayer result, FipLayerPrimary fipLayer, BecDefinition bec, int source)
	//			throws ProcessingException {
	//
	//		var quadMeanDiameterTotal = result.getQuadraticMeanDiameterByUtilization().getCoe(UTIL_ALL); // DQ_TOT
	//		var baseAreaTotal = result.getBaseAreaByUtilization().getCoe(UTIL_ALL); // BA_TOT
	//		var treesPerHectareTotal = result.getTreesPerHectareByUtilization().getCoe(UTIL_ALL); // TPH_TOT
	//		Map<String, Float> goal = new LinkedHashMap<>(); // GOAL
	//		Map<String, Float> xMap = new LinkedHashMap<>(); // X
	//
	//		float treesPerHectareSum;
	//
	//		assert result.getSpecies().size() > 0;
	//
	//		if (result.getSpecies().size() == 1) {
	//			var spec = result.getSpecies().values().iterator().next();
	//			for (var accessors : NON_VOLUME_UTILIZATION_VECTOR_ACCESSORS) {
	//
	//				try {
	//					Coefficients specVector = (Coefficients) accessors.getReadMethod().invoke(spec);
	//					Coefficients layerVector = (Coefficients) accessors.getReadMethod().invoke(result);
	//					specVector.setCoe(UTIL_ALL, layerVector.getCoe(UTIL_ALL));
	//				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
	//					throw new IllegalStateException(e);
	//				}
	//			}
	//
	//			result.getLoreyHeightByUtilization().setCoe(UTIL_ALL, spec.getLoreyHeightByUtilization().getCoe(UTIL_ALL));
	//			spec.setPercentGenus(100f);
	//			treesPerHectareSum = treesPerHectareTotal;
	//		} else {
	//			// Multiple Species
	//			for (var spec : result.getSpecies().values()) {
	//
	//				Coefficients limitCoe = getLimitsForHeightAndDiameter(spec.getGenus(), bec.getRegion());
	//
	//				final float maxHeightMultiplier = fipLayer.getPrimaryGenus().equals(spec.getGenus()) ? 1.5f : 1.0f;
	//				final float heightMax = limitCoe.getCoe(1) * maxHeightMultiplier;
	//
	//				spec.getLoreyHeightByUtilization().scalarInPlace(UTIL_ALL, x -> min(x, heightMax));
	//			}
	//			ToDoubleFunction<VdypSpecies> accessor;
	//
	//			switch (source) {
	//			case 1:
	//				accessor = x -> x.getPercentGenus();
	//				break;
	//			case 2:
	//				accessor = x -> x.getPercentGenus() / x.getLoreyHeightByUtilization().getCoe(UTIL_ALL);
	//				break;
	//			case 3:
	//				accessor = x -> x.getBaseAreaByUtilization().getCoe(UTIL_ALL);
	//				break;
	//			default:
	//				throw new UnsupportedOperationException("Unknown source for root finding " + source);
	//			}
	//
	//			var sumSourceArea = result.getSpecies().values().stream().mapToDouble(accessor).sum();
	//
	//			// FRJ
	//			var fractionMap = result.getSpecies().values().stream().collect(
	//					Collectors.toMap(
	//							VdypSpecies::getGenus, spec -> (float) (accessor.applyAsDouble(spec) / sumSourceArea)
	//					)
	//			);
	//
	//			// HL_TOT
	//			float loreyHeightTotal = (float) fractionMap.entrySet().stream().mapToDouble(
	//					e -> e.getValue() * result.getSpecies().get(e.getKey()).getLoreyHeightByUtilization().getCoe(0)
	//			).sum();
	//			// FRJ(ISP) = FRJ(J) // We aren't using the remapping between global species
	//			// index and index for the species within the layer, so we can probably assign
	//			// directly to the fraction attribute on the species object.
	//			fractionMap.entrySet().forEach(e -> result.getSpecies().get(e.getKey()).setFractionGenus(e.getValue()));
	//
	//			double[] quadMeanDiameterBase = new double[result.getSpecies().size()]; // DQspbase
	//
	//			{
	//				int i = 0;
	//				for (var spec : result.getSpecies().values()) {
	//
	//					// EMP061
	//					Coefficients limitCoe = getLimitsForHeightAndDiameter(spec.getGenus(), bec.getRegion());
	//
	//					var dqMin = limitCoe.getCoe(3) * spec.getLoreyHeightByUtilization().getCoe(UTIL_ALL);
	//					var dqMax = max(
	//							limitCoe.getCoe(2), limitCoe.getCoe(4) * spec.getLoreyHeightByUtilization().getCoe(UTIL_ALL)
	//					);
	//
	//					// EMP060
	//					float quadMeanDiameter = clamp(
	//							estimateQuadMeanDiameterForSpecies(
	//									spec, result.getSpecies(), bec.getRegion(), quadMeanDiameterTotal, baseAreaTotal,
	//									treesPerHectareTotal, loreyHeightTotal
	//							), //
	//							dqMin, dqMax
	//					);
	//
	//					quadMeanDiameterBase[i++] = quadMeanDiameter;
	//				}
	//			}
	//			// VDYP7 checks the number of species here, but this is already inside a branch
	//			// that must be more than 1
	//			// Fill in target and trial values
	//
	//			eachButLast(result.getSpecies().values(), spec -> {
	//				goal.put(spec.getGenus(), spec.getPercentGenus());
	//				xMap.put(spec.getGenus(), spec.getPercentGenus());
	//			}, spec -> {
	//				goal.put(spec.getGenus(), quadMeanDiameterTotal);
	//				xMap.put(spec.getGenus(), 0f);
	//			});
	//
	//			var xVec = xMap.values().stream().mapToDouble(v -> (double) v).toArray();
	//			var goalVec = goal.values().stream().mapToDouble(v -> (double) v).toArray();
	//
	//			// SNQSOL
	//			var rootVec = this.findRoot(quadMeanDiameterBase, goalVec, xVec, result, TOLERANCE);
	//
	//			var rootMap = new LinkedHashMap<String, Float>();
	//			{
	//				float percentSum = 0;
	//				var it = result.getSpecies().values().iterator();
	//				for (int i = 0; it.hasNext(); i++) {
	//					var spec = it.next();
	//					rootMap.put(spec.getGenus(), (float) rootVec.getEntry(i));
	//					if (it.hasNext()) {
	//						spec.setPercentGenus((float) rootVec.getEntry(i));
	//						percentSum += rootVec.getEntry(i);
	//					} else {
	//						spec.setPercentGenus(100 - percentSum);
	//					}
	//				}
	//			}
	//
	//			float loreyHeightSum = 0;
	//			treesPerHectareSum = 0;
	//
	//			{
	//				int i = 0;
	//				for (var spec : result.getSpecies().values()) {
	//					float dqBase = (float) quadMeanDiameterBase[i++];
	//					float dq = 7.5f + (dqBase - 7.5f) * exp((float) rootVec.getEntry(rootVec.getDimension() - 1) / 20f);
	//					assert dq >= 0;
	//					float ba = baseAreaTotal * spec.getPercentGenus() / 100f;
	//					assert ba >= 0;
	//					float tph = treesPerHectare(ba, dq);
	//					assert tph >= 0;
	//					spec.getQuadraticMeanDiameterByUtilization().setCoe(UTIL_ALL, dq);
	//					spec.getBaseAreaByUtilization().setCoe(UTIL_ALL, ba);
	//					spec.getTreesPerHectareByUtilization().setCoe(UTIL_ALL, tph);
	//					treesPerHectareSum += tph;
	//					loreyHeightSum += spec.getLoreyHeightByUtilization().getCoe(UTIL_ALL) * ba;
	//				}
	//			}
	//			result.getLoreyHeightByUtilization().setCoe(UTIL_ALL, loreyHeightSum / baseAreaTotal);
	//
	//		} // end of Multiple Species branch
	//
	//		var volumeSum = 0f;
	//
	//		for (var spec : result.getSpecies().values()) {
	//			// EMP090
	//			var wholeStemVolume = spec.getTreesPerHectareByUtilization().getCoe(UTIL_ALL)
	//					* estimateWholeStemVolumePerTree(
	//							spec.getVolumeGroup(), spec.getLoreyHeightByUtilization().getCoe(UTIL_ALL),
	//							spec.getQuadraticMeanDiameterByUtilization().getCoe(UTIL_ALL)
	//					);
	//			spec.getWholeStemVolumeByUtilization().setCoe(UTIL_ALL, wholeStemVolume);
	//			volumeSum += wholeStemVolume;
	//		}
	//
	//		result.getWholeStemVolumeByUtilization().setCoe(UTIL_ALL, volumeSum);
	//		var treesPerHectareStart = result.getTreesPerHectareByUtilization().getCoe(UTIL_ALL);
	//		result.getTreesPerHectareByUtilization().setCoe(UTIL_ALL, treesPerHectareSum);
	//		result.getQuadraticMeanDiameterByUtilization().setCoe(
	//				UTIL_ALL,
	//				quadMeanDiameter(
	//						result.getBaseAreaByUtilization().getCoe(UTIL_ALL),
	//						result.getTreesPerHectareByUtilization().getCoe(UTIL_ALL)
	//				)
	//		);
	//
	//		if (abs(treesPerHectareStart / result.getTreesPerHectareByUtilization().getCoe(UTIL_ALL) - 1f) > 0.002) {
	//			throw new ProcessingException("TODO");
	//		}
	//
	//		if (result.getSpecies().size() > 1) {
	//			for (var spec : result.getSpecies().values()) {
	//				if (spec.getWholeStemVolumeByUtilization().getCoe(UTIL_ALL) / volumeSum
	//						- goal.get(spec.getGenus()) > 0.1) {
	//					throw new ProcessingException("TODO");
	//				}
	//			}
	//		}
	//	}
	//
	//	// EMP060
	//	/**
	//	 * Estimate DQ for a species (primary or not). Using eqn in jf125.doc.
	//	 *
	//	 * Enforces mins and maxes from EMP061.
	//	 *
	//	 * @param spec                  Species of insterest
	//	 * @param allSpecies            Collection of all species on the layer
	//	 * @param region                BEC Region of the stand
	//	 * @param standQuadMeanDiameter Quadratic mean diameter of the stand
	//	 * @param standBaseArea         Base area of the stand
	//	 * @param standTreesPerHectare  Density opf the stand
	//	 * @param standLoreyHeight      Lorey height of the stand
	//	 * @return Quadratic mean diameter of the species of interest
	//	 * @throws ProcessingException
	//	 */
	//	float estimateQuadMeanDiameterForSpecies(
	//			VdypSpecies spec, // ISP, HLsp, DQsp
	//			Map<String, VdypSpecies> allSpecies, // FR
	//			Region region, // INDEX_IC
	//			float standQuadMeanDiameter, // DQ_TOT
	//			float standBaseArea, // BA_TOT
	//			float standTreesPerHectare, // TPH_TOT
	//			float standLoreyHeight // HL_TOT
	//	) throws ProcessingException {
	//		String species = spec.getGenus();
	//
	//		float c = 0.00441786467f;
	//
	//		float minQuadMeanDiameter = min(7.6f, standQuadMeanDiameter);
	//
	//		// Quick solution
	//		if (spec.getFractionGenus() >= 1f || standQuadMeanDiameter < minQuadMeanDiameter) {
	//			return standQuadMeanDiameter;
	//		}
	//
	//		var coeMap = Utils.<Map<String, Coefficients>>expectParsedControl(
	//				controlMap, BySpeciesDqCoefficientParser.CONTROL_KEY, Map.class
	//		);
	//		var specAliases = GenusDefinitionParser.getSpeciesAliases(controlMap);
	//
	//		// TODO we can probably remove these as they seem to only be used for debugging
	//		// in VDYP7
	//		Map<String, Float> adjust = new HashMap<>(coeMap.size());
	//		Map<String, Float> mult = new HashMap<>(coeMap.size());
	//
	//		var specIt = specAliases.iterator();
	//
	//		var spec1 = specIt.next();
	//
	//		float a2 = coeMap.get(spec1).getCoe(2);
	//
	//		float fractionOther = 1f - spec.getFractionGenus(); // FR_REST
	//
	//		mult.put(spec1, 1f);
	//		float a0 = coeMap.get(spec1).getCoe(0);
	//		float a1 = coeMap.get(spec1).getCoe(1);
	//
	//		while (specIt.hasNext()) {
	//			var specIAlias = specIt.next();
	//			var specI = allSpecies.get(specIAlias);
	//			if (specIAlias.equals(spec.getGenus())) {
	//				float multI = 1f;
	//				mult.put(specIAlias, multI);
	//				a0 += multI * coeMap.get(specIAlias).getCoe(0);
	//				a1 += multI * coeMap.get(specIAlias).getCoe(1);
	//			} else {
	//				if (specI != null && specI.getFractionGenus() > 0f) {
	//					float multI = -specI.getFractionGenus() / fractionOther;
	//					mult.put(specIAlias, multI);
	//					a0 += multI * coeMap.get(specIAlias).getCoe(0);
	//					a1 -= multI * coeMap.get(specIAlias).getCoe(1);
	//				}
	//			}
	//		}
	//
	//		float loreyHeightSpec = spec.getLoreyHeightByUtilization().getCoe(UTIL_ALL);
	//		float loreyHeight1 = max(4f, loreyHeightSpec);
	//		float loreyHeight2 = (standLoreyHeight - loreyHeightSpec * spec.getFractionGenus()) / fractionOther;
	//		float loreyHeightRatio = clamp( (loreyHeight1 - 3f) / (loreyHeight2 - 3f), 0.05f, 20f);
	//
	//		float r = exp(
	//				a0 + a1 * log(loreyHeightRatio) + a2 * log(standQuadMeanDiameter) + adjust.getOrDefault(species, 0f)
	//		);
	//
	//		float baseArea1 = spec.getFractionGenus() * standBaseArea;
	//		float baseArea2 = standBaseArea - baseArea1;
	//
	//		float treesPerHectare1;
	//		if (abs(r - 1f) < 0.0005) {
	//			treesPerHectare1 = spec.getFractionGenus() * standTreesPerHectare;
	//		} else {
	//			float aa = (r - 1f) * c;
	//			float bb = c * (1f - r) * standTreesPerHectare + baseArea1 + baseArea2 * r;
	//			float cc = -baseArea1 * standTreesPerHectare;
	//			float term = bb * bb - 4 * aa * cc;
	//			if (term <= 0f) {
	//				throw new ProcessingException(
	//						"Term for trees per hectare calculation when estimating quadratic mean diameter for species "
	//								+ species + " was " + term + " but should be positive."
	//				);
	//			}
	//			treesPerHectare1 = (-bb + sqrt(term)) / (2f * aa);
	//			if (treesPerHectare1 <= 0f || treesPerHectare1 > standTreesPerHectare) {
	//				throw new ProcessingException(
	//						"Trees per hectare 1 for species " + species + " was " + treesPerHectare1
	//								+ " but should be positive and less than or equal to stand trees per hectare "
	//								+ standTreesPerHectare
	//				);
	//			}
	//		}
	//
	//		float quadMeanDiameter1 = quadMeanDiameter(baseArea1, treesPerHectare1);
	//		float treesPerHectare2 = standTreesPerHectare - treesPerHectare1;
	//		float quadMeanDiameter2 = quadMeanDiameter(baseArea2, treesPerHectare2);
	//
	//		if (quadMeanDiameter2 < minQuadMeanDiameter) {
	//			// species 2 is too small. Make target species smaller.
	//			quadMeanDiameter2 = minQuadMeanDiameter;
	//			treesPerHectare2 = treesPerHectare(baseArea2, quadMeanDiameter2);
	//			treesPerHectare1 = standTreesPerHectare - treesPerHectare2;
	//			quadMeanDiameter1 = quadMeanDiameter(baseArea1, treesPerHectare1);
	//		}
	//		var limitCoe = getLimitsForHeightAndDiameter(species, region);
	//
	//		final var dqMinSp = max(minQuadMeanDiameter, limitCoe.getCoe(3) * loreyHeightSpec);
	//		final var dqMaxSp = max(7.6f, min(limitCoe.getCoe(2), limitCoe.getCoe(4) * loreyHeightSpec));
	//		if (quadMeanDiameter1 < dqMinSp) {
	//			quadMeanDiameter1 = dqMinSp;
	//			treesPerHectare1 = treesPerHectare(baseArea1, quadMeanDiameter1);
	//			treesPerHectare2 = standTreesPerHectare - treesPerHectare2;
	//			quadMeanDiameter2 = quadMeanDiameter(baseArea2, treesPerHectare2);
	//		}
	//		if (quadMeanDiameter1 > dqMaxSp) {
	//			// target species is too big. Make target species smaller, DQ2 bigger.
	//
	//			quadMeanDiameter1 = dqMaxSp;
	//			treesPerHectare1 = treesPerHectare(baseArea1, quadMeanDiameter1);
	//			treesPerHectare2 = standTreesPerHectare - treesPerHectare2;
	//
	//			if (treesPerHectare2 > 0f && baseArea2 > 0f) {
	//				quadMeanDiameter2 = quadMeanDiameter(baseArea2, treesPerHectare2);
	//			} else {
	//				quadMeanDiameter2 = 1000f;
	//			}
	//
	//			// under rare circumstances, let DQ1 exceed DQMAXsp
	//			if (quadMeanDiameter2 < minQuadMeanDiameter) {
	//				quadMeanDiameter2 = minQuadMeanDiameter;
	//				treesPerHectare2 = treesPerHectare(baseArea2, quadMeanDiameter2);
	//				treesPerHectare1 = standTreesPerHectare - treesPerHectare2;
	//				quadMeanDiameter1 = quadMeanDiameter(baseArea1, treesPerHectare1);
	//			}
	//
	//		}
	//		return quadMeanDiameter1;
	//	}
	//
	//	// EMP061
	//	private Coefficients getLimitsForHeightAndDiameter(String genus, Region region) {
	//		var coeMap = Utils.<MatrixMap2<String, Region, Coefficients>>expectParsedControl(
	//				controlMap, ComponentSizeParser.CONTROL_KEY, MatrixMap2.class
	//		);
	//
	//		return coeMap.get(genus, region);
	//	}
	//
	//	// EMP090
	//	private float estimateWholeStemVolumePerTree(int volumeGroup, float loreyHeight, float quadMeanDiameter) {
	//		var coeMap = Utils.<Map<Integer, Coefficients>>expectParsedControl(
	//				controlMap, TotalStandWholeStemParser.CONTROL_KEY, Map.class
	//		);
	//		var coe = coeMap.get(volumeGroup).reindex(0);
	//
	//		var logMeanVolume = coe.getCoe(UTIL_ALL) + //
	//				coe.getCoe(1) * log(quadMeanDiameter) + //
	//				coe.getCoe(2) * log(loreyHeight) + //
	//				coe.getCoe(3) * quadMeanDiameter + //
	//				coe.getCoe(4) / quadMeanDiameter + //
	//				coe.getCoe(5) * loreyHeight + //
	//				coe.getCoe(6) * quadMeanDiameter * quadMeanDiameter + //
	//				coe.getCoe(7) * loreyHeight * quadMeanDiameter //
	//				+ coe.getCoe(8) * loreyHeight / quadMeanDiameter;
	//
	//		return exp(logMeanVolume);
	//	}
	//
	//	// EMP053 Using eqns N1 and N2 from ipsjf124.doc
	//	/**
	//	 * Estimate the lorey height of a non-primary species of a primary layer.
	//	 *
	//	 * @param vspec         The species.
	//	 * @param vspecPrime    The primary species.
	//	 * @param leadHeight    lead height of the layer
	//	 * @param primaryHeight height of the primary species
	//	 * @throws ProcessingException
	//	 */
	//	float estimateNonPrimaryLoreyHeight(
	//			VdypSpecies vspec, VdypSpecies vspecPrime, BecDefinition bec, float leadHeight, float primaryHeight
	//	) throws ProcessingException {
	//		var coeMap = Utils.<MatrixMap3<String, String, Region, Optional<NonprimaryHLCoefficients>>>expectParsedControl(
	//				controlMap, HLNonprimaryCoefficientParser.CONTROL_KEY, MatrixMap3.class
	//		);
	//
	//		var coe = coeMap.get(vspec.getGenus(), vspecPrime.getGenus(), bec.getRegion()).orElseThrow(
	//				() -> new ProcessingException(
	//						String.format(
	//								"Could not find Lorey Height Nonprimary Coefficients for %s %s %s", vspec.getGenus(),
	//								vspecPrime.getGenus(), bec.getRegion()
	//						)
	//				)
	//		);
	//		var heightToUse = coe.getEquationIndex() == 1 ? leadHeight : primaryHeight;
	//		return 1.3f + coe.getCoe(1) * pow(heightToUse - 1.3f, coe.getCoe(2));
	//	}
	//
	//	VdypLayer processLayerAsVeteran(FipPolygon fipPolygon, FipLayer fipLayer) throws ProcessingException {
	//
	//		var polygonIdentifier = fipLayer.getPolygonIdentifier();
	//
	//		assert fipLayer.getLayer().equals(Layer.VETERAN) : "Layer must be VETERAN";
	//		assert fipPolygon.getPolygonIdentifier().equals(fipLayer.getPolygonIdentifier()) : String.format(
	//				"Polygon polygonIdentifier '%s' doesn't match that of layer '%s'", fipPolygon.getPolygonIdentifier(),
	//				fipLayer.getPolygonIdentifier()
	//		);
	//
	//		var layer = Layer.VETERAN;
	//
	//		// find Primary genus (highest percentage) ISPPVET
	//
	//		var primaryGenus = fipLayer.getSpecies().values().stream() //
	//				.max(Utils.compareUsing(FipSpecies::getPercentGenus)) //
	//				.orElseThrow(() -> new IllegalStateException("No primarty genus (SP0) found. This should not happen."))
	//				.getGenus();
	//
	//		// ageTotal copy, LVCOM3/AGETOTLV copied from FIPL_V/AGETOT_LV
	//		var ageTotal = fipLayer.getAgeTotal();
	//
	//		// yearsToBreastHeight copy, minimum 6.0, LVCOM3/YTBHLV copied from
	//		// FIPL_V/YTBH_L
	//		var yearsToBreastHeight = Math.max(fipLayer.getYearsToBreastHeight(), 6.0f);
	//
	//		// breastHeightAge LVCOM3/AGEBHLV ageTotal-yearsToBreastHeight
	//		var breastHeightAge = ageTotal - yearsToBreastHeight;
	//
	//		// height? copy LVCOM3/HDLV = FIPL_V/HT_LV
	//		var height = fipLayer.getHeight();
	//
	//		var crownClosure = fipLayer.getCrownClosure();
	//
	//		var becId = fipPolygon.getBiogeoclimaticZone();
	//		var bec = BecDefinitionParser.getBecs(controlMap).get(becId)
	//				.orElseThrow(() -> new ProcessingException("Could not find BEC " + becId));
	//		var region = bec.getRegion();
	//
	//		// Call EMP098 to get Veteran Basal Area, store in LVCOM1/BA array at positions
	//		// 0,0 and 0,4
	//		var estimatedBaseArea = estimateVeteranBaseArea(height, crownClosure, primaryGenus, region);
	//		var baseAreaByUtilization = new Coefficients(
	//				new float[] { 0.0f, estimatedBaseArea, 0.0f, 0.0f, 0.0f, estimatedBaseArea }, -1
	//		);
	//		// Copy over Species entries.
	//		// LVCOM/ISPLV=ISPV
	//		// LVCOM4/SP0LV=FIPSA/SP0V
	//		// LVCOM4/SP64DISTLV=FIPSA/VDISTRV
	//		// LVCOM1/PCLTV=FIPS/PCTVOLV
	//		// LVCOM1/HL=FIPL_V/HT_LV
	//		var vdypSpecies = fipLayer.getSpecies().values().stream() //
	//				.map(fipSpec -> {
	//					var vs = new VdypSpecies(fipSpec);
	//					vs.setLoreyHeightByUtilization(new Coefficients(new float[] { 0f, height }, -1));
	//					return vs;
	//				}) //
	//				.collect(Collectors.toMap(VdypSpecies::getGenus, Function.identity()));
	//
	//		// Lookup volume group, Decay Group, and Breakage group for each species.
	//
	//		var volumeGroupMap = getGroupMap(VolumeEquationGroupParser.CONTROL_KEY);
	//		var decayGroupMap = getGroupMap(DecayEquationGroupParser.CONTROL_KEY);
	//		var breakageGroupMap = getGroupMap(BreakageEquationGroupParser.CONTROL_KEY);
	//		for (var vSpec : vdypSpecies.values()) {
	//			var volumeGroup = getGroup(fipPolygon, volumeGroupMap, vSpec);
	//			var decayGroup = getGroup(fipPolygon, decayGroupMap, vSpec);
	//			var breakageGroup = getGroup(fipPolygon, breakageGroupMap, vSpec);
	//
	//			vSpec.setVolumeGroup(volumeGroup);
	//			vSpec.setDecayGroup(decayGroup);
	//			vSpec.setBreakageGroup(breakageGroup);
	//		}
	//
	//		/*
	//		 * From VDYP7
	//		 *
	//		 * At this point we SHOULD invoke a root finding procedure sets species percents
	//		 * and adjusts DQ by species. fills in main components, through whole-stem
	//		 * volume INSTEAD, I will assume %volumes apply to % BA's
	//		 */
	//
	//		for (var vSpec : vdypSpecies.values()) {
	//			vSpec.getBaseAreaByUtilization()
	//					.setCoe(UTIL_LARGEST, baseAreaByUtilization.getCoe(UTIL_LARGEST) * vSpec.getPercentGenus() / 100f);
	//		}
	//
	//		var vetDqMap = Utils.<MatrixMap2<String, Region, Coefficients>>expectParsedControl(
	//				controlMap, VeteranDQParser.CONTROL_KEY, MatrixMap2.class
	//		);
	//
	//		for (var vSpec : vdypSpecies.values()) {
	//			var genus = vSpec.getGenus();
	//			var coe = vetDqMap.get(genus, region);
	//			var a0 = coe.getCoe(1);
	//			var a1 = coe.getCoe(2);
	//			var a2 = coe.getCoe(3);
	//			float hl = vSpec.getLoreyHeightByUtilization().getCoe(0);
	//			float dq = max(a0 + a1 * pow(hl, a2), 22.5f);
	//			vSpec.getQuadraticMeanDiameterByUtilization().setCoe(UTIL_LARGEST, dq);
	//			vSpec.getTreesPerHectareByUtilization()
	//					.setCoe(UTIL_LARGEST, treesPerHectare(vSpec.getBaseAreaByUtilization().getCoe(UTIL_LARGEST), dq));
	//		}
	//
	//		var vdypLayer = new VdypLayer(polygonIdentifier, layer);
	//		vdypLayer.setAgeTotal(ageTotal);
	//		vdypLayer.setHeight(height);
	//		vdypLayer.setYearsToBreastHeight(yearsToBreastHeight);
	//		vdypLayer.setBreastHeightAge(breastHeightAge);
	//		vdypLayer.setSpecies(vdypSpecies);
	//		// vdypLayer.setPrimaryGenus(primaryGenus);
	//		vdypLayer.setBaseAreaByUtilization(baseAreaByUtilization);
	//
	//		computeUtilizationComponentsVeteran(vdypLayer, bec);
	//
	//		return vdypLayer;
	//	}
	//
	//	static Coefficients utilizationVector() {
	//		return new Coefficients(new float[] { 0f, 0f, 0f, 0f, 0f, 0f }, -1);
	//	}
	//
	//	static Coefficients utilizationVector(float small, float all, float u1, float u2, float u3, float u4) {
	//		return new Coefficients(new float[] { small, all, u1, u2, u3, u4 }, -1);
	//	}
	//
	//	static Coefficients utilizationVector(float small, float u1, float u2, float u3, float u4) {
	//		return new Coefficients(new float[] { small, u1 + u2 + u3 + u4, u1, u2, u3, u4 }, -1);
	//	}
	//
	//	// YUC1
	//	void computeUtilizationComponentsPrimary(
	//			BecDefinition bec, VdypLayer vdypLayer, VolumeComputeMode volumeComputeMode,
	//			CompatibilityVariableMode compatibilityVariableMode
	//	) throws ProcessingException {
	//		log.atTrace().setMessage("computeUtilizationComponentsPrimary for {}, stand total age is {}")
	//				.addArgument(vdypLayer.getPolygonIdentifier()).addArgument(vdypLayer.getAgeTotal()).log();
	//		log.atDebug().setMessage("Primary layer for {} has {} species/genera: {}")
	//				.addArgument(vdypLayer::getPolygonIdentifier) //
	//				.addArgument(() -> vdypLayer.getSpecies().size()) //
	//				.addArgument(() -> vdypLayer.getSpecies().keySet().stream().collect(Collectors.joining(", "))) //
	//				.log();
	//
	//		for (VdypSpecies spec : vdypLayer.getSpecies().values()) {
	//			float loreyHeightSpec = spec.getLoreyHeightByUtilization().getCoe(UTIL_ALL);
	//			float baseAreaSpec = spec.getBaseAreaByUtilization().getCoe(UTIL_ALL);
	//			float quadMeanDiameterSpec = spec.getQuadraticMeanDiameterByUtilization().getCoe(UTIL_ALL);
	//			float treesPerHectareSpec = spec.getTreesPerHectareByUtilization().getCoe(UTIL_ALL);
	//
	//			log.atDebug().setMessage("Working with species {}  LH: {}  DQ: {}  BA: {}  TPH: {}")
	//					.addArgument(spec.getClass()).addArgument(loreyHeightSpec).addArgument(quadMeanDiameterSpec)
	//					.addArgument(baseAreaSpec).addArgument(treesPerHectareSpec);
	//
	//			if (volumeComputeMode == VolumeComputeMode.BY_UTIL_WITH_WHOLE_STEM_BY_SPEC) {
	//				log.atDebug().log("Estimating tree volume");
	//
	//				// EMP090
	//				throw new UnsupportedOperationException("TODO"); // Not used yet
	//
	//				// log.atDebug().setMessage("Species WS stand volume {}")
	//				// .addArgument(() -> spec.getWholeStemVolumeByUtilization().getCoe(UTIL_ALL));
	//
	//			}
	//			float wholeStemVolumeSpec = spec.getWholeStemVolumeByUtilization().getCoe(UTIL_ALL);
	//
	//			var baseAreaUtil = utilizationVector();
	//			var quadMeanDiameterUtil = utilizationVector();
	//			var treesPerHectareUtil = utilizationVector();
	//			var wholeStemVolumeUtil = utilizationVector();
	//			var closeVolumeUtil = utilizationVector();
	//			var closeVolumeNetDecayUtil = utilizationVector();
	//			var closeVolumeNetDecayWasteUtil = utilizationVector();
	//			var closeVolumeNetDecayWasteBreakUtil = utilizationVector();
	//
	//			baseAreaUtil.setCoe(UTIL_ALL, baseAreaSpec); // BAU
	//			quadMeanDiameterUtil.setCoe(UTIL_ALL, quadMeanDiameterSpec); // DQU
	//			treesPerHectareUtil.setCoe(UTIL_ALL, treesPerHectareSpec); // TPHU
	//			wholeStemVolumeUtil.setCoe(UTIL_ALL, wholeStemVolumeSpec); // WSU
	//
	//			var adjustCloseUtil = utilizationVector(); // ADJVCU
	//			var adjustDecayUtil = utilizationVector(); // ADJVD
	//			var adjustDecayWasteUtil = utilizationVector(); // ADJVDW
	//
	//			// EMP071
	//			estimateQuadMeanDiameterByUtilization(bec, quadMeanDiameterUtil, spec);
	//
	//			// EMP070
	//			estimateBaseAreaByUtilization(bec, quadMeanDiameterUtil, baseAreaUtil, spec);
	//
	//			// Calculate tree density components
	//			for (var uc : UTIL_CLASSES) {
	//				treesPerHectareUtil.setCoe(
	//						uc.index, treesPerHectare(baseAreaUtil.getCoe(uc.index), quadMeanDiameterUtil.getCoe(uc.index))
	//				);
	//			}
	//
	//			// reconcile components with totals
	//
	//			// YUC1R
	//			reconcileComponents(baseAreaUtil, treesPerHectareUtil, quadMeanDiameterUtil);
	//
	//			if (compatibilityVariableMode != CompatibilityVariableMode.NONE) {
	//				throw new UnsupportedOperationException("TODO");
	//			}
	//
	//			// Recalculate TPH's
	//
	//			for (var uc : UTIL_CLASSES) {
	//				treesPerHectareUtil.setCoe(
	//						uc.index, treesPerHectare(baseAreaUtil.getCoe(uc.index), quadMeanDiameterUtil.getCoe(uc.index))
	//				);
	//			}
	//
	//			// Since DQ's may have changed, MUST RECONCILE AGAIN
	//			// Seems this might only be needed when compatibilityVariableMode is not NONE?
	//
	//			// YUC1R
	//			reconcileComponents(baseAreaUtil, treesPerHectareUtil, quadMeanDiameterUtil);
	//
	//			if (volumeComputeMode == VolumeComputeMode.ZERO) {
	//				throw new UnsupportedOperationException("TODO");
	//			} else {
	//
	//				// EMP091
	//				estimateWholeStemVolume(
	//						UtilizationClass.ALL, adjustCloseUtil.getCoe(4), spec.getVolumeGroup(), loreyHeightSpec,
	//						quadMeanDiameterUtil, baseAreaUtil, wholeStemVolumeUtil
	//				);
	//
	//				if (compatibilityVariableMode == CompatibilityVariableMode.ALL) {
	//					// apply compatibity variables to WS volume
	//					throw new UnsupportedOperationException("TODO");
	//				}
	//				// TODO Combine this with IF above
	//				if (compatibilityVariableMode == CompatibilityVariableMode.ALL) {
	//					// Set the adjustment factors for next three volume types
	//					throw new UnsupportedOperationException("TODO");
	//				} else {
	//					// Do nothing as the adjustment vectors are already set to 0
	//				}
	//
	//				// EMP092
	//				estimateCloseUtilizationVolume(
	//						UtilizationClass.ALL, adjustCloseUtil, spec.getVolumeGroup(), loreyHeightSpec,
	//						quadMeanDiameterUtil, wholeStemVolumeUtil, closeVolumeUtil
	//				);
	//
	//				// EMP093
	//				estimateNetDecayVolume(
	//						spec.getGenus(), bec.getRegion(), UtilizationClass.ALL, adjustCloseUtil, spec.getDecayGroup(),
	//						loreyHeightSpec, vdypLayer.getBreastHeightAge(), quadMeanDiameterUtil, closeVolumeUtil,
	//						closeVolumeNetDecayUtil
	//				);
	//
	//				// EMP094
	//				estimateNetDecayAndWasteVolume(
	//						bec.getRegion(), UtilizationClass.ALL, adjustCloseUtil, spec.getGenus(), loreyHeightSpec,
	//						vdypLayer.getBreastHeightAge(), quadMeanDiameterUtil, closeVolumeUtil, closeVolumeNetDecayUtil,
	//						closeVolumeNetDecayWasteUtil
	//				);
	//
	//				if (jprogram < 6) {
	//					// EMP095
	//					estimateNetDecayWasteAndBreakageVolume(
	//							UtilizationClass.ALL, spec.getBreakageGroup(), quadMeanDiameterUtil, closeVolumeUtil,
	//							closeVolumeNetDecayWasteUtil, closeVolumeNetDecayWasteBreakUtil
	//					);
	//				}
	//			}
	//
	//			spec.getBaseAreaByUtilization().pairwiseInPlace(baseAreaUtil, COPY_IF_BAND);
	//			spec.getTreesPerHectareByUtilization().pairwiseInPlace(treesPerHectareUtil, COPY_IF_BAND);
	//			spec.getQuadraticMeanDiameterByUtilization().pairwiseInPlace(quadMeanDiameterUtil, COPY_IF_BAND);
	//
	//			spec.getWholeStemVolumeByUtilization().pairwiseInPlace(wholeStemVolumeUtil, COPY_IF_NOT_TOTAL);
	//			spec.getCloseUtilizationVolumeByUtilization().pairwiseInPlace(closeVolumeUtil, COPY_IF_NOT_TOTAL);
	//			spec.getCloseUtilizationVolumeNetOfDecayByUtilization()
	//					.pairwiseInPlace(closeVolumeNetDecayUtil, COPY_IF_NOT_TOTAL);
	//			spec.getCloseUtilizationVolumeNetOfDecayAndWasteByUtilization()
	//					.pairwiseInPlace(closeVolumeNetDecayWasteUtil, COPY_IF_NOT_TOTAL);
	//			spec.getCloseUtilizationVolumeNetOfDecayWasteAndBreakageByUtilization()
	//					.pairwiseInPlace(closeVolumeNetDecayWasteBreakUtil, COPY_IF_NOT_TOTAL);
	//
	//		}
	//		computeLayerUtilizationComponentsFromSpecies(vdypLayer);
	//
	//		for (VdypSpecies spec : vdypLayer.getSpecies().values()) {
	//			if (vdypLayer.getBaseAreaByUtilization().getCoe(UTIL_ALL) > 0f) {
	//				spec.setFractionGenus(
	//						spec.getBaseAreaByUtilization().getCoe(UTIL_ALL)
	//								/ vdypLayer.getBaseAreaByUtilization().getCoe(UTIL_ALL)
	//				);
	//			}
	//			log.atDebug().addArgument(spec.getGenus()).addArgument(spec.getFractionGenus())
	//					.setMessage("Species {} base area {}%").log();
	//		}
	//
	//		log.atDebug().setMessage("Calculating Stand Lorey Height").log();
	//
	//		vdypLayer.getLoreyHeightByUtilization().setCoe(UTIL_SMALL, 0f);
	//		vdypLayer.getLoreyHeightByUtilization().setCoe(UTIL_ALL, 0f);
	//
	//		for (VdypSpecies spec : vdypLayer.getSpecies().values()) {
	//			log.atDebug() //
	//					.addArgument(spec.getGenus()) //
	//					.addArgument(() -> spec.getLoreyHeightByUtilization().getCoe(UTIL_ALL))
	//					.addArgument(() -> spec.getBaseAreaByUtilization().getCoe(UTIL_ALL))
	//					.addArgument(
	//							() -> spec.getLoreyHeightByUtilization().getCoe(UTIL_ALL)
	//									* spec.getBaseAreaByUtilization().getCoe(UTIL_ALL)
	//					)
	//					.setMessage(
	//							"For species {}, Species LH (7.5cm+): {}, Species BA (7.5cm+): {}, Weighted LH (7.5cm+): {}"
	//					).log();
	//			vdypLayer.getLoreyHeightByUtilization().scalarInPlace(
	//					UTIL_SMALL,
	//					x -> x + spec.getLoreyHeightByUtilization().getCoe(UTIL_SMALL)
	//							* spec.getBaseAreaByUtilization().getCoe(UTIL_SMALL)
	//			);
	//			vdypLayer.getLoreyHeightByUtilization().scalarInPlace(
	//					UTIL_ALL,
	//					x -> x + spec.getLoreyHeightByUtilization().getCoe(UTIL_ALL)
	//							* spec.getBaseAreaByUtilization().getCoe(UTIL_ALL)
	//			);
	//		}
	//		{
	//			float baSmall = vdypLayer.getBaseAreaByUtilization().getCoe(UTIL_SMALL);
	//			float baAll = vdypLayer.getBaseAreaByUtilization().getCoe(UTIL_ALL);
	//
	//			if (baSmall > 0) {
	//				vdypLayer.getLoreyHeightByUtilization().scalarInPlace(UTIL_SMALL, x -> x / baSmall);
	//			}
	//			if (baAll > 0) {
	//				vdypLayer.getLoreyHeightByUtilization().scalarInPlace(UTIL_ALL, x -> x / baAll);
	//			}
	//
	//		}
	//
	//	}
	//
	//	private static final IndexedFloatBinaryOperator COPY_IF_BAND = (oldX, newX, i) -> i <= UTIL_ALL ? oldX : newX;
	//	private static final IndexedFloatBinaryOperator COPY_IF_NOT_TOTAL = (oldX, newX, i) -> i < UTIL_ALL ? oldX : newX;
	//
	//	// YUCV
	//	private void computeUtilizationComponentsVeteran(VdypLayer vdypLayer, BecDefinition bec)
	//			throws ProcessingException {
	//		log.trace(
	//				"computeUtilizationComponentsVeterany for {}, stand total age is {}", vdypLayer.getPolygonIdentifier(),
	//				vdypLayer.getAgeTotal()
	//		);
	//
	//		var volumeAdjustMap = Utils.<Map<String, Coefficients>>expectParsedControl(
	//				controlMap, VeteranLayerVolumeAdjustParser.CONTROL_KEY, Map.class
	//		);
	//		try {
	//			for (var vdypSpecies : vdypLayer.getSpecies().values()) {
	//
	//				var treesPerHectareUtil = utilizationVector();
	//				var quadMeanDiameterUtil = utilizationVector();
	//				var baseAreaUtil = utilizationVector();
	//				var wholeStemVolumeUtil = utilizationVector();
	//
	//				var closeUtilizationVolumeUtil = utilizationVector();
	//				var closeUtilizationNetOfDecayUtil = utilizationVector();
	//				var closeUtilizationNetOfDecayAndWasteUtil = utilizationVector();
	//				var closeUtilizationNetOfDecayWasteAndBreakageUtil = utilizationVector();
	//
	//				var hlSp = vdypSpecies.getLoreyHeightByUtilization().getCoe(UTIL_ALL);
	//				{
	//					var baSp = vdypSpecies.getBaseAreaByUtilization().getCoe(UTIL_LARGEST);
	//					var tphSp = vdypSpecies.getTreesPerHectareByUtilization().getCoe(UTIL_LARGEST);
	//					var dqSp = vdypSpecies.getQuadraticMeanDiameterByUtilization().getCoe(UTIL_LARGEST);
	//
	//					treesPerHectareUtil.setCoe(UTIL_ALL, tphSp);
	//					quadMeanDiameterUtil.setCoe(UTIL_ALL, dqSp);
	//					baseAreaUtil.setCoe(UTIL_ALL, baSp);
	//					wholeStemVolumeUtil.setCoe(UTIL_ALL, 0f);
	//
	//					treesPerHectareUtil.setCoe(UTIL_LARGEST, tphSp);
	//					quadMeanDiameterUtil.setCoe(UTIL_LARGEST, dqSp);
	//					baseAreaUtil.setCoe(UTIL_LARGEST, baSp);
	//					wholeStemVolumeUtil.setCoe(UTIL_LARGEST, 0f);
	//				}
	//				// AADJUSTV
	//				var volumeAdjustCoe = volumeAdjustMap.get(vdypSpecies.getGenus());
	//
	//				var utilizationClass = UtilizationClass.OVER225; // IUC_VET
	//
	//				// ADJ
	//				var adjust = new Coefficients(new float[] { 0f, 0f, 0f, 0f }, 1);
	//
	//				// EMP091
	//				estimateWholeStemVolume(
	//						utilizationClass, volumeAdjustCoe.getCoe(1), vdypSpecies.getVolumeGroup(), hlSp,
	//						quadMeanDiameterUtil, baseAreaUtil, wholeStemVolumeUtil
	//				);
	//
	//				adjust.setCoe(4, volumeAdjustCoe.getCoe(2));
	//				// EMP092
	//				estimateCloseUtilizationVolume(
	//						utilizationClass, adjust, vdypSpecies.getVolumeGroup(), hlSp, quadMeanDiameterUtil,
	//						wholeStemVolumeUtil, closeUtilizationVolumeUtil
	//				);
	//
	//				adjust.setCoe(4, volumeAdjustCoe.getCoe(3));
	//				// EMP093
	//				estimateNetDecayVolume(
	//						vdypSpecies.getGenus(), bec.getRegion(), utilizationClass, adjust, vdypSpecies.getDecayGroup(),
	//						hlSp, vdypLayer.getBreastHeightAge(), quadMeanDiameterUtil, closeUtilizationVolumeUtil,
	//						closeUtilizationNetOfDecayUtil
	//				);
	//
	//				adjust.setCoe(4, volumeAdjustCoe.getCoe(4));
	//				// EMP094
	//				estimateNetDecayAndWasteVolume(
	//						bec.getRegion(), utilizationClass, adjust, vdypSpecies.getGenus(), hlSp,
	//						vdypLayer.getBreastHeightAge(), quadMeanDiameterUtil, closeUtilizationVolumeUtil,
	//						closeUtilizationNetOfDecayUtil, closeUtilizationNetOfDecayAndWasteUtil
	//				);
	//
	//				if (jprogram < 6) {
	//					// EMP095
	//					estimateNetDecayWasteAndBreakageVolume(
	//							utilizationClass, vdypSpecies.getBreakageGroup(), quadMeanDiameterUtil,
	//							closeUtilizationVolumeUtil, closeUtilizationNetOfDecayAndWasteUtil,
	//							closeUtilizationNetOfDecayWasteAndBreakageUtil
	//					);
	//				}
	//
	//				vdypSpecies.setBaseAreaByUtilization(baseAreaUtil);
	//				vdypSpecies.setTreesPerHectareByUtilization(treesPerHectareUtil);
	//				vdypSpecies.setQuadraticMeanDiameterByUtilization(quadMeanDiameterUtil);
	//				vdypSpecies.setWholeStemVolumeByUtilization(wholeStemVolumeUtil);
	//				vdypSpecies.setCloseUtilizationVolumeByUtilization(closeUtilizationVolumeUtil);
	//				vdypSpecies.setCloseUtilizationVolumeNetOfDecayByUtilization(closeUtilizationNetOfDecayUtil);
	//				vdypSpecies.setCloseUtilizationVolumeNetOfDecayAndWasteByUtilization(
	//						closeUtilizationNetOfDecayAndWasteUtil
	//				);
	//				vdypSpecies.setCloseUtilizationVolumeNetOfDecayWasteAndBreakageByUtilization(
	//						closeUtilizationNetOfDecayWasteAndBreakageUtil
	//				);
	//
	//				for (var accessors : UTILIZATION_VECTOR_ACCESSORS) {
	//					Coefficients utilVector = (Coefficients) accessors.getReadMethod().invoke(vdypSpecies);
	//
	//					// Set all components other than 4 to 0.0
	//					for (var i = -1; i < UTIL_LARGEST; i++) {
	//						utilVector.setCoe(i, 0f);
	//					}
	//
	//					// Set component 0 to equal component 4.
	//					utilVector.setCoe(UTIL_ALL, utilVector.getCoe(UTIL_LARGEST));
	//
	//					accessors.getWriteMethod().invoke(vdypSpecies, utilVector);
	//				}
	//			}
	//
	//			computeLayerUtilizationComponentsFromSpecies(vdypLayer);
	//
	//		} catch (IllegalAccessException | InvocationTargetException ex) {
	//			throw new IllegalStateException(ex);
	//		}
	//	}
	//
	//	/**
	//	 * Sets the Layer's utilization components based on those of its species.
	//	 *
	//	 * @param vdypLayer
	//	 */
	//	private void computeLayerUtilizationComponentsFromSpecies(VdypLayer vdypLayer) {
	//		// Layer utilization vectors other than quadratic mean diameter are the pairwise
	//		// sums of those of their species
	//		sumSpeciesUtilizationVectorsToLayer(vdypLayer);
	//
	//		// Quadratic mean diameter for the layer is computed from the BA and TPH after
	//		// they have been found from the species
	//		{
	//			var utilVector = vdypLayer.getBaseAreaByUtilization()
	//					.pairwise(vdypLayer.getTreesPerHectareByUtilization(), VDYPForwardStart::quadMeanDiameter);
	//			vdypLayer.setQuadraticMeanDiameterByUtilization(utilVector);
	//		}
	//	}
	//
	//	// TODO De-reflectify this when we want to make it work in GralVM
	//	private void sumSpeciesUtilizationVectorsToLayer(VdypLayer vdypLayer) throws IllegalStateException {
	//		try {
	//			for (var accessors : SUMMABLE_UTILIZATION_VECTOR_ACCESSORS) {
	//				var utilVector = utilizationVector();
	//				for (var vdypSpecies : vdypLayer.getSpecies().values()) {
	//					var speciesVector = (Coefficients) accessors.getReadMethod().invoke(vdypSpecies);
	//					utilVector.pairwiseInPlace(speciesVector, (x, y) -> x + y);
	//				}
	//				accessors.getWriteMethod().invoke(vdypLayer, utilVector);
	//			}
	//		} catch (IllegalAccessException | InvocationTargetException ex) {
	//			throw new IllegalStateException(ex);
	//		}
	//	}
	//
	//	// TODO De-reflectify this when we want to make it work in GralVM
	//	private void scaleAllSummableUtilization(VdypUtilizationHolder holder, float factor) throws IllegalStateException {
	//		try {
	//			for (var accessors : SUMMABLE_UTILIZATION_VECTOR_ACCESSORS) {
	//				((Coefficients) accessors.getReadMethod().invoke(holder)).scalarInPlace(x -> x * factor);
	//			}
	//		} catch (IllegalAccessException | InvocationTargetException ex) {
	//			throw new IllegalStateException(ex);
	//		}
	//	}
	//
	//	/**
	//	 * Implements the three reconciliation modes for layer 1 as described in
	//	 * ipsjf120.doc
	//	 *
	//	 * @param baseAreaUtil
	//	 * @param treesPerHectareUtil
	//	 * @param quadMeanDiameterUtil
	//	 * @throws ProcessingException
	//	 */
	//	// YUC1R
	//	void reconcileComponents(
	//			Coefficients baseAreaUtil, Coefficients treesPerHectareUtil, Coefficients quadMeanDiameterUtil
	//	) throws ProcessingException {
	//		if (baseAreaUtil.getCoe(UTIL_ALL) == 0f) {
	//			UTIL_CLASSES.forEach(uc -> {
	//				treesPerHectareUtil.setCoe(uc.index, 0f);
	//				baseAreaUtil.setCoe(uc.index, 0f);
	//			});
	//			return;
	//		}
	//
	//		float tphSum = 0f;
	//		float baSum = 0f;
	//		for (var uc : UTIL_CLASSES) {
	//			tphSum += treesPerHectareUtil.getCoe(uc.index);
	//			baSum += baseAreaUtil.getCoe(uc.index);
	//		}
	//
	//		if (abs(baSum - baseAreaUtil.getCoe(UTIL_ALL)) / baSum > 0.00003) {
	//			throw new ProcessingException("Computed base areas for 7.5+ components do not sum to expected total");
	//		}
	//
	//		float dq0 = quadMeanDiameter(baseAreaUtil.getCoe(UTIL_ALL), treesPerHectareUtil.getCoe(UTIL_ALL));
	//
	//		if (dq0 < 7.5f) {
	//			throw new ProcessingException(
	//					"Quadratic mean diameter computed from total base area and trees per hectare is less than 7.5 cm"
	//			);
	//		}
	//
	//		float tphSumHigh = (float) UTIL_CLASSES.stream()
	//				.mapToDouble(uc -> treesPerHectare(baseAreaUtil.getCoe(uc.index), uc.lowBound)).sum();
	//
	//		if (tphSumHigh < treesPerHectareUtil.getCoe(UTIL_ALL)) {
	//			reconcileComponentsMode1(baseAreaUtil, treesPerHectareUtil, quadMeanDiameterUtil, tphSumHigh);
	//		} else {
	//			reconcileComponentsMode2Check(baseAreaUtil, treesPerHectareUtil, quadMeanDiameterUtil);
	//		}
	//
	//	}
	//
	//	private static final List<UtilizationClass> MODE_1_RECONCILE_AVAILABILITY_CLASSES = List
	//			.of(UtilizationClass.OVER225, UtilizationClass.U175TO225, UtilizationClass.U125TO175);
	//
	//	void reconcileComponentsMode1(
	//			Coefficients baseAreaUtil, Coefficients treesPerHectareUtil, Coefficients quadMeanDiameterUtil,
	//			float tphSumHigh
	//	) {
	//		// MODE 1
	//
	//		// the high sum of TPH's is too low. Need MODE 1 reconciliation MUST set DQU's
	//		// to lowest allowable values AND must move BA from upper classes to lower
	//		// classes.
	//
	//		float tphNeed = treesPerHectareUtil.getCoe(UTIL_ALL) - tphSumHigh;
	//
	//		UTIL_CLASSES.forEach(uc -> quadMeanDiameterUtil.setCoe(uc.index, uc.lowBound));
	//
	//		for (var uc : MODE_1_RECONCILE_AVAILABILITY_CLASSES) {
	//			float tphAvail = treesPerHectare(baseAreaUtil.getCoe(uc.index), uc.previous().get().lowBound)
	//					- treesPerHectare(baseAreaUtil.getCoe(uc.index), uc.lowBound);
	//
	//			if (tphAvail < tphNeed) {
	//				baseAreaUtil.scalarInPlace(uc.previous().get().index, x -> x + baseAreaUtil.getCoe(uc.index));
	//				baseAreaUtil.setCoe(uc.index, 0f);
	//				tphNeed -= tphAvail;
	//			} else {
	//				float baseAreaMove = baseAreaUtil.getCoe(uc.index) * tphNeed / tphAvail;
	//				baseAreaUtil.scalarInPlace(uc.previous().get().index, x -> x + baseAreaMove);
	//				baseAreaUtil.scalarInPlace(uc.index, x -> x - baseAreaMove);
	//				break;
	//			}
	//		}
	//		UTIL_CLASSES.forEach(
	//				uc -> treesPerHectareUtil.setCoe(
	//						uc.index, treesPerHectare(baseAreaUtil.getCoe(uc.index), quadMeanDiameterUtil.getCoe(uc.index))
	//				)
	//		);
	//	}
	//
	//	void reconcileComponentsMode2Check(
	//			Coefficients baseAreaUtil, Coefficients treesPerHectareUtil, Coefficients quadMeanDiameterUtil
	//	) throws ProcessingException {
	//		// Before entering mode 2, check to see if reconciliation is already adequate
	//
	//		float tphSum = (float) UTIL_CLASSES.stream().mapToDouble(uc -> treesPerHectareUtil.getCoe(uc.index)).sum();
	//
	//		if (abs(tphSum - treesPerHectareUtil.getCoe(UTIL_ALL)) / tphSum > 0.00001) {
	//			reconcileComponentsMode2(baseAreaUtil, treesPerHectareUtil, quadMeanDiameterUtil);
	//			return;
	//		}
	//		for (var uc : UTIL_CLASSES) {
	//			if (baseAreaUtil.getCoe(uc.index) > 0f) {
	//				if (treesPerHectareUtil.getCoe(uc.index) <= 0f) {
	//					reconcileComponentsMode2(baseAreaUtil, treesPerHectareUtil, quadMeanDiameterUtil);
	//					return;
	//				}
	//				float dWant = quadMeanDiameter(baseAreaUtil.getCoe(uc.index), treesPerHectareUtil.getCoe(uc.index));
	//				float dqI = quadMeanDiameterUtil.getCoe(uc.index);
	//				if (dqI >= uc.lowBound && dqI <= uc.highBound && abs(dWant - dqI) < 0.00001) {
	//					return;
	//				}
	//			}
	//		}
	//
	//	}
	//
	//	private void reconcileComponentsMode2(
	//			Coefficients baseAreaUtil, Coefficients treesPerHectareUtil, Coefficients quadMeanDiameterUtil
	//	) throws ProcessingException {
	//		int n = 0;
	//		float baseAreaFixed = 0f;
	//		float treesPerHectareFixed = 0f;
	//		var quadMeanDiameterLimit = new boolean[] { false, false, false, false, false };
	//		Coefficients dqTrial = utilizationVector();
	//
	//		while (true) {
	//			n++;
	//
	//			if (n > 4) {
	//				throw new ProcessingException("Mode 2 component reconciliation iterations exceeded 4");
	//			}
	//
	//			float sum = (float) UTIL_CLASSES.stream().mapToDouble(uc -> {
	//				float baI = baseAreaUtil.getCoe(uc.index);
	//				float dqI = quadMeanDiameterUtil.getCoe(uc.index);
	//				if (baI != 0 && !quadMeanDiameterLimit[uc.index]) {
	//					return baI / (dqI * dqI);
	//				}
	//				return 0;
	//			}).sum();
	//
	//			float baAll = baseAreaUtil.getCoe(UTIL_ALL) - baseAreaFixed;
	//			float tphAll = treesPerHectareUtil.getCoe(UTIL_ALL) - treesPerHectareFixed;
	//
	//			if (baAll <= 0f || tphAll <= 0f) {
	//				reconcileComponentsMode3(baseAreaUtil, treesPerHectareUtil, quadMeanDiameterUtil);
	//				return;
	//			}
	//
	//			float dqAll = quadMeanDiameter(baAll, tphAll);
	//
	//			float k = dqAll * dqAll / baAll * sum;
	//			float sqrtK = sqrt(k);
	//
	//			for (var uc : UTIL_CLASSES) {
	//				if (!quadMeanDiameterLimit[uc.index] && baseAreaUtil.getCoe(uc.index) > 0f) {
	//					dqTrial.setCoe(uc.index, quadMeanDiameterUtil.getCoe(uc.index) * sqrtK);
	//				}
	//			}
	//
	//			UtilizationClass violateClass = null;
	//			float violate = 0f;
	//			boolean violateLow = false;
	//
	//			for (var uc : UTIL_CLASSES) {
	//				if (baseAreaUtil.getCoe(uc.index) > 0f) {
	//					if (dqTrial.getCoe(uc.index) < uc.lowBound) {
	//						float vi = 1f - dqTrial.getCoe(uc.index) / uc.lowBound;
	//						if (vi > violate) {
	//							violate = vi;
	//							violateClass = uc;
	//							violateLow = true;
	//						}
	//					}
	//				}
	//				if (dqTrial.getCoe(uc.index) > uc.highBound) {
	//					float vi = dqTrial.getCoe(uc.index) / uc.highBound - 1f;
	//					if (vi > violate) {
	//						violate = vi;
	//						violateClass = uc;
	//						violateLow = false;
	//					}
	//				}
	//			}
	//			if (violateClass == null)
	//				break;
	//			// Move the worst offending DQ to its limit
	//			dqTrial.setCoe(violateClass.index, violateLow ? violateClass.lowBound : violateClass.highBound);
	//
	//			quadMeanDiameterLimit[violateClass.index] = true;
	//			baseAreaFixed += baseAreaUtil.getCoe(violateClass.index);
	//			treesPerHectareFixed += treesPerHectare(
	//					baseAreaUtil.getCoe(violateClass.index), dqTrial.getCoe(violateClass.index)
	//			);
	//		}
	//
	//		// Make BA's agree with DQ's and TPH's
	//		for (var uc : UTIL_CLASSES) {
	//			quadMeanDiameterUtil.setCoe(uc.index, dqTrial.getCoe(uc.index));
	//			treesPerHectareUtil.setCoe(
	//					uc.index, treesPerHectare(baseAreaUtil.getCoe(uc.index), quadMeanDiameterUtil.getCoe(uc.index))
	//			);
	//		}
	//		// RE VERIFY That sums are correct
	//		float baSum = (float) UTIL_CLASSES.stream().mapToDouble(uc -> baseAreaUtil.getCoe(uc.index)).sum();
	//		float tphSum = (float) UTIL_CLASSES.stream().mapToDouble(uc -> treesPerHectareUtil.getCoe(uc.index)).sum();
	//		if (abs(baSum - baseAreaUtil.getCoe(UTIL_ALL)) / baSum > 0.0002) {
	//			throw new ProcessingException("Failed to reconcile Base Area");
	//		}
	//		if (abs(tphSum - treesPerHectareUtil.getCoe(UTIL_ALL)) / tphSum > 0.0002) {
	//			throw new ProcessingException("Failed to reconcile Trees per Hectare");
	//		}
	//	}
	//
	//	void reconcileComponentsMode3(
	//			Coefficients baseAreaUtil, Coefficients treesPerHectareUtil, Coefficients quadMeanDiameterUtil
	//	) {
	//
	//		/*
	//		 * Reconciliation mode 3 NOT IN THE ORIGINAL DESIGN The primary motivation for
	//		 * this mode is an example where all trees were in a signle utilization class
	//		 * and had a DQ of 12.4 cm. BUT the true DQ for the stand was slightly over
	//		 * 12.5. In this case the best solution is to simply reassign all trees to the
	//		 * single most appropriate class.
	//		 *
	//		 * Note, "original design" means something pre-VDYP 7. This was added to the
	//		 * Fortran some time before the port to Java including the comment above.
	//		 */
	//		UTIL_CLASSES.forEach(uc -> {
	//			baseAreaUtil.setCoe(uc.index, 0f);
	//			treesPerHectareUtil.setCoe(uc.index, 0f);
	//			quadMeanDiameterUtil.setCoe(uc.index, uc.lowBound + 2.5f);
	//		});
	//
	//		var ucToUpdate = UTIL_CLASSES.stream().filter(uc -> quadMeanDiameterUtil.getCoe(UTIL_ALL) < uc.highBound)
	//				.findFirst().get();
	//
	//		baseAreaUtil.setCoe(ucToUpdate.index, baseAreaUtil.getCoe(UTIL_ALL));
	//		treesPerHectareUtil.setCoe(ucToUpdate.index, treesPerHectareUtil.getCoe(UTIL_ALL));
	//		quadMeanDiameterUtil.setCoe(ucToUpdate.index, quadMeanDiameterUtil.getCoe(UTIL_ALL));
	//	}
	//
	//	// EMP070
	//	void estimateBaseAreaByUtilization(
	//			BecDefinition bec, Coefficients quadMeanDiameterUtil, Coefficients baseAreaUtil, VdypSpecies spec
	//	) throws ProcessingException {
	//		final var coeMap = Utils.<MatrixMap3<Integer, String, String, Coefficients>>expectParsedControl(
	//				controlMap, UtilComponentBaseAreaParser.CONTROL_KEY, MatrixMap3.class
	//		);
	//
	//		float dq = quadMeanDiameterUtil.getCoe(UTIL_ALL);
	//		var b = utilizationVector();
	//		b.setCoe(0, baseAreaUtil.getCoe(UTIL_ALL));
	//		for (int i = 1; i < UTIL_LARGEST; i++) {
	//			var coe = coeMap.get(i, spec.getGenus(), bec.getGrowthBec().getAlias());
	//
	//			float a0 = coe.getCoe(1);
	//			float a1 = coe.getCoe(2);
	//
	//			float logit;
	//			if (i == 1) {
	//				logit = a0 + a1 * pow(dq, 0.25f);
	//			} else {
	//				logit = a0 + a1 * dq;
	//			}
	//			b.setCoe(i, b.getCoe(i - 1) * exponentRatio(logit));
	//			if (i == 1 && quadMeanDiameterUtil.getCoe(UTIL_ALL) < 12.5f) {
	//				float ba12Max = (1f - pow(
	//						(quadMeanDiameterUtil.getCoe(1) - 7.4f) / (quadMeanDiameterUtil.getCoe(UTIL_ALL) - 7.4f), 2f
	//				)) * b.getCoe(0);
	//				b.scalarInPlace(1, x -> min(x, ba12Max));
	//			}
	//		}
	//
	//		baseAreaUtil.setCoe(1, baseAreaUtil.getCoe(UTIL_ALL) - b.getCoe(1));
	//		baseAreaUtil.setCoe(2, b.getCoe(1) - b.getCoe(2));
	//		baseAreaUtil.setCoe(3, b.getCoe(2) - b.getCoe(3));
	//		baseAreaUtil.setCoe(4, b.getCoe(3));
	//	}
	//
	//	/**
	//	 * Estimate DQ by utilization class, see ipsjf120.doc
	//	 *
	//	 * @param bec
	//	 * @param quadMeanDiameterUtil
	//	 * @param spec
	//	 * @throws ProcessingException
	//	 */
	//	// EMP071
	//	void estimateQuadMeanDiameterByUtilization(BecDefinition bec, Coefficients quadMeanDiameterUtil, VdypSpecies spec)
	//			throws ProcessingException {
	//		log.atTrace().setMessage("Estimate DQ by utilization class for {} in BEC {}.  DQ for all >7.5 is {}")
	//				.addArgument(spec.getGenus()).addArgument(bec.getName())
	//				.addArgument(quadMeanDiameterUtil.getCoe(UTIL_ALL));
	//
	//		float quadMeanDiameter07 = quadMeanDiameterUtil.getCoe(UTIL_ALL);
	//
	//		for (var uc : UTIL_CLASSES) {
	//			log.atDebug().setMessage("For util level {}").addArgument(uc.name);
	//			final var coeMap = Utils.<MatrixMap3<Integer, String, String, Coefficients>>expectParsedControl(
	//					controlMap, UtilComponentDQParser.CONTROL_KEY, MatrixMap3.class
	//			);
	//			var coe = coeMap.get(uc.index, spec.getGenus(), bec.getGrowthBec().getAlias());
	//
	//			float a0 = coe.getCoe(1);
	//			float a1 = coe.getCoe(2);
	//			float a2 = coe.getCoe(3);
	//
	//			log.atDebug().setMessage("a0={}, a1={}, a3={}").addArgument(a0).addArgument(a1).addArgument(a2);
	//
	//			float logit;
	//
	//			switch (uc) {
	//			case U75TO125:
	//				if (quadMeanDiameter07 < 7.5001f) {
	//					quadMeanDiameterUtil.setCoe(UTIL_ALL, 7.5f);
	//				} else {
	//					log.atDebug().setMessage("DQ = 7.5 + a0 * (1 - exp(a1 / a0*(DQ07 - 7.5) ))**a2' )");
	//
	//					logit = a1 / a0 * (quadMeanDiameter07 - 7.5f);
	//
	//					quadMeanDiameterUtil
	//							.setCoe(uc.index, min(7.5f + a0 * pow(1 - safeExponent(logit), a2), quadMeanDiameter07));
	//				}
	//				break;
	//			case U125TO175, U175TO225:
	//				log.atDebug().setMessage(
	//						"LOGIT = a0 + a1*(SQ07 / 7.5)**a2,  DQ = (12.5 or 17.5) + 5 * exp(LOGIT) / (1 + exp(LOGIT))"
	//				);
	//				logit = a0 + a1 * pow(quadMeanDiameter07 / 7.5f, a2);
	//
	//				quadMeanDiameterUtil.setCoe(uc.index, uc.lowBound + 5f * exponentRatio(logit));
	//				break;
	//			case OVER225:
	//				float a3 = coe.getCoe(4);
	//
	//				log.atDebug().setMessage(
	//						"Coeff A3 {}, LOGIT = a2 + a1*DQ07**a3,  DQ = DQ07 + a0 * (1 - exp(LOGIT) / (1 + exp(LOGIT)) )"
	//				);
	//
	//				logit = a2 + a1 * pow(quadMeanDiameter07, a3);
	//
	//				quadMeanDiameterUtil
	//						.setCoe(uc.index, max(22.5f, quadMeanDiameter07 + a0 * (1f - exponentRatio(logit))));
	//				break;
	//			case ALL, SMALL:
	//				throw new IllegalStateException(
	//						"Should not be attempting to process small component or all large components"
	//				);
	//			}
	//
	//			log.atDebug().setMessage("Util DQ for class {} is {}").addArgument(uc.name)
	//					.addArgument(quadMeanDiameterUtil.getCoe(uc.index));
	//		}
	//
	//		log.atTrace().setMessage("Estimated Diameters {}").addArgument(
	//				() -> UTIL_CLASSES.stream()
	//						.map(uc -> String.format("%s: %d", uc.name, quadMeanDiameterUtil.getCoe(uc.index)))
	//		);
	//
	//	}
	//
	//	static float exponentRatio(float logit) throws ProcessingException {
	//		float exp = safeExponent(logit);
	//		return exp / (1f + exp);
	//	}
	//
	//	static float safeExponent(float logit) throws ProcessingException {
	//		if (logit > 88f) {
	//			throw new ProcessingException("logit " + logit + " exceeds 88");
	//		}
	//		return exp(logit);
	//	}
	//
	//	/**
	//	 * Returns the primary, and secondary if present species records as a one or two
	//	 * element list.
	//	 */
	//	// PRIMFIND
	//	List<FipSpecies> findPrimarySpecies(Map<String, FipSpecies> allSpecies) {
	//		if (allSpecies.isEmpty()) {
	//			throw new IllegalArgumentException("Can not find primary species as there are no species");
	//		}
	//		var result = new ArrayList<FipSpecies>(2);
	//
	//		// Start with a deep copy of the species map so there are no side effects from
	//		// the manipulation this method does.
	//		var combined = new HashMap<String, FipSpecies>(allSpecies.size());
	//		allSpecies.entrySet().stream().forEach(spec -> {
	//			combined.put(spec.getKey(), new FipSpecies(spec.getValue()));
	//		});
	//
	//		for (var combinationGroup : PRIMARY_SPECIES_TO_COMBINE) {
	//			var groupSpecies = combinationGroup.stream().map(combined::get).filter(Objects::nonNull).toList();
	//			if (groupSpecies.size() < 2) {
	//				continue;
	//			}
	//			var groupPrimary = new FipSpecies(groupSpecies.stream().sorted(PERCENT_GENUS_DESCENDING).findFirst().get());
	//			var total = (float) groupSpecies.stream().mapToDouble(FipSpecies::getPercentGenus).sum();
	//			combinationGroup.forEach(combined::remove);
	//			groupPrimary.setPercentGenus(total);
	//			combined.put(groupPrimary.getGenus(), groupPrimary);
	//		}
	//
	//		assert !combined.isEmpty();
	//
	//		if (combined.size() == 1) {
	//			// There's only one
	//			result.addAll(combined.values());
	//		} else {
	//			var NDEBUG_22 = false;
	//			if (NDEBUG_22) {
	//				// TODO
	//				throw new UnsupportedOperationException();
	//			} else {
	//				combined.values().stream().sorted(PERCENT_GENUS_DESCENDING).limit(2).forEach(result::add);
	//			}
	//		}
	//
	//		assert !result.isEmpty();
	//		assert result.size() <= 2;
	//		return result;
	//	}
	//
	//	/**
	//	 * Find Inventory type group (ITG)
	//	 *
	//	 * @param primarySecondary
	//	 * @return
	//	 * @throws ProcessingException
	//	 */
	//	// ITGFIND
	//	int findItg(List<FipSpecies> primarySecondary) throws ProcessingException {
	//		var primary = primarySecondary.get(0);
	//
	//		if (primary.getPercentGenus() > 79.999) { // Copied from VDYP7
	//			return ITG_PURE.get(primary.getGenus());
	//		}
	//		assert primarySecondary.size() == 2;
	//
	//		var secondary = primarySecondary.get(1);
	//
	//		assert !primary.getGenus().equals(secondary.getGenus());
	//
	//		switch (primary.getGenus()) {
	//		case "F":
	//			switch (secondary.getGenus()) {
	//			case "C", "Y":
	//				return 2;
	//			case "B", "H":
	//				return 3;
	//			case "S":
	//				return 4;
	//			case "PL", "PA":
	//				return 5;
	//			case "PY":
	//				return 6;
	//			case "L", "PW":
	//				return 7;
	//			default:
	//				return 8;
	//			}
	//		case "C", "Y":
	//			switch (secondary.getGenus()) {
	//			case "C", "Y":
	//				return 10;
	//			case "H", "B", "S":
	//				return 11;
	//			default:
	//				return 10;
	//			}
	//		case "B":
	//			switch (secondary.getGenus()) {
	//			case "C", "Y", "H":
	//				return 19;
	//			default:
	//				return 20;
	//			}
	//		case "S":
	//			switch (secondary.getGenus()) {
	//			case "C", "Y", "H":
	//				return 23;
	//			case "B":
	//				return 24;
	//			case "PL":
	//				return 25;
	//			default:
	//				if (HARDWOODS.contains(secondary.getGenus())) {
	//					return 26;
	//				}
	//				return 22;
	//			}
	//		case "PW":
	//			return 27;
	//		case "PL", "PA":
	//			switch (secondary.getGenus()) {
	//			case "PL", "PA":
	//				return 28;
	//			case "F", "PW", "L", "PY":
	//				return 29;
	//			default:
	//				if (HARDWOODS.contains(secondary.getGenus())) {
	//					return 31;
	//				}
	//				return 30;
	//			}
	//		case "PY":
	//			return 32;
	//		case "L":
	//			switch (secondary.getGenus()) {
	//			case "F":
	//				return 33;
	//			default:
	//				return 34;
	//			}
	//		case "AC":
	//			if (HARDWOODS.contains(secondary.getGenus())) {
	//				return 36;
	//			}
	//			return 35;
	//		case "D":
	//			if (HARDWOODS.contains(secondary.getGenus())) {
	//				return 38;
	//			}
	//			return 37;
	//		case "MB":
	//			return 39;
	//		case "E":
	//			return 40;
	//		case "AT":
	//			if (HARDWOODS.contains(secondary.getGenus())) {
	//				return 42;
	//			}
	//			return 41;
	//		default:
	//			throw new ProcessingException("Unexpected primary species: " + primary.getGenus());
	//		}
	//	}
	//
	//	// GRPBA1FD
	//	int findBaseAreaGroup(FipSpecies fipSpecies, BecDefinition bec, int itg) {
	//		var growthBec = bec.getGrowthBec();
	//		final var defaultGroupsMap = Utils.<MatrixMap2<String, String, Integer>>expectParsedControl(
	//				controlMap, DefaultEquationNumberParser.CONTROL_KEY, MatrixMap2.class
	//		);
	//		final var modifierMap = Utils.<MatrixMap2<Integer, Integer, Optional<Integer>>>expectParsedControl(
	//				controlMap, EquationModifierParser.CONTROL_KEY, MatrixMap2.class
	//		);
	//		var defaultGroup = defaultGroupsMap.get(fipSpecies.getGenus(), growthBec.getAlias());
	//		return modifierMap.getM(defaultGroup, itg).orElse(defaultGroup);
	//	}
	//
	//	private float heightMultiplier(String genus, Region region, float treesPerHectarePrimary) {
	//		final var coeMap = Utils.<MatrixMap2<String, Region, Optional<Coefficients>>>expectParsedControl(
	//				controlMap, HLCoefficientParser.CONTROL_KEY_P1, MatrixMap2.class
	//		);
	//		var coe = coeMap.get(genus, region).orElse(Coefficients.empty(HLCoefficientParser.NUM_COEFFICIENTS_P1, 1))
	//				.reindex(0);
	//		return coe.get(0) - coe.getCoe(1) + coe.getCoe(1) * exp(coe.getCoe(2) * (treesPerHectarePrimary - 100f));
	//	}
	//
	//	// EMP050 Meth==1
	//	/**
	//	 * Return the lorey height of the primary species based on the dominant height
	//	 * of the lead species.
	//	 *
	//	 * @param leadHeight             dominant height of the lead species
	//	 * @param genus                  Primary species
	//	 * @param region                 Region of the polygon
	//	 * @param treesPerHectarePrimary trees per hectare >7.5 cm of the primary
	//	 *                               species
	//	 * @return
	//	 */
	//	float primaryHeightFromLeadHeight(float leadHeight, String genus, Region region, float treesPerHectarePrimary) {
	//		return 1.3f + (leadHeight - 1.3f) * heightMultiplier(genus, region, treesPerHectarePrimary);
	//	}
	//
	//	// EMP050 Meth==2
	//	/**
	//	 * Return the dominant height of the lead species based on the lorey height of
	//	 * the primary species.
	//	 *
	//	 * @param primaryHeight          lorey height of the primary species
	//	 * @param genus                  Primary species
	//	 * @param region                 Region of the polygon
	//	 * @param treesPerHectarePrimary trees per hectare >7.5 cm of the primary
	//	 *                               species
	//	 * @return
	//	 */
	//	float leadHeightFromPrimaryHeight(float primaryHeight, String genus, Region region, float treesPerHectarePrimary) {
	//		return 1.3f + (primaryHeight - 1.3f) / heightMultiplier(genus, region, treesPerHectarePrimary);
	//	}
	//
	//	// EMP051
	//	/**
	//	 * Return the lorey height of the primary species based on the dominant height
	//	 * of the lead species.
	//	 *
	//	 * @param leadHeight dominant height of the lead species
	//	 * @param genus      Primary species
	//	 * @param region     Region of the polygon
	//	 * @return
	//	 */
	//	private float primaryHeightFromLeadHeightInitial(float leadHeight, String genus, Region region) {
	//		final var coeMap = Utils.<MatrixMap2<String, Region, Optional<Coefficients>>>expectParsedControl(
	//				controlMap, HLCoefficientParser.CONTROL_KEY_P2, MatrixMap2.class
	//		);
	//		var coe = coeMap.get(genus, region).orElse(Coefficients.empty(HLCoefficientParser.NUM_COEFFICIENTS_P2, 1));
	//		return 1.3f + coe.getCoe(1) * pow(leadHeight - 1.3f, coe.getCoe(2));
	//	}
	//
	//	/**
	//	 * Accessor methods for utilization vectors, except for Lorey Height, on Layer
	//	 * and Species objects.
	//	 */
	//	static final Collection<PropertyDescriptor> UTILIZATION_VECTOR_ACCESSORS;
	//
	//	/**
	//	 * Accessor methods for utilization vectors, except for Lorey Height and
	//	 * Quadratic Mean Diameter, on Layer and Species objects. These are properties
	//	 * where the values for the layer are the sum of those for its species.
	//	 */
	//	static final Collection<PropertyDescriptor> SUMMABLE_UTILIZATION_VECTOR_ACCESSORS;
	//
	//	/**
	//	 * Accessor methods for utilization vectors, except for Lorey Height,and Volume
	//	 * on Layer and Species objects.
	//	 */
	//	static final Collection<PropertyDescriptor> NON_VOLUME_UTILIZATION_VECTOR_ACCESSORS;
	//
	//	private static final float LOW_CROWN_CLOSURE = 10f;
	//
	//	static {
	//		try {
	//			var bean = Introspector.getBeanInfo(VdypUtilizationHolder.class);
	//			UTILIZATION_VECTOR_ACCESSORS = Arrays.stream(bean.getPropertyDescriptors()) //
	//					.filter(p -> p.getName().endsWith("ByUtilization")) //
	//					.filter(p -> !p.getName().startsWith("loreyHeight")) //
	//					.filter(p -> p.getPropertyType() == Coefficients.class) //
	//					.toList();
	//		} catch (IntrospectionException e) {
	//			throw new IllegalStateException(e);
	//		}
	//
	//		SUMMABLE_UTILIZATION_VECTOR_ACCESSORS = UTILIZATION_VECTOR_ACCESSORS.stream()
	//				.filter(x -> !x.getName().startsWith("quadraticMeanDiameter")).toList();
	//
	//		NON_VOLUME_UTILIZATION_VECTOR_ACCESSORS = UTILIZATION_VECTOR_ACCESSORS.stream()
	//				.filter(x -> !x.getName().contains("Volume")).toList();
	//	}
	//
	//	// EMP091
	//	/**
	//	 * Updates wholeStemVolumeUtil with estimated values.
	//	 */
	//	void estimateWholeStemVolume(
	//			UtilizationClass utilizationClass, float adjustCloseUtil, int volumeGroup, Float hlSp,
	//			Coefficients quadMeanDiameterUtil, Coefficients baseAreaUtil, Coefficients wholeStemVolumeUtil
	//	) throws ProcessingException {
	//		var dqSp = quadMeanDiameterUtil.getCoe(UTIL_ALL);
	//		final var wholeStemUtilizationComponentMap = Utils
	//				.<MatrixMap2<Integer, Integer, Optional<Coefficients>>>expectParsedControl(
	//						controlMap, UtilComponentWSVolumeParser.CONTROL_KEY, MatrixMap2.class
	//				);
	//		estimateUtilization(baseAreaUtil, wholeStemVolumeUtil, utilizationClass, (uc, ba) -> {
	//			Coefficients wholeStemCoe = wholeStemUtilizationComponentMap.get(uc.index, volumeGroup).orElseThrow(
	//					() -> new ProcessingException(
	//							"Could not find whole stem utilization coefficients for group " + volumeGroup
	//					)
	//			);
	//
	//			// Fortran code uses 1 index into array when reading it here, but 0 index when
	//			// writing into it in the parser. I use 0 for both.
	//			var a0 = wholeStemCoe.getCoe(0);
	//			var a1 = wholeStemCoe.getCoe(1);
	//			var a2 = wholeStemCoe.getCoe(2);
	//			var a3 = wholeStemCoe.getCoe(3);
	//
	//			var arg = a0 + a1 * log(hlSp) + a2 * log(quadMeanDiameterUtil.getCoe(uc.index))
	//					+ ( (uc != UtilizationClass.OVER225) ? a3 * log(dqSp) : a3 * dqSp);
	//
	//			if (uc == utilizationClass) {
	//				arg += adjustCloseUtil;
	//			}
	//
	//			var vbaruc = exp(arg); // volume base area ?? utilization class?
	//
	//			return ba * vbaruc;
	//		}, x -> x < 0f, 0f);
	//
	//		if (utilizationClass == UtilizationClass.ALL) {
	//			normalizeUtilizationComponents(wholeStemVolumeUtil);
	//		}
	//
	//	}
	//
	//	// EMP092
	//	/**
	//	 * Updates closeUtilizationVolumeUtil with estimated values.
	//	 *
	//	 * @throws ProcessingException
	//	 */
	//	void estimateCloseUtilizationVolume(
	//			UtilizationClass utilizationClass, Coefficients aAdjust, int volumeGroup, float hlSp,
	//			Coefficients quadMeanDiameterUtil, Coefficients wholeStemVolumeUtil, Coefficients closeUtilizationVolumeUtil
	//	) throws ProcessingException {
	//		var dqSp = quadMeanDiameterUtil.getCoe(UTIL_ALL);
	//		final var closeUtilizationCoeMap = Utils
	//				.<MatrixMap2<Integer, Integer, Optional<Coefficients>>>expectParsedControl(
	//						controlMap, CloseUtilVolumeParser.CONTROL_KEY, MatrixMap2.class
	//				);
	//		estimateUtilization(wholeStemVolumeUtil, closeUtilizationVolumeUtil, utilizationClass, (uc, ws) -> {
	//			Coefficients closeUtilCoe = closeUtilizationCoeMap.get(uc.index, volumeGroup).orElseThrow(
	//					() -> new ProcessingException(
	//							"Could not find whole stem utilization coefficients for group " + volumeGroup
	//					)
	//			);
	//			var a0 = closeUtilCoe.getCoe(1);
	//			var a1 = closeUtilCoe.getCoe(2);
	//			var a2 = closeUtilCoe.getCoe(3);
	//
	//			var arg = a0 + a1 * quadMeanDiameterUtil.getCoe(uc.index) + a2 * hlSp + aAdjust.getCoe(uc.index);
	//
	//			float ratio = ratio(arg, 7.0f);
	//
	//			return ws * ratio;
	//		});
	//
	//		if (utilizationClass == UtilizationClass.ALL) {
	//			storeSumUtilizationComponents(closeUtilizationVolumeUtil);
	//		}
	//	}
	//
	//	@FunctionalInterface
	//	static interface UtilizationProcessor {
	//		float apply(UtilizationClass utilizationClass, float inputValue) throws ProcessingException;
	//	}
	//
	//	/**
	//	 * Estimate values for one utilization vector from another
	//	 *
	//	 * @param input            source utilization
	//	 * @param output           result utilization
	//	 * @param utilizationClass the utilization class for which to do the
	//	 *                         computation, UTIL_ALL for all of them.
	//	 * @param processor        Given a utilization class, and the source utilization
	//	 *                         for that class, return the result utilization
	//	 * @throws ProcessingException
	//	 */
	//	static void estimateUtilization(
	//			Coefficients input, Coefficients output, UtilizationClass utilizationClass, UtilizationProcessor processor
	//	) throws ProcessingException {
	//		estimateUtilization(input, output, utilizationClass, processor, x -> false, 0f);
	//	}
	//
	//	/**
	//	 * Estimate values for one utilization vector from another
	//	 *
	//	 * @param input            source utilization
	//	 * @param output           result utilization
	//	 * @param utilizationClass the utilization class for which to do the
	//	 *                         computation, UTIL_ALL for all of them.
	//	 * @param processor        Given a utilization class, and the source utilization
	//	 *                         for that class, return the result utilization
	//	 * @param skip             a utilization class will be skipped and the result
	//	 *                         set to the default value if this is true for the
	//	 *                         value of the source utilization
	//	 * @param defaultValue     the default value
	//	 * @throws ProcessingException
	//	 */
	//	static void estimateUtilization(
	//			Coefficients input, Coefficients output, UtilizationClass utilizationClass, UtilizationProcessor processor,
	//			Predicate<Float> skip, float defaultValue
	//	) throws ProcessingException {
	//		for (var uc : UTIL_CLASSES) {
	//			var inputValue = input.getCoe(uc.index);
	//
	//			// it seems like this should be done after checking i against utilizationClass,
	//			// which could just be done as part of the processor definition, but this is how
	//			// VDYP7 did it.
	//			if (skip.test(inputValue)) {
	//				output.setCoe(uc.index, defaultValue);
	//				continue;
	//			}
	//
	//			if (utilizationClass != UtilizationClass.ALL && utilizationClass != uc) {
	//				continue;
	//			}
	//
	//			var result = processor.apply(uc, input.getCoe(uc.index));
	//			output.setCoe(uc.index, result);
	//		}
	//	}
	//
	//	/**
	//	 * Estimate volume NET OF DECAY by (DBH) utilization classes
	//	 *
	//	 * @param utilizationClass
	//	 * @param aAdjust
	//	 * @param decayGroup
	//	 * @param lorieHeight
	//	 * @param ageBreastHeight
	//	 * @param quadMeanDiameterUtil
	//	 * @param closeUtilizationUtil
	//	 * @param closeUtilizationNetOfDecayUtil
	//	 * @throws ProcessingException
	//	 */
	//	// EMP093
	//	void estimateNetDecayVolume(
	//			String genus, Region region, UtilizationClass utilizationClass, Coefficients aAdjust, int decayGroup,
	//			float lorieHeight, float ageBreastHeight, Coefficients quadMeanDiameterUtil,
	//			Coefficients closeUtilizationUtil, Coefficients closeUtilizationNetOfDecayUtil
	//	) throws ProcessingException {
	//		var dqSp = quadMeanDiameterUtil.getCoe(UTIL_ALL);
	//		final var netDecayCoeMap = Utils.<MatrixMap2<Integer, Integer, Optional<Coefficients>>>expectParsedControl(
	//				controlMap, VolumeNetDecayParser.CONTROL_KEY, MatrixMap2.class
	//		);
	//		final var decayModifierMap = Utils.<MatrixMap2<String, Region, Float>>expectParsedControl(
	//				controlMap, ModifierParser.CONTROL_KEY_MOD301_DECAY, MatrixMap2.class
	//		);
	//
	//		final var ageTr = (float) Math.log(Math.max(20.0, ageBreastHeight));
	//
	//		estimateUtilization(closeUtilizationUtil, closeUtilizationNetOfDecayUtil, utilizationClass, (uc, cu) -> {
	//			Coefficients netDecayCoe = netDecayCoeMap.get(uc.index, decayGroup).orElseThrow(
	//					() -> new ProcessingException("Could not find net decay coefficients for group " + decayGroup)
	//			);
	//			var a0 = netDecayCoe.getCoe(1);
	//			var a1 = netDecayCoe.getCoe(2);
	//			var a2 = netDecayCoe.getCoe(3);
	//
	//			float arg;
	//			if (uc != UtilizationClass.OVER225) {
	//				arg = a0 + a1 * log(dqSp) + a2 * ageTr;
	//			} else {
	//				arg = a0 + a1 * log(quadMeanDiameterUtil.getCoe(uc.index)) + a2 * ageTr;
	//			}
	//
	//			arg += aAdjust.getCoe(uc.index) + decayModifierMap.get(genus, region);
	//
	//			float ratio = ratio(arg, 8.0f);
	//
	//			return cu * ratio;
	//		});
	//
	//		if (utilizationClass == UtilizationClass.ALL) {
	//			storeSumUtilizationComponents(closeUtilizationNetOfDecayUtil);
	//		}
	//	}
	//
	//	/**
	//	 * Estimate utilization net of decay and waste
	//	 */
	//	// EMP094
	//	void estimateNetDecayAndWasteVolume(
	//			Region region, UtilizationClass utilizationClass, Coefficients aAdjust, String genus, float lorieHeight,
	//			float ageBreastHeight, Coefficients quadMeanDiameterUtil, Coefficients closeUtilizationUtil,
	//			Coefficients closeUtilizationNetOfDecayUtil, Coefficients closeUtilizationNetOfDecayAndWasteUtil
	//	) throws ProcessingException {
	//		final var netDecayCoeMap = Utils.<Map<String, Coefficients>>expectParsedControl(
	//				controlMap, VolumeNetDecayWasteParser.CONTROL_KEY, Map.class
	//		);
	//		final var wasteModifierMap = Utils.<MatrixMap2<String, Region, Float>>expectParsedControl(
	//				controlMap, ModifierParser.CONTROL_KEY_MOD301_WASTE, MatrixMap2.class
	//		);
	//
	//		estimateUtilization(
	//				closeUtilizationNetOfDecayUtil, closeUtilizationNetOfDecayAndWasteUtil, utilizationClass,
	//				(i, netDecay) -> {
	//					if (Float.isNaN(netDecay) || netDecay <= 0f) {
	//						return 0f;
	//					}
	//
	//					Coefficients netWasteCoe = netDecayCoeMap.get(genus);
	//					if (netWasteCoe == null) {
	//						throw new ProcessingException("Could not find net waste coefficients for genus " + genus);
	//					}
	//
	//					var a0 = netWasteCoe.getCoe(0);
	//					var a1 = netWasteCoe.getCoe(1);
	//					var a2 = netWasteCoe.getCoe(2);
	//					var a3 = netWasteCoe.getCoe(3);
	//					var a4 = netWasteCoe.getCoe(4);
	//					var a5 = netWasteCoe.getCoe(5);
	//
	//					if (i == UtilizationClass.OVER225) {
	//						a0 += a5;
	//					}
	//					var frd = 1.0f - netDecay / closeUtilizationUtil.getCoe(i.index);
	//
	//					float arg = a0 + a1 * frd + a3 * log(quadMeanDiameterUtil.getCoe(i.index)) + a4 * log(lorieHeight);
	//
	//					arg += wasteModifierMap.get(genus, region);
	//
	//					arg = clamp(arg, -10f, 10f);
	//
	//					var frw = (1.0f - exp(a2 * frd)) * exp(arg) / (1f + exp(arg)) * (1f - frd);
	//					frw = min(frd, frw);
	//
	//					float result = closeUtilizationUtil.getCoe(i.index) * (1f - frd - frw);
	//
	//					/*
	//					 * Check for an apply adjustments. This is done after computing the result above
	//					 * to allow for clamping frw to frd
	//					 */
	//					if (aAdjust.getCoe(i.index) != 0f) {
	//						var ratio = result / netDecay;
	//						if (ratio < 1f && ratio > 0f) {
	//							arg = log(ratio / (1f - ratio));
	//							arg += aAdjust.getCoe(i.index);
	//							arg = clamp(arg, -10f, 10f);
	//							result = exp(arg) / (1f + exp(arg)) * netDecay;
	//						}
	//					}
	//
	//					return result;
	//				}
	//		);
	//
	//		if (utilizationClass == UtilizationClass.ALL) {
	//			storeSumUtilizationComponents(closeUtilizationNetOfDecayAndWasteUtil);
	//		}
	//	}
	//
	//	/**
	//	 * Estimate utilization net of decay, waste, and breakage
	//	 *
	//	 * @throws ProcessingException
	//	 */
	//	void estimateNetDecayWasteAndBreakageVolume(
	//			UtilizationClass utilizationClass, int breakageGroup, Coefficients quadMeanDiameterUtil,
	//			Coefficients closeUtilizationUtil, Coefficients closeUtilizationNetOfDecayAndWasteUtil,
	//			Coefficients closeUtilizationNetOfDecayWasteAndBreakageUtil
	//	) throws ProcessingException {
	//		final var netBreakageCoeMap = Utils
	//				.<Map<Integer, Coefficients>>expectParsedControl(controlMap, BreakageParser.CONTROL_KEY, Map.class);
	//		final var coefficients = netBreakageCoeMap.get(breakageGroup);
	//		if (coefficients == null) {
	//			throw new ProcessingException("Could not find net breakage coefficients for group " + breakageGroup);
	//		}
	//
	//		final var a1 = coefficients.getCoe(1);
	//		final var a2 = coefficients.getCoe(2);
	//		final var a3 = coefficients.getCoe(3);
	//		final var a4 = coefficients.getCoe(4);
	//
	//		estimateUtilization(
	//				closeUtilizationNetOfDecayAndWasteUtil, closeUtilizationNetOfDecayWasteAndBreakageUtil,
	//				utilizationClass, (uc, netWaste) -> {
	//
	//					if (netWaste <= 0f) {
	//						return 0f;
	//					}
	//					var percentBroken = a1 + a2 * log(quadMeanDiameterUtil.getCoe(uc.index));
	//					percentBroken = clamp(percentBroken, a3, a4);
	//					var broken = min(percentBroken / 100 * closeUtilizationUtil.getCoe(uc.index), netWaste);
	//					return netWaste - broken;
	//				}
	//		);
	//
	//		if (utilizationClass == UtilizationClass.ALL) {
	//			storeSumUtilizationComponents(closeUtilizationNetOfDecayWasteAndBreakageUtil);
	//		}
	//
	//	}
	//
	//	/**
	//	 * Sums the individual utilization components (1-4)
	//	 */
	//	float sumUtilizationComponents(Coefficients components) {
	//		return (float) UTIL_CLASSES.stream().mapToInt(x -> x.index).mapToDouble(components::getCoe).sum();
	//	}
	//
	//	/**
	//	 * Sums the individual utilization components (1-4) and stores the results in
	//	 * coefficient UTIL_ALL
	//	 */
	//	float storeSumUtilizationComponents(Coefficients components) {
	//		var sum = sumUtilizationComponents(components);
	//		components.setCoe(UTIL_ALL, sum);
	//		return sum;
	//	}
	//
	//	/**
	//	 * Normalizes the utilization components 1-4 so they sum to the value of
	//	 * component UTIL_ALL
	//	 *
	//	 * @throws ProcessingException if the sum is not positive
	//	 */
	//	float normalizeUtilizationComponents(Coefficients components) throws ProcessingException {
	//		var sum = sumUtilizationComponents(components);
	//		var k = components.getCoe(UTIL_ALL) / sum;
	//		if (sum <= 0f) {
	//			throw new ProcessingException("Total volume " + sum + " was not positive.");
	//		}
	//		UTIL_CLASSES.forEach(uc -> {
	//			components.setCoe(uc.index, components.getCoe(uc.index) * k);
	//		});
	//		return k;
	//	}
	//
	//	int getGroup(FipPolygon fipPolygon, MatrixMap2<String, String, Integer> volumeGroupMap, VdypSpecies vSpec) {
	//		return volumeGroupMap.get(vSpec.getGenus(), fipPolygon.getBiogeoclimaticZone());
	//	}
	//
	//	MatrixMap2<String, String, Integer> getGroupMap(String key) {
	//		return Utils.expectParsedControl(controlMap, key, MatrixMap2.class);
	//	}
	//
	//	// FIP_GET
	//	private FipPolygon getPolygon(
	//			StreamingParser<FipPolygon> polyStream, StreamingParser<Map<Layer, FipLayer>> layerStream,
	//			StreamingParser<Collection<FipSpecies>> speciesStream
	//	) throws ProcessingException, IOException, ResourceParseException {
	//
	//		log.trace("Getting polygon");
	//		var polygon = polyStream.next();
	//
	//		log.trace("Getting layers for polygon {}", polygon.getPolygonIdentifier());
	//		Map<Layer, FipLayer> layers;
	//		try {
	//			layers = layerStream.next();
	//		} catch (NoSuchElementException ex) {
	//			throw validationError("Layers file has fewer records than polygon file.", ex);
	//		}
	//
	//		log.trace("Getting species for polygon {}", polygon.getPolygonIdentifier());
	//		Collection<FipSpecies> species;
	//		try {
	//			species = speciesStream.next();
	//		} catch (NoSuchElementException ex) {
	//			throw validationError("Species file has fewer records than polygon file.", ex);
	//		}
	//
	//		// Validate that layers belong to the correct polygon
	//		for (FipLayer layer : layers.values()) {
	//			if (!layer.getPolygonIdentifier().equals(polygon.getPolygonIdentifier())) {
	//				throw validationError(
	//						"Record in layer file contains layer for polygon %s when expecting one for %s.",
	//						layer.getPolygonIdentifier(), polygon.getPolygonIdentifier()
	//				);
	//			}
	//			layer.setSpecies(new HashMap<>());
	//		}
	//
	//		for (var spec : species) {
	//			var layer = layers.get(spec.getLayer());
	//			// Validate that species belong to the correct polygon
	//			if (!spec.getPolygonIdentifier().equals(polygon.getPolygonIdentifier())) {
	//				throw validationError(
	//						"Record in species file contains species for polygon %s when expecting one for %s.",
	//						layer.getPolygonIdentifier(), polygon.getPolygonIdentifier()
	//				);
	//			}
	//			if (Objects.isNull(layer)) {
	//				throw validationError(
	//						"Species entry references layer %s of polygon %s but it is not present.", layer,
	//						polygon.getPolygonIdentifier()
	//				);
	//			}
	//			layer.getSpecies().put(spec.getGenus(), spec);
	//		}
	//
	//		polygon.setLayers(layers);
	//
	//		return polygon;
	//	}
	//
	//	private Optional<Float> heightMinimum(Layer layer) {
	//		var minima = Utils.<Map<String, Float>>expectParsedControl(controlMap, VDYPForwardControlParser.MINIMA, Map.class);
	//		switch (layer) {
	//		case PRIMARY:
	//			return Optional.of(minima.get(VDYPForwardControlParser.MINIMUM_HEIGHT));
	//		case VETERAN:
	//			return Optional.of(minima.get(VDYPForwardControlParser.MINIMUM_VETERAN_HEIGHT));
	//		default:
	//			return Optional.empty();
	//		}
	//	}
	//
	//	// FIP_CHK
	//	void checkPolygon(FipPolygon polygon) throws ProcessingException {
	//
	//		// Fortran did debug logging when a polygon is found to be invalid. Attaching
	//		// messages to exceptions fills that need.
	//
	//		// TODO finding all the things that are wrong rather than failing on just the
	//		// first would be a good idea.
	//
	//		if (!polygon.getLayers().containsKey(Layer.PRIMARY)) {
	//			throw validationError(
	//					"Polygon %s has no %s layer, or that layer has non-positive height or crown closure.",
	//					polygon.getPolygonIdentifier(), Layer.PRIMARY
	//			);
	//		}
	//
	//		var primaryLayer = (FipLayerPrimary) polygon.getLayers().get(Layer.PRIMARY);
	//
	//		// FIXME VDYP7 actually tests if total age - YTBH is less than 0.5 but gives an
	//		// error that total age is "less than" YTBH. Replicating that for now but
	//		// consider changing it.
	//
	//		if (primaryLayer.getAgeTotal() - primaryLayer.getYearsToBreastHeight() < 0.5f) {
	//			throw validationError(
	//					"Polygon %s has %s layer where total age is less than YTBH.", polygon.getPolygonIdentifier(),
	//					Layer.PRIMARY
	//			);
	//		}
	//
	//		// TODO This is the only validation step done to non-primary layers, VDYP7 had a
	//		// less well defined idea of a layer being present or not and so it may have
	//		// skipped validating other layers rather than validating them conditionally on
	//		// being present. Consider extending validation of other properties to other
	//		// layers.
	//
	//		for (FipLayer layer : polygon.getLayers().values()) {
	//			var height = layer.getHeight();
	//
	//			throwIfPresent(
	//					heightMinimum(layer.getLayer()).filter(minimum -> height < minimum).map(
	//							minimum -> validationError(
	//									"Polygon %s has %s layer where height %.1f is less than minimum %.1f.",
	//									polygon.getPolygonIdentifier(), layer.getLayer(), layer.getHeight(), minimum
	//							)
	//					)
	//			);
	//		}
	//
	//		if (polygon.getModeFip().map(x -> x == FipMode.FIPYOUNG).orElse(false)) {
	//			throw validationError(
	//					"Polygon %s is using unsupported mode %s.", polygon.getPolygonIdentifier(), FipMode.FIPYOUNG
	//			);
	//		}
	//
	//		if (primaryLayer.getYearsToBreastHeight() < 0.5) {
	//			throw validationError(
	//					"Polygon %s has %s layer where years to breast height %.1f is less than minimum %.1f years.",
	//					polygon.getPolygonIdentifier(), Layer.PRIMARY, primaryLayer.getYearsToBreastHeight(), 0.5f
	//			);
	//		}
	//
	//		if (primaryLayer.getSiteIndex() < 0.5) {
	//			throw validationError(
	//					"Polygon %s has %s layer where site index %.1f is less than minimum %.1f years.",
	//					polygon.getPolygonIdentifier(), Layer.PRIMARY, primaryLayer.getSiteIndex(), 0.5f
	//			);
	//		}
	//
	//		for (FipLayer layer : polygon.getLayers().values()) {
	//			var percentTotal = layer.getSpecies().values().stream()//
	//					.map(FipSpecies::getPercentGenus)//
	//					.reduce(0.0f, (x, y) -> x + y);
	//			if (Math.abs(percentTotal - 100f) > 0.01f) {
	//				throw validationError(
	//						"Polygon %s has %s layer where species entries have a percentage total that does not sum to 100%%.",
	//						polygon.getPolygonIdentifier(), Layer.PRIMARY
	//				);
	//			}
	//			// VDYP7 performs this step which should be negligible but might have a small
	//			// impact due to the 0.01 percent variation and floating point errors.
	//			if (layer.getLayer() == Layer.PRIMARY) {
	//				layer.getSpecies().values()
	//						.forEach(species -> species.setFractionGenus(species.getPercentGenus() / percentTotal));
	//			}
	//		}
	//
	//	}
	//
	//	// EMP040
	//	float estimatePrimaryBaseArea(
	//			FipLayer fipLayer, BecDefinition bec, float yieldFactor, float breastHeightAge, float baseAreaOverstory,
	//			float crownClosure
	//	) throws LowValueException {
	//		boolean lowCrownClosure = fipLayer.getCrownClosure() < LOW_CROWN_CLOSURE;
	//		crownClosure = lowCrownClosure ? LOW_CROWN_CLOSURE : crownClosure;
	//
	//		var coeMap = Utils.<MatrixMap2<String, String, Coefficients>>expectParsedControl(
	//				controlMap, CoefficientParser.BA_CONTROL_KEY, MatrixMap2.class
	//		);
	//		var modMap = Utils.<MatrixMap2<String, Region, Float>>expectParsedControl(
	//				controlMap, ModifierParser.CONTROL_KEY_MOD200_BA, MatrixMap2.class
	//		);
	//		var upperBoundMap = Utils.<MatrixMap3<Region, String, Integer, Float>>expectParsedControl(
	//				controlMap, UpperCoefficientParser.CONTROL_KEY, MatrixMap3.class
	//		);
	//
	//		var leadGenus = leadGenus(fipLayer);
	//
	//		var decayBecAlias = bec.getDecayBec().getAlias();
	//		Coefficients coe = weightedCoefficientSum(
	//				List.of(0, 1, 2, 3, 4, 5), 9, 0, fipLayer.getSpecies().values(), FipSpecies::getFractionGenus,
	//				s -> coeMap.get(decayBecAlias, s.getGenus())
	//		);
	//
	//		var ageToUse = clamp(breastHeightAge, 5f, 350f);
	//		var trAge = log(ageToUse);
	//
	//		var a00 = exp(coe.getCoe(0)) * (1f + coe.getCoe(1) * trAge);
	//
	//		var ap = exp(coe.getCoe(3)) + exp(coe.getCoe(4)) * trAge;
	//
	//		var baseArea = 0f;
	//
	//		float height = fipLayer.getHeight();
	//		if (height > coe.getCoe(2) - 3f) {
	//			var fHeight = height <= coe.getCoe(2) + 3f ? //
	//					pow(height - (coe.getCoe(2) - 3), 2) / 12f //
	//					: height - coe.getCoe(2);
	//
	//			baseArea = a00 * pow(crownClosure / 100, coe.getCoe(7) + coe.getCoe(8) * log(height)) * pow(fHeight, ap)
	//					* exp(coe.getCoe(5) * height + coe.getCoe(6) * baseAreaOverstory);
	//
	//			baseArea *= modMap.get(leadGenus.getGenus(), bec.getRegion());
	//
	//			// TODO
	//			var NDEBUG_1 = 0;
	//			if (NDEBUG_1 <= 0) {
	//				// See ISPSJF128
	//				var upperBound = upperBoundMap.get(bec.getRegion(), leadGenus.getGenus(), UpperCoefficientParser.BA);
	//				baseArea = min(baseArea, upperBound);
	//			}
	//
	//			if (lowCrownClosure) {
	//				baseArea *= fipLayer.getCrownClosure() / LOW_CROWN_CLOSURE;
	//			}
	//
	//		}
	//
	//		baseArea *= yieldFactor;
	//
	//		// This is to prevent underflow errors in later calculations
	//		if (baseArea <= 0.05f) {
	//			throw new LowValueException("Estimated base area", baseArea, 0.05f);
	//		}
	//		return baseArea;
	//	}
	//
	//	// EMP040
	//	float estimatePrimaryBaseArea(
	//			FipLayer fipLayer, BecDefinition bec, float yieldFactor, float breastHeightAge, float baseAreaOverstory
	//	) throws LowValueException {
	//		return estimatePrimaryBaseArea(
	//				fipLayer, bec, yieldFactor, breastHeightAge, baseAreaOverstory, fipLayer.getCrownClosure()
	//		);
	//	}
	//
	//	/**
	//	 * Create a coefficients object where its values are either a weighted sum of
	//	 * those for each of the given entities, or the value from one arbitrarily chose
	//	 * entity.
	//	 *
	//	 * @param <T>             The type of entity
	//	 * @param weighted        the indicies of the coefficients that should be
	//	 *                        weighted sums, those that are not included are assumed
	//	 *                        to be constant across all entities and one is choses
	//	 *                        arbitrarily.
	//	 * @param size            Size of the resulting coefficients object
	//	 * @param indexFrom       index from of the resulting coefficients object
	//	 * @param entities        the entities to do weighted sums over
	//	 * @param weight          the weight for a given entity
	//	 * @param getCoefficients the coefficients for a given entity
	//	 */
	//	<T> Coefficients weightedCoefficientSum(
	//			Collection<Integer> weighted, int size, int indexFrom, Collection<T> entities, ToDoubleFunction<T> weight,
	//			Function<T, Coefficients> getCoefficients
	//	) {
	//		Coefficients coe = Coefficients.empty(size, indexFrom);
	//
	//		// Do the summation in double precision
	//		var coeWorking = new double[size];
	//		Arrays.fill(coeWorking, 0.0);
	//
	//		for (var entity : entities) {
	//			var entityCoe = getCoefficients.apply(entity);
	//			double fraction = weight.applyAsDouble(entity);
	//			for (int i : weighted) {
	//				coeWorking[i - indexFrom] += (entityCoe.getCoe(i)) * fraction;
	//			}
	//		}
	//		// Reduce back to float once done
	//		for (int i : weighted) {
	//			coe.setCoe(i, (float) coeWorking[i - indexFrom]);
	//		}
	//
	//		// Pick one entity to fill in the fixed coefficients
	//		// Choice is arbitrary, they should all be the same
	//		var anyCoe = getCoefficients.apply(entities.iterator().next());
	//
	//		for (int i = indexFrom; i < size + indexFrom; i++) {
	//			if (weighted.contains(i))
	//				continue;
	//			coe.setCoe(i, anyCoe.getCoe(i));
	//		}
	//		return coe;
	//	}
	//
	//	FipSpecies leadGenus(FipLayer fipLayer) {
	//		return fipLayer.getSpecies().values().stream()
	//				.sorted(Utils.compareUsing(FipSpecies::getFractionGenus).reversed()).findFirst().orElseThrow();
	//	}
	//
	//	// EMP098
	//	float estimateVeteranBaseArea(float height, float crownClosure, String genus, Region region) {
	//		@SuppressWarnings("unchecked")
	//		var coefficients = ((MatrixMap2<String, Region, Coefficients>) controlMap.get(VeteranBQParser.CONTROL_KEY))
	//				.getM(genus, region);
	//
	//		// mismatched index is copied from VDYP7
	//		float a0 = coefficients.getCoe(1);
	//		float a1 = coefficients.getCoe(2);
	//		float a2 = coefficients.getCoe(3);
	//
	//		float baseArea = a0 * pow(max(height - a1, 0.0f), a2);
	//
	//		baseArea *= crownClosure / 4.0f;
	//
	//		baseArea = max(baseArea, 0.01f);
	//
	//		return baseArea;
	//	}
	//
	//	// EMP041
	//	float estimatePrimaryQuadMeanDiameter(
	//			FipLayerPrimary fipLayer, BecDefinition bec, float breastHeightAge, float baseAreaOverstory
	//	) {
	//		var coeMap = Utils.<MatrixMap2<String, String, Coefficients>>expectParsedControl(
	//				controlMap, CoefficientParser.DQ_CONTROL_KEY, MatrixMap2.class
	//		);
	//		var modMap = Utils.<MatrixMap2<String, Region, Float>>expectParsedControl(
	//				controlMap, ModifierParser.CONTROL_KEY_MOD200_DQ, MatrixMap2.class
	//		);
	//		var upperBoundMap = Utils.<MatrixMap3<Region, String, Integer, Float>>expectParsedControl(
	//				controlMap, UpperCoefficientParser.CONTROL_KEY, MatrixMap3.class
	//		);
	//
	//		var leadGenus = leadGenus(fipLayer);
	//
	//		var decayBecAlias = bec.getDecayBec().getAlias();
	//		Coefficients coe = weightedCoefficientSum(
	//				List.of(0, 1, 2, 3, 4), 9, 0, fipLayer.getSpecies().values(), FipSpecies::getFractionGenus,
	//				s -> coeMap.get(decayBecAlias, s.getGenus())
	//		);
	//
	//		var trAge = log(clamp(breastHeightAge, 5f, 350f));
	//		var height = fipLayer.getHeight();
	//
	//		if (height <= coe.getCoe(5)) {
	//			return 7.6f;
	//		}
	//
	//		var c0 = coe.get(0);
	//		var c1 = exp(coe.getCoe(1)) + exp(coe.getCoe(2)) * trAge;
	//		var c2 = exp(coe.getCoe(3)) + exp(coe.getCoe(4)) * trAge;
	//
	//
	//		var quadMeanDiameter = c0 + pow(c1 * pow(height - coe.getCoe(5), c2), 2)
	//				* exp(coe.getCoe(7) * baseAreaOverstory) * (1f - coe.getCoe(6) * fipLayer.getCrownClosure() / 100f);
	//
	//		quadMeanDiameter *= modMap.get(leadGenus.getGenus(), bec.getRegion());
	//
	//		quadMeanDiameter = max(quadMeanDiameter, 7.6f);
	//
	//		// TODO
	//		var NDEBUG_2 = 0;
	//		if (NDEBUG_2 <= 0) {
	//			// See ISPSJF129
	//			var upperBound = upperBoundMap.get(bec.getRegion(), leadGenus.getGenus(), UpperCoefficientParser.DQ);
	//			quadMeanDiameter = min(quadMeanDiameter, upperBound);
	//		}
	//
	//		return quadMeanDiameter;
	//	}
	//
	//	// FT_BD
	//	static float treesPerHectare(float baseArea, float quadraticMeanDiameter) {
	//		if (baseArea != 0) {
	//			return baseArea / PI_40K / (quadraticMeanDiameter * quadraticMeanDiameter);
	//		}
	//		return 0f;
	//	}
	//
	//	// FD_BT
	//	static float quadMeanDiameter(float baseArea, float treesPerHectare) {
	//		if (baseArea > 1e6f || treesPerHectare > 1e6f || Float.isNaN(baseArea) || Float.isNaN(treesPerHectare)) {
	//			return 0f;
	//		} else if (baseArea > 0f && treesPerHectare > 0f) {
	//			return sqrt(baseArea / treesPerHectare / PI_40K);
	//		}
	//		return 0f;
	//
	//	}
	//
	//	private static <E extends Throwable> void throwIfPresent(Optional<E> opt) throws E {
	//		if (opt.isPresent()) {
	//			throw opt.get();
	//		}
	//	}
	//
	//	private static StandProcessingException validationError(String template, Object... values) {
	//		return new StandProcessingException(String.format(template, values));
	//	}
	//
	//	/**
	//	 * estimate mean volume per tree For a species, for trees with dbh >= 7.5 CM
	//	 * Using eqn in jf117.doc
	//	 *
	//	 * @param volumeGroup
	//	 * @param loreyHeight
	//	 * @param quadMeanDiameter
	//	 * @return
	//	 */
	//	public float estimateMeanVolume(int volumeGroup, float loreyHeight, float quadMeanDiameter) {
	//		var coeMap = Utils.<Map<Integer, Coefficients>>expectParsedControl(
	//				controlMap, TotalStandWholeStemParser.CONTROL_KEY, Map.class
	//		);
	//
	//		var coe = coeMap.get(volumeGroup);
	//
	//		if (coe == null) {
	//			throw new IllegalArgumentException("Coefficients not found for volume group " + volumeGroup);
	//		}
	//
	//		float lvMean = //
	//				coe.getCoe(0) + //
	//						coe.getCoe(1) * log(quadMeanDiameter) + //
	//						coe.getCoe(2) * log(loreyHeight) + //
	//						coe.getCoe(3) * quadMeanDiameter + //
	//						coe.getCoe(4) / quadMeanDiameter + //
	//						coe.getCoe(5) * loreyHeight + //
	//						coe.getCoe(6) * quadMeanDiameter * quadMeanDiameter + //
	//						coe.getCoe(7) * quadMeanDiameter * loreyHeight + //
	//						coe.getCoe(8) * loreyHeight / quadMeanDiameter;
	//
	//		return exp(lvMean);
	//	}
	//
	//	double[] rootFinderFunction(double[] point, VdypLayer layer, double[] diameterBase) {
	//
	//		var percentL1 = new double[point.length];
	//		double percentSum = 0;
	//		if (point.length > 1) {
	//			for (int i = 0; i < point.length - 1; i++) {
	//				percentL1[i] = point[i];
	//				percentSum += point[i];
	//			}
	//		}
	//		percentL1[point.length - 1] = 100d - percentSum;
	//
	//		double volumeSum = 0d;
	//		double treesPerHectareSum = 0d;
	//
	//		final var layerBa = layer.getBaseAreaByUtilization().getCoe(UTIL_ALL);
	//
	//		// Iterate over the fixed order list with an index
	//		{
	//			var it = layer.getSpecies().entrySet().iterator();
	//			for (int j = 0; it.hasNext(); j++) {
	//				var spec = it.next().getValue();
	//
	//				// These side effects are evil but that's how VDYP7 works.
	//
	//				final float quadMeanDiameter = (float) (7.5
	//						+ (diameterBase[j] - 7.5) * FastMath.exp(point[point.length - 1] / 20d));
	//				spec.getQuadraticMeanDiameterByUtilization().setCoe(UTIL_ALL, quadMeanDiameter);
	//
	//				final float baseArea = (float) (layerBa * percentL1[j] / 100d);
	//				spec.getBaseAreaByUtilization().setCoe(UTIL_ALL, baseArea);
	//
	//				final float tph = VDYPForwardStart.treesPerHectare(baseArea, quadMeanDiameter);
	//				spec.getTreesPerHectareByUtilization().setCoe(UTIL_ALL, tph);
	//				treesPerHectareSum += tph;
	//
	//				final float loreyHeight = spec.getLoreyHeightByUtilization().getCoe(UTIL_ALL);
	//
	//				final float meanVolume = estimateMeanVolume(spec.getVolumeGroup(), loreyHeight, quadMeanDiameter);
	//				final float wholeStemVolume = tph * meanVolume;
	//
	//				spec.getWholeStemVolumeByUtilization().setCoe(UTIL_ALL, wholeStemVolume);
	//				volumeSum += wholeStemVolume;
	//			}
	//		}
	//
	//		double dqFinal = VDYPForwardStart
	//				.quadMeanDiameter(layer.getBaseAreaByUtilization().getCoe(UTIL_ALL), (float) treesPerHectareSum);
	//
	//		var y = new double[point.length];
	//
	//		if (layer.getSpecies().size() > 1) {
	//			var it = layer.getSpecies().values().iterator();
	//			for (int i = 0; it.hasNext(); i++) {
	//				var spec = it.next();
	//
	//				y[i] = 100d * spec.getWholeStemVolumeByUtilization().getCoe(UTIL_ALL) / volumeSum;
	//			}
	//		}
	//		y[y.length - 1] = dqFinal;
	//		return y;
	//	}
	//
	//	// YSMALL
	//	// TODO move to shared location as it's used elsewhere and implement
	//	// compatibility variables
	//	/**
	//	 * Estimate small components for primary layer
	//	 */
	//	public void estimateSmallComponents(FipPolygon fPoly, VdypLayer layer) {
	//		float loreyHeightSum = 0f;
	//		float baseAreaSum = 0f;
	//		float treesPerHectareSum = 0f;
	//		float volumeSum = 0f;
	//
	//		Region region = BecDefinitionParser.getBecs(controlMap).get(fPoly.getBiogeoclimaticZone()).get().getRegion();
	//
	//		for (VdypSpecies spec : layer.getSpecies().values()) {
	//			float loreyHeightSpec = spec.getLoreyHeightByUtilization().getCoe(UTIL_ALL); // HLsp
	//			float baseAreaSpec = spec.getBaseAreaByUtilization().getCoe(UTIL_ALL); // BAsp
	//			float quadMeanDiameterSpec = spec.getBaseAreaByUtilization().getCoe(UTIL_ALL); // DQsp
	//
	//			// EMP080
	//			float smallComponentProbability = smallComponentProbability(layer, spec, region); // PROBsp
	//
	//			// this WHOLE operation on Actual BA's, not 100% occupancy.
	//			float fractionAvailable = fPoly.getPercentAvailable().map(p -> p / 100f).orElse(1f);
	//			baseAreaSpec *= fractionAvailable;
	//			// EMP081
	//			float conditionalExpectedBaseArea = conditionalExpectedBaseArea(spec, baseAreaSpec, region); // BACONDsp
	//			conditionalExpectedBaseArea /= fractionAvailable;
	//
	//			float baseAreaSpecSmall = smallComponentProbability * conditionalExpectedBaseArea;
	//
	//			// EMP082
	//			float quadMeanDiameterSpecSmall = smallComponentQuadMeanDiameter(spec); // DQSMsp
	//
	//			// EMP085
	//			float loreyHeightSpecSmall = smallComponentLoreyHeight(spec, quadMeanDiameterSpecSmall); // HLSMsp
	//
	//			// EMP086
	//			float meanVolumeSmall = meanVolumeSmall(spec, quadMeanDiameterSpecSmall, loreyHeightSpecSmall); // VMEANSMs
	//
	//			// TODO Apply Compatibility Variables, not needed for FIPSTART
	//
	//			spec.getLoreyHeightByUtilization().setCoe(UTIL_SMALL, loreyHeightSpecSmall);
	//			float treesPerHectareSpecSmall = treesPerHectare(baseAreaSpecSmall, quadMeanDiameterSpecSmall); // TPHSMsp
	//			spec.getBaseAreaByUtilization().setCoe(UTIL_SMALL, baseAreaSpecSmall);
	//			spec.getTreesPerHectareByUtilization().setCoe(UTIL_SMALL, treesPerHectareSpecSmall);
	//			spec.getQuadraticMeanDiameterByUtilization().setCoe(UTIL_SMALL, quadMeanDiameterSpecSmall);
	//			float wholeStemVolumeSpecSmall = treesPerHectareSpecSmall * meanVolumeSmall; // VOLWS(I,-1)
	//			spec.getWholeStemVolumeByUtilization().setCoe(UTIL_SMALL, wholeStemVolumeSpecSmall);
	//
	//			loreyHeightSum += baseAreaSpecSmall * loreyHeightSpecSmall;
	//			baseAreaSum += baseAreaSpecSmall;
	//			treesPerHectareSum += treesPerHectareSpecSmall;
	//			volumeSum += wholeStemVolumeSpecSmall;
	//		}
	//
	//		if (baseAreaSum > 0f) {
	//			layer.getLoreyHeightByUtilization().setCoe(UTIL_SMALL, loreyHeightSum / baseAreaSum);
	//		} else {
	//			layer.getLoreyHeightByUtilization().setCoe(UTIL_SMALL, 0f);
	//		}
	//		layer.getBaseAreaByUtilization().setCoe(UTIL_SMALL, baseAreaSum);
	//		layer.getTreesPerHectareByUtilization().setCoe(UTIL_SMALL, treesPerHectareSum);
	//		layer.getQuadraticMeanDiameterByUtilization()
	//				.setCoe(UTIL_SMALL, quadMeanDiameter(baseAreaSum, treesPerHectareSum));
	//		layer.getWholeStemVolumeByUtilization().setCoe(UTIL_SMALL, volumeSum);
	//	}
	//
	//	// EMP086
	//	private float meanVolumeSmall(VdypSpecies spec, float quadMeanDiameterSpecSmall, float loreyHeightSpecSmall) {
	//		Coefficients coe = getCoeForSpec(spec, SmallComponentWSVolumeParser.CONTROL_KEY);
	//
	//		// EQN 1 in IPSJF119.doc
	//
	//		float a0 = coe.getCoe(1);
	//		float a1 = coe.getCoe(2);
	//		float a2 = coe.getCoe(3);
	//		float a3 = coe.getCoe(4);
	//
	//		return exp(
	//				a0 + a1 * log(quadMeanDiameterSpecSmall) + a2 * log(loreyHeightSpecSmall)
	//						+ a3 * quadMeanDiameterSpecSmall
	//		);
	//	}
	//
	//	// EMP085
	//	private float smallComponentLoreyHeight(VdypSpecies spec, float quadMeanDiameterSpecSmall) {
	//		Coefficients coe = getCoeForSpec(spec, SmallComponentHLParser.CONTROL_KEY);
	//
	//		// EQN 1 in IPSJF119.doc
	//
	//		float a0 = coe.getCoe(1);
	//		float a1 = coe.getCoe(2);
	//
	//		return 1.3f + (spec.getLoreyHeightByUtilization().getCoe(VDYPForwardStart.UTIL_ALL) - 1.3f) * exp(
	//				a0 * (pow(quadMeanDiameterSpecSmall, a1)
	//						- pow(spec.getQuadraticMeanDiameterByUtilization().getCoe(UTIL_ALL), a1))
	//		);
	//	}
	//
	//	// EMP082
	//	private float smallComponentQuadMeanDiameter(VdypSpecies spec) {
	//		Coefficients coe = getCoeForSpec(spec, SmallComponentDQParser.CONTROL_KEY);
	//
	//		// EQN 5 in IPSJF118.doc
	//
	//		float a0 = coe.getCoe(1);
	//		float a1 = coe.getCoe(2);
	//
	//		float logit = //
	//				a0 + a1 * spec.getLoreyHeightByUtilization().getCoe(VDYPForwardStart.UTIL_ALL);
	//
	//		return 4.0f + 3.5f * exp(logit) / (1.0f + exp(logit));
	//	}
	//
	//	// EMP081
	//	private float conditionalExpectedBaseArea(VdypSpecies spec, float baseAreaSpec, Region region) {
	//		Coefficients coe = getCoeForSpec(spec, SmallComponentBaseAreaParser.CONTROL_KEY);
	//
	//		// EQN 3 in IPSJF118.doc
	//
	//		float a0 = coe.getCoe(1);
	//		float a1 = coe.getCoe(2);
	//		float a2 = coe.getCoe(3);
	//		float a3 = coe.getCoe(4);
	//
	//		float coast = region == Region.COASTAL ? 1.0f : 0.0f;
	//
	//		// FIXME due to a bug in VDYP7 it always treats this as interior. Replicating
	//		// that for now.
	//		coast = 0f;
	//
	//		float arg = //
	//				(a0 + //
	//						a1 * coast + //
	//						a2 * spec.getBaseAreaByUtilization().getCoe(VDYPForwardStart.UTIL_ALL)//
	//				) * exp(a3 * spec.getLoreyHeightByUtilization().getCoe(VDYPForwardStart.UTIL_ALL));
	//		arg = max(arg, 0f);
	//
	//		return arg;
	//	}
	//
	//	// EMP080
	//	private float smallComponentProbability(VdypLayer layer, VdypSpecies spec, Region region) {
	//		Coefficients coe = getCoeForSpec(spec, SmallComponentProbabilityParser.CONTROL_KEY);
	//
	//		// EQN 1 in IPSJF118.doc
	//
	//		float a0 = coe.getCoe(1);
	//		float a1 = coe.getCoe(2);
	//		float a2 = coe.getCoe(3);
	//		float a3 = coe.getCoe(4);
	//
	//		float coast = region == Region.COASTAL ? 1.0f : 0.0f;
	//
	//		float logit = //
	//				a0 + //
	//						a1 * coast + //
	//						a2 * layer.getBreastHeightAge() + //
	//						a3 * spec.getLoreyHeightByUtilization().getCoe(VDYPForwardStart.UTIL_ALL);
	//
	//		return exp(logit) / (1.0f + exp(logit));
	//	}
	//
	//	Coefficients getCoeForSpec(VdypSpecies spec, String controlKey) {
	//		var coeMap = Utils.<Map<String, Coefficients>>expectParsedControl(controlMap, controlKey, Map.class);
	//		return coeMap.get(spec.getGenus());
	//	}
	//
	//	/**
	//	 * Estimate the Jacobian Matrix of a function using forward difference
	//	 *
	//	 * @param x
	//	 * @param func
	//	 * @return
	//	 */
	//	double[][] estimateJacobian(double[] x, MultivariateVectorFunction func) {
	//		return estimateJacobian(x, func.value(x), func);
	//	}
	//
	//	/**
	//	 * Estimate the Jacobian Matrix of a function using forward difference
	//	 *
	//	 * @param x
	//	 * @param y
	//	 * @param func
	//	 * @return
	//	 */
	//	double[][] estimateJacobian(double[] x, double[] y, MultivariateVectorFunction func) {
	//		// TODO
	//		final double machineEpsilon = 2.22e-16;
	//		final double functionEpsilon = 1.19e-07;
	//
	//		double epsilon = FastMath.sqrt(FastMath.max(functionEpsilon, machineEpsilon));
	//
	//		double[] x2 = Arrays.copyOf(x, x.length);
	//
	//		double[][] result = new double[x.length][x.length];
	//
	//		for (int j = 0; j < x.length; j++) {
	//			double temp = x[j];
	//			double h = epsilon * FastMath.abs(temp);
	//			if (h == 0) {
	//				h = epsilon;
	//			}
	//			x2[j] = temp + h;
	//			double[] y2 = func.value(x2);
	//			x2[j] = temp;
	//			for (int i = 0; i < x.length; i++) {
	//				result[i][j] = (y2[i] - y[i]) / h;
	//			}
	//		}
	//		return result;
	//	}
	//
	//	RealMatrix identityMatrix(int n) {
	//		var diag = new double[n];
	//		Arrays.fill(diag, n);
	//		return new DiagonalMatrix(diag);
	//
	//	}
	//
	//	RealVector findRoot(double[] diameterBase, double[] goal, double[] x, VdypLayer layer, double tolerance) {
	//		MultivariateVectorFunction func = point -> rootFinderFunction(point, layer, diameterBase);
	//
	//		MultivariateMatrixFunction jacFunc = point -> estimateJacobian(point, func);
	//
	//		LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
	//
	//		optimizer.withCostRelativeTolerance(tolerance); // Not sure if this is the right tolerance
	//
	//		LeastSquaresProblem leastSquaresProblem = LeastSquaresFactory.create(
	//				func, //
	//				jacFunc, //
	//				goal, //
	//				x, //
	//				identityMatrix(x.length), //
	//				null, //
	//				200, //
	//				1000 //
	//		);
	//
	//		var result = optimizer.optimize(leastSquaresProblem);
	//
	//		return result.getPoint();
	//	}
	//
	//	/**
	//	 * Iterates over all but the last entry, passing them to the first consumer then
	//	 * passes the last entry to the second consumer
	//	 */
	//	<T> void eachButLast(Collection<T> items, Consumer<T> body, Consumer<T> lastBody) {
	//		var it = items.iterator();
	//		while (it.hasNext()) {
	//			var value = it.next();
	//			if (it.hasNext()) {
	//				body.accept(value);
	//			} else {
	//				lastBody.accept(value);
	//			}
	//		}
	//	}

	// @formatter:off

}
