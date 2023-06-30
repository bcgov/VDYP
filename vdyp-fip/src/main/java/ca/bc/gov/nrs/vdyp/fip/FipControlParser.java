package ca.bc.gov.nrs.vdyp.fip;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import ca.bc.gov.nrs.vdyp.io.FileResolver;
import ca.bc.gov.nrs.vdyp.io.parse.BecDefinitionParser;
import ca.bc.gov.nrs.vdyp.io.parse.BreakageEquationGroupParser;
import ca.bc.gov.nrs.vdyp.io.parse.BreakageParser;
import ca.bc.gov.nrs.vdyp.io.parse.BySpeciesDqCoefficientParser;
import ca.bc.gov.nrs.vdyp.io.parse.CloseUtilVolumeParser;
import ca.bc.gov.nrs.vdyp.io.parse.CoefficientParser;
import ca.bc.gov.nrs.vdyp.io.parse.ComponentSizeParser;
import ca.bc.gov.nrs.vdyp.io.parse.ControlFileParser;
import ca.bc.gov.nrs.vdyp.io.parse.ControlMapModifier;
import ca.bc.gov.nrs.vdyp.io.parse.DecayEquationGroupParser;
import ca.bc.gov.nrs.vdyp.io.parse.DefaultEquationNumberParser;
import ca.bc.gov.nrs.vdyp.io.parse.SP0DefinitionParser;
import ca.bc.gov.nrs.vdyp.io.parse.SiteCurveAgeMaximumParser;
import ca.bc.gov.nrs.vdyp.io.parse.SiteCurveParser;
import ca.bc.gov.nrs.vdyp.io.parse.SmallComponentBaseAreaParser;
import ca.bc.gov.nrs.vdyp.io.parse.SmallComponentDQParser;
import ca.bc.gov.nrs.vdyp.io.parse.SmallComponentHLParser;
import ca.bc.gov.nrs.vdyp.io.parse.SmallComponentProbabilityParser;
import ca.bc.gov.nrs.vdyp.io.parse.SmallComponentWSVolumeParser;
import ca.bc.gov.nrs.vdyp.io.parse.StockingClassFactorParser;
import ca.bc.gov.nrs.vdyp.io.parse.TotalStandWholeStemParser;
import ca.bc.gov.nrs.vdyp.io.parse.UpperCoefficientParser;
import ca.bc.gov.nrs.vdyp.io.parse.UtilComponentBaseAreaParser;
import ca.bc.gov.nrs.vdyp.io.parse.UtilComponentDQParser;
import ca.bc.gov.nrs.vdyp.io.parse.UtilComponentWSVolumeParser;
import ca.bc.gov.nrs.vdyp.io.parse.ValueParser;
import ca.bc.gov.nrs.vdyp.io.parse.VeteranBQParser;
import ca.bc.gov.nrs.vdyp.io.parse.VeteranDQParser;
import ca.bc.gov.nrs.vdyp.io.parse.VeteranLayerVolumeAdjustParser;
import ca.bc.gov.nrs.vdyp.io.parse.VolumeEquationGroupParser;
import ca.bc.gov.nrs.vdyp.io.parse.VolumeNetDecayParser;
import ca.bc.gov.nrs.vdyp.io.parse.VolumeNetDecayWasteParser;
import ca.bc.gov.nrs.vdyp.io.parse.EquationModifierParser;
import ca.bc.gov.nrs.vdyp.io.parse.HLCoefficientParser;
import ca.bc.gov.nrs.vdyp.io.parse.HLNonprimaryCoefficientParser;
import ca.bc.gov.nrs.vdyp.io.parse.ResourceParseException;

/**
 * Parser for FIP control files
 *
 * @author Kevin Smith, Vivid Solutions
 *
 */
public class FipControlParser {

