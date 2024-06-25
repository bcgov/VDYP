package ca.bc.gov.nrs.vdyp.io.parse.coe;

import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.coe;
import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.controlMapHasEntry;
import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.mmAll;
import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.mmDimensions;
import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.notPresent;
import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.present;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import ca.bc.gov.nrs.vdyp.application.VdypApplicationIdentifier;
import ca.bc.gov.nrs.vdyp.common.ControlKey;
import ca.bc.gov.nrs.vdyp.model.Coefficients;
import ca.bc.gov.nrs.vdyp.model.MatrixMap;
import ca.bc.gov.nrs.vdyp.model.MatrixMap2;
import ca.bc.gov.nrs.vdyp.model.MatrixMap2Impl;
import ca.bc.gov.nrs.vdyp.model.MatrixMap3;
import ca.bc.gov.nrs.vdyp.model.MatrixMap3Impl;
import ca.bc.gov.nrs.vdyp.model.NonprimaryHLCoefficients;
import ca.bc.gov.nrs.vdyp.model.Region;
import ca.bc.gov.nrs.vdyp.test.MockFileResolver;
import ca.bc.gov.nrs.vdyp.test.TestUtils;

@SuppressWarnings({ "unchecked", "rawtypes" })
class ModifierParserTest {

	@Test
	void testNoFilenameForControlFile() throws Exception {
		var parser = new ModifierParser(VdypApplicationIdentifier.FIP_START);

		Map<String, Object> controlMap = new HashMap<>();
		controlMap.put(ControlKey.MODIFIER_FILE.name(), Optional.empty());
		TestUtils.populateControlMapGenusReal(controlMap);

		var fileResolver = new MockFileResolver("TEST");

		parser.modify(controlMap, fileResolver);

		assertThat(controlMap, (Matcher) controlMapHasEntry(ControlKey.MODIFIER_FILE, notPresent()));
	}

	@Test
	void testMissingControlFile() throws Exception {
		var parser = new ModifierParser(VdypApplicationIdentifier.FIP_START);

		Map<String, Object> controlMap = new HashMap<>();
		controlMap.put(ControlKey.MODIFIER_FILE.name(), Optional.of("testFilename"));
		TestUtils.populateControlMapBec(controlMap);

		var fileResolver = new MockFileResolver("TEST");
		fileResolver.addError("testFilename", () -> new IOException());

		var ex = Assertions.assertThrows(IOException.class, () -> parser.modify(controlMap, fileResolver));

		assertThat(ex, Matchers.notNullValue());

	}

	@Test
	void testLoadEmptyFile() throws Exception {
		var parser = new ModifierParser(VdypApplicationIdentifier.FIP_START);

		Map<String, Object> controlMap = new HashMap<>();
		controlMap.put(ControlKey.MODIFIER_FILE.name(), Optional.of("testFilename"));
		TestUtils.populateControlMapGenusReal(controlMap);
		populateVetBq(controlMap);
		populateHlP1(controlMap);
		populateHlP2(controlMap);
		populateHlP3(controlMap);
		populateHlNP(controlMap);

		var is = TestUtils.makeInputStream();

		var fileResolver = new MockFileResolver("TEST");
		fileResolver.addStream("testFilename", is);

		parser.modify(controlMap, fileResolver);

		modifierDefaultAsserts(controlMap);
	}

	protected void modifierDefaultAsserts(Map<String, Object> controlMap) {
		var expectedSp0Aliases = TestUtils.getSpeciesAliases();

		assertThat(controlMap, (Matcher) controlMapHasEntry(ControlKey.MODIFIER_FILE, present(is("testFilename"))));

		assertThat(
				controlMap,
				(Matcher) controlMapHasEntry(
						ControlKey.BA_MODIFIERS,
						mmDimensions(contains((Object[]) expectedSp0Aliases), contains((Object[]) Region.values()))
				)
		);
		assertThat(controlMap, (Matcher) controlMapHasEntry(ControlKey.BA_MODIFIERS, mmAll(is(1.0f))));
		assertThat(
				controlMap,
				(Matcher) controlMapHasEntry(
						ControlKey.DQ_MODIFIERS,
						mmDimensions(contains((Object[]) expectedSp0Aliases), contains((Object[]) Region.values()))
				)
		);
		assertThat(controlMap, (Matcher) controlMapHasEntry(ControlKey.DQ_MODIFIERS, mmAll(is(1.0f))));

		assertThat(
				controlMap,
				(Matcher) controlMapHasEntry(
						ModifierParser.CONTROL_KEY_MOD301_DECAY,
						mmDimensions(contains((Object[]) expectedSp0Aliases), contains((Object[]) Region.values()))
				)
		);
		assertThat(controlMap, (Matcher) controlMapHasEntry(ModifierParser.CONTROL_KEY_MOD301_DECAY, mmAll(is(0.0f))));
		assertThat(
				controlMap,
				(Matcher) controlMapHasEntry(
						ModifierParser.CONTROL_KEY_MOD301_WASTE,
						mmDimensions(contains((Object[]) expectedSp0Aliases), contains((Object[]) Region.values()))
				)
		);
		assertThat(controlMap, (Matcher) controlMapHasEntry(ModifierParser.CONTROL_KEY_MOD301_WASTE, mmAll(is(0.0f))));
	}

