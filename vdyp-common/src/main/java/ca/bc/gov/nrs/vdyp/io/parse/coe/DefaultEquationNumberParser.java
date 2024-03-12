package ca.bc.gov.nrs.vdyp.io.parse.coe;

import java.util.Arrays;

import ca.bc.gov.nrs.vdyp.common.ControlKey;

/**
 * Default Equation Number parser. This file contains a list of the default equation numbers for each pair of Species
 * and BEC Zone, excluding BEC Zones BG and AT.
 * <ol>
 * <li>(cols 0-1) a Species code</li>
 * <li>(cols 3-6) a BEC Zone id</li>
 * <li>(cols 8-12) a Decay equation number</li>
 * </ol>
 * Lines whose Species code AND BEC Zone id are both empty are considered blank lines are are skipped.
 * <p>
 * The file will contain exactly one entry for each such pair, excluding the hidden BEC Zones (see below.)
 * <p>
 * FIP Control index: 030
 * <p>
 * Example: coe/GRPBA1.DAT
 *
 * @see EquationGroupParser
 * @author Kevin Smith, Vivid Solutions
 */
public class DefaultEquationNumberParser extends EquationGroupParser {
	// C_BAGRP1/BG1DEFV

	public DefaultEquationNumberParser() {
		super(5);
		this.setHiddenBecs(Arrays.asList("BG", "AT"));
	}

	@Override
	public ControlKey getControlKey() {
		return ControlKey.DEFAULT_EQ_NUM;
	}

}
