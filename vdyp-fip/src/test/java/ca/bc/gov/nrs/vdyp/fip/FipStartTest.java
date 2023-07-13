package ca.bc.gov.nrs.vdyp.fip;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.jupiter.api.Test;

import ca.bc.gov.nrs.vdyp.fip.model.FipLayer;
import ca.bc.gov.nrs.vdyp.fip.model.FipLayerPrimary;
import ca.bc.gov.nrs.vdyp.fip.model.FipMode;
import ca.bc.gov.nrs.vdyp.fip.model.FipPolygon;
import ca.bc.gov.nrs.vdyp.fip.model.FipSpecies;
import ca.bc.gov.nrs.vdyp.io.parse.MockStreamingParser;
import ca.bc.gov.nrs.vdyp.io.parse.ResourceParseException;
import ca.bc.gov.nrs.vdyp.io.parse.StreamingParserFactory;
import ca.bc.gov.nrs.vdyp.model.Layer;

public class FipStartTest {

	@Test
	public void testProcessEmpty() throws Exception {

		testWith(Arrays.asList(), Arrays.asList(), Arrays.asList(), app -> {
			assertDoesNotThrow(app::process);
		});
	}

	@Test
	public void testProcessSimple() throws Exception {

		var polygonId = polygonId("Test Polygon", 2023);
		var layer = Layer.PRIMARY;

		// One polygon with one primary layer with one species entry

		testWith(
				Arrays.asList(getTestPolygon(polygonId, valid())), //
				Arrays.asList(layerMap(getTestPrimaryLayer(polygonId, valid()))), //
				Arrays.asList(Collections.singletonList(getTestSpecies(polygonId, layer, valid()))), //
				app -> {
					assertDoesNotThrow(app::process);
				}
		);

	}

	@Test
	public void testPolygonWithNoLayersRecord() throws Exception {

		var polygonId = polygonId("Test Polygon", 2023);

		testWith(
				Arrays.asList(getTestPolygon(polygonId, valid())), //
				Collections.emptyList(), //
				Collections.emptyList(), //
				app -> {
					var ex = assertThrows(ProcessingException.class, () -> app.process());

					assertThat(ex, hasProperty("message", is("Layers file has fewer records than polygon file.")));

				}
		);
	}

	@Test
	public void testPolygonWithNoSpeciesRecord() throws Exception {

		var polygonId = polygonId("Test Polygon", 2023);

		testWith(
				Arrays.asList(getTestPolygon(polygonId, valid())), //
				Arrays.asList(layerMap(getTestPrimaryLayer(polygonId, valid()))), //
				Collections.emptyList(), //
				app -> {
					var ex = assertThrows(ProcessingException.class, () -> app.process());

					assertThat(ex, hasProperty("message", is("Species file has fewer records than polygon file.")));

				}
		);
	}

	@Test
	public void testPolygonWithNoPrimaryLayer() throws Exception {

		var polygonId = polygonId("Test Polygon", 2023);
		var layer = Layer.VETERAN;

		// One polygon with one layer with one species entry, and type is VETERAN
		testWith(
				Arrays.asList(getTestPolygon(polygonId, valid())), //
				Arrays.asList(layerMap(getTestVeteranLayer(polygonId, valid()))), //
				Arrays.asList(Collections.singletonList(getTestSpecies(polygonId, layer, valid()))), //
				app -> {
					var ex = assertThrows(ProcessingException.class, () -> app.process());

					assertThat(
							ex,
							hasProperty(
									"message",
									is(
											"Polygon " + polygonId + " has no " + Layer.PRIMARY
													+ " layer, or that layer has non-positive height or crown closure."
									)
							)
					);

				}
		);

	}

	@Test
	public void testPrimaryLayerHeightLessThanMinimum() throws Exception {

		var polygonId = polygonId("Test Polygon", 2023);
		var layer = Layer.PRIMARY;

		testWith(
				Arrays.asList(getTestPolygon(polygonId, valid())), //
				Arrays.asList(layerMap(getTestPrimaryLayer(polygonId, x -> {
					x.setHeight(4f);
				}))), //
				Arrays.asList(Collections.singletonList(getTestSpecies(polygonId, layer, valid()))), //
				app -> {
					var ex = assertThrows(ProcessingException.class, () -> app.process());

					assertThat(
							ex,
							hasProperty(
									"message",
									is(
											"Polygon " + polygonId + " has " + Layer.PRIMARY
													+ " layer where height 4.0 is less than minimum 5.0."
									)
							)
					);

				}
		);

	}

