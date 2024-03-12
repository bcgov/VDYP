package ca.bc.gov.nrs.vdyp.io.parse;

import static org.hamcrest.MatcherAssert.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import ca.bc.gov.nrs.vdyp.common.ControlKey;
import ca.bc.gov.nrs.vdyp.io.parse.coe.DqGrowthFiatParser;
import ca.bc.gov.nrs.vdyp.model.GrowthFiatDetails;
import ca.bc.gov.nrs.vdyp.model.Region;
import ca.bc.gov.nrs.vdyp.test.TestUtils;

/**
 * See {@link BasalAreaGrowthFiatParserTest} for other tests, the two parsers being identical.
 *
 * @author Michael Junkin, Vivid Solutions
 */
public class DqGrowthFiatParserTest {

	@Test
	void testParseSimple() throws Exception {

		var parser = new DqGrowthFiatParser();

		Map<String, Object> controlMap = new HashMap<>();

		TestUtils.populateControlMapFromResource(controlMap, parser, "EMP117A1.prm");

		@SuppressWarnings("unchecked")
		Map<Region, GrowthFiatDetails> m = (Map<Region, GrowthFiatDetails>) controlMap
				.get(ControlKey.DQ_GROWTH_FIAT.name());

		assertThat(m, Matchers.aMapWithSize(2));
		assertThat(
				m.get(Region.COASTAL),
				Matchers.allOf(
						Matchers.hasProperty("ages", Matchers.arrayContaining(1.0f, 0.02f, 100.0f, 0.01f)),
						Matchers.hasProperty("coefficients", Matchers.arrayContaining(200f, 0.0f, 0.0f, 0.0f)),
						Matchers.hasProperty("mixedCoefficients", Matchers.arrayContaining(100.0f, 150.0f, 1.0f))
				)
		);
		assertThat(
				m.get(Region.INTERIOR),
				Matchers.allOf(
						Matchers.hasProperty("ages", Matchers.arrayContaining(1.0f, 0.02f, 100.0f, 0.01f)),
						Matchers.hasProperty("coefficients", Matchers.arrayContaining(200f, 0.0f, 0.0f, 0.0f)),
						Matchers.hasProperty("mixedCoefficients", Matchers.arrayContaining(100.0f, 150.0f, 1.0f))
				)
		);
	}
}
