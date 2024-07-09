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
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.bc.gov.nrs.vdyp.application.ProcessingException;
import ca.bc.gov.nrs.vdyp.application.StandProcessingException;
import ca.bc.gov.nrs.vdyp.common.ControlKey;
import ca.bc.gov.nrs.vdyp.common.Estimators;
import ca.bc.gov.nrs.vdyp.common.ReconcilationMethods;
import ca.bc.gov.nrs.vdyp.common.Utils;
import ca.bc.gov.nrs.vdyp.common_calculators.custom_exceptions.CommonCalculatorException;
import ca.bc.gov.nrs.vdyp.common_calculators.custom_exceptions.CurveErrorException;
import ca.bc.gov.nrs.vdyp.common_calculators.custom_exceptions.NoAnswerException;
import ca.bc.gov.nrs.vdyp.common_calculators.custom_exceptions.SpeciesErrorException;
import ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexAgeType;
import ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexEquation;
import ca.bc.gov.nrs.vdyp.forward.model.ControlVariable;
import ca.bc.gov.nrs.vdyp.forward.model.ForwardControlVariables;
import ca.bc.gov.nrs.vdyp.forward.model.ForwardDebugSettings;
import ca.bc.gov.nrs.vdyp.forward.model.VdypEntity;
import ca.bc.gov.nrs.vdyp.forward.model.VdypPolygon;
import ca.bc.gov.nrs.vdyp.io.parse.coe.UpperBoundsParser;
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
import ca.bc.gov.nrs.vdyp.model.SiteCurveAgeMaximum;
import ca.bc.gov.nrs.vdyp.model.SmallUtilizationClassVariable;
import ca.bc.gov.nrs.vdyp.model.UtilizationClass;
import ca.bc.gov.nrs.vdyp.model.VolumeVariable;
import ca.bc.gov.nrs.vdyp.si32.site.SiteTool;

public class ForwardProcessingEngine {

	private static final Logger logger = LoggerFactory.getLogger(ForwardProcessor.class);

	private static final int UC_ALL_INDEX = UtilizationClass.ALL.ordinal();
	private static final int UC_SMALL_INDEX = UtilizationClass.SMALL.ordinal();

	private static final float MIN_BASAL_AREA = 0.001f;

	/** π/4/10⁴ */
	public static final float PI_40K = (float) (Math.PI / 40_000);

	/* pp */ final ForwardProcessingState fps;
	/* pp */ final Estimators estimators;

	public ForwardProcessingEngine(Map<String, Object> controlMap) {

		this.fps = new ForwardProcessingState(controlMap);
		this.estimators = new Estimators(fps.fcm);
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
		GROW, //

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

		// Determine the target year of the growth
		int targetYear;

		int growTargetControlVariableValue = fps.fcm.getForwardControlVariables()
				.getControlVariable(ControlVariable.GROW_TARGET_1);
		if (growTargetControlVariableValue == -1) {
			if (polygon.getTargetYear().isEmpty()) {
				throw new ProcessingException(
						"Control Variable 1 has the value -1, indicating that the grow-to years are"
								+ " to be read from a grow-to-year file (at " + ControlKey.FORWARD_INPUT_GROWTO.name()
								+ " in the"
								+ " control file), but no such file was specified."
				);
			}
			targetYear = polygon.getTargetYear().get();
		} else {
			if (growTargetControlVariableValue <= 400) {
				targetYear = polygon.getDescription().getYear() + growTargetControlVariableValue;
			} else {
				targetYear = growTargetControlVariableValue;
			}
		}

		// Run the forward algorithm for this polygon

		executeForwardAlgorithm(lastStep, targetYear);
	}

