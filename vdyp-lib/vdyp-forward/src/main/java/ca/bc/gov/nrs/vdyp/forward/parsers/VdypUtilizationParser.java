package ca.bc.gov.nrs.vdyp.forward.parsers;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import ca.bc.gov.nrs.vdyp.common.ControlKey;
import ca.bc.gov.nrs.vdyp.common.ValueOrMarker;
import ca.bc.gov.nrs.vdyp.forward.model.VdypSpeciesUtilization;
import ca.bc.gov.nrs.vdyp.io.EndOfRecord;
import ca.bc.gov.nrs.vdyp.io.FileResolver;
import ca.bc.gov.nrs.vdyp.io.parse.common.LineParser;
import ca.bc.gov.nrs.vdyp.io.parse.common.ResourceParseException;
import ca.bc.gov.nrs.vdyp.io.parse.control.ControlMapValueReplacer;
import ca.bc.gov.nrs.vdyp.io.parse.streaming.AbstractStreamingParser;
import ca.bc.gov.nrs.vdyp.io.parse.streaming.GroupingStreamingParser;
import ca.bc.gov.nrs.vdyp.io.parse.streaming.StreamingParserFactory;
import ca.bc.gov.nrs.vdyp.io.parse.value.ControlledValueParser;
import ca.bc.gov.nrs.vdyp.io.parse.value.ValueParser;
import ca.bc.gov.nrs.vdyp.model.LayerType;
import ca.bc.gov.nrs.vdyp.model.UtilizationClass;

public class VdypUtilizationParser implements ControlMapValueReplacer<Object, String> {

	private static final String DESCRIPTION = "DESCRIPTION"; // POLYDESC
	private static final String LAYER_TYPE = "LAYER_TYPE"; // LAYERG
	private static final String GENUS_INDEX = "SPECIES_INDEX"; // ISP
	private static final String GENUS = "SPECIES"; // SP0
	private static final String UTILIZATION_CLASS_INDEX = "UTILIZATION_CLASS_INDEX"; // J
	private static final String BASAL_AREA = "BASAL_AREA"; // BA
	private static final String LIVE_TREES_PER_HECTARE = "LIVE_TREES_PER_HECTARE"; // TPH
	private static final String LOREY_HEIGHT = "LOREY_HEIGHT"; // LHJ
	private static final String WHOLE_STEM_VOLUME = "WHOLE_STEM_VOLUME"; // VOLWS
	private static final String CLOSE_UTIL_VOLUME = "CLOSE_UTIL_VOLUME"; // VOLCU
	private static final String CU_VOLUME_LESS_DECAY = "CU_VOLUME_LESS_DECAY"; // VOL_D
	private static final String CU_VOLUME_LESS_DECAY_WASTAGE = "CU_VOLUME_LESS_DECAY_WASTAGE"; // VOL_DW
	private static final String CU_VOLUME_LESS_DECAY_WASTAGE_BREAKAGE = "CU_VOLUME_LESS_DECAY_WASTAGE_BREAKAGE"; // VOL_DWB
	private static final String QUADRATIC_MEAN_DIAMETER_BREAST_HEIGHT = "QUADRATIC_MEAN_DIAMETER_BREAST_HEIGHT"; // DQ

	@Override
	public ControlKey getControlKey() {
		return ControlKey.FORWARD_INPUT_VDYP_LAYER_BY_SP0_BY_UTIL;
	}

