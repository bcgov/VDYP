package ca.bc.gov.nrs.vdyp.fip;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.bc.gov.nrs.vdyp.application.VdypApplicationIdentifier;
import ca.bc.gov.nrs.vdyp.common.ControlKey;
import ca.bc.gov.nrs.vdyp.io.FileResolver;
import ca.bc.gov.nrs.vdyp.io.parse.coe.ModifierParser;
import ca.bc.gov.nrs.vdyp.io.parse.coe.StockingClassFactorParser;
import ca.bc.gov.nrs.vdyp.io.parse.common.ResourceParseException;
import ca.bc.gov.nrs.vdyp.io.parse.control.ControlMapModifier;
import ca.bc.gov.nrs.vdyp.io.parse.control.ControlMapValueReplacer;
import ca.bc.gov.nrs.vdyp.io.parse.control.StartApplicationControlParser;
import ca.bc.gov.nrs.vdyp.io.parse.value.ValueParser;

/**
 * Parser for FIP control files
 *
 * @author Kevin Smith, Vivid Solutions
 *
 */
public class FipControlParser extends StartApplicationControlParser {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(FipControlParser.class);

	public FipControlParser() {

		initialize();

		this.controlParser //
				// FIP only
				.record(ControlKey.STOCKING_CLASS_FACTORS, FILENAME) // RD_STK33
		;
	}

	@Override
	protected ValueParser<Map<String, Float>> minimaParser() {
		return ValueParser.callback(
				ValueParser.toMap(
						ValueParser.list(ValueParser.FLOAT), Collections.singletonMap(MINIMUM_VETERAN_HEIGHT, 10.0f),
						MINIMUM_HEIGHT, MINIMUM_BASE_AREA, MINIMUM_FULLY_STOCKED_AREA, MINIMUM_VETERAN_HEIGHT
				), minima -> {
					log.atDebug().setMessage(
							"Minima read from FIPSTART Control at line {}\n  Minimum Height: {}\n  Minimum BA: {}\n  Minimum  BA Fully Stocked: {}\n  Minimum Veteran Height: {}"
					)//
							.addArgument(ControlKey.MINIMA.sequence.map(i -> Integer.toString(i)).orElse("N/A"))
							.addArgument(minima.get(MINIMUM_HEIGHT)) //
							.addArgument(minima.get(MINIMUM_BASE_AREA)) //
							.addArgument(minima.get(MINIMUM_FULLY_STOCKED_AREA)) //
							.addArgument(minima.get(MINIMUM_VETERAN_HEIGHT));
				}
		);
	}

	List<ControlMapModifier> fipstartOnly = Arrays.asList(

			// RD_STK33
			new StockingClassFactorParser()
	);

	List<ControlMapModifier> additionalModifiers = Arrays.asList(

			// RD_E198
			new ModifierParser(VdypApplicationIdentifier.VRI_START)
	);

	@Override
	protected void applyAllModifiers(Map<String, Object> map, FileResolver fileResolver)
			throws ResourceParseException, IOException {
		applyModifiers(map, basicDefinitions, fileResolver);

		// Read Groups

		applyModifiers(map, groupDefinitions, fileResolver);

		// Initialize data file parser factories

		applyModifiers(map, inputFileParsers(), fileResolver);

		applyModifiers(map, fipstartOnly, fileResolver);

		applyModifiers(map, siteCurves, fileResolver);

		// Coeff for Empirical relationships

		applyModifiers(map, coefficients, fileResolver);

		// Modifiers, IPSJF155-Appendix XII

		// RD_E198
		applyModifiers(map, additionalModifiers, fileResolver);

		// Debug switches (normally zero)
		// TODO

	}

	@Override
	protected VdypApplicationIdentifier getProgramId() {
		return VdypApplicationIdentifier.FIP_START;
	}

	@Override
	protected List<ControlMapValueReplacer<Object, String>> inputFileParsers() {
		return inputParserList(

				// V7O_FIP
				new FipPolygonParser(),

				// V7O_FIL
				new FipLayerParser(),

				// V7O_FIS
				new FipSpeciesParser()
		);
	}

	@Override
	protected List<ControlKey> outputFileParsers() {
		return List.of(
				ControlKey.VRI_OUTPUT_VDYP_POLYGON, ControlKey.VRI_OUTPUT_VDYP_LAYER_BY_SPECIES,
				ControlKey.VRI_OUTPUT_VDYP_LAYER_BY_SP0_BY_UTIL
		);
	}

}
