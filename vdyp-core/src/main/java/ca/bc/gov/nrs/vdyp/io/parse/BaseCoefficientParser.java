package ca.bc.gov.nrs.vdyp.io.parse;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ca.bc.gov.nrs.vdyp.model.Coefficients;
import ca.bc.gov.nrs.vdyp.model.MatrixMap;
import ca.bc.gov.nrs.vdyp.model.Region;

/**
 * Parses a Coefficient data file.
 *
 * @author Kevin Smith, Vivid Solutions
 *
 */
public abstract class BaseCoefficientParser<T extends Coefficients, M extends MatrixMap<T>>
		implements ControlMapSubResourceParser<M> {

	public static final String SP0_KEY = "sp0";
	public static final String REGION_KEY = "region";
	public static final String COEFFICIENTS_KEY = "coefficients";
	public static final String UC_INDEX = "ucIndex";
	public static final String GROUP_INDEX = "groupIndex";

	int numCoefficients;

	List<String> metaKeys = new ArrayList<>();
	List<Function<Map<String, Object>, Collection<?>>> keyRanges = new ArrayList<>();
	private int expectedKeys;
	private final String controlKey;

	public BaseCoefficientParser(int expectedKeys, String controlKey) {
		super();
		this.expectedKeys = expectedKeys;
		this.lineParser = new LineParser() {

			@Override
			public boolean isStopSegment(List<String> segments) {
				return segments.get(0).isBlank();
			}

		};
		this.controlKey = controlKey;
	}

	public BaseCoefficientParser(String controlKey) {
		this(0, controlKey);
	}

	/**
	 * Add a key for the multimap
	 *
	 * @param <K>           type of the key
	 * @param length        length of the key in the file
	 * @param name          name of the key
	 * @param parser        Parser for the
	 * @param range         Function that returns the set of values based on the
	 *                      control map
	 * @param errorTemplate Error message if the parsed value is not in the given
	 *                      range, it will be formatted (see {@link String.format})
	 *                      with the erroneous value as a parameter.
	 * @return
	 */
	public <K> BaseCoefficientParser<T, M> key(
			int length, String name, ControlledValueParser<K> parser,
			Function<Map<String, Object>, Collection<?>> range, String errorTemplate
	) {
		if (expectedKeys > 0 && metaKeys.size() == expectedKeys) {
			throw new IllegalStateException(
					"Expected " + expectedKeys + " keys but " + name + " was key " + expectedKeys + 1
			);
		}
		var validParser = ControlledValueParser.validate(
				parser,
				(v, control) -> range.apply(control).contains(v) ? Optional.empty()
						: Optional.of(String.format(errorTemplate, v))
		);
		lineParser.value(length, name, validParser);
		metaKeys.add(name);
		keyRanges.add(range);
		return this;
	}

	public <K> BaseCoefficientParser<T, M>
			key(int length, String name, ControlledValueParser<K> parser, Collection<?> range, String errorTemplate) {
		return key(length, name, parser, (c) -> range, errorTemplate);
	}

	public BaseCoefficientParser<T, M> regionKey() {
		var regions = Arrays.asList(Region.values());
		return key(1, REGION_KEY, ValueParser.REGION, regions, "%s is not a valid region");
	}

	public BaseCoefficientParser<T, M> ucIndexKey() {
		var indicies = Arrays.asList(1, 2, 3, 4);
		return key(
				2, UC_INDEX, ValueParser.INTEGER, indicies, "%s is not a valid UC Index, should be 1 to 4 inclusive"
		);
	}

	public BaseCoefficientParser<T, M> groupIndexKey(int maxGroups) {
		var indicies = Stream.iterate(1, x -> x + 1).limit(maxGroups).toList();
		return key(
				3, GROUP_INDEX, ValueParser.INTEGER, indicies,
				"%s is not a valid Group Index, should be 1 to " + maxGroups + " inclusive"
		);
	}

	public BaseCoefficientParser<T, M> speciesKey(String name) {
		return key(
				2, name, ControlledValueParser.SPECIES, SP0DefinitionParser::getSpeciesAliases,
				"%s is not a valid species"
		);
	}

	public BaseCoefficientParser<T, M> speciesKey() {
		return speciesKey(SP0_KEY);
	}

	public BaseCoefficientParser<T, M> space(int length) {
		lineParser.space(length);
		return this;
	}

	public <K> BaseCoefficientParser<T, M> coefficients(int number, int length) {
		lineParser.multiValue(number, length, COEFFICIENTS_KEY, ValueParser.FLOAT);
		numCoefficients = number;
		return this;
	}

	protected LineParser lineParser;

	@Override
	public M parse(InputStream is, Map<String, Object> control) throws IOException, ResourceParseException {
		if (expectedKeys > 0 && metaKeys.size() != expectedKeys) {
			throw new IllegalStateException("Expected " + expectedKeys + " keys but there were " + metaKeys.size());
		}
		keyRanges.stream().map(x -> x.apply(control)).toList();
		@SuppressWarnings({ "unchecked", "rawtypes" })
		M result = (M) createMap((List) (keyRanges.stream().map(x -> x.apply(control)).toList()));

		lineParser.parse(is, result, (v, r) -> {
			var key = metaKeys.stream().map(v::get).collect(Collectors.toList()).toArray(Object[]::new);

			@SuppressWarnings("unchecked")
			var coe = getCoefficients((List<Float>) v.get(COEFFICIENTS_KEY));

			r.putM(coe, key);
			return r;
		}, control);
		return result;
	}

	protected abstract M createMap(List<Collection<?>> keyRanges);

	protected abstract T getCoefficients(List<Float> coefficients);

	@Override
	public String getControlKey() {
		return controlKey;
	}
}
