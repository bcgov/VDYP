package ca.bc.gov.nrs.vdyp.io.parse.coe.base;

import java.util.Collection;
import java.util.List;

import ca.bc.gov.nrs.vdyp.common.ControlKey;
import ca.bc.gov.nrs.vdyp.io.parse.common.ResourceParseValidException;
import ca.bc.gov.nrs.vdyp.model.Coefficients;
import ca.bc.gov.nrs.vdyp.model.MatrixMap2;
import ca.bc.gov.nrs.vdyp.model.MatrixMap2Impl;

public abstract class SimpleCoefficientParser2<K1, K2>
		extends BaseCoefficientParser<Coefficients, Coefficients, MatrixMap2<K1, K2, Coefficients>> {

	private int indexFrom;

	protected SimpleCoefficientParser2(int indexFrom, ControlKey controlKey) {
		super(2, controlKey);
		this.indexFrom = indexFrom;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected MatrixMap2<K1, K2, Coefficients> createMap(List<Collection<?>> keyRanges) {
		return new MatrixMap2Impl<>(
				(Collection<K1>) keyRanges.get(0), (Collection<K2>) keyRanges.get(1), (k1, k2) -> getCoefficients()
		);
	}

	@Override
	protected Coefficients getCoefficients(List<Float> coefficients) {
		return new Coefficients(coefficients, indexFrom);
	}

	@Override
	protected Coefficients wrapCoefficients(Coefficients coefficients) {
		return coefficients;
	}

	@Override
	protected void validate(MatrixMap2<K1, K2, Coefficients> result, int parsed, List<Collection<?>> keyRanges)
			throws ResourceParseValidException {
		var expected = keyRanges.stream().mapToInt(Collection::size).reduce(1, (x, y) -> x * y);
		if (expected != parsed) {
			throw new ResourceParseValidException("Expected " + expected + " records but there were " + parsed);
		}

	}

}
