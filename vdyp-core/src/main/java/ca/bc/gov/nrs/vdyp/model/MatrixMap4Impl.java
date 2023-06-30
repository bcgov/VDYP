package ca.bc.gov.nrs.vdyp.model;

import java.util.Arrays;
import java.util.Collection;

public class MatrixMap4Impl<K1, K2, K3, K4, V> extends MatrixMapImpl<V> implements MatrixMap4<K1, K2, K3, K4, V> {

	public MatrixMap4Impl(
			Collection<K1> dimension1, Collection<K2> dimension2, Collection<K3> dimension3, Collection<K4> dimension4
	) {
		super(Arrays.asList(dimension1, dimension2, dimension3, dimension4));
	}

}
