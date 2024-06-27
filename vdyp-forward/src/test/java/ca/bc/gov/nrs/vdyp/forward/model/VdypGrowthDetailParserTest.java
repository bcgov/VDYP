package ca.bc.gov.nrs.vdyp.forward.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import ca.bc.gov.nrs.vdyp.forward.parsers.VdypControlVariableParser;
import ca.bc.gov.nrs.vdyp.io.parse.value.ValueParseException;

class VdypGrowthDetailParserTest {

	@Test
	void testNullInput() throws Exception {
		try {
			var parser = new VdypControlVariableParser();
			parser.parse(null);
			Assertions.fail();
		} catch (ValueParseException e) {
			assertThat(e, hasProperty("message", is("VdypControlVariableParser: supplied string is null")));
		}
	}

	@Test
	void testEmptyInput() throws Exception {
		try {
			var parser = new VdypControlVariableParser();
			parser.parse("   ");
			Assertions.fail();
		} catch (ValueParseException e) {
			assertThat(e, hasProperty("message", is("VdypControlVariableParser: supplied string \"   \" is empty")));
		}
	}

	@Test
	void testInvalidInput() throws Exception {
		try {
			var parser = new VdypControlVariableParser();
			parser.parse("a b c");
			Assertions.fail();
		} catch (ValueParseException e) {
			assertThat(e, hasProperty("message", is("\"a\" is not a valid Integer")));
		}
	}

	@Test
	void testValidInput() throws Exception {
		var parser = new VdypControlVariableParser();
		var details1 = parser.parse("-1");
		assertThat(details1, hasProperty("yearCounter", is(-1)));

		var details2 = parser.parse("1 2 3 4 5 6");
		assertThat(details2, hasProperty("yearCounter", is(1)));
		assertThat(1, equalTo(details2.getControlVariable(1)));
		assertThat(2, equalTo(details2.getControlVariable(2)));
		assertThat(3, equalTo(details2.getControlVariable(3)));
		assertThat(4, equalTo(details2.getControlVariable(4)));
		assertThat(5, equalTo(details2.getControlVariable(5)));
		assertThat(6, equalTo(details2.getControlVariable(6)));
	}

	@Test
	void testExtraInputIgnored() throws Exception {
		var parser = new VdypControlVariableParser();
		var details1 = parser.parse("-1");
		assertThat(details1, hasProperty("yearCounter", is(-1)));

		var details2 = parser.parse("1 2 3 4 5 6 7 8 9 10 11");
		assertThat(details2, hasProperty("yearCounter", is(1)));
		assertThat(10, equalTo(details2.getControlVariable(10)));
	}
}
