package ca.bc.gov.nrs.vdyp.vri;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import ca.bc.gov.nrs.vdyp.application.StandProcessingException;
import ca.bc.gov.nrs.vdyp.common.ControlKey;
import ca.bc.gov.nrs.vdyp.common.Utils;
import ca.bc.gov.nrs.vdyp.io.parse.coe.BasalAreaYieldParser;
import ca.bc.gov.nrs.vdyp.io.parse.coe.UpperBoundsParser;
import ca.bc.gov.nrs.vdyp.model.BecDefinition;
import ca.bc.gov.nrs.vdyp.model.LayerType;
import ca.bc.gov.nrs.vdyp.model.PolygonMode;
import ca.bc.gov.nrs.vdyp.test.MockFileResolver;
import ca.bc.gov.nrs.vdyp.test.TestUtils;
import ca.bc.gov.nrs.vdyp.vri.model.VriPolygon;

class VriInputValidationTest {

	Map<String, Object> controlMap = new HashMap<>();

	@BeforeEach
	void setupControlMap() {
		TestUtils.populateControlMapBecReal(controlMap);
		TestUtils.populateControlMapGenusReal(controlMap);
		TestUtils.populateControlMapFromResource(controlMap, new BasalAreaYieldParser(), "YLDBA407.COE");
		TestUtils.populateControlMapFromResource(controlMap, new UpperBoundsParser(), "PCT_407.coe");
	}

	@Test
	void testPassValid() throws Exception {
		var app = new VriStart();

		MockFileResolver resolver = dummyInput();

		controlMap.put(ControlKey.MINIMA.name(), Utils.constMap(map -> {
			map.put(VriControlParser.MINIMUM_BASE_AREA, 0f);
			map.put(VriControlParser.MINIMUM_HEIGHT, 6f);
			map.put(VriControlParser.MINIMUM_PREDICTED_BASE_AREA, 2f);
			map.put(VriControlParser.MINIMUM_VETERAN_HEIGHT, VriControlParser.DEFAULT_MINIMUM_VETERAN_HEIGHT);
		}));

		app.init(resolver, controlMap);

		var poly = VriPolygon.build(pBuilder -> {
			pBuilder.polygonIdentifier("082F074/0071         2001");
			pBuilder.biogeoclimaticZone("IDF");
			pBuilder.forestInventoryZone(" ");
			pBuilder.yieldFactor(1.0f);
			pBuilder.addLayer(lBuilder -> {
				lBuilder.layerType(LayerType.PRIMARY);
				lBuilder.crownClosure(57.8f);
				lBuilder.baseArea(66.0f);
				lBuilder.treesPerHectare(850f);
				lBuilder.utilization(7.5f);
				lBuilder.empiricalRelationshipParameterIndex(76);

				// Sites
				lBuilder.addSite(iBuilder -> {
					iBuilder.siteGenus("B");
					iBuilder.siteSpecies("BL");
					iBuilder.siteCurveNumber(8);
				});
				lBuilder.addSite(iBuilder -> {
					iBuilder.ageTotal(200);
					iBuilder.height(28.0f);
					iBuilder.siteIndex(14.3f);
					iBuilder.siteGenus("C");
					iBuilder.siteSpecies("CW");
					iBuilder.yearsToBreastHeight(10.9f);
					iBuilder.breastHeightAge(189.1f);
					iBuilder.siteCurveNumber(11);
				});
				lBuilder.addSite(iBuilder -> {
					iBuilder.ageTotal(200);
					iBuilder.height(32.0f);
					iBuilder.siteIndex(14.6f);
					iBuilder.siteGenus("H");
					iBuilder.siteSpecies("HW");
					iBuilder.yearsToBreastHeight(9.7f);
					iBuilder.breastHeightAge(190.3f);
					iBuilder.siteCurveNumber(37);
				});
				lBuilder.addSite(iBuilder -> {
					iBuilder.siteGenus("S");
					iBuilder.siteSpecies("SE");
					iBuilder.siteCurveNumber(71);
				});

				// Species
				lBuilder.addSpecies(sBuilder -> {
					sBuilder.genus("B");
					sBuilder.percentGenus(3f);
					sBuilder.addSpecies("BL", 100);
				});
				lBuilder.addSpecies(sBuilder -> {
					sBuilder.genus("C");
					sBuilder.percentGenus(30f);
					sBuilder.addSpecies("CW", 100);
				});
				lBuilder.addSpecies(sBuilder -> {
					sBuilder.genus("H");
					sBuilder.percentGenus(48.9f);
					sBuilder.addSpecies("HW", 100);
				});
				lBuilder.addSpecies(sBuilder -> {
					sBuilder.genus("S");
					sBuilder.percentGenus(18.1f);
					sBuilder.addSpecies("SE", 100);
				});

				lBuilder.primaryGenus("H");

			});
			pBuilder.addLayer(lBuilder -> {
				lBuilder.layerType(LayerType.VETERAN);
				lBuilder.crownClosure(0f);
				lBuilder.baseArea(0f);
				lBuilder.treesPerHectare(0f);
				lBuilder.utilization(0f);
				// Sites
				lBuilder.addSite(iBuilder -> {
					iBuilder.ageTotal(200);
					iBuilder.height(34.0f);
					iBuilder.siteIndex(14.6f);
					iBuilder.siteGenus("H");
					iBuilder.siteSpecies("HW");
					iBuilder.yearsToBreastHeight(9.7f);
					iBuilder.breastHeightAge(190.3f);
					iBuilder.siteCurveNumber(37);
				});

				// Species
				lBuilder.addSpecies(sBuilder -> {
					sBuilder.genus("H");
					sBuilder.percentGenus(100f);
					sBuilder.addSpecies("HW", 100);
				});

				lBuilder.primaryGenus("H");
			});
		});

		PolygonMode result = assertDoesNotThrow(() -> app.checkPolygon(poly));
		assertThat(result, is(PolygonMode.START));

		app.close();
	}

	@Test
	void testFailPrimarySpeciesDontSumTo100() throws Exception {
		var app = new VriStart();

		MockFileResolver resolver = dummyInput();

		app.init(resolver, controlMap);

		var poly = VriPolygon.build(pBuilder -> {
			pBuilder.polygonIdentifier("082F074/0071         2001");
			pBuilder.biogeoclimaticZone("IDF");
			pBuilder.forestInventoryZone(" ");
			pBuilder.yieldFactor(1.0f);
			pBuilder.addLayer(lBuilder -> {
				lBuilder.layerType(LayerType.PRIMARY);
				lBuilder.crownClosure(57.8f);
				lBuilder.baseArea(66.0f);
				lBuilder.treesPerHectare(850f);
				lBuilder.utilization(7.5f);

				// Sites
				lBuilder.addSite(iBuilder -> {
					iBuilder.siteGenus("B");
					iBuilder.siteSpecies("BL");
					iBuilder.siteCurveNumber(8);
				});
				lBuilder.addSite(iBuilder -> {
					iBuilder.ageTotal(200);
					iBuilder.height(28.0f);
					iBuilder.siteIndex(14.3f);
					iBuilder.siteGenus("C");
					iBuilder.siteSpecies("CW");
					iBuilder.yearsToBreastHeight(10.9f);
					iBuilder.breastHeightAge(189.1f);
					iBuilder.siteCurveNumber(11);
				});
				lBuilder.addSite(iBuilder -> {
					iBuilder.ageTotal(200);
					iBuilder.height(32.0f);
					iBuilder.siteIndex(14.6f);
					iBuilder.siteGenus("H");
					iBuilder.siteSpecies("HW");
					iBuilder.yearsToBreastHeight(9.7f);
					iBuilder.breastHeightAge(190.3f);
					iBuilder.siteCurveNumber(37);
				});
				lBuilder.addSite(iBuilder -> {
					iBuilder.siteGenus("S");
					iBuilder.siteSpecies("SE");
					iBuilder.siteCurveNumber(71);
				});

				// Species
				lBuilder.addSpecies(sBuilder -> {
					sBuilder.genus("B");
					sBuilder.percentGenus(3f);
					sBuilder.addSpecies("BL", 100);
				});
				lBuilder.addSpecies(sBuilder -> {
					sBuilder.genus("C");
					sBuilder.percentGenus(30f);
					sBuilder.addSpecies("CW", 100);
				});
				lBuilder.addSpecies(sBuilder -> {
					sBuilder.genus("H");
					sBuilder.percentGenus(48.7f);
					sBuilder.addSpecies("HW", 100);
				});
				lBuilder.addSpecies(sBuilder -> {
					sBuilder.genus("S");
					sBuilder.percentGenus(18.1f);
					sBuilder.addSpecies("SE", 100);
				});

				lBuilder.primaryGenus("H");

			});
		});

		assertThrows(StandProcessingException.class, () -> app.checkPolygon(poly));

		app.close();
	}

