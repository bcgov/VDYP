package ca.bc.gov.nrs.vdyp.model;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * A mapping from the cartesian product of a set of arbitrary identifiers to a value.
 *
 * @author Kevin Smith, Vivid Solutions
 *
 * @param <T>
 */
public interface MatrixMap<T> {

	public T getM(Object... params);

	public boolean hasM(Object... params);

	public void putM(T value, Object... params);

	public boolean all(Predicate<T> pred);

	public boolean any(Predicate<T> pred);

	public void eachKey(Consumer<Object[]> body);

	public List<Set<?>> getDimensions();

	public default int getNumDimensions() {
		return getDimensions().size();
	}

	public default boolean isFull() {
		return all(x -> x != null);
	}

	public default boolean isEmpty() {
		return all(x -> x == null);
	}

	public T remove(Object... params);

	/**
	 * Wraps a 1 dimensional MatrixMap as a regular Java Map.
	 */
	public static <K1, V> Map<K1, V> cast(MatrixMap<V> o) {
		return cast(o, x -> x, x -> x);
	}

	/**
	 * Wraps a 1 dimensional MatrixMap as a regular Java Map.
	 */
	public static <K1, V, T> Map<K1, T> cast(MatrixMap<V> o, Function<V, T> toMapValue, Function<T, V> toMatrixValue) {
		// TODO check compatibility of range types

		// Wrap it if it's not a MatrixMap3 but has 1 dimension
		if (o.getNumDimensions() == 1) {
			return new AbstractMap<K1, T>() {

				@SuppressWarnings({ "unchecked", "rawtypes" })
				@Override
				public Set<Entry<K1, T>> entrySet() {
					return (Set) o.getDimensions().get(0).stream().filter(k -> o.getM(k) != null)
							.collect(Collectors.toMap(k -> k, k -> o.getM(k))).entrySet();
				}

				@Override
				public int size() {
					return o.getDimensions().get(0).size();
				}

				@Override
				public boolean isEmpty() {
					return o.isEmpty();
				}

				@Override
				public boolean containsValue(Object value) {
					@SuppressWarnings("unchecked")
					var expected = toMatrixValue.apply((T) value);
					return o.any(expected::equals);
				}

				@Override
				public boolean containsKey(Object key) {
					return o.getDimensions().get(0).contains(key);
				}

				@Override
				public T get(Object key) {
					if (o.getDimensions().get(0).contains(key)) {
						return toMapValue.apply(o.getM(key));
					}
					return null;
				}

				@Override
				public T put(K1 key, T value) {
					var old = get(key);
					o.putM(toMatrixValue.apply(value), key);
					return old;
				}

				@Override
				public T remove(Object key) {
					return toMapValue.apply(o.remove(key));
				}

				@SuppressWarnings("unchecked")
				@Override
				public Set<K1> keySet() {
					return (Set<K1>) o.getDimensions().get(0);
				}

			};
		}

		// Can't cast it if it doesn't have 1 dimensions
		throw new ClassCastException("MatrixMap did not have 1 dimension");
	}

	/**
	 * Set all cells to the given value
	 *
	 * @param value
	 */
	public default void setAll(T value) {
		setAll(k -> value);
	}

	/**
	 * Set all cells to the generated value
	 *
	 * @param value
	 */
	public default void setAll(Function<Object[], T> generator) {
		eachKey((k) -> {
			putM(generator.apply(k), k);
		});
	}

	public static <U> Optional<U> safeGet(MatrixMap<Optional<U>> map, Object... params) {
		if (map.hasM(params)) {
			return map.getM(params);
		}
		return Optional.empty();
	}
}
