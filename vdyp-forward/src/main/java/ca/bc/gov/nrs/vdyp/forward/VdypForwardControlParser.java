package ca.bc.gov.nrs.vdyp.forward;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.bc.gov.nrs.vdyp.application.VdypApplication;
import ca.bc.gov.nrs.vdyp.application.VdypApplicationIdentifier;
import ca.bc.gov.nrs.vdyp.common.ControlKey;
import ca.bc.gov.nrs.vdyp.io.FileResolver;
import ca.bc.gov.nrs.vdyp.io.FileSystemFileResolver;
import ca.bc.gov.nrs.vdyp.io.parse.coe.BasalAreaGrowthEmpiricalParser;
import ca.bc.gov.nrs.vdyp.io.parse.coe.BasalAreaGrowthFiatParser;
import ca.bc.gov.nrs.vdyp.io.parse.coe.BasalAreaYieldParser;
import ca.bc.gov.nrs.vdyp.io.parse.coe.BecDefinitionParser;
import ca.bc.gov.nrs.vdyp.io.parse.coe.BreakageEquationGroupParser;
import ca.bc.gov.nrs.vdyp.io.parse.coe.BreakageParser;
import ca.bc.gov.nrs.vdyp.io.parse.coe.BySpeciesDqCoefficientParser;
import ca.bc.gov.nrs.vdyp.io.parse.coe.CloseUtilVolumeParser;
import ca.bc.gov.nrs.vdyp.io.parse.coe.CompVarAdjustmentsParser;
import ca.bc.gov.nrs.vdyp.io.parse.coe.ComponentSizeParser;
import ca.bc.gov.nrs.vdyp.io.parse.coe.DecayEquationGroupParser;
import ca.bc.gov.nrs.vdyp.io.parse.coe.DefaultEquationNumberParser;
import ca.bc.gov.nrs.vdyp.io.parse.coe.DqGrowthEmpiricalLimitsParser;
import ca.bc.gov.nrs.vdyp.io.parse.coe.DqGrowthEmpiricalParser;
import ca.bc.gov.nrs.vdyp.io.parse.coe.DqGrowthFiatParser;
import ca.bc.gov.nrs.vdyp.io.parse.coe.EquationModifierParser;
import ca.bc.gov.nrs.vdyp.io.parse.coe.GenusDefinitionParser;
import ca.bc.gov.nrs.vdyp.io.parse.coe.HLNonprimaryCoefficientParser;
import ca.bc.gov.nrs.vdyp.io.parse.coe.HLPrimarySpeciesEqnP1Parser;
import ca.bc.gov.nrs.vdyp.io.parse.coe.HLPrimarySpeciesEqnP2Parser;
import ca.bc.gov.nrs.vdyp.io.parse.coe.HLPrimarySpeciesEqnP3Parser;
import ca.bc.gov.nrs.vdyp.io.parse.coe.NonPrimarySpeciesBasalAreaGrowthParser;
import ca.bc.gov.nrs.vdyp.io.parse.coe.NonPrimarySpeciesDqGrowthParser;
import ca.bc.gov.nrs.vdyp.io.parse.coe.PrimarySpeciesBasalAreaGrowthParser;
import ca.bc.gov.nrs.vdyp.io.parse.coe.PrimarySpeciesDqGrowthParser;
import ca.bc.gov.nrs.vdyp.io.parse.coe.QuadraticMeanDiameterYieldParser;
import ca.bc.gov.nrs.vdyp.io.parse.coe.SiteCurveAgeMaximumParser;
import ca.bc.gov.nrs.vdyp.io.parse.coe.SiteCurveParser;
import ca.bc.gov.nrs.vdyp.io.parse.coe.SmallComponentBaseAreaParser;
import ca.bc.gov.nrs.vdyp.io.parse.coe.SmallComponentDQParser;
import ca.bc.gov.nrs.vdyp.io.parse.coe.SmallComponentHLParser;
import ca.bc.gov.nrs.vdyp.io.parse.coe.SmallComponentProbabilityParser;
import ca.bc.gov.nrs.vdyp.io.parse.coe.SmallComponentWSVolumeParser;
import ca.bc.gov.nrs.vdyp.io.parse.coe.TotalStandWholeStemParser;
import ca.bc.gov.nrs.vdyp.io.parse.coe.UpperBoundsParser;
import ca.bc.gov.nrs.vdyp.io.parse.coe.UpperCoefficientParser;
import ca.bc.gov.nrs.vdyp.io.parse.coe.UtilComponentBaseAreaParser;
import ca.bc.gov.nrs.vdyp.io.parse.coe.UtilComponentDQParser;
import ca.bc.gov.nrs.vdyp.io.parse.coe.UtilComponentWSVolumeParser;
import ca.bc.gov.nrs.vdyp.io.parse.coe.VeteranDQParser;
import ca.bc.gov.nrs.vdyp.io.parse.coe.VeteranLayerVolumeAdjustParser;
import ca.bc.gov.nrs.vdyp.io.parse.coe.VolumeEquationGroupParser;
import ca.bc.gov.nrs.vdyp.io.parse.coe.VolumeNetDecayParser;
import ca.bc.gov.nrs.vdyp.io.parse.coe.VolumeNetDecayWasteParser;
import ca.bc.gov.nrs.vdyp.io.parse.common.ResourceParseException;
import ca.bc.gov.nrs.vdyp.io.parse.control.ControlFileParser;
import ca.bc.gov.nrs.vdyp.io.parse.control.ControlMapModifier;
import ca.bc.gov.nrs.vdyp.io.parse.value.ValueParser;

