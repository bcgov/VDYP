package ca.bc.gov.nrs.vdyp.common;

import static ca.bc.gov.nrs.vdyp.test.TestUtils.closeUtilMap;
import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.closeTo;
import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.utilization;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import ca.bc.gov.nrs.vdyp.application.ProcessingException;
import ca.bc.gov.nrs.vdyp.common_calculators.BaseAreaTreeDensityDiameter;
import ca.bc.gov.nrs.vdyp.model.BecLookup;
import ca.bc.gov.nrs.vdyp.model.Coefficients;
import ca.bc.gov.nrs.vdyp.model.GenusDefinition;
import ca.bc.gov.nrs.vdyp.model.LayerType;
import ca.bc.gov.nrs.vdyp.model.MatrixMap2;
import ca.bc.gov.nrs.vdyp.model.Region;
import ca.bc.gov.nrs.vdyp.model.UtilizationClass;
import ca.bc.gov.nrs.vdyp.model.UtilizationVector;
import ca.bc.gov.nrs.vdyp.model.VdypLayer;
import ca.bc.gov.nrs.vdyp.model.VdypSpecies;
import ca.bc.gov.nrs.vdyp.test.TestUtils;
import ca.bc.gov.nrs.vdyp.test.VdypMatchers;

class EstimationMethodsTest {

	Map<String, Object> controlMap;
	BecLookup becLookup;
	EstimationMethods emp;

	@BeforeEach
	void setup() {
		controlMap = TestUtils.loadControlMap();
		emp = new EstimationMethods(controlMap);
		becLookup = (BecLookup) controlMap.get(ControlKey.BEC_DEF.name());
	}

	@Nested
	class BasalAreaEstimation {
		@Test
		void testWhenBasalAreaAllIsZero() throws ProcessingException {
			var becDefinition = becLookup.get("IDF").get();

			@SuppressWarnings("unchecked")
			var genera = (List<GenusDefinition>) controlMap.get(ControlKey.SP0_DEF.name());
			var genus = genera.get(0);

			UtilizationVector quadMeanDiameterByUtilization = Utils.utilizationVector(0.0f);
			UtilizationVector basalAreaByUtilization = Utils.utilizationVector(0.0f);

			EstimationMethods.estimateBaseAreaByUtilization(
					controlMap, becDefinition, quadMeanDiameterByUtilization, basalAreaByUtilization, genus.getAlias()
			);

			for (var c : basalAreaByUtilization) {
				assertThat(c, is(0.0f));
			}
		}

		@Test
		void testWhenBasalAreaAllIsTenAndQuadMeanDiametersAreAllZero() throws ProcessingException {
			var becDefinition = becLookup.get("IDF").get();

			@SuppressWarnings("unchecked")
			var genera = (List<GenusDefinition>) controlMap.get(ControlKey.SP0_DEF.name());
			var genus = genera.get(0);

			UtilizationVector quadMeanDiameterByUtilization = Utils.utilizationVector(0.0f);
			UtilizationVector basalAreaByUtilization = Utils.utilizationVector(10.0f);

			EstimationMethods.estimateBaseAreaByUtilization(
					controlMap, becDefinition, quadMeanDiameterByUtilization, basalAreaByUtilization, genus.getAlias()
			);

			assertThat(basalAreaByUtilization.getCoe(UtilizationClass.SMALL.index), is(0.0f));
			assertThat(basalAreaByUtilization.getCoe(UtilizationClass.ALL.index), is(10.0f));
			assertThat(basalAreaByUtilization.getCoe(UtilizationClass.U75TO125.index), is(10.0f));
			assertThat(basalAreaByUtilization.getCoe(UtilizationClass.U125TO175.index), is(0.0f));
			assertThat(basalAreaByUtilization.getCoe(UtilizationClass.U175TO225.index), is(0.0f));
			assertThat(basalAreaByUtilization.getCoe(UtilizationClass.OVER225.index), is(0.0f));
		}

