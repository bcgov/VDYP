package ca.bc.gov.nrs.vdyp.common;

import static ca.bc.gov.nrs.vdyp.math.FloatMath.clamp;
import static ca.bc.gov.nrs.vdyp.math.FloatMath.exp;
import static ca.bc.gov.nrs.vdyp.math.FloatMath.log;
import static ca.bc.gov.nrs.vdyp.math.FloatMath.pow;
import static ca.bc.gov.nrs.vdyp.math.FloatMath.ratio;
import static java.lang.Math.max;
import static java.lang.Math.min;

import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.bc.gov.nrs.vdyp.application.ProcessingException;
import ca.bc.gov.nrs.vdyp.io.parse.coe.ModifierParser;
import ca.bc.gov.nrs.vdyp.model.BecDefinition;
import ca.bc.gov.nrs.vdyp.model.Coefficients;
import ca.bc.gov.nrs.vdyp.model.MatrixMap2;
import ca.bc.gov.nrs.vdyp.model.MatrixMap3;
import ca.bc.gov.nrs.vdyp.model.Region;
import ca.bc.gov.nrs.vdyp.model.UtilizationClass;

public class EstimationMethods {

	public static final Logger log = LoggerFactory.getLogger(EstimationMethods.class);

	private static final int UTIL_ALL = UtilizationClass.ALL.index;
	private static final int UTIL_LARGEST = UtilizationClass.OVER225.index;

	private EstimationMethods() {
	}

	/**
	 * EMP092. Updates closeUtilizationVolumeUtil with estimated values.
	 *
	 * @param controlMap
	 * @param utilizationClass
	 * @param aAdjust
	 * @param volumeGroup
	 * @param hlSp
	 * @param quadMeanDiameterUtil
	 * @param wholeStemVolumeUtil
	 * @param closeUtilizationVolumeUtil
	 * @throws ProcessingException
	 */
	public static void estimateCloseUtilizationVolume(
			Map<String, Object> controlMap, UtilizationClass utilizationClass, Coefficients aAdjust, int volumeGroup,
			float hlSp, Coefficients quadMeanDiameterUtil, Coefficients wholeStemVolumeUtil,
			Coefficients closeUtilizationVolumeUtil
	) throws ProcessingException {
		final var closeUtilizationCoeMap = Utils
				.<MatrixMap2<Integer, Integer, Optional<Coefficients>>>expectParsedControl(
						controlMap, ControlKey.CLOSE_UTIL_VOLUME, MatrixMap2.class
				);
		estimateCloseUtilizationVolume(
				utilizationClass, aAdjust, volumeGroup, hlSp, closeUtilizationCoeMap, quadMeanDiameterUtil,
				wholeStemVolumeUtil, closeUtilizationVolumeUtil
		);
	}

	/**
	 * EMP092. Updates closeUtilizationVolumeUtil with estimated values.
	 *
	 * @param utilizationClass
	 * @param aAdjust
	 * @param volumeGroup
	 * @param hlSp
	 * @param closeUtilizationCoeMap
	 * @param quadMeanDiameterUtil
	 * @param wholeStemVolumeUtil
	 * @param closeUtilizationVolumeUtil
	 * @throws ProcessingException
	 */
	public static void estimateCloseUtilizationVolume(
			UtilizationClass utilizationClass, Coefficients aAdjust, int volumeGroup, float hlSp,
			MatrixMap2<Integer, Integer, Optional<Coefficients>> closeUtilizationCoeMap,
			Coefficients quadMeanDiameterUtil, Coefficients wholeStemVolumeUtil, Coefficients closeUtilizationVolumeUtil
	) throws ProcessingException {
		estimateUtilization(wholeStemVolumeUtil, closeUtilizationVolumeUtil, utilizationClass, (uc, ws) -> {
			Coefficients closeUtilCoe = closeUtilizationCoeMap.get(uc.index, volumeGroup).orElseThrow(
					() -> new ProcessingException(
							"Could not find whole stem utilization coefficients for group " + volumeGroup
					)
			);
			var a0 = closeUtilCoe.getCoe(1);
			var a1 = closeUtilCoe.getCoe(2);
			var a2 = closeUtilCoe.getCoe(3);

			var arg = a0 + a1 * quadMeanDiameterUtil.getCoe(uc.index) + a2 * hlSp + aAdjust.getCoe(uc.index);

			float ratio = ratio(arg, 7.0f);

			return ws * ratio;
		});

		if (utilizationClass == UtilizationClass.ALL) {
			storeSumUtilizationComponents(closeUtilizationVolumeUtil);
		}
	}