/**
 * Parser for VDYP control files
 *
 * @author Michael Junkin, Vivid Solutions
 */
public class VdypForwardControlParser {

	private static final ValueParser<String> FILENAME = String::strip;

	private final VdypApplication app;

	ControlFileParser controlParser = new ControlFileParser()

			.record(ControlKey.MAX_NUM_POLY, ValueParser.INTEGER)

			.record(ControlKey.BEC_DEF, FILENAME) // RD_BECD
			.record(ControlKey.SP0_DEF, FILENAME) // RD_SP0

			.record(ControlKey.VDYP_POLYGON, FILENAME) //
			.record(ControlKey.VDYP_LAYER_BY_SPECIES, FILENAME) //
			.record(ControlKey.VDYP_LAYER_BY_SP0_BY_UTIL, FILENAME) //

			.record(ControlKey.VOLUME_EQN_GROUPS, FILENAME) // RD_VGRP
			.record(ControlKey.DECAY_GROUPS, FILENAME) // RD_DGRP
			.record(ControlKey.BREAKAGE_GROUPS, FILENAME) // RD_BGRP IPSJF157

			.record(ControlKey.SITE_CURVE_NUMBERS, ValueParser.optional(FILENAME)) // RD_E025
			.record(ControlKey.SITE_CURVE_AGE_MAX, ValueParser.optional(FILENAME)) // RD_E026

			.record(ControlKey.PARAM_ADJUSTMENTS, ValueParser.optional(FILENAME)) // RD_E028

			.record(ControlKey.DEFAULT_EQ_NUM, FILENAME) // RD_GRBA1
			.record(ControlKey.EQN_MODIFIERS, FILENAME) // RD_GMBA1

			.record(ControlKey.UPPER_BA_BY_CI_S0_P, FILENAME) // RD_E043 IPSJF128

			.record(ControlKey.HL_PRIMARY_SP_EQN_P1, FILENAME) // RD_YHL1
			.record(ControlKey.HL_PRIMARY_SP_EQN_P2, FILENAME) // RD_YHL2
			.record(ControlKey.HL_PRIMARY_SP_EQN_P3, FILENAME) // RD_YHL3
			.record(ControlKey.HL_NONPRIMARY, FILENAME) // RD_YHL4

			.record(ControlKey.BY_SPECIES_DQ, FILENAME) // RD_E060 IPFJF125
			.record(ControlKey.SPECIES_COMPONENT_SIZE_LIMIT, FILENAME) // RD_E061 IPSJF158

			.record(ControlKey.UTIL_COMP_BA, FILENAME) // RD_UBA1
			.record(ControlKey.UTIL_COMP_DQ, FILENAME) // RD_UDQ1

			.record(ControlKey.SMALL_COMP_PROBABILITY, FILENAME) // RD_SBA1
			.record(ControlKey.SMALL_COMP_BA, FILENAME) // RD_SBA2
			.record(ControlKey.SMALL_COMP_DQ, FILENAME) // RD_SDQ1
			.record(ControlKey.SMALL_COMP_HL, FILENAME) // RD_SHL1
			.record(ControlKey.SMALL_COMP_WS_VOLUME, FILENAME) // RD_SVT1

			.record(ControlKey.TOTAL_STAND_WHOLE_STEM_VOL, FILENAME) // RD_YVT1 IPSJF117
			.record(ControlKey.UTIL_COMP_WS_VOLUME, FILENAME) // RD_YVT2 IPSJF121
			.record(ControlKey.CLOSE_UTIL_VOLUME, FILENAME) // RD_YVC1 IPSJF122
			.record(ControlKey.VOLUME_NET_DECAY, FILENAME) // RD_YVD1 IPSJF123
			.record(ControlKey.VOLUME_NET_DECAY_WASTE, FILENAME) // RD_YVW1 IPSJF123
			.record(ControlKey.BREAKAGE, FILENAME) // RD_EMP95 IPSJF157

