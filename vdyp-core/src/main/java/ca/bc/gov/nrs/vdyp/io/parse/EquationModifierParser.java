package ca.bc.gov.nrs.vdyp.io.parse;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Parser for an equation modifier mapping data file
 *
 * @author Kevin Smith, Vivid Solutions
 *
 */
public class EquationModifierParser implements ControlMapSubResourceParser<Map<Integer, Map<Integer, Integer>>> {

	public static final String CONTROL_KEY = "EQN_MODIFIERS";

	private static final String DEFAULT_KEY = "default";
	private static final String ITG_KEY = "itg";
	private static final String REASSIGNED_KEY = "reassigned";

	LineParser lineParser = new LineParser().integer(3, DEFAULT_KEY).integer(4, ITG_KEY).integer(4, REASSIGNED_KEY);

	public EquationModifierParser() {
	}

	@Override
	public Map<Integer, Map<Integer, Integer>> parse(InputStream is, Map<String, Object> control)
			throws IOException, ResourceParseException {

		Map<Integer, Map<Integer, Integer>> result = new HashMap<>();
		result = lineParser.parse(is, result, (v, r) -> {
			final int defaultId = (int) v.get(DEFAULT_KEY);
			final int itg = (int) v.get(ITG_KEY);
			final int reassignedId = (int) v.get(REASSIGNED_KEY);

			r.computeIfAbsent(defaultId, k -> new HashMap<>()).put(itg, reassignedId);
			return r;
		}, control);

		for (var e : result.entrySet()) {
			result.put(e.getKey(), Collections.unmodifiableMap(e.getValue()));
		}

		return Collections.unmodifiableMap(result);
	}

	@Override
	public String getControlKey() {
		return CONTROL_KEY;
	}

}
