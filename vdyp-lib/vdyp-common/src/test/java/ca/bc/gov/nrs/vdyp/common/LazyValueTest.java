package ca.bc.gov.nrs.vdyp.common;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.function.Supplier;

import org.easymock.EasyMock;
import org.junit.jupiter.api.Test;

class LazyValueTest {

	@Test
	void testSimple() {
		Supplier<Integer> supplier = EasyMock.createMock(Supplier.class);
		EasyMock.expect(supplier.get()).andReturn(42).once();
		EasyMock.replay(supplier);
		var unit = new LazyValue<>(supplier);
		assertThat(unit.get(), is(42));
		EasyMock.verify(supplier);
	}

	@Test
	void testOnlyCallsSupplierOnceForMultipleCalls() {
		Supplier<Integer> supplier = EasyMock.createMock(Supplier.class);
		EasyMock.expect(supplier.get()).andReturn(42).once();
		EasyMock.replay(supplier);
		var unit = new LazyValue<>(supplier);
		assertThat(unit.get(), is(42));
		assertThat(unit.get(), is(42));
		EasyMock.verify(supplier);
	}

	@Test
	void testDoesNotCallSupplierUntilGet() {
		Supplier<Integer> supplier = EasyMock.createMock(Supplier.class);
		EasyMock.replay(supplier);
		var unit = new LazyValue<>(supplier);
		EasyMock.verify(supplier);
		EasyMock.reset(supplier);
		EasyMock.expect(supplier.get()).andReturn(42).once();
		EasyMock.replay(supplier);
		assertThat(unit.get(), is(42));
		EasyMock.verify(supplier);
	}
}
