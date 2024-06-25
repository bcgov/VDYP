package ca.bc.gov.nrs.vdyp.io.parse;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import ca.bc.gov.nrs.vdyp.common.ControlKey;
import ca.bc.gov.nrs.vdyp.io.parse.coe.PrimarySpeciesBasalAreaGrowthParser;
import ca.bc.gov.nrs.vdyp.model.ModelCoefficients;
import ca.bc.gov.nrs.vdyp.test.TestUtils;

public class PrimarySpeciesBasalAreaGrowthParserTest {

	@Test
	void testParseSimple() throws Exception {

		var parser = new PrimarySpeciesBasalAreaGrowthParser();

		Map<String, Object> controlMap = new HashMap<>();

		TestUtils.populateControlMapFromResource(controlMap, parser, "BASP05.COE");

		@SuppressWarnings("unchecked")
		Map<Integer, ModelCoefficients> m = (Map<Integer, ModelCoefficients>) controlMap
				.get(ControlKey.PRIMARY_SP_BA_GROWTH.name());

		assertThat(m.get(1), hasProperty("model", is(9)));
		assertThat(m.get(1), hasProperty("coefficients", contains(-0.08960f, 0.007892f, 0.00105f)));
		assertThat(m.get(30), hasProperty("model", is(8)));
		assertThat(m.get(30), hasProperty("coefficients", contains(0.00579f, -0.000076f, 0.00449f)));

		// Check that defaults are applied
		assertThat(m.get(27), hasProperty("model", is(0)));
		assertThat(m.get(27), hasProperty("coefficients", contains(0.0f, 0.0f, 0.0f)));
	}

	@Test
	void testBlankLinesStopParse() throws Exception {
		var parser = new PrimarySpeciesBasalAreaGrowthParser();

		Map<String, Object> controlMap = new HashMap<>();

		var is = TestUtils
				.makeInputStream(" 1  9  -0.08960  0.007892   0.00105", " ", " 2  3  -0.05273  0.001709   0.00000");

		Map<Integer, ModelCoefficients> m = parser.parse(is, controlMap);
		assertThat(m.get(1), hasProperty("model", is(9)));
		assertThat(m.get(1), hasProperty("coefficients", contains(-0.08960f, 0.007892f, 0.00105f)));
		assertThat(m.get(2), hasProperty("model", is(0)));
		assertThat(m.get(2), hasProperty("coefficients", contains(0.0f, 0.0f, 0.0f)));
	}
}
