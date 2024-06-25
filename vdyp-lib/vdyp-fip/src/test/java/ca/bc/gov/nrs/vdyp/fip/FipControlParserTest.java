package ca.bc.gov.nrs.vdyp.fip;

import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.coe;
import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.controlMapHasEntry;
import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.hasBec;
import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.mmEmpty;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import ca.bc.gov.nrs.vdyp.common.ControlKey;
import ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexEquation;
import ca.bc.gov.nrs.vdyp.io.parse.coe.SiteCurveAgeMaximumParserTest;
import ca.bc.gov.nrs.vdyp.io.parse.common.ResourceParseException;
import ca.bc.gov.nrs.vdyp.io.parse.control.BaseControlParser;
import ca.bc.gov.nrs.vdyp.io.parse.streaming.StreamingParserFactory;
import ca.bc.gov.nrs.vdyp.model.BecDefinition;
import ca.bc.gov.nrs.vdyp.model.BecLookup;
import ca.bc.gov.nrs.vdyp.model.GenusDefinition;
import ca.bc.gov.nrs.vdyp.model.MatrixMap2;
import ca.bc.gov.nrs.vdyp.model.Region;
import ca.bc.gov.nrs.vdyp.model.SiteCurveAgeMaximum;
import ca.bc.gov.nrs.vdyp.model.StockingClassFactor;
import ca.bc.gov.nrs.vdyp.test.TestUtils;

@SuppressWarnings({ "unused", "unchecked", "rawtypes" })
class FipControlParserTest {

	@Test
	void testParseBec() throws Exception {
		BaseControlParser parser = new FipControlParser();
		var result = parse(parser, TestUtils.class, "FIPSTART.CTR");
		assertThat(
				result,
				(Matcher) controlMapHasEntry(
						ControlKey.BEC_DEF,
						allOf(instanceOf(BecLookup.class), hasBec("AT", present(instanceOf(BecDefinition.class))))
				)
		);
	}

	@Test
	void testParseSP0() throws Exception {
		BaseControlParser parser = new FipControlParser();
		var result = parse(parser, TestUtils.class, "FIPSTART.CTR");
		assertThat(
				result,
				(Matcher) controlMapHasEntry(
						ControlKey.SP0_DEF, allOf(instanceOf(List.class), hasItem(instanceOf(GenusDefinition.class)))
				)
		);
	}

	@Test
	void testParseVGRP() throws Exception {
		BaseControlParser parser = new FipControlParser();
		var result = parse(parser, TestUtils.class, "FIPSTART.CTR");
		assertThat(
				result,
				(Matcher) controlMapHasEntry(
						ControlKey.VOLUME_EQN_GROUPS, allOf(isA(MatrixMap2.class), mmHasEntry(is(7), "AT", "CDF"))
				)
		);
	}

	@Test
	void testParseDGRP() throws Exception {
		BaseControlParser parser = new FipControlParser();
		var result = parse(parser, TestUtils.class, "FIPSTART.CTR");
		assertThat(
				result,
				(Matcher) controlMapHasEntry(
						ControlKey.DECAY_GROUPS, allOf(isA(MatrixMap2.class), mmHasEntry(is(5), "AT", "CDF"))
				)
		);
	}

	@Test
	void testParseBGRP() throws Exception {
		BaseControlParser parser = new FipControlParser();
		var result = parse(parser, TestUtils.class, "FIPSTART.CTR");
		assertThat(
				result,
				(Matcher) controlMapHasEntry(
						ControlKey.BREAKAGE_GROUPS, allOf(isA(MatrixMap2.class), mmHasEntry(is(3), "AT", "CDF"))
				)
		);
	}

	@Test
	void testParseGRBA1() throws Exception {
		BaseControlParser parser = new FipControlParser();
		var result = parse(parser, TestUtils.class, "FIPSTART.CTR");
		assertThat(
				result,
				(Matcher) controlMapHasEntry(
						ControlKey.DEFAULT_EQ_NUM, allOf(isA(MatrixMap2.class), mmHasEntry(is(11), "AT", "CDF"))
				)
		);
	}

	@Test
	void testParseGMBA1() throws Exception {
		BaseControlParser parser = new FipControlParser();
		var result = parse(parser, TestUtils.class, "FIPSTART.CTR");
		assertThat(
				result, (Matcher) controlMapHasEntry(
						ControlKey.EQN_MODIFIERS, allOf(
								// Default Equation
								isA(MatrixMap2.class), mmHasEntry(present(is(34)), 33, 9)
						)
				)
		);
	}

