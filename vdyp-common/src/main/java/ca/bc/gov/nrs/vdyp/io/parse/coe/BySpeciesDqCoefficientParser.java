package ca.bc.gov.nrs.vdyp.io.parse.coe;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import ca.bc.gov.nrs.vdyp.common.ControlKey;
import ca.bc.gov.nrs.vdyp.common.HoldFirst;
import ca.bc.gov.nrs.vdyp.io.parse.common.LineParser;
import ca.bc.gov.nrs.vdyp.io.parse.common.ResourceParseException;
import ca.bc.gov.nrs.vdyp.io.parse.control.ControlMapSubResourceParser;
import ca.bc.gov.nrs.vdyp.io.parse.value.ValueParseException;
import ca.bc.gov.nrs.vdyp.io.parse.value.ValueParser;
import ca.bc.gov.nrs.vdyp.model.Coefficients;

/**
 * Parses a mapping from a Species to a list of three coefficients. Each row contains
 * <ol>
 * <li>(cols 0-1) an index (the letter "A" in column zero followed by an integer from 0 to 2 in column one.)</li>
 * <li>(cols 2-3) int - "indicator". This value is ignored.</li>
 * <li>(cols 4-12, 13-21, ...) float * 16 - a list of coefficient values. Not all values are required - see below.</li>
 * </ol>
 * If index is zero or one, the ith coefficient is the value used for Species identified by the number i (determined by
 * calling <code>GenusDefinitionParser.getSpeciesAliases</code> as usual.) If index is two, the first (and only)
 * coefficient given is used as the value for all Species.
 * <p>
 * For example, the values for Species with number s are (row w/index 0 coefficient s, row w/index 1 coefficient s, row
 * w/index 2 coefficient 0).
 * <p>
 * All lines are parsed, and there is no provision for blank lines.
 * <p>
 * The result of the parse is a map from Species to a (zero-based) three-element coefficient array.
 * <p>
 * FIP Control index: 060
 * <p>
 * Example file: coe/REGDQI04.COE
 *
 * @author Kevin Smith, Vivid Solutions
 * @see ControlMapSubResourceParser
 */
public class BySpeciesDqCoefficientParser implements ControlMapSubResourceParser<Map<String, Coefficients>> {

	public static final String COEFFICIENT_INDEX_KEY = "coefficientIndex";
	public static final String COEFFICIENTS_KEY = "coefficients";
	public static final String INDICATOR_KEY = "indicator";

	private static final int NUM_COEFFICIENTS = 3;
	private static final int NUM_SPECIES = 16;

	public BySpeciesDqCoefficientParser() {
		super();
		this.lineParser = new LineParser() {

			@Override
			public boolean isStopLine(String line) {
				return line.startsWith("   ");
			}

		}.space(1).value(1, COEFFICIENT_INDEX_KEY, ValueParser.INTEGER).value(2, INDICATOR_KEY, ValueParser.INTEGER)
				.multiValue(NUM_SPECIES, 9, COEFFICIENTS_KEY, ValueParser.FLOAT);
	}

	LineParser lineParser;

	@Override
	public Map<String, Coefficients> parse(InputStream is, Map<String, Object> control)
			throws IOException, ResourceParseException {

		Map<String, Coefficients> result = new HashMap<>();

		var sp0Aliases = GenusDefinitionParser.getSpeciesAliases(control);

		lineParser.parse(is, result, (value, r, lineNumber) -> {
			var index = (int) value.get(COEFFICIENT_INDEX_KEY);
			@SuppressWarnings("unchecked")
			var specCoefficients = (List<Float>) value.get(COEFFICIENTS_KEY);

			var specIt = sp0Aliases.iterator();
			var coeIt = specCoefficients.iterator();
			HoldFirst<Float> firstSpecCoe = new HoldFirst<>();
			while (specIt.hasNext()) {
				var specAlias = specIt.next();
				float coe;
				if (! (index == 0 || index == 1)) {
					coe = firstSpecCoe.get(coeIt::next);
				} else {
					try {
						coe = coeIt.next();
					} catch (NoSuchElementException ex) {
						throw new ValueParseException(
								MessageFormat.format("Line {0}: expected {1} coefficients", lineNumber, NUM_SPECIES), ex
						);
					}
				}
				result.computeIfAbsent(specAlias, x -> Coefficients.empty(NUM_COEFFICIENTS, 0)).setCoe(index, coe);
			}

			return r;
		}, control);
		return result;

	}

	@Override
	public ControlKey getControlKey() {
		return ControlKey.BY_SPECIES_DQ;
	}

}
