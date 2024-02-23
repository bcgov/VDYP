package ca.bc.gov.nrs.vdyp.io.parse;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import ca.bc.gov.nrs.vdyp.common.ControlKey;
import ca.bc.gov.nrs.vdyp.io.parse.coe.DqGrowthEmpiricalLimitsParser;
import ca.bc.gov.nrs.vdyp.model.Coefficients;
import ca.bc.gov.nrs.vdyp.test.TestUtils;

public class DqGrowthEmpiricalLimitsParserTest {

	@Test
	void testParseSimple() throws Exception {

		var parser = new DqGrowthEmpiricalLimitsParser();

		Map<String, Object> controlMap = new HashMap<>();

		TestUtils.populateControlMapFromResource(controlMap, parser, "REGDQL2.coe");

		@SuppressWarnings("unchecked")
		Map<Integer, Coefficients> m = (Map<Integer, Coefficients>) controlMap
				.get(ControlKey.DQ_GROWTH_EMPIRICAL_LIMITS.name());

		assertThat(m.get(1), hasSize(8));
		assertThat(m.get(1), contains(0.30997f, -0.00717f, -0.01070f, 0.38206f, 0.01038f, 0.00000f, 0.08f, 0.70f));
		assertThat(m.get(30), contains(0.08197f, -0.00249f, 0.00000f, 0.15000f, 0.00949f, 0.00000f, -0.05f, 0.38f));
	}
}
