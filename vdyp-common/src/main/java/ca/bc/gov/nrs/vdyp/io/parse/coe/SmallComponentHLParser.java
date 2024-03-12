package ca.bc.gov.nrs.vdyp.io.parse.coe;

import java.util.Map;

import ca.bc.gov.nrs.vdyp.common.ControlKey;
import ca.bc.gov.nrs.vdyp.io.parse.coe.base.SimpleCoefficientParser1;

/**
 * Parser for a Small HL Component data file. Each line contains a Species code and an array of two coefficients
 * (one-based).
 * <ol>
 * <li>(cols 0-2) key - a Species code</li>
 * <li>(col 3-12, 13-22) - float - coefficients</li>
 * </ol>
 * Lines with an empty Species code are considered blank and are skipped. All lines in the file are parsed.
 * <p>
 * The result of the parse is a {@link Map} of Species codes to a set of two coefficients.
 * <p>
 * FIP Control index: 085
 * <p>
 * Example file: coe/REGHL1C.DAT
 *
 * @author Kevin Smith, Vivid Solutions
 * @see SimpleCoefficientParser1
 */
public class SmallComponentHLParser extends SimpleCoefficientParser1<String> {

	public SmallComponentHLParser() {
		super(String.class, 1, ControlKey.SMALL_COMP_HL);
		this.speciesKey();
		this.coefficients(2, 10);
	}

}
