package ca.bc.gov.nrs.vdyp.model;

import java.util.Arrays;
import java.util.Collection;

public class MatrixMap3Impl<K1, K2, K3, V> extends MatrixMapImpl<V> implements MatrixMap3<K1, K2, K3, V> {

	public MatrixMap3Impl(Collection<K1> dimension1, Collection<K2> dimension2, Collection<K3> dimension3) {
		super(Arrays.asList(dimension1, dimension2, dimension3));
	}

}
