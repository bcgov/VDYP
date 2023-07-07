package ca.bc.gov.nrs.vdyp.fip;

import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.hasBec;
import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.hasSpecificEntry;
import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.mmHasEntry;
import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.present;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import ca.bc.gov.nrs.vdyp.io.parse.BecDefinitionParser;
import ca.bc.gov.nrs.vdyp.io.parse.ControlFileParserTest;
import ca.bc.gov.nrs.vdyp.io.parse.HLNonprimaryCoefficientParserTest;
import ca.bc.gov.nrs.vdyp.io.parse.ResourceParseException;
import ca.bc.gov.nrs.vdyp.io.parse.GenusDefinitionParser;
import ca.bc.gov.nrs.vdyp.io.parse.SiteCurveAgeMaximumParserTest;
import ca.bc.gov.nrs.vdyp.io.parse.StreamingParserFactory;
import ca.bc.gov.nrs.vdyp.model.BecDefinition;
import ca.bc.gov.nrs.vdyp.model.BecLookup;
import ca.bc.gov.nrs.vdyp.model.Region;
import ca.bc.gov.nrs.vdyp.model.GenusDefinition;
import ca.bc.gov.nrs.vdyp.model.SiteCurve;
import ca.bc.gov.nrs.vdyp.model.SiteCurveAgeMaximum;
import ca.bc.gov.nrs.vdyp.model.StockingClassFactor;

@SuppressWarnings({ "unused", "unchecked", "rawtypes" })
public class FipControlParserTest {

	@Test
	public void testParse() throws Exception {
		var parser = new FipControlParser();
		var result = parser.parse(ControlFileParserTest.class, "FIPSTART.CTR");

	}

	@Test
	public void testParseBec() throws Exception {
		var parser = new FipControlParser();
		var result = parser.parse(ControlFileParserTest.class, "FIPSTART.CTR");
		assertThat(
				result,
				(Matcher) hasSpecificEntry(
						BecDefinitionParser.CONTROL_KEY,
						allOf(instanceOf(BecLookup.class), hasBec("AT", present(instanceOf(BecDefinition.class))))
				)
		);
	}

	@Test
	public void testParseSP0() throws Exception {
		var parser = new FipControlParser();
		var result = parser.parse(ControlFileParserTest.class, "FIPSTART.CTR");
		assertThat(
				result,
				(Matcher) hasSpecificEntry(
						GenusDefinitionParser.CONTROL_KEY,
						allOf(instanceOf(List.class), hasItem(instanceOf(GenusDefinition.class)))
				)
		);
	}

	@Test
	public void testParseVGRP() throws Exception {
		var parser = new FipControlParser();
		var result = parser.parse(ControlFileParserTest.class, "FIPSTART.CTR");
		assertThat(
				result, (Matcher) hasSpecificEntry(
						FipControlParser.VOLUME_EQN_GROUPS, allOf(
								// Map of SP0 Aliases
								isA(Map.class), hasEntry(
										isA(String.class), allOf(
												// Map of BEC aliases
												isA(Map.class), hasEntry(
														isA(String.class), isA(Integer.class) // Equation Identifier
												)
										)
								)
						)
				)
		);
	}

	@Test
	public void testParseDGRP() throws Exception {
		var parser = new FipControlParser();
		var result = parser.parse(ControlFileParserTest.class, "FIPSTART.CTR");
		assertThat(
				result, (Matcher) hasSpecificEntry(
						FipControlParser.DECAY_GROUPS, allOf(
								// Map of SP0 Aliases
								isA(Map.class), hasEntry(
										isA(String.class), allOf(
												// Map of BEC aliases
												isA(Map.class), hasEntry(
														isA(String.class), isA(Integer.class) // Equation Identifier
												)
										)
								)
						)
				)
		);
	}

	@Test
	public void testParseBGRP() throws Exception {
		var parser = new FipControlParser();
		var result = parser.parse(ControlFileParserTest.class, "FIPSTART.CTR");
		assertThat(
				result, (Matcher) hasSpecificEntry(
						FipControlParser.BREAKAGE_GROUPS, allOf(
								// Map of SP0 Aliases
								isA(Map.class), hasEntry(
										isA(String.class), allOf(
												// Map of BEC aliases
												isA(Map.class), hasEntry(
														isA(String.class), isA(Integer.class) // Equation Identifier
												)
										)
								)
						)
				)
		);
	}

