package ca.bc.gov.nrs.vdyp.io.parse;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import ca.bc.gov.nrs.vdyp.common.ControlKey;
import ca.bc.gov.nrs.vdyp.io.parse.coe.BasalAreaGrowthEmpiricalParser;
import ca.bc.gov.nrs.vdyp.model.Coefficients;
import ca.bc.gov.nrs.vdyp.model.MatrixMap2;
import ca.bc.gov.nrs.vdyp.test.TestUtils;

class BasalAreaGrowthEmpiricalParserTest {

	@Test
	void testParseSimple() throws Exception {

		var parser = new BasalAreaGrowthEmpiricalParser();

		Map<String, Object> controlMap = new HashMap<>();

		TestUtils.populateControlMapGenusReal(controlMap);
		TestUtils.populateControlMapBecReal(controlMap);

		TestUtils.populateControlMapFromResource(controlMap, parser, "GROWBA27.COE");

		@SuppressWarnings("unchecked")
		MatrixMap2<String, String, Coefficients> m = (MatrixMap2<String, String, Coefficients>) controlMap
				.get(ControlKey.BA_GROWTH_EMPIRICAL.name());

		assertTrue(m.isFull());
		assertThat(m.get("AT", "AC"), hasSize(8));
		assertThat(
				m.get("AT", "AC"),
				Matchers.contains(7.9550f, -0.5818f, 0.00538f, 3.90488f, -1.0999f, 0.01348f, 0.82063f, 0.69837f)
		);
		assertThat(m.get("AT", "AT"), Matchers.contains(0.0f, 0.0f, 0.0f, 0.0f, 0.0766f, -0.01473f, 0.0f, 0.0f));
	}

	@Test
	void testParseContentWithBlankLines() throws Exception {

		var parser = new BasalAreaGrowthEmpiricalParser();

		Map<String, Object> controlMap = new HashMap<>();

		TestUtils.populateControlMapGenusReal(controlMap);
		TestUtils.populateControlMapBecReal(controlMap);

		var is = TestUtils.makeInputStream(
				"AT   B0 0  7.9550  0.0000  0.0000  0.0000  0.0000  0.0000  0.0000  0.0000  0.0000  0.0000  0.0000  0.0000  0.0000  0.0000  0.0000  0.0000",
				"     B2 0  8.1110  0.0000  0.0000  0.0000  0.0000  0.0000  0.0000  0.0000  0.0000  0.0000  0.0000  0.0000  0.0000  0.0000  0.0000  0.0000",
				" ",
				"AT   B1 0 -0.5818  0.0000  0.0000  0.0000  0.0000  0.0000  0.0000  0.0000  0.0000  0.0000  0.0000  0.0000  0.0000  0.0000  0.0000  0.0000"
		);

		MatrixMap2<String, String, Coefficients> m = parser.parse(is, controlMap);

		assertThat(m.get("AT", "AC"), Matchers.contains(7.9550f, -0.5818f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f));
		assertThat(m.get("AT", "AT"), Matchers.contains(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f));
	}
}
