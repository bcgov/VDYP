package ca.bc.gov.nrs.vdyp.fip;

import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.closeTo;
import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.coe;
import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.present;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.junit.jupiter.api.Test;

import ca.bc.gov.nrs.vdyp.application.ApplicationTestUtils;
import ca.bc.gov.nrs.vdyp.application.ProcessingException;
import ca.bc.gov.nrs.vdyp.application.StandProcessingException;
import ca.bc.gov.nrs.vdyp.application.VdypStartApplication;
import ca.bc.gov.nrs.vdyp.common.ControlKey;
import ca.bc.gov.nrs.vdyp.common.Utils;
import ca.bc.gov.nrs.vdyp.fip.FipStart.CompatibilityVariableMode;
import ca.bc.gov.nrs.vdyp.fip.FipStart.VolumeComputeMode;
import ca.bc.gov.nrs.vdyp.fip.model.FipLayer;
import ca.bc.gov.nrs.vdyp.fip.model.FipLayerPrimary;
import ca.bc.gov.nrs.vdyp.fip.model.FipLayerPrimary.PrimaryBuilder;
import ca.bc.gov.nrs.vdyp.fip.model.FipPolygon;
import ca.bc.gov.nrs.vdyp.fip.model.FipSite;
import ca.bc.gov.nrs.vdyp.fip.model.FipSpecies;
import ca.bc.gov.nrs.vdyp.fip.test.FipTestUtils;
import ca.bc.gov.nrs.vdyp.io.parse.coe.BecDefinitionParser;
import ca.bc.gov.nrs.vdyp.io.parse.coe.GenusDefinitionParser;
import ca.bc.gov.nrs.vdyp.io.parse.common.ResourceParseException;
import ca.bc.gov.nrs.vdyp.io.parse.control.BaseControlParser;
import ca.bc.gov.nrs.vdyp.io.parse.streaming.MockStreamingParser;
import ca.bc.gov.nrs.vdyp.io.parse.streaming.StreamingParserFactory;
import ca.bc.gov.nrs.vdyp.model.Coefficients;
import ca.bc.gov.nrs.vdyp.model.PolygonMode;
import ca.bc.gov.nrs.vdyp.model.LayerType;
import ca.bc.gov.nrs.vdyp.model.MatrixMap2;
import ca.bc.gov.nrs.vdyp.model.PolygonIdentifier;
import ca.bc.gov.nrs.vdyp.model.Region;
import ca.bc.gov.nrs.vdyp.model.StockingClassFactor;
import ca.bc.gov.nrs.vdyp.model.UtilizationClass;
import ca.bc.gov.nrs.vdyp.model.VdypLayer;
import ca.bc.gov.nrs.vdyp.model.VdypPolygon;
import ca.bc.gov.nrs.vdyp.model.VdypSpecies;
import ca.bc.gov.nrs.vdyp.model.VdypUtilizationHolder;
import ca.bc.gov.nrs.vdyp.test.TestUtils;

class FipStartTest {

	@Test
	void testProcessEmpty() throws Exception {

		testWith(Arrays.asList(), Arrays.asList(), Arrays.asList(), (app, controlMap) -> {
			assertDoesNotThrow(app::process);
		});
	}

	@Test
	void testProcessSimple() throws Exception {

		var polygonId = polygonId("Test Polygon", 2023);
		var layer = LayerType.PRIMARY;

		// One polygon with one primary layer with one species entry
		testWith(
				FipTestUtils.loadControlMap(), Arrays.asList(getTestPolygon(polygonId, valid())), //
				Arrays.asList(layerMap(getTestPrimaryLayer(polygonId, valid(), valid()))), //
				Arrays.asList(Collections.singletonList(getTestSpecies(polygonId, layer, valid()))), //
				(app, controlMap) -> {
					assertDoesNotThrow(app::process);
				}
		);

	}

	@Test
	void testPolygonWithNoLayersRecord() throws Exception {

		var polygonId = polygonId("Test Polygon", 2023);

		testWith(
				Arrays.asList(getTestPolygon(polygonId, valid())), //
				Collections.emptyList(), //
				Collections.emptyList(), //
				(app, controlMap) -> {
					var ex = assertThrows(ProcessingException.class, () -> app.process());

					assertThat(ex, hasProperty("message", is("Layers file has fewer records than polygon file.")));

				}
		);
	}

	@Test
	void testPolygonWithNoSpeciesRecord() throws Exception {

		var polygonId = polygonId("Test Polygon", 2023);

		testWith(
				Arrays.asList(getTestPolygon(polygonId, valid())), //
				Arrays.asList(layerMap(getTestPrimaryLayer(polygonId, valid(), valid()))), //
				Collections.emptyList(), //
				(app, controlMap) -> {
					var ex = assertThrows(ProcessingException.class, () -> app.process());

					assertThat(ex, hasProperty("message", is("Species file has fewer records than polygon file.")));

				}
		);
	}

	@Test
	void testPolygonWithNoPrimaryLayer() throws Exception {

		// One polygon with one layer with one species entry, and type is VETERAN

		var controlMap = FipTestUtils.loadControlMap();
		try (var app = new FipStart()) {
			ApplicationTestUtils.setControlMap(app, controlMap);

			var polygonId = polygonId("Test Polygon", 2023);

			var polygon = getTestPolygon(polygonId, valid());
			var layer2 = getTestVeteranLayer(polygonId, valid(), siteBuilder -> {
				siteBuilder.height(9f);
			});
			polygon.setLayers(List.of(layer2));

			var ex = assertThrows(StandProcessingException.class, () -> app.checkPolygon(polygon));
			assertThat(
					ex,
					hasProperty(
							"message",
							is(
									"Polygon \"" + polygonId + "\" has no " + LayerType.PRIMARY
											+ " layer, or that layer has non-positive height or crown closure."
							)
					)
			);
		}
	}

	@Test
	void testPrimaryLayerHeightLessThanMinimum() throws Exception {

		var controlMap = FipTestUtils.loadControlMap();
		var polygonId = new PolygonIdentifier("TestPolygon", 2024);
		try (var app = new FipStart()) {
			ApplicationTestUtils.setControlMap(app, controlMap);

			var polygon = getTestPolygon(polygonId, valid());
			FipLayer layer = this.getTestPrimaryLayer(polygonId, valid(), sBuilder -> {
				sBuilder.height(4f);
			});
			polygon.setLayers(Collections.singletonMap(LayerType.PRIMARY, layer));

			var ex = assertThrows(StandProcessingException.class, () -> app.checkPolygon(polygon));
			assertThat(
					ex,
					hasProperty(
							"message",
							is(
									"Polygon " + polygonId + " has " + LayerType.PRIMARY
											+ " layer where height 4.0 is less than minimum 5.0."
							)
					)
			);
		}

	}

	@Test
	void testVeteranLayerHeightLessThanMinimum() throws Exception {
		var controlMap = FipTestUtils.loadControlMap();
		try (var app = new FipStart()) {
			ApplicationTestUtils.setControlMap(app, controlMap);

			var polygonId = polygonId("Test Polygon", 2023);

			var polygon = getTestPolygon(polygonId, valid());
			var layer1 = getTestPrimaryLayer(polygonId, valid(), valid());
			var layer2 = getTestVeteranLayer(polygonId, valid(), sBuilder -> {
				sBuilder.height(9f);
			});
			polygon.setLayers(List.of(layer1, layer2));

			var ex = assertThrows(StandProcessingException.class, () -> app.checkPolygon(polygon));
			assertThat(
					ex,
					hasProperty(
							"message",
							is(
									"Polygon " + polygonId + " has " + LayerType.VETERAN
											+ " layer where height 9.0 is less than minimum 10.0."
							)
					)
			);
		}

	}

	@Test
	void testPrimaryLayerYearsToBreastHeightLessThanMinimum() throws Exception {

		var controlMap = FipTestUtils.loadControlMap();
		try (var app = new FipStart()) {
			ApplicationTestUtils.setControlMap(app, controlMap);

			var polygonId = polygonId("Test Polygon", 2023);

			var polygon = getTestPolygon(polygonId, valid());
			var layer1 = getTestPrimaryLayer(polygonId, valid(), sBuilder -> {
				sBuilder.yearsToBreastHeight(0.2f);
			});
			polygon.setLayers(List.of(layer1));

			var ex = assertThrows(StandProcessingException.class, () -> app.checkPolygon(polygon));
			assertThat(
					ex,
					hasProperty(
							"message",
							is(
									"Polygon " + polygonId + " has " + LayerType.PRIMARY
											+ " layer where years to breast height 0.2 is less than minimum 0.5 years."
							)
					)
			);
		}
	}

	@Test
	void testPrimaryLayerTotalAgeLessThanYearsToBreastHeight() throws Exception {

		// FIXME VDYP7 actually tests if total age - YTBH is less than 0.5 but gives an
		// error that total age is "less than" YTBH. Replicating that for now but
		// consider changing it.

		var controlMap = FipTestUtils.loadControlMap();
		try (var app = new FipStart()) {
			ApplicationTestUtils.setControlMap(app, controlMap);

			var polygonId = polygonId("Test Polygon", 2023);

			var polygon = getTestPolygon(polygonId, valid());
			var layer1 = getTestPrimaryLayer(polygonId, valid(), siteBuilder -> {
				siteBuilder.ageTotal(7f);
				siteBuilder.yearsToBreastHeight(8f);
			});
			polygon.setLayers(List.of(layer1));

			var ex = assertThrows(StandProcessingException.class, () -> app.checkPolygon(polygon));
			assertThat(
					ex,
					hasProperty(
							"message",
							is(
									"Polygon " + polygonId + " has " + LayerType.PRIMARY
											+ " layer where total age is less than YTBH."
							)
					)
			);
		}
	}

	@Test
	void testPrimaryLayerSiteIndexLessThanMinimum() throws Exception {

		var controlMap = FipTestUtils.loadControlMap();
		var polygonId = new PolygonIdentifier("TestPolygon", 2024);
		try (var app = new FipStart()) {
			ApplicationTestUtils.setControlMap(app, controlMap);

			var polygon = getTestPolygon(polygonId, valid());
			var layer = this.getTestPrimaryLayer(polygonId, valid(), siteBuilder -> {
				siteBuilder.siteIndex(0.2f);
			});
			polygon.setLayers(Collections.singletonMap(LayerType.PRIMARY, layer));

			var ex = assertThrows(StandProcessingException.class, () -> app.checkPolygon(polygon));
			assertThat(
					ex,
					hasProperty(
							"message",
							is(
									"Polygon " + polygonId + " has " + LayerType.PRIMARY
											+ " layer where site index 0.2 is less than minimum 0.5 years."
							)
					)
			);
		}
	}

	@Test
	void testPolygonWithModeFipYoung() throws Exception {

		var controlMap = FipTestUtils.loadControlMap();
		var polygonId = new PolygonIdentifier("TestPolygon", 2024);
		try (var app = new FipStart()) {
			ApplicationTestUtils.setControlMap(app, controlMap);

			var polygon = getTestPolygon(polygonId, x -> {
				x.setMode(Optional.of(PolygonMode.YOUNG));
			});
			var layer = this.getTestPrimaryLayer(polygonId, valid(), valid());
			polygon.setLayers(List.of(layer));

			var ex = assertThrows(StandProcessingException.class, () -> app.checkPolygon(polygon));
			assertThat(
					ex,
					hasProperty(
							"message",
							is("Polygon " + polygonId + " is using unsupported mode " + PolygonMode.YOUNG + ".")
					)
			);
		}

	}

	@Test
	void testOneSpeciesLessThan100Percent() throws Exception {

		var controlMap = FipTestUtils.loadControlMap();
		try (var app = new FipStart()) {
			ApplicationTestUtils.setControlMap(app, controlMap);

			var polygonId = polygonId("Test Polygon", 2023);

			var polygon = getTestPolygon(polygonId, valid());
			var layer = this.getTestPrimaryLayer(polygonId, valid(), valid());
			var spec = getTestSpecies(polygonId, LayerType.PRIMARY, x -> {
				x.setPercentGenus(99f);
			});
			layer.setSpecies(List.of(spec));
			polygon.setLayers(List.of(layer));

			var ex = assertThrows(StandProcessingException.class, () -> app.checkPolygon(polygon));
			assertThat(
					ex,
					hasProperty(
							"message",
							is(
									"Polygon \"" + polygonId
											+ "\" has PRIMARY layer where species entries have a percentage total that does not sum to 100%."
							)
					)
			);
		}

	}

	@Test
	void testOneSpeciesMoreThan100Percent() throws Exception {

		var controlMap = FipTestUtils.loadControlMap();
		try (var app = new FipStart()) {
			ApplicationTestUtils.setControlMap(app, controlMap);

			var polygonId = polygonId("Test Polygon", 2023);

			var polygon = getTestPolygon(polygonId, valid());
			var layer = this.getTestPrimaryLayer(polygonId, valid(), valid());
			var spec = getTestSpecies(polygonId, LayerType.PRIMARY, x -> {
				x.setPercentGenus(101f);
			});
			layer.setSpecies(List.of(spec));
			polygon.setLayers(List.of(layer));

			var ex = assertThrows(StandProcessingException.class, () -> app.checkPolygon(polygon));
			assertThat(
					ex,
					hasProperty(
							"message",
							is(
									"Polygon \"" + polygonId
											+ "\" has PRIMARY layer where species entries have a percentage total that does not sum to 100%."
							)
					)
			);
		}

	}

	@Test
	void testTwoSpeciesSumTo100Percent() throws Exception {

		var controlMap = FipTestUtils.loadControlMap();
		try (var app = new FipStart()) {
			ApplicationTestUtils.setControlMap(app, controlMap);

			var polygonId = polygonId("Test Polygon", 2023);

			var polygon = getTestPolygon(polygonId, valid());
			var layer = this.getTestPrimaryLayer(polygonId, valid(), valid());
			var spec1 = getTestSpecies(polygonId, LayerType.PRIMARY, "B", x -> {
				x.setPercentGenus(75f);
			});
			var spec2 = getTestSpecies(polygonId, LayerType.PRIMARY, "C", x -> {
				x.setPercentGenus(25f);
			});
			layer.setSpecies(List.of(spec1, spec2));
			polygon.setLayers(List.of(layer));

			assertDoesNotThrow(() -> app.checkPolygon(polygon));
		}
	}

	@Test
	void testTwoSpeciesSumToLessThan100Percent() throws Exception {

		var controlMap = FipTestUtils.loadControlMap();
		try (var app = new FipStart()) {
			ApplicationTestUtils.setControlMap(app, controlMap);

			var polygonId = polygonId("Test Polygon", 2023);

			var polygon = getTestPolygon(polygonId, valid());
			var layer = this.getTestPrimaryLayer(polygonId, valid(), valid());
			var spec1 = getTestSpecies(polygonId, LayerType.PRIMARY, "B", x -> {
				x.setPercentGenus(75f - 1f);
			});
			var spec2 = getTestSpecies(polygonId, LayerType.PRIMARY, "C", x -> {
				x.setPercentGenus(25f);
			});
			layer.setSpecies(List.of(spec1, spec2));
			polygon.setLayers(List.of(layer));

			var ex = assertThrows(StandProcessingException.class, () -> app.checkPolygon(polygon));
			assertThat(
					ex,
					hasProperty(
							"message",
							is(
									"Polygon \"" + polygonId
											+ "\" has PRIMARY layer where species entries have a percentage total that does not sum to 100%."
							)
					)
			);
		}

	}

	@Test
	void testTwoSpeciesSumToMoreThan100Percent() throws Exception {

		var controlMap = FipTestUtils.loadControlMap();
		try (var app = new FipStart()) {
			ApplicationTestUtils.setControlMap(app, controlMap);

			var polygonId = polygonId("Test Polygon", 2023);

			var polygon = getTestPolygon(polygonId, valid());
			var layer = this.getTestPrimaryLayer(polygonId, valid(), valid());
			var spec1 = getTestSpecies(polygonId, LayerType.PRIMARY, "B", x -> {
				x.setPercentGenus(75f + 1f);
			});
			var spec2 = getTestSpecies(polygonId, LayerType.PRIMARY, "C", x -> {
				x.setPercentGenus(25f);
			});
			layer.setSpecies(List.of(spec1, spec2));
			polygon.setLayers(List.of(layer));

			var ex = assertThrows(StandProcessingException.class, () -> app.checkPolygon(polygon));
			assertThat(
					ex,
					hasProperty(
							"message",
							is(
									"Polygon \"" + polygonId
											+ "\" has PRIMARY layer where species entries have a percentage total that does not sum to 100%."
							)
					)
			);
		}

	}

	@Test
	void testFractionGenusCalculation() throws Exception {

		var polygonId = polygonId("Test Polygon", 2023);
		var layer = LayerType.PRIMARY;

		final var speciesList = Arrays.asList(
				//
				getTestSpecies(polygonId, layer, "B", x -> {
					x.setPercentGenus(75f);
				}), getTestSpecies(polygonId, layer, "C", x -> {
					x.setPercentGenus(25f);
				})
		);
		testWith(
				FipTestUtils.loadControlMap(), Arrays.asList(getTestPolygon(polygonId, valid())), //
				Arrays.asList(layerMap(getTestPrimaryLayer(polygonId, valid(), valid()))), //
				Arrays.asList(speciesList), //
				(app, controlMap) -> {

					app.process();

					// Testing exact floating point equality is intentional
					assertThat(
							speciesList, contains(
									//
									allOf(hasProperty("genus", is("B")), hasProperty("fractionGenus", is(0.75f))), //
									allOf(hasProperty("genus", is("C")), hasProperty("fractionGenus", is(0.25f)))//
							)
					);
				}
		);

	}

	@Test
	void testFractionGenusCalculationWithSlightError() throws Exception {

		var polygonId = polygonId("Test Polygon", 2023);
		var layer = LayerType.PRIMARY;

		final var speciesList = Arrays.asList(
				//
				getTestSpecies(polygonId, layer, "B", x -> {
					x.setPercentGenus(75 + 0.009f);
				}), getTestSpecies(polygonId, layer, "C", x -> {
					x.setPercentGenus(25f);
				})
		);
		testWith(
				FipTestUtils.loadControlMap(), Arrays.asList(getTestPolygon(polygonId, valid())), //
				Arrays.asList(layerMap(getTestPrimaryLayer(polygonId, valid(), valid()))), //
				Arrays.asList(speciesList), //
				(app, controlMap) -> {

					app.process();

					// Testing exact floating point equality is intentional
					assertThat(
							speciesList, contains(
									//
									allOf(hasProperty("genus", is("B")), hasProperty("fractionGenus", is(0.75002253f))), //
									allOf(hasProperty("genus", is("C")), hasProperty("fractionGenus", is(0.2499775f)))//
							)
					);
				}
		);

	}

	@Test
	void testProcessVeteran() throws Exception {

		var polygonId = polygonId("Test Polygon", 2023);

		var fipPolygon = getTestPolygon(polygonId, valid());
		var fipLayer = getTestVeteranLayer(polygonId, valid(), valid());
		var fipSpecies = getTestSpecies(polygonId, LayerType.VETERAN, x -> {
			x.setSpeciesPercent(Collections.emptyMap());
		});
		fipPolygon.setLayers(Collections.singletonMap(LayerType.VETERAN, fipLayer));
		fipLayer.setSpecies(Collections.singletonMap(fipSpecies.getGenus(), fipSpecies));

		var controlMap = new HashMap<String, Object>();
		TestUtils.populateControlMapBecReal(controlMap);
		TestUtils.populateControlMapGenusReal(controlMap);
		TestUtils.populateControlMapVeteranBq(controlMap);
		TestUtils.populateControlMapEquationGroups(controlMap, (s, b) -> new int[] { 1, 1, 1 });
		TestUtils.populateControlMapVeteranDq(controlMap, (s, r) -> new float[] { 0f, 0f, 0f });
		TestUtils.populateControlMapVeteranVolAdjust(controlMap, s -> new float[] { 0f, 0f, 0f, 0f });
		TestUtils.populateControlMapWholeStemVolume(controlMap, wholeStemMap(1));
		TestUtils.populateControlMapCloseUtilization(controlMap, closeUtilMap(1));
		TestUtils.populateControlMapNetDecay(controlMap, closeUtilMap(1));
		FipTestUtils.populateControlMapDecayModifiers(controlMap, (s, r) -> 0f);
		TestUtils.populateControlMapNetWaste(
				controlMap, s -> new Coefficients(new float[] { 0f, 0f, 0f, 0f, 0f, 0f }, 0)
		);
		FipTestUtils.populateControlMapWasteModifiers(controlMap, (s, r) -> 0f);
		TestUtils
				.populateControlMapNetBreakage(controlMap, bgrp -> new Coefficients(new float[] { 0f, 0f, 0f, 0f }, 1));

		try (var app = new FipStart()) {
			ApplicationTestUtils.setControlMap(app, controlMap);

			var result = app.processLayerAsVeteran(fipPolygon, fipLayer);

			assertThat(result, notNullValue());

			// Keys
			assertThat(result, hasProperty("polygonIdentifier", is(polygonId)));
			assertThat(result, hasProperty("layerType", is(LayerType.VETERAN)));

			// Direct Copy
			assertThat(result, hasProperty("ageTotal", present(is(8f))));
			assertThat(result, hasProperty("height", present(is(6f))));
			assertThat(result, hasProperty("yearsToBreastHeight", present(is(7f))));

			// Computed
			assertThat(result, hasProperty("breastHeightAge", present(is(1f))));

			// Remap species
			assertThat(
					result, hasProperty(
							"species", allOf(
									aMapWithSize(1), //
									hasEntry(is("B"), instanceOf(VdypSpecies.class))//
							)
					)
			);
			var speciesResult = result.getSpecies().get("B");

			// Keys
			assertThat(speciesResult, hasProperty("polygonIdentifier", is(polygonId)));
			assertThat(speciesResult, hasProperty("layerType", is(LayerType.VETERAN)));
			assertThat(speciesResult, hasProperty("genus", is("B")));

			// Copied
			assertThat(speciesResult, hasProperty("percentGenus", is(100f)));

			// Species distribution
			assertThat(speciesResult, hasProperty("speciesPercent", anEmptyMap())); // Test map was empty
		}
	}