	private void executeForwardAlgorithm(ExecutionStep lastStep, int untilYear) throws ProcessingException {

		PolygonProcessingState pps = fps.getPolygonProcessingState();
		Bank bank = fps.getBank(0, LayerType.PRIMARY);

		logger.info("Beginning processing of polygon {} layer {}", pps.getLayer().getParent(), pps.getLayer());

		// BANKCHK1, simplified for the parameters METH_CHK = 4, LayerI = 1, and INSTANCE = 1
		if (lastStep.ordinal() >= ExecutionStep.CHECK_FOR_WORK.ordinal()) {
			stopIfNoWork(pps);
		}

		// SCINXSET - note these are calculated directly from the Primary bank of instance 1
		if (lastStep.ordinal() >= ExecutionStep.CALCULATE_MISSING_SITE_CURVES.ordinal()) {
			calculateMissingSiteCurves(bank, fps.fcm.getSiteCurveMap(), fps.getPolygonProcessingState());
		}

		// VPRIME1, method == 1
		if (lastStep.ordinal() >= ExecutionStep.CALCULATE_COVERAGES.ordinal()) {
			calculateCoverages();
		}

		if (lastStep.ordinal() >= ExecutionStep.DETERMINE_POLYGON_RANKINGS.ordinal()) {
			determinePolygonRankings(CommonData.PRIMARY_SPECIES_TO_COMBINE);
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
			calculateDominantHeightAgeSiteIndex(pps, fps.fcm.getHl1Coefficients());
		}

		// CVSET1
		if (lastStep.ordinal() >= ExecutionStep.SET_COMPATIBILITY_VARIABLES.ordinal()) {
			setCompatibilityVariables(pps);
		}

		// VGROW1
		if (lastStep.ordinal() >= ExecutionStep.GROW.ordinal()) {
			int veteranLayerInstance = 0;

			int primaryLayerSourceInstance = 2;
			fps.storeActive(primaryLayerSourceInstance, LayerType.PRIMARY);

			int startingYear = fps.getPolygonProcessingState().getPolygon().getDescription().getYear();

			writeLayers(primaryLayerSourceInstance, veteranLayerInstance, false);

			boolean createNewGroups = fps.fcm.getDebugSettings()
					.getValue(ForwardDebugSettings.Vars.SPECIES_DYNAMICS_1) != 1
					&& fps.getPolygonProcessingState().getNSpecies() > 1;

			int primaryLayerTargetInstance = 2;
			int currentYear = startingYear;
			while (currentYear <= untilYear) {

				grow(primaryLayerSourceInstance, currentYear, primaryLayerTargetInstance, veteranLayerInstance);

				// Store polygon (both primary and veteran layers) to output
				writeLayers(primaryLayerTargetInstance, veteranLayerInstance, createNewGroups);

				currentYear += 1;

				int newPrimaryLayerSourceInstance = primaryLayerTargetInstance;
				primaryLayerTargetInstance = primaryLayerSourceInstance;
				primaryLayerSourceInstance = newPrimaryLayerSourceInstance;
			}
		}
	}

	private void grow(
			int primaryLayerSourceInstance, int currentYear, int primaryLayerTargetInstance, int veteranLayerInstance
	)
			throws ProcessingException {

		PolygonProcessingState pps = fps.getPolygonProcessingState();
		VdypPolygon polygon = pps.getPolygon();

		logger.info(
				"Performing grow of {} for year {} from instance {} to instance {}", polygon.getDescription()
						.getName(), currentYear, primaryLayerSourceInstance, primaryLayerTargetInstance
		);

		// Call to BANKOUT1 unnecessary; pps.wallet already contains the primary layer

		Bank primaryBank = fps.getBank(primaryLayerSourceInstance, LayerType.PRIMARY);
		Optional<Bank> veteranBank = Optional.ofNullable(fps.getBank(veteranLayerInstance, LayerType.VETERAN));

		// If update-during-growth is set, and this is not the starting year, update the
		// context
		int startingYear = fps.getPolygonProcessingState().getPolygon().getDescription().getYear();
		if (currentYear > startingYear
				&& fps.fcm.getForwardControlVariables()
						.getControlVariable(ControlVariable.UPDATE_DURING_GROWTH_6) >= 1) {
			// VPRIME1, method == 1
			calculateCoverages();

			// VHDOM1 METH_H = 2, METH_A = 2, METH_SI = 2
			calculateDominantHeightAgeSiteIndex(pps, fps.fcm.getHl1Coefficients());
		}

		float dominantHeight = pps.getPrimarySpeciesDominantHeight();
		int siteCurveNumber = pps.getSiteCurveNumber(pps.getPrimarySpeciesIndex());
		float siteIndex = pps.getPrimarySpeciesSiteIndex();
		float yearsToBreastHeight = pps.getPrimarySpeciesAgeToBreastHeight();
		float yearsAtBreastHeight = pps.getPrimarySpeciesAgeAtBreastHeight();

		// Calculate change in dominant height

		float growthInDominantHeight = growDominantHeight(
				dominantHeight, siteCurveNumber, siteIndex, yearsToBreastHeight
		);

		// Calculate change in basal area

		final Optional<Float> veteranLayerBasalArea;
		if (veteranBank.isPresent())
			veteranLayerBasalArea = Optional.of(veteranBank.get().basalAreas[0][UC_ALL_INDEX]);
		else {
			veteranLayerBasalArea = Optional.empty();
		}

		float[] speciesProportionByBasalArea = new float[pps.getNSpecies() + 1];
		for (int i = 1; i <= pps.getNSpecies(); i++) {
			speciesProportionByBasalArea[i] = pps.wallet.basalAreas[i][UC_ALL_INDEX]
					/ pps.wallet.basalAreas[0][UC_ALL_INDEX];
		}

		float growthInBasalArea = growBasalArea(
				speciesProportionByBasalArea, yearsAtBreastHeight, dominantHeight, primaryBank.basalAreas[0][UC_ALL_INDEX], veteranLayerBasalArea, growthInDominantHeight
		);
	}

