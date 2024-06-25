package ca.bc.gov.nrs.vdyp.vri;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ca.bc.gov.nrs.vdyp.application.ProcessingException;
import ca.bc.gov.nrs.vdyp.application.RuntimeProcessingException;
import ca.bc.gov.nrs.vdyp.application.RuntimeStandProcessingException;
import ca.bc.gov.nrs.vdyp.application.StandProcessingException;
import ca.bc.gov.nrs.vdyp.application.VdypApplicationIdentifier;
import ca.bc.gov.nrs.vdyp.application.VdypStartApplication;
import ca.bc.gov.nrs.vdyp.common.ControlKey;
import ca.bc.gov.nrs.vdyp.common.Utils;
import ca.bc.gov.nrs.vdyp.common.ValueOrMarker;
import ca.bc.gov.nrs.vdyp.common_calculators.BaseAreaTreeDensityDiameter;
import ca.bc.gov.nrs.vdyp.common_calculators.SiteIndex2Height;
import ca.bc.gov.nrs.vdyp.common_calculators.custom_exceptions.CommonCalculatorException;
import ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexAgeType;
import ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexEquation;
import ca.bc.gov.nrs.vdyp.io.parse.common.ResourceParseException;
import ca.bc.gov.nrs.vdyp.io.parse.control.BaseControlParser;
import ca.bc.gov.nrs.vdyp.io.parse.streaming.StreamingParser;
import ca.bc.gov.nrs.vdyp.math.FloatMath;
import ca.bc.gov.nrs.vdyp.model.PolygonMode;
import ca.bc.gov.nrs.vdyp.model.Region;
import ca.bc.gov.nrs.vdyp.model.BaseVdypSite;
import ca.bc.gov.nrs.vdyp.model.BaseVdypSpecies;
import ca.bc.gov.nrs.vdyp.model.BaseVdypSpecies.Builder;
import ca.bc.gov.nrs.vdyp.model.BecDefinition;
import ca.bc.gov.nrs.vdyp.model.BecLookup;
import ca.bc.gov.nrs.vdyp.model.Coefficients;
import ca.bc.gov.nrs.vdyp.model.InputLayer;
import ca.bc.gov.nrs.vdyp.model.LayerType;
import ca.bc.gov.nrs.vdyp.model.MatrixMap2;
import ca.bc.gov.nrs.vdyp.model.ModelClassBuilder;
import ca.bc.gov.nrs.vdyp.model.PolygonIdentifier;
import ca.bc.gov.nrs.vdyp.model.VdypLayer;
import ca.bc.gov.nrs.vdyp.model.VdypPolygon;
import ca.bc.gov.nrs.vdyp.vri.model.VriLayer;
import ca.bc.gov.nrs.vdyp.vri.model.VriPolygon;
import ca.bc.gov.nrs.vdyp.vri.model.VriSite;
import ca.bc.gov.nrs.vdyp.vri.model.VriSpecies;

public class VriStart extends VdypStartApplication<VriPolygon, VriLayer, VriSpecies, VriSite> implements Closeable {

	private static final String SPECIAL_PROCESSING_LOG_TEMPLATE = "Doing special processing for mode {}";
	static final float FRACTION_AVAILABLE_N = 0.85f; // PCTFLAND_N;

	static final Logger log = LoggerFactory.getLogger(VriStart.class);

	static final float EMPOC = 0.85f;

	public static void main(final String... args) throws IOException {

		try (var app = new VriStart();) {

			doMain(app, args);
		}
	}

	// VRI_SUB
	// TODO Fortran takes a vector of flags (FIPPASS) controlling which stages are
	// implemented. FIPSTART always uses the same vector so far now that's not
	// implemented.
	@Override
	public void process() throws ProcessingException {
		int polygonsRead = 0;
		int polygonsWritten = 0;
		try (
				var polyStream = this.<VriPolygon>getStreamingParser(ControlKey.VRI_INPUT_YIELD_POLY);
				var layerStream = this.<Map<LayerType, VriLayer.Builder>>getStreamingParser(
						ControlKey.VRI_INPUT_YIELD_LAYER
				);
				var speciesStream = this
						.<Collection<VriSpecies>>getStreamingParser(ControlKey.VRI_INPUT_YIELD_SPEC_DIST);
				var siteStream = this.<Collection<VriSite>>getStreamingParser(ControlKey.VRI_INPUT_YIELD_HEIGHT_AGE_SI);
		) {
			log.atDebug().setMessage("Start Stand processing").log();

			while (polyStream.hasNext()) {

				// FIP_GET
				log.atInfo().setMessage("Getting polygon {}").addArgument(polygonsRead + 1).log();
				var polygon = getPolygon(polyStream, layerStream, speciesStream, siteStream);
				try {

					var resultPoly = processPolygon(polygonsRead, polygon);
					if (resultPoly.isPresent()) {
						polygonsRead++;

						// Output
						getVriWriter().writePolygonWithSpeciesAndUtilization(resultPoly.get());

						polygonsWritten++;
					}

					log.atInfo().setMessage("Read {} polygons and wrote {}").addArgument(polygonsRead)
							.addArgument(polygonsWritten).log();

				} catch (StandProcessingException ex) {
					// TODO include some sort of hook for different forms of user output
					// TODO Implement single stand mode that propagates the exception

					log.atWarn().setMessage("Polygon {} bypassed").addArgument(polygon.getPolygonIdentifier())
							.setCause(ex).log();
				}

			}
		} catch (IOException | ResourceParseException ex) {
			throw new ProcessingException("Error while reading or writing data.", ex);
		}
	}

