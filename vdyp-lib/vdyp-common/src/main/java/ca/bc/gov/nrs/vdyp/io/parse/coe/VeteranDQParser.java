package ca.bc.gov.nrs.vdyp.io.parse.coe;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import ca.bc.gov.nrs.vdyp.common.ControlKey;
import ca.bc.gov.nrs.vdyp.io.parse.common.LineParser;
import ca.bc.gov.nrs.vdyp.io.parse.common.ResourceParseException;
import ca.bc.gov.nrs.vdyp.io.parse.control.ControlMapSubResourceParser;
import ca.bc.gov.nrs.vdyp.io.parse.value.ControlledValueParser;
import ca.bc.gov.nrs.vdyp.io.parse.value.ValueParseException;
import ca.bc.gov.nrs.vdyp.io.parse.value.ValueParser;
import ca.bc.gov.nrs.vdyp.model.Coefficients;
import ca.bc.gov.nrs.vdyp.model.MatrixMap2;
import ca.bc.gov.nrs.vdyp.model.MatrixMap2Impl;
import ca.bc.gov.nrs.vdyp.model.Region;

/**
 * Parses a mapping from a Species and Region to a list of three coefficients. Each row contains
 * <ol>
 * <li>(cols 0-1): a Species identifier</li>
 * <li>(col 3): a Region indicator ('C' or 'I') or a blank</li>
 * <li>(cols 4-13, 14-23, 24-33) - floats - coefficients</li>
 * </ol>
 * If the Region value is blank, the line is applied to both Regions. All lines are read - no lines are skipped, except
 * the last line in the file if empty. All three coefficients must be present on each line.
 * <p>
 * FIP Control index: 097
 * <p>
 * Example file: coe/VETDQ2.DAT
 *
 * @author Kevin Smith, Vivid Solutions
 * @see ControlMapSubResourceParser
 */
public class VeteranDQParser implements ControlMapSubResourceParser<MatrixMap2<String, Region, Coefficients>> {
	public static final int NUM_COEFFICIENTS = 3;

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
				.multiValue(NUM_COEFFICIENTS, 10, COEFFICIENT_KEY, ValueParser.FLOAT);
	}

	LineParser lineParser;

	@Override
	public MatrixMap2<String, Region, Coefficients> parse(InputStream is, Map<String, Object> control)
			throws IOException, ResourceParseException {
		final var speciesIndicies = GenusDefinitionParser.getSpeciesAliases(control);
		final var regionIndicies = Arrays.asList(Region.values());

		MatrixMap2<String, Region, Coefficients> result = new MatrixMap2Impl<>(
				speciesIndicies, regionIndicies, (k1, k2) -> Coefficients.empty(NUM_COEFFICIENTS, 1)
		);
		lineParser.parse(is, result, (value, r, line) -> {
			var sp0 = (String) value.get(SPECIES_KEY);
			@SuppressWarnings("unchecked")
			var regions = ((Optional<Region>) value.get(REGION_KEY)).map(Collections::singletonList)
					.orElse(regionIndicies);

			@SuppressWarnings("unchecked")
			var coefficients = (List<Float>) value.get(COEFFICIENT_KEY);
			GenusDefinitionParser.checkSpecies(speciesIndicies, sp0);

			if (coefficients.size() < NUM_COEFFICIENTS) {
				throw new ValueParseException(null, "Expected " + NUM_COEFFICIENTS + " coefficients");
			}

			for (var region : regions) {
				r.put(sp0, region, new Coefficients(coefficients, 1));
			}

			return r;
		}, control);
		return result;
	}

	@Override
	public ControlKey getControlKey() {
		return ControlKey.VETERAN_LAYER_DQ;
	}

}
