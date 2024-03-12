package ca.bc.gov.nrs.vdyp.common;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * A value that can be set once but not changed. Subsequent attempts to change it are ignored silently.
 *
 * @author Kevin Smith, Vivid Solutions
 *
 * @param <T>
 */
public class HoldFirst<T> {

	private Optional<T> value = Optional.empty();

	/**
	 * Sets the value if it has not been set, does nothing otherwise.
	 */
	public void set(T value) {
		if (this.value.isEmpty())
			this.value = Optional.of(value);
	}

	/**
	 * Returns the value if it has been set, empty otherwise.
	 *
	 * @return
	 */
	public Optional<T> get() {
		return value;
	}

	/**
	 * Sets the value by calling the supplier if it has not been set already. Returns the value.
	 */
	public T get(Supplier<T> source) {
		if (value.isEmpty()) {
			this.value = Optional.of(source.get());
		}
		return this.value.get();
	}
}
