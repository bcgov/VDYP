package ca.bc.gov.nrs.vdyp.io.parse.coe;

import ca.bc.gov.nrs.vdyp.common.ControlKey;

/**
 * Decay Equation parser. This file contains a list of the Decay equation numbers for each pair of Species and BEC Zone.
 * <ol>
 * <li>(cols 0-1) a Species code</li>
 * <li>(cols 3-6) a BEC Zone id</li>
 * <li>(cols 8-10) a Decay equation number</li>
 * </ol>
 * Lines whose Species code AND BEC Zone id are both empty are considered blank lines are are skipped.
 * <p>
 * The file must contain a definition for each pair of Species and BEC Zone.
 * <p>
 * FIP Control index: 021
 * <p>
 * Example: coe/DGRP.DAT
 *
 * @see EquationGroupParser
 * @author Kevin Smith, Vivid Solutions
 */
public class DecayEquationGroupParser extends EquationGroupParser {

	public DecayEquationGroupParser() {
		super(3);
	}

	@Override
	public ControlKey getControlKey() {
		return ControlKey.DECAY_GROUPS;
	}

}