	/**
	 * EMP111A - Basal area growth for the primary layer.
	 * @param speciesProportionByBasalArea the proportion by basal area of each of the polygon's species
	 * @param yearsAtBreastHeight at the start of the year
	 * @param dominantHeight primary species dominant height at start of year
	 * @param primaryLayerBasalArea at the start of the year
	 * @param veteranLayerBasalArea at the start of the year
	 * @param growthInDominantHeight during the year
	 * 
	 * @return the growth in the basal area for the year
	 * @throws StandProcessingException 
	 */
	private float growBasalArea(
			float[] speciesProportionByBasalArea, float yearsAtBreastHeight, float dominantHeight, float primaryLayerBasalArea, 
			Optional<Float> veteranLayerBasalArea, float growthInDominantHeight
	) throws StandProcessingException {
		
		// UPPERGEN( 1, BATOP98, DQTOP98)
		var baUpperBound = growBasalAreaUpperBound();
		var dqUpperBound = growQuadraticMeanDiameterUpperBound();

		baUpperBound = baUpperBound / Estimators.EMPIRICAL_OCCUPANCY;
		var baLimit = Math.max(baUpperBound, primaryLayerBasalArea);
		
		var baYield = fps.fcm
		
		boolean isFullOccupancy = true;
		int primarySpeciesGroupNumber = fps.getPolygonProcessingState().getPrimarySpeciesGroupNumber();
		float basalAreaYield = estimators.estimateBaseAreaYield(null, dominantHeight, yearsAtBreastHeight, veteranLayerBasalArea, 
				isFullOccupancy, fps.getPolygonProcessingState().getBecZone(), primarySpeciesGroupNumber);

		return basalAreaYield;
	}

	/**
	 * UPPERGEN(1, BATOP98, DQTOP98) for basal area
	 */
	private float growBasalAreaUpperBound() {
		var primarySpeciesGroupNumber = fps.getPolygonProcessingState().getPrimarySpeciesGroupNumber();
		return fps.fcm.getUpperBounds().get(primarySpeciesGroupNumber).getCoe(UpperBoundsParser.BA_INDEX);
	}

	/**
	 * UPPERGEN(1, BATOP98, DQTOP98) for quad-mean-diameter
	 */
	private float growQuadraticMeanDiameterUpperBound() {
		var primarySpeciesGroupNumber = fps.getPolygonProcessingState().getPrimarySpeciesGroupNumber();
		return fps.fcm.getUpperBounds().get(primarySpeciesGroupNumber).getCoe(UpperBoundsParser.DQ_INDEX);
	}