	VriPolygon getPolygon(
			StreamingParser<VriPolygon> polyStream, StreamingParser<Map<LayerType, VriLayer.Builder>> layerStream,
			StreamingParser<Collection<VriSpecies>> speciesStream, StreamingParser<Collection<VriSite>> siteStream
	) throws StandProcessingException, IOException, ResourceParseException {

		var becLookup = Utils.expectParsedControl(controlMap, ControlKey.BEC_DEF, BecLookup.class);

		log.trace("Getting polygon");
		var polygon = polyStream.next();

		BecDefinition bec = becLookup.get(polygon.getBiogeoclimaticZone())
				.orElseThrow(() -> new StandProcessingException("Unknown BEC " + polygon.getBiogeoclimaticZone()));

		log.trace("Getting species for polygon {}", polygon.getPolygonIdentifier());
		Collection<VriSpecies> species;
		try {
			species = speciesStream.next();
		} catch (NoSuchElementException ex) {
			throw validationError("Species file has fewer records than polygon file.", ex);
		}

		Map<LayerType, VriLayer.Builder> layersBuilders = layerStream.next();

		for (var spec : species) {
			var layerBuilder = layersBuilders.get(spec.getLayerType());
			// Validate that species belong to the correct polygon
			if (!spec.getPolygonIdentifier().equals(polygon.getPolygonIdentifier())) {
				throw validationError(
						"Record in species file contains species for polygon %s when expecting one for %s.",
						spec.getPolygonIdentifier(), polygon.getPolygonIdentifier()
				);
			}
			if (Objects.isNull(layerBuilder)) {
				throw validationError(
						"Species entry references layer %s of polygon %s but it is not present.", spec.getLayerType(),
						polygon.getPolygonIdentifier()
				);
			}
			layerBuilder.addSpecies(spec);
		}

		log.trace("Getting sites for polygon {}", polygon.getPolygonIdentifier());
		Collection<VriSite> sites;
		try {
			sites = siteStream.next();
		} catch (NoSuchElementException ex) {
			throw validationError("Sites file has fewer records than polygon file.", ex);
		}

		for (var site : sites) {
			var layerBuilder = layersBuilders.get(site.getLayerType());
			// Validate that species belong to the correct polygon
			if (!site.getPolygonIdentifier().equals(polygon.getPolygonIdentifier())) {
				throw validationError(
						"Record in site file contains site for polygon %s when expecting one for %s.",
						site.getPolygonIdentifier(), polygon.getPolygonIdentifier()
				);
			}
			if (Objects.isNull(layerBuilder)) {
				throw validationError(
						"Site entry references layer %s of polygon %s but it is not present.", site.getLayerType(),
						polygon.getPolygonIdentifier()
				);
			}
			layerBuilder.addSite(site);
		}

		Map<LayerType, VriLayer> layers = getLayersForPolygon(polygon, bec, layersBuilders);

		// Validate that layers belong to the correct polygon
		for (var layer : layers.values()) {
			if (!layer.getPolygonIdentifier().equals(polygon.getPolygonIdentifier())) {
				throw validationError(
						"Record in layer file contains layer for polygon %s when expecting one for %s.",
						layer.getPolygonIdentifier(), polygon.getPolygonIdentifier()
				);
			}
			layer.setSpecies(new HashMap<>());
		}

		polygon.setLayers(layers);

		return polygon;

	}

	private Map<LayerType, VriLayer>
			getLayersForPolygon(VriPolygon polygon, BecDefinition bec, Map<LayerType, VriLayer.Builder> layersBuilders)
					throws StandProcessingException {
		log.trace("Getting layers for polygon {}", polygon.getPolygonIdentifier());
		Map<LayerType, VriLayer> layers;
		try {

			// Do some additional processing then build the layers.
			layers = layersBuilders.values().stream().map(builder -> {

				var layerType = builder.getLayerType().get();

				builder.buildChildren(); // Make sure all children are built before getting them.
				var layerSpecies = builder.getSpecies();

				if (layerType == LayerType.PRIMARY) {
					builder.percentAvailable(polygon.getPercentAvailable().orElse(1f));
				}
				if (!layerSpecies.isEmpty()) {
					var primarySpecs = this.findPrimarySpecies(layerSpecies);
					int itg;
					try {
						itg = findItg(primarySpecs);

						builder.inventoryTypeGroup(itg);
					} catch (StandProcessingException ex) {
						throw new RuntimeStandProcessingException(ex);
					}
					builder.primaryGenus(primarySpecs.get(0).getGenus());

					if (layerType == LayerType.PRIMARY) {
						modifyPrimaryLayerBuild(bec, builder, primarySpecs, itg);
					}
				}
				if (layerType == LayerType.VETERAN) {
					modifyVeteranLayerBuild(layersBuilders, builder);
				}
				return builder;
			}).map(VriLayer.Builder::build).collect(Collectors.toUnmodifiableMap(VriLayer::getLayerType, x -> x));

		} catch (NoSuchElementException ex) {
			throw validationError("Layers file has fewer records than polygon file.", ex);
		} catch (RuntimeStandProcessingException ex) {
			throw ex.getCause();
		}
		return layers;
	}

	private void modifyVeteranLayerBuild(
			Map<LayerType, VriLayer.Builder> layersBuilders, ca.bc.gov.nrs.vdyp.vri.model.VriLayer.Builder builder
	) {
		if (builder.getBaseArea().map(x -> x <= 0f).orElse(true)
				|| builder.getTreesPerHectare().map(x -> x <= 0f).orElse(true)) {
			// BA or TPH missing from Veteran layer.

			builder.treesPerHectare(0f);

			float crownClosure = builder.getCrownClosure().filter(x -> x > 0f).orElseThrow(
					() -> new RuntimeStandProcessingException(
							validationError(
									"Expected a positive crown closure for veteran layer but was %s",
									Utils.optNa(builder.getCrownClosure())
							)
					)
			);
			// If the primary layer base area is positive, multiply that by veteran crown
			// closure, otherwise just use half the veteran crown closure.
			builder.baseArea(
					layersBuilders.get(LayerType.PRIMARY).getBaseArea().filter(x -> x > 0f)
							.map(pba -> crownClosure / 100f * pba).orElse(crownClosure / 2f)
			);
		}
	}