	/**
	 * EMP094. Estimate utilization net of decay and waste
	 *
	 * @param controlMap
	 * @param region
	 * @param utilizationClass
	 * @param aAdjust
	 * @param genus
	 * @param loreyHeight
	 * @param quadMeanDiameterUtil
	 * @param closeUtilizationUtil
	 * @param closeUtilizationNetOfDecayUtil
	 * @param closeUtilizationNetOfDecayAndWasteUtil
	 * @throws ProcessingException
	 */
	public static void estimateNetDecayAndWasteVolume(
			Map<String, Object> controlMap, Region region, UtilizationClass utilizationClass, Coefficients aAdjust,
			String genus, float loreyHeight, Coefficients quadMeanDiameterUtil, Coefficients closeUtilizationUtil,
			Coefficients closeUtilizationNetOfDecayUtil, Coefficients closeUtilizationNetOfDecayAndWasteUtil
	) throws ProcessingException {
		final var netDecayWasteCoeMap = Utils.<Map<String, Coefficients>>expectParsedControl(
				controlMap, ControlKey.VOLUME_NET_DECAY_WASTE, Map.class
		);
		final var wasteModifierMap = Utils.<MatrixMap2<String, Region, Float>>expectParsedControl(
				controlMap, ControlKey.WASTE_MODIFIERS, MatrixMap2.class
		);

		estimateNetDecayAndWasteVolume(
				region, utilizationClass, aAdjust, genus, loreyHeight, netDecayWasteCoeMap, wasteModifierMap,
				quadMeanDiameterUtil, closeUtilizationUtil, closeUtilizationNetOfDecayUtil,
				closeUtilizationNetOfDecayAndWasteUtil
		);
	}

	/**
	 * EMP094. Estimate utilization net of decay and waste
	 *
	 * @param region
	 * @param utilizationClass
	 * @param aAdjust
	 * @param genus
	 * @param loreyHeight
	 * @param netDecayWasteCoeMap
	 * @param wasteModifierMap
	 * @param quadMeanDiameterUtil
	 * @param closeUtilizationUtil
	 * @param closeUtilizationNetOfDecayUtil
	 * @param closeUtilizationNetOfDecayAndWasteUtil
	 * @throws ProcessingException
	 */
	public static void estimateNetDecayAndWasteVolume(
			Region region, UtilizationClass utilizationClass, Coefficients aAdjust, String genus, float loreyHeight,
			Map<String, Coefficients> netDecayWasteCoeMap, MatrixMap2<String, Region, Float> wasteModifierMap,
			Coefficients quadMeanDiameterUtil, Coefficients closeUtilizationUtil,
			Coefficients closeUtilizationNetOfDecayUtil, Coefficients closeUtilizationNetOfDecayAndWasteUtil
	) throws ProcessingException {
		estimateUtilization(
				closeUtilizationNetOfDecayUtil, closeUtilizationNetOfDecayAndWasteUtil, utilizationClass,
				(i, netDecay) -> {
					if (Float.isNaN(netDecay) || netDecay <= 0f) {
						return 0f;
					}

					Coefficients netWasteCoe = netDecayWasteCoeMap.get(genus);
					if (netWasteCoe == null) {
						throw new ProcessingException("Could not find net waste coefficients for genus " + genus);
					}

					var a0 = netWasteCoe.getCoe(0);
					var a1 = netWasteCoe.getCoe(1);
					var a2 = netWasteCoe.getCoe(2);
					var a3 = netWasteCoe.getCoe(3);
					var a4 = netWasteCoe.getCoe(4);
					var a5 = netWasteCoe.getCoe(5);

					if (i == UtilizationClass.OVER225) {
						a0 += a5;
					}
					var frd = 1.0f - netDecay / closeUtilizationUtil.getCoe(i.index);

					float arg = a0 + a1 * frd + a3 * log(quadMeanDiameterUtil.getCoe(i.index)) + a4 * log(loreyHeight);

					arg += wasteModifierMap.get(genus, region);

					arg = clamp(arg, -10f, 10f);

					var frw = (1.0f - exp(a2 * frd)) * exp(arg) / (1f + exp(arg)) * (1f - frd);
					frw = min(frd, frw);

					float result = closeUtilizationUtil.getCoe(i.index) * (1f - frd - frw);

					/*
					 * Check for an apply adjustments. This is done after computing the result above to allow for
					 * clamping frw to frd
					 */
					if (aAdjust.getCoe(i.index) != 0f) {
						var ratio = result / netDecay;
						if (ratio < 1f && ratio > 0f) {
							arg = log(ratio / (1f - ratio));
							arg += aAdjust.getCoe(i.index);
							arg = clamp(arg, -10f, 10f);
							result = exp(arg) / (1f + exp(arg)) * netDecay;
						}
					}

					return result;
				}
		);

		if (utilizationClass == UtilizationClass.ALL) {
			storeSumUtilizationComponents(closeUtilizationNetOfDecayAndWasteUtil);
		}
	}

