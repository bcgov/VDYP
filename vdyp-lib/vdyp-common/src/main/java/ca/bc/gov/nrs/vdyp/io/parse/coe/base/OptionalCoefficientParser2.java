package ca.bc.gov.nrs.vdyp.io.parse.coe.base;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import ca.bc.gov.nrs.vdyp.common.ControlKey;
import ca.bc.gov.nrs.vdyp.model.Coefficients;
import ca.bc.gov.nrs.vdyp.model.MatrixMap2;
import ca.bc.gov.nrs.vdyp.model.MatrixMap2Impl;

public abstract class OptionalCoefficientParser2<K1, K2> extends
		BaseCoefficientParser<Coefficients, Optional<Coefficients>, MatrixMap2<K1, K2, Optional<Coefficients>>> {

	private int indexFrom;

	protected OptionalCoefficientParser2(int indexFrom, ControlKey controlKey) {
		super(2, controlKey);
		this.indexFrom = indexFrom;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected MatrixMap2<K1, K2, Optional<Coefficients>> createMap(List<Collection<?>> keyRanges) {
		return new MatrixMap2Impl<>(
				(Collection<K1>) keyRanges.get(0), (Collection<K2>) keyRanges.get(1), (k1, k2) -> Optional.empty()
		);
	}

	@Override
	protected Coefficients getCoefficients(List<Float> coefficients) {
		return new Coefficients(coefficients, indexFrom);
	}

	@Override
	protected Optional<Coefficients> wrapCoefficients(Coefficients coefficients) {
		return Optional.of(coefficients);
	}

}