	public static final String FIP_YIELD_POLY_INPUT = "FIP_YIELD_POLY_INPUT";
	public static final String FIP_YIELD_LAYER_INPUT = "FIP_YIELD_LAYER_INPUT";
	public static final String FIP_YIELD_LX_SP0_INPUT = "FIP_YIELD_LxSP0_INPUT";
	public static final String VDYP_POLYGON = "VDYP_POLYGON";
	public static final String VDYP_LAYER_BY_SPECIES = "VDYP_LAYER_BY_SPECIES";
	public static final String VDYP_LAYER_BY_SP0_BY_UTIL = "VDYP_LAYER_BY_SP0_BY_UTIL";
	public static final String VOLUME_EQN_GROUPS = VolumeEquationGroupParser.CONTROL_KEY;
	public static final String DECAY_GROUPS = DecayEquationGroupParser.CONTROL_KEY;
	public static final String BREAKAGE_GROUPS = BreakageEquationGroupParser.CONTROL_KEY;
	public static final String SITE_CURVE_NUMBERS = SiteCurveParser.CONTROL_KEY;
	public static final String SITE_CURVE_AGE_MAX = SiteCurveAgeMaximumParser.CONTROL_KEY;
	public static final String DEFAULT_EQ_NUM = DefaultEquationNumberParser.CONTROL_KEY;
	public static final String EQN_MODIFIERS = EquationModifierParser.CONTROL_KEY;
	public static final String STOCKING_CLASS_FACTORS = StockingClassFactorParser.CONTROL_KEY;
	public static final String COE_BA = CoefficientParser.BA_CONTROL_KEY;
	public static final String COE_DQ = CoefficientParser.DQ_CONTROL_KEY;
	public static final String UPPER_BA_BY_CI_S0_P = UpperCoefficientParser.CONTROL_KEY;
	public static final String HL_PRIMARY_SP_EQN_P1 = HLCoefficientParser.CONTROL_KEY_P1;
	public static final String HL_PRIMARY_SP_EQN_P2 = HLCoefficientParser.CONTROL_KEY_P2;
	public static final String HL_PRIMARY_SP_EQN_P3 = HLCoefficientParser.CONTROL_KEY_P3;
	public static final String HL_NONPRIMARY = HLNonprimaryCoefficientParser.CONTROL_KEY;
	public static final String BY_SPECIES_DQ = BySpeciesDqCoefficientParser.CONTROL_KEY;
	public static final String SPECIES_COMPONENT_SIZE_LIMIT = ComponentSizeParser.CONTROL_KEY;
	public static final String UTIL_COMP_BA = UtilComponentBaseAreaParser.CONTROL_KEY;
	public static final String UTIL_COMP_DQ = UtilComponentDQParser.CONTROL_KEY;
	public static final String SMALL_COMP_PROBABILITY = SmallComponentProbabilityParser.CONTROL_KEY;
	public static final String SMALL_COMP_BA = SmallComponentBaseAreaParser.CONTROL_KEY;
	public static final String SMALL_COMP_DQ = SmallComponentDQParser.CONTROL_KEY;
	public static final String SMALL_COMP_HL = SmallComponentHLParser.CONTROL_KEY;
	public static final String SMALL_COMP_WS_VOLUME = SmallComponentWSVolumeParser.CONTROL_KEY;
	public static final String TOTAL_STAND_WHOLE_STEM_VOL = TotalStandWholeStemParser.CONTROL_KEY;
	public static final String UTIL_COMP_WS_VOLUME = UtilComponentWSVolumeParser.CONTROL_KEY;
	public static final String CLOSE_UTIL_VOLUME = CloseUtilVolumeParser.CONTROL_KEY;
	public static final String VOLUME_NET_DECAY = VolumeNetDecayParser.CONTROL_KEY;
	public static final String VOLUME_NET_DECAY_WASTE = VolumeNetDecayWasteParser.CONTROL_KEY;
	public static final String BREAKAGE = BreakageParser.CONTROL_KEY;
	public static final String VETERAN_LAYER_VOLUME_ADJUST = VeteranLayerVolumeAdjustParser.CONTROL_KEY;
	public static final String VETERAN_LAYER_DQ = "VETERAN_LAYER_DQ";
	public static final String VETERAN_BQ = VeteranBQParser.CONTROL_KEY;
	public static final String MINIMA = "MINIMA";
	public static final String MODIFIER_FILE = ModifierParser.CONTROL_KEY;
	public static final String DEBUG_SWITCHES = "DEBUG_SWITCHES";
	public static final String MAX_NUM_POLY = "MAX_NUM_POLY";
	public static final String BEC_DEF = BecDefinitionParser.CONTROL_KEY;
	public static final String SP0_DEF = SP0DefinitionParser.CONTROL_KEY;

