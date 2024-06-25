package ca.bc.gov.nrs.vdyp.io.parse.coe;

import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.mmHasEntry;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import ca.bc.gov.nrs.vdyp.common.ControlKey;
import ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexEquation;
import ca.bc.gov.nrs.vdyp.io.parse.common.ResourceParseValidException;
import ca.bc.gov.nrs.vdyp.model.GenusDefinition;
import ca.bc.gov.nrs.vdyp.model.Region;
import ca.bc.gov.nrs.vdyp.test.TestUtils;

class SiteCurveParserTest {

	@Test
	void testSimple() throws Exception {
		var parser = new SiteCurveParser();
		var is = TestUtils.makeInputStream("S1 001002");

		Map<String, Object> controlMap = new HashMap<>();
		List<GenusDefinition> sp0List = new ArrayList<>();

		sp0List.add(new GenusDefinition("S1", java.util.Optional.empty(), "Test Species 1"));

		controlMap.put(ControlKey.SP0_DEF.name(), sp0List);

		var result = parser.parse(is, controlMap);

		assertThat(result, mmHasEntry(is(SiteIndexEquation.getByIndex(1)), "S1", Region.COASTAL));
		assertThat(result, mmHasEntry(is(SiteIndexEquation.getByIndex(2)), "S1", Region.INTERIOR));
	}

	@Test
	void testExtraSpecies() throws Exception {
		var parser = new SiteCurveParser();
		var is = TestUtils.makeInputStream("S1 001002", "X2 003004");

		Map<String, Object> controlMap = new HashMap<>();
		List<GenusDefinition> sp0List = new ArrayList<>();

		sp0List.add(new GenusDefinition("S1", java.util.Optional.empty(), "Test Species 1"));

		controlMap.put(ControlKey.SP0_DEF.name(), sp0List);

		var result = parser.parse(is, controlMap);

		assertThat(result, mmHasEntry(is(SiteIndexEquation.getByIndex(1)), "S1", Region.COASTAL));
		assertThat(result, mmHasEntry(is(SiteIndexEquation.getByIndex(2)), "S1", Region.INTERIOR));
		assertThat(result, mmHasEntry(is(SiteIndexEquation.getByIndex(3)), "X2", Region.COASTAL));
		assertThat(result, mmHasEntry(is(SiteIndexEquation.getByIndex(4)), "X2", Region.INTERIOR));
	}

	@Test
	void testMissingSpecies() throws Exception {
		var parser = new SiteCurveParser();
		var is = TestUtils.makeInputStream("S1 001002");

		Map<String, Object> controlMap = new HashMap<>();
		List<GenusDefinition> sp0List = new ArrayList<>();

		sp0List.add(new GenusDefinition("S1", java.util.Optional.empty(), "Test Species 1"));
		sp0List.add(new GenusDefinition("S2", java.util.Optional.empty(), "Test Species 2"));

		controlMap.put(ControlKey.SP0_DEF.name(), sp0List);

		var ex = assertThrows(ResourceParseValidException.class, () -> parser.parse(is, controlMap));

		assertThat(ex, hasProperty("message", is("Missing expected entries for S2")));
	}

	@Test
	void testHashComment() throws Exception {
		var parser = new SiteCurveParser();
		var is = TestUtils.makeInputStream("# Foo", "S1 001002");

		Map<String, Object> controlMap = new HashMap<>();
		List<GenusDefinition> sp0List = new ArrayList<>();

		sp0List.add(new GenusDefinition("S1", java.util.Optional.empty(), "Test Species 1"));

		controlMap.put(ControlKey.SP0_DEF.name(), sp0List);

		var result = parser.parse(is, controlMap);

		assertThat(result, mmHasEntry(is(SiteIndexEquation.getByIndex(1)), "S1", Region.COASTAL));
		assertThat(result, mmHasEntry(is(SiteIndexEquation.getByIndex(2)), "S1", Region.INTERIOR));
	}

	@Test
	void testSpaceComment() throws Exception {
		var parser = new SiteCurveParser();
		var is = TestUtils.makeInputStream("  Foo", "S1 001002");

		Map<String, Object> controlMap = new HashMap<>();
		List<GenusDefinition> sp0List = new ArrayList<>();

		sp0List.add(new GenusDefinition("S1", java.util.Optional.empty(), "Test Species 1"));

		controlMap.put(ControlKey.SP0_DEF.name(), sp0List);

		var result = parser.parse(is, controlMap);

		assertThat(result, mmHasEntry(is(SiteIndexEquation.getByIndex(1)), "S1", Region.COASTAL));
		assertThat(result, mmHasEntry(is(SiteIndexEquation.getByIndex(2)), "S1", Region.INTERIOR));
	}

	@Test
	void testEndFileLine() throws Exception {
		var parser = new SiteCurveParser();
		var is = TestUtils.makeInputStream("S1 001002", "##", "S2 003004");

		Map<String, Object> controlMap = new HashMap<>();
		List<GenusDefinition> sp0List = new ArrayList<>();

		sp0List.add(new GenusDefinition("S1", java.util.Optional.empty(), "Test Species 1"));

		controlMap.put(ControlKey.SP0_DEF.name(), sp0List);

		var result = parser.parse(is, controlMap);

		assertThat(result, mmHasEntry(is(SiteIndexEquation.getByIndex(1)), "S1", Region.COASTAL));
		assertThat(result, mmHasEntry(is(SiteIndexEquation.getByIndex(2)), "S1", Region.INTERIOR));
	}

}
