package ca.bc.gov.nrs.vdyp.io.parse.coe;

import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.coe;
import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.mmHasEntry;
import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.present;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import ca.bc.gov.nrs.vdyp.io.parse.common.ResourceParseLineException;
import ca.bc.gov.nrs.vdyp.model.Region;
import ca.bc.gov.nrs.vdyp.test.TestUtils;

class HLNonprimaryCoefficientParserTest {

	@Test
	void testParseSimple() throws Exception {

		var parser = new HLNonprimaryCoefficientParser();

		var is = TestUtils.makeInputStream("S1 S2 C 1   0.86323   1.00505");

		Map<String, Object> controlMap = new HashMap<>();

		TestUtils.populateControlMapGenus(controlMap);

		var result = parser.parse(is, controlMap);

		assertThat(result, mmHasEntry(present(coe(1, 0.86323f, 1.00505f)), "S1", "S2", Region.COASTAL));
	}

	@Test
	void testParseBadSpecies1() throws Exception {

		var parser = new HLNonprimaryCoefficientParser();

		var is = TestUtils.makeInputStream("SX S2 C 1   0.86323   1.00505");

		Map<String, Object> controlMap = new HashMap<>();

		TestUtils.populateControlMapGenus(controlMap);

		assertThrows(ResourceParseLineException.class, () -> parser.parse(is, controlMap));

	}

	@Test
	void testParseBadSpecies2() throws Exception {

		var parser = new HLNonprimaryCoefficientParser();

		var is = TestUtils.makeInputStream("S1 SX C 1   0.86323   1.00505");

		Map<String, Object> controlMap = new HashMap<>();

		TestUtils.populateControlMapGenus(controlMap);

		assertThrows(ResourceParseLineException.class, () -> parser.parse(is, controlMap));

	}

	@Test
	void testParseBadRegion() throws Exception {

		var parser = new HLNonprimaryCoefficientParser();

		var is = TestUtils.makeInputStream("S1 S2 X 1   0.86323   1.00505");

		Map<String, Object> controlMap = new HashMap<>();

		TestUtils.populateControlMapGenus(controlMap);

		assertThrows(ResourceParseLineException.class, () -> parser.parse(is, controlMap));

	}

	@Test
	void testParseMissingCoefficient() throws Exception {

		var parser = new HLNonprimaryCoefficientParser();

		var is = TestUtils.makeInputStream("S1 S2 C 1   0.86323");

		Map<String, Object> controlMap = new HashMap<>();

		TestUtils.populateControlMapGenus(controlMap);

		assertThrows(ResourceParseLineException.class, () -> parser.parse(is, controlMap));
	}

	@Test
	void testParseMultiple() throws Exception {

		var parser = new HLNonprimaryCoefficientParser();

		var is = TestUtils.makeInputStream("AC AT C 1   0.86323   1.00505", "AC  B C 1   4.44444   5.55555");

		Map<String, Object> controlMap = new HashMap<>();

		TestUtils.populateControlMapGenusReal(controlMap);

		var result = parser.parse(is, controlMap);

		assertThat(result, mmHasEntry(present(coe(1, 0.86323f, 1.00505f)), "AC", "AT", Region.COASTAL));
		assertThat(result, mmHasEntry(present(coe(1, 4.44444f, 5.55555f)), "AC", "B", Region.COASTAL));
	}

	@Test
	void testParseBlank() throws Exception {

		var parser = new HLNonprimaryCoefficientParser();

		var is = TestUtils.makeInputStream(
				"AC AT C 1   0.86323   1.00505", "      C 1   6.66666   7.77777", "AC  B C 1   4.44444   5.55555"
		);

		Map<String, Object> controlMap = new HashMap<>();

		TestUtils.populateControlMapGenusReal(controlMap);

		var result = parser.parse(is, controlMap);

		assertThat(result, mmHasEntry(present(coe(1, 0.86323f, 1.00505f)), "AC", "AT", Region.COASTAL));
		assertThat(result, mmHasEntry(present(coe(1, 4.44444f, 5.55555f)), "AC", "B", Region.COASTAL));
	}

	@Test
	void testParseEmpty() throws Exception {

		var parser = new HLNonprimaryCoefficientParser();

		var is = TestUtils.makeInputStream("AC AT C 1   0.86323   1.00505", "", "AC  B C 1   4.44444   5.55555");

		Map<String, Object> controlMap = new HashMap<>();

		TestUtils.populateControlMapGenusReal(controlMap);

		var result = parser.parse(is, controlMap);

		assertThat(result, mmHasEntry(present(coe(1, 0.86323f, 1.00505f)), "AC", "AT", Region.COASTAL));
		assertThat(result, mmHasEntry(present(coe(1, 4.44444f, 5.55555f)), "AC", "B", Region.COASTAL));
	}

}
