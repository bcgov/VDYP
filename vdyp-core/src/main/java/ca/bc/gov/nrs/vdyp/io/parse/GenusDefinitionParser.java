package ca.bc.gov.nrs.vdyp.io.parse;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import ca.bc.gov.nrs.vdyp.model.GenusDefinition;

/**
 * Parser for a Genus (SP0) Definition data file
 *
 * @author Kevin Smith, Vivid Solutions
 *
 */
public class GenusDefinitionParser implements ControlMapSubResourceParser<List<GenusDefinition>> {

	public static final String CONTROL_KEY = "SP0_DEF";

	private int num_sp0;

	LineParser lineParser = new LineParser().strippedString(2, "alias").space(1).strippedString(32, "name").space(1)
			.value(
					2, "preference",
					(s, c) -> ValueParser.optional(ValueParser.INTEGER).parse(s)
							.flatMap(v -> v == 0 ? Optional.empty() : Optional.of(v))
			);

	public GenusDefinitionParser() {
		super();
		this.num_sp0 = 16;
	}

	public GenusDefinitionParser(int num_sp0) {
		super();
		this.num_sp0 = num_sp0;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<GenusDefinition> parse(InputStream is, Map<String, Object> control)
			throws IOException, ResourceParseException {
		GenusDefinition[] result = new GenusDefinition[num_sp0];
		result = lineParser.parse(is, result, (v, r) -> {
			String alias = (String) v.get("alias");
			Optional<Integer> preference = (Optional<Integer>) v.get("preference");
			String name = (String) v.get("name");
			Integer lineNumber = (Integer) v.get(LineParser.LINE_NUMBER_KEY);

			var defn = new GenusDefinition(alias, preference, name);
			int p = preference.orElse(lineNumber);

			if (p > num_sp0) {
				throw new ValueParseException(
						Integer.toString(p), String.format("Preference %d is larger than %d", p, num_sp0)
				);
			}
			if (p < 1) {
				throw new ValueParseException(
						Integer.toString(p), String.format("Preference %d is less than %d", p, 0)
				);
			}
			if (r[p - 1] != null) {
				throw new ValueParseException(
						Integer.toString(p),
						String.format("Preference %d has already been set to %s", p, r[p - 1].getAlias())
				);
			}

			r[p - 1] = defn;
			return r;
		}, control);

		return Collections.unmodifiableList(Arrays.asList(result));
	}

	public static void checkSpecies(final List<String> speciesIndicies, final String sp0) throws ValueParseException {
		if (!speciesIndicies.contains(sp0)) {
			throw new ValueParseException(sp0, sp0 + " is not a valid genus (SP0)");
		}
	}

	public static void checkSpecies(final Map<String, Object> controlMap, final String sp0) throws ValueParseException {
		final var speciesIndicies = getSpeciesAliases(controlMap);
		checkSpecies(speciesIndicies, sp0);
	}

	public static List<GenusDefinition> getSpecies(final Map<String, Object> controlMap) {
		return ResourceParser
				.<List<GenusDefinition>>expectParsedControl(controlMap, GenusDefinitionParser.CONTROL_KEY, List.class);
	}

	public static List<String> getSpeciesAliases(final Map<String, Object> controlMap) {
		return getSpecies(controlMap).stream().map(GenusDefinition::getAlias).collect(Collectors.toList());
	}

	/**
	 * Get a species based on its index from 1
	 *
	 * @param index
	 * @param controlMap
	 * @return
	 */
	public static GenusDefinition getSpeciesByIndex(final int index, final Map<String, Object> controlMap) {

		return getSpecies(controlMap).get(index - 1);
	}

	@Override
	public String getControlKey() {
		return CONTROL_KEY;
	}

}