	/**
	 * EMP093. Estimate volume NET OF DECAY by (DBH) utilization classes
	 *
	 * @param controlMap
	 * @param genus
	 * @param region
	 * @param utilizationClass
	 * @param aAdjust
	 * @param decayGroup
	 * @param ageBreastHeight
	 * @param quadMeanDiameterUtil
	 * @param closeUtilizationUtil
	 * @param closeUtilizationNetOfDecayUtil
	 * @throws ProcessingException
	 */
	public static void estimateNetDecayVolume(
			Map<String, Object> controlMap, String genus, Region region, UtilizationClass utilizationClass,
			Coefficients aAdjust, int decayGroup, float ageBreastHeight, Coefficients quadMeanDiameterUtil,
			Coefficients closeUtilizationUtil, Coefficients closeUtilizationNetOfDecayUtil
	) throws ProcessingException {
		final var netDecayCoeMap = Utils.<MatrixMap2<Integer, Integer, Optional<Coefficients>>>expectParsedControl(
				controlMap, ControlKey.VOLUME_NET_DECAY, MatrixMap2.class
		);
		final var decayModifierMap = Utils.<MatrixMap2<String, Region, Float>>expectParsedControl(
				controlMap, ModifierParser.CONTROL_KEY_MOD301_DECAY, MatrixMap2.class
		);
		estimateNetDecayVolume(
				genus, region, utilizationClass, aAdjust, decayGroup, ageBreastHeight, netDecayCoeMap, decayModifierMap,
				quadMeanDiameterUtil, closeUtilizationUtil, closeUtilizationNetOfDecayUtil
		);
	}

	/**
	 * EMP093. Estimate volume NET OF DECAY by (DBH) utilization classes
	 *
	 * @param genus
	 * @param region
	 * @param utilizationClass
	 * @param aAdjust
	 * @param decayGroup
	 * @param ageBreastHeight
	 * @param netDecayCoeMap
	 * @param decayModifierMap
	 * @param quadMeanDiameterUtil
	 * @param closeUtilizationUtil
	 * @param closeUtilizationNetOfDecayUtil
	 * @param loreyHeight
	 * @throws ProcessingException
	 */
	public static void estimateNetDecayVolume(
			String genus, Region region, UtilizationClass utilizationClass, Coefficients aAdjust, int decayGroup,
			float ageBreastHeight, MatrixMap2<Integer, Integer, Optional<Coefficients>> netDecayCoeMap,
			MatrixMap2<String, Region, Float> decayModifierMap, Coefficients quadMeanDiameterUtil,
			Coefficients closeUtilizationUtil, Coefficients closeUtilizationNetOfDecayUtil
	) throws ProcessingException {
		var dqSp = quadMeanDiameterUtil.getCoe(UTIL_ALL);

		final var ageTr = (float) Math.log(Math.max(20.0, ageBreastHeight));

		estimateUtilization(closeUtilizationUtil, closeUtilizationNetOfDecayUtil, utilizationClass, (uc, cu) -> {
			Coefficients netDecayCoe = netDecayCoeMap.get(uc.index, decayGroup).orElseThrow(
					() -> new ProcessingException("Could not find net decay coefficients for group " + decayGroup)
			);
			var a0 = netDecayCoe.getCoe(1);
			var a1 = netDecayCoe.getCoe(2);
			var a2 = netDecayCoe.getCoe(3);

			float arg;
			if (uc != UtilizationClass.OVER225) {
				arg = a0 + a1 * log(dqSp) + a2 * ageTr;
			} else {
				arg = a0 + a1 * log(quadMeanDiameterUtil.getCoe(uc.index)) + a2 * ageTr;
			}

			arg += aAdjust.getCoe(uc.index) + decayModifierMap.get(genus, region);

			float ratio = ratio(arg, 8.0f);

			return cu * ratio;
		});

		if (utilizationClass == UtilizationClass.ALL) {
			storeSumUtilizationComponents(closeUtilizationNetOfDecayUtil);
		}
	}