	static final ValueParser<String> FILENAME = String::strip;

	ControlFileParser controlParser = new ControlFileParser().record(1, MAX_NUM_POLY, ValueParser.INTEGER)

			.record(9, BEC_DEF, FILENAME) // RD_BECD
			.record(10, SP0_DEF, FILENAME) // RD_SP0

			.record(11, FIP_YIELD_POLY_INPUT, FILENAME) // GET_FIPP
			.record(12, FIP_YIELD_LAYER_INPUT, FILENAME) // GET_FIPL
			.record(13, FIP_YIELD_LX_SP0_INPUT, FILENAME) // GET_FIPS

			.record(15, VDYP_POLYGON, FILENAME) //
			.record(16, VDYP_LAYER_BY_SPECIES, FILENAME) //
			.record(18, VDYP_LAYER_BY_SP0_BY_UTIL, FILENAME) //

			.record(20, VOLUME_EQN_GROUPS, FILENAME) // RD_VGRP
			.record(21, DECAY_GROUPS, FILENAME) // RD_DGRP
			.record(22, BREAKAGE_GROUPS, FILENAME) // RD_BGRP IPSJF157

			.record(25, SITE_CURVE_NUMBERS, ValueParser.optional(FILENAME)) // RD_E025
			.record(26, SITE_CURVE_AGE_MAX, ValueParser.optional(FILENAME)) // RD_E026

			.record(30, DEFAULT_EQ_NUM, FILENAME) // RD_GRBA1
			.record(31, EQN_MODIFIERS, FILENAME) // RD_GMBA1
			.record(33, STOCKING_CLASS_FACTORS, FILENAME) // RD_STK33

			.record(40, COE_BA, FILENAME) // RD_E040 IPSJF128
			.record(41, COE_DQ, FILENAME) // RD_E041 IPSJF129
			.record(43, UPPER_BA_BY_CI_S0_P, FILENAME) // RD_E043 IPSJF128

			.record(50, HL_PRIMARY_SP_EQN_P1, FILENAME) // RD_YHL1
			.record(51, HL_PRIMARY_SP_EQN_P2, FILENAME) // RD_YHL2
			.record(52, HL_PRIMARY_SP_EQN_P3, FILENAME) // RD_YHL3
			.record(53, HL_NONPRIMARY, FILENAME) // RD_YHL4

			.record(60, BY_SPECIES_DQ, FILENAME) // RD_E060 IPFJF125
			.record(61, SPECIES_COMPONENT_SIZE_LIMIT, FILENAME) // RD_E061 IPSJF158

			.record(70, UTIL_COMP_BA, FILENAME) // RD_UBA1
			.record(71, UTIL_COMP_DQ, FILENAME) // RD_UDQ1

			.record(80, SMALL_COMP_PROBABILITY, FILENAME) // RD_SBA1
			.record(81, SMALL_COMP_BA, FILENAME) // RD_SBA2
			.record(82, SMALL_COMP_DQ, FILENAME) // RD_SDQ1
			.record(85, SMALL_COMP_HL, FILENAME) // RD_SHL1
			.record(86, SMALL_COMP_WS_VOLUME, FILENAME) // RD_SVT1

			.record(90, TOTAL_STAND_WHOLE_STEM_VOL, FILENAME) // RD_YVT1 IPSJF117
			.record(91, UTIL_COMP_WS_VOLUME, FILENAME) // RD_YVT2 IPSJF121
			.record(92, CLOSE_UTIL_VOLUME, FILENAME) // RD_YVC1 IPSJF122
			.record(93, VOLUME_NET_DECAY, FILENAME) // RD_YVD1 IPSJF123
			.record(94, VOLUME_NET_DECAY_WASTE, FILENAME) // RD_YVW1 IPSJF123
			.record(95, BREAKAGE, FILENAME) // RD_EMP95 IPSJF157

