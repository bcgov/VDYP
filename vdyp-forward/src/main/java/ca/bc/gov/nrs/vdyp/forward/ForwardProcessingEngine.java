package ca.bc.gov.nrs.vdyp.forward;

import static ca.bc.gov.nrs.vdyp.math.FloatMath.clamp;
import static ca.bc.gov.nrs.vdyp.math.FloatMath.exp;
import static ca.bc.gov.nrs.vdyp.math.FloatMath.log;
import static ca.bc.gov.nrs.vdyp.math.FloatMath.pow;
import static java.lang.Math.max;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.bc.gov.nrs.vdyp.application.ProcessingException;
import ca.bc.gov.nrs.vdyp.common.EstimationMethods;
import ca.bc.gov.nrs.vdyp.common.ReconcilationMethods;
import ca.bc.gov.nrs.vdyp.common.Utils;
import ca.bc.gov.nrs.vdyp.common_calculators.custom_exceptions.CommonCalculatorException;
import ca.bc.gov.nrs.vdyp.common_calculators.custom_exceptions.CurveErrorException;
import ca.bc.gov.nrs.vdyp.common_calculators.custom_exceptions.NoAnswerException;
import ca.bc.gov.nrs.vdyp.common_calculators.custom_exceptions.SpeciesErrorException;
import ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexEquation;
import ca.bc.gov.nrs.vdyp.forward.model.VdypEntity;
import ca.bc.gov.nrs.vdyp.forward.model.VdypGrowthDetails;
import ca.bc.gov.nrs.vdyp.forward.model.VdypPolygon;
import ca.bc.gov.nrs.vdyp.math.FloatMath;
import ca.bc.gov.nrs.vdyp.model.BecDefinition;
import ca.bc.gov.nrs.vdyp.model.Coefficients;
import ca.bc.gov.nrs.vdyp.model.CommonData;
import ca.bc.gov.nrs.vdyp.model.GenusDistribution;
import ca.bc.gov.nrs.vdyp.model.LayerType;
import ca.bc.gov.nrs.vdyp.model.MatrixMap2;
import ca.bc.gov.nrs.vdyp.model.MatrixMap2Impl;
import ca.bc.gov.nrs.vdyp.model.MatrixMap3;
import ca.bc.gov.nrs.vdyp.model.MatrixMap3Impl;
import ca.bc.gov.nrs.vdyp.model.Region;
import ca.bc.gov.nrs.vdyp.model.SmallUtilizationClassVariable;
import ca.bc.gov.nrs.vdyp.model.UtilizationClass;
import ca.bc.gov.nrs.vdyp.model.VolumeVariable;
import ca.bc.gov.nrs.vdyp.si32.site.SiteTool;

public class ForwardProcessingEngine {

	private static final Logger logger = LoggerFactory.getLogger(ForwardProcessor.class);

	private static final int UTILIZATION_ALL_INDEX = UtilizationClass.ALL.ordinal();
	private static final int UTILIZATION_SMALL_INDEX = UtilizationClass.SMALL.ordinal();

	private static final float MIN_BASAL_AREA = 0.001f;

	/** π/4/10⁴ */
	public static final float PI_40K = (float) (Math.PI / 40_000);

	/* pp */ final ForwardProcessingState fps;

	public ForwardProcessingEngine(Map<String, Object> controlMap) {

		this.fps = new ForwardProcessingState(controlMap);
	}

	public enum ExecutionStep {
		// Must be first
		NONE, //

		CHECK_FOR_WORK, //
		CALCULATE_MISSING_SITE_CURVES, //
		CALCULATE_COVERAGES, //
		DETERMINE_POLYGON_RANKINGS, //
		ESTIMATE_MISSING_SITE_INDICES, //
		ESTIMATE_MISSING_YEARS_TO_BREAST_HEIGHT_VALUES, //
		CALCULATE_DOMINANT_HEIGHT_AGE_SITE_INDEX, //
		SET_COMPATIBILITY_VARIABLES, //

		// Must be last
		ALL; //

		public ExecutionStep predecessor() {
			if (this == NONE) {
				throw new IllegalStateException("ExecutionStep.None has no predecessor");
			}

			return ExecutionStep.values()[ordinal() - 1];
		}

		public ExecutionStep successor() {
			if (this == ALL) {
				throw new IllegalStateException("ExecutionStep.All has no successor");
			}

			return ExecutionStep.values()[ordinal() + 1];
		}
	}

	public void processPolygon(VdypPolygon polygon) throws ProcessingException {

		processPolygon(polygon, ExecutionStep.ALL);
	}

	public void processPolygon(VdypPolygon polygon, ExecutionStep lastStep) throws ProcessingException {

		logger.info("Starting processing of polygon {}", polygon.getDescription());

		fps.setPolygon(polygon);

		// All of BANKCHK1 that we need
		validatePolygon(polygon);

		executeForwardAlgorithm(lastStep);
	}

	private void executeForwardAlgorithm(ExecutionStep lastStep) throws ProcessingException {

		PolygonProcessingState pps = fps.getPolygonProcessingState();
		Bank bank = fps.getBank(0, LayerType.PRIMARY);

		logger.info("Beginning processing of polygon {} layer {}", pps.getLayer().getParent(), pps.getLayer());

		// BANKCHK1, simplified for the parameters METH_CHK = 4, LayerI = 1, and INSTANCE = 1
		if (lastStep.ordinal() >= ExecutionStep.CHECK_FOR_WORK.ordinal()) {
			stopIfNoWork(pps);
		}

		// SCINXSET - note these are calculated directly from the Primary bank of instance 1
		if (lastStep.ordinal() >= ExecutionStep.CALCULATE_MISSING_SITE_CURVES.ordinal()) {
			calculateMissingSiteCurves(bank, fps.getSiteCurveMap(), fps.getPolygonProcessingState());
		}

		// VPRIME, method == 1
		if (lastStep.ordinal() >= ExecutionStep.CALCULATE_COVERAGES.ordinal()) {
			calculateCoverages(pps);
		}

		if (lastStep.ordinal() >= ExecutionStep.DETERMINE_POLYGON_RANKINGS.ordinal()) {
			determinePolygonRankings(pps, CommonData.PRIMARY_SPECIES_TO_COMBINE);
		}

		// SITEADD (TODO: SITEADDU when NDEBUG 11 > 0)
		if (lastStep.ordinal() >= ExecutionStep.ESTIMATE_MISSING_SITE_INDICES.ordinal()) {
			estimateMissingSiteIndices(pps);
		}

		if (lastStep.ordinal() >= ExecutionStep.ESTIMATE_MISSING_YEARS_TO_BREAST_HEIGHT_VALUES.ordinal()) {
			estimateMissingYearsToBreastHeightValues(pps);
		}

		// VHDOM1 METH_H = 2, METH_A = 2, METH_SI = 2
		if (lastStep.ordinal() >= ExecutionStep.CALCULATE_DOMINANT_HEIGHT_AGE_SITE_INDEX.ordinal()) {
			calculateDominantHeightAgeSiteIndex(pps, fps.getHl1Coefficients());
		}

		// CVSET1
		if (lastStep.ordinal() >= ExecutionStep.SET_COMPATIBILITY_VARIABLES.ordinal()) {
			setCompatibilityVariables(pps);
		}
		
		fps.storeActive(2, LayerType.PRIMARY);
	}

