package ca.bc.gov.nrs.vdyp.io.parse;

import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.mmHasEntry;
import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.present;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;
import java.util.Map;

import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;

import ca.bc.gov.nrs.vdyp.model.NonprimaryHLCoefficients;
import ca.bc.gov.nrs.vdyp.model.Region;
import ca.bc.gov.nrs.vdyp.test.TestUtils;

public class HLNonprimaryCoefficientParserTest {

	@Test
	public void testParseSimple() throws Exception {

		var parser = new HLNonprimaryCoefficientParser();

		var is = TestUtils.makeStream("S1 S2 C 1   0.86323   1.00505");

		Map<String, Object> controlMap = new HashMap<>();

		GenusDefinitionParserTest.populateControlMap(controlMap);

		var result = parser.parse(is, controlMap);

		assertThat(result, mmHasEntry(present(coe(0.86323f, 1.00505f, 1)), "S1", "S2", Region.COASTAL));
	}

	@Test
	public void testParseBadSpecies1() throws Exception {

		var parser = new HLNonprimaryCoefficientParser();

		var is = TestUtils.makeStream("SX S2 C 1   0.86323   1.00505");

		Map<String, Object> controlMap = new HashMap<>();

		GenusDefinitionParserTest.populateControlMap(controlMap);

		assertThrows(ResourceParseLineException.class, () -> parser.parse(is, controlMap));

	}

	@Test
	public void testParseBadSpecies2() throws Exception {

		var parser = new HLNonprimaryCoefficientParser();

		var is = TestUtils.makeStream("S1 SX C 1   0.86323   1.00505");

		Map<String, Object> controlMap = new HashMap<>();

		GenusDefinitionParserTest.populateControlMap(controlMap);

		assertThrows(ResourceParseLineException.class, () -> parser.parse(is, controlMap));

	}

	@Test
	public void testParseBadRegion() throws Exception {

		var parser = new HLNonprimaryCoefficientParser();

		var is = TestUtils.makeStream("S1 S2 X 1   0.86323   1.00505");

		Map<String, Object> controlMap = new HashMap<>();

		GenusDefinitionParserTest.populateControlMap(controlMap);

		assertThrows(ResourceParseLineException.class, () -> parser.parse(is, controlMap));

	}

	@Test
	public void testParseMissingCoefficient() throws Exception {

		var parser = new HLNonprimaryCoefficientParser();

		var is = TestUtils.makeStream("S1 S2 C 1   0.86323");

		Map<String, Object> controlMap = new HashMap<>();

		GenusDefinitionParserTest.populateControlMap(controlMap);

		assertThrows(ResourceParseLineException.class, () -> parser.parse(is, controlMap));
	}

	public static Matcher<NonprimaryHLCoefficients> coe(float c1, float c2, int ieqn) {
		return allOf(hasProperty("equationIndex", is(ieqn)), contains(c1, c2));
	}
}
