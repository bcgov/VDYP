package ca.bc.gov.nrs.vdyp.io.parse;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ca.bc.gov.nrs.vdyp.model.Coefficients;

/**
 * Parses an mapping from a species to a set of coefficients.
 *
 * @author Kevin Smith, Vivid Solutions
 *
 */
public class BySpeciesDqCoefficientParser implements ControlMapSubResourceParser<List<Coefficients>> {

	public static final String CONTROL_KEY = "BY_SPECIES_DQ";

	public static final String COEFFICIENT_INDEX_KEY = "coefficientIndex";
	public static final String COEFFICIENTS_KEY = "coefficients";
	public static final String INDICATOR_KEY = "indicator";

	private int NUM_COEFFICIENTS = 3;
	private int NUM_SPECIES = 16;

	public BySpeciesDqCoefficientParser() {
		super();
		this.lineParser = new LineParser() {

			@Override
			public boolean isStopLine(String line) {
				return line.startsWith("   ");
			}

		}.space(1).value(1, COEFFICIENT_INDEX_KEY, ValueParser.INTEGER).value(2, INDICATOR_KEY, ValueParser.INTEGER)
				.multiValue(NUM_SPECIES, 9, COEFFICIENTS_KEY, ValueParser.FLOAT);
	}

	LineParser lineParser;

	@Override
	public List<Coefficients> parse(InputStream is, Map<String, Object> control)
			throws IOException, ResourceParseException {

		List<Coefficients> result = new ArrayList<>(NUM_COEFFICIENTS);

		result.add(null);
		result.add(null);
		result.add(null);

		lineParser.parse(is, result, (v, r) -> {
			var index = (int) v.get(COEFFICIENT_INDEX_KEY);
			@SuppressWarnings("unchecked")
			var coefficients = (List<Float>) v.get(COEFFICIENTS_KEY);

			if (! (index == 0 || index == 1)) {
				final var first = coefficients.get(0);
				coefficients = Stream.generate(() -> first).limit(NUM_SPECIES).collect(Collectors.toList());
			}

			if (coefficients.size() < NUM_SPECIES) {
				throw new ValueParseException(null, "Expected " + NUM_SPECIES + " coefficients"); // TODO handle this
																									// better
			}
			r.set(index, new Coefficients(coefficients, 1));

			return r;
		}, control);
		return result;
	}

	@Override
	public String getControlKey() {
		return CONTROL_KEY;
	}

}