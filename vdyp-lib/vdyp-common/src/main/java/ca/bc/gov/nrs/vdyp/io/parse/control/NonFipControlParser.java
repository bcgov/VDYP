package ca.bc.gov.nrs.vdyp.io.parse.control;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.bc.gov.nrs.vdyp.common.ControlKey;
import ca.bc.gov.nrs.vdyp.io.FileResolver;
import ca.bc.gov.nrs.vdyp.io.parse.coe.BasalAreaYieldParser;
import ca.bc.gov.nrs.vdyp.io.parse.coe.QuadraticMeanDiameterYieldParser;
import ca.bc.gov.nrs.vdyp.io.parse.coe.UpperBoundsParser;
import ca.bc.gov.nrs.vdyp.io.parse.common.ResourceParseException;
import ca.bc.gov.nrs.vdyp.io.parse.value.ValueParser;

public abstract class NonFipControlParser extends StartApplicationControlParser {

	private static final Logger log = LoggerFactory.getLogger(NonFipControlParser.class);

	List<ControlMapModifier> nonFipStart = Arrays.asList(
			// RD_E106
			new BasalAreaYieldParser(),
			// RD_E106
			new QuadraticMeanDiameterYieldParser(),
			// RD_E106
			new UpperBoundsParser()
	);

	protected NonFipControlParser() {
		super();

		this.controlParser //

				.record(ControlKey.BA_YIELD, FILENAME) // COE106
				.record(ControlKey.DQ_YIELD, FILENAME) // COE107
				.record(ControlKey.BA_DQ_UPPER_BOUNDS, FILENAME) // COE108

				.record(
						ControlKey.MINIMA,
						ValueParser.toMap(
								ValueParser.list(ValueParser.FLOAT),
								Collections.singletonMap(MINIMUM_VETERAN_HEIGHT, 10.0f), MINIMUM_HEIGHT, // VMINH
								MINIMUM_BASE_AREA, // VMINBA
								MINIMUM_PREDICTED_BASE_AREA, // VMINBAeqn
								MINIMUM_VETERAN_HEIGHT // VMINVetH
						)
				)

		;
	}

	@Override
	protected void applyAllModifiers(Map<String, Object> map, FileResolver fileResolver)
			throws ResourceParseException, IOException {
		applyModifiers(map, basicDefinitions, fileResolver);

		// Read Groups

		applyModifiers(map, groupDefinitions, fileResolver);

		// Initialize data file parser factories

		applyModifiers(map, inputFileParsers(), fileResolver);

		applyModifiers(map, siteCurves, fileResolver);

		// Coeff for Empirical relationships

		applyModifiers(map, coefficients, fileResolver);

		// Initiation items NOT for FIPSTART

		applyModifiers(map, nonFipStart, fileResolver);

		// RD_E198
		applyModifiers(map, additionalModifiers, fileResolver);

	}

	@Override
	protected ValueParser<Map<String, Float>> minimaParser() {
		return ValueParser.callback(
				ValueParser.toMap(
						ValueParser.list(ValueParser.FLOAT), Collections.singletonMap(MINIMUM_VETERAN_HEIGHT, 10.0f),
						MINIMUM_HEIGHT, MINIMUM_BASE_AREA, MINIMUM_PREDICTED_BASE_AREA, MINIMUM_VETERAN_HEIGHT
				), minima -> {
					log.atDebug().setMessage(
							"Minima read from VRISTART Control at line {}\n  Minimum Height: {}\n  Minimum BA: {}\n  Minimum Predicted BA: {}\n  Minimum Veteran Height: {}"
					)//
							.addArgument(ControlKey.MINIMA.sequence.map(i -> Integer.toString(i)).orElse("N/A"))
							.addArgument(minima.get(MINIMUM_HEIGHT)) //
							.addArgument(minima.get(MINIMUM_BASE_AREA)) //
							.addArgument(minima.get(MINIMUM_PREDICTED_BASE_AREA)) //
							.addArgument(minima.get(MINIMUM_VETERAN_HEIGHT));
				}
		);
	}

}