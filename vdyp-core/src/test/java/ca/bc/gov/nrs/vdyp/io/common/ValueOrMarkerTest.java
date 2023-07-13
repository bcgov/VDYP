package ca.bc.gov.nrs.vdyp.io.common;

import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.isMarker;
import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.isValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

import ca.bc.gov.nrs.vdyp.common.ValueOrMarker;

public class ValueOrMarkerTest {

	@Test
	public void testMarker() throws Exception {
		var builder = ValueOrMarker.builder(Float.class, String.class);

		var vm = builder.marker("Test");

		assertThat(vm, isMarker(is("Test")));

		var result = vm.handle((value) -> {
			fail("Should not call value handler");
			return "Failure";
		}, (marker) -> "Success");

		assertThat(result, is("Success"));
	}

	@Test
	public void testValue() throws Exception {
		var builder = ValueOrMarker.builder(Float.class, String.class);

		var vm = builder.value(1.0f);

		assertThat(vm, isValue(is(1.0f)));

		var result = vm.handle((value) -> "Success", (marker) -> {
			fail("Should not call marker handler");
			return "Failure";
		});

		assertThat(result, is("Success"));
	}
}