	/**
	 * EMP095. Estimate utilization net of decay, waste, and breakage
	 *
	 * @param controlMap
	 * @param utilizationClass
	 * @param breakageGroup
	 * @param quadMeanDiameterUtil
	 * @param closeUtilizationUtil
	 * @param closeUtilizationNetOfDecayAndWasteUtil
	 * @param closeUtilizationNetOfDecayWasteAndBreakageUtil
	 * @throws ProcessingException
	 */
	public static void estimateNetDecayWasteAndBreakageVolume(
			Map<String, Object> controlMap, UtilizationClass utilizationClass, int breakageGroup,
			Coefficients quadMeanDiameterUtil, Coefficients closeUtilizationUtil,
			Coefficients closeUtilizationNetOfDecayAndWasteUtil,
			Coefficients closeUtilizationNetOfDecayWasteAndBreakageUtil
	) throws ProcessingException {
		final var netBreakageCoeMap = Utils
				.<Map<Integer, Coefficients>>expectParsedControl(controlMap, ControlKey.BREAKAGE, Map.class);

		estimateNetDecayWasteAndBreakageVolume(
				utilizationClass, breakageGroup, netBreakageCoeMap, quadMeanDiameterUtil, closeUtilizationUtil,
				closeUtilizationNetOfDecayAndWasteUtil, closeUtilizationNetOfDecayWasteAndBreakageUtil
		);
	}

	/**
	 * EMP095. Estimate utilization net of decay, waste, and breakage
	 *
	 * @param controlMap
	 * @param utilizationClass
	 * @param breakageGroup
	 * @param quadMeanDiameterUtil
	 * @param closeUtilizationUtil
	 * @param closeUtilizationNetOfDecayAndWasteUtil
	 * @param closeUtilizationNetOfDecayWasteAndBreakageUtil
	 * @throws ProcessingException
	 */
	public static void estimateNetDecayWasteAndBreakageVolume(
			UtilizationClass utilizationClass, int breakageGroup, Map<Integer, Coefficients> netBreakageCoeMap,
			Coefficients quadMeanDiameterUtil, Coefficients closeUtilizationUtil,
			Coefficients closeUtilizationNetOfDecayAndWasteUtil,
			Coefficients closeUtilizationNetOfDecayWasteAndBreakageUtil
	) throws ProcessingException {
		final var coefficients = netBreakageCoeMap.get(breakageGroup);
		if (coefficients == null) {
			throw new ProcessingException("Could not find net breakage coefficients for group " + breakageGroup);
		}

		final var a1 = coefficients.getCoe(1);
		final var a2 = coefficients.getCoe(2);
		final var a3 = coefficients.getCoe(3);
		final var a4 = coefficients.getCoe(4);

		estimateUtilization(
				closeUtilizationNetOfDecayAndWasteUtil, closeUtilizationNetOfDecayWasteAndBreakageUtil,
				utilizationClass, (uc, netWaste) -> {

					if (netWaste <= 0f) {
						return 0f;
					}
					var percentBroken = a1 + a2 * log(quadMeanDiameterUtil.getCoe(uc.index));
					percentBroken = clamp(percentBroken, a3, a4);
					var broken = min(percentBroken / 100 * closeUtilizationUtil.getCoe(uc.index), netWaste);
					return netWaste - broken;
				}
		);

		if (utilizationClass == UtilizationClass.ALL) {
			storeSumUtilizationComponents(closeUtilizationNetOfDecayWasteAndBreakageUtil);
		}
	}

