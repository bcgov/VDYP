package ca.bc.gov.nrs.vdyp.fip;

import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.hasSpecificEntry;
import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.mmAll;
import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.mmDimensions;
import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.notPresent;
import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.present;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import ca.bc.gov.nrs.vdyp.io.FileResolver;
import ca.bc.gov.nrs.vdyp.io.parse.HLCoefficientParser;
import ca.bc.gov.nrs.vdyp.io.parse.HLNonprimaryCoefficientParser;
import ca.bc.gov.nrs.vdyp.io.parse.GenusDefinitionParserTest;
import ca.bc.gov.nrs.vdyp.io.parse.VeteranBQParser;
import ca.bc.gov.nrs.vdyp.model.Coefficients;
import ca.bc.gov.nrs.vdyp.model.MatrixMap;
import ca.bc.gov.nrs.vdyp.model.MatrixMap2;
import ca.bc.gov.nrs.vdyp.model.MatrixMap2Impl;
import ca.bc.gov.nrs.vdyp.model.MatrixMap3;
import ca.bc.gov.nrs.vdyp.model.MatrixMap3Impl;
import ca.bc.gov.nrs.vdyp.model.NonprimaryHLCoefficients;
import ca.bc.gov.nrs.vdyp.model.Region;
import ca.bc.gov.nrs.vdyp.test.TestUtils;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class ModifierParserTest {

	@Test
	public void testNoFilenameForControlFile() throws Exception {
		var parser = new ModifierParser(1);

		Map<String, Object> controlMap = new HashMap<>();
		controlMap.put(ModifierParser.CONTROL_KEY, Optional.empty());
		GenusDefinitionParserTest.populateControlMapReal(controlMap);

		var fileResolver = new FileResolver() {

			@Override
			public InputStream resolve(String filename) throws IOException {
				fail("Should not call FileResolver::resolve");
				return null;
			}

			@Override
			public String toString(String filename) throws IOException {
				fail("Should not call FileResolver::toString");
				return filename;
			}

		};

		parser.modify(controlMap, fileResolver);

		assertThat(controlMap, (Matcher) hasSpecificEntry(ModifierParser.CONTROL_KEY, notPresent()));
	}

	@Test
	public void testMissingControlFile() throws Exception {
		var parser = new ModifierParser(1);

		Map<String, Object> controlMap = new HashMap<>();
		controlMap.put(ModifierParser.CONTROL_KEY, Optional.of("testFilename"));
		GenusDefinitionParserTest.populateControlMap(controlMap);

		var fileResolver = new FileResolver() {

			@Override
			public InputStream resolve(String filename) throws IOException {
				assertThat(filename, is("testFilename"));

				throw new IOException();
			}

			@Override
			public String toString(String filename) throws IOException {
				fail("Should not call FileResolver::toString");
				return filename;
			}

		};

		var ex = Assertions.assertThrows(IOException.class, () -> parser.modify(controlMap, fileResolver));

		assertThat(ex, Matchers.notNullValue());

	}

	@Test
	public void testLoadEmptyFile() throws Exception {
		var parser = new ModifierParser(1);

		Map<String, Object> controlMap = new HashMap<>();
		controlMap.put(ModifierParser.CONTROL_KEY, Optional.of("testFilename"));
		GenusDefinitionParserTest.populateControlMapReal(controlMap);
		populateVetBq(controlMap);
		populateHlP1(controlMap);
		populateHlP2(controlMap);
		populateHlP3(controlMap);
		populateHlNP(controlMap);

		var is = TestUtils.makeStream();

		var fileResolver = new FileResolver() {

			@Override
			public InputStream resolve(String filename) throws IOException {
				assertThat(filename, is("testFilename"));

				return is;
			}

			@Override
			public String toString(String filename) throws IOException {
				return filename;
			}

		};

		parser.modify(controlMap, fileResolver);

		modifierDefaultAsserts(controlMap);
	}

	protected void modifierDefaultAsserts(Map<String, Object> controlMap) {
		var expectedSp0Aliases = GenusDefinitionParserTest.getSpeciesAliases();

		assertThat(controlMap, (Matcher) hasSpecificEntry(ModifierParser.CONTROL_KEY, present(is("testFilename"))));

		assertThat(
				controlMap,
				(Matcher) hasSpecificEntry(
						ModifierParser.CONTROL_KEY_MOD200_BA,
						mmDimensions(contains((Object[]) expectedSp0Aliases), contains((Object[]) Region.values()))
				)
		);
		assertThat(controlMap, (Matcher) hasSpecificEntry(ModifierParser.CONTROL_KEY_MOD200_BA, mmAll(is(1.0f))));
		assertThat(
				controlMap,
				(Matcher) hasSpecificEntry(
						ModifierParser.CONTROL_KEY_MOD200_DQ,
						mmDimensions(contains((Object[]) expectedSp0Aliases), contains((Object[]) Region.values()))
				)
		);
		assertThat(controlMap, (Matcher) hasSpecificEntry(ModifierParser.CONTROL_KEY_MOD200_DQ, mmAll(is(1.0f))));

		assertThat(
				controlMap,
				(Matcher) hasSpecificEntry(
						ModifierParser.CONTROL_KEY_MOD301_DECAY,
						mmDimensions(contains((Object[]) expectedSp0Aliases), contains((Object[]) Region.values()))
				)
		);
		assertThat(controlMap, (Matcher) hasSpecificEntry(ModifierParser.CONTROL_KEY_MOD301_DECAY, mmAll(is(0.0f))));
		assertThat(
				controlMap,
				(Matcher) hasSpecificEntry(
						ModifierParser.CONTROL_KEY_MOD301_WASTE,
						mmDimensions(contains((Object[]) expectedSp0Aliases), contains((Object[]) Region.values()))
				)
		);
		assertThat(controlMap, (Matcher) hasSpecificEntry(ModifierParser.CONTROL_KEY_MOD301_WASTE, mmAll(is(0.0f))));
	}

	@Test
	public void testBaDqSpecies() throws Exception {
		var parser = new ModifierParser(1);

		Map<String, Object> controlMap = new HashMap<>();
		controlMap.put(ModifierParser.CONTROL_KEY, Optional.of("testFilename"));
		GenusDefinitionParserTest.populateControlMapReal(controlMap);
		populateVetBq(controlMap);
		populateHlP1(controlMap);
		populateHlP2(controlMap);
		populateHlP3(controlMap);
		populateHlNP(controlMap);

		var is = TestUtils.makeStream("201 1 0 0 0 0 0 2.000 3.000 4.000 5.000");

		var fileResolver = new FileResolver() {

			@Override
			public InputStream resolve(String filename) throws IOException {
				assertThat(filename, is("testFilename"));

				return is;
			}

			@Override
			public String toString(String filename) throws IOException {
				return filename;
			}

		};

		parser.modify(controlMap, fileResolver);

		var baMap = ((MatrixMap<Float>) controlMap.get(ModifierParser.CONTROL_KEY_MOD200_BA));
		baMap.eachKey(k -> {
			if (k[0].equals("AC")) {
				if (k[1].equals(Region.COASTAL)) {
					assertThat(baMap.getM(k), present(is(2.0f)));
				} else {
					assertThat(baMap.getM(k), present(is(3.0f)));
				}
			} else {
				assertThat(baMap.getM(k), present(is(1.0f)));
			}
		});
		var dqMap = ((MatrixMap<Float>) controlMap.get(ModifierParser.CONTROL_KEY_MOD200_DQ));
		dqMap.eachKey(k -> {
			if (k[0].equals("AC")) {
				if (k[1].equals(Region.COASTAL)) {
					assertThat(dqMap.getM(k), present(is(4.0f)));
				} else {
					assertThat(dqMap.getM(k), present(is(5.0f)));
				}
			} else {
				assertThat(dqMap.getM(k), present(is(1.0f)));
			}
		});
	}

	@Test
	public void testBaDqSpeciesDifferentProgram() throws Exception {
		var parser = new ModifierParser(3);

		Map<String, Object> controlMap = new HashMap<>();
		controlMap.put(ModifierParser.CONTROL_KEY, Optional.of("testFilename"));
		GenusDefinitionParserTest.populateControlMapReal(controlMap);
		populateVetBq(controlMap);
		populateHlP1(controlMap);
		populateHlP2(controlMap);
		populateHlP3(controlMap);
		populateHlNP(controlMap);

		var is = TestUtils.makeStream("201 1 0 0 0 0 0 0.000 0.000 0.000 0.000");

		var fileResolver = new FileResolver() {

			@Override
			public InputStream resolve(String filename) throws IOException {
				assertThat(filename, is("testFilename"));

				return is;
			}

			@Override
			public String toString(String filename) throws IOException {
				return filename;
			}

		};

		parser.modify(controlMap, fileResolver);

		modifierDefaultAsserts(controlMap);
	}

	@Test
	public void testIgnoreAfterStop() throws Exception {
		var parser = new ModifierParser(1);

		Map<String, Object> controlMap = new HashMap<>();
		controlMap.put(ModifierParser.CONTROL_KEY, Optional.of("testFilename"));
		GenusDefinitionParserTest.populateControlMapReal(controlMap);
		populateVetBq(controlMap);
		populateHlP1(controlMap);
		populateHlP2(controlMap);
		populateHlP3(controlMap);
		populateHlNP(controlMap);

		var is = TestUtils.makeStream("999", "201 1 0 0 0 0 0 0.000 0.000 0.000 0.000");

		var fileResolver = new FileResolver() {

			@Override
			public InputStream resolve(String filename) throws IOException {
				assertThat(filename, is("testFilename"));

				return is;
			}

			@Override
			public String toString(String filename) throws IOException {
				return filename;
			}

		};

		parser.modify(controlMap, fileResolver);

		modifierDefaultAsserts(controlMap);
	}

	@Test
	public void testIgnoreCommentsAndBlanks() throws Exception {
		var parser = new ModifierParser(1);

		Map<String, Object> controlMap = new HashMap<>();
		controlMap.put(ModifierParser.CONTROL_KEY, Optional.of("testFilename"));
		GenusDefinitionParserTest.populateControlMapReal(controlMap);
		populateVetBq(controlMap);
		populateHlP1(controlMap);
		populateHlP2(controlMap);
		populateHlP3(controlMap);
		populateHlNP(controlMap);

		var is = TestUtils.makeStream("", "    x", "000 x", "201 1 0 0 0 0 0 2.000 3.000 4.000 5.000");

		var fileResolver = new FileResolver() {

			@Override
			public InputStream resolve(String filename) throws IOException {
				assertThat(filename, is("testFilename"));

				return is;
			}

			@Override
			public String toString(String filename) throws IOException {
				return filename;
			}

		};

		parser.modify(controlMap, fileResolver);

		var baMap = ((MatrixMap<Float>) controlMap.get(ModifierParser.CONTROL_KEY_MOD200_BA));
		baMap.eachKey(k -> {
			if (k[0].equals("AC")) {
				if (k[1].equals(Region.COASTAL)) {
					assertThat(baMap.getM(k), present(is(2.0f)));
				} else {
					assertThat(baMap.getM(k), present(is(3.0f)));
				}
			} else {
				assertThat(baMap.getM(k), present(is(1.0f)));
			}
		});
		var dqMap = ((MatrixMap<Float>) controlMap.get(ModifierParser.CONTROL_KEY_MOD200_DQ));
		dqMap.eachKey(k -> {
			if (k[0].equals("AC")) {
				if (k[1].equals(Region.COASTAL)) {
					assertThat(dqMap.getM(k), present(is(4.0f)));
				} else {
					assertThat(dqMap.getM(k), present(is(5.0f)));
				}
			} else {
				assertThat(dqMap.getM(k), present(is(1.0f)));
			}
		});
	}

	@Test
	public void testBaDqAllSpecies() throws Exception {
		var parser = new ModifierParser(1);

		Map<String, Object> controlMap = new HashMap<>();
		controlMap.put(ModifierParser.CONTROL_KEY, Optional.of("testFilename"));
		GenusDefinitionParserTest.populateControlMapReal(controlMap);
		populateVetBq(controlMap);
		populateHlP1(controlMap);
		populateHlP2(controlMap);
		populateHlP3(controlMap);
		populateHlNP(controlMap);

		var is = TestUtils.makeStream("200 1 0 0 0 0 0 2.000 3.000 4.000 5.000");

		var fileResolver = new FileResolver() {

			@Override
			public InputStream resolve(String filename) throws IOException {
				assertThat(filename, is("testFilename"));

				return is;
			}

			@Override
			public String toString(String filename) throws IOException {
				return filename;
			}

		};

		parser.modify(controlMap, fileResolver);

		var baMap = ((MatrixMap<Float>) controlMap.get(ModifierParser.CONTROL_KEY_MOD200_BA));
		baMap.eachKey(k -> {
			if (k[1].equals(Region.COASTAL)) {
				assertThat(baMap.getM(k), present(is(2.0f)));
			} else {
				assertThat(baMap.getM(k), present(is(3.0f)));
			}
		});
		var dqMap = ((MatrixMap<Float>) controlMap.get(ModifierParser.CONTROL_KEY_MOD200_DQ));
		dqMap.eachKey(k -> {
			if (k[1].equals(Region.COASTAL)) {
				assertThat(dqMap.getM(k), present(is(4.0f)));
			} else {
				assertThat(dqMap.getM(k), present(is(5.0f)));
			}
		});
	}

	@Test
	public void testVetBq() throws Exception {
		var parser = new ModifierParser(1);

		Map<String, Object> controlMap = new HashMap<>();
		controlMap.put(ModifierParser.CONTROL_KEY, Optional.of("testFilename"));
		GenusDefinitionParserTest.populateControlMapReal(controlMap);
		MatrixMap2<String, Region, Coefficients> vetBqMap = populateVetBq(controlMap);
		populateHlP1(controlMap);
		populateHlP2(controlMap);
		populateHlP3(controlMap);
		populateHlNP(controlMap);

		var is = TestUtils.makeStream("098 1 0 0 0 0 0 0.200 0.300");

		var fileResolver = new FileResolver() {

			@Override
			public InputStream resolve(String filename) throws IOException {
				assertThat(filename, is("testFilename"));

				return is;
			}

			@Override
			public String toString(String filename) throws IOException {
				return filename;
			}

		};

		parser.modify(controlMap, fileResolver);

		vetBqMap.eachKey(k -> {
			if (k[1].equals(Region.COASTAL)) {
				assertThat(vetBqMap.getM(k), present(contains(is(0.2f), is(5.0f), is(7.0f))));
			} else {
				assertThat(vetBqMap.getM(k), present(contains(is(0.3f), is(5.0f), is(7.0f))));
			}
		});
	}

	private MatrixMap2<String, Region, Coefficients> populateVetBq(Map<String, Object> controlMap) {
		MatrixMap2<String, Region, Coefficients> vetBqMap = new MatrixMap2Impl(
				Arrays.asList(GenusDefinitionParserTest.getSpeciesAliases()), Arrays.asList(Region.values())
		);
		vetBqMap.setAll(k -> new Coefficients(Arrays.asList(1.0f, 5.0f, 7.0f), 1));
		controlMap.put(VeteranBQParser.CONTROL_KEY, vetBqMap);
		return vetBqMap;
	}

	@Test
	public void testDecayWaste() throws Exception {
		var parser = new ModifierParser(1);

		Map<String, Object> controlMap = new HashMap<>();
		controlMap.put(ModifierParser.CONTROL_KEY, Optional.of("testFilename"));
		GenusDefinitionParserTest.populateControlMapReal(controlMap);
		populateVetBq(controlMap);
		populateHlP1(controlMap);
		populateHlP2(controlMap);
		populateHlP3(controlMap);
		populateHlNP(controlMap);

		var is = TestUtils.makeStream("301 1 0 0 0 0 0 2.000 3.000 4.000 5.000");

		var fileResolver = new FileResolver() {

			@Override
			public InputStream resolve(String filename) throws IOException {
				assertThat(filename, is("testFilename"));

				return is;
			}

			@Override
			public String toString(String filename) throws IOException {
				return filename;
			}

		};

		parser.modify(controlMap, fileResolver);

		var decayMap = ((MatrixMap<Float>) controlMap.get(ModifierParser.CONTROL_KEY_MOD301_DECAY));
		decayMap.eachKey(k -> {
			if (k[0].equals("AC")) {
				if (k[1].equals(Region.COASTAL)) {
					assertThat(decayMap.getM(k), present(is(2.0f)));
				} else {
					assertThat(decayMap.getM(k), present(is(3.0f)));
				}
			} else {
				assertThat(decayMap.getM(k), present(is(0.0f)));
			}
		});
		var wasteMap = ((MatrixMap<Float>) controlMap.get(ModifierParser.CONTROL_KEY_MOD301_WASTE));
		wasteMap.eachKey(k -> {
			if (k[0].equals("AC")) {
				if (k[1].equals(Region.COASTAL)) {
					assertThat(wasteMap.getM(k), present(is(4.0f)));
				} else {
					assertThat(wasteMap.getM(k), present(is(5.0f)));
				}
			} else {
				assertThat(wasteMap.getM(k), present(is(0.0f)));
			}
		});
	}

	@Test
	public void testHL() throws Exception {
		var parser = new ModifierParser(1);

		Map<String, Object> controlMap = new HashMap<>();
		controlMap.put(ModifierParser.CONTROL_KEY, Optional.of("testFilename"));

		GenusDefinitionParserTest.populateControlMapReal(controlMap);
		populateVetBq(controlMap);
		var hlP1Map = populateHlP1(controlMap);
		var hlP2Map = populateHlP2(controlMap);
		var hlP3Map = populateHlP3(controlMap);
		var hlNPMap = populateHlNP(controlMap);

		var is = TestUtils.makeStream("401 1 0 0 0 0 0 0.200 0.300 0.500 0.700");

		var fileResolver = new FileResolver() {

			@Override
			public InputStream resolve(String filename) throws IOException {
				assertThat(filename, is("testFilename"));

				return is;
			}

			@Override
			public String toString(String filename) throws IOException {
				return filename;
			}

		};

		parser.modify(controlMap, fileResolver);

		hlP1Map.eachKey(k -> {
			if (k[0].equals("AC")) {
				if (k[1].equals(Region.COASTAL)) {
					assertThat(hlP1Map.getM(k), present(contains(is(0.2f), is(0.2f * 5.0f), is(7.0f))));
				} else {
					assertThat(hlP1Map.getM(k), present(contains(is(0.3f), is(0.3f * 5.0f), is(7.0f))));
				}
			} else {
				assertThat(hlP1Map.getM(k), present(contains(is(1.0f), is(5.0f), is(7.0f))));
			}
		});
		hlP2Map.eachKey(k -> {
			if (k[0].equals("AC")) {
				if (k[1].equals(Region.COASTAL)) {
					assertThat(hlP2Map.getM(k), present(contains(is(0.2f), is(5.0f))));
				} else {
					assertThat(hlP2Map.getM(k), present(contains(is(0.3f), is(5.0f))));
				}
			} else {
				assertThat(hlP2Map.getM(k), present(contains(is(1.0f), is(5.0f))));
			}
		});
		hlP3Map.eachKey(k -> {
			if (k[0].equals("AC")) {
				if (k[1].equals(Region.COASTAL)) {
					assertThat(hlP3Map.getM(k), present(contains(is(0.2f), is(5.0f), is(7.0f), is(13.0f))));
				} else {
					assertThat(hlP3Map.getM(k), present(contains(is(0.3f), is(5.0f), is(7.0f), is(13.0f))));
				}
			} else {
				assertThat(hlP3Map.getM(k), present(contains(is(1.0f), is(5.0f), is(7.0f), is(13.0f))));
			}
		});
		hlNPMap.eachKey(k -> {
			if (k[0].equals("AC")) {
				if (k[2].equals(Region.COASTAL)) {
					assertThat(hlNPMap.getM(k), present(contains(is(0.5f), is(5.0f))));
				} else {
					assertThat(hlNPMap.getM(k), present(contains(is(0.7f), is(5.0f))));
				}
			} else {
				assertThat(hlNPMap.getM(k), present(contains(is(1.0f), is(5.0f))));
			}
		});
	}

	private MatrixMap3<String, String, Region, NonprimaryHLCoefficients> populateHlNP(Map<String, Object> controlMap) {
		MatrixMap3<String, String, Region, NonprimaryHLCoefficients> hlNPMap = new MatrixMap3Impl(
				Arrays.asList(GenusDefinitionParserTest.getSpeciesAliases()),
				Arrays.asList(GenusDefinitionParserTest.getSpeciesAliases()), Arrays.asList(Region.values())
		);
		hlNPMap.setAll(k -> new NonprimaryHLCoefficients(Arrays.asList(1.0f, 5.0f), 1));
		controlMap.put(HLNonprimaryCoefficientParser.CONTROL_KEY, hlNPMap);
		return hlNPMap;
	}

	private MatrixMap2<String, Region, Coefficients> populateHlP3(Map<String, Object> controlMap) {
		MatrixMap2<String, Region, Coefficients> hlP3Map = new MatrixMap2Impl(
				Arrays.asList(GenusDefinitionParserTest.getSpeciesAliases()), Arrays.asList(Region.values())
		);
		hlP3Map.setAll(k -> new Coefficients(Arrays.asList(1.0f, 5.0f, 7.0f, 13.0f), 1));
		controlMap.put(HLCoefficientParser.CONTROL_KEY_P3, hlP3Map);
		return hlP3Map;
	}

	private MatrixMap2<String, Region, Coefficients> populateHlP2(Map<String, Object> controlMap) {
		MatrixMap2<String, Region, Coefficients> hlP2Map = new MatrixMap2Impl(
				Arrays.asList(GenusDefinitionParserTest.getSpeciesAliases()), Arrays.asList(Region.values())
		);
		hlP2Map.setAll(k -> new Coefficients(Arrays.asList(1.0f, 5.0f), 1));
		controlMap.put(HLCoefficientParser.CONTROL_KEY_P2, hlP2Map);
		return hlP2Map;
	}

	private MatrixMap2<String, Region, Coefficients> populateHlP1(Map<String, Object> controlMap) {
		MatrixMap2<String, Region, Coefficients> hlP1Map = new MatrixMap2Impl(
				Arrays.asList(GenusDefinitionParserTest.getSpeciesAliases()), Arrays.asList(Region.values())
		);
		hlP1Map.setAll(k -> new Coefficients(Arrays.asList(1.0f, 5.0f, 7.0f), 1));
		controlMap.put(HLCoefficientParser.CONTROL_KEY_P1, hlP1Map);
		return hlP1Map;
	}

}