	private void modifyPrimaryLayerBuild(
			BecDefinition bec, ca.bc.gov.nrs.vdyp.vri.model.VriLayer.Builder builder, List<VriSpecies> primarySpecs,
			int itg
	) {
		// This was being done in VRI_CHK but I moved it here to when the object is
		// being built instead.
		if (builder.getBaseArea()
				.flatMap(
						ba -> builder.getTreesPerHectare()
								.map(tph -> BaseAreaTreeDensityDiameter.quadMeanDiameter(ba, tph) < 7.5f)
				).orElse(false)) {
			builder.baseArea(Optional.empty());
			builder.treesPerHectare(Optional.empty());
		}

		if (primarySpecs.size() > 1) {
			builder.secondaryGenus(primarySpecs.get(1).getGenus());
		}

		builder.empiricalRelationshipParameterIndex(
				findEmpiricalRelationshipParameterIndex(primarySpecs.get(0).getGenus(), bec, itg)
		);
	}

	static final EnumSet<PolygonMode> ACCEPTABLE_MODES = EnumSet.of(PolygonMode.START, PolygonMode.YOUNG);

	Optional<VdypPolygon> processPolygon(int polygonsRead, VriPolygon polygon) throws ProcessingException {
		log.atInfo().setMessage("Read polygon {}, preparing to process").addArgument(polygon.getPolygonIdentifier())
				.log();

		var mode = polygon.getMode().orElse(PolygonMode.START);

		if (mode == PolygonMode.DONT_PROCESS) {
			log.atInfo().setMessage("Skipping polygon with mode {}").addArgument(mode).log();
			return Optional.empty();
		}

		log.atInfo().setMessage("Checking validity of polygon {}:{}").addArgument(polygonsRead)
				.addArgument(polygon.getPolygonIdentifier()).log();

		mode = checkPolygon(polygon);

		switch (mode) {
		case YOUNG:
			log.atTrace().setMessage(SPECIAL_PROCESSING_LOG_TEMPLATE).addArgument(mode).log();
			polygon = processYoung(polygon);
			break;
		case BATC:
			log.atTrace().setMessage(SPECIAL_PROCESSING_LOG_TEMPLATE).addArgument(mode).log();
			polygon = processBatc(polygon);
			break;
		case BATN:
			log.atTrace().setMessage(SPECIAL_PROCESSING_LOG_TEMPLATE).addArgument(mode).log();
			polygon = processBatn(polygon);
			break;
		default:
			log.atTrace().setMessage("No special processing for mode {}").addArgument(mode).log();
			break;
		}

		// TODO
		return Optional.empty();

	}

	// VRI_CHK
	PolygonMode checkPolygon(VriPolygon polygon) throws ProcessingException {

		BecDefinition bec = Utils.getBec(polygon.getBiogeoclimaticZone(), controlMap);

		var primaryLayer = requireLayer(polygon, LayerType.PRIMARY);

		// At this point the Fortran implementation nulled the BA and TPH of Primary
		// layers if the BA and TPH were present and resulted in a DQ <7.5
		// I did that in getPolygon instead of here.

		for (var layer : polygon.getLayers().values()) {
			checkLayer(polygon, layer);
		}

		PolygonMode mode = checkPolygonForMode(polygon, bec);

		Map<String, Float> minMap = Utils.expectParsedControl(controlMap, ControlKey.MINIMA, Map.class);

		float veteranMinHeight = minMap.get(VriControlParser.MINIMUM_VETERAN_HEIGHT);

		VriLayer veteranLayer = polygon.getLayers().get(LayerType.VETERAN);
		if (veteranLayer != null) {
			Optional<Float> veteranHeight = veteranLayer.getPrimarySite().flatMap(VriSite::getHeight);
			validateMinimum("Veteran layer primary species height", veteranHeight, veteranMinHeight, true);
		}

		return mode;
	}

	private void checkLayer(VriPolygon polygon, VriLayer layer) throws StandProcessingException {
		if (layer.getSpecies().isEmpty())
			return;
		if (layer.getLayerType() == LayerType.PRIMARY)
			this.getPercentTotal(layer); // Validate that percent total is close to 100%
		Optional<VriSite> primarySite = layer.getPrimaryGenus().flatMap(id -> Utils.optSafe(layer.getSites().get(id)));
		var ageTotal = primarySite.flatMap(VriSite::getAgeTotal);
		var treesPerHectare = layer.getTreesPerHectare();
		var height = primarySite.flatMap(VriSite::getHeight);
		if (polygon.getMode().map(x -> x == PolygonMode.YOUNG).orElse(false)
				&& layer.getLayerType() == LayerType.PRIMARY) {
			if (ageTotal.map(x -> x <= 0f).orElse(true) || treesPerHectare.map(x -> x <= 0f).orElse(true)) {
				throw validationError(
						"Age Total and Trees Per Hectare must be positive for a PRIMARY layer in mode YOUNG"
				);
			}
		} else {
			if (height.map(x -> x <= 0f).orElse(true)) {
				throw validationError(
						"Height must be positive for a VETERAN layer or a PRIMARY layer not in mode YOUNG"
				);
			}
		}
	}