	@Test
	public void testParseGRBA1() throws Exception {
		var parser = new FipControlParser();
		var result = parser.parse(ControlFileParserTest.class, "FIPSTART.CTR");
		assertThat(
				result, (Matcher) hasSpecificEntry(
						FipControlParser.DEFAULT_EQ_NUM, allOf(
								// Map of SP0 Aliases
								isA(Map.class), hasEntry(
										isA(String.class), allOf(
												// Map of BEC aliases
												isA(Map.class), hasEntry(
														isA(String.class), isA(Integer.class) // Equation Identifier
												)
										)
								)
						)
				)
		);
	}

	@Test
	public void testParseGMBA1() throws Exception {
		var parser = new FipControlParser();
		var result = parser.parse(ControlFileParserTest.class, "FIPSTART.CTR");
		assertThat(
				result, (Matcher) hasSpecificEntry(
						FipControlParser.EQN_MODIFIERS, allOf(
								// Default Equation
								isA(Map.class), hasEntry(
										isA(Integer.class), allOf(
												// ITG
												isA(Map.class), hasEntry(
														isA(Integer.class), isA(Integer.class) // Reassigned Equation
												)
										)
								)
						)
				)
		);
	}

	@Test
	public void testParseSTK33() throws Exception {
		var parser = new FipControlParser();
		var result = parser.parse(ControlFileParserTest.class, "FIPSTART.CTR");
		assertThat(
				result, (Matcher) hasSpecificEntry(
						FipControlParser.STOCKING_CLASS_FACTORS, allOf(
								// STK
								isA(Map.class), hasEntry(
										isA(Character.class), allOf(
												// Region
												isA(Map.class),
												hasEntry(
														isA(Region.class), isA(StockingClassFactor.class) // Factors
												)
										)
								)
						)
				)
		);
	}

	@Test
	public void testParseE025() throws Exception {
		var parser = new FipControlParser();
		var result = parseWithAppendix(parser, "025 coe/SIEQN.PRM");
		assertThat(
				result, (Matcher) hasSpecificEntry(
						FipControlParser.SITE_CURVE_NUMBERS, allOf(
								// Species
								isA(Map.class), hasEntry(isA(String.class), isA(SiteCurve.class))
						)
				)
		);
	}

	@Test
	public void testParseE025Empty() throws Exception {
		var parser = new FipControlParser();
		var result = parser.parse(ControlFileParserTest.class, "FIPSTART.CTR");
		assertThat(result, (Matcher) hasSpecificEntry(FipControlParser.SITE_CURVE_NUMBERS, Matchers.anEmptyMap()));
	}

	@Test
	public void testParseE026() throws Exception {
		var parser = new FipControlParser();
		var result = parser.parse(ControlFileParserTest.class, "FIPSTART.CTR");
		assertThat(
				result, (Matcher) hasSpecificEntry(
						FipControlParser.SITE_CURVE_AGE_MAX, allOf(
								// Species
								isA(Map.class), hasEntry(isA(Integer.class), isA(SiteCurveAgeMaximum.class))
						)
				)
		);
	}

	@Test
	// @Disabled
	public void testParseE026Empty() throws Exception {
		var parser = new FipControlParser();
		var result = parseWithAppendix(parser, "026  ");
		// Map is empty but gives appropriate default values
		assertThat(result, (Matcher) hasSpecificEntry(FipControlParser.SITE_CURVE_AGE_MAX, Matchers.anEmptyMap()));
		assertThat(
				((Map<Integer, SiteCurveAgeMaximum>) result.get(FipControlParser.SITE_CURVE_AGE_MAX)).get(1),
				(Matcher) allOf(SiteCurveAgeMaximumParserTest.hasAge(Region.COASTAL, is(140.f)))
		);
	}

	@Test
	public void testParseE040() throws Exception {
		var parser = new FipControlParser();
		var result = parser.parse(ControlFileParserTest.class, "FIPSTART.CTR");
		assertThat(
				result,
				(Matcher) hasSpecificEntry(FipControlParser.COE_BA, allOf(mmHasEntry(present(is(2.0028f)), 0, "AT", 1)))
		);
	}

	@Test
	public void testParseE041() throws Exception {
		var parser = new FipControlParser();
		var result = parser.parse(ControlFileParserTest.class, "FIPSTART.CTR");
		assertThat(
				result,
				(Matcher) hasSpecificEntry(FipControlParser.COE_DQ, allOf(mmHasEntry(present(is(6.6190f)), 0, "AT", 1)))
		);
	}

