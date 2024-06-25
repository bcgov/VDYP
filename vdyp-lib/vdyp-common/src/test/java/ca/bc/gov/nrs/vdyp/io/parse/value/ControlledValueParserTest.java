package ca.bc.gov.nrs.vdyp.io.parse.value;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collections;
import java.util.Map;

import org.easymock.EasyMock;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import ca.bc.gov.nrs.vdyp.common.ControlKey;
import ca.bc.gov.nrs.vdyp.model.BecLookup;
import ca.bc.gov.nrs.vdyp.model.UtilizationClass;

class ControlledValueParserTest {

	@Test
	void testParseBec() throws Exception {
		BecLookup becLookup = EasyMock.createMock(BecLookup.class);
		EasyMock.expect(becLookup.getBecAliases()).andStubReturn(Collections.singletonList("TST"));
		EasyMock.replay(becLookup);
		var control = Collections.singletonMap(ControlKey.BEC_DEF.name(), (Object) becLookup);
		assertThat(ControlledValueParser.BEC.parse("TST", control), is("TST"));
		EasyMock.verify(becLookup);
	}

	@ParameterizedTest
	@ValueSource(strings = { "BAD", " XX", "  X", "   ", "X  " })
	void testParseBadBec(String badId) throws Exception {
		BecLookup becLookup = EasyMock.createMock(BecLookup.class);
		EasyMock.expect(becLookup.getBecAliases()).andStubReturn(Collections.singletonList("TST"));
		EasyMock.replay(becLookup);
		var control = Collections.singletonMap(ControlKey.BEC_DEF.name(), (Object) becLookup);
		assertThrows(ValueParseException.class, () -> {
			ControlledValueParser.BEC.parse(badId, control);
		});
		EasyMock.verify(becLookup);
	}

	@ParameterizedTest
	@EnumSource(UtilizationClass.class)
	void testParseUtilClass(UtilizationClass uc) throws Exception {
		Map<String, Object> control = Collections.emptyMap(); // Doesn't actually need to be a ControlledValueParser but
																// it's here with the BEC parser
		assertThat(ControlledValueParser.UTILIZATION_CLASS.parse(String.format("%2d", uc.index), control), is(uc));
	}

	@ParameterizedTest
	@ValueSource(strings = { "  ", "-2", " 5", " X" })
	void testParseBadUtilClass(String uc) throws Exception {
		Map<String, Object> control = Collections.emptyMap(); // Doesn't actually need to be a ControlledValueParser but
																// it's here with the BEC parser
		assertThrows(ValueParseException.class, () -> {
			ControlledValueParser.UTILIZATION_CLASS.parse(uc, control);
		});
	}

}
