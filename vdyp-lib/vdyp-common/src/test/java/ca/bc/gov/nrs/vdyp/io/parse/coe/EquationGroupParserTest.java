package ca.bc.gov.nrs.vdyp.io.parse.coe;

import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.causedBy;
import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.mmHasEntry;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import ca.bc.gov.nrs.vdyp.io.parse.common.ResourceParseLineException;
import ca.bc.gov.nrs.vdyp.io.parse.common.ResourceParseValidException;
import ca.bc.gov.nrs.vdyp.test.TestUtils;

@SuppressWarnings("unused")
class EquationGroupParserTest {

	@Test
	void testParse() throws Exception {
		var parser = new DefaultEquationNumberParser();

		var controlMap = makeControlMapSingle();

		var is = TestUtils.makeInputStream(
				"S1 B1   001"//
		);
		var result = parser.parse(is, Collections.unmodifiableMap(controlMap));

		assertThat(result, mmHasEntry(is(1), "S1", "B1"));
	}

	@Test
	void testSP0MustExist() throws Exception {
		var parser = new DefaultEquationNumberParser();

		var controlMap = makeControlMapSingle();

		var is = TestUtils.makeInputStream("SX B1   001");

		ResourceParseLineException ex1 = Assertions.assertThrows(
				ResourceParseLineException.class, () -> parser.parse(is, Collections.unmodifiableMap(controlMap))
		);

		assertThat(ex1, hasProperty("message", stringContainsInOrder("line 1", "SX", "SP0")));
		assertThat(ex1, hasProperty("line", is(1)));
		assertThat(ex1, causedBy(hasProperty("value", is("SX"))));
	}

	@Test
	void testBecMustExist() throws Exception {
		var parser = new DefaultEquationNumberParser();

		var controlMap = makeControlMapSingle();

		var is = TestUtils.makeInputStream(
				"S1 BX   001"//
		);

		ResourceParseLineException ex1 = Assertions.assertThrows(
				ResourceParseLineException.class, () -> parser.parse(is, Collections.unmodifiableMap(controlMap))
		);

		assertThat(ex1, hasProperty("message", stringContainsInOrder("line 1", "BX", "BEC")));
		assertThat(ex1, hasProperty("line", is(1)));
		assertThat(ex1, causedBy(hasProperty("value", is("BX"))));
	}

	@Test
	void testParseOvewrite() throws Exception {
		// Original Fortran allows subsequent entries to overwrite old ones so don't
		// validate against that

		var parser = new DefaultEquationNumberParser();

		var controlMap = makeControlMapSingle();

		var is = TestUtils.makeInputStream(
				"S1 B1   001", //
				"S1 B1   002"
		);
		var result = parser.parse(is, Collections.unmodifiableMap(controlMap));

		assertThat(result, mmHasEntry(is(2), "S1", "B1"));
	}

	@Test
	void testParseIgnoreBlankSpecies() throws Exception {
		// Original Fortran allows subsequent entries to overwrite old ones so don't
		// validate against that

		var parser = new DefaultEquationNumberParser();

		var controlMap = makeControlMapSingle();

		var is = TestUtils.makeInputStream(
				"S1 B1   001", //
				"   B1   003", //
				"S1 B1   002"
		);
		var result = parser.parse(is, Collections.unmodifiableMap(controlMap));

		assertThat(result, mmHasEntry(is(2), "S1", "B1"));
	}

	@Test
	void testParseIgnoreBlankBec() throws Exception {
		// Original Fortran allows subsequent entries to overwrite old ones so don't
		// validate against that

		var parser = new DefaultEquationNumberParser();

		var controlMap = makeControlMapSingle();

		var is = TestUtils.makeInputStream(
				"S1 B1   001", //
				"S1      003", //
				"S1 B1   002"
		);
		var result = parser.parse(is, Collections.unmodifiableMap(controlMap));

		assertThat(result, mmHasEntry(is(2), "S1", "B1"));
	}

	@Test
	void testParseIgnoreBlankLine() throws Exception {
		// Original Fortran allows subsequent entries to overwrite old ones so don't
		// validate against that

		var parser = new DefaultEquationNumberParser();

		var controlMap = makeControlMapSingle();

		var is = TestUtils.makeInputStream(
				"S1 B1   001", //
				"", //
				"S1 B1   002"
		);
		var result = parser.parse(is, Collections.unmodifiableMap(controlMap));

		assertThat(result, mmHasEntry(is(2), "S1", "B1"));
	}

