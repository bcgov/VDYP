package ca.bc.gov.nrs.vdyp.io.parse.coe;

import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.causedBy;
import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.coe;
import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.mmHasEntry;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import ca.bc.gov.nrs.vdyp.io.parse.common.ResourceParseLineException;
import ca.bc.gov.nrs.vdyp.test.TestUtils;

class UtilComponentBaseAreaParserTest {

	@Test
	void testParseSingleBec() throws Exception {

		var parser = new UtilComponentBaseAreaParser();

		var is = TestUtils.makeInputStream("BA12 S1 B1   -23.22790  12.60472");

		Map<String, Object> controlMap = new HashMap<>();

		TestUtils.populateControlMapGenus(controlMap);
		TestUtils.populateControlMapBec(controlMap, "B1", "B2", "B3", "B4");

		var result = parser.parse(is, controlMap);

		assertThat(result, mmHasEntry(coe(1, contains(-23.22790f, 12.60472f)), 1, "S1", "B1"));
		assertThat(result, mmHasEntry(coe(1, contains(0f, 0f)), 1, "S1", "B2"));
		assertThat(result, mmHasEntry(coe(1, contains(0f, 0f)), 1, "S1", "B3"));
		assertThat(result, mmHasEntry(coe(1, contains(0f, 0f)), 1, "S1", "B4"));

		assertThat(result, mmHasEntry(coe(1, contains(0f, 0f)), 1, "S2", "B1"));
		assertThat(result, mmHasEntry(coe(1, contains(0f, 0f)), 1, "S2", "B2"));
		assertThat(result, mmHasEntry(coe(1, contains(0f, 0f)), 1, "S2", "B3"));
		assertThat(result, mmHasEntry(coe(1, contains(0f, 0f)), 1, "S2", "B4"));

		assertThat(result, mmHasEntry(coe(1, contains(0f, 0f)), 2, "S1", "B1"));
	}

	@Test
	void testParseSingleRegion() throws Exception {

		var parser = new UtilComponentBaseAreaParser();

		var is = TestUtils.makeInputStream("BA12 S1 C    -23.22790  12.60472");

		Map<String, Object> controlMap = new HashMap<>();

		TestUtils.populateControlMapGenus(controlMap);
		TestUtils.populateControlMapBec(controlMap, "B1", "B2", "B3", "B4");

		var result = parser.parse(is, controlMap);

		assertThat(result, mmHasEntry(coe(1, contains(-23.22790f, 12.60472f)), 1, "S1", "B1"));
		assertThat(result, mmHasEntry(coe(1, contains(-23.22790f, 12.60472f)), 1, "S1", "B3"));
		assertThat(result, mmHasEntry(coe(1, contains(0f, 0f)), 1, "S1", "B2"));
		assertThat(result, mmHasEntry(coe(1, contains(0f, 0f)), 1, "S1", "B4"));

		assertThat(result, mmHasEntry(coe(1, contains(0f, 0f)), 1, "S2", "B1"));
		assertThat(result, mmHasEntry(coe(1, contains(0f, 0f)), 1, "S2", "B2"));
		assertThat(result, mmHasEntry(coe(1, contains(0f, 0f)), 1, "S2", "B3"));
		assertThat(result, mmHasEntry(coe(1, contains(0f, 0f)), 1, "S2", "B4"));

		assertThat(result, mmHasEntry(coe(1, contains(0f, 0f)), 2, "S1", "B1"));
	}

