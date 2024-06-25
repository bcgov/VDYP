package ca.bc.gov.nrs.vdyp.vri;

import java.util.Arrays;
import java.util.List;

import ca.bc.gov.nrs.vdyp.application.VdypApplicationIdentifier;
import ca.bc.gov.nrs.vdyp.common.ControlKey;
import ca.bc.gov.nrs.vdyp.io.parse.control.ControlMapValueReplacer;
import ca.bc.gov.nrs.vdyp.io.parse.control.NonFipControlParser;
import ca.bc.gov.nrs.vdyp.io.parse.streaming.StreamingParserFactory;

/**
 * Parser for VRI control files
 *
 * @author Kevin Smith, Vivid Solutions
 *
 */
public class VriControlParser extends NonFipControlParser {

	List<ControlMapValueReplacer<? extends StreamingParserFactory<? extends Object>, String>> dataFiles = Arrays.asList(

			// V7O_FIP
			new VriPolygonParser(),

			// V7O_FIL
			new VriLayerParser(),

			// V7O_FIS
			new VriSpeciesParser()
	);

	public VriControlParser() {

		initialize();
	}

	@Override
	protected VdypApplicationIdentifier getProgramId() {
		return VdypApplicationIdentifier.VRI_START;
	}

	@Override
	protected List<ControlMapValueReplacer<Object, String>> inputFileParsers() {
		return inputParserList(

				// V7O_FIP
				new VriPolygonParser(),

				// V7O_FIL
				new VriLayerParser(),

				// V7O_FIS
				new VriSpeciesParser()
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
