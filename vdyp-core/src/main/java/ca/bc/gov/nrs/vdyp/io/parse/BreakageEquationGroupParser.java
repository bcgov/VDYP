package ca.bc.gov.nrs.vdyp.io.parse;

public class BreakageEquationGroupParser extends EquationGroupParser {

	public static final String CONTROL_KEY = "BREAKAGE_GROUPS";

	public BreakageEquationGroupParser() {
		super(3);
	}

	@Override
	public String getControlKey() {
		return CONTROL_KEY;
	}

}