	@Test
	void testFailIfMissingPrimary() throws Exception {
		var app = new VriStart();

		MockFileResolver resolver = dummyInput();

		app.init(resolver, controlMap);

		var poly = VriPolygon.build(pBuilder -> {
			pBuilder.polygonIdentifier("082F074/0071         2001");
			pBuilder.biogeoclimaticZone("IDF");
			pBuilder.forestInventoryZone(" ");
			pBuilder.yieldFactor(1.0f);
			pBuilder.addLayer(lBuilder -> {
				lBuilder.layerType(LayerType.VETERAN);
				lBuilder.crownClosure(57.8f);
				lBuilder.baseArea(66.0f);
				lBuilder.treesPerHectare(850f);
				lBuilder.utilization(7.5f);

				// Sites
				lBuilder.addSite(iBuilder -> {
					iBuilder.siteGenus("B");
					iBuilder.siteSpecies("BL");
					iBuilder.siteCurveNumber(8);
				});
				lBuilder.addSite(iBuilder -> {
					iBuilder.ageTotal(200);
					iBuilder.height(28.0f);
					iBuilder.siteIndex(14.3f);
					iBuilder.siteGenus("C");
					iBuilder.siteSpecies("CW");
					iBuilder.yearsToBreastHeight(10.9f);
					iBuilder.breastHeightAge(189.1f);
					iBuilder.siteCurveNumber(11);
				});
				lBuilder.addSite(iBuilder -> {
					iBuilder.ageTotal(200);
					iBuilder.height(32.0f);
					iBuilder.siteIndex(14.6f);
					iBuilder.siteGenus("H");
					iBuilder.siteSpecies("HW");
					iBuilder.yearsToBreastHeight(9.7f);
					iBuilder.breastHeightAge(190.3f);
					iBuilder.siteCurveNumber(37);
				});
				lBuilder.addSite(iBuilder -> {
					iBuilder.siteGenus("S");
					iBuilder.siteSpecies("SE");
					iBuilder.siteCurveNumber(71);
				});

				// Species
				lBuilder.addSpecies(sBuilder -> {
					sBuilder.genus("B");
					sBuilder.percentGenus(3f);
					sBuilder.addSpecies("BL", 100);
				});
				lBuilder.addSpecies(sBuilder -> {
					sBuilder.genus("C");
					sBuilder.percentGenus(30f);
					sBuilder.addSpecies("CW", 100);
				});
				lBuilder.addSpecies(sBuilder -> {
					sBuilder.genus("H");
					sBuilder.percentGenus(48.7f);
					sBuilder.addSpecies("HW", 100);
				});
				lBuilder.addSpecies(sBuilder -> {
					sBuilder.genus("S");
					sBuilder.percentGenus(18.1f);
					sBuilder.addSpecies("SE", 100);
				});

				lBuilder.primaryGenus("H");

			});
		});

		assertThrows(StandProcessingException.class, () -> app.checkPolygon(poly));

		app.close();
	}

	private MockFileResolver dummyInput() {
		controlMap.put(ControlKey.VRI_OUTPUT_VDYP_POLYGON.name(), "DUMMY1");
		controlMap.put(ControlKey.VRI_OUTPUT_VDYP_LAYER_BY_SPECIES.name(), "DUMMY2");
		controlMap.put(ControlKey.VRI_OUTPUT_VDYP_LAYER_BY_SP0_BY_UTIL.name(), "DUMMY3");

		MockFileResolver resolver = new MockFileResolver("Test");
		resolver.addStream("DUMMY1", (OutputStream) new ByteArrayOutputStream());
		resolver.addStream("DUMMY2", (OutputStream) new ByteArrayOutputStream());
		resolver.addStream("DUMMY3", (OutputStream) new ByteArrayOutputStream());
		return resolver;
	}

	@Test
	void testFailIfPrimaryModeYoungAndLacksAge() throws Exception {
		var app = new VriStart();

		MockFileResolver resolver = dummyInput();

		controlMap.put(ControlKey.MINIMA.name(), Utils.constMap(map -> {
			map.put(VriControlParser.MINIMUM_BASE_AREA, 0f);
			map.put(VriControlParser.MINIMUM_HEIGHT, 6f);
			map.put(VriControlParser.MINIMUM_PREDICTED_BASE_AREA, 2f);
			map.put(VriControlParser.MINIMUM_VETERAN_HEIGHT, VriControlParser.DEFAULT_MINIMUM_VETERAN_HEIGHT);
		}));

		app.init(resolver, controlMap);

		var poly = VriPolygon.build(pBuilder -> {
			pBuilder.polygonIdentifier("082F074/0071         2001");
			pBuilder.biogeoclimaticZone("IDF");
			pBuilder.forestInventoryZone(" ");
			pBuilder.yieldFactor(1.0f);
			pBuilder.mode(PolygonMode.YOUNG);
			pBuilder.addLayer(lBuilder -> {
				lBuilder.layerType(LayerType.PRIMARY);
				lBuilder.crownClosure(57.8f);
				lBuilder.baseArea(66.0f);
				lBuilder.treesPerHectare(850f);
				lBuilder.utilization(7.5f);

				// Sites
				lBuilder.addSite(iBuilder -> {
					iBuilder.siteGenus("B");
					iBuilder.siteSpecies("BL");
					iBuilder.siteCurveNumber(8);
				});
				lBuilder.addSite(iBuilder -> {
					iBuilder.ageTotal(200);
					iBuilder.height(28.0f);
					iBuilder.siteIndex(14.3f);
					iBuilder.siteGenus("C");
					iBuilder.siteSpecies("CW");
					iBuilder.yearsToBreastHeight(10.9f);
					iBuilder.breastHeightAge(189.1f);
					iBuilder.siteCurveNumber(11);
				});
				lBuilder.addSite(iBuilder -> {
					// iBuilder.ageTotal(200); // don't include age on primary species
					iBuilder.height(32.0f);
					iBuilder.siteIndex(14.6f);
					iBuilder.siteGenus("H");
					iBuilder.siteSpecies("HW");
					// iBuilder.yearsToBreastHeight(9.7f);
					// iBuilder.breastHeightAge(190.3f);
					iBuilder.siteCurveNumber(37);
				});
				lBuilder.addSite(iBuilder -> {
					iBuilder.siteGenus("S");
					iBuilder.siteSpecies("SE");
					iBuilder.siteCurveNumber(71);
				});

				// Species
				lBuilder.addSpecies(sBuilder -> {
					sBuilder.genus("B");
					sBuilder.percentGenus(3f);
					sBuilder.addSpecies("BL", 100);
				});
				lBuilder.addSpecies(sBuilder -> {
					sBuilder.genus("C");
					sBuilder.percentGenus(30f);
					sBuilder.addSpecies("CW", 100);
				});
				lBuilder.addSpecies(sBuilder -> {
					sBuilder.genus("H");
					sBuilder.percentGenus(48.9f);
					sBuilder.addSpecies("HW", 100);
				});
				lBuilder.addSpecies(sBuilder -> {
					sBuilder.genus("S");
					sBuilder.percentGenus(18.1f);
					sBuilder.addSpecies("SE", 100);
				});

				lBuilder.primaryGenus("H");
			});
		});

		assertThrows(StandProcessingException.class, () -> app.checkPolygon(poly));

		app.close();
	}

