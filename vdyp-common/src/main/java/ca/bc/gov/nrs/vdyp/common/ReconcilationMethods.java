package ca.bc.gov.nrs.vdyp.common;

import static ca.bc.gov.nrs.vdyp.math.FloatMath.abs;
import static ca.bc.gov.nrs.vdyp.math.FloatMath.sqrt;

import java.util.List;

import ca.bc.gov.nrs.vdyp.application.ProcessingException;
import ca.bc.gov.nrs.vdyp.common_calculators.BaseAreaTreeDensityDiameter;
import ca.bc.gov.nrs.vdyp.model.Coefficients;
import ca.bc.gov.nrs.vdyp.model.UtilizationClass;

public class ReconcilationMethods {

	private static final int UTIL_ALL = UtilizationClass.ALL.index;
	private static final List<UtilizationClass> MODE_1_RECONCILE_AVAILABILITY_CLASSES = List
			.of(UtilizationClass.OVER225, UtilizationClass.U175TO225, UtilizationClass.U125TO175);

	private ReconcilationMethods() {
	}

	/**
	 * YUC1R. Implements the three reconciliation modes for layer 1 as described in ipsjf120.doc
	 *
	 * @param baseAreaUtil
	 * @param treesPerHectareUtil
	 * @param quadMeanDiameterUtil
	 * @throws ProcessingException
	 */
	public static void reconcileComponents(
			Coefficients baseAreaUtil, Coefficients treesPerHectareUtil, Coefficients quadMeanDiameterUtil
	) throws ProcessingException {
		if (baseAreaUtil.getCoe(UTIL_ALL) == 0f) {
			UtilizationClass.UTIL_CLASSES.forEach(uc -> {
				treesPerHectareUtil.setCoe(uc.index, 0f);
				baseAreaUtil.setCoe(uc.index, 0f);
			});
			return;
		}

		@SuppressWarnings("unused")
		float tphSum = 0f;
		float baSum = 0f;
		for (var uc : UtilizationClass.UTIL_CLASSES) {
			tphSum += treesPerHectareUtil.getCoe(uc.index);
			baSum += baseAreaUtil.getCoe(uc.index);
		}

		if (abs(baSum - baseAreaUtil.getCoe(UTIL_ALL)) > 0.00003 * baSum) {
			throw new ProcessingException("Computed base areas for 7.5+ components do not sum to expected total");
		}

		float dq0 = BaseAreaTreeDensityDiameter
				.quadMeanDiameter(baseAreaUtil.getCoe(UTIL_ALL), treesPerHectareUtil.getCoe(UTIL_ALL));

		if (dq0 < 7.5f) {
			throw new ProcessingException(
					"Quadratic mean diameter computed from total base area and trees per hectare is less than 7.5 cm"
			);
		}

		float tphSumHigh = (float) UtilizationClass.UTIL_CLASSES.stream()
				.mapToDouble(
						uc -> BaseAreaTreeDensityDiameter.treesPerHectare(baseAreaUtil.getCoe(uc.index), uc.lowBound)
				).sum();

		if (tphSumHigh < treesPerHectareUtil.getCoe(UTIL_ALL)) {
			reconcileComponentsMode1(baseAreaUtil, treesPerHectareUtil, quadMeanDiameterUtil, tphSumHigh);
		} else {
			reconcileComponentsMode2Check(baseAreaUtil, treesPerHectareUtil, quadMeanDiameterUtil);
		}
	}

