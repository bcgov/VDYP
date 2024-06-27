package ca.bc.gov.nrs.vdyp.common;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;

import java.util.List;

import org.junit.jupiter.api.Test;

import ca.bc.gov.nrs.vdyp.application.ProcessingException;
import ca.bc.gov.nrs.vdyp.model.BecLookup;
import ca.bc.gov.nrs.vdyp.model.Coefficients;
import ca.bc.gov.nrs.vdyp.model.GenusDefinition;
import ca.bc.gov.nrs.vdyp.model.MatrixMap2;
import ca.bc.gov.nrs.vdyp.model.UtilizationClass;
import ca.bc.gov.nrs.vdyp.test.TestUtils;

class EstimationMethodsTest {

	@Test
	void testBasalAreaEstimationWhenBasalAreaAllIsZero() throws ProcessingException {
		var controlMap = TestUtils.loadControlMap();

		var becLookup = (BecLookup) controlMap.get(ControlKey.BEC_DEF.name());
		var becDefinition = becLookup.get("IDF").get();

		@SuppressWarnings("unchecked")
		var genera = (List<GenusDefinition>) controlMap.get(ControlKey.SP0_DEF.name());
		var genus = genera.get(0);

		Coefficients quadMeanDiameterByUtilization = Utils.utilizationVector(0.0f);
		Coefficients basalAreaByUtilization = Utils.utilizationVector(0.0f);

		EstimationMethods.estimateBaseAreaByUtilization(
				controlMap, becDefinition, quadMeanDiameterByUtilization, basalAreaByUtilization, genus.getAlias()
		);

		for (var c : basalAreaByUtilization) {
			assertThat(c, is(0.0f));
		}
	}

