package ca.bc.gov.nrs.vdyp.io.parse;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import ca.bc.gov.nrs.vdyp.model.SP0Definition;
import ca.bc.gov.nrs.vdyp.test.TestUtils;

public class SiteCurveParserTest {

	@Test
	public void testSimple() throws Exception {
		var parser = new SiteCurveParser();
		var is = TestUtils.makeStream("S1 001002");

		Map<String, Object> controlMap = new HashMap<>();
		List<SP0Definition> sp0List = new ArrayList<>();

		sp0List.add(new SP0Definition("S1", java.util.Optional.empty(), "Test Species 1"));

		controlMap.put(SP0DefinitionParser.CONTROL_KEY, sp0List);

		var result = parser.parse(is, controlMap);

		assertThat(result, hasEntry(is("S1"), allOf(hasProperty("value1", is(1)), hasProperty("value2", is(2)))));
	}

	@Test
	public void testExtraSpecies() throws Exception {
		var parser = new SiteCurveParser();
		var is = TestUtils.makeStream("S1 001002", "X2 003004");

		Map<String, Object> controlMap = new HashMap<>();
		List<SP0Definition> sp0List = new ArrayList<>();

		sp0List.add(new SP0Definition("S1", java.util.Optional.empty(), "Test Species 1"));

		controlMap.put(SP0DefinitionParser.CONTROL_KEY, sp0List);

		var result = parser.parse(is, controlMap);

		assertThat(result, hasEntry(is("S1"), allOf(hasProperty("value1", is(1)), hasProperty("value2", is(2)))));
		assertThat(result, hasEntry(is("X2"), allOf(hasProperty("value1", is(3)), hasProperty("value2", is(4)))));
	}

	@Test
	public void testMissingSpecies() throws Exception {
		var parser = new SiteCurveParser();
		var is = TestUtils.makeStream("S1 001002");

		Map<String, Object> controlMap = new HashMap<>();
		List<SP0Definition> sp0List = new ArrayList<>();

		sp0List.add(new SP0Definition("S1", java.util.Optional.empty(), "Test Species 1"));
		sp0List.add(new SP0Definition("S2", java.util.Optional.empty(), "Test Species 2"));

		controlMap.put(SP0DefinitionParser.CONTROL_KEY, sp0List);

		var ex = assertThrows(ResourceParseValidException.class, () -> parser.parse(is, controlMap));

		assertThat(ex, hasProperty("message", is("Missing expected entries for S2")));
	}

	@Test
	public void testHashComment() throws Exception {
		var parser = new SiteCurveParser();
		var is = TestUtils.makeStream("# Foo", "S1 001002");

		Map<String, Object> controlMap = new HashMap<>();
		List<SP0Definition> sp0List = new ArrayList<>();

		sp0List.add(new SP0Definition("S1", java.util.Optional.empty(), "Test Species 1"));

		controlMap.put(SP0DefinitionParser.CONTROL_KEY, sp0List);

		var result = parser.parse(is, controlMap);

		assertThat(result, hasEntry(is("S1"), allOf(hasProperty("value1", is(1)), hasProperty("value2", is(2)))));
	}

	@Test
	public void testSpaceComment() throws Exception {
		var parser = new SiteCurveParser();
		var is = TestUtils.makeStream("  Foo", "S1 001002");

		Map<String, Object> controlMap = new HashMap<>();
		List<SP0Definition> sp0List = new ArrayList<>();

		sp0List.add(new SP0Definition("S1", java.util.Optional.empty(), "Test Species 1"));

		controlMap.put(SP0DefinitionParser.CONTROL_KEY, sp0List);

		var result = parser.parse(is, controlMap);

		assertThat(result, hasEntry(is("S1"), allOf(hasProperty("value1", is(1)), hasProperty("value2", is(2)))));
	}

	@Test
	public void testEndFileLine() throws Exception {
		var parser = new SiteCurveParser();
		var is = TestUtils.makeStream("S1 001002", "##", "S2 003004");

		Map<String, Object> controlMap = new HashMap<>();
		List<SP0Definition> sp0List = new ArrayList<>();

		sp0List.add(new SP0Definition("S1", java.util.Optional.empty(), "Test Species 1"));

		controlMap.put(SP0DefinitionParser.CONTROL_KEY, sp0List);

		var result = parser.parse(is, controlMap);

		assertThat(result, hasEntry(is("S1"), allOf(hasProperty("value1", is(1)), hasProperty("value2", is(2)))));
	}

}
