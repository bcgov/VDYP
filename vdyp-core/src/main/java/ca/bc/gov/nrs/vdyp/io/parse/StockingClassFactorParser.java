package ca.bc.gov.nrs.vdyp.io.parse;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.bc.gov.nrs.vdyp.model.Region;
import ca.bc.gov.nrs.vdyp.model.StockingClassFactor;

/**
 * Parser for an stcking class factor data file
 *
 * @author Kevin Smith, Vivid Solutions
 *
 */
public class StockingClassFactorParser
		implements ControlMapSubResourceParser<Map<Character, Map<Region, StockingClassFactor>>> {

	public static final String CONTROL_KEY = "STOCKING_CLASS_FACTORS";

	private static final String STK_KEY = "stk";
	private static final String REGION_KEY = "region";
	private static final String LAYER_KEY = "layer";
	private static final String ITG_KEY = "itg";
	private static final String FACTOR_KEY = "factor";
	private static final String NPCT_KEY = "npct";

	LineParser lineParser = new LineParser() {

		@Override
		public boolean isIgnoredLine(String line) {
			return line.isBlank();
		}

		@Override
		public boolean isIgnoredSegment(List<String> segments) {
			return segments.get(0).isBlank();
		}

		@Override
		public boolean isStopSegment(List<String> segments) {
			return "Z".equalsIgnoreCase(segments.get(0));
		}
	}.value(1, STK_KEY, ValueParser.CHARACTER).space(1).value(1, REGION_KEY, ValueParser.REGION).space(1)
			.value(1, LAYER_KEY, ValueParser.CHARACTER).integer(3, ITG_KEY).floating(6, FACTOR_KEY)
			.integer(5, NPCT_KEY);

	public StockingClassFactorParser() {
	}

	@Override
	public Map<Character, Map<Region, StockingClassFactor>> parse(InputStream is, Map<String, Object> control)
			throws IOException, ResourceParseException {

		Map<Character, Map<Region, StockingClassFactor>> result = new HashMap<>();
		result = lineParser.parse(is, result, (v, r) -> {
			char stk = (char) v.get(STK_KEY);
			Region region = (Region) v.get(REGION_KEY);
			float factor = (float) v.get(FACTOR_KEY);
			int npctArea = (int) v.get(NPCT_KEY);

			// Fortran was ignoring Layer and ITG

			var factorEntry = new StockingClassFactor(stk, region, factor, npctArea);

			r.computeIfAbsent(stk, (c) -> new HashMap<Region, StockingClassFactor>()).put(region, factorEntry);

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