	@Test
	void testParseSTK33() throws Exception {
		BaseControlParser parser = new FipControlParser();
		var result = parse(parser, TestUtils.class, "FIPSTART.CTR");
		assertThat(
				result, (Matcher) controlMapHasEntry(
						ControlKey.STOCKING_CLASS_FACTORS, allOf(
								// STK
								isA(MatrixMap2.class),
								mmHasEntry(present(isA(StockingClassFactor.class)), 'R', Region.COASTAL)
						)
				)
		);
	}

	@Test
	void testParseE025() throws Exception {
		BaseControlParser parser = new FipControlParser();
		var result = parseWithAppendix(parser, "025 coe/SIEQN.PRM");
		assertThat(
				result, (Matcher) controlMapHasEntry(
						ControlKey.SITE_CURVE_NUMBERS, allOf(
								// Species
								isA(MatrixMap2.class), mmHasEntry(isA(SiteIndexEquation.class), "MB", Region.COASTAL)
						)
				)
		);
	}

	@Test
	void testParseE025Empty() throws Exception {
		BaseControlParser parser = new FipControlParser();
		var result = parse(parser, TestUtils.class, "FIPSTART.CTR");
		assertThat(result, (Matcher) controlMapHasEntry(ControlKey.SITE_CURVE_NUMBERS, mmEmpty()));
	}

	@Test
	void testParseE026() throws Exception {
		BaseControlParser parser = new FipControlParser();
		var result = parse(parser, TestUtils.class, "FIPSTART.CTR");
		assertThat(
				result, (Matcher) controlMapHasEntry(
						ControlKey.SITE_CURVE_AGE_MAX, allOf(
								// Species
								isA(Map.class), hasEntry(isA(Integer.class), isA(SiteCurveAgeMaximum.class))
						)
				)
		);
	}

	@Test
	// @Disabled
	void testParseE026Empty() throws Exception {
		BaseControlParser parser = new FipControlParser();
		var result = parseWithAppendix(parser, "026  ");
		// Map is empty but gives appropriate default values
		assertThat(result, (Matcher) controlMapHasEntry(ControlKey.SITE_CURVE_AGE_MAX, Matchers.anEmptyMap()));
		assertThat(
				((Map<Integer, SiteCurveAgeMaximum>) result.get(ControlKey.SITE_CURVE_AGE_MAX.name())).get(1),
				(Matcher) allOf(SiteCurveAgeMaximumParserTest.hasAge(Region.COASTAL, is(140.f)))
		);
	}

	@Test
	void testParseE040() throws Exception {
		BaseControlParser parser = new FipControlParser();
		var result = parse(parser, TestUtils.class, "FIPSTART.CTR");
		assertThat(
				result,
				(Matcher) controlMapHasEntry(
						ControlKey.COE_BA,
						allOf(
								mmHasEntry(
										coe(
												0,
												contains(
														2.0028f, 0.2426f, 10.1668f, -0.9042f, -5.0012f, -0.0068f,
														-0.0095f, 1.1938f, -0.2749f, 0f
												)
										), "AT", "AC"
								)
						)
				)
		);
	}

	@Test
	void testParseE041() throws Exception {
		BaseControlParser parser = new FipControlParser();
		var result = parse(parser, TestUtils.class, "FIPSTART.CTR");
		assertThat(
				result,
				(Matcher) controlMapHasEntry(
						ControlKey.COE_DQ,
						allOf(
								mmHasEntry(
										coe(
												0,
												contains(
														6.6190f, -0.5579f, -1.9421f, -0.7092f, -5.2290f, 4.8473f,
														0.2629f, -0.0062f, 0f, 0f
												)
										), "AT", "AC"
								)
						)
				)
		);
	}

	@Test
	void testParseE043() throws Exception {
		BaseControlParser parser = new FipControlParser();
		var result = parse(parser, TestUtils.class, "FIPSTART.CTR");
		assertThat(
				result,
				(Matcher) controlMapHasEntry(
						ControlKey.UPPER_BA_BY_CI_S0_P, allOf(mmHasEntry(is(109.27f), Region.COASTAL, "AC", 1))
				)
		);
	}

