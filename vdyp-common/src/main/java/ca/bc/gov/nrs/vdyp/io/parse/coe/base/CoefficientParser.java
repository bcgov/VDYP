package ca.bc.gov.nrs.vdyp.io.parse.coe.base;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.function.IntFunction;

import ca.bc.gov.nrs.vdyp.common.ControlKey;
import ca.bc.gov.nrs.vdyp.io.parse.coe.BecDefinitionParser;
import ca.bc.gov.nrs.vdyp.io.parse.coe.GenusDefinitionParser;
import ca.bc.gov.nrs.vdyp.io.parse.common.LineParser;
import ca.bc.gov.nrs.vdyp.io.parse.common.ResourceParseException;
import ca.bc.gov.nrs.vdyp.io.parse.control.ControlMapSubResourceParser;
import ca.bc.gov.nrs.vdyp.io.parse.value.ValueParseException;
import ca.bc.gov.nrs.vdyp.io.parse.value.ValueParser;
import ca.bc.gov.nrs.vdyp.model.Coefficients;
import ca.bc.gov.nrs.vdyp.model.MatrixMap2;
import ca.bc.gov.nrs.vdyp.model.MatrixMap2Impl;

/**
 * Parses a Coefficient data file. This builds a MatrixMap2 indexed by BEC Zone and then Species. Each map entry
 * contains nine coefficients, 0-based. The coefficents for a given BEC Zone and Species are given in the nth column of
 * the data file, for the rows whose first 4 columns identify the BEC Zone. The seventh column is a digit from indicates
 * the index, running from 0 through 8. Following this is an Indicator (a integer ranging from 0 to 2) and 16 float
 * values, one per species. The mth column is the value for the species numbered m, determined from the speciesAliases
 * supplied in the given control map. For example, species "C" has m == 3 since it is fourth in the list of Species.
 * <p>
 * For example, the row starting "PP A4 x" contains the Species values for index 4 of BEC Zone PP.
 * <p>
 * The indicator 0 sets the value for all Species in the row to the first float value - all others are ignored.
 * <p>
 * The indicator 1 sets the value for Species m to the value of the mth float value minus the value of the first float
 * value (unless m == 0, in which case the value is just the first float value.)
 * <p>
 * The indicator 2 sets the value for Species m to the mth float value.
 * <p>
 * If the BEC Zone on a given line is empty, the line is considered blank as is skipped.
 * <p>
 * FIP Control index: 040, 041
 * <p>
 * Example file: coe/REGBA25.coe
 *
 * @author Kevin Smith, Vivid Solutions
 * @see ControlMapSubResourceParser
 */
public abstract class CoefficientParser
		implements ControlMapSubResourceParser<MatrixMap2<String, String, Coefficients>> {

	public static final String BEC_KEY = "bec";
	public static final String COEFFICIENT_INDEX_KEY = "index";
	public static final String INDICATOR_KEY = "indicator";
	public static final String COEFFICIENT_KEY = "coefficient";

	public static final int NUM_COEFFICIENTS = 10;
	public static final int NUM_SPECIES = 16;

	private final ControlKey controlKey;

	protected CoefficientParser(ControlKey controlKey) {
		this.controlKey = controlKey;
	}

	LineParser lineParser = new LineParser() {

		@Override
		public boolean isStopLine(String line) {
			return line.startsWith("   ");
		}

	}.value(4, BEC_KEY, ValueParser.STRING).space(2).value(1, COEFFICIENT_INDEX_KEY, ValueParser.INTEGER)
			.value(2, INDICATOR_KEY, ValueParser.INTEGER)
			.multiValue(NUM_SPECIES, 8, COEFFICIENT_KEY, ValueParser.FLOAT);

	@Override
	public MatrixMap2<String, String, Coefficients> parse(InputStream is, Map<String, Object> control)
			throws IOException, ResourceParseException {
		var becAliases = BecDefinitionParser.getBecs(control).getBecAliases();
		var speciesIndecies = GenusDefinitionParser.getSpeciesAliases(control);
		MatrixMap2<String, String, Coefficients> result = new MatrixMap2Impl<>(
				becAliases, speciesIndecies, (k1, k2) -> Coefficients.empty(NUM_COEFFICIENTS, 0)
		);
		IntFunction<String> speciesLookup = index -> GenusDefinitionParser.getSpeciesByIndex(index, control).getAlias();

		lineParser.parse(is, result, (value, r, line) -> {
			var bec = (String) value.get(BEC_KEY);
			var indicator = (int) value.get(INDICATOR_KEY);
			var index = (int) value.get(COEFFICIENT_INDEX_KEY);
			@SuppressWarnings("unchecked")
			var coefficients = (List<Float>) value.get(COEFFICIENT_KEY);

			if (!becAliases.contains(bec)) {
				throw new ValueParseException(bec, bec + " is not a valid BEC alias");
			}
			if (index < 0 || index >= NUM_COEFFICIENTS) {
				throw new ValueParseException(Integer.toString(index), index + " is not a valid coefficient index");
			}

			for (int species = 0; species < speciesIndecies.size(); species++) {
				float c;
				switch (indicator) {
				case 2:
					c = coefficients.get(species);
					break;
				case 1:
					if (species == 0)
						c = coefficients.get(0);
					else
						c = coefficients.get(0) + coefficients.get(species);
					break;
				case 0:
				default:
					c = coefficients.get(0);
					break;
				}
				r.get(bec, speciesLookup.apply(species + 1)).setCoe(index, c);
			}
			return r;
		}, control);
		return result;
	}

	@Override
	public ControlKey getControlKey() {
		return controlKey;
	}

}