	@Test
	void testFailIfPrimaryModeYoungAndLacksTreesPerHectare() throws Exception {
		var app = new VriStart();

		MockFileResolver resolver = dummyInput();

		controlMap.put(ControlKey.MINIMA.name(), Utils.constMap(map -> {
			map.put(VriControlParser.MINIMUM_BASE_AREA, 0f);
			map.put(VriControlParser.MINIMUM_HEIGHT, 6f);
			map.put(VriControlParser.MINIMUM_PREDICTED_BASE_AREA, 2f);
			map.put(VriControlParser.MINIMUM_VETERAN_HEIGHT, VriControlParser.DEFAULT_MINIMUM_VETERAN_HEIGHT);
		}));

		app.init(resolver, controlMap);

		var poly = VriPolygon.build(pBuilder -> {
			pBuilder.polygonIdentifier("082F074/0071         2001");
			pBuilder.biogeoclimaticZone("IDF");
			pBuilder.forestInventoryZone(" ");
			pBuilder.yieldFactor(1.0f);
			pBuilder.mode(PolygonMode.YOUNG);
			pBuilder.addLayer(lBuilder -> {
				lBuilder.layerType(LayerType.PRIMARY);
				lBuilder.crownClosure(57.8f);
				lBuilder.baseArea(66.0f);
				// lBuilder.treesPerHectare(850f);
				lBuilder.utilization(7.5f);

				// Sites
				lBuilder.addSite(iBuilder -> {
					iBuilder.siteGenus("B");
					iBuilder.siteSpecies("BL");
					iBuilder.siteCurveNumber(8);
				});
				lBuilder.addSite(iBuilder -> {
					iBuilder.ageTotal(200);
					iBuilder.height(28.0f);
					iBuilder.siteIndex(14.3f);
					iBuilder.siteGenus("C");
					iBuilder.siteSpecies("CW");
					iBuilder.yearsToBreastHeight(10.9f);
					iBuilder.breastHeightAge(189.1f);
					iBuilder.siteCurveNumber(11);
				});
				lBuilder.addSite(iBuilder -> {
					iBuilder.ageTotal(200);
					iBuilder.height(32.0f);
					iBuilder.siteIndex(14.6f);
					iBuilder.siteGenus("H");
					iBuilder.siteSpecies("HW");
					iBuilder.yearsToBreastHeight(9.7f);
					iBuilder.breastHeightAge(190.3f);
					iBuilder.siteCurveNumber(37);
				});
				lBuilder.addSite(iBuilder -> {
					iBuilder.siteGenus("S");
					iBuilder.siteSpecies("SE");
					iBuilder.siteCurveNumber(71);
				});

				// Species
				lBuilder.addSpecies(sBuilder -> {
					sBuilder.genus("B");
					sBuilder.percentGenus(3f);
					sBuilder.addSpecies("BL", 100);
				});
				lBuilder.addSpecies(sBuilder -> {
					sBuilder.genus("C");
					sBuilder.percentGenus(30f);
					sBuilder.addSpecies("CW", 100);
				});
				lBuilder.addSpecies(sBuilder -> {
					sBuilder.genus("H");
					sBuilder.percentGenus(48.9f);
					sBuilder.addSpecies("HW", 100);
				});
				lBuilder.addSpecies(sBuilder -> {
					sBuilder.genus("S");
					sBuilder.percentGenus(18.1f);
					sBuilder.addSpecies("SE", 100);
				});

				lBuilder.primaryGenus("H");
			});

		});

		assertThrows(StandProcessingException.class, () -> app.checkPolygon(poly));

		app.close();
	}

	@Test
	void testValidPrimaryModeYoung() throws Exception {
		var app = new VriStart();

		MockFileResolver resolver = dummyInput();

		controlMap.put(ControlKey.MINIMA.name(), Utils.constMap(map -> {
			map.put(VriControlParser.MINIMUM_BASE_AREA, 0f);
			map.put(VriControlParser.MINIMUM_HEIGHT, 6f);
			map.put(VriControlParser.MINIMUM_PREDICTED_BASE_AREA, 2f);
			map.put(VriControlParser.MINIMUM_VETERAN_HEIGHT, VriControlParser.DEFAULT_MINIMUM_VETERAN_HEIGHT);
		}));

		app.init(resolver, controlMap);

		var poly = VriPolygon.build(pBuilder -> {
			pBuilder.polygonIdentifier("082F074/0071         2001");
			pBuilder.biogeoclimaticZone("IDF");
			pBuilder.forestInventoryZone(" ");
			pBuilder.yieldFactor(1.0f);
			pBuilder.mode(PolygonMode.YOUNG);
			pBuilder.addLayer(lBuilder -> {
				lBuilder.layerType(LayerType.PRIMARY);
				lBuilder.crownClosure(57.8f);
				lBuilder.baseArea(66.0f);
				lBuilder.treesPerHectare(850f);
				lBuilder.utilization(7.5f);
				lBuilder.empiricalRelationshipParameterIndex(76);

				// Sites
				lBuilder.addSite(iBuilder -> {
					iBuilder.siteGenus("B");
					iBuilder.siteSpecies("BL");
					iBuilder.siteCurveNumber(8);
				});
				lBuilder.addSite(iBuilder -> {
					iBuilder.ageTotal(200);
					iBuilder.height(28.0f);
					iBuilder.siteIndex(14.3f);
					iBuilder.siteGenus("C");
					iBuilder.siteSpecies("CW");
					iBuilder.yearsToBreastHeight(10.9f);
					iBuilder.breastHeightAge(189.1f);
					iBuilder.siteCurveNumber(11);
				});
				lBuilder.addSite(iBuilder -> {
					iBuilder.ageTotal(200);
					iBuilder.height(32.0f);
					iBuilder.siteIndex(14.6f);
					iBuilder.siteGenus("H");
					iBuilder.siteSpecies("HW");
					iBuilder.yearsToBreastHeight(9.7f);
					iBuilder.breastHeightAge(190.3f);
					iBuilder.siteCurveNumber(37);
				});
				lBuilder.addSite(iBuilder -> {
					iBuilder.siteGenus("S");
					iBuilder.siteSpecies("SE");
					iBuilder.siteCurveNumber(71);
				});

				// Species
				lBuilder.addSpecies(sBuilder -> {
					sBuilder.genus("B");
					sBuilder.percentGenus(3f);
					sBuilder.addSpecies("BL", 100);
				});
				lBuilder.addSpecies(sBuilder -> {
					sBuilder.genus("C");
					sBuilder.percentGenus(30f);
					sBuilder.addSpecies("CW", 100);
				});
				lBuilder.addSpecies(sBuilder -> {
					sBuilder.genus("H");
					sBuilder.percentGenus(48.9f);
					sBuilder.addSpecies("HW", 100);
				});
				lBuilder.addSpecies(sBuilder -> {
					sBuilder.genus("S");
					sBuilder.percentGenus(18.1f);
					sBuilder.addSpecies("SE", 100);
				});

				lBuilder.primaryGenus("H");
			});
		});

		PolygonMode result = assertDoesNotThrow(() -> app.checkPolygon(poly));
		assertThat(result, is(PolygonMode.YOUNG));

		app.close();
	}