	@Test
	void testBaDqSpecies() throws Exception {
		var parser = new ModifierParser(VdypApplicationIdentifier.FIP_START);

		Map<String, Object> controlMap = new HashMap<>();
		controlMap.put(ControlKey.MODIFIER_FILE.name(), Optional.of("testFilename"));
		TestUtils.populateControlMapGenusReal(controlMap);
		populateVetBq(controlMap);
		populateHlP1(controlMap);
		populateHlP2(controlMap);
		populateHlP3(controlMap);
		populateHlNP(controlMap);

		var is = TestUtils.makeInputStream("201 1 0 0 0 0 0 2.000 3.000 4.000 5.000");

		var fileResolver = new MockFileResolver("TEST");
		fileResolver.addStream("testFilename", is);

		parser.modify(controlMap, fileResolver);

		var baMap = ((MatrixMap<Float>) controlMap.get(ControlKey.BA_MODIFIERS.name()));
		baMap.eachKey(k -> {
			if (k[0].equals("AC")) {
				if (k[1].equals(Region.COASTAL)) {
					assertThat(baMap.getM(k), is(2.0f));
				} else {
					assertThat(baMap.getM(k), is(3.0f));
				}
			} else {
				assertThat(baMap.getM(k), is(1.0f));
			}
		});
		var dqMap = ((MatrixMap<Float>) controlMap.get(ControlKey.DQ_MODIFIERS.name()));
		dqMap.eachKey(k -> {
			if (k[0].equals("AC")) {
				if (k[1].equals(Region.COASTAL)) {
					assertThat(dqMap.getM(k), is(4.0f));
				} else {
					assertThat(dqMap.getM(k), is(5.0f));
				}
			} else {
				assertThat(dqMap.getM(k), is(1.0f));
			}
		});
	}

	@Test
	void testBaDqSpeciesDifferentProgram() throws Exception {
		var parser = new ModifierParser(VdypApplicationIdentifier.VRI_START);

		Map<String, Object> controlMap = new HashMap<>();
		controlMap.put(ControlKey.MODIFIER_FILE.name(), Optional.of("testFilename"));
		TestUtils.populateControlMapGenusReal(controlMap);
		populateVetBq(controlMap);
		populateHlP1(controlMap);
		populateHlP2(controlMap);
		populateHlP3(controlMap);
		populateHlNP(controlMap);

		var is = TestUtils.makeInputStream("201 1 0 0 0 0 0 0.000 0.000 0.000 0.000");

		var fileResolver = new MockFileResolver("TEST");
		fileResolver.addStream("testFilename", is);

		parser.modify(controlMap, fileResolver);

		modifierDefaultAsserts(controlMap);
	}

	@Test
	void testIgnoreAfterStop() throws Exception {
		var parser = new ModifierParser(VdypApplicationIdentifier.FIP_START);

		Map<String, Object> controlMap = new HashMap<>();
		controlMap.put(ControlKey.MODIFIER_FILE.name(), Optional.of("testFilename"));
		TestUtils.populateControlMapGenusReal(controlMap);
		populateVetBq(controlMap);
		populateHlP1(controlMap);
		populateHlP2(controlMap);
		populateHlP3(controlMap);
		populateHlNP(controlMap);

		var is = TestUtils.makeInputStream("999", "201 1 0 0 0 0 0 0.000 0.000 0.000 0.000");

		var fileResolver = new MockFileResolver("TEST");
		fileResolver.addStream("testFilename", is);

		parser.modify(controlMap, fileResolver);

		modifierDefaultAsserts(controlMap);
	}

