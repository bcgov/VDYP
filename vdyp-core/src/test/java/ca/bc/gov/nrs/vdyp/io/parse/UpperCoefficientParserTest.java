package ca.bc.gov.nrs.vdyp.io.parse;

import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.mmHasEntry;
import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.present;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import ca.bc.gov.nrs.vdyp.model.Region;
import ca.bc.gov.nrs.vdyp.model.SP0Definition;
import ca.bc.gov.nrs.vdyp.test.TestUtils;

public class UpperCoefficientParserTest {

	@Test
	public void testParseSimple() throws Exception {

		var parser = new UpperCoefficientParser();

		var is = TestUtils.makeStream("S1 I 2.0028 -0.5343");

		Map<String, Object> controlMap = new HashMap<>();

		List<SP0Definition> sp0List = Arrays.asList(
				new SP0Definition("S1", java.util.Optional.empty(), "Test S1"),
				new SP0Definition("S2", java.util.Optional.empty(), "Test S2")
		);

		controlMap.put(SP0DefinitionParser.CONTROL_KEY, sp0List);

		var result = parser.parse(is, controlMap);

		assertThat(result, mmHasEntry(present(is(2.0028f)), Region.INTERIOR, "S1", (Integer) 1));
	}

	@Test
	public void testParseBadSpecies() throws Exception {

		var parser = new UpperCoefficientParser();

		var is = TestUtils.makeStream("SX I 2.0028 -0.5343");

		Map<String, Object> controlMap = new HashMap<>();

		List<SP0Definition> sp0List = Arrays.asList(
				new SP0Definition("S1", java.util.Optional.empty(), "Test S1"),
				new SP0Definition("S2", java.util.Optional.empty(), "Test S2")
		);

		controlMap.put(SP0DefinitionParser.CONTROL_KEY, sp0List);

		assertThrows(ResourceParseLineException.class, () -> parser.parse(is, controlMap));

	}

	@Test
	public void testParseBadRegion() throws Exception {

		var parser = new UpperCoefficientParser();

		var is = TestUtils.makeStream("S1 X 2.0028 -0.5343");

		Map<String, Object> controlMap = new HashMap<>();

		List<SP0Definition> sp0List = Arrays.asList(
				new SP0Definition("S1", java.util.Optional.empty(), "Test S1"),
				new SP0Definition("S2", java.util.Optional.empty(), "Test S2")
		);

		controlMap.put(SP0DefinitionParser.CONTROL_KEY, sp0List);

		assertThrows(ResourceParseLineException.class, () -> parser.parse(is, controlMap));

	}

}
