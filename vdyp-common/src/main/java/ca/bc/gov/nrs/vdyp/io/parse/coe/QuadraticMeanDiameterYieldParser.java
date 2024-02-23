package ca.bc.gov.nrs.vdyp.io.parse.coe;

import ca.bc.gov.nrs.vdyp.common.ControlKey;
import ca.bc.gov.nrs.vdyp.io.parse.coe.base.BecZoneBySpeciesCoefficientParser;

/**
 * Parses a mapping from a BEC Zone Alias and Species to a list of six
 * coefficients. Each row contains
 * <ol>
 * <li>(cols 0-3) BEC Zone Alias</li>
 * <li>(col 6) int - index into the zero-based coefficient list (range 0-6)</li>
 * <li>(cols 7-9) int - indicator. 0 = process only the first coefficient; 1 =
 * process all coefficients</li>
 * <li>(cols 10-17, 18-25, 26-33 ...) float * 16 - coefficient list, by species
 * ordinal (AC = 0, AT = 1, etc)</li>
 * </ol>
 * All lines are parsed. A BEC Zone Alias value, trimmed, of "" results in the
 * line being skipped.
 * <p>
 * The result of the parse is a map from BEC Zone Alias and Species to a
 * (zero-based) eight-element coefficient array.
 * <p>
 * Control index: 107
 * <p>
 * Example file: coe/YLDDQ45.COE
 *
 * @author Michael Junkin, Vivid Solutions
 * @see BecZoneBySpeciesCoefficientParser
 */
public class QuadraticMeanDiameterYieldParser extends BecZoneBySpeciesCoefficientParser {

	private static final int NUM_COEFFICIENTS = 6;

	public QuadraticMeanDiameterYieldParser() {
		super(NUM_COEFFICIENTS);
	}

	@Override
	public ControlKey getControlKey() {
		return ControlKey.DQ_YIELD;
	}
}
