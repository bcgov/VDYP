package ca.bc.gov.nrs.vdyp.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

import ca.bc.gov.nrs.vdyp.application.VdypApplicationIdentifier;
import ca.bc.gov.nrs.vdyp.common.ControlKey;
import ca.bc.gov.nrs.vdyp.io.FileResolver;
import ca.bc.gov.nrs.vdyp.io.parse.coe.BecDefinitionParser;
import ca.bc.gov.nrs.vdyp.io.parse.coe.BreakageParser;
import ca.bc.gov.nrs.vdyp.io.parse.coe.CloseUtilVolumeParser;
import ca.bc.gov.nrs.vdyp.io.parse.coe.GenusDefinitionParser;
import ca.bc.gov.nrs.vdyp.io.parse.coe.UtilComponentWSVolumeParser;
import ca.bc.gov.nrs.vdyp.io.parse.coe.VeteranBAParser;
import ca.bc.gov.nrs.vdyp.io.parse.coe.VolumeNetDecayParser;
import ca.bc.gov.nrs.vdyp.io.parse.common.ResourceParseException;
import ca.bc.gov.nrs.vdyp.io.parse.control.BaseControlParser;
import ca.bc.gov.nrs.vdyp.io.parse.control.ControlMapValueReplacer;
import ca.bc.gov.nrs.vdyp.io.parse.control.NonFipControlParser;
import ca.bc.gov.nrs.vdyp.io.parse.control.ResourceControlMapModifier;
import ca.bc.gov.nrs.vdyp.io.parse.control.StartApplicationControlParser;
import ca.bc.gov.nrs.vdyp.model.BecDefinition;
import ca.bc.gov.nrs.vdyp.model.BecLookup;
import ca.bc.gov.nrs.vdyp.model.Coefficients;
import ca.bc.gov.nrs.vdyp.model.GenusDefinition;
import ca.bc.gov.nrs.vdyp.model.MatrixMap2Impl;
import ca.bc.gov.nrs.vdyp.model.Region;

public class TestUtils {

	/**
	 * Create a stream returning the given sequence of lines.
	 */
	public static InputStream makeInputStream(String... lines) {
		return new ByteArrayInputStream(String.join("\r\n", lines).getBytes());
	}

	public static class MockOutputStream extends OutputStream {
		private boolean isOpen = true;
		private ByteArrayOutputStream realStream;
		private String streamName;

		public MockOutputStream(String streamName) {
			this.realStream = new ByteArrayOutputStream();
			this.streamName = streamName;
		}

		@Override
		public void write(int b) throws IOException {
			if (isOpen) {
				realStream.write(b);
			} else {
				fail("Attempt to write to closed stream");
			}
		}

		@Override
		public void close() throws IOException {
			isOpen = false;
		}

		@Override
		public String toString() {
			return realStream.toString();
		}

		public boolean isOpen() {
			return isOpen;
		}

		public void assertClosed() {
			assertTrue(!isOpen, "stream " + streamName + " was not closed");
		}

		public void assertContent(Matcher<String> matcher) {
			assertThat("Stream " + streamName + "contents", toString(), matcher);
		}
	}

	/**
	 * Read an output streams contents as a string..
	 */
	public static String readOutputStream(ByteArrayOutputStream os) {
		return os.toString();
	}

	/**
	 * Return a mock file resolver that expects to resolve one file with the given name and returns the given input
	 * stream.
	 *
	 * @param expectedFilename
	 * @param is
	 * @return
	 */
	public static FileResolver fileResolver(String expectedFilename, InputStream is) {
		var result = new MockFileResolver("TEST");
		result.addStream(expectedFilename, is);
		return result;
	}

	/**
	 * Add a mock control map entry for BEC parse results
	 */
	public static void populateControlMapBecReal(Map<String, Object> controlMap) {
		populateControlMapFromResource(controlMap, new BecDefinitionParser(), "Becdef.dat");
	}

	/**
	 * Add a mock control map entry for SP0 parse results. Alternates assigning to Coastal and Interior regions,
	 * starting with Coastal.
	 */
	public static void populateControlMapBec(Map<String, Object> controlMap, String... aliases) {

		List<BecDefinition> becs = new ArrayList<>();

		int i = 0;
		for (var alias : aliases) {
			becs.add(new BecDefinition(alias, Region.values()[i % 2], "Test " + alias));
			i++;
		}

		controlMap.put(ControlKey.BEC_DEF.name(), new BecLookup(becs));
	}

	/**
	 * Add a mock control map entry for BEC parse results with species "B1" and "B2" for Coastal and Interior Regions
	 * respectively
	 */
	public static void populateControlMapBec(Map<String, Object> controlMap) {
		populateControlMapBec(controlMap, "B1", "B2");
	}

