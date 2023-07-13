package ca.bc.gov.nrs.vdyp.io.parse;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import ca.bc.gov.nrs.vdyp.model.SiteCurveAgeMaximum;

/**
 * Parses a Site curve maximum age data file.
 *
 * @author Kevin Smith, Vivid Solutions
 *
 */
public class SiteCurveAgeMaximumParser
		implements OptionalControlMapSubResourceParser<Map<Integer, SiteCurveAgeMaximum>> {
	public static final String CONTROL_KEY = "SITE_CURVE_AGE_MAX";

	public static final String SC_KEY = "siteCurve";
	public static final String COASTAL_KEY = "coastalAgeMax";
	public static final String INTERIOR_KEY = "interiorAgeMax";
	public static final String T1_KEY = "t1";
	public static final String T2_KEY = "t2";

	private static final float MAX_AGE = 1999.0f;
	private static final int MAX_SC = 40;

	private static final float DEFAULT_AGE = 140.0f;
	private static final float DEFAULT_T1 = 0.0f;
	private static final float DEFAULT_T2 = 0.0f;

	public static final int DEFAULT_SC = 140;

	LineParser lineParser = new LineParser() {

		@Override
		public boolean isStopLine(String line) {
			return line.startsWith("999");
		}

	}.value(3, SC_KEY, PARSE_SC).value(7, COASTAL_KEY, PARSE_AGE).value(7, INTERIOR_KEY, PARSE_AGE)
			.value(7, T1_KEY, PARSE_AGE).value(7, T2_KEY, PARSE_AGE);

	static ControlledValueParser<Integer> PARSE_SC = ControlledValueParser.validate(ValueParser.INTEGER, (v, c) -> {
		if (v < -1 || v > MAX_SC) {
			return Optional.of("Site curve number must be in the range -1 to " + MAX_SC + " inclusive");
		}
		return Optional.empty();
	});

	static ValueParser<Float> PARSE_AGE = s -> {
		var value = ValueParser.FLOAT.parse(s);
		return value <= 0.0 ? MAX_AGE : value;
	};

	@SuppressWarnings("serial")
	public Map<Integer, SiteCurveAgeMaximum> defaultResult() {
		return new HashMap<>() {
			@Override
			public SiteCurveAgeMaximum get(Object key) {
				return containsKey(key) ? super.get(key)
						: new SiteCurveAgeMaximum(DEFAULT_AGE, DEFAULT_AGE, DEFAULT_T1, DEFAULT_T2);
			}
		};
	}

	@Override
	public Map<Integer, SiteCurveAgeMaximum> parse(InputStream is, Map<String, Object> control)
			throws IOException, ResourceParseException {
		Map<Integer, SiteCurveAgeMaximum> result = defaultResult();
		lineParser.parse(is, result, (v, r) -> {
			var sc = (int) v.get(SC_KEY);
			var ageCoast = (float) v.get(COASTAL_KEY);
			var ageInt = (float) v.get(INTERIOR_KEY);
			var t1 = (float) v.get(T1_KEY);
			var t2 = (float) v.get(T2_KEY);

			if (sc <= -1)
				sc = DEFAULT_SC;

			r.put(sc, new SiteCurveAgeMaximum(ageCoast, ageInt, t1, t2));

			return r;
		}, control);
		return result;
	}

	@Override
	public String getControlKey() {
		return CONTROL_KEY;
	}

}
