package ca.bc.gov.nrs.vdyp.io.parse;

import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.causedBy;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import ca.bc.gov.nrs.vdyp.test.TestUtils;

public class SmallComponentProbabilityParserTest {

	@Test
	public void testParseSimpleP1() throws Exception {

		var is = TestUtils.makeStream("S1   0.48205   0.00000 -0.011862  -0.10014");

		Map<String, Object> controlMap = new HashMap<>();

		SP0DefinitionParserTest.populateControlMap(controlMap);

		var parser = new SmallComponentProbabilityParser();
		var result = parser.parse(is, controlMap);

		assertThat(result, hasEntry(is("S1"), contains(0.48205f, 0.00000f, -0.011862f, -0.10014f)));
	}

	@Test
	public void testParseBadSpecies() throws Exception {

		var is = TestUtils.makeStream("SX   0.48205   0.00000 -0.011862  -0.10014");

		Map<String, Object> controlMap = new HashMap<>();

		SP0DefinitionParserTest.populateControlMap(controlMap);

		var parser = new SmallComponentProbabilityParser();

		var ex = assertThrows(ResourceParseLineException.class, () -> parser.parse(is, controlMap));
		assertThat(ex, causedBy(hasProperty("value", is("SX"))));
	}

}