	@ParameterizedTest
	@ValueSource(strings = { "001", "097", "099", "199", "500" })
	void testIgnoreUnknownSequence(String sequence) throws Exception {
		var parser = new ModifierParser(VdypApplicationIdentifier.FIP_START);

		Map<String, Object> controlMap = new HashMap<>();
		controlMap.put(ControlKey.MODIFIER_FILE.name(), Optional.of("testFilename"));
		TestUtils.populateControlMapGenusReal(controlMap);
		populateVetBq(controlMap);
		populateHlP1(controlMap);
		populateHlP2(controlMap);
		populateHlP3(controlMap);
		populateHlNP(controlMap);

		var is = TestUtils.makeInputStream(sequence + " 1 0 0 0 0 0 0.000 0.000 0.000 0.000");

		var fileResolver = new MockFileResolver("TEST");
		fileResolver.addStream("testFilename", is);

		parser.modify(controlMap, fileResolver);

		modifierDefaultAsserts(controlMap);

	}

	@Test
	void testIgnoreCommentsAndBlanks() throws Exception {
		var parser = new ModifierParser(VdypApplicationIdentifier.FIP_START);

		Map<String, Object> controlMap = new HashMap<>();
		controlMap.put(ControlKey.MODIFIER_FILE.name(), Optional.of("testFilename"));
		TestUtils.populateControlMapGenusReal(controlMap);
		populateVetBq(controlMap);
		populateHlP1(controlMap);
		populateHlP2(controlMap);
		populateHlP3(controlMap);
		populateHlNP(controlMap);

		var is = TestUtils.makeInputStream("", "    x", "000 x", "201 1 0 0 0 0 0 2.000 3.000 4.000 5.000");

		var fileResolver = new MockFileResolver("TEST");
		fileResolver.addStream("testFilename", is);

		parser.modify(controlMap, fileResolver);

		var baMap = ((MatrixMap<Float>) controlMap.get(ControlKey.BA_MODIFIERS.name()));
		baMap.eachKey(k -> {
			if (k[0].equals("AC")) {
				if (k[1].equals(Region.COASTAL)) {
					assertThat(baMap.getM(k), is(2.0f));
				} else {
					assertThat(baMap.getM(k), is(3.0f));
				}
			} else {
				assertThat(baMap.getM(k), is(1.0f));
			}
		});
		var dqMap = ((MatrixMap<Float>) controlMap.get(ControlKey.DQ_MODIFIERS.name()));
		dqMap.eachKey(k -> {
			if (k[0].equals("AC")) {
				if (k[1].equals(Region.COASTAL)) {
					assertThat(dqMap.getM(k), is(4.0f));
				} else {
					assertThat(dqMap.getM(k), is(5.0f));
				}
			} else {
				assertThat(dqMap.getM(k), is(1.0f));
			}
		});
	}

	@Test
	void testBaDqAllSpecies() throws Exception {
		var parser = new ModifierParser(VdypApplicationIdentifier.FIP_START);

		Map<String, Object> controlMap = new HashMap<>();
		controlMap.put(ControlKey.MODIFIER_FILE.name(), Optional.of("testFilename"));
		TestUtils.populateControlMapGenusReal(controlMap);
		populateVetBq(controlMap);
		populateHlP1(controlMap);
		populateHlP2(controlMap);
		populateHlP3(controlMap);
		populateHlNP(controlMap);

		var is = TestUtils.makeInputStream("200 1 0 0 0 0 0 2.000 3.000 4.000 5.000");

		var fileResolver = new MockFileResolver("TEST");
		fileResolver.addStream("testFilename", is);

		parser.modify(controlMap, fileResolver);

		var baMap = ((MatrixMap<Float>) controlMap.get(ControlKey.BA_MODIFIERS.name()));
		baMap.eachKey(k -> {
			if (k[1].equals(Region.COASTAL)) {
				assertThat(baMap.getM(k), is(2.0f));
			} else {
				assertThat(baMap.getM(k), is(3.0f));
			}
		});
		var dqMap = ((MatrixMap<Float>) controlMap.get(ControlKey.DQ_MODIFIERS.name()));
		dqMap.eachKey(k -> {
			if (k[1].equals(Region.COASTAL)) {
				assertThat(dqMap.getM(k), is(4.0f));
			} else {
				assertThat(dqMap.getM(k), is(5.0f));
			}
		});
	}

