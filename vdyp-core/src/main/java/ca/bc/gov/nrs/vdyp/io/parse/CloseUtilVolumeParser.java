package ca.bc.gov.nrs.vdyp.io.parse;

/**
 * Parse a datafile with close util volumen coefficients
 *
 * @author Kevin Smith, Vivid Solutions
 *
 */
public class CloseUtilVolumeParser extends SimpleCoefficientParser2<Integer, Integer> {

	public static final String CONTROL_KEY = "CLOSE_UTIL_VOLUME";

	public CloseUtilVolumeParser() {
		super(1, CONTROL_KEY);
		this.ucIndexKey().space(1).groupIndexKey(80).coefficients(4, 10);
	}

}
