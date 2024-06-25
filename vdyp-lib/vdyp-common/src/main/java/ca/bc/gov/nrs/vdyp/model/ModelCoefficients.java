package ca.bc.gov.nrs.vdyp.model;

public class ModelCoefficients {
	private final int model;
	private final Coefficients coefficients;

	public ModelCoefficients(int model, Coefficients coefficients) {
		this.model = model;
		this.coefficients = coefficients;
	}

	public int getModel() {
		return model;
	}

	public Coefficients getCoefficients() {
		return coefficients;
	}
}
