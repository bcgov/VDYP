package ca.bc.gov.nrs.vdyp.io.parse.coe;

import ca.bc.gov.nrs.vdyp.common.ControlKey;
import ca.bc.gov.nrs.vdyp.io.parse.coe.base.OptionalCoefficientParser2;
import ca.bc.gov.nrs.vdyp.model.MatrixMap2;

/**
 * Parse a datafile with volume net decay coefficients.
 *
 * Each line starts with two integers - a utilization class (UC) index and a Decay Group index, followed by an array of
 * three coefficients (one-based).
 * <ol>
 * <li>(cols 0-1) int - a utilization component number (1-4)</li>
 * <li>(cols 2-5) int - a decay group identifier (1-80)</li>
 * <li>(cols 6-15, 16-25, 26-35) - float * 3 - coefficients</li>
 * </ol>
 * All lines in the file are parsed. Lines that are blank, whose Decay Group value is zero, all spaces, or missing, are
 * skipped.
 * <p>
 * The result of the parse is a {@link MatrixMap2} of coefficients indexed first by UC then Decay Group.
 * <p>
 * It is legal and expected that some of the key pairs will be missing from the result (hence the use of
 * OptionalCoefficientParser.)
 * <p>
 * FIP Control index: 093
 * <p>
 * Example file: coe/REGVDU.COE
 *
 * @author Kevin Smith, Vivid Solutions
 * @see OptionalCoefficientParser2
 */
public class VolumeNetDecayParser extends OptionalCoefficientParser2<Integer, Integer> {

	public static final int MAX_GROUPS = 80;

	public VolumeNetDecayParser() {
		super(1, ControlKey.VOLUME_NET_DECAY);
		this.ucIndexKey().space(1).groupIndexKey(MAX_GROUPS).coefficients(4, 10);
	}

}