	@Test
	void testProcessVeteranUtilization() throws Exception {

		var polygonId = polygonId("Test Polygon", 2023);

		var fipPolygon = getTestPolygon(polygonId, x -> {
			x.setBiogeoclimaticZone("CWH");
			x.setForestInventoryZone("A");
			x.setYieldFactor(1f);
		});
		var fipLayer = getTestVeteranLayer(polygonId, layerBuilder -> {
			layerBuilder.crownClosure(4f);
		}, siteBuilder -> {
			siteBuilder.siteCurveNumber(Optional.of(34));
			siteBuilder.height(26.2f);
			siteBuilder.siteIndex(16.7f);
			siteBuilder.yearsToBreastHeight(7.1f);
			siteBuilder.ageTotal(97.9f + 7.1f);
			siteBuilder.siteSpecies("H");
			siteBuilder.siteGenus("H");
		});
		var fipSpecies1 = getTestSpecies(polygonId, LayerType.VETERAN, "B", x -> {
			x.setPercentGenus(22f);
		});
		var fipSpecies2 = getTestSpecies(polygonId, LayerType.VETERAN, "H", x -> {
			x.setPercentGenus(60f);
		});
		var fipSpecies3 = getTestSpecies(polygonId, LayerType.VETERAN, "S", x -> {
			x.setPercentGenus(18f);
		});
		fipPolygon.setLayers(Collections.singletonMap(LayerType.VETERAN, fipLayer));
		fipLayer.setSpecies(List.of(fipSpecies1, fipSpecies2, fipSpecies3));

		var controlMap = FipTestUtils.loadControlMap();

		VdypLayer result;
		try (var app = new FipStart();) {
			ApplicationTestUtils.setControlMap(app, controlMap);

			result = app.processLayerAsVeteran(fipPolygon, fipLayer);
		}

		assertThat(result, notNullValue());

		// Keys
		assertThat(result, hasProperty("polygonIdentifier", is(polygonId)));
		assertThat(result, hasProperty("layerType", is(LayerType.VETERAN)));

		// Direct Copy
		assertThat(result, hasProperty("ageTotal", present(is(105f))));
		assertThat(result, hasProperty("height", present(is(26.2f))));
		assertThat(result, hasProperty("yearsToBreastHeight", present(is(7.1f))));

		// Computed
		assertThat(result, hasProperty("breastHeightAge", present(closeTo(97.9f))));

		// Remap species
		assertThat(
				result, hasProperty(
						"species", allOf(
								aMapWithSize(3), //
								hasEntry(is("B"), instanceOf(VdypSpecies.class)), //
								hasEntry(is("H"), instanceOf(VdypSpecies.class)), //
								hasEntry(is("S"), instanceOf(VdypSpecies.class))//
						)
				)
		);

		var speciesResult1 = result.getSpecies().get("B");

		// Keys
		assertThat(speciesResult1, hasProperty("polygonIdentifier", is(polygonId)));
		assertThat(speciesResult1, hasProperty("layerType", is(LayerType.VETERAN)));
		assertThat(speciesResult1, hasProperty("genus", is("B")));

		// Copied
		assertThat(speciesResult1, hasProperty("percentGenus", is(22f)));

		// Species distribution
		assertThat(speciesResult1, hasProperty("speciesPercent", aMapWithSize(1)));

		var speciesResult2 = result.getSpecies().get("H");

		// Keys
		assertThat(speciesResult2, hasProperty("polygonIdentifier", is(polygonId)));
		assertThat(speciesResult2, hasProperty("layerType", is(LayerType.VETERAN)));
		assertThat(speciesResult2, hasProperty("genus", is("H")));

		// Copied
		assertThat(speciesResult2, hasProperty("percentGenus", is(60f)));

		// Species distribution
		assertThat(speciesResult2, hasProperty("speciesPercent", aMapWithSize(1)));

		var speciesResult3 = result.getSpecies().get("S");

		// Keys
		assertThat(speciesResult3, hasProperty("polygonIdentifier", is(polygonId)));
		assertThat(speciesResult3, hasProperty("layerType", is(LayerType.VETERAN)));
		assertThat(speciesResult3, hasProperty("genus", is("S")));

		// Copied
		assertThat(speciesResult3, hasProperty("percentGenus", is(18f)));

		// Species distribution
		assertThat(speciesResult3, hasProperty("speciesPercent", aMapWithSize(1)));

		// These Utilizations should differ between the layer and each genus

		{
			var holder = speciesResult1;
			String reason = "Genus " + holder.getGenus();
			assertThat(reason, holder, hasProperty("baseAreaByUtilization", utilizationAllAndBiggest(0.492921442f)));
			assertThat(
					reason, holder, hasProperty("treesPerHectareByUtilization", utilizationAllAndBiggest(2.3357718f))
			);
			assertThat(
					reason, holder, hasProperty("wholeStemVolumeByUtilization", utilizationAllAndBiggest(6.11904192f))
			);
			assertThat(
					reason, holder,
					hasProperty("closeUtilizationVolumeByUtilization", utilizationAllAndBiggest(5.86088896f))
			);
			assertThat(
					reason, holder,
					hasProperty("closeUtilizationVolumeNetOfDecayByUtilization", utilizationAllAndBiggest(5.64048958f))
			);
			assertThat(
					reason, holder,
					hasProperty(
							"closeUtilizationVolumeNetOfDecayAndWasteByUtilization",
							utilizationAllAndBiggest(5.57935333f)
					)
			);
			assertThat(
					reason, holder,
					hasProperty(
							"closeUtilizationVolumeNetOfDecayWasteAndBreakageByUtilization",
							utilizationAllAndBiggest(5.27515411f)
					)
			);
			assertThat(
					reason, holder,
					hasProperty("quadraticMeanDiameterByUtilization", utilizationAllAndBiggest(51.8356705f))
			);
		}
		{
			var holder = speciesResult2;
			String reason = "Genus " + holder.getGenus();
			assertThat(reason, holder, hasProperty("baseAreaByUtilization", utilizationAllAndBiggest(1.34433115f)));
			assertThat(
					reason, holder, hasProperty("treesPerHectareByUtilization", utilizationAllAndBiggest(5.95467329f))
			);
			assertThat(
					reason, holder, hasProperty("wholeStemVolumeByUtilization", utilizationAllAndBiggest(14.5863571f))
			);
			assertThat(
					reason, holder,
					hasProperty("closeUtilizationVolumeByUtilization", utilizationAllAndBiggest(13.9343023f))
			);
			assertThat(
					reason, holder,
					hasProperty("closeUtilizationVolumeNetOfDecayByUtilization", utilizationAllAndBiggest(13.3831034f))
			);
			assertThat(
					reason, holder,
					hasProperty(
							"closeUtilizationVolumeNetOfDecayAndWasteByUtilization",
							utilizationAllAndBiggest(13.2065458f)
					)
			);
			assertThat(
					reason, holder,
					hasProperty(
							"closeUtilizationVolumeNetOfDecayWasteAndBreakageByUtilization",
							utilizationAllAndBiggest(12.4877129f)
					)
			);
			assertThat(
					reason, holder,
					hasProperty("quadraticMeanDiameterByUtilization", utilizationAllAndBiggest(53.6141243f))
			);
		}
		{
			var holder = speciesResult3;
			String reason = "Genus " + holder.getGenus();
			assertThat(reason, holder, hasProperty("baseAreaByUtilization", utilizationAllAndBiggest(0.403299361f)));
			assertThat(
					reason, holder, hasProperty("treesPerHectareByUtilization", utilizationAllAndBiggest(2.38468361f))
			);
			assertThat(
					reason, holder, hasProperty("wholeStemVolumeByUtilization", utilizationAllAndBiggest(4.04864883f))
			);
			assertThat(
					reason, holder,
					hasProperty("closeUtilizationVolumeByUtilization", utilizationAllAndBiggest(3.81141663f))
			);
			assertThat(
					reason, holder,
					hasProperty("closeUtilizationVolumeNetOfDecayByUtilization", utilizationAllAndBiggest(3.75043678f))
			);
			assertThat(
					reason, holder,
					hasProperty(
							"closeUtilizationVolumeNetOfDecayAndWasteByUtilization",
							utilizationAllAndBiggest(3.72647476f)
					)
			);
			assertThat(
					reason, holder,
					hasProperty(
							"closeUtilizationVolumeNetOfDecayWasteAndBreakageByUtilization",
							utilizationAllAndBiggest(3.56433797f)
					)
			);
			assertThat(
					reason, holder,
					hasProperty("quadraticMeanDiameterByUtilization", utilizationAllAndBiggest(46.4037895f))
			);
		}
		{
			var holder = result;
			String reason = "Layer";
			assertThat(reason, holder, hasProperty("baseAreaByUtilization", utilizationAllAndBiggest(2.24055195f)));
			assertThat(
					reason, holder, hasProperty("treesPerHectareByUtilization", utilizationAllAndBiggest(10.6751289f))
			);
			assertThat(
					reason, holder, hasProperty("wholeStemVolumeByUtilization", utilizationAllAndBiggest(24.7540474f))
			);
			assertThat(
					reason, holder,
					hasProperty("closeUtilizationVolumeByUtilization", utilizationAllAndBiggest(23.6066074f))
			);
			assertThat(
					reason, holder,
					hasProperty("closeUtilizationVolumeNetOfDecayByUtilization", utilizationAllAndBiggest(22.7740307f))
			);
			assertThat(
					reason, holder,
					hasProperty(
							"closeUtilizationVolumeNetOfDecayAndWasteByUtilization",
							utilizationAllAndBiggest(22.5123749f)
					)
			);
			assertThat(
					reason, holder,
					hasProperty(
							"closeUtilizationVolumeNetOfDecayWasteAndBreakageByUtilization",
							utilizationAllAndBiggest(21.3272057f)
					)
			);
			assertThat(
					reason, holder,
					hasProperty("quadraticMeanDiameterByUtilization", utilizationAllAndBiggest(51.6946983f))
			);
		}
		// Lorey Height should be the same across layer and each species
		for (var holder : List.of(speciesResult1, speciesResult2, speciesResult3, result)) {
			String reason;
			if (holder instanceof VdypLayer) {
				reason = "Layer";
			} else {
				reason = "Genus " + ((VdypSpecies) holder).getGenus();
			}
			assertThat(reason, holder, hasProperty("loreyHeightByUtilization", coe(-1, 0f, 26.2f)));
		}
	}

	@Test
	void testProcessVeteranYearsToBreastHeightLessThanMinimum() throws Exception {

		var polygonId = polygonId("Test Polygon", 2023);

		var fipPolygon = getTestPolygon(polygonId, valid());
		var fipLayer = getTestVeteranLayer(polygonId, valid(), siteBuilder -> {
			siteBuilder.yearsToBreastHeight(5.0f);
		});
		var fipSpecies = getTestSpecies(polygonId, LayerType.VETERAN, valid());
		fipPolygon.setLayers(Collections.singletonMap(LayerType.VETERAN, fipLayer));
		fipLayer.setSpecies(Collections.singletonMap(fipSpecies.getGenus(), fipSpecies));

		var controlMap = new HashMap<String, Object>();
		TestUtils.populateControlMapBecReal(controlMap);
		TestUtils.populateControlMapGenusReal(controlMap);
		TestUtils.populateControlMapVeteranBq(controlMap);
		TestUtils.populateControlMapEquationGroups(controlMap, (s, b) -> new int[] { 1, 1, 1 });
		TestUtils.populateControlMapVeteranDq(controlMap, (s, r) -> new float[] { 0f, 0f, 0f });
		TestUtils.populateControlMapVeteranVolAdjust(controlMap, s -> new float[] { 0f, 0f, 0f, 0f });
		TestUtils.populateControlMapWholeStemVolume(controlMap, (wholeStemMap(1)));
		TestUtils.populateControlMapCloseUtilization(controlMap, closeUtilMap(1));
		TestUtils.populateControlMapNetDecay(controlMap, closeUtilMap(1));
		FipTestUtils.populateControlMapDecayModifiers(controlMap, (s, r) -> 0f);
		TestUtils.populateControlMapNetWaste(
				controlMap, s -> new Coefficients(new float[] { 0f, 0f, 0f, 0f, 0f, 0f }, 0)
		);
		FipTestUtils.populateControlMapWasteModifiers(controlMap, (s, r) -> 0f);
		TestUtils
				.populateControlMapNetBreakage(controlMap, bgrp -> new Coefficients(new float[] { 0f, 0f, 0f, 0f }, 1));

		try (var app = new FipStart()) {
			ApplicationTestUtils.setControlMap(app, controlMap);

			var result = app.processLayerAsVeteran(fipPolygon, fipLayer);

			assertThat(result, notNullValue());

			// Set minimum
			assertThat(result, hasProperty("yearsToBreastHeight", present(is(6f))));

			// Computed based on minimum
			assertThat(result, hasProperty("breastHeightAge", present(is(2f))));
		}

	}

	@Test
	void testProcessVeteranWithSpeciesDistribution() throws Exception {

		var polygonId = polygonId("Test Polygon", 2023);

		var fipPolygon = getTestPolygon(polygonId, valid());
		var fipLayer = getTestVeteranLayer(polygonId, valid(), valid());
		var fipSpecies = getTestSpecies(polygonId, LayerType.VETERAN, x -> {
			var map = new LinkedHashMap<String, Float>();
			map.put("S1", 75f);
			map.put("S2", 25f);
			x.setSpeciesPercent(map);
		});
		fipPolygon.setLayers(Collections.singletonMap(LayerType.VETERAN, fipLayer));
		fipLayer.setSpecies(Collections.singletonMap(fipSpecies.getGenus(), fipSpecies));

		var controlMap = new HashMap<String, Object>();
		TestUtils.populateControlMapBecReal(controlMap);
		TestUtils.populateControlMapGenusReal(controlMap);
		TestUtils.populateControlMapVeteranBq(controlMap);
		TestUtils.populateControlMapEquationGroups(controlMap, (s, b) -> new int[] { 1, 1, 1 });
		TestUtils.populateControlMapVeteranDq(controlMap, (s, r) -> new float[] { 0f, 0f, 0f });
		TestUtils.populateControlMapVeteranVolAdjust(controlMap, s -> new float[] { 0f, 0f, 0f, 0f });
		TestUtils.populateControlMapWholeStemVolume(controlMap, (wholeStemMap(1)));
		TestUtils.populateControlMapCloseUtilization(controlMap, closeUtilMap(1));
		TestUtils.populateControlMapNetDecay(controlMap, closeUtilMap(1));
		FipTestUtils.populateControlMapDecayModifiers(controlMap, (s, r) -> 0f);
		TestUtils.populateControlMapNetWaste(
				controlMap, s -> new Coefficients(new float[] { 0f, 0f, 0f, 0f, 0f, 0f }, 0)
		);
		FipTestUtils.populateControlMapWasteModifiers(controlMap, (s, r) -> 0f);
		TestUtils
				.populateControlMapNetBreakage(controlMap, bgrp -> new Coefficients(new float[] { 0f, 0f, 0f, 0f }, 1));

		try (var app = new FipStart()) {
			ApplicationTestUtils.setControlMap(app, controlMap);

			var result = app.processLayerAsVeteran(fipPolygon, fipLayer);

			assertThat(result, notNullValue());

			// Remap species
			assertThat(
					result, hasProperty(
							"species", allOf(
									aMapWithSize(1), //
									hasEntry(is("B"), instanceOf(VdypSpecies.class))//
							)
					)
			);
			var speciesResult = result.getSpecies().get("B");

			// Keys
			assertThat(speciesResult, hasProperty("polygonIdentifier", is(polygonId)));
			assertThat(speciesResult, hasProperty("layerType", is(LayerType.VETERAN)));
			assertThat(speciesResult, hasProperty("genus", is("B")));

			// Copied
			assertThat(speciesResult, hasProperty("percentGenus", is(100f)));

			// Species distribution
			assertThat(speciesResult, hasProperty("speciesPercent", aMapWithSize(2)));

			var distributionResult = speciesResult.getSpeciesPercent();

			assertThat(distributionResult, hasEntry("S1", 75f));
			assertThat(distributionResult, hasEntry("S2", 25f));
		}

	}

