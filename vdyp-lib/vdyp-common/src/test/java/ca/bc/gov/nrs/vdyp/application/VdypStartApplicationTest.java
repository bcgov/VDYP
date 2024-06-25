package ca.bc.gov.nrs.vdyp.application;

import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.causedBy;
import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.closeTo;
import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.coe;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.describedAs;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import org.easymock.Capture;
import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import ca.bc.gov.nrs.vdyp.common.ControlKey;
import ca.bc.gov.nrs.vdyp.common.Utils;
import ca.bc.gov.nrs.vdyp.io.FileSystemFileResolver;
import ca.bc.gov.nrs.vdyp.io.parse.coe.BecDefinitionParser;
import ca.bc.gov.nrs.vdyp.io.parse.common.ResourceParseException;
import ca.bc.gov.nrs.vdyp.io.parse.control.ControlMapValueReplacer;
import ca.bc.gov.nrs.vdyp.io.parse.control.NonFipControlParser;
import ca.bc.gov.nrs.vdyp.io.parse.streaming.StreamingParser;
import ca.bc.gov.nrs.vdyp.io.parse.streaming.StreamingParserFactory;
import ca.bc.gov.nrs.vdyp.model.BaseVdypLayer;
import ca.bc.gov.nrs.vdyp.model.BaseVdypPolygon;
import ca.bc.gov.nrs.vdyp.model.BaseVdypSite;
import ca.bc.gov.nrs.vdyp.model.BaseVdypSpecies;
import ca.bc.gov.nrs.vdyp.model.BecDefinition;
import ca.bc.gov.nrs.vdyp.model.Coefficients;
import ca.bc.gov.nrs.vdyp.model.InputLayer;
import ca.bc.gov.nrs.vdyp.model.LayerType;
import ca.bc.gov.nrs.vdyp.model.MatrixMap2Impl;
import ca.bc.gov.nrs.vdyp.model.PolygonIdentifier;
import ca.bc.gov.nrs.vdyp.model.PolygonMode;
import ca.bc.gov.nrs.vdyp.model.Region;
import ca.bc.gov.nrs.vdyp.test.MockFileResolver;
import ca.bc.gov.nrs.vdyp.test.TestUtils;

@SuppressWarnings({ "unchecked", "rawtypes" })
class VdypStartApplicationTest {

	private Map<String, Object> controlMap = new HashMap<>();

	static class TestSpecies extends BaseVdypSpecies {

		protected TestSpecies(
				PolygonIdentifier polygonIdentifier, LayerType layerType, String genus, float percentGenus
		) {
			super(polygonIdentifier, layerType, genus, percentGenus);
		}

		public static TestSpecies build(Consumer<Builder> config) {
			var builder = new Builder();
			config.accept(builder);
			return builder.build();
		}

		public static class Builder extends BaseVdypSpecies.Builder<TestSpecies> {

			@Override
			protected TestSpecies doBuild() {
				return new TestSpecies(polygonIdentifier.get(), layerType.get(), genus.get(), percentGenus.get());
			}

		}
	}

	static class TestSite extends BaseVdypSite {

		protected TestSite(
				PolygonIdentifier polygonIdentifier, LayerType layerType, String siteGenus,
				Optional<Integer> siteCurveNumber, Optional<Float> siteIndex, Optional<Float> height,
				Optional<Float> ageTotal, Optional<Float> yearsToBreastHeight
		) {
			super(
					polygonIdentifier, layerType, siteGenus, siteCurveNumber, siteIndex, height, ageTotal,
					yearsToBreastHeight
			);
		}

		public static TestSite build(Consumer<Builder> config) {
			var builder = new Builder();
			config.accept(builder);
			return builder.build();
		}

		public static class Builder extends BaseVdypSite.Builder<TestSite> {

			protected Optional<String> siteSpecies = Optional.empty();

			@Override
			protected TestSite doBuild() {
				return new TestSite(
						this.polygonIdentifier.get(), //
						this.layerType.get(), //
						this.siteGenus.get(), //
						this.siteCurveNumber, //
						this.siteIndex, //
						this.height, //
						this.ageTotal, //
						this.yearsToBreastHeight //
				);
			}
		}
	}

	static class TestLayer extends BaseVdypLayer<TestSpecies, TestSite> implements InputLayer {

		final float crownClosure;

		protected TestLayer(
				PolygonIdentifier polygonIdentifier, LayerType layerType, Optional inventoryTypeGroup,
				float crownClosure
		) {
			super(polygonIdentifier, layerType, inventoryTypeGroup);
			this.crownClosure = crownClosure;
		}

		@Override
		public float getCrownClosure() {
			return crownClosure;
		}

		public static TestLayer build(Consumer<Builder> config) {
			var builder = new Builder();
			config.accept(builder);
			return builder.build();
		}

		public static class Builder
				extends BaseVdypLayer.Builder<TestLayer, TestSpecies, TestSite, TestSpecies.Builder, TestSite.Builder> {

			protected Optional<Float> crownClosure = Optional.empty();

			public Builder crownClosure(float crownClosure) {
				this.crownClosure = Optional.of(crownClosure);
				return this;
			}

			@Override
			protected void check(Collection<String> errors) {
				super.check(errors);
				requirePresent(crownClosure, "crownClosure", errors);
			}

			@Override
			protected TestLayer doBuild() {

				return (new TestLayer(
						polygonIdentifier.get(), //
						layerType.get(), //
						inventoryTypeGroup, //
						crownClosure.get() //
				));
			}

			@Override
			protected TestSpecies buildSpecies(Consumer<TestSpecies.Builder> config) {
				return TestSpecies.build(sb -> {
					sb.polygonIdentifier(polygonIdentifier.get());
					sb.layerType(layerType.get());
					config.accept(sb);
				});
			}

			@Override
			protected TestSite buildSite(Consumer<TestSite.Builder> config) {
				return TestSite.build(si -> {
					si.polygonIdentifier(polygonIdentifier.get());
					si.layerType(layerType.get());
					config.accept(si);
				});
			}
		}
	}

	static class TestPolygon extends BaseVdypPolygon<TestLayer, Optional<Float>, TestSpecies, TestSite> {

		public static TestPolygon build(Consumer<Builder> config) {
			var builder = new Builder();
			config.accept(builder);
			return builder.build();
		}

		protected TestPolygon(
				PolygonIdentifier polygonIdentifier, Optional<Float> percentAvailable, String fiz, String becIdentifier,
				Optional<PolygonMode> mode
		) {
			super(polygonIdentifier, percentAvailable, fiz, becIdentifier, mode);
		}

		public static class Builder extends
				BaseVdypPolygon.Builder<TestPolygon, TestLayer, Optional<Float>, TestSpecies, TestSite, TestLayer.Builder, TestSpecies.Builder, TestSite.Builder> {

			@Override
			protected TestLayer.Builder getLayerBuilder() {
				var builder = new TestLayer.Builder();
				return builder;
			}

			@Override
			protected TestPolygon doBuild() {
				return (new TestPolygon(
						polygonIdentifier.get(), //
						percentAvailable.flatMap(x -> x), //
						forestInventoryZone.get(), //
						biogeoclimaticZone.get(), //
						mode //
				));

			}

		}

	};

