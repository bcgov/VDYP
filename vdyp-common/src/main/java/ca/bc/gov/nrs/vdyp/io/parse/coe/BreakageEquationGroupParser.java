package ca.bc.gov.nrs.vdyp.io.parse.coe;

import ca.bc.gov.nrs.vdyp.common.ControlKey;

/**
 * Breakage Group Equation parser. This file contains a list of the Breakage Group equation numbers for each pair of
 * Species and BEC Zone. The file will contain exactly one entry for each such pair (16 * 14 = 224 records).
 * <ol>
 * <li>(cols 0-1) a species code</li>
 * <li>(cols 3-6) a BEC Zone alias</li>
 * <li>(cols 8-10) a breakage group identifier (1-180)</li>
 * </ol>
 * Lines that are empty or contain only blanks in columns 0-1 and 3-6 are considered blank.
 * <p>
 * The result is a map from Species x BEC Zones to integers.
 * <p>
 * FIP Control index: 022
 * <p>
 * Example: coe/BGRP.DAT
 *
 * @see EquationGroupParser
 * @author Kevin Smith, Vivid Solutions
 */
public class BreakageEquationGroupParser extends EquationGroupParser {

	public BreakageEquationGroupParser() {
		super(3);
	}

	@Override
	public ControlKey getControlKey() {
		return ControlKey.BREAKAGE_GROUPS;
	}

}
