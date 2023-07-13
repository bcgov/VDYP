package ca.bc.gov.nrs.vdyp.io.parse;

public class SmallComponentDQParser extends SimpleCoefficientParser1<String> {

	public static final String CONTROL_KEY = "SMALL_COMP_DQ";

	public SmallComponentDQParser() {
		super(String.class, 1, CONTROL_KEY);
		this.speciesKey();
		this.coefficients(2, 10);
	}

}