	@Test
	void testValidPrimaryModeStart() throws Exception {
		var app = new VriStart();

		MockFileResolver resolver = dummyInput();

		controlMap.put(ControlKey.MINIMA.name(), Utils.constMap(map -> {
			map.put(VriControlParser.MINIMUM_BASE_AREA, 0f);
			map.put(VriControlParser.MINIMUM_HEIGHT, 6f);
			map.put(VriControlParser.MINIMUM_PREDICTED_BASE_AREA, 2f);
			map.put(VriControlParser.MINIMUM_VETERAN_HEIGHT, VriControlParser.DEFAULT_MINIMUM_VETERAN_HEIGHT);
		}));

		app.init(resolver, controlMap);

		var poly = VriPolygon.build(pBuilder -> {
			pBuilder.polygonIdentifier("082F074/0071         2001");
			pBuilder.biogeoclimaticZone("IDF");
			pBuilder.forestInventoryZone(" ");
			pBuilder.yieldFactor(1.0f);
			pBuilder.mode(PolygonMode.START);
			pBuilder.addLayer(lBuilder -> {
				lBuilder.layerType(LayerType.PRIMARY);
				lBuilder.crownClosure(57.8f);
				lBuilder.baseArea(66.0f);
				lBuilder.treesPerHectare(850f);
				lBuilder.utilization(7.5f);
				lBuilder.empiricalRelationshipParameterIndex(76);

				// Sites
				lBuilder.addSite(iBuilder -> {
					iBuilder.siteGenus("B");
					iBuilder.siteSpecies("BL");
					iBuilder.siteCurveNumber(8);
				});
				lBuilder.addSite(iBuilder -> {
					iBuilder.ageTotal(200);
					iBuilder.height(28.0f);
					iBuilder.siteIndex(14.3f);
					iBuilder.siteGenus("C");
					iBuilder.siteSpecies("CW");
					iBuilder.yearsToBreastHeight(10.9f);
					iBuilder.breastHeightAge(189.1f);
					iBuilder.siteCurveNumber(11);
				});
				lBuilder.addSite(iBuilder -> {
					iBuilder.ageTotal(200);
					iBuilder.height(32.0f);
					iBuilder.siteIndex(14.6f);
					iBuilder.siteGenus("H");
					iBuilder.siteSpecies("HW");
					iBuilder.yearsToBreastHeight(9.7f);
					iBuilder.breastHeightAge(190.3f);
					iBuilder.siteCurveNumber(37);
				});
				lBuilder.addSite(iBuilder -> {
					iBuilder.siteGenus("S");
					iBuilder.siteSpecies("SE");
					iBuilder.siteCurveNumber(71);
				});

				// Species
				lBuilder.addSpecies(sBuilder -> {
					sBuilder.genus("B");
					sBuilder.percentGenus(3f);
					sBuilder.addSpecies("BL", 100);
				});
				lBuilder.addSpecies(sBuilder -> {
					sBuilder.genus("C");
					sBuilder.percentGenus(30f);
					sBuilder.addSpecies("CW", 100);
				});
				lBuilder.addSpecies(sBuilder -> {
					sBuilder.genus("H");
					sBuilder.percentGenus(48.9f);
					sBuilder.addSpecies("HW", 100);
				});
				lBuilder.addSpecies(sBuilder -> {
					sBuilder.genus("S");
					sBuilder.percentGenus(18.1f);
					sBuilder.addSpecies("SE", 100);
				});

				lBuilder.primaryGenus("H");
			});
		});

		PolygonMode result = assertDoesNotThrow(() -> app.checkPolygon(poly));
		assertThat(result, is(PolygonMode.START));

		app.close();
	}

	@Test
	void testFailIfPrimaryModeStartMissingHeight() throws Exception {
		var app = new VriStart();

		MockFileResolver resolver = dummyInput();

		controlMap.put(ControlKey.MINIMA.name(), Utils.constMap(map -> {
			map.put(VriControlParser.MINIMUM_BASE_AREA, 0f);
			map.put(VriControlParser.MINIMUM_HEIGHT, 6f);
			map.put(VriControlParser.MINIMUM_PREDICTED_BASE_AREA, 2f);
			map.put(VriControlParser.MINIMUM_VETERAN_HEIGHT, VriControlParser.DEFAULT_MINIMUM_VETERAN_HEIGHT);
		}));

		app.init(resolver, controlMap);

		var poly = VriPolygon.build(pBuilder -> {
			pBuilder.polygonIdentifier("082F074/0071         2001");
			pBuilder.biogeoclimaticZone("IDF");
			pBuilder.forestInventoryZone(" ");
			pBuilder.yieldFactor(1.0f);
			pBuilder.mode(PolygonMode.START);
			pBuilder.addLayer(lBuilder -> {
				lBuilder.layerType(LayerType.PRIMARY);
				lBuilder.crownClosure(57.8f);
				lBuilder.baseArea(66.0f);
				lBuilder.treesPerHectare(850f);
				lBuilder.utilization(7.5f);

				// Sites
				lBuilder.addSite(iBuilder -> {
					iBuilder.siteGenus("B");
					iBuilder.siteSpecies("BL");
					iBuilder.siteCurveNumber(8);
				});
				lBuilder.addSite(iBuilder -> {
					iBuilder.ageTotal(200);
					iBuilder.height(28.0f);
					iBuilder.siteIndex(14.3f);
					iBuilder.siteGenus("C");
					iBuilder.siteSpecies("CW");
					iBuilder.yearsToBreastHeight(10.9f);
					iBuilder.breastHeightAge(189.1f);
					iBuilder.siteCurveNumber(11);
				});
				lBuilder.addSite(iBuilder -> {
					iBuilder.ageTotal(200);
					// iBuilder.height(32.0f); // Remove height from primary species
					iBuilder.siteIndex(14.6f);
					iBuilder.siteGenus("H");
					iBuilder.siteSpecies("HW");
					iBuilder.yearsToBreastHeight(9.7f);
					iBuilder.breastHeightAge(190.3f);
					iBuilder.siteCurveNumber(37);
				});
				lBuilder.addSite(iBuilder -> {
					iBuilder.siteGenus("S");
					iBuilder.siteSpecies("SE");
					iBuilder.siteCurveNumber(71);
				});

				// Species
				lBuilder.addSpecies(sBuilder -> {
					sBuilder.genus("B");
					sBuilder.percentGenus(3f);
					sBuilder.addSpecies("BL", 100);
				});
				lBuilder.addSpecies(sBuilder -> {
					sBuilder.genus("C");
					sBuilder.percentGenus(30f);
					sBuilder.addSpecies("CW", 100);
				});
				lBuilder.addSpecies(sBuilder -> {
					sBuilder.genus("H");
					sBuilder.percentGenus(48.9f);
					sBuilder.addSpecies("HW", 100);
				});
				lBuilder.addSpecies(sBuilder -> {
					sBuilder.genus("S");
					sBuilder.percentGenus(18.1f);
					sBuilder.addSpecies("SE", 100);
				});

				lBuilder.primaryGenus("H");
			});
		});

		assertThrows(StandProcessingException.class, () -> app.checkPolygon(poly));

		app.close();
	}

	@ParameterizedTest
	@CsvSource({ "START,fail", "YOUNG,fail", "BATC,fail", "BATN,fail", "DONT_PROCESS,pass" })
	void testFailIfSiteIndexMissing(String modeName, String passFail) throws Exception {

		PolygonMode mode = PolygonMode.valueOf(modeName);
		boolean pass = passFail.equals("pass");
		var app = new VriStart();

		MockFileResolver resolver = dummyInput();

		controlMap.put(ControlKey.MINIMA.name(), Utils.constMap(map -> {
			map.put(VriControlParser.MINIMUM_BASE_AREA, 0f);
			map.put(VriControlParser.MINIMUM_HEIGHT, 6f);
			map.put(VriControlParser.MINIMUM_PREDICTED_BASE_AREA, 2f);
			map.put(VriControlParser.MINIMUM_VETERAN_HEIGHT, VriControlParser.DEFAULT_MINIMUM_VETERAN_HEIGHT);
		}));

		app.init(resolver, controlMap);

		var poly = VriPolygon.build(pBuilder -> {
			pBuilder.polygonIdentifier("082F074/0071         2001");
			pBuilder.biogeoclimaticZone("IDF");
			pBuilder.forestInventoryZone(" ");
			pBuilder.yieldFactor(1.0f);
			pBuilder.mode(mode);
			pBuilder.addLayer(lBuilder -> {
				lBuilder.layerType(LayerType.PRIMARY);
				lBuilder.crownClosure(57.8f);
				lBuilder.baseArea(66.0f);
				lBuilder.treesPerHectare(850f);
				lBuilder.utilization(7.5f);

				// Sites
				lBuilder.addSite(iBuilder -> {
					iBuilder.siteGenus("B");
					iBuilder.siteSpecies("BL");
					iBuilder.height(5f);
					iBuilder.siteCurveNumber(8);
					// iBuilder.ageTotal(100f);
				});

				// Species
				lBuilder.addSpecies(sBuilder -> {
					sBuilder.genus("B");
					sBuilder.percentGenus(100f);
					sBuilder.addSpecies("BL", 100);
				});

				lBuilder.primaryGenus("B");
			});
		});
		BecDefinition bec = Utils.getBec(poly.getBiogeoclimaticZone(), controlMap);

		if (pass) {
			var result = assertDoesNotThrow(() -> app.checkPolygonForMode(poly, bec));
			assertThat(result, is(mode));
		} else {
			var ex = assertThrows(StandProcessingException.class, () -> app.checkPolygonForMode(poly, bec));

			assertThat(ex, hasProperty("message", is("Site index is not present")));
		}

		app.close();
	}