	static class TestStartApplication extends VdypStartApplication<TestPolygon, TestLayer, TestSpecies, TestSite> {

		boolean realInit;

		public TestStartApplication(Map<String, Object> controlMap, boolean realInit) {
			this.setControlMap(controlMap);
			this.realInit = realInit;
		}

		@Override
		public void init(FileSystemFileResolver resolver, String... controlFilePaths)
				throws IOException, ResourceParseException {
			if (realInit) {
				super.init(resolver, controlFilePaths);
			}
		}

		@Override
		public void init(FileSystemFileResolver resolver, Map<String, Object> controlMap) throws IOException {
			if (realInit) {
				super.init(resolver, controlMap);
			}
		}

		@Override
		protected NonFipControlParser getControlFileParser() {
			return new NonFipControlParser() {

				@Override
				protected List<ControlMapValueReplacer<Object, String>> inputFileParsers() {
					return List.of();
				}

				@Override
				protected List<ControlKey> outputFileParsers() {
					return List.of();
				}

				@Override
				protected VdypApplicationIdentifier getProgramId() {
					return VdypApplicationIdentifier.VRI_START;
				}

			};
		}

		@Override
		public void process() throws ProcessingException {
			// Do Nothing
		}

		@Override
		public VdypApplicationIdentifier getId() {
			return VdypApplicationIdentifier.VRI_START;
		}

		@Override
		protected TestSpecies copySpecies(TestSpecies toCopy, Consumer config) {
			return null;
		}

		@Override
		protected Optional<TestSite> getPrimarySite(TestLayer layer) {
			return Utils.optSafe(layer.getSites().values().iterator().next());
		}

		@Override
		protected float getYieldFactor(TestPolygon polygon) {
			// TODO Auto-generated method stub
			return 1;
		}

	}

	@Nested
	class Init {

		@Test
		void testInitWithoutErrors() throws IOException, ResourceParseException {
			controlMap = TestUtils.loadControlMap();

			MockFileResolver resolver = dummyIo();

			InputStream inputStream = TestUtils.makeInputStream("");
			resolver.addStream("testControl", inputStream);

			var app = new TestStartApplication(controlMap, false);

			app.init(resolver, "testControl");
			assertThat(app.controlMap, is(controlMap));

			app.close();
		}

		@Test
		void testInitNoControlFiles() throws IOException, ResourceParseException {
			MockFileResolver resolver = new MockFileResolver("Test");

			var app = new TestStartApplication(controlMap, true);

			var ex = assertThrows(IllegalArgumentException.class, () -> app.init(resolver));
			assertThat(ex, hasProperty("message", is("At least one control file must be specified.")));

			app.close();
		}

	}

	private MockFileResolver dummyIo() {
		controlMap.put(ControlKey.VRI_OUTPUT_VDYP_POLYGON.name(), "DUMMY1");
		controlMap.put(ControlKey.VRI_OUTPUT_VDYP_LAYER_BY_SPECIES.name(), "DUMMY2");
		controlMap.put(ControlKey.VRI_OUTPUT_VDYP_LAYER_BY_SP0_BY_UTIL.name(), "DUMMY3");

		MockFileResolver resolver = new MockFileResolver("Test");
		resolver.addStream("DUMMY1", (OutputStream) new ByteArrayOutputStream());
		resolver.addStream("DUMMY2", (OutputStream) new ByteArrayOutputStream());
		resolver.addStream("DUMMY3", (OutputStream) new ByteArrayOutputStream());
		return resolver;
	}

	@Nested
	class GetStreamingParser {

		@Test
		void testSimple() throws IOException, ResourceParseException, ProcessingException {
			StreamingParserFactory streamingParserFactory = EasyMock.createMock(StreamingParserFactory.class);
			StreamingParser streamingParser = EasyMock.createMock(StreamingParser.class);

			MockFileResolver resolver = dummyIo();

			controlMap.put(ControlKey.FIP_INPUT_YIELD_LAYER.name(), streamingParserFactory);

			EasyMock.expect(streamingParserFactory.get()).andReturn(streamingParser);

			var app = new TestStartApplication(controlMap, false);

			EasyMock.replay(streamingParserFactory, streamingParser);

			app.init(resolver, controlMap);

			var result = app.getStreamingParser(ControlKey.FIP_INPUT_YIELD_LAYER);

			assertThat(result, is(streamingParser));

			EasyMock.verify(streamingParserFactory, streamingParser);

			app.close();
		}

		@Test
		void testEntryMissing() throws IOException, ResourceParseException, ProcessingException {

			MockFileResolver resolver = dummyIo();

			var app = new TestStartApplication(controlMap, false);

			app.init(resolver, controlMap);

			@SuppressWarnings("resource") // mock object can't leak anything
			var ex = assertThrows(
					ProcessingException.class, () -> app.getStreamingParser(ControlKey.FIP_INPUT_YIELD_LAYER)
			);
			assertThat(ex, hasProperty("message", is("Data file FIP_INPUT_YIELD_LAYER not specified in control map.")));

			app.close();
		}

		@Test
		void testErrorOpeningFile() throws IOException, ResourceParseException, ProcessingException {
			var mockControl = EasyMock.createControl();

			StreamingParserFactory streamingParserFactory = mockControl.createMock(StreamingParserFactory.class);

			MockFileResolver resolver = dummyIo();
			;
			controlMap.put(ControlKey.FIP_INPUT_YIELD_LAYER.name(), streamingParserFactory);

			IOException exception = new IOException("This is a Test");
			EasyMock.expect(streamingParserFactory.get()).andThrow(exception);

			var app = getTestUnit(mockControl);

			mockControl.replay();

			app.init(resolver, controlMap);

			@SuppressWarnings("resource") // mock object can't leak anything
			var ex = assertThrows(
					ProcessingException.class, () -> app.getStreamingParser(ControlKey.FIP_INPUT_YIELD_LAYER)
			);
			assertThat(ex, hasProperty("message", is("Error while opening data file.")));
			assertThat(ex, causedBy(is(exception)));

			mockControl.verify();

			app.close();
		}

	}

	protected VdypStartApplication getTestUnit(IMocksControl control) throws IOException {

		VdypStartApplication mock = EasyMock.createMockBuilder(VdypStartApplication.class)//
				.addMockedMethods("getControlFileParser", "process", "getId", "copySpecies")//
				.createMock(control);

		return mock;
	}

	BaseVdypSpecies mockSpecies(IMocksControl mockControl, String id, float percent) {
		BaseVdypSpecies spec = mockControl.createMock("spec" + id, BaseVdypSpecies.class);

		EasyMock.expect(spec.getGenus()).andStubReturn(id);
		EasyMock.expect(spec.getPercentGenus()).andStubReturn(percent);
		EasyMock.expect(spec.getFractionGenus()).andStubReturn(percent / 100f);

		return spec;
	}

	@Nested
	class FindPrimarySpecies {
		@Test
		void testFindPrimarySpeciesNoSpecies() throws Exception {
			var mockControl = EasyMock.createControl();

			try (VdypStartApplication app = getTestUnit(mockControl)) {
				mockControl.replay();
				var allSpecies = Collections.emptyList();
				assertThrows(IllegalArgumentException.class, () -> app.findPrimarySpecies(allSpecies));
			}
			mockControl.verify();
		}