	@SuppressWarnings("java:S3655")
	public static void reconcileComponentsMode1(
			Coefficients baseAreaUtil, Coefficients treesPerHectareUtil, Coefficients quadMeanDiameterUtil,
			float tphSumHigh
	) {
		// MODE 1

		// the high sum of TPH's is too low. Need MODE 1 reconciliation MUST set DQU's
		// to lowest allowable values AND must move BA from upper classes to lower
		// classes.

		float tphNeed = treesPerHectareUtil.getCoe(UTIL_ALL) - tphSumHigh;

		UtilizationClass.UTIL_CLASSES.forEach(uc -> quadMeanDiameterUtil.setCoe(uc.index, uc.lowBound));

		for (var uc : MODE_1_RECONCILE_AVAILABILITY_CLASSES) {
			float tphAvail = BaseAreaTreeDensityDiameter
					.treesPerHectare(baseAreaUtil.getCoe(uc.index), uc.previous().get().lowBound)
					- BaseAreaTreeDensityDiameter.treesPerHectare(baseAreaUtil.getCoe(uc.index), uc.lowBound);

			if (tphAvail < tphNeed) {
				baseAreaUtil.scalarInPlace(uc.previous().get().index, x -> x + baseAreaUtil.getCoe(uc.index));
				baseAreaUtil.setCoe(uc.index, 0f);
				tphNeed -= tphAvail;
			} else {
				float baseAreaMove = baseAreaUtil.getCoe(uc.index) * tphNeed / tphAvail;
				baseAreaUtil.scalarInPlace(uc.previous().get().index, x -> x + baseAreaMove);
				baseAreaUtil.scalarInPlace(uc.index, x -> x - baseAreaMove);
				break;
			}
		}
		UtilizationClass.UTIL_CLASSES.forEach(
				uc -> treesPerHectareUtil.setCoe(
						uc.index,
						BaseAreaTreeDensityDiameter
								.treesPerHectare(baseAreaUtil.getCoe(uc.index), quadMeanDiameterUtil.getCoe(uc.index))
				)
		);
	}

	public static void reconcileComponentsMode2Check(
			Coefficients baseAreaUtil, Coefficients treesPerHectareUtil, Coefficients quadMeanDiameterUtil
	) throws ProcessingException {
		// Before entering mode 2, check to see if reconciliation is already adequate

		float tphSum = (float) UtilizationClass.UTIL_CLASSES.stream()
				.mapToDouble(uc -> treesPerHectareUtil.getCoe(uc.index)).sum();

		if (abs(tphSum - treesPerHectareUtil.getCoe(UTIL_ALL)) / tphSum > 0.00001) {
			reconcileComponentsMode2(baseAreaUtil, treesPerHectareUtil, quadMeanDiameterUtil);
			return;
		}
		for (var uc : UtilizationClass.UTIL_CLASSES) {
			if (baseAreaUtil.getCoe(uc.index) > 0f) {
				if (treesPerHectareUtil.getCoe(uc.index) <= 0f) {
					reconcileComponentsMode2(baseAreaUtil, treesPerHectareUtil, quadMeanDiameterUtil);
					return;
				}
				float dWant = BaseAreaTreeDensityDiameter
						.quadMeanDiameter(baseAreaUtil.getCoe(uc.index), treesPerHectareUtil.getCoe(uc.index));
				float dqI = quadMeanDiameterUtil.getCoe(uc.index);
				if (dqI >= uc.lowBound && dqI <= uc.highBound && abs(dWant - dqI) < 0.00001) {
					return;
				}
			}
		}
	}