	@Test
	void testParseMultiple() throws Exception {
		var parser = new DefaultEquationNumberParser();

		var controlMap = makeControlMap();

		var is = TestUtils.makeInputStream(
				"S1 B1   011", //
				"S1 B2   012", //
				"S1 B3   013", //
				"S1 B4   014", //
				"S2 B1   021", //
				"S2 B2   022", //
				"S2 B3   023", //
				"S2 B4   024"
		);
		var result = parser.parse(is, Collections.unmodifiableMap(controlMap));

		assertThat(result, mmHasEntry(is(11), "S1", "B1"));
		assertThat(result, mmHasEntry(is(12), "S1", "B2"));
		assertThat(result, mmHasEntry(is(13), "S1", "B3"));
		assertThat(result, mmHasEntry(is(14), "S1", "B4"));
		assertThat(result, mmHasEntry(is(21), "S2", "B1"));
		assertThat(result, mmHasEntry(is(22), "S2", "B2"));
		assertThat(result, mmHasEntry(is(23), "S2", "B3"));
		assertThat(result, mmHasEntry(is(24), "S2", "B4"));

	}

	@Test
	void testRequireNoMissingSp0() throws Exception {

		var parser = new DefaultEquationNumberParser();

		var controlMap = makeControlMap();

		List<String> unusedBecs = Arrays.asList("B2", "B4");

		var is = TestUtils.makeInputStream(
				"S1 B1   011", //
				"S1 B2   012", //
				"S1 B3   013", //
				"S1 B4   014"
		);

		ResourceParseValidException ex1 = assertThrows(
				ResourceParseValidException.class, () -> parser.parse(is, Collections.unmodifiableMap(controlMap))
		);

		assertThat(ex1, hasProperty("message", is("Expected mappings for SP0 S2 but it was missing")));

	}

	@Test
	void testRequireNoMissingBec() throws Exception {

		var parser = new DefaultEquationNumberParser();

		var controlMap = makeControlMap();

		List<String> unusedBecs = Arrays.asList("B2", "B4");
		String[] lines = {};

		var is = TestUtils.makeInputStream(
				"S1 B1   011", //
				"S1 B2   012", //
				"S1 B4   014", //
				"S2 B1   021", //
				"S2 B2   022", //
				"S2 B3   023", //
				"S2 B4   024"
		);

		ResourceParseValidException ex1 = assertThrows(
				ResourceParseValidException.class, () -> parser.parse(is, Collections.unmodifiableMap(controlMap))
		);

		assertThat(ex1, hasProperty("message", is("Expected mappings for BEC B3 but it was missing for SP0 S1")));

	}

	@Test
	void testRequireNoUnexpectedBec() throws Exception {

		var parser = new DefaultEquationNumberParser();

		var controlMap = makeControlMap();

		List<String> hiddenBecs = Arrays.asList("B3");
		parser.setHiddenBecs(hiddenBecs);

		var is = TestUtils.makeInputStream(
				"S1 B1   011", //
				"S1 B2   012", //
				"S1 B4   014", //
				"S2 B1   021", //
				"S2 B2   022", //
				"S2 B3   023", //
				"S2 B4   024"
		);

		ResourceParseValidException ex1 = assertThrows(
				ResourceParseValidException.class, () -> parser.parse(is, Collections.unmodifiableMap(controlMap))
		);

		assertThat(ex1, hasProperty("message", is("Unexpected mapping for BEC B3 under SP0 S2")));

	}

	private HashMap<String, Object> makeControlMapSingle() {
		var controlMap = new HashMap<String, Object>();

		TestUtils.populateControlMapBec(controlMap, "B1");
		TestUtils.populateControlMapGenus(controlMap, "S1");
		return controlMap;
	}

	private HashMap<String, Object> makeControlMap() {
		var controlMap = new HashMap<String, Object>();

		TestUtils.populateControlMapBec(controlMap, "B1", "B2", "B3", "B4");
		TestUtils.populateControlMapGenus(controlMap, "S1", "S2");
		return controlMap;
	}
}
