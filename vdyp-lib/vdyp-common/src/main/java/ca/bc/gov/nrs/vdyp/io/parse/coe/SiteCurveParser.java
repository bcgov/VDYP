package ca.bc.gov.nrs.vdyp.io.parse.coe;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import ca.bc.gov.nrs.vdyp.common.ControlKey;
import ca.bc.gov.nrs.vdyp.common.ExpectationDifference;
import ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexEquation;
import ca.bc.gov.nrs.vdyp.io.parse.common.LineParser;
import ca.bc.gov.nrs.vdyp.io.parse.common.ResourceParseException;
import ca.bc.gov.nrs.vdyp.io.parse.common.ResourceParseValidException;
import ca.bc.gov.nrs.vdyp.io.parse.control.OptionalControlMapSubResourceParser;
import ca.bc.gov.nrs.vdyp.model.MatrixMap2;
import ca.bc.gov.nrs.vdyp.model.MatrixMap2Impl;
import ca.bc.gov.nrs.vdyp.model.Region;

/**
 * Parses a Site Curve data file.
 *
 * These files have multiple lines, each containing a key followed by two integers. The key may a Species code or some
 * other value. All 16 Species codes are required. Any number, including zero, of additional codes are permitted.
 * <ol>
 * <li>(cols 0-2) key - often, but not necessarily, a Species code.</li>
 * <li>(col 3-5) int - coastal site curve for given key</li>
 * <li>(col 6-8) int - interior site curve for given key</li>
 * </ol>
 * The result of the parse is a {@link Map} of keys (e.g., Species codes) to a pair of site curve numbers, one for each
 * region.
 * <p>
 * A line starting with ## terminates the parsing; anything can appear in lines following this. Lines that are empty,
 * contain only white space, or start with "# " or " " are considered blank lines; in the latter case, any data
 * following these characters is ignored. Prior to "##" (or the end of the file, if missing), there must be a definition
 * for each of the 16 species. There can't be more than 200 definitions in total.
 * <p>
 * FIP Control index: 025
 * <p>
 * Example: coe/SIEQN.PRM
 *
 * @see OptionalControlMapSubResourceParser
 * @author Kevin Smith, Vivid Solutions
 */
public class SiteCurveParser
		implements OptionalControlMapSubResourceParser<MatrixMap2<String, Region, SiteIndexEquation>> {
	public static final String SPECIES_KEY = "species";
	public static final String REGION_1_KEY = "region1";
	public static final String REGION_2_KEY = "region2";

	private LineParser lineParser = new LineParser() {

		@Override
		public boolean isStopLine(String line) {
			return line.startsWith("##");
		}

		@Override
		public boolean isIgnoredLine(String line) {
			return line.isBlank() || line.startsWith("# ") || line.startsWith("  ");
		}

	}.strippedString(3, SPECIES_KEY).integer(3, REGION_1_KEY).integer(3, REGION_2_KEY);

	@Override
	public MatrixMap2<String, Region, SiteIndexEquation> parse(InputStream is, Map<String, Object> control)
			throws IOException, ResourceParseException {
		Map<String, int[]> result = new HashMap<>();
		lineParser.parse(is, result, (value, r, line) -> {
			var species = (String) value.get(SPECIES_KEY);
			var region1Value = (int) value.get(REGION_1_KEY);
			var region2Value = (int) value.get(REGION_2_KEY);

			r.put(species, new int[] { region1Value, region2Value });

			return r;
		}, control);
		final var sp0List = GenusDefinitionParser.getSpeciesAliases(control);

		var missing = ExpectationDifference.difference(result.keySet(), sp0List).getMissing();
		if (!missing.isEmpty()) {
			throw new ResourceParseValidException("Missing expected entries for " + String.join(", ", missing));
		}

		return new MatrixMap2Impl<>(
				result.keySet(), Arrays.asList(Region.values()),
				(id, region) -> SiteIndexEquation.getByIndex(result.get(id)[region.getIndex() - 1])
		);
	}

	@Override
	public ControlKey getControlKey() {
		return ControlKey.SITE_CURVE_NUMBERS;
	}

	@Override
	public MatrixMap2<String, Region, SiteIndexEquation> defaultResult() {
		return new MatrixMap2Impl<>(Collections.emptySet(), Collections.emptySet(), (x, y) -> null);
	}

}
