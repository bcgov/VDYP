package ca.bc.gov.nrs.vdyp.common;

import java.util.Objects;
import java.util.function.Supplier;

import javax.annotation.Nullable;

/**
 * A lazily computed value.
 *
 * @author Kevin Smith, Vivid Solutions
 *
 * @param <T>
 */
public class LazyValue<T> implements Supplier<T> {

	public LazyValue(Supplier<T> compute) {
		this.compute = compute;
	}

	@Nullable
	private T value = null;

	private final Supplier<T> compute;

	@Override
	public T get() {
		if (Objects.isNull(value)) {
			synchronized (this) {
				if (Objects.isNull(value)) {
					value = compute.get();
				}
			}
		}
		return value;
	}

}
