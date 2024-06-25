package ca.bc.gov.nrs.vdyp.common_calculators;

import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.closeTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

class BaseAreaTreeDensityDiameterTest {

	@Test
	void testTPH() {
		var tph = BaseAreaTreeDensityDiameter.treesPerHectare(4f, 20f);
		assertThat(tph, closeTo(127.324f));
	}

	@Test
	void testTPHWhereBAIsZero() {
		var tph = BaseAreaTreeDensityDiameter.treesPerHectare(0f, 4f);
		assertThat(tph, is(0f));
	}

	@Test
	void testDQ() {
		var dq = BaseAreaTreeDensityDiameter.quadMeanDiameter(4f, 127.324f);
		assertThat(dq, closeTo(20f));
	}

	@Test
	void testDQWhereBAIsZero() {
		var dq = BaseAreaTreeDensityDiameter.quadMeanDiameter(0f, 127.324f);
		assertThat(dq, is(0f));
	}

	@Test
	void testDQWhereTPHIsZero() {
		var dq = BaseAreaTreeDensityDiameter.quadMeanDiameter(4f, 0f);
		assertThat(dq, is(0f));
	}

	@Test
	void testDQWhereBAIsBig() {
		var dq = BaseAreaTreeDensityDiameter.quadMeanDiameter(10_000_0000f, 127.324f);
		assertThat(dq, is(0f));
	}

	@Test
	void testDQWhereTPHIsBig() {
		var dq = BaseAreaTreeDensityDiameter.quadMeanDiameter(4f, 10_000_0000f);
		assertThat(dq, is(0f));
	}

	@Test
	void testDQWhereBAIsNan() {
		var dq = BaseAreaTreeDensityDiameter.quadMeanDiameter(Float.NaN, 127.324f);
		assertThat(dq, is(0f));
	}

	@Test
	void testDQWhereTPHIsNan() {
		var dq = BaseAreaTreeDensityDiameter.quadMeanDiameter(4f, Float.NaN);
		assertThat(dq, is(0f));
	}

}