	/**
	 * EMP090. Return an estimate of the volume, per tree, of the whole stem, based on the given lorey height and quad
	 * mean diameter.
	 *
	 * @param controlMap       the control map from which the total stand whole stem volume equation coefficients are
	 *                         retrieved
	 * @param volumeGroup      the species' volume group
	 * @param loreyHeight      the species' lorey height
	 * @param quadMeanDiameter the species' quadratic mean diameter
	 * @return as described
	 */
	public static float estimateWholeStemVolumePerTree(
			Map<String, Object> controlMap, int volumeGroup, float loreyHeight, float quadMeanDiameter
	) {
		var coeMap = Utils.<Map<Integer, Coefficients>>expectParsedControl(
				controlMap, ControlKey.TOTAL_STAND_WHOLE_STEM_VOL, Map.class
		);

		return estimateWholeStemVolumePerTree(volumeGroup, loreyHeight, quadMeanDiameter, coeMap);
	}

	/**
	 * EMP090. Return an estimate of the volume, per tree, of the whole stem, based on the given lorey height and quad
	 * mean diameter.
	 *
	 * @param controlMap       the control map from which the total stand whole stem volume equation coefficients are
	 *                         retrieved
	 * @param volumeGroup      the species' volume group
	 * @param loreyHeight      the species' lorey height
	 * @param quadMeanDiameter the species' quadratic mean diameter
	 * @return as described
	 */
	public static float estimateWholeStemVolumePerTree(
			int volumeGroup, float loreyHeight, float quadMeanDiameter,
			Map<Integer, Coefficients> totalStandWholeStemVolumeCoeMap
	) {
		var coe = totalStandWholeStemVolumeCoeMap.get(volumeGroup).reindex(0);

		var logMeanVolume = //
				coe.getCoe(UtilizationClass.ALL.index) + //
						coe.getCoe(1) * log(quadMeanDiameter) + //
						coe.getCoe(2) * log(loreyHeight) + //
						coe.getCoe(3) * quadMeanDiameter + //
						coe.getCoe(4) / quadMeanDiameter + //
						coe.getCoe(5) * loreyHeight + //
						coe.getCoe(6) * quadMeanDiameter * quadMeanDiameter + //
						coe.getCoe(7) * loreyHeight * quadMeanDiameter + //
						coe.getCoe(8) * loreyHeight / quadMeanDiameter;

		return exp(logMeanVolume);
	}

	/**
	 * EMP091. Updates wholeStemVolumeUtil with estimated values, getting wholeStemUtilizationComponentMap from the
	 * given controlMap.
	 *
	 * @param controlMap
	 * @param utilizationClass
	 * @param adjustCloseUtil
	 * @param volumeGroup
	 * @param hlSp
	 * @param quadMeanDiameterUtil
	 * @param baseAreaUtil
	 * @param wholeStemVolumeUtil
	 * @throws ProcessingException
	 */
	public static void estimateWholeStemVolume(
			Map<String, Object> controlMap, UtilizationClass utilizationClass, float adjustCloseUtil, int volumeGroup,
			Float hlSp, Coefficients quadMeanDiameterUtil, Coefficients baseAreaUtil, Coefficients wholeStemVolumeUtil
	) throws ProcessingException {
		final var wholeStemUtilizationComponentMap = Utils
				.<MatrixMap2<Integer, Integer, Optional<Coefficients>>>expectParsedControl(
						controlMap, ControlKey.UTIL_COMP_WS_VOLUME, MatrixMap2.class
				);

		estimateWholeStemVolume(
				utilizationClass, adjustCloseUtil, volumeGroup, hlSp, wholeStemUtilizationComponentMap,
				quadMeanDiameterUtil, baseAreaUtil, wholeStemVolumeUtil
		);
	}