		@Test
		void testOneSpecies() throws Exception {
			var mockControl = EasyMock.createControl();

			BaseVdypSpecies spec = mockControl.createMock(BaseVdypSpecies.class);

			EasyMock.expect(spec.getGenus()).andStubReturn("B");
			EasyMock.expect(spec.getPercentGenus()).andStubReturn(100f);
			EasyMock.expect(spec.getFractionGenus()).andStubReturn(1f);

			try (VdypStartApplication app = getTestUnit(mockControl)) {
				EasyMock.expect(app.copySpecies(EasyMock.same(spec), EasyMock.anyObject())).andReturn(spec);
				mockControl.replay();
				var allSpecies = List.of(spec);
				List<BaseVdypSpecies> result = app.findPrimarySpecies(allSpecies);

				assertThat(result, hasSize(1));
				assertThat(result, contains(is(spec)));
			}
			mockControl.verify();
		}

		@Test
		void testCombinePAIntoPL() throws Exception {
			var mockControl = EasyMock.createControl();

			BaseVdypSpecies spec1 = mockSpecies(mockControl, "PA", 25f);
			BaseVdypSpecies spec2 = mockSpecies(mockControl, "PL", 75f);
			BaseVdypSpecies specCombined = mockSpecies(mockControl, "PL", 100f);
			BaseVdypSpecies.Builder specBuilder = mockControl.createMock(BaseVdypSpecies.Builder.class);
			BaseVdypSpecies.Builder copyBuilder = mockControl.createMock(BaseVdypSpecies.Builder.class); // Should not
																											// have
																											// any
																											// methods
																											// called.

			Capture<Consumer<BaseVdypSpecies.Builder>> configCapture = Capture.newInstance();
			Capture<Consumer<BaseVdypSpecies.Builder>> copyCapture = Capture.newInstance();

			EasyMock.expect(specBuilder.percentGenus(100f)).andReturn(specBuilder);

			try (VdypStartApplication app = getTestUnit(mockControl)) {
				EasyMock.expect(app.copySpecies(EasyMock.same(spec1), EasyMock.capture(copyCapture))).andReturn(spec1);
				EasyMock.expect(app.copySpecies(EasyMock.same(spec2), EasyMock.capture(copyCapture))).andReturn(spec2);
				EasyMock.expect(app.copySpecies(EasyMock.same(spec2), EasyMock.capture(configCapture)))
						.andReturn(specCombined);
				mockControl.replay();

				var allSpecies = List.of(spec1, spec2);

				List<BaseVdypSpecies> result = app.findPrimarySpecies(allSpecies);

				assertThat(result, hasSize(1));
				assertThat(result, contains(is(specCombined)));

				configCapture.getValue().accept(specBuilder);
				for (var config : copyCapture.getValues()) {
					config.accept(copyBuilder);
				}
			}
			mockControl.verify();
		}

		@Test
		void testCombinePLIntoPA() throws Exception {

			var mockControl = EasyMock.createControl();

			BaseVdypSpecies spec1 = mockSpecies(mockControl, "PA", 75f);
			BaseVdypSpecies spec2 = mockSpecies(mockControl, "PL", 25f);
			BaseVdypSpecies specCombined = mockSpecies(mockControl, "PA", 100f);
			BaseVdypSpecies.Builder specBuilder = mockControl.createMock(BaseVdypSpecies.Builder.class);
			BaseVdypSpecies.Builder copyBuilder = mockControl.createMock(BaseVdypSpecies.Builder.class); // Should not
																											// have
																											// any
																											// methods
																											// called.

			Capture<Consumer<BaseVdypSpecies.Builder>> configCapture = Capture.newInstance();
			Capture<Consumer<BaseVdypSpecies.Builder>> copyCapture = Capture.newInstance();

			EasyMock.expect(specBuilder.percentGenus(100f)).andReturn(specBuilder);

			try (VdypStartApplication app = getTestUnit(mockControl)) {
				EasyMock.expect(app.copySpecies(EasyMock.same(spec1), EasyMock.capture(copyCapture))).andReturn(spec1);
				EasyMock.expect(app.copySpecies(EasyMock.same(spec2), EasyMock.capture(copyCapture))).andReturn(spec2);
				EasyMock.expect(app.copySpecies(EasyMock.same(spec1), EasyMock.capture(configCapture)))
						.andReturn(specCombined);
				mockControl.replay();

				var allSpecies = List.of(spec1, spec2);

				List<BaseVdypSpecies> result = app.findPrimarySpecies(allSpecies);

				assertThat(result, hasSize(1));
				assertThat(result, contains(is(specCombined)));

				configCapture.getValue().accept(specBuilder);
				for (var config : copyCapture.getValues()) {
					config.accept(copyBuilder);
				}
			}
			mockControl.verify();
		}

		@Test
		void testCombineCIntoY() throws Exception {

			var mockControl = EasyMock.createControl();

			BaseVdypSpecies spec1 = mockSpecies(mockControl, "C", 25f);
			BaseVdypSpecies spec2 = mockSpecies(mockControl, "Y", 75f);
			BaseVdypSpecies specCombined = mockSpecies(mockControl, "Y", 100f);

			BaseVdypSpecies.Builder specBuilder = mockControl.createMock(BaseVdypSpecies.Builder.class);
			BaseVdypSpecies.Builder copyBuilder = mockControl.createMock(BaseVdypSpecies.Builder.class); // Should not
																											// have
																											// any
																											// methods
																											// called.

			Capture<Consumer<BaseVdypSpecies.Builder>> configCapture = Capture.newInstance();
			Capture<Consumer<BaseVdypSpecies.Builder>> copyCapture = Capture.newInstance();

			EasyMock.expect(specBuilder.percentGenus(100f)).andReturn(specBuilder);

			try (VdypStartApplication app = getTestUnit(mockControl)) {
				EasyMock.expect(app.copySpecies(EasyMock.same(spec1), EasyMock.capture(copyCapture))).andReturn(spec1);
				EasyMock.expect(app.copySpecies(EasyMock.same(spec2), EasyMock.capture(copyCapture))).andReturn(spec2);
				EasyMock.expect(app.copySpecies(EasyMock.same(spec2), EasyMock.capture(configCapture)))
						.andReturn(specCombined);
				mockControl.replay();

				var allSpecies = List.of(spec1, spec2);

				List<BaseVdypSpecies> result = app.findPrimarySpecies(allSpecies);

				assertThat(result, hasSize(1));
				assertThat(result, contains(is(specCombined)));

				configCapture.getValue().accept(specBuilder);
				for (var config : copyCapture.getValues()) {
					config.accept(copyBuilder);
				}
			}
			mockControl.verify();
		}