			.record(ControlKey.VETERAN_LAYER_VOLUME_ADJUST, FILENAME) // RD_YVET
			.record(ControlKey.VETERAN_LAYER_DQ, FILENAME) // RD_YDQV

			.record(ControlKey.VTROL, new VdypVtrolParser())

			.record(ControlKey.BA_YIELD, FILENAME) // RD_E106
			.record(ControlKey.DQ_YIELD, FILENAME) // RD_E107
			.record(ControlKey.BA_DQ_UPPER_BOUNDS, FILENAME) // RD_E108

			.record(ControlKey.BA_GROWTH_FIAT, FILENAME) // RD_E111
			.record(ControlKey.DQ_GROWTH_FIAT, FILENAME) // RD_E117

			.record(ControlKey.BA_GROWTH_EMPIRICAL, FILENAME) // RD_E121
			.record(ControlKey.DQ_GROWTH_EMPIRICAL, FILENAME) // RD_E122
			.record(ControlKey.DQ_GROWTH_EMPIRICAL_LIMITS, FILENAME) // RD_E123

			.record(ControlKey.PRIMARY_SP_BA_GROWTH, FILENAME) // RD_E148
			.record(ControlKey.NON_PRIMARY_SP_BA_GROWTH, FILENAME) // RD_E149
			.record(ControlKey.PRIMARY_SP_DQ_GROWTH, FILENAME) // RD_E150
			.record(ControlKey.NON_PRIMARY_SP_DQ_GROWTH, FILENAME) // RD_E151

			.record(ControlKey.MODIFIER_FILE, ValueParser.optional(FILENAME)) // RD_E198 IPSJF155, XII

			.record(ControlKey.DEBUG_SWITCHES, ValueParser.list(ValueParser.INTEGER));
	// Debug switches (25)
	// 0 is the default value. See IPSJF155, App. IX
	// (1) = 1 to DIASABLE species dynamics
	// (2) = n, maximum BH age in BA and DQ eqns = 100*n.
	// (3) = 0 Fiat BA growth model (approach to yield curves)
	// = 1 to invoke empirical BA growth model (see IPSJF176)
	// = 2 invoke empirical growth model PLUS mixing with fiat model.
	// (4) = (1,2) to use limits (SEQ108-GRPBA1, SEQ043-(CI,Pri))
	// = 0, defaults to option 2 at present.
	// (5) MATH77 Error message control. Should be zero.
	// = 0: show no errors
	// = 1: show some errors
	// = 2: shows all errors.
	// (6) = 0: for fiat DQ growth model (see IPSJF176, and SEQ 117)
	// = 1: for empirical
	// = 2: for mixed.
	// Recommend: 0 or 2.
	// (7) Not used
	// (8) = 1: force growth in non-primary HL to zero when del HD=0
	// = 2: same as 1, but also applies to primary species.
	// (9) = 1 for limited BA incr if DQ upper limit hit, else 0 (2009.03.18)
	// (10) Not used.
	// (11-20) Controls Ht/Age/SI fillin. See IPSJF174.doc
	// [Above values from Cam Bartram 14MAR2002]
	// (22) = 1 implies a preferred sp is primary if ba >.9995 of other. ;

	public VdypForwardControlParser(VdypApplication app) {
		this.app = app;
	}

	Map<String, Object> parse(Path inputFile) throws IOException, ResourceParseException {
		try (var is = Files.newInputStream(inputFile)) {

			return parse(is, new FileSystemFileResolver());
		}
	}

	List<ControlMapModifier> DATA_FILES = Arrays.asList(

			// V7O_VIP
			new VdypPolygonParser(),

			// V7O_VIS
			new VdypSpeciesParser(),

			// V7O_VIU
			new VdypUtilizationParser()
	);

	private void
			applyModifiers(Map<String, Object> control, List<ControlMapModifier> modifiers, FileResolver fileResolver)
					throws ResourceParseException, IOException {

		for (var modifier : modifiers) {
			modifier.modify(control, fileResolver);
		}
	}