	/**
	 * EMP091. Updates wholeStemVolumeUtil with estimated values.
	 *
	 * @param utilizationClass
	 * @param adjustCloseUtil
	 * @param volumeGroup
	 * @param hlSp
	 * @param wholeStemUtilizationComponentMap
	 * @param quadMeanDiameterUtil
	 * @param baseAreaUtil
	 * @param wholeStemVolumeUtil
	 * @throws ProcessingException
	 */
	public static void estimateWholeStemVolume(
			UtilizationClass utilizationClass, float adjustCloseUtil, int volumeGroup, Float hlSp,
			MatrixMap2<Integer, Integer, Optional<Coefficients>> wholeStemUtilizationComponentMap,
			Coefficients quadMeanDiameterUtil, Coefficients baseAreaUtil, Coefficients wholeStemVolumeUtil
	) throws ProcessingException {
		var dqSp = quadMeanDiameterUtil.getCoe(UTIL_ALL);

		estimateUtilization(baseAreaUtil, wholeStemVolumeUtil, utilizationClass, (uc, ba) -> {
			Coefficients wholeStemCoe = wholeStemUtilizationComponentMap.get(uc.index, volumeGroup).orElseThrow(
					() -> new ProcessingException(
							"Could not find whole stem utilization coefficients for group " + volumeGroup
					)
			);

			// Fortran code uses 1 index into array when reading it here, but 0 index when
			// writing into it in the parser. I use 0 for both.
			var a0 = wholeStemCoe.getCoe(0);
			var a1 = wholeStemCoe.getCoe(1);
			var a2 = wholeStemCoe.getCoe(2);
			var a3 = wholeStemCoe.getCoe(3);

			var arg = a0 + a1 * log(hlSp) + a2 * log(quadMeanDiameterUtil.getCoe(uc.index))
					+ ( (uc != UtilizationClass.OVER225) ? a3 * log(dqSp) : a3 * dqSp);

			if (uc == utilizationClass) {
				arg += adjustCloseUtil;
			}

			var vbaruc = exp(arg); // volume base area ?? utilization class?

			return ba * vbaruc;
		}, x -> x <= 0f, 0f);

		if (utilizationClass == UtilizationClass.ALL) {
			normalizeUtilizationComponents(wholeStemVolumeUtil);
		}
	}

	/**
	 * EMP071. Estimate DQ by utilization class, see ipsjf120.doc.
	 *
	 * @param controlMap
	 * @param bec
	 * @param quadMeanDiameterUtil
	 * @param genus
	 * @throws ProcessingException
	 */
	public static void estimateQuadMeanDiameterByUtilization(
			Map<String, Object> controlMap, BecDefinition bec, Coefficients quadMeanDiameterUtil, String genus
	) throws ProcessingException {

		final var coeMap = Utils.<MatrixMap3<Integer, String, String, Coefficients>>expectParsedControl(
				controlMap, ControlKey.UTIL_COMP_DQ, MatrixMap3.class
		);

		estimateQuadMeanDiameterByUtilization(bec, coeMap, quadMeanDiameterUtil, genus);
	}

	/**
	 * EMP071. Estimate DQ by utilization class, see ipsjf120.doc.
	 *
	 * @param bec
	 * @param coeMap
	 * @param quadMeanDiameterUtil
	 * @param genus
	 * @throws ProcessingException
	 */
	public static void estimateQuadMeanDiameterByUtilization(
			BecDefinition bec, MatrixMap3<Integer, String, String, Coefficients> coeMap,
			Coefficients quadMeanDiameterUtil, String genus
	) throws ProcessingException {
		log.atTrace().setMessage("Estimate DQ by utilization class for {} in BEC {}.  DQ for all >7.5 is {}")
				.addArgument(genus).addArgument(bec.getName()).addArgument(quadMeanDiameterUtil.getCoe(UTIL_ALL));

		float quadMeanDiameter07 = quadMeanDiameterUtil.getCoe(UTIL_ALL);

		for (var uc : UtilizationClass.UTIL_CLASSES) {
			log.atDebug().setMessage("For util level {}").addArgument(uc.className);
			var coe = coeMap.get(uc.index, genus, bec.getGrowthBec().getAlias());

			float a0 = coe.getCoe(1);
			float a1 = coe.getCoe(2);
			float a2 = coe.getCoe(3);

			log.atDebug().setMessage("a0={}, a1={}, a3={}").addArgument(a0).addArgument(a1).addArgument(a2);

			float logit;

			switch (uc) {
			case U75TO125:
				if (quadMeanDiameter07 < 7.5001f) {
					quadMeanDiameterUtil.setCoe(UTIL_ALL, 7.5f);
				} else {
					log.atDebug().setMessage("DQ = 7.5 + a0 * (1 - exp(a1 / a0*(DQ07 - 7.5) ))**a2' )");

					logit = a1 / a0 * (quadMeanDiameter07 - 7.5f);

					quadMeanDiameterUtil
							.setCoe(uc.index, min(7.5f + a0 * pow(1 - safeExponent(logit), a2), quadMeanDiameter07));
				}
				break;
			case U125TO175, U175TO225:
				log.atDebug().setMessage(
						"LOGIT = a0 + a1*(SQ07 / 7.5)**a2,  DQ = (12.5 or 17.5) + 5 * exp(LOGIT) / (1 + exp(LOGIT))"
				);
				logit = a0 + a1 * pow(quadMeanDiameter07 / 7.5f, a2);

				quadMeanDiameterUtil.setCoe(uc.index, uc.lowBound + 5f * exponentRatio(logit));
				break;
			case OVER225:
				float a3 = coe.getCoe(4);

				log.atDebug().setMessage(
						"Coeff A3 {}, LOGIT = a2 + a1*DQ07**a3,  DQ = DQ07 + a0 * (1 - exp(LOGIT) / (1 + exp(LOGIT)) )"
				);

				logit = a2 + a1 * pow(quadMeanDiameter07, a3);

				quadMeanDiameterUtil
						.setCoe(uc.index, max(22.5f, quadMeanDiameter07 + a0 * (1f - exponentRatio(logit))));
				break;
			case ALL, SMALL:
				throw new IllegalStateException(
						"Should not be attempting to process small component or all large components"
				);
			default:
				throw new IllegalStateException("Unknown utilization class " + uc);
			}

			log.atDebug().setMessage("Util DQ for class {} is {}").addArgument(uc.className)
					.addArgument(quadMeanDiameterUtil.getCoe(uc.index));
		}

		log.atTrace().setMessage("Estimated Diameters {}").addArgument(
				() -> UtilizationClass.UTIL_CLASSES.stream()
						.map(uc -> String.format("%s: %d", uc.className, quadMeanDiameterUtil.getCoe(uc.index)))
		);

	}

