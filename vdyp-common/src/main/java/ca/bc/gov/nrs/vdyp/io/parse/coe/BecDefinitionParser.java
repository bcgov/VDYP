package ca.bc.gov.nrs.vdyp.io.parse.coe;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ca.bc.gov.nrs.vdyp.common.ControlKey;
import ca.bc.gov.nrs.vdyp.common.Utils;
import ca.bc.gov.nrs.vdyp.io.parse.common.LineParser;
import ca.bc.gov.nrs.vdyp.io.parse.common.ResourceParseException;
import ca.bc.gov.nrs.vdyp.io.parse.control.ControlMapSubResourceParser;
import ca.bc.gov.nrs.vdyp.io.parse.value.ValueParser;
import ca.bc.gov.nrs.vdyp.model.BecDefinition;
import ca.bc.gov.nrs.vdyp.model.BecLookup;
import ca.bc.gov.nrs.vdyp.model.Region;

/**
 * Parser for a BEC Definition data file. These files contain a list of lines, each of which defines one BEC Zone: an
 * alias (2 - 4 characters), a space, a one character region indicator
 * <ol>
 * <li>(cols 0-3) BEC Zone alias</li>
 * <li>(col 5) region ('C' (coastal) or 'I' (interior), or 'Z' (see below)</li>
 * <li>(cols 7-56) BEC Zone name</li>
 * </ol>
 * The file is terminated by a line containing a Z in the region indicator column. All lines up to that point are parsed
 * - there are no blank lines.
 * <p>
 * The "default" BEC Zone is ESSF (Englemann Sruce -SubAlpine Fir (sic)).
 * <p>
 * In addition, this parser determines whether each BEC Zone is a growth, decay or volume BEC Zone. All are growth zones
 * except AT and BG. All are volume zones except BG. All are decay zones.
 * <p>
 * FIP Control index: 009
 * <p>
 * Example file: coe/Becdef.dat
 *
 * @author Kevin Smith, Vivid Solutions
 * @see ControlMapSubResourceParser
 */
public class BecDefinitionParser implements ControlMapSubResourceParser<BecLookup> {

	/**
	 * Alias of the default BEC
	 */
	public static final String DEFAULT_BEC = "ESSF";

	LineParser lineParser = new LineParser() {

		@Override
		public boolean isStopSegment(List<String> segments) {
			return "Z".equalsIgnoreCase(segments.get(2));
		}

	}.strippedString(4, "alias").space(1).value(1, "region", ValueParser.REGION).space(1).strippedString("name");

	public static final Set<String> NON_GROWTH_BECS = Set.of("AT", "BG");
	public static final Set<String> NON_VOLUME_BECS = Set.of("BG");
	public static final Set<String> NON_DECAY_BECS = Set.of();

	@Override
	public BecLookup parse(InputStream is, Map<String, Object> control) throws IOException, ResourceParseException {
		List<BecDefinition> result = new ArrayList<>(16);
		result = lineParser.parse(is, result, (value, r, line) -> {
			String alias = (String) value.get("alias");
			Region region = (Region) value.get("region");
			String name = (String) value.get("name");
			var defn = new BecDefinition(alias, region, name);
			r.add(defn);

			return r;
		}, control);
		var defaultBec = result.stream().filter(bec -> bec.getAlias().equals(DEFAULT_BEC)).findAny()
				.orElseThrow(() -> new IllegalStateException("Could not find default BEC " + DEFAULT_BEC));

		checkDefaultBecType(defaultBec, NON_GROWTH_BECS, "growth");
		checkDefaultBecType(defaultBec, NON_DECAY_BECS, "decay");
		checkDefaultBecType(defaultBec, NON_VOLUME_BECS, "volume");

		// Map the BECs so they know what their contextual alternate BECs are
		result = result.stream().map(baseBec -> {
			var isGrowth = !NON_GROWTH_BECS.contains(baseBec.getAlias());
			var isDecay = !NON_VOLUME_BECS.contains(baseBec.getAlias());
			var isVolume = !NON_DECAY_BECS.contains(baseBec.getAlias());
			if (isGrowth && isDecay && isVolume) {
				return baseBec;
			}
			return new BecDefinition(
					baseBec, defaultBec, !NON_GROWTH_BECS.contains(baseBec.getAlias()),
					!NON_VOLUME_BECS.contains(baseBec.getAlias()), !NON_DECAY_BECS.contains(baseBec.getAlias())
			);
		}).toList();
		return new BecLookup(result);
	}

	private void checkDefaultBecType(BecDefinition defaultBec, Set<String> badBecs, String becTypeName) {
		if (badBecs.contains(defaultBec.getAlias())) {
			throw new IllegalStateException("Default BEC " + defaultBec + " is not a " + becTypeName + " BEC.");
		}
	}

	/**
	 * Get all the BECs mapped by their aliases
	 *
	 * @param control Control map containing the parsed BEC definitions.
	 * @return
	 */
	public static BecLookup getBecs(Map<String, Object> control) {
		return Utils.expectParsedControl(control, ControlKey.BEC_DEF, BecLookup.class);
	}

	@Override
	public ControlKey getControlKey() {
		return ControlKey.BEC_DEF;
	}
}