	private static final float[] DEFAULT_QUAD_MEAN_DIAMETERS = new float[] { Float.NaN, 10.0f, 15.0f, 20.0f, 25.0f };
	private static final float V_BASE_MIN = 0.1f;
	private static final float B_BASE_MIN = 0.01f;

	@SuppressWarnings("unchecked")
	static void setCompatibilityVariables(PolygonProcessingState pps) throws ProcessingException {
		Coefficients aAdjust = new Coefficients(new float[] { 0.0f, 0.0f, 0.0f, 0.0f }, 1);

		var growthDetails = pps.getVdypGrowthDetails();

		// Note: L1COM2 (INL1VGRP, INL1DGRP, INL1BGRP) is initialized when
		// PolygonProcessingState (volumeEquationGroups, decayEquationGroups
		// breakageEquationGroups, respectively) is constructed. Copying
		// the values into LCOM1 is not necessary. Note, however, that
		// VolumeEquationGroup 10 is mapped to 11 (VGRPFIND) - this is done
		// when volumeEquationGroups is built (i.e., when the equivalent to
		// INL1VGRP is built, rather than when LCOM1 VGRPL is built in the
		// original code.)

		var cvVolume = new MatrixMap3[pps.getNSpecies() + 1];
		var cvBasalArea = new MatrixMap2[pps.getNSpecies() + 1];
		var cvQuadraticMeanDiameter = new MatrixMap2[pps.getNSpecies() + 1];
		var cvSmall = new HashMap[pps.getNSpecies() + 1];

		for (int s = 1; s <= pps.getNSpecies(); s++) {

			String genusName = pps.wallet.speciesNames[s];

			float spLoreyHeight_All = pps.wallet.loreyHeights[s][UtilizationClass.ALL.ordinal()];

			Coefficients basalAreas = Utils.utilizationVector();
			Coefficients wholeStemVolumes = Utils.utilizationVector();
			Coefficients closeUtilizationVolumes = Utils.utilizationVector();
			Coefficients closeUtilizationVolumesNetOfDecay = Utils.utilizationVector();
			Coefficients closeUtilizationVolumesNetOfDecayAndWaste = Utils.utilizationVector();
			Coefficients quadMeanDiameters = Utils.utilizationVector();
			Coefficients treesPerHectare = Utils.utilizationVector();

			cvVolume[s] = new MatrixMap3Impl<UtilizationClass, VolumeVariable, LayerType, Float>(
					UtilizationClass.ALL_BUT_SMALL_ALL, VolumeVariable.ALL, LayerType.ALL_USED, (k1, k2, k3) -> 0f
			);
			cvBasalArea[s] = new MatrixMap2Impl<UtilizationClass, LayerType, Float>(
					UtilizationClass.ALL_BUT_SMALL_ALL, LayerType.ALL_USED, (k1, k2) -> 0f
			);
			cvQuadraticMeanDiameter[s] = new MatrixMap2Impl<UtilizationClass, LayerType, Float>(
					UtilizationClass.ALL_BUT_SMALL_ALL, LayerType.ALL_USED, (k1, k2) -> 0f
			);

			for (UtilizationClass uc : UtilizationClass.ALL_BUT_SMALL) {

				basalAreas.setCoe(uc.index, pps.wallet.basalAreas[s][uc.ordinal()]);
				wholeStemVolumes.setCoe(uc.index, pps.wallet.wholeStemVolumes[s][uc.ordinal()]);
				closeUtilizationVolumes.setCoe(uc.index, pps.wallet.closeUtilizationVolumes[s][uc.ordinal()]);
				closeUtilizationVolumesNetOfDecay.setCoe(uc.index, pps.wallet.cuVolumesMinusDecay[s][uc.ordinal()]);
				closeUtilizationVolumesNetOfDecayAndWaste
						.setCoe(uc.index, pps.wallet.cuVolumesMinusDecayAndWastage[s][uc.ordinal()]);

				quadMeanDiameters.setCoe(uc.index, pps.wallet.quadMeanDiameters[s][uc.ordinal()]);
				if (uc != UtilizationClass.ALL && quadMeanDiameters.getCoe(uc.index) <= 0.0f) {
					quadMeanDiameters.setCoe(uc.index, DEFAULT_QUAD_MEAN_DIAMETERS[uc.ordinal()]);
				}
			}

			for (UtilizationClass uc : UtilizationClass.ALL_BUT_SMALL_ALL) {

				float adjustment;
				float baseVolume;

				// Volume less decay and waste
				adjustment = 0.0f;
				baseVolume = pps.wallet.cuVolumesMinusDecay[s][uc.ordinal()];

				if (growthDetails.allowCalculation(baseVolume, V_BASE_MIN, (l, r) -> l > r)) {

					// EMP094
					EstimationMethods.estimateNetDecayAndWasteVolume(
							pps.getBecZone().getRegion(), uc, aAdjust, pps.wallet.speciesNames[s], spLoreyHeight_All,
							pps.getNetDecayWasteCoeMap(), pps.getWasteModifierMap(), quadMeanDiameters,
							closeUtilizationVolumes, closeUtilizationVolumesNetOfDecay,
							closeUtilizationVolumesNetOfDecayAndWaste
					);

					float actualVolume = pps.wallet.cuVolumesMinusDecayAndWastage[s][uc.ordinal()];
					float staticVolume = closeUtilizationVolumesNetOfDecayAndWaste.getCoe(uc.index);
					adjustment = calculateCompatibilityVariable(actualVolume, baseVolume, staticVolume);
				}

				cvVolume[s]
						.put(uc, VolumeVariable.CLOSE_UTIL_VOL_LESS_DECAY_LESS_WASTAGE, LayerType.PRIMARY, adjustment);

				// Volume less decay
				adjustment = 0.0f;
				baseVolume = pps.wallet.closeUtilizationVolumes[s][uc.ordinal()];

				if (growthDetails.allowCalculation(baseVolume, V_BASE_MIN, (l, r) -> l > r)) {

					// EMP093
					int decayGroup = pps.decayEquationGroups[s];
					EstimationMethods.estimateNetDecayVolume(
							pps.wallet.speciesNames[s], pps.getBecZone().getRegion(), uc, aAdjust, decayGroup,
							pps.getPrimarySpeciesAgeAtBreastHeight(), pps.getNetDecayCoeMap(),
							pps.getDecayModifierMap(), quadMeanDiameters, closeUtilizationVolumes,
							closeUtilizationVolumesNetOfDecay
					);

					float actualVolume = pps.wallet.cuVolumesMinusDecay[s][uc.ordinal()];
					float staticVolume = closeUtilizationVolumesNetOfDecay.getCoe(uc.index);
					adjustment = calculateCompatibilityVariable(actualVolume, baseVolume, staticVolume);
				}

				cvVolume[s].put(uc, VolumeVariable.CLOSE_UTIL_VOL_LESS_DECAY, LayerType.PRIMARY, adjustment);

				// Volume
				adjustment = 0.0f;
				baseVolume = pps.wallet.wholeStemVolumes[s][uc.ordinal()];

				if (growthDetails.allowCalculation(baseVolume, V_BASE_MIN, (l, r) -> l > r)) {

					// EMP092
					int volumeGroup = pps.volumeEquationGroups[s];
					EstimationMethods.estimateCloseUtilizationVolume(
							uc, aAdjust, volumeGroup, spLoreyHeight_All, pps.getCloseUtilizationCoeMap(),
							quadMeanDiameters, wholeStemVolumes, closeUtilizationVolumes
					);

					float actualVolume = pps.wallet.closeUtilizationVolumes[s][uc.ordinal()];
					float staticVolume = closeUtilizationVolumes.getCoe(uc.index);
					adjustment = calculateCompatibilityVariable(actualVolume, baseVolume, staticVolume);
				}

				cvVolume[s].put(uc, VolumeVariable.CLOSE_UTIL_VOL, LayerType.PRIMARY, adjustment);
			}

			int primarySpeciesVolumeGroup = pps.volumeEquationGroups[s];
			float primarySpeciesQMDAll = pps.wallet.quadMeanDiameters[s][UTILIZATION_ALL_INDEX];
			var wholeStemVolume = pps.wallet.treesPerHectare[s][UTILIZATION_ALL_INDEX]
					* EstimationMethods.estimateWholeStemVolumePerTree(
							primarySpeciesVolumeGroup, spLoreyHeight_All, primarySpeciesQMDAll,
							pps.getTotalStandWholeStepVolumeCoeMap()
					);

			wholeStemVolumes.setCoe(UTILIZATION_ALL_INDEX, wholeStemVolume);

			EstimationMethods.estimateWholeStemVolume(
					UtilizationClass.ALL, 0.0f, primarySpeciesVolumeGroup, spLoreyHeight_All,
					pps.getWholeStemUtilizationComponentMap(), quadMeanDiameters, basalAreas, wholeStemVolumes
			);

			for (UtilizationClass uc : UtilizationClass.ALL_BUT_SMALL_ALL) {
				float adjustment = 0.0f;
				float basalArea = basalAreas.getCoe(uc.index);
				if (growthDetails.allowCalculation(basalArea, B_BASE_MIN, (l, r) -> l > r)) {
					adjustment = calculateWholeStemVolume(
							pps.wallet.wholeStemVolumes[s][uc.ordinal()], basalArea, wholeStemVolumes.getCoe(uc.index)
					);
				}

				cvVolume[s].put(uc, VolumeVariable.WHOLE_STEM_VOL, LayerType.PRIMARY, adjustment);
			}

			EstimationMethods.estimateQuadMeanDiameterByUtilization(
					pps.getBecZone(), pps.getQuadMeanDiameterUtilizationComponentMap(), quadMeanDiameters, genusName
			);

			EstimationMethods.estimateBaseAreaByUtilization(
					pps.getBecZone(), pps.getBasalAreaUtilizationComponentMap(), quadMeanDiameters, basalAreas,
					genusName
			);

			// Calculate trees-per-hectare per utilization
			treesPerHectare.setCoe(UtilizationClass.ALL.index, pps.wallet.treesPerHectare[s][UTILIZATION_ALL_INDEX]);
			for (UtilizationClass uc : UtilizationClass.UTIL_CLASSES) {
				treesPerHectare.setCoe(
						uc.index,
						calculateTreesPerHectare(basalAreas.getCoe(uc.index), quadMeanDiameters.getCoe(uc.index))
				);
			}

			ReconcilationMethods.reconcileComponents(basalAreas, treesPerHectare, quadMeanDiameters);

			for (UtilizationClass uc : UtilizationClass.UTIL_CLASSES) {
				float baCvValue = pps.wallet.basalAreas[s][uc.ordinal()] - basalAreas.getCoe(uc.index);
				cvBasalArea[s].put(uc, LayerType.PRIMARY, baCvValue);

				float originalQmd = pps.wallet.quadMeanDiameters[s][uc.ordinal()];
				float adjustedQmd = quadMeanDiameters.getCoe(uc.index);

				float qmdCvValue;
				if (growthDetails.allowCalculation(() -> originalQmd < B_BASE_MIN)) {
					qmdCvValue = 0.0f;
				} else if (originalQmd > 0 && adjustedQmd > 0) {
					qmdCvValue = originalQmd - adjustedQmd;
				} else {
					qmdCvValue = 0.0f;
				}

				cvQuadraticMeanDiameter[s].put(uc, LayerType.PRIMARY, qmdCvValue);
			}

			// Small components

			cvSmall[s] = estimateSmallComponents(pps, s, growthDetails);
		}

		pps.setCompatibilityVariableDetails(cvVolume, cvBasalArea, cvQuadraticMeanDiameter, cvSmall);
	}

