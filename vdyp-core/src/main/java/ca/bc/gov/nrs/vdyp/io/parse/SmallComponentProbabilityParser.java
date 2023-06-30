package ca.bc.gov.nrs.vdyp.io.parse;

public class SmallComponentProbabilityParser extends SimpleCoefficientParser1<String> {

	public static final String CONTROL_KEY = "SMALL_COMP_PROBABILITY";

	public SmallComponentProbabilityParser() {
		super(String.class, 1, CONTROL_KEY);
		this.speciesKey();
		this.coefficients(4, 10);
	}

}
