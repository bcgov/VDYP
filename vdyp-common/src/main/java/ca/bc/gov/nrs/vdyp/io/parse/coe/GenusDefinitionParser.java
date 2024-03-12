package ca.bc.gov.nrs.vdyp.io.parse.coe;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import ca.bc.gov.nrs.vdyp.common.ControlKey;
import ca.bc.gov.nrs.vdyp.common.Utils;
import ca.bc.gov.nrs.vdyp.io.parse.common.LineParser;
import ca.bc.gov.nrs.vdyp.io.parse.common.ResourceParseException;
import ca.bc.gov.nrs.vdyp.io.parse.common.ResourceParseValidException;
import ca.bc.gov.nrs.vdyp.io.parse.control.ControlMapSubResourceParser;
import ca.bc.gov.nrs.vdyp.io.parse.value.ValueParseException;
import ca.bc.gov.nrs.vdyp.io.parse.value.ValueParser;
import ca.bc.gov.nrs.vdyp.model.GenusDefinition;

/**
 * Parser for a Genus (SP0; Species) Definition data file.
 *
 * This file contains records defining the species on which the system functions. Each record contains:
 * <ol>
 * <li>(cols 0-1) a species alias
 * <li>(cols 3-34) the name of the species
 * <li>(cols 36-37) - int - the "preference" value.
 * </ol>
 * The preference values define an ordering of the species from 1 to 16. If a line provides no value or the value 0, the
 * ordering is implicitly chosen to be the line number. Otherwise, a value from 1 - 16 must be provided and this value
 * is used as the ordering. In the end, all values from 1 - 16 must be used. Note that it's possible, when ordering is
 * explicitly given, for a given ordering to appear more than once; in this case, the last one present wins.
 * <p>
 * All lines in the file are read; there is no provision for blank lines (except the last line, if empty).
 * <p>
 * In the end, a value for each of the 16 Species must be provided, and each must have a distinct ordering value.
 * <p>
 * FIP Control index: 010
 * <p>
 * Example: coe/SP0DEF_v0.dat
 *
 * @author Kevin Smith, Vivid Solutions
 * @see ControlMapSubResourceParser
 */
public class GenusDefinitionParser implements ControlMapSubResourceParser<List<GenusDefinition>> {

	private int numSp0;

	private LineParser lineParser = new LineParser().strippedString(2, "alias").space(1).strippedString(32, "name")
			.space(1).value(
					2, "preference",
					(s, c) -> ValueParser.optional(ValueParser.INTEGER).parse(s)
							.flatMap(v -> v == 0 ? Optional.empty() : Optional.of(v))
			);

	public GenusDefinitionParser() {
		super();
		this.numSp0 = 16;
	}

	public GenusDefinitionParser(int numSp0) {
		super();
		this.numSp0 = numSp0;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<GenusDefinition> parse(InputStream is, Map<String, Object> control)
			throws IOException, ResourceParseException {
		GenusDefinition[] result = new GenusDefinition[numSp0];
		result = lineParser.parse(is, result, (value, r, line) -> {
			String alias = (String) value.get("alias");
			Optional<Integer> preference = (Optional<Integer>) value.get("preference");
			String name = (String) value.get("name");
			Integer lineNumber = (Integer) value.get(LineParser.LINE_NUMBER_KEY);

			var defn = new GenusDefinition(alias, preference, name);
			int p = preference.orElse(lineNumber);

			if (p > numSp0) {
				throw new ValueParseException(
						Integer.toString(p), String.format("Preference %d is larger than %d", p, numSp0)
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
		if (Arrays.stream(result).anyMatch(Objects::isNull)) {
			throw new ResourceParseValidException("Not all genus definitions were provided.");
		}
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
		return Utils.<List<GenusDefinition>>expectParsedControl(controlMap, ControlKey.SP0_DEF.name(), List.class);
	}

	public static List<String> getSpeciesAliases(final Map<String, Object> controlMap) {
		return getSpecies(controlMap).stream().map(GenusDefinition::getAlias).toList();
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

	public static Optional<Integer> getIndex(final String alias, final Map<String, Object> controlMap) {
		return Optional.of(getSpeciesAliases(controlMap).indexOf(alias) + 1).filter(x -> x != 0);
	}

	@Override
	public ControlKey getControlKey() {
		return ControlKey.SP0_DEF;
	}

}
