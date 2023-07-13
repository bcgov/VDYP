package ca.bc.gov.nrs.vdyp.model;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

public interface MatrixMap2<K1, K2, V> extends MatrixMap<V> {

	default public void put(K1 key1, K2 key2, V value) {
		putM(value, key1, key2);
	}

	default public Optional<V> get(K1 key1, K2 key2) {
		return getM(key1, key2);
	}

	/**
	 * Cast a 2 dimension MatrixMap to MatrixMap2, wrapping it if it has 2
	 * dimensions but does not implement the interface.
	 */
	@SuppressWarnings("unchecked")
	public static <CK1, CK2, CV> MatrixMap2<CK1, CK2, CV>
			cast(MatrixMap<CV> o, Class<CK1> keyClass1, Class<CK2> keyClass2) {
		// TODO check compatibility of range types

		// Pass through if it's already a MatrixMap2
		if (o instanceof MatrixMap2) {
			return (MatrixMap2<CK1, CK2, CV>) o;
		}
		// Wrap it if it's not a MatrixMap2 but has 2 dimensions
		if (o.getNumDimensions() == 3) {
			return new MatrixMap2<CK1, CK2, CV>() {

				@Override
				public Optional<CV> getM(Object... params) {
					return o.getM(params);
				}

				@Override
				public void putM(CV value, Object... params) {
					o.putM(value, params);
				}

				@Override
				public boolean all(Predicate<CV> pred) {
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
				public boolean any(Predicate<CV> pred) {
					return o.any(pred);
				}

				@Override
				public void setAll(CV value) {
					o.setAll(value);
				}

				@Override
				public void eachKey(Consumer<Object[]> body) {
					o.eachKey(body);
				}
			};
		}

		// Can't cast it if it doesn't have 3 dimensions
		throw new ClassCastException("MatrixMap did not have 2 dimensions");
	}
}