	@Test
	void testBasalAreaEstimationWhenBasalAreaAllIsTenAndQuadMeanDiametersAreAllZero() throws ProcessingException {
		var controlMap = TestUtils.loadControlMap();

		var becLookup = (BecLookup) controlMap.get(ControlKey.BEC_DEF.name());
		var becDefinition = becLookup.get("IDF").get();

		@SuppressWarnings("unchecked")
		var genera = (List<GenusDefinition>) controlMap.get(ControlKey.SP0_DEF.name());
		var genus = genera.get(0);

		Coefficients quadMeanDiameterByUtilization = Utils.utilizationVector(0.0f);
		Coefficients basalAreaByUtilization = Utils.utilizationVector(10.0f);

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
	void testBasalAreaEstimation() throws ProcessingException {
		var controlMap = TestUtils.loadControlMap();

		var becLookup = (BecLookup) controlMap.get(ControlKey.BEC_DEF.name());
		var becDefinition = becLookup.get("CDF").get();

		@SuppressWarnings("unchecked")
		var genera = (List<GenusDefinition>) controlMap.get(ControlKey.SP0_DEF.name());
		var genus = genera.get(2);

		Coefficients quadMeanDiameterByUtilization = Utils.utilizationVector(31.5006275f);
		Coefficients basalAreaByUtilization = Utils.utilizationVector(0.406989872f);

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
	void testCloseUtilizationEstimationWhenWholeStemVolumeIsZero() throws ProcessingException {
		var controlMap = TestUtils.loadControlMap();

		var becLookup = (BecLookup) controlMap.get(ControlKey.BEC_DEF.name());
		var becDefinition = becLookup.get("IDF").get();

		@SuppressWarnings("unchecked")
		var genera = (List<GenusDefinition>) controlMap.get(ControlKey.SP0_DEF.name());
		var genus = genera.get(0);

		var volumeEquationGroupMatrix = Utils.<MatrixMap2<String, String, Integer>>expectParsedControl(
				controlMap, ControlKey.VOLUME_EQN_GROUPS, MatrixMap2.class
		);
		int volumeGroup = volumeEquationGroupMatrix.get(genus.getAlias(), becDefinition.getAlias());

		Coefficients aAdjust = Utils.utilizationVector(0.0f);
		Coefficients quadMeanDiameterByUtilization = Utils.utilizationVector(0.0f);
		Coefficients wholeStemVolumeByUtilization = Utils.utilizationVector(0.0f);
		Coefficients closeUtilizationVolume = Utils.utilizationVector(0.0f);
		float loreyHeight = 30.0f;

		EstimationMethods.estimateCloseUtilizationVolume(
				controlMap, UtilizationClass.U75TO125, aAdjust, volumeGroup, loreyHeight, quadMeanDiameterByUtilization,
				wholeStemVolumeByUtilization, closeUtilizationVolume
		);

		assertThat(closeUtilizationVolume, contains(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f));
	}

	@Test
	void testCloseUtilizationEstimation() throws ProcessingException {
		var controlMap = TestUtils.loadControlMap();

		var becLookup = (BecLookup) controlMap.get(ControlKey.BEC_DEF.name());
		var becDefinition = becLookup.get("CDF").get();

		@SuppressWarnings("unchecked")
		var genera = (List<GenusDefinition>) controlMap.get(ControlKey.SP0_DEF.name());
		var genus = genera.get(2);

		var volumeEquationGroupMatrix = Utils.<MatrixMap2<String, String, Integer>>expectParsedControl(
				controlMap, ControlKey.VOLUME_EQN_GROUPS, MatrixMap2.class
		);
		int volumeGroup = volumeEquationGroupMatrix.get(genus.getAlias(), becDefinition.getAlias());

		Coefficients aAdjust = Utils.utilizationVector(0.0f);
		Coefficients quadMeanDiameterByUtilization = Utils
				.utilizationVector(0.0f, 31.5006275f, 9.17065048f, 13.6603403f, 18.1786556f, 42.0707741f);
		Coefficients wholeStemVolumeByUtilization = Utils
				.utilizationVector(0.0f, 0.0186868683f, 0.0764646456f, 0.176565647f, 6.00080776f);
		Coefficients closeUtilizationVolume = Utils.utilizationVector(0.0f);
		float loreyHeight = 36.7552986f;

		EstimationMethods.estimateCloseUtilizationVolume(
				controlMap, UtilizationClass.U175TO225, aAdjust, volumeGroup, loreyHeight,
				quadMeanDiameterByUtilization, wholeStemVolumeByUtilization, closeUtilizationVolume
		);

		// Result of run in FORTRAN VDYP7 with the above parameters.
		assertThat(closeUtilizationVolume, contains(0.0f, 0.0f, 0.0f, 0.0f, 0.15350838f, 0.0f));
	}

	@Test
	void testCloseUtilizationLessDecayEstimationWhenCloseUtilizationIsZero() throws ProcessingException {
		var controlMap = TestUtils.loadControlMap();

		var becLookup = (BecLookup) controlMap.get(ControlKey.BEC_DEF.name());
		var becDefinition = becLookup.get("CDF").get();

		@SuppressWarnings("unchecked")
		var genera = (List<GenusDefinition>) controlMap.get(ControlKey.SP0_DEF.name());
		var genus = genera.get(2);

		Coefficients aAdjust = Utils.utilizationVector(0.0f);
		Coefficients quadMeanDiameterByUtilization = Utils.utilizationVector(0.0f);
		Coefficients closeUtilization = Utils.utilizationVector(0.0f);
		Coefficients closeUtilizationNetOfDecay = Utils.utilizationVector(0.0f);

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
	void testCloseUtilizationLessDecayEstimation() throws ProcessingException {
		var controlMap = TestUtils.loadControlMap();

		var becLookup = (BecLookup) controlMap.get(ControlKey.BEC_DEF.name());
		var becDefinition = becLookup.get("CWH").get();

		@SuppressWarnings("unchecked")
		var genera = (List<GenusDefinition>) controlMap.get(ControlKey.SP0_DEF.name());
		var genus = genera.get(2);

		Coefficients aAdjust = Utils.utilizationVector(0.0f);
		Coefficients quadMeanDiameterByUtilization = Utils
				.utilizationVector(0.0f, 31.5006275f, 9.17065048f, 13.6603403f, 18.1786556f, 42.0707741f);
		Coefficients closeUtilization = Utils
				.utilizationVector(0.0f, 6.01939344f, 0.000909090857f, 0.0503030308f, 0.153636351f, 5.81454515f);
		Coefficients closeUtilizationNetOfDecay = Utils.utilizationVector(0.0f);

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

	@Test
	void testCloseUtilizationLessDecayAndWastageEstimationWhenCloseUtilizationLessDecayIsZero()
			throws ProcessingException {
		var controlMap = TestUtils.loadControlMap();

		var becLookup = (BecLookup) controlMap.get(ControlKey.BEC_DEF.name());
		var becDefinition = becLookup.get("CDF").get();

		@SuppressWarnings("unchecked")
		var genera = (List<GenusDefinition>) controlMap.get(ControlKey.SP0_DEF.name());
		var genus = genera.get(2);

		Coefficients aAdjust = Utils.utilizationVector(0.0f);
		Coefficients quadMeanDiameterByUtilization = Utils.utilizationVector(0.0f);
		Coefficients closeUtilization = Utils.utilizationVector(0.0f);
		Coefficients closeUtilizationNetOfDecay = Utils.utilizationVector(0.0f);
		Coefficients closeUtilizationNetOfDecayAndWastage = Utils.utilizationVector(0.0f);

		EstimationMethods.estimateNetDecayAndWasteVolume(
				controlMap, becDefinition.getRegion(), UtilizationClass.U175TO225, aAdjust, genus.getAlias(), 0.0f,
				quadMeanDiameterByUtilization, closeUtilization, closeUtilizationNetOfDecay,
				closeUtilizationNetOfDecayAndWastage
		);

		assertThat(closeUtilizationNetOfDecay, contains(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f));
	}

	@Test
	void testCloseUtilizationLessDecayAndWastageEstimation() throws ProcessingException {
		var controlMap = TestUtils.loadControlMap();

		var becLookup = (BecLookup) controlMap.get(ControlKey.BEC_DEF.name());
		var becDefinition = becLookup.get("CWH").get();

		@SuppressWarnings("unchecked")
		var genera = (List<GenusDefinition>) controlMap.get(ControlKey.SP0_DEF.name());
		var genus = genera.get(2);

		Coefficients aAdjust = Utils.utilizationVector(0.0f);
		Coefficients quadMeanDiameterByUtilization = Utils
				.utilizationVector(0.0f, 31.5006275f, 9.17065048f, 13.6603403f, 18.1786556f, 42.0707741f);
		Coefficients closeUtilization = Utils
				.utilizationVector(0.0f, 6.01939344f, 0.000909090857f, 0.0503030308f, 0.153636351f, 5.81454515f);
		Coefficients closeUtilizationNetOfDecay = Utils
				.utilizationVector(0.0f, 5.90565634f, 0.000909090857f, 0.0502020158f, 0.152929291f, 5.70161581f);
		Coefficients closeUtilizationNetOfDecayAndWastage = Utils.utilizationVector(0.0f);

		EstimationMethods.estimateNetDecayAndWasteVolume(
				controlMap, becDefinition.getRegion(), UtilizationClass.U175TO225, aAdjust, genus.getAlias(),
				36.7552986f, quadMeanDiameterByUtilization, closeUtilization, closeUtilizationNetOfDecay,
				closeUtilizationNetOfDecayAndWastage
		);

		// Result of run in FORTRAN VDYP7 with the above parameters.
		assertThat(closeUtilizationNetOfDecayAndWastage, contains(0.0f, 0.0f, 0.0f, 0.0f, 0.15271991f, 0.0f));
	}

	@Test
	void testCloseUtilizationLessDecayWastageAndBreakageEstimationWhenCloseUtilizationLessDecayAndWastageIsZero()
			throws ProcessingException {
		var controlMap = TestUtils.loadControlMap();

		var becLookup = (BecLookup) controlMap.get(ControlKey.BEC_DEF.name());
		var becDefinition = becLookup.get("CDF").get();

		@SuppressWarnings("unchecked")
		var genera = (List<GenusDefinition>) controlMap.get(ControlKey.SP0_DEF.name());
		var genus = genera.get(2);

		Coefficients quadMeanDiameterByUtilization = Utils.utilizationVector(0.0f);
		Coefficients closeUtilization = Utils.utilizationVector(0.0f);
		Coefficients closeUtilizationNetOfDecayAndWastage = Utils.utilizationVector(0.0f);
		Coefficients closeUtilizationNetOfDecayWastageAndBreakage = Utils.utilizationVector(0.0f);

		var breakageEquationGroupMatrix = Utils.<MatrixMap2<String, String, Integer>>expectParsedControl(
				controlMap, ControlKey.BREAKAGE_GROUPS, MatrixMap2.class
		);
		int breakageGroup = breakageEquationGroupMatrix.get(genus.getAlias(), becDefinition.getAlias());

		EstimationMethods.estimateNetDecayWasteAndBreakageVolume(
				controlMap, UtilizationClass.U175TO225, breakageGroup, quadMeanDiameterByUtilization, closeUtilization,
				closeUtilizationNetOfDecayAndWastage, closeUtilizationNetOfDecayWastageAndBreakage
		);

		assertThat(closeUtilizationNetOfDecayAndWastage, contains(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f));
	}

	@Test
	void testCloseUtilizationLessDecayWastageAndBreakageEstimation() throws ProcessingException {
		var controlMap = TestUtils.loadControlMap();

		var becLookup = (BecLookup) controlMap.get(ControlKey.BEC_DEF.name());
		var becDefinition = becLookup.get("CWH").get();

		@SuppressWarnings("unchecked")
		var genera = (List<GenusDefinition>) controlMap.get(ControlKey.SP0_DEF.name());
		var genus = genera.get(2);

		Coefficients quadMeanDiameterByUtilization = Utils
				.utilizationVector(0.0f, 31.5006275f, 9.17065048f, 13.6603403f, 18.1786556f, 42.0707741f);
		Coefficients closeUtilization = Utils
				.utilizationVector(0.0f, 6.01939344f, 0.000909090857f, 0.0503030308f, 0.153636351f, 5.81454515f);
		Coefficients closeUtilizationNetOfDecayAndWastage = Utils
				.utilizationVector(0.0f, 0.0f, 0.0f, 0.0f, 0.15271991f, 0.0f);
		Coefficients closeUtilizationNetOfDecayWastageAndBreakage = Utils.utilizationVector(0.0f);

		var breakageEquationGroupMatrix = Utils.<MatrixMap2<String, String, Integer>>expectParsedControl(
				controlMap, ControlKey.BREAKAGE_GROUPS, MatrixMap2.class
		);
		int breakageGroup = breakageEquationGroupMatrix.get(genus.getAlias(), becDefinition.getAlias());

		EstimationMethods.estimateNetDecayWasteAndBreakageVolume(
				controlMap, UtilizationClass.U175TO225, breakageGroup, quadMeanDiameterByUtilization, closeUtilization,
				closeUtilizationNetOfDecayAndWastage, closeUtilizationNetOfDecayWastageAndBreakage
		);

		// Result of run in FORTRAN VDYP7 with the above parameters.
		assertThat(closeUtilizationNetOfDecayWastageAndBreakage, contains(0.0f, 0.0f, 0.0f, 0.0f, 0.14595404f, 0.0f));
	}

	@Test
	void testQuadMeanDiameterEstimation() throws ProcessingException {
		var controlMap = TestUtils.loadControlMap();

		var becLookup = (BecLookup) controlMap.get(ControlKey.BEC_DEF.name());
		var becDefinition = becLookup.get("CWH").get();

		@SuppressWarnings("unchecked")
		var genera = (List<GenusDefinition>) controlMap.get(ControlKey.SP0_DEF.name());
		var genus = genera.get(2);

		Coefficients quadMeanDiameterByUtilization = Utils
				.utilizationVector(0.0f, 31.5006275f, 9.17065048f, 13.6603403f, 18.1786556f, 42.0707741f);

		EstimationMethods.estimateQuadMeanDiameterByUtilization(
				controlMap, becDefinition, quadMeanDiameterByUtilization, genus.getAlias()
		);

		// Result of run in FORTRAN VDYP7 with the above parameters.
		assertThat(
				quadMeanDiameterByUtilization, contains(0.0f, 31.500628f, 10.059469f, 14.966558f, 19.945124f, 46.03752f)
		);
	}

	@Test
	void testWholeStemVolumeEstimation() throws ProcessingException {
		var controlMap = TestUtils.loadControlMap();

		var becLookup = (BecLookup) controlMap.get(ControlKey.BEC_DEF.name());
		var becDefinition = becLookup.get("CWH").get();

		@SuppressWarnings("unchecked")
		var genera = (List<GenusDefinition>) controlMap.get(ControlKey.SP0_DEF.name());
		var genus = genera.get(2);

		Coefficients quadMeanDiameterByUtilization = Utils
				.utilizationVector(0.0f, 31.5006275f, 9.17065048f, 13.6603403f, 18.1786556f, 42.0707741f);
		Coefficients basalAreaByUtilization = Utils
				.utilizationVector(0.0f, 0.406989872f, 0.00507070683f, 0.0137676764f, 0.0230707061f, 0.365080774f);
		Coefficients wholeStemVolumeByUtilization = Utils.utilizationVector(0.0f, 6.27250576f, 0.0f, 0.0f, 0.0f, 0.0f);

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
	void testWholeStemVolumePerTreeEstimation() {
		var controlMap = TestUtils.loadControlMap();

		var becLookup = (BecLookup) controlMap.get(ControlKey.BEC_DEF.name());
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
}