	public Map<String, Object> parse(InputStream is, FileResolver fileResolver)
			throws IOException, ResourceParseException {

		var map = controlParser.parse(is, new HashMap<String, Object>());

		// This must be processed in an order identical to the order in GRO_INIT

		applyModifiers(
				map, List.of(
						// RD_BECD - 09
						new BecDefinitionParser(),

						// DEF_BEC - superseded by BecLookup.getGrowthBecs

						// RD_SP0 - 10
						new GenusDefinitionParser()
				), fileResolver
		);

		// Read Groups

		applyModifiers(
				map, List.of(
						// RD_VGRP - 20
						new VolumeEquationGroupParser(),
						// RD_DGRP - 21
						new DecayEquationGroupParser(),
						// RD_BGRP - 22
						new BreakageEquationGroupParser(),
						// RD_GRBA1 - 30
						new DefaultEquationNumberParser(),
						// RD_GMBA1 - 31
						new EquationModifierParser()
				), fileResolver
		);

		// Initialize data file parser factories

		applyModifiers(map, DATA_FILES, fileResolver);

		applyModifiers(
				map, List.of(
						// User-assigned SC's (Site Curve Numbers)
						// RD_E025
						new SiteCurveParser(),

						// Max tot ages to apply site curves (by SC)
						// RD_E026
						new SiteCurveAgeMaximumParser(),

						// RD_E028
						new CompVarAdjustmentsParser()
				), fileResolver
		);

		// Coeff for Empirical relationships

		applyModifiers(
				map, List.of(
						// RD_E043
						new UpperCoefficientParser(),
						// RD_YHL1 - 50
						new HLPrimarySpeciesEqnP1Parser(),
						// RD_YHL2 - 51
						new HLPrimarySpeciesEqnP2Parser(),
						// RD_YHL3 - 52
						new HLPrimarySpeciesEqnP3Parser(),
						// RD_YHL4 - 53
						new HLNonprimaryCoefficientParser(),
						// RD_E060
						new BySpeciesDqCoefficientParser(),

						// Min and max DQ by species
						// RD_E061
						new ComponentSizeParser(),
						// RD_UBA1 - 70
						new UtilComponentBaseAreaParser(),
						// RD_UDQ1 - 71
						new UtilComponentDQParser(),

						// Small Component (4.5 to 7.5 cm)

						// RD_SBA1 - 80
						new SmallComponentProbabilityParser(),
						// RD_SBA2 - 81
						new SmallComponentBaseAreaParser(),
						// RD_SDQ1 - 82
						new SmallComponentDQParser(),
						// RD_SHL1 - 85
						new SmallComponentHLParser(),
						// RD_SVT1 - 86
						new SmallComponentWSVolumeParser(),

						// Standard Volume Relationships

						// RD_YVT1 - 90
						new TotalStandWholeStemParser(),
						// RD_YVT2 - 91
						new UtilComponentWSVolumeParser(),
						// RD_YVC1 - 92
						new CloseUtilVolumeParser(),
						// RD_YVD1 - 93
						new VolumeNetDecayParser(),
						// RD_YVW1 - 94
						new VolumeNetDecayWasteParser(),
						// RD_E095
						new BreakageParser(),

						// Veterans
						// RD_YVVET - 96
						new VeteranLayerVolumeAdjustParser(),
						// RD_YDQV - 97
						new VeteranDQParser()
				), fileResolver
		);

		applyModifiers(
				map, List.of(
						// RD_E106
						new BasalAreaYieldParser(),
						// RD_E107
						new QuadraticMeanDiameterYieldParser(),
						// RD_E108
						new UpperBoundsParser()
				), fileResolver
		);

		assert VdypApplicationIdentifier.VDYP_FORWARD.getJProgramNumber() == app.getJProgramNumber();

		applyModifiers(
				map, List.of(
						// RD_E111
						new BasalAreaGrowthFiatParser(),
						// RD_E117
						new DqGrowthFiatParser(),
						// RD_E121
						new BasalAreaGrowthEmpiricalParser(),
						// RD_E122
						new DqGrowthEmpiricalParser(),
						// RD_E123
						new DqGrowthEmpiricalLimitsParser(),
						// RD_E148
						new PrimarySpeciesBasalAreaGrowthParser(),
						// RD_E149
						new NonPrimarySpeciesBasalAreaGrowthParser(),
						// RD_E150
						new PrimarySpeciesDqGrowthParser(),
						// RD_E151
						new NonPrimarySpeciesDqGrowthParser()
				), fileResolver
		);

		// Modifiers, IPSJF155-Appendix XII

		applyModifiers(
				map, List.of(
						// RD_E198
						new ModifierParser(VdypApplicationIdentifier.VDYP_FORWARD.getJProgramNumber())
				), fileResolver
		);

		// Debug switches (normally zero)
		// TODO - 199

		return map;
	}

}
