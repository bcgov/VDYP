package ca.bc.gov.nrs.vdyp.common_calculators;

import static ca.bc.gov.nrs.vdyp.test.TestUtils.closeUtilMap;
import static ca.bc.gov.nrs.vdyp.test.TestUtils.netDecayMap;
import static ca.bc.gov.nrs.vdyp.test.TestUtils.polygonId;
import static ca.bc.gov.nrs.vdyp.test.TestUtils.valid;
import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.closeTo;
import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.coe;
import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.utilization;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import ca.bc.gov.nrs.vdyp.application.ProcessingException;
import ca.bc.gov.nrs.vdyp.application.VdypStartApplication;
import ca.bc.gov.nrs.vdyp.application.test.TestLayer;
import ca.bc.gov.nrs.vdyp.application.test.TestPolygon;
import ca.bc.gov.nrs.vdyp.application.test.TestSite;
import ca.bc.gov.nrs.vdyp.application.test.TestSpecies;
import ca.bc.gov.nrs.vdyp.common.Utils;
import ca.bc.gov.nrs.vdyp.model.Coefficients;
import ca.bc.gov.nrs.vdyp.model.LayerType;
import ca.bc.gov.nrs.vdyp.model.PolygonIdentifier;
import ca.bc.gov.nrs.vdyp.model.PolygonMode;
import ca.bc.gov.nrs.vdyp.model.Region;
import ca.bc.gov.nrs.vdyp.model.UtilizationClass;
import ca.bc.gov.nrs.vdyp.model.VdypLayer;
import ca.bc.gov.nrs.vdyp.model.VdypSpecies;
import ca.bc.gov.nrs.vdyp.test.TestUtils;
import ca.bc.gov.nrs.vdyp.test.VdypMatchers;

class EMPTest {

	Map<String, Object> controlMap;
	EMP emp;

	@BeforeEach
	void setup() {
		controlMap = TestUtils.loadControlMap();
		emp = new EMP(controlMap);
	}

	@Nested
	class EstimateNonPrimaryLoreyHeight {

		@Test
		void testEqn1() throws Exception {

			var bec = Utils.getBec("CWH", controlMap);

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

			var result = emp.estimateNonPrimaryLoreyHeight(spec, specPrime, bec, 24.2999992f, 20.5984688f);

			assertThat(result, closeTo(21.5356998f));

		}

		@Test
		void testEqn2() throws Exception {

			var bec = Utils.getBec("CWH", controlMap);

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

			var result = emp.estimateNonPrimaryLoreyHeight(spec, specPrime, bec, 35.2999992f, 33.6889763f);

			assertThat(result, closeTo(38.7456512f));

		}
	}

	@Test
	void testEstimateQuadMeanDiameterForSpecies() throws Exception {

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

		float dq = emp.estimateQuadMeanDiameterForSpecies(
				spec1, specs, Region.COASTAL, 30.2601795f, 44.6249847f, 620.504883f, 31.6603775f
		);

		assertThat(dq, closeTo(31.7022133f));

	}

	@Nested
	class EstimateQuadMeanDiameterByUtilization {
		@Test
		void testTest1() throws Exception {
			var controlMap = TestUtils.loadControlMap();

			var coe = Utils.utilizationVector();
			coe.setCoe(VdypStartApplication.UTIL_ALL, 31.6622887f);

			var bec = Utils.getBec("CWH", controlMap);

			var spec1 = VdypSpecies.build(builder -> {
				builder.polygonIdentifier("Test", 2024);
				builder.layerType(LayerType.PRIMARY);
				builder.genus("B");
				builder.percentGenus(100f);
				builder.volumeGroup(-1);
				builder.decayGroup(-1);
				builder.breakageGroup(-1);
			});

			emp.estimateQuadMeanDiameterByUtilization(bec, coe, spec1);

			assertThat(coe, utilization(0f, 31.6622887f, 10.0594692f, 14.966774f, 19.9454956f, 46.1699982f));
		}

