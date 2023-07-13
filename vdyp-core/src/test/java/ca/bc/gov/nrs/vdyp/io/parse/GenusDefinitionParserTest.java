package ca.bc.gov.nrs.vdyp.io.parse;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.stringContainsInOrder;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.hamcrest.Matchers;

import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import ca.bc.gov.nrs.vdyp.model.GenusDefinition;

public class GenusDefinitionParserTest {

	@Test
	public void testParse() throws Exception {
		var parser = new GenusDefinitionParser();

		var result = parser.parse(ControlFileParserTest.class, "coe/SP0DEF_v0.dat", Collections.emptyMap());

		assertThat(
				result,
				contains(
						allOf(
								Matchers.instanceOf(GenusDefinition.class),
								Matchers.hasProperty("alias", equalTo("AC")),
								Matchers.hasProperty("name", equalTo("Cottonwood"))
						),
						allOf(
								Matchers.instanceOf(GenusDefinition.class),
								Matchers.hasProperty("alias", equalTo("AT")),
								Matchers.hasProperty("name", equalTo("Aspen"))
						),
						allOf(
								Matchers.instanceOf(GenusDefinition.class), Matchers.hasProperty("alias", equalTo("B")),
								Matchers.hasProperty("name", equalTo("Balsam"))
						),
						allOf(
								Matchers.instanceOf(GenusDefinition.class), Matchers.hasProperty("alias", equalTo("C")),
								Matchers.hasProperty("name", equalTo("Cedar (X yellow)"))
						),
						allOf(
								Matchers.instanceOf(GenusDefinition.class), Matchers.hasProperty("alias", equalTo("D")),
								Matchers.hasProperty("name", equalTo("Alder"))
						),
						allOf(
								Matchers.instanceOf(GenusDefinition.class), Matchers.hasProperty("alias", equalTo("E")),
								Matchers.hasProperty("name", equalTo("Birch"))
						),
						allOf(
								Matchers.instanceOf(GenusDefinition.class), Matchers.hasProperty("alias", equalTo("F")),
								Matchers.hasProperty("name", equalTo("Douglas Fir"))
						),
						allOf(
								Matchers.instanceOf(GenusDefinition.class), Matchers.hasProperty("alias", equalTo("H")),
								Matchers.hasProperty("name", equalTo("Hemlock"))
						),
						allOf(
								Matchers.instanceOf(GenusDefinition.class), Matchers.hasProperty("alias", equalTo("L")),
								Matchers.hasProperty("name", equalTo("Larch"))
						),
						allOf(
								Matchers.instanceOf(GenusDefinition.class),
								Matchers.hasProperty("alias", equalTo("MB")),
								Matchers.hasProperty("name", equalTo("Maple"))
						),
						allOf(
								Matchers.instanceOf(GenusDefinition.class),
								Matchers.hasProperty("alias", equalTo("PA")),
								Matchers.hasProperty("name", equalTo("White-bark pine"))
						),
						allOf(
								Matchers.instanceOf(GenusDefinition.class),
								Matchers.hasProperty("alias", equalTo("PL")),
								Matchers.hasProperty("name", equalTo("Lodgepole Pine"))
						),
						allOf(
								Matchers.instanceOf(GenusDefinition.class),
								Matchers.hasProperty("alias", equalTo("PW")),
								Matchers.hasProperty("name", equalTo("White pine"))
						),
						allOf(
								Matchers.instanceOf(GenusDefinition.class),
								Matchers.hasProperty("alias", equalTo("PY")),
								Matchers.hasProperty("name", equalTo("Yellow pine"))
						),
						allOf(
								Matchers.instanceOf(GenusDefinition.class), Matchers.hasProperty("alias", equalTo("S")),
								Matchers.hasProperty("name", equalTo("Spruce"))
						),
						allOf(
								Matchers.instanceOf(GenusDefinition.class), Matchers.hasProperty("alias", equalTo("Y")),
								Matchers.hasProperty("name", equalTo("Yellow cedar"))
						)

				)
		);
	}

	@Test
	public void testOrderByPreference() throws Exception {
		var parser = new GenusDefinitionParser(2);

		List<GenusDefinition> result;
		try (
				var is = new ByteArrayInputStream(
						"AT Aspen                            02\r\nAC Cottonwood                       01".getBytes()
				);
		) {
			result = parser.parse(is, Collections.emptyMap());
		}
		assertThat(
				result,
				contains(
						allOf(
								Matchers.instanceOf(GenusDefinition.class),
								Matchers.hasProperty("alias", equalTo("AC")),
								Matchers.hasProperty("name", equalTo("Cottonwood"))
						),
						allOf(
								Matchers.instanceOf(GenusDefinition.class),
								Matchers.hasProperty("alias", equalTo("AT")),
								Matchers.hasProperty("name", equalTo("Aspen"))
						)
				)
		);
	}

