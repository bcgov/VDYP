package ca.bc.gov.nrs.vdyp.io.parse;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import ca.bc.gov.nrs.vdyp.model.Coefficients;
import ca.bc.gov.nrs.vdyp.model.MatrixMap2;
import ca.bc.gov.nrs.vdyp.model.MatrixMap2Impl;
import ca.bc.gov.nrs.vdyp.model.Region;

public class VeteranDQParser implements ControlMapSubResourceParser<MatrixMap2<String, Region, Coefficients>> {
	public static final String CONTROL_KEY = "VETERAN_LAYER_DQ";

	public final static int numCoefficients = 3;

	public static final String SPECIES_KEY = "species";
	public static final String REGION_KEY = "region";
	public static final String COEFFICIENT_KEY = "coefficient";

	public VeteranDQParser() {
		super();
		this.lineParser = new LineParser() {

			@Override
			public boolean isStopLine(String line) {
				return line.startsWith("    ");
			}

		}.value(2, SPECIES_KEY, ValueParser.STRING).space(1)
				.value(1, REGION_KEY, ControlledValueParser.optional(ValueParser.REGION))
				.multiValue(numCoefficients, 10, COEFFICIENT_KEY, ValueParser.FLOAT);
	}

	LineParser lineParser;

	@Override
	public MatrixMap2<String, Region, Coefficients> parse(InputStream is, Map<String, Object> control)
			throws IOException, ResourceParseException {
		final var speciesIndicies = GenusDefinitionParser.getSpeciesAliases(control);
		final var regionIndicies = Arrays.asList(Region.values());

		MatrixMap2<String, Region, Coefficients> result = new MatrixMap2Impl<String, Region, Coefficients>(
				speciesIndicies, regionIndicies
		);
		lineParser.parse(is, result, (v, r) -> {
			var sp0 = (String) v.get(SPECIES_KEY);
			@SuppressWarnings("unchecked")
			var regions = ((Optional<Region>) v.get(REGION_KEY)).map(Collections::singletonList).orElse(regionIndicies);

			@SuppressWarnings("unchecked")
			var coefficients = (List<Float>) v.get(COEFFICIENT_KEY);
			GenusDefinitionParser.checkSpecies(speciesIndicies, sp0);

			if (coefficients.size() < numCoefficients) {
				throw new ValueParseException(null, "Expected " + numCoefficients + " coefficients");
			}

			for (var region : regions) {
				r.put(sp0, region, new Coefficients(coefficients, 1));
			}

			return r;
		}, control);
		return result;
	}

	@Override
	public String getControlKey() {
		return CONTROL_KEY;
	}

}
