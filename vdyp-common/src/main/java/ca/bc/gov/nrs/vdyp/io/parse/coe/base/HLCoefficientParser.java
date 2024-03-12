package ca.bc.gov.nrs.vdyp.io.parse.coe.base;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import ca.bc.gov.nrs.vdyp.common.ControlKey;
import ca.bc.gov.nrs.vdyp.io.parse.coe.GenusDefinitionParser;
import ca.bc.gov.nrs.vdyp.io.parse.common.LineParser;
import ca.bc.gov.nrs.vdyp.io.parse.common.ResourceParseException;
import ca.bc.gov.nrs.vdyp.io.parse.control.ControlMapSubResourceParser;
import ca.bc.gov.nrs.vdyp.io.parse.value.ValueParseException;
import ca.bc.gov.nrs.vdyp.io.parse.value.ValueParser;
import ca.bc.gov.nrs.vdyp.model.Coefficients;
import ca.bc.gov.nrs.vdyp.model.MatrixMap2;
import ca.bc.gov.nrs.vdyp.model.MatrixMap2Impl;
import ca.bc.gov.nrs.vdyp.model.Region;

/**
 * Parses an HL Coefficient data file.
 *
 * These files each contain a complete mapping of species aliases and regions to a (one-based) list of coefficients, and
 * therefore has 32 lines. Each row contains:
 * <ol>
 * <li>(cols 0-1) Species alias</li>
 * <li>(col 3) Region ('I' or 'C')</li>
 * <li>(cols 4-13, 14-23, ...) two - four floats in 10 character fields.
 * </ol>
 * All lines are read; there is no provision for blank lines. There may be multiple lines with the same Species and
 * Region values; the last one wins.
 * <p>
 * The file must populate #Species * #Regions (currently 32) values.
 * <p>
 * The result of the parse is a {@link MatrixMap2} of Coefficients indexed by first species, then region.
 * <p>
 * FIP Control indices: 050, 051, 052
 * <p>
 * Examples: coe/REGYHLP.COE, coe/REGYHLPA.COE, coe/REGYHLPB.DAT (respectively)
 *
 * @author Kevin Smith, Vivid Solutions
 * @see ControlMapSubResourceParser
 */
public abstract class HLCoefficientParser
		implements ControlMapSubResourceParser<MatrixMap2<String, Region, Coefficients>> {

	public static final int NUM_COEFFICIENTS_P2 = 2;
	public static final int NUM_COEFFICIENTS_P3 = 4;

	public static final String SP0_KEY = "sp0";
	public static final String REGION_KEY = "region";
	public static final String COEFFICIENT_KEY = "coefficient";

	private ControlKey controlKey;

	protected HLCoefficientParser(int numCoefficients, ControlKey controlKey) {
		super();
		this.lineParser = new LineParser() {

			@Override
			public boolean isStopLine(String line) {
				return line.startsWith("   ");
			}

		}.value(2, SP0_KEY, ValueParser.STRING).space(1).value(1, REGION_KEY, ValueParser.REGION)
				.multiValue(numCoefficients, 10, COEFFICIENT_KEY, ValueParser.FLOAT);
		this.controlKey = controlKey;
	}

	LineParser lineParser;

	@Override
	public MatrixMap2<String, Region, Coefficients> parse(InputStream is, Map<String, Object> control)
			throws IOException, ResourceParseException {
		final var regionIndicies = Arrays.asList(Region.values());
		final var speciesIndicies = GenusDefinitionParser.getSpeciesAliases(control);

		MatrixMap2<String, Region, Coefficients> result = new MatrixMap2Impl<>(
				speciesIndicies, regionIndicies, (k1, k2) -> Coefficients.empty(NUM_COEFFICIENTS_P3, 1)
		);
		lineParser.parse(is, result, (value, r, line) -> {
			var sp0 = (String) value.get(SP0_KEY);
			var region = (Region) value.get(REGION_KEY);
			@SuppressWarnings("unchecked")
			var coefficients = (List<Float>) value.get(COEFFICIENT_KEY);
			if (!speciesIndicies.contains(sp0)) {
				throw new ValueParseException(sp0, sp0 + " is not a valid species");
			}

			r.put(sp0, region, new Coefficients(coefficients, 1));

			return r;
		}, control);

		// TODO Consider requiring that all 32 combinations of spec and region are
		// provided instead of defaulting to all 0.

		return result;
	}

	@Override
	public ControlKey getControlKey() {
		return controlKey;
	}

}