	@Test
	public void testVeteranLayerHeightLessThanMinimum() throws Exception {

		var polygonId = polygonId("Test Polygon", 2023);
		var layer = Layer.VETERAN;

		testWith(
				Arrays.asList(getTestPolygon(polygonId, valid())), //
				Arrays.asList(
						layerMap(
								getTestPrimaryLayer(polygonId, valid()), //
								getTestVeteranLayer(polygonId, x -> {
									x.setHeight(9f);
								}) //
						)
				), //
				Arrays.asList(Collections.singletonList(getTestSpecies(polygonId, layer, valid()))), //
				app -> {
					var ex = assertThrows(ProcessingException.class, () -> app.process());

					assertThat(
							ex,
							hasProperty(
									"message",
									is(
											"Polygon " + polygonId + " has " + Layer.VETERAN
													+ " layer where height 9.0 is less than minimum 10.0."
									)
							)
					);

				}
		);

	}

	@Test
	public void testPrimaryLayerTotalAgeLessThanYearsToBreastHeight() throws Exception {

		var polygonId = polygonId("Test Polygon", 2023);
		var layer = Layer.PRIMARY;

		// FIXME VDYP7 actually tests if total age - YTBH is less than 0.5 but gives an
		// error that total age is "less than" YTBH. Replicating that for now but
		// consider changing it.

		testWith(
				Arrays.asList(getTestPolygon(polygonId, valid())), //
				Arrays.asList(layerMap(getTestPrimaryLayer(polygonId, x -> {
					x.setAgeTotal(7f);
					x.setYearsToBreastHeight(8f);
				}))), //
				Arrays.asList(Collections.singletonList(getTestSpecies(polygonId, layer, valid()))), //
				app -> {

					var ex = assertThrows(ProcessingException.class, () -> app.process());

					assertThat(
							ex,
							hasProperty(
									"message",
									is(
											"Polygon " + polygonId + " has " + Layer.PRIMARY
													+ " layer where total age is less than YTBH."
									)
							)
					);
				}
		);

	}

	@Test
	public void testPolygonWithModeFipYoung() throws Exception {

		var polygonId = polygonId("Test Polygon", 2023);
		var layer = Layer.PRIMARY;

		testWith(Arrays.asList(getTestPolygon(polygonId, x -> {
			x.setModeFip(Optional.of(FipMode.FIPYOUNG));
		})), //
				Arrays.asList(layerMap(getTestPrimaryLayer(polygonId, valid()))), //
				Arrays.asList(Collections.singletonList(getTestSpecies(polygonId, layer, valid()))), //
				app -> {

					var ex = assertThrows(ProcessingException.class, () -> app.process());

					assertThat(
							ex,
							hasProperty(
									"message",
									is("Polygon " + polygonId + " is using unsupported mode " + FipMode.FIPYOUNG + ".")
							)
					);
				}
		);

	}

	private static <T> MockStreamingParser<T>
			mockStream(IMocksControl control, Map<String, Object> controlMap, String key, String name)
					throws IOException {
		StreamingParserFactory<T> streamFactory = control.mock(name + "Factory", StreamingParserFactory.class);
		MockStreamingParser<T> stream = new MockStreamingParser<>();

		EasyMock.expect(streamFactory.get()).andReturn(stream);

		controlMap.put(key, streamFactory);
		return stream;
	}

	private static void expectAllClosed(MockStreamingParser<?>... toClose) throws Exception {
		for (var x : toClose) {
			x.expectClosed();
		}
	}

	private static <T> void mockWith(MockStreamingParser<T> stream, List<T> results)
			throws IOException, ResourceParseException {
		stream.addValues(results);
	}

	@SuppressWarnings("unused")
	@SafeVarargs
	private static <T> void mockWith(MockStreamingParser<T> stream, T... results)
			throws IOException, ResourceParseException {
		stream.addValues(results);
	}

	private String polygonId(String prefix, int year) {
		return String.format("%-23s%4d", prefix, year);
	}

	private static final void testWith(
			List<FipPolygon> polygons, List<Map<Layer, FipLayer>> layers, List<Collection<FipSpecies>> species,
			TestConsumer<FipStart> test
	) throws Exception {
		testWith(new HashMap<>(), polygons, layers, species, test);
	}