	static final String SITE_INDEX_PROPERTY_NAME = "Site index";
	static final String AGE_TOTAL_PROPERTY_NAME = "Age total";
	static final String BREAST_HEIGHT_AGE_PROPERTY_NAME = "Breast height age";
	static final String YEARS_TO_BREAST_HEIGHT_PROPERTY_NAME = "Years to breast height";
	static final String HEIGHT_PROPERTY_NAME = "Height";
	static final String BASE_AREA_PROPERTY_NAME = "Base area";
	static final String TREES_PER_HECTARE_PROPERTY_NAME = "Trees per hectare";
	static final String CROWN_CLOSURE_PROPERTY_NAME = "Crown closure";

	protected PolygonMode checkPolygonForMode(VriPolygon polygon, BecDefinition bec) throws StandProcessingException {
		VriLayer primaryLayer = polygon.getLayers().get(LayerType.PRIMARY);
		Optional<VriSite> primarySite = primaryLayer.getPrimaryGenus()
				.flatMap(id -> Utils.optSafe(primaryLayer.getSites().get(id)));
		var ageTotal = primarySite.flatMap(VriSite::getAgeTotal);
		var height = primarySite.flatMap(VriSite::getHeight);
		var siteIndex = primarySite.flatMap(VriSite::getSiteIndex);
		var yearsToBreastHeight = primarySite.flatMap(VriSite::getYearsToBreastHeight);
		var baseArea = primaryLayer.getBaseArea();
		var treesPerHectare = primaryLayer.getTreesPerHectare();
		var crownClosure = primaryLayer.getCrownClosure();
		var percentForest = polygon.getPercentAvailable();

		try {
			PolygonMode mode = polygon.getMode().orElseGet(() -> {
				try {
					return findDefaultPolygonMode(
							ageTotal, yearsToBreastHeight, height, baseArea, treesPerHectare, percentForest,
							primaryLayer.getSpecies().values(), bec,
							primaryLayer.getEmpericalRelationshipParameterIndex()
					);
				} catch (StandProcessingException e) {
					throw new RuntimeStandProcessingException(e);
				}
			});
			polygon.setMode(Optional.of(mode));
			Optional<Float> primaryBreastHeightAge = Utils.mapBoth(
					primaryLayer.getPrimarySite().flatMap(VriSite::getAgeTotal),
					primaryLayer.getPrimarySite().flatMap(VriSite::getYearsToBreastHeight), (at, ytbh) -> at - ytbh
			);
			log.atDebug().setMessage("Polygon mode {} checks").addArgument(mode).log();
			switch (mode) {

			case START:
				validateMinimum(SITE_INDEX_PROPERTY_NAME, siteIndex, 0f, false);
				validateMinimum(AGE_TOTAL_PROPERTY_NAME, ageTotal, 0f, false);
				validateMinimum(BREAST_HEIGHT_AGE_PROPERTY_NAME, primaryBreastHeightAge, 0f, false);
				validateMinimum(HEIGHT_PROPERTY_NAME, height, 4.5f, false);
				validateMinimum(BASE_AREA_PROPERTY_NAME, baseArea, 0f, false);
				validateMinimum(TREES_PER_HECTARE_PROPERTY_NAME, treesPerHectare, 0f, false);
				break;

			case YOUNG:
				validateMinimum(SITE_INDEX_PROPERTY_NAME, siteIndex, 0f, false);
				validateMinimum(AGE_TOTAL_PROPERTY_NAME, ageTotal, 0f, false);
				validateMinimum(YEARS_TO_BREAST_HEIGHT_PROPERTY_NAME, yearsToBreastHeight, 0f, false);
				break;

			case BATN:
				validateMinimum(SITE_INDEX_PROPERTY_NAME, siteIndex, 0f, false);
				validateMinimum(AGE_TOTAL_PROPERTY_NAME, ageTotal, 0f, false);
				validateMinimum(BREAST_HEIGHT_AGE_PROPERTY_NAME, primaryBreastHeightAge, 0f, false);
				validateMinimum(HEIGHT_PROPERTY_NAME, height, 1.3f, false);
				break;

			case BATC:
				validateMinimum(SITE_INDEX_PROPERTY_NAME, siteIndex, 0f, false);
				validateMinimum(AGE_TOTAL_PROPERTY_NAME, ageTotal, 0f, false);
				validateMinimum(BREAST_HEIGHT_AGE_PROPERTY_NAME, primaryBreastHeightAge, 0f, false);
				validateMinimum(HEIGHT_PROPERTY_NAME, height, 1.3f, false);
				validateMinimum(CROWN_CLOSURE_PROPERTY_NAME, crownClosure, 0f, false);
				break;

			case DONT_PROCESS:
				log.atDebug().setMessage("Skipping validation for ignored polygon");
				// Do Nothing
				break;
			}
			return mode;
		} catch (RuntimeStandProcessingException e) {
			throw e.getCause();
		}
	}

	void validateMinimum(String fieldName, float value, float minimum, boolean inclusive)
			throws StandProcessingException {
		if (value < minimum || (value == minimum && !inclusive))
			throw validationError(
					"%s %s should be %s %s", fieldName, value, inclusive ? "greater than or equal to" : "greater than",
					minimum
			);
	}

	void validateMinimum(String fieldName, Optional<Float> value, float minimum, boolean inclusive)
			throws StandProcessingException {
		validateMinimum(
				fieldName, value.orElseThrow(() -> validationError("%s is not present", fieldName)), minimum, inclusive
		);
	}

	// UPPERGEN Method 1
	Coefficients upperBounds(int baseAreaGroup) {
		var upperBoundsMap = Utils
				.<Map<Integer, Coefficients>>expectParsedControl(controlMap, ControlKey.BA_DQ_UPPER_BOUNDS, Map.class);
		return Utils.<Coefficients>optSafe(upperBoundsMap.get(baseAreaGroup)).orElseThrow(
				() -> new IllegalStateException("Could not find limits for base area group " + baseAreaGroup)
		);
	}

