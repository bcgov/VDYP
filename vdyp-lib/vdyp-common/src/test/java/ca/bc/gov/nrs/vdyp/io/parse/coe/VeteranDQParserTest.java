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
import ca.bc.gov.nrs.vdyp.model.Region;
import ca.bc.gov.nrs.vdyp.test.TestUtils;

class VeteranDQParserTest {

	@Test
	void testParseSingleRegion() throws Exception {

		var parser = new VeteranDQParser();

		var is = TestUtils.makeInputStream("S1 C   22.500  0.24855  1.46089");

		Map<String, Object> controlMap = new HashMap<>();

		TestUtils.populateControlMapGenus(controlMap);

		var result = parser.parse(is, controlMap);

		assertThat(result, mmHasEntry(coe(1, contains(22.500f, 0.24855f, 1.46089f)), "S1", Region.COASTAL));
		assertThat(result, mmHasEntry(coe(1, contains(0f, 0f, 0f)), "S1", Region.INTERIOR));
		assertThat(result, mmHasEntry(coe(1, contains(0f, 0f, 0f)), "S2", Region.COASTAL));
	}

	@Test
	void testParseAllRegions() throws Exception {

		var parser = new VeteranDQParser();

		var is = TestUtils.makeInputStream("S1     22.500  0.24855  1.46089");

		Map<String, Object> controlMap = new HashMap<>();

		TestUtils.populateControlMapGenus(controlMap);

		var result = parser.parse(is, controlMap);

		assertThat(result, mmHasEntry(coe(1, contains(22.500f, 0.24855f, 1.46089f)), "S1", Region.COASTAL));
		assertThat(result, mmHasEntry(coe(1, contains(22.500f, 0.24855f, 1.46089f)), "S1", Region.INTERIOR));
		assertThat(result, mmHasEntry(coe(1, contains(0f, 0f, 0f)), "S2", Region.COASTAL));
	}

	@Test
	void testParseBadSpecies() throws Exception {

		var parser = new VeteranDQParser();

		var is = TestUtils.makeInputStream("SX C   22.500  0.24855  1.46089");

		Map<String, Object> controlMap = new HashMap<>();

		TestUtils.populateControlMapGenus(controlMap);
		TestUtils.populateControlMapBec(controlMap);

		var ex = assertThrows(ResourceParseLineException.class, () -> parser.parse(is, controlMap));
		assertThat(ex, causedBy(hasProperty("value", is("SX"))));
	}

	@Test
	void testParseMissingCoefficient() throws Exception {

		var parser = new VeteranDQParser();

		var is = TestUtils.makeInputStream("S1 C   22.500  0.24855 ");

		Map<String, Object> controlMap = new HashMap<>();

		TestUtils.populateControlMapGenus(controlMap);
		TestUtils.populateControlMapBec(controlMap);

		var ex = assertThrows(ResourceParseLineException.class, () -> parser.parse(is, controlMap));
		assertThat(ex, causedBy(hasProperty("value", nullValue()))); // TODO Do this better
	}

}
