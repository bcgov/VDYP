package ca.bc.gov.nrs.vdyp.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

public class MatrixMap3Impl<K1, K2, K3, V> extends MatrixMapImpl<V> implements MatrixMap3<K1, K2, K3, V> {

	@SuppressWarnings("unchecked")
	public MatrixMap3Impl(
			Collection<K1> dimension1, Collection<K2> dimension2, Collection<K3> dimension3,
			TriFunction<K1, K2, K3, V> defaultMapper
	) {
		super(k -> {
			var k1 = (K1) k[0];
			var k2 = (K2) k[1];
			var k3 = (K3) k[2];
			return defaultMapper.apply(k1, k2, k3);
		}, Arrays.asList(dimension1, dimension2, dimension3));
	}

	@FunctionalInterface
	public static interface TriFunction<P1, P2, P3, V> {
		public V apply(P1 p1, P2 p2, P3 p3);
	}

	/**
	 * Default mapper function that maps all keys to an empty Optional
	 */
	public static <K1, K2, K3, V> TriFunction<K1, K2, K3, Optional<V>> emptyDefault() {
		return (k1, k2, k3) -> Optional.empty();
	}

}