	public static void reconcileComponentsMode2(
			Coefficients baseAreaUtil, Coefficients treesPerHectareUtil, Coefficients quadMeanDiameterUtil
	) throws ProcessingException {
		int n = 0;
		float baseAreaFixed = 0f;
		float treesPerHectareFixed = 0f;
		var quadMeanDiameterLimit = new boolean[] { false, false, false, false, false };
		Coefficients dqTrial = Utils.utilizationVector();

		while (true) {
			n++;

			if (n > 4) {
				throw new ProcessingException("Mode 2 component reconciliation iterations exceeded 4");
			}

			float sum = (float) UtilizationClass.UTIL_CLASSES.stream().mapToDouble(uc -> {
				float baI = baseAreaUtil.getCoe(uc.index);
				float dqI = quadMeanDiameterUtil.getCoe(uc.index);
				if (baI != 0 && !quadMeanDiameterLimit[uc.index]) {
					return baI / (dqI * dqI);
				}
				return 0;
			}).sum();

			float baAll = baseAreaUtil.getCoe(UTIL_ALL) - baseAreaFixed;
			float tphAll = treesPerHectareUtil.getCoe(UTIL_ALL) - treesPerHectareFixed;

			if (baAll <= 0f || tphAll <= 0f) {
				reconcileComponentsMode3(baseAreaUtil, treesPerHectareUtil, quadMeanDiameterUtil);
				return;
			}

			float dqAll = BaseAreaTreeDensityDiameter.quadMeanDiameter(baAll, tphAll);

			float k = dqAll * dqAll / baAll * sum;
			float sqrtK = sqrt(k);

			for (var uc : UtilizationClass.UTIL_CLASSES) {
				if (!quadMeanDiameterLimit[uc.index] && baseAreaUtil.getCoe(uc.index) > 0f) {
					dqTrial.setCoe(uc.index, quadMeanDiameterUtil.getCoe(uc.index) * sqrtK);
				}
			}

			UtilizationClass violateClass = null;
			float violate = 0f;
			boolean violateLow = false;

			for (var uc : UtilizationClass.UTIL_CLASSES) {
				if (baseAreaUtil.getCoe(uc.index) > 0f && dqTrial.getCoe(uc.index) < uc.lowBound) {
					float vi = 1f - dqTrial.getCoe(uc.index) / uc.lowBound;
					if (vi > violate) {
						violate = vi;
						violateClass = uc;
						violateLow = true;

					}
				}
				if (dqTrial.getCoe(uc.index) > uc.highBound) {
					float vi = dqTrial.getCoe(uc.index) / uc.highBound - 1f;
					if (vi > violate) {
						violate = vi;
						violateClass = uc;
						violateLow = false;
					}
				}
			}
			if (violateClass == null)
				break;
			// Move the worst offending DQ to its limit
			dqTrial.setCoe(violateClass.index, violateLow ? violateClass.lowBound : violateClass.highBound);

			quadMeanDiameterLimit[violateClass.index] = true;
			baseAreaFixed += baseAreaUtil.getCoe(violateClass.index);
			treesPerHectareFixed += BaseAreaTreeDensityDiameter
					.treesPerHectare(baseAreaUtil.getCoe(violateClass.index), dqTrial.getCoe(violateClass.index));
		}

		// Make BA's agree with DQ's and TPH's
		for (var uc : UtilizationClass.UTIL_CLASSES) {
			quadMeanDiameterUtil.setCoe(uc.index, dqTrial.getCoe(uc.index));
			treesPerHectareUtil.setCoe(
					uc.index,
					BaseAreaTreeDensityDiameter
							.treesPerHectare(baseAreaUtil.getCoe(uc.index), quadMeanDiameterUtil.getCoe(uc.index))
			);
		}
		// RE VERIFY That sums are correct
		float baSum = (float) UtilizationClass.UTIL_CLASSES.stream().mapToDouble(uc -> baseAreaUtil.getCoe(uc.index))
				.sum();
		float tphSum = (float) UtilizationClass.UTIL_CLASSES.stream()
				.mapToDouble(uc -> treesPerHectareUtil.getCoe(uc.index)).sum();
		if (abs(baSum - baseAreaUtil.getCoe(UTIL_ALL)) > 0.0002 * baSum) {
			throw new ProcessingException("Failed to reconcile Base Area");
		}
		if (abs(tphSum - treesPerHectareUtil.getCoe(UTIL_ALL)) > 0.0002 * tphSum) {
			throw new ProcessingException("Failed to reconcile Trees per Hectare");
		}
	}

	@SuppressWarnings("java:S3655")
	public static void reconcileComponentsMode3(
			Coefficients baseAreaUtil, Coefficients treesPerHectareUtil, Coefficients quadMeanDiameterUtil
	) {

		/*
		 * Reconciliation mode 3 NOT IN THE ORIGINAL DESIGN The primary motivation for this mode is an example where all
		 * trees were in a single utilization class and had a DQ of 12.4 cm. BUT the true DQ for the stand was slightly
		 * over 12.5. In this case the best solution is to simply reassign all trees to the single most appropriate
		 * class.
		 *
		 * Note, "original design" means something pre-VDYP 7. This was added to the Fortran some time before the port
		 * to Java including the comment above.
		 */
		UtilizationClass.UTIL_CLASSES.forEach(uc -> {
			baseAreaUtil.setCoe(uc.index, 0f);
			treesPerHectareUtil.setCoe(uc.index, 0f);
			quadMeanDiameterUtil.setCoe(uc.index, uc.lowBound + 2.5f);
		});

		var ucToUpdate = UtilizationClass.UTIL_CLASSES.stream()
				.filter(uc -> quadMeanDiameterUtil.getCoe(UTIL_ALL) < uc.highBound).findFirst().get();

		baseAreaUtil.setCoe(ucToUpdate.index, baseAreaUtil.getCoe(UTIL_ALL));
		treesPerHectareUtil.setCoe(ucToUpdate.index, treesPerHectareUtil.getCoe(UTIL_ALL));
		quadMeanDiameterUtil.setCoe(ucToUpdate.index, quadMeanDiameterUtil.getCoe(UTIL_ALL));
	}
}
