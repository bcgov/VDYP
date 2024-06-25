package ca.bc.gov.nrs.vdyp.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

public class MatrixMap4Impl<K1, K2, K3, K4, V> extends MatrixMapImpl<V> implements MatrixMap4<K1, K2, K3, K4, V> {

	@SuppressWarnings("unchecked")
	public MatrixMap4Impl(
			Collection<K1> dimension1, Collection<K2> dimension2, Collection<K3> dimension3, Collection<K4> dimension4,
			QuadFunction<K1, K2, K3, K4, V> defaultMapper
	) {
		super(k -> {
			var k1 = (K1) k[0];
			var k2 = (K2) k[1];
			var k3 = (K3) k[2];
			var k4 = (K4) k[3];
			return defaultMapper.apply(k1, k2, k3, k4);
		}, Arrays.asList(dimension1, dimension2, dimension3, dimension4));
	}

	@FunctionalInterface
	public static interface QuadFunction<P1, P2, P3, P4, V> {
		public V apply(P1 p1, P2 p2, P3 p3, P4 p4);
	}

	/**
	 * Default mapper function that maps all keys to an empty Optional
	 */
	public static <K1, K2, K3, K4, V> QuadFunction<K1, K2, K3, K4, Optional<V>> emptyDefault() {
		return (k1, k2, k3, k4) -> Optional.empty();
	}
}
