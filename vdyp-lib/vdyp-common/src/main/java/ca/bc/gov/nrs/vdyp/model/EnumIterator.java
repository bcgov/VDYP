package ca.bc.gov.nrs.vdyp.model;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * This class supports iterating over (portions of) enumerations. It is most useful for enumerations that contain some
 * values that need to be included but are not formally part of the item set such as "not an element" entries and the
 * like.
 *
 * @param <T> the enum type
 */
public class EnumIterator<T extends Enum<T>> implements Iterator<T> {

	private final int lastIndex;
	private int currentIndex;
	private final T[] valueArray;

	public EnumIterator(T[] values) {

		this(values, values[0], values[values.length - 1]);
	}

	public EnumIterator(T[] values, T first, T last) {

		if (values == null) {
			throw new IllegalArgumentException("values parameter to EnumIterator may not be null");
		}

		this.valueArray = values;
		this.currentIndex = first.ordinal();
		this.lastIndex = last.ordinal() + 1;
	}

	@Override
	public boolean hasNext() {
		return currentIndex < lastIndex;
	}

	@Override
	public T next() {
		if (currentIndex == lastIndex) {
			throw new NoSuchElementException();
		} else {
			T result = valueArray[currentIndex];

			currentIndex += 1;

			return result;
		}
	}
}
