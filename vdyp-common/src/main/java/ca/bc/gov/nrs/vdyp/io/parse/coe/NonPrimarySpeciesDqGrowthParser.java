package ca.bc.gov.nrs.vdyp.io.parse.coe;

import ca.bc.gov.nrs.vdyp.common.ControlKey;
import ca.bc.gov.nrs.vdyp.io.parse.coe.base.NonPrimarySpeciesGrowthParser;
import ca.bc.gov.nrs.vdyp.model.MatrixMap2;

/**
 * Parse a Quadratic Mean Diameter Growth Coefficients (non-primary species) data file.
 *
 * Each line contains:
 * <ol>
 * <li>(cols 0-1) int - a Species identifier</li>
 * <li>(cols 2-4) int - a Basal Area Group id (0-30)</li>
 * <li>(cols 5-14, 15-24, 25-34) - floats - coefficients</li>
 * </ol>
 * All lines are read - there is no termination line. There can be no blank lines in the file. All three coefficients
 * must be present for each line.
 * <p>
 * Basal Area Group 0 defines, for that species, the coefficients to be used for all (Species, BA Groups) not defined
 * elsewhere in the file.
 * <p>
 * The result of the parse is a {@link MatrixMap2} of three coefficients (first index = 1) indexed first by Species then
 * Basal Area Group Id.
 * <p>
 * Control index: 151
 * <p>
 * Example file: coe/DQSP06.COE
 *
 * @author Michael Junkin, Vivid Solutions
 * @see NonPrimarySpeciesGrowthParser
 */
public class NonPrimarySpeciesDqGrowthParser extends NonPrimarySpeciesGrowthParser {

	public NonPrimarySpeciesDqGrowthParser() {
		super(ControlKey.NON_PRIMARY_SP_DQ_GROWTH);
	}
}
