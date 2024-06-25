package ca.bc.gov.nrs.vdyp.io.parse;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

import java.util.HashMap;
import java.util.Map;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import ca.bc.gov.nrs.vdyp.common.ControlKey;
import ca.bc.gov.nrs.vdyp.io.parse.coe.BasalAreaYieldParser;
import ca.bc.gov.nrs.vdyp.model.Coefficients;
import ca.bc.gov.nrs.vdyp.model.MatrixMap2;
import ca.bc.gov.nrs.vdyp.test.TestUtils;

class BasalAreaYieldParserTest {

	@Test
	void testParseSimple() throws Exception {

		var parser = new BasalAreaYieldParser();

		Map<String, Object> controlMap = new HashMap<>();

		TestUtils.populateControlMapGenusReal(controlMap);
		TestUtils.populateControlMapBecReal(controlMap);

		TestUtils.populateControlMapFromResource(controlMap, parser, "YLDBA407.COE");

		@SuppressWarnings("unchecked")
		MatrixMap2<String, String, Coefficients> m = (MatrixMap2<String, String, Coefficients>) controlMap
				.get(ControlKey.BA_YIELD.name());

		assertThat(m.get("AT", "AC"), hasSize(7));
		assertThat(
				m.get("AT", "AC"), Matchers.contains(-4.8137f, 3.2029f, 7.2295f, 0.5142f, -0.0026f, -0.0054f, -0.0090f)
		);
		assertThat(
				m.get("AT", "AT"),
				Matchers.contains(
						-4.8137f - 0.8603f, 3.2029f - 0.2732f, 7.2295f, 0.5142f + 0.1973f, -0.0026f - 0.0272f,
						-0.0054f - 0.0007f, -0.0090f
				)
		);
	}

	@Test
	void testParseContentWithBlankLines() throws Exception {

		var parser = new BasalAreaYieldParser();

		Map<String, Object> controlMap = new HashMap<>();

		TestUtils.populateControlMapGenusReal(controlMap);
		TestUtils.populateControlMapBecReal(controlMap);

		var is = TestUtils.makeInputStream(
				"AT   A0 1 -4.8137 -0.8603 11.2376  7.6975 -1.5588-16.3758 12.6180 11.6395 -2.8281 -1.5588  0.3191  0.3191 -0.9866 -4.2533 10.7814  5.2548",
				"     B2 0  8.1110  0.0000  0.0000  0.0000  0.0000  0.0000  0.0000  0.0000  0.0000  0.0000  0.0000  0.0000  0.0000  0.0000  0.0000  0.0000",
				" ",
				"AT   A1 1  3.2029 -0.2732 -3.2149 -0.8761  0.1580  3.5650 -3.2740 -1.1113  0.0753  0.1580 -0.1094 -0.1094 -0.1094 -0.1094 -3.4182 -0.8761"
		);

		MatrixMap2<String, String, Coefficients> m = parser.parse(is, controlMap);

		assertThat(m.get("AT", "AC"), Matchers.contains(-4.8137f, 3.2029f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f));
		assertThat(
				m.get("AT", "AT"),
				Matchers.contains(-4.8137f - 0.8603f, 3.2029f - 0.2732f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f)
		);
	}
}
