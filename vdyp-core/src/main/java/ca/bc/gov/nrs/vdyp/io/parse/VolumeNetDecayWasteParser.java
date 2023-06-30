package ca.bc.gov.nrs.vdyp.io.parse;

public class VolumeNetDecayWasteParser extends SimpleCoefficientParser1<String> {

	public static final String CONTROL_KEY = "VOLUME_NET_DECAY_WASTE";

	public VolumeNetDecayWasteParser() {
		super(String.class, 0, CONTROL_KEY);
		this.speciesKey();
		this.coefficients(6, 9);
	}
}
