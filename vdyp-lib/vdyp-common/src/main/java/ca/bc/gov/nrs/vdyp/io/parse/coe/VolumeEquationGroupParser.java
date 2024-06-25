package ca.bc.gov.nrs.vdyp.io.parse.coe;

import java.util.Arrays;

import ca.bc.gov.nrs.vdyp.common.ControlKey;

/**
 * Volume Equation Number parser. Each line of this file contains:
 * <ol>
 * <li>(cols 0-1) a species code</li>
 * <li>(cols 3-6) a BEC Zone identifier</li>
 * <li>(cols 8-10) a volume group identifier (1-180)</li>
 * </ol>
 * Lines that are empty or contain only blanks in columns 0-1 and 3-6 are considered blank. The file must contain one
 * entry for each combination of Species and BEC Zone (excluding hidden BEC Zones) and thus there must be 16 * 13 values
 * in the file (at this time.)
 * <p>
 * The result is a map from Species x Visible BEC Zones to integers.
 * <p>
 * FIP Control index: 020
 * <p>
 * Example: coe/VGRPDEF1.DAT
 *
 * @see EquationGroupParser
 * @author Kevin Smith, Vivid Solutions
 */
public class VolumeEquationGroupParser extends EquationGroupParser {

	public VolumeEquationGroupParser() {
		super(3);
		this.setHiddenBecs(Arrays.asList("BG"));
	}

	@Override
	public ControlKey getControlKey() {
		return ControlKey.VOLUME_EQN_GROUPS;
	}

}