	@Test
	void testProcessPrimary() throws Exception {

		var polygonId = polygonId("Test Polygon", 2023);

		var fipPolygon = getTestPolygon(polygonId, x -> {
			x.setBiogeoclimaticZone("CWH");
			x.setForestInventoryZone("A");
		});
		var fipLayer = getTestPrimaryLayer(polygonId, layerBuilder -> {
			((FipLayerPrimary.Builder) layerBuilder).crownClosure(87.4f);
			((FipLayerPrimary.PrimaryBuilder) layerBuilder).primaryGenus("H");
			layerBuilder.inventoryTypeGroup(13);
		}, siteBuilder -> {
			siteBuilder.ageTotal(55f);
			siteBuilder.yearsToBreastHeight(1f);
			siteBuilder.height(35.3f);
			siteBuilder.siteIndex(5f);
			siteBuilder.siteGenus("D");
			siteBuilder.siteSpecies("D");
		});
		var fipSpecies1 = getTestSpecies(polygonId, LayerType.PRIMARY, "B", x -> {
			x.setPercentGenus(1f);
		});
		var fipSpecies2 = getTestSpecies(polygonId, LayerType.PRIMARY, "C", x -> {
			x.setPercentGenus(7f);
		});
		var fipSpecies3 = getTestSpecies(polygonId, LayerType.PRIMARY, "D", x -> {
			x.setPercentGenus(74f);
		});
		var fipSpecies4 = getTestSpecies(polygonId, LayerType.PRIMARY, "H", x -> {
			x.setPercentGenus(9f);
		});
		var fipSpecies5 = getTestSpecies(polygonId, LayerType.PRIMARY, "S", x -> {
			x.setPercentGenus(9f);
		});
		fipPolygon.setLayers(List.of(fipLayer));
		fipLayer.setSpecies(List.of(fipSpecies1, fipSpecies2, fipSpecies3, fipSpecies4, fipSpecies5));

		var controlMap = FipTestUtils.loadControlMap();

		try (var app = new FipStart()) {
			ApplicationTestUtils.setControlMap(app, controlMap);

			var result = app.processLayerAsPrimary(fipPolygon, fipLayer, 0f);

			assertThat(result, notNullValue());

			assertThat(result, hasProperty("polygonIdentifier", is(polygonId)));
			assertThat(result, hasProperty("layerType", is(LayerType.PRIMARY)));

			assertThat(result, hasProperty("ageTotal", present(is(55f))));
			assertThat(result, hasProperty("height", present(is(35.3f))));
			assertThat(result, hasProperty("yearsToBreastHeight", present(is(1f))));

			assertThat(result, hasProperty("breastHeightAge", present(is(54f))));

			assertThat(
					result,
					allOf(
							hasProperty("loreyHeightByUtilization", coe(-1, 7.14446497f, 31.3307228f)),
							hasProperty(
									"baseAreaByUtilization",
									utilization(
											0.0153773092f, 44.6249809f, 0.513127923f, 1.26773751f, 2.5276401f,
											40.3164787f
									)
							),
							hasProperty(
									"quadraticMeanDiameterByUtilization",
									utilization(
											6.05058956f, 30.2606678f, 10.208025f, 15.0549212f, 20.11759f, 35.5117531f
									)
							),
							hasProperty(
									"treesPerHectareByUtilization",
									utilization(
											5.34804535f, 620.484802f, 62.6977997f, 71.2168045f, 79.5194702f, 407.05072f
									)
							),
							hasProperty(
									"wholeStemVolumeByUtilization",
									utilization(
											0.0666879341f, 635.659668f, 2.66822577f, 9.68201256f, 26.5469246f,
											596.762512f
									)
							),

							// Ignore intermediate close volumes, if they are wrong,
							// closeUtilizationVolumeNetOfDecayWasteAndBreakageByUtilization should also be
							// wrong

							hasProperty(
									"closeUtilizationVolumeNetOfDecayWasteAndBreakageByUtilization",
									utilization(0f, 563.218933f, 0.414062887f, 7.01947737f, 22.6179276f, 533.16748f)
							)

					)
			);

			assertThat(
					result, hasProperty(
							"species", allOf(
									aMapWithSize(5), //
									hasEntry(is("B"), instanceOf(VdypSpecies.class)), //
									hasEntry(is("C"), instanceOf(VdypSpecies.class)), //
									hasEntry(is("D"), instanceOf(VdypSpecies.class)), //
									hasEntry(is("H"), instanceOf(VdypSpecies.class)), //
									hasEntry(is("S"), instanceOf(VdypSpecies.class))//
							)
					)
			);

			// Setting the primaryGenus on the FIP layer is a necessary side effect
			assertThat(fipLayer, hasProperty("primaryGenus", present(equalTo("D"))));

			var speciesResult = result.getSpecies().get("B");

			assertThat(speciesResult, hasProperty("polygonIdentifier", is(polygonId)));
			assertThat(speciesResult, hasProperty("layerType", is(LayerType.PRIMARY)));
			assertThat(speciesResult, hasProperty("genus", is("B")));

			assertThat(speciesResult, hasProperty("fractionGenus", closeTo(0.00890319888f)));

			assertThat(speciesResult, hasProperty("speciesPercent", aMapWithSize(1)));

			assertThat(
					speciesResult,
					allOf(
							hasProperty("loreyHeightByUtilization", coe(-1, 8.39441967f, 38.6004372f)),
							hasProperty(
									"baseAreaByUtilization",
									utilization(
											0f, 0.397305071f, 0.00485289097f, 0.0131751001f, 0.0221586525f, 0.357118428f
									)
							),
							hasProperty(
									"quadraticMeanDiameterByUtilization",
									utilization(
											6.13586617f, 31.6622887f, 9.17939758f, 13.6573782f, 18.2005272f, 42.1307297f
									)
							),
							hasProperty(
									"treesPerHectareByUtilization",
									utilization(0f, 5.04602766f, 0.733301044f, 0.899351299f, 0.851697803f, 2.56167722f)
							),
							hasProperty(
									"wholeStemVolumeByUtilization",
									utilization(
											0f, 6.35662031f, 0.0182443243f, 0.0747248605f, 0.172960356f, 6.09069061f
									)
							),

							// Ignore intermediate close volumes, if they are wrong,
							// closeUtilizationVolumeNetOfDecayWasteAndBreakageByUtilization should also be
							// wrong

							hasProperty(
									"closeUtilizationVolumeNetOfDecayWasteAndBreakageByUtilization",
									utilization(
											0f, 5.65764236f, 0.000855736958f, 0.046797853f, 0.143031254f, 5.46695757f
									)
							)

					)
			);

			speciesResult = result.getSpecies().get("C");

			assertThat(speciesResult, hasProperty("polygonIdentifier", is(polygonId)));
			assertThat(speciesResult, hasProperty("layerType", is(LayerType.PRIMARY)));
			assertThat(speciesResult, hasProperty("genus", is("C")));

			assertThat(speciesResult, hasProperty("fractionGenus", closeTo(0.114011094f)));

			assertThat(speciesResult, hasProperty("speciesPercent", aMapWithSize(1)));

			assertThat(
					speciesResult,
					allOf(
							hasProperty("loreyHeightByUtilization", coe(-1, 6.61517191f, 22.8001652f)),
							hasProperty(
									"baseAreaByUtilization",
									utilization(
											0.0131671466f, 5.08774281f, 0.157695293f, 0.365746498f, 0.565057278f,
											3.99924374f
									)
							),
							hasProperty(
									"quadraticMeanDiameterByUtilization",
									utilization(
											5.99067688f, 26.4735165f, 10.1137667f, 14.9345293f, 19.964777f, 38.7725677f
									)
							),
							hasProperty(
									"treesPerHectareByUtilization",
									utilization(
											4.67143154f, 92.4298019f, 19.6292171f, 20.8788815f, 18.0498524f, 33.8718452f
									)
							),
							hasProperty(
									"wholeStemVolumeByUtilization",
									utilization(
											0.0556972362f, 44.496151f, 0.78884691f, 2.40446854f, 4.43335152f,
											36.8694839f
									)
							),

							// Ignore intermediate close volumes, if they are wrong,
							// closeUtilizationVolumeNetOfDecayWasteAndBreakageByUtilization should also be
							// wrong

							hasProperty(
									"closeUtilizationVolumeNetOfDecayWasteAndBreakageByUtilization",
									utilization(0f, 33.6030083f, 0.138336331f, 1.6231581f, 3.49037051f, 28.3511429f)
							)

					)
			);

			speciesResult = result.getSpecies().get("D");

			assertThat(speciesResult, hasProperty("polygonIdentifier", is(polygonId)));
			assertThat(speciesResult, hasProperty("layerType", is(LayerType.PRIMARY)));
			assertThat(speciesResult, hasProperty("genus", is("D")));

			assertThat(speciesResult, hasProperty("fractionGenus", closeTo(0.661987007f)));

			assertThat(speciesResult, hasProperty("speciesPercent", aMapWithSize(1)));

			assertThat(
					speciesResult,
					allOf(
							hasProperty("loreyHeightByUtilization", coe(-1, 10.8831682f, 33.5375252f)),
							hasProperty(
									"baseAreaByUtilization",
									utilization(
											0.00163476227f, 29.5411568f, 0.0225830078f, 0.0963115692f, 0.748186111f,
											28.6740761f
									)
							),
							hasProperty(
									"quadraticMeanDiameterByUtilization",
									utilization(
											6.46009731f, 33.9255791f, 10.4784775f, 15.5708427f, 20.4805717f, 35.0954628f
									)
							),
							hasProperty(
									"treesPerHectareByUtilization",
									utilization(
											0.498754263f, 326.800781f, 2.61875916f, 5.05783129f, 22.7109661f,
											296.413239f
									)
							),
							hasProperty(
									"wholeStemVolumeByUtilization",
									utilization(
											0.0085867513f, 470.388489f, 0.182312608f, 1.08978188f, 10.1118069f,
											459.004578f
									)
							),

							// Ignore intermediate close volumes, if they are wrong,
							// closeUtilizationVolumeNetOfDecayWasteAndBreakageByUtilization should also be
							// wrong

							hasProperty(
									"closeUtilizationVolumeNetOfDecayWasteAndBreakageByUtilization",
									utilization(0f, 424.163849f, 0.0895428956f, 0.929004371f, 8.9712553f, 414.174042f)
							)

					)
			);

			speciesResult = result.getSpecies().get("H");

			assertThat(speciesResult, hasProperty("polygonIdentifier", is(polygonId)));
			assertThat(speciesResult, hasProperty("layerType", is(LayerType.PRIMARY)));
			assertThat(speciesResult, hasProperty("genus", is("H")));

			assertThat(speciesResult, hasProperty("fractionGenus", closeTo(0.123297341f)));

			assertThat(speciesResult, hasProperty("speciesPercent", aMapWithSize(1)));

			assertThat(
					speciesResult,
					allOf(
							hasProperty("loreyHeightByUtilization", coe(-1, 7.93716192f, 24.3451157f)),
							hasProperty(
									"baseAreaByUtilization",
									utilization(0f, 5.50214148f, 0.311808586f, 0.736046314f, 0.988982677f, 3.4653039f)
							),
							hasProperty(
									"quadraticMeanDiameterByUtilization",
									utilization(
											6.03505516f, 21.4343796f, 10.260808f, 15.0888424f, 20.0664616f, 32.2813988f
									)
							),
							hasProperty(
									"treesPerHectareByUtilization",
									utilization(0f, 152.482513f, 37.7081375f, 41.1626587f, 31.2721119f, 42.3395996f)
							),
							hasProperty(
									"wholeStemVolumeByUtilization",
									utilization(0f, 57.2091446f, 1.57991886f, 5.59581661f, 9.53606987f, 40.4973412f)
							),

							// Ignore intermediate close volumes, if they are wrong,
							// closeUtilizationVolumeNetOfDecayWasteAndBreakageByUtilization should also be
							// wrong

							hasProperty(
									"closeUtilizationVolumeNetOfDecayWasteAndBreakageByUtilization",
									utilization(0f, 48.1333618f, 0.168331802f, 4.01862335f, 8.05745506f, 35.8889503f)
							)

					)
			);

			speciesResult = result.getSpecies().get("S");

			assertThat(speciesResult, hasProperty("polygonIdentifier", is(polygonId)));
			assertThat(speciesResult, hasProperty("layerType", is(LayerType.PRIMARY)));
			assertThat(speciesResult, hasProperty("genus", is("S")));

			assertThat(speciesResult, hasProperty("fractionGenus", closeTo(0.0918014571f)));

			assertThat(speciesResult, hasProperty("speciesPercent", aMapWithSize(1)));

			assertThat(
					speciesResult,
					allOf(
							hasProperty("loreyHeightByUtilization", coe(-1, 8.63455391f, 34.6888771f)),
							hasProperty(
									"baseAreaByUtilization",
									utilization(
											0.000575399841f, 4.0966382f, 0.0161881447f, 0.0564579964f, 0.203255415f,
											3.82073665f
									)
							),
							hasProperty(
									"quadraticMeanDiameterByUtilization",
									utilization(
											6.41802597f, 34.5382729f, 10.1304808f, 14.9457884f, 19.7497196f, 39.0729332f
									)
							),
							hasProperty(
									"treesPerHectareByUtilization",
									utilization(
											0.17785944f, 43.7256737f, 2.00838566f, 3.21808815f, 6.63483906f, 31.8643608f
									)
							),
							hasProperty(
									"wholeStemVolumeByUtilization",
									utilization(
											0.00240394124f, 57.2092552f, 0.0989032984f, 0.517220974f, 2.29273605f,
											54.300396f
									)
							),

							// Ignore intermediate close volumes, if they are wrong,
							// closeUtilizationVolumeNetOfDecayWasteAndBreakageByUtilization should also be
							// wrong

							hasProperty(
									"closeUtilizationVolumeNetOfDecayWasteAndBreakageByUtilization",
									utilization(0f, 51.6610985f, 0.0169961192f, 0.401893795f, 1.95581412f, 49.286396f)
							)

					)
			);
		}
	}

	@Test
	void testProcessPrimaryWithOverstory() throws Exception {

		var polygonId = polygonId("01002 S000002 00", 1970);

		var fipPolygon = getTestPolygon(polygonId, x -> {
			x.setBiogeoclimaticZone("CWH");
			x.setForestInventoryZone("A");
		});
		var fipLayer = getTestPrimaryLayer(polygonId, x -> {
			((FipLayer.Builder) x).crownClosure(82.8f);
			((PrimaryBuilder) x).primaryGenus(Optional.empty());
			x.inventoryTypeGroup(Optional.empty());
		}, x -> {
			x.ageTotal(45f);
			x.height(24.3f);
			x.siteIndex(28.7f);
			x.siteGenus("H");
			x.siteSpecies("H");
			x.yearsToBreastHeight(5.4f);
			x.siteCurveNumber(34);
		});
		var fipSpecies1 = getTestSpecies(polygonId, LayerType.PRIMARY, "B", x -> {
			x.setPercentGenus(15f);
		});
		var fipSpecies2 = getTestSpecies(polygonId, LayerType.PRIMARY, "D", x -> {
			x.setPercentGenus(7f);
		});
		var fipSpecies3 = getTestSpecies(polygonId, LayerType.PRIMARY, "H", x -> {
			x.setPercentGenus(77f);
		});
		var fipSpecies4 = getTestSpecies(polygonId, LayerType.PRIMARY, "S", x -> {
			x.setPercentGenus(1f);
		});
		fipPolygon.setLayers(List.of(fipLayer));
		fipLayer.setSpecies(List.of(fipSpecies1, fipSpecies2, fipSpecies3, fipSpecies4));

		var controlMap = FipTestUtils.loadControlMap();

		try (var app = new FipStart()) {
			ApplicationTestUtils.setControlMap(app, controlMap);

			var result = app.processLayerAsPrimary(fipPolygon, fipLayer, 2.24055195f);

			assertThat(result, notNullValue());

			assertThat(result, hasProperty("polygonIdentifier", is(polygonId)));
			assertThat(result, hasProperty("layerType", is(LayerType.PRIMARY)));

			assertThat(result, hasProperty("ageTotal", present(is(45f))));
			assertThat(result, hasProperty("height", present(is(24.3f))));
			assertThat(result, hasProperty("yearsToBreastHeight", present(is(5.4f))));

			assertThat(result, hasProperty("breastHeightAge", present(is(45f - 5.4f))));

			assertThat(
					result, hasProperty(
							"species", allOf(
									aMapWithSize(4), //
									hasEntry(is("B"), instanceOf(VdypSpecies.class)), //
									hasEntry(is("D"), instanceOf(VdypSpecies.class)), //
									hasEntry(is("H"), instanceOf(VdypSpecies.class)), //
									hasEntry(is("S"), instanceOf(VdypSpecies.class))
							)
					)
			);

			// Setting the primaryGenus on the FIP layer is a necessary side effect
			assertThat(fipLayer, hasProperty("primaryGenus", present(equalTo("H"))));

			var speciesResult = result.getSpecies().get("H");

			assertThat(speciesResult, hasProperty("polygonIdentifier", is(polygonId)));
			assertThat(speciesResult, hasProperty("layerType", is(LayerType.PRIMARY)));
			assertThat(speciesResult, hasProperty("genus", is("H")));

			assertThat(speciesResult, hasProperty("fractionGenus", closeTo(0.787526369f)));

			assertThat(speciesResult, hasProperty("speciesPercent", aMapWithSize(1)));

			assertThat(
					speciesResult,
					allOf(
							hasProperty("loreyHeightByUtilization", coe(-1, 7.00809479f, 20.9070625f)),
							hasProperty(
									"baseAreaByUtilization",
									utilization(
											0.512469947f, 35.401783f, 2.32033157f, 5.18892097f, 6.6573391f, 21.2351913f
									)
							),
							hasProperty(
									"quadraticMeanDiameterByUtilization",
									utilization(
											5.94023561f, 20.7426338f, 10.2836504f, 15.1184902f, 20.1040707f, 31.6741638f
									)
							),
							hasProperty(
									"treesPerHectareByUtilization",
									utilization(
											184.914597f, 1047.62891f, 279.36087f, 289.048248f, 209.72142f, 269.49826f
									)
							)

					)
			);

			assertThat(
					result,
					allOf(
							hasProperty("loreyHeightByUtilization", coe(-1, 7.01034021f, 21.1241722f)),
							hasProperty(
									"baseAreaByUtilization",
									utilization(
											0.553745031f, 44.9531403f, 2.83213019f, 6.17823505f, 8.11753464f,
											27.8252392f
									)
							),
							hasProperty(
									"quadraticMeanDiameterByUtilization",
									utilization(
											5.9399271f, 21.0548763f, 10.235322f, 15.0843554f, 20.0680523f, 32.0662689f
									)
							),
							hasProperty(
									"treesPerHectareByUtilization",
									utilization(
											199.828629f, 1291.1145f, 344.207489f, 345.717224f, 256.639709f, 344.549957f
									)
							)

					)
			);
		}
	}

	@Test
	void testEstimateVeteranLayerBaseArea() throws Exception {

		var controlMap = FipTestUtils.loadControlMap();

		try (var app = new FipStart()) {
			ApplicationTestUtils.setControlMap(app, controlMap);

			var result = app.estimateVeteranBaseArea(26.2000008f, 4f, "H", Region.COASTAL);

			assertThat(result, closeTo(2.24055195f));
		}
	}

	void populateControlMapVeteranVolumeAdjust(HashMap<String, Object> controlMap, Function<String, float[]> mapper) {
		var map = GenusDefinitionParser.getSpeciesAliases(controlMap).stream()
				.collect(Collectors.toMap(x -> x, mapper.andThen(x -> new Coefficients(x, 1))));

		controlMap.put(ControlKey.VETERAN_LAYER_VOLUME_ADJUST.name(), map);
	}

	@Test
	void testVeteranLayerLoreyHeight() throws Exception {

		var polygonId = polygonId("Test Polygon", 2023);

		var fipPolygon = getTestPolygon(polygonId, valid());
		var fipLayer = getTestVeteranLayer(polygonId, valid(), valid());
		var fipSpecies = getTestSpecies(polygonId, LayerType.VETERAN, x -> {
			var map = new LinkedHashMap<String, Float>();
			map.put("B", 100f);
			x.setSpeciesPercent(map);
		});
		fipPolygon.setLayers(Collections.singletonMap(LayerType.VETERAN, fipLayer));
		fipLayer.setSpecies(Collections.singletonMap(fipSpecies.getGenus(), fipSpecies));

		var controlMap = new HashMap<String, Object>();
		TestUtils.populateControlMapBecReal(controlMap);
		TestUtils.populateControlMapGenusReal(controlMap);
		TestUtils.populateControlMapVeteranBq(controlMap);
		TestUtils.populateControlMapEquationGroups(controlMap, (s, b) -> new int[] { 1, 1, 1 });
		TestUtils.populateControlMapVeteranDq(controlMap, (s, r) -> new float[] { 0f, 0f, 0f });
		TestUtils.populateControlMapVeteranVolAdjust(controlMap, s -> new float[] { 0f, 0f, 0f, 0f });
		TestUtils.populateControlMapWholeStemVolume(controlMap, (wholeStemMap(1)));
		TestUtils.populateControlMapCloseUtilization(controlMap, closeUtilMap(1));
		TestUtils.populateControlMapNetDecay(controlMap, closeUtilMap(1));
		FipTestUtils.populateControlMapDecayModifiers(controlMap, (s, r) -> 0f);
		TestUtils.populateControlMapNetWaste(
				controlMap, s -> new Coefficients(new float[] { 0f, 0f, 0f, 0f, 0f, 0f }, 0)
		);
		FipTestUtils.populateControlMapWasteModifiers(controlMap, (s, r) -> 0f);
		TestUtils
				.populateControlMapNetBreakage(controlMap, bgrp -> new Coefficients(new float[] { 0f, 0f, 0f, 0f }, 1));

		try (var app = new FipStart()) {
			ApplicationTestUtils.setControlMap(app, controlMap);

			var result = app.processLayerAsVeteran(fipPolygon, fipLayer);

			Matcher<Float> heightMatcher = closeTo(6f);
			Matcher<Float> zeroMatcher = is(0.0f);
			// Expect the estimated HL in 0 (-1 to 0)
			assertThat(
					result,
					hasProperty(
							"species",
							hasEntry(
									is("B"),
									hasProperty("loreyHeightByUtilization", contains(zeroMatcher, heightMatcher))
							)
					)
			);
		}

	}

	@Test
	void testVeteranLayerEquationGroups() throws Exception {

		var polygonId = polygonId("Test Polygon", 2023);

		var fipPolygon = getTestPolygon(polygonId, valid());
		var fipLayer = getTestVeteranLayer(polygonId, valid(), valid());
		var fipSpecies = getTestSpecies(polygonId, LayerType.VETERAN, x -> {
			var map = new LinkedHashMap<String, Float>();
			map.put("B", 100f);
			x.setSpeciesPercent(map);
		});
		fipPolygon.setLayers(Collections.singletonMap(LayerType.VETERAN, fipLayer));
		fipLayer.setSpecies(Collections.singletonMap(fipSpecies.getGenus(), fipSpecies));

		var controlMap = new HashMap<String, Object>();
		TestUtils.populateControlMapBecReal(controlMap);
		TestUtils.populateControlMapGenusReal(controlMap);
		TestUtils.populateControlMapVeteranBq(controlMap);
		TestUtils.populateControlMapEquationGroups(
				controlMap, (s, b) -> s.equals("B") && b.equals("BG") ? new int[] { 1, 2, 3 } : new int[] { 0, 0, 0 }
		);
		TestUtils.populateControlMapVeteranDq(controlMap, (s, r) -> new float[] { 0f, 0f, 0f });
		TestUtils.populateControlMapVeteranVolAdjust(controlMap, s -> new float[] { 0f, 0f, 0f, 0f });
		TestUtils.populateControlMapWholeStemVolume(controlMap, (wholeStemMap(1)));
		TestUtils.populateControlMapCloseUtilization(controlMap, closeUtilMap(1));
		TestUtils.populateControlMapNetDecay(controlMap, closeUtilMap(2));
		FipTestUtils.populateControlMapDecayModifiers(controlMap, (s, r) -> 0f);
		TestUtils.populateControlMapNetWaste(
				controlMap, s -> new Coefficients(new float[] { 0f, 0f, 0f, 0f, 0f, 0f }, 0)
		);
		FipTestUtils.populateControlMapWasteModifiers(controlMap, (s, r) -> 0f);
		TestUtils
				.populateControlMapNetBreakage(controlMap, bgrp -> new Coefficients(new float[] { 0f, 0f, 0f, 0f }, 1));

		try (var app = new FipStart()) {
			ApplicationTestUtils.setControlMap(app, controlMap);

			var result = app.processLayerAsVeteran(fipPolygon, fipLayer).getSpecies().get("B");

			assertThat(result, hasProperty("volumeGroup", is(1)));
			assertThat(result, hasProperty("decayGroup", is(2)));
			assertThat(result, hasProperty("breakageGroup", is(3)));
		}

	}

	@Test
	void testEstimateVeteranLayerDQ() throws Exception {

		var polygonId = polygonId("Test Polygon", 2023);

		var fipPolygon = getTestPolygon(polygonId, valid());
		var fipLayer = getTestVeteranLayer(polygonId, valid(), x -> {
			x.height(10f);
		});
		var fipSpecies1 = getTestSpecies(polygonId, LayerType.VETERAN, "B", x -> {
			var map = new LinkedHashMap<String, Float>();
			map.put("S1", 75f);
			map.put("S2", 25f);
			x.setSpeciesPercent(map);
			x.setPercentGenus(60f);
		});
		var fipSpecies2 = getTestSpecies(polygonId, LayerType.VETERAN, "C", x -> {
			var map = new LinkedHashMap<String, Float>();
			map.put("S3", 75f);
			map.put("S4", 25f);
			x.setSpeciesPercent(map);
			x.setPercentGenus(40f);
		});
		fipPolygon.setLayers(Collections.singletonMap(LayerType.VETERAN, fipLayer));
		var speciesMap = new HashMap<String, FipSpecies>();
		speciesMap.put("B", fipSpecies1);
		speciesMap.put("C", fipSpecies2);
		fipLayer.setSpecies(speciesMap);

		var controlMap = new HashMap<String, Object>();
		TestUtils.populateControlMapBecReal(controlMap);
		TestUtils.populateControlMapGenusReal(controlMap);
		TestUtils.populateControlMapEquationGroups(controlMap, (s, b) -> new int[] { 1, 1, 1 });
		TestUtils.populateControlMapVeteranBq(controlMap);
		TestUtils.populateControlMapVeteranDq(controlMap, (s, r) -> {
			if (s.equals("B") && r == Region.INTERIOR)
				return new float[] { 19.417f, 0.04354f, 1.96395f };
			else if (s.equals("C") && r == Region.INTERIOR)
				return new float[] { 22.500f, 0.00157f, 2.96382f };
			return new float[] { 0f, 0f, 0f };
		});
		TestUtils.populateControlMapVeteranVolAdjust(controlMap, s -> new float[] { 0f, 0f, 0f, 0f });
		TestUtils.populateControlMapWholeStemVolume(controlMap, (wholeStemMap(1)));
		TestUtils.populateControlMapCloseUtilization(controlMap, closeUtilMap(1));
		TestUtils.populateControlMapNetDecay(controlMap, closeUtilMap(1));
		FipTestUtils.populateControlMapDecayModifiers(controlMap, (s, r) -> 0f);
		TestUtils.populateControlMapNetWaste(
				controlMap, s -> new Coefficients(new float[] { 0f, 0f, 0f, 0f, 0f, 0f }, 0)
		);
		FipTestUtils.populateControlMapWasteModifiers(controlMap, (s, r) -> 0f);
		TestUtils
				.populateControlMapNetBreakage(controlMap, bgrp -> new Coefficients(new float[] { 0f, 0f, 0f, 0f }, 1));

		try (var app = new FipStart()) {
			ApplicationTestUtils.setControlMap(app, controlMap);

			var result = app.processLayerAsVeteran(fipPolygon, fipLayer);

			Matcher<Float> zeroMatcher = is(0.0f);
			// Expect the estimated DQ in 4 (-1 to 4)

			var expectedDqB = 19.417f + 0.04354f * (float) Math.pow(10f, 1.96395f);
			var expectedDqC = 22.500f + 0.00157f * (float) Math.pow(10f, 2.96382);

			var resultB = result.getSpecies().get("B");

			assertThat(
					resultB,
					hasProperty(
							"quadraticMeanDiameterByUtilization",
							contains(
									zeroMatcher, closeTo(expectedDqB), zeroMatcher, zeroMatcher, zeroMatcher,
									closeTo(expectedDqB)
							)
					)
			);
			assertThat(
					resultB,
					hasProperty(
							"treesPerHectareByUtilization",
							contains(
									zeroMatcher, closeTo(3.8092144f), zeroMatcher, zeroMatcher, zeroMatcher,
									closeTo(3.8092144f)
							)
					)
			);
			var resultC = result.getSpecies().get("C");
			assertThat(
					resultC,
					hasProperty(
							"quadraticMeanDiameterByUtilization",
							contains(
									zeroMatcher, closeTo(expectedDqC), zeroMatcher, zeroMatcher, zeroMatcher,
									closeTo(expectedDqC)
							)
					)
			);
			assertThat(
					resultC,
					hasProperty(
							"treesPerHectareByUtilization",
							contains(
									zeroMatcher, closeTo(2.430306f), zeroMatcher, zeroMatcher, zeroMatcher,
									closeTo(2.430306f)
							)
					)
			);
		}
	}

