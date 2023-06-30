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

import ca.bc.gov.nrs.vdyp.model.Region;
import ca.bc.gov.nrs.vdyp.test.TestUtils;

public class VeteranDQParserTest {

	@Test
	public void testParseSingleRegion() throws Exception {

		var parser = new VeteranDQParser();

		var is = TestUtils.makeStream("S1 C   22.500  0.24855  1.46089");

		Map<String, Object> controlMap = new HashMap<>();

		SP0DefinitionParserTest.populateControlMap(controlMap);

		var result = parser.parse(is, controlMap);

		assertThat(result, mmHasEntry(present(contains(22.500f, 0.24855f, 1.46089f)), "S1", Region.COASTAL));
		assertThat(result, mmHasEntry(notPresent(), "S1", Region.INTERIOR));
		assertThat(result, mmHasEntry(notPresent(), "S2", Region.COASTAL));
	}

	@Test
	public void testParseAllRegions() throws Exception {

		var parser = new VeteranDQParser();

		var is = TestUtils.makeStream("S1     22.500  0.24855  1.46089");

		Map<String, Object> controlMap = new HashMap<>();

		SP0DefinitionParserTest.populateControlMap(controlMap);

		var result = parser.parse(is, controlMap);

		assertThat(result, mmHasEntry(present(contains(22.500f, 0.24855f, 1.46089f)), "S1", Region.COASTAL));
		assertThat(result, mmHasEntry(present(contains(22.500f, 0.24855f, 1.46089f)), "S1", Region.INTERIOR));
		assertThat(result, mmHasEntry(notPresent(), "S2", Region.COASTAL));
	}

	@Test
	public void testParseBadSpecies() throws Exception {

		var parser = new VeteranDQParser();

		var is = TestUtils.makeStream("SX C   22.500  0.24855  1.46089");

		Map<String, Object> controlMap = new HashMap<>();

		SP0DefinitionParserTest.populateControlMap(controlMap);
		BecDefinitionParserTest.populateControlMap(controlMap);

		var ex = assertThrows(ResourceParseLineException.class, () -> parser.parse(is, controlMap));
		assertThat(ex, causedBy(hasProperty("value", is("SX"))));
	}

	@Test
	public void testParseMissingCoefficient() throws Exception {

		var parser = new VeteranDQParser();

		var is = TestUtils.makeStream("S1 C   22.500  0.24855 ");

		Map<String, Object> controlMap = new HashMap<>();

		SP0DefinitionParserTest.populateControlMap(controlMap);
		BecDefinitionParserTest.populateControlMap(controlMap);

		var ex = assertThrows(ResourceParseLineException.class, () -> parser.parse(is, controlMap));
		assertThat(ex, causedBy(hasProperty("value", nullValue()))); // TODO Do this better
	}

}
