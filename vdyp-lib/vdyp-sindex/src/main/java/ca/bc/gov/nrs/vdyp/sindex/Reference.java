package ca.bc.gov.nrs.vdyp.sindex;

import java.util.Optional;

public class Reference<T> {

	private Optional<T> value;

	public Reference(T initialValue) {
		value = Optional.of(initialValue);
	}

	public Reference() {
		value = Optional.empty();
	}

	public void set(T newValue) {
		value = Optional.of(newValue);
	}

	public T get() {
		if (value.isPresent()) {
			return value.get();
		} else {
			throw new IllegalStateException("Attempting to get the value of a Reference that hasn't one");
		}
	}

	public boolean isPresent() {
		return value.isPresent();
	}
}
