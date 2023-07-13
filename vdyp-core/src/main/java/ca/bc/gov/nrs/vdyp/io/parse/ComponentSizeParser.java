package ca.bc.gov.nrs.vdyp.io.parse;

import ca.bc.gov.nrs.vdyp.model.Region;

/**
 * Parse a datafile with species component size limits
 *
 * @author Kevin Smith, Vivid Solutions
 *
 */
public class ComponentSizeParser extends SimpleCoefficientParser2<String, Region> {

	public static final String CONTROL_KEY = "SPECIES_COMPONENT_SIZE_LIMIT";

	public ComponentSizeParser() {
		super(1, CONTROL_KEY);
		this.speciesKey().space(1).regionKey().coefficients(4, 6);
	}

}
