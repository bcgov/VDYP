package ca.bc.gov.nrs.vdyp.common;

import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.coe;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
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

import ca.bc.gov.nrs.vdyp.model.LayerType;
import ca.bc.gov.nrs.vdyp.model.VdypLayer;

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

	@Nested
	class UtilizationVectorAsArray {
		@Test
		void testFromSingleValue() {
			var layer = VdypLayer.build(lb -> {
				lb.polygonIdentifier("Test", 2024);
				lb.layerType(LayerType.PRIMARY);

				lb.baseArea(0.7f, 0.9f, 1.1f, 1.3f, 1.5f);

				lb.addSpecies(sb -> {
					sb.genus("B");
					sb.baseArea(0.1f, 0.2f, 0.3f, 0.4f, 0.5f);
					sb.percentGenus(40);
					sb.volumeGroup(42);
					sb.decayGroup(42);
					sb.breakageGroup(42);
				});
				lb.addSpecies(sb -> {
					sb.genus("C");
					sb.baseArea(0.6f, 0.7f, 0.8f, 0.9f, 1f);
					sb.percentGenus(60);
					sb.volumeGroup(42);
					sb.decayGroup(42);
					sb.breakageGroup(42);
				});
			});
			var unit = Utils.baArray(layer);
			assertThat(
					unit, equalTo(
							new float[][] { //
									{ 0.7f, 0.1f, 0.6f }, //
									{ 4.8f, 1.4f, 3.4f }, //
									{ 0.9f, 0.2f, 0.7f }, //
									{ 1.1f, 0.3f, 0.8f }, //
									{ 1.3f, 0.4f, 0.9f }, //
									{ 1.5f, 0.5f, 1.0f } //
							}
					)
			);
		}
	}
}
