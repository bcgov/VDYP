package ca.bc.gov.nrs.vdyp.io.parse.coe;

import ca.bc.gov.nrs.vdyp.common.ControlKey;
import ca.bc.gov.nrs.vdyp.io.parse.coe.base.OptionalCoefficientParser2;
import ca.bc.gov.nrs.vdyp.model.MatrixMap2;

/**
 * Parse a datafile with Utilization Component WS Volume coefficients.
 *
 * Each line contains:
 * <ol>
 * <li>(cols 0-1) int - a utilization class (UC) index (ranging from 1-4)</li>
 * <li>(cols 2-5) int - a Volume Group identifier (ranging from 1 to 80)</li>
 * <li>(cols 6-15, 16-25, 26-35, 36-45) - floats - coefficients</li>
 * </ol>
 * Lines whose Volume Group identifier is 0 or blank indicate lines to be skipped. All lines are read - there is no
 * termination line.
 * <p>
 * The result of the parse is a {@link MatrixMap2} of coefficients indexed first by UC then Volume Group.
 * <p>
 * It is legal and expected that some of the key pairs will be missing from the result (hence the use of
 * OptionalCoefficientParser.)
 * <p>
 * FIP Control index: 091
 * <p>
 * Example file: coe/REGVU.COE
 *
 * @author Kevin Smith, Vivid Solutions
 * @see OptionalCoefficientParser2
 */
public class UtilComponentWSVolumeParser extends OptionalCoefficientParser2<Integer, Integer> {

	public static final int MAX_GROUPS = 80;

	public UtilComponentWSVolumeParser() {
		super(0, ControlKey.UTIL_COMP_WS_VOLUME);
		this.ucIndexKey().space(1).groupIndexKey(MAX_GROUPS).coefficients(4, 10);
	}
}