		@Test
		void testTest2() throws Exception {
			var controlMap = TestUtils.loadControlMap();

			var coe = Utils.utilizationVector();
			coe.setCoe(VdypStartApplication.UTIL_ALL, 13.4943399f);

			var bec = Utils.getBec("MH", controlMap);

			var spec1 = VdypSpecies.build(builder -> {
				builder.polygonIdentifier("Test", 2024);
				builder.layerType(LayerType.PRIMARY);
				builder.genus("L");
				builder.percentGenus(100f);
				builder.volumeGroup(-1);
				builder.decayGroup(-1);
				builder.breakageGroup(-1);
			});

			emp.estimateQuadMeanDiameterByUtilization(bec, coe, spec1);

			assertThat(coe, utilization(0f, 13.4943399f, 10.2766619f, 14.67033f, 19.4037666f, 25.719244f));
		}
	}

	@Test
	void testEstimateVeteranWholeStemVolume() throws Exception {
		var polygonId = polygonId("Test Polygon", 2023);

		var fipPolygon = getTestPolygon(polygonId, valid());
		var fipLayer = getTestVeteranLayer(polygonId, valid(), valid());
		var fipSpecies = getTestSpecies(polygonId, LayerType.VETERAN, valid());
		fipPolygon.setLayers(Collections.singletonMap(LayerType.VETERAN, fipLayer));
		fipLayer.setSpecies(Collections.singletonMap(fipSpecies.getGenus(), fipSpecies));

		var utilizationClass = UtilizationClass.OVER225;
		var aAdjust = 0.10881f;
		var volumeGroup = 12;
		var lorieHeight = 26.2000008f;
		var quadMeanDiameterUtil = new Coefficients(new float[] { 51.8356705f, 0f, 0f, 0f, 51.8356705f }, 0);
		var baseAreaUtil = new Coefficients(new float[] { 0.492921442f, 0f, 0f, 0f, 0.492921442f }, 0);
		var wholeStemVolumeUtil = new Coefficients(new float[] { 0f, 0f, 0f, 0f, 0f }, 0);

		emp.estimateWholeStemVolume(
				utilizationClass, aAdjust, volumeGroup, lorieHeight, quadMeanDiameterUtil, baseAreaUtil,
				wholeStemVolumeUtil
		);

		assertThat(wholeStemVolumeUtil, coe(0, contains(is(0f), is(0f), is(0f), is(0f), closeTo(6.11904192f))));

	}

