package ca.bc.gov.nrs.vdyp.io.parse.coe;

import ca.bc.gov.nrs.vdyp.common.ControlKey;
import ca.bc.gov.nrs.vdyp.io.parse.coe.base.HLCoefficientParser;
import ca.bc.gov.nrs.vdyp.model.MatrixMap2;

/**
 * Parses an HL Coefficient data file.
 *
 * These files each contain a complete mapping of species aliases and regions to a (one-based) list of coefficients, and
 * therefore has 32 lines. Each row contains:
 * <ol>
 * <li>(cols 0-1) Species alias</li>
 * <li>(col 3) Region ('I' or 'C')</li>
 * <li>(cols 4-13, 14-23, ...) three floats in 10 character fields.
 * </ol>
 * All lines are read; there is no provision for blank lines. There may be multiple lines with the same Species and
 * Region values; the last one wins.
 * <p>
 * The file must populate #Species * #Regions (currently 32) values.
 * <p>
 * The result of the parse is a {@link MatrixMap2} of Coefficients indexed by first species, then region.
 * <p>
 * FIP Control indices: 050
 * <p>
 * Examples: coe/REGYHLP.COE
 *
 * @author Kevin Smith, Vivid Solutions
 * @see HLCoefficientParser
 */
public class HLPrimarySpeciesEqnP1Parser extends HLCoefficientParser {
	public static final int NUM_COEFFICIENTS = 3;

	public HLPrimarySpeciesEqnP1Parser() {
		super(NUM_COEFFICIENTS, ControlKey.HL_PRIMARY_SP_EQN_P1);
	}
}