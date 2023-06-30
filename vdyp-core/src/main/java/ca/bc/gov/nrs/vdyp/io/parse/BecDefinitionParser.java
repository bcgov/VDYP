package ca.bc.gov.nrs.vdyp.io.parse;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.bc.gov.nrs.vdyp.model.BecDefinition;
import ca.bc.gov.nrs.vdyp.model.BecLookup;
import ca.bc.gov.nrs.vdyp.model.Region;
import ca.bc.gov.nrs.vdyp.model.BecLookup.Substitution;

/**
 * Parser for a BEC Definition data file
 *
 * @author Kevin Smith, Vivid Solutions
 *
 */
public class BecDefinitionParser implements ControlMapSubResourceParser<BecLookup> {

	public static final String CONTROL_KEY = "BEC_DEF";

	LineParser lineParser = new LineParser() {

		@Override
		public boolean isStopSegment(List<String> segments) {
			return "Z".equalsIgnoreCase(segments.get(2));
		}

	}.strippedString(4, "alias").space(1).value(1, "region", ValueParser.REGION).space(1).strippedString("name");

	private static final Map<String, Integer> GROWTH_INDEX = new HashMap<>();
	private static final Map<String, Integer> VOLUME_INDEX = new HashMap<>();
	private static final Map<String, Integer> DECAY_INDEX = new HashMap<>();
	static {
		GROWTH_INDEX.put("AT", 0);
		GROWTH_INDEX.put("BG", 0);
		GROWTH_INDEX.put("BWBS", 1);
		GROWTH_INDEX.put("CDF", 2);
		GROWTH_INDEX.put("CWH", 3);
		GROWTH_INDEX.put("ESSF", 4);
		GROWTH_INDEX.put("ICH", 5);
		GROWTH_INDEX.put("IDF", 6);
		GROWTH_INDEX.put("MH", 7);
		GROWTH_INDEX.put("MS", 8);
		GROWTH_INDEX.put("PP", 9);
		GROWTH_INDEX.put("SBPS", 10);
		GROWTH_INDEX.put("SBS", 11);
		GROWTH_INDEX.put("SWB", 12);

		VOLUME_INDEX.put("AT", 1);
		VOLUME_INDEX.put("BG", 0);
		VOLUME_INDEX.put("BWBS", 2);
		VOLUME_INDEX.put("CDF", 3);
		VOLUME_INDEX.put("CWH", 4);
		VOLUME_INDEX.put("ESSF", 5);
		VOLUME_INDEX.put("ICH", 6);
		VOLUME_INDEX.put("IDF", 7);
		VOLUME_INDEX.put("MH", 8);
		VOLUME_INDEX.put("MS", 9);
		VOLUME_INDEX.put("PP", 10);
		VOLUME_INDEX.put("SBPS", 11);
		VOLUME_INDEX.put("SBS", 12);
		VOLUME_INDEX.put("SWB", 13);

		DECAY_INDEX.put("AT", 1);
		DECAY_INDEX.put("BG", 2);
		DECAY_INDEX.put("BWBS", 3);
		DECAY_INDEX.put("CDF", 4);
		DECAY_INDEX.put("CWH", 5);
		DECAY_INDEX.put("ESSF", 6);
		DECAY_INDEX.put("ICH", 7);
		DECAY_INDEX.put("IDF", 8);
		DECAY_INDEX.put("MH", 9);
		DECAY_INDEX.put("MS", 10);
		DECAY_INDEX.put("PP", 11);
		DECAY_INDEX.put("SBPS", 12);
		DECAY_INDEX.put("SBS", 13);
		DECAY_INDEX.put("SWB", 14);
	}

	@Override
	public BecLookup parse(InputStream is, Map<String, Object> control) throws IOException, ResourceParseException {
		List<BecDefinition> result = new ArrayList<>(16);
		result = lineParser.parse(is, result, (v, r) -> {
			String alias = (String) v.get("alias");
			Region region = (Region) v.get("region");
			String name = (String) v.get("name");
			var defn = new BecDefinition(
					alias, region, name, GROWTH_INDEX.get(alias), VOLUME_INDEX.get(alias), DECAY_INDEX.get(alias)
			);
			r.add(defn);
			return r;
		}, control);

		return new BecLookup(result);
	}

	/**
	 * Get all the BECs mapped by their aliases
	 *
	 * @param control Control map containing the parsed BEC definitions.
	 * @return
	 */
	public static BecLookup getBecs(Map<String, Object> control) {
		return ResourceParser.expectParsedControl(control, CONTROL_KEY, BecLookup.class);
	}

	/**
	 * Get all the BECs in the specified region
	 *
	 * @param control Control map containing the parsed BEC definitions.
	 * @return
	 */
	public static Collection<BecDefinition> getBecsByRegion(Map<String, Object> control, Region region) {
		return getBecs(control).getBecsForRegion(region, Substitution.PARTIAL_FILL_OK);
	}

	/**
	 * Get all the aliases for defined BECs
	 *
	 * @param control Control map containing the parsed BEC definitions.
	 * @return
	 */
	public static Collection<String> getBecAliases(Map<String, Object> control) {
		return getBecs(control).getBecs(Substitution.PARTIAL_FILL_OK).stream().map(BecDefinition::getAlias).toList();
	}

	/**
	 * Find a set of BECs for the given scope. If the scope is blank, that's all
	 * BECs, if it's a Region alias, it's all BECs for that region, otherwise its
	 * treated as a BEC alias and the BEC matching it is returned.
	 *
	 * @param control Control map containing the parsed BEC definitions.
	 * @param scope   The scope to match
	 * @return A collection of matching BECs, or the empty set if none match.
	 */
	public static Collection<BecDefinition> getBecsByScope(Map<String, Object> control, String scope) {
		return getBecs(control).getBecsForScope(scope, Substitution.PARTIAL_FILL_OK);
	}

	@Override
	public String getControlKey() {
		return CONTROL_KEY;
	}
}
