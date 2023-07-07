package ca.bc.gov.nrs.vdyp.model;

import java.util.Optional;

public interface MatrixMap4<K1, K2, K3, K4, V> extends MatrixMap<V> {

	default public void put(K1 key1, K2 key2, K3 key3, K4 key4, V value) {
		putM(value, key1, key2, key3, key4);
	}

	default public Optional<V> get(K1 key1, K2 key2, K3 key3, K4 key4) {
		return getM(key1, key2, key3, key4);
	}
}
