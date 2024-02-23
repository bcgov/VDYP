package ca.bc.gov.nrs.vdyp.io.parse.coe.base;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import ca.bc.gov.nrs.vdyp.common.ControlKey;
import ca.bc.gov.nrs.vdyp.common.Utils;
import ca.bc.gov.nrs.vdyp.io.parse.common.ResourceParseException;
import ca.bc.gov.nrs.vdyp.io.parse.value.ValueParser;
import ca.bc.gov.nrs.vdyp.model.Coefficients;
import ca.bc.gov.nrs.vdyp.model.MatrixMap2;

public abstract class NonPrimarySpeciesGrowthParser extends OptionalCoefficientParser2<String, Integer> {

	private static final int MAX_BASAL_AREA_GROUP_ID = 30;

	private static final Optional<Coefficients> defaultCoefficients = Optional
			.of(new Coefficients(new float[] { 0.0f, 0.0f, 0.0f }, 1));

	private static final String BASAL_AREA_GROUP_ID_KEY = "BasalAreaGroupId";

	protected NonPrimarySpeciesGrowthParser(ControlKey controlKey) {
		super(1, controlKey);
		this.speciesKey()
				.key(
						3, BASAL_AREA_GROUP_ID_KEY, ValueParser.INTEGER,
						IntStream.rangeClosed(0, MAX_BASAL_AREA_GROUP_ID).boxed().collect(Collectors.toList()),
						"%s is not a valid basal area group id", k -> Utils.nullOrBlank(k)
				).coefficients(3, 10);
	}

	@Override
	public MatrixMap2<String, Integer, Optional<Coefficients>> parse(InputStream is, Map<String, Object> control)
			throws IOException, ResourceParseException {
		var m = super.parse(is, control);

		m.eachKey(k -> {
			if (m.getM(k).isEmpty()) {
				m.put((String) k[0], (Integer) k[1], defaultCoefficients);
			}
		});

		return m;
	}
}
