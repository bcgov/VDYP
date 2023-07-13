package ca.bc.gov.nrs.vdyp.io.parse;

/**
 * Parse a datafile with volume net decay coefficients
 *
 * @author Kevin Smith, Vivid Solutions
 *
 */
public class VolumeNetDecayParser extends SimpleCoefficientParser2<Integer, Integer> {

	public static final String CONTROL_KEY = "VOLUME_NET_DECAY";

	public VolumeNetDecayParser() {
		super(1, CONTROL_KEY);
		this.ucIndexKey().space(1).groupIndexKey(80).coefficients(4, 10);
	}

}
