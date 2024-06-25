package ca.bc.gov.nrs.vdyp.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import ca.bc.gov.nrs.vdyp.test.VdypMatchers;

class CoefficientsTest {

	@Test
	void testGetCoe() {
		var unit = new Coefficients(new float[] { 2f, 3f, 4f }, -1);
		assertThat(unit.getCoe(-1), is(2f));
		assertThat(unit.getCoe(0), is(3f));
		assertThat(unit.getCoe(1), is(4f));
	}

	@Test
	void testGetCoeOutOfBounds() {
		var unit = new Coefficients(new float[] { 2f, 3f, 4f }, -1);
		assertThrows(ArrayIndexOutOfBoundsException.class, () -> unit.getCoe(-2));
		assertThrows(ArrayIndexOutOfBoundsException.class, () -> unit.getCoe(2));
	}

	@Test
	void testPairwiseInPlace() {
		var unit1 = new Coefficients(new float[] { 2f, 3f, 4f }, -1);
		var unit2 = new Coefficients(new float[] { 5f, 6f, 7f }, -1);
		unit1.pairwiseInPlace(unit2, (x, y) -> x + y);
		assertThat(unit1, VdypMatchers.coe(-1, 7f, 9f, 11f));
	}

	@Test
	void testPairwiseInPlaceIndexMissmatch() {
		var unit1 = new Coefficients(new float[] { 2f, 3f, 4f }, -1);
		var unit2 = new Coefficients(new float[] { 5f, 6f, 7f }, 0);
		assertThrows(IllegalArgumentException.class, () -> unit1.pairwiseInPlace(unit2, (x, y) -> x + y));
	}

	@Test
	void testPairwiseInPlaceSizeMissmatch() {
		var unit1 = new Coefficients(new float[] { 2f, 3f, 4f }, -1);
		var unit2 = new Coefficients(new float[] { 5f, 6f }, -1);
		assertThrows(IllegalArgumentException.class, () -> unit1.pairwiseInPlace(unit2, (x, y) -> x + y));
	}

	@Test
	void testPairwiseInPlaceIndexed() {
		var unit1 = new Coefficients(new float[] { 2f, 3f, 4f }, -1);
		var unit2 = new Coefficients(new float[] { 5f, 6f, 7f }, -1);
		unit1.pairwiseInPlace(unit2, (x, y, i) -> x + y + i);
		assertThat(unit1, VdypMatchers.coe(-1, 6f, 9f, 12f));
	}

	@Test
	void testPairwiseInPlaceIndexedIndexMissmatch() {
		var unit1 = new Coefficients(new float[] { 2f, 3f, 4f }, -1);
		var unit2 = new Coefficients(new float[] { 5f, 6f, 7f }, 0);
		assertThrows(IllegalArgumentException.class, () -> unit1.pairwiseInPlace(unit2, (x, y, i) -> x + y + i));
	}

	@Test
	void testPairwiseInPlaceIndexedSizeMissmatch() {
		var unit1 = new Coefficients(new float[] { 2f, 3f, 4f }, -1);
		var unit2 = new Coefficients(new float[] { 5f, 6f }, -1);
		assertThrows(IllegalArgumentException.class, () -> unit1.pairwiseInPlace(unit2, (x, y, i) -> x + y + i));
	}

	@Test
	void testPairwise() {
		var unit1 = new Coefficients(new float[] { 2f, 3f, 4f }, -1);
		var unit2 = new Coefficients(new float[] { 5f, 6f, 7f }, -1);
		var result = unit1.pairwise(unit2, (x, y) -> x + y);
		assertThat(result, VdypMatchers.coe(-1, 7f, 9f, 11f));
		assertThat(unit1, VdypMatchers.coe(-1, 2f, 3f, 4f));
	}

	@Test
	void testPairwiseIndexMissmatch() {
		var unit1 = new Coefficients(new float[] { 2f, 3f, 4f }, -1);
		var unit2 = new Coefficients(new float[] { 5f, 6f, 7f }, 0);
		assertThrows(IllegalArgumentException.class, () -> unit1.pairwise(unit2, (x, y) -> x + y));
	}

