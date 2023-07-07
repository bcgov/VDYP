package ca.bc.gov.nrs.vdyp.io.parse;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import ca.bc.gov.nrs.vdyp.model.MatrixMap3;
import ca.bc.gov.nrs.vdyp.model.MatrixMap3Impl;
import ca.bc.gov.nrs.vdyp.model.NonprimaryHLCoefficients;
import ca.bc.gov.nrs.vdyp.model.Region;

/**
 * Parses an HL Nonprimary Coefficient data file.
 *
 * @author Kevin Smith, Vivid Solutions
 *
 */
public class HLNonprimaryCoefficientParser
		implements ControlMapSubResourceParser<MatrixMap3<String, String, Region, NonprimaryHLCoefficients>> {

	public static final String CONTROL_KEY = "HL_NONPRIMARY";

	public static final int NUM_COEFFICIENTS = 2;

	public static final String SPECIES_1_KEY = "species1";
	public static final String SPECIES_2_KEY = "species2";
	public static final String REGION_KEY = "region";
	public static final String EQUATION_KEY = "ieqn";
	public static final String COEFFICIENT_KEY = "coefficient";

	public HLNonprimaryCoefficientParser() {
		super();
		this.lineParser = new LineParser() {

			@Override
			public boolean isStopLine(String line) {
				return line.startsWith("   ");
			}

		}.value(2, SPECIES_1_KEY, ValueParser.STRING).space(1).value(2, SPECIES_2_KEY, ValueParser.STRING).space(1)
				.value(1, REGION_KEY, ValueParser.REGION).space(1).integer(1, EQUATION_KEY)
				.multiValue(NUM_COEFFICIENTS, 10, COEFFICIENT_KEY, ValueParser.FLOAT);
	}

	LineParser lineParser;

	@Override
	public MatrixMap3<String, String, Region, NonprimaryHLCoefficients>
			parse(InputStream is, Map<String, Object> control) throws IOException, ResourceParseException {
		final var regionIndicies = Arrays.asList(Region.values());
		final var speciesIndicies = GenusDefinitionParser.getSpeciesAliases(control);

		MatrixMap3<String, String, Region, NonprimaryHLCoefficients> result = new MatrixMap3Impl<String, String, Region, NonprimaryHLCoefficients>(
				speciesIndicies, speciesIndicies, regionIndicies
		);
		lineParser.parse(is, result, (v, r) -> {
			var sp0_1 = (String) v.get(SPECIES_1_KEY);
			var sp0_2 = (String) v.get(SPECIES_2_KEY);
			var ieqn = (Integer) v.get(EQUATION_KEY);
			var region = (Region) v.get(REGION_KEY);
			@SuppressWarnings("unchecked")
			var coefficients = (List<Float>) v.get(COEFFICIENT_KEY);
			GenusDefinitionParser.checkSpecies(speciesIndicies, sp0_1);
			GenusDefinitionParser.checkSpecies(speciesIndicies, sp0_2);

			if (coefficients.size() < NUM_COEFFICIENTS) {
				throw new ValueParseException(null, "Expected 2 coefficients"); // TODO handle this better
			}
			r.put(sp0_1, sp0_2, region, new NonprimaryHLCoefficients(coefficients, ieqn));

			return r;
		}, control);
		return result;
	}

	@Override
	public String getControlKey() {
		return CONTROL_KEY;
	}

}