	/**
	 * Estimate small component utilization values for primary layer
	 *
	 * @param allowCompatibilitySetting
	 *
	 * @throws ProcessingException
	 */
	private static HashMap<SmallUtilizationClassVariable, Float>
			estimateSmallComponents(PolygonProcessingState pps, int speciesIndex, VdypGrowthDetails growthDetails)
					throws ProcessingException {

		Region region = pps.getPolygon().getBiogeoclimaticZone().getRegion();
		String speciesName = pps.wallet.speciesNames[speciesIndex];

		float spLoreyHeight_All = pps.wallet.loreyHeights[speciesIndex][UTILIZATION_ALL_INDEX]; // HLsp
		float spQuadMeanDiameter_All = pps.wallet.quadMeanDiameters[speciesIndex][UTILIZATION_ALL_INDEX]; // DQsp

		// this WHOLE operation on Actual BA's, not 100% occupancy.
		// TODO: verify this: float fractionAvailable = polygon.getPercentForestLand();
		float spBaseArea_All = pps.wallet.basalAreas[speciesIndex][UTILIZATION_ALL_INDEX] /* * fractionAvailable */; // BAsp

		// EMP080
		float cvSmallComponentProbability = smallComponentProbability(pps, speciesName, spLoreyHeight_All, region); // PROBsp

		// EMP081
		float conditionalExpectedBaseArea = conditionalExpectedBaseArea(
				pps, speciesName, spBaseArea_All, spLoreyHeight_All, region
		); // BACONDsp

		// TODO (see previous TODO): conditionalExpectedBaseArea /= fractionAvailable;

		float cvBasalArea_Small = cvSmallComponentProbability * conditionalExpectedBaseArea;

		// EMP082
		float cvQuadMeanDiameter_Small = smallComponentQuadMeanDiameter(pps, speciesName, spLoreyHeight_All); // DQSMsp

		// EMP085
		float cvLoreyHeight_Small = smallComponentLoreyHeight(
				pps, speciesName, spLoreyHeight_All, cvQuadMeanDiameter_Small, spQuadMeanDiameter_All
		); // HLSMsp

		// EMP086
		float cvMeanVolume_Small = meanVolumeSmall(pps, speciesName, cvQuadMeanDiameter_Small, cvLoreyHeight_Small); // VMEANSMs

		var cvSmall = new HashMap<SmallUtilizationClassVariable, Float>();

		float spInputBasalArea_Small = pps.wallet.basalAreas[speciesIndex][UTILIZATION_SMALL_INDEX];
		cvSmall.put(SmallUtilizationClassVariable.BASAL_AREA, spInputBasalArea_Small - cvBasalArea_Small);

		if (growthDetails.allowCalculation(spInputBasalArea_Small, B_BASE_MIN, (l, r) -> l > r)) {
			float spInputQuadMeanDiameter_Small = pps.wallet.quadMeanDiameters[speciesIndex][UTILIZATION_SMALL_INDEX];
			cvSmall.put(
					SmallUtilizationClassVariable.QUAD_MEAN_DIAMETER,
					spInputQuadMeanDiameter_Small - cvQuadMeanDiameter_Small
			);
		} else {
			cvSmall.put(SmallUtilizationClassVariable.QUAD_MEAN_DIAMETER, 0.0f);
		}

		float spInputLoreyHeight_Small = pps.wallet.loreyHeights[speciesIndex][UTILIZATION_SMALL_INDEX];
		if (spInputLoreyHeight_Small > 1.3f && cvLoreyHeight_Small > 1.3f && spInputBasalArea_Small > 0.0f) {
			float cvLoreyHeight = FloatMath.log( (spInputLoreyHeight_Small - 1.3f) / (cvLoreyHeight_Small - 1.3f));
			cvSmall.put(SmallUtilizationClassVariable.LOREY_HEIGHT, cvLoreyHeight);
		} else {
			cvSmall.put(SmallUtilizationClassVariable.LOREY_HEIGHT, 0.0f);
		}

		float spInputWholeStemVolume_Small = pps.wallet.wholeStemVolumes[speciesIndex][UTILIZATION_SMALL_INDEX];
		if (spInputWholeStemVolume_Small > 0.0f && cvMeanVolume_Small > 0.0f
				&& growthDetails.allowCalculation(spInputBasalArea_Small, B_BASE_MIN, (l, r) -> l >= r)) {

			float spInputTreePerHectare_Small = pps.wallet.treesPerHectare[speciesIndex][UTILIZATION_SMALL_INDEX];

			var cvWholeStemVolume = FloatMath
					.log(spInputWholeStemVolume_Small / spInputTreePerHectare_Small / cvMeanVolume_Small);
			cvSmall.put(SmallUtilizationClassVariable.WHOLE_STEM_VOLUME, cvWholeStemVolume);

		} else {
			cvSmall.put(SmallUtilizationClassVariable.WHOLE_STEM_VOLUME, 0.0f);
		}

		return cvSmall;
	}

