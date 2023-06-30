package ca.bc.gov.nrs.vdyp.io.parse;

public class UtilComponentWSVolumeParser extends SimpleCoefficientParser2<Integer, Integer> {

	public static final String CONTROL_KEY = "UTIL_COMP_WS_VOLUME";

	public UtilComponentWSVolumeParser() {
		super(0, CONTROL_KEY);
		this.ucIndexKey().space(1).groupIndexKey(80).coefficients(4, 10);
	}

}