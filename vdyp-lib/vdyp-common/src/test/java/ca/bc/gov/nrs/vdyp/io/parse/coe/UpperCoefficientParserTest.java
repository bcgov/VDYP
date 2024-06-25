package ca.bc.gov.nrs.vdyp.io.parse.coe;

import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.mmHasEntry;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import ca.bc.gov.nrs.vdyp.common.ControlKey;
import ca.bc.gov.nrs.vdyp.io.parse.common.ResourceParseLineException;
import ca.bc.gov.nrs.vdyp.model.GenusDefinition;
import ca.bc.gov.nrs.vdyp.model.Region;
import ca.bc.gov.nrs.vdyp.test.TestUtils;

class UpperCoefficientParserTest {

	@Test
	void testParseSimple() throws Exception {

		var parser = new UpperCoefficientParser();

		var is = TestUtils.makeInputStream("S1 I 2.0028 -0.5343");

		Map<String, Object> controlMap = new HashMap<>();

		List<GenusDefinition> sp0List = Arrays.asList(
				new GenusDefinition("S1", java.util.Optional.empty(), "Test S1"),
				new GenusDefinition("S2", java.util.Optional.empty(), "Test S2")
		);

		controlMap.put(ControlKey.SP0_DEF.name(), sp0List);

		var result = parser.parse(is, controlMap);

		assertThat(result, mmHasEntry(is(2.0028f), Region.INTERIOR, "S1", (Integer) 1));
	}

	@Test
	void testParseBadSpecies() throws Exception {

		var parser = new UpperCoefficientParser();

		var is = TestUtils.makeInputStream("SX I 2.0028 -0.5343");

		Map<String, Object> controlMap = new HashMap<>();

		List<GenusDefinition> sp0List = Arrays.asList(
				new GenusDefinition("S1", java.util.Optional.empty(), "Test S1"),
				new GenusDefinition("S2", java.util.Optional.empty(), "Test S2")
		);

		controlMap.put(ControlKey.SP0_DEF.name(), sp0List);

		assertThrows(ResourceParseLineException.class, () -> parser.parse(is, controlMap));

	}

	@Test
	void testParseBadRegion() throws Exception {

		var parser = new UpperCoefficientParser();

		var is = TestUtils.makeInputStream("S1 X 2.0028 -0.5343");

		Map<String, Object> controlMap = new HashMap<>();

		List<GenusDefinition> sp0List = Arrays.asList(
				new GenusDefinition("S1", java.util.Optional.empty(), "Test S1"),
				new GenusDefinition("S2", java.util.Optional.empty(), "Test S2")
		);

		controlMap.put(ControlKey.SP0_DEF.name(), sp0List);

		assertThrows(ResourceParseLineException.class, () -> parser.parse(is, controlMap));

	}

}
