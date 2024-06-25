package ca.bc.gov.nrs.vdyp.model;

import static ca.bc.gov.nrs.vdyp.test.TestUtils.assumeThat;
import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.present;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class UtilizationClassTest {

	@ParameterizedTest
	@EnumSource(UtilizationClass.class)
	void testRoundTripByIndex(UtilizationClass uc) {
		assertThat(UtilizationClass.getByIndex(uc.index), is(uc));
	}

	@ParameterizedTest
	@EnumSource(UtilizationClass.class)
	void testRoundTripByIndexString(UtilizationClass uc) {
		assertThat(UtilizationClass.getByIndex(Integer.toString(uc.index)), is(uc));
	}

	@ParameterizedTest
	@EnumSource(UtilizationClass.class)
	void previousOfNextIsSelf(UtilizationClass uc) {
		assumeThat(uc.next(), present(Matchers.anything()));
		assertThat(uc.next().get().previous(), present(is(uc)));
	}

	@ParameterizedTest
	@EnumSource(UtilizationClass.class)
	void nextOfPreviousIsSelf(UtilizationClass uc) {
		assumeThat(uc.previous(), present(Matchers.anything()));
		assertThat(uc.previous().get().next(), present(is(uc)));
	}

	@Test
	void dontAcceptBadIndex() throws Exception {
		assertThat(assertThrows(IllegalArgumentException.class, () -> {
			UtilizationClass.getByIndex(-2);
		}), hasProperty("message", is("UtilizationClass index -2 is not recognized")));
		assertThat(assertThrows(IllegalArgumentException.class, () -> {
			UtilizationClass.getByIndex(5);
		}), hasProperty("message", is("UtilizationClass index 5 is not recognized")));

	}

	@Test
	void dontAcceptBadIndexString() throws Exception {
		assertThat(assertThrows(IllegalArgumentException.class, () -> {
			UtilizationClass.getByIndex("-2");
		}), hasProperty("message", is("UtilizationClass index -2 is not recognized")));
		assertThat(assertThrows(IllegalArgumentException.class, () -> {
			UtilizationClass.getByIndex("5");
		}), hasProperty("message", is("UtilizationClass index 5 is not recognized")));
		assertThat(assertThrows(IllegalArgumentException.class, () -> {
			UtilizationClass.getByIndex("");
		}), hasProperty("message", is("UtilizationClass index  is not recognized")));
		assertThat(assertThrows(IllegalArgumentException.class, () -> {
			UtilizationClass.getByIndex(" ");
		}), hasProperty("message", is("UtilizationClass index   is not recognized")));
		assertThat(assertThrows(IllegalArgumentException.class, () -> {
			UtilizationClass.getByIndex("NOTANUMBER");
		}), hasProperty("message", is("UtilizationClass index NOTANUMBER is not recognized")));

	}
}
