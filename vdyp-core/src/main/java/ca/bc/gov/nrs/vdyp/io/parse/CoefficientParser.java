package ca.bc.gov.nrs.vdyp.io.parse;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ca.bc.gov.nrs.vdyp.model.MatrixMap3;
import ca.bc.gov.nrs.vdyp.model.MatrixMap3Impl;

/**
 * Parses a Coefficient data file.
 *
 * @author Kevin Smith, Vivid Solutions
 *
 */
public class CoefficientParser implements ControlMapSubResourceParser<MatrixMap3<Integer, String, Integer, Float>> {
	public static final String BA_CONTROL_KEY = "COE_BA";
	public static final String DQ_CONTROL_KEY = "COE_DQ";

	public static final String BEC_KEY = "bec";
	public static final String COEFFICIENT_INDEX_KEY = "index";
	public static final String INDICATOR_KEY = "indicator";
	public static final String COEFFICIENT_KEY = "coefficient";

	public static final int NUM_COEFFICIENTS = 10;
	public static final int NUM_SPECIES = 16;

	final String controlKey;

	public CoefficientParser(String controlKey) {
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
	public MatrixMap3<Integer, String, Integer, Float> parse(InputStream is, Map<String, Object> control)
			throws IOException, ResourceParseException {
		var becAliases = BecDefinitionParser.getBecAliases(control);
		var coeIndecies = Stream.iterate(0, x -> x + 1).limit(NUM_COEFFICIENTS).collect(Collectors.toList());
		var speciesIndecies = Stream.iterate(1, x -> x + 1).limit(NUM_SPECIES).collect(Collectors.toList());
		MatrixMap3<Integer, String, Integer, Float> result = new MatrixMap3Impl<Integer, String, Integer, Float>(
				coeIndecies, becAliases, speciesIndecies
		);
		lineParser.parse(is, result, (v, r) -> {
			var bec = (String) v.get(BEC_KEY);
			var indicator = (int) v.get(INDICATOR_KEY);
			var index = (int) v.get(COEFFICIENT_INDEX_KEY);
			@SuppressWarnings("unchecked")
			var coefficients = (List<Float>) v.get(COEFFICIENT_KEY);

			if (!becAliases.contains(bec)) {
				throw new ValueParseException(bec, bec + " is not a valid BEC alias");
			}
			if (!coeIndecies.contains(index)) {
				throw new ValueParseException(Integer.toString(index), index + " is not a valid coefficient index");
			}

			for (int species = 0; species < NUM_SPECIES; species++) {
				float c;
				switch (indicator) {
				case 0:
				default:
					c = coefficients.get(0);
					break;
				case 1:
					c = coefficients.get(species);
					break;
				case 2:
					if (species == 0)
						c = coefficients.get(0);
					else
						c = coefficients.get(0) + coefficients.get(species);
					break;
				}
				r.put(index, bec, species + 1, c);
			}
			return r;
		}, control);
		return result;
	}

	@Override
	public String getControlKey() {
		return controlKey;
	}

}