	@Test
	void testPairwiseSizeMissmatch() {
		var unit1 = new Coefficients(new float[] { 2f, 3f, 4f }, -1);
		var unit2 = new Coefficients(new float[] { 5f, 6f }, -1);
		assertThrows(IllegalArgumentException.class, () -> unit1.pairwise(unit2, (x, y) -> x + y));
	}

	@Test
	void testPairwiseIndexed() {
		var unit1 = new Coefficients(new float[] { 2f, 3f, 4f }, -1);
		var unit2 = new Coefficients(new float[] { 5f, 6f, 7f }, -1);
		var result = unit1.pairwise(unit2, (x, y, i) -> x + y + i);
		assertThat(result, VdypMatchers.coe(-1, 6f, 9f, 12f));
		assertThat(unit1, VdypMatchers.coe(-1, 2f, 3f, 4f));
	}

	@Test
	void testPairwiseIndexedIndexMissmatch() {
		var unit1 = new Coefficients(new float[] { 2f, 3f, 4f }, -1);
		var unit2 = new Coefficients(new float[] { 5f, 6f, 7f }, 0);
		assertThrows(IllegalArgumentException.class, () -> unit1.pairwise(unit2, (x, y, i) -> x + y + i));
	}

	@Test
	void testPairwiseIndexedSizeMissmatch() {
		var unit1 = new Coefficients(new float[] { 2f, 3f, 4f }, -1);
		var unit2 = new Coefficients(new float[] { 5f, 6f }, -1);
		assertThrows(IllegalArgumentException.class, () -> unit1.pairwise(unit2, (x, y, i) -> x + y + i));
	}

	@Test
	void testScalarInPlace() {
		var unit1 = new Coefficients(new float[] { 2f, 3f, 4f }, -1);
		unit1.scalarInPlace(x -> x * 2);
		assertThat(unit1, VdypMatchers.coe(-1, 4f, 6f, 8f));
	}

	@Test
	void testScalarInPlaceIndexed() {
		var unit1 = new Coefficients(new float[] { 2f, 3f, 4f }, -1);
		unit1.scalarInPlace((x, i) -> x * 2 + i);
		assertThat(unit1, VdypMatchers.coe(-1, 3f, 6f, 9f));
	}

	@Test
	void testScalar() {
		var unit1 = new Coefficients(new float[] { 2f, 3f, 4f }, -1);
		var result = unit1.scalar(x -> x * 2);
		assertThat(result, VdypMatchers.coe(-1, 4f, 6f, 8f));
		assertThat(unit1, VdypMatchers.coe(-1, 2f, 3f, 4f));
	}

	@Test
	void testScalarIndexed() {
		var unit1 = new Coefficients(new float[] { 2f, 3f, 4f }, -1);
		var result = unit1.scalar((x, i) -> x * 2 + i);
		assertThat(result, VdypMatchers.coe(-1, 3f, 6f, 9f));
		assertThat(unit1, VdypMatchers.coe(-1, 2f, 3f, 4f));
	}

	@Test
	void testScalarInPlaceSpecificIndex() {
		var unit1 = new Coefficients(new float[] { 2f, 3f, 4f }, -1);
		unit1.scalarInPlace(1, x -> x * 2);
		assertThat(unit1, VdypMatchers.coe(-1, 2f, 3f, 8f));
	}

	@Test
	void testScalarInPlaceSpecificIndexOutOfBounds() {
		var unit = new Coefficients(new float[] { 2f, 3f, 4f }, -1);
		assertThrows(ArrayIndexOutOfBoundsException.class, () -> unit.scalarInPlace(-2, x -> x * 2));
		assertThrows(ArrayIndexOutOfBoundsException.class, () -> unit.scalarInPlace(2, x -> x * 2));
	}

}
