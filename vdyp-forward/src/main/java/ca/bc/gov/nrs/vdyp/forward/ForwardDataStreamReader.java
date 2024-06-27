package ca.bc.gov.nrs.vdyp.forward;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.bc.gov.nrs.vdyp.application.ProcessingException;
import ca.bc.gov.nrs.vdyp.common.ControlKey;
import ca.bc.gov.nrs.vdyp.forward.model.VdypLayerSpecies;
import ca.bc.gov.nrs.vdyp.forward.model.VdypPolygon;
import ca.bc.gov.nrs.vdyp.forward.model.VdypPolygonDescription;
import ca.bc.gov.nrs.vdyp.forward.model.VdypPolygonLayer;
import ca.bc.gov.nrs.vdyp.forward.model.VdypSpeciesUtilization;
import ca.bc.gov.nrs.vdyp.io.parse.common.ResourceParseException;
import ca.bc.gov.nrs.vdyp.io.parse.streaming.StreamingParser;
import ca.bc.gov.nrs.vdyp.io.parse.streaming.StreamingParserFactory;
import ca.bc.gov.nrs.vdyp.model.LayerType;
import ca.bc.gov.nrs.vdyp.model.UtilizationClass;

public class ForwardDataStreamReader {

	private static final Logger logger = LoggerFactory.getLogger(ForwardDataStreamReader.class);

	private final StreamingParser<VdypPolygon> polygonStream;
	private final StreamingParser<Collection<VdypLayerSpecies>> layerSpeciesStream;
	private final StreamingParser<Collection<VdypSpeciesUtilization>> speciesUtilizationStream;

	@SuppressWarnings("unchecked")
	public ForwardDataStreamReader(Map<String, Object> controlMap) throws IOException {

		var polygonStreamFactory = controlMap.get(ControlKey.FORWARD_INPUT_VDYP_POLY.name());
		polygonStream = ((StreamingParserFactory<VdypPolygon>) polygonStreamFactory).get();

		var layerSpeciesStreamFactory = controlMap.get(ControlKey.FORWARD_INPUT_VDYP_LAYER_BY_SPECIES.name());
		layerSpeciesStream = ((StreamingParserFactory<Collection<VdypLayerSpecies>>) layerSpeciesStreamFactory).get();

		var speciesUtilizationStreamFactory = controlMap.get(ControlKey.FORWARD_INPUT_VDYP_LAYER_BY_SP0_BY_UTIL.name());
		speciesUtilizationStream = ((StreamingParserFactory<Collection<VdypSpeciesUtilization>>) speciesUtilizationStreamFactory)
				.get();
	}