		@Test
		void testTypical() throws ProcessingException {

			var becDefinition = becLookup.get("CDF").get();

			@SuppressWarnings("unchecked")
			var genera = (List<GenusDefinition>) controlMap.get(ControlKey.SP0_DEF.name());
			var genus = genera.get(2);

			UtilizationVector quadMeanDiameterByUtilization = Utils.utilizationVector(31.5006275f);
			UtilizationVector basalAreaByUtilization = Utils.utilizationVector(0.406989872f);

			EstimationMethods.estimateBaseAreaByUtilization(
					controlMap, becDefinition, quadMeanDiameterByUtilization, basalAreaByUtilization, genus.getAlias()
			);

			// Result of run in FORTRAN VDYP7 with the above parameters.
			assertThat(
					basalAreaByUtilization,
					contains(0.0f, 0.406989872f, 0.00509467721f, 0.0138180256f, 0.023145527f, 0.36493164f)
			);
		}

		@Test
		void testWithInstantiatedControlMap() throws ProcessingException {
			var dq = Utils.utilizationVector();
			var ba = Utils.utilizationVector();
			dq.setCoe(0, 31.6622887f);
			dq.setCoe(1, 10.0594692f);
			dq.setCoe(2, 14.966774f);
			dq.setCoe(3, 19.9454956f);
			dq.setCoe(4, 46.1699982f);

			ba.setCoe(0, 0.397305071f);

			var bec = Utils.getBec("CWH", controlMap);

			emp.estimateBaseAreaByUtilization(bec, dq, ba, "B");

			assertThat(
					ba,
					VdypMatchers
							.utilization(0f, 0.397305071f, 0.00485289097f, 0.0131751001f, 0.0221586525f, 0.357118428f)
			);

		}

	}

	@Nested
	class CloseUtilizationEstimation {

		@Test
		void testWhenWholeStemVolumeIsZero() throws ProcessingException {

			var becDefinition = becLookup.get("IDF").get();

			@SuppressWarnings("unchecked")
			var genera = (List<GenusDefinition>) controlMap.get(ControlKey.SP0_DEF.name());
			var genus = genera.get(0);

			var volumeEquationGroupMatrix = Utils.<MatrixMap2<String, String, Integer>>expectParsedControl(
					controlMap, ControlKey.VOLUME_EQN_GROUPS, MatrixMap2.class
			);
			int volumeGroup = volumeEquationGroupMatrix.get(genus.getAlias(), becDefinition.getAlias());

			Coefficients aAdjust = Utils.utilizationVector(0.0f);
			UtilizationVector quadMeanDiameterByUtilization = Utils.utilizationVector(0.0f);
			UtilizationVector wholeStemVolumeByUtilization = Utils.utilizationVector(0.0f);
			UtilizationVector closeUtilizationVolume = Utils.utilizationVector(0.0f);
			float loreyHeight = 30.0f;

			EstimationMethods.estimateCloseUtilizationVolume(
					controlMap, UtilizationClass.U75TO125, aAdjust, volumeGroup, loreyHeight,
					quadMeanDiameterByUtilization, wholeStemVolumeByUtilization, closeUtilizationVolume
			);

			assertThat(closeUtilizationVolume, contains(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f));
		}

		@Test
		void testTypical() throws ProcessingException {

			var becDefinition = becLookup.get("CDF").get();

			@SuppressWarnings("unchecked")
			var genera = (List<GenusDefinition>) controlMap.get(ControlKey.SP0_DEF.name());
			var genus = genera.get(2);

			var volumeEquationGroupMatrix = Utils.<MatrixMap2<String, String, Integer>>expectParsedControl(
					controlMap, ControlKey.VOLUME_EQN_GROUPS, MatrixMap2.class
			);
			int volumeGroup = volumeEquationGroupMatrix.get(genus.getAlias(), becDefinition.getAlias());

			Coefficients aAdjust = Utils.utilizationVector(0.0f);
			UtilizationVector quadMeanDiameterByUtilization = Utils
					.utilizationVector(0.0f, 31.5006275f, 9.17065048f, 13.6603403f, 18.1786556f, 42.0707741f);
			UtilizationVector wholeStemVolumeByUtilization = Utils
					.utilizationVector(0.0f, 0.0186868683f, 0.0764646456f, 0.176565647f, 6.00080776f);
			UtilizationVector closeUtilizationVolume = Utils.utilizationVector(0.0f);
			float loreyHeight = 36.7552986f;

			EstimationMethods.estimateCloseUtilizationVolume(
					controlMap, UtilizationClass.U175TO225, aAdjust, volumeGroup, loreyHeight,
					quadMeanDiameterByUtilization, wholeStemVolumeByUtilization, closeUtilizationVolume
			);

			// Result of run in FORTRAN VDYP7 with the above parameters.
			assertThat(closeUtilizationVolume, contains(0.0f, 0.0f, 0.0f, 0.0f, 0.15350838f, 0.0f));
		}