	/**
	 * EMP070. Estimate basal area by utilization class from the given parameters, after getting the estimation
	 * coefficients map from the control map.
	 *
	 * @param controlMap
	 * @param bec
	 * @param quadMeanDiameterUtil
	 * @param baseAreaUtil
	 * @param genus
	 * @throws ProcessingException
	 */
	public static void estimateBaseAreaByUtilization(
			Map<String, Object> controlMap, BecDefinition bec, Coefficients quadMeanDiameterUtil,
			Coefficients baseAreaUtil, String genus
	) throws ProcessingException {
		final var coeMap = Utils.<MatrixMap3<Integer, String, String, Coefficients>>expectParsedControl(
				controlMap, ControlKey.UTIL_COMP_BA, MatrixMap3.class
		);

		estimateBaseAreaByUtilization(bec, coeMap, quadMeanDiameterUtil, baseAreaUtil, genus);
	}

	/**
	 * EMP070: estimate basal area by utilization class from the given parameters
	 *
	 * @param bec
	 * @param basalAreaUtilCompCoeMap
	 * @param quadMeanDiameterUtil
	 * @param baseAreaUtil
	 * @param genus
	 * @throws ProcessingException
	 */
	public static void estimateBaseAreaByUtilization(
			BecDefinition bec, MatrixMap3<Integer, String, String, Coefficients> basalAreaUtilCompCoeMap,
			Coefficients quadMeanDiameterUtil, Coefficients baseAreaUtil, String genus
	) throws ProcessingException {

		float dq = quadMeanDiameterUtil.getCoe(UTIL_ALL);
		var b = Utils.utilizationVector();
		b.setCoe(0, baseAreaUtil.getCoe(UTIL_ALL));
		for (int i = 1; i < UTIL_LARGEST; i++) {
			var coe = basalAreaUtilCompCoeMap.get(i, genus, bec.getGrowthBec().getAlias());

			float a0 = coe.getCoe(1);
			float a1 = coe.getCoe(2);

			float logit;
			if (i == 1) {
				logit = a0 + a1 * pow(dq, 0.25f);
			} else {
				logit = a0 + a1 * dq;
			}
			b.setCoe(i, b.getCoe(i - 1) * exponentRatio(logit));
			if (i == 1 && quadMeanDiameterUtil.getCoe(UTIL_ALL) < 12.5f) {
				float ba12Max = (1f - pow(
						(quadMeanDiameterUtil.getCoe(1) - 7.4f) / (quadMeanDiameterUtil.getCoe(UTIL_ALL) - 7.4f), 2f
				)) * b.getCoe(0);
				b.scalarInPlace(1, x -> min(x, ba12Max));
			}
		}

		baseAreaUtil.setCoe(1, baseAreaUtil.getCoe(UTIL_ALL) - b.getCoe(1));
		baseAreaUtil.setCoe(2, b.getCoe(1) - b.getCoe(2));
		baseAreaUtil.setCoe(3, b.getCoe(2) - b.getCoe(3));
		baseAreaUtil.setCoe(4, b.getCoe(3));
	}