	public float growDominantHeight(
					float dominantHeight, int siteCurveNumber, float siteIndex,
					float yearsToBreastHeight
			) throws ProcessingException {

		SiteCurveAgeMaximum scAgeMaximums = fps.fcm.getMaximumAgeBySiteCurveNumber().get(siteCurveNumber);
		Region region = fps.getPolygonProcessingState().wallet.getBecZone().getRegion();

		if (siteCurveNumber == VdypEntity.MISSING_INTEGER_VALUE) {
			throw new ProcessingException("No SiteCurveNumber supplied");
		}

		var siteIndexEquation = SiteIndexEquation.getByIndex(siteCurveNumber);

		if (dominantHeight <= 1.3) {
			throw new ProcessingException(
					MessageFormat.format(
							"(current) DominantHeight {0} is out of range (must be above 1.3)", dominantHeight
					)
			);
		}

		final SiteIndexAgeType ageType = SiteIndexAgeType.SI_AT_BREAST;

		double siteIndex_d = siteIndex;
		double dominantHeight_d = dominantHeight;
		double yearsToBreastHeight_d = yearsToBreastHeight;

		double currentAge;
		try {
			currentAge = SiteTool.heightAndSiteIndexToAge(
					siteIndexEquation, dominantHeight_d, ageType, siteIndex_d, yearsToBreastHeight_d
			);
		} catch (CommonCalculatorException e) {
			throw new ProcessingException(
					MessageFormat.format(
							"Encountered exception when calling heightAndSiteIndexToAge({0}, {1}, {2}, {3}, {4})", siteIndexEquation, dominantHeight_d, ageType, siteIndex_d, yearsToBreastHeight_d
					), e
			);
		}

		if (currentAge <= 0.0d) {
			if (dominantHeight_d > siteIndex_d) {
				return 0.0f /* no growth */;
			} else {
				throw new ProcessingException(
						MessageFormat.format("currentBreastHeightAge value {0} must be positive", currentAge)
				);
			}
		}

		double nextAge = currentAge + 1.0f;

		// If we are past the total age limit for site curve, assign no growth. If we are almost there, go 
		// slightly past the limit (by .01 yr). Once there, we should stop growing. The TOTAL age limit was 
		// stored so we must calculate a BH age limit first...

		float ageLimitInYears = scAgeMaximums.getAgeMaximum(region);

		float breastHeightAgeLimitInYears = 0.0f;
		if (ageLimitInYears > 0) {
			breastHeightAgeLimitInYears = ageLimitInYears - yearsToBreastHeight;
		}

		if (currentAge <= breastHeightAgeLimitInYears || scAgeMaximums.getT1() <= 0.0f) {

			float yearPart = 1.0f;

			if (scAgeMaximums.getT1() <= 0.0f) {

				if (breastHeightAgeLimitInYears > 0.0f && nextAge > breastHeightAgeLimitInYears) {
					if (currentAge > breastHeightAgeLimitInYears) {
						return 0.0f /* no growth */;
					}

					yearPart = (float) (breastHeightAgeLimitInYears - currentAge + 0.01);
					nextAge = currentAge + yearPart;
				}
			}

			// The above code to find ages allows errors up to .005 m. At high ages with some 
			// species this can correspond to a half year. Therefore, AGED1 can not be assumed to 
			// correspond to HDD1. Find a new HDD1 to at least get the increment correct.

			double currentDominantHeight = ageAndSiteIndexToHeight(
					siteIndexEquation, currentAge, ageType, siteIndex_d, yearsToBreastHeight_d
			);

			double nextDominantHeight = ageAndSiteIndexToHeight(
					siteIndexEquation, nextAge, ageType, siteIndex_d, yearsToBreastHeight_d, r -> r >= 0.0
			);

			if (nextDominantHeight < currentDominantHeight && yearPart == 1.0) {
				// Rounding error in site routines?
				if (Math.abs(currentDominantHeight - nextDominantHeight) < 0.01) {
					return 0.0f /* no growth */;
				} else {
					throw new ProcessingException(
							MessageFormat.format(
									"New dominant height {0} is less than the current dominant height {1}", nextDominantHeight, currentDominantHeight
							)
					);
				}
			}

			return (float) (nextDominantHeight - currentDominantHeight);

		} else {
			// We are in a special extension of the curve. Derive the new curve form and then 
			// compute the answer.

			double breastHeightAgeLimitInYears_d = breastHeightAgeLimitInYears;

			double currentDominantHeight = ageAndSiteIndexToHeight(
					siteIndexEquation, breastHeightAgeLimitInYears_d, ageType, siteIndex_d, yearsToBreastHeight_d
			);

			breastHeightAgeLimitInYears_d += 1.0;

			double nextDominantHeight = ageAndSiteIndexToHeight(
					siteIndexEquation, breastHeightAgeLimitInYears_d, ageType, siteIndex_d, yearsToBreastHeight_d
			);

			float rate = (float) (nextDominantHeight - currentDominantHeight);
			if (rate < 0.0005f) {
				rate = 0.0005f;
			}

			float a = FloatMath.log(0.5f) / scAgeMaximums.getT1();
			float y = (float) currentDominantHeight;
			// Model is:
			//     Y = y - rate/a * (1 - exp(a * t)) where t = AGE - BHAGELIM
			// Solve for t:
			//	   1 - exp(a * t) = (y - dominantHeight) * a/rate
			//	   -exp(a * t) = (y - dominantHeight) * a/rate - 1
			//	   exp(a * t) = (dominantHeight - y) * a/rate + 1
			//	   a * t = ln(1 + (dominantHeight - y) * a/rate)
			//	   t = ln(1 + (dominantHeight - y) * a/rate) / a
			float t;
			if (dominantHeight > y) {
				float term = 1.0f + (dominantHeight - y) * a / rate;
				if (term <= 1.0e-7) {
					return 0.0f;
				}
				t = FloatMath.log(term) / a;
			} else {
				t = 0.0f;
			}

			if (t > scAgeMaximums.getT2()) {
				return 0.0f;
			} else {
				return rate / a * (-FloatMath.exp(a * t) + FloatMath.exp(a * (t + 1.0f)));
			}
		}
	}

