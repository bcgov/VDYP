package ca.bc.gov.nrs.vdyp.si32;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

import ca.bc.gov.nrs.vdyp.model.EnumIterator;
import ca.bc.gov.nrs.vdyp.si32.cfs.CfsTreeSpecies;

class SI32EnumIteratorTest {

	@Test
	void testFullIteration() {
		EnumIterator<CfsTreeSpecies> i = new EnumIterator<>(CfsTreeSpecies.values());

		int numberSeen = 0;
		while (i.hasNext()) {
			i.next();
			numberSeen += 1;
		}

		assertThat(numberSeen, is(CfsTreeSpecies.values().length));
	}

	@Test
	void testPartialIteration() {
		EnumIterator<CfsTreeSpecies> i = new EnumIterator<>(
				CfsTreeSpecies.values(), CfsTreeSpecies.forIndex(3), CfsTreeSpecies.forIndex(5)
		);

		int numberSeen = 0;
		while (i.hasNext()) {
			i.next();
			numberSeen += 1;
		}

		assertThat(numberSeen, is(3));
	}

	@Test
	void testEmptyIteration() {
		EnumIterator<CfsTreeSpecies> i = new EnumIterator<>(
				CfsTreeSpecies.values(), CfsTreeSpecies.ALDER_RED, CfsTreeSpecies.ALDER
		);

		int numberSeen = 0;
		while (i.hasNext()) {
			i.next();
			numberSeen += 1;
		}

		assertThat(numberSeen, is(0));
	}

	@Test
	void testErrors() {

		try {
			new EnumIterator<>(null, CfsTreeSpecies.ALDER_RED, CfsTreeSpecies.ALDER);
			fail();
		} catch (IllegalArgumentException e) {
			// expected
		}

		try {
			new EnumIterator<>(CfsTreeSpecies.values(), null, CfsTreeSpecies.ALDER);
			fail();
		} catch (NullPointerException e) {
			// expected
		}

		try {
			new EnumIterator<>(CfsTreeSpecies.values(), CfsTreeSpecies.ALDER, null);
			fail();
		} catch (NullPointerException e) {
			// expected
		}
	}
}