	@Test
	public void testParseE043() throws Exception {
		var parser = new FipControlParser();
		var result = parser.parse(ControlFileParserTest.class, "FIPSTART.CTR");
		assertThat(
				result,
				(Matcher) hasSpecificEntry(
						FipControlParser.UPPER_BA_BY_CI_S0_P,
						allOf(mmHasEntry(present(is(109.27f)), Region.COASTAL, "AC", 1))
				)
		);
	}

	@Test
	public void testParseE050() throws Exception {
		var parser = new FipControlParser();
		var result = parser.parse(ControlFileParserTest.class, "FIPSTART.CTR");
		assertThat(
				result,
				(Matcher) hasSpecificEntry(
						FipControlParser.HL_PRIMARY_SP_EQN_P1,
						allOf(mmHasEntry(present(contains(1.00160f, 0.20508f, -0.0013743f)), "AC", Region.COASTAL))
				)
		);
	}

	@Test
	public void testParseE051() throws Exception {
		var parser = new FipControlParser();
		var result = parser.parse(ControlFileParserTest.class, "FIPSTART.CTR");
		assertThat(
				result,
				(Matcher) hasSpecificEntry(
						FipControlParser.HL_PRIMARY_SP_EQN_P2,
						allOf(mmHasEntry(present(contains(0.49722f, 1.18403f)), "AC", Region.COASTAL))
				)
		);
	}

	@Test
	public void testParseE052() throws Exception {
		var parser = new FipControlParser();
		var result = parser.parse(ControlFileParserTest.class, "FIPSTART.CTR");
		assertThat(
				result,
				(Matcher) hasSpecificEntry(
						FipControlParser.HL_PRIMARY_SP_EQN_P3,
						allOf(
								mmHasEntry(
										present(contains(1.04422f, 0.93010f, -0.05745f, -2.50000f)), "AC",
										Region.COASTAL
								)
						)
				)
		);
	}

	@Test
	public void testParseE053() throws Exception {
		var parser = new FipControlParser();
		var result = parser.parse(ControlFileParserTest.class, "FIPSTART.CTR");
		assertThat(
				result,
				(Matcher) hasSpecificEntry(
						FipControlParser.HL_NONPRIMARY,
						allOf(
								mmHasEntry(
										present(HLNonprimaryCoefficientParserTest.coe(0.86323f, 1.00505f, 1)), "AC",
										"AT", Region.COASTAL
								)
						)
				)
		);
	}

	@Test
	public void testParseE060() throws Exception {
		var parser = new FipControlParser();
		var result = parser.parse(ControlFileParserTest.class, "FIPSTART.CTR");
		assertThat(
				result,
				(Matcher) hasSpecificEntry(
						FipControlParser.BY_SPECIES_DQ,
						contains(
								contains(
										-0.65484f, -0.48275f, -0.75134f, 0.04482f, -0.31195f, -0.53012f, -0.12645f,
										-0.64668f, -0.43538f, -0.31134f, -0.03435f, -0.27833f, -0.32476f, 0.10819f,
										-0.38103f, -0.12273f
								),
								contains(
										2.26389f, 0.19886f, -0.25704f, 0.18579f, -0.38547f, -0.14115f, -0.10146f,
										0.09067f, 0.54304f, -0.02947f, 0.08473f, -0.39934f, 0.02206f, -0.18235f,
										0.01411f, -0.21683f
								),
								contains(
										0.23162f, 0.23162f, 0.23162f, 0.23162f, 0.23162f, 0.23162f, 0.23162f, 0.23162f,
										0.23162f, 0.23162f, 0.23162f, 0.23162f, 0.23162f, 0.23162f, 0.23162f, 0.23162f
								)
						)
				)
		);
	}

	@Test
	public void testParseE061() throws Exception {
		var parser = new FipControlParser();
		var result = parser.parse(ControlFileParserTest.class, "FIPSTART.CTR");
		assertThat(
				result,
				(Matcher) hasSpecificEntry(
						FipControlParser.SPECIES_COMPONENT_SIZE_LIMIT,
						allOf(mmHasEntry(present(contains(49.4f, 153.3f, 0.726f, 3.647f)), "AC", Region.COASTAL))
				)
		);
	}

	@Test
	public void testParseUBA1() throws Exception {
		var parser = new FipControlParser();
		var result = parser.parse(ControlFileParserTest.class, "FIPSTART.CTR");
		assertThat(
				result,
				(Matcher) hasSpecificEntry(
						FipControlParser.UTIL_COMP_BA,
						allOf(mmHasEntry(present(contains(-26.68771f, 14.38811f)), 2, "AT", "ICH"))
				)
		);
	}