		@Test
		void testCombineYIntoC() throws Exception {

			var mockControl = EasyMock.createControl();

			BaseVdypSpecies spec1 = mockSpecies(mockControl, "C", 75f);
			BaseVdypSpecies spec2 = mockSpecies(mockControl, "Y", 25f);
			BaseVdypSpecies specCombined = mockSpecies(mockControl, "C", 100f);

			BaseVdypSpecies.Builder specBuilder = mockControl.createMock(BaseVdypSpecies.Builder.class);
			BaseVdypSpecies.Builder copyBuilder = mockControl.createMock(BaseVdypSpecies.Builder.class); // Should not
																											// have
																											// any
																											// methods
																											// called.

			Capture<Consumer<BaseVdypSpecies.Builder>> configCapture = Capture.newInstance();
			Capture<Consumer<BaseVdypSpecies.Builder>> copyCapture = Capture.newInstance();

			EasyMock.expect(specBuilder.percentGenus(100f)).andReturn(specBuilder);

			try (VdypStartApplication app = getTestUnit(mockControl)) {
				EasyMock.expect(app.copySpecies(EasyMock.same(spec1), EasyMock.capture(copyCapture))).andReturn(spec1);
				EasyMock.expect(app.copySpecies(EasyMock.same(spec2), EasyMock.capture(copyCapture))).andReturn(spec2);
				EasyMock.expect(app.copySpecies(EasyMock.same(spec1), EasyMock.capture(configCapture)))
						.andReturn(specCombined);
				mockControl.replay();

				var allSpecies = List.of(spec1, spec2);

				List<BaseVdypSpecies> result = app.findPrimarySpecies(allSpecies);

				assertThat(result, hasSize(1));
				assertThat(result, contains(is(specCombined)));

				configCapture.getValue().accept(specBuilder);
				for (var config : copyCapture.getValues()) {
					config.accept(copyBuilder);
				}
			}
			mockControl.verify();
		}

		@Test
		void testSort() throws Exception {

			var mockControl = EasyMock.createControl();

			BaseVdypSpecies spec1 = mockSpecies(mockControl, "B", 20f);
			BaseVdypSpecies spec2 = mockSpecies(mockControl, "H", 70f);
			BaseVdypSpecies spec3 = mockSpecies(mockControl, "MB", 10f);
			BaseVdypSpecies.Builder copyBuilder = mockControl.createMock(BaseVdypSpecies.Builder.class); // Should not
																											// have
																											// any
																											// methods
																											// called.

			Capture<Consumer<BaseVdypSpecies.Builder>> copyCapture = Capture.newInstance();

			try (VdypStartApplication app = getTestUnit(mockControl)) {
				EasyMock.expect(app.copySpecies(EasyMock.same(spec1), EasyMock.capture(copyCapture))).andReturn(spec1);
				EasyMock.expect(app.copySpecies(EasyMock.same(spec2), EasyMock.capture(copyCapture))).andReturn(spec2);
				EasyMock.expect(app.copySpecies(EasyMock.same(spec3), EasyMock.capture(copyCapture))).andReturn(spec3);
				mockControl.replay();

				var allSpecies = List.of(spec1, spec2, spec3);

				List<BaseVdypSpecies> result = app.findPrimarySpecies(allSpecies);

				assertThat(result, hasSize(2));
				assertThat(result, contains(is(spec2), is(spec1)));

				for (var config : copyCapture.getValues()) {
					config.accept(copyBuilder);
				}
			}
			mockControl.verify();

		}
	}

	@Nested
	class FindItg {

		@Test
		void testFindItg80PercentPure() throws Exception {
			var mockControl = EasyMock.createControl();

			BaseVdypSpecies spec1 = mockSpecies(mockControl, "F", 80f);
			BaseVdypSpecies spec2 = mockSpecies(mockControl, "C", 20f);

			try (VdypStartApplication app = getTestUnit(mockControl)) {
				mockControl.replay();

				Map<String, BaseVdypSpecies> allSpecies = new HashMap<>();
				allSpecies.put(spec1.getGenus(), spec1);
				allSpecies.put(spec2.getGenus(), spec2);

				List<BaseVdypSpecies> primarySpecies = List.of(spec1, spec2);

				var result = app.findItg(primarySpecies);

				assertEquals(1, result);

			}
			mockControl.verify();
		}

		@Test
		void testFindItgNoSecondary() throws Exception {

			var mockControl = EasyMock.createControl();

			BaseVdypSpecies spec1 = mockSpecies(mockControl, "F", 100f);

			try (VdypStartApplication app = getTestUnit(mockControl)) {
				mockControl.replay();

				Map<String, BaseVdypSpecies> allSpecies = new HashMap<>();
				allSpecies.put(spec1.getGenus(), spec1);

				List<BaseVdypSpecies> primarySpecies = List.of(spec1);

				var result = app.findItg(primarySpecies);

				assertEquals(1, result);

			}
			mockControl.verify();
		}

		void assertItgMixed(VdypStartApplication app, int expected, String primary, String... secondary)
				throws ProcessingException {
			for (var sec : secondary) {
				var result = app.findItg(primarySecondarySpecies(primary, sec));
				assertThat(
						result,
						describedAs("ITG for " + primary + " and " + sec + " should be " + expected, is(expected))
				);
			}
		}

		void assertItgMixed(VdypStartApplication app, int expected, String primary, Collection<String> secondary)
				throws ProcessingException {
			for (var sec : secondary) {
				var mocks = primarySecondarySpecies(primary, sec);
				var result = app.findItg(mocks);
				assertThat(
						result,
						describedAs("ITG for " + primary + " and " + sec + " should be " + expected, is(expected))
				);
			}
		}