	private static double ageAndSiteIndexToHeight(
			SiteIndexEquation curve, double age, SiteIndexAgeType ageType, double siteIndex, double years2BreastHeight,
			Function<Double, Boolean> checkResultValidity
	) throws ProcessingException {
		Double r = ageAndSiteIndexToHeight(curve, age, ageType, siteIndex, years2BreastHeight);
		if (!checkResultValidity.apply(r)) {
			throw new ProcessingException(
					MessageFormat.format(
							"SiteTool.ageAndSiteIndexToHeight({0}, {1}, {2}, {3}, {4}) returned {5}", curve, age, ageType, siteIndex, years2BreastHeight, r
					)
			);
		}

		return r;
	}

	private static double ageAndSiteIndexToHeight(
			SiteIndexEquation curve, double age, SiteIndexAgeType ageType, double siteIndex, double years2BreastHeight
	) throws ProcessingException {
		try {
			return SiteTool.ageAndSiteIndexToHeight(
					curve, age, ageType, siteIndex, years2BreastHeight
			);
		} catch (CommonCalculatorException e) {
			throw new ProcessingException(
					MessageFormat.format(
							"SiteTool.ageAndSiteIndexToHeight({0}, {1}, {2}, {3}, {4}) threw exception", curve, age, ageType, siteIndex, years2BreastHeight
					), e
			);
		}
	}

	private void writeLayers(int primaryLayerInstance, int veteranLayerInstance, boolean b) {

		logger.info(
				"Writing primary layer from instance {} and veteran layer from instance {}", primaryLayerInstance, veteranLayerInstance
		);
	}

	private static final float[] DEFAULT_QUAD_MEAN_DIAMETERS = new float[] { Float.NaN, 10.0f, 15.0f, 20.0f, 25.0f };
	private static final float V_BASE_MIN = 0.1f;
	private static final float B_BASE_MIN = 0.01f;