	@Test
	public void testParseYVC1() throws Exception {
		var parser = new FipControlParser();
		var result = parser.parse(ControlFileParserTest.class, "FIPSTART.CTR");
		assertThat(
				result,
				(Matcher) hasSpecificEntry(
						FipControlParser.CLOSE_UTIL_VOLUME,
						allOf(mmHasEntry(present(contains(-3.249f, 0.2426f, 0.04621f)), 2, 53))
				)
		);
	}

	@Test
	public void testParseYVD1() throws Exception {
		var parser = new FipControlParser();
		var result = parser.parse(ControlFileParserTest.class, "FIPSTART.CTR");
		assertThat(
				result,
				(Matcher) hasSpecificEntry(
						FipControlParser.VOLUME_NET_DECAY,
						allOf(mmHasEntry(present(contains(12.7054f, 0.14984f, -1.73471f)), 2, 53))
				)
		);
	}

	@Test
	public void testParseSBA1() throws Exception {
		var parser = new FipControlParser();
		var result = parser.parse(ControlFileParserTest.class, "FIPSTART.CTR");
		assertThat(
				result,
				(Matcher) hasSpecificEntry(
						FipControlParser.SMALL_COMP_PROBABILITY,
						allOf(hasEntry(is("AT"), contains(-1.76158f, 2.50045f, -0.030447f, -0.11746f)))
				)
		);
	}

	@Test
	public void testParseSBA2() throws Exception {
		var parser = new FipControlParser();
		var result = parser.parse(ControlFileParserTest.class, "FIPSTART.CTR");
		assertThat(
				result,
				(Matcher) hasSpecificEntry(
						FipControlParser.SMALL_COMP_BA,
						allOf(hasEntry(is("B"), contains(-1.3504f, 9.5806f, 3.35173f, -0.27311f)))
				)
		);
	}

	@Test
	public void testParseSDQ1() throws Exception {
		var parser = new FipControlParser();
		var result = parser.parse(ControlFileParserTest.class, "FIPSTART.CTR");
		assertThat(
				result,
				(Matcher) hasSpecificEntry(
						FipControlParser.SMALL_COMP_DQ, allOf(hasEntry(is("B"), contains(-0.33485f, 0.02029f)))
				)
		);
	}

	@Test
	public void testParseSHL1() throws Exception {
		var parser = new FipControlParser();
		var result = parser.parse(ControlFileParserTest.class, "FIPSTART.CTR");
		assertThat(
				result,
				(Matcher) hasSpecificEntry(
						FipControlParser.SMALL_COMP_HL, allOf(hasEntry(is("B"), contains(-8.5269f, -0.20000f)))
				)
		);
	}

	@Test
	public void testParseSVT1() throws Exception {
		var parser = new FipControlParser();
		var result = parser.parse(ControlFileParserTest.class, "FIPSTART.CTR");
		assertThat(
				result,
				(Matcher) hasSpecificEntry(
						FipControlParser.SMALL_COMP_WS_VOLUME,
						allOf(hasEntry(is("B"), contains(-9.6020f, 1.09191f, 1.26171f, 0.10841f)))
				)
		);
	}

	@Test
	public void testParseYVT1() throws Exception {
		var parser = new FipControlParser();
		var result = parser.parse(ControlFileParserTest.class, "FIPSTART.CTR");
		assertThat(
				result,
				(Matcher) hasSpecificEntry(
						FipControlParser.TOTAL_STAND_WHOLE_STEM_VOL,
						allOf(
								hasEntry(
										is(2),
										contains(
												-10.41832f, 1.94182f, 0.99414f, 0.000000f, 1.11329f, 0.000000f,
												0.0000000f, 0.0000000f, 0.19884f
										)
								)
						)
				)
		);
	}

	@Test
	public void testParseYVT2() throws Exception {
		var parser = new FipControlParser();
		var result = parser.parse(ControlFileParserTest.class, "FIPSTART.CTR");
		assertThat(
				result,
				(Matcher) hasSpecificEntry(
						FipControlParser.UTIL_COMP_WS_VOLUME,
						allOf(mmHasEntry(present(contains(-1.44375f, 1.20115f, 1.14639f, -1.17521f)), 2, 11))
				)
		);
	}

	@Test
	public void testParseYVW1() throws Exception {
		var parser = new FipControlParser();
		var result = parser.parse(ControlFileParserTest.class, "FIPSTART.CTR");
		assertThat(
				result,
				(Matcher) hasSpecificEntry(
						FipControlParser.VOLUME_NET_DECAY_WASTE,
						allOf(hasEntry(is("B"), contains(-4.2025f, 11.2235f, -33.0270f, 0.1246f, -0.2318f, -0.1259f)))
				)
		);
	}

