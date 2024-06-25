package ca.bc.gov.nrs.vdyp.io.parse;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import ca.bc.gov.nrs.vdyp.common.ControlKey;
import ca.bc.gov.nrs.vdyp.io.parse.coe.DqGrowthEmpiricalParser;
import ca.bc.gov.nrs.vdyp.model.Coefficients;
import ca.bc.gov.nrs.vdyp.test.TestUtils;

public class DqGrowthEmpiricalParserTest {

	@Test
	void testParseSimple() throws Exception {

		var parser = new DqGrowthEmpiricalParser();

		Map<String, Object> controlMap = new HashMap<>();

		TestUtils.populateControlMapFromResource(controlMap, parser, "GD23.coe");

		@SuppressWarnings("unchecked")
		Map<Integer, Coefficients> m = (Map<Integer, Coefficients>) controlMap
				.get(ControlKey.DQ_GROWTH_EMPIRICAL.name());

		assertThat(m.get(1), hasSize(10));
		assertThat(
				m.get(1),
				contains(-0.76723f, 0.00000f, -0.18524f, 0.02194f, -0.00752f, -0.00339f, 0.41806f, 0.0f, 0.0f, 0.0f)
		);
		assertThat(
				m.get(6),
				contains(0.36789f, 0.43192f, -0.73431f, 0.01266f, 0.02527f, 0.00007f, 0.00000f, 0.0f, 0.0f, 0.0f)
		);
		assertThat(
				m.get(30),
				contains(0.54463f, 0.20881f, -1.16629f, 0.06061f, 0.00743f, 0.01324f, 0.99554f, 0.0f, 0.0f, 0.0f)
		);

		// Verify that defaults are applied
		assertThat(m.get(29), contains(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f));
	}
}
