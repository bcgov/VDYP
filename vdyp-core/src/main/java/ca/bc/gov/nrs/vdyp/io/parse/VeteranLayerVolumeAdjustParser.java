package ca.bc.gov.nrs.vdyp.io.parse;

public class VeteranLayerVolumeAdjustParser extends SimpleCoefficientParser1<String> {

	public static final String CONTROL_KEY = "VETERAN_LAYER_VOLUME_ADJUST";

	public VeteranLayerVolumeAdjustParser() {
		super(String.class, 0, CONTROL_KEY);
		this.speciesKey();
		this.coefficients(4, 9);
	}

}
