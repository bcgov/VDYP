package ca.bc.gov.nrs.vdyp.io.parse.coe;

import ca.bc.gov.nrs.vdyp.common.ControlKey;
import ca.bc.gov.nrs.vdyp.io.parse.coe.base.UtilComponentParser;
import ca.bc.gov.nrs.vdyp.model.MatrixMap3;

/**
 * Parses an Utilization Component Basal Area data file. Each line contains:
 * <ol>
 * <li>(cols 0-3) - a diameter value (one of 07.5, 12.5, 17.5, 22.5) as text</li>
 * <li>(cols 4-12) - ignored (in practice, this text identifies the upper bound of a range.)</li>
 * <li>(cols 13-14) - a Species code</li>
 * <li>(cols 16-19) - a BEC Scope key. This will be either blank, 'C' or 'I', or a BEC Zone identifier</li>
 * <li>(cols 20-29, 30-39, 40-49, 50-59) - floats - coefficients</li>
 * </ol>
 * The result of the parse is a {@link MatrixMap3} of four coefficients indexed by one of the listed diameter values,
 * Species, and then BEC Zone. The BEC Scope field is converted in a (set of) BEC Zones in this way:
 * <ul>
 * <li>blank - all BEC zones</li>
 * <li>'C' or 'I' - all coastal (C) or interior (I) BEC Zones</li>
 * <li>a BEC Zone identifier - that specific Zone</li>
 * </ul>
 * The diameter value indicates a range from this value (inclusive) to the next listed value (exclusive), except in the
 * case of the last value, where the upper bound of the range is +infinity.
 * <p>
 * Multiple lines with the same indices are legal, with the last entry winning. Lines starting with " " (four blanks)
 * are skipped, regardless of the remainder of the line. There is no parsing termination line format - all lines are
 * always read.
 * <p>
 * FIP Control index: 071
 * <p>
 * Example file: coe/REGDQC.DAT
 *
 * @author Kevin Smith, Vivid Solutions
 * @see UtilComponentParser
 */
public class UtilComponentDQParser extends UtilComponentParser {
	public static final int NUM_COEFFICIENTS = 4;

	public UtilComponentDQParser() {
		super(NUM_COEFFICIENTS, 9, "07.5", "12.5", "17.5", "22.5");
	}

	@Override
	public ControlKey getControlKey() {
		return ControlKey.UTIL_COMP_DQ;
	}

}
