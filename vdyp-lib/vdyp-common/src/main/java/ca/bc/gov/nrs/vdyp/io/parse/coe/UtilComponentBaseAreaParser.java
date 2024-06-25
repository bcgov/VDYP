package ca.bc.gov.nrs.vdyp.io.parse.coe;

import ca.bc.gov.nrs.vdyp.common.ControlKey;
import ca.bc.gov.nrs.vdyp.io.parse.coe.base.UtilComponentParser;
import ca.bc.gov.nrs.vdyp.model.MatrixMap3;

/**
 * Parses an Utilization Component Basal Area data file. Each line contains:
 * <ol>
 * <li>(cols 0-3) - a Utilization Component code (one of BA12, BA17 or BA22)</li>
 * <li>(cols 5-6) - a Species code</li>
 * <li>(cols 8-11) - a BEC Scope key. This will be either blank, 'C' or 'I', or a BEC Zone identifier</li>
 * <li>(cols 12-21) - float - coefficient one</li>
 * <li>(cols 22-31) - float - coefficient two</li>
 * </ol>
 * The result of the parse is a {@link MatrixMap3} of two coefficients indexed by Utilization Component, Species, and
 * then BEC Zone. The BEC Scope field is converted in a (set of) BEC Zones in this way:
 * <ul>
 * <li>blank - all BEC zones</li>
 * <li>'C' or 'I' - all coastal (C) or interior (I) BEC Zones</li>
 * <li>a BEC Zone identifier - that specific Zone</li>
 * </ul>
 * Multiple lines with the same indices are legal, with the last entry winning. Lines starting with " " (four blanks)
 * are skipped, regardless of the remainder of the line. There is no parsing termination line format - all lines are
 * always read.
 * <p>
 * FIP Control index: 070
 * <p>
 * Example file: coe/REGBAC.DAT
 *
 * @author Kevin Smith, Vivid Solutions
 * @see UtilComponentParser
 */
public class UtilComponentBaseAreaParser extends UtilComponentParser {
	public static final int NUM_COEFFICIENTS = 2;

	public UtilComponentBaseAreaParser() {
		super(NUM_COEFFICIENTS, 1, "BA12", "BA17", "BA22");
	}

	@Override
	public ControlKey getControlKey() {
		return ControlKey.UTIL_COMP_BA;
	}

}
