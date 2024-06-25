package ca.bc.gov.nrs.vdyp.io.parse.control;

import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.notPresent;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;

import ca.bc.gov.nrs.vdyp.common.ControlKey;
import ca.bc.gov.nrs.vdyp.io.FileResolver;
import ca.bc.gov.nrs.vdyp.io.parse.common.ResourceParseException;
import ca.bc.gov.nrs.vdyp.io.parse.common.ResourceParseValidException;
import ca.bc.gov.nrs.vdyp.io.parse.value.ValueParser;

class ControlMapValueReplacerTest {

	@Test
	void testParse() throws ResourceParseException, IOException {
		var unit = new ControlMapValueReplacer<Integer, String>() {

			@Override
			public ControlKey getControlKey() {
				return ControlKey.FIP_INPUT_YIELD_LAYER;
			}

			@Override
			public ValueParser<Object> getValueParser() {
				return ControlMapModifier.FILENAME;
			}

			@Override
			public Integer map(String rawValue, FileResolver fileResolver, Map<String, Object> control)
					throws ResourceParseException, IOException {
				assertThat(rawValue, is("TEST"));
				return 42;
			}
		};

		var controlMap = new HashMap<String, Object>();

		controlMap.put(ControlKey.FIP_INPUT_YIELD_LAYER.name(), "TEST");

		unit.modify(controlMap, null);

		assertThat(controlMap, hasEntry(is(ControlKey.FIP_INPUT_YIELD_LAYER.name()), is(42)));
	}

	@Test
	void testNull() throws ResourceParseException, IOException {
		var unit = new ControlMapValueReplacer<Integer, String>() {

			@Override
			public ControlKey getControlKey() {
				return ControlKey.FIP_INPUT_YIELD_LAYER;
			}

			@Override
			public ValueParser<Object> getValueParser() {
				return ControlMapModifier.FILENAME;
			}

			@Override
			public Integer map(String rawValue, FileResolver fileResolver, Map<String, Object> control)
					throws ResourceParseException, IOException {
				assertThat(rawValue, is("TEST"));
				return 42;
			}
		};

		var controlMap = new HashMap<String, Object>();

		var ex = assertThrows(ResourceParseValidException.class, () -> unit.modify(controlMap, null));

		assertThat(ex, hasProperty("message", is("Expected FIP_INPUT_YIELD_LAYER(12) but it was not present.")));

		assertThat(controlMap, anEmptyMap());

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	void testEmptyOptional() throws ResourceParseException, IOException {
		var unit = new ControlMapValueReplacer<Integer, String>() {

			@Override
			public ControlKey getControlKey() {
				return ControlKey.FIP_INPUT_YIELD_LAYER;
			}

			@Override
			public ValueParser<Object> getValueParser() {
				return ControlMapModifier.FILENAME;
			}

			@Override
			public Integer map(String rawValue, FileResolver fileResolver, Map<String, Object> control)
					throws ResourceParseException, IOException {
				assertThat(rawValue, is("TEST"));
				return 42;
			}
		};

		var controlMap = new HashMap<String, Object>();

		controlMap.put(ControlKey.FIP_INPUT_YIELD_LAYER.name(), Optional.empty());

		var ex = assertThrows(ResourceParseValidException.class, () -> unit.modify(controlMap, null));

		assertThat(ex, hasProperty("message", is("Expected FIP_INPUT_YIELD_LAYER(12) but it was not present.")));

		assertThat(controlMap, hasEntry(is(ControlKey.FIP_INPUT_YIELD_LAYER.name()), (Matcher) notPresent()));

	}

	@Test
	void testPresentOptional() throws ResourceParseException, IOException {
		var unit = new ControlMapValueReplacer<Integer, String>() {

			@Override
			public ControlKey getControlKey() {
				return ControlKey.FIP_INPUT_YIELD_LAYER;
			}

			@Override
			public ValueParser<Object> getValueParser() {
				return ControlMapModifier.FILENAME;
			}

			@Override
			public Integer map(String rawValue, FileResolver fileResolver, Map<String, Object> control)
					throws ResourceParseException, IOException {
				assertThat(rawValue, is("TEST"));
				return 42;
			}
		};

		var controlMap = new HashMap<String, Object>();

		controlMap.put(ControlKey.FIP_INPUT_YIELD_LAYER.name(), Optional.of("TEST"));

		unit.modify(controlMap, null);

		assertThat(controlMap, hasEntry(is(ControlKey.FIP_INPUT_YIELD_LAYER.name()), is(42)));
	}

	@Test
	void testOverrideDefault() throws ResourceParseException, IOException {
		var unit = new ControlMapValueReplacer<Integer, String>() {

			@Override
			public ControlKey getControlKey() {
				return ControlKey.FIP_INPUT_YIELD_LAYER;
			}

			@Override
			public ValueParser<Object> getValueParser() {
				return ControlMapModifier.FILENAME;
			}

			@Override
			public Integer map(String rawValue, FileResolver fileResolver, Map<String, Object> control)
					throws ResourceParseException, IOException {
				fail();
				return 42;
			}

			@Override
			public Integer defaultModification(Map<String, Object> control) throws ResourceParseValidException {
				return 64;
			}

		};

		var controlMap = new HashMap<String, Object>();

		controlMap.put(ControlKey.FIP_INPUT_YIELD_LAYER.name(), Optional.empty());

		unit.modify(controlMap, null);

		assertThat(controlMap, hasEntry(is(ControlKey.FIP_INPUT_YIELD_LAYER.name()), is(64)));
	}

	@Test
	void testOverrideDefaultNotWhenPresent() throws ResourceParseException, IOException {
		var unit = new ControlMapValueReplacer<Integer, String>() {

			@Override
			public ControlKey getControlKey() {
				return ControlKey.FIP_INPUT_YIELD_LAYER;
			}

			@Override
			public ValueParser<Object> getValueParser() {
				return ControlMapModifier.FILENAME;
			}

			@Override
			public Integer map(String rawValue, FileResolver fileResolver, Map<String, Object> control)
					throws ResourceParseException, IOException {
				assertThat(rawValue, is("TEST"));
				return 42;
			}

			@Override
			public Integer defaultModification(Map<String, Object> control) throws ResourceParseValidException {
				fail();
				return 64;
			}

		};

		var controlMap = new HashMap<String, Object>();

		controlMap.put(ControlKey.FIP_INPUT_YIELD_LAYER.name(), Optional.of("TEST"));

		unit.modify(controlMap, null);

		assertThat(controlMap, hasEntry(is(ControlKey.FIP_INPUT_YIELD_LAYER.name()), is(42)));
	}

}
