package ca.bc.gov.nrs.vdyp.io.parse;

import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.coe;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import ca.bc.gov.nrs.vdyp.common.ControlKey;
import ca.bc.gov.nrs.vdyp.io.parse.coe.QuadraticMeanDiameterYieldParser;
import ca.bc.gov.nrs.vdyp.model.Coefficients;
import ca.bc.gov.nrs.vdyp.model.MatrixMap2;
import ca.bc.gov.nrs.vdyp.test.TestUtils;

class QuadraticMeanDiameterYieldParserTest {

	@Test
	void testParseSimple() throws Exception {

		var parser = new QuadraticMeanDiameterYieldParser();

		Map<String, Object> controlMap = new HashMap<>();

		TestUtils.populateControlMapGenusReal(controlMap);
		TestUtils.populateControlMapBecReal(controlMap);

		TestUtils.populateControlMapFromResource(controlMap, parser, "YLDDQ45.COE");

		@SuppressWarnings("unchecked")
		MatrixMap2<String, String, Coefficients> m = (MatrixMap2<String, String, Coefficients>) controlMap
				.get(ControlKey.DQ_YIELD.name());

		assertThat(m.get("AT", "AC"), hasSize(6));
		assertThat(m.get("AT", "AC"), coe(0, 7.5065f, 2.9903f, -0.4081f, -0.4935f, 0.3187f, -0.0028f));
		assertThat(m.get("AT", "AT"), coe(0, 7.5000f, 2.8345f, -0.4107f, -1.1172f, 0.4411f, -0.0028f));
		assertThat(m.get("IDF", "B"), coe(0, 10.1437f, -0.6724f, 0.2121f, 1.6877f, -0.0991f, -0.0028f));
	}
}