		@Test
		void testVeteran() throws Exception {
			TestUtils.populateControlMapCloseUtilization(controlMap, closeUtilMap(12));

			var utilizationClass = UtilizationClass.OVER225;
			var aAdjust = new Coefficients(new float[] { 0f, 0f, 0f, -0.0981800035f }, 1);
			var volumeGroup = 12;
			var lorieHeight = 26.2000008f;
			var quadMeanDiameterUtil = Utils.utilizationVector(51.8356705f, 0f, 0f, 0f, 51.8356705f);
			var wholeStemVolumeUtil = Utils.utilizationVector(0f, 0f, 0f, 0f, 6.11904192f);

			var closeUtilizationUtil = Utils.utilizationVector(0f, 0f, 0f, 0f, 0f);

			emp.estimateCloseUtilizationVolume(
					utilizationClass, aAdjust, volumeGroup, lorieHeight, quadMeanDiameterUtil, wholeStemVolumeUtil,
					closeUtilizationUtil
			);

			assertThat(closeUtilizationUtil, utilization(0f, 0f, 0f, 0f, 0f, 5.86088896f));

		}
	}

	@Nested
	class CloseUtilizationLessDecayEstimation {
		@Test
		void testWhenCloseUtilizationIsZero() throws ProcessingException {

			var becDefinition = becLookup.get("CDF").get();

			@SuppressWarnings("unchecked")
			var genera = (List<GenusDefinition>) controlMap.get(ControlKey.SP0_DEF.name());
			var genus = genera.get(2);

			Coefficients aAdjust = Utils.utilizationVector(0.0f);
			UtilizationVector quadMeanDiameterByUtilization = Utils.utilizationVector(0.0f);
			UtilizationVector closeUtilization = Utils.utilizationVector(0.0f);
			UtilizationVector closeUtilizationNetOfDecay = Utils.utilizationVector(0.0f);

			var volumeEquationGroupMatrix = Utils.<MatrixMap2<String, String, Integer>>expectParsedControl(
					controlMap, ControlKey.VOLUME_EQN_GROUPS, MatrixMap2.class
			);
			int volumeGroup = volumeEquationGroupMatrix.get(genus.getAlias(), becDefinition.getAlias());

			EstimationMethods.estimateNetDecayVolume(
					controlMap, genus.getAlias(), becDefinition.getRegion(), UtilizationClass.U175TO225, aAdjust,
					volumeGroup, 0.0f, quadMeanDiameterByUtilization, closeUtilization, closeUtilizationNetOfDecay
			);

			assertThat(closeUtilizationNetOfDecay, contains(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f));
		}

		@Test
		void testTypical() throws ProcessingException {

			var becDefinition = becLookup.get("CWH").get();

			@SuppressWarnings("unchecked")
			var genera = (List<GenusDefinition>) controlMap.get(ControlKey.SP0_DEF.name());
			var genus = genera.get(2);

			Coefficients aAdjust = Utils.utilizationVector(0.0f);
			UtilizationVector quadMeanDiameterByUtilization = Utils
					.utilizationVector(0.0f, 31.5006275f, 9.17065048f, 13.6603403f, 18.1786556f, 42.0707741f);
			UtilizationVector closeUtilization = Utils
					.utilizationVector(0.0f, 6.01939344f, 0.000909090857f, 0.0503030308f, 0.153636351f, 5.81454515f);
			UtilizationVector closeUtilizationNetOfDecay = Utils.utilizationVector(0.0f);

			var decayEquationGroupMatrix = Utils.<MatrixMap2<String, String, Integer>>expectParsedControl(
					controlMap, ControlKey.DECAY_GROUPS, MatrixMap2.class
			);
			int decayGroup = decayEquationGroupMatrix.get(genus.getAlias(), becDefinition.getAlias());

			EstimationMethods.estimateNetDecayVolume(
					controlMap, genus.getAlias(), becDefinition.getRegion(), UtilizationClass.U175TO225, aAdjust,
					decayGroup, 54.0f, quadMeanDiameterByUtilization, closeUtilization, closeUtilizationNetOfDecay
			);

			// Result of run in FORTRAN VDYP7 with the above parameters.
			assertThat(closeUtilizationNetOfDecay, contains(0.0f, 0.0f, 0.0f, 0.0f, 0.15293269f, 0.0f));
		}
	}