	@Test
	void testEstimateVeteranCloseUtilization() throws Exception {
		var polygonId = polygonId("Test Polygon", 2023);

		var fipPolygon = getTestPolygon(polygonId, valid());
		var fipLayer = getTestVeteranLayer(polygonId, valid(), valid());
		var fipSpecies = getTestSpecies(polygonId, LayerType.VETERAN, valid());
		fipPolygon.setLayers(Collections.singletonMap(LayerType.VETERAN, fipLayer));
		fipLayer.setSpecies(Collections.singletonMap(fipSpecies.getGenus(), fipSpecies));

		TestUtils.populateControlMapCloseUtilization(controlMap, closeUtilMap(12));

		var utilizationClass = UtilizationClass.OVER225;
		var aAdjust = new Coefficients(new float[] { 0f, 0f, 0f, -0.0981800035f }, 1);
		var volumeGroup = 12;
		var lorieHeight = 26.2000008f;
		var quadMeanDiameterUtil = new Coefficients(new float[] { 51.8356705f, 0f, 0f, 0f, 51.8356705f }, 0);
		var wholeStemVolumeUtil = new Coefficients(new float[] { 0f, 0f, 0f, 0f, 6.11904192f }, 0);

		var closeUtilizationUtil = new Coefficients(new float[] { 0f, 0f, 0f, 0f, 0f }, 0);

		emp.estimateCloseUtilizationVolume(
				utilizationClass, aAdjust, volumeGroup, lorieHeight, quadMeanDiameterUtil, wholeStemVolumeUtil,
				closeUtilizationUtil
		);

		assertThat(closeUtilizationUtil, coe(0, contains(is(0f), is(0f), is(0f), is(0f), closeTo(5.86088896f))));

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

		TestUtils.populateControlMapNetDecay(controlMap, netDecayMap(7));
		TestUtils.populateControlMapDecayModifiers(
				controlMap, (s, r) -> s.equals("B") && r == Region.INTERIOR ? 0f : 0f
		);

		var utilizationClass = UtilizationClass.OVER225;
		var aAdjust = new Coefficients(new float[] { 0f, 0f, 0f, 0.000479999988f }, 1);
		var decayGroup = 7;
		var lorieHeight = 26.2000008f;
		var breastHeightAge = 97.9000015f;
		var quadMeanDiameterUtil = new Coefficients(new float[] { 51.8356705f, 0f, 0f, 0f, 51.8356705f }, 0);
		var closeUtilizationUtil = new Coefficients(new float[] { 0f, 0f, 0f, 0f, 5.86088896f }, 0);

		var closeUtilizationNetOfDecayUtil = new Coefficients(new float[] { 0f, 0f, 0f, 0f, 0f }, 0);

		emp.estimateNetDecayVolume(
				fipSpecies.getGenus(), Region.INTERIOR, utilizationClass, aAdjust, decayGroup, lorieHeight,
				breastHeightAge, quadMeanDiameterUtil, closeUtilizationUtil, closeUtilizationNetOfDecayUtil
		);

		assertThat(
				closeUtilizationNetOfDecayUtil, coe(0, contains(is(0f), is(0f), is(0f), is(0f), closeTo(5.64048958f)))
		);

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

		TestUtils.populateControlMapNetWaste(controlMap, s -> s.equals("B") ? //
				new Coefficients(
						new float[] { -4.20249987f, 11.2235003f, -33.0270004f, 0.124600001f, -0.231800005f, -0.1259f },
						0
				) : //
				new Coefficients(new float[] { 0f, 0f, 0f, 0f, 0f, 0f }, 0)
		);
		TestUtils.populateControlMapWasteModifiers(
				controlMap, (s, r) -> s.equals("B") && r == Region.INTERIOR ? 0f : 0f
		);

		var utilizationClass = UtilizationClass.OVER225;
		var aAdjust = new Coefficients(new float[] { 0f, 0f, 0f, -0.00295000011f }, 1);
		var lorieHeight = 26.2000008f;
		var breastHeightAge = 97.9000015f;
		var quadMeanDiameterUtil = new Coefficients(new float[] { 51.8356705f, 0f, 0f, 0f, 51.8356705f }, 0);
		var closeUtilizationUtil = new Coefficients(new float[] { 0f, 0f, 0f, 0f, 5.86088896f }, 0);
		var closeUtilizationNetOfDecayUtil = new Coefficients(new float[] { 0f, 0f, 0f, 0f, 5.64048958f }, 0);

		var closeUtilizationNetOfDecayAndWasteUtil = new Coefficients(new float[] { 0f, 0f, 0f, 0f, 0f }, 0);

		emp.estimateNetDecayAndWasteVolume(
				Region.INTERIOR, utilizationClass, aAdjust, fipSpecies.getGenus(), lorieHeight, breastHeightAge,
				quadMeanDiameterUtil, closeUtilizationUtil, closeUtilizationNetOfDecayUtil,
				closeUtilizationNetOfDecayAndWasteUtil
		);

		assertThat(
				closeUtilizationNetOfDecayAndWasteUtil,
				coe(0, contains(is(0f), is(0f), is(0f), is(0f), closeTo(5.57935333f)))
		);

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

		TestUtils.populateControlMapNetBreakage(controlMap, bgrp -> bgrp == 5 ? //
				new Coefficients(new float[] { 2.2269001f, 0.75059998f, 4f, 6f }, 1) : //
				new Coefficients(new float[] { 0f, 0f, 0f, 0f }, 1)
		);

		var utilizationClass = UtilizationClass.OVER225;
		var breakageGroup = 5;
		var quadMeanDiameterUtil = new Coefficients(new float[] { 51.8356705f, 0f, 0f, 0f, 51.8356705f }, 0);
		var closeUtilizationUtil = new Coefficients(new float[] { 0f, 0f, 0f, 0f, 5.86088896f }, 0);
		var closeUtilizationNetOfDecayAndWasteUtil = new Coefficients(new float[] { 0f, 0f, 0f, 0f, 5.57935333f }, 0);

		var closeUtilizationNetOfDecayWasteAndBreakageUtil = new Coefficients(new float[] { 0f, 0f, 0f, 0f, 0f }, 0);

		emp.estimateNetDecayWasteAndBreakageVolume(
				utilizationClass, breakageGroup, quadMeanDiameterUtil, closeUtilizationUtil,
				closeUtilizationNetOfDecayAndWasteUtil, closeUtilizationNetOfDecayWasteAndBreakageUtil
		);

		assertThat(
				closeUtilizationNetOfDecayWasteAndBreakageUtil,
				coe(0, contains(is(0f), is(0f), is(0f), is(0f), closeTo(5.27515411f)))
		);

	}