	float upperBoundsBaseArea(int baseAreaGroup) {
		return upperBounds(baseAreaGroup).getCoe(1);
	}

	float upperBoundsQuadMeanDiameter(int baseAreaGroup) {
		return upperBounds(baseAreaGroup).getCoe(2);
	}

	// EMP106
	float estimateBaseAreaYield(
			float dominantHeight, float breastHeightAge, Optional<Float> baseAreaOverstory, boolean fullOccupancy,
			Collection<? extends BaseVdypSpecies> species, BecDefinition bec, int baseAreaGroup
	) throws StandProcessingException {
		var coe = estimateBaseAreaYieldCoefficients(species, bec);

		float upperBoundBaseArea = upperBoundsBaseArea(baseAreaGroup);

		/*
		 * The original Fortran had the following comment and a commented out modification to upperBoundsBaseArea
		 * (BATOP98). I have included them here.
		 */

		/*
		 * And one POSSIBLY one last vestage of grouping by ITG That limit applies to full occupancy and Empirical
		 * occupancy. They were derived as the 98th percentile of Empirical stocking, though adjusted PSP's were
		 * included. If the ouput of this routine is bumped up from empirical to full, MIGHT adjust this limit DOWN
		 * here, so that at end, it is correct. Tentatively decide NOT to do this.
		 */

		// if (fullOccupancy)
		// upperBoundsBaseArea *= EMPOC;

		float ageToUse = breastHeightAge;

		// TODO NDEBUG(2)

		if (ageToUse <= 0f) {
			throw new StandProcessingException("Age was not positive");
		}

		float trAge = FloatMath.log(ageToUse);

		float a00 = Math.max(coe.getCoe(0) + coe.getCoe(1) * trAge, 0f);
		float ap = Math.max(coe.getCoe(3) + coe.getCoe(4) * trAge, 0f);

		float bap;
		if (dominantHeight <= coe.getCoe(2)) {
			bap = 0f;
		} else {
			bap = a00 * FloatMath.pow(dominantHeight - coe.getCoe(2), ap)
					* FloatMath.exp(coe.getCoe(5) * dominantHeight + coe.getCoe(6) * baseAreaOverstory.orElse(0f));
			bap = Math.min(bap, upperBoundBaseArea);
		}

		if (fullOccupancy)
			bap /= EMPOC;

		return bap;
	}

	Coefficients estimateBaseAreaYieldCoefficients(Collection<? extends BaseVdypSpecies> species, BecDefinition bec) {
		var coe = sumCoefficientsWeightedBySpeciesAndDecayBec(species, bec, ControlKey.BA_YIELD, 7);

		// TODO confirm going over 0.5 should drop to 0 as this seems odd.
		coe.scalarInPlace(5, x -> x > 0.0f ? 0f : x);
		return coe;
	}

	Coefficients sumCoefficientsWeightedBySpeciesAndDecayBec(
			Collection<? extends BaseVdypSpecies> species, BecDefinition bec, ControlKey key, int size
	) {
		var coeMap = Utils
				.<MatrixMap2<String, String, Coefficients>>expectParsedControl(controlMap, key, MatrixMap2.class);

		final String decayBecAlias = bec.getDecayBec().getAlias();

		return weightedCoefficientSum(
				size, 0, //
				species, //
				BaseVdypSpecies::getFractionGenus, // Weight by fraction
				spec -> coeMap.get(decayBecAlias, spec.getGenus())

		);
	}

	// EMP107
	/**
	 *
	 * @param dominantHeight  Dominant height (m)
	 * @param breastHeightAge breast height age
	 * @param veteranBaseArea Basal area of overstory (>= 0)
	 * @param species         Species for the layer
	 * @param bec             BEC of the polygon
	 * @param baseAreaGroup   Index of the base area group
	 * @return DQ of primary layer (w DBH >= 7.5)
	 * @throws StandProcessingException
	 */
	float estimateQuadMeanDiameterYield(
			float dominantHeight, float breastHeightAge, Optional<Float> veteranBaseArea,
			Collection<? extends BaseVdypSpecies> species, BecDefinition bec, int baseAreaGroup
	) throws StandProcessingException {
		final var coe = sumCoefficientsWeightedBySpeciesAndDecayBec(species, bec, ControlKey.DQ_YIELD, 6);

		// TODO handle NDEBUG(2) case
		final float ageUse = breastHeightAge;

		final float upperBoundsQuadMeanDiameter = upperBoundsQuadMeanDiameter(baseAreaGroup);

		if (ageUse <= 0f) {
			throw new StandProcessingException("Primary breast height age must be positive but was " + ageUse);
		}

		final float trAge = FloatMath.log(ageUse);

		final float c0 = coe.getCoe(0);
		final float c1 = Math.max(coe.getCoe(1) + coe.getCoe(2) * trAge, 0f);
		final float c2 = Math.max(coe.getCoe(3) + coe.getCoe(4) * trAge, 0f);

		return FloatMath.clamp(c0 + c1 * FloatMath.pow(dominantHeight - 5f, c2), 7.6f, upperBoundsQuadMeanDiameter);

	}