	@Nested
	class CloseUtilizationLessDecayAndWastageEstimation {
		@Test
		void testWhenCloseUtilizationLessDecayIsZero() throws ProcessingException {

			var becDefinition = becLookup.get("CDF").get();

			@SuppressWarnings("unchecked")
			var genera = (List<GenusDefinition>) controlMap.get(ControlKey.SP0_DEF.name());
			var genus = genera.get(2);

			Coefficients aAdjust = Utils.utilizationVector(0.0f);
			UtilizationVector quadMeanDiameterByUtilization = Utils.utilizationVector(0.0f);
			UtilizationVector closeUtilization = Utils.utilizationVector(0.0f);
			UtilizationVector closeUtilizationNetOfDecay = Utils.utilizationVector(0.0f);
			UtilizationVector closeUtilizationNetOfDecayAndWastage = Utils.utilizationVector(0.0f);

			EstimationMethods.estimateNetDecayAndWasteVolume(
					controlMap, becDefinition.getRegion(), UtilizationClass.U175TO225, aAdjust, genus.getAlias(), 0.0f,
					quadMeanDiameterByUtilization, closeUtilization, closeUtilizationNetOfDecay,
					closeUtilizationNetOfDecayAndWastage
			);

			assertThat(closeUtilizationNetOfDecay, contains(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f));
		}

		@Test
		void testTypical() throws ProcessingException {

			var becDefinition = becLookup.get("CWH").get();

			@SuppressWarnings("unchecked")
			var genera = (List<GenusDefinition>) controlMap.get(ControlKey.SP0_DEF.name());
			var genus = genera.get(2);

			Coefficients aAdjust = Utils.utilizationVector(0.0f);
			UtilizationVector quadMeanDiameterByUtilization = Utils
					.utilizationVector(0.0f, 31.5006275f, 9.17065048f, 13.6603403f, 18.1786556f, 42.0707741f);
			UtilizationVector closeUtilization = Utils
					.utilizationVector(0.0f, 6.01939344f, 0.000909090857f, 0.0503030308f, 0.153636351f, 5.81454515f);
			UtilizationVector closeUtilizationNetOfDecay = Utils
					.utilizationVector(0.0f, 5.90565634f, 0.000909090857f, 0.0502020158f, 0.152929291f, 5.70161581f);
			UtilizationVector closeUtilizationNetOfDecayAndWastage = Utils.utilizationVector(0.0f);

			EstimationMethods.estimateNetDecayAndWasteVolume(
					controlMap, becDefinition.getRegion(), UtilizationClass.U175TO225, aAdjust, genus.getAlias(),
					36.7552986f, quadMeanDiameterByUtilization, closeUtilization, closeUtilizationNetOfDecay,
					closeUtilizationNetOfDecayAndWastage
			);

			// Result of run in FORTRAN VDYP7 with the above parameters.
			assertThat(closeUtilizationNetOfDecayAndWastage, contains(0.0f, 0.0f, 0.0f, 0.0f, 0.15271991f, 0.0f));
		}
	}

	@Nested
	class CloseUtilizationLessDecayAndWastageAndBreakageEstimation {

		@Test
		void testWhenCloseUtilizationLessDecayAndWastageIsZero() throws ProcessingException {

			var becDefinition = becLookup.get("CDF").get();

			@SuppressWarnings("unchecked")
			var genera = (List<GenusDefinition>) controlMap.get(ControlKey.SP0_DEF.name());
			var genus = genera.get(2);

			UtilizationVector quadMeanDiameterByUtilization = Utils.utilizationVector(0.0f);
			UtilizationVector closeUtilization = Utils.utilizationVector(0.0f);
			UtilizationVector closeUtilizationNetOfDecayAndWastage = Utils.utilizationVector(0.0f);
			UtilizationVector closeUtilizationNetOfDecayWastageAndBreakage = Utils.utilizationVector(0.0f);

			var breakageEquationGroupMatrix = Utils.<MatrixMap2<String, String, Integer>>expectParsedControl(
					controlMap, ControlKey.BREAKAGE_GROUPS, MatrixMap2.class
			);
			int breakageGroup = breakageEquationGroupMatrix.get(genus.getAlias(), becDefinition.getAlias());

			EstimationMethods.estimateNetDecayWasteAndBreakageVolume(
					controlMap, UtilizationClass.U175TO225, breakageGroup, quadMeanDiameterByUtilization,
					closeUtilization, closeUtilizationNetOfDecayAndWastage, closeUtilizationNetOfDecayWastageAndBreakage
			);

			assertThat(closeUtilizationNetOfDecayAndWastage, contains(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f));
		}