	@SuppressWarnings("unused")
	private static BecDefinition makeBec(String id, Region region, String name) {
		return new BecDefinition(id, region, name);
	}

	/**
	 * Add a mock control map entry for SP0 parse results with species "S1" and "S2"
	 */
	public static void populateControlMapGenus(Map<String, Object> controlMap) {
		populateControlMapGenus(controlMap, "S1", "S2");
	}

	/**
	 * Add a mock control map entry for SP0 parse results with 16 species
	 */
	public static void populateControlMapGenusReal(Map<String, Object> controlMap) {
		populateControlMapFromResource(controlMap, new GenusDefinitionParser(), "SP0DEF_v0.dat");
	}

	/**
	 * Get the species aliases expected
	 */
	public static String[] getSpeciesAliases() {
		return new String[] { "AC", "AT", "B", "C", "D", "E", "F", "H", "L", "MB", "PA", "PL", "PW", "PY", "S", "Y" };
	}

	/**
	 * Add a mock control map entry for SP0 parse results
	 */
	public static void populateControlMapGenus(Map<String, Object> controlMap, String... aliases) {

		List<GenusDefinition> sp0List = new ArrayList<>();

		for (var alias : aliases) {
			sp0List.add(new GenusDefinition(alias, java.util.Optional.empty(), "Test " + alias));
		}

		controlMap.put(ControlKey.SP0_DEF.name(), sp0List);
	}

	/**
	 * Add a mock control map entries for equation groups
	 */
	public static void populateControlMapEquationGroups(
			Map<String, Object> controlMap, //
			BiFunction<String, String, int[]> mapper
	) {

		var becAliases = BecDefinitionParser.getBecs(controlMap).getBecAliases();
		var genusAliases = GenusDefinitionParser.getSpeciesAliases(controlMap);

		var volume = new MatrixMap2Impl<String, String, Integer>(genusAliases, becAliases, mapper.andThen(x -> x[0]));
		controlMap.put(ControlKey.VOLUME_EQN_GROUPS.name(), volume);

		var decay = new MatrixMap2Impl<String, String, Integer>(genusAliases, becAliases, mapper.andThen(x -> x[1]));
		controlMap.put(ControlKey.DECAY_GROUPS.name(), decay);

		var breakage = new MatrixMap2Impl<String, String, Integer>(genusAliases, becAliases, mapper.andThen(x -> x[2]));
		controlMap.put(ControlKey.BREAKAGE_GROUPS.name(), breakage);
	}

	/**
	 * Add mock control map entry for VeteranBQ Map
	 */
	public static void populateControlMapVeteranBq(Map<String, Object> controlMap) {
		populateControlMapFromResource(controlMap, new VeteranBAParser(), "REGBAV01.COE");
	}

	public static void
			populateControlMapVeteranDq(Map<String, Object> controlMap, BiFunction<String, Region, float[]> mapper) {
		var regions = Arrays.asList(Region.values());
		var genusAliases = GenusDefinitionParser.getSpeciesAliases(controlMap);

		var result = new MatrixMap2Impl<String, Region, Coefficients>(
				genusAliases, regions, mapper.andThen(x -> new Coefficients(x, 1))
		);
		controlMap.put(ControlKey.VETERAN_LAYER_DQ.name(), result);
	}

	public static void
			populateControlMapVeteranVolAdjust(Map<String, Object> controlMap, Function<String, float[]> mapper) {

		var genusAliases = GenusDefinitionParser.getSpeciesAliases(controlMap);

		var result = genusAliases.stream()
				.collect(Collectors.toMap(x -> x, mapper.andThen(x -> new Coefficients(x, 1))));

		controlMap.put(ControlKey.VETERAN_LAYER_VOLUME_ADJUST.name(), result);
	}

	public static void populateControlMapWholeStemVolume(
			Map<String, Object> controlMap, BiFunction<Integer, Integer, Optional<Coefficients>> mapper
	) {

		var groupIndicies = groupIndices(UtilComponentWSVolumeParser.MAX_GROUPS);

		populateControlMap2(controlMap, ControlKey.UTIL_COMP_WS_VOLUME.name(), UTIL_CLASSES, groupIndicies, mapper);
	}

	public static void populateControlMapCloseUtilization(
			Map<String, Object> controlMap, BiFunction<Integer, Integer, Optional<Coefficients>> mapper
	) {

		var groupIndicies = groupIndices(CloseUtilVolumeParser.MAX_GROUPS);

		populateControlMap2(controlMap, ControlKey.CLOSE_UTIL_VOLUME.name(), UTIL_CLASSES, groupIndicies, mapper);
	}

	public static void populateControlMapNetDecay(
			Map<String, Object> controlMap, BiFunction<Integer, Integer, Optional<Coefficients>> mapper
	) {

		var groupIndicies = groupIndices(VolumeNetDecayParser.MAX_GROUPS);

		populateControlMap2(controlMap, ControlKey.VOLUME_NET_DECAY.name(), UTIL_CLASSES, groupIndicies, mapper);
	}

