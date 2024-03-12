package ca.bc.gov.nrs.vdyp.io.parse.coe;

import java.util.Map;

import ca.bc.gov.nrs.vdyp.common.ControlKey;
import ca.bc.gov.nrs.vdyp.io.parse.coe.base.SimpleCoefficientParser1;

/**
 * Parser for a volume net decay waste coefficients data file. Each line contains a species code and an array of six
 * coefficients (zero-based).
 * <ol>
 * <li>(cols 0-1) key - a species code</li>
 * <li>(col 2-10, 11-19, ...) - float * 6 - coefficients</li>
 * </ol>
 * All lines in the file are parsed. Lines that are all blank or whose species code is blank are skipped.
 * <p>
 * The result of the parse is a {@link Map} of Species codes to a set of six coefficients.
 * <p>
 * FIP Control index: 094
 * <p>
 * Example file: coe/REGVWU.COE
 *
 * @author Kevin Smith, Vivid Solutions
 * @see SimpleCoefficientParser1
 */
public class VolumeNetDecayWasteParser extends SimpleCoefficientParser1<String> {

	public VolumeNetDecayWasteParser() {
		super(String.class, 0, ControlKey.VOLUME_NET_DECAY_WASTE);
		this.speciesKey();
		this.coefficients(6, 9);
	}
}
