package ca.bc.gov.nrs.vdyp.io.parse;

public class DecayEquationGroupParser extends EquationGroupParser {

	public static final String CONTROL_KEY = "DECAY_GROUPS";

	public DecayEquationGroupParser() {
		super(3);
	}

	@Override
	public String getControlKey() {
		return CONTROL_KEY;
	}

}
