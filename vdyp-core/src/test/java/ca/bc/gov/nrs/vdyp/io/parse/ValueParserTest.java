package ca.bc.gov.nrs.vdyp.io.parse;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class ValueParserTest {

	@Test
	public void rangeParserTest() throws Exception {
		var exclusiveParser = ValueParser.range(ValueParser.INTEGER, 10, false, 20, false, "Test");

		var result = exclusiveParser.parse("11");
		assertThat(result, is(11));

		result = exclusiveParser.parse("19");
		assertThat(result, is(19));

		var ex = assertThrows(ValueParseException.class, () -> exclusiveParser.parse("10"));
		assertThat(ex, hasProperty("message", is("Test must be greater than 10.")));

		ex = assertThrows(ValueParseException.class, () -> exclusiveParser.parse("9"));
		assertThat(ex, hasProperty("message", is("Test must be greater than 10.")));

		ex = assertThrows(ValueParseException.class, () -> exclusiveParser.parse("20"));
		assertThat(ex, hasProperty("message", is("Test must be less than 20.")));

		ex = assertThrows(ValueParseException.class, () -> exclusiveParser.parse("21"));
		assertThat(ex, hasProperty("message", is("Test must be less than 20.")));

		var inclusiveParser = ValueParser.range(ValueParser.INTEGER, 10, true, 20, true, "Test");

		result = inclusiveParser.parse("11");
		assertThat(result, is(11));

		result = inclusiveParser.parse("19");
		assertThat(result, is(19));

		result = inclusiveParser.parse("10");
		assertThat(result, is(10));

		result = inclusiveParser.parse("20");
		assertThat(result, is(20));

		ex = assertThrows(ValueParseException.class, () -> inclusiveParser.parse("9"));
		assertThat(ex, hasProperty("message", is("Test must be greater than or equal to 10.")));

		ex = assertThrows(ValueParseException.class, () -> inclusiveParser.parse("21"));
		assertThat(ex, hasProperty("message", is("Test must be less than or equal to 20.")));

	}

}
