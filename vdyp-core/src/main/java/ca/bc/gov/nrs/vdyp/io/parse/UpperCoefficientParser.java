package ca.bc.gov.nrs.vdyp.io.parse;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ca.bc.gov.nrs.vdyp.model.MatrixMap3;
import ca.bc.gov.nrs.vdyp.model.MatrixMap3Impl;
import ca.bc.gov.nrs.vdyp.model.Region;

/**
 * Parses a Coefficient data file.
 *
 * @author Kevin Smith, Vivid Solutions
 *
 */
public class UpperCoefficientParser implements ControlMapSubResourceParser<MatrixMap3<Region, String, Integer, Float>> {
	public static final int BA = 1;
	public static final int DQ = 2;

	public static final String CONTROL_KEY = "UPPER_BA_BY_CI_S0_P";

	public static final String SP0_KEY = "sp0";
	public static final String REGION_KEY = "region";
	public static final String COEFFICIENT_KEY = "coefficient";

	public static final int NUM_COEFFICIENTS = 2;

	LineParser lineParser = new LineParser() {

		@Override
		public boolean isStopLine(String line) {
			return line.startsWith("   ");
		}

	}.value(2, SP0_KEY, ValueParser.STRING).space(1).value(1, REGION_KEY, ValueParser.REGION)
			.multiValue(NUM_COEFFICIENTS, 7, COEFFICIENT_KEY, ValueParser.FLOAT);

	@Override
	public MatrixMap3<Region, String, Integer, Float> parse(InputStream is, Map<String, Object> control)
			throws IOException, ResourceParseException {
		var regionIndicies = Arrays.asList(Region.values());
		List<Integer> coeIndicies = Stream.iterate(1, x -> x + 1).limit(NUM_COEFFICIENTS).collect(Collectors.toList());
		final var speciesIndicies = GenusDefinitionParser.getSpeciesAliases(control);

		MatrixMap3<Region, String, Integer, Float> result = new MatrixMap3Impl<Region, String, Integer, Float>(
				regionIndicies, speciesIndicies, coeIndicies
		);
		lineParser.parse(is, result, (v, r) -> {
			var sp0 = (String) v.get(SP0_KEY);
			var region = (Region) v.get(REGION_KEY);
			@SuppressWarnings("unchecked")
			var coefficients = (List<Float>) v.get(COEFFICIENT_KEY);
			if (!speciesIndicies.contains(sp0)) {
				throw new ValueParseException(sp0, sp0 + " is not a valid species");
			}

			for (int coeIndex = 0; coeIndex < NUM_COEFFICIENTS; coeIndex++) {
				r.put(region, sp0, coeIndex + 1, coefficients.get(coeIndex));
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
