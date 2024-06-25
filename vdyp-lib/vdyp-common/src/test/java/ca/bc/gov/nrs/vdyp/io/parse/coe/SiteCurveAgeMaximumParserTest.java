package ca.bc.gov.nrs.vdyp.io.parse.coe;

import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.hasSpecificEntry;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;

import java.util.HashMap;
import java.util.Map;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;

import ca.bc.gov.nrs.vdyp.model.Region;
import ca.bc.gov.nrs.vdyp.model.SiteCurveAgeMaximum;
import ca.bc.gov.nrs.vdyp.test.TestUtils;

public class SiteCurveAgeMaximumParserTest {

	@Test
	void testSimple() throws Exception {
		var parser = new SiteCurveAgeMaximumParser();
		var is = TestUtils.makeInputStream(" 16  150.0  150.0   20.0   60.0");

		Map<String, Object> controlMap = new HashMap<>();

		var result = parser.parse(is, controlMap);

		assertThat(
				result,
				hasEntry(
						is(16),
						allOf(
								hasAge(Region.COASTAL, is(150.0f)), hasAge(Region.INTERIOR, is(150.0f)),
								hasProperty("t1", is(20.0f)), hasProperty("t2", is(60.0f))
						)
				)
		);
	}

	@Test
	void testDefault() throws Exception {
		var parser = new SiteCurveAgeMaximumParser();
		var is = TestUtils.makeInputStream(" -1  150.0  150.0   20.0   60.0");

		Map<String, Object> controlMap = new HashMap<>();

		var result = parser.parse(is, controlMap);

		assertThat(
				result,
				hasEntry(
						is(SiteCurveAgeMaximumParser.DEFAULT_SC),
						allOf(
								hasAge(Region.COASTAL, is(150.0f)), hasAge(Region.INTERIOR, is(150.0f)),
								hasProperty("t1", is(20.0f)), hasProperty("t2", is(60.0f))
						)
				)
		);
	}

	@Test
	void testEndLine() throws Exception {
		var parser = new SiteCurveAgeMaximumParser();
		var is = TestUtils.makeInputStream(
				" -1  150.0  150.0   20.0   60.0", "999                                End of usuable info",
				" 42  160.0  145.0   25.0   65.0"
		);

		Map<String, Object> controlMap = new HashMap<>();

		var result = parser.parse(is, controlMap);

		assertThat(
				result,
				hasEntry(
						is(SiteCurveAgeMaximumParser.DEFAULT_SC),
						allOf(
								hasAge(Region.COASTAL, is(150.0f)), hasAge(Region.INTERIOR, is(150.0f)),
								hasProperty("t1", is(20.0f)), hasProperty("t2", is(60.0f))
						)
				)
		);
		assertThat(result, hasSpecificEntry(42, hasProperty("t1", is(20f))));

	}

	@Test
	void testDefaultValue() throws Exception {
		var parser = new SiteCurveAgeMaximumParser();
		var is = TestUtils.makeInputStream(" 16  150.0  150.0   20.0   60.0");

		Map<String, Object> controlMap = new HashMap<>();

		var map = parser.parse(is, controlMap);

		var result = map.get(27);

		assertThat(
				result,
				allOf(
						hasAge(Region.COASTAL, is(140.0f)), hasAge(Region.INTERIOR, is(140.0f)),
						hasProperty("t1", is(0.0f)), hasProperty("t2", is(0.0f))
				)
		);
	}

	public static Matcher<SiteCurveAgeMaximum> hasAge(Region region, Matcher<Float> delegate) {
		return new BaseMatcher<SiteCurveAgeMaximum>() {

			@Override
			public boolean matches(Object actual) {
				var max = (SiteCurveAgeMaximum) actual;
				return delegate.matches(max.getAgeMaximum(region));
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("has age for ").appendValue(region).appendText(" that ");
				delegate.describeTo(description);
			}

		};
	}
}
