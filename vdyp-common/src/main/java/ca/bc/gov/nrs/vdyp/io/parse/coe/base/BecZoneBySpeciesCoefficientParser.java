package ca.bc.gov.nrs.vdyp.io.parse.coe.base;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

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
 * Base class for parsers of configuration files whose lines contain a BEC Zone
 * Alias, an integer index, an indicator flag and a list of 16 coefficients (one
 * per species) and generates a MatrixMap2 indexed by BEC Zone alias and then
 * sp0 alias of coefficients, one per index.
 *
 * Indices range from 0 to a parameterized value (7, for example).
 *
 * @author Michael Junkin, Vivid Solutions
 * @see ControlMapSubResourceParser
 */
public abstract class BecZoneBySpeciesCoefficientParser
		implements ControlMapSubResourceParser<MatrixMap2<String, String, Coefficients>> {

	public static final String BEC_ZONE_ID_KEY = "BecId";
	public static final String INDEX_KEY = "index";
	public static final String INDICATOR_KEY = "indicator";
	public static final String COEFFICIENTS_KEY = "coefficients";

	private final int nCoefficients;
	private static final int NUM_SPECIES = 16;

	protected BecZoneBySpeciesCoefficientParser(int nCoefficients) {

		this.nCoefficients = nCoefficients;

		this.lineParser = new LineParser() {

			@Override
			public boolean isIgnoredLine(String line) {
				return line.substring(0, Math.min(4, line.length())).trim().length() == 0;
			}

		}.value(4, BEC_ZONE_ID_KEY, ValueParser.STRING).space(2).value(1, INDEX_KEY, ValueParser.INTEGER)
				.value(2, INDICATOR_KEY, ValueParser.INTEGER)
				.multiValue(NUM_SPECIES, 8, COEFFICIENTS_KEY, ValueParser.FLOAT);
	}

	LineParser lineParser;

	@Override
	public MatrixMap2<String, String, Coefficients> parse(InputStream is, Map<String, Object> control)
			throws IOException, ResourceParseException {

		var becAliases = BecDefinitionParser.getBecs(control).getBecAliases();
		var sp0Aliases = GenusDefinitionParser.getSpeciesAliases(control);
		MatrixMap2<String, String, Coefficients> result = new MatrixMap2Impl<>(
				becAliases, sp0Aliases, (k1, k2) -> Coefficients.empty(nCoefficients, 0)
		);

		lineParser.parse(is, result, (value, r, lineNumber) -> {
			var becZoneId = (String) value.get(BEC_ZONE_ID_KEY);
			var index = (int) value.get(INDEX_KEY);
			var indicator = (int) value.get(INDICATOR_KEY);

			@SuppressWarnings("unchecked")
			var specCoefficients = (List<Float>) value.get(COEFFICIENTS_KEY);

			if (index < 0 || index >= nCoefficients) {
				throw new ValueParseException(
						MessageFormat.format(
								"Line {0}: Index value {1} is out of range; expecting a value from 0 to {2}",
								lineNumber, index, nCoefficients
						)
				);
			}

			if (indicator < 0 || indicator > 1) {
				throw new ValueParseException(
						MessageFormat.format(
								"Line {0}: Indicator value {1} is out of range; expecting either 0 or 1", lineNumber,
								indicator
						)
				);
			}

			BecDefinitionParser.getBecs(control).get(becZoneId).orElseThrow(
					() -> new ValueParseException("BEC Zone Id " + becZoneId + " is not a recognized BEC Zone")
			);

			var specIt = sp0Aliases.iterator();

			int coefficientIndex = 0;
			while (specIt.hasNext()) {

				Coefficients coefficients = r.get(becZoneId, specIt.next());
				Float coe = specCoefficients.get(coefficientIndex);
				coefficients.setCoe(index, coe);

				if (indicator == 0)
					break;

				coefficientIndex += 1;
			}

			return r;
		}, control);

		return result;
	}

	@Override
	public ControlKey getControlKey() {
		return ControlKey.BA_GROWTH_EMPIRICAL;
	}
}