		@Test
		void testFindItgMixed() throws Exception {
			var mockControl = EasyMock.createControl();
			try (VdypStartApplication app = getTestUnit(mockControl)) {
				mockControl.replay();

				assertItgMixed(app, 2, "F", /*  */ "Y", "C");
				assertItgMixed(app, 3, "F", /*  */ "B", "H");
				assertItgMixed(app, 3, "F", /*  */ "H");
				assertItgMixed(app, 4, "F", /*  */ "S");
				assertItgMixed(app, 5, "F", /*  */ "PL", "PA");
				assertItgMixed(app, 6, "F", /*  */ "PY");
				assertItgMixed(app, 7, "F", /*  */ "L", "PW");
				assertItgMixed(app, 8, "F", /*  */ VdypStartApplication.HARDWOODS);

				assertItgMixed(app, 10, "C", /* */ "Y");
				assertItgMixed(app, 11, "C", /* */ "B", "H", "S");
				assertItgMixed(app, 10, "C", /* */ "PL", "PA", "PY", "L", "PW");
				assertItgMixed(app, 10, "C", /* */ VdypStartApplication.HARDWOODS);

				assertItgMixed(app, 10, "Y", /* */ "C");
				assertItgMixed(app, 11, "Y", /* */ "B", "H", "S");
				assertItgMixed(app, 10, "Y", /* */ "PL", "PA", "PY", "L", "PW");
				assertItgMixed(app, 10, "Y", /* */ VdypStartApplication.HARDWOODS);

				assertItgMixed(app, 14, "H", /* */ "C", "Y");
				assertItgMixed(app, 15, "H", /* */ "B");
				assertItgMixed(app, 16, "H", /* */ "S");
				assertItgMixed(app, 17, "H", /* */ VdypStartApplication.HARDWOODS);
				assertItgMixed(app, 13, "H", /* */ "F", "L", "PA", "PL", "PY");

				assertItgMixed(app, 19, "B", /* */ "C", "Y", "H");
				assertItgMixed(app, 20, "B", /* */ "S", "PL", "PA", "PY", "L", "PW");
				assertItgMixed(app, 20, "B", /* */ VdypStartApplication.HARDWOODS);

				assertItgMixed(app, 22, "S", /* */ "F", "L", "PA", "PW", "PY");
				assertItgMixed(app, 23, "S", /* */ "C", "Y", "H");
				assertItgMixed(app, 24, "S", /* */ "B");
				assertItgMixed(app, 25, "S", /* */ "PL");
				assertItgMixed(app, 26, "S", /* */ VdypStartApplication.HARDWOODS);

				assertItgMixed(app, 27, "PW", /**/ "B", "C", "F", "H", "L", "PA", "PL", "PY", "S", "Y");
				assertItgMixed(app, 27, "PW", /**/ VdypStartApplication.HARDWOODS);

				assertItgMixed(app, 28, "PL", /**/ "PA");
				assertItgMixed(app, 30, "PL", /**/ "B", "C", "H", "S", "Y");
				assertItgMixed(app, 29, "PL", /**/ "F", "PW", "L", "PY");
				assertItgMixed(app, 31, "PL", /**/ VdypStartApplication.HARDWOODS);

				assertItgMixed(app, 28, "PA", /**/ "PL");
				assertItgMixed(app, 30, "PA", /**/ "B", "C", "H", "S", "Y");
				assertItgMixed(app, 29, "PA", /**/ "F", "PW", "L", "PY");
				assertItgMixed(app, 31, "PA", /**/ VdypStartApplication.HARDWOODS);

				assertItgMixed(app, 32, "PY", /**/ "B", "C", "F", "H", "L", "PA", "PL", "PW", "S", "Y");
				assertItgMixed(app, 32, "PY", /**/ VdypStartApplication.HARDWOODS);

				assertItgMixed(app, 33, "L", /* */ "F");
				assertItgMixed(app, 34, "L", /* */ "B", "C", "H", "PA", "PL", "PW", "PY", "S", "Y");
				assertItgMixed(app, 34, "L", /* */ VdypStartApplication.HARDWOODS);

				assertItgMixed(app, 35, "AC", /**/ "B", "C", "F", "H", "L", "PA", "PL", "PW", "PY", "S", "Y");
				assertItgMixed(app, 36, "AC", /**/ "AT", "D", "E", "MB");

				assertItgMixed(app, 37, "D", /* */ "B", "C", "F", "H", "L", "PA", "PL", "PW", "PY", "S", "Y");
				assertItgMixed(app, 38, "D", /* */ "AC", "AT", "E", "MB");

				assertItgMixed(app, 39, "MB", /**/ "B", "C", "F", "H", "L", "PA", "PL", "PW", "PY", "S", "Y");
				assertItgMixed(app, 39, "MB", /**/ "AC", "AT", "D", "E");

				assertItgMixed(app, 40, "E", /* */ "B", "C", "F", "H", "L", "PA", "PL", "PW", "PY", "S", "Y");
				assertItgMixed(app, 40, "E", /* */ "AC", "AT", "D", "MB");

				assertItgMixed(app, 41, "AT", /**/ "B", "C", "F", "H", "L", "PA", "PL", "PW", "PY", "S", "Y");
				assertItgMixed(app, 42, "AT", /**/ "AC", "D", "E", "MB");

			}
		}

	}

	List<BaseVdypSpecies> primarySecondarySpecies(String primary, String secondary) {
		var mockControl = EasyMock.createControl();

		var spec1 = this.mockSpecies(mockControl, primary, 70f);
		var spec2 = this.mockSpecies(mockControl, secondary, 70f);

		mockControl.replay();

		return List.of(spec1, spec2);
	}

	@Nested
	class FindEmpericalRelationshipParameterIndex {

		@Test
		void testModified() throws Exception {
			var mockControl = EasyMock.createControl();

			controlMap.put(
					ControlKey.DEFAULT_EQ_NUM.name(),
					new MatrixMap2Impl(Collections.singletonList("D"), Collections.singletonList("CDF"), (x, y) -> 42)
			);
			controlMap.put(
					ControlKey.EQN_MODIFIERS.name(),
					new MatrixMap2Impl(
							Collections.singletonList(42), Collections.singletonList(37), (x, y) -> Optional.of(64)
					)
			);

			try (VdypStartApplication app = getTestUnit(mockControl)) {

				MockFileResolver resolver = dummyIo();

				mockControl.replay();

				app.init(resolver, controlMap);

				var bec = new BecDefinition("CDF", Region.COASTAL, "Coastal Douglas Fir");

				int result = app.findEmpiricalRelationshipParameterIndex("D", bec, 37);

				assertThat(result, is(64));
			}
		}

		@Test
		void testUnmodified() throws Exception {
			var mockControl = EasyMock.createControl();

			controlMap.put(
					ControlKey.DEFAULT_EQ_NUM.name(),
					new MatrixMap2Impl(Collections.singletonList("D"), Collections.singletonList("CDF"), (x, y) -> 42)
			);
			controlMap.put(
					ControlKey.EQN_MODIFIERS.name(),
					new MatrixMap2Impl(
							Collections.singletonList(42), Collections.singletonList(37), (x, y) -> Optional.empty()
					)
			);

			try (VdypStartApplication app = getTestUnit(mockControl)) {

				MockFileResolver resolver = dummyIo();

				mockControl.replay();

				app.init(resolver, controlMap);

				var bec = new BecDefinition("CDF", Region.COASTAL, "Coastal Douglas Fir");

				int result = app.findEmpiricalRelationshipParameterIndex("D", bec, 37);

				assertThat(result, is(42));
			}
		}
	}

	@Nested
	class EstimatePrimaryBaseArea {

		@Test
		void testSimple() throws Exception {
			var controlMap = TestUtils.loadControlMap();
			var polygonId = new PolygonIdentifier("TestPolygon", 2024);
			try (TestStartApplication app = new TestStartApplication(controlMap, false)) {

				var bec = Utils.getBec("CWH", controlMap);

				var layer = getTestPrimaryLayer(polygonId, l -> {
					l.crownClosure(82.8000031f);

				}, s -> {
					s.ageTotal(Optional.of(85f));
					s.height(Optional.of(38.2999992f));
					s.siteIndex(Optional.of(28.6000004f));
					s.yearsToBreastHeight(Optional.of(5.4000001f));
					s.siteCurveNumber(Optional.of(34));
					s.siteGenus(Optional.of("H"));
				});

				var spec1 = getTestSpecies(polygonId, LayerType.PRIMARY, "B", s -> {
					s.setPercentGenus(33f);
					s.setFractionGenus(0.330000013f);
				});
				var spec2 = getTestSpecies(polygonId, LayerType.PRIMARY, "H", s -> {
					s.setPercentGenus(67f);
					s.setFractionGenus(0.670000017f);
				});

				Map<String, TestSpecies> allSpecies = new LinkedHashMap<>();
				allSpecies.put(spec1.getGenus(), spec1);
				allSpecies.put(spec2.getGenus(), spec2);

				layer.setSpecies(allSpecies);

				var result = app.estimatePrimaryBaseArea(layer, bec, 1f, 79.5999985f, 3.13497972f);

				assertThat(result, closeTo(62.6653595f));
			}
		}

