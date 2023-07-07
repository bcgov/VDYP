package ca.bc.gov.nrs.vdyp.io.parse;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import ca.bc.gov.nrs.vdyp.common.ExpectationDifference;
import ca.bc.gov.nrs.vdyp.model.SiteCurve;

public class SiteCurveParser implements OptionalControlMapSubResourceParser<Map<String, SiteCurve>> {
	public static final String CONTROL_KEY = "SITE_CURVE_NUMBERS";

	public static final String SPECIES_KEY = "species";
	public static final String VALUE_1_KEY = "value1";
	public static final String VALUE_2_KEY = "value2";

	LineParser lineParser = new LineParser() {

		@Override
		public boolean isStopLine(String line) {
			return line.startsWith("##");
		}

		@Override
		public boolean isIgnoredLine(String line) {
			return line.isBlank() || line.startsWith("# ") || line.startsWith("  ");
		}

	}.strippedString(3, SPECIES_KEY).integer(3, VALUE_1_KEY).integer(3, VALUE_2_KEY);

	@Override
	public Map<String, SiteCurve> parse(InputStream is, Map<String, Object> control)
			throws IOException, ResourceParseException {
		Map<String, SiteCurve> result = new HashMap<>();
		lineParser.parse(is, result, (v, r) -> {
			var species = (String) v.get(SPECIES_KEY);
			var value1 = (int) v.get(VALUE_1_KEY);
			var value2 = (int) v.get(VALUE_2_KEY);
			r.put(species, new SiteCurve(value1, value2));

			return r;
		}, control);
		final var sp0List = GenusDefinitionParser.getSpeciesAliases(control);

		var missing = ExpectationDifference.difference(result.keySet(), sp0List).getMissing();
		if (!missing.isEmpty()) {
			throw new ResourceParseValidException("Missing expected entries for " + String.join(", ", missing));
		}
		return result;
	}

	@Override
	public String getControlKey() {
		return CONTROL_KEY;
	}

	@Override
	public Map<String, SiteCurve> defaultResult() {
		return Collections.emptyMap();
	}

}
