package ca.bc.gov.nrs.vdyp.vri;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import ca.bc.gov.nrs.vdyp.common.ControlKey;
import ca.bc.gov.nrs.vdyp.common.Utils;
import ca.bc.gov.nrs.vdyp.common.ValueOrMarker;
import ca.bc.gov.nrs.vdyp.io.EndOfRecord;
import ca.bc.gov.nrs.vdyp.io.FileResolver;
import ca.bc.gov.nrs.vdyp.io.parse.common.LineParser;
import ca.bc.gov.nrs.vdyp.io.parse.common.ResourceParseException;
import ca.bc.gov.nrs.vdyp.io.parse.common.ResourceParseValidException;
import ca.bc.gov.nrs.vdyp.io.parse.control.ControlMapValueReplacer;
import ca.bc.gov.nrs.vdyp.io.parse.streaming.AbstractStreamingParser;
import ca.bc.gov.nrs.vdyp.io.parse.streaming.GroupingStreamingParser;
import ca.bc.gov.nrs.vdyp.io.parse.streaming.StreamingParserFactory;
import ca.bc.gov.nrs.vdyp.io.parse.value.ControlledValueParser;
import ca.bc.gov.nrs.vdyp.io.parse.value.ValueParser;
import ca.bc.gov.nrs.vdyp.model.LayerType;
import ca.bc.gov.nrs.vdyp.vri.model.VriSpecies;

