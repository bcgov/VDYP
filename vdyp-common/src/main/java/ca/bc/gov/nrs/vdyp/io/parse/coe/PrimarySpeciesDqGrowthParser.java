package ca.bc.gov.nrs.vdyp.io.parse.coe;

import ca.bc.gov.nrs.vdyp.common.ControlKey;
import ca.bc.gov.nrs.vdyp.io.parse.coe.base.PrimarySpeciesGrowthParser;

/**
 * Parses a mapping from a Basal Area Group number to a ModelCoefficients instance containing the coefficients for the
 * Quadratic Mean Diameter Primary Species Growth function as described in IPSJF150.doc.
 * <p>
 * Control index: 150
 * <p>
 * Example file: coe/DQSP05.COE
 *
 * @author Michael Junkin, Vivid Solutions
 * @see PrimarySpeciesGrowthParser
 */
public class PrimarySpeciesDqGrowthParser extends PrimarySpeciesGrowthParser {

	@Override
	public ControlKey getControlKey() {
		return ControlKey.PRIMARY_SP_DQ_GROWTH;
	}
}