	// EMP080
	private static float smallComponentProbability(
			PolygonProcessingState pps, String speciesName, float loreyHeight, Region region
	) {
		Coefficients coe = pps.getSmallComponentProbabilityCoefficients().get(speciesName);

		// EQN 1 in IPSJF118.doc

		float a0 = coe.getCoe(1);
		float a1 = coe.getCoe(2);
		float a2 = coe.getCoe(3);
		float a3 = coe.getCoe(4);

		a1 = (region == Region.COASTAL) ? a1 : 0.0f;

		float logit = a0 + //
				a1 + //
				a2 * pps.wallet.yearsAtBreastHeight[pps.getPrimarySpeciesIndex()] + //
				a3 * loreyHeight;

		return exp(logit) / (1.0f + exp(logit));
	}

	// EMP081
	private static float conditionalExpectedBaseArea(
			PolygonProcessingState pps, String speciesName, float basalArea, float loreyHeight, Region region
	) {
		Coefficients coe = pps.getSmallComponentBasalAreaCoefficients().get(speciesName);

		// EQN 3 in IPSJF118.doc

		float a0 = coe.getCoe(1);
		float a1 = coe.getCoe(2);
		float a2 = coe.getCoe(3);
		float a3 = coe.getCoe(4);

		float coast = region == Region.COASTAL ? 1.0f : 0.0f;

		// FIXME due to a bug in VDYP7 it always treats this as interior. Replicating
		// that for now.
		coast = 0f;

		float arg = (a0 + a1 * coast + a2 * basalArea) * exp(a3 * loreyHeight);
		arg = max(arg, 0f);

		return arg;
	}

	// EMP082
	private static float
			smallComponentQuadMeanDiameter(PolygonProcessingState pps, String speciesName, float loreyHeight) {
		Coefficients coe = pps.getSmallComponentQuadMeanDiameterCoefficients().get(speciesName);

		// EQN 5 in IPSJF118.doc

		float a0 = coe.getCoe(1);
		float a1 = coe.getCoe(2);

		float logit = a0 + a1 * loreyHeight;

		return 4.0f + 3.5f * exp(logit) / (1.0f + exp(logit));
	}

	// EMP085
	private static float smallComponentLoreyHeight(
			PolygonProcessingState pps, String speciesName, float speciesLoreyHeight_All,
			float quadMeanDiameterSpecSmall, float speciesQuadMeanDiameter_All
	) {
		Coefficients coe = pps.getSmallComponentLoreyHeightCoefficients().get(speciesName);

		// EQN 1 in IPSJF119.doc

		float a0 = coe.getCoe(1);
		float a1 = coe.getCoe(2);

		return 1.3f + (speciesLoreyHeight_All - 1.3f) //
				* exp(a0 * (pow(quadMeanDiameterSpecSmall, a1) - pow(speciesQuadMeanDiameter_All, a1)));
	}

	// EMP086
	private static float meanVolumeSmall(
			PolygonProcessingState pps, String speciesName, float quadMeanDiameterSpecSmall, float loreyHeightSpecSmall
	) {
		Coefficients coe = pps.getSmallComponentWholeStemVolumeCoefficients().get(speciesName);

		// EQN 1 in IPSJF119.doc

		float a0 = coe.getCoe(1);
		float a1 = coe.getCoe(2);
		float a2 = coe.getCoe(3);
		float a3 = coe.getCoe(4);

		return exp(
				a0 + a1 * log(quadMeanDiameterSpecSmall) + a2 * log(loreyHeightSpecSmall)
						+ a3 * quadMeanDiameterSpecSmall
		);
	}