public class VriSpeciesParser
		implements ControlMapValueReplacer<StreamingParserFactory<Collection<VriSpecies>>, String> {

	static final String GENUS = "GENUS"; // SP0
	static final String PERCENT_GENUS = "PERCENT_GENUS"; // PTVSP0

	static final String SPECIES_1 = "SPECIES_1"; // SP641
	static final String PERCENT_SPECIES_1 = "PERCENT_SPECIES_1"; // PCT1
	static final String SPECIES_2 = "SPECIES_2"; // SP642
	static final String PERCENT_SPECIES_2 = "PERCENT_SPECIES_2"; // PCT2
	static final String SPECIES_3 = "SPECIES_3"; // SP643
	static final String PERCENT_SPECIES_3 = "PERCENT_SPECIES_3"; // PCT3
	static final String SPECIES_4 = "SPECIES_4"; // SP644
	static final String PERCENT_SPECIES_4 = "PERCENT_SPECIES_4"; // PCT4

	// TODO reduce code duplication with FipSpecies

	@Override
	public ControlKey getControlKey() {
		return ControlKey.VRI_INPUT_YIELD_SPEC_DIST;
	}

	@Override
	public StreamingParserFactory<Collection<VriSpecies>>
			map(String fileName, FileResolver fileResolver, Map<String, Object> control)
					throws IOException, ResourceParseException {
		return () -> {
			var lineParser = new LineParser() {

				@Override
				public boolean isIgnoredSegment(List<String> entry) {
					if (Utils.getIfPresent(entry, 2).map(String::strip).map("Z"::equals).orElse(false)) {
						return false;
					}
					return Utils.getIfPresent(entry, 5).map(Utils::parsesBlankOrNonPositive).orElse(true);
				}

			} //
					.strippedString(25, VriPolygonParser.POLYGON_IDENTIFIER) //
					.space(1) //
					.value(
							1, VriLayerParser.LAYER,
							ValueParser.valueOrMarker(
									ValueParser.LAYER,
									ValueParser.optionalSingleton("Z"::equals, EndOfRecord.END_OF_RECORD)
							)
					) //
					.space(1) //
					.value(2, GENUS, ControlledValueParser.optional(ValueParser.GENUS)) //
					.value(6, PERCENT_GENUS, ValueParser.PERCENTAGE) //
					.value(3, SPECIES_1, ControlledValueParser.optional(ValueParser.SPECIES)) //
					.value(5, PERCENT_SPECIES_1, ValueParser.PERCENTAGE) //
					.value(3, SPECIES_2, ControlledValueParser.optional(ValueParser.SPECIES)) //
					.value(5, PERCENT_SPECIES_2, ValueParser.PERCENTAGE) //
					.value(3, SPECIES_3, ControlledValueParser.optional(ValueParser.SPECIES)) //
					.value(5, PERCENT_SPECIES_3, ValueParser.PERCENTAGE) //
					.value(3, SPECIES_4, ControlledValueParser.optional(ValueParser.SPECIES)) //
					.value(5, PERCENT_SPECIES_4, ValueParser.PERCENTAGE);

			var is = fileResolver.resolveForInput(fileName);

			var delegateStream = new AbstractStreamingParser<ValueOrMarker<Optional<VriSpecies>, EndOfRecord>>(
					is, lineParser, control
			) {

				@SuppressWarnings("unchecked")
				@Override
				protected ValueOrMarker<Optional<VriSpecies>, EndOfRecord> convert(Map<String, Object> entry)
						throws ResourceParseException {
					var polygonId = (String) entry.get(VriPolygonParser.POLYGON_IDENTIFIER);
					var species = (ValueOrMarker<Optional<LayerType>, EndOfRecord>) entry.get(VriLayerParser.LAYER);
					String genus;
					if (species.isValue()) {
						genus = ((Optional<String>) entry.get(GENUS)).orElseThrow(
								() -> new ResourceParseValidException(
										"Genus identifier can not be empty except in end of record entries"
								)
						);
					} else {
						genus = null;
					}
					var percentGenus = (Float) entry.get(PERCENT_GENUS);
					var species1 = (Optional<String>) entry.get(SPECIES_1);
					var percentSpecies1 = (Float) entry.get(PERCENT_SPECIES_1);
					var species2 = (Optional<String>) entry.get(SPECIES_2);
					var percentSpecies2 = (Float) entry.get(PERCENT_SPECIES_2);
					var species3 = (Optional<String>) entry.get(SPECIES_3);
					var percentSpecies3 = (Float) entry.get(PERCENT_SPECIES_3);
					var species4 = (Optional<String>) entry.get(SPECIES_4);
					var percentSpecies4 = (Float) entry.get(PERCENT_SPECIES_4);

					var markerBuilder = new ValueOrMarker.Builder<Optional<VriSpecies>, EndOfRecord>();
					return species.handle(s -> {
						return markerBuilder.value(s.map(layerType -> {
							Map<String, Float> speciesPercent = new LinkedHashMap<>();
							species1.ifPresent((sp) -> speciesPercent.put(sp, percentSpecies1));
							species2.ifPresent((sp) -> speciesPercent.put(sp, percentSpecies2));
							species3.ifPresent((sp) -> speciesPercent.put(sp, percentSpecies3));
							species4.ifPresent((sp) -> speciesPercent.put(sp, percentSpecies4));
							return VriSpecies.build(specBuilder -> {
								specBuilder.polygonIdentifier(polygonId);
								specBuilder.layerType(layerType);
								specBuilder.genus(genus);
								specBuilder.percentGenus(percentGenus);
								specBuilder.addSpecies(speciesPercent);
							});
						}));
					}, markerBuilder::marker);
				}

			};

			return new GroupingStreamingParser<Collection<VriSpecies>, ValueOrMarker<Optional<VriSpecies>, EndOfRecord>>(
					delegateStream
			) {

				@Override
				protected boolean skip(ValueOrMarker<Optional<VriSpecies>, EndOfRecord> nextChild) {
					return nextChild.getValue().map(Optional::isEmpty).orElse(false);
				}

				@Override
				protected boolean stop(ValueOrMarker<Optional<VriSpecies>, EndOfRecord> nextChild) {
					return nextChild.isMarker();
				}

				@Override
				protected Collection<VriSpecies>
						convert(List<ValueOrMarker<Optional<VriSpecies>, EndOfRecord>> children) {
					return children.stream().map(ValueOrMarker::getValue).map(Optional::get) // Should never be empty as
							// we've filtered out
							// markers
							.flatMap(Optional::stream) // Skip if empty (and unknown layer type)
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
