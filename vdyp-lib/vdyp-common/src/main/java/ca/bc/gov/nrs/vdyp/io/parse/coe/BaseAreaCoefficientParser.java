package ca.bc.gov.nrs.vdyp.io.parse.coe;

import ca.bc.gov.nrs.vdyp.common.ControlKey;
import ca.bc.gov.nrs.vdyp.io.parse.coe.base.CoefficientParser;
import ca.bc.gov.nrs.vdyp.io.parse.control.ControlMapSubResourceParser;

/**
 * Parses a Coefficient data file. This builds a MatrixMap2 indexed by BEC Zone and then Species. Each map entry
 * contains nine coefficients, 0-based. The coefficents for a given BEC Zone and Species are given in the nth column of
 * the data file, for the rows whose first 4 columns identify the BEC Zone. The seventh column is a digit from indicates
 * the index, running from 0 through 8. Following this is an Indicator (a integer ranging from 0 to 2) and 16 float
 * values, one per species. The mth column is the value for the species numbered m, determined from the speciesAliases
 * supplied in the given control map. For example, species "C" has m == 3 since it is fourth in the list of Species.
 * <p>
 * For example, the row starting "PP A4 x" contains the Species values for index 4 of BEC Zone PP.
 * <p>
 * The indicator 0 sets the value for all Species in the row to the first float value - all others are ignored.
 * <p>
 * The indicator 1 sets the value for Species m to the value of the mth float value minus the value of the first float
 * value (unless m == 0, in which case the value is just the first float value.)
 * <p>
 * The indicator 2 sets the value for Species m to the mth float value.
 * <p>
 * If the BEC Zone on a given line is empty, the line is considered blank as is skipped.
 * <p>
 * FIP Control index: 040
 * <p>
 * Example file: coe/REGBA25.coe
 *
 * @author Kevin Smith, Vivid Solutions
 * @see ControlMapSubResourceParser
 */
public class BaseAreaCoefficientParser extends CoefficientParser {
	public BaseAreaCoefficientParser() {
		super(ControlKey.COE_BA);
	}

	@Override
	public boolean isRequired() {
		return false;
	}

}