		@Test
		void testHeightCloseToA2() throws Exception {
			controlMap = TestUtils.loadControlMap();
			var polygonId = new PolygonIdentifier("TestPolygon", 2024);
			try (TestStartApplication app = new TestStartApplication(controlMap, false)) {

				var bec = Utils.getBec("CWH", controlMap);

				var layer = getTestPrimaryLayer(polygonId, l -> {
					l.crownClosure(82.8000031f);
				}, s -> {
					s.ageTotal(Optional.of(85f));
					s.height(Optional.of(10.1667995f)); // Altered this in the debugger while running VDYP7
					s.siteIndex(Optional.of(28.6000004f));
					s.yearsToBreastHeight(Optional.of(5.4000001f));
					s.siteCurveNumber(Optional.of(34));
					s.siteGenus(Optional.of("H"));
				});

				var spec1 = getTestSpecies(polygonId, LayerType.PRIMARY, "B", s -> {
					s.setPercentGenus(33f);
					s.setFractionGenus(0.330000013f);
				});
				var spec2 = getTestSpecies(polygonId, LayerType.PRIMARY, "H", s -> {
					s.setPercentGenus(67f);
					s.setFractionGenus(0.670000017f);
				});

				Map<String, TestSpecies> allSpecies = new LinkedHashMap<>();
				allSpecies.put(spec1.getGenus(), spec1);
				allSpecies.put(spec2.getGenus(), spec2);

				layer.setSpecies(allSpecies);

				var result = app.estimatePrimaryBaseArea(layer, bec, 1f, 79.5999985f, 3.13497972f);

				assertThat(result, closeTo(23.1988659f));
			}
		}

		@Test
		void testLowCrownClosure() throws Exception {
			controlMap = TestUtils.loadControlMap();
			var polygonId = new PolygonIdentifier("TestPolygon", 2024);
			try (TestStartApplication app = new TestStartApplication(controlMap, false)) {

				var bec = Utils.getBec("CWH", controlMap);

				var layer = getTestPrimaryLayer(polygonId, l -> {
					l.crownClosure(9f); // Altered this in the debugger while running VDYP7
				}, s -> {
					s.ageTotal(Optional.of(85f));
					s.height(Optional.of(38.2999992f));
					s.siteIndex(Optional.of(28.6000004f));
					s.yearsToBreastHeight(Optional.of(5.4000001f));
					s.siteCurveNumber(Optional.of(34));
					s.siteGenus(Optional.of("H"));
				});

				var spec1 = getTestSpecies(polygonId, LayerType.PRIMARY, "B", s -> {
					s.setPercentGenus(33f);
					s.setFractionGenus(0.330000013f);
				});
				var spec2 = getTestSpecies(polygonId, LayerType.PRIMARY, "H", s -> {
					s.setPercentGenus(67f);
					s.setFractionGenus(0.670000017f);
				});

				Map<String, TestSpecies> allSpecies = new LinkedHashMap<>();
				allSpecies.put(spec1.getGenus(), spec1);
				allSpecies.put(spec2.getGenus(), spec2);

				layer.setSpecies(allSpecies);

				var result = app.estimatePrimaryBaseArea(layer, bec, 1f, 79.5999985f, 3.13497972f);

				assertThat(result, closeTo(37.6110077f));
			}
		}

		@Test
		void testLowResult() throws Exception {
			controlMap = TestUtils.loadControlMap();
			var polygonId = new PolygonIdentifier("TestPolygon", 2024);
			try (TestStartApplication app = new TestStartApplication(controlMap, false)) {

				var bec = Utils.getBec("CWH", controlMap);

				TestLayer layer = getTestPrimaryLayer(polygonId, l -> {
					l.crownClosure(82.8000031f);
				}, s -> {
					s.ageTotal(85f);
					s.height(7f); // Altered this in the debugger while running VDYP7
					s.siteIndex(28.6000004f);
					s.yearsToBreastHeight(5.4000001f);
					s.siteCurveNumber(34);
					s.siteGenus("H");
				});

				var spec1 = getTestSpecies(polygonId, LayerType.PRIMARY, "B", s -> {
					s.setPercentGenus(33f);
					s.setFractionGenus(0.330000013f);
				});
				var spec2 = getTestSpecies(polygonId, LayerType.PRIMARY, "H", s -> {
					s.setPercentGenus(67f);
					s.setFractionGenus(0.670000017f);
				});

				Map<String, TestSpecies> allSpecies = new LinkedHashMap<>();
				allSpecies.put(spec1.getGenus(), spec1);
				allSpecies.put(spec2.getGenus(), spec2);

				layer.setSpecies(allSpecies);

				var ex = assertThrows(
						LowValueException.class,
						() -> app.estimatePrimaryBaseArea(layer, bec, 1f, 79.5999985f, 3.13497972f)
				);

				assertThat(ex, hasProperty("value", is(0f)));
				assertThat(ex, hasProperty("threshold", is(0.05f)));
			}
		}

	}

	@Nested
	class EstimatePrimaryQuadMeanDiameter {

		@Test
		void testSimple() throws Exception {
			var controlMap = TestUtils.loadControlMap();
			var polygonId = new PolygonIdentifier("TestPolygon", 2024);
			try (var app = new TestStartApplication(controlMap, false)) {

				var becLookup = BecDefinitionParser.getBecs(controlMap);
				var bec = becLookup.get("CWH").get();

				var layer = getTestPrimaryLayer(polygonId, l -> {
					l.crownClosure(82.8000031f);
				}, s -> {
					s.ageTotal(85f);
					s.height(38.2999992f);
					s.siteIndex(28.6000004f);
					s.yearsToBreastHeight(5.4000001f);
					s.siteCurveNumber(34);
					s.siteGenus("H");
				});

				var spec1 = getTestSpecies(polygonId, LayerType.PRIMARY, "B", s -> {
					s.setPercentGenus(33f);
					s.setFractionGenus(0.330000013f);
				});
				var spec2 = getTestSpecies(polygonId, LayerType.PRIMARY, "H", s -> {
					s.setPercentGenus(67f);
					s.setFractionGenus(0.670000017f);
				});

				Map<String, TestSpecies> allSpecies = new LinkedHashMap<>();
				allSpecies.put(spec1.getGenus(), spec1);
				allSpecies.put(spec2.getGenus(), spec2);

				layer.setSpecies(allSpecies);

				var result = app.estimatePrimaryQuadMeanDiameter(layer, bec, 79.5999985f, 3.13497972f);

				assertThat(result, closeTo(32.5390053f));
			}
		}

		@Test
		void testHeightLessThanA5() throws Exception {
			var controlMap = TestUtils.loadControlMap();
			var polygonId = new PolygonIdentifier("TestPolygon", 2024);
			try (var app = new TestStartApplication(controlMap, false)) {

				var becLookup = BecDefinitionParser.getBecs(controlMap);
				var bec = becLookup.get("CWH").get();

				var layer = getTestPrimaryLayer(polygonId, l -> {
					l.crownClosure(82.8000031f);
				}, s -> {
					s.ageTotal(85f);
					s.height(4.74730005f);
					s.siteIndex(28.6000004f);
					s.yearsToBreastHeight(5.4000001f);
					s.siteCurveNumber(34);
					s.siteGenus("H");
				});

				var spec1 = getTestSpecies(polygonId, LayerType.PRIMARY, "B", s -> {
					s.setPercentGenus(33f);
					s.setFractionGenus(0.330000013f);
				});
				var spec2 = getTestSpecies(polygonId, LayerType.PRIMARY, "H", s -> {
					s.setPercentGenus(67f);
					s.setFractionGenus(0.670000017f);
				});

				Map<String, TestSpecies> allSpecies = new LinkedHashMap<>();
				allSpecies.put(spec1.getGenus(), spec1);
				allSpecies.put(spec2.getGenus(), spec2);

				layer.setSpecies(allSpecies);

				var result = app.estimatePrimaryQuadMeanDiameter(layer, bec, 79.5999985f, 3.13497972f);

				assertThat(result, closeTo(7.6f));
			}
		}

