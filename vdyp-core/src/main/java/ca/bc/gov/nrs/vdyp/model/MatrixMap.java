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
 * A mapping from the cartesian product of a set of arbitrary identifiers to a
 * value.
 *
 * @author Kevin Smith, Vivid Solutions
 *
 * @param <T>
 */
public interface MatrixMap<T> {

	public Optional<T> getM(Object... params);

	public void putM(T value, Object... params);

	public boolean all(Predicate<T> pred);

	public boolean any(Predicate<T> pred);

	public void eachKey(Consumer<Object[]> body);

	public List<Set<?>> getDimensions();

	default public int getNumDimensions() {
		return getDimensions().size();
	}

	default public boolean isFull() {
		return all(x -> x != null);
	}

	default public boolean isEmpty() {
		return all(x -> x == null);
	}

	/**
	 * Wraps a 1 dimensional MatrixMap as a regular Java Map.
	 */
	public static <CK1, CV> Map<CK1, CV> cast(MatrixMap<CV> o, Class<CK1> keyClass1) {
		// TODO check compatibility of range types

		// Wrap it if it's not a MatrixMap3 but has 1 dimension
		if (o.getNumDimensions() == 1) {
			return new AbstractMap<CK1, CV>() {

				@SuppressWarnings({ "unchecked", "rawtypes" })
				@Override
				public Set<Entry<CK1, CV>> entrySet() {
					return (Set) o.getDimensions().get(0).stream().filter(k -> o.getM(k).isPresent())
							.collect(Collectors.toMap(k -> k, k -> o.getM(k).get())).entrySet();
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
					return o.any(v -> value.equals(v));
				}

				@Override
				public boolean containsKey(Object key) {
					return o.getDimensions().get(0).contains(key);
				}

				@Override
				public CV get(Object key) {
					return o.getM(key).orElseGet(() -> null);
				}

				@Override
				public CV put(CK1 key, CV value) {
					var old = get(key);
					o.putM(value, key);
					return old;
				}

				@SuppressWarnings("unchecked")
				@Override
				public CV remove(Object key) {
					return put((CK1) key, null);
				}

				@SuppressWarnings("unchecked")
				@Override
				public Set<CK1> keySet() {
					return (Set<CK1>) o.getDimensions().get(0);
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
	};

	/**
	 * Set all cells to the generated value
	 *
	 * @param value
	 */
	public default void setAll(Function<Object[], T> generator) {
		eachKey((k) -> {
			putM(generator.apply(k), k);
		});
	};
}
