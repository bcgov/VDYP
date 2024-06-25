package ca.bc.gov.nrs.vdyp.io.parse;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import ca.bc.gov.nrs.vdyp.common.ControlKey;
import ca.bc.gov.nrs.vdyp.io.parse.coe.PrimarySpeciesDqGrowthParser;
import ca.bc.gov.nrs.vdyp.model.ModelCoefficients;
import ca.bc.gov.nrs.vdyp.test.TestUtils;

public class PrimarySpeciesDqGrowthParserTest {

	@Test
	void testParseSimple() throws Exception {

		var parser = new PrimarySpeciesDqGrowthParser();

		Map<String, Object> controlMap = new HashMap<>();

		TestUtils.populateControlMapFromResource(controlMap, parser, "DQSP05.COE");

		@SuppressWarnings("unchecked")
		Map<Integer, ModelCoefficients> m = (Map<Integer, ModelCoefficients>) controlMap
				.get(ControlKey.PRIMARY_SP_DQ_GROWTH.name());

		assertThat(m.get(1), hasProperty("model", is(9)));
		assertThat(m.get(1), hasProperty("coefficients", contains(0.008306f, -0.007918f, -0.000214f)));
		assertThat(m.get(30), hasProperty("model", is(9)));
		assertThat(m.get(30), hasProperty("coefficients", contains(0.004418f, 0.010218f, -0.000047f)));

		// Check that defaults are applied
		assertThat(m.get(27), hasProperty("model", is(0)));
		assertThat(m.get(27), hasProperty("coefficients", contains(0.0f, 0.0f, 0.0f)));
	}
}
