package ca.bc.gov.nrs.vdyp.io.parse;

import ca.bc.gov.nrs.vdyp.model.Region;

public class VeteranBQParser extends SimpleCoefficientParser2<String, Region> {

	public static final String CONTROL_KEY = "VETERAN_BQ";

	public VeteranBQParser() {
		super(1, CONTROL_KEY);
		this.speciesKey().space(1).regionKey().coefficients(3, 9);
	}

}