	@Test
	void testParseE050() throws Exception {
		BaseControlParser parser = new FipControlParser();
		var result = parse(parser, TestUtils.class, "FIPSTART.CTR");
		assertThat(
				result,
				(Matcher) controlMapHasEntry(
						ControlKey.HL_PRIMARY_SP_EQN_P1,
						allOf(mmHasEntry(coe(1, contains(1.00160f, 0.20508f, -0.0013743f)), "AC", Region.COASTAL))
				)
		);
	}

	@Test
	void testParseE051() throws Exception {
		BaseControlParser parser = new FipControlParser();
		var result = parse(parser, TestUtils.class, "FIPSTART.CTR");
		assertThat(
				result,
				(Matcher) controlMapHasEntry(
						ControlKey.HL_PRIMARY_SP_EQN_P2,
						allOf(mmHasEntry(coe(1, contains(0.49722f, 1.18403f)), "AC", Region.COASTAL))
				)
		);
	}

	@Test
	void testParseE052() throws Exception {
		BaseControlParser parser = new FipControlParser();
		var result = parse(parser, TestUtils.class, "FIPSTART.CTR");
		assertThat(
				result,
				(Matcher) controlMapHasEntry(
						ControlKey.HL_PRIMARY_SP_EQN_P3,
						allOf(
								mmHasEntry(
										coe(1, contains(1.04422f, 0.93010f, -0.05745f, -2.50000f)), "AC", Region.COASTAL
								)
						)
				)
		);
	}

	@Test
	void testParseE053() throws Exception {
		BaseControlParser parser = new FipControlParser();
		var result = parse(parser, TestUtils.class, "FIPSTART.CTR");
		assertThat(
				result,
				(Matcher) controlMapHasEntry(
						ControlKey.HL_NONPRIMARY,
						allOf(mmHasEntry(present(coe(1, 0.86323f, 1.00505f)), "AC", "AT", Region.COASTAL))
				)
		);
	}

	@Test
	void testParseE060() throws Exception {
		BaseControlParser parser = new FipControlParser();
		var result = parse(parser, TestUtils.class, "FIPSTART.CTR");
		assertThat(
				result,
				(Matcher) controlMapHasEntry(
						ControlKey.BY_SPECIES_DQ, hasEntry(is("AT"), coe(0, -0.48275f, 0.19886f, 0.23162f))
				)
		);
	}

	@Test
	void testParseE061() throws Exception {
		BaseControlParser parser = new FipControlParser();
		var result = parse(parser, TestUtils.class, "FIPSTART.CTR");
		assertThat(
				result,
				(Matcher) controlMapHasEntry(
						ControlKey.SPECIES_COMPONENT_SIZE_LIMIT,
						allOf(mmHasEntry(coe(1, contains(49.4f, 153.3f, 0.726f, 3.647f)), "AC", Region.COASTAL))
				)
		);
	}

	@Test
	void testParseUBA1() throws Exception {
		BaseControlParser parser = new FipControlParser();
		var result = parse(parser, TestUtils.class, "FIPSTART.CTR");
		assertThat(
				result,
				(Matcher) controlMapHasEntry(
						ControlKey.UTIL_COMP_BA,
						allOf(mmHasEntry(coe(1, contains(-26.68771f, 14.38811f)), 1, "AT", "ICH"))
				)
		);
	}

	@Test
	void testParseYVC1() throws Exception {
		BaseControlParser parser = new FipControlParser();
		var result = parse(parser, TestUtils.class, "FIPSTART.CTR");
		assertThat(
				result,
				(Matcher) controlMapHasEntry(
						ControlKey.CLOSE_UTIL_VOLUME,
						allOf(mmHasEntry(present(coe(1, contains(-3.249f, 0.2426f, 0.04621f))), 2, 53))
				)
		);
	}

	@Test
	void testParseYVD1() throws Exception {
		BaseControlParser parser = new FipControlParser();
		var result = parse(parser, TestUtils.class, "FIPSTART.CTR");
		assertThat(
				result,
				(Matcher) controlMapHasEntry(
						ControlKey.VOLUME_NET_DECAY,
						allOf(mmHasEntry(present(coe(1, contains(12.7054f, 0.14984f, -1.73471f))), 2, 53))
				)
		);
	}

	@Test
	void testParseSBA1() throws Exception {
		BaseControlParser parser = new FipControlParser();
		var result = parse(parser, TestUtils.class, "FIPSTART.CTR");
		assertThat(
				result,
				(Matcher) controlMapHasEntry(
						ControlKey.SMALL_COMP_PROBABILITY,
						allOf(hasEntry(is("AT"), contains(-1.76158f, 2.50045f, -0.030447f, -0.11746f)))
				)
		);
	}