	@Test
	void testParseAllBecs() throws Exception {

		var parser = new UtilComponentBaseAreaParser();

		var is = TestUtils.makeInputStream("BA12 S1      -23.22790  12.60472");

		Map<String, Object> controlMap = new HashMap<>();

		TestUtils.populateControlMapGenus(controlMap);
		TestUtils.populateControlMapBec(controlMap, "B1", "B2", "B3", "B4");

		var result = parser.parse(is, controlMap);

		assertThat(result, mmHasEntry(coe(1, contains(-23.22790f, 12.60472f)), 1, "S1", "B1"));
		assertThat(result, mmHasEntry(coe(1, contains(-23.22790f, 12.60472f)), 1, "S1", "B2"));
		assertThat(result, mmHasEntry(coe(1, contains(-23.22790f, 12.60472f)), 1, "S1", "B3"));
		assertThat(result, mmHasEntry(coe(1, contains(-23.22790f, 12.60472f)), 1, "S1", "B4"));

		assertThat(result, mmHasEntry(coe(1, contains(0f, 0f)), 1, "S2", "B1"));
		assertThat(result, mmHasEntry(coe(1, contains(0f, 0f)), 1, "S2", "B2"));
		assertThat(result, mmHasEntry(coe(1, contains(0f, 0f)), 1, "S2", "B3"));
		assertThat(result, mmHasEntry(coe(1, contains(0f, 0f)), 1, "S2", "B4"));

		assertThat(result, mmHasEntry(coe(1, contains(0f, 0f)), 2, "S1", "B1"));

	}

	@Test
	void testParseBadSpecies() throws Exception {

		var parser = new UtilComponentBaseAreaParser();

		var is = TestUtils.makeInputStream("BA12 SX B1   -23.22790  12.60472");

		Map<String, Object> controlMap = new HashMap<>();

		TestUtils.populateControlMapGenus(controlMap);
		TestUtils.populateControlMapBec(controlMap);

		var ex = assertThrows(ResourceParseLineException.class, () -> parser.parse(is, controlMap));
		assertThat(ex, causedBy(hasProperty("value", is("SX"))));
	}

	@Test
	void testParseBadBec() throws Exception {

		var parser = new UtilComponentBaseAreaParser();

		var is = TestUtils.makeInputStream("BA12 S1 BX   -23.22790  12.60472");

		Map<String, Object> controlMap = new HashMap<>();

		TestUtils.populateControlMapGenus(controlMap);
		TestUtils.populateControlMapBec(controlMap);

		var ex = assertThrows(ResourceParseLineException.class, () -> parser.parse(is, controlMap));
		assertThat(ex, causedBy(hasProperty("value", is("BX"))));
	}

	@Test
	void testParseBadBau() throws Exception {

		var parser = new UtilComponentBaseAreaParser();

		var is = TestUtils.makeInputStream("BAXX S1 B1   -23.22790  12.60472");

		Map<String, Object> controlMap = new HashMap<>();

		TestUtils.populateControlMapGenus(controlMap);
		TestUtils.populateControlMapBec(controlMap);

		var ex = assertThrows(ResourceParseLineException.class, () -> parser.parse(is, controlMap));
		assertThat(ex, causedBy(hasProperty("value", is("BAXX"))));
	}

	@Test
	void testParseMissingCoefficient() throws Exception {

		var parser = new UtilComponentBaseAreaParser();

		var is = TestUtils.makeInputStream("BA12 S1 B1   -23.22790");

		Map<String, Object> controlMap = new HashMap<>();

		TestUtils.populateControlMapGenus(controlMap);
		TestUtils.populateControlMapBec(controlMap);

		var ex = assertThrows(ResourceParseLineException.class, () -> parser.parse(is, controlMap));
		assertThat(ex, causedBy(hasProperty("value", nullValue()))); // TODO Do this better
	}

	@Test
	void testParseTwoBecs() throws Exception {

		var parser = new UtilComponentBaseAreaParser();

		var is = TestUtils.makeInputStream("BA12 S1 B1   -23.22790  12.60472", "BA12 S1 B2   -42.22790  64.60472");

		Map<String, Object> controlMap = new HashMap<>();

		TestUtils.populateControlMapGenus(controlMap);
		TestUtils.populateControlMapBec(controlMap, "B1", "B2", "B3", "B4");

		var result = parser.parse(is, controlMap);

		assertThat(result, mmHasEntry(coe(1, contains(-23.22790f, 12.60472f)), 1, "S1", "B1"));
		assertThat(result, mmHasEntry(coe(1, contains(-42.22790f, 64.60472f)), 1, "S1", "B2"));
		assertThat(result, mmHasEntry(coe(1, contains(0f, 0f)), 1, "S1", "B3"));
		assertThat(result, mmHasEntry(coe(1, contains(0f, 0f)), 1, "S1", "B4"));

		assertThat(result, mmHasEntry(coe(1, contains(0f, 0f)), 1, "S2", "B1"));
		assertThat(result, mmHasEntry(coe(1, contains(0f, 0f)), 1, "S2", "B2"));
		assertThat(result, mmHasEntry(coe(1, contains(0f, 0f)), 1, "S2", "B3"));
		assertThat(result, mmHasEntry(coe(1, contains(0f, 0f)), 1, "S2", "B4"));

		assertThat(result, mmHasEntry(coe(1, contains(0f, 0f)), 2, "S1", "B1"));
	}

