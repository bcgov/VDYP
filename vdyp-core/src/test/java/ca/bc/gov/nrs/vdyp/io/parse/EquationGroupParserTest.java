package ca.bc.gov.nrs.vdyp.io.parse;

import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.causedBy;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import ca.bc.gov.nrs.vdyp.model.BecDefinition;
import ca.bc.gov.nrs.vdyp.model.Region;
import ca.bc.gov.nrs.vdyp.model.GenusDefinition;
import ca.bc.gov.nrs.vdyp.test.TestUtils;
import ca.bc.gov.nrs.vdyp.test.VdypMatchers;

@SuppressWarnings("unused")
public class EquationGroupParserTest {

	@Test
	public void testParse() throws Exception {
		var parser = new DefaultEquationNumberParser();

		var controlMap = makeControlMapSingle();
		String[] lines = { "S1 B1   001" };

		var is = TestUtils.makeStream(lines);
		var result = parser.parse(is, Collections.unmodifiableMap(controlMap));

		assertThat(result, hasEntry(is("S1"), hasEntry(is("B1"), is(1))));
	}

	@Test
	public void testSP0MustExist() throws Exception {
		var parser = new DefaultEquationNumberParser();

		var controlMap = makeControlMapSingle();
		String[] lines = { "SX B1   001" };

		var is = TestUtils.makeStream(lines);

		ResourceParseLineException ex1 = Assertions.assertThrows(
				ResourceParseLineException.class, () -> parser.parse(is, Collections.unmodifiableMap(controlMap))
		);

		assertThat(ex1, hasProperty("message", stringContainsInOrder("line 1", "SX", "SP0")));
		assertThat(ex1, hasProperty("line", is(1)));
		assertThat(ex1, causedBy(hasProperty("value", is("SX"))));
	}

	@Test
	public void testBecMustExist() throws Exception {
		var parser = new DefaultEquationNumberParser();

		var controlMap = makeControlMapSingle();
		String[] lines = { "S1 BX   001" };

		var is = TestUtils.makeStream(lines);

		ResourceParseLineException ex1 = Assertions.assertThrows(
				ResourceParseLineException.class, () -> parser.parse(is, Collections.unmodifiableMap(controlMap))
		);

		assertThat(ex1, hasProperty("message", stringContainsInOrder("line 1", "BX", "BEC")));
		assertThat(ex1, hasProperty("line", is(1)));
		assertThat(ex1, causedBy(hasProperty("value", is("BX"))));
	}

	@Test
	public void testParseOvewrite() throws Exception {
		// Original Fortran allows subsequent entries to overwrite old ones so don't
		// validate against that

		var parser = new DefaultEquationNumberParser();

		var controlMap = makeControlMapSingle();
		String[] lines = { "S1 B1   001", "S1 B1   002" };

		var is = TestUtils.makeStream(lines);
		var result = parser.parse(is, Collections.unmodifiableMap(controlMap));

		assertThat(result, hasEntry(is("S1"), hasEntry(is("B1"), is(2))));
	}

	@Test
	public void testParseMultiple() throws Exception {
		var parser = new DefaultEquationNumberParser();

		var controlMap = makeControlMap();
		String[] lines = { "S1 B1   011", "S1 B2   012", "S1 B3   013", "S1 B4   014", "S2 B1   021", "S2 B2   022",
				"S2 B3   023", "S2 B4   024" };

		var is = TestUtils.makeStream(lines);
		var result = parser.parse(is, Collections.unmodifiableMap(controlMap));

		assertThat(result, hasEntry(is("S1"), hasEntry(is("B1"), is(11))));
		assertThat(result, hasEntry(is("S1"), hasEntry(is("B2"), is(12))));
		assertThat(result, hasEntry(is("S1"), hasEntry(is("B3"), is(13))));
		assertThat(result, hasEntry(is("S1"), hasEntry(is("B4"), is(14))));
		assertThat(result, hasEntry(is("S2"), hasEntry(is("B1"), is(21))));
		assertThat(result, hasEntry(is("S2"), hasEntry(is("B2"), is(22))));
		assertThat(result, hasEntry(is("S2"), hasEntry(is("B3"), is(23))));
		assertThat(result, hasEntry(is("S2"), hasEntry(is("B4"), is(24))));
	}

	@Test
	public void testRequireNoMissingSp0() throws Exception {

		var parser = new DefaultEquationNumberParser();

		var controlMap = makeControlMap();

		List<String> unusedBecs = Arrays.asList("B2", "B4");
		String[] lines = { "S1 B1   011", "S1 B2   012", "S1 B3   013", "S1 B4   014" };

		var is = TestUtils.makeStream(lines);

		ResourceParseValidException ex1 = assertThrows(
				ResourceParseValidException.class, () -> parser.parse(is, Collections.unmodifiableMap(controlMap))
		);

		assertThat(ex1, hasProperty("message", is("Expected mappings for SP0 S2 but it was missing")));

	}

	@Test
	public void testRequireNoMissingBec() throws Exception {

		var parser = new DefaultEquationNumberParser();

		var controlMap = makeControlMap();

		List<String> unusedBecs = Arrays.asList("B2", "B4");
		String[] lines = { "S1 B1   011", "S1 B2   012", "S1 B4   014", "S2 B1   021", "S2 B2   022", "S2 B3   023",
				"S2 B4   024" };

		var is = TestUtils.makeStream(lines);

		ResourceParseValidException ex1 = assertThrows(
				ResourceParseValidException.class, () -> parser.parse(is, Collections.unmodifiableMap(controlMap))
		);

		assertThat(ex1, hasProperty("message", is("Expected mappings for BEC B3 but it was missing for SP0 S1")));

	}

	@Test
	public void testRequireNoUnexpectedBec() throws Exception {

		var parser = new DefaultEquationNumberParser();

		var controlMap = makeControlMap();

		List<String> hiddenBecs = Arrays.asList("B3");
		parser.setHiddenBecs(hiddenBecs);
		String[] lines = { "S1 B1   011", "S1 B2   012", "S1 B4   014", "S2 B1   021", "S2 B2   022", "S2 B3   023",
				"S2 B4   024" };

		var is = TestUtils.makeStream(lines);

		ResourceParseValidException ex1 = assertThrows(
				ResourceParseValidException.class, () -> parser.parse(is, Collections.unmodifiableMap(controlMap))
		);

		assertThat(ex1, hasProperty("message", is("Unexpected mapping for BEC B3 under SP0 S2")));

	}

	private HashMap<String, Object> makeControlMapSingle() {
		var controlMap = new HashMap<String, Object>();

		BecDefinitionParserTest.populateControlMap(controlMap, "B1");
		GenusDefinitionParserTest.populateControlMap(controlMap, "S1");
		return controlMap;
	}

	private HashMap<String, Object> makeControlMap() {
		var controlMap = new HashMap<String, Object>();

		BecDefinitionParserTest.populateControlMap(controlMap, "B1", "B2", "B3", "B4");
		GenusDefinitionParserTest.populateControlMap(controlMap, "S1", "S2");
		return controlMap;
	}
}