		@Test
		void testResultLargerThanUpperBound() throws Exception {
			var controlMap = TestUtils.loadControlMap();
			var polygonId = new PolygonIdentifier("TestPolygon", 2024);
			try (var app = new TestStartApplication(controlMap, false)) {

				var becLookup = BecDefinitionParser.getBecs(controlMap);
				var bec = becLookup.get("CWH").get();

				// Tweak the values to produce a very large DQ
				var layer = getTestPrimaryLayer(polygonId, l -> {
					l.crownClosure(82.8000031f);
				}, s -> {
					s.ageTotal(350f);
					s.height(80f);
					s.siteIndex(28.6000004f);
					s.yearsToBreastHeight(5.4000001f);
					s.siteCurveNumber(34);
					s.siteGenus("H");
				});

				var spec1 = getTestSpecies(polygonId, LayerType.PRIMARY, "B", s -> {
					s.setPercentGenus(33f);
					s.setFractionGenus(0.330000013f);
				});
				var spec2 = getTestSpecies(polygonId, LayerType.PRIMARY, "H", s -> {
					s.setPercentGenus(67f);
					s.setFractionGenus(0.670000017f);
				});

				Map<String, TestSpecies> allSpecies = new LinkedHashMap<>();
				allSpecies.put(spec1.getGenus(), spec1);
				allSpecies.put(spec2.getGenus(), spec2);

				layer.setSpecies(allSpecies);

				var result = app.estimatePrimaryQuadMeanDiameter(layer, bec, 350f - 5.4000001f, 3.13497972f);

				assertThat(result, closeTo(61.1f)); // Clamp to the COE043/UPPER_BA_BY_CI_S0_P DQ value for this species
													// and
				// region
			}
		}
	}

	@Nested
	class EstimatePercentForestLand {

		@Test
		void testAlreadySet() throws Exception {
			var controlMap = TestUtils.loadControlMap();
			try (var app = new TestStartApplication(controlMap, false)) {

				var polygon = TestPolygon.build(pb -> {
					pb.polygonIdentifier("TestPolygon", 2024);
					pb.biogeoclimaticZone("IDF");
					pb.forestInventoryZone("Z");
					pb.mode(PolygonMode.START);

					pb.percentAvailable(Optional.of(42f));

				});

				TestLayer primaryLayer = polygon.getLayers().get(LayerType.PRIMARY);
				Optional<TestLayer> veteranLayer = Optional.empty();

				var result = app.estimatePercentForestLand(polygon, veteranLayer, primaryLayer);

				assertThat(result, closeTo(42f));
			}
		}

		@Test
		void testWithoutVeteran() throws Exception {
			var controlMap = TestUtils.loadControlMap();
			try (var app = new TestStartApplication(controlMap, false)) {

				var polygon = TestPolygon.build(pb -> {
					pb.polygonIdentifier("TestPolygon", 2024);
					pb.biogeoclimaticZone("IDF");
					pb.forestInventoryZone("Z");
					pb.mode(PolygonMode.START);

					pb.percentAvailable(Optional.empty());

					pb.addLayer(lb -> {
						lb.layerType(LayerType.PRIMARY);

						lb.crownClosure(60f);

						lb.addSite(ib -> {
							ib.siteGenus("L");
							ib.ageTotal(60f);
							ib.height(15f);
							ib.siteIndex(5f);
							ib.yearsToBreastHeight(8.5f);
						});

						lb.addSpecies(sb -> {
							sb.genus("L");
							sb.percentGenus(10);
						});
						lb.addSpecies(sb -> {
							sb.genus("PL");
							sb.percentGenus(90);
						});
					});
				});

				TestLayer primaryLayer = polygon.getLayers().get(LayerType.PRIMARY);
				Optional<TestLayer> veteranLayer = Optional.empty();

				var result = app.estimatePercentForestLand(polygon, veteranLayer, primaryLayer);

				assertThat(result, closeTo(90f));
			}
		}

		@Test
		void testWithVeteran() throws Exception {
			var controlMap = TestUtils.loadControlMap();
			try (var app = new TestStartApplication(controlMap, false) {

				@Override
				public VdypApplicationIdentifier getId() {
					return VdypApplicationIdentifier.FIP_START;
				}

			}) {

				var polygon = TestPolygon.build(pb -> {
					pb.polygonIdentifier("TestPolygon", 2024);
					pb.biogeoclimaticZone("CWH");
					pb.forestInventoryZone("A");
					pb.mode(PolygonMode.START);

					pb.percentAvailable(Optional.empty());

					pb.addLayer(lb -> {
						lb.layerType(LayerType.PRIMARY);

						lb.crownClosure(82.8f);

						lb.addSite(ib -> {
							ib.siteGenus("H");
							ib.ageTotal(45f);
							ib.height(24.3f);
							ib.siteIndex(28.7f);
							ib.yearsToBreastHeight(7.1f);
						});

						lb.addSpecies(sb -> {
							sb.genus("B");
							sb.percentGenus(15);
						});
						lb.addSpecies(sb -> {
							sb.genus("D");
							sb.percentGenus(7);
						});
						lb.addSpecies(sb -> {
							sb.genus("H");
							sb.percentGenus(77);
						});
						lb.addSpecies(sb -> {
							sb.genus("S");
							sb.percentGenus(1);
						});
					});
					pb.addLayer(lb -> {
						lb.layerType(LayerType.VETERAN);

						lb.crownClosure(4f);

						lb.addSite(ib -> {
							ib.siteGenus("H");
							ib.ageTotal(105f);
							ib.height(26.2f);
							ib.siteIndex(16.7f);
							ib.yearsToBreastHeight(7.1f);
						});

					});
				});

				TestLayer primaryLayer = polygon.getLayers().get(LayerType.PRIMARY);
				Optional<TestLayer> veteranLayer = Optional.of(polygon.getLayers().get(LayerType.VETERAN));

				var result = app.estimatePercentForestLand(polygon, veteranLayer, primaryLayer);

				assertThat(result, closeTo(98f));
			}
		}
	}

	@Nested
	class GetPercentTotal {