	@Test
	public void testOrderByLinesBlank() throws Exception {
		var parser = new GenusDefinitionParser(2);

		List<GenusDefinition> result;
		try (
				var is = new ByteArrayInputStream(
						"AT Aspen                              \r\nAC Cottonwood                         ".getBytes()
				);
		) {
			result = parser.parse(is, Collections.emptyMap());
		}
		assertThat(
				result,
				contains(
						allOf(
								Matchers.instanceOf(GenusDefinition.class),
								Matchers.hasProperty("alias", equalTo("AT")),
								Matchers.hasProperty("name", equalTo("Aspen"))
						),
						allOf(
								Matchers.instanceOf(GenusDefinition.class),
								Matchers.hasProperty("alias", equalTo("AC")),
								Matchers.hasProperty("name", equalTo("Cottonwood"))
						)
				)
		);
	}

	@Test
	public void testOrderByLinesZero() throws Exception {
		var parser = new GenusDefinitionParser(2);

		List<GenusDefinition> result;
		try (
				var is = new ByteArrayInputStream(
						"AT Aspen                            00\r\nAC Cottonwood                       00".getBytes()
				);
		) {
			result = parser.parse(is, Collections.emptyMap());
		}
		assertThat(
				result,
				contains(
						allOf(
								Matchers.instanceOf(GenusDefinition.class),
								Matchers.hasProperty("alias", equalTo("AT")),
								Matchers.hasProperty("name", equalTo("Aspen"))
						),
						allOf(
								Matchers.instanceOf(GenusDefinition.class),
								Matchers.hasProperty("alias", equalTo("AC")),
								Matchers.hasProperty("name", equalTo("Cottonwood"))
						)
				)
		);
	}

	@Test
	public void testErrorPreferenceOutOfBoundsHigh() throws Exception {
		var parser = new GenusDefinitionParser(2);

		Exception ex1;
		try (
				var is = new ByteArrayInputStream(
						"AT Aspen                            00\r\nAC Cottonwood                       03".getBytes()
				);
		) {

			ex1 = Assertions
					.assertThrows(ResourceParseLineException.class, () -> parser.parse(is, Collections.emptyMap()));
		}
		assertThat(ex1, hasProperty("line", is(2)));
		assertThat(ex1, hasProperty("message", stringContainsInOrder("line 2", "Preference 3", "larger than 2")));
	}

	@Test
	public void testErrorPreferenceOutOfBoundsLow() throws Exception {
		var parser = new GenusDefinitionParser(2);

		Exception ex1;
		try (
				var is = new ByteArrayInputStream(
						"AT Aspen                            00\r\nAC Cottonwood                       -1".getBytes()
				);
		) {

			ex1 = Assertions
					.assertThrows(ResourceParseLineException.class, () -> parser.parse(is, Collections.emptyMap()));
		}
		assertThat(ex1, hasProperty("line", is(2)));
		assertThat(ex1, hasProperty("message", stringContainsInOrder("line 2", "Preference -1", "less than 0")));
	}

	@Test
	public void testErrorPreferenceDuplicate() throws Exception {
		var parser = new GenusDefinitionParser(2);

		Exception ex1;
		try (
				var is = new ByteArrayInputStream(
						"AT Aspen                            01\r\nAC Cottonwood                       01".getBytes()
				);
		) {

			ex1 = Assertions
					.assertThrows(ResourceParseLineException.class, () -> parser.parse(is, Collections.emptyMap()));
		}
		assertThat(ex1, hasProperty("line", is(2)));
		assertThat(ex1, hasProperty("message", stringContainsInOrder("line 2", "Preference 1", "set to AT")));
	}

	/**
	 * Add a mock control map entry for SP0 parse results with species "S1" and "S2"
	 */
	public static void populateControlMap(Map<String, Object> controlMap) {
		populateControlMap(controlMap, "S1", "S2");
	}

	/**
	 * Add a mock control map entry for SP0 parse results with 16 species
	 */
	public static void populateControlMapReal(Map<String, Object> controlMap) {
		populateControlMap(controlMap, getSpeciesAliases());
	}

	/**
	 * Get the species aliases expected
	 */
	public static String[] getSpeciesAliases() {
		return new String[] { "AC", "AT", "B", "C", "D", "E", "F", "H", "L", "MB", "PA", "PL", "PW", "PY", "S", "Y" };
	}

	/**
	 * Add a mock control map entry for SP0 parse results
	 */
	public static void populateControlMap(Map<String, Object> controlMap, String... aliases) {

		List<GenusDefinition> sp0List = new ArrayList<>();

		for (var alias : aliases) {
			sp0List.add(new GenusDefinition(alias, java.util.Optional.empty(), "Test " + alias));
		}

		controlMap.put(GenusDefinitionParser.CONTROL_KEY, sp0List);
	}
}
