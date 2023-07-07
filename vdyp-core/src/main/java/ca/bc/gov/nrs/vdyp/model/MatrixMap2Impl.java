package ca.bc.gov.nrs.vdyp.model;

import java.util.Arrays;
import java.util.Collection;

public class MatrixMap2Impl<K1, K2, V> extends MatrixMapImpl<V> implements MatrixMap2<K1, K2, V> {

	public MatrixMap2Impl(Collection<K1> dimension1, Collection<K2> dimension2) {
		super(Arrays.asList(dimension1, dimension2));
	}

}
