package ca.bc.gov.nrs.vdyp.io.parse.coe;

import ca.bc.gov.nrs.vdyp.common.ControlKey;
import ca.bc.gov.nrs.vdyp.io.parse.coe.base.OptionalCoefficientParser2;
import ca.bc.gov.nrs.vdyp.model.MatrixMap2;

/**
 * Parse a datafile with close util volume coefficients.
 *
 * Each line contains:
 * <ol>
 * <li>(cols 0-1) a Utilization Class id</li>
 * <li>(cols 3-6) a Volume Group id</li>
 * <li>(cols 8-10) a breakage group identifier (1-180)</li>
 * </ol>
 * A line with a Volume Group id of 0 or one that's empty is considered blank and is skipped.
 * <p>
 * It is legal and expected that some of the key pairs will be missing from the result (hence the use of
 * OptionalCoefficientParser2).
 * <p>
 * The result of the parse is a {@link MatrixMap2} of coefficients indexed first by UC then Volume Group id.
 * <p>
 * FIP Control index: 092
 * <p>
 * Example file: coe/REGVCU.COE
 *
 * @author Kevin Smith, Vivid Solutions
 * @see OptionalCoefficientParser2
 */
public class CloseUtilVolumeParser extends OptionalCoefficientParser2<Integer, Integer> {

	public static final int MAX_GROUPS = 80;

	public CloseUtilVolumeParser() {
		super(1, ControlKey.CLOSE_UTIL_VOLUME);
		this.ucIndexKey().space(1).groupIndexKey(MAX_GROUPS).coefficients(4, 10);
	}

}