	public static float calculateTreesPerHectare(float basalArea, float qmd) {
		if (qmd == 0.0f || Float.isNaN(qmd) || Float.isNaN(basalArea)) {
			return 0.0f;
		} else {
			// basalArea is in m**2/hectare. qmd is diameter in cm. pi/4 converts between
			// diameter in cm and area in cm**2 since a = pi * r**2 = pi * (d/2)**2
			// = pi/4 * d**2. Finally, dividing by 10000 converts from cm**2 to m**2.

			return basalArea / PI_40K / (qmd * qmd);
		}
	}

	public static float calculateBasalArea(float qmd, float treesPerHectare) {

		if (Float.isNaN(qmd) || Float.isNaN(treesPerHectare)) {
			return 0.0f;
		} else {
			// qmd is diameter in cm (per tree); qmd**2 is in cm**2. Multiplying by pi/4 converts
			// to area in cm**2. Dividing by 10000 converts into m**2. Finally, multiplying
			// by trees-per-hectare takes the per-tree area and converts it into a per-hectare
			// area - that is, the basal area per hectare.

			return qmd * qmd * PI_40K * treesPerHectare;
		}
	}

	public static float calculateQuadMeanDiameter(float basalArea, float treesPerHectare) {

		if (basalArea > 1.0e6 || basalArea == 0.0 || Float.isNaN(basalArea) || treesPerHectare > 1.0e6
				|| treesPerHectare == 0.0 || Float.isNaN(treesPerHectare)) {
			return 0.0f;
		} else {
			// See comments above explaining this calculation
			return FloatMath.sqrt(basalArea / treesPerHectare / PI_40K);
		}
	}

	private static float calculateCompatibilityVariable(float actualVolume, float baseVolume, float staticVolume) {

		float staticRatio = staticVolume / baseVolume;
		float staticLogit;
		if (staticRatio <= 0.0f) {
			staticLogit = -7.0f;
		} else if (staticRatio >= 1.0f) {
			staticLogit = 7.0f;
		} else {
			staticLogit = clamp(log(staticRatio / (1.0f - staticRatio)), -7.0f, 7.0f);
		}

		float actualRatio = actualVolume / baseVolume;
		float actualLogit;
		if (actualRatio <= 0.0f) {
			actualLogit = -7.0f;
		} else if (actualRatio >= 1.0f) {
			actualLogit = 7.0f;
		} else {
			actualLogit = clamp(log(actualRatio / (1.0f - actualRatio)), -7.0f, 7.0f);
		}

		return actualLogit - staticLogit;
	}

	private static float calculateWholeStemVolume(float actualVolume, float basalArea, float staticVolume) {

		float staticRatio = staticVolume / basalArea;
		float staticLogit;
		if (staticRatio <= 0.0f) {
			staticLogit = -2.0f;
		} else {
			staticLogit = log(staticRatio);
		}

		float actualRatio = actualVolume / basalArea;
		float actualLogit;
		if (actualRatio <= 0.0f) {
			actualLogit = -2.0f;
		} else {
			actualLogit = log(actualRatio);
		}

		return actualLogit - staticLogit;
	}

	static void calculateDominantHeightAgeSiteIndex(
			PolygonProcessingState state, MatrixMap2<String, Region, Coefficients> hl1Coefficients
	) throws ProcessingException {

		// Calculate primary species values
		int primarySpeciesIndex = state.getPrimarySpeciesIndex();

		// (1) Dominant Height
		float primarySpeciesDominantHeight = state.wallet.dominantHeights[primarySpeciesIndex];
		if (Float.isNaN(primarySpeciesDominantHeight)) {
			float loreyHeight = state.wallet.loreyHeights[primarySpeciesIndex][UTILIZATION_ALL_INDEX];
			if (Float.isNaN(loreyHeight)) {
				throw new ProcessingException(
						MessageFormat.format(
								"Neither dominant nor lorey height[All] is available for primary species {}",
								state.wallet.speciesNames[primarySpeciesIndex]
						), 2
				);
			}

			// Estimate dominant height from the lorey height
			String primarySpeciesAlias = state.wallet.speciesNames[primarySpeciesIndex];
			Region primarySpeciesRegion = state.getBecZone().getRegion();

			var coefficients = hl1Coefficients.get(primarySpeciesAlias, primarySpeciesRegion);
			float a0 = coefficients.getCoe(1);
			float a1 = coefficients.getCoe(2);
			float a2 = coefficients.getCoe(3);

			float treesPerHectare = state.wallet.treesPerHectare[primarySpeciesIndex][UTILIZATION_ALL_INDEX];
			float hMult = a0 - a1 + a1 * FloatMath.exp(a2 * (treesPerHectare - 100.0f));

			primarySpeciesDominantHeight = 1.3f + (loreyHeight - 1.3f) / hMult;
		}

		// (2) Age (total, years at breast height, years to breast height
		float primarySpeciesTotalAge = state.wallet.ageTotals[primarySpeciesIndex];
		float primarySpeciesYearsAtBreastHeight = state.wallet.yearsAtBreastHeight[primarySpeciesIndex];
		float primarySpeciesYearsToBreastHeight = state.wallet.yearsToBreastHeight[primarySpeciesIndex];

		Optional<Integer> activeIndex = Optional.empty();

		if (Float.isNaN(primarySpeciesTotalAge)) {

			if (state.hasSecondarySpeciesIndex()
					&& !Float.isNaN(state.wallet.ageTotals[state.getSecondarySpeciesIndex()])) {
				activeIndex = Optional.of(state.getSecondarySpeciesIndex());
			} else {
				for (int i = 1; i <= state.getNSpecies(); i++) {
					if (!Float.isNaN(state.wallet.ageTotals[i])) {
						activeIndex = Optional.of(i);
						break;
					}
				}
			}

			activeIndex.orElseThrow(() -> new ProcessingException("Age data unavailable for ALL species", 5));

			primarySpeciesTotalAge = state.wallet.ageTotals[activeIndex.get()];
			if (!Float.isNaN(primarySpeciesYearsToBreastHeight)) {
				primarySpeciesYearsAtBreastHeight = primarySpeciesTotalAge - primarySpeciesYearsToBreastHeight;
			} else if (!Float.isNaN(primarySpeciesYearsAtBreastHeight)) {
				primarySpeciesYearsToBreastHeight = primarySpeciesTotalAge - primarySpeciesYearsAtBreastHeight;
			} else {
				primarySpeciesYearsAtBreastHeight = state.wallet.yearsAtBreastHeight[activeIndex.get()];
				primarySpeciesYearsToBreastHeight = state.wallet.yearsToBreastHeight[activeIndex.get()];
			}
		}

		// (3) Site Index
		float primarySpeciesSiteIndex = state.wallet.siteIndices[primarySpeciesIndex];
		if (Float.isNaN(primarySpeciesSiteIndex)) {

			if (state.hasSecondarySpeciesIndex()
					&& !Float.isNaN(state.wallet.siteIndices[state.getSecondarySpeciesIndex()])) {
				activeIndex = Optional.of(state.getSecondarySpeciesIndex());
			} else {
				if (activeIndex.isEmpty() || Float.isNaN(state.wallet.siteIndices[activeIndex.get()])) {
					for (int i = 1; i <= state.getNSpecies(); i++) {
						if (!Float.isNaN(state.wallet.siteIndices[i])) {
							activeIndex = Optional.of(i);
							break;
						}
					}
				}
			}
			primarySpeciesSiteIndex = state.wallet.siteIndices[activeIndex
					.orElseThrow(() -> new ProcessingException("Site Index data unavailable for ALL species", 7))];
		} else {
			activeIndex = Optional.of(primarySpeciesIndex);
		}

		SiteIndexEquation siteCurve1 = SiteIndexEquation.getByIndex(state.getSiteCurveNumber(activeIndex.get()));
		SiteIndexEquation siteCurve2 = SiteIndexEquation.getByIndex(state.getSiteCurveNumber(0));

		try {
			double newSI = SiteTool.convertSiteIndexBetweenCurves(siteCurve1, activeIndex.get(), siteCurve2);
			if (newSI > 1.3) {
				primarySpeciesSiteIndex = (float) newSI;
			}
		} catch (CommonCalculatorException e) {
			// do nothing. primarySpeciesSiteIndex will not be modified.
		}

		state.setPrimarySpeciesDetails(
				new PrimarySpeciesDetails(
						primarySpeciesDominantHeight, primarySpeciesSiteIndex, primarySpeciesTotalAge,
						primarySpeciesYearsAtBreastHeight, primarySpeciesYearsToBreastHeight
				)
		);
	}

