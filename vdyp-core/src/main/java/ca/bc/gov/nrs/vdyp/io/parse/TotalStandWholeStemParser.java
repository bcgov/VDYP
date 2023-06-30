package ca.bc.gov.nrs.vdyp.io.parse;

public class TotalStandWholeStemParser extends SimpleCoefficientParser1<Integer> {

	public static final String CONTROL_KEY = "TOTAL_STAND_WHOLE_STEM_VOL";

	public TotalStandWholeStemParser() {
		super(Integer.class, 0, CONTROL_KEY);
		this.groupIndexKey(80);
		this.coefficients(9, 10);
	}

}