		@ParameterizedTest
		@CsvSource(
				value = { //
						"'A:100.0', 100.0", //
						"'A:99.991', 99.991", //
						"'A:100.009', 100.009", //
						"'A:75.0 B:25.0', 100.0", //
						"'A:75.0 B:25.009', 100.009", //
						"'A:75.0 B:24.991', 99.991" //
				}
		)
		void testPass(String dist, float expected) throws Exception {
			var controlMap = TestUtils.loadControlMap();
			try (var app = new TestStartApplication(controlMap, false)) {

				var layer = TestLayer.build(lb -> {
					lb.polygonIdentifier("TestPolygon", 2024);
					lb.layerType(LayerType.PRIMARY);
					lb.crownClosure(90f);
					for (String s : dist.split(" ")) {
						var parts = s.split(":");
						lb.addSpecies(sb -> {
							sb.genus(parts[0]);
							sb.percentGenus(Float.valueOf(parts[1]));
						});
					}
				});

				var result = app.getPercentTotal(layer);

				assertThat(result, closeTo(expected));
			}
		}

		@ParameterizedTest
		@ValueSource(
				strings = { //
						"A:99.989", //
						"A:100.011", //
						"A:75.0 B:25.011", //
						"A:75.0 B:24.989" //
				}
		)
		void testFail(String dist) throws Exception {
			var controlMap = TestUtils.loadControlMap();
			try (var app = new TestStartApplication(controlMap, false)) {

				var layer = TestLayer.build(lb -> {
					lb.polygonIdentifier("TestPolygon", 2024);
					lb.layerType(LayerType.PRIMARY);
					lb.crownClosure(90f);
					for (String s : dist.split(" ")) {
						var parts = s.split(":");
						lb.addSpecies(sb -> {
							sb.genus(parts[0]);
							sb.percentGenus(Float.valueOf(parts[1]));
						});
					}
				});

				assertThrows(StandProcessingException.class, () -> app.getPercentTotal(layer));
			}
		}
	}

	@Nested
	class UtilityMethods {

		@Test
		void testRequireLayer() throws ProcessingException, IOException {
			var mockControl = EasyMock.createControl();
			BaseVdypPolygon poly = mockControl.createMock(BaseVdypPolygon.class);
			BaseVdypLayer layer = mockControl.createMock(BaseVdypLayer.class);
			EasyMock.expect(poly.getLayers()).andStubReturn(Collections.singletonMap(LayerType.PRIMARY, layer));
			EasyMock.expect(poly.getPolygonIdentifier()).andStubReturn(new PolygonIdentifier("TestPoly", 2024));

			try (VdypStartApplication app = getTestUnit(mockControl)) {
				mockControl.replay();

				var result = app.requireLayer(poly, LayerType.PRIMARY);
				assertThat(result, is(layer));
				var ex = assertThrows(StandProcessingException.class, () -> app.requireLayer(poly, LayerType.VETERAN));
				assertThat(
						ex,
						hasProperty(
								"message",
								is(
										"Polygon \"TestPoly             2024\" has no VETERAN layer, or that layer has non-positive height or crown closure."
								)
						)
				);

			}

		}

		@Test
		void testGetCoeForSpecies() throws Exception {
			var controlMap = TestUtils.loadControlMap();
			try (var app = new TestStartApplication(controlMap, false)) {
				var species = TestSpecies.build(sb -> {
					sb.polygonIdentifier("TestPolygon", 2024);
					sb.layerType(LayerType.PRIMARY);
					sb.genus("MB");

					sb.percentGenus(90);
				});
				var result = app.getCoeForSpecies(species, ControlKey.SMALL_COMP_BA);

				assertThat(result, coe(1, 0.1183f, 0f, 0.03896f, -0.04348f)); // Small Component Base Area coefficients
																				// for
																				// genus MB
			}

		}

		@Test
		void testWeightedCoefficientSum() {
			var result = VdypStartApplication
					.weightedCoefficientSum(List.of(2, 4, 6), 6, 1, List.of("A", "B", "C", "D"), s -> {
						switch (s) {
						case "A":
							return 0.2f;
						case "B":
							return 0.4f;
						case "C":
							return 0.1f;
						default:
							return 0.3f;
						}
					}, s -> {
						switch (s) {
						case "A":
							return new Coefficients(new float[] { 2.0f, 10.0f, 7.0f, 0.0f, 9.0f, 0.0f }, 1);
						case "B":
							return new Coefficients(new float[] { 2.0f, 0.0f, 7.0f, 6.0f, 9.0f, 4.0f }, 1);
						case "C":
							return new Coefficients(new float[] { 2.0f, 3.0f, 7.0f, -3.0f, 9.0f, 3.0f }, 1);
						default:
							return new Coefficients(new float[] { 2.0f, 1.0f, 7.0f, 1.0f, 9.0f, 1.0f }, 1);
						}
					});

			assertThat(
					result,
					coe(
							1, 2f, 2f + 0.3f + 0.3f, 7f, 6f * 0.4f - 3f * 0.1f + 0.3f * 1f, 9f,
							4f * 0.4f + 3f * 0.1f + 0.3f * 1f
					)
			);
		}

	}

	TestPolygon getTestPolygon(PolygonIdentifier polygonId, Consumer<TestPolygon.Builder> mutator) {
		return TestPolygon.build(builder -> {
			builder.polygonIdentifier(polygonId);
			builder.forestInventoryZone("0");
			builder.biogeoclimaticZone("BG");
			builder.mode(PolygonMode.START);
			mutator.accept(builder);
		});
	};

	TestLayer getTestPrimaryLayer(
			PolygonIdentifier polygonId, Consumer<TestLayer.Builder> mutator, Consumer<TestSite.Builder> siteMutator
	) {
		var result = TestLayer.build(builder -> {
			builder.polygonIdentifier(polygonId);
			builder.layerType(LayerType.PRIMARY);
			builder.addSite(siteBuilder -> {
				siteBuilder.ageTotal(8f);
				siteBuilder.yearsToBreastHeight(7f);
				siteBuilder.height(6f);
				siteBuilder.siteIndex(5f);
				siteBuilder.siteGenus("B");
				siteMutator.accept(siteBuilder);
			});

			builder.crownClosure(0.9f);
			mutator.accept(builder);
		});

		return result;
	};

	TestLayer getTestVeteranLayer(
			PolygonIdentifier polygonId, Consumer<TestLayer.Builder> mutator, Consumer<TestSite.Builder> siteMutator
	) {
		var result = TestLayer.build(builder -> {
			builder.polygonIdentifier(polygonId);
			builder.layerType(LayerType.VETERAN);

			builder.addSite(siteBuilder -> {
				siteBuilder.ageTotal(8f);
				siteBuilder.yearsToBreastHeight(7f);
				siteBuilder.height(6f);
				siteBuilder.siteIndex(5f);
				siteBuilder.siteGenus("B");
				siteMutator.accept(siteBuilder);
			});

			builder.crownClosure(0.9f);
			mutator.accept(builder);
		});

		return result;
	};

	TestSpecies getTestSpecies(PolygonIdentifier polygonId, LayerType layer, Consumer<TestSpecies> mutator) {
		return getTestSpecies(polygonId, layer, "B", mutator);
	};

	TestSpecies getTestSpecies(
			PolygonIdentifier polygonId, LayerType layer, String genusId, Consumer<TestSpecies> mutator
	) {
		var result = TestSpecies.build(builder -> {
			builder.polygonIdentifier(polygonId);
			builder.layerType(layer);
			builder.genus(genusId);
			builder.percentGenus(100.0f);
			builder.addSpecies(genusId, 100f);
		});
		mutator.accept(result);
		return result;
	};

}
