package ca.bc.gov.nrs.vdyp.fip;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import ca.bc.gov.nrs.vdyp.fip.model.FipMode;
import ca.bc.gov.nrs.vdyp.fip.model.FipPolygon;
import ca.bc.gov.nrs.vdyp.io.FileResolver;
import ca.bc.gov.nrs.vdyp.io.parse.AbstractStreamingParser;
import ca.bc.gov.nrs.vdyp.io.parse.ControlMapValueReplacer;
import ca.bc.gov.nrs.vdyp.io.parse.LineParser;
import ca.bc.gov.nrs.vdyp.io.parse.ResourceParseException;
import ca.bc.gov.nrs.vdyp.io.parse.StreamingParserFactory;
import ca.bc.gov.nrs.vdyp.io.parse.ValueParser;

public class FipPolygonParser implements ControlMapValueReplacer<StreamingParserFactory<FipPolygon>, String> {

	public static final String CONTROL_KEY = "FIP_POLYGONS";

	static final String POLYGON_IDENTIFIER = "POLYGON_IDENTIFIER"; // POLYDESC
	static final String FOREST_INVENTORY_ZONE = "FOREST_INVENTORY_ZONE"; // FIZ
	static final String BIOGEOGRAPHIC_ZONE = "BIOGEOGRAPHIC_ZONE"; // BEC
	static final String PERCENT_FOREST_LAND = "PERCENT_FOREST_LAND"; // PCTFLAND
	static final String FIP_MODE = "FIP_MODE"; // MODEfip
	static final String NONPRODUCTIVE_DESCRIPTION = "NONPRODUCTIVE_DESCRIPTION"; // NPDESC
	static final String YIELD_FACTOR = "YIELD_FACTOR"; // YLDFACT

	@Override
	public String getControlKey() {
		return CONTROL_KEY;
	}

	@Override
	public StreamingParserFactory<FipPolygon>
			map(String fileName, FileResolver fileResolver, Map<String, Object> control)
					throws IOException, ResourceParseException {
		return () -> {
			var lineParser = new LineParser().strippedString(25, POLYGON_IDENTIFIER).space(1)
					.strippedString(1, FOREST_INVENTORY_ZONE).space(1).value(4, BIOGEOGRAPHIC_ZONE, ValueParser.BEC)
					.space(1).value(4, PERCENT_FOREST_LAND, ValueParser.optional(ValueParser.FLOAT)).space(1)
					.value(2, FIP_MODE, ValueParser.optional(ValueParser.INTEGER)).space(1)
					.value(5, NONPRODUCTIVE_DESCRIPTION, ValueParser.optional(ValueParser.STRING))
					.value(5, YIELD_FACTOR, ValueParser.optional(ValueParser.FLOAT));

			var is = fileResolver.resolve(fileName);

			return new AbstractStreamingParser<FipPolygon>(is, lineParser, control) {

				@SuppressWarnings("unchecked")
				@Override
				protected FipPolygon convert(Map<String, Object> entry) {
					var polygonId = (String) entry.get(POLYGON_IDENTIFIER);
					var fizId = (String) entry.get(FOREST_INVENTORY_ZONE);
					var becId = (String) entry.get(BIOGEOGRAPHIC_ZONE);
					var percentForestLand = (Optional<Float>) entry.get(PERCENT_FOREST_LAND);
					var fipMode = (Optional<Integer>) entry.get(FIP_MODE);
					var nonproductiveDesc = (Optional<String>) entry.get(NONPRODUCTIVE_DESCRIPTION);
					var yieldFactor = (Optional<Float>) entry.get(YIELD_FACTOR);

					percentForestLand = percentForestLand.filter(x -> x > 0.0f);
					yieldFactor = yieldFactor.filter(x -> x > 0.0f);

					return new FipPolygon(
							polygonId, fizId, becId, percentForestLand, fipMode.flatMap(FipMode::getByCode),
							nonproductiveDesc, yieldFactor.orElse(1.0f)
					);
				}

			};
		};
	}

}
