package ca.bc.gov.nrs.vdyp.io.parse.coe.base;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import ca.bc.gov.nrs.vdyp.common.Utils;
import ca.bc.gov.nrs.vdyp.io.parse.coe.BecDefinitionParser;
import ca.bc.gov.nrs.vdyp.io.parse.coe.GenusDefinitionParser;
import ca.bc.gov.nrs.vdyp.io.parse.common.LineParser;
import ca.bc.gov.nrs.vdyp.io.parse.common.ResourceParseException;
import ca.bc.gov.nrs.vdyp.io.parse.control.ControlMapSubResourceParser;
import ca.bc.gov.nrs.vdyp.io.parse.value.ValueParseException;
import ca.bc.gov.nrs.vdyp.io.parse.value.ValueParser;
import ca.bc.gov.nrs.vdyp.model.Coefficients;
import ca.bc.gov.nrs.vdyp.model.MatrixMap3;
import ca.bc.gov.nrs.vdyp.model.MatrixMap3Impl;

public abstract class UtilComponentParser
		implements ControlMapSubResourceParser<MatrixMap3<Integer, String, String, Coefficients>> {

	// "07.5", "12.5", "17.5", "22.5"
	public final int numCoefficients;

	public static final String UC_KEY = "uc";
	public static final String SPECIES_KEY = "species";
	public static final String REGION_KEY = "region";
	public static final String BEC_SCOPE_KEY = "becScope";
	public static final String COEFFICIENT_KEY = "coefficient";

	protected UtilComponentParser(int numCoefficients, int gap, String... ucCodes) {
		super();
		this.numCoefficients = numCoefficients;
		this.lineParser = new LineParser() {

			@Override
			public boolean isIgnoredLine(String line) {
				return Utils.nullOrPrefixBlank(line, 4);
			}

		}.value(4, UC_KEY, ValueParser.indexParser("UC", 1, ucCodes)).space(gap)
				.value(2, SPECIES_KEY, ValueParser.STRING).space(1).value(4, BEC_SCOPE_KEY, ValueParser.STRING)
				.multiValue(numCoefficients, 10, COEFFICIENT_KEY, ValueParser.FLOAT);
	}

	LineParser lineParser;

	@Override
	public MatrixMap3<Integer, String, String, Coefficients> parse(InputStream is, Map<String, Object> control)
			throws IOException, ResourceParseException {
		final var becIndices = BecDefinitionParser.getBecs(control).getBecAliases();
		final var speciesIndicies = GenusDefinitionParser.getSpeciesAliases(control);
		final var ucIndices = Arrays.asList(1, 2, 3, 4);

		MatrixMap3<Integer, String, String, Coefficients> result = new MatrixMap3Impl<>(
				ucIndices, speciesIndicies, becIndices, (k1, k2, k3) -> Coefficients.empty(numCoefficients, 1)
		);
		lineParser.parse(is, result, (v, r, line) -> {
			var uc = (Integer) v.get(UC_KEY);
			var sp0 = (String) v.get(SPECIES_KEY);
			var scope = (String) v.get(BEC_SCOPE_KEY);

			var becs = BecDefinitionParser.getBecs(control).getBecsForScope(scope);
			if (becs.isEmpty()) {
				throw new ValueParseException(scope, "Could not find any BECs for scope " + scope);
			}

			@SuppressWarnings("unchecked")
			var coefficients = (List<Float>) v.get(COEFFICIENT_KEY);
			GenusDefinitionParser.checkSpecies(speciesIndicies, sp0);

			if (coefficients.size() < numCoefficients) {
				throw new ValueParseException(null, "Expected " + numCoefficients + " coefficients");
			}
			for (var bec : becs) {
				r.put(uc, sp0, bec.getAlias(), new Coefficients(coefficients, 1));
			}

			return r;
		}, control);
		return result;
	}

}
