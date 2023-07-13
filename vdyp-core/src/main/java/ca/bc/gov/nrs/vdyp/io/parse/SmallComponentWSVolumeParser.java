package ca.bc.gov.nrs.vdyp.io.parse;

public class SmallComponentWSVolumeParser extends SimpleCoefficientParser1<String> {

	public static final String CONTROL_KEY = "SMALL_COMP_WS_VOLUME";

	public SmallComponentWSVolumeParser() {
		super(String.class, 1, CONTROL_KEY);
		this.speciesKey();
		this.coefficients(4, 10);
	}

}