			.record(96, VETERAN_LAYER_VOLUME_ADJUST, FILENAME) // RD_YVET
			.record(97, VETERAN_LAYER_DQ, FILENAME) // RD_YDQV
			.record(98, VETERAN_BQ, FILENAME) // RD_E098

			.record(197, MINIMA, ValueParser.list(ValueParser.FLOAT)) // Minimum Height, Minimum BA, Min BA fully
																		// stocked.

			.record(198, MODIFIER_FILE, ValueParser.optional(FILENAME)) // RD_E198 IPSJF155, XII

			.record(199, DEBUG_SWITCHES, ValueParser.list(ValueParser.INTEGER)) // IPSJF155
	/*
	 * Debug switches (25) 0=default See IPSJF155, App IX 1st: 1: Do NOT apply BA
	 * limits from SEQ043 2nd: 1: Do NOT apply DQ limits from SEQ043 4th: Future
	 * Development. Choice of upper limits 9th: 0: Normal - Suppress MATH77 error
	 * messages. 1: show some MATH77 errors; 2: show all. 22nd 1: extra preference
	 * for preferred sp (SEQ 010).
	 */

	;

	int jprogram = 1; // FIPSTART only TODO Track this down

	public FipControlParser() {

	}

	Map<String, ?> parse(Path inputFile) throws IOException, ResourceParseException {
		try (var is = Files.newInputStream(inputFile)) {

			return parse(is, new FileResolver() {

				@Override
				public InputStream resolve(String filename) throws IOException {
					return Files.newInputStream(inputFile.resolveSibling(filename));
				}

				@Override
				public String toString(String filename) throws IOException {
					return inputFile.resolveSibling(filename).toString();
				}
			});
		}
	}

	Map<String, ?> parse(Class<?> klazz, String resourceName) throws IOException, ResourceParseException {
		try (var is = klazz.getResourceAsStream(resourceName)) {

			return parse(is, fileResolver(klazz));
		}
	}

	static FileResolver fileResolver(Class<?> klazz) {
		return new FileResolver() {

			@Override
			public InputStream resolve(String filename) throws IOException {
				InputStream resourceAsStream = klazz.getResourceAsStream(filename);
				if (resourceAsStream == null)
					throw new IOException("Could not load " + filename);
				return resourceAsStream;
			}

			@Override
			public String toString(String filename) throws IOException {
				return klazz.getResource(filename).toString();
			}
		};
	}

	List<ControlMapModifier> BASIC_DEFINITIONS = Arrays.asList(

			// RD_BEC
			new BecDefinitionParser(),

			// DEF_BEC
			// Superseded by BecLookup.getGrowthBecs

			// RD_SP0
			new SP0DefinitionParser()
	);

	List<ControlMapModifier> GROUP_DEFINITIONS = Arrays.asList(

			// RD_VGRP
			new VolumeEquationGroupParser(),

			// RD_DGRP
			new DecayEquationGroupParser(),

			// RD_BGRP
			new BreakageEquationGroupParser(),

			// RD_GRBA1
			new DefaultEquationNumberParser(),

			// RD_GMBA1
			new EquationModifierParser()
	);

	List<ControlMapModifier> FIPSTART_ONLY = Arrays.asList(

			// RD_STK33
			new StockingClassFactorParser()

	// TODO minima?
	/*
	 * READ(CNTRV(197), 197, ERR= 912 ) FMINH, FMINBA, FMINBAF,FMINVetH IF (FMINVetH
	 * .le. 0.0) FMINVetH=10.0
	 */
	);
	List<ControlMapModifier> SITE_CURVES = Arrays.asList(

			// User-assigned SC's (Site Curve Numbers)
			//
			// RD_E025
			new SiteCurveParser(),

			// Max tot ages to apply site curves (by SC)
			//
			// RD_E026
			new SiteCurveAgeMaximumParser()
	);

