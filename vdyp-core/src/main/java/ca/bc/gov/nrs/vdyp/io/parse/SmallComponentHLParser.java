package ca.bc.gov.nrs.vdyp.io.parse;

public class SmallComponentHLParser extends SimpleCoefficientParser1<String> {

	public static final String CONTROL_KEY = "SMALL_COMP_HL";

	public SmallComponentHLParser() {
		super(String.class, 1, CONTROL_KEY);
		this.speciesKey();
		this.coefficients(2, 10);
	}

}
