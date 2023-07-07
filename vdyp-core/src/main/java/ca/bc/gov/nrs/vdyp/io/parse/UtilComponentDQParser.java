package ca.bc.gov.nrs.vdyp.io.parse;

public class UtilComponentDQParser extends UtilComponentParser {
	public static final String CONTROL_KEY = "UTIL_COMP_DQ";

	public static final int NUM_COEFFICIENTS = 4;

	public UtilComponentDQParser() {
		super(NUM_COEFFICIENTS, 9, "07.5", "12.5", "17.5", "22.5");
	}

	@Override
	public String getControlKey() {
		return CONTROL_KEY;
	}

}