	@ParameterizedTest
	@CsvSource({ "START,fail", "YOUNG,fail", "BATC,fail", "BATN,fail", "DONT_PROCESS,pass" })
	void testFailIfSiteIndexLow(String modeName, String passFail) throws Exception {

		PolygonMode mode = PolygonMode.valueOf(modeName);
		boolean pass = passFail.equals("pass");
		var app = new VriStart();

		MockFileResolver resolver = dummyInput();

		controlMap.put(ControlKey.MINIMA.name(), Utils.constMap(map -> {
			map.put(VriControlParser.MINIMUM_BASE_AREA, 0f);
			map.put(VriControlParser.MINIMUM_HEIGHT, 6f);
			map.put(VriControlParser.MINIMUM_PREDICTED_BASE_AREA, 2f);
			map.put(VriControlParser.MINIMUM_VETERAN_HEIGHT, VriControlParser.DEFAULT_MINIMUM_VETERAN_HEIGHT);
		}));

		app.init(resolver, controlMap);

		var poly = VriPolygon.build(pBuilder -> {
			pBuilder.polygonIdentifier("082F074/0071         2001");
			pBuilder.biogeoclimaticZone("IDF");
			pBuilder.forestInventoryZone(" ");
			pBuilder.yieldFactor(1.0f);
			pBuilder.mode(mode);
			pBuilder.addLayer(lBuilder -> {
				lBuilder.layerType(LayerType.PRIMARY);
				lBuilder.crownClosure(57.8f);
				lBuilder.baseArea(66.0f);
				lBuilder.treesPerHectare(850f);
				lBuilder.utilization(7.5f);

				// Sites
				lBuilder.addSite(iBuilder -> {
					iBuilder.siteGenus("B");
					iBuilder.siteSpecies("BL");
					iBuilder.height(5f);
					iBuilder.siteCurveNumber(8);
					iBuilder.siteIndex(0.0f);
				});

				// Species
				lBuilder.addSpecies(sBuilder -> {
					sBuilder.genus("B");
					sBuilder.percentGenus(100f);
					sBuilder.addSpecies("BL", 100);
				});

				lBuilder.primaryGenus("B");
			});
		});
		BecDefinition bec = Utils.getBec(poly.getBiogeoclimaticZone(), controlMap);

		if (pass) {
			var result = assertDoesNotThrow(() -> app.checkPolygonForMode(poly, bec));
			assertThat(result, is(mode));
		} else {
			var ex = assertThrows(StandProcessingException.class, () -> app.checkPolygonForMode(poly, bec));

			assertThat(ex, hasProperty("message", is("Site index 0.0 should be greater than 0.0")));
		}

		app.close();
	}

	@ParameterizedTest
	@CsvSource({ "START,fail", "YOUNG,fail", "BATC,fail", "BATN,fail", "DONT_PROCESS,pass" })
	void testFailIfAgeTotalMissing(String modeName, String passFail) throws Exception {

		PolygonMode mode = PolygonMode.valueOf(modeName);
		boolean pass = passFail.equals("pass");
		var app = new VriStart();

		MockFileResolver resolver = dummyInput();

		controlMap.put(ControlKey.MINIMA.name(), Utils.constMap(map -> {
			map.put(VriControlParser.MINIMUM_BASE_AREA, 0f);
			map.put(VriControlParser.MINIMUM_HEIGHT, 6f);
			map.put(VriControlParser.MINIMUM_PREDICTED_BASE_AREA, 2f);
			map.put(VriControlParser.MINIMUM_VETERAN_HEIGHT, VriControlParser.DEFAULT_MINIMUM_VETERAN_HEIGHT);
		}));

		app.init(resolver, controlMap);

		var poly = VriPolygon.build(pBuilder -> {
			pBuilder.polygonIdentifier("082F074/0071         2001");
			pBuilder.biogeoclimaticZone("IDF");
			pBuilder.forestInventoryZone(" ");
			pBuilder.yieldFactor(1.0f);
			pBuilder.mode(mode);
			pBuilder.addLayer(lBuilder -> {
				lBuilder.layerType(LayerType.PRIMARY);
				lBuilder.crownClosure(57.8f);
				lBuilder.baseArea(66.0f);
				lBuilder.treesPerHectare(850f);
				lBuilder.utilization(7.5f);

				// Sites
				lBuilder.addSite(iBuilder -> {
					iBuilder.siteGenus("B");
					iBuilder.siteSpecies("BL");
					iBuilder.height(5f);
					iBuilder.siteCurveNumber(8);
					iBuilder.siteIndex(1f);
					iBuilder.ageTotal(Optional.empty());
				});

				// Species
				lBuilder.addSpecies(sBuilder -> {
					sBuilder.genus("B");
					sBuilder.percentGenus(100f);
					sBuilder.addSpecies("BL", 100);
				});

				lBuilder.primaryGenus("B");
			});
		});
		BecDefinition bec = Utils.getBec(poly.getBiogeoclimaticZone(), controlMap);

		if (pass) {
			var result = assertDoesNotThrow(() -> app.checkPolygonForMode(poly, bec));
			assertThat(result, is(mode));
		} else {
			var ex = assertThrows(StandProcessingException.class, () -> app.checkPolygonForMode(poly, bec));

			assertThat(ex, hasProperty("message", is("Age total is not present")));
		}

		app.close();
	}

	@ParameterizedTest
	@CsvSource({ "START,fail", "YOUNG,fail", "BATC,fail", "BATN,fail", "DONT_PROCESS,pass" })
	void testFailIfAgeTotalLow(String modeName, String passFail) throws Exception {

		PolygonMode mode = PolygonMode.valueOf(modeName);
		boolean pass = passFail.equals("pass");
		var app = new VriStart();

		MockFileResolver resolver = dummyInput();

		controlMap.put(ControlKey.MINIMA.name(), Utils.constMap(map -> {
			map.put(VriControlParser.MINIMUM_BASE_AREA, 0f);
			map.put(VriControlParser.MINIMUM_HEIGHT, 6f);
			map.put(VriControlParser.MINIMUM_PREDICTED_BASE_AREA, 2f);
			map.put(VriControlParser.MINIMUM_VETERAN_HEIGHT, VriControlParser.DEFAULT_MINIMUM_VETERAN_HEIGHT);
		}));

		app.init(resolver, controlMap);

		var poly = VriPolygon.build(pBuilder -> {
			pBuilder.polygonIdentifier("082F074/0071         2001");
			pBuilder.biogeoclimaticZone("IDF");
			pBuilder.forestInventoryZone(" ");
			pBuilder.yieldFactor(1.0f);
			pBuilder.mode(mode);
			pBuilder.addLayer(lBuilder -> {
				lBuilder.layerType(LayerType.PRIMARY);
				lBuilder.crownClosure(57.8f);
				lBuilder.baseArea(66.0f);
				lBuilder.treesPerHectare(850f);
				lBuilder.utilization(7.5f);

				// Sites
				lBuilder.addSite(iBuilder -> {
					iBuilder.siteGenus("B");
					iBuilder.siteSpecies("BL");
					iBuilder.height(5f);
					iBuilder.siteCurveNumber(8);
					iBuilder.siteIndex(1f);
					iBuilder.ageTotal(0f);
				});

				// Species
				lBuilder.addSpecies(sBuilder -> {
					sBuilder.genus("B");
					sBuilder.percentGenus(100f);
					sBuilder.addSpecies("BL", 100);
				});

				lBuilder.primaryGenus("B");
			});
		});
		BecDefinition bec = Utils.getBec(poly.getBiogeoclimaticZone(), controlMap);

		if (pass) {
			var result = assertDoesNotThrow(() -> app.checkPolygonForMode(poly, bec));
			assertThat(result, is(mode));
		} else {
			var ex = assertThrows(StandProcessingException.class, () -> app.checkPolygonForMode(poly, bec));

			assertThat(ex, hasProperty("message", is("Age total 0.0 should be greater than 0.0")));
		}

		app.close();
	}

