package ca.bc.gov.nrs.vdyp.forward;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.bc.gov.nrs.vdyp.application.VdypApplicationIdentifier;
import ca.bc.gov.nrs.vdyp.common.ControlKey;
import ca.bc.gov.nrs.vdyp.common.Utils;
import ca.bc.gov.nrs.vdyp.forward.parsers.VdypPolygonDescriptionParser;
import ca.bc.gov.nrs.vdyp.forward.parsers.VdypPolygonParser;
import ca.bc.gov.nrs.vdyp.forward.parsers.VdypSpeciesParser;
import ca.bc.gov.nrs.vdyp.forward.parsers.VdypUtilizationParser;
import ca.bc.gov.nrs.vdyp.forward.parsers.VdypVtrolParser;
import ca.bc.gov.nrs.vdyp.io.FileResolver;
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
import ca.bc.gov.nrs.vdyp.io.parse.coe.ModifierParser;
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
import ca.bc.gov.nrs.vdyp.io.parse.control.BaseControlParser;
import ca.bc.gov.nrs.vdyp.io.parse.control.ControlMapValueReplacer;
import ca.bc.gov.nrs.vdyp.io.parse.control.ResourceControlMapModifier;
import ca.bc.gov.nrs.vdyp.io.parse.value.ValueParser;

/**
 * Parser for VDYP control files
 *
 * @author Michael Junkin, Vivid Solutions
 */
public class ForwardControlParser extends BaseControlParser {

	private static final Logger logger = LoggerFactory.getLogger(ForwardControlParser.class);

	public ForwardControlParser() {
		initialize();

		controlParser.record(ControlKey.VTROL, new VdypVtrolParser());
	}

	@Override
	protected VdypApplicationIdentifier getProgramId() {
		return VdypApplicationIdentifier.VDYP_FORWARD;
	}

	@Override
	protected List<ControlMapValueReplacer<Object, String>> inputFileParsers() {
		return new ArrayList<>(vdypForwardInputParsers.values());
	}

	@Override
	protected List<ControlKey> outputFileParsers() {
		return Collections.emptyList();
	}

	@Override
	protected ValueParser<Map<String, Float>> minimaParser() {
		return ValueParser.callback(
				ValueParser.toMap(
						ValueParser.list(ValueParser.FLOAT), Collections.singletonMap(MINIMUM_VETERAN_HEIGHT, 10.0f),
						MINIMUM_HEIGHT, MINIMUM_BASE_AREA, MINIMUM_PREDICTED_BASE_AREA, MINIMUM_VETERAN_HEIGHT
				),
				minima -> logger.atDebug().setMessage(
						"Minima read from VRISTART Control at line {}\n  Minimum Height: {}\n  Minimum BA: {}\n  Minimum Predicted BA: {}\n  Minimum Veteran Height: {}"
				) //
						.addArgument(ControlKey.MINIMA.sequence.map(i -> Integer.toString(i)).orElse("N/A"))
						.addArgument(minima.get(MINIMUM_HEIGHT)) //
						.addArgument(minima.get(MINIMUM_BASE_AREA)) //
						.addArgument(minima.get(MINIMUM_PREDICTED_BASE_AREA)) //
						.addArgument(minima.get(MINIMUM_VETERAN_HEIGHT))
		);
	}

	private final List<ControlKey> orderedControlKeys = new ArrayList<>();

	private final Map<ControlKey, ControlMapValueReplacer<Object, String>> vdypForwardInputParsers = new EnumMap<>(
			ControlKey.class
	);

	private void addInputParser(ControlMapValueReplacer<Object, String> parser) {
		vdypForwardInputParsers.put(parser.getControlKey(), parser);
		orderedControlKeys.add(parser.getControlKey());
	}

	private final Map<ControlKey, ResourceControlMapModifier> vdypForwardConfigurationParsers = new EnumMap<>(
			ControlKey.class
	);

