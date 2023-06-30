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

public class ComponentSizeParserTest {

	@Test
	public void testParseSimpleP1() throws Exception {

		var is = TestUtils.makeStream("S1 C  49.4 153.3 0.726 3.647");

		Map<String, Object> controlMap = new HashMap<>();

		SP0DefinitionParserTest.populateControlMap(controlMap);

		var parser = new ComponentSizeParser();

		var result = parser.parse(is, controlMap);

		assertThat(result, mmHasEntry(present(contains(49.4f, 153.3f, 0.726f, 3.647f)), "S1", Region.COASTAL));
	}

	@Test
	public void testParseBadSpecies() throws Exception {

		var is = TestUtils.makeStream("SX C  49.4 153.3 0.726 3.647");

		Map<String, Object> controlMap = new HashMap<>();

		SP0DefinitionParserTest.populateControlMap(controlMap);

		var parser = new ComponentSizeParser();

		@SuppressWarnings("unused")
		var ex = assertThrows(ResourceParseLineException.class, () -> parser.parse(is, controlMap));

	}

	@Test
	public void testParseBadRegion() throws Exception {

		var is = TestUtils.makeStream("S1 X  49.4 153.3 0.726 3.647");

		Map<String, Object> controlMap = new HashMap<>();

		SP0DefinitionParserTest.populateControlMap(controlMap);

		var parser = new ComponentSizeParser();

		@SuppressWarnings("unused")
		var ex = assertThrows(ResourceParseLineException.class, () -> parser.parse(is, controlMap));

	}

}
