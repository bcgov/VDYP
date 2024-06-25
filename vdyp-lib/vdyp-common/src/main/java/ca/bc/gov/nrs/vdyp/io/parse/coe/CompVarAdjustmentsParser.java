package ca.bc.gov.nrs.vdyp.io.parse.coe;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import ca.bc.gov.nrs.vdyp.common.ControlKey;
import ca.bc.gov.nrs.vdyp.io.parse.common.LineParser;
import ca.bc.gov.nrs.vdyp.io.parse.common.ResourceParseException;
import ca.bc.gov.nrs.vdyp.io.parse.control.OptionalControlMapSubResourceParser;
import ca.bc.gov.nrs.vdyp.io.parse.value.ControlledValueParser;
import ca.bc.gov.nrs.vdyp.io.parse.value.ValueParser;
import ca.bc.gov.nrs.vdyp.model.CompVarAdjustments;

/**
 * Parses a Comp Var Adjustments data file.
 *
 * These files have multiple lines, each containing an index number and a float value.
 * <ol>
 * <li>(cols 0-2) index - an integer from 1 to 98</li>
 * <li>(cols 3-9) float - adjustment value for that index</li>
 * </ol>
 * Any line whose index value is blank or missing is considered a blank line. An index of 999 stops the parse; this line
 * and all following are ignored.
 * <p>
 * The result of the parse is a {@link Map} of indices to floats. The supported indices are:
 * <ul>
 * <li>1 - small species (Utilization Class -1) basal area adjustment
 * <li>2 - small species quadratic mean diameter adjustment
 * <li>3 - small species lorey height adjustment
 * <li>4 - small species volume adjustment
 * <li>51 - lorey height adjustment for primary species
 * <li>52 - lorey height adjustment for other species
 * <li>5 - 8 - basal area adjustment for Utilization Classes 1 - 4, respectively
 * <li>15 - 18 - quadratic mean diameter adjustment for Utilization Classes 1 - 4, respectively
 * <li>11, 21, 31, 41: whole stem volume adjustment for Utilization Classes 1 - 4, respectively
 * <li>12, 22, 32, 42: close util volume adjustment for Utilization Classes 1 - 4, respectively
 * <li>13, 23, 33, 43: close util volume, less decay, adjustment for Utilization Classes 1 - 4, respectively
 * <li>14, 24, 34, 44: close util volume, less decay, less waste, adjustment for Utilization Classes 1 - 4, respectively
 * </ul>
 * <p>
 * Control index: 028
 * <p>
 * Example: coe/CVADJ.PRM
 *
 * @see OptionalControlMapSubResourceParser
 * @author Michael Junkin, Vivid Solutions
 */
public class CompVarAdjustmentsParser implements OptionalControlMapSubResourceParser<CompVarAdjustments> {
	private static final String INDEX_KEY = "index";
	private static final String ADJUSTMENT_KEY = "adjustmentKey";

	private LineParser lineParser = new LineParser() {
		@Override
		public boolean isStopLine(String line) {
			return line.startsWith("999");
		}

		@Override
		public boolean isIgnoredLine(String line) {
			return line.substring(0, Math.min(line.length(), 3)).trim().length() == 0;
		}
	}.value(3, INDEX_KEY, PARSE_INDEX).value(8, ADJUSTMENT_KEY, ValueParser.FLOAT);

	static final ControlledValueParser<Integer> PARSE_INDEX = ControlledValueParser
			.validate(ValueParser.INTEGER, (v, c) -> {
				if (v < CompVarAdjustments.MIN_INDEX || v > CompVarAdjustments.MAX_INDEX) {
					return Optional.of(
							"Index " + v + " not in the range " + CompVarAdjustments.MIN_INDEX + " to "
									+ CompVarAdjustments.MAX_INDEX + " inclusive"
					);
				}
				return Optional.empty();
			});

	@Override
	public CompVarAdjustments defaultResult() {
		return new CompVarAdjustments();
	}

	@Override
	public CompVarAdjustments parse(InputStream is, Map<String, Object> control)
			throws IOException, ResourceParseException {

		Map<Integer, Float> values = new HashMap<>();

		lineParser.parse(is, values, (value, r, lineNumber) -> {
			var index = (Integer) value.get(INDEX_KEY);
			var adjustment = (Float) value.get(ADJUSTMENT_KEY);

			r.put(index, adjustment);

			return r;
		}, control);

		return new CompVarAdjustments(values);
	}

	@Override
	public ControlKey getControlKey() {
		return ControlKey.PARAM_ADJUSTMENTS;
	}
}