	private static final void testWith(
			Map<String, Object> myControlMap, List<FipPolygon> polygons, List<Map<Layer, FipLayer>> layers,
			List<Collection<FipSpecies>> species, TestConsumer<FipStart> test
	) throws Exception {

		var app = new FipStart();

		Map<String, Object> controlMap = new HashMap<>();

		Map<String, Float> minima = new HashMap<>();

		minima.put(FipControlParser.MINIMUM_HEIGHT, 5f);
		minima.put(FipControlParser.MINIMUM_BASE_AREA, 0f);
		minima.put(FipControlParser.MINIMUM_PREDICTED_BASE_AREA, 2f);
		minima.put(FipControlParser.MINIMUM_VETERAN_HEIGHT, 10f);

		controlMap.put(FipControlParser.MINIMA, minima);

		controlMap.putAll(myControlMap);

		var control = EasyMock.createControl();

		MockStreamingParser<FipPolygon> polygonStream = mockStream(
				control, controlMap, FipPolygonParser.CONTROL_KEY, "polygonStream"
		);
		MockStreamingParser<Map<Layer, FipLayer>> layerStream = mockStream(
				control, controlMap, FipLayerParser.CONTROL_KEY, "layerStream"
		);
		MockStreamingParser<Collection<FipSpecies>> speciesStream = mockStream(
				control, controlMap, FipSpeciesParser.CONTROL_KEY, "speciesStream"
		);

		mockWith(polygonStream, polygons);
		mockWith(layerStream, layers);
		mockWith(speciesStream, species);

		app.setControlMap(controlMap);

		control.replay();

		test.accept(app);

		control.verify();

		expectAllClosed(polygonStream, layerStream, speciesStream);

	}

	/**
	 * Do nothing to mutate valid test data
	 */
	static final <T> Consumer<T> valid() {
		return x -> {
		};
	};

	static Map<Layer, FipLayer> layerMap(FipLayer... layers) {
		Map<Layer, FipLayer> result = new HashMap<>();
		for (var layer : layers) {
			result.put(layer.getLayer(), layer);
		}
		return result;
	}

	FipPolygon getTestPolygon(String polygonId, Consumer<FipPolygon> mutator) {
		var result = new FipPolygon(
				polygonId, // polygonIdentifier
				"0", // fiz
				"BG", // becIdentifier
				Optional.empty(), // percentAvailable
				Optional.of(FipMode.FIPSTART), // modeFip
				Optional.empty(), // nonproductiveDescription
				1.0f // yieldFactor
		);
		mutator.accept(result);
		return result;
	};

	FipLayerPrimary getTestPrimaryLayer(String polygonId, Consumer<FipLayerPrimary> mutator) {
		var result = new FipLayerPrimary(
				polygonId, // polygonIdentifier
				8f, // ageTotal
				6f, // height
				5.0f, // siteIndex
				0.9f, // crownClosure
				"B", // siteGenus
				"B", // siteSpecies
				7f, // yearsToBreastHeight
				Optional.empty(), // stockingClass
				Optional.empty(), // inventoryTypeGroup
				Optional.empty(), // breastHeightAge
				Optional.empty() // siteCurveNumber
		);
		mutator.accept(result);
		return result;
	};

	FipLayer getTestVeteranLayer(String polygonId, Consumer<FipLayer> mutator) {
		var result = new FipLayer(
				polygonId, // polygonIdentifier
				Layer.VETERAN, // layer
				8f, // ageTotal
				6f, // height
				5.0f, // siteIndex
				0.9f, // crownClosure
				"B", // siteGenus
				"B", // siteSpecies
				7f, // yearsToBreastHeight
				Optional.empty(), // inventoryTypeGroup
				Optional.empty() // breastHeightAge
		);
		mutator.accept(result);
		return result;
	};

	FipSpecies getTestSpecies(String polygonId, Layer layer, Consumer<FipSpecies> mutator) {
		var result = new FipSpecies(
				polygonId, // polygonIdentifier
				layer, // layer
				"B", // genus
				100.0f, // percentGenus
				Collections.emptyMap() // speciesPercent
		);
		mutator.accept(result);
		return result;
	};

	@FunctionalInterface
	private static interface TestConsumer<T> {
		public void accept(T unit) throws Exception;
	}
}