	static BiFunction<Integer, Integer, Optional<Coefficients>> wholeStemMap(int group) {
		return (u, g) -> {
			if (g == group) {
				switch (u) {
				case 1:
					return Optional.of(
							new Coefficients(new float[] { -1.20775998f, 0.670000017f, 1.43023002f, -0.886789978f }, 0)
					);
				case 2:
					return Optional.of(
							new Coefficients(new float[] { -1.58211005f, 0.677200019f, 1.36449003f, -0.781769991f }, 0)
					);
				case 3:
					return Optional.of(
							new Coefficients(new float[] { -1.61995006f, 0.651030004f, 1.17782998f, -0.607379973f }, 0)
					);
				case 4:
					return Optional
							.of(
									new Coefficients(
											new float[] { -0.172529995f, 0.932619989f, -0.0697899982f,
													-0.00362000009f },
											0
									)
							);
				}
			}
			return Optional.empty();
		};
	}

	static BiFunction<Integer, Integer, Optional<Coefficients>> closeUtilMap(int group) {
		return (u, g) -> {
			if (g == group) {
				switch (u) {
				case 1:
					return Optional.of(new Coefficients(new float[] { -10.6339998f, 0.835500002f, 0f }, 1));
				case 2:
					return Optional.of(new Coefficients(new float[] { -4.44999981f, 0.373400003f, 0f }, 1));
				case 3:
					return Optional.of(new Coefficients(new float[] { -0.796000004f, 0.141299993f, 0.0033499999f }, 1));
				case 4:
					return Optional.of(new Coefficients(new float[] { 2.35400009f, 0.00419999985f, 0.0247699991f }, 1));
				}
			}
			return Optional.empty();
		};
	}

	static BiFunction<Integer, Integer, Optional<Coefficients>> netDecayMap(int group) {
		return (u, g) -> {
			if (g == group) {
				switch (u) {
				case 1:
					return Optional.of(new Coefficients(new float[] { 9.84819984f, -0.224209994f, -0.814949989f }, 1));
				case 2:
					return Optional.of(new Coefficients(new float[] { 9.61330032f, -0.224209994f, -0.814949989f }, 1));
				case 3:
					return Optional.of(new Coefficients(new float[] { 9.40579987f, -0.224209994f, -0.814949989f }, 1));
				case 4:
					return Optional.of(new Coefficients(new float[] { 10.7090998f, -0.952880025f, -0.808309972f }, 1));
				}
			}
			return Optional.empty();
		};
	}

	@Test
	void testEstimateVeteranWholeStemVolume() throws Exception {
		var polygonId = polygonId("Test Polygon", 2023);

		var fipPolygon = getTestPolygon(polygonId, valid());
		var fipLayer = getTestVeteranLayer(polygonId, valid(), valid());
		var fipSpecies = getTestSpecies(polygonId, LayerType.VETERAN, valid());
		fipPolygon.setLayers(Collections.singletonMap(LayerType.VETERAN, fipLayer));
		fipLayer.setSpecies(Collections.singletonMap(fipSpecies.getGenus(), fipSpecies));

		var controlMap = new HashMap<String, Object>();
		TestUtils.populateControlMapWholeStemVolume(controlMap, wholeStemMap(12));

		try (var app = new FipStart()) {
			ApplicationTestUtils.setControlMap(app, controlMap);

			var utilizationClass = UtilizationClass.OVER225;
			var aAdjust = 0.10881f;
			var volumeGroup = 12;
			var lorieHeight = 26.2000008f;
			var quadMeanDiameterUtil = new Coefficients(new float[] { 51.8356705f, 0f, 0f, 0f, 51.8356705f }, 0);
			var baseAreaUtil = new Coefficients(new float[] { 0.492921442f, 0f, 0f, 0f, 0.492921442f }, 0);
			var wholeStemVolumeUtil = new Coefficients(new float[] { 0f, 0f, 0f, 0f, 0f }, 0);

			app.estimateWholeStemVolume(
					utilizationClass, aAdjust, volumeGroup, lorieHeight, quadMeanDiameterUtil, baseAreaUtil,
					wholeStemVolumeUtil
			);

			assertThat(wholeStemVolumeUtil, coe(0, contains(is(0f), is(0f), is(0f), is(0f), closeTo(6.11904192f))));
		}

	}

	@Test
	void testEstimateVeteranCloseUtilization() throws Exception {
		var polygonId = polygonId("Test Polygon", 2023);

		var fipPolygon = getTestPolygon(polygonId, valid());
		var fipLayer = getTestVeteranLayer(polygonId, valid(), valid());
		var fipSpecies = getTestSpecies(polygonId, LayerType.VETERAN, valid());
		fipPolygon.setLayers(Collections.singletonMap(LayerType.VETERAN, fipLayer));
		fipLayer.setSpecies(Collections.singletonMap(fipSpecies.getGenus(), fipSpecies));

		var controlMap = new HashMap<String, Object>();
		TestUtils.populateControlMapCloseUtilization(controlMap, closeUtilMap(12));

		try (var app = new FipStart()) {
			ApplicationTestUtils.setControlMap(app, controlMap);

			var utilizationClass = UtilizationClass.OVER225;
			var aAdjust = new Coefficients(new float[] { 0f, 0f, 0f, -0.0981800035f }, 1);
			var volumeGroup = 12;
			var lorieHeight = 26.2000008f;
			var quadMeanDiameterUtil = new Coefficients(new float[] { 51.8356705f, 0f, 0f, 0f, 51.8356705f }, 0);
			var wholeStemVolumeUtil = new Coefficients(new float[] { 0f, 0f, 0f, 0f, 6.11904192f }, 0);

			var closeUtilizationUtil = new Coefficients(new float[] { 0f, 0f, 0f, 0f, 0f }, 0);

			app.estimateCloseUtilizationVolume(
					utilizationClass, aAdjust, volumeGroup, lorieHeight, quadMeanDiameterUtil, wholeStemVolumeUtil,
					closeUtilizationUtil
			);

			assertThat(closeUtilizationUtil, coe(0, contains(is(0f), is(0f), is(0f), is(0f), closeTo(5.86088896f))));
		}

	}

	@Test
	void testEstimateVeteranNetDecay() throws Exception {
		var polygonId = polygonId("Test Polygon", 2023);

		var fipPolygon = getTestPolygon(polygonId, valid());
		var fipLayer = getTestVeteranLayer(polygonId, valid(), valid());
		var fipSpecies = getTestSpecies(polygonId, LayerType.VETERAN, s -> {
		});
		fipPolygon.setLayers(Collections.singletonMap(LayerType.VETERAN, fipLayer));
		fipLayer.setSpecies(Collections.singletonMap(fipSpecies.getGenus(), fipSpecies));

		var controlMap = new HashMap<String, Object>();
		TestUtils.populateControlMapNetDecay(controlMap, netDecayMap(7));
		FipTestUtils.populateControlMapDecayModifiers(
				controlMap, (s, r) -> s.equals("B") && r == Region.INTERIOR ? 0f : 0f
		);

		try (var app = new FipStart()) {
			ApplicationTestUtils.setControlMap(app, controlMap);

			var utilizationClass = UtilizationClass.OVER225;
			var aAdjust = new Coefficients(new float[] { 0f, 0f, 0f, 0.000479999988f }, 1);
			var decayGroup = 7;
			var lorieHeight = 26.2000008f;
			var breastHeightAge = 97.9000015f;
			var quadMeanDiameterUtil = new Coefficients(new float[] { 51.8356705f, 0f, 0f, 0f, 51.8356705f }, 0);
			var closeUtilizationUtil = new Coefficients(new float[] { 0f, 0f, 0f, 0f, 5.86088896f }, 0);

			var closeUtilizationNetOfDecayUtil = new Coefficients(new float[] { 0f, 0f, 0f, 0f, 0f }, 0);

			app.estimateNetDecayVolume(
					fipSpecies.getGenus(), Region.INTERIOR, utilizationClass, aAdjust, decayGroup, lorieHeight,
					breastHeightAge, quadMeanDiameterUtil, closeUtilizationUtil, closeUtilizationNetOfDecayUtil
			);

			assertThat(
					closeUtilizationNetOfDecayUtil,
					coe(0, contains(is(0f), is(0f), is(0f), is(0f), closeTo(5.64048958f)))
			);
		}

	}

	@Test
	void testEstimateVeteranNetWaste() throws Exception {
		var polygonId = polygonId("Test Polygon", 2023);

		var fipPolygon = getTestPolygon(polygonId, valid());
		var fipLayer = getTestVeteranLayer(polygonId, valid(), valid());
		var fipSpecies = getTestSpecies(polygonId, LayerType.VETERAN, s -> {
		});
		fipPolygon.setLayers(Collections.singletonMap(LayerType.VETERAN, fipLayer));
		fipLayer.setSpecies(Collections.singletonMap(fipSpecies.getGenus(), fipSpecies));

		var controlMap = new HashMap<String, Object>();
		TestUtils.populateControlMapNetWaste(controlMap, s -> s.equals("B") ? //
				new Coefficients(
						new float[] { -4.20249987f, 11.2235003f, -33.0270004f, 0.124600001f, -0.231800005f, -0.1259f },
						0
				) : //
				new Coefficients(new float[] { 0f, 0f, 0f, 0f, 0f, 0f }, 0)
		);
		FipTestUtils.populateControlMapWasteModifiers(
				controlMap, (s, r) -> s.equals("B") && r == Region.INTERIOR ? 0f : 0f
		);

		try (var app = new FipStart()) {
			ApplicationTestUtils.setControlMap(app, controlMap);

			var utilizationClass = UtilizationClass.OVER225;
			var aAdjust = new Coefficients(new float[] { 0f, 0f, 0f, -0.00295000011f }, 1);
			var lorieHeight = 26.2000008f;
			var breastHeightAge = 97.9000015f;
			var quadMeanDiameterUtil = new Coefficients(new float[] { 51.8356705f, 0f, 0f, 0f, 51.8356705f }, 0);
			var closeUtilizationUtil = new Coefficients(new float[] { 0f, 0f, 0f, 0f, 5.86088896f }, 0);
			var closeUtilizationNetOfDecayUtil = new Coefficients(new float[] { 0f, 0f, 0f, 0f, 5.64048958f }, 0);

			var closeUtilizationNetOfDecayAndWasteUtil = new Coefficients(new float[] { 0f, 0f, 0f, 0f, 0f }, 0);

			app.estimateNetDecayAndWasteVolume(
					Region.INTERIOR, utilizationClass, aAdjust, fipSpecies.getGenus(), lorieHeight, breastHeightAge,
					quadMeanDiameterUtil, closeUtilizationUtil, closeUtilizationNetOfDecayUtil,
					closeUtilizationNetOfDecayAndWasteUtil
			);

			assertThat(
					closeUtilizationNetOfDecayAndWasteUtil,
					coe(0, contains(is(0f), is(0f), is(0f), is(0f), closeTo(5.57935333f)))
			);
		}

	}

	@Test
	void testEstimateVeteranNetBreakage() throws Exception {
		var polygonId = polygonId("Test Polygon", 2023);

		var fipPolygon = getTestPolygon(polygonId, valid());
		var fipLayer = getTestVeteranLayer(polygonId, valid(), valid());
		var fipSpecies = getTestSpecies(polygonId, LayerType.VETERAN, s -> {
		});
		fipPolygon.setLayers(Collections.singletonMap(LayerType.VETERAN, fipLayer));
		fipLayer.setSpecies(Collections.singletonMap(fipSpecies.getGenus(), fipSpecies));

		var controlMap = new HashMap<String, Object>();
		TestUtils.populateControlMapNetBreakage(controlMap, bgrp -> bgrp == 5 ? //
				new Coefficients(new float[] { 2.2269001f, 0.75059998f, 4f, 6f }, 1) : //
				new Coefficients(new float[] { 0f, 0f, 0f, 0f }, 1)
		);

		try (var app = new FipStart()) {
			ApplicationTestUtils.setControlMap(app, controlMap);

			var utilizationClass = UtilizationClass.OVER225;
			var breakageGroup = 5;
			var quadMeanDiameterUtil = new Coefficients(new float[] { 51.8356705f, 0f, 0f, 0f, 51.8356705f }, 0);
			var closeUtilizationUtil = new Coefficients(new float[] { 0f, 0f, 0f, 0f, 5.86088896f }, 0);
			var closeUtilizationNetOfDecayAndWasteUtil = new Coefficients(
					new float[] { 0f, 0f, 0f, 0f, 5.57935333f }, 0
			);

			var closeUtilizationNetOfDecayWasteAndBreakageUtil = new Coefficients(
					new float[] { 0f, 0f, 0f, 0f, 0f }, 0
			);

			app.estimateNetDecayWasteAndBreakageVolume(
					utilizationClass, breakageGroup, quadMeanDiameterUtil, closeUtilizationUtil,
					closeUtilizationNetOfDecayAndWasteUtil, closeUtilizationNetOfDecayWasteAndBreakageUtil
			);

			assertThat(
					closeUtilizationNetOfDecayWasteAndBreakageUtil,
					coe(0, contains(is(0f), is(0f), is(0f), is(0f), closeTo(5.27515411f)))
			);
		}

	}

	@Test
	void testEstimatePrimaryNetBreakage() throws Exception {
		var controlMap = FipTestUtils.loadControlMap();

		try (var app = new FipStart()) {
			ApplicationTestUtils.setControlMap(app, controlMap);

			var utilizationClass = UtilizationClass.ALL;
			var breakageGroup = 20;
			var quadMeanDiameterUtil = Utils
					.utilizationVector(0f, 13.4943399f, 10.2402296f, 14.6183214f, 19.3349762f, 25.6280651f);
			var closeUtilizationUtil = Utils
					.utilizationVector(0f, 6.41845179f, 0.0353721268f, 2.99654913f, 2.23212862f, 1.1544019f);
			var closeUtilizationNetOfDecayAndWasteUtil = Utils
					.utilizationVector(0f, 6.18276405f, 0.0347718038f, 2.93580461f, 2.169273853f, 1.04291379f);

			var closeUtilizationNetOfDecayWasteAndBreakageUtil = Utils.utilizationVector();

			app.estimateNetDecayWasteAndBreakageVolume(
					utilizationClass, breakageGroup, quadMeanDiameterUtil, closeUtilizationUtil,
					closeUtilizationNetOfDecayAndWasteUtil, closeUtilizationNetOfDecayWasteAndBreakageUtil
			);

			assertThat(
					closeUtilizationNetOfDecayWasteAndBreakageUtil,
					utilization(0f, 5.989573f, 0.0337106399f, 2.84590816f, 2.10230994f, 1.00764418f)
			);
		}

	}

	@Test
	void testProcessAsVeteranLayer() throws Exception {

		var polygonId = PolygonIdentifier.split("01002 S000002 00     1970");

		var fipPolygon = getTestPolygon(polygonId, x -> {
			x.setBiogeoclimaticZone("CWH");
			x.setForestInventoryZone("A");
			x.setYieldFactor(1f);
		});

		var fipLayer = getTestVeteranLayer(polygonId, x -> {
			((FipLayer.Builder) x).crownClosure(4.0f);
		}, x -> {
			x.ageTotal(105f);
			x.height(26.2f);
			x.siteIndex(16.7f);
			x.siteGenus("H");
			x.siteSpecies("H");
			x.yearsToBreastHeight(7.1f);
		});
		var fipSpecies1 = getTestSpecies(polygonId, LayerType.VETERAN, "B", x -> {
			var map = new LinkedHashMap<String, Float>();
			x.setSpeciesPercent(map);
			x.setPercentGenus(22f);
		});
		var fipSpecies2 = getTestSpecies(polygonId, LayerType.VETERAN, "H", x -> {
			var map = new LinkedHashMap<String, Float>();
			x.setSpeciesPercent(map);
			x.setPercentGenus(60f);
		});
		var fipSpecies3 = getTestSpecies(polygonId, LayerType.VETERAN, "S", x -> {
			var map = new LinkedHashMap<String, Float>();
			x.setSpeciesPercent(map);
			x.setPercentGenus(18f);
		});
		fipPolygon.setLayers(Collections.singletonMap(LayerType.VETERAN, fipLayer));
		var speciesMap = new HashMap<String, FipSpecies>();
		speciesMap.put("B", fipSpecies1);
		speciesMap.put("H", fipSpecies2);
		speciesMap.put("S", fipSpecies3);
		fipLayer.setSpecies(speciesMap);

		var controlMap = FipTestUtils.loadControlMap();

		try (var app = new FipStart()) {
			ApplicationTestUtils.setControlMap(app, controlMap);

			var result = app.processLayerAsVeteran(fipPolygon, fipLayer);

			assertThat(result, hasProperty("polygonIdentifier", is(polygonId)));
			assertThat(result, hasProperty("layerType", is(LayerType.VETERAN)));

			assertThat(result, hasProperty("ageTotal", present(closeTo(105f)))); // LVCOM3/AGETOTLV
			assertThat(result, hasProperty("breastHeightAge", present(closeTo(97.9000015f)))); // LVCOM3/AGEBHLV
			assertThat(result, hasProperty("yearsToBreastHeight", present(closeTo(7.0999999f)))); // LVCOM3/YTBHLV
			assertThat(result, hasProperty("height", present(closeTo(26.2000008f)))); // LVCOM3/HDLV

			assertThat(result, hasProperty("species", aMapWithSize(3)));
			var resultSpeciesMap = result.getSpecies();

			assertThat(resultSpeciesMap, Matchers.hasKey("B"));
			assertThat(resultSpeciesMap, Matchers.hasKey("H"));
			assertThat(resultSpeciesMap, Matchers.hasKey("S"));

			var resultSpeciesB = resultSpeciesMap.get("B");
			var resultSpeciesH = resultSpeciesMap.get("H");
			var resultSpeciesS = resultSpeciesMap.get("S");

			assertThat(resultSpeciesB, hasProperty("genus", is("B")));
			assertThat(resultSpeciesH, hasProperty("genus", is("H")));
			assertThat(resultSpeciesS, hasProperty("genus", is("S")));

			vetUtilization("baseAreaByUtilization", matchGenerator -> {
				assertThat(result, matchGenerator.apply(2.24055195f));
				assertThat(result.getSpecies().get("B"), matchGenerator.apply(0.492921442f));
				assertThat(result.getSpecies().get("H"), matchGenerator.apply(1.34433115f));
				assertThat(result.getSpecies().get("S"), matchGenerator.apply(0.403299361f));
			});
			vetUtilization("quadraticMeanDiameterByUtilization", matchGenerator -> {
				assertThat(result, matchGenerator.apply(51.6946983f));
				assertThat(result.getSpecies().get("B"), matchGenerator.apply(51.8356705f));
				assertThat(result.getSpecies().get("H"), matchGenerator.apply(53.6141243f));
				assertThat(result.getSpecies().get("S"), matchGenerator.apply(46.4037895f));
			});
			vetUtilization("treesPerHectareByUtilization", matchGenerator -> {
				assertThat(result, matchGenerator.apply(10.6751289f));
				assertThat(result.getSpecies().get("B"), matchGenerator.apply(2.3357718f));
				assertThat(result.getSpecies().get("H"), matchGenerator.apply(5.95467329f));
				assertThat(result.getSpecies().get("S"), matchGenerator.apply(2.38468361f));
			});
			vetUtilization("wholeStemVolumeByUtilization", matchGenerator -> {
				assertThat(result.getSpecies().get("B"), matchGenerator.apply(6.11904192f));
				assertThat(result.getSpecies().get("H"), matchGenerator.apply(14.5863571f));
				assertThat(result.getSpecies().get("S"), matchGenerator.apply(4.04864883f));
				assertThat(result, matchGenerator.apply(24.7540474f));
			});
			vetUtilization("closeUtilizationVolumeByUtilization", matchGenerator -> {
				assertThat(result, matchGenerator.apply(23.6066074f));
				assertThat(result.getSpecies().get("B"), matchGenerator.apply(5.86088896f));
				assertThat(result.getSpecies().get("H"), matchGenerator.apply(13.9343023f));
				assertThat(result.getSpecies().get("S"), matchGenerator.apply(3.81141663f));
			});
			vetUtilization("closeUtilizationVolumeNetOfDecayByUtilization", matchGenerator -> {
				assertThat(result, matchGenerator.apply(22.7740307f));
				assertThat(result.getSpecies().get("B"), matchGenerator.apply(5.64048958f));
				assertThat(result.getSpecies().get("H"), matchGenerator.apply(13.3831034f));
				assertThat(result.getSpecies().get("S"), matchGenerator.apply(3.75043678f));
			});
			vetUtilization("closeUtilizationVolumeNetOfDecayAndWasteByUtilization", matchGenerator -> {
				assertThat(result, matchGenerator.apply(22.5123749f));
				assertThat(result.getSpecies().get("B"), matchGenerator.apply(5.57935333f));
				assertThat(result.getSpecies().get("H"), matchGenerator.apply(13.2065458f));
				assertThat(result.getSpecies().get("S"), matchGenerator.apply(3.72647476f));
			});
			vetUtilization("closeUtilizationVolumeNetOfDecayWasteAndBreakageByUtilization", matchGenerator -> {
				assertThat(result, matchGenerator.apply(21.3272057f));
				assertThat(result.getSpecies().get("B"), matchGenerator.apply(5.27515411f));
				assertThat(result.getSpecies().get("H"), matchGenerator.apply(12.4877129f));
				assertThat(result.getSpecies().get("S"), matchGenerator.apply(3.56433797f));
			});
		}

	}

	@Test
	void testEstimatePrimaryLayerNonPrimarySpeciesHeightEqn1() throws Exception {
		var controlMap = FipTestUtils.loadControlMap();
		try (var app = new FipStart()) {
			ApplicationTestUtils.setControlMap(app, controlMap);

			var becLookup = BecDefinitionParser.getBecs(controlMap);
			var bec = becLookup.get("CWH").get();

			var spec = VdypSpecies.build(builder -> {
				builder.polygonIdentifier("Test", 2024);
				builder.layerType(LayerType.PRIMARY);
				builder.genus("B");
				builder.percentGenus(50f);
				builder.volumeGroup(-1);
				builder.decayGroup(-1);
				builder.breakageGroup(-1);
			});
			var specPrime = VdypSpecies.build(builder -> {
				builder.polygonIdentifier("Test", 2024);
				builder.layerType(LayerType.PRIMARY);
				builder.genus("H");
				builder.percentGenus(50f);
				builder.volumeGroup(-1);
				builder.decayGroup(-1);
				builder.breakageGroup(-1);
			});

			var result = app.estimateNonPrimaryLoreyHeight(spec, specPrime, bec, 24.2999992f, 20.5984688f);

			assertThat(result, closeTo(21.5356998f));
		}
	}