	/**
	 * For each species for which a years-to-breast-height value was not supplied, calculate it from the given
	 * years-at-breast-height and age-total values if given or otherwise estimate it from the site curve and site index
	 * values for the species.
	 *
	 * @param state the bank in which calculations are performed
	 */
	static void estimateMissingYearsToBreastHeightValues(PolygonProcessingState state) {

		int primarySpeciesIndex = state.getPrimarySpeciesIndex();
		float primarySpeciesSiteIndex = state.wallet.siteIndices[primarySpeciesIndex];

		// Determine the default site index by using the site index of the primary species unless
		// it hasn't been set in which case pick any. Note that there may still not be a
		// meaningful value after this for example when the value is not available for the primary
		// species (see estimateMissingSiteIndices) and it's the only one.

		float defaultSiteIndex = primarySpeciesSiteIndex;

		if (Float.isNaN(defaultSiteIndex)) {
			for (int i : state.getIndices()) {
				if (!Float.isNaN(state.wallet.siteIndices[i])) {
					defaultSiteIndex = state.wallet.siteIndices[i];
					break;
				}
			}
		}

		for (int i : state.getIndices()) {
			if (!Float.isNaN(state.wallet.yearsToBreastHeight[i])) {
				// was supplied
				continue;
			}

			// Note: this block will normally never be executed because of the logic in
			// the constructor of VdypLayerSpecies that computes missing values when the
			// other two measurement values are present.
			if (!Float.isNaN(state.wallet.yearsAtBreastHeight[i])
					&& state.wallet.ageTotals[i] > state.wallet.yearsAtBreastHeight[i]) {
				state.wallet.yearsToBreastHeight[i] = state.wallet.ageTotals[i] - state.wallet.yearsAtBreastHeight[i];
				continue;
			}

			float siteIndex = !Float.isNaN(state.wallet.siteIndices[i]) ? state.wallet.siteIndices[i]
					: defaultSiteIndex;
			try {
				SiteIndexEquation curve = SiteIndexEquation.getByIndex(state.getSiteCurveNumber(i));
				double yearsToBreastHeight = SiteTool.yearsToBreastHeight(curve, siteIndex);
				state.wallet.yearsToBreastHeight[i] = (float) yearsToBreastHeight;
			} catch (CommonCalculatorException e) {
				logger.warn(MessageFormat.format("Unable to determine yearsToBreastHeight of species {0}", i), e);
			}
		}
	}

	/**
	 * (1) If the site index of the primary species has not been set, calculate it as the average of the site indices of
	 * the other species that -do- have one, after converting it between the site curve of the other species and that of
	 * the primary species.
	 * <p>
	 * (2) If the site index of the primary species has (now) been set, calculate that of the other species whose site
	 * index has not been set from the primary site index after converting it between the site curve of the other
	 * species and that of the primary species.
	 *
	 * @param state the bank in which the calculations are done.
	 * @throws ProcessingException
	 */
	static void estimateMissingSiteIndices(PolygonProcessingState state) throws ProcessingException {

		int primarySpeciesIndex = state.getPrimarySpeciesIndex();
		SiteIndexEquation primarySiteCurve = SiteIndexEquation
				.getByIndex(state.getSiteCurveNumber(primarySpeciesIndex));

		// (1)

		if (Float.isNaN(state.wallet.siteIndices[primarySpeciesIndex])) {

			double otherSiteIndicesSum = 0.0f;
			int nOtherSiteIndices = 0;

			for (int i : state.getIndices()) {

				if (i == primarySpeciesIndex) {
					continue;
				}

				float siteIndexI = state.wallet.siteIndices[i];

				if (!Float.isNaN(siteIndexI)) {
					SiteIndexEquation siteCurveI = SiteIndexEquation.getByIndex(state.getSiteCurveNumber(i));

					try {
						double mappedSiteIndex = SiteTool
								.convertSiteIndexBetweenCurves(siteCurveI, siteIndexI, primarySiteCurve);
						if (mappedSiteIndex > 1.3) {
							otherSiteIndicesSum += mappedSiteIndex;
							nOtherSiteIndices += 1;
						}
					} catch (NoAnswerException e) {
						logger.warn(
								MessageFormat.format(
										"there is no conversion from curves {0} to {1}. Skipping species {3}",
										siteCurveI, primarySiteCurve, i
								)
						);
					} catch (CurveErrorException | SpeciesErrorException e) {
						throw new ProcessingException(
								MessageFormat.format(
										"convertSiteIndexBetweenCurves on {0}, {1} and {2} failed", siteCurveI,
										siteIndexI, primarySiteCurve
								), e
						);
					}
				}
			}

			if (nOtherSiteIndices > 0) {
				state.wallet.siteIndices[primarySpeciesIndex] = (float) (otherSiteIndicesSum / nOtherSiteIndices);
			}
		}

		// (2)

		float primarySpeciesSiteIndex = state.wallet.siteIndices[primarySpeciesIndex];
		if (!Float.isNaN(primarySpeciesSiteIndex)) {

			for (int i : state.getIndices()) {

				if (i == primarySpeciesIndex) {
					continue;
				}

				float siteIndexI = state.wallet.siteIndices[i];
				if (Float.isNaN(siteIndexI)) {
					SiteIndexEquation siteCurveI = SiteIndexEquation.getByIndex(state.getSiteCurveNumber(i));

					try {
						double mappedSiteIndex = SiteTool
								.convertSiteIndexBetweenCurves(primarySiteCurve, primarySpeciesSiteIndex, siteCurveI);
						state.wallet.siteIndices[i] = (float) mappedSiteIndex;
					} catch (NoAnswerException e) {
						logger.warn(
								MessageFormat.format(
										"there is no conversion between curves {0} and {1}. Skipping species {2}",
										primarySiteCurve, siteCurveI, i
								)
						);
					} catch (CurveErrorException | SpeciesErrorException e) {
						throw new ProcessingException(
								MessageFormat.format(
										"convertSiteIndexBetweenCurves on {0}, {1} and {2} failed. Skipping species {3}",
										primarySiteCurve, primarySpeciesSiteIndex, siteCurveI, i
								), e
						);
					}
				}
			}
		}

		// Finally, set bank.siteIndices[0] to that of the primary species.
		state.wallet.siteIndices[0] = primarySpeciesSiteIndex;
	}

