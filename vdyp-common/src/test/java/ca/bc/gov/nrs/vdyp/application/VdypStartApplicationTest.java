package ca.bc.gov.nrs.vdyp.application;

import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.causedBy;
import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.closeTo;
import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.coe;
import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.utilization;
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
import java.util.Arrays;
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

import ca.bc.gov.nrs.vdyp.application.test.TestLayer;
import ca.bc.gov.nrs.vdyp.application.test.TestPolygon;
import ca.bc.gov.nrs.vdyp.application.test.TestSite;
import ca.bc.gov.nrs.vdyp.application.test.TestSpecies;
import ca.bc.gov.nrs.vdyp.application.test.TestStartApplication;
import ca.bc.gov.nrs.vdyp.common.ControlKey;
import ca.bc.gov.nrs.vdyp.common.Utils;
import ca.bc.gov.nrs.vdyp.io.parse.coe.BecDefinitionParser;
import ca.bc.gov.nrs.vdyp.io.parse.common.ResourceParseException;
import ca.bc.gov.nrs.vdyp.io.parse.streaming.StreamingParser;
import ca.bc.gov.nrs.vdyp.io.parse.streaming.StreamingParserFactory;
import ca.bc.gov.nrs.vdyp.model.BaseVdypLayer;
import ca.bc.gov.nrs.vdyp.model.BaseVdypPolygon;
import ca.bc.gov.nrs.vdyp.model.BaseVdypSpecies;
import ca.bc.gov.nrs.vdyp.model.BecDefinition;
import ca.bc.gov.nrs.vdyp.model.Coefficients;
import ca.bc.gov.nrs.vdyp.model.CompatibilityVariableMode;
import ca.bc.gov.nrs.vdyp.model.LayerType;
import ca.bc.gov.nrs.vdyp.model.MatrixMap2Impl;
import ca.bc.gov.nrs.vdyp.model.PolygonIdentifier;
import ca.bc.gov.nrs.vdyp.model.PolygonMode;
import ca.bc.gov.nrs.vdyp.model.Region;
import ca.bc.gov.nrs.vdyp.model.VdypLayer;
import ca.bc.gov.nrs.vdyp.model.VdypSpecies;
import ca.bc.gov.nrs.vdyp.model.VolumeComputeMode;
import ca.bc.gov.nrs.vdyp.test.MockFileResolver;
import ca.bc.gov.nrs.vdyp.test.TestUtils;
import ca.bc.gov.nrs.vdyp.test.VdypMatchers;

@SuppressWarnings({ "unchecked", "rawtypes" })
class VdypStartApplicationTest {