	@Test
	void testVetBq() throws Exception {
		var parser = new ModifierParser(VdypApplicationIdentifier.FIP_START);

		Map<String, Object> controlMap = new HashMap<>();
		controlMap.put(ControlKey.MODIFIER_FILE.name(), Optional.of("testFilename"));
		TestUtils.populateControlMapGenusReal(controlMap);
		MatrixMap2<String, Region, Coefficients> vetBqMap = populateVetBq(controlMap);
		populateHlP1(controlMap);
		populateHlP2(controlMap);
		populateHlP3(controlMap);
		populateHlNP(controlMap);

		var is = TestUtils.makeInputStream("098 1 0 0 0 0 0 0.200 0.300");

		var fileResolver = new MockFileResolver("TEST");
		fileResolver.addStream("testFilename", is);

		parser.modify(controlMap, fileResolver);

		vetBqMap.eachKey(k -> {
			if (k[1].equals(Region.COASTAL)) {
				assertThat(vetBqMap.getM(k), coe(1, contains(is(0.2f), is(5.0f), is(7.0f))));
			} else {
				assertThat(vetBqMap.getM(k), coe(1, contains(is(0.3f), is(5.0f), is(7.0f))));
			}
		});
	}

	private MatrixMap2<String, Region, Coefficients> populateVetBq(Map<String, Object> controlMap) {
		MatrixMap2<String, Region, Coefficients> vetBqMap = new MatrixMap2Impl(
				Arrays.asList(TestUtils.getSpeciesAliases()), Arrays.asList(Region.values()),
				(k1, k2) -> new Coefficients(Arrays.asList(1.0f, 5.0f, 7.0f), 1)
		);
		controlMap.put(ControlKey.VETERAN_BQ.name(), vetBqMap);
		return vetBqMap;
	}

	@Test
	void testDecayWaste() throws Exception {
		var parser = new ModifierParser(VdypApplicationIdentifier.FIP_START);

		Map<String, Object> controlMap = new HashMap<>();
		controlMap.put(ControlKey.MODIFIER_FILE.name(), Optional.of("testFilename"));
		TestUtils.populateControlMapGenusReal(controlMap);
		populateVetBq(controlMap);
		populateHlP1(controlMap);
		populateHlP2(controlMap);
		populateHlP3(controlMap);
		populateHlNP(controlMap);

		var is = TestUtils.makeInputStream("301 1 0 0 0 0 0 2.000 3.000 4.000 5.000");

		var fileResolver = new MockFileResolver("TEST");
		fileResolver.addStream("testFilename", is);

		parser.modify(controlMap, fileResolver);

		var decayMap = ((MatrixMap<Float>) controlMap.get(ModifierParser.CONTROL_KEY_MOD301_DECAY.name()));
		decayMap.eachKey(k -> {
			if (k[0].equals("AC")) {
				if (k[1].equals(Region.COASTAL)) {
					assertThat(decayMap.getM(k), is(2.0f));
				} else {
					assertThat(decayMap.getM(k), is(3.0f));
				}
			} else {
				assertThat(decayMap.getM(k), is(0.0f));
			}
		});
		var wasteMap = ((MatrixMap<Float>) controlMap.get(ModifierParser.CONTROL_KEY_MOD301_WASTE.name()));
		wasteMap.eachKey(k -> {
			if (k[0].equals("AC")) {
				if (k[1].equals(Region.COASTAL)) {
					assertThat(wasteMap.getM(k), is(4.0f));
				} else {
					assertThat(wasteMap.getM(k), is(5.0f));
				}
			} else {
				assertThat(wasteMap.getM(k), is(0.0f));
			}
		});
	}