	List<ControlMapModifier> COEFFICIENTS = Arrays.asList(
			// RD_E040
			new CoefficientParser(COE_BA),

			// RD_E041
			new CoefficientParser(COE_DQ),

			// RD_E043
			new UpperCoefficientParser(),

			// RD_YHL1
			new HLCoefficientParser(HLCoefficientParser.NUM_COEFFICIENTS_P1, HL_PRIMARY_SP_EQN_P1),

			// RD_YHL2
			new HLCoefficientParser(HLCoefficientParser.NUM_COEFFICIENTS_P2, HL_PRIMARY_SP_EQN_P2),

			// RD_YHL3
			new HLCoefficientParser(HLCoefficientParser.NUM_COEFFICIENTS_P3, HL_PRIMARY_SP_EQN_P3),

			// RD_YHL4
			new HLNonprimaryCoefficientParser(),

			// RD_E060
			new BySpeciesDqCoefficientParser(),

			// Min and max DQ by species

			// RD_E061
			new ComponentSizeParser(),

			// RD_UBA1
			new UtilComponentBaseAreaParser(),

			// RD_UDQ1
			new UtilComponentDQParser(),

			// Small Component (4.5 to 7.5 cm)

			// RD_SBA1
			new SmallComponentProbabilityParser(),

			// RD_SBA2
			new SmallComponentBaseAreaParser(),

			// RD_SDQ1
			new SmallComponentDQParser(),

			// RD_SHL1
			new SmallComponentHLParser(),

			// RD_SVT1
			new SmallComponentWSVolumeParser(),

			// Standard Volume Relationships

			// RD_YVT1
			new TotalStandWholeStemParser(),

			// RD_YVT2
			new UtilComponentWSVolumeParser(),

			// RD_YVC1
			new CloseUtilVolumeParser(),

			// RD_YVD1
			new VolumeNetDecayParser(),

			// RD_YVW1
			new VolumeNetDecayWasteParser(),

			// RD_E095
			new BreakageParser(),

			// Veterans

			// RD_YVVET
			new VeteranLayerVolumeAdjustParser(),

			// RD_YDQV
			new VeteranDQParser(),

			// RD_E098
			new VeteranBQParser()
	);

	List<ControlMapModifier> ADDITIONAL_MODIFIERS = Arrays.asList(

			// RD_E198
			new ModifierParser(jprogram)
	);

	private void
			applyModifiers(Map<String, Object> control, List<ControlMapModifier> modifiers, FileResolver fileResolver)
					throws ResourceParseException, IOException {
		for (var modifier : modifiers) {
			modifier.modify(control, fileResolver);
		}
	}

