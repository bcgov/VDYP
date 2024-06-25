package ca.bc.gov.nrs.vdyp.io.parse.coe;

import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.coe;
import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.mmHasEntry;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import ca.bc.gov.nrs.vdyp.common.ControlKey;
import ca.bc.gov.nrs.vdyp.io.parse.coe.base.CoefficientParser;
import ca.bc.gov.nrs.vdyp.io.parse.common.ResourceParseLineException;
import ca.bc.gov.nrs.vdyp.test.TestUtils;

class CoefficientParserTest {

	private final ControlKey TEST_KEY = ControlKey.MAX_NUM_POLY;

	@Test
	void testParseSimple() throws Exception {

		var parser = new CoefficientParser(TEST_KEY) {
		};

		var is = TestUtils.makeInputStream(
				"B1   A0 2  2.0028 -0.5343  1.3949 -0.3683 -0.3343  0.5699  0.2314  0.0528  0.2366 -0.3343  0.5076  0.5076  0.6680 -0.1353  1.2445 -0.4507"
		);

		Map<String, Object> controlMap = new HashMap<>();

		TestUtils.populateControlMapBec(controlMap);
		TestUtils.populateControlMapGenus(controlMap);

		var result = parser.parse(is, controlMap);

		assertThat(result, mmHasEntry(coe(0, contains(2.0028f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)), "B1", "S1"));
		assertThat(result, mmHasEntry(coe(0, contains(-0.5343f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)), "B1", "S2"));

	}

	@Test
	void testBadBec() throws Exception {

		var parser = new CoefficientParser(TEST_KEY) {
		};

		var is = TestUtils.makeInputStream(
				"BX   A0 0  2.0028 -0.5343  1.3949 -0.3683 -0.3343  0.5699  0.2314  0.0528  0.2366 -0.3343  0.5076  0.5076  0.6680 -0.1353  1.2445 -0.4507"
		);

		Map<String, Object> controlMap = new HashMap<>();

		TestUtils.populateControlMapBec(controlMap);
		TestUtils.populateControlMapGenus(controlMap);

		assertThrows(ResourceParseLineException.class, () -> parser.parse(is, controlMap));

	}

	@Test
	void testBadIndex() throws Exception {

		var parser = new CoefficientParser(TEST_KEY) {
		};

		var is = TestUtils.makeInputStream(
				"B1   AX 0  2.0028 -0.5343  1.3949 -0.3683 -0.3343  0.5699  0.2314  0.0528  0.2366 -0.3343  0.5076  0.5076  0.6680 -0.1353  1.2445 -0.4507"
		);

		Map<String, Object> controlMap = new HashMap<>();

		TestUtils.populateControlMapBec(controlMap);
		TestUtils.populateControlMapGenus(controlMap);

		assertThrows(ResourceParseLineException.class, () -> parser.parse(is, controlMap));

	}

	@Test
	void testParseDelta() throws Exception {

		var parser = new CoefficientParser(TEST_KEY) {
		};

		var is = TestUtils.makeInputStream(
				"B1   A0 1  2.0028 -0.5343  1.3949 -0.3683 -0.3343  0.5699  0.2314  0.0528  0.2366 -0.3343  0.5076  0.5076  0.6680 -0.1353  1.2445 -0.4507"
		);

		Map<String, Object> controlMap = new HashMap<>();

		TestUtils.populateControlMapBec(controlMap);
		TestUtils.populateControlMapGenus(controlMap);

		var result = parser.parse(is, controlMap);

		assertThat(result, mmHasEntry(coe(0, contains(2.0028f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)), "B1", "S1"));
		assertThat(
				result, mmHasEntry(coe(0, contains(2.0028f - 0.5343f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)), "B1", "S2")
		);
	}

	@Test
	void testParseFixed() throws Exception {

		var parser = new CoefficientParser(TEST_KEY) {
		};

		var is = TestUtils.makeInputStream(
				"B1   A0 0  2.0028 -0.5343  1.3949 -0.3683 -0.3343  0.5699  0.2314  0.0528  0.2366 -0.3343  0.5076  0.5076  0.6680 -0.1353  1.2445 -0.4507"
		);

		Map<String, Object> controlMap = new HashMap<>();

		TestUtils.populateControlMapBec(controlMap);
		TestUtils.populateControlMapGenus(controlMap);

		var result = parser.parse(is, controlMap);

		assertThat(result, mmHasEntry(coe(0, contains(2.0028f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)), "B1", "S1"));
		assertThat(result, mmHasEntry(coe(0, contains(2.0028f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)), "B1", "S2"));

	}

}
