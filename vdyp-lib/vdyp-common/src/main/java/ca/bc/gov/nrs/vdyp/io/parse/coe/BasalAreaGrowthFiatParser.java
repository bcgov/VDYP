package ca.bc.gov.nrs.vdyp.io.parse.coe;

import java.util.Map;

import ca.bc.gov.nrs.vdyp.common.ControlKey;
import ca.bc.gov.nrs.vdyp.io.parse.coe.base.GrowthFiatParser;

/**
 * Parser for Basal Area Growth Fiat model details data file.
 * <ol>
 * <li>(cols 0-2) int - region (either 1 or 2)</li>
 * <li>(cols 3-8, 9-14, 15-20, 21-26) float - age values</li>
 * <li>(cols 27-32, 33-38, 39-44, 45-50) float - coefficient values</li>
 * <li>(cols 51-56, 57-62, 63-68) float - mixed model coefficient values</li>
 * </ol>
 * The result of the parse is a {@link Map} from Region to a {@link GrowthFaitDetails} instance.
 * <p>
 * Lines with a blank or empty region are ignored. There must be exactly one line for region 1 (COASTAL) and one line
 * for region 2 (INTERIOR). No other region ids are supported. The age values must start with a value other than 0.0.
 * <p>
 * Control index: 111
 * <p>
 * Example file: coe/EMP111A1.PRM
 *
 * @author Michael Junkin, Vivid Solutions
 * @see GrowthFiatParser
 */
public class BasalAreaGrowthFiatParser extends GrowthFiatParser {

	@Override
	public ControlKey getControlKey() {
		return ControlKey.BA_GROWTH_FIAT;
	}
}
