package ca.bc.gov.nrs.vdyp.io.parse;

import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.causedBy;
import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.mmHasEntry;
import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.notPresent;
import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.present;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import ca.bc.gov.nrs.vdyp.test.TestUtils;

public class UtilComponentBaseAreaParserTest {

	@Test
	public void testParseSingleBec() throws Exception {

		var parser = new UtilComponentBaseAreaParser();

		var is = TestUtils.makeStream("BA12 S1 B1   -23.22790  12.60472");

		Map<String, Object> controlMap = new HashMap<>();

		GenusDefinitionParserTest.populateControlMap(controlMap);
		BecDefinitionParserTest.populateControlMap(controlMap, "B1", "B2", "B3", "B4");

		var result = parser.parse(is, controlMap);

		assertThat(result, mmHasEntry(present(contains(-23.22790f, 12.60472f)), 2, "S1", "B1"));
		assertThat(result, mmHasEntry(notPresent(), 2, "S1", "B2"));
		assertThat(result, mmHasEntry(notPresent(), 2, "S1", "B3"));
		assertThat(result, mmHasEntry(notPresent(), 2, "S1", "B4"));

		assertThat(result, mmHasEntry(notPresent(), 2, "S2", "B1"));
		assertThat(result, mmHasEntry(notPresent(), 2, "S2", "B2"));
		assertThat(result, mmHasEntry(notPresent(), 2, "S2", "B3"));
		assertThat(result, mmHasEntry(notPresent(), 2, "S2", "B4"));

		assertThat(result, mmHasEntry(notPresent(), 3, "S1", "B1"));
	}

	@Test
	public void testParseSingleRegion() throws Exception {

		var parser = new UtilComponentBaseAreaParser();

		var is = TestUtils.makeStream("BA12 S1 C    -23.22790  12.60472");

		Map<String, Object> controlMap = new HashMap<>();

		GenusDefinitionParserTest.populateControlMap(controlMap);
		BecDefinitionParserTest.populateControlMap(controlMap, "B1", "B2", "B3", "B4");

		var result = parser.parse(is, controlMap);

		assertThat(result, mmHasEntry(present(contains(-23.22790f, 12.60472f)), 2, "S1", "B1"));
		assertThat(result, mmHasEntry(present(contains(-23.22790f, 12.60472f)), 2, "S1", "B3"));
		assertThat(result, mmHasEntry(notPresent(), 2, "S1", "B2"));
		assertThat(result, mmHasEntry(notPresent(), 2, "S1", "B4"));

		assertThat(result, mmHasEntry(notPresent(), 2, "S2", "B1"));
		assertThat(result, mmHasEntry(notPresent(), 2, "S2", "B2"));
		assertThat(result, mmHasEntry(notPresent(), 2, "S2", "B3"));
		assertThat(result, mmHasEntry(notPresent(), 2, "S2", "B4"));

		assertThat(result, mmHasEntry(notPresent(), 3, "S1", "B1"));
	}

	@Test
	public void testParseAllBecs() throws Exception {

		var parser = new UtilComponentBaseAreaParser();

		var is = TestUtils.makeStream("BA12 S1      -23.22790  12.60472");

		Map<String, Object> controlMap = new HashMap<>();

		GenusDefinitionParserTest.populateControlMap(controlMap);
		BecDefinitionParserTest.populateControlMap(controlMap, "B1", "B2", "B3", "B4");

		var result = parser.parse(is, controlMap);

		assertThat(result, mmHasEntry(present(contains(-23.22790f, 12.60472f)), 2, "S1", "B1"));
		assertThat(result, mmHasEntry(present(contains(-23.22790f, 12.60472f)), 2, "S1", "B2"));
		assertThat(result, mmHasEntry(present(contains(-23.22790f, 12.60472f)), 2, "S1", "B3"));
		assertThat(result, mmHasEntry(present(contains(-23.22790f, 12.60472f)), 2, "S1", "B4"));

		assertThat(result, mmHasEntry(notPresent(), 2, "S2", "B1"));
		assertThat(result, mmHasEntry(notPresent(), 2, "S2", "B2"));
		assertThat(result, mmHasEntry(notPresent(), 2, "S2", "B3"));
		assertThat(result, mmHasEntry(notPresent(), 2, "S2", "B4"));

		assertThat(result, mmHasEntry(notPresent(), 3, "S1", "B1"));

	}

	@Test
	public void testParseBadSpecies() throws Exception {

		var parser = new UtilComponentBaseAreaParser();

		var is = TestUtils.makeStream("BA12 SX B1   -23.22790  12.60472");

		Map<String, Object> controlMap = new HashMap<>();

		GenusDefinitionParserTest.populateControlMap(controlMap);
		BecDefinitionParserTest.populateControlMap(controlMap);

		var ex = assertThrows(ResourceParseLineException.class, () -> parser.parse(is, controlMap));
		assertThat(ex, causedBy(hasProperty("value", is("SX"))));
	}

	@Test
	public void testParseBadBec() throws Exception {

		var parser = new UtilComponentBaseAreaParser();

		var is = TestUtils.makeStream("BA12 S1 BX   -23.22790  12.60472");

		Map<String, Object> controlMap = new HashMap<>();

		GenusDefinitionParserTest.populateControlMap(controlMap);
		BecDefinitionParserTest.populateControlMap(controlMap);

		var ex = assertThrows(ResourceParseLineException.class, () -> parser.parse(is, controlMap));
		assertThat(ex, causedBy(hasProperty("value", is("BX"))));
	}

	@Test
	public void testParseBadBau() throws Exception {

		var parser = new UtilComponentBaseAreaParser();

		var is = TestUtils.makeStream("BAXX S1 B1   -23.22790  12.60472");

		Map<String, Object> controlMap = new HashMap<>();

		GenusDefinitionParserTest.populateControlMap(controlMap);
		BecDefinitionParserTest.populateControlMap(controlMap);

		var ex = assertThrows(ResourceParseLineException.class, () -> parser.parse(is, controlMap));
		assertThat(ex, causedBy(hasProperty("value", is("BAXX"))));
	}

	@Test
	public void testParseMissingCoefficient() throws Exception {

		var parser = new UtilComponentBaseAreaParser();

		var is = TestUtils.makeStream("BA12 S1 B1   -23.22790");

		Map<String, Object> controlMap = new HashMap<>();

		GenusDefinitionParserTest.populateControlMap(controlMap);
		BecDefinitionParserTest.populateControlMap(controlMap);

		var ex = assertThrows(ResourceParseLineException.class, () -> parser.parse(is, controlMap));
		assertThat(ex, causedBy(hasProperty("value", nullValue()))); // TODO Do this better
	}

}
