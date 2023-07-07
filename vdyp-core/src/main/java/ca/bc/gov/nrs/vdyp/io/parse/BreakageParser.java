package ca.bc.gov.nrs.vdyp.io.parse;

public class BreakageParser extends SimpleCoefficientParser1<Integer> {

	public static final String CONTROL_KEY = "BREAKAGE";

	public BreakageParser() {
		super(Integer.class, 1, CONTROL_KEY);
		this.groupIndexKey(40);
		this.coefficients(6, 9);
	}
}