		@Test
		void testTypical() throws ProcessingException {

			var becDefinition = becLookup.get("CWH").get();

			@SuppressWarnings("unchecked")
			var genera = (List<GenusDefinition>) controlMap.get(ControlKey.SP0_DEF.name());
			var genus = genera.get(2);

			UtilizationVector quadMeanDiameterByUtilization = Utils
					.utilizationVector(0.0f, 31.5006275f, 9.17065048f, 13.6603403f, 18.1786556f, 42.0707741f);
			UtilizationVector closeUtilization = Utils
					.utilizationVector(0.0f, 6.01939344f, 0.000909090857f, 0.0503030308f, 0.153636351f, 5.81454515f);
			UtilizationVector closeUtilizationNetOfDecayAndWastage = Utils
					.utilizationVector(0.0f, 0.0f, 0.0f, 0.0f, 0.15271991f, 0.0f);
			UtilizationVector closeUtilizationNetOfDecayWastageAndBreakage = Utils.utilizationVector(0.0f);

			var breakageEquationGroupMatrix = Utils.<MatrixMap2<String, String, Integer>>expectParsedControl(
					controlMap, ControlKey.BREAKAGE_GROUPS, MatrixMap2.class
			);
			int breakageGroup = breakageEquationGroupMatrix.get(genus.getAlias(), becDefinition.getAlias());

			EstimationMethods.estimateNetDecayWasteAndBreakageVolume(
					controlMap, UtilizationClass.U175TO225, breakageGroup, quadMeanDiameterByUtilization,
					closeUtilization, closeUtilizationNetOfDecayAndWastage, closeUtilizationNetOfDecayWastageAndBreakage
			);

			// Result of run in FORTRAN VDYP7 with the above parameters.
			assertThat(
					closeUtilizationNetOfDecayWastageAndBreakage, contains(0.0f, 0.0f, 0.0f, 0.0f, 0.14595404f, 0.0f)
			);
		}

	}

	@Nested
	class EstimateQuadMeanDiameterByUtilization {

		@Test
		void testTest1() throws Exception {
			var controlMap = TestUtils.loadControlMap();

			var coe = Utils.utilizationVector();
			coe.setAll(31.6622887f);

			var bec = Utils.getBec("CWH", controlMap);

			emp.estimateQuadMeanDiameterByUtilization(bec, coe, "B");

			assertThat(coe, utilization(0f, 31.6622887f, 10.0594692f, 14.966774f, 19.9454956f, 46.1699982f));
		}

		@Test
		void testTest2() throws Exception {
			var controlMap = TestUtils.loadControlMap();

			var coe = Utils.utilizationVector();
			coe.setAll(13.4943399f);

			var bec = Utils.getBec("MH", controlMap);

			emp.estimateQuadMeanDiameterByUtilization(bec, coe, "L");

			assertThat(coe, utilization(0f, 13.4943399f, 10.2766619f, 14.67033f, 19.4037666f, 25.719244f));
		}

	}