	@ParameterizedTest
	@CsvSource({ "START,fail", "YOUNG,pass", "BATC,fail", "BATN,fail", "DONT_PROCESS,pass" })
	void testFailIfBreastHeightAgeLow(String modeName, String passFail) throws Exception {

		PolygonMode mode = PolygonMode.valueOf(modeName);
		boolean pass = passFail.equals("pass");
		var app = new VriStart();

		MockFileResolver resolver = dummyInput();

		controlMap.put(ControlKey.MINIMA.name(), Utils.constMap(map -> {
			map.put(VriControlParser.MINIMUM_BASE_AREA, 0f);
			map.put(VriControlParser.MINIMUM_HEIGHT, 6f);
			map.put(VriControlParser.MINIMUM_PREDICTED_BASE_AREA, 2f);
			map.put(VriControlParser.MINIMUM_VETERAN_HEIGHT, VriControlParser.DEFAULT_MINIMUM_VETERAN_HEIGHT);
		}));

		app.init(resolver, controlMap);

		var poly = VriPolygon.build(pBuilder -> {
			pBuilder.polygonIdentifier("082F074/0071         2001");
			pBuilder.biogeoclimaticZone("IDF");
			pBuilder.forestInventoryZone(" ");
			pBuilder.yieldFactor(1.0f);
			pBuilder.mode(mode);
			pBuilder.addLayer(lBuilder -> {
				lBuilder.layerType(LayerType.PRIMARY);
				lBuilder.crownClosure(57.8f);
				lBuilder.baseArea(66.0f);
				lBuilder.treesPerHectare(850f);
				lBuilder.utilization(7.5f);

				// Sites
				lBuilder.addSite(iBuilder -> {
					iBuilder.siteGenus("B");
					iBuilder.siteSpecies("BL");
					iBuilder.height(5f);
					iBuilder.siteCurveNumber(8);
					iBuilder.siteIndex(1f);

					// These are equal so computed breast height age will be 0
					iBuilder.ageTotal(10f);
					iBuilder.yearsToBreastHeight(10f);
				});

				// Species
				lBuilder.addSpecies(sBuilder -> {
					sBuilder.genus("B");
					sBuilder.percentGenus(100f);
					sBuilder.addSpecies("BL", 100);
				});

				lBuilder.primaryGenus("B");
			});
		});
		BecDefinition bec = Utils.getBec(poly.getBiogeoclimaticZone(), controlMap);

		if (pass) {
			var result = assertDoesNotThrow(() -> app.checkPolygonForMode(poly, bec));
			assertThat(result, is(mode));
		} else {
			var ex = assertThrows(StandProcessingException.class, () -> app.checkPolygonForMode(poly, bec));

			assertThat(ex, hasProperty("message", is("Breast height age 0.0 should be greater than 0.0")));
		}

		app.close();
	}

	@ParameterizedTest
	@CsvSource({ "START,pass", "YOUNG,fail", "BATC,pass", "BATN,pass", "DONT_PROCESS,pass" })
	void testFailIfYearsToBreastHeightLow(String modeName, String passFail) throws Exception {

		PolygonMode mode = PolygonMode.valueOf(modeName);
		boolean pass = passFail.equals("pass");
		var app = new VriStart();

		MockFileResolver resolver = dummyInput();

		controlMap.put(ControlKey.MINIMA.name(), Utils.constMap(map -> {
			map.put(VriControlParser.MINIMUM_BASE_AREA, 0f);
			map.put(VriControlParser.MINIMUM_HEIGHT, 6f);
			map.put(VriControlParser.MINIMUM_PREDICTED_BASE_AREA, 2f);
			map.put(VriControlParser.MINIMUM_VETERAN_HEIGHT, VriControlParser.DEFAULT_MINIMUM_VETERAN_HEIGHT);
		}));

		app.init(resolver, controlMap);

		var poly = VriPolygon.build(pBuilder -> {
			pBuilder.polygonIdentifier("082F074/0071         2001");
			pBuilder.biogeoclimaticZone("IDF");
			pBuilder.forestInventoryZone(" ");
			pBuilder.yieldFactor(1.0f);
			pBuilder.mode(mode);
			pBuilder.addLayer(lBuilder -> {
				lBuilder.layerType(LayerType.PRIMARY);
				lBuilder.crownClosure(57.8f);
				lBuilder.baseArea(66.0f);
				lBuilder.treesPerHectare(850f);
				lBuilder.utilization(7.5f);

				// Sites
				lBuilder.addSite(iBuilder -> {
					iBuilder.siteGenus("B");
					iBuilder.siteSpecies("BL");
					iBuilder.height(5f);
					iBuilder.siteCurveNumber(8);
					iBuilder.siteIndex(1f);
					iBuilder.ageTotal(100f);
					iBuilder.yearsToBreastHeight(0f);
				});

				// Species
				lBuilder.addSpecies(sBuilder -> {
					sBuilder.genus("B");
					sBuilder.percentGenus(100f);
					sBuilder.addSpecies("BL", 100);
				});

				lBuilder.primaryGenus("B");
			});
		});
		BecDefinition bec = Utils.getBec(poly.getBiogeoclimaticZone(), controlMap);

		if (pass) {
			var result = assertDoesNotThrow(() -> app.checkPolygonForMode(poly, bec));
			assertThat(result, is(mode));
		} else {
			var ex = assertThrows(StandProcessingException.class, () -> app.checkPolygonForMode(poly, bec));

			assertThat(ex, hasProperty("message", is("Years to breast height 0.0 should be greater than 0.0")));
		}

		app.close();
	}

	@ParameterizedTest
	@CsvSource(
		{ //
				"START,5.0,pass", "YOUNG,5.0,pass", "BATC,5.0,pass", "BATN,5.0,pass", "DONT_PROCESS,5.0,pass", //
				"START,4.5,fail", "YOUNG,4.5,pass", "BATC,4.5,pass", "BATN,4.5,pass", "DONT_PROCESS,4.5,pass", //
				"START,1.4,fail", "YOUNG,1.4,pass", "BATC,1.4,pass", "BATN,1.4,pass", "DONT_PROCESS,1.4,pass", //
				"START,1.3,fail", "YOUNG,1.3,pass", "BATC,1.3,fail", "BATN,1.3,fail", "DONT_PROCESS,1.4,pass", //
				"START,0.0,fail", "YOUNG,0.0,pass", "BATC,0.0,fail", "BATN,0.0,fail" } // DONT_PROCESS triggers another
																						// check in this case
	)

	void testFailIfYearsToBreastHeightLow(String modeName, String heightS, String passFail) throws Exception {

		PolygonMode mode = PolygonMode.valueOf(modeName);
		boolean pass = passFail.equals("pass");
		var app = new VriStart();

		MockFileResolver resolver = dummyInput();

		controlMap.put(ControlKey.MINIMA.name(), Utils.constMap(map -> {
			map.put(VriControlParser.MINIMUM_BASE_AREA, 0f);
			map.put(VriControlParser.MINIMUM_HEIGHT, 6f);
			map.put(VriControlParser.MINIMUM_PREDICTED_BASE_AREA, 2f);
			map.put(VriControlParser.MINIMUM_VETERAN_HEIGHT, VriControlParser.DEFAULT_MINIMUM_VETERAN_HEIGHT);
		}));

		app.init(resolver, controlMap);

		var poly = VriPolygon.build(pBuilder -> {
			pBuilder.polygonIdentifier("082F074/0071         2001");
			pBuilder.biogeoclimaticZone("IDF");
			pBuilder.forestInventoryZone(" ");
			pBuilder.yieldFactor(1.0f);
			pBuilder.mode(mode);
			pBuilder.addLayer(lBuilder -> {
				lBuilder.layerType(LayerType.PRIMARY);
				lBuilder.crownClosure(57.8f);
				lBuilder.baseArea(66.0f);
				lBuilder.treesPerHectare(850f);
				lBuilder.utilization(7.5f);

				// Sites
				lBuilder.addSite(iBuilder -> {
					iBuilder.siteGenus("B");
					iBuilder.siteSpecies("BL");
					iBuilder.height(Float.valueOf(heightS));
					iBuilder.siteCurveNumber(8);
					iBuilder.siteIndex(1f);
					iBuilder.ageTotal(100f);
					iBuilder.yearsToBreastHeight(5f);
				});

				// Species
				lBuilder.addSpecies(sBuilder -> {
					sBuilder.genus("B");
					sBuilder.percentGenus(100f);
					sBuilder.addSpecies("BL", 100);
				});

				lBuilder.primaryGenus("B");
			});
		});
		BecDefinition bec = Utils.getBec(poly.getBiogeoclimaticZone(), controlMap);

		if (pass) {
			var result = assertDoesNotThrow(() -> app.checkPolygonForMode(poly, bec));
			assertThat(result, is(mode));
		} else {
			var ex = assertThrows(StandProcessingException.class, () -> app.checkPolygonForMode(poly, bec));

			assertThat(ex, hasProperty("message", startsWith("Height " + heightS + " should be greater than ")));
		}

		app.close();
	}

