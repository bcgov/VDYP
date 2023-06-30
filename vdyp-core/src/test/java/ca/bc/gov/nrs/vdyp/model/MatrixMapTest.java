package ca.bc.gov.nrs.vdyp.model;

import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.notPresent;
import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.present;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MatrixMapTest {

	@Test
	public void testContruct() {
		var dim1 = Arrays.asList("a", "b");
		var dim2 = Arrays.asList(1, 2);
		var dims = Arrays.asList(dim1, dim2);
		new MatrixMapImpl<Character>(dims);
	}

	@Test
	public void testContructNoDimensionsFails() {
		List<List<Object>> dims = Collections.emptyList();
		Assertions.assertThrows(IllegalArgumentException.class, () -> new MatrixMapImpl<Character>(dims));
	}

	@Test
	public void testContructEmptyDimensionsFails() {
		var dim1 = Collections.emptyList();
		var dims = Arrays.asList(dim1);
		Assertions.assertThrows(IllegalArgumentException.class, () -> new MatrixMapImpl<Character>(dims));
	}

	@Test
	public void testNewMapIsEmpty() {
		var dim1 = Arrays.asList("a", "b");
		var dim2 = Arrays.asList(1, 2);
		var dims = Arrays.asList(dim1, dim2);
		var result = new MatrixMapImpl<Character>(dims);
		assertThat(result, hasProperty("empty", is(true)));
		assertThat(result, hasProperty("full", is(false)));
	}

	@Test
	public void testDefaultIsEmpty() {
		var dim1 = Arrays.asList("a", "b");
		var dim2 = Arrays.asList(1, 2);
		var dims = Arrays.asList(dim1, dim2);
		var map = new MatrixMapImpl<Character>(dims);

		var result = map.getM("a", 2);

		assertThat(result, notPresent());
	}

	@Test
	public void testCanRetrieveAValue() {
		var dim1 = Arrays.asList("a", "b");
		var dim2 = Arrays.asList(1, 2);
		var dims = Arrays.asList(dim1, dim2);
		var map = new MatrixMapImpl<Character>(dims);

		map.putM('Z', "a", 2);
		var result = map.getM("a", 2);

		assertThat(result, present(is('Z')));
	}

	@Test
	public void testWithOneValueIsNeitherFullNorEmpty() {
		var dim1 = Arrays.asList("a", "b");
		var dim2 = Arrays.asList(1, 2);
		var dims = Arrays.asList(dim1, dim2);
		var map = new MatrixMapImpl<Character>(dims);

		map.putM('Z', "a", 2);

		assertThat(map, hasProperty("full", is(false)));
		assertThat(map, hasProperty("empty", is(false)));
	}

	@Test
	public void testGetIndex() {
		var dim1 = Arrays.asList("a", "b");
		var dim2 = Arrays.asList(1, 2);
		var dims = Arrays.asList(dim1, dim2);
		var map = new MatrixMapImpl<Character>(dims);

		assertThat(map.getIndex("a", 1), present(is(0)));
		assertThat(map.getIndex("b", 1), present(is(1)));
		assertThat(map.getIndex("a", 2), present(is(2)));
		assertThat(map.getIndex("b", 2), present(is(3)));
	}

	@Test
	public void testFullMap() {
		var dim1 = Arrays.asList("a", "b");
		var dim2 = Arrays.asList(1, 2);
		var dims = Arrays.asList(dim1, dim2);
		var map = new MatrixMapImpl<Character>(dims);

		map.putM('W', "a", 1);
		map.putM('X', "a", 2);
		map.putM('Y', "b", 1);
		map.putM('Z', "b", 2);

		assertThat(map, hasProperty("full", is(true)));
		assertThat(map, hasProperty("empty", is(false)));

		assertThat(map.getM("a", 1), present(is('W')));
		assertThat(map.getM("a", 2), present(is('X')));
		assertThat(map.getM("b", 1), present(is('Y')));
		assertThat(map.getM("b", 2), present(is('Z')));
	}

	@Test
	public void testSetAll() {
		var dim1 = Arrays.asList("a", "b");
		var dim2 = Arrays.asList(1, 2);
		var dims = Arrays.asList(dim1, dim2);
		var map = new MatrixMapImpl<Character>(dims);

		map.putM('W', "a", 1);
		map.putM('X', "a", 2);
		map.putM('Y', "b", 1);

		map.setAll('A');

		assertThat(map, hasProperty("full", is(true)));
		assertThat(map, hasProperty("empty", is(false)));

		assertThat(map.getM("a", 1), present(is('A')));
		assertThat(map.getM("a", 2), present(is('A')));
		assertThat(map.getM("b", 1), present(is('A')));
		assertThat(map.getM("b", 2), present(is('A')));
	}

	@Test
	public void testEachKey() {
		var dim1 = Arrays.asList("a", "b");
		var dim2 = Arrays.asList(1, 2);
		var dims = Arrays.asList(dim1, dim2);
		var map = new MatrixMapImpl<Character>(dims);

		var result = new ArrayList<Object[]>();
		map.eachKey(result::add);
		assertThat(
				result,
				containsInAnyOrder(
						arrayContaining("a", 1), arrayContaining("b", 1), arrayContaining("a", 2),
						arrayContaining("b", 2)
				)
		);
	}

}