	@Nested
	class EstimateQuadMeanDiameterForSpecies {
		@Test
		void testSimple() throws Exception {

			var layer = VdypLayer.build(builder -> {
				builder.polygonIdentifier("Test", 2024);
				builder.layerType(LayerType.PRIMARY);
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
				builder.addSite(siteBuilder -> {
					siteBuilder.ageTotal(55f);
					siteBuilder.yearsToBreastHeight(1f);
					siteBuilder.height(32.2999992f);
				});
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

		@Test
		void testClampSimple() throws Exception {
			var limits = new EstimationMethods.Limits(48.3f, 68.7f, 0.729f, 1.718f);
			float standTreesPerHectare = 620.5049f;
			float minQuadMeanDiameter = 7.6f;
			float loreyHeightSpec = 38.74565f;
			float baseArea1 = 0.36464578f;
			float baseArea2 = 44.260338f;
			float quadMeanDiameter1 = 31.697449f;
			float treesPerHectare2 = 615.8839f;
			float quadMeanDiameter2 = 30.249138f;

			float dq = emp.estimateQuadMeanDiameterClampResult(
					limits, standTreesPerHectare, minQuadMeanDiameter, loreyHeightSpec, baseArea1, baseArea2,
					quadMeanDiameter1, treesPerHectare2, quadMeanDiameter2
			);

			assertThat(dq, is(quadMeanDiameter1));

		}

		@Test
		void testClampToLow2() throws Exception {
			var limits = new EstimationMethods.Limits(48.3f, 68.7f, 0.729f, 1.718f);
			float standTreesPerHectare = 620.5049f;
			float minQuadMeanDiameter = 7.6f;
			float loreyHeightSpec = 38.74565f;

			float baseArea1 = 44.36464578f;
			float baseArea2 = 0.1f;

			float quadMeanDiameter2 = 7.3f; // Less than minQuadMeanDiameter 7.6

			float treesPerHectare2 = BaseAreaTreeDensityDiameter.treesPerHectare(baseArea2, quadMeanDiameter2);
			float treesPerHectare1 = standTreesPerHectare - treesPerHectare2;

			float quadMeanDiameter1 = BaseAreaTreeDensityDiameter.quadMeanDiameter(baseArea1, treesPerHectare1);

			float dq = emp.estimateQuadMeanDiameterClampResult(
					limits, standTreesPerHectare, minQuadMeanDiameter, loreyHeightSpec, baseArea1, baseArea2,
					quadMeanDiameter1, treesPerHectare2, quadMeanDiameter2
			);

			assertThat(dq, closeTo(30.722431f));

		}

		@Test
		void testClampToLow1() throws Exception {
			var limits = new EstimationMethods.Limits(48.3f, 68.7f, 0.729f, 1.718f);
			float standTreesPerHectare = 620.5049f;
			float minQuadMeanDiameter = 7.6f;
			float loreyHeightSpec = 38.74565f;

			float baseArea1 = 30f;
			float baseArea2 = 10f;

			float quadMeanDiameter1 = 26f; // Less than computed min of 28.245578

			float treesPerHectare1 = BaseAreaTreeDensityDiameter.treesPerHectare(baseArea1, quadMeanDiameter1);
			float treesPerHectare2 = standTreesPerHectare - treesPerHectare1;

			float quadMeanDiameter2 = BaseAreaTreeDensityDiameter.quadMeanDiameter(baseArea2, treesPerHectare2);

			float dq = emp.estimateQuadMeanDiameterClampResult(
					limits, standTreesPerHectare, minQuadMeanDiameter, loreyHeightSpec, baseArea1, baseArea2,
					quadMeanDiameter1, treesPerHectare2, quadMeanDiameter2
			);

			assertThat(dq, closeTo(28.245578f));

		}

		@Test
		void testClampToHigh1() throws Exception {
			var limits = new EstimationMethods.Limits(48.3f, 68.7f, 0.729f, 1.718f);
			float standTreesPerHectare = 620.5049f;
			float minQuadMeanDiameter = 7.6f;
			float loreyHeightSpec = 38.74565f;

			float baseArea1 = 30f;
			float baseArea2 = 10f;

			float quadMeanDiameter1 = 70f; // More than than computed max of 66.565033

			float treesPerHectare1 = BaseAreaTreeDensityDiameter.treesPerHectare(baseArea1, quadMeanDiameter1);
			float treesPerHectare2 = standTreesPerHectare - treesPerHectare1;

			float quadMeanDiameter2 = BaseAreaTreeDensityDiameter.quadMeanDiameter(baseArea2, treesPerHectare2);

			float dq = emp.estimateQuadMeanDiameterClampResult(
					limits, standTreesPerHectare, minQuadMeanDiameter, loreyHeightSpec, baseArea1, baseArea2,
					quadMeanDiameter1, treesPerHectare2, quadMeanDiameter2
			);

			assertThat(dq, closeTo(66.565033f));

		}

	}

	@Nested
	class WholeStemVolumeEstimation {

		@Test
		void testPrimary() throws ProcessingException {

			var becDefinition = becLookup.get("CWH").get();

			@SuppressWarnings("unchecked")
			var genera = (List<GenusDefinition>) controlMap.get(ControlKey.SP0_DEF.name());
			var genus = genera.get(2);

			UtilizationVector quadMeanDiameterByUtilization = Utils
					.utilizationVector(0.0f, 31.5006275f, 9.17065048f, 13.6603403f, 18.1786556f, 42.0707741f);
			UtilizationVector basalAreaByUtilization = Utils
					.utilizationVector(0.0f, 0.406989872f, 0.00507070683f, 0.0137676764f, 0.0230707061f, 0.365080774f);
			UtilizationVector wholeStemVolumeByUtilization = Utils
					.utilizationVector(0.0f, 6.27250576f, 0.0f, 0.0f, 0.0f, 0.0f);

			var volumeEquationGroupMatrix = Utils.<MatrixMap2<String, String, Integer>>expectParsedControl(
					controlMap, ControlKey.VOLUME_EQN_GROUPS, MatrixMap2.class
			);
			int volumeGroup = volumeEquationGroupMatrix.get(genus.getAlias(), becDefinition.getAlias());

			EstimationMethods.estimateWholeStemVolume(
					controlMap, UtilizationClass.ALL, 0.0f, volumeGroup, 36.7552986f, quadMeanDiameterByUtilization,
					basalAreaByUtilization, wholeStemVolumeByUtilization
			);

			// Result of run in FORTRAN VDYP7 with the above parameters.
			assertThat(
					wholeStemVolumeByUtilization,
					contains(0.0f, 6.27250576f, 0.01865777f, 0.07648385f, 0.17615195f, 6.00121212f)
			);
		}

		@Test
		void testVeteran() throws Exception {
			var utilizationClass = UtilizationClass.OVER225;
			var aAdjust = 0.10881f;
			var volumeGroup = 12;
			var lorieHeight = 26.2000008f;
			var quadMeanDiameterUtil = Utils.utilizationVector(51.8356705f, 0f, 0f, 0f, 51.8356705f);
			var baseAreaUtil = Utils.utilizationVector(0.492921442f, 0f, 0f, 0f, 0.492921442f);
			var wholeStemVolumeUtil = Utils.utilizationVector();

			emp.estimateWholeStemVolume(
					utilizationClass, aAdjust, volumeGroup, lorieHeight, quadMeanDiameterUtil, baseAreaUtil,
					wholeStemVolumeUtil
			);

			assertThat(wholeStemVolumeUtil, utilization(0f, 0f, 0f, 0f, 0f, 6.11904192f));

		}

	}

	@Test
	void testWholeStemVolumePerTreeEstimation() {

		var becDefinition = becLookup.get("CWH").get();

		@SuppressWarnings("unchecked")
		var genera = (List<GenusDefinition>) controlMap.get(ControlKey.SP0_DEF.name());
		var genus = genera.get(2);

		var volumeEquationGroupMatrix = Utils.<MatrixMap2<String, String, Integer>>expectParsedControl(
				controlMap, ControlKey.VOLUME_EQN_GROUPS, MatrixMap2.class
		);
		int volumeGroup = volumeEquationGroupMatrix.get(genus.getAlias(), becDefinition.getAlias());

		float result = EstimationMethods
				.estimateWholeStemVolumePerTree(controlMap, volumeGroup, 36.7552986f, 31.5006275f);

		// Result of run in FORTRAN VDYP7 with the above parameters.
		assertThat(result, is(1.2011181f));
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

	@Nested
	class PrimaryLeadHeightConversion {

		@Test
		void testPrimaryHeightFromLeadHeight() throws Exception {
			float result = emp.primaryHeightFromLeadHeight(20.0f, "B", Region.COASTAL, 40.260403f);

			assertThat(result, closeTo(19.870464f));
		}

		@Test
		void testLeadHeightFromPrimaryHeight() throws Exception {
			float result = emp.leadHeightFromPrimaryHeight(19.870464f, "B", Region.COASTAL, 40.260403f);

			assertThat(result, closeTo(20));
		}

	}

}
