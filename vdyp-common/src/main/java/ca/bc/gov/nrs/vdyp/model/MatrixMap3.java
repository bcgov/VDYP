package ca.bc.gov.nrs.vdyp.model;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

public interface MatrixMap3<K1, K2, K3, V> extends MatrixMap<V> {

	public default void put(K1 key1, K2 key2, K3 key3, V value) {
		putM(value, key1, key2, key3);
	}

	public default V get(K1 key1, K2 key2, K3 key3) {
		return getM(key1, key2, key3);
	}

	/**
	 * Cast a 3 dimension MatrixMap to MatrixMap3, wrapping it if it has 3 dimensions but does not implement the
	 * interface.
	 */
	@SuppressWarnings("unchecked")
	public static <K1, K2, K3, V> MatrixMap3<K1, K2, K3, V>
			cast(MatrixMap<V> o, Class<K1> keyClass1, Class<K2> keyClass2, Class<K3> keyClass3) {
		// TODO check compatibility of range types

		// Pass through if it's already a MatrixMap3
		if (o instanceof MatrixMap3) {
			return (MatrixMap3<K1, K2, K3, V>) o;
		}
		// Wrap it if it's not a MatrixMap3 but has 3 dimensions
		if (o.getNumDimensions() == 3) {
			return new MatrixMap3<K1, K2, K3, V>() {

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
					return o.hasM(params);
				}

			};
		}

		// Can't cast it if it doesn't have 3 dimensions
		throw new ClassCastException("MatrixMap did not have 3 dimensions");
	}
}