	@Test
	void testEstimatePrimaryLayerNonPrimarySpeciesHeightEqn2() throws Exception {
		var controlMap = FipTestUtils.loadControlMap();
		try (var app = new FipStart()) {
			ApplicationTestUtils.setControlMap(app, controlMap);

			var becLookup = BecDefinitionParser.getBecs(controlMap);
			var bec = becLookup.get("ESSF").get();

			var spec = VdypSpecies.build(builder -> {
				builder.polygonIdentifier("Test", 2024);
				builder.layerType(LayerType.PRIMARY);
				builder.genus("B");
				builder.percentGenus(50f);
				builder.volumeGroup(-1);
				builder.decayGroup(-1);
				builder.breakageGroup(-1);
			});
			var specPrime = VdypSpecies.build(builder -> {
				builder.polygonIdentifier("Test", 2024);
				builder.layerType(LayerType.PRIMARY);
				builder.genus("D");
				builder.percentGenus(50f);
				builder.volumeGroup(-1);
				builder.decayGroup(-1);
				builder.breakageGroup(-1);
			});

			var result = app.estimateNonPrimaryLoreyHeight(spec, specPrime, bec, 35.2999992f, 33.6889763f);

			assertThat(result, closeTo(38.7456512f));
		}
	}

	void vetUtilization(String property, Consumer<Function<Float, Matcher<VdypUtilizationHolder>>> body) {
		Function<Float, Matcher<VdypUtilizationHolder>> generator = v -> hasProperty(
				property, coe(-1, contains(is(0f), closeTo(v), is(0f), is(0f), is(0f), closeTo(v)))
		);
		body.accept(generator);
	}

	@Test
	void testFindRootsForPrimaryLayerDiameterAndAreaOneSpecies() throws Exception {
		var controlMap = FipTestUtils.loadControlMap();
		var polygonId = new PolygonIdentifier("TestPolygon", 2024);
		try (var app = new FipStart()) {
			ApplicationTestUtils.setControlMap(app, controlMap);

			var becLookup = BecDefinitionParser.getBecs(controlMap);
			var bec = becLookup.get("CWH").get();

			var layer = VdypLayer.build(builder -> {
				builder.polygonIdentifier(polygonId);
				builder.layerType(LayerType.PRIMARY);
				builder.addSite(siteBuilder -> {
					siteBuilder.ageTotal(285f);
					siteBuilder.yearsToBreastHeight(11.3999996f);
					siteBuilder.height(24.3999996f);
					siteBuilder.siteGenus("Y");
				});
			});
			layer.getBaseAreaByUtilization().setCoe(0, 76.5122147f);
			layer.getTreesPerHectareByUtilization().setCoe(0, 845.805969f);
			layer.getQuadraticMeanDiameterByUtilization().setCoe(0, 33.9379082f);

			var spec = VdypSpecies.build(layer, builder -> {
				builder.genus("Y");
				builder.percentGenus(100f);
				builder.volumeGroup(-1);
				builder.decayGroup(-1);
				builder.breakageGroup(-1);
			});
			spec.setVolumeGroup(74);
			spec.setDecayGroup(63);
			spec.setBreakageGroup(31);
			spec.getLoreyHeightByUtilization().setCoe(0, 19.9850883f);

			var fipLayer = this.getTestPrimaryLayer(polygonId, l -> {
				l.inventoryTypeGroup(Optional.of(9));
				((PrimaryBuilder) l).primaryGenus(Optional.of("Y"));
			}, valid());

			app.findRootsForDiameterAndBaseArea(layer, fipLayer, bec, 2);

			assertThat(
					layer, hasProperty(
							"loreyHeightByUtilization", //
							coe(-1, 0f, 19.9850883f)
					)
			);
			assertThat(
					spec, hasProperty(
							"baseAreaByUtilization", //
							coe(-1, 0f, 76.5122147f, 0f, 0f, 0f, 0f)
					)
			);
			assertThat(
					spec, hasProperty(
							"treesPerHectareByUtilization", //
							coe(-1, 0f, 845.805969f, 0f, 0f, 0f, 0f)
					)
			);
			assertThat(
					spec, hasProperty(
							"quadraticMeanDiameterByUtilization", //
							coe(-1, 0f, 33.9379082f, 0f, 0f, 0f, 0f)
					)
			);

			assertThat(
					layer, hasProperty(
							"wholeStemVolumeByUtilization", //
							coe(-1, 0f, 571.22583f, 0f, 0f, 0f, 0f)
					)
			);
			assertThat(
					spec, hasProperty(
							"wholeStemVolumeByUtilization", //
							coe(-1, 0f, 571.22583f, 0f, 0f, 0f, 0f)
					)
			);
		}

	}

	@Test
	void testFindRootsForPrimaryLayerDiameterAndAreaMultipleSpeciesPass1() throws Exception {
		var controlMap = FipTestUtils.loadControlMap();
		var polygonId = new PolygonIdentifier("TestPolygon", 2024);
		try (var app = new FipStart()) {
			ApplicationTestUtils.setControlMap(app, controlMap);

			var becLookup = BecDefinitionParser.getBecs(controlMap);
			var bec = becLookup.get("CWH").get();

			var layer = VdypLayer.build(builder -> {
				builder.polygonIdentifier(polygonId);
				builder.layerType(LayerType.PRIMARY);
				builder.addSite(siteBuilder -> {
					siteBuilder.ageTotal(55f);
					siteBuilder.yearsToBreastHeight(1f);
					siteBuilder.height(35.2999992f);
					siteBuilder.siteGenus("B");
				});
			});
			layer.getBaseAreaByUtilization().setCoe(0, 44.6249847f);
			layer.getTreesPerHectareByUtilization().setCoe(0, 620.504883f);
			layer.getQuadraticMeanDiameterByUtilization().setCoe(0, 30.2601795f);

			/*
			 * HL[*, -1] 0 HL[0, 0] 0 BA[*, -1] BA[1, 0] VOLWS VOLCU VOL_D VOL_DW VOLDWB dqspbase,goal
			 *
			 * HL[1, 0] spec BA[0, 0] layer TPH[0, 0] layer DQ[0,0] layer INL1VGRP, INL1DGRP, INL1BGRP spec VGRPL,
			 * DGRPL, BGRPL spec Same as above AGETOTL1 layer AGEBHL1 layer YTBH hdl1
			 *
			 */
			// sp 3, 4, 5, 8, 15
			// sp B, C, D, H, S
			var spec1 = VdypSpecies.build(layer, builder -> {
				builder.genus("B");
				builder.percentGenus(1f);
				builder.volumeGroup(12);
				builder.decayGroup(7);
				builder.breakageGroup(5);
			});
			spec1.getLoreyHeightByUtilization().setCoe(0, 38.7456512f);
			var spec2 = VdypSpecies.build(layer, builder -> {
				builder.genus("C");
				builder.percentGenus(7f);
				builder.volumeGroup(20);
				builder.decayGroup(14);
				builder.breakageGroup(6);
			});

			spec2.getLoreyHeightByUtilization().setCoe(0, 22.8001652f);
			var spec3 = VdypSpecies.build(layer, builder -> {
				builder.genus("D");
				builder.percentGenus(74f);
				builder.volumeGroup(25);
				builder.decayGroup(19);
				builder.breakageGroup(12);
			});
			spec3.getLoreyHeightByUtilization().setCoe(0, 33.6889763f);
			var spec4 = VdypSpecies.build(layer, builder -> {
				builder.genus("H");
				builder.percentGenus(9f);
				builder.volumeGroup(37);
				builder.decayGroup(31);
				builder.breakageGroup(17);
			});
			spec4.getLoreyHeightByUtilization().setCoe(0, 24.3451157f);
			var spec5 = VdypSpecies.build(layer, builder -> {
				builder.genus("S");
				builder.percentGenus(9f);
				builder.volumeGroup(66);
				builder.decayGroup(54);
				builder.breakageGroup(28);
			});
			spec5.getLoreyHeightByUtilization().setCoe(0, 34.6888771f);

			Collection<VdypSpecies> specs = new ArrayList<>(5);
			specs.add(spec1);
			specs.add(spec2);
			specs.add(spec3);
			specs.add(spec4);
			specs.add(spec5);

			layer.setSpecies(specs);

			var fipLayer = this.getTestPrimaryLayer(polygonId, l -> {
				l.inventoryTypeGroup(Optional.of(9));
				((PrimaryBuilder) l).primaryGenus(Optional.of("H"));
			}, valid());

			app.findRootsForDiameterAndBaseArea(layer, fipLayer, bec, 2);

			assertThat(
					layer, hasProperty(
							"loreyHeightByUtilization", //
							coe(-1, 0f, 31.4222546f)
					)
			);
			assertThat(
					spec1, hasProperty(
							"loreyHeightByUtilization", //
							coe(-1, 0f, 38.7456512f)
					)
			);
			assertThat(
					spec2, hasProperty(
							"loreyHeightByUtilization", //
							coe(-1, 0f, 22.8001652f)
					)
			);
			assertThat(
					spec3, hasProperty(
							"loreyHeightByUtilization", //
							coe(-1, 0f, 33.6889763f)
					)
			);
			assertThat(
					spec4, hasProperty(
							"loreyHeightByUtilization", //
							coe(-1, 0f, 24.3451157f)
					)
			);
			assertThat(
					spec5, hasProperty(
							"loreyHeightByUtilization", //
							coe(-1, 0f, 34.6888771f)
					)
			);

			assertThat(
					layer, hasProperty(
							"baseAreaByUtilization", //
							coe(-1, 0f, 44.6249847f, 0f, 0f, 0f, 0f)
					)
			);
			assertThat(
					spec1, hasProperty(
							"baseAreaByUtilization", //
							coe(-1, 0f, 0.398000091f, 0f, 0f, 0f, 0f)
					)
			);
			assertThat(
					spec2, hasProperty(
							"baseAreaByUtilization", //
							coe(-1, 0f, 5.10918713f, 0f, 0f, 0f, 0f)
					)
			);
			assertThat(
					spec3, hasProperty(
							"baseAreaByUtilization", //
							coe(-1, 0f, 29.478117f, 0f, 0f, 0f, 0f)
					)
			);
			assertThat(
					spec4, hasProperty(
							"baseAreaByUtilization", //
							coe(-1, 0f, 5.52707148f, 0f, 0f, 0f, 0f)
					)
			);
			assertThat(
					spec5, hasProperty(
							"baseAreaByUtilization", //
							coe(-1, 0f, 4.11260939f, 0f, 0f, 0f, 0f)
					)
			);

			assertThat(
					layer, hasProperty(
							"treesPerHectareByUtilization", //
							coe(-1, 0f, 620.497803f, 0f, 0f, 0f, 0f)
					)
			);
			assertThat(
					spec1, hasProperty(
							"treesPerHectareByUtilization", //
							coe(-1, 0f, 5.04042435f, 0f, 0f, 0f, 0f)
					)
			);
			assertThat(
					spec2, hasProperty(
							"treesPerHectareByUtilization", //
							coe(-1, 0f, 92.9547882f, 0f, 0f, 0f, 0f)
					)
			);
			assertThat(
					spec3, hasProperty(
							"treesPerHectareByUtilization", //
							coe(-1, 0f, 325.183502f, 0f, 0f, 0f, 0f)
					)
			);
			assertThat(
					spec4, hasProperty(
							"treesPerHectareByUtilization", //
							coe(-1, 0f, 153.230591f, 0f, 0f, 0f, 0f)
					)
			);
			assertThat(
					spec5, hasProperty(
							"treesPerHectareByUtilization", //
							coe(-1, 0f, 44.0884819f, 0f, 0f, 0f, 0f)
					)
			);

			assertThat(
					layer, hasProperty(
							"quadraticMeanDiameterByUtilization", //
							coe(-1, 0f, 30.2603531f, 0f, 0f, 0f, 0f)
					)
			);
			assertThat(
					spec1, hasProperty(
							"quadraticMeanDiameterByUtilization", //
							coe(-1, 0f, 31.7075806f, 0f, 0f, 0f, 0f)
					)
			);
			assertThat(
					spec2, hasProperty(
							"quadraticMeanDiameterByUtilization", //
							coe(-1, 0f, 26.4542274f, 0f, 0f, 0f, 0f)
					)
			);
			assertThat(
					spec3, hasProperty(
							"quadraticMeanDiameterByUtilization", //
							coe(-1, 0f, 33.9735298f, 0f, 0f, 0f, 0f)
					)
			);
			assertThat(
					spec4, hasProperty(
							"quadraticMeanDiameterByUtilization", //
							coe(-1, 0f, 21.4303799f, 0f, 0f, 0f, 0f)
					)
			);
			assertThat(
					spec5, hasProperty(
							"quadraticMeanDiameterByUtilization", //
							coe(-1, 0f, 34.4628525f, 0f, 0f, 0f, 0f)
					)
			);

			assertThat(
					layer, hasProperty(
							"wholeStemVolumeByUtilization", //
							coe(-1, 0f, 638.572754f, 0f, 0f, 0f, 0f)
					)
			);
			assertThat(
					spec1, hasProperty(
							"wholeStemVolumeByUtilization", //
							coe(-1, 0f, 6.38573837f, 0f, 0f, 0f, 0f)
					)
			);
			assertThat(
					spec2, hasProperty(
							"wholeStemVolumeByUtilization", //
							coe(-1, 0f, 44.7000046f, 0f, 0f, 0f, 0f)
					)
			);
			assertThat(
					spec3, hasProperty(
							"wholeStemVolumeByUtilization", //
							coe(-1, 0f, 472.54422f, 0f, 0f, 0f, 0f)
					)
			);
			assertThat(
					spec4, hasProperty(
							"wholeStemVolumeByUtilization", //
							coe(-1, 0f, 57.471405f, 0f, 0f, 0f, 0f)
					)
			);
			assertThat(
					spec5, hasProperty(
							"wholeStemVolumeByUtilization", //
							coe(-1, 0f, 57.4714355f, 0f, 0f, 0f, 0f)
					)
			);
		}

	}

	@Test
	void testFindRootsForPrimaryLayerDiameterAndAreaMultipleSpeciesPass1Test2() throws Exception {
		var controlMap = FipTestUtils.loadControlMap();
		var polygonId = new PolygonIdentifier("TestPolygon", 2024);
		try (var app = new FipStart()) {
			ApplicationTestUtils.setControlMap(app, controlMap);

			var becLookup = BecDefinitionParser.getBecs(controlMap);
			var bec = becLookup.get("CWH").get();

			var layer = VdypLayer.build(builder -> {
				builder.polygonIdentifier(polygonId);
				builder.layerType(LayerType.PRIMARY);
				builder.addSite(siteBuilder -> {
					siteBuilder.ageTotal(45f);
					siteBuilder.yearsToBreastHeight(5.4000001f);
					siteBuilder.height(24.2999992f);
					siteBuilder.siteGenus("H");
				});
			});

			layer.getBaseAreaByUtilization().setCoe(0, 44.9531403f);
			layer.getTreesPerHectareByUtilization().setCoe(0, 1291.11597f);
			layer.getQuadraticMeanDiameterByUtilization().setCoe(0, 21.0548649f);

			/*
			 * HL[*, -1] 0 HL[0, 0] 0 BA[*, -1] BA[1, 0] VOLWS VOLCU VOL_D VOL_DW VOLDWB dqspbase,goal
			 *
			 * HL[1, 0] spec BA[0, 0] layer TPH[0, 0] layer DQ[0,0] layer INL1VGRP, INL1DGRP, INL1BGRP spec VGRPL,
			 * DGRPL, BGRPL spec Same as above AGETOTL1 layer AGEBHL1 layer YTBH hdl1
			 *
			 */
			// sp 3, 4, 5, 8, 15
			// sp B, C, D, H, S
			var spec1 = VdypSpecies.build(layer, builder -> {
				builder.genus("B");
				builder.percentGenus(15f);
				builder.volumeGroup(12);
				builder.decayGroup(7);
				builder.breakageGroup(5);
			});
			spec1.getLoreyHeightByUtilization().setCoe(0, 21.5356998f);
			var spec2 = VdypSpecies.build(layer, builder -> {
				builder.genus("D");
				builder.percentGenus(7f);
				builder.volumeGroup(25);
				builder.decayGroup(19);
				builder.breakageGroup(12);
			});
			spec2.getLoreyHeightByUtilization().setCoe(0, 22.4329224f);
			var spec3 = VdypSpecies.build(layer, builder -> {
				builder.genus("H");
				builder.percentGenus(77f);
				builder.volumeGroup(37);
				builder.decayGroup(54);
				builder.breakageGroup(28);
			});
			spec3.getLoreyHeightByUtilization().setCoe(0, 20.5984688f);
			var spec4 = VdypSpecies.build(layer, builder -> {
				builder.genus("S");
				builder.percentGenus(1f);
				builder.volumeGroup(66);
				builder.decayGroup(54);
				builder.breakageGroup(28);
			});
			spec4.getLoreyHeightByUtilization().setCoe(0, 24.0494442f);

			var fipLayer = this.getTestPrimaryLayer(polygonId, l -> {
				l.inventoryTypeGroup(Optional.of(15));
				((PrimaryBuilder) l).primaryGenus(Optional.of("H"));
			}, valid());

			app.findRootsForDiameterAndBaseArea(layer, fipLayer, bec, 2);

			// This is L1COM1/PCTL1 not FIPS/PCTVOLV
			assertThat(
					spec1, hasProperty(
							"percentGenus", //
							closeTo(13.4858189f) // Change
					)
			);
			assertThat(
					spec2, hasProperty(
							"percentGenus", //
							closeTo(6.56449223f) // Change
					)
			);
			assertThat(
					spec3, hasProperty(
							"percentGenus", //
							closeTo(79.0027771f) // Change
					)
			);
			assertThat(
					spec4, hasProperty(
							"percentGenus", //
							closeTo(0.946914673f) // Change
					)
			);

			assertThat(
					layer, hasProperty(
							"loreyHeightByUtilization", //
							coe(-1, 0f, 20.8779621f) // Changed
					)
			);
			assertThat(
					spec1, hasProperty(
							"loreyHeightByUtilization", //
							coe(-1, 0f, 21.5356998f) // No Change
					)
			);
			assertThat(
					spec2, hasProperty(
							"loreyHeightByUtilization", //
							coe(-1, 0f, 22.4329224f) // No Change
					)
			);
			assertThat(
					spec3, hasProperty(
							"loreyHeightByUtilization", //
							coe(-1, 0f, 20.5984688f) // No Change
					)
			);
			assertThat(
					spec4, hasProperty(
							"loreyHeightByUtilization", //
							coe(-1, 0f, 24.0494442f) // No Change
					)
			);

			assertThat(
					layer, hasProperty(
							"baseAreaByUtilization", //
							coe(-1, 0f, 44.9531403f, 0f, 0f, 0f, 0f) // No Change
					)
			);
			assertThat(
					spec1, hasProperty(
							"baseAreaByUtilization", //
							coe(-1, 0f, 6.06229925f, 0f, 0f, 0f, 0f) // Change
					)
			);
			assertThat(
					spec2, hasProperty(
							"baseAreaByUtilization", //
							coe(-1, 0f, 2.95094538f, 0f, 0f, 0f, 0f) // Change
					)
			);
			assertThat(
					spec3, hasProperty(
							"baseAreaByUtilization", //
							coe(-1, 0f, 35.5142288f, 0f, 0f, 0f, 0f) // Change
					)
			);
			assertThat(
					spec4, hasProperty(
							"baseAreaByUtilization", //
							coe(-1, 0f, 0.425667882f, 0f, 0f, 0f, 0f) // Change
					)
			);

			assertThat(
					layer, hasProperty(
							"treesPerHectareByUtilization", //
							coe(-1, 0f, 1291.11621f, 0f, 0f, 0f, 0f) // Change
					)
			);
			assertThat(
					spec1, hasProperty(
							"treesPerHectareByUtilization", //
							coe(-1, 0f, 175.253494f, 0f, 0f, 0f, 0f) // Change
					)
			);
			assertThat(
					spec2, hasProperty(
							"treesPerHectareByUtilization", //
							coe(-1, 0f, 52.7488708f, 0f, 0f, 0f, 0f) // Change
					)
			);
			assertThat(
					spec3, hasProperty(
							"treesPerHectareByUtilization", //
							coe(-1, 0f, 1056.43982f, 0f, 0f, 0f, 0f) // Change
					)
			);
			assertThat(
					spec4, hasProperty(
							"treesPerHectareByUtilization", //
							coe(-1, 0f, 6.67403078f, 0f, 0f, 0f, 0f) // Change
					)
			);

			assertThat(
					layer, hasProperty(
							"quadraticMeanDiameterByUtilization", //
							coe(-1, 0f, 21.054863f, 0f, 0f, 0f, 0f) // No Change
					)
			);
			assertThat(
					spec1, hasProperty(
							"quadraticMeanDiameterByUtilization", //
							coe(-1, 0f, 20.9865189f, 0f, 0f, 0f, 0f) // Change
					)
			);
			assertThat(
					spec2, hasProperty(
							"quadraticMeanDiameterByUtilization", //
							coe(-1, 0f, 26.6888008f, 0f, 0f, 0f, 0f) // Change
					)
			);
			assertThat(
					spec3, hasProperty(
							"quadraticMeanDiameterByUtilization", //
							coe(-1, 0f, 20.6887321f, 0f, 0f, 0f, 0f) // Change
					)
			);
			assertThat(
					spec4, hasProperty(
							"quadraticMeanDiameterByUtilization", //
							coe(-1, 0f, 28.4968204f, 0f, 0f, 0f, 0f) // Change
					)
			);

			assertThat(
					layer, hasProperty(
							"wholeStemVolumeByUtilization", //
							coe(-1, 0f, 393.150146f, 0f, 0f, 0f, 0f) // No Change
					)
			);
			assertThat(
					spec1, hasProperty(
							"wholeStemVolumeByUtilization", //
							coe(-1, 0f, 58.9725418f, 0f, 0f, 0f, 0f) // No Change
					)
			);
			assertThat(
					spec2, hasProperty(
							"wholeStemVolumeByUtilization", //
							coe(-1, 0f, 27.5205307f, 0f, 0f, 0f, 0f) // No Change
					)
			);
			assertThat(
					spec3, hasProperty(
							"wholeStemVolumeByUtilization", //
							coe(-1, 0f, 302.725555f, 0f, 0f, 0f, 0f) // No Change
					)
			);
			assertThat(
					spec4, hasProperty(
							"wholeStemVolumeByUtilization", //
							coe(-1, 0f, 3.93152428f, 0f, 0f, 0f, 0f) // No Change
					)
			);
		}

	}