	public static void
			populateControlMapNetWaste(Map<String, Object> controlMap, Function<String, Coefficients> mapper) {
		var speciesDim = Arrays.asList(getSpeciesAliases());

		populateControlMap1(controlMap, ControlKey.VOLUME_NET_DECAY_WASTE.name(), speciesDim, mapper);
	}

	public static void
			populateControlMapNetBreakage(HashMap<String, Object> controlMap, Function<Integer, Coefficients> mapper) {
		var groupIndicies = groupIndices(BreakageParser.MAX_GROUPS);

		populateControlMap1(controlMap, ControlKey.BREAKAGE.name(), groupIndicies, mapper);
	}

	public static <K1, K2, V> void populateControlMap2(
			Map<String, Object> controlMap, String key, Collection<K1> keys1, Collection<K2> keys2,
			BiFunction<K1, K2, V> mapper
	) {

		var result = new MatrixMap2Impl<>(keys1, keys2, mapper);

		controlMap.put(key, result);
	}

	public static <K, V> void populateControlMap1(
			Map<String, Object> controlMap, String key, Collection<K> keys1, Function<K, V> mapper
	) {

		var result = keys1.stream().collect(Collectors.toMap(k -> k, mapper));

		controlMap.put(key, result);
	}

	static final Collection<Integer> UTIL_CLASSES = IntStream.rangeClosed(-1, 4).mapToObj(x -> x).toList();

	static Collection<Integer> groupIndices(int max) {
		return IntStream.rangeClosed(1, max).mapToObj(x -> x).toList();
	}

	public static void populateControlMapFromResource(
			Map<String, Object> controlMap, ResourceControlMapModifier parser, String resource
	) {
		try (var is = TestUtils.class.getResourceAsStream("coe/" + resource)) {
			parser.modify(controlMap, is);
		} catch (IOException | ResourceParseException ex) {
			fail(ex);
		}
	}

	public static void populateControlMapFromStream(
			Map<String, Object> controlMap, ResourceControlMapModifier parser, InputStream is
	) {
		try {
			parser.modify(controlMap, is);
		} catch (IOException | ResourceParseException ex) {
			fail(ex);
		}
	}

	public static FileResolver fileResolver(Class<?> klazz) {
		return new FileResolver() {

			@Override
			public InputStream resolveForInput(String filename) throws IOException {
				assertThat("Attempt to resolve a null filename for input", filename, Matchers.notNullValue());
				InputStream resourceAsStream = klazz.getResourceAsStream(filename);
				if (resourceAsStream == null)
					throw new IOException("Could not load " + filename);
				return resourceAsStream;
			}

			@Override
			public OutputStream resolveForOutput(String filename) throws IOException {
				fail("Should not be opening file " + filename + " for output");
				return null;
			}

			@Override
			public String toString(String filename) throws IOException {
				return klazz.getResource(filename).getPath();
			}

			@Override
			public FileResolver relative(String path) throws IOException {
				fail("Should not be requesting relative file resolver " + path);
				return null;
			}

		};
	}

	public static void assumeThat(java.lang.Object actual, @SuppressWarnings("rawtypes") org.hamcrest.Matcher matcher) {
		assumeTrue(matcher.matches(actual));
	}

	public static Map<String, Object> loadControlMap(BaseControlParser parser, Class<?> klazz, String resourceName)
			throws IOException, ResourceParseException {
		try (var is = klazz.getResourceAsStream(resourceName)) {

			return parser.parse(is, TestUtils.fileResolver(klazz), new HashMap<>());
		}
	}

	public static Map<String, Object> loadControlMap() {
		BaseControlParser parser = new TestNonFipControlParser();
		try {
			return TestUtils.loadControlMap(parser, TestUtils.class, "VRISTART.CTR");
		} catch (IOException | ResourceParseException ex) {
			fail(ex);
			return null;
		}

	}

	public static String polygonId(String name, int year) {
		String result = String.format("%-21s%4d", name, year);
		assert result.length() == 25;
		return result;
	}

	public static StartApplicationControlParser startAppControlParser() {
		return new TestNonFipControlParser();
	}

	static private class TestNonFipControlParser extends NonFipControlParser {

		public TestNonFipControlParser() {
			initialize();
		}

		@Override
		protected List<ControlMapValueReplacer<Object, String>> inputFileParsers() {
			return Collections.emptyList();
		}

		@Override
		protected List<ControlKey> outputFileParsers() {
			return Collections.emptyList();
		}

		@Override
		protected VdypApplicationIdentifier getProgramId() {
			return VdypApplicationIdentifier.VRI_START;
		}
	};

}