	@Test
	public void testParseE095() throws Exception {
		var parser = new FipControlParser();
		var result = parser.parse(ControlFileParserTest.class, "FIPSTART.CTR");
		assertThat(
				result,
				(Matcher) hasSpecificEntry(
						FipControlParser.BREAKAGE, allOf(hasEntry(is(10), contains(-0.7153f, 2.0108f, 4.00f, 8.00f)))
				)
		);
	}

	@Test
	public void testParseYVVET() throws Exception {
		var parser = new FipControlParser();
		var result = parser.parse(ControlFileParserTest.class, "FIPSTART.CTR");
		assertThat(
				result,
				(Matcher) hasSpecificEntry(
						FipControlParser.VETERAN_LAYER_VOLUME_ADJUST,
						allOf(hasEntry(is("B"), contains(0.10881f, -0.09818f, 0.00048f, -0.00295f)))
				)
		);
	}

	@Test
	public void testParseYDQV() throws Exception {
		var parser = new FipControlParser();
		var result = parser.parse(ControlFileParserTest.class, "FIPSTART.CTR");
		assertThat(
				result,
				(Matcher) hasSpecificEntry(
						FipControlParser.VETERAN_LAYER_DQ,
						allOf(
								mmHasEntry(present(contains(22.500f, 0.24855f, 1.46089f)), "B", Region.COASTAL),
								mmHasEntry(present(contains(19.417f, 0.04354f, 1.96395f)), "B", Region.INTERIOR),
								mmHasEntry(present(contains(22.500f, 0.80260f, 1.00000f)), "D", Region.COASTAL),
								mmHasEntry(present(contains(22.500f, 0.80260f, 1.00000f)), "D", Region.INTERIOR)
						)
				)
		);
	}

	@Test
	public void testParseE098() throws Exception {
		var parser = new FipControlParser();
		var result = parser.parse(ControlFileParserTest.class, "FIPSTART.CTR");
		assertThat(
				result, (Matcher) hasSpecificEntry(
						FipControlParser.VETERAN_BQ,
						// Includes modifiers from 198
						allOf(
								mmHasEntry(
										present(contains(0.12874f * 0.311f, 8.00000f, 1.26982f)), "B", Region.COASTAL
								),
								mmHasEntry(
										present(contains(0.70932f * 0.374f, 7.63269f, 0.62545f)), "B", Region.INTERIOR
								),
								mmHasEntry(
										present(contains(0.07962f * 0.311f, 6.60231f, 1.37998f)), "D", Region.COASTAL
								),
								mmHasEntry(
										present(contains(0.07962f * 0.374f, 6.60231f, 1.37998f)), "D", Region.INTERIOR
								)
						)
				)
		);
	}

	@Test
	public void testParseV7O_FIP() throws Exception {
		var parser = new FipControlParser();
		var result = parser.parse(ControlFileParserTest.class, "FIPSTART.CTR");
		assertThat(
				result,
				(Matcher) hasSpecificEntry(FipPolygonParser.CONTROL_KEY, instanceOf(StreamingParserFactory.class))
		);
	}

	@Test
	public void testParseV7O_FIL() throws Exception {
		var parser = new FipControlParser();
		var result = parser.parse(ControlFileParserTest.class, "FIPSTART.CTR");
		assertThat(
				result, (Matcher) hasSpecificEntry(FipLayerParser.CONTROL_KEY, instanceOf(StreamingParserFactory.class))
		);
	}

	@Test
	public void testParseV7O_FIS() throws Exception {
		var parser = new FipControlParser();
		var result = parser.parse(ControlFileParserTest.class, "FIPSTART.CTR");
		assertThat(
				result,
				(Matcher) hasSpecificEntry(FipSpeciesParser.CONTROL_KEY, instanceOf(StreamingParserFactory.class))
		);
	}

	static InputStream addToEnd(InputStream is, String... lines) {
		var appendix = new ByteArrayInputStream(String.join("\r\n", lines).getBytes(StandardCharsets.US_ASCII));
		var result = new SequenceInputStream(is, appendix);
		return result;
	}

	static Map<String, ?> parseWithAppendix(FipControlParser parser, String... lines)
			throws IOException, ResourceParseException {
		var resolver = FipControlParser.fileResolver(ControlFileParserTest.class);
		try (
				InputStream baseIs = ControlFileParserTest.class.getResourceAsStream("FIPSTART.CTR");
				InputStream is = addToEnd(baseIs, lines);
		) {
			return parser.parse(is, resolver);
		}
	}
}
