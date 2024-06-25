package ca.bc.gov.nrs.vdyp.io.parse.coe.base;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import ca.bc.gov.nrs.vdyp.common.ControlKey;
import ca.bc.gov.nrs.vdyp.common.Utils;
import ca.bc.gov.nrs.vdyp.io.parse.coe.GenusDefinitionParser;
import ca.bc.gov.nrs.vdyp.io.parse.common.LineParser;
import ca.bc.gov.nrs.vdyp.io.parse.common.ResourceParseException;
import ca.bc.gov.nrs.vdyp.io.parse.common.ResourceParseValidException;
import ca.bc.gov.nrs.vdyp.io.parse.control.ControlMapSubResourceParser;
import ca.bc.gov.nrs.vdyp.io.parse.value.ControlledValueParser;
import ca.bc.gov.nrs.vdyp.io.parse.value.ValueParser;
import ca.bc.gov.nrs.vdyp.model.Coefficients;
import ca.bc.gov.nrs.vdyp.model.MatrixMap;
import ca.bc.gov.nrs.vdyp.model.Region;

/**
 * Parses a Coefficient data file.
 *
 * @author Kevin Smith, Vivid Solutions
 *
 */
public abstract class BaseCoefficientParser<T extends Coefficients, W, M extends MatrixMap<W>>
		implements ControlMapSubResourceParser<M> {

	public static final String SP0_KEY = "sp0";
	public static final String REGION_KEY = "region";
	public static final String COEFFICIENTS_KEY = "coefficients";
	public static final String UC_INDEX = "ucIndex";
	public static final String GROUP_INDEX = "groupIndex";

	private int numCoefficients;
	private Optional<IntFunction<Float>> defaultCoefficientValuator;
	private Optional<Function<Void, T>> defaultEntryValuator;

	private List<String> metaKeys = new ArrayList<>();
	private List<Function<Map<String, Object>, Collection<?>>> keyRanges = new ArrayList<>();
	private int expectedKeys;
	private final ControlKey controlKey;
	private List<Predicate<String>> segmentIgnoreTests = new ArrayList<>();

	protected BaseCoefficientParser(int expectedKeys, ControlKey controlKey) {
		super();
		this.expectedKeys = expectedKeys;
		this.lineParser = new LineParser() {

			@Override
			public boolean isIgnoredSegment(List<String> segments) {
				if (segments.size() < segmentIgnoreTests.size()) {
					return true;
				}
				for (var pair : Utils.parallelIterate(segmentIgnoreTests, segments)) {
					if (pair.getSecond() == null || pair.getFirst().test(pair.getSecond())) {
						return true;
					}
				}
				return false;
			}

		};
		this.controlKey = controlKey;
	}

	protected BaseCoefficientParser(ControlKey controlKey) {
		this(0, controlKey);
	}

	static final int MAX_BLANKABLE_KEY_LENGTH = 3;
	static final Pattern BLANK_OR_ZERO = Pattern.compile(
			"^\\s{0," + MAX_BLANKABLE_KEY_LENGTH + "}0{0," + MAX_BLANKABLE_KEY_LENGTH + "}\\.?0{0,"
					+ MAX_BLANKABLE_KEY_LENGTH + "}\\s{0," + MAX_BLANKABLE_KEY_LENGTH + "}$"
	);

	/**
	 * Add a key for the multimap
	 *
	 * @param <K>           type of the key
	 * @param length        length of the key in the file
	 * @param name          name of the key
	 * @param parser        Parser for the
	 * @param range         Function that returns the set of values based on the control map
	 * @param errorTemplate Error message if the parsed value is not in the given range, it will be formatted (see
	 *                      {@link String.format}) with the erroneous value as a parameter.
	 * @return
	 */
	public <K> BaseCoefficientParser<T, W, M> key(
			int length, String name, ControlledValueParser<K> parser,
			Function<Map<String, Object>, Collection<?>> range, String errorTemplate, Predicate<String> keyIgnoreTest
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
		segmentIgnoreTests.add(keyIgnoreTest);
		return this;
	}

	public <K> BaseCoefficientParser<T, W, M> key(
			int length, String name, ControlledValueParser<K> parser, Collection<?> range, String errorTemplate,
			Predicate<String> keyIgnoreTest
	) {
		return key(length, name, parser, c -> range, errorTemplate, keyIgnoreTest);
	}

	public BaseCoefficientParser<T, W, M> regionKey() {
		var regions = Arrays.asList(Region.values());
		return key(1, REGION_KEY, ValueParser.REGION, regions, "%s is not a valid region", String::isBlank);
	}

	public BaseCoefficientParser<T, W, M> ucIndexKey() {
		var indicies = Arrays.asList(1, 2, 3, 4);
		return key(
				2, UC_INDEX, ValueParser.INTEGER, indicies, "%s is not a valid UC Index, should be 1 to 4 inclusive",
				BLANK_OR_ZERO.asPredicate()
		);
	}

	public BaseCoefficientParser<T, W, M> groupIndexKey(int maxGroups) {
		var indicies = Stream.iterate(1, x -> x + 1).limit(maxGroups).toList();
		return key(
				3, GROUP_INDEX, ValueParser.INTEGER, indicies,
				"%s is not a valid Group Index, should be 1 to " + maxGroups + " inclusive", BLANK_OR_ZERO.asPredicate()
		);
	}

	public BaseCoefficientParser<T, W, M> speciesKey(String name) {
		return key(
				2, name, ControlledValueParser.GENUS, GenusDefinitionParser::getSpeciesAliases,
				"%s is not a valid species", String::isBlank
		);
	}

	public BaseCoefficientParser<T, W, M> speciesKey() {
		return speciesKey(SP0_KEY);
	}

	public BaseCoefficientParser<T, W, M> space(int length) {
		lineParser.space(length);
		segmentIgnoreTests.add(x -> false); // Has no impact on whether the line should be ignored
		return this;
	}

	public <K> BaseCoefficientParser<T, W, M> coefficients(int number, int length) {
		lineParser.multiValue(number, length, COEFFICIENTS_KEY, ValueParser.FLOAT);
		this.numCoefficients = number;
		this.defaultEntryValuator = Optional.empty();
		this.defaultCoefficientValuator = Optional.empty();
		return this;
	}

	public <K> BaseCoefficientParser<T, W, M> coefficients(
			int number, int length, Optional<Function<Void, T>> defaultEntryValuator,
			Optional<IntFunction<Float>> defaultCoefficientValuator
	) {
		lineParser.multiValue(number, length, COEFFICIENTS_KEY, ValueParser.FLOAT);
		this.numCoefficients = number;
		this.defaultEntryValuator = defaultEntryValuator;
		this.defaultCoefficientValuator = defaultCoefficientValuator;
		return this;
	}

	protected LineParser lineParser;

	@Override
	public M parse(InputStream is, Map<String, Object> control) throws IOException, ResourceParseException {
		if (expectedKeys > 0 && metaKeys.size() != expectedKeys) {
			throw new IllegalStateException("Expected " + expectedKeys + " keys but there were " + metaKeys.size());
		}
		@SuppressWarnings({ "unchecked", "rawtypes" })
		List<Collection<?>> keys = (List) keyRanges.stream().map(x -> x.apply(control)).toList();
		M result = createMap(keys);

		var parsed = new AtomicInteger();

		lineParser.parse(is, result, (value, r, lineNumber) -> {
			var key = metaKeys.stream().map(value::get).toList().toArray(Object[]::new);

			@SuppressWarnings("unchecked")
			var coeList = (List<Float>) value.get(COEFFICIENTS_KEY);
			if (coeList.size() < numCoefficients && defaultCoefficientValuator.isPresent()) {

				List<Float> defaultedValues = IntStream.range(coeList.size(), numCoefficients)
						.mapToObj(i -> defaultCoefficientValuator.get().apply(i)).collect(Collectors.toList());

				defaultedValues.addAll(0, coeList);
				coeList = defaultedValues;
			}

			var coe = getCoefficients(coeList);

			r.putM(wrapCoefficients(coe), key);
			parsed.incrementAndGet();
			return r;
		}, control);
		validate(result, parsed.get(), keys);
		return result;
	}

	protected void validate(M result, int parsed, List<Collection<?>> keyRanges) throws ResourceParseValidException {
		// Do Nothing
	}

	protected abstract M createMap(List<Collection<?>> keyRanges);

	protected abstract T getCoefficients(List<Float> coefficients);

	protected abstract W wrapCoefficients(T coefficients);

	protected T getCoefficients() {
		if (defaultEntryValuator.isPresent())
			return defaultEntryValuator.get().apply(null);
		else
			return getCoefficients(Coefficients.sameSize(numCoefficients, 0f));
	}

	@Override
	public ControlKey getControlKey() {
		return controlKey;
	}
}
