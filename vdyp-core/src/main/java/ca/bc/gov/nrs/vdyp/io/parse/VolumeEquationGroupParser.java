package ca.bc.gov.nrs.vdyp.io.parse;

import java.util.Arrays;

public class VolumeEquationGroupParser extends EquationGroupParser {

	public static final String CONTROL_KEY = "VOLUME_EQN_GROUPS";

	public VolumeEquationGroupParser() {
		super(3);
		this.setHiddenBecs(Arrays.asList("BG"));
	}

	@Override
	public String getControlKey() {
		return CONTROL_KEY;
	}

}
