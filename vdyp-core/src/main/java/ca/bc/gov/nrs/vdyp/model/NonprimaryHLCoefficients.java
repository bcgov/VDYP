package ca.bc.gov.nrs.vdyp.model;

import java.util.List;

public class NonprimaryHLCoefficients extends Coefficients {

	private int equationIndex;

	public NonprimaryHLCoefficients(float[] coe, int equationIndex) {
		super(coe, 1);
		this.equationIndex = equationIndex;
	}

	public NonprimaryHLCoefficients(List<Float> coe, int equationIndex) {
		super(coe, 1);
		this.equationIndex = equationIndex;
	}

	public int getEquationIndex() {
		return equationIndex;
	}

}
