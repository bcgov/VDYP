package ca.bc.gov.nrs.vdyp.forward;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import ca.bc.gov.nrs.vdyp.common.ControlKey;
import ca.bc.gov.nrs.vdyp.common.Utils;
import ca.bc.gov.nrs.vdyp.common.ValueOrMarker;
import ca.bc.gov.nrs.vdyp.forward.model.VdypLayerSpecies;
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
import ca.bc.gov.nrs.vdyp.model.SpeciesDistribution;
import ca.bc.gov.nrs.vdyp.model.SpeciesDistributionSet;

public class VdypSpeciesParser implements ControlMapValueReplacer<Object, String> {

	private static final String DESCRIPTION = "DESCRIPTION"; // POLYDESC
	private static final String LAYER_TYPE = "LAYER_TYPE"; // LAYERG
	private static final String GENUS_INDEX = "SPECIES_INDEX"; // ISP
	private static final String GENUS = "SPECIES"; // SP0

	private static final String SPECIES_1 = "SPECIES_1"; // SP641
	private static final String PERCENT_SPECIES_1 = "PERCENT_SPECIES_1"; // PCT1
	private static final String SPECIES_2 = "SPECIES_2"; // SP642
	private static final String PERCENT_SPECIES_2 = "PERCENT_SPECIES_2"; // PCT2
	private static final String SPECIES_3 = "SPECIES_3"; // SP643
	private static final String PERCENT_SPECIES_3 = "PERCENT_SPECIES_3"; // PCT3
	private static final String SPECIES_4 = "SPECIES_4"; // SP644
	private static final String PERCENT_SPECIES_4 = "PERCENT_SPECIES_4"; // PCT4

	private static final String SITE_INDEX = "SITE_INDEX"; // SI
	private static final String DOMINANT_HEIGHT = "DOMINANT_HEIGHT"; // HD
	private static final String TOTAL_AGE = "TOTAL_AGE"; // AGETOT
	private static final String AGE_AT_BREAST_HEIGHT = "AGE_AT_BREAST_HEIGHT"; // AGEBH
	private static final String YEARS_TO_BREAST_HEIGHT = "YEARS_TO_BREAST_HEIGHT"; // YTBH
	private static final String IS_PRIMARY_SPECIES = "IS_PRIMARY_SPECIES"; // INSITESP
	private static final String SITE_CURVE_NUMBER = "SITE_CURVE_NUMBER"; // SCN

	@Override
	public ControlKey getControlKey() {
		return ControlKey.FORWARD_INPUT_VDYP_LAYER_BY_SPECIES;
	}

