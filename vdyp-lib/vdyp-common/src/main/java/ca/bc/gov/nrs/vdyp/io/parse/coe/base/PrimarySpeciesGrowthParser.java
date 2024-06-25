package ca.bc.gov.nrs.vdyp.io.parse.coe.base;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import ca.bc.gov.nrs.vdyp.io.parse.common.LineParser;
import ca.bc.gov.nrs.vdyp.io.parse.common.ResourceParseException;
import ca.bc.gov.nrs.vdyp.io.parse.control.ControlMapSubResourceParser;
import ca.bc.gov.nrs.vdyp.io.parse.value.ValueParseException;
import ca.bc.gov.nrs.vdyp.io.parse.value.ValueParser;
import ca.bc.gov.nrs.vdyp.model.Coefficients;
import ca.bc.gov.nrs.vdyp.model.ModelCoefficients;

/**
 * Parses a mapping from a Basal Area Group number to a ModelCoefficients instance. Each row contains
 * <ol>
 * <li>(cols 0-1) int - Basal Area Group number</li>
 * <li>(cols 2-4) float - max. basal area for group</li>
 * <li>(cols 5-14, 15-24, 25-34) float - coefficients for group function</li>
 * </ol>
 * All lines are parsed. There is no provision for blank lines. If a value for a given Basal Area Group is not included
 * in the file, the values model value and coefficients (0.0, 0.0, 0.0) are used.
 * <p>
 * The result of the parse is a map from a Basal Area Group number to ModelCoefficients instance.
 *
 * @author Michael Junkin, Vivid Solutions
 * @see ControlMapSubResourceParser
 */
public abstract class PrimarySpeciesGrowthParser
		implements ControlMapSubResourceParser<Map<Integer, ModelCoefficients>> {

	private static final int MAX_BASAL_AREA_GROUP_NUMBER = 30;

	private static final ModelCoefficients defaultModelCoefficients = new ModelCoefficients(
			0, new Coefficients(new float[] { 0.0f, 0.0f, 0.0f }, 1)
	);

	public static final String BASAL_AREA_GROUP_ID_KEY = "BasalAreaGroupId";
	public static final String MODEL_NUMBER_KEY = "Model";
	public static final String COEFFICIENTS_KEY = "Coefficients";

	protected PrimarySpeciesGrowthParser() {

		this.lineParser = new LineParser() {
			@Override
			public boolean isStopLine(String line) {
				return line == null || line.trim().length() == 0;
			}
		}.value(2, BASAL_AREA_GROUP_ID_KEY, ValueParser.INTEGER).value(3, MODEL_NUMBER_KEY, ValueParser.INTEGER)
				.multiValue(3, 10, COEFFICIENTS_KEY, ValueParser.FLOAT);
	}

	private LineParser lineParser;

	@Override
	public Map<Integer, ModelCoefficients> parse(InputStream is, Map<String, Object> control)
			throws IOException, ResourceParseException {

		Map<Integer, ModelCoefficients> result = new HashMap<>();

		lineParser.parse(is, result, (value, r, lineNumber) -> {
			var basalAreaGroupId = (Integer) value.get(BASAL_AREA_GROUP_ID_KEY);
			var modelNumber = (Integer) value.get(MODEL_NUMBER_KEY);

			@SuppressWarnings("unchecked")
			var coefficientList = (List<Float>) value.get(COEFFICIENTS_KEY);

			if (basalAreaGroupId < 1 || basalAreaGroupId > MAX_BASAL_AREA_GROUP_NUMBER) {
				throw new ValueParseException(
						MessageFormat.format(
								"Line {0}: basal area group id {1} is out of range; expecting a value from 1 to {2}",
								lineNumber, basalAreaGroupId, MAX_BASAL_AREA_GROUP_NUMBER
						)
				);
			}

			Coefficients coefficients = new Coefficients(coefficientList, 1);

			ModelCoefficients mc = new ModelCoefficients(modelNumber, coefficients);

			r.put(basalAreaGroupId, mc);

			return r;
		}, control);

		IntStream.rangeClosed(1, MAX_BASAL_AREA_GROUP_NUMBER).forEach(i -> {
			if (!result.containsKey(i))
				result.put(i, defaultModelCoefficients);
		});

		return result;
	}
}
