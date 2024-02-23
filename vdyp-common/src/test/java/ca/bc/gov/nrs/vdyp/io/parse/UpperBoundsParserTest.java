package ca.bc.gov.nrs.vdyp.io.parse;

import static org.hamcrest.MatcherAssert.assertThat;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import ca.bc.gov.nrs.vdyp.common.ControlKey;
import ca.bc.gov.nrs.vdyp.io.parse.coe.UpperBoundsParser;
import ca.bc.gov.nrs.vdyp.model.Coefficients;
import ca.bc.gov.nrs.vdyp.test.TestUtils;

class UpperBoundsParserTest {

	@Test
	void testParseSimple() throws Exception {

		var parser = new UpperBoundsParser();
		Map<String, Object> controlMap = new HashMap<>();

		TestUtils.populateControlMapFromResource(controlMap, parser, "PCT_407.coe");

		@SuppressWarnings("unchecked")
		Map<Integer, Coefficients> m = (Map<Integer, Coefficients>) controlMap
				.get(ControlKey.BA_DQ_UPPER_BOUNDS.name());

		assertThat(m, Matchers.aMapWithSize(UpperBoundsParser.LAST_BA_GROUP_ID));
		assertThat(m.get(1), Matchers.contains(74.78f, 76.10f));
		assertThat(m.get(144), Matchers.contains(0.0f, 7.6f));
		assertThat(m.get(171), Matchers.contains(125.12f, 49.80f));

		// Test that defaults are applied
		assertThat(m.get(UpperBoundsParser.LAST_BA_GROUP_ID), Matchers.contains(0.0f, 7.6f));
	}

	@Test
	void testParseBlank() throws Exception {

		var parser = new UpperBoundsParser();
		Map<String, Object> controlMap = new HashMap<>();

		InputStream is = TestUtils.makeInputStream(
				"  1   74.78   76.10    AC", "", "0 ", " 00", " 0 ", "000", " ", "  ", "   ", "    xxx",
				" 11   50.77   34.70    AT"
		);
		TestUtils.populateControlMapFromStream(controlMap, parser, is);

		@SuppressWarnings("unchecked")
		Map<Integer, Coefficients> m = (Map<Integer, Coefficients>) controlMap
				.get(ControlKey.BA_DQ_UPPER_BOUNDS.name());

		assertThat(m, Matchers.aMapWithSize(UpperBoundsParser.LAST_BA_GROUP_ID));
		assertThat(m.get(1), Matchers.contains(74.78f, 76.10f));
		assertThat(m.get(11), Matchers.contains(50.77f, 34.70f));

		// Test that default are applied
		assertThat(m.get(180), Matchers.contains(0.0f, 7.6f));
	}
}