	private void addConfigurationParser(ResourceControlMapModifier parser) {
		vdypForwardConfigurationParsers.put(parser.getControlKey(), parser);
		orderedControlKeys.add(parser.getControlKey());
	}

	@Override
	protected void initialize() {

		// RD_BECD - 09
		addConfigurationParser(new BecDefinitionParser());

		// DEF_BEC - superceded by BecLookup.getGrowthBecs

		// RD_SP0 - 10
		addConfigurationParser(new GenusDefinitionParser());

		// RD_VGRP - 20
		addConfigurationParser(new VolumeEquationGroupParser());
		// RD_DGRP - 21
		addConfigurationParser(new DecayEquationGroupParser());
		// RD_BGRP - 22
		addConfigurationParser(new BreakageEquationGroupParser());
		// RD_GRBA1 - 30
		addConfigurationParser(new DefaultEquationNumberParser());
		// RD_GMBA1 - 31
		addConfigurationParser(new EquationModifierParser());

		// RD_E025: User-assigned SC's (Site Curve Numbers)
		addConfigurationParser(new SiteCurveParser());

		// RD_E026: Max tot ages to apply site curves (by SC)
		addConfigurationParser(new SiteCurveAgeMaximumParser());

		// RD_E028
		addConfigurationParser(new CompVarAdjustmentsParser());

		// RD_E043
		addConfigurationParser(new UpperCoefficientParser());
		// RD_YHL1 - 50
		addConfigurationParser(new HLPrimarySpeciesEqnP1Parser());
		// RD_YHL2 - 51
		addConfigurationParser(new HLPrimarySpeciesEqnP2Parser());
		// RD_YHL3 - 52
		addConfigurationParser(new HLPrimarySpeciesEqnP3Parser());
		// RD_YHL4 - 53
		addConfigurationParser(new HLNonprimaryCoefficientParser());
		// RD_E060
		addConfigurationParser(new BySpeciesDqCoefficientParser());

		// Min and max DQ by species

		// RD_E061
		addConfigurationParser(new ComponentSizeParser());
		// RD_UBA1 - 70
		addConfigurationParser(new UtilComponentBaseAreaParser());
		// RD_UDQ1 - 71
		addConfigurationParser(new UtilComponentDQParser());

		// Small Component (4.5 to 7.5 cm)

		// RD_SBA1 - 80
		addConfigurationParser(new SmallComponentProbabilityParser());
		// RD_SBA2 - 81
		addConfigurationParser(new SmallComponentBaseAreaParser());
		// RD_SDQ1 - 82
		addConfigurationParser(new SmallComponentDQParser());
		// RD_SHL1 - 85
		addConfigurationParser(new SmallComponentHLParser());
		// RD_SVT1 - 86
		addConfigurationParser(new SmallComponentWSVolumeParser());

		// Standard Volume Relationships

		// RD_YVT1 - 90
		addConfigurationParser(new TotalStandWholeStemParser());
		// RD_YVT2 - 91
		addConfigurationParser(new UtilComponentWSVolumeParser());
		// RD_YVC1 - 92
		addConfigurationParser(new CloseUtilVolumeParser());
		// RD_YVD1 - 93
		addConfigurationParser(new VolumeNetDecayParser());
		// RD_YVW1 - 94
		addConfigurationParser(new VolumeNetDecayWasteParser());
		// RD_E095
		addConfigurationParser(new BreakageParser());

		// Veterans

		// RD_YVVET - 96
		addConfigurationParser(new VeteranLayerVolumeAdjustParser());
		// RD_YDQV - 97
		addConfigurationParser(new VeteranDQParser());

		// RD_E106
		addConfigurationParser(new BasalAreaYieldParser());
		// RD_E107
		addConfigurationParser(new QuadraticMeanDiameterYieldParser());
		// RD_E108
		addConfigurationParser(new UpperBoundsParser());
		// RD_E111
		addConfigurationParser(new BasalAreaGrowthFiatParser());
		// RD_E117
		addConfigurationParser(new DqGrowthFiatParser());
		// RD_E121
		addConfigurationParser(new BasalAreaGrowthEmpiricalParser());
		// RD_E122
		addConfigurationParser(new DqGrowthEmpiricalParser());
		// RD_E123
		addConfigurationParser(new DqGrowthEmpiricalLimitsParser());
		// RD_E148
		addConfigurationParser(new PrimarySpeciesBasalAreaGrowthParser());
		// RD_E149
		addConfigurationParser(new NonPrimarySpeciesBasalAreaGrowthParser());
		// RD_E150
		addConfigurationParser(new PrimarySpeciesDqGrowthParser());
		// RD_E151
		addConfigurationParser(new NonPrimarySpeciesDqGrowthParser());

		// RD_E198: Modifiers); IPSJF155-Appendix XII
		addConfigurationParser(new ModifierParser(VdypApplicationIdentifier.VDYP_FORWARD));

		// V7O_VIP - 11
		addInputParser(new VdypPolygonParser());
		// V7O_VIS - 12
		addInputParser(new VdypSpeciesParser());
		// V7O_VIU - 13
		addInputParser(new VdypUtilizationParser());
		// V7O_VI7 - 14
		addInputParser(new VdypPolygonDescriptionParser());

		// 101 - a literal value of type VdypGrowthDetails
		orderedControlKeys.add(ControlKey.VTROL);

		// 199 - debug switches
		orderedControlKeys.add(ControlKey.DEBUG_SWITCHES);

		// 1 - MAX_NUM_POLY
		orderedControlKeys.add(ControlKey.MAX_NUM_POLY);

		super.initialize();
	}

