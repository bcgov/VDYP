package ca.bc.gov.nrs.vdyp.io.parse.coe;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.bc.gov.nrs.vdyp.common.ControlKey;
import ca.bc.gov.nrs.vdyp.common.Utils;
import ca.bc.gov.nrs.vdyp.io.parse.common.LineParser;
import ca.bc.gov.nrs.vdyp.io.parse.common.ResourceParseException;
import ca.bc.gov.nrs.vdyp.io.parse.control.ControlMapSubResourceParser;
import ca.bc.gov.nrs.vdyp.io.parse.value.ValueParseException;
import ca.bc.gov.nrs.vdyp.io.parse.value.ValueParser;
import ca.bc.gov.nrs.vdyp.model.Coefficients;

/**
 * Parses a mapping from a Basal Area Group number to a list of eight coefficients. Each row contains
 * <ol>
 * <li>(cols 0-2) int - Basal Area Group number</li>
 * <li>(cols 3-11, 12-20, 21-30, 31-39, 40-48, 49-57) float * 6 - coefficient list (9 characters)</li>
 * <li>(cols 58-63, 64-69) float * 2 - coefficient list (6 characters)</li>
 * </ol>
 * All lines are parsed. There is no provision for blank lines; all lines must have content.
 * <p>
 * The result of the parse is a map from a Basal Area Group number to a (zero-based) eight-element coefficient array.
 * <p>
 * Control index: 123
 * <p>
 * Example file: coe/REGDQL2.COE
 *
 * @author Michael Junkin, Vivid Solutions
 * @see ControlMapSubResourceParser
 */
public class DqGrowthEmpiricalLimitsParser implements ControlMapSubResourceParser<Map<Integer, Coefficients>> {

	public static final int MAX_BASAL_AREA_GROUP_ID = 40;

	public static final String BASAL_AREA_GROUP_ID_KEY = "BasalAreaGroupId";
	public static final String COEFFICIENTS_9_KEY = "Coefficients-9";
	public static final String COEFFICIENTS_6_KEY = "Coefficients-6";

	public DqGrowthEmpiricalLimitsParser() {

		this.lineParser = new LineParser() {
			@Override
			public boolean isStopLine(String line) {
				return Utils.nullOrBlank(line);
			}
		}.value(3, BASAL_AREA_GROUP_ID_KEY, ValueParser.INTEGER).multiValue(6, 9, COEFFICIENTS_9_KEY, ValueParser.FLOAT)
				.multiValue(2, 6, COEFFICIENTS_6_KEY, ValueParser.FLOAT);
	}

	private LineParser lineParser;

	@Override
	public Map<Integer, Coefficients> parse(InputStream is, Map<String, Object> control)
			throws IOException, ResourceParseException {

		Map<Integer, Coefficients> result = new HashMap<>();

		lineParser.parse(is, result, (value, r, lineNumber) -> {
			var basalAreaGroupId = (Integer) value.get(BASAL_AREA_GROUP_ID_KEY);

			if (basalAreaGroupId < 1 || basalAreaGroupId > MAX_BASAL_AREA_GROUP_ID) {
				throw new ValueParseException(
						MessageFormat.format(
								"Line {0}: basal area group id {1} is out of range; expecting a value from 1 to {2}",
								lineNumber, basalAreaGroupId, MAX_BASAL_AREA_GROUP_ID
						)
				);
			}

			List<Float> coefficientList = new ArrayList<Float>();
			@SuppressWarnings("unchecked")
			var coefficient9List = (List<Float>) value.get(COEFFICIENTS_9_KEY);
			@SuppressWarnings("unchecked")
			var coefficient6List = (List<Float>) value.get(COEFFICIENTS_6_KEY);

			coefficientList.addAll(coefficient9List);
			coefficientList.addAll(coefficient6List);

			r.put(basalAreaGroupId, new Coefficients(coefficientList, 0));

			return r;
		}, control);

		return result;
	}

	@Override
	public ControlKey getControlKey() {
		return ControlKey.DQ_GROWTH_EMPIRICAL_LIMITS;
	}
}