	@Override
	public StreamingParserFactory<Collection<VdypSpeciesUtilization>>
			map(String fileName, FileResolver fileResolver, Map<String, Object> control)
					throws IOException, ResourceParseException {
		return () -> {
			var lineParser = new LineParser().strippedString(25, DESCRIPTION).space(1)
					.value(
							1, LAYER_TYPE,
							ValueParser.valueOrMarker(
									ValueParser.LAYER,
									ValueParser.optionalSingleton(
											x -> x == null || x.trim().length() == 0 || x.trim().equals("Z"),
											EndOfRecord.END_OF_RECORD
									)
							)
					).value(3, GENUS_INDEX, ValueParser.INTEGER).space(1)
					.value(2, GENUS, ControlledValueParser.optional(ValueParser.GENUS))
					.value(3, UTILIZATION_CLASS_INDEX, ControlledValueParser.UTILIZATION_CLASS)
					.value(9, BASAL_AREA, VdypForwardDefaultingParser.FLOAT_WITH_DEFAULT)
					.value(9, LIVE_TREES_PER_HECTARE, VdypForwardDefaultingParser.FLOAT_WITH_DEFAULT)
					.value(9, LOREY_HEIGHT, VdypForwardDefaultingParser.FLOAT_WITH_DEFAULT)
					.value(9, WHOLE_STEM_VOLUME, VdypForwardDefaultingParser.FLOAT_WITH_DEFAULT)
					.value(9, CLOSE_UTIL_VOLUME, VdypForwardDefaultingParser.FLOAT_WITH_DEFAULT)
					.value(9, CU_VOLUME_LESS_DECAY, VdypForwardDefaultingParser.FLOAT_WITH_DEFAULT)
					.value(9, CU_VOLUME_LESS_DECAY_WASTAGE, VdypForwardDefaultingParser.FLOAT_WITH_DEFAULT)
					.value(9, CU_VOLUME_LESS_DECAY_WASTAGE_BREAKAGE, VdypForwardDefaultingParser.FLOAT_WITH_DEFAULT)
					.value(6, QUADRATIC_MEAN_DIAMETER_BREAST_HEIGHT, VdypForwardDefaultingParser.FLOAT_WITH_DEFAULT);

			var is = fileResolver.resolveForInput(fileName);

			var delegateStream = new AbstractStreamingParser<ValueOrMarker<Optional<VdypSpeciesUtilization>, EndOfRecord>>(
					is, lineParser, control
			) {
				@SuppressWarnings("unchecked")
				@Override
				protected ValueOrMarker<Optional<VdypSpeciesUtilization>, EndOfRecord>
						convert(Map<String, Object> entry) throws ResourceParseException {

					var polygonId = VdypPolygonDescriptionParser.parse((String) entry.get(DESCRIPTION));
					var layerType = (ValueOrMarker<Optional<LayerType>, EndOfRecord>) entry.get(LAYER_TYPE);
					if (layerType == null) {
						var builder = new ValueOrMarker.Builder<Optional<LayerType>, EndOfRecord>();
						layerType = builder.marker(EndOfRecord.END_OF_RECORD);
					}
					var genusIndex = (Integer) entry.get(GENUS_INDEX);
					var genus = (Optional<String>) entry.get(GENUS);
					var utilizationClass = (UtilizationClass) entry.get(UTILIZATION_CLASS_INDEX);
					var basalArea = (Float) (entry.get(BASAL_AREA));
					var liveTreesPerHectare = (Float) (entry.get(LIVE_TREES_PER_HECTARE));
					var loreyHeight = (Float) (entry.get(LOREY_HEIGHT));
					var wholeStemVolume = (Float) (entry.get(WHOLE_STEM_VOLUME));
					var closeUtilVolume = (Float) (entry.get(CLOSE_UTIL_VOLUME));
					var cuVolumeLessDecay = (Float) (entry.get(CU_VOLUME_LESS_DECAY));
					var cuVolumeLessDecayWastage = (Float) (entry.get(CU_VOLUME_LESS_DECAY_WASTAGE));
					var cuVolumeLessDecayWastageBreakage = (Float) (entry.get(CU_VOLUME_LESS_DECAY_WASTAGE_BREAKAGE));
					var quadraticMeanDBH = (Float) (entry.get(QUADRATIC_MEAN_DIAMETER_BREAST_HEIGHT));

					var builder = new ValueOrMarker.Builder<Optional<VdypSpeciesUtilization>, EndOfRecord>();
					return layerType.handle(l -> {
						return builder.value(l.map(lt -> {
							return new VdypSpeciesUtilization(
									polygonId, lt, genusIndex, genus, utilizationClass, basalArea, liveTreesPerHectare,
									loreyHeight, wholeStemVolume, closeUtilVolume, cuVolumeLessDecay,
									cuVolumeLessDecayWastage, cuVolumeLessDecayWastageBreakage, quadraticMeanDBH
							);
						}));
					}, builder::marker);
				}
			};

			return new GroupingStreamingParser<Collection<VdypSpeciesUtilization>, ValueOrMarker<Optional<VdypSpeciesUtilization>, EndOfRecord>>(
					delegateStream
			) {

				@Override
				protected boolean skip(ValueOrMarker<Optional<VdypSpeciesUtilization>, EndOfRecord> nextChild) {
					return nextChild.getValue().map(Optional::isEmpty).orElse(false);
				}

				@Override
				protected boolean stop(ValueOrMarker<Optional<VdypSpeciesUtilization>, EndOfRecord> nextChild) {
					return nextChild.isMarker();
				}

				@Override
				protected Collection<VdypSpeciesUtilization>
						convert(List<ValueOrMarker<Optional<VdypSpeciesUtilization>, EndOfRecord>> children) {
					// Skip if empty (and unknown layer type)
					return children.stream().map(ValueOrMarker::getValue).map(Optional::get).flatMap(Optional::stream)
							.toList();
				}
			};
		};
	}

	@Override
	public ValueParser<Object> getValueParser() {
		return FILENAME;
	}
}
