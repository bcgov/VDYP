package ca.bc.gov.nrs.vdyp.io.parse;

public class UtilComponentBaseAreaParser extends UtilComponentParser {
	public static final String CONTROL_KEY = "UTIL_COMP_BA";

	public static final int NUM_COEFFICIENTS = 2;

	public UtilComponentBaseAreaParser() {
		super(NUM_COEFFICIENTS, 1, "BA07", "BA12", "BA17", "BA22");
	}

	@Override
	public String getControlKey() {
		return CONTROL_KEY;
	}

}