	PolygonMode findDefaultPolygonMode(
			Optional<Float> ageTotal, Optional<Float> yearsToBreastHeight, Optional<Float> height,
			Optional<Float> baseArea, Optional<Float> treesPerHectare, Optional<Float> percentForest,
			Collection<VriSpecies> species, BecDefinition bec, Optional<Integer> baseAreaGroup
	) throws StandProcessingException {
		Optional<Float> ageBH = ageTotal.map(at -> at - yearsToBreastHeight.orElse(3f));

		float bap;
		if (ageBH.map(abh -> abh >= 1).orElse(false)) {
			bap = this.estimateBaseAreaYield(
					height.get(), ageBH.get(), Optional.empty(), false, species, bec, baseAreaGroup.get()
			);
		} else {
			bap = 0;
		}

		var mode = PolygonMode.START;

		Map<String, Float> minMap = Utils.expectParsedControl(controlMap, ControlKey.MINIMA, Map.class);

		float minHeight = minMap.get(BaseControlParser.MINIMUM_HEIGHT);
		float minBA = minMap.get(BaseControlParser.MINIMUM_BASE_AREA);
		float minPredictedBA = minMap.get(BaseControlParser.MINIMUM_PREDICTED_BASE_AREA);

		if (height.map(h -> h < minHeight).orElse(true)) {
			mode = PolygonMode.YOUNG;

			log.atDebug().setMessage("Mode {} because Height {} is below minimum {}.").addArgument(mode)
					.addArgument(height).addArgument(minHeight).log();
		} else if (bap < minPredictedBA) {
			mode = PolygonMode.YOUNG;

			log.atDebug().setMessage("Mode {} because predicted Base Area {} is below minimum {}.").addArgument(mode)
					.addArgument(bap).addArgument(minBA).log();
		} else if (baseArea.map(x -> x == 0).orElse(true) || treesPerHectare.map(x -> x == 0).orElse(true)) {
			mode = PolygonMode.YOUNG;

			log.atDebug().setMessage("Mode {} because given Base Area and Trees Per Hectare were not specified or zero")
					.addArgument(mode).log();
		} else {
			var ration = Utils.mapBoth(baseArea, percentForest, (ba, pf) -> ba * (100f / pf));

			if (ration.map(r -> r < minBA).orElse(false)) {
				mode = PolygonMode.YOUNG;
				log.atDebug().setMessage(
						"Mode {} because ration ({}) of given Base Area ({}) to Percent Forest Land ({}) was below minimum {}"
				).addArgument(mode).addArgument(ration).addArgument(baseArea).addArgument(percentForest)
						.addArgument(minBA).log();

			}
		}
		log.atDebug().setMessage("Defaulting to mode {}.").addArgument(mode).log();

		return mode;
	}

	VdypPolygon createVdypPolygon(VriPolygon sourcePolygon, Map<LayerType, VdypLayer> processedLayers)
			throws ProcessingException {

		// TODO expand this

		var vdypPolygon = VdypPolygon.build(builder -> builder.adapt(sourcePolygon, x -> x.get()));
		vdypPolygon.setLayers(processedLayers);
		return vdypPolygon;
	}

	@Override
	public VdypApplicationIdentifier getId() {
		return VdypApplicationIdentifier.VRI_START;
	}

	@Override
	protected BaseControlParser getControlFileParser() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected VriSpecies copySpecies(VriSpecies toCopy, Consumer<Builder<VriSpecies>> config) {
		return VriSpecies.build(builder -> builder.copy(toCopy));
	}

	static record Increase(float dominantHeight, float ageIncrease) {
	}

	VriPolygon processYoung(VriPolygon poly) throws ProcessingException {

		PolygonIdentifier polygonIdentifier = poly.getPolygonIdentifier();
		int year = polygonIdentifier.getYear();

		if (year < 1900) {
			throw new StandProcessingException("Year for YOUNG stand should be at least 1900 but was " + year);
		}

		var bec = Utils.getBec(poly.getBiogeoclimaticZone(), controlMap);

		var primaryLayer = poly.getLayers().get(LayerType.PRIMARY);
		var primarySite = primaryLayer.getPrimarySite().orElseThrow();
		try {
			SiteIndexEquation siteCurve = primaryLayer.getPrimarySite() //
					.flatMap(BaseVdypSite::getSiteCurveNumber) //
					.map(SiteIndexEquation::getByIndex)//
					.orElseGet(() -> {
						try {
							return this.findSiteCurveNumber(
									bec.getRegion(), primarySite.getSiteSpecies(), primarySite.getSiteGenus()
							);
						} catch (StandProcessingException e) {
							throw new RuntimeStandProcessingException(e);
						}
					});

			float primaryAgeTotal = primarySite.getAgeTotal().orElseThrow(); // AGETOT_L1
			float primaryYearsToBreastHeight = primarySite.getYearsToBreastHeight().orElseThrow(); // YTBH_L1

			float primaryBreastHeightAge0 = primaryAgeTotal - primaryYearsToBreastHeight; // AGEBH0

			float siteIndex = primarySite.getSiteIndex().orElseThrow(); // SID
			float yeastToBreastHeight = primaryYearsToBreastHeight; // YTBHD

			Map<String, Float> minimaMap = Utils.expectParsedControl(controlMap, ControlKey.MINIMA, Map.class);

			float minimumPredictedBaseArea = minimaMap.get(BaseControlParser.MINIMUM_PREDICTED_BASE_AREA); // VMINBAeqn
			float minimumHeight = minimaMap.get(BaseControlParser.MINIMUM_HEIGHT); // VMINH

			// Find an increase that puts stand into suitable condition with EMP106
			// predicting reasonable BA

			float baseAreaTarget = minimumPredictedBaseArea; // BATARGET
			float heightTarget = minimumHeight; // HTARGET
			float ageTarget = 5f; // AGETARGET

			// If PCTFLAND is very low, INCREASE the target BA, so as to avoid rounding
			// problems on output. But Target never increased by more than a factor of 4.
			// Before Jan 2008, this all started at PCTFLAND < 50.

			float percentAvailable = poly.getPercentAvailable().filter(x -> x >= 0f).orElse(85.0f); // PCT

			if (percentAvailable < 10f) {
				float factor = Math.min(10f / percentAvailable, 4f);
				baseAreaTarget *= factor;
			}

			float dominantHeight0 = 0f; // HD0

			int moreYears = Math.max(80, (int) (130 - primaryAgeTotal));

			float primaryHeight = primarySite.getHeight().orElseThrow(); // HT_L1

			final Increase inc = findIncreaseForYoungMode(
					bec, primaryLayer, siteCurve, primaryBreastHeightAge0, siteIndex, yeastToBreastHeight,
					baseAreaTarget, heightTarget, ageTarget, dominantHeight0, moreYears, primaryHeight
			);

			return VriPolygon.build(pBuilder -> {
				pBuilder.copy(poly);
				pBuilder.polygonIdentifier(polygonIdentifier.forYear(year + (int) inc.ageIncrease));
				pBuilder.mode(PolygonMode.BATN);
				pBuilder.copyLayers(poly, (lBuilder, layer) -> {

					lBuilder.copySites(layer, (iBuilder, site) -> {
						if (layer.getLayerType() == LayerType.PRIMARY
								&& primaryLayer.getPrimaryGenus().map(site.getSiteGenus()::equals).orElse(false)) {
							iBuilder.height(inc.dominantHeight);
						} else {
							iBuilder.height(Optional.empty());
						}

						site.getAgeTotal().map(x -> x + inc.ageIncrease).ifPresentOrElse(ageTotal -> {
							iBuilder.ageTotal(ageTotal);
							iBuilder.breastHeightAge(
									site.getYearsToBreastHeight()//
											.map(ytbh -> ageTotal - ytbh)
											.or(() -> site.getBreastHeightAge().map(bha -> bha + inc.ageIncrease))
							);
						}, () -> iBuilder.breastHeightAge(site.getBreastHeightAge().map(bha -> bha + inc.ageIncrease)));

					});
					lBuilder.copySpecies(layer, (sBuilder, species) -> {
						// No changes, just copy
					});
				});
			});

		} catch (RuntimeStandProcessingException e) {
			throw e.getCause();
		} catch (CommonCalculatorException e) {
			throw new StandProcessingException(e);
		}
	}

