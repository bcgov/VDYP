package ca.bc.gov.nrs.vdyp.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class GenusDistributionSetTest {

	private static GenusDefinition a;
	private static GenusDefinition b;

	@BeforeAll
	static void beforeAll() {
		a = new GenusDefinition("A", Optional.empty(), "Genus A");
		b = new GenusDefinition("B", Optional.of(2), "Genus B");
	}

	@Test
	void testGenusDistributionConstruction() {
		assertThrows(IllegalArgumentException.class, () -> new GenusDistribution(1, a, null));
		assertThrows(IllegalArgumentException.class, () -> new GenusDistribution(1, a, -8.0f));
		assertThrows(IllegalArgumentException.class, () -> new GenusDistribution(1, a, 120.0f));
		assertThrows(IllegalArgumentException.class, () -> new GenusDistribution(1, null, 50.0f));
	}

	@Test
	void testGenusDefinitionComparison() {
		assertNotEquals(a, null);

		assertNotEquals(a, b);
		assertEquals(a.hashCode(), a.hashCode());
		assertTrue(a.compareTo(a) == 0);
		assertTrue(a.compareTo(b) < 0);
		assertTrue(b.compareTo(a) > 0);
		assertTrue(a.compareTo(null) > 0);
	}

	@Test
	void testGenusDistributionComparison() {
		var gd1 = new GenusDistribution(0, a, 70.0f);
		var gd2 = new GenusDistribution(0, b, 30.0f);

		assertNotEquals(gd1, gd2);
		assertNotEquals(gd1, null);
		assertNotEquals(null, gd2);

		assertTrue(gd1.compareTo(gd1) == 0);
		assertTrue(gd1.compareTo(gd2) < 0);
		assertTrue(gd2.compareTo(gd1) > 0);
		assertTrue(gd1.compareTo(null) > 0);

		assertEquals(gd1.hashCode(), gd1.hashCode());
	}

	@Test
	void testGenusDistribution() {
		var gd1 = new GenusDistribution(0, a, 70.0f);
		assertThat(gd1.getGenus(), is(a));
		assertThat(gd1.getIndex(), is(0));
		assertThat(gd1.getPercentage(), is(70.0f));
	}

	@Test
	void testGenusDistributionSetConstruction() {
		var gd1 = new GenusDistribution(0, a, 70.0f);
		var gd1a = new GenusDistribution(1, a, 30.0f);
		var gd2 = new GenusDistribution(1, b, 30.0f);
		var gd2a = new GenusDistribution(1, b, 25.0f);

		assertNotNull(new GenusDistributionSet(3, List.of(gd1, gd2)));
		assertNotNull(new GenusDistributionSet(1, List.of(gd1, gd2)));
		assertThrows(IllegalArgumentException.class, () -> new GenusDistributionSet(1, List.of(gd1, gd1a)));

		assertThrows(IllegalArgumentException.class, () -> new GenusDistributionSet(0, List.of(gd1, gd2)));
		assertThrows(IllegalArgumentException.class, () -> new GenusDistributionSet(0, List.of(gd1, gd2a)));
	}

	@Test
	void testGenusDistributionSetAccessor() {
		var gd1 = new GenusDistribution(0, a, 70.0f);
		var gd2 = new GenusDistribution(1, b, 30.0f);

		var gds = new GenusDistributionSet(3, List.of(gd1, gd2));

		assertThat(gds.getSpeciesDistribution(0), isA(Optional.class));
		assertThat(gds.getSpeciesDistribution(0).get(), is(gd1));
		assertThat(gds.getSpeciesDistribution(1), isA(Optional.class));
		assertThat(gds.getSpeciesDistribution(1).get(), is(gd2));
		assertThat(gds.getSpeciesDistribution(2), is(Optional.empty()));
		assertThat(gds.getSpeciesDistribution(3), is(Optional.empty()));
		assertThrows(IllegalArgumentException.class, () -> gds.getSpeciesDistribution(4));
	}
}
