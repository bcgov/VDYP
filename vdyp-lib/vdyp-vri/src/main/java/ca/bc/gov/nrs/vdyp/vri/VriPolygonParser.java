package ca.bc.gov.nrs.vdyp.vri;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import ca.bc.gov.nrs.vdyp.common.ControlKey;
import ca.bc.gov.nrs.vdyp.io.FileResolver;
import ca.bc.gov.nrs.vdyp.io.parse.common.LineParser;
import ca.bc.gov.nrs.vdyp.io.parse.common.ResourceParseException;
import ca.bc.gov.nrs.vdyp.io.parse.control.ControlMapValueReplacer;
import ca.bc.gov.nrs.vdyp.io.parse.streaming.AbstractStreamingParser;
import ca.bc.gov.nrs.vdyp.io.parse.streaming.StreamingParserFactory;
import ca.bc.gov.nrs.vdyp.io.parse.value.ValueParser;
import ca.bc.gov.nrs.vdyp.model.PolygonMode;
import ca.bc.gov.nrs.vdyp.vri.model.VriPolygon;

public class VriPolygonParser implements ControlMapValueReplacer<StreamingParserFactory<VriPolygon>, String> {
	static final String POLYGON_IDENTIFIER = "POLYGON_IDENTIFIER"; // POLYDESC
	static final String BIOGEOGRAPHIC_ZONE = "BIOGEOGRAPHIC_ZONE"; // BEC
	static final String PERCENT_FOREST_LAND = "PERCENT_FOREST_LAND"; // PCTFLAND
	static final String FIP_MODE = "FIP_MODE"; // MODEfip
	static final String NONPRODUCTIVE_DESCRIPTION = "NONPRODUCTIVE_DESCRIPTION"; // NPDESC
	static final String YIELD_FACTOR = "YIELD_FACTOR"; // YLDFACT

	@Override
	public ControlKey getControlKey() {
		return ControlKey.VRI_INPUT_YIELD_POLY;
	}

	@Override
	public StreamingParserFactory<VriPolygon>
			map(String fileName, FileResolver fileResolver, Map<String, Object> control)
					throws IOException, ResourceParseException {
		return () -> {
			var lineParser = new LineParser()//
					.strippedString(25, POLYGON_IDENTIFIER) //
					.space(3) //
					.value(4, BIOGEOGRAPHIC_ZONE, ValueParser.BEC) //
					.space(1) //
					.value(4, PERCENT_FOREST_LAND, ValueParser.optional(ValueParser.FLOAT)) //
					.space(1) //
					.value(2, FIP_MODE, ValueParser.optional(ValueParser.INTEGER)) //
					.space(1) //
					.value(5, NONPRODUCTIVE_DESCRIPTION, ValueParser.optional(ValueParser.STRING)) //
					.value(5, YIELD_FACTOR, ValueParser.optional(ValueParser.FLOAT));

			var is = fileResolver.resolveForInput(fileName);

			return new AbstractStreamingParser<VriPolygon>(is, lineParser, control) {

				@SuppressWarnings("unchecked")
				@Override
				protected VriPolygon convert(Map<String, Object> entry) {
					var polygonId = (String) entry.get(POLYGON_IDENTIFIER);
					var becId = (String) entry.get(BIOGEOGRAPHIC_ZONE);
					var percentForestLand = ((Optional<Float>) entry.get(PERCENT_FOREST_LAND)).filter(x -> x > 0.0f);
					var fipMode = ((Optional<Integer>) entry.get(FIP_MODE)).flatMap(PolygonMode::getByCode);
					var nonproductiveDesc = (Optional<String>) entry.get(NONPRODUCTIVE_DESCRIPTION);
					var yieldFactor = ((Optional<Float>) entry.get(YIELD_FACTOR)).filter(x -> x > 0.0f);
					var percentForestLandWithDefault = percentForestLand.or(
							() -> fipMode
									.flatMap(mode -> mode == PolygonMode.BATC ? Optional.of(85f) : Optional.empty())
					);
					return VriPolygon.build(builder -> {
						builder.polygonIdentifier(polygonId);
						builder.biogeoclimaticZone(becId);
						builder.percentAvailable(percentForestLandWithDefault);
						builder.mode(fipMode);
						builder.nonproductiveDescription(nonproductiveDesc);
						builder.yieldFactor(yieldFactor.orElse(1.0f));
					});
				}

			};
		};
	}

	@Override
	public ValueParser<Object> getValueParser() {
		return FILENAME;
	}
}