	@Override
	protected List<ResourceControlMapModifier> configurationFileParsers() {
		return new ArrayList<>(vdypForwardConfigurationParsers.values());
	}

	@Override
	protected void applyAllModifiers(Map<String, Object> map, FileResolver fileResolver)
			throws ResourceParseException, IOException {

		// FORWARD_INPUT_GROWTO is optional; if missing, the polygon list is read from the
		// polygon file itself.
		Optional<String> source = Utils.optSafe(map.get(ControlKey.FORWARD_INPUT_GROWTO.name()));
		if (source.isEmpty()) {
			String polyFileName = (String) map.get(ControlKey.FORWARD_INPUT_VDYP_POLY.name());
			map.put(ControlKey.FORWARD_INPUT_GROWTO.name(), polyFileName);
		}

		for (ControlKey key : orderedControlKeys) {

			ResourceControlMapModifier m = vdypForwardConfigurationParsers.get(key);
			if (m != null) {
				// m is a configuration file parser.
				logger.debug(
						"Parsing configuration file {}[{}] using {}", m.getControlKeyName(), key.sequence.get(),
						m.getClass().getName()
				);
				m.modify(map, fileResolver);
			}

			ControlMapValueReplacer<?, ?> r = vdypForwardInputParsers.get(key);
			if (r != null) {
				// r is an input file parser.
				logger.debug(
						"Parsing input file {}[{}] using {}", r.getControlKeyName(), key.sequence.get(),
						r.getClass().getName()
				);
				r.modify(map, fileResolver);
			}
		}

		// Report any control map items that are a) not included in orderedControlKeys
		// or b) for which
		// not parser was registered.

		// a
		Set<ControlKey> parsersNotExecuted = new HashSet<>(
				map.keySet().stream().filter(ControlKey::isControlKey).map(ControlKey::valueOf)
						.filter(k -> k.sequence.isPresent()).toList()
		);
		parsersNotExecuted.removeAll(orderedControlKeys);
		parsersNotExecuted.stream().forEach(
				e -> logger.warn("{}[{}] was present in the configuration file but not read", e, e.sequence.get())
		);

		// b
		map.keySet().stream().filter(k -> !ControlKey.isControlKey(k))
				.forEach(k -> logger.warn("{} was present in the configuration file but no parser was registered", k));
	}
}
