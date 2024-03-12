package ca.bc.gov.nrs.vdyp.io.parse.coe;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import ca.bc.gov.nrs.vdyp.common.ControlKey;
import ca.bc.gov.nrs.vdyp.io.parse.common.LineParser;
import ca.bc.gov.nrs.vdyp.io.parse.common.ResourceParseException;
import ca.bc.gov.nrs.vdyp.io.parse.control.ControlMapSubResourceParser;
import ca.bc.gov.nrs.vdyp.io.parse.value.ValueParseException;
import ca.bc.gov.nrs.vdyp.io.parse.value.ValueParser;
import ca.bc.gov.nrs.vdyp.model.Coefficients;

/**
 * Parses a mapping from a Basal Area Group number to a list of two coefficients. Each row contains
 * <ol>
 * <li>(cols 0-2) int - Basal Area Group number</li>
 * <li>(cols 3-10) float - max. basal area for group</li>
 * <li>(cols 12-19) float - max. quadratic mean diameter for group</li>
 * </ol>
 * All lines are parsed. Lines with a Basal Area Group number that's blank or has the integer value 0 are treated as
 * blank lines and ignored. If a value for a given Basal Area Group is not included in the file, the values 0.0f (BA)
 * and 7.6f (DQ) are used.
 * <p>
 * The result of the parse is a map from a Basal Area Group number to a (one-based) two-element coefficient array.
 * <p>
 * Control index: 108
 * <p>
 * Example file: coe/PCT_407.coe
 *
 * @author Michael Junkin, Vivid Solutions
 * @see ControlMapSubResourceParser
 */
public class UpperBoundsParser implements ControlMapSubResourceParser<Map<Integer, Coefficients>> {

	public static final int LAST_BA_GROUP_ID = 180;

	private static final Coefficients defaultCoefficients = new Coefficients(new float[] { 0.0f, 7.6f }, 1);

	public static final String BASAL_AREA_GROUP_ID_KEY = "baGroupId";
	public static final String MAX_BA_KEY = "maxBaKey";
	public static final String MAX_DQ_KEY = "maxDqKey";

	private static final Pattern ZEROES = Pattern.compile("0{0,3}");

	public UpperBoundsParser() {

		this.lineParser = new LineParser() {

			@Override
			public boolean isIgnoredLine(String line) {
				String basalAreaGroupIdText = line.substring(0, Math.min(3, line.length())).trim();
				return ZEROES.matcher(basalAreaGroupIdText).matches();
			}
		}.value(3, BASAL_AREA_GROUP_ID_KEY, ValueParser.INTEGER).value(8, MAX_BA_KEY, ValueParser.FLOAT).space(1)
				.value(8, MAX_DQ_KEY, ValueParser.FLOAT);
	}

	LineParser lineParser;

	@Override
	public Map<Integer, Coefficients> parse(InputStream is, Map<String, Object> control)
			throws IOException, ResourceParseException {

		Map<Integer, Coefficients> result = new HashMap<>();

		lineParser.parse(is, result, (value, r, lineNumber) -> {
			var baGroupId = (int) value.get(BASAL_AREA_GROUP_ID_KEY);
			var maxBaKey = (float) value.get(MAX_BA_KEY);
			var maxDqKey = (float) value.get(MAX_DQ_KEY);

			if (baGroupId < 0 || baGroupId >= LAST_BA_GROUP_ID) {
				throw new ValueParseException(
						MessageFormat.format(
								"Line {0}: Basal Area Group Id {0} is out of range; expecting a value from 1 to {1}",
								lineNumber, baGroupId, LAST_BA_GROUP_ID
						)
				);
			}

			r.put(baGroupId, new Coefficients(new float[] { maxBaKey, maxDqKey }, 1));

			return r;
		}, control);

		IntStream.rangeClosed(1, LAST_BA_GROUP_ID).forEach(i -> {
			if (!result.containsKey(i))
				result.put(i, defaultCoefficients);
		});

		return result;
	}

	@Override
	public ControlKey getControlKey() {
		return ControlKey.BA_DQ_UPPER_BOUNDS;
	}
}