	@ParameterizedTest
	@CsvSource({ "START,fail", "YOUNG,pass", "BATC,pass", "BATN,pass", "DONT_PROCESS,pass" })
	void testFailIfBaseAreaLow(String modeName, String passFail) throws Exception {

		PolygonMode mode = PolygonMode.valueOf(modeName);
		boolean pass = passFail.equals("pass");
		var app = new VriStart();

		MockFileResolver resolver = dummyInput();

		controlMap.put(ControlKey.MINIMA.name(), Utils.constMap(map -> {
			map.put(VriControlParser.MINIMUM_BASE_AREA, 0f);
			map.put(VriControlParser.MINIMUM_HEIGHT, 6f);
			map.put(VriControlParser.MINIMUM_PREDICTED_BASE_AREA, 2f);
			map.put(VriControlParser.MINIMUM_VETERAN_HEIGHT, VriControlParser.DEFAULT_MINIMUM_VETERAN_HEIGHT);
		}));

		app.init(resolver, controlMap);

		var poly = VriPolygon.build(pBuilder -> {
			pBuilder.polygonIdentifier("082F074/0071         2001");
			pBuilder.biogeoclimaticZone("IDF");
			pBuilder.forestInventoryZone(" ");
			pBuilder.yieldFactor(1.0f);
			pBuilder.mode(mode);
			pBuilder.addLayer(lBuilder -> {
				lBuilder.layerType(LayerType.PRIMARY);
				lBuilder.crownClosure(57.8f);
				lBuilder.baseArea(0.0f);
				lBuilder.treesPerHectare(850f);
				lBuilder.utilization(7.5f);

				// Sites
				lBuilder.addSite(iBuilder -> {
					iBuilder.siteGenus("B");
					iBuilder.siteSpecies("BL");
					iBuilder.height(5f);
					iBuilder.siteCurveNumber(8);
					iBuilder.siteIndex(1f);
					iBuilder.ageTotal(100f);
					iBuilder.yearsToBreastHeight(5f);
				});

				// Species
				lBuilder.addSpecies(sBuilder -> {
					sBuilder.genus("B");
					sBuilder.percentGenus(100f);
					sBuilder.addSpecies("BL", 100);
				});

				lBuilder.primaryGenus("B");
			});
		});
		BecDefinition bec = Utils.getBec(poly.getBiogeoclimaticZone(), controlMap);

		if (pass) {
			var result = assertDoesNotThrow(() -> app.checkPolygonForMode(poly, bec));
			assertThat(result, is(mode));
		} else {
			var ex = assertThrows(StandProcessingException.class, () -> app.checkPolygonForMode(poly, bec));

			assertThat(ex, hasProperty("message", is("Base area 0.0 should be greater than 0.0")));
		}

		app.close();
	}

	@ParameterizedTest
	@CsvSource(
		{ "START,fail", /* YOUNG triggers a different validation error in this case */ "BATC,pass", "BATN,pass",
				"DONT_PROCESS,pass" }
	)
	void testFailIfTreesPerHectareLow(String modeName, String passFail) throws Exception {

		PolygonMode mode = PolygonMode.valueOf(modeName);
		boolean pass = passFail.equals("pass");
		var app = new VriStart();

		MockFileResolver resolver = dummyInput();

		controlMap.put(ControlKey.MINIMA.name(), Utils.constMap(map -> {
			map.put(VriControlParser.MINIMUM_BASE_AREA, 0f);
			map.put(VriControlParser.MINIMUM_HEIGHT, 6f);
			map.put(VriControlParser.MINIMUM_PREDICTED_BASE_AREA, 2f);
			map.put(VriControlParser.MINIMUM_VETERAN_HEIGHT, VriControlParser.DEFAULT_MINIMUM_VETERAN_HEIGHT);
		}));

		app.init(resolver, controlMap);

		var poly = VriPolygon.build(pBuilder -> {
			pBuilder.polygonIdentifier("082F074/0071         2001");
			pBuilder.biogeoclimaticZone("IDF");
			pBuilder.forestInventoryZone(" ");
			pBuilder.yieldFactor(1.0f);
			pBuilder.mode(mode);
			pBuilder.addLayer(lBuilder -> {
				lBuilder.layerType(LayerType.PRIMARY);
				lBuilder.crownClosure(57.8f);
				lBuilder.baseArea(30.0f);
				lBuilder.treesPerHectare(0f);
				lBuilder.utilization(7.5f);

				// Sites
				lBuilder.addSite(iBuilder -> {
					iBuilder.siteGenus("B");
					iBuilder.siteSpecies("BL");
					iBuilder.height(5f);
					iBuilder.siteCurveNumber(8);
					iBuilder.siteIndex(1f);
					iBuilder.ageTotal(100f);
					iBuilder.yearsToBreastHeight(5f);
				});

				// Species
				lBuilder.addSpecies(sBuilder -> {
					sBuilder.genus("B");
					sBuilder.percentGenus(100f);
					sBuilder.addSpecies("BL", 100);
				});

				lBuilder.primaryGenus("B");
			});
		});
		BecDefinition bec = Utils.getBec(poly.getBiogeoclimaticZone(), controlMap);

		if (pass) {
			var result = assertDoesNotThrow(() -> app.checkPolygonForMode(poly, bec));
			assertThat(result, is(mode));
		} else {
			var ex = assertThrows(StandProcessingException.class, () -> app.checkPolygonForMode(poly, bec));

			assertThat(ex, hasProperty("message", is("Trees per hectare 0.0 should be greater than 0.0")));
		}

		app.close();
	}

	@ParameterizedTest
	@CsvSource({ "START,pass", "YOUNG,pass", "BATC,fail", "BATN,pass", "DONT_PROCESS,pass" })
	void testFailIfCrownClosureLow(String modeName, String passFail) throws Exception {

		PolygonMode mode = PolygonMode.valueOf(modeName);
		boolean pass = passFail.equals("pass");
		var app = new VriStart();

		MockFileResolver resolver = dummyInput();

		controlMap.put(ControlKey.MINIMA.name(), Utils.constMap(map -> {
			map.put(VriControlParser.MINIMUM_BASE_AREA, 0f);
			map.put(VriControlParser.MINIMUM_HEIGHT, 6f);
			map.put(VriControlParser.MINIMUM_PREDICTED_BASE_AREA, 2f);
			map.put(VriControlParser.MINIMUM_VETERAN_HEIGHT, VriControlParser.DEFAULT_MINIMUM_VETERAN_HEIGHT);
		}));

		app.init(resolver, controlMap);

		var poly = VriPolygon.build(pBuilder -> {
			pBuilder.polygonIdentifier("082F074/0071         2001");
			pBuilder.biogeoclimaticZone("IDF");
			pBuilder.forestInventoryZone(" ");
			pBuilder.yieldFactor(1.0f);
			pBuilder.mode(mode);
			pBuilder.addLayer(lBuilder -> {
				lBuilder.layerType(LayerType.PRIMARY);
				lBuilder.crownClosure(0.0f);
				lBuilder.baseArea(30.0f);
				lBuilder.treesPerHectare(850f);
				lBuilder.utilization(7.5f);

				// Sites
				lBuilder.addSite(iBuilder -> {
					iBuilder.siteGenus("B");
					iBuilder.siteSpecies("BL");
					iBuilder.height(5f);
					iBuilder.siteCurveNumber(8);
					iBuilder.siteIndex(1f);
					iBuilder.ageTotal(100f);
					iBuilder.yearsToBreastHeight(5f);
				});

				// Species
				lBuilder.addSpecies(sBuilder -> {
					sBuilder.genus("B");
					sBuilder.percentGenus(100f);
					sBuilder.addSpecies("BL", 100);
				});

				lBuilder.primaryGenus("B");
			});
		});
		BecDefinition bec = Utils.getBec(poly.getBiogeoclimaticZone(), controlMap);

		if (pass) {
			var result = assertDoesNotThrow(() -> app.checkPolygonForMode(poly, bec));
			assertThat(result, is(mode));
		} else {
			var ex = assertThrows(StandProcessingException.class, () -> app.checkPolygonForMode(poly, bec));

			assertThat(ex, hasProperty("message", is("Crown closure 0.0 should be greater than 0.0")));
		}

		app.close();
	}