	@Test
	void testParseSBA2() throws Exception {
		BaseControlParser parser = new FipControlParser();
		var result = parse(parser, TestUtils.class, "FIPSTART.CTR");
		assertThat(
				result,
				(Matcher) controlMapHasEntry(
						ControlKey.SMALL_COMP_BA,
						allOf(hasEntry(is("B"), contains(-1.3504f, 9.5806f, 3.35173f, -0.27311f)))
				)
		);
	}

	@Test
	void testParseSDQ1() throws Exception {
		BaseControlParser parser = new FipControlParser();
		var result = parse(parser, TestUtils.class, "FIPSTART.CTR");
		assertThat(
				result,
				(Matcher) controlMapHasEntry(
						ControlKey.SMALL_COMP_DQ, allOf(hasEntry(is("B"), contains(-0.33485f, 0.02029f)))
				)
		);
	}

	@Test
	void testParseSHL1() throws Exception {
		BaseControlParser parser = new FipControlParser();
		var result = parse(parser, TestUtils.class, "FIPSTART.CTR");
		assertThat(
				result,
				(Matcher) controlMapHasEntry(
						ControlKey.SMALL_COMP_HL, allOf(hasEntry(is("B"), contains(-8.5269f, -0.20000f)))
				)
		);
	}

	@Test
	void testParseSVT1() throws Exception {
		BaseControlParser parser = new FipControlParser();
		var result = parse(parser, TestUtils.class, "FIPSTART.CTR");
		assertThat(
				result,
				(Matcher) controlMapHasEntry(
						ControlKey.SMALL_COMP_WS_VOLUME,
						allOf(hasEntry(is("B"), contains(-9.6020f, 1.09191f, 1.26171f, 0.10841f)))
				)
		);
	}

