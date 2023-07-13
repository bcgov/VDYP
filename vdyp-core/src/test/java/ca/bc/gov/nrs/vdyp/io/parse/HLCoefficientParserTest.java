package ca.bc.gov.nrs.vdyp.io.parse;

import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.mmHasEntry;
import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.present;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import ca.bc.gov.nrs.vdyp.model.Region;
import ca.bc.gov.nrs.vdyp.test.TestUtils;

public class HLCoefficientParserTest {

	@Test
	public void testParseSimpleP1() throws Exception {

		var parser = new HLCoefficientParser(HLCoefficientParser.NUM_COEFFICIENTS_P1, "TEST");

		var is = TestUtils.makeStream("S1 I   1.00160   0.20508-0.0013743");

		Map<String, Object> controlMap = new HashMap<>();

		GenusDefinitionParserTest.populateControlMap(controlMap);

		var result = parser.parse(is, controlMap);

		assertThat(result, mmHasEntry(present(contains(1.00160f, 0.20508f, -0.0013743f)), "S1", Region.INTERIOR));
	}

	@Test
	public void testParseSimpleP2() throws Exception {

		var parser = new HLCoefficientParser(HLCoefficientParser.NUM_COEFFICIENTS_P2, "TEST");

		var is = TestUtils.makeStream("S1 C   0.49722   1.18403");

		Map<String, Object> controlMap = new HashMap<>();

		GenusDefinitionParserTest.populateControlMap(controlMap);

		var result = parser.parse(is, controlMap);

		assertThat(result, mmHasEntry(present(contains(0.49722f, 1.18403f)), "S1", Region.COASTAL));
	}

	@Test
	public void testParseSimpleP3() throws Exception {

		var parser = new HLCoefficientParser(HLCoefficientParser.NUM_COEFFICIENTS_P3, "TEST");

		var is = TestUtils.makeStream("S1 I   1.04422   0.93010  -0.05745  -2.50000");

		Map<String, Object> controlMap = new HashMap<>();

		GenusDefinitionParserTest.populateControlMap(controlMap);

		var result = parser.parse(is, controlMap);

		assertThat(
				result, mmHasEntry(present(contains(1.04422f, 0.93010f, -0.05745f, -2.50000f)), "S1", Region.INTERIOR)
		);
	}

	@Test
	public void testParseBadSpecies() throws Exception {

		var parser = new HLCoefficientParser(HLCoefficientParser.NUM_COEFFICIENTS_P1, "TEST");

		var is = TestUtils.makeStream("SX I   1.00160   0.20508-0.0013743");

		Map<String, Object> controlMap = new HashMap<>();

		GenusDefinitionParserTest.populateControlMap(controlMap);

		assertThrows(ResourceParseLineException.class, () -> parser.parse(is, controlMap));

	}

	@Test
	public void testParseBadRegion() throws Exception {

		var parser = new HLCoefficientParser(HLCoefficientParser.NUM_COEFFICIENTS_P1, "TEST");

		var is = TestUtils.makeStream("S1 X   1.00160   0.20508-0.0013743");

		Map<String, Object> controlMap = new HashMap<>();

		GenusDefinitionParserTest.populateControlMap(controlMap);

		assertThrows(ResourceParseLineException.class, () -> parser.parse(is, controlMap));

	}

}