	@ParameterizedTest
	@CsvSource({ "START,pass", "YOUNG,pass", "BATC,pass", "BATN,pass", "DONT_PROCESS,pass" })
	void testModeSpecificValidationEverythingOK(String modeName, String passFail) throws Exception {

		PolygonMode mode = PolygonMode.valueOf(modeName);
		boolean pass = passFail.equals("pass");
		var app = new VriStart();

		MockFileResolver resolver = dummyInput();

		controlMap.put(ControlKey.MINIMA.name(), Utils.constMap(map -> {
			map.put(VriControlParser.MINIMUM_BASE_AREA, 0f);
			map.put(VriControlParser.MINIMUM_HEIGHT, 6f);
			map.put(VriControlParser.MINIMUM_PREDICTED_BASE_AREA, 2f);
			map.put(VriControlParser.MINIMUM_VETERAN_HEIGHT, VriControlParser.DEFAULT_MINIMUM_VETERAN_HEIGHT);
		}));

		app.init(resolver, controlMap);

		var poly = VriPolygon.build(pBuilder -> {
			pBuilder.polygonIdentifier("082F074/0071         2001");
			pBuilder.biogeoclimaticZone("IDF");
			pBuilder.forestInventoryZone(" ");
			pBuilder.yieldFactor(1.0f);
			pBuilder.mode(mode);
			pBuilder.addLayer(lBuilder -> {
				lBuilder.layerType(LayerType.PRIMARY);
				lBuilder.crownClosure(57.8f);
				lBuilder.baseArea(30.0f);
				lBuilder.treesPerHectare(850f);
				lBuilder.utilization(7.5f);

				// Sites
				lBuilder.addSite(iBuilder -> {
					iBuilder.siteGenus("B");
					iBuilder.siteSpecies("BL");
					iBuilder.height(5f);
					iBuilder.siteCurveNumber(8);
					iBuilder.siteIndex(1f);
					iBuilder.ageTotal(100f);
					iBuilder.yearsToBreastHeight(5f);
				});

				// Species
				lBuilder.addSpecies(sBuilder -> {
					sBuilder.genus("B");
					sBuilder.percentGenus(100f);
					sBuilder.addSpecies("BL", 100);
				});

				lBuilder.primaryGenus("B");
			});
		});
		BecDefinition bec = Utils.getBec(poly.getBiogeoclimaticZone(), controlMap);

		if (pass) {
			var result = assertDoesNotThrow(() -> app.checkPolygonForMode(poly, bec));
			assertThat(result, is(mode));
		} else {
			var ex = assertThrows(StandProcessingException.class, () -> app.checkPolygonForMode(poly, bec));

			assertThat(ex, hasProperty("message", is("Crown closure 0.0 should be greater than 0.0")));
		}

		app.close();
	}

	@Test
	void testCheckVeteranHeight() throws Exception {
		var app = new VriStart();

		MockFileResolver resolver = dummyInput();

		controlMap.put(ControlKey.MINIMA.name(), Utils.constMap(map -> {
			map.put(VriControlParser.MINIMUM_BASE_AREA, 0f);
			map.put(VriControlParser.MINIMUM_HEIGHT, 6f);
			map.put(VriControlParser.MINIMUM_PREDICTED_BASE_AREA, 2f);
			map.put(VriControlParser.MINIMUM_VETERAN_HEIGHT, 36f);
		}));

		app.init(resolver, controlMap);

		var poly = VriPolygon.build(pBuilder -> {
			pBuilder.polygonIdentifier("082F074/0071         2001");
			pBuilder.biogeoclimaticZone("IDF");
			pBuilder.forestInventoryZone(" ");
			pBuilder.yieldFactor(1.0f);
			pBuilder.addLayer(lBuilder -> {
				lBuilder.layerType(LayerType.PRIMARY);
				lBuilder.crownClosure(57.8f);
				lBuilder.baseArea(66.0f);
				lBuilder.treesPerHectare(850f);
				lBuilder.utilization(7.5f);
				lBuilder.empiricalRelationshipParameterIndex(76);

				// Sites
				lBuilder.addSite(iBuilder -> {
					iBuilder.siteGenus("B");
					iBuilder.siteSpecies("BL");
					iBuilder.siteCurveNumber(8);
				});
				lBuilder.addSite(iBuilder -> {
					iBuilder.ageTotal(200);
					iBuilder.height(28.0f);
					iBuilder.siteIndex(14.3f);
					iBuilder.siteGenus("C");
					iBuilder.siteSpecies("CW");
					iBuilder.yearsToBreastHeight(10.9f);
					iBuilder.breastHeightAge(189.1f);
					iBuilder.siteCurveNumber(11);
				});
				lBuilder.addSite(iBuilder -> {
					iBuilder.ageTotal(200);
					iBuilder.height(32.0f);
					iBuilder.siteIndex(14.6f);
					iBuilder.siteGenus("H");
					iBuilder.siteSpecies("HW");
					iBuilder.yearsToBreastHeight(9.7f);
					iBuilder.breastHeightAge(190.3f);
					iBuilder.siteCurveNumber(37);
				});
				lBuilder.addSite(iBuilder -> {
					iBuilder.siteGenus("S");
					iBuilder.siteSpecies("SE");
					iBuilder.siteCurveNumber(71);
				});

				// Species
				lBuilder.addSpecies(sBuilder -> {
					sBuilder.genus("B");
					sBuilder.percentGenus(3f);
					sBuilder.addSpecies("BL", 100);
				});
				lBuilder.addSpecies(sBuilder -> {
					sBuilder.genus("C");
					sBuilder.percentGenus(30f);
					sBuilder.addSpecies("CW", 100);
				});
				lBuilder.addSpecies(sBuilder -> {
					sBuilder.genus("H");
					sBuilder.percentGenus(48.9f);
					sBuilder.addSpecies("HW", 100);
				});
				lBuilder.addSpecies(sBuilder -> {
					sBuilder.genus("S");
					sBuilder.percentGenus(18.1f);
					sBuilder.addSpecies("SE", 100);
				});

				lBuilder.primaryGenus("H");

			});
			pBuilder.addLayer(lBuilder -> {
				lBuilder.layerType(LayerType.VETERAN);
				lBuilder.crownClosure(0f);
				lBuilder.baseArea(0f);
				lBuilder.treesPerHectare(0f);
				lBuilder.utilization(0f);

				// Sites
				lBuilder.addSite(iBuilder -> {
					iBuilder.ageTotal(200);
					iBuilder.height(34.0f);
					iBuilder.siteIndex(14.6f);
					iBuilder.siteGenus("H");
					iBuilder.siteSpecies("HW");
					iBuilder.yearsToBreastHeight(9.7f);
					iBuilder.breastHeightAge(190.3f);
					iBuilder.siteCurveNumber(37);
				});

				// Species
				lBuilder.addSpecies(sBuilder -> {
					sBuilder.genus("H");
					sBuilder.percentGenus(100f);
					sBuilder.addSpecies("HW", 100);
				});

				lBuilder.primaryGenus("H");

			});
		});

		var ex = assertThrows(StandProcessingException.class, () -> app.checkPolygon(poly));
		assertThat(
				ex,
				hasProperty(
						"message",
						is("Veteran layer primary species height 34.0 should be greater than or equal to 36.0")
				)
		);

		app.close();
	}

}