	private Increase findIncreaseForYoungMode(
			BecDefinition bec, VriLayer primaryLayer, SiteIndexEquation siteCurve, float primaryBreastHeightAge0,
			float siteIndex, float yeastToBreastHeight, float baseAreaTarget, float heightTarget, float ageTarget,
			float dominantHeight0, int moreYears, float primaryHeight
	) throws CommonCalculatorException, StandProcessingException {
		float dominantHeight;
		float ageIncrease;
		for (int increase = 0; increase <= moreYears; increase++) {
			float primaryBreastHeightAge = primaryBreastHeightAge0 + increase; // AGEBH

			if (primaryBreastHeightAge > 1f) {

				float ageD = primaryBreastHeightAge; // AGED

				float dominantHeightD = (float) SiteIndex2Height.indexToHeight(
						siteCurve, ageD, SiteIndexAgeType.SI_AT_BREAST, siteIndex, ageD, yeastToBreastHeight
				); // HDD

				if (increase == 0) {
					dominantHeight0 = dominantHeightD;
				}
				dominantHeight = dominantHeightD; // HD
				if (primaryHeight > 0f && dominantHeight0 > 0f) {
					dominantHeight = primaryHeight + (dominantHeight - dominantHeight0);
				}

				// check empirical BA assuming BAV = 0

				float predictedBaseArea = estimateBaseAreaYield(
						dominantHeight, primaryBreastHeightAge, Optional.empty(), false,
						primaryLayer.getSpecies().values(), bec,
						primaryLayer.getEmpericalRelationshipParameterIndex().orElseThrow()
				); // BAP

				// Calculate the full occupancy BA Hence the BA we will test is the Full
				// occupanct BA

				predictedBaseArea /= FRACTION_AVAILABLE_N;

				if (dominantHeight >= heightTarget && primaryBreastHeightAge >= ageTarget
						&& predictedBaseArea >= baseAreaTarget) {
					ageIncrease = increase;
					return new Increase(dominantHeight, ageIncrease);
				}
			}
		}
		throw new StandProcessingException("Unable to increase to target height.");

	}

	static final <T, B extends ModelClassBuilder<T>> BiConsumer<B, T> noChange() {
		return (builder, toCopy) -> {
			/* Do Nothing */
		};
	}

