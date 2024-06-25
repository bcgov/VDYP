package ca.bc.gov.nrs.vdyp.io.parse.coe;

import java.util.Map;

import ca.bc.gov.nrs.vdyp.common.ControlKey;
import ca.bc.gov.nrs.vdyp.io.parse.coe.base.SimpleCoefficientParser1;

/**
 * Parser for a Breakage data file. Each line contains an index (an integer from 1 to 40) and an array of four
 * coefficients (one-based):
 * <ol>
 * <li>(cols 0-1) integer - a breakage group id (1-40)</li>
 * <li>(col 2-10, 11-19, ...) - float - coefficients</li>
 * </ol>
 * All lines are parsed. A breakage group id of 0, or blank, is considered a blank line.
 * <p>
 * The result of the parse is a {@link Map} of Breakage Group ids to a set of four coefficients.
 * <p>
 * FIP Control index: 095
 * <p>
 * Example file: coe/REGBREAK.COE
 *
 * @author Kevin Smith, Vivid Solutions
 * @see SimpleCoefficientParser1
 */
public class BreakageParser extends SimpleCoefficientParser1<Integer> {

	public static final int MAX_GROUPS = 40;

	public BreakageParser() {
		super(Integer.class, 1, ControlKey.BREAKAGE);
		this.groupIndexKey(MAX_GROUPS);
		this.coefficients(6, 9);
	}
}
