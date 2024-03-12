package ca.bc.gov.nrs.vdyp.io.parse.coe;

import ca.bc.gov.nrs.vdyp.common.ControlKey;
import ca.bc.gov.nrs.vdyp.io.parse.coe.base.SimpleCoefficientParser2;
import ca.bc.gov.nrs.vdyp.model.MatrixMap2;
import ca.bc.gov.nrs.vdyp.model.Region;

/**
 * Parse a datafile with species component size limits.
 *
 * Each line contains:
 * <ol>
 * <li>(cols 0-1) a Species key
 * <li>(col 3) a region indicator (either C or I)
 * <li>(cols 4-9, 10-15, 16-21, 22-27) - float * 4 - coefficients
 * </ol>
 * <ul>
 * <li>Coefficient 1 is High value for HL
 * <li>Coefficient 2 is High value for DQ
 * <li>Coefficient 3 is low value for DQ/HL
 * <li>Coefficient 4 is high value for DQ/HL
 * </ul>
 * All lines are read; there is no provision for blank lines.
 * <p>
 * The result is a {@link MatrixMap2} of coefficients (one-based), indexed first by BEC Zone and then Region.
 * <p>
 * FIP Control index: 061
 * <p>
 * Example file: coe/COMPLIM.COE
 *
 * @author Kevin Smith, Vivid Solutions
 * @see SimpleCoefficientParser2
 */
public class ComponentSizeParser extends SimpleCoefficientParser2<String, Region> {

	public ComponentSizeParser() {
		super(1, ControlKey.SPECIES_COMPONENT_SIZE_LIMIT);
		this.speciesKey().space(1).regionKey().coefficients(4, 6);
	}

}