	/**
	 * Calculate the percentage of forested land covered by each species by dividing the basal area of each given
	 * species with the basal area of the polygon covered by forest.
	 *
	 * @param state the bank in which the calculations are performed
	 */
	static void calculateCoverages(PolygonProcessingState state) {

		logger.atDebug().addArgument(state.getNSpecies()).addArgument(state.wallet.basalAreas[0][0]).log(
				"Calculating coverages as a ratio of Species BA over Total BA. # species: {}; Layer total 7.5cm+ basal area: {}"
		);

		for (int i : state.getIndices()) {
			state.wallet.percentagesOfForestedLand[i] = state.wallet.basalAreas[i][UTILIZATION_ALL_INDEX]
					/ state.wallet.basalAreas[0][UTILIZATION_ALL_INDEX] * 100.0f;

			logger.atDebug().addArgument(i).addArgument(state.wallet.speciesIndices[i])
					.addArgument(state.wallet.speciesNames[i]).addArgument(state.wallet.basalAreas[i][0])
					.addArgument(state.wallet.percentagesOfForestedLand[i])
					.log("Species {}: SP0 {}, Name {}, Species 7.5cm+ BA {}, Calculated Percent {}");
		}
	}

	/**
	 * Calculate the siteCurve number of all species for which one was not supplied. All calculations are done in the
	 * given bank, but the resulting site curve vector is stored in the given PolygonProcessingState.
	 *
	 * FORTRAN notes: the original SXINXSET function set both INXSC/INXSCV and BANK3/SCNB, except for index 0 of SCNB.
	 *
	 * @param bank         the bank in which the calculations are done.
	 * @param siteCurveMap the Site Curve definitions.
	 * @param pps          the PolygonProcessingState to where the calculated curves are also to be
	 */
	static void calculateMissingSiteCurves(
			Bank bank, MatrixMap2<String, Region, SiteIndexEquation> siteCurveMap, PolygonProcessingState pps
	) {
		BecDefinition becZone = bank.getBecZone();

		for (int i : bank.getIndices()) {

			if (bank.siteCurveNumbers[i] == VdypEntity.MISSING_INTEGER_VALUE) {

				Optional<SiteIndexEquation> scIndex = Optional.empty();

				Optional<GenusDistribution> sp0Dist = bank.sp64Distributions[i].getSpeciesDistribution(0);

				// First alternative is to use the name of the first of the species' sp64Distributions
				if (sp0Dist.isPresent()) {
					if (!siteCurveMap.isEmpty()) {
						scIndex = Utils
								.optSafe(siteCurveMap.get(sp0Dist.get().getGenus().getAlias(), becZone.getRegion()));
					} else {
						SiteIndexEquation siCurve = SiteTool
								.getSICurve(bank.speciesNames[i], becZone.getRegion().equals(Region.COASTAL));
						scIndex = siCurve == SiteIndexEquation.SI_NO_EQUATION ? Optional.empty() : Optional.of(siCurve);
					}
				}

				// Second alternative is to use the species name as given in the species' "speciesName" field
				if (scIndex.isEmpty()) {
					String sp0 = bank.speciesNames[i];
					if (!siteCurveMap.isEmpty()) {
						scIndex = Utils.optSafe(siteCurveMap.get(sp0, becZone.getRegion()));
					} else {
						SiteIndexEquation siCurve = SiteTool
								.getSICurve(sp0, becZone.getRegion().equals(Region.COASTAL));
						scIndex = siCurve == SiteIndexEquation.SI_NO_EQUATION ? Optional.empty() : Optional.of(siCurve);
					}
				}

				bank.siteCurveNumbers[i] = scIndex.orElseThrow().n();
			}
		}

		pps.setSiteCurveNumbers(bank.siteCurveNumbers);
	}

	/**
	 * Validate that the given polygon is in good order for processing.
	 *
	 * @param polygon the subject polygon.
	 * @returns if this method doesn't throw, all is good.
	 * @throws ProcessingException if the polygon does not pass validation.
	 */
	private static void validatePolygon(VdypPolygon polygon) throws ProcessingException {

		if (polygon.getDescription().getYear() < 1900) {

			throw new ProcessingException(
					MessageFormat.format(
							"Polygon {0}''s year value {1} is < 1900", polygon.getDescription().getName(),
							polygon.getDescription().getYear()
					)
			);
		}
	}

	private static void stopIfNoWork(PolygonProcessingState state) throws ProcessingException {

		// The following is extracted from BANKCHK1, simplified for the parameters
		// METH_CHK = 4, LayerI = 1, and INSTANCE = 1. So IR = 1, which is the first
		// bank, numbered 0.

		// => all that is done is that an exception is thrown if there are no species to
		// process.

		if (state.getNSpecies() == 0) {
			throw new ProcessingException(
					MessageFormat.format(
							"Polygon {0} layer 0 has no species with basal area above {1}",
							state.getLayer().getParent().getDescription().getName(), MIN_BASAL_AREA
					)
			);
		}
	}