	@SuppressWarnings("unchecked")
	void setCompatibilityVariables(PolygonProcessingState pps) throws ProcessingException {
		Coefficients aAdjust = new Coefficients(new float[] { 0.0f, 0.0f, 0.0f, 0.0f }, 1);

		var growthDetails = pps.getFps().fcm.getForwardControlVariables();

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
					estimators.estimateNetDecayAndWasteVolume(
							pps.getBecZone()
									.getRegion(), uc, aAdjust, pps.wallet.speciesNames[s], spLoreyHeight_All, quadMeanDiameters, closeUtilizationVolumes, closeUtilizationVolumesNetOfDecay, closeUtilizationVolumesNetOfDecayAndWaste
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
					estimators.estimateNetDecayVolume(
							pps.wallet.speciesNames[s], pps.getBecZone().getRegion(), uc, aAdjust, decayGroup, pps
									.getPrimarySpeciesAgeAtBreastHeight(), quadMeanDiameters, closeUtilizationVolumes, closeUtilizationVolumesNetOfDecay
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
					estimators.estimateCloseUtilizationVolume(
							uc, aAdjust, volumeGroup, spLoreyHeight_All, quadMeanDiameters, wholeStemVolumes, closeUtilizationVolumes
					);

					float actualVolume = pps.wallet.closeUtilizationVolumes[s][uc.ordinal()];
					float staticVolume = closeUtilizationVolumes.getCoe(uc.index);
					adjustment = calculateCompatibilityVariable(actualVolume, baseVolume, staticVolume);
				}

				cvVolume[s].put(uc, VolumeVariable.CLOSE_UTIL_VOL, LayerType.PRIMARY, adjustment);
			}

			int primarySpeciesVolumeGroup = pps.volumeEquationGroups[s];
			float primarySpeciesQMDAll = pps.wallet.quadMeanDiameters[s][UC_ALL_INDEX];
			var wholeStemVolume = pps.wallet.treesPerHectare[s][UC_ALL_INDEX]
					* estimators.estimateWholeStemVolumePerTree(
							primarySpeciesVolumeGroup, spLoreyHeight_All, primarySpeciesQMDAll
					);

			wholeStemVolumes.setCoe(UC_ALL_INDEX, wholeStemVolume);

			estimators.estimateWholeStemVolume(
					UtilizationClass.ALL, 0.0f, primarySpeciesVolumeGroup, spLoreyHeight_All, quadMeanDiameters, basalAreas, wholeStemVolumes
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

			estimators.estimateQuadMeanDiameterByUtilization(
					pps.getBecZone(), quadMeanDiameters, genusName
			);

			estimators.estimateBaseAreaByUtilization(
					pps.getBecZone(), quadMeanDiameters, basalAreas, genusName
			);

			// Calculate trees-per-hectare per utilization
			treesPerHectare.setCoe(UtilizationClass.ALL.index, pps.wallet.treesPerHectare[s][UC_ALL_INDEX]);
			for (UtilizationClass uc : UtilizationClass.UTIL_CLASSES) {
				treesPerHectare.setCoe(
						uc.index, calculateTreesPerHectare(
								basalAreas.getCoe(uc.index), quadMeanDiameters.getCoe(uc.index)
						)
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

			cvSmall[s] = estimateSmallComponents(s, growthDetails);
		}

		pps.setCompatibilityVariableDetails(cvVolume, cvBasalArea, cvQuadraticMeanDiameter, cvSmall);
	}

	/**
	 * Estimate small component utilization values for primary layer
	 * @param allowCompatibilitySetting
	 *
	 * @throws ProcessingException
	 */
	private HashMap<SmallUtilizationClassVariable, Float>
			estimateSmallComponents(int speciesIndex, ForwardControlVariables growthDetails)
					throws ProcessingException {

		PolygonProcessingState pps = fps.getPolygonProcessingState();
		Bank wallet = pps.wallet;
		
		Region region = pps.getPolygon().getBiogeoclimaticZone().getRegion();
		String speciesName = wallet.speciesNames[speciesIndex];

		float spLoreyHeight_All = wallet.loreyHeights[speciesIndex][UC_ALL_INDEX]; // HLsp
		float spQuadMeanDiameter_All = wallet.quadMeanDiameters[speciesIndex][UC_ALL_INDEX]; // DQsp

		// this WHOLE operation on Actual BA's, not 100% occupancy.
		// TODO: verify this: float fractionAvailable = polygon.getPercentForestLand();
		float spBaseArea_All = wallet.basalAreas[speciesIndex][UC_ALL_INDEX] /* * fractionAvailable */; // BAsp

		// EMP080
		float cvSmallComponentProbability = smallComponentProbability(speciesName, spLoreyHeight_All, region); // PROBsp

		// EMP081
		float conditionalExpectedBaseArea = conditionalExpectedBaseArea(
				speciesName, spBaseArea_All, spLoreyHeight_All, region
		); // BACONDsp

		// TODO (see previous TODO): conditionalExpectedBaseArea /= fractionAvailable;

		float cvBasalArea_Small = cvSmallComponentProbability * conditionalExpectedBaseArea;

		// EMP082
		float cvQuadMeanDiameter_Small = smallComponentQuadMeanDiameter(speciesName, spLoreyHeight_All); // DQSMsp

		// EMP085
		float cvLoreyHeight_Small = smallComponentLoreyHeight(
				speciesName, spLoreyHeight_All, cvQuadMeanDiameter_Small, spQuadMeanDiameter_All
		); // HLSMsp

		// EMP086
		float cvMeanVolume_Small = meanVolumeSmall(speciesName, cvQuadMeanDiameter_Small, cvLoreyHeight_Small); // VMEANSMs

		var cvSmall = new HashMap<SmallUtilizationClassVariable, Float>();

		float spInputBasalArea_Small = wallet.basalAreas[speciesIndex][UC_SMALL_INDEX];
		cvSmall.put(SmallUtilizationClassVariable.BASAL_AREA, spInputBasalArea_Small - cvBasalArea_Small);

		if (growthDetails.allowCalculation(spInputBasalArea_Small, B_BASE_MIN, (l, r) -> l > r)) {
			float spInputQuadMeanDiameter_Small = wallet.quadMeanDiameters[speciesIndex][UC_SMALL_INDEX];
			cvSmall.put(
					SmallUtilizationClassVariable.QUAD_MEAN_DIAMETER, spInputQuadMeanDiameter_Small
							- cvQuadMeanDiameter_Small
			);
		} else {
			cvSmall.put(SmallUtilizationClassVariable.QUAD_MEAN_DIAMETER, 0.0f);
		}

		float spInputLoreyHeight_Small = wallet.loreyHeights[speciesIndex][UC_SMALL_INDEX];
		if (spInputLoreyHeight_Small > 1.3f && cvLoreyHeight_Small > 1.3f && spInputBasalArea_Small > 0.0f) {
			float cvLoreyHeight = FloatMath.log( (spInputLoreyHeight_Small - 1.3f) / (cvLoreyHeight_Small - 1.3f));
			cvSmall.put(SmallUtilizationClassVariable.LOREY_HEIGHT, cvLoreyHeight);
		} else {
			cvSmall.put(SmallUtilizationClassVariable.LOREY_HEIGHT, 0.0f);
		}

		float spInputWholeStemVolume_Small = wallet.wholeStemVolumes[speciesIndex][UC_SMALL_INDEX];
		if (spInputWholeStemVolume_Small > 0.0f && cvMeanVolume_Small > 0.0f
				&& growthDetails.allowCalculation(spInputBasalArea_Small, B_BASE_MIN, (l, r) -> l >= r)) {

			float spInputTreePerHectare_Small = wallet.treesPerHectare[speciesIndex][UC_SMALL_INDEX];

			var cvWholeStemVolume = FloatMath
					.log(spInputWholeStemVolume_Small / spInputTreePerHectare_Small / cvMeanVolume_Small);
			cvSmall.put(SmallUtilizationClassVariable.WHOLE_STEM_VOLUME, cvWholeStemVolume);

		} else {
			cvSmall.put(SmallUtilizationClassVariable.WHOLE_STEM_VOLUME, 0.0f);
		}

		return cvSmall;
	}

	// EMP080
	private float smallComponentProbability(
			String speciesName, float loreyHeight, Region region
	) {
		PolygonProcessingState pps = fps.getPolygonProcessingState();

		Coefficients coe = fps.fcm.getSmallComponentProbabilityCoefficients().get(speciesName);

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
	private float conditionalExpectedBaseArea(
			String speciesName, float basalArea, float loreyHeight, Region region
	) {
		Coefficients coe = fps.fcm.getSmallComponentBasalAreaCoefficients().get(speciesName);

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
	private float smallComponentQuadMeanDiameter(String speciesName, float loreyHeight) {
		Coefficients coe = fps.fcm.getSmallComponentQuadMeanDiameterCoefficients().get(speciesName);

		// EQN 5 in IPSJF118.doc

		float a0 = coe.getCoe(1);
		float a1 = coe.getCoe(2);

		float logit = a0 + a1 * loreyHeight;

		return 4.0f + 3.5f * exp(logit) / (1.0f + exp(logit));
	}

	// EMP085
	private float smallComponentLoreyHeight(
			String speciesName, float speciesLoreyHeight_All, float quadMeanDiameterSpecSmall,
			float speciesQuadMeanDiameter_All
	) {
		Coefficients coe = fps.fcm.getSmallComponentLoreyHeightCoefficients().get(speciesName);

		// EQN 1 in IPSJF119.doc

		float a0 = coe.getCoe(1);
		float a1 = coe.getCoe(2);

		return 1.3f + (speciesLoreyHeight_All - 1.3f) //
				* exp(a0 * (pow(quadMeanDiameterSpecSmall, a1) - pow(speciesQuadMeanDiameter_All, a1)));
	}

	// EMP086
	private float meanVolumeSmall(
			String speciesName, float quadMeanDiameterSpecSmall, float loreyHeightSpecSmall
	) {
		Coefficients coe = fps.fcm.getSmallComponentWholeStemVolumeCoefficients().get(speciesName);

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

	/**
	 * VHDOM1 METH_H = 2, METH_A = 2, METH_SI = 2
	 * 
	 * @param state
	 * @param hl1Coefficients
	 * @throws ProcessingException
	 */
	static void calculateDominantHeightAgeSiteIndex(
			PolygonProcessingState state, MatrixMap2<String, Region, Coefficients> hl1Coefficients
	) throws ProcessingException {

		// Calculate primary species values
		int primarySpeciesIndex = state.getPrimarySpeciesIndex();

		// (1) Dominant Height
		float primarySpeciesDominantHeight = state.wallet.dominantHeights[primarySpeciesIndex];
		if (Float.isNaN(primarySpeciesDominantHeight)) {
			float loreyHeight = state.wallet.loreyHeights[primarySpeciesIndex][UC_ALL_INDEX];
			if (Float.isNaN(loreyHeight)) {
				throw new ProcessingException(
						MessageFormat.format(
								"Neither dominant nor lorey height[All] is available for primary species {}", state.wallet.speciesNames[primarySpeciesIndex]
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

			float treesPerHectare = state.wallet.treesPerHectare[primarySpeciesIndex][UC_ALL_INDEX];
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
			double newSI = SiteTool.convertSiteIndexBetweenCurves(siteCurve1, primarySpeciesSiteIndex, siteCurve2);
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
										"there is no conversion from curves {0} to {1}. Skipping species {3}", siteCurveI, primarySiteCurve, i
								)
						);
					} catch (CurveErrorException | SpeciesErrorException e) {
						throw new ProcessingException(
								MessageFormat.format(
										"convertSiteIndexBetweenCurves on {0}, {1} and {2} failed", siteCurveI, siteIndexI, primarySiteCurve
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
										"there is no conversion between curves {0} and {1}. Skipping species {2}", primarySiteCurve, siteCurveI, i
								)
						);
					} catch (CurveErrorException | SpeciesErrorException e) {
						throw new ProcessingException(
								MessageFormat.format(
										"convertSiteIndexBetweenCurves on {0}, {1} and {2} failed. Skipping species {3}", primarySiteCurve, primarySpeciesSiteIndex, siteCurveI, i
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
	void calculateCoverages() {

		PolygonProcessingState pps = this.fps.getPolygonProcessingState();

		logger.atDebug().addArgument(pps.getNSpecies()).addArgument(pps.wallet.basalAreas[0][0]).log(
				"Calculating coverages as a ratio of Species BA over Total BA. # species: {}; Layer total 7.5cm+ basal area: {}"
		);

		for (int i : pps.getIndices()) {
			pps.wallet.percentagesOfForestedLand[i] = pps.wallet.basalAreas[i][UC_ALL_INDEX]
					/ pps.wallet.basalAreas[0][UC_ALL_INDEX] * 100.0f;

			logger.atDebug().addArgument(i).addArgument(pps.wallet.speciesIndices[i])
					.addArgument(pps.wallet.speciesNames[i]).addArgument(pps.wallet.basalAreas[i][0])
					.addArgument(pps.wallet.percentagesOfForestedLand[i])
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
							"Polygon {0}''s year value {1} is < 1900", polygon.getDescription().getName(), polygon
									.getDescription().getYear()
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
							"Polygon {0} layer 0 has no species with basal area above {1}", state.getLayer().getParent()
									.getDescription().getName(), MIN_BASAL_AREA
					)
			);
		}
	}

	/** Default Equation Group, by species. Indexed by the species number, a one-based value. */
	private static final int[] defaultEquationGroups = { 0 /* placeholder */, 1, 2, 3, 4, 1, 2, 5, 6, 7, 1, 9, 8, 9, 9,
			10, 4 };
	private static final Set<Integer> exceptedSpeciesIndicies = new HashSet<>(List.of(3, 4, 5, 6, 10));

	// PRIMFIND
	/**
	 * Returns a {@code SpeciesRankingDetails} instance giving:
	 * <ul>
	 * <li>the index in {@code bank} of the primary species
	 * <li>the index in {@code bank} of the secondary species, or Optional.empty() if none, and
	 * <li>the percentage of forested land occupied by the primary species
	 * </ul>
	 *
	 * @param pps the bank on which to operate
	 * @return as described
	 */
	void determinePolygonRankings(Collection<List<String>> speciesToCombine) {

		PolygonProcessingState pps = fps.getPolygonProcessingState();

		if (pps.getNSpecies() == 0) {
			throw new IllegalArgumentException("Can not find primary species as there are no species");
		}

		float[] percentages = Arrays
				.copyOf(pps.wallet.percentagesOfForestedLand, pps.wallet.percentagesOfForestedLand.length);

		for (var speciesPair : speciesToCombine) {
			combinePercentages(pps.wallet.speciesNames, speciesPair, percentages);
		}

		float highestPercentage = 0.0f;
		int highestPercentageIndex = -1;
		float secondHighestPercentage = 0.0f;
		int secondHighestPercentageIndex = -1;
		for (int i : pps.getIndices()) {

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

		String primaryGenusName = pps.wallet.speciesNames[highestPercentageIndex];
		Optional<String> secondaryGenusName = secondHighestPercentageIndex != -1
				? Optional.of(pps.wallet.speciesNames[secondHighestPercentageIndex])
				: Optional.empty();

		try {
			int inventoryTypeGroup = findInventoryTypeGroup(primaryGenusName, secondaryGenusName, highestPercentage);

			int basalAreaGroup1 = 0;

			String primarySpeciesName = pps.wallet.speciesNames[highestPercentageIndex];
			String becZoneAlias = pps.wallet.getBecZone().getAlias();

			int defaultEquationGroup = fps.fcm.getDefaultEquationGroup().get(primarySpeciesName, becZoneAlias);
			Optional<Integer> equationModifierGroup = fps.fcm.getEquationModifierGroup()
					.get(defaultEquationGroup, inventoryTypeGroup);
			if (equationModifierGroup.isPresent()) {
				basalAreaGroup1 = equationModifierGroup.get();
			} else {
				basalAreaGroup1 = defaultEquationGroup;
			}

			int primarySpeciesIndex = pps.wallet.speciesIndices[highestPercentageIndex];
			int basalAreaGroup3 = defaultEquationGroups[primarySpeciesIndex];
			if (Region.INTERIOR.equals(pps.wallet.getBecZone().getRegion())
					&& exceptedSpeciesIndicies.contains(primarySpeciesIndex)) {
				basalAreaGroup3 += 20;
			}

			pps.setSpeciesRankingDetails(
					new SpeciesRankingDetails(
							highestPercentageIndex,
							secondHighestPercentageIndex != -1 ? Optional.of(secondHighestPercentageIndex)
									: Optional.empty(),
							inventoryTypeGroup,
							basalAreaGroup1,
							basalAreaGroup3
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
							"the length of speciesNames ({}) must match that of percentages ({}) but it doesn't", speciesNames.length, percentages.length
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