	@Test
	void testEstimateQuadMeanDiameterForSpecies() throws Exception {
		var controlMap = FipTestUtils.loadControlMap();
		try (var app = new FipStart()) {
			ApplicationTestUtils.setControlMap(app, controlMap);

			var layer = VdypLayer.build(builder -> {
				builder.polygonIdentifier("Test", 2024);
				builder.layerType(LayerType.PRIMARY);
				builder.addSite(siteBuilder -> {
					siteBuilder.ageTotal(55f);
					siteBuilder.yearsToBreastHeight(1f);
					siteBuilder.height(32.2999992f);
					siteBuilder.siteGenus("H");
				});
			});

			// sp 3, 4, 5, 8, 15
			// sp B, C, D, H, S
			var spec1 = VdypSpecies.build(layer, builder -> {
				builder.genus("B");
				builder.volumeGroup(12);
				builder.decayGroup(7);
				builder.breakageGroup(5);
				builder.percentGenus(1f);
			});
			spec1.getLoreyHeightByUtilization().setCoe(0, 38.7456512f);
			spec1.setFractionGenus(0.00817133673f);

			var spec2 = VdypSpecies.build(layer, builder -> {
				builder.genus("C");
				builder.volumeGroup(20);
				builder.decayGroup(14);
				builder.breakageGroup(6);
				builder.percentGenus(7f);
			});
			spec2.getLoreyHeightByUtilization().setCoe(0, 22.8001652f);
			spec2.setFractionGenus(0.0972022042f);

			var spec3 = VdypSpecies.build(layer, builder -> {
				builder.genus("D");
				builder.volumeGroup(25);
				builder.decayGroup(19);
				builder.breakageGroup(12);
				builder.percentGenus(74f);
			});
			spec3.getLoreyHeightByUtilization().setCoe(0, 33.6889763f);
			spec3.setFractionGenus(0.695440531f);

			var spec4 = VdypSpecies.build(layer, builder -> {
				builder.genus("H");
				builder.volumeGroup(37);
				builder.decayGroup(31);
				builder.breakageGroup(17);
				builder.percentGenus(9f);
			});
			spec4.getLoreyHeightByUtilization().setCoe(0, 24.3451157f);
			spec4.setFractionGenus(0.117043354f);

			var spec5 = VdypSpecies.build(layer, builder -> {
				builder.genus("S");
				builder.volumeGroup(66);
				builder.decayGroup(54);
				builder.breakageGroup(28);
				builder.percentGenus(9f);
			});
			spec5.getLoreyHeightByUtilization().setCoe(0, 34.6888771f);
			spec5.setFractionGenus(0.082142584f);

			Map<String, VdypSpecies> specs = new HashMap<>();
			specs.put(spec1.getGenus(), spec1);
			specs.put(spec2.getGenus(), spec2);
			specs.put(spec3.getGenus(), spec3);
			specs.put(spec4.getGenus(), spec4);
			specs.put(spec5.getGenus(), spec5);

			float dq = app.estimateQuadMeanDiameterForSpecies(
					spec1, specs, Region.COASTAL, 30.2601795f, 44.6249847f, 620.504883f, 31.6603775f
			);

			assertThat(dq, closeTo(31.7022133f));
		}
	}

