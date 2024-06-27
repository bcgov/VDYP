package ca.bc.gov.nrs.vdyp.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.iterableWithSize;

import org.junit.jupiter.api.Test;

class CommonDataTest {

	@Test
	void test() {
		assertThat(CommonData.ITG_PURE, aMapWithSize(16));
		assertThat(
				CommonData.ITG_PURE.keySet(),
				containsInAnyOrder(
						"AC", "AT", "B", "C", "D", "E", "F", "H", "L", "MB", "PA", "PL", "PW", "PY", "S", "Y"
				)
		);

		assertThat(CommonData.HARDWOODS, containsInAnyOrder("AC", "AT", "D", "E", "MB"));

		for (var m : CommonData.PRIMARY_SPECIES_TO_COMBINE) {
			assertThat(m, iterableWithSize(2));
		}
	}
}