	@Test
	void testEstimatePrimaryNetBreakage() throws Exception {

		var utilizationClass = UtilizationClass.ALL;
		var breakageGroup = 20;
		var quadMeanDiameterUtil = Utils
				.utilizationVector(0f, 13.4943399f, 10.2402296f, 14.6183214f, 19.3349762f, 25.6280651f);
		var closeUtilizationUtil = Utils
				.utilizationVector(0f, 6.41845179f, 0.0353721268f, 2.99654913f, 2.23212862f, 1.1544019f);
		var closeUtilizationNetOfDecayAndWasteUtil = Utils
				.utilizationVector(0f, 6.18276405f, 0.0347718038f, 2.93580461f, 2.169273853f, 1.04291379f);

		var closeUtilizationNetOfDecayWasteAndBreakageUtil = Utils.utilizationVector();

		emp.estimateNetDecayWasteAndBreakageVolume(
				utilizationClass, breakageGroup, quadMeanDiameterUtil, closeUtilizationUtil,
				closeUtilizationNetOfDecayAndWasteUtil, closeUtilizationNetOfDecayWasteAndBreakageUtil
		);

		assertThat(
				closeUtilizationNetOfDecayWasteAndBreakageUtil,
				VdypMatchers.utilization(0f, 5.989573f, 0.0337106399f, 2.84590816f, 2.10230994f, 1.00764418f)
		);

	}

	@Test
	void testEstimateBaseAreaByUtilization() throws ProcessingException {
		var dq = Utils.utilizationVector();
		var ba = Utils.utilizationVector();
		dq.setCoe(0, 31.6622887f);
		dq.setCoe(1, 10.0594692f);
		dq.setCoe(2, 14.966774f);
		dq.setCoe(3, 19.9454956f);
		dq.setCoe(4, 46.1699982f);

		ba.setCoe(0, 0.397305071f);

		var bec = Utils.getBec("CWH", controlMap);

		var spec1 = VdypSpecies.build(builder -> {
			builder.polygonIdentifier("Test", 2024);
			builder.layerType(LayerType.PRIMARY);
			builder.genus("B");
			builder.percentGenus(100f);
			builder.volumeGroup(-1);
			builder.decayGroup(-1);
			builder.breakageGroup(-1);
		});

		emp.estimateBaseAreaByUtilization(bec, dq, ba, spec1);

		assertThat(
				ba,
				VdypMatchers.utilization(0f, 0.397305071f, 0.00485289097f, 0.0131751001f, 0.0221586525f, 0.357118428f)
		);

	}

	@Test
	void testEstimateWholeStemVolumeByUtilizationClass() throws ProcessingException {

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

		wsv.setCoe(VdypStartApplication.UTIL_ALL, 11.7993851f);

		// app.estimateWholeStemVolumeByUtilizationClass(46, 14.2597857f, dq, ba, wsv);
		emp.estimateWholeStemVolume(UtilizationClass.ALL, 0f, 46, 14.2597857f, dq, ba, wsv);

		assertThat(wsv, VdypMatchers.utilization(0f, 11.7993851f, 3.13278913f, 4.76524019f, 2.63645673f, 1.26489878f));
	}

	TestPolygon getTestPolygon(PolygonIdentifier polygonId, Consumer<TestPolygon> mutator) {
		var result = TestPolygon.build(builder -> {
			builder.polygonIdentifier(polygonId);
			builder.forestInventoryZone("0");
			builder.biogeoclimaticZone("BG");
			builder.percentAvailable(Optional.of(100f));
			builder.mode(PolygonMode.START);
		});
		mutator.accept(result);
		return result;
	};

	TestLayer getTestPrimaryLayer(
			PolygonIdentifier polygonId, Consumer<TestLayer.Builder> mutator, Consumer<TestSite.Builder> siteMutator
	) {
		var result = TestLayer.build(builder -> {
			builder.layerType(LayerType.PRIMARY);
			builder.polygonIdentifier(polygonId);
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