	private Map<String, Object> controlMap = new HashMap<>();

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
	class SmallComponents {
		@Test
		void testEstimate() throws ProcessingException {
			var controlMap = TestUtils.loadControlMap();
			try (var app = new TestStartApplication(controlMap, false)) {
				ApplicationTestUtils.setControlMap(app, controlMap);

				var fPoly = TestPolygon.build(builder -> {
					builder.polygonIdentifier("Test", 2024);
					builder.forestInventoryZone("A");
					builder.biogeoclimaticZone("CWH");
					builder.percentAvailable(Optional.of(100f));
					// builder.yieldFactor(1f);
				});
				VdypLayer layer = VdypLayer.build(builder -> {
					builder.polygonIdentifier("Test", 2024);
					builder.layerType(LayerType.PRIMARY);
					builder.addSite(siteBuilder -> {
						siteBuilder.ageTotal(55f);
						siteBuilder.yearsToBreastHeight(1f);
						siteBuilder.height(31f);
						siteBuilder.siteGenus("H");
					});
				});

				layer.getLoreyHeightByUtilization().setCoe(VdypStartApplication.UTIL_ALL, 31.3307209f);
				layer.getBaseAreaByUtilization().setCoe(VdypStartApplication.UTIL_ALL, 44.6249847f);
				layer.getTreesPerHectareByUtilization().setCoe(VdypStartApplication.UTIL_ALL, 620.484802f);
				layer.getQuadraticMeanDiameterByUtilization().setCoe(VdypStartApplication.UTIL_ALL, 30.2606697f);
				layer.getWholeStemVolumeByUtilization().setCoe(VdypStartApplication.UTIL_ALL, 635.659668f);

				var spec1 = VdypSpecies.build(layer, builder -> {
					builder.genus("B");
					builder.percentGenus(20f);
					builder.volumeGroup(-1);
					builder.decayGroup(-1);
					builder.breakageGroup(-1);
				});
				spec1.getLoreyHeightByUtilization().setCoe(VdypStartApplication.UTIL_ALL, 38.6004372f);
				spec1.getBaseAreaByUtilization().setCoe(VdypStartApplication.UTIL_ALL, 0.397305071f);
				spec1.getTreesPerHectareByUtilization().setCoe(VdypStartApplication.UTIL_ALL, 5.04602766f);
				spec1.getQuadraticMeanDiameterByUtilization().setCoe(VdypStartApplication.UTIL_ALL, 31.6622887f);
				spec1.getWholeStemVolumeByUtilization().setCoe(VdypStartApplication.UTIL_ALL, 635.659668f);
				var spec2 = VdypSpecies.build(layer, builder -> {
					builder.genus("C");
					builder.percentGenus(20f);
					builder.volumeGroup(-1);
					builder.decayGroup(-1);
					builder.breakageGroup(-1);
				});
				spec2.getLoreyHeightByUtilization().setCoe(VdypStartApplication.UTIL_ALL, 22.8001652f);
				spec2.getBaseAreaByUtilization().setCoe(VdypStartApplication.UTIL_ALL, 5.08774281f);
				spec2.getTreesPerHectareByUtilization().setCoe(VdypStartApplication.UTIL_ALL, 92.4298019f);
				spec2.getQuadraticMeanDiameterByUtilization().setCoe(VdypStartApplication.UTIL_ALL, 26.4735165f);
				spec2.getWholeStemVolumeByUtilization().setCoe(VdypStartApplication.UTIL_ALL, 6.35662031f);
				var spec3 = VdypSpecies.build(layer, builder -> {
					builder.genus("D");
					builder.percentGenus(20f);
					builder.volumeGroup(-1);
					builder.decayGroup(-1);
					builder.breakageGroup(-1);
				});
				spec3.getLoreyHeightByUtilization().setCoe(VdypStartApplication.UTIL_ALL, 33.5375252f);
				spec3.getBaseAreaByUtilization().setCoe(VdypStartApplication.UTIL_ALL, 29.5411568f);
				spec3.getTreesPerHectareByUtilization().setCoe(VdypStartApplication.UTIL_ALL, 326.800781f);
				spec3.getQuadraticMeanDiameterByUtilization().setCoe(VdypStartApplication.UTIL_ALL, 33.9255791f);
				spec3.getWholeStemVolumeByUtilization().setCoe(VdypStartApplication.UTIL_ALL, 44.496151f);
				var spec4 = VdypSpecies.build(layer, builder -> {
					builder.genus("H");
					builder.percentGenus(20f);
					builder.volumeGroup(-1);
					builder.decayGroup(-1);
					builder.breakageGroup(-1);
				});
				spec4.getLoreyHeightByUtilization().setCoe(VdypStartApplication.UTIL_ALL, 24.3451157f);
				spec4.getBaseAreaByUtilization().setCoe(VdypStartApplication.UTIL_ALL, 5.50214148f);
				spec4.getTreesPerHectareByUtilization().setCoe(VdypStartApplication.UTIL_ALL, 152.482513f);
				spec4.getQuadraticMeanDiameterByUtilization().setCoe(VdypStartApplication.UTIL_ALL, 21.4343796f);
				spec4.getWholeStemVolumeByUtilization().setCoe(VdypStartApplication.UTIL_ALL, 470.388489f);
				var spec5 = VdypSpecies.build(layer, builder -> {
					builder.genus("S");
					builder.percentGenus(20f);
					builder.volumeGroup(-1);
					builder.decayGroup(-1);
					builder.breakageGroup(-1);
				});
				spec5.getLoreyHeightByUtilization().setCoe(VdypStartApplication.UTIL_ALL, 34.6888771f);
				spec5.getBaseAreaByUtilization().setCoe(VdypStartApplication.UTIL_ALL, 4.0966382f);
				spec5.getTreesPerHectareByUtilization().setCoe(VdypStartApplication.UTIL_ALL, 43.7256737f);
				spec5.getQuadraticMeanDiameterByUtilization().setCoe(VdypStartApplication.UTIL_ALL, 34.5382729f);
				spec5.getWholeStemVolumeByUtilization().setCoe(VdypStartApplication.UTIL_ALL, 57.2091446f);

				layer.setSpecies(Arrays.asList(spec1, spec2, spec3, spec4, spec5));

				app.estimateSmallComponents(fPoly, layer);

				assertThat(
						layer.getLoreyHeightByUtilization().getCoe(VdypStartApplication.UTIL_SMALL),
						closeTo(7.14446497f)
				);
				assertThat(
						spec1.getLoreyHeightByUtilization().getCoe(VdypStartApplication.UTIL_SMALL),
						closeTo(8.39441967f)
				);
				assertThat(
						spec2.getLoreyHeightByUtilization().getCoe(VdypStartApplication.UTIL_SMALL),
						closeTo(6.61517191f)
				);
				assertThat(
						spec3.getLoreyHeightByUtilization().getCoe(VdypStartApplication.UTIL_SMALL),
						closeTo(10.8831682f)
				);
				assertThat(
						spec4.getLoreyHeightByUtilization().getCoe(VdypStartApplication.UTIL_SMALL),
						closeTo(7.93716192f)
				);
				assertThat(
						spec5.getLoreyHeightByUtilization().getCoe(VdypStartApplication.UTIL_SMALL),
						closeTo(8.63455391f)
				);

				assertThat(
						layer.getBaseAreaByUtilization().getCoe(VdypStartApplication.UTIL_SMALL), closeTo(0.0153773092f)
				);
				assertThat(spec1.getBaseAreaByUtilization().getCoe(VdypStartApplication.UTIL_SMALL), closeTo(0f));
				assertThat(
						spec2.getBaseAreaByUtilization().getCoe(VdypStartApplication.UTIL_SMALL), closeTo(0.0131671466f)
				);
				assertThat(
						spec3.getBaseAreaByUtilization().getCoe(VdypStartApplication.UTIL_SMALL),
						closeTo(0.00163476227f)
				);
				assertThat(spec4.getBaseAreaByUtilization().getCoe(VdypStartApplication.UTIL_SMALL), closeTo(0f));
				assertThat(
						spec5.getBaseAreaByUtilization().getCoe(VdypStartApplication.UTIL_SMALL),
						closeTo(0.000575399841f)
				);

				assertThat(
						layer.getTreesPerHectareByUtilization().getCoe(VdypStartApplication.UTIL_SMALL),
						closeTo(5.34804487f)
				);
				assertThat(
						spec1.getTreesPerHectareByUtilization().getCoe(VdypStartApplication.UTIL_SMALL), closeTo(0f)
				);
				assertThat(
						spec2.getTreesPerHectareByUtilization().getCoe(VdypStartApplication.UTIL_SMALL),
						closeTo(4.67143154f)
				);
				assertThat(
						spec3.getTreesPerHectareByUtilization().getCoe(VdypStartApplication.UTIL_SMALL),
						closeTo(0.498754263f)
				);
				assertThat(
						spec4.getTreesPerHectareByUtilization().getCoe(VdypStartApplication.UTIL_SMALL), closeTo(0f)
				);
				assertThat(
						spec5.getTreesPerHectareByUtilization().getCoe(VdypStartApplication.UTIL_SMALL),
						closeTo(0.17785944f)
				);

				assertThat(
						layer.getQuadraticMeanDiameterByUtilization().getCoe(VdypStartApplication.UTIL_SMALL),
						closeTo(6.05059004f)
				);
				assertThat(
						spec1.getQuadraticMeanDiameterByUtilization().getCoe(VdypStartApplication.UTIL_SMALL),
						closeTo(6.13586617f)
				);
				assertThat(
						spec2.getQuadraticMeanDiameterByUtilization().getCoe(VdypStartApplication.UTIL_SMALL),
						closeTo(5.99067688f)
				);
				assertThat(
						spec3.getQuadraticMeanDiameterByUtilization().getCoe(VdypStartApplication.UTIL_SMALL),
						closeTo(6.46009731f)
				);
				assertThat(
						spec4.getQuadraticMeanDiameterByUtilization().getCoe(VdypStartApplication.UTIL_SMALL),
						closeTo(6.03505516f)
				);
				assertThat(
						spec5.getQuadraticMeanDiameterByUtilization().getCoe(VdypStartApplication.UTIL_SMALL),
						closeTo(6.41802597f)
				);

				assertThat(
						layer.getWholeStemVolumeByUtilization().getCoe(VdypStartApplication.UTIL_SMALL),
						closeTo(0.0666879341f)
				);
				assertThat(
						spec1.getWholeStemVolumeByUtilization().getCoe(VdypStartApplication.UTIL_SMALL), closeTo(0f)
				);
				assertThat(
						spec2.getWholeStemVolumeByUtilization().getCoe(VdypStartApplication.UTIL_SMALL),
						closeTo(0.0556972362f)
				);
				assertThat(
						spec3.getWholeStemVolumeByUtilization().getCoe(VdypStartApplication.UTIL_SMALL),
						closeTo(0.0085867513f)
				);
				assertThat(
						spec4.getWholeStemVolumeByUtilization().getCoe(VdypStartApplication.UTIL_SMALL), closeTo(0f)
				);
				assertThat(
						spec5.getWholeStemVolumeByUtilization().getCoe(VdypStartApplication.UTIL_SMALL),
						closeTo(0.00240394124f)
				);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Nested
	class ReconcileComponents {
		@Test
		void testReconcileComponentsMode1() throws ProcessingException {
			var controlMap = TestUtils.loadControlMap();
			try (var app = new TestStartApplication(controlMap, false)) {

				var dq = Utils.utilizationVector();
				var ba = Utils.utilizationVector();
				var tph = Utils.utilizationVector();

				// '082E004 615 1988' with component BA re-ordered from smallest to largest to
				// force mode 1.

				dq.setCoe(0, 13.4943399f);
				dq.setCoe(1, 10.2766619f);
				dq.setCoe(2, 14.67033f);
				dq.setCoe(3, 19.4037666f);
				dq.setCoe(4, 25.719244f);

				ba.setCoe(0, 2.20898318f);
				ba.setCoe(1, 0.220842764f);
				ba.setCoe(2, 0.433804274f);
				ba.setCoe(3, 0.691931725f);
				ba.setCoe(4, 0.862404406f);

				tph.setCoe(0, 154.454025f);
				tph.setCoe(1, 83.4198151f);
				tph.setCoe(2, 51.0201035f);
				tph.setCoe(3, 14.6700592f);
				tph.setCoe(4, 4.25086117f);

				app.reconcileComponents(ba, tph, dq);

				assertThat(ba, VdypMatchers.utilization(0f, 2.20898318f, 0.220842764f, 0.546404183f, 1.44173622f, 0f));
				assertThat(tph, VdypMatchers.utilization(0f, 154.454025f, 49.988575f, 44.5250206f, 59.9404259f, 0f));
				assertThat(dq, VdypMatchers.utilization(0f, 13.4943399f, 7.5f, 12.5f, 17.5f, 22.5f));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		@Test
		void testReconcileComponentsMode2() throws ProcessingException {
			var controlMap = TestUtils.loadControlMap();
			try (var app = new TestStartApplication(controlMap, false)) {

				var dq = Utils.utilizationVector();
				var ba = Utils.utilizationVector();
				var tph = Utils.utilizationVector();
				dq.setCoe(0, 31.6622887f);
				dq.setCoe(1, 10.0594692f);
				dq.setCoe(2, 14.966774f);
				dq.setCoe(3, 19.9454956f);
				dq.setCoe(4, 46.1699982f);

				ba.setCoe(0, 0.397305071f);
				ba.setCoe(1, 0.00485289097f);
				ba.setCoe(2, 0.0131751001f);
				ba.setCoe(3, 0.0221586525f);
				ba.setCoe(4, 0.357118428f);

				tph.setCoe(0, 5.04602766f);
				tph.setCoe(1, 0.61060524f);
				tph.setCoe(2, 0.748872101f);
				tph.setCoe(3, 0.709191978f);
				tph.setCoe(4, 2.13305807f);

				app.reconcileComponents(ba, tph, dq);

				assertThat(
						ba,
						VdypMatchers.utilization(
								0f, 0.397305071f, 0.00485289097f, 0.0131751001f, 0.0221586525f, 0.357118428f
						)
				);
				assertThat(
						tph,
						VdypMatchers.utilization(0f, 5.04602766f, 0.733301044f, 0.899351299f, 0.851697803f, 2.56167722f)
				);
				assertThat(
						dq,
						VdypMatchers.utilization(0f, 31.6622887f, 9.17939758f, 13.6573782f, 18.2005272f, 42.1307297f)
				);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		@Test
		void testReconcileComponentsMode3() throws ProcessingException {
			var controlMap = TestUtils.loadControlMap();
			try (var app = new TestStartApplication(controlMap, false)) {

				var dq = Utils.utilizationVector();
				var ba = Utils.utilizationVector();
				var tph = Utils.utilizationVector();

				// Set of inputs that cause mode 2 to fail over into mode 3

				dq.setCoe(0, 12.51f);
				dq.setCoe(1, 12.4f);
				dq.setCoe(2, 0f);
				dq.setCoe(3, 0f);
				dq.setCoe(4, 0f);

				ba.setCoe(0, 2.20898318f);
				ba.setCoe(1, 2.20898318f);
				ba.setCoe(2, 0f);
				ba.setCoe(3, 0f);
				ba.setCoe(4, 0f);

				tph.setCoe(0, 179.71648f);
				tph.setCoe(1, 182.91916f);
				tph.setCoe(2, 0f);
				tph.setCoe(3, 0f);
				tph.setCoe(4, 0f);

				app.reconcileComponents(ba, tph, dq);

				assertThat(ba, VdypMatchers.utilization(0f, 2.20898318f, 0f, 2.20898318f, 0f, 0f));
				assertThat(tph, VdypMatchers.utilization(0f, 179.71648f, 0f, 179.71648f, 0f, 0f));
				assertThat(dq, VdypMatchers.utilization(0f, 12.51f, 10, 12.51f, 20f, 25f));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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

	@Test
	void testComputeUtilizationComponentsPrimaryByUtilNoCV() throws ProcessingException {
		var controlMap = TestUtils.loadControlMap();
		try (var app = new TestStartApplication(controlMap, false)) {

			var bec = BecDefinitionParser.getBecs(controlMap).get("IDF").get();

			var layer = VdypLayer.build(builder -> {
				builder.polygonIdentifier("Test", 2024);
				builder.layerType(LayerType.PRIMARY);
				builder.addSite(siteBuilder -> {
					siteBuilder.ageTotal(55f);
					siteBuilder.yearsToBreastHeight(3.5f);
					siteBuilder.height(20f);
					siteBuilder.siteGenus("H");
				});

			});

			layer.getLoreyHeightByUtilization().setCoe(VdypStartApplication.UTIL_ALL, 13.0660105f);
			layer.getBaseAreaByUtilization().setCoe(VdypStartApplication.UTIL_ALL, 19.9786701f);
			layer.getTreesPerHectareByUtilization().setCoe(VdypStartApplication.UTIL_ALL, 1485.8208f);
			layer.getQuadraticMeanDiameterByUtilization().setCoe(VdypStartApplication.UTIL_ALL, 13.0844402f);
			layer.getWholeStemVolumeByUtilization().setCoe(VdypStartApplication.UTIL_ALL, 117.993797f);

			layer.getLoreyHeightByUtilization().setCoe(VdypStartApplication.UTIL_SMALL, 7.83768177f);
			layer.getBaseAreaByUtilization().setCoe(VdypStartApplication.UTIL_SMALL, 0.0286490358f);
			layer.getTreesPerHectareByUtilization().setCoe(VdypStartApplication.UTIL_SMALL, 9.29024601f);
			layer.getQuadraticMeanDiameterByUtilization().setCoe(VdypStartApplication.UTIL_SMALL, 6.26608753f);
			layer.getWholeStemVolumeByUtilization().setCoe(VdypStartApplication.UTIL_SMALL, 0.107688069f);

			var spec1 = VdypSpecies.build(layer, builder -> {
				builder.genus("L");
				builder.percentGenus(11.0567074f);
				builder.volumeGroup(46);
				builder.decayGroup(38);
				builder.breakageGroup(20);
			});

			spec1.getLoreyHeightByUtilization().setCoe(VdypStartApplication.UTIL_ALL, 14.2597857f);
			spec1.getBaseAreaByUtilization().setCoe(VdypStartApplication.UTIL_ALL, 2.20898318f);
			spec1.getTreesPerHectareByUtilization().setCoe(VdypStartApplication.UTIL_ALL, 154.454025f);
			spec1.getQuadraticMeanDiameterByUtilization().setCoe(VdypStartApplication.UTIL_ALL, 13.4943399f);
			spec1.getWholeStemVolumeByUtilization().setCoe(VdypStartApplication.UTIL_ALL, 11.7993851f);

			spec1.getLoreyHeightByUtilization().setCoe(VdypStartApplication.UTIL_SMALL, 7.86393309f);
			spec1.getBaseAreaByUtilization().setCoe(VdypStartApplication.UTIL_SMALL, 0.012636207f);
			spec1.getTreesPerHectareByUtilization().setCoe(VdypStartApplication.UTIL_SMALL, 3.68722916f);
			spec1.getQuadraticMeanDiameterByUtilization().setCoe(VdypStartApplication.UTIL_SMALL, 6.60561657f);
			spec1.getWholeStemVolumeByUtilization().setCoe(VdypStartApplication.UTIL_SMALL, 0.0411359742f);

			var spec2 = VdypSpecies.build(layer, builder -> {
				builder.genus("PL");
				builder.percentGenus(88.9432907f);
				builder.volumeGroup(54);
				builder.decayGroup(42);
				builder.breakageGroup(24);
			});

			spec2.getLoreyHeightByUtilization().setCoe(VdypStartApplication.UTIL_ALL, 12.9176102f);
			spec2.getBaseAreaByUtilization().setCoe(VdypStartApplication.UTIL_ALL, 17.7696857f);
			spec2.getTreesPerHectareByUtilization().setCoe(VdypStartApplication.UTIL_ALL, 1331.36682f);
			spec2.getQuadraticMeanDiameterByUtilization().setCoe(VdypStartApplication.UTIL_ALL, 13.0360518f);
			spec2.getWholeStemVolumeByUtilization().setCoe(VdypStartApplication.UTIL_ALL, 106.194412f);

			spec2.getLoreyHeightByUtilization().setCoe(VdypStartApplication.UTIL_SMALL, 7.81696558f);
			spec2.getBaseAreaByUtilization().setCoe(VdypStartApplication.UTIL_SMALL, 0.0160128288f);
			spec2.getTreesPerHectareByUtilization().setCoe(VdypStartApplication.UTIL_SMALL, 5.60301685f);
			spec2.getQuadraticMeanDiameterByUtilization().setCoe(VdypStartApplication.UTIL_SMALL, 6.03223324f);
			spec2.getWholeStemVolumeByUtilization().setCoe(VdypStartApplication.UTIL_SMALL, 0.0665520951f);

			layer.setSpecies(Arrays.asList(spec1, spec2));

			app.computeUtilizationComponentsPrimary(
					bec, layer, VolumeComputeMode.BY_UTIL, CompatibilityVariableMode.NONE
			);

			// TODO test percent for each species

			assertThat(
					layer.getLoreyHeightByUtilization(), coe(-1, contains(closeTo(7.83768177f), closeTo(13.0660114f)))
			);
			assertThat(
					spec1.getLoreyHeightByUtilization(), coe(-1, contains(closeTo(7.86393309f), closeTo(14.2597857f)))
			);
			assertThat(
					spec2.getLoreyHeightByUtilization(), coe(-1, contains(closeTo(7.81696558f), closeTo(12.9176102f)))
			);

			assertThat(
					spec1.getBaseAreaByUtilization(),
					utilization(0.012636207f, 2.20898318f, 0.691931725f, 0.862404406f, 0.433804274f, 0.220842764f)
			);
			assertThat(
					spec2.getBaseAreaByUtilization(),
					utilization(0.0160128288f, 17.7696857f, 6.10537529f, 7.68449211f, 3.20196891f, 0.777849257f)
			);
			assertThat(
					layer.getBaseAreaByUtilization(),
					utilization(0.0286490358f, 19.9786682f, 6.79730701f, 8.54689693f, 3.63577318f, 0.998692036f)
			);

			assertThat(
					spec1.getTreesPerHectareByUtilization(),
					utilization(3.68722916f, 154.454025f, 84.0144501f, 51.3837852f, 14.7746315f, 4.28116179f)
			);
			assertThat(
					spec2.getTreesPerHectareByUtilization(),
					utilization(5.60301685f, 1331.36682f, 750.238892f, 457.704498f, 108.785675f, 14.6378069f)
			);
			assertThat(
					layer.getTreesPerHectareByUtilization(),
					utilization(9.29024601f, 1485.8208f, 834.253357f, 509.088287f, 123.560303f, 18.9189682f)
			);

			assertThat(
					spec1.getQuadraticMeanDiameterByUtilization(),
					utilization(6.60561657f, 13.4943399f, 10.2402296f, 14.6183214f, 19.3349762f, 25.6280651f)
			);
			assertThat(
					spec2.getQuadraticMeanDiameterByUtilization(),
					utilization(6.03223324f, 13.0360518f, 10.1791487f, 14.6207638f, 19.3587704f, 26.0114632f)
			);
			assertThat(
					layer.getQuadraticMeanDiameterByUtilization(),
					utilization(6.26608753f, 13.0844393f, 10.1853161f, 14.6205177f, 19.3559265f, 25.9252014f)
			);

			assertThat(
					spec1.getWholeStemVolumeByUtilization(),
					utilization(0.0411359742f, 11.7993851f, 3.13278913f, 4.76524019f, 2.63645673f, 1.26489878f)
			);
			assertThat(
					spec2.getWholeStemVolumeByUtilization(),
					utilization(0.0665520951f, 106.194412f, 30.2351704f, 47.6655998f, 22.5931034f, 5.70053911f)
			);
			assertThat(
					layer.getWholeStemVolumeByUtilization(),
					utilization(0.107688069f, 117.993797f, 33.3679581f, 52.4308395f, 25.2295609f, 6.96543789f)
			);

			assertThat(
					spec1.getCloseUtilizationVolumeByUtilization(),
					utilization(0f, 6.41845179f, 0.0353721268f, 2.99654913f, 2.23212862f, 1.1544019f)
			);
			assertThat(
					spec2.getCloseUtilizationVolumeByUtilization(),
					utilization(0f, 61.335495f, 2.38199472f, 33.878521f, 19.783432f, 5.29154539f)
			);
			assertThat(
					layer.getCloseUtilizationVolumeByUtilization(),
					utilization(0f, 67.7539444f, 2.41736674f, 36.8750687f, 22.0155602f, 6.44594717f)
			);

			assertThat(
					spec1.getCloseUtilizationVolumeNetOfDecayByUtilization(),
					utilization(0f, 6.26433992f, 0.0349677317f, 2.95546484f, 2.18952441f, 1.08438313f)
			);
			assertThat(
					spec2.getCloseUtilizationVolumeNetOfDecayByUtilization(),
					utilization(0f, 60.8021164f, 2.36405492f, 33.6109734f, 19.6035042f, 5.2235837f)
			);
			assertThat(
					layer.getCloseUtilizationVolumeNetOfDecayByUtilization(),
					utilization(0f, 67.0664597f, 2.39902258f, 36.5664368f, 21.7930279f, 6.30796671f)
			);

			assertThat(
					spec1.getCloseUtilizationVolumeNetOfDecayAndWasteByUtilization(),
					utilization(0f, 6.18276405f, 0.0347718038f, 2.93580461f, 2.16927385f, 1.04291379f)
			);
			assertThat(
					spec2.getCloseUtilizationVolumeNetOfDecayAndWasteByUtilization(),
					utilization(0f, 60.6585732f, 2.36029577f, 33.544487f, 19.5525551f, 5.20123625f)
			);
			assertThat(
					layer.getCloseUtilizationVolumeNetOfDecayAndWasteByUtilization(),
					utilization(0f, 66.8413391f, 2.39506769f, 36.4802933f, 21.7218285f, 6.24415016f)
			);

			assertThat(
					spec1.getCloseUtilizationVolumeNetOfDecayWasteAndBreakageByUtilization(),
					utilization(0f, 5.989573f, 0.0337106399f, 2.84590816f, 2.10230994f, 1.00764418f)
			);
			assertThat(
					spec2.getCloseUtilizationVolumeNetOfDecayWasteAndBreakageByUtilization(),
					utilization(0f, 59.4318657f, 2.31265593f, 32.8669167f, 19.1568871f, 5.09540558f)
			);
			assertThat(
					layer.getCloseUtilizationVolumeNetOfDecayWasteAndBreakageByUtilization(),
					utilization(0f, 65.4214401f, 2.34636664f, 35.7128258f, 21.2591972f, 6.10304976f)
			);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
