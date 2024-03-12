package ca.bc.gov.nrs.vdyp.model;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public interface MatrixMap2<K1, K2, V> extends MatrixMap<V> {

	public default void put(K1 key1, K2 key2, V value) {
		putM(value, key1, key2);
	}

	public default V get(K1 key1, K2 key2) {
		return getM(key1, key2);
	}

	public default void addAll(Map<K1, Map<K2, V>> nestedMap) {
		addAll(nestedMap, x -> x);
	}

	public default <T> void addAll(Map<K1, Map<K2, T>> nestedMap, Function<T, V> valueMapper) {
		nestedMap.entrySet().forEach(entry1 -> {
			entry1.getValue().entrySet().forEach(entry2 -> {
				put(entry1.getKey(), entry2.getKey(), valueMapper.apply(entry2.getValue()));
			});
		});
	}

	@SuppressWarnings("unchecked")
	public default void setAll(BiFunction<K1, K2, V> mapper) {
		setAll(key -> {
			K1 key1 = (K1) key[0];
			K2 key2 = (K2) key[1];
			return mapper.apply(key1, key2);
		});
	}

	/**
	 * Cast a 2 dimension MatrixMap to MatrixMap2, wrapping it if it has 2 dimensions but does not implement the
	 * interface.
	 */
	@SuppressWarnings("unchecked")
	public static <K1, K2, V> MatrixMap2<K1, K2, V> cast(MatrixMap<V> o, Class<K1> keyClass1, Class<K2> keyClass2) {
		// TODO check compatibility of range types

		// Pass through if it's already a MatrixMap2
		if (o instanceof MatrixMap2) {
			return (MatrixMap2<K1, K2, V>) o;
		}
		// Wrap it if it's not a MatrixMap2 but has 2 dimensions
		if (o.getNumDimensions() == 3) {
			return new MatrixMap2<K1, K2, V>() {

				@Override
				public V getM(Object... params) {
					return o.getM(params);
				}

				@Override
				public void putM(V value, Object... params) {
					o.putM(value, params);
				}

				@Override
				public boolean all(Predicate<V> pred) {
					return o.all(pred);
				}

				@Override
				public List<Set<?>> getDimensions() {
					return o.getDimensions();
				}

				@Override
				public int getNumDimensions() {
					return o.getNumDimensions();
				}

				@Override
				public boolean any(Predicate<V> pred) {
					return o.any(pred);
				}

				@Override
				public void setAll(V value) {
					o.setAll(value);
				}

				@Override
				public void eachKey(Consumer<Object[]> body) {
					o.eachKey(body);
				}

				@Override
				public V remove(Object... params) {
					return o.remove(params);
				}

				@Override
				public boolean hasM(Object... params) {
					// TODO Auto-generated method stub
					return o.hasM(params);
				}
			};
		}

		// Can't cast it if it doesn't have 3 dimensions
		throw new ClassCastException("MatrixMap did not have 2 dimensions");
	}
}
