package ca.bc.gov.nrs.vdyp.io.parse.coe;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import ca.bc.gov.nrs.vdyp.common.ControlKey;
import ca.bc.gov.nrs.vdyp.io.parse.common.LineParser;
import ca.bc.gov.nrs.vdyp.io.parse.common.ResourceParseException;
import ca.bc.gov.nrs.vdyp.io.parse.control.ControlMapSubResourceParser;
import ca.bc.gov.nrs.vdyp.io.parse.value.ValueParseException;
import ca.bc.gov.nrs.vdyp.io.parse.value.ValueParser;
import ca.bc.gov.nrs.vdyp.model.MatrixMap3;
import ca.bc.gov.nrs.vdyp.model.MatrixMap3Impl;
import ca.bc.gov.nrs.vdyp.model.Region;

/**
 * Parses an Upper Percentiles data file. Each line contains:
 * <ol>
 * <li>(cols 0-1) - a species code</li>
 * <li>(col 3) - a region indicator ('C' or 'I')</li>
 * <li>(col 4-10) - float - percentage value for BA (Basal Area)</li>
 * <li>(col 11-16) - float - percentage value for DQ (Quadratic Mean Diameter)</li>
 * </ol>
 * The result of the parse is a {@link MatrixMap3} indexed by Species Code, then Region, then either one (BA percentage)
 * or two (DQ percentage). Multiple lines with the same indices are legal, with the last entry winning.
 * <p>
 * FIP Control index: 043
 * <p>
 * Example file: coe/UPPERB02.COE
 *
 * @author Kevin Smith, Vivid Solutions
 * @see ControlMapSubResourceParser
 */
public class UpperCoefficientParser implements ControlMapSubResourceParser<MatrixMap3<Region, String, Integer, Float>> {

	public static final int BA = 1;
	public static final int DQ = 2;

	public static final String SP0_KEY = "sp0";
	public static final String REGION_KEY = "region";
	public static final String COEFFICIENT_KEY_1 = "coefficient_1";
	public static final String COEFFICIENT_KEY_2 = "coefficient_2";

	public static final int NUM_COEFFICIENTS = 2;

	private LineParser lineParser = new LineParser() {

		@Override
		public boolean isStopLine(String line) {
			return line.startsWith("   ");
		}

	}.value(2, SP0_KEY, ValueParser.STRING).space(1).value(1, REGION_KEY, ValueParser.REGION)
			.floating(7, COEFFICIENT_KEY_1).floating(6, COEFFICIENT_KEY_2);

	@Override
	public MatrixMap3<Region, String, Integer, Float> parse(InputStream is, Map<String, Object> control)
			throws IOException, ResourceParseException {
		var regionIndicies = Arrays.asList(Region.values());
		List<Integer> coeIndicies = Stream.iterate(1, x -> x + 1).limit(NUM_COEFFICIENTS).toList();
		final var speciesIndicies = GenusDefinitionParser.getSpeciesAliases(control);

		MatrixMap3<Region, String, Integer, Float> result = new MatrixMap3Impl<>(
				regionIndicies, speciesIndicies, coeIndicies, (k1, k2, k3) -> 0f
		);
		lineParser.parse(is, result, (value, r, line) -> {
			var sp0 = (String) value.get(SP0_KEY);
			var region = (Region) value.get(REGION_KEY);
			var coefficients = List.of((Float) value.get(COEFFICIENT_KEY_1), (Float) value.get(COEFFICIENT_KEY_2));
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
	public ControlKey getControlKey() {
		return ControlKey.UPPER_BA_BY_CI_S0_P;
	}

}