	@FunctionalInterface
	public static interface UtilizationProcessor {
		float apply(UtilizationClass utilizationClass, float inputValue) throws ProcessingException;
	}

	/**
	 * Estimate values for one utilization vector from another
	 *
	 * @param input            source utilization
	 * @param output           result utilization
	 * @param utilizationClass the utilization class for which to do the computation, UTIL_ALL for all of them.
	 * @param processor        Given a utilization class, and the source utilization for that class, return the result
	 *                         utilization
	 * @param skip             a utilization class will be skipped and the result set to the default value if this is
	 *                         true for the value of the source utilization
	 * @param defaultValue     the default value
	 * @throws ProcessingException
	 */
	private static void estimateUtilization(
			Coefficients input, Coefficients output, UtilizationClass utilizationClass, UtilizationProcessor processor,
			Predicate<Float> skip, float defaultValue
	) throws ProcessingException {
		for (var uc : UtilizationClass.UTIL_CLASSES) {
			var inputValue = input.getCoe(uc.index);

			// it seems like this should be done after checking i against utilizationClass,
			// which could just be done as part of the processor definition, but this is how
			// VDYP7 did it.
			if (skip.test(inputValue)) {
				output.setCoe(uc.index, defaultValue);
				continue;
			}

			if (utilizationClass != UtilizationClass.ALL && utilizationClass != uc) {
				continue;
			}

			var result = processor.apply(uc, inputValue);
			output.setCoe(uc.index, result);
		}
	}

	/**
	 * Estimate values for one utilization vector from another
	 *
	 * @param input            source utilization
	 * @param output           result utilization
	 * @param utilizationClass the utilization class for which to do the computation, UTIL_ALL for all of them.
	 * @param processor        Given a utilization class, and the source utilization for that class, return the result
	 *                         utilization
	 * @throws ProcessingException
	 */
	private static void estimateUtilization(
			Coefficients input, Coefficients output, UtilizationClass utilizationClass, UtilizationProcessor processor
	) throws ProcessingException {
		estimateUtilization(input, output, utilizationClass, processor, x -> false, 0f);
	}

	private static float exponentRatio(float logit) throws ProcessingException {
		float exp = safeExponent(logit);
		return exp / (1f + exp);
	}

	private static float safeExponent(float logit) throws ProcessingException {
		if (logit > 88f) {
			throw new ProcessingException("logit " + logit + " exceeds 88");
		}
		return exp(logit);
	}

	/**
	 * Normalizes the utilization components 1-4 so they sum to the value of component UTIL_ALL
	 *
	 * @throws ProcessingException if the sum is not positive
	 */
	private static float normalizeUtilizationComponents(Coefficients components) throws ProcessingException {
		var sum = sumUtilizationComponents(components);
		var k = components.getCoe(UTIL_ALL) / sum;
		if (sum <= 0f) {
			throw new ProcessingException("Total volume " + sum + " was not positive.");
		}
		UtilizationClass.UTIL_CLASSES.forEach(uc -> components.setCoe(uc.index, components.getCoe(uc.index) * k));
		return k;
	}

	/**
	 * Sums the individual utilization components (1-4)
	 */
	private static float sumUtilizationComponents(Coefficients components) {
		return (float) UtilizationClass.UTIL_CLASSES.stream().mapToInt(x -> x.index).mapToDouble(components::getCoe)
				.sum();
	}

	/**
	 * Sums the individual utilization components (1-4) and stores the results in coefficient UTIL_ALL
	 */
	private static float storeSumUtilizationComponents(Coefficients components) {
		var sum = sumUtilizationComponents(components);
		components.setCoe(UtilizationClass.ALL.index, sum);
		return sum;
	}
}
