package ca.bc.gov.nrs.vdyp.io.parse.coe;

import java.util.Map;

import ca.bc.gov.nrs.vdyp.common.ControlKey;
import ca.bc.gov.nrs.vdyp.io.parse.coe.base.SimpleCoefficientParser1;

/**
 * Parser for a Small Component WS Volume data file. Each line contains a species code and an array of four coefficients
 * (one-based).
 * <ol>
 * <li>(cols 0-2) a Species code</li>
 * <li>(col 3-12, 13-22, 23-32, 33-42) - float * 4 - coefficients</li>
 * </ol>
 * Lines with an empty Species code are considered blank and are skipped. All lines in the file are parsed.
 * <p>
 * The result of the parse is a {@link Map} of Species codes to a set of four coefficients.
 * <p>
 * FIP Control index: 086
 * <p>
 * Example file: coe/REGV1C.DAT
 *
 * @author Kevin Smith, Vivid Solutions
 * @see SimpleCoefficientParser1
 */
public class SmallComponentWSVolumeParser extends SimpleCoefficientParser1<String> {

	public SmallComponentWSVolumeParser() {
		super(String.class, 1, ControlKey.SMALL_COMP_WS_VOLUME);
		this.speciesKey();
		this.coefficients(4, 10);
	}

}