	@Test
	void testParseYVT1() throws Exception {
		BaseControlParser parser = new FipControlParser();
		var result = parse(parser, TestUtils.class, "FIPSTART.CTR");
		assertThat(
				result,
				(Matcher) controlMapHasEntry(
						ControlKey.TOTAL_STAND_WHOLE_STEM_VOL,
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
	void testParseYVT2() throws Exception {
		BaseControlParser parser = new FipControlParser();
		var result = parse(parser, TestUtils.class, "FIPSTART.CTR");
		assertThat(
				result,
				(Matcher) controlMapHasEntry(
						ControlKey.UTIL_COMP_WS_VOLUME,
						allOf(mmHasEntry(present(contains(-1.44375f, 1.20115f, 1.14639f, -1.17521f)), 2, 11))
				)
		);
	}

	@Test
	void testParseYVW1() throws Exception {
		BaseControlParser parser = new FipControlParser();
		var result = parse(parser, TestUtils.class, "FIPSTART.CTR");
		assertThat(
				result,
				(Matcher) controlMapHasEntry(
						ControlKey.VOLUME_NET_DECAY_WASTE,
						allOf(hasEntry(is("B"), contains(-4.2025f, 11.2235f, -33.0270f, 0.1246f, -0.2318f, -0.1259f)))
				)
		);
	}

	@Test
	void testParseE095() throws Exception {
		BaseControlParser parser = new FipControlParser();
		var result = parse(parser, TestUtils.class, "FIPSTART.CTR");
		assertThat(
				result,
				(Matcher) controlMapHasEntry(
						ControlKey.BREAKAGE, allOf(hasEntry(is(10), contains(-0.7153f, 2.0108f, 4.00f, 8.00f)))
				)
		);
	}

	@Test
	void testParseYVVET() throws Exception {
		BaseControlParser parser = new FipControlParser();
		var result = parse(parser, TestUtils.class, "FIPSTART.CTR");
		assertThat(
				result,
				(Matcher) controlMapHasEntry(
						ControlKey.VETERAN_LAYER_VOLUME_ADJUST,
						allOf(hasEntry(is("B"), contains(0.10881f, -0.09818f, 0.00048f, -0.00295f)))
				)
		);
	}

	@Test
	void testParseYDQV() throws Exception {
		BaseControlParser parser = new FipControlParser();
		var result = parse(parser, TestUtils.class, "FIPSTART.CTR");
		assertThat(
				result,
				(Matcher) controlMapHasEntry(
						ControlKey.VETERAN_LAYER_DQ,
						allOf(
								mmHasEntry(coe(1, contains(22.500f, 0.24855f, 1.46089f)), "B", Region.COASTAL),
								mmHasEntry(coe(1, contains(19.417f, 0.04354f, 1.96395f)), "B", Region.INTERIOR),
								mmHasEntry(coe(1, contains(22.500f, 0.80260f, 1.00000f)), "D", Region.COASTAL),
								mmHasEntry(coe(1, contains(22.500f, 0.80260f, 1.00000f)), "D", Region.INTERIOR)
						)
				)
		);
	}

	@Test
	void testParseE098() throws Exception {
		BaseControlParser parser = new FipControlParser();
		var result = parse(parser, TestUtils.class, "FIPSTART.CTR");
		assertThat(
				result, (Matcher) controlMapHasEntry(
						ControlKey.VETERAN_BQ,
						// Includes modifiers from 198
						allOf(
								mmHasEntry(contains(0.12874f * 0.311f, 8.00000f, 1.26982f), "B", Region.COASTAL),
								mmHasEntry(contains(0.70932f * 0.374f, 7.63269f, 0.62545f), "B", Region.INTERIOR),
								mmHasEntry(contains(0.07962f * 0.311f, 6.60231f, 1.37998f), "D", Region.COASTAL),
								mmHasEntry(contains(0.07962f * 0.374f, 6.60231f, 1.37998f), "D", Region.INTERIOR)
						)
				)
		);
	}

	@Test
	void testParseMinima() throws Exception {
		BaseControlParser parser = new FipControlParser();
		var result = parse(parser, TestUtils.class, "FIPSTART.CTR");
		assertThat(
				result, (Matcher) controlMapHasEntry(
						ControlKey.MINIMA,
						// Includes modifiers from 198
						allOf(
								hasEntry(is(BaseControlParser.MINIMUM_HEIGHT), is(5.0f)),
								hasEntry(is(BaseControlParser.MINIMUM_BASE_AREA), is(0.0f)),
								hasEntry(is(BaseControlParser.MINIMUM_FULLY_STOCKED_AREA), is(2.0f)),
								hasEntry(is(BaseControlParser.MINIMUM_VETERAN_HEIGHT), is(10.0f))
						)
				)
		);
	}

	@Test
	void testParseV7O_FIP() throws Exception {
		BaseControlParser parser = new FipControlParser();
		var result = parse(parser, TestUtils.class, "FIPSTART.CTR");
		assertThat(
				result,
				(Matcher) controlMapHasEntry(ControlKey.FIP_INPUT_YIELD_POLY, instanceOf(StreamingParserFactory.class))
		);
	}

	@Test
	void testParseV7O_FIL() throws Exception {
		BaseControlParser parser = new FipControlParser();
		var result = parse(parser, TestUtils.class, "FIPSTART.CTR");
		assertThat(
				result,
				(Matcher) controlMapHasEntry(ControlKey.FIP_INPUT_YIELD_LAYER, instanceOf(StreamingParserFactory.class))
		);
	}

	@Test
	void testParseV7O_FIS() throws Exception {
		BaseControlParser parser = new FipControlParser();
		var result = parse(parser, TestUtils.class, "FIPSTART.CTR");
		assertThat(
				result,
				(Matcher) controlMapHasEntry(
						ControlKey.FIP_INPUT_YIELD_LX_SP0, instanceOf(StreamingParserFactory.class)
				)
		);
	}

	static InputStream addToEnd(InputStream is, String... lines) {
		var appendix = new ByteArrayInputStream(String.join("\r\n", lines).getBytes(StandardCharsets.US_ASCII));
		var result = new SequenceInputStream(is, appendix);
		return result;
	}

	static Map<String, ?> parseWithAppendix(BaseControlParser parser, String... lines)
			throws IOException, ResourceParseException {
		var resolver = TestUtils.fileResolver(TestUtils.class);
		try (
				InputStream baseIs = TestUtils.class.getResourceAsStream("FIPSTART.CTR");
				InputStream is = addToEnd(baseIs, lines);
		) {
			return parser.parse(is, resolver, new HashMap<>());
		}
	}

	Map<String, ?> parse(BaseControlParser parser, Class<?> klazz, String resourceName)
			throws IOException, ResourceParseException {
		try (var is = klazz.getResourceAsStream(resourceName)) {

			return parser.parse(is, TestUtils.fileResolver(klazz), new HashMap<>());
		}
	}
}
