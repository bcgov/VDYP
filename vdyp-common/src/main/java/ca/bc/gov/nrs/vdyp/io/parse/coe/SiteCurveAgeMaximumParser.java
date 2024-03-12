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
import ca.bc.gov.nrs.vdyp.model.SiteCurveAgeMaximum;

/**
 * Parses a Site Curve Maximum Age data file.
 *
 * These files have multiple lines, each containing a key followed by four floats.
 * <ol>
 * <li>(cols 0-2) site curve key - an integer from -1 to 40. "-1" defines the defaults for all site curves not given in
 * the file</li>
 * <li>(cols 3-9) float - max total age that a site curve will increment (coastal region)</li>
 * <li>(cols 10-16) float - max total age that a site curve will increment (interior region)</li>
 * <li>(cols 17-23) float - "T1"</li>
 * <li>(cols 24-30) float - "T2". T1 and T2 are "parameters that control special site curve modifications beyond max
 * total age."</li>
 * </ol>
 * All lines in the file are read; there is no provision for blank lines. A Site Curve key of 999 stops the parse; this
 * line and all following are ignored.
 * <p>
 * The result of the parse is a {@link Map} of Site Curve numbers to quads (the four floats.)
 * <p>
 * FIP Control index: 026
 * <p>
 * Example: coe/SIAGEMAX.PRM
 *
 * @see OptionalControlMapSubResourceParser
 * @author Kevin Smith, Vivid Solutions
 */
public class SiteCurveAgeMaximumParser
		implements OptionalControlMapSubResourceParser<Map<Integer, SiteCurveAgeMaximum>> {
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

	private LineParser lineParser = new LineParser() {

		@Override
		public boolean isStopLine(String line) {
			return line.startsWith("999");
		}

	}.value(3, SC_KEY, PARSE_SC).value(7, COASTAL_KEY, PARSE_AGE).value(7, INTERIOR_KEY, PARSE_AGE)
			.value(7, T1_KEY, ValueParser.FLOAT).value(7, T2_KEY, ValueParser.FLOAT);

	static final ControlledValueParser<Integer> PARSE_SC = ControlledValueParser
			.validate(ValueParser.INTEGER, (v, c) -> {
				if (v < -1 || v > MAX_SC) {
					return Optional.of("Site curve number must be in the range -1 to " + MAX_SC + " inclusive");
				}
				return Optional.empty();
			});

	static final ValueParser<Float> PARSE_AGE = s -> {
		var value = ValueParser.FLOAT.parse(s);
		return value <= 0.0 ? MAX_AGE : value;
	};

	@Override
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
		lineParser.parse(is, result, (value, r, line) -> {
			var sc = (int) value.get(SC_KEY);
			var ageCoast = (float) value.get(COASTAL_KEY);
			var ageInt = (float) value.get(INTERIOR_KEY);
			var t1 = (float) value.get(T1_KEY);
			var t2 = (float) value.get(T2_KEY);

			if (sc <= -1) {
				for (sc = 0; sc <= 140; sc++) {
					r.put(sc, new SiteCurveAgeMaximum(ageCoast, ageInt, t1, t2));
				}
			} else {
				r.put(sc, new SiteCurveAgeMaximum(ageCoast, ageInt, t1, t2));
			}

			return r;
		}, control);
		return result;
	}

	@Override
	public ControlKey getControlKey() {
		return ControlKey.SITE_CURVE_AGE_MAX;
	}

}