	VriPolygon processBatc(VriPolygon poly) throws ProcessingException {

		VriLayer primaryLayer = getPrimaryLayer(poly);
		Optional<VriLayer> veteranLayer = getVeteranLayer(poly);
		BecDefinition bec = getBec(poly);

		try {
			//
			final float percentForestLand = poly.getPercentAvailable().orElseGet(() -> {
				try {
					return this.estimatePercentForestLand(poly, veteranLayer, primaryLayer);
				} catch (ProcessingException ex) {
					throw new RuntimeProcessingException(ex);
				}
			}); // PCTFLAND

			final float primaryBreastHeightAge = getLayerBreastHeightAge(primaryLayer).orElseThrow();

			// EMP040
			final float initialPrimaryBaseArea = this
					.estimatePrimaryBaseArea(primaryLayer, bec, poly.getYieldFactor(), primaryBreastHeightAge, 0.0f);

			final Optional<Float> veteranBaseArea = veteranLayer.map(InputLayer::getCrownClosure) // BAV
					.map(ccV -> ccV * initialPrimaryBaseArea / primaryLayer.getCrownClosure());

			final float primaryBaseArea = this.estimatePrimaryBaseArea(
					primaryLayer, bec, poly.getYieldFactor(), primaryBreastHeightAge, veteranBaseArea.orElse(0.0f) // BAP
			);

			final float primaryQuadMeanDiameter = this.estimatePrimaryQuadMeanDiameter(
					primaryLayer, bec, primaryBreastHeightAge, veteranBaseArea.orElse(0f)
			);

			return VriPolygon.build(pBuilder -> {
				pBuilder.copy(poly);

				pBuilder.addLayer(lBuilder -> {
					lBuilder.copy(primaryLayer);
					lBuilder.baseArea(primaryBaseArea * (percentForestLand / 100));
					lBuilder.treesPerHectare(
							BaseAreaTreeDensityDiameter.treesPerHectare(primaryBaseArea, primaryQuadMeanDiameter)
					);
					lBuilder.copySites(primaryLayer, noChange());
					lBuilder.copySpecies(primaryLayer, noChange());
				});
				veteranLayer.ifPresent(vLayer -> pBuilder.addLayer(lBuilder -> {
					lBuilder.copy(vLayer);
					lBuilder.baseArea(veteranBaseArea);

					lBuilder.copySites(primaryLayer, noChange());
					lBuilder.copySpecies(primaryLayer, noChange());
				}));

			});

		} catch (RuntimeProcessingException ex) {
			throw ex.getCause();
		}
	}

	VriPolygon processBatn(VriPolygon poly) throws ProcessingException {

		final VriLayer primaryLayer = poly.getLayers().get(LayerType.PRIMARY);
		final VriSite primarySite = primaryLayer.getPrimarySite()
				.orElseThrow(() -> new StandProcessingException("Primary layer does not have a primary site"));
		final Optional<VriLayer> veteranLayer = Utils.optSafe(poly.getLayers().get(LayerType.VETERAN));
		BecDefinition bec = getBec(poly);

		final float primaryHeight = primarySite.getHeight()
				.orElseThrow(() -> new StandProcessingException("Primary site does not have a height"));
		final float primaryBreastHeightAge = primarySite.getBreastHeightAge()
				.orElseThrow(() -> new StandProcessingException("Primary site does not have a breast height age"));
		final Optional<Float> veteranBaseArea = veteranLayer.flatMap(VriLayer::getBaseArea);

		final int primaryEmpericalRelationshipParameterIndex = primaryLayer.getEmpericalRelationshipParameterIndex()
				.orElseThrow(
						() -> new StandProcessingException(
								"Primary layer does not have an emperical relationship parameter index"
						)
				);

		float primaryBaseAreaEstimated = estimateBaseAreaYield(
				primaryHeight, primaryBreastHeightAge, veteranBaseArea, false, primaryLayer.getSpecies().values(), bec,
				primaryEmpericalRelationshipParameterIndex
		);

		// EMP107
		float normativeQuadMeanDiameter = estimateQuadMeanDiameterYield(
				primaryHeight, primaryBreastHeightAge, veteranBaseArea, primaryLayer.getSpecies().values(), bec,
				primaryEmpericalRelationshipParameterIndex
		);

		final float normativePercentAvailable = 85f;

		final float primaryBaseAreaFinal = primaryBaseAreaEstimated * (100 / normativePercentAvailable);

		final float primaryTreesPerHectare = BaseAreaTreeDensityDiameter
				.treesPerHectare(primaryBaseAreaFinal, normativeQuadMeanDiameter);

		if (primaryBaseAreaFinal < 0.5f) {
			throw new StandProcessingException(
					"Normative esitimate " + primaryBaseAreaFinal + " of base area was less than 0.5."
			);
		}

		return VriPolygon.build(pBuilder -> {
			pBuilder.copy(poly);

			pBuilder.addLayer(lBuilder -> {
				lBuilder.copy(primaryLayer);
				lBuilder.baseArea(primaryBaseAreaFinal);
				lBuilder.treesPerHectare(primaryTreesPerHectare);
				lBuilder.copySites(primaryLayer, noChange());
				lBuilder.copySpecies(primaryLayer, noChange());
			});
			veteranLayer.ifPresent(vLayer -> pBuilder.addLayer(lBuilder -> {
				lBuilder.copy(vLayer);
				lBuilder.baseArea(veteranBaseArea);

				lBuilder.copySites(primaryLayer, noChange());
				lBuilder.copySpecies(primaryLayer, noChange());
			}));

		});

	}

	@Override
	protected ValueOrMarker<Float, Boolean>
			isVeteranForEstimatePercentForestLand(VriPolygon polygon, Optional<VriLayer> vetLayer) {
		return FLOAT_OR_BOOL.marker(vetLayer.isPresent());
	}

	/**
	 * Returns the siteCurveNumber for the first of the given ids that has one.
	 *
	 * @param region
	 * @param ids
	 * @return
	 * @throws StandProcessingException if no entry for any of the given species IDs is present.
	 */
	SiteIndexEquation findSiteCurveNumber(Region region, String... ids) throws StandProcessingException {
		var scnMap = Utils.<MatrixMap2<String, Region, SiteIndexEquation>>expectParsedControl(
				controlMap, ControlKey.SITE_CURVE_NUMBERS, MatrixMap2.class
		);

		for (String id : ids) {
			if (scnMap.hasM(id, region))
				return scnMap.get(id, region);
		}
		throw new StandProcessingException(
				"Could not find Site Curve Number for inst of the following species: " + String.join(", ", ids)
		);
	}

	@Override
	protected Optional<VriSite> getPrimarySite(VriLayer layer) {
		return layer.getPrimarySite();
	}

	@Override
	protected float getYieldFactor(VriPolygon polygon) {
		return polygon.getYieldFactor();
	}
}