	// PRIMFIND
	/**
	 * Returns a {@code SpeciesRankingDetails} instance giving:
	 * <ul>
	 * <li>the index in {@code bank} of the primary species
	 * <li>the index in {@code bank} of the secondary species, or Optional.empty() if none, and
	 * <li>the percentage of forested land occupied by the primary species
	 * </ul>
	 *
	 * @param state the bank on which to operate
	 * @return as described
	 */
	static void determinePolygonRankings(PolygonProcessingState state, Collection<List<String>> speciesToCombine) {

		if (state.getNSpecies() == 0) {
			throw new IllegalArgumentException("Can not find primary species as there are no species");
		}

		float[] percentages = Arrays
				.copyOf(state.wallet.percentagesOfForestedLand, state.wallet.percentagesOfForestedLand.length);

		for (var speciesPair : speciesToCombine) {
			combinePercentages(state.wallet.speciesNames, speciesPair, percentages);
		}

		float highestPercentage = 0.0f;
		int highestPercentageIndex = -1;
		float secondHighestPercentage = 0.0f;
		int secondHighestPercentageIndex = -1;
		for (int i : state.getIndices()) {

			if (percentages[i] > highestPercentage) {

				secondHighestPercentageIndex = highestPercentageIndex;
				secondHighestPercentage = highestPercentage;
				highestPercentageIndex = i;
				highestPercentage = percentages[i];

			} else if (percentages[i] > secondHighestPercentage) {

				secondHighestPercentageIndex = i;
				secondHighestPercentage = percentages[i];
			}

			// TODO: implement NDEBUG22 = 1 logic
		}

		if (highestPercentageIndex == -1) {
			throw new IllegalStateException("There are no species with covering percentage > 0");
		}

		String primaryGenusName = state.wallet.speciesNames[highestPercentageIndex];
		Optional<String> secondaryGenusName = secondHighestPercentageIndex != -1
				? Optional.of(state.wallet.speciesNames[secondHighestPercentageIndex]) : Optional.empty();

		try {
			int inventoryTypeGroup = findInventoryTypeGroup(primaryGenusName, secondaryGenusName, highestPercentage);

			state.setSpeciesRankingDetails(
					new SpeciesRankingDetails(
							highestPercentageIndex,
							secondHighestPercentageIndex != -1 ? Optional.of(secondHighestPercentageIndex)
									: Optional.empty(),
							inventoryTypeGroup
					)
			);
		} catch (ProcessingException e) {
			// This should never fail because the bank has already been validated and hence the genera
			// are known to be valid.

			throw new IllegalStateException(e);
		}
	}

	/**
	 * <code>combinationGroup</code> is a list of precisely two species names. This method determines the indices within
	 * <code>speciesNames</code> that match the two given names. If two do, say i and j, the <code>percentages</code> is
	 * modified as follows. Assuming percentages[i] > percentages[j], percentages[i] is set to percentages[i] +
	 * percentages[j] and percentages[j] is set to 0.0. If fewer than two indices match, nothing is done.
	 *
	 * @param speciesNames     an array of (possibly null) distinct Strings.
	 * @param combinationGroup a pair of (not null) Strings.
	 * @param percentages      an array with one entry for each entry in <code>speciesName</code>.
	 */
	static void combinePercentages(String[] speciesNames, List<String> combinationGroup, float[] percentages) {

		if (combinationGroup.size() != 2) {
			throw new IllegalArgumentException(
					MessageFormat.format("combinationGroup must have size 2; it has size {0}", combinationGroup.size())
			);
		}

		if (combinationGroup.get(0) == null || combinationGroup.get(1) == null) {
			throw new IllegalArgumentException("combinationGroup must not contain null values");
		}

		if (speciesNames.length != percentages.length) {
			throw new IllegalArgumentException(
					MessageFormat.format(
							"the length of speciesNames ({}) must match that of percentages ({}) but it doesn't",
							speciesNames.length, percentages.length
					)
			);
		}

		Set<Integer> groupIndices = new HashSet<>();
		for (int i = 0; i < speciesNames.length; i++) {
			if (combinationGroup.contains(speciesNames[i]))
				groupIndices.add(i);
		}

		if (groupIndices.size() == 2) {
			Integer[] groupIndicesArray = new Integer[2];
			groupIndices.toArray(groupIndicesArray);

			int higherPercentageIndex;
			int lowerPercentageIndex;
			if (percentages[groupIndicesArray[0]] > percentages[groupIndicesArray[1]]) {
				higherPercentageIndex = groupIndicesArray[0];
				lowerPercentageIndex = groupIndicesArray[1];
			} else {
				higherPercentageIndex = groupIndicesArray[1];
				lowerPercentageIndex = groupIndicesArray[0];
			}
			percentages[higherPercentageIndex] = percentages[higherPercentageIndex] + percentages[lowerPercentageIndex];
			percentages[lowerPercentageIndex] = 0.0f;
		}
	}

	// ITGFIND
	/**
	 * Find Inventory type group (ITG) for the given primary and secondary (if given) genera.
	 *
	 * @param primaryGenus           the genus of the primary species
	 * @param optionalSecondaryGenus the genus of the primary species, which may be empty
	 * @param primaryPercentage      the percentage covered by the primary species
	 * @return as described
	 * @throws ProcessingException if primaryGenus is not a known genus
	 */
	static int findInventoryTypeGroup(
			String primaryGenus, Optional<String> optionalSecondaryGenus, float primaryPercentage
	) throws ProcessingException {

		if (primaryPercentage > 79.999 /* Copied from VDYP7 */) {

			Integer recordedInventoryTypeGroup = CommonData.ITG_PURE.get(primaryGenus);
			if (recordedInventoryTypeGroup == null) {
				throw new ProcessingException("Unrecognized primary species: " + primaryGenus);
			}

			return recordedInventoryTypeGroup;
		}

		String secondaryGenus = optionalSecondaryGenus.isPresent() ? optionalSecondaryGenus.get() : "";

		if (primaryGenus.equals(secondaryGenus)) {
			throw new IllegalArgumentException("The primary and secondary genera are the same");
		}

		switch (primaryGenus) {
		case "F":
			switch (secondaryGenus) {
			case "C", "Y":
				return 2;
			case "B", "H":
				return 3;
			case "S":
				return 4;
			case "PL", "PA":
				return 5;
			case "PY":
				return 6;
			case "L", "PW":
				return 7;
			default:
				return 8;
			}
		case "C", "Y":
			switch (secondaryGenus) {
			case "H", "B", "S":
				return 11;
			default:
				return 10;
			}
		case "H":
			switch (secondaryGenus) {
			case "C", "Y":
				return 14;
			case "B":
				return 15;
			case "S":
				return 16;
			default:
				return 13;
			}
		case "B":
			switch (secondaryGenus) {
			case "C", "Y", "H":
				return 19;
			default:
				return 20;
			}
		case "S":
			switch (secondaryGenus) {
			case "C", "Y", "H":
				return 23;
			case "B":
				return 24;
			case "PL":
				return 25;
			default:
				if (CommonData.HARDWOODS.contains(secondaryGenus)) {
					return 26;
				}
				return 22;
			}
		case "PW":
			return 27;
		case "PL", "PA":
			switch (secondaryGenus) {
			case "PL", "PA":
				return 28;
			case "F", "PW", "L", "PY":
				return 29;
			default:
				if (CommonData.HARDWOODS.contains(secondaryGenus)) {
					return 31;
				}
				return 30;
			}
		case "PY":
			return 32;
		case "L":
			switch (secondaryGenus) {
			case "F":
				return 33;
			default:
				return 34;
			}
		case "AC":
			if (CommonData.HARDWOODS.contains(secondaryGenus)) {
				return 36;
			}
			return 35;
		case "D":
			if (CommonData.HARDWOODS.contains(secondaryGenus)) {
				return 38;
			}
			return 37;
		case "MB":
			return 39;
		case "E":
			return 40;
		case "AT":
			if (CommonData.HARDWOODS.contains(secondaryGenus)) {
				return 42;
			}
			return 41;
		default:
			throw new ProcessingException("Unrecognized primary species: " + primaryGenus);
		}
	}
}
