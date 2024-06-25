package ca.bc.gov.nrs.vdyp.common;

import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.coe;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class UtilsTest {

	@Nested
	class SingletonOrEmpty {
		@Test
		void testForNull() {
			var result = Utils.singletonOrEmpty(null);
			assertThat(result, Matchers.empty());
		}

		@Test
		void testForNonNull() {
			var result = Utils.singletonOrEmpty("X");
			assertThat(result, Matchers.contains("X"));
		}
	}

	@Nested
	class ExpectParsedControl {

		@Test
		void testMissing() {
			var ex = assertThrows(
					IllegalStateException.class,
					() -> Utils.expectParsedControl(Collections.emptyMap(), "NOT_PRESENT", Integer.class)
			);
			assertThat(
					ex, hasProperty("message", stringContainsInOrder("Expected control map to have", "NOT_PRESENT"))
			);
		}

		@Test
		void testWrongType() {
			var ex = assertThrows(
					IllegalStateException.class,
					() -> Utils.expectParsedControl(
							Collections.singletonMap("WRONG_TYPE", 2d), "WRONG_TYPE", Integer.class
					)
			);
			assertThat(
					ex,
					hasProperty(
							"message",
							stringContainsInOrder(
									"Expected control map entry", "WRONG_TYPE", "to be", "Integer", "was", "Double"
							)
					)
			);
		}

		@Test
		void testStillString() {
			var ex = assertThrows(
					IllegalStateException.class,
					() -> Utils.expectParsedControl(
							Collections.singletonMap("WRONG_TYPE", "UNPARSED"), "WRONG_TYPE", Integer.class
					)
			);
			assertThat(
					ex,
					hasProperty(
							"message",
							stringContainsInOrder(
									"Expected control map entry", "WRONG_TYPE", "to be parsed but was still a String"
							)
					)
			);
		}

		@Test
		void testPresent() {
			var result = Utils.expectParsedControl(Collections.singletonMap("PRESENT", 2), "PRESENT", Integer.class);
			assertThat(result, is(2));
		}

		@Test
		void testPresentString() {
			var result = Utils.expectParsedControl(Collections.singletonMap("PRESENT", "X"), "PRESENT", String.class);
			assertThat(result, is("X"));
		}
	}

	@Test
	void testCompareUsing() {
		var unit = Utils.compareUsing((String s) -> s.substring(1, 2)); // Compares Strings by their second character.

		var list = new ArrayList<>(List.of("12", "21", "33"));
		list.sort(unit);

		assertThat(list, Matchers.contains("21", "12", "33"));

	}

	@Test
	void testConstMap() {
		var unit = Utils.<String, String>constMap((x) -> {
			x.put("TEST", "VALUE");
		});

		assertThat(unit, hasEntry("TEST", "VALUE"));

		assertThrows(UnsupportedOperationException.class, () -> unit.put("TEST", "CHANGED"));
		assertThrows(UnsupportedOperationException.class, () -> unit.put("ANOTHER", "VALUE"));
	}

	@Nested
	class UtilizationVector {
		@Test
		void testFromSingleValue() {
			var unit = Utils.utilizationVector(5f);
			assertThat(unit, coe(-1, 0f, 5f, 0f, 0f, 0f, 5f));
		}
	}
}