	public VdypPolygon readNextPolygon(VdypPolygonDescription polygonDescription) throws ProcessingException {

		// Advance all the streams until the definition for the polygon is found.

		logger.debug("Looking for polygon {}", polygonDescription);

		Optional<VdypPolygon> thePolygon = Optional.empty();

		try {
			while (thePolygon.isEmpty() && polygonStream.hasNext()) {
				var polygon = polygonStream.next();

				logger.debug("Reading polygon {}", polygon);

				var utilizationCollection = speciesUtilizationStream.next();
				var utilizationsBySpeciesMap = new HashMap<UtilizationBySpeciesKey, Map<UtilizationClass, VdypSpeciesUtilization>>();
				for (var utilization : utilizationCollection) {
					logger.trace("Saw utilization {}", utilization);

					var key = new UtilizationBySpeciesKey(utilization.getLayerType(), utilization.getGenusIndex());
					utilizationsBySpeciesMap.putIfAbsent(key, new EnumMap<>(UtilizationClass.class));
					utilizationsBySpeciesMap.get(key).put(utilization.getUcIndex(), utilization);
				}

				var speciesCollection = layerSpeciesStream.next();
				var primarySpecies = new HashMap<Integer, VdypLayerSpecies>();
				var veteranSpecies = new HashMap<Integer, VdypLayerSpecies>();
				for (var species : speciesCollection) {
					logger.trace("Saw species {}", species);

					var key = new UtilizationBySpeciesKey(species.getLayerType(), species.getGenusIndex());
					var speciesUtilizations = utilizationsBySpeciesMap.get(key);

					if (speciesUtilizations != null) {
						species.setUtilizations(Optional.of(speciesUtilizations));

						for (VdypSpeciesUtilization u : speciesUtilizations.values()) {
							u.setParent(species);
						}
					} else {
						species.setUtilizations(Optional.empty());
					}

					if (LayerType.PRIMARY.equals(species.getLayerType())) {
						primarySpecies.put(species.getGenusIndex(), species);
					} else if (LayerType.VETERAN.equals(species.getLayerType())) {
						veteranSpecies.put(species.getGenusIndex(), species);
					} else {
						throw new IllegalStateException(
								MessageFormat.format(
										"Unrecognized layer type {} for species {} of polygon {}",
										species.getLayerType(), species.getGenusIndex(), polygon.getDescription()
								)
						);
					}
				}

				VdypPolygonLayer primaryLayer = null;
				if (primarySpecies.size() > 0) {

					var key = new UtilizationBySpeciesKey(LayerType.PRIMARY, 0);
					Map<UtilizationClass, VdypSpeciesUtilization> defaultSpeciesUtilization = utilizationsBySpeciesMap
							.get(key);

					primaryLayer = new VdypPolygonLayer(
							LayerType.PRIMARY, polygon, primarySpecies, Optional.ofNullable(defaultSpeciesUtilization)
					);

					for (VdypLayerSpecies v : primarySpecies.values()) {
						v.setParent(primaryLayer);
					}
				}

				VdypPolygonLayer veteranLayer = null;
				if (veteranSpecies.size() > 0) {

					var key = new UtilizationBySpeciesKey(LayerType.VETERAN, 0);
					Map<UtilizationClass, VdypSpeciesUtilization> defaultSpeciesUtilization = utilizationsBySpeciesMap
							.get(key);

					veteranLayer = new VdypPolygonLayer(
							LayerType.VETERAN, polygon, veteranSpecies, Optional.ofNullable(defaultSpeciesUtilization)
					);

					for (VdypLayerSpecies v : veteranSpecies.values()) {
						v.setParent(veteranLayer);
					}
				}

				polygon.setLayers(primaryLayer, veteranLayer);

				if (polygonDescription.equals(polygon.getDescription())) {
					thePolygon = Optional.of(polygon);

					adjustUtilizations(polygon);
				}
			}
		} catch (ResourceParseException | IOException e) {
			throw new ProcessingException(e);
		}

		if (thePolygon.isEmpty()) {
			throw new ProcessingException(
					MessageFormat.format("Unable to find the definition of {0}", polygonDescription)
			);
		}

		return thePolygon.get();
	}

	/** 
	 * Both scale the per-hectare values of all the utilizations of the primary
	 * layer of the given polygon, and for all utilizations of both the primary and
	 * veteran layer (if present) of the polygon,
	 * 1. Adjust the basal area to be within bounds of the utilization class, and
	 * 2. Calculate the quad-mean-diameter value from the basal area and trees per hectare.
	 * 
	 * @param polygon
	 */
	private void adjustUtilizations(VdypPolygon polygon) throws ProcessingException {

		float percentForestedLand = polygon.getPercentForestLand();
		assert !Float.isNaN(percentForestedLand);
		float scalingFactor = 100.0f / percentForestedLand;

		List<VdypSpeciesUtilization> utilizationsToAdjust = new ArrayList<>();

		for (VdypPolygonLayer l : polygon.getLayers()) {

			l.getDefaultUtilizationMap().ifPresent(m -> utilizationsToAdjust.addAll(m.values()));

			l.getGenera().values().stream()
					.forEach(s -> s.getUtilizations().ifPresent(m -> utilizationsToAdjust.addAll(m.values())));
		}

		for (VdypSpeciesUtilization u : utilizationsToAdjust) {

			if (percentForestedLand > 0.0f && percentForestedLand < 100.0f) {
				u.scale(scalingFactor);
			}

			u.doPostCreateAdjustments();
		}
	}

	private class UtilizationBySpeciesKey {
		private final LayerType layerType;
		private final Integer genusIndex;

		public UtilizationBySpeciesKey(LayerType layerType, Integer genusIndex) {
			this.layerType = layerType;
			this.genusIndex = genusIndex;
		}

		@Override
		public boolean equals(Object other) {
			if (other instanceof UtilizationBySpeciesKey that) {
				return layerType.equals(that.layerType) && genusIndex.equals(that.genusIndex);
			} else {
				return false;
			}
		}

		@Override
		public int hashCode() {
			return layerType.hashCode() * 17 + genusIndex.hashCode();
		}
	}
}
