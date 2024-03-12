package ca.bc.gov.nrs.vdyp.io.parse.coe;

import java.util.Map;

import ca.bc.gov.nrs.vdyp.common.ControlKey;
import ca.bc.gov.nrs.vdyp.io.parse.coe.base.SimpleCoefficientParser1;

/**
 * Parser for a Total Stand Whole Stem data file. Each line contains a Volume Group id and an array of nine coefficients
 * (zero-based). It is ended by a blank line.
 * <ol>
 * <li>(cols 0-2) integer - a Volume Group id (1 - 80)</li>
 * <li>(col 3-12, 13-22, ...) - float * 10 - coefficients</li>
 * </ol>
 * Lines with an empty Volume Group id are considered blank and are skipped. All lines in the file are parsed.
 * <p>
 * The result of the parse is a {@link Map} of Volume Group ids to a set of nine coefficients.
 * <p>
 * FIP Control index: 090
 * <p>
 * Example file: coe/VTOTREG4.COE
 *
 * @author Kevin Smith, Vivid Solutions
 * @see SimpleCoefficientParser1
 */
public class TotalStandWholeStemParser extends SimpleCoefficientParser1<Integer> {

	public TotalStandWholeStemParser() {
		super(Integer.class, 0, ControlKey.TOTAL_STAND_WHOLE_STEM_VOL);
		this.groupIndexKey(80);
		this.coefficients(9, 10);
	}

}