	@Test
	void testParseTwoBecsWithBlank() throws Exception {

		var parser = new UtilComponentBaseAreaParser();

		var is = TestUtils.makeInputStream(
				"BA12 S1 B1   -23.22790  12.60472", "     S1 B2   -77.22790  77.60472",
				"BA12 S1 B2   -42.22790  64.60472"
		);

		Map<String, Object> controlMap = new HashMap<>();

		TestUtils.populateControlMapGenus(controlMap);
		TestUtils.populateControlMapBec(controlMap, "B1", "B2", "B3", "B4");

		var result = parser.parse(is, controlMap);

		assertThat(result, mmHasEntry(coe(1, contains(-23.22790f, 12.60472f)), 1, "S1", "B1"));
		assertThat(result, mmHasEntry(coe(1, contains(-42.22790f, 64.60472f)), 1, "S1", "B2"));
		assertThat(result, mmHasEntry(coe(1, contains(0f, 0f)), 1, "S1", "B3"));
		assertThat(result, mmHasEntry(coe(1, contains(0f, 0f)), 1, "S1", "B4"));

		assertThat(result, mmHasEntry(coe(1, contains(0f, 0f)), 1, "S2", "B1"));
		assertThat(result, mmHasEntry(coe(1, contains(0f, 0f)), 1, "S2", "B2"));
		assertThat(result, mmHasEntry(coe(1, contains(0f, 0f)), 1, "S2", "B3"));
		assertThat(result, mmHasEntry(coe(1, contains(0f, 0f)), 1, "S2", "B4"));

		assertThat(result, mmHasEntry(coe(1, contains(0f, 0f)), 2, "S1", "B1"));
	}

	@Test
	void testParseTwoBecsWithEmptyLine() throws Exception {

		var parser = new UtilComponentBaseAreaParser();

		var is = TestUtils.makeInputStream("BA12 S1 B1   -23.22790  12.60472", "", "BA12 S1 B2   -42.22790  64.60472");

		Map<String, Object> controlMap = new HashMap<>();

		TestUtils.populateControlMapGenus(controlMap);
		TestUtils.populateControlMapBec(controlMap, "B1", "B2", "B3", "B4");

		var result = parser.parse(is, controlMap);

		assertThat(result, mmHasEntry(coe(1, contains(-23.22790f, 12.60472f)), 1, "S1", "B1"));
		assertThat(result, mmHasEntry(coe(1, contains(-42.22790f, 64.60472f)), 1, "S1", "B2"));
		assertThat(result, mmHasEntry(coe(1, contains(0f, 0f)), 1, "S1", "B3"));
		assertThat(result, mmHasEntry(coe(1, contains(0f, 0f)), 1, "S1", "B4"));

		assertThat(result, mmHasEntry(coe(1, contains(0f, 0f)), 1, "S2", "B1"));
		assertThat(result, mmHasEntry(coe(1, contains(0f, 0f)), 1, "S2", "B2"));
		assertThat(result, mmHasEntry(coe(1, contains(0f, 0f)), 1, "S2", "B3"));
		assertThat(result, mmHasEntry(coe(1, contains(0f, 0f)), 1, "S2", "B4"));

		assertThat(result, mmHasEntry(coe(1, contains(0f, 0f)), 2, "S1", "B1"));
	}

}