	@Test
	void testHL() throws Exception {
		var parser = new ModifierParser(VdypApplicationIdentifier.FIP_START);

		Map<String, Object> controlMap = new HashMap<>();
		controlMap.put(ControlKey.MODIFIER_FILE.name(), Optional.of("testFilename"));

		TestUtils.populateControlMapGenusReal(controlMap);
		populateVetBq(controlMap);
		var hlP1Map = populateHlP1(controlMap);
		var hlP2Map = populateHlP2(controlMap);
		var hlP3Map = populateHlP3(controlMap);
		var hlNPMap = populateHlNP(controlMap);

		var is = TestUtils.makeInputStream("401 1 0 0 0 0 0 0.200 0.300 0.500 0.700");

		var fileResolver = new MockFileResolver("TEST");
		fileResolver.addStream("testFilename", is);

		parser.modify(controlMap, fileResolver);

		hlP1Map.eachKey(k -> {
			if (k[0].equals("AC")) {
				if (k[1].equals(Region.COASTAL)) {
					assertThat(hlP1Map.getM(k), coe(1, contains(is(0.2f), is(0.2f * 5.0f), is(7.0f))));
				} else {
					assertThat(hlP1Map.getM(k), coe(1, contains(is(0.3f), is(0.3f * 5.0f), is(7.0f))));
				}
			} else {
				assertThat(hlP1Map.getM(k), coe(1, contains(is(1.0f), is(5.0f), is(7.0f))));
			}
		});
		hlP2Map.eachKey(k -> {
			if (k[0].equals("AC")) {
				if (k[1].equals(Region.COASTAL)) {
					assertThat(hlP2Map.getM(k), coe(1, contains(is(0.2f), is(5.0f))));
				} else {
					assertThat(hlP2Map.getM(k), coe(1, contains(is(0.3f), is(5.0f))));
				}
			} else {
				assertThat(hlP2Map.getM(k), coe(1, contains(is(1.0f), is(5.0f))));
			}
		});
		hlP3Map.eachKey(k -> {
			if (k[0].equals("AC")) {
				if (k[1].equals(Region.COASTAL)) {
					assertThat(hlP3Map.getM(k), coe(1, contains(is(0.2f), is(5.0f), is(7.0f), is(13.0f))));
				} else {
					assertThat(hlP3Map.getM(k), coe(1, contains(is(0.3f), is(5.0f), is(7.0f), is(13.0f))));
				}
			} else {
				assertThat(hlP3Map.getM(k), coe(1, contains(is(1.0f), is(5.0f), is(7.0f), is(13.0f))));
			}
		});
		hlNPMap.eachKey(k -> {
			if (k[0].equals("AC")) {
				if (k[2].equals(Region.COASTAL)) {
					assertThat(hlNPMap.getM(k), coe(1, contains(is(0.5f), is(5.0f))));
				} else {
					assertThat(hlNPMap.getM(k), coe(1, contains(is(0.7f), is(5.0f))));
				}
			} else {
				assertThat(hlNPMap.getM(k), coe(1, contains(is(1.0f), is(5.0f))));
			}
		});
	}

	private MatrixMap3<String, String, Region, NonprimaryHLCoefficients> populateHlNP(Map<String, Object> controlMap) {
		MatrixMap3<String, String, Region, NonprimaryHLCoefficients> hlNPMap = new MatrixMap3Impl(
				Arrays.asList(TestUtils.getSpeciesAliases()), //
				Arrays.asList(TestUtils.getSpeciesAliases()), //
				Arrays.asList(Region.values()), //
				MatrixMap3Impl.emptyDefault()
		);
		hlNPMap.setAll(k -> new NonprimaryHLCoefficients(Arrays.asList(1.0f, 5.0f), 1));
		controlMap.put(ControlKey.HL_NONPRIMARY.name(), hlNPMap);
		return hlNPMap;
	}

	private MatrixMap2<String, Region, Coefficients> populateHlP3(Map<String, Object> controlMap) {
		MatrixMap2<String, Region, Coefficients> hlP3Map = new MatrixMap2Impl(
				Arrays.asList(TestUtils.getSpeciesAliases()), //
				Arrays.asList(Region.values()), //
				MatrixMap2Impl.emptyDefault()
		);
		hlP3Map.setAll(k -> new Coefficients(Arrays.asList(1.0f, 5.0f, 7.0f, 13.0f), 1));
		controlMap.put(ControlKey.HL_PRIMARY_SP_EQN_P3.name(), hlP3Map);
		return hlP3Map;
	}

	private MatrixMap2<String, Region, Coefficients> populateHlP2(Map<String, Object> controlMap) {
		MatrixMap2<String, Region, Coefficients> hlP2Map = new MatrixMap2Impl(
				Arrays.asList(TestUtils.getSpeciesAliases()), //
				Arrays.asList(Region.values()), //
				MatrixMap2Impl.emptyDefault()
		);
		hlP2Map.setAll(k -> new Coefficients(Arrays.asList(1.0f, 5.0f), 1));
		controlMap.put(ControlKey.HL_PRIMARY_SP_EQN_P2.name(), hlP2Map);
		return hlP2Map;
	}

	private MatrixMap2<String, Region, Coefficients> populateHlP1(Map<String, Object> controlMap) {
		MatrixMap2<String, Region, Coefficients> hlP1Map = new MatrixMap2Impl(
				Arrays.asList(TestUtils.getSpeciesAliases()), //
				Arrays.asList(Region.values()), //
				MatrixMap2Impl.emptyDefault()
		);
		hlP1Map.setAll(k -> new Coefficients(Arrays.asList(1.0f, 5.0f, 7.0f), 1));
		controlMap.put(ControlKey.HL_PRIMARY_SP_EQN_P1.name(), hlP1Map);
		return hlP1Map;
	}

}
