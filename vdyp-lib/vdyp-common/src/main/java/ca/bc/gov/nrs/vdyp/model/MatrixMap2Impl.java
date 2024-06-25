package ca.bc.gov.nrs.vdyp.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.function.BiFunction;

public class MatrixMap2Impl<K1, K2, V> extends MatrixMapImpl<V> implements MatrixMap2<K1, K2, V> {

	@SuppressWarnings("unchecked")
	public MatrixMap2Impl(Collection<K1> dimension1, Collection<K2> dimension2, BiFunction<K1, K2, V> defaultMapper) {

		super(k -> {
			var k1 = (K1) k[0];
			var k2 = (K2) k[1];
			return defaultMapper.apply(k1, k2);
		}, Arrays.asList(dimension1, dimension2));
	}

	/**
	 * Default mapper function that maps all keys to an empty Optional
	 */
	public static <K1, K2, V> BiFunction<K1, K2, Optional<V>> emptyDefault() {
		return (k1, k2) -> Optional.empty();
	}
}
