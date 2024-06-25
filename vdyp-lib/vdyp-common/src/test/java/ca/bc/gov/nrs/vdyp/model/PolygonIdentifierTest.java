package ca.bc.gov.nrs.vdyp.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.ValueSource;

class PolygonIdentifierTest {

	@ParameterizedTest
	@CsvFileSource(resources = { "PolygonIdentifierTestValid.csv" }, numLinesToSkip = 1, useHeadersInDisplayName = true)
	void testSplit(String id, String base, int year) {
		var result = PolygonIdentifier.split(id);
		assertThat(result, equalTo(new PolygonIdentifier(base, year)));
		assertThat(result.getBase(), is(base));
		assertThat(result.getYear(), is(year));
	}

	@ParameterizedTest
	@CsvFileSource(resources = { "PolygonIdentifierTestValid.csv" }, numLinesToSkip = 1, useHeadersInDisplayName = true)
	void testToString(String id, String base, int year) {
		assertThat(new PolygonIdentifier(base, year).toString(), is(id));
	}

	@ParameterizedTest
	@CsvFileSource(resources = { "PolygonIdentifierTestValid.csv" }, numLinesToSkip = 1, useHeadersInDisplayName = true)
	void testEquality(String id, String base, int year) {
		var unit = new PolygonIdentifier(base, year);
		var equivalent = new PolygonIdentifier(base, year);
		var differentBase = new PolygonIdentifier("Different", year);
		var differentYear = new PolygonIdentifier(base, year + 1);

		assertThat(unit, equalTo(equivalent));
		assertThat(unit.hashCode(), is(equivalent.hashCode()));
		assertThat(unit, not(equalTo(differentBase)));
		assertThat(unit.hashCode(), not(is(differentBase.hashCode())));
		assertThat(unit, not(equalTo(differentYear)));
		assertThat(unit.hashCode(), not(is(differentYear.hashCode())));
	}

	@ParameterizedTest
	@ValueSource(
			strings = { //
					"TestPolygonxxxxxxxxxxx024", //
					"TestPolygonxxxxxxxxxx202x", //
					"TestPolygonxxxxxxxxxx20 4", //
					"TestPolygonxxxxxxxxxx    " }
	)
	void testYearNotAnumber(String id) {
		assertThrows(NumberFormatException.class, () -> PolygonIdentifier.split(id));
	}

	@ParameterizedTest
	@ValueSource(
			strings = { //
					"TestPolygon         2024", //
					"TestPolygon           2024", //
					"TestPolygon          256", //
					"TestPolygon", //
					"" }
	)
	void testIdWrongLength(String id) {
		var ex = assertThrows(IllegalArgumentException.class, () -> PolygonIdentifier.split(id));
		assertThat(ex, hasProperty("message", is("Polygon identifier \"" + id + "\" must be exactly 25 characters.")));
	}

	@ParameterizedTest
	@ValueSource(strings = { "TestPolygonxxxxxxxxxxx" })
	void testBaseWrongLength(String base) {
		var ex = assertThrows(IllegalArgumentException.class, () -> new PolygonIdentifier(base, 2024));
		assertThat(ex, hasProperty("message", is("Polygon identifier base \"" + base + "\" is too long.")));
	}

	@ParameterizedTest
	@ValueSource(ints = { 0, -1, -2024 })
	void testBaseWrongYear(int year) {
		var ex = assertThrows(IllegalArgumentException.class, () -> new PolygonIdentifier("TestPolygon", year));
		assertThat(ex, hasProperty("message", is("Polygon identifier year " + year + " must be positive.")));
	}
}