	Map<String, ?> parse(InputStream is, FileResolver fileResolver) throws IOException, ResourceParseException {
		var map = controlParser.parse(is, Collections.emptyMap());

		applyModifiers(map, BASIC_DEFINITIONS, fileResolver);

		// Read Groups

		applyModifiers(map, GROUP_DEFINITIONS, fileResolver);

		if (jprogram == 1) {
			applyModifiers(map, FIPSTART_ONLY, fileResolver);
		}

		applyModifiers(map, SITE_CURVES, fileResolver);

		// Coeff for Empirical relationships

		applyModifiers(map, COEFFICIENTS, fileResolver);

		// Initiation items NOT for FIPSTART
		if (jprogram > 1) {

			throw new UnsupportedOperationException();
			// RD_E106
			// TODO

			// RD_E107
			// TODO

			// RD_E108
			// TODO

			// Minima again, differently?
			// TODO

			/*
			 * READ(CNTRV(197), 197, ERR= 912 ) VMINH, VMINBA, VMINBAeqn,VMINvetH IF
			 * (VMINVetH .le. 0.0) VMINVetH=10.0
			 */

			// RD_E112
			// Was commented out in Fortran

			// RD_E116
			// Was commented out in Fortran
		}

		// Modifiers, IPSJF155-Appendix XII

		// RD_E198
		applyModifiers(map, ADDITIONAL_MODIFIERS, fileResolver);

		// Debug switches (normally zero)
		// TODO
		/*
		 * 141 FORMAT(25I2) DO 140 I=1,25 if ((NDEBUG(I) .ne.0) .and. (IU_WRITE .gt. 0))
		 * & WRITE(IU_WRITE,142) I, NDEBUG(I) 142 FORMAT(' CTR Line 199 has
		 * NDEBUG(',i2,') =',I3) 140 CONTINUE
		 *
		 * if (isDBG) then call dbgMsg( logr, 0 ) "Debug Switches (Ctl Line 199)" ) call
		 * dbgI4( logr, 6 ) " 1: Applying BA Limits               ", NDEBUG( 1) ) call
		 * dbgMsg( logr, 6 ) "       0 - Apply BA limits from SEQ043." ) call dbgMsg(
		 * logr, 6 ) "       1 - Do not apply BA limits from SEQ043." ) call dbgI4(
		 * logr, 6 ) " 2: Applying BA Limits               ", NDEBUG( 2) ) call dbgMsg(
		 * logr, 6 ) "       0 - Apply DQ limits from SEQ043." ) call dbgMsg( logr, 6 )
		 * "       1 - Do not apply DQ limits from SEQ043." ) call dbgI4( logr, 6 )
		 * " 3: Not used                         ", NDEBUG( 3) ) call dbgI4( logr, 6 )
		 * " 4: Not implemented. Upper bounds    ", NDEBUG( 4) ) call dbgMsg( logr, 6 )
		 * "       0 - Will default to (2)." ) call dbgMsg( logr, 6 )
		 * "       1 - Limits from Ctl Line 108 (GRPBA1)." ) call dbgMsg( logr, 6 )
		 * "       2 - Limits from Ctl Line 043 (Coast/Interior)" ) call dbgI4( logr, 6
		 * ) " 5: Not used                         ", NDEBUG( 5) ) call dbgI4( logr, 6 )
		 * " 6: Not used                         ", NDEBUG( 6) ) call dbgI4( logr, 6 )
		 * " 7: Not used                         ", NDEBUG( 7) ) call dbgI4( logr, 6 )
		 * " 8: Not used                         ", NDEBUG( 8) ) call dbgI4( logr, 6 )
		 * " 9: Error handling                   ", NDEBUG( 9) ) call dbgMsg( logr, 6 )
		 * "       0 - Stop on all errors." ) call dbgMsg( logr, 6 )
		 * "       1 - Continue to next poly (non-I/O errs)." ) call dbgI4( logr, 6 )
		 * "10: Not used                         ", NDEBUG(10) ) call dbgI4( logr, 6 )
		 * "11: Not used                         ", NDEBUG(11) ) call dbgI4( logr, 6 )
		 * "12: Not used                         ", NDEBUG(12) ) call dbgI4( logr, 6 )
		 * "13: Not used                         ", NDEBUG(13) ) call dbgI4( logr, 6 )
		 * "14: Not used                         ", NDEBUG(14) ) call dbgI4( logr, 6 )
		 * "15: Not used                         ", NDEBUG(15) ) call dbgI4( logr, 6 )
		 * "16: Not used                         ", NDEBUG(16) ) call dbgI4( logr, 6 )
		 * "17: Not used                         ", NDEBUG(17) ) call dbgI4( logr, 6 )
		 * "18: Not used                         ", NDEBUG(18) ) call dbgI4( logr, 6 )
		 * "19: Not used                         ", NDEBUG(19) ) call dbgI4( logr, 6 )
		 * "20: Not used                         ", NDEBUG(20) ) call dbgI4( logr, 6 )
		 * "21: Not used                         ", NDEBUG(21) ) call dbgI4( logr, 6 )
		 * "22: Determining primary species      ", NDEBUG(22) ) call dbgMsg( logr, 6 )
		 * "       0 - Normal " ) call dbgMsg( logr, 6 )
		 * "       1 - Extra preference for preferred sp. as primary" ) call dbgI4(
		 * logr, 6 ) "23: Not used                         ", NDEBUG(23) ) call dbgI4(
		 * logr, 6 ) "24: Not used                         ", NDEBUG(24) ) call dbgI4(
		 * logr, 6 ) "25: Not used                         ", NDEBUG(25) ) end if
		 */

		return map;
	}

}
