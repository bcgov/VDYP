package ca.bc.gov.nrs.vdyp.io.parse.coe;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import ca.bc.gov.nrs.vdyp.common.ControlKey;
import ca.bc.gov.nrs.vdyp.io.parse.common.LineParser;
import ca.bc.gov.nrs.vdyp.io.parse.common.ResourceParseException;
import ca.bc.gov.nrs.vdyp.io.parse.control.ControlMapSubResourceParser;
import ca.bc.gov.nrs.vdyp.io.parse.value.ValueParser;
import ca.bc.gov.nrs.vdyp.model.MatrixMap2;
import ca.bc.gov.nrs.vdyp.model.MatrixMap2Impl;
import ca.bc.gov.nrs.vdyp.model.Region;
import ca.bc.gov.nrs.vdyp.model.StockingClassFactor;

/**
 * Parser for a Stocking Class Factor data file.
 * <ol>
 * <li>(col 0) stocking class code</li>
 * <li>(col 2) region code ('C' or 'I')</li>
 * <li>(col 4) layer (ignored)</li>
 * <li>(cols 5-7) - int - ITG value (ignored)</li>
 * <li>(cols 10-15) - float - stocking factopr</li>
 * <li>(cols 16-20) - int - percentage</li>
 * </ol>
 * The result of the parse is a {@link MatrixMap2} from Stocking Class and Region to a {@link StockingClassFactor}.
 * <p>
 * A line with stocking class 'Z' terminates the parse. There is no provision for blank lines prior to this line. It is
 * legal to have multiple definitions for a given stocking class factor; the last one in the file wins. There can be no
 * more than ten stocking classes with definitions.
 * <p>
 * FIP Control index: 033
 * <p>
 * Example file: coe/FIPSTKR.PRM
 *
 * @author Kevin Smith, Vivid Solutions
 * @see ControlMapSubResourceParser
 */
public class StockingClassFactorParser implements
		ControlMapSubResourceParser<MatrixMap2<Character /* Stocking class */, Region, Optional<StockingClassFactor>>> {

	private static final String STK_KEY = "stk";
	private static final String REGION_KEY = "region";
	private static final String LAYER_KEY = "layer";
	private static final String ITG_KEY = "itg";
	private static final String FACTOR_KEY = "factor";
	private static final String NPCT_KEY = "npct";

	private LineParser lineParser = new LineParser() {

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

	@Override
	public MatrixMap2<Character /* Stocking class */, Region, Optional<StockingClassFactor>>
			parse(InputStream is, Map<String, Object> control) throws IOException, ResourceParseException {

		Map<Character, Map<Region, StockingClassFactor>> result1 = lineParser
				.parse(is, new HashMap<>(), (value, result, line) -> {
					char stk = (char) value.get(STK_KEY);
					Region region = (Region) value.get(REGION_KEY);
					float factor = (float) value.get(FACTOR_KEY);
					int npctArea = (int) value.get(NPCT_KEY);

					// Fortran was ignoring Layer and ITG

					var factorEntry = new StockingClassFactor(stk, region, factor, npctArea);

					result.computeIfAbsent(stk, c -> new HashMap<Region, StockingClassFactor>())
							.put(region, factorEntry);

					return result;
				}, control);

		Collection<Region> regions = List.of(Region.values());
		Collection<Character> classes = result1.keySet();
		return new MatrixMap2Impl<>(classes, regions, (k1, k2) -> {
			var subMap = result1.get(k1);
			if (subMap == null) {
				return Optional.empty();
			}
			return Optional.ofNullable(subMap.get(k2));
		});

	}

	@Override
	public ControlKey getControlKey() {
		return ControlKey.STOCKING_CLASS_FACTORS;
	}

}