	@Override
	public StreamingParserFactory<Collection<VdypLayerSpecies>>
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
					).space(1).value(2, GENUS_INDEX, ValueParser.INTEGER).space(1)
					.value(2, GENUS, ControlledValueParser.optional(ValueParser.GENUS)).space(1)
					.value(3, SPECIES_1, ValueParser.SPECIES).value(5, PERCENT_SPECIES_1, ValueParser.PERCENTAGE)
					.value(3, SPECIES_2, ControlledValueParser.optional(ValueParser.SPECIES))
					.value(5, PERCENT_SPECIES_2, ControlledValueParser.optional(ValueParser.PERCENTAGE))
					.value(3, SPECIES_3, ControlledValueParser.optional(ValueParser.SPECIES))
					.value(5, PERCENT_SPECIES_3, ControlledValueParser.optional(ValueParser.PERCENTAGE))
					.value(3, SPECIES_4, ControlledValueParser.optional(ValueParser.SPECIES))
					.value(5, PERCENT_SPECIES_4, ControlledValueParser.optional(ValueParser.PERCENTAGE))
					.value(6, SITE_INDEX, ValueParser.FLOAT).value(6, DOMINANT_HEIGHT, ValueParser.FLOAT)
					.value(6, TOTAL_AGE, ValueParser.FLOAT).value(6, AGE_AT_BREAST_HEIGHT, ValueParser.FLOAT)
					.value(6, YEARS_TO_BREAST_HEIGHT, ValueParser.FLOAT)
					.value(2, IS_PRIMARY_SPECIES, ControlledValueParser.optional(ValueParser.LOGICAL_0_1))
					.value(3, SITE_CURVE_NUMBER, ControlledValueParser.optional(ValueParser.INTEGER));

			var is = fileResolver.resolveForInput(fileName);

			var delegateStream = new AbstractStreamingParser<ValueOrMarker<Optional<VdypLayerSpecies>, EndOfRecord>>(
					is, lineParser, control
			) {
				@SuppressWarnings("unchecked")
				@Override
				protected ValueOrMarker<Optional<VdypLayerSpecies>, EndOfRecord> convert(Map<String, Object> entry)
						throws ResourceParseException {

					var polygonId = (String) entry.get(DESCRIPTION);
					var layerType = (ValueOrMarker<Optional<LayerType>, EndOfRecord>) entry.get(LAYER_TYPE);
					if (layerType == null) {
						var builder = new ValueOrMarker.Builder<Optional<LayerType>, EndOfRecord>();
						layerType = builder.marker(EndOfRecord.END_OF_RECORD);
					}
					var genusIndex = (Integer) entry.get(GENUS_INDEX);
					var genus = (Optional<String>) entry.get(GENUS);
					var species1 = (String) entry.get(SPECIES_1);
					var percentSpecies1 = (Float) entry.get(PERCENT_SPECIES_1);
					var species2 = (Optional<String>) entry.get(SPECIES_2);
					var percentSpecies2 = (Optional<Float>) entry.get(PERCENT_SPECIES_2);
					var species3 = (Optional<String>) entry.get(SPECIES_3);
					var percentSpecies3 = (Optional<Float>) entry.get(PERCENT_SPECIES_3);
					var species4 = (Optional<String>) entry.get(SPECIES_4);
					var percentSpecies4 = (Optional<Float>) entry.get(PERCENT_SPECIES_4);
					var siteIndex = (Float) entry.get(SITE_INDEX);
					var dominantHeight = (Float) entry.get(DOMINANT_HEIGHT);
					var totalAge = (Float) entry.get(TOTAL_AGE);
					var ageAtBreastHeight = (Float) entry.get(AGE_AT_BREAST_HEIGHT);
					var yearsToBreastHeight = (Float) entry.get(YEARS_TO_BREAST_HEIGHT);
					var isPrimarySpecies = Utils.<Boolean>optSafe(entry.get(IS_PRIMARY_SPECIES));
					var siteCurveNumber = Optional.of(Utils.<Integer>optSafe(entry.get(SITE_CURVE_NUMBER)).orElse(9));

					var builder = new ValueOrMarker.Builder<Optional<VdypLayerSpecies>, EndOfRecord>();
					return layerType.handle(l -> {
						return builder.value(l.map(lt -> {

							List<SpeciesDistribution> sdList = new ArrayList<>();
							sdList.add(new SpeciesDistribution(species1, percentSpecies1));
							Utils.ifBothPresent(species2, percentSpecies2, (s, p) -> {
								sdList.add(new SpeciesDistribution(s, p));
							});
							Utils.ifBothPresent(species3, percentSpecies3, (s, p) -> {
								sdList.add(new SpeciesDistribution(s, p));
							});
							Utils.ifBothPresent(species4, percentSpecies4, (s, p) -> {
								sdList.add(new SpeciesDistribution(s, p));
							});
							SpeciesDistributionSet speciesDistributionSet = new SpeciesDistributionSet(sdList);

							return new VdypLayerSpecies(
									polygonId, lt, genusIndex, genus, speciesDistributionSet, siteIndex, dominantHeight,
									totalAge, ageAtBreastHeight, yearsToBreastHeight, isPrimarySpecies, siteCurveNumber
							);
						}));
					}, builder::marker);

				}
			};

			return new GroupingStreamingParser<Collection<VdypLayerSpecies>, ValueOrMarker<Optional<VdypLayerSpecies>, EndOfRecord>>(
					delegateStream
			) {

				@Override
				protected boolean skip(ValueOrMarker<Optional<VdypLayerSpecies>, EndOfRecord> nextChild) {
					return nextChild.getValue().map(Optional::isEmpty).orElse(false);
				}

				@Override
				protected boolean stop(ValueOrMarker<Optional<VdypLayerSpecies>, EndOfRecord> nextChild) {
					return nextChild.isMarker();
				}

				@Override
				protected Collection<VdypLayerSpecies>
						convert(List<ValueOrMarker<Optional<VdypLayerSpecies>, EndOfRecord>> children) {
					return children.stream().map(ValueOrMarker::getValue).map(Optional::get).flatMap(Optional::stream) // Skip
																														// if
																														// empty
																														// (and
																														// unknown
																														// layer
																														// type)
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