	@Test
	void testEstimateSmallComponents() throws ProcessingException {
		var controlMap = FipTestUtils.loadControlMap();
		try (var app = new FipStart()) {
			ApplicationTestUtils.setControlMap(app, controlMap);

			var fPoly = FipPolygon.build(builder -> {
				builder.polygonIdentifier("Test", 2024);
				builder.forestInventoryZone("A");
				builder.biogeoclimaticZone("CWH");
				builder.yieldFactor(1f);
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

			layer.getLoreyHeightByUtilization().setCoe(FipStart.UTIL_ALL, 31.3307209f);
			layer.getBaseAreaByUtilization().setCoe(FipStart.UTIL_ALL, 44.6249847f);
			layer.getTreesPerHectareByUtilization().setCoe(FipStart.UTIL_ALL, 620.484802f);
			layer.getQuadraticMeanDiameterByUtilization().setCoe(FipStart.UTIL_ALL, 30.2606697f);
			layer.getWholeStemVolumeByUtilization().setCoe(FipStart.UTIL_ALL, 635.659668f);

			var spec1 = VdypSpecies.build(layer, builder -> {
				builder.genus("B");
				builder.percentGenus(20f);
				builder.volumeGroup(-1);
				builder.decayGroup(-1);
				builder.breakageGroup(-1);
			});
			spec1.getLoreyHeightByUtilization().setCoe(FipStart.UTIL_ALL, 38.6004372f);
			spec1.getBaseAreaByUtilization().setCoe(FipStart.UTIL_ALL, 0.397305071f);
			spec1.getTreesPerHectareByUtilization().setCoe(FipStart.UTIL_ALL, 5.04602766f);
			spec1.getQuadraticMeanDiameterByUtilization().setCoe(FipStart.UTIL_ALL, 31.6622887f);
			spec1.getWholeStemVolumeByUtilization().setCoe(FipStart.UTIL_ALL, 635.659668f);
			var spec2 = VdypSpecies.build(layer, builder -> {
				builder.genus("C");
				builder.percentGenus(20f);
				builder.volumeGroup(-1);
				builder.decayGroup(-1);
				builder.breakageGroup(-1);
			});
			spec2.getLoreyHeightByUtilization().setCoe(FipStart.UTIL_ALL, 22.8001652f);
			spec2.getBaseAreaByUtilization().setCoe(FipStart.UTIL_ALL, 5.08774281f);
			spec2.getTreesPerHectareByUtilization().setCoe(FipStart.UTIL_ALL, 92.4298019f);
			spec2.getQuadraticMeanDiameterByUtilization().setCoe(FipStart.UTIL_ALL, 26.4735165f);
			spec2.getWholeStemVolumeByUtilization().setCoe(FipStart.UTIL_ALL, 6.35662031f);
			var spec3 = VdypSpecies.build(layer, builder -> {
				builder.genus("D");
				builder.percentGenus(20f);
				builder.volumeGroup(-1);
				builder.decayGroup(-1);
				builder.breakageGroup(-1);
			});
			spec3.getLoreyHeightByUtilization().setCoe(FipStart.UTIL_ALL, 33.5375252f);
			spec3.getBaseAreaByUtilization().setCoe(FipStart.UTIL_ALL, 29.5411568f);
			spec3.getTreesPerHectareByUtilization().setCoe(FipStart.UTIL_ALL, 326.800781f);
			spec3.getQuadraticMeanDiameterByUtilization().setCoe(FipStart.UTIL_ALL, 33.9255791f);
			spec3.getWholeStemVolumeByUtilization().setCoe(FipStart.UTIL_ALL, 44.496151f);
			var spec4 = VdypSpecies.build(layer, builder -> {
				builder.genus("H");
				builder.percentGenus(20f);
				builder.volumeGroup(-1);
				builder.decayGroup(-1);
				builder.breakageGroup(-1);
			});
			spec4.getLoreyHeightByUtilization().setCoe(FipStart.UTIL_ALL, 24.3451157f);
			spec4.getBaseAreaByUtilization().setCoe(FipStart.UTIL_ALL, 5.50214148f);
			spec4.getTreesPerHectareByUtilization().setCoe(FipStart.UTIL_ALL, 152.482513f);
			spec4.getQuadraticMeanDiameterByUtilization().setCoe(FipStart.UTIL_ALL, 21.4343796f);
			spec4.getWholeStemVolumeByUtilization().setCoe(FipStart.UTIL_ALL, 470.388489f);
			var spec5 = VdypSpecies.build(layer, builder -> {
				builder.genus("S");
				builder.percentGenus(20f);
				builder.volumeGroup(-1);
				builder.decayGroup(-1);
				builder.breakageGroup(-1);
			});
			spec5.getLoreyHeightByUtilization().setCoe(FipStart.UTIL_ALL, 34.6888771f);
			spec5.getBaseAreaByUtilization().setCoe(FipStart.UTIL_ALL, 4.0966382f);
			spec5.getTreesPerHectareByUtilization().setCoe(FipStart.UTIL_ALL, 43.7256737f);
			spec5.getQuadraticMeanDiameterByUtilization().setCoe(FipStart.UTIL_ALL, 34.5382729f);
			spec5.getWholeStemVolumeByUtilization().setCoe(FipStart.UTIL_ALL, 57.2091446f);

			layer.setSpecies(Arrays.asList(spec1, spec2, spec3, spec4, spec5));

			app.estimateSmallComponents(fPoly, layer);

			assertThat(layer.getLoreyHeightByUtilization().getCoe(FipStart.UTIL_SMALL), closeTo(7.14446497f));
			assertThat(spec1.getLoreyHeightByUtilization().getCoe(FipStart.UTIL_SMALL), closeTo(8.39441967f));
			assertThat(spec2.getLoreyHeightByUtilization().getCoe(FipStart.UTIL_SMALL), closeTo(6.61517191f));
			assertThat(spec3.getLoreyHeightByUtilization().getCoe(FipStart.UTIL_SMALL), closeTo(10.8831682f));
			assertThat(spec4.getLoreyHeightByUtilization().getCoe(FipStart.UTIL_SMALL), closeTo(7.93716192f));
			assertThat(spec5.getLoreyHeightByUtilization().getCoe(FipStart.UTIL_SMALL), closeTo(8.63455391f));

			assertThat(layer.getBaseAreaByUtilization().getCoe(FipStart.UTIL_SMALL), closeTo(0.0153773092f));
			assertThat(spec1.getBaseAreaByUtilization().getCoe(FipStart.UTIL_SMALL), closeTo(0f));
			assertThat(spec2.getBaseAreaByUtilization().getCoe(FipStart.UTIL_SMALL), closeTo(0.0131671466f));
			assertThat(spec3.getBaseAreaByUtilization().getCoe(FipStart.UTIL_SMALL), closeTo(0.00163476227f));
			assertThat(spec4.getBaseAreaByUtilization().getCoe(FipStart.UTIL_SMALL), closeTo(0f));
			assertThat(spec5.getBaseAreaByUtilization().getCoe(FipStart.UTIL_SMALL), closeTo(0.000575399841f));

			assertThat(layer.getTreesPerHectareByUtilization().getCoe(FipStart.UTIL_SMALL), closeTo(5.34804487f));
			assertThat(spec1.getTreesPerHectareByUtilization().getCoe(FipStart.UTIL_SMALL), closeTo(0f));
			assertThat(spec2.getTreesPerHectareByUtilization().getCoe(FipStart.UTIL_SMALL), closeTo(4.67143154f));
			assertThat(spec3.getTreesPerHectareByUtilization().getCoe(FipStart.UTIL_SMALL), closeTo(0.498754263f));
			assertThat(spec4.getTreesPerHectareByUtilization().getCoe(FipStart.UTIL_SMALL), closeTo(0f));
			assertThat(spec5.getTreesPerHectareByUtilization().getCoe(FipStart.UTIL_SMALL), closeTo(0.17785944f));

			assertThat(layer.getQuadraticMeanDiameterByUtilization().getCoe(FipStart.UTIL_SMALL), closeTo(6.05059004f));
			assertThat(spec1.getQuadraticMeanDiameterByUtilization().getCoe(FipStart.UTIL_SMALL), closeTo(6.13586617f));
			assertThat(spec2.getQuadraticMeanDiameterByUtilization().getCoe(FipStart.UTIL_SMALL), closeTo(5.99067688f));
			assertThat(spec3.getQuadraticMeanDiameterByUtilization().getCoe(FipStart.UTIL_SMALL), closeTo(6.46009731f));
			assertThat(spec4.getQuadraticMeanDiameterByUtilization().getCoe(FipStart.UTIL_SMALL), closeTo(6.03505516f));
			assertThat(spec5.getQuadraticMeanDiameterByUtilization().getCoe(FipStart.UTIL_SMALL), closeTo(6.41802597f));

			assertThat(layer.getWholeStemVolumeByUtilization().getCoe(FipStart.UTIL_SMALL), closeTo(0.0666879341f));
			assertThat(spec1.getWholeStemVolumeByUtilization().getCoe(FipStart.UTIL_SMALL), closeTo(0f));
			assertThat(spec2.getWholeStemVolumeByUtilization().getCoe(FipStart.UTIL_SMALL), closeTo(0.0556972362f));
			assertThat(spec3.getWholeStemVolumeByUtilization().getCoe(FipStart.UTIL_SMALL), closeTo(0.0085867513f));
			assertThat(spec4.getWholeStemVolumeByUtilization().getCoe(FipStart.UTIL_SMALL), closeTo(0f));
			assertThat(spec5.getWholeStemVolumeByUtilization().getCoe(FipStart.UTIL_SMALL), closeTo(0.00240394124f));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	void testEstimateQuadMeanDiameterByUtilization() throws ProcessingException {
		var controlMap = FipTestUtils.loadControlMap();
		try (var app = new FipStart()) {
			ApplicationTestUtils.setControlMap(app, controlMap);

			var coe = Utils.utilizationVector();
			coe.setCoe(FipStart.UTIL_ALL, 31.6622887f);

			var bec = BecDefinitionParser.getBecs(controlMap).get("CWH").get();

			var spec1 = VdypSpecies.build(builder -> {
				builder.polygonIdentifier("Test", 2024);
				builder.layerType(LayerType.PRIMARY);
				builder.genus("B");
				builder.percentGenus(100f);
				builder.volumeGroup(-1);
				builder.decayGroup(-1);
				builder.breakageGroup(-1);
			});

			app.estimateQuadMeanDiameterByUtilization(bec, coe, spec1);

			assertThat(coe, utilization(0f, 31.6622887f, 10.0594692f, 14.966774f, 19.9454956f, 46.1699982f));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	void testEstimateQuadMeanDiameterByUtilization2() throws ProcessingException {
		var controlMap = FipTestUtils.loadControlMap();
		try (var app = new FipStart()) {
			ApplicationTestUtils.setControlMap(app, controlMap);

			var coe = Utils.utilizationVector();
			coe.setCoe(FipStart.UTIL_ALL, 13.4943399f);

			var bec = BecDefinitionParser.getBecs(controlMap).get("MH").get();

			var spec1 = VdypSpecies.build(builder -> {
				builder.polygonIdentifier("Test", 2024);
				builder.layerType(LayerType.PRIMARY);
				builder.genus("L");
				builder.percentGenus(100f);
				builder.volumeGroup(-1);
				builder.decayGroup(-1);
				builder.breakageGroup(-1);
			});

			app.estimateQuadMeanDiameterByUtilization(bec, coe, spec1);

			assertThat(coe, utilization(0f, 13.4943399f, 10.2766619f, 14.67033f, 19.4037666f, 25.719244f));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	void testEstimateBaseAreaByUtilization() throws ProcessingException {
		var controlMap = FipTestUtils.loadControlMap();
		try (var app = new FipStart()) {
			ApplicationTestUtils.setControlMap(app, controlMap);

			var dq = Utils.utilizationVector();
			var ba = Utils.utilizationVector();
			dq.setCoe(0, 31.6622887f);
			dq.setCoe(1, 10.0594692f);
			dq.setCoe(2, 14.966774f);
			dq.setCoe(3, 19.9454956f);
			dq.setCoe(4, 46.1699982f);

			ba.setCoe(0, 0.397305071f);

			var bec = BecDefinitionParser.getBecs(controlMap).get("CWH").get();

			var spec1 = VdypSpecies.build(builder -> {
				builder.polygonIdentifier("Test", 2024);
				builder.layerType(LayerType.PRIMARY);
				builder.genus("B");
				builder.percentGenus(100f);
				builder.volumeGroup(-1);
				builder.decayGroup(-1);
				builder.breakageGroup(-1);
			});

			app.estimateBaseAreaByUtilization(bec, dq, ba, spec1);

			assertThat(ba, utilization(0f, 0.397305071f, 0.00485289097f, 0.0131751001f, 0.0221586525f, 0.357118428f));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	void testReconcileComponentsMode1() throws ProcessingException {
		var controlMap = FipTestUtils.loadControlMap();
		try (var app = new FipStart()) {
			ApplicationTestUtils.setControlMap(app, controlMap);

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

			assertThat(ba, utilization(0f, 2.20898318f, 0.220842764f, 0.546404183f, 1.44173622f, 0f));
			assertThat(tph, utilization(0f, 154.454025f, 49.988575f, 44.5250206f, 59.9404259f, 0f));
			assertThat(dq, utilization(0f, 13.4943399f, 7.5f, 12.5f, 17.5f, 22.5f));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	void testReconcileComponentsMode2() throws ProcessingException {
		var controlMap = FipTestUtils.loadControlMap();
		try (var app = new FipStart()) {
			ApplicationTestUtils.setControlMap(app, controlMap);

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

			assertThat(ba, utilization(0f, 0.397305071f, 0.00485289097f, 0.0131751001f, 0.0221586525f, 0.357118428f));
			assertThat(tph, utilization(0f, 5.04602766f, 0.733301044f, 0.899351299f, 0.851697803f, 2.56167722f));
			assertThat(dq, utilization(0f, 31.6622887f, 9.17939758f, 13.6573782f, 18.2005272f, 42.1307297f));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	void testReconcileComponentsMode3() throws ProcessingException {
		var controlMap = FipTestUtils.loadControlMap();
		try (var app = new FipStart()) {
			ApplicationTestUtils.setControlMap(app, controlMap);

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

			assertThat(ba, utilization(0f, 2.20898318f, 0f, 2.20898318f, 0f, 0f));
			assertThat(tph, utilization(0f, 179.71648f, 0f, 179.71648f, 0f, 0f));
			assertThat(dq, utilization(0f, 12.51f, 10, 12.51f, 20f, 25f));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	void testEstimateWholeStemVolumeByUtilizationClass() throws ProcessingException {
		var controlMap = FipTestUtils.loadControlMap();
		try (var app = new FipStart()) {
			ApplicationTestUtils.setControlMap(app, controlMap);

			var dq = Utils.utilizationVector();
			var ba = Utils.utilizationVector();
			var wsv = Utils.utilizationVector();

			dq.setCoe(0, 13.4943399f);
			dq.setCoe(1, 10.2402296f);
			dq.setCoe(2, 14.6183214f);
			dq.setCoe(3, 19.3349762f);
			dq.setCoe(4, 25.6280651f);

			ba.setCoe(0, 2.20898318f);
			ba.setCoe(1, 0.691931725f);
			ba.setCoe(2, 0.862404406f);
			ba.setCoe(3, 0.433804274f);
			ba.setCoe(4, 0.220842764f);

			wsv.setCoe(FipStart.UTIL_ALL, 11.7993851f);

			// app.estimateWholeStemVolumeByUtilizationClass(46, 14.2597857f, dq, ba, wsv);
			app.estimateWholeStemVolume(UtilizationClass.ALL, 0f, 46, 14.2597857f, dq, ba, wsv);

			assertThat(wsv, utilization(0f, 11.7993851f, 3.13278913f, 4.76524019f, 2.63645673f, 1.26489878f));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	void testComputeUtilizationComponentsPrimaryByUtilNoCV() throws ProcessingException {
		var controlMap = FipTestUtils.loadControlMap();
		try (var app = new FipStart()) {
			ApplicationTestUtils.setControlMap(app, controlMap);

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

			layer.getLoreyHeightByUtilization().setCoe(FipStart.UTIL_ALL, 13.0660105f);
			layer.getBaseAreaByUtilization().setCoe(FipStart.UTIL_ALL, 19.9786701f);
			layer.getTreesPerHectareByUtilization().setCoe(FipStart.UTIL_ALL, 1485.8208f);
			layer.getQuadraticMeanDiameterByUtilization().setCoe(FipStart.UTIL_ALL, 13.0844402f);
			layer.getWholeStemVolumeByUtilization().setCoe(FipStart.UTIL_ALL, 117.993797f);

			layer.getLoreyHeightByUtilization().setCoe(FipStart.UTIL_SMALL, 7.83768177f);
			layer.getBaseAreaByUtilization().setCoe(FipStart.UTIL_SMALL, 0.0286490358f);
			layer.getTreesPerHectareByUtilization().setCoe(FipStart.UTIL_SMALL, 9.29024601f);
			layer.getQuadraticMeanDiameterByUtilization().setCoe(FipStart.UTIL_SMALL, 6.26608753f);
			layer.getWholeStemVolumeByUtilization().setCoe(FipStart.UTIL_SMALL, 0.107688069f);

			var spec1 = VdypSpecies.build(layer, builder -> {
				builder.genus("L");
				builder.percentGenus(11.0567074f);
				builder.volumeGroup(46);
				builder.decayGroup(38);
				builder.breakageGroup(20);
			});

			spec1.getLoreyHeightByUtilization().setCoe(FipStart.UTIL_ALL, 14.2597857f);
			spec1.getBaseAreaByUtilization().setCoe(FipStart.UTIL_ALL, 2.20898318f);
			spec1.getTreesPerHectareByUtilization().setCoe(FipStart.UTIL_ALL, 154.454025f);
			spec1.getQuadraticMeanDiameterByUtilization().setCoe(FipStart.UTIL_ALL, 13.4943399f);
			spec1.getWholeStemVolumeByUtilization().setCoe(FipStart.UTIL_ALL, 11.7993851f);

			spec1.getLoreyHeightByUtilization().setCoe(FipStart.UTIL_SMALL, 7.86393309f);
			spec1.getBaseAreaByUtilization().setCoe(FipStart.UTIL_SMALL, 0.012636207f);
			spec1.getTreesPerHectareByUtilization().setCoe(FipStart.UTIL_SMALL, 3.68722916f);
			spec1.getQuadraticMeanDiameterByUtilization().setCoe(FipStart.UTIL_SMALL, 6.60561657f);
			spec1.getWholeStemVolumeByUtilization().setCoe(FipStart.UTIL_SMALL, 0.0411359742f);

			var spec2 = VdypSpecies.build(layer, builder -> {
				builder.genus("PL");
				builder.percentGenus(88.9432907f);
				builder.volumeGroup(54);
				builder.decayGroup(42);
				builder.breakageGroup(24);
			});

			spec2.getLoreyHeightByUtilization().setCoe(FipStart.UTIL_ALL, 12.9176102f);
			spec2.getBaseAreaByUtilization().setCoe(FipStart.UTIL_ALL, 17.7696857f);
			spec2.getTreesPerHectareByUtilization().setCoe(FipStart.UTIL_ALL, 1331.36682f);
			spec2.getQuadraticMeanDiameterByUtilization().setCoe(FipStart.UTIL_ALL, 13.0360518f);
			spec2.getWholeStemVolumeByUtilization().setCoe(FipStart.UTIL_ALL, 106.194412f);

			spec2.getLoreyHeightByUtilization().setCoe(FipStart.UTIL_SMALL, 7.81696558f);
			spec2.getBaseAreaByUtilization().setCoe(FipStart.UTIL_SMALL, 0.0160128288f);
			spec2.getTreesPerHectareByUtilization().setCoe(FipStart.UTIL_SMALL, 5.60301685f);
			spec2.getQuadraticMeanDiameterByUtilization().setCoe(FipStart.UTIL_SMALL, 6.03223324f);
			spec2.getWholeStemVolumeByUtilization().setCoe(FipStart.UTIL_SMALL, 0.0665520951f);

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

	@Test
	void testCreateVdypPolygon() throws ProcessingException {
		var controlMap = FipTestUtils.loadControlMap();
		try (var app = new FipStart()) {
			ApplicationTestUtils.setControlMap(app, controlMap);

			var fipPolygon = FipPolygon.build(builder -> {
				builder.polygonIdentifier("Test", 2024);
				builder.forestInventoryZone("D");
				builder.biogeoclimaticZone("IDF");
				builder.mode(PolygonMode.START);
				builder.yieldFactor(1f);
			});

			// var fipVeteranLayer = new FipLayer("Test", LayerType.VETERAN);
			var fipPrimaryLayer = FipLayerPrimary.buildPrimary(fipPolygon, builder -> {

				builder.crownClosure(60f);
				builder.addSite(siteBuilder -> {
					siteBuilder.ageTotal(60f);
					siteBuilder.yearsToBreastHeight(8.5f);
					siteBuilder.height(15f);
					siteBuilder.siteGenus("L");
					siteBuilder.siteSpecies("L");
					siteBuilder.siteIndex(5f);
				});

			});

			var processedLayers = new HashMap<LayerType, VdypLayer>();
			processedLayers.put(LayerType.PRIMARY, VdypLayer.build(builder -> {
				builder.polygonIdentifier("Test", 2024);
				builder.layerType(LayerType.PRIMARY);
				builder.addSite(siteBuilder -> {
					siteBuilder.ageTotal(60f);
					siteBuilder.yearsToBreastHeight(8.5f);
					siteBuilder.height(15f);
					siteBuilder.siteGenus("H");
				});
			}));

			FipSpecies.build(fipPrimaryLayer, builder -> {
				builder.genus("L");
				builder.percentGenus(10f);
			});
			FipSpecies.build(fipPrimaryLayer, builder -> {
				builder.genus("PL");
				builder.percentGenus(90f);
			});

			var vdypPolygon = app.createVdypPolygon(fipPolygon, processedLayers);

			assertThat(vdypPolygon, notNullValue());
			assertThat(vdypPolygon, hasProperty("layers", equalTo(processedLayers)));
			assertThat(vdypPolygon, hasProperty("percentAvailable", closeTo(90f)));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	void testCreateVdypPolygonPercentForestLandGiven() throws ProcessingException {
		var controlMap = FipTestUtils.loadControlMap();
		try (var app = new FipStart()) {
			ApplicationTestUtils.setControlMap(app, controlMap);

			var fipPolygon = FipPolygon.build(builder -> {
				builder.polygonIdentifier("Test", 2024);
				builder.forestInventoryZone("D");
				builder.biogeoclimaticZone("IDF");
				builder.mode(PolygonMode.START);
				builder.yieldFactor(1f);

				builder.percentAvailable(42f);
			});

			// var fipVeteranLayer = new FipLayer("Test", LayerType.VETERAN);
			var fipPrimaryLayer = FipLayerPrimary.buildPrimary(fipPolygon, builder -> {
				builder.crownClosure(60f);

				builder.addSite(siteBuilder -> {
					siteBuilder.ageTotal(60f);
					siteBuilder.yearsToBreastHeight(8.5f);
					siteBuilder.height(15f);

					siteBuilder.siteIndex(5f);
					siteBuilder.siteGenus("L");
					siteBuilder.siteSpecies("L");
				});
			});

			var processedLayers = new HashMap<LayerType, VdypLayer>();
			processedLayers.put(LayerType.PRIMARY, VdypLayer.build(builder -> {
				builder.polygonIdentifier("Test", 2024);
				builder.layerType(LayerType.PRIMARY);
				builder.addSite(siteBuilder -> {
					siteBuilder.ageTotal(60f);
					siteBuilder.yearsToBreastHeight(8.5f);
					siteBuilder.height(15f);
					siteBuilder.siteGenus("L");
				});
			}));

			FipSpecies.build(fipPrimaryLayer, builder -> {
				builder.polygonIdentifier("Test", 2024);
				builder.layerType(LayerType.PRIMARY);
				builder.genus("L");
				builder.percentGenus(10f);
			});
			FipSpecies.build(fipPrimaryLayer, builder -> {
				builder.polygonIdentifier("Test", 2024);
				builder.layerType(LayerType.PRIMARY);
				builder.genus("PL");
				builder.percentGenus(90f);
			});

			var vdypPolygon = app.createVdypPolygon(fipPolygon, processedLayers);

			assertThat(vdypPolygon, notNullValue());
			assertThat(vdypPolygon, hasProperty("layers", equalTo(processedLayers)));
			assertThat(vdypPolygon, hasProperty("percentAvailable", closeTo(42f)));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	void testCreateVdypPolygonFipYoung() throws ProcessingException {
		var controlMap = FipTestUtils.loadControlMap();
		try (var app = new FipStart()) {
			ApplicationTestUtils.setControlMap(app, controlMap);

			var fipPolygon = FipPolygon.build(builder -> {
				builder.polygonIdentifier("Test", 2024);
				builder.forestInventoryZone("D");
				builder.biogeoclimaticZone("IDF");
				builder.mode(PolygonMode.YOUNG);
				builder.yieldFactor(1f);
			});

			// var fipVeteranLayer = new FipLayer("Test", LayerType.VETERAN);
			var fipPrimaryLayer = FipLayerPrimary.buildPrimary(fipPolygon, builder -> {
				builder.crownClosure(60f);

				builder.addSite(siteBuilder -> {
					siteBuilder.ageTotal(60f);
					siteBuilder.yearsToBreastHeight(8.5f);
					siteBuilder.height(15f);

					siteBuilder.siteIndex(5f);
					siteBuilder.siteGenus("L");
					siteBuilder.siteSpecies("L");
				});
			});

			var processedLayers = new HashMap<LayerType, VdypLayer>();
			processedLayers.put(LayerType.PRIMARY, VdypLayer.build(builder -> {
				builder.polygonIdentifier("Test", 2024);
				builder.layerType(LayerType.PRIMARY);
				builder.addSite(siteBuilder -> {
					siteBuilder.ageTotal(60f);
					siteBuilder.yearsToBreastHeight(8.5f);
					siteBuilder.height(15f);
					siteBuilder.siteGenus("L");
				});
			}));

			FipSpecies.build(fipPrimaryLayer, builder -> {
				builder.genus("L");
				builder.percentGenus(10f);
			});
			FipSpecies.build(fipPrimaryLayer, builder -> {
				builder.genus("PL");
				builder.percentGenus(90f);
			});

			var vdypPolygon = app.createVdypPolygon(fipPolygon, processedLayers);

			assertThat(vdypPolygon, notNullValue());
			assertThat(vdypPolygon, hasProperty("layers", equalTo(processedLayers)));
			assertThat(vdypPolygon, hasProperty("percentAvailable", closeTo(100f)));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	void testApplyStockingFactor() throws ProcessingException {
		var controlMap = FipTestUtils.loadControlMap();
		@SuppressWarnings("unchecked")
		var stockingClassMap = (MatrixMap2<Character, Region, Optional<StockingClassFactor>>) controlMap
				.get(ControlKey.STOCKING_CLASS_FACTORS.name());

		stockingClassMap
				.put('R', Region.INTERIOR, Optional.of(new StockingClassFactor('R', Region.INTERIOR, 0.42f, 100)));

		try (var app = new FipStart()) {
			ApplicationTestUtils.setControlMap(app, controlMap);

			// var fipVeteranLayer = new FipLayer("Test", LayerType.VETERAN);
			var fipPrimaryLayer = FipLayerPrimary.buildPrimary(builder -> {
				builder.polygonIdentifier("Test", 2024);
				builder.crownClosure(0.9f);

				builder.addSite(siteBuilder -> {
					siteBuilder.ageTotal(60f);
					siteBuilder.yearsToBreastHeight(8.5f);
					siteBuilder.height(20f);

					siteBuilder.siteIndex(5f);
					siteBuilder.siteGenus("L");
					siteBuilder.siteSpecies("L");
				});

				builder.stockingClass('R');
			});

			var processedLayers = new HashMap<LayerType, VdypLayer>();
			processedLayers.put(LayerType.PRIMARY, VdypLayer.build(builder -> {
				builder.polygonIdentifier("Test", 2024);
				builder.layerType(LayerType.PRIMARY);
				builder.addSite(siteBuilder -> {
					siteBuilder.ageTotal(60f);
					siteBuilder.yearsToBreastHeight(8.5f);
					siteBuilder.height(20f);

					siteBuilder.siteIndex(5f);
					siteBuilder.siteGenus("L");
				});
			}));

			var vdypLayer = VdypLayer.build(builder -> {
				builder.polygonIdentifier("Test", 2024);
				builder.layerType(LayerType.PRIMARY);
				builder.addSite(siteBuilder -> {
					siteBuilder.ageTotal(60f);
					siteBuilder.yearsToBreastHeight(8.5f);
					siteBuilder.height(20f);

					siteBuilder.siteIndex(5f);
					siteBuilder.siteGenus("L");
				});
			});

			vdypLayer.setLoreyHeightByUtilization(Utils.utilizationVector(1f, 1f, 1f, 1f, 1f, 1f));
			vdypLayer.setQuadraticMeanDiameterByUtilization(Utils.utilizationVector(1f, 1f, 1f, 1f, 1f, 1f));

			vdypLayer.setBaseAreaByUtilization(Utils.utilizationVector(1f, 1f, 1f, 1f, 1f));
			vdypLayer.setTreesPerHectareByUtilization(Utils.utilizationVector(1f, 1f, 1f, 1f, 1f));
			vdypLayer.setWholeStemVolumeByUtilization(Utils.utilizationVector(1f, 1f, 1f, 1f, 1f));
			vdypLayer.setCloseUtilizationVolumeByUtilization(Utils.utilizationVector(1f, 1f, 1f, 1f, 1f));
			vdypLayer.setCloseUtilizationVolumeNetOfDecayByUtilization(Utils.utilizationVector(1f, 1f, 1f, 1f, 1f));
			vdypLayer.setCloseUtilizationVolumeNetOfDecayAndWasteByUtilization(
					Utils.utilizationVector(1f, 1f, 1f, 1f, 1f)
			);
			vdypLayer.setCloseUtilizationVolumeNetOfDecayWasteAndBreakageByUtilization(
					Utils.utilizationVector(1f, 1f, 1f, 1f, 1f)
			);

			var spec1 = VdypSpecies.build(vdypLayer, builder -> {
				builder.genus("L");
				builder.percentGenus(50f);
				builder.volumeGroup(-1);
				builder.decayGroup(-1);
				builder.breakageGroup(-1);
			});

			spec1.setLoreyHeightByUtilization(Utils.utilizationVector(1f, 1f, 1f, 1f, 1f, 1f));
			spec1.setQuadraticMeanDiameterByUtilization(Utils.utilizationVector(1f, 1f, 1f, 1f, 1f, 1f));

			spec1.setBaseAreaByUtilization(Utils.utilizationVector(1f, 1f, 1f, 1f, 1f));
			spec1.setTreesPerHectareByUtilization(Utils.utilizationVector(1f, 1f, 1f, 1f, 1f));
			spec1.setWholeStemVolumeByUtilization(Utils.utilizationVector(1f, 1f, 1f, 1f, 1f));
			spec1.setCloseUtilizationVolumeNetOfDecayByUtilization(Utils.utilizationVector(1f, 1f, 1f, 1f, 1f));
			spec1.setCloseUtilizationVolumeNetOfDecayAndWasteByUtilization(Utils.utilizationVector(1f, 1f, 1f, 1f, 1f));
			spec1.setCloseUtilizationVolumeNetOfDecayWasteAndBreakageByUtilization(
					Utils.utilizationVector(1f, 1f, 1f, 1f, 1f)
			);

			var spec2 = VdypSpecies.build(vdypLayer, builder -> {
				builder.genus("PL");
				builder.percentGenus(50f);
				builder.volumeGroup(-1);
				builder.decayGroup(-1);
				builder.breakageGroup(-1);
			});

			spec2.setLoreyHeightByUtilization(Utils.utilizationVector(1f, 1f, 1f, 1f, 1f, 1f));
			spec2.setQuadraticMeanDiameterByUtilization(Utils.utilizationVector(1f, 1f, 1f, 1f, 1f, 1f));

			spec2.setBaseAreaByUtilization(Utils.utilizationVector(1f, 1f, 1f, 1f, 1f));
			spec2.setTreesPerHectareByUtilization(Utils.utilizationVector(1f, 1f, 1f, 1f, 1f));
			spec2.setWholeStemVolumeByUtilization(Utils.utilizationVector(1f, 1f, 1f, 1f, 1f));
			spec2.setCloseUtilizationVolumeNetOfDecayByUtilization(Utils.utilizationVector(1f, 1f, 1f, 1f, 1f));
			spec2.setCloseUtilizationVolumeNetOfDecayAndWasteByUtilization(Utils.utilizationVector(1f, 1f, 1f, 1f, 1f));
			spec2.setCloseUtilizationVolumeNetOfDecayWasteAndBreakageByUtilization(
					Utils.utilizationVector(1f, 1f, 1f, 1f, 1f)
			);

			app.adjustForStocking(vdypLayer, fipPrimaryLayer, BecDefinitionParser.getBecs(controlMap).get("IDF").get());

			final var MODIFIED = utilization(0.42f, 4 * 0.42f, 0.42f, 0.42f, 0.42f, 0.42f);
			final var NEVER_MODIFIED = utilization(1f, 1f, 1f, 1f, 1f, 1f);

			assertThat(vdypLayer, hasProperty("loreyHeightByUtilization", NEVER_MODIFIED));
			assertThat(vdypLayer, hasProperty("quadraticMeanDiameterByUtilization", NEVER_MODIFIED));

			assertThat(vdypLayer, hasProperty("baseAreaByUtilization", MODIFIED));
			assertThat(vdypLayer, hasProperty("treesPerHectareByUtilization", MODIFIED));
			assertThat(vdypLayer, hasProperty("closeUtilizationVolumeNetOfDecayByUtilization", MODIFIED));
			assertThat(vdypLayer, hasProperty("closeUtilizationVolumeNetOfDecayAndWasteByUtilization", MODIFIED));
			assertThat(
					vdypLayer, hasProperty("closeUtilizationVolumeNetOfDecayWasteAndBreakageByUtilization", MODIFIED)
			);

			assertThat(spec1, hasProperty("loreyHeightByUtilization", NEVER_MODIFIED));
			assertThat(spec1, hasProperty("quadraticMeanDiameterByUtilization", NEVER_MODIFIED));

			assertThat(spec1, hasProperty("baseAreaByUtilization", MODIFIED));
			assertThat(spec1, hasProperty("treesPerHectareByUtilization", MODIFIED));
			assertThat(spec1, hasProperty("closeUtilizationVolumeNetOfDecayByUtilization", MODIFIED));
			assertThat(spec1, hasProperty("closeUtilizationVolumeNetOfDecayAndWasteByUtilization", MODIFIED));
			assertThat(spec1, hasProperty("closeUtilizationVolumeNetOfDecayWasteAndBreakageByUtilization", MODIFIED));

			assertThat(spec2, hasProperty("loreyHeightByUtilization", NEVER_MODIFIED));
			assertThat(spec2, hasProperty("quadraticMeanDiameterByUtilization", NEVER_MODIFIED));

			assertThat(spec2, hasProperty("baseAreaByUtilization", MODIFIED));
			assertThat(spec2, hasProperty("treesPerHectareByUtilization", MODIFIED));
			assertThat(spec2, hasProperty("closeUtilizationVolumeNetOfDecayByUtilization", MODIFIED));
			assertThat(spec2, hasProperty("closeUtilizationVolumeNetOfDecayAndWasteByUtilization", MODIFIED));
			assertThat(spec2, hasProperty("closeUtilizationVolumeNetOfDecayWasteAndBreakageByUtilization", MODIFIED));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Test
	void testApplyStockingFactorNoFactorForLayer() throws ProcessingException {
		var controlMap = FipTestUtils.loadControlMap();
		@SuppressWarnings("unchecked")
		var stockingClassMap = (MatrixMap2<Character, Region, StockingClassFactor>) controlMap
				.get(ControlKey.STOCKING_CLASS_FACTORS.name());

		stockingClassMap.put('R', Region.INTERIOR, new StockingClassFactor('R', Region.INTERIOR, 0.42f, 100));

		try (var app = new FipStart()) {
			ApplicationTestUtils.setControlMap(app, controlMap);

			// var fipVeteranLayer = new FipLayer("Test", LayerType.VETERAN);
			var fipPrimaryLayer = FipLayerPrimary.buildPrimary(builder -> {
				builder.polygonIdentifier("Test", 2024);
				builder.crownClosure(60f);

				builder.addSite(siteBuilder -> {
					siteBuilder.ageTotal(60f);
					siteBuilder.yearsToBreastHeight(8.5f);
					siteBuilder.height(15f);

					siteBuilder.siteIndex(5f);
					siteBuilder.siteGenus("L");
					siteBuilder.siteSpecies("L");
				});
			});

			var processedLayers = new HashMap<LayerType, VdypLayer>();
			processedLayers.put(LayerType.PRIMARY, VdypLayer.build(builder -> {
				builder.polygonIdentifier("Test", 2024);
				builder.layerType(LayerType.PRIMARY);

				builder.addSite(siteBuilder -> {
					siteBuilder.ageTotal(60f);
					siteBuilder.yearsToBreastHeight(8.5f);
					siteBuilder.height(20f);

					siteBuilder.siteIndex(5f);
					siteBuilder.siteGenus("L");
				});
			}));

			fipPrimaryLayer.setStockingClass(Optional.empty());

			var vdypLayer = VdypLayer.build(builder -> {
				builder.polygonIdentifier("Test", 2024);
				builder.layerType(LayerType.PRIMARY);

				builder.addSite(siteBuilder -> {
					siteBuilder.ageTotal(60f);
					siteBuilder.yearsToBreastHeight(8.5f);
					siteBuilder.height(20f);

					siteBuilder.siteIndex(5f);
					siteBuilder.siteGenus("L");
				});
			});
			vdypLayer.setLoreyHeightByUtilization(Utils.utilizationVector(1f, 1f, 1f, 1f, 1f, 1f));
			vdypLayer.setQuadraticMeanDiameterByUtilization(Utils.utilizationVector(1f, 1f, 1f, 1f, 1f, 1f));

			vdypLayer.setBaseAreaByUtilization(Utils.utilizationVector(1f, 1f, 1f, 1f, 1f));
			vdypLayer.setTreesPerHectareByUtilization(Utils.utilizationVector(1f, 1f, 1f, 1f, 1f));
			vdypLayer.setWholeStemVolumeByUtilization(Utils.utilizationVector(1f, 1f, 1f, 1f, 1f));
			vdypLayer.setCloseUtilizationVolumeByUtilization(Utils.utilizationVector(1f, 1f, 1f, 1f, 1f));
			vdypLayer.setCloseUtilizationVolumeNetOfDecayByUtilization(Utils.utilizationVector(1f, 1f, 1f, 1f, 1f));
			vdypLayer.setCloseUtilizationVolumeNetOfDecayAndWasteByUtilization(
					Utils.utilizationVector(1f, 1f, 1f, 1f, 1f)
			);
			vdypLayer.setCloseUtilizationVolumeNetOfDecayWasteAndBreakageByUtilization(
					Utils.utilizationVector(1f, 1f, 1f, 1f, 1f)
			);

			var spec1 = VdypSpecies.build(vdypLayer, builder -> {
				builder.genus("L");
				builder.percentGenus(50f);
				builder.volumeGroup(-1);
				builder.decayGroup(-1);
				builder.breakageGroup(-1);
			});

			spec1.setLoreyHeightByUtilization(Utils.utilizationVector(1f, 1f, 1f, 1f, 1f, 1f));
			spec1.setQuadraticMeanDiameterByUtilization(Utils.utilizationVector(1f, 1f, 1f, 1f, 1f, 1f));

			spec1.setBaseAreaByUtilization(Utils.utilizationVector(1f, 1f, 1f, 1f, 1f));
			spec1.setTreesPerHectareByUtilization(Utils.utilizationVector(1f, 1f, 1f, 1f, 1f));
			spec1.setWholeStemVolumeByUtilization(Utils.utilizationVector(1f, 1f, 1f, 1f, 1f));
			spec1.setCloseUtilizationVolumeNetOfDecayByUtilization(Utils.utilizationVector(1f, 1f, 1f, 1f, 1f));
			spec1.setCloseUtilizationVolumeNetOfDecayAndWasteByUtilization(Utils.utilizationVector(1f, 1f, 1f, 1f, 1f));
			spec1.setCloseUtilizationVolumeNetOfDecayWasteAndBreakageByUtilization(
					Utils.utilizationVector(1f, 1f, 1f, 1f, 1f)
			);

			var spec2 = VdypSpecies.build(vdypLayer, builder -> {
				builder.genus("PL");
				builder.percentGenus(50f);
				builder.volumeGroup(-1);
				builder.decayGroup(-1);
				builder.breakageGroup(-1);
			});

			spec2.setLoreyHeightByUtilization(Utils.utilizationVector(1f, 1f, 1f, 1f, 1f, 1f));
			spec2.setQuadraticMeanDiameterByUtilization(Utils.utilizationVector(1f, 1f, 1f, 1f, 1f, 1f));

			spec2.setBaseAreaByUtilization(Utils.utilizationVector(1f, 1f, 1f, 1f, 1f));
			spec2.setTreesPerHectareByUtilization(Utils.utilizationVector(1f, 1f, 1f, 1f, 1f));
			spec2.setWholeStemVolumeByUtilization(Utils.utilizationVector(1f, 1f, 1f, 1f, 1f));
			spec2.setCloseUtilizationVolumeNetOfDecayByUtilization(Utils.utilizationVector(1f, 1f, 1f, 1f, 1f));
			spec2.setCloseUtilizationVolumeNetOfDecayAndWasteByUtilization(Utils.utilizationVector(1f, 1f, 1f, 1f, 1f));
			spec2.setCloseUtilizationVolumeNetOfDecayWasteAndBreakageByUtilization(
					Utils.utilizationVector(1f, 1f, 1f, 1f, 1f)
			);

			app.adjustForStocking(vdypLayer, fipPrimaryLayer, BecDefinitionParser.getBecs(controlMap).get("IDF").get());

			final var MOFIIABLE_NOT_MODIFIED = utilization(1f, 4f, 1f, 1f, 1f, 1f);
			final var NEVER_MODIFIED = utilization(1f, 1f, 1f, 1f, 1f, 1f);

			assertThat(vdypLayer, hasProperty("loreyHeightByUtilization", NEVER_MODIFIED));
			assertThat(vdypLayer, hasProperty("quadraticMeanDiameterByUtilization", NEVER_MODIFIED));

			assertThat(vdypLayer, hasProperty("baseAreaByUtilization", MOFIIABLE_NOT_MODIFIED));
			assertThat(vdypLayer, hasProperty("treesPerHectareByUtilization", MOFIIABLE_NOT_MODIFIED));
			assertThat(vdypLayer, hasProperty("closeUtilizationVolumeNetOfDecayByUtilization", MOFIIABLE_NOT_MODIFIED));
			assertThat(
					vdypLayer,
					hasProperty("closeUtilizationVolumeNetOfDecayAndWasteByUtilization", MOFIIABLE_NOT_MODIFIED)
			);
			assertThat(
					vdypLayer,
					hasProperty("closeUtilizationVolumeNetOfDecayWasteAndBreakageByUtilization", MOFIIABLE_NOT_MODIFIED)
			);

			assertThat(spec1, hasProperty("loreyHeightByUtilization", NEVER_MODIFIED));
			assertThat(spec1, hasProperty("quadraticMeanDiameterByUtilization", NEVER_MODIFIED));

			assertThat(spec1, hasProperty("baseAreaByUtilization", MOFIIABLE_NOT_MODIFIED));
			assertThat(spec1, hasProperty("treesPerHectareByUtilization", MOFIIABLE_NOT_MODIFIED));
			assertThat(spec1, hasProperty("closeUtilizationVolumeNetOfDecayByUtilization", MOFIIABLE_NOT_MODIFIED));
			assertThat(
					spec1, hasProperty("closeUtilizationVolumeNetOfDecayAndWasteByUtilization", MOFIIABLE_NOT_MODIFIED)
			);
			assertThat(
					spec1,
					hasProperty("closeUtilizationVolumeNetOfDecayWasteAndBreakageByUtilization", MOFIIABLE_NOT_MODIFIED)
			);

			assertThat(spec2, hasProperty("loreyHeightByUtilization", NEVER_MODIFIED));
			assertThat(spec2, hasProperty("quadraticMeanDiameterByUtilization", NEVER_MODIFIED));

			assertThat(spec2, hasProperty("baseAreaByUtilization", MOFIIABLE_NOT_MODIFIED));
			assertThat(spec2, hasProperty("treesPerHectareByUtilization", MOFIIABLE_NOT_MODIFIED));
			assertThat(spec2, hasProperty("closeUtilizationVolumeNetOfDecayByUtilization", MOFIIABLE_NOT_MODIFIED));
			assertThat(
					spec2, hasProperty("closeUtilizationVolumeNetOfDecayAndWasteByUtilization", MOFIIABLE_NOT_MODIFIED)
			);
			assertThat(
					spec2,
					hasProperty("closeUtilizationVolumeNetOfDecayWasteAndBreakageByUtilization", MOFIIABLE_NOT_MODIFIED)
			);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Test
	void testApplyStockingFactorNoFactorForClass() throws ProcessingException {
		var controlMap = FipTestUtils.loadControlMap();
		@SuppressWarnings("unchecked")
		var stockingClassMap = (MatrixMap2<Character, Region, StockingClassFactor>) controlMap
				.get(ControlKey.STOCKING_CLASS_FACTORS.name());

		stockingClassMap.remove('R', Region.INTERIOR);

		try (var app = new FipStart()) {
			ApplicationTestUtils.setControlMap(app, controlMap);

			// var fipVeteranLayer = new FipLayer("Test", LayerType.VETERAN);
			var fipPrimaryLayer = FipLayerPrimary.buildPrimary(builder -> {
				builder.polygonIdentifier("Test", 2024);
				builder.crownClosure(60f);

				builder.addSite(siteBuilder -> {
					siteBuilder.ageTotal(60f);
					siteBuilder.yearsToBreastHeight(8.5f);
					siteBuilder.height(15f);

					siteBuilder.siteIndex(5f);
					siteBuilder.siteGenus("L");
					siteBuilder.siteSpecies("L");
				});
			});

			var processedLayers = new HashMap<LayerType, VdypLayer>();
			processedLayers.put(LayerType.PRIMARY, VdypLayer.build(builder -> {
				builder.polygonIdentifier("Test", 2024);
				builder.layerType(LayerType.PRIMARY);

				builder.addSite(siteBuilder -> {
					siteBuilder.ageTotal(60f);
					siteBuilder.yearsToBreastHeight(3.5f);
					siteBuilder.height(20f);

					siteBuilder.siteIndex(5f);
					siteBuilder.siteGenus("L");
				});
			}));

			fipPrimaryLayer.setStockingClass(Optional.of('R'));

			var vdypLayer = VdypLayer.build(builder -> {
				builder.polygonIdentifier("Test", 2024);
				builder.layerType(LayerType.PRIMARY);

				builder.addSite(siteBuilder -> {
					siteBuilder.ageTotal(60f);
					siteBuilder.yearsToBreastHeight(3.5f);
					siteBuilder.height(20f);

					siteBuilder.siteIndex(5f);
					siteBuilder.siteGenus("L");
				});
			});

			vdypLayer.setLoreyHeightByUtilization(Utils.utilizationVector(1f, 1f, 1f, 1f, 1f, 1f));
			vdypLayer.setQuadraticMeanDiameterByUtilization(Utils.utilizationVector(1f, 1f, 1f, 1f, 1f, 1f));

			vdypLayer.setBaseAreaByUtilization(Utils.utilizationVector(1f, 1f, 1f, 1f, 1f));
			vdypLayer.setTreesPerHectareByUtilization(Utils.utilizationVector(1f, 1f, 1f, 1f, 1f));
			vdypLayer.setWholeStemVolumeByUtilization(Utils.utilizationVector(1f, 1f, 1f, 1f, 1f));
			vdypLayer.setCloseUtilizationVolumeByUtilization(Utils.utilizationVector(1f, 1f, 1f, 1f, 1f));
			vdypLayer.setCloseUtilizationVolumeNetOfDecayByUtilization(Utils.utilizationVector(1f, 1f, 1f, 1f, 1f));
			vdypLayer.setCloseUtilizationVolumeNetOfDecayAndWasteByUtilization(
					Utils.utilizationVector(1f, 1f, 1f, 1f, 1f)
			);
			vdypLayer.setCloseUtilizationVolumeNetOfDecayWasteAndBreakageByUtilization(
					Utils.utilizationVector(1f, 1f, 1f, 1f, 1f)
			);

			var spec1 = VdypSpecies.build(vdypLayer, builder -> {
				builder.genus("L");
				builder.percentGenus(50f);
				builder.volumeGroup(-1);
				builder.decayGroup(-1);
				builder.breakageGroup(-1);
			});

			spec1.setLoreyHeightByUtilization(Utils.utilizationVector(1f, 1f, 1f, 1f, 1f, 1f));
			spec1.setQuadraticMeanDiameterByUtilization(Utils.utilizationVector(1f, 1f, 1f, 1f, 1f, 1f));

			spec1.setBaseAreaByUtilization(Utils.utilizationVector(1f, 1f, 1f, 1f, 1f));
			spec1.setTreesPerHectareByUtilization(Utils.utilizationVector(1f, 1f, 1f, 1f, 1f));
			spec1.setWholeStemVolumeByUtilization(Utils.utilizationVector(1f, 1f, 1f, 1f, 1f));
			spec1.setCloseUtilizationVolumeNetOfDecayByUtilization(Utils.utilizationVector(1f, 1f, 1f, 1f, 1f));
			spec1.setCloseUtilizationVolumeNetOfDecayAndWasteByUtilization(Utils.utilizationVector(1f, 1f, 1f, 1f, 1f));
			spec1.setCloseUtilizationVolumeNetOfDecayWasteAndBreakageByUtilization(
					Utils.utilizationVector(1f, 1f, 1f, 1f, 1f)
			);

			var spec2 = VdypSpecies.build(vdypLayer, builder -> {
				builder.genus("PL");
				builder.percentGenus(50f);
				builder.volumeGroup(-1);
				builder.decayGroup(-1);
				builder.breakageGroup(-1);
			});

			spec2.setLoreyHeightByUtilization(Utils.utilizationVector(1f, 1f, 1f, 1f, 1f, 1f));
			spec2.setQuadraticMeanDiameterByUtilization(Utils.utilizationVector(1f, 1f, 1f, 1f, 1f, 1f));

			spec2.setBaseAreaByUtilization(Utils.utilizationVector(1f, 1f, 1f, 1f, 1f));
			spec2.setTreesPerHectareByUtilization(Utils.utilizationVector(1f, 1f, 1f, 1f, 1f));
			spec2.setWholeStemVolumeByUtilization(Utils.utilizationVector(1f, 1f, 1f, 1f, 1f));
			spec2.setCloseUtilizationVolumeNetOfDecayByUtilization(Utils.utilizationVector(1f, 1f, 1f, 1f, 1f));
			spec2.setCloseUtilizationVolumeNetOfDecayAndWasteByUtilization(Utils.utilizationVector(1f, 1f, 1f, 1f, 1f));
			spec2.setCloseUtilizationVolumeNetOfDecayWasteAndBreakageByUtilization(
					Utils.utilizationVector(1f, 1f, 1f, 1f, 1f)
			);

			app.adjustForStocking(vdypLayer, fipPrimaryLayer, BecDefinitionParser.getBecs(controlMap).get("IDF").get());

			final var MODIFIABLE_NOT_MODIFIED = utilization(1f, 4f, 1f, 1f, 1f, 1f);
			final var NEVER_MODIFIED = utilization(1f, 1f, 1f, 1f, 1f, 1f);

			assertThat(vdypLayer, hasProperty("loreyHeightByUtilization", NEVER_MODIFIED));
			assertThat(vdypLayer, hasProperty("quadraticMeanDiameterByUtilization", NEVER_MODIFIED));

			assertThat(vdypLayer, hasProperty("baseAreaByUtilization", MODIFIABLE_NOT_MODIFIED));
			assertThat(vdypLayer, hasProperty("treesPerHectareByUtilization", MODIFIABLE_NOT_MODIFIED));
			assertThat(
					vdypLayer, hasProperty("closeUtilizationVolumeNetOfDecayByUtilization", MODIFIABLE_NOT_MODIFIED)
			);
			assertThat(
					vdypLayer,
					hasProperty("closeUtilizationVolumeNetOfDecayAndWasteByUtilization", MODIFIABLE_NOT_MODIFIED)
			);
			assertThat(
					vdypLayer,
					hasProperty(
							"closeUtilizationVolumeNetOfDecayWasteAndBreakageByUtilization", MODIFIABLE_NOT_MODIFIED
					)
			);

			assertThat(spec1, hasProperty("loreyHeightByUtilization", NEVER_MODIFIED));
			assertThat(spec1, hasProperty("quadraticMeanDiameterByUtilization", NEVER_MODIFIED));

			assertThat(spec1, hasProperty("baseAreaByUtilization", MODIFIABLE_NOT_MODIFIED));
			assertThat(spec1, hasProperty("treesPerHectareByUtilization", MODIFIABLE_NOT_MODIFIED));
			assertThat(spec1, hasProperty("closeUtilizationVolumeNetOfDecayByUtilization", MODIFIABLE_NOT_MODIFIED));
			assertThat(
					spec1, hasProperty("closeUtilizationVolumeNetOfDecayAndWasteByUtilization", MODIFIABLE_NOT_MODIFIED)
			);
			assertThat(
					spec1,
					hasProperty(
							"closeUtilizationVolumeNetOfDecayWasteAndBreakageByUtilization", MODIFIABLE_NOT_MODIFIED
					)
			);

			assertThat(spec2, hasProperty("loreyHeightByUtilization", NEVER_MODIFIED));
			assertThat(spec2, hasProperty("quadraticMeanDiameterByUtilization", NEVER_MODIFIED));

			assertThat(spec2, hasProperty("baseAreaByUtilization", MODIFIABLE_NOT_MODIFIED));
			assertThat(spec2, hasProperty("treesPerHectareByUtilization", MODIFIABLE_NOT_MODIFIED));
			assertThat(spec2, hasProperty("closeUtilizationVolumeNetOfDecayByUtilization", MODIFIABLE_NOT_MODIFIED));
			assertThat(
					spec2, hasProperty("closeUtilizationVolumeNetOfDecayAndWasteByUtilization", MODIFIABLE_NOT_MODIFIED)
			);
			assertThat(
					spec2,
					hasProperty(
							"closeUtilizationVolumeNetOfDecayWasteAndBreakageByUtilization", MODIFIABLE_NOT_MODIFIED
					)
			);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Test
	void testProcessPolygon() throws ProcessingException, IOException {
		var controlMap = FipTestUtils.loadControlMap();

		var poly = FipPolygon.build(builder -> {
			builder.polygonIdentifier("Test", 2024);
			builder.biogeoclimaticZone("CWH");
			builder.yieldFactor(1f);
			builder.forestInventoryZone("0");
			builder.mode(PolygonMode.START);
		});

		var layer = FipLayerPrimary.buildPrimary(poly, builder -> {
			builder.crownClosure(0.9f);

			builder.addSite(siteBuilder -> {
				siteBuilder.ageTotal(50f);
				siteBuilder.yearsToBreastHeight(2f);
				siteBuilder.height(20f);

				siteBuilder.siteIndex(1f);
				siteBuilder.siteGenus("B");
				siteBuilder.siteSpecies("B");
			});

		});

		@SuppressWarnings("unused")
		var spec = FipSpecies.build(layer, builder -> {
			builder.genus("B");
			builder.percentGenus(100f);
		});

		var app = new FipStart();
		ApplicationTestUtils.setControlMap(app, controlMap);

		var result = app.processPolygon(0, poly);

		assertThat(result, present(any(VdypPolygon.class)));

		app.close();
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

	private PolygonIdentifier polygonId(String prefix, int year) {
		return new PolygonIdentifier(prefix, year);
	}

	private static final void testWith(
			List<FipPolygon> polygons, List<Map<LayerType, FipLayer>> layers, List<Collection<FipSpecies>> species,
			TestConsumer<FipStart> test
	) throws Exception {
		testWith(new HashMap<>(), polygons, layers, species, test);
	}

	private static final void testWith(
			Map<String, Object> myControlMap, List<FipPolygon> polygons, List<Map<LayerType, FipLayer>> layers,
			List<Collection<FipSpecies>> species, TestConsumer<FipStart> test
	) throws Exception {

		VdypStartApplication<FipPolygon, FipLayer, FipSpecies, FipSite> app = new FipStart();

		Map<String, Object> controlMap = new HashMap<>();

		Map<String, Float> minima = new HashMap<>();

		minima.put(BaseControlParser.MINIMUM_HEIGHT, 5f);
		minima.put(BaseControlParser.MINIMUM_BASE_AREA, 0f);
		minima.put(BaseControlParser.MINIMUM_PREDICTED_BASE_AREA, 2f);
		minima.put(BaseControlParser.MINIMUM_VETERAN_HEIGHT, 10f);

		controlMap.put(ControlKey.MINIMA.name(), minima);

		controlMap.putAll(myControlMap);

		var control = EasyMock.createControl();

		MockStreamingParser<FipPolygon> polygonStream = mockStream(
				control, controlMap, ControlKey.FIP_INPUT_YIELD_POLY.name(), "polygonStream"
		);
		MockStreamingParser<Map<LayerType, FipLayer>> layerStream = mockStream(
				control, controlMap, ControlKey.FIP_INPUT_YIELD_LAYER.name(), "layerStream"
		);
		MockStreamingParser<Collection<FipSpecies>> speciesStream = mockStream(
				control, controlMap, ControlKey.FIP_INPUT_YIELD_LX_SP0.name(), "speciesStream"
		);

		mockWith(polygonStream, polygons);
		mockWith(layerStream, layers);
		mockWith(speciesStream, species);

		ApplicationTestUtils.setControlMap(app, controlMap);

		control.replay();

		test.accept(app, controlMap);

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

	static Map<LayerType, FipLayer> layerMap(FipLayer... layers) {
		Map<LayerType, FipLayer> result = new HashMap<>();
		for (var layer : layers) {
			result.put(layer.getLayerType(), layer);
		}
		return result;
	}

	FipPolygon getTestPolygon(PolygonIdentifier polygonId, Consumer<FipPolygon> mutator) {
		var result = FipPolygon.build(builder -> {
			builder.polygonIdentifier(polygonId);
			builder.forestInventoryZone("0");
			builder.biogeoclimaticZone("BG");
			builder.mode(PolygonMode.START);
			builder.yieldFactor(1.0f);
		});
		mutator.accept(result);
		return result;
	};

	FipLayerPrimary getTestPrimaryLayer(
			PolygonIdentifier polygonId, Consumer<FipLayerPrimary.Builder> mutator,
			Consumer<FipSite.Builder> siteMutator
	) {
		var result = FipLayerPrimary.buildPrimary(builder -> {
			builder.polygonIdentifier(polygonId);
			builder.addSite(siteBuilder -> {
				siteBuilder.ageTotal(8f);
				siteBuilder.yearsToBreastHeight(7f);
				siteBuilder.height(6f);
				siteBuilder.siteIndex(5f);
				siteBuilder.siteGenus("B");
				siteBuilder.siteSpecies("B");
				siteMutator.accept(siteBuilder);
			});

			builder.crownClosure(0.9f);
			mutator.accept(builder);
		});

		return result;
	};

	FipLayer getTestVeteranLayer(
			PolygonIdentifier polygonId, Consumer<FipLayer.Builder> mutator, Consumer<FipSite.Builder> siteMutator
	) {
		var result = FipLayer.build(builder -> {
			builder.polygonIdentifier(polygonId);
			builder.layerType(LayerType.VETERAN);

			builder.addSite(siteBuilder -> {
				siteBuilder.ageTotal(8f);
				siteBuilder.yearsToBreastHeight(7f);
				siteBuilder.height(6f);
				siteBuilder.siteIndex(5f);
				siteBuilder.siteGenus("B");
				siteBuilder.siteSpecies("B");
				siteMutator.accept(siteBuilder);
			});

			builder.crownClosure(0.9f);
			mutator.accept(builder);
		});

		return result;
	};

	FipSpecies getTestSpecies(PolygonIdentifier polygonId, LayerType layer, Consumer<FipSpecies> mutator) {
		return getTestSpecies(polygonId, layer, "B", mutator);
	};

	FipSpecies
			getTestSpecies(PolygonIdentifier polygonId, LayerType layer, String genusId, Consumer<FipSpecies> mutator) {
		var result = FipSpecies.build(builder -> {
			builder.polygonIdentifier(polygonId);
			builder.layerType(layer);
			builder.genus(genusId);
			builder.percentGenus(100.0f);
			builder.addSpecies(genusId, 100f);
		});
		mutator.accept(result);
		return result;
	};

	@FunctionalInterface
	private static interface TestConsumer<T> {
		public void accept(
				VdypStartApplication<FipPolygon, FipLayer, FipSpecies, FipSite> unit, Map<String, Object> controlMap
		) throws Exception;
	}

	Matcher<Coefficients> utilizationAllAndBiggest(float all) {
		return utilization(0f, all, 0f, 0f, 0f, all);
	}

	Matcher<Coefficients> utilization(float small, float all, float util1, float util2, float util3, float util4) {
		return new TypeSafeDiagnosingMatcher<Coefficients>() {

			boolean matchesComponent(Description description, float expected, float result) {
				boolean matches = closeTo(expected).matches(result);
				description.appendText(String.format(matches ? "%f" : "[[%f]]", result));
				return matches;
			}

			@Override
			public void describeTo(Description description) {
				String utilizationRep = String.format(
						"[Small: %f, All: %f, 7.5cm: %f, 12.5cm: %f, 17.5cm: %f, 22.5cm: %f]", small, all, util1, util2,
						util3, util4
				);
				description.appendText("A utilization vector ").appendValue(utilizationRep);
			}

			@Override
			protected boolean matchesSafely(Coefficients item, Description mismatchDescription) {
				if (item.size() != 6 || item.getIndexFrom() != -1) {
					mismatchDescription.appendText("Was not a utilization vector");
					return false;
				}
				boolean matches = true;
				mismatchDescription.appendText("Was [Small: ");
				matches &= matchesComponent(mismatchDescription, small, item.getCoe(UtilizationClass.SMALL.index));
				mismatchDescription.appendText(", All: ");
				matches &= matchesComponent(mismatchDescription, all, item.getCoe(UtilizationClass.ALL.index));
				mismatchDescription.appendText(", 7.5cm: ");
				matches &= matchesComponent(mismatchDescription, util1, item.getCoe(UtilizationClass.U75TO125.index));
				mismatchDescription.appendText(", 12.5cm: ");
				matches &= matchesComponent(mismatchDescription, util2, item.getCoe(UtilizationClass.U125TO175.index));
				mismatchDescription.appendText(", 17.5cm: ");
				matches &= matchesComponent(mismatchDescription, util3, item.getCoe(UtilizationClass.U175TO225.index));
				mismatchDescription.appendText(", 22.5cm: ");
				matches &= matchesComponent(mismatchDescription, util4, item.getCoe(UtilizationClass.OVER225.index));
				mismatchDescription.appendText("]");
				return matches;
			}

		};
	}
}
