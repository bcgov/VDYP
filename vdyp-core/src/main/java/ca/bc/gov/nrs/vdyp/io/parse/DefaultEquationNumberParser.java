package ca.bc.gov.nrs.vdyp.io.parse;

import java.util.Arrays;

public class DefaultEquationNumberParser extends EquationGroupParser {

	public static final String CONTROL_KEY = "DEFAULT_EQ_NUM";

	public DefaultEquationNumberParser() {
		super(5);
		this.setHiddenBecs(Arrays.asList("BG", "AT"));
	}

	@Override
	public String getControlKey() {
		return CONTROL_KEY;
	}

}
