package ca.bc.gov.nrs.vdyp.io.parse.coe;

import java.util.Map;

import ca.bc.gov.nrs.vdyp.common.ControlKey;
import ca.bc.gov.nrs.vdyp.io.parse.coe.base.SimpleCoefficientParser1;

/**
 * Parser for a Veteran Layer Volume Adjust data file. Each line contains a Species code and an array of four
 * coefficients (one-based).
 * <ol>
 * <li>(cols 0-1) key - a species code</li>
 * <li>(col 2-10, 11-19, 20-28, 29-37) - float - coefficients</li>
 * </ol>
 * Exactly 16 data items (one per species) must be present. There can be no blank lines in the file, except possibly the
 * last (17th) line provided its empty.
 * <p>
 * The result of the parse is a {@link Map} of Species aliases (e.g., "AC") to a set of four coefficients.
 * <p>
 * FIP Control index: 096
 * <p>
 * Example file: coe/VETVOL1.DAT
 *
 * @author Kevin Smith, Vivid Solutions
 * @see SimpleCoefficientParser1
 */
public class VeteranLayerVolumeAdjustParser extends SimpleCoefficientParser1<String> {

	public VeteranLayerVolumeAdjustParser() {
		super(String.class, 1, ControlKey.VETERAN_LAYER_VOLUME_ADJUST);
		this.speciesKey();
		this.coefficients(4, 9);
	}

}
