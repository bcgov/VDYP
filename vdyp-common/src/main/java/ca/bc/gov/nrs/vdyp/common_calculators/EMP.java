package ca.bc.gov.nrs.vdyp.common_calculators;

import static ca.bc.gov.nrs.vdyp.math.FloatMath.abs;
import static ca.bc.gov.nrs.vdyp.math.FloatMath.clamp;
import static ca.bc.gov.nrs.vdyp.math.FloatMath.exp;
import static ca.bc.gov.nrs.vdyp.math.FloatMath.exponentRatio;
import static ca.bc.gov.nrs.vdyp.math.FloatMath.log;
import static ca.bc.gov.nrs.vdyp.math.FloatMath.pow;
import static ca.bc.gov.nrs.vdyp.math.FloatMath.ratio;
import static ca.bc.gov.nrs.vdyp.math.FloatMath.safeExponent;
import static ca.bc.gov.nrs.vdyp.math.FloatMath.sqrt;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static ca.bc.gov.nrs.vdyp.application.VdypStartApplication.UTIL_ALL;
import static ca.bc.gov.nrs.vdyp.application.VdypStartApplication.UTIL_SMALL;
import static ca.bc.gov.nrs.vdyp.application.VdypStartApplication.UTIL_LARGEST;
import static ca.bc.gov.nrs.vdyp.application.VdypStartApplication.UTIL_CLASSES;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.bc.gov.nrs.vdyp.application.ProcessingException;
import ca.bc.gov.nrs.vdyp.common.ControlKey;
import ca.bc.gov.nrs.vdyp.common.IndexedFloatBinaryOperator;
import ca.bc.gov.nrs.vdyp.common.Utils;
import ca.bc.gov.nrs.vdyp.io.parse.coe.GenusDefinitionParser;
import ca.bc.gov.nrs.vdyp.io.parse.coe.ModifierParser;
import ca.bc.gov.nrs.vdyp.math.FloatMath;
import ca.bc.gov.nrs.vdyp.model.BaseVdypSpecies;
import ca.bc.gov.nrs.vdyp.model.BecDefinition;
import ca.bc.gov.nrs.vdyp.model.Coefficients;
import ca.bc.gov.nrs.vdyp.model.MatrixMap2;
import ca.bc.gov.nrs.vdyp.model.MatrixMap3;
import ca.bc.gov.nrs.vdyp.model.NonprimaryHLCoefficients;
import ca.bc.gov.nrs.vdyp.model.Region;
import ca.bc.gov.nrs.vdyp.model.UtilizationClass;
import ca.bc.gov.nrs.vdyp.model.VdypLayer;
import ca.bc.gov.nrs.vdyp.model.VdypSpecies;

/**
 * EMP### functions from VDYP 7
 */
public class EMP {
	private static final Logger log = LoggerFactory.getLogger(EMP.class);

	Map<String, Object> controlMap;

	public EMP(Map<String, Object> controlMap) {
		super();
		this.controlMap = controlMap;
	}

	public static final IndexedFloatBinaryOperator COPY_IF_BAND = (oldX, newX, i) -> i <= UTIL_ALL ? oldX : newX;
	public static final IndexedFloatBinaryOperator COPY_IF_NOT_TOTAL = (oldX, newX, i) -> i < UTIL_ALL ? oldX : newX;

	private float heightMultiplier(String genus, Region region, float treesPerHectarePrimary) {
		final var coeMap = Utils.<MatrixMap2<String, Region, Coefficients>>expectParsedControl(
				controlMap, ControlKey.HL_PRIMARY_SP_EQN_P1, MatrixMap2.class
		);
		var coe = coeMap.get(genus, region).reindex(0);
		return coe.get(0) - coe.getCoe(1) + coe.getCoe(1) * exp(coe.getCoe(2) * (treesPerHectarePrimary - 100f));
	}

	// EMP050 Meth==1
	/**
	 * Return the lorey height of the primary species based on the dominant height of the lead species.
	 *
	 * @param leadHeight             dominant height of the lead species
	 * @param genus                  Primary species
	 * @param region                 Region of the polygon
	 * @param treesPerHectarePrimary trees per hectare >7.5 cm of the primary species
	 * @return
	 */
	public float
			primaryHeightFromLeadHeight(float leadHeight, String genus, Region region, float treesPerHectarePrimary) {
		return 1.3f + (leadHeight - 1.3f) * heightMultiplier(genus, region, treesPerHectarePrimary);
	}

	// EMP050 Meth==2
	/**
	 * Return the dominant height of the lead species based on the lorey height of the primary species.
	 *
	 * @param primaryHeight          lorey height of the primary species
	 * @param genus                  Primary species
	 * @param region                 Region of the polygon
	 * @param treesPerHectarePrimary trees per hectare >7.5 cm of the primary species
	 * @return
	 */
	public float leadHeightFromPrimaryHeight(
			float primaryHeight, String genus, Region region, float treesPerHectarePrimary
	) {
		return 1.3f + (primaryHeight - 1.3f) / heightMultiplier(genus, region, treesPerHectarePrimary);
	}

	// EMP051
	/**
	 * Return the lorey height of the primary species based on the dominant height of the lead species.
	 *
	 * @param leadHeight dominant height of the lead species
	 * @param genus      Primary species
	 * @param region     Region of the polygon
	 * @return
	 */
	public float primaryHeightFromLeadHeightInitial(float leadHeight, String genus, Region region) {
		final var coeMap = Utils.<MatrixMap2<String, Region, Coefficients>>expectParsedControl(
				controlMap, ControlKey.HL_PRIMARY_SP_EQN_P2, MatrixMap2.class
		);
		var coe = coeMap.get(genus, region);
		return 1.3f + coe.getCoe(1) * pow(leadHeight - 1.3f, coe.getCoe(2));
	}

	// EMP053 Using eqns N1 and N2 from ipsjf124.doc
	/**
	 * Estimate the lorey height of a non-primary species of a primary layer.
	 *
	 * @param vspec         The species.
	 * @param vspecPrime    The primary species.
	 * @param leadHeight    lead height of the layer
	 * @param primaryHeight height of the primary species
	 * @throws ProcessingException
	 */
	public float estimateNonPrimaryLoreyHeight(
			BaseVdypSpecies vspec, BaseVdypSpecies vspecPrime, BecDefinition bec, float leadHeight, float primaryHeight
	) throws ProcessingException {
		return estimateNonPrimaryLoreyHeight(vspec.getGenus(), vspecPrime.getGenus(), bec, leadHeight, primaryHeight);
	}

	// EMP053 Using eqns N1 and N2 from ipsjf124.doc
	/**
	 * Estimate the lorey height of a non-primary species of a primary layer.
	 *
	 * @param vspec         The species.
	 * @param vspecPrime    The primary species.
	 * @param leadHeight    lead height of the layer
	 * @param primaryHeight height of the primary species
	 * @throws ProcessingException
	 */
	public float estimateNonPrimaryLoreyHeight(
			String vspec, String vspecPrime, BecDefinition bec, float leadHeight, float primaryHeight
	) throws ProcessingException {
		var coeMap = Utils.<MatrixMap3<String, String, Region, Optional<NonprimaryHLCoefficients>>>expectParsedControl(
				controlMap, ControlKey.HL_NONPRIMARY, MatrixMap3.class
		);

		var coe = coeMap.get(vspec, vspecPrime, bec.getRegion()).orElseThrow(
				() -> new ProcessingException(
						String.format(
								"Could not find Lorey Height Nonprimary Coefficients for %s %s %s", vspec, vspecPrime,
								bec.getRegion()
						)
				)
		);
		var heightToUse = coe.getEquationIndex() == 1 ? leadHeight : primaryHeight;
		return 1.3f + coe.getCoe(1) * pow(heightToUse - 1.3f, coe.getCoe(2));
	}

	// EMP060
	/**
	 * Estimate DQ for a species (primary or not). Using eqn in jf125.doc.
	 *
	 * Enforces mins and maxes from EMP061.
	 *
	 * @param spec                  Species of insterest
	 * @param allSpecies            Collection of all species on the layer
	 * @param region                BEC Region of the stand
	 * @param standQuadMeanDiameter Quadratic mean diameter of the stand
	 * @param standBaseArea         Base area of the stand
	 * @param standTreesPerHectare  Density opf the stand
	 * @param standLoreyHeight      Lorey height of the stand
	 * @return Quadratic mean diameter of the species of interest
	 * @throws ProcessingException
	 */
	public float estimateQuadMeanDiameterForSpecies(
			VdypSpecies spec, // ISP, HLsp, DQsp
			Map<String, VdypSpecies> allSpecies, // FR
			Region region, // INDEX_IC
			float standQuadMeanDiameter, // DQ_TOT
			float standBaseArea, // BA_TOT
			float standTreesPerHectare, // TPH_TOT
			float standLoreyHeight // HL_TOT
	) throws ProcessingException {
		String species = spec.getGenus();

		float c = 0.00441786467f;

		float minQuadMeanDiameter = min(7.6f, standQuadMeanDiameter);

		// Quick solution
		if (spec.getFractionGenus() >= 1f || standQuadMeanDiameter < minQuadMeanDiameter) {
			return standQuadMeanDiameter;
		}

		var coeMap = Utils
				.<Map<String, Coefficients>>expectParsedControl(controlMap, ControlKey.BY_SPECIES_DQ, Map.class);
		var specAliases = GenusDefinitionParser.getSpeciesAliases(controlMap);

		// TODO we can probably remove these as they seem to only be used for debugging
		// in VDYP7
		Map<String, Float> adjust = new HashMap<>(coeMap.size());
		Map<String, Float> mult = new HashMap<>(coeMap.size());

		var specIt = specAliases.iterator();

		var spec1 = specIt.next();

		float a2 = coeMap.get(spec1).getCoe(2);

		float fractionOther = 1f - spec.getFractionGenus(); // FR_REST

		mult.put(spec1, 1f);
		float a0 = coeMap.get(spec1).getCoe(0);
		float a1 = coeMap.get(spec1).getCoe(1);

		while (specIt.hasNext()) {
			var specIAlias = specIt.next();
			var specI = allSpecies.get(specIAlias);
			if (specIAlias.equals(spec.getGenus())) {
				float multI = 1f;
				mult.put(specIAlias, multI);
				a0 += multI * coeMap.get(specIAlias).getCoe(0);
				a1 += multI * coeMap.get(specIAlias).getCoe(1);
			} else {
				if (specI != null && specI.getFractionGenus() > 0f) {
					float multI = -specI.getFractionGenus() / fractionOther;
					mult.put(specIAlias, multI);
					a0 += multI * coeMap.get(specIAlias).getCoe(0);
					a1 -= multI * coeMap.get(specIAlias).getCoe(1);
				}
			}
		}

		float loreyHeightSpec = spec.getLoreyHeightByUtilization().getCoe(UtilizationClass.ALL.index);
		float loreyHeight1 = max(4f, loreyHeightSpec);
		float loreyHeight2 = (standLoreyHeight - loreyHeightSpec * spec.getFractionGenus()) / fractionOther;
		float loreyHeightRatio = clamp( (loreyHeight1 - 3f) / (loreyHeight2 - 3f), 0.05f, 20f);

		float r = exp(
				a0 + a1 * log(loreyHeightRatio) + a2 * log(standQuadMeanDiameter) + adjust.getOrDefault(species, 0f)
		);

		float baseArea1 = spec.getFractionGenus() * standBaseArea;
		float baseArea2 = standBaseArea - baseArea1;

		float treesPerHectare1;
		if (abs(r - 1f) < 0.0005) {
			treesPerHectare1 = spec.getFractionGenus() * standTreesPerHectare;
		} else {
			float aa = (r - 1f) * c;
			float bb = c * (1f - r) * standTreesPerHectare + baseArea1 + baseArea2 * r;
			float cc = -baseArea1 * standTreesPerHectare;
			float term = bb * bb - 4 * aa * cc;
			if (term <= 0f) {
				throw new ProcessingException(
						"Term for trees per hectare calculation when estimating quadratic mean diameter for species "
								+ species + " was " + term + " but should be positive."
				);
			}
			treesPerHectare1 = (-bb + sqrt(term)) / (2f * aa);
			if (treesPerHectare1 <= 0f || treesPerHectare1 > standTreesPerHectare) {
				throw new ProcessingException(
						"Trees per hectare 1 for species " + species + " was " + treesPerHectare1
								+ " but should be positive and less than or equal to stand trees per hectare "
								+ standTreesPerHectare
				);
			}
		}

		float quadMeanDiameter1 = BaseAreaTreeDensityDiameter.quadMeanDiameter(baseArea1, treesPerHectare1);
		float treesPerHectare2 = standTreesPerHectare - treesPerHectare1;
		float quadMeanDiameter2 = BaseAreaTreeDensityDiameter.quadMeanDiameter(baseArea2, treesPerHectare2);

		if (quadMeanDiameter2 < minQuadMeanDiameter) {
			// species 2 is too small. Make target species smaller.
			quadMeanDiameter2 = minQuadMeanDiameter;
			treesPerHectare2 = BaseAreaTreeDensityDiameter.treesPerHectare(baseArea2, quadMeanDiameter2);
			treesPerHectare1 = standTreesPerHectare - treesPerHectare2;
			quadMeanDiameter1 = BaseAreaTreeDensityDiameter.quadMeanDiameter(baseArea1, treesPerHectare1);
		}
		var limits = getLimitsForHeightAndDiameter(species, region);

		final float dqMinSp = max(minQuadMeanDiameter, limits.minDiameterHeight() * loreyHeightSpec);
		final float dqMaxSp = max(
				7.6f, min(limits.maxQuadMeanDiameter(), limits.maxDiameterHeight() * loreyHeightSpec)
		);
		if (quadMeanDiameter1 < dqMinSp) {
			quadMeanDiameter1 = dqMinSp;
			treesPerHectare1 = BaseAreaTreeDensityDiameter.treesPerHectare(baseArea1, quadMeanDiameter1);
			treesPerHectare2 = standTreesPerHectare - treesPerHectare2;
			quadMeanDiameter2 = BaseAreaTreeDensityDiameter.quadMeanDiameter(baseArea2, treesPerHectare2);
		}
		if (quadMeanDiameter1 > dqMaxSp) {
			// target species is too big. Make target species smaller, DQ2 bigger.

			quadMeanDiameter1 = dqMaxSp;
			treesPerHectare1 = BaseAreaTreeDensityDiameter.treesPerHectare(baseArea1, quadMeanDiameter1);
			treesPerHectare2 = standTreesPerHectare - treesPerHectare2;

			if (treesPerHectare2 > 0f && baseArea2 > 0f) {
				quadMeanDiameter2 = BaseAreaTreeDensityDiameter.quadMeanDiameter(baseArea2, treesPerHectare2);
			} else {
				quadMeanDiameter2 = 1000f;
			}

			// under rare circumstances, let DQ1 exceed DQMAXsp
			if (quadMeanDiameter2 < minQuadMeanDiameter) {
				quadMeanDiameter2 = minQuadMeanDiameter;
				treesPerHectare2 = BaseAreaTreeDensityDiameter.treesPerHectare(baseArea2, quadMeanDiameter2);
				treesPerHectare1 = standTreesPerHectare - treesPerHectare2;
				quadMeanDiameter1 = BaseAreaTreeDensityDiameter.quadMeanDiameter(baseArea1, treesPerHectare1);
			}

		}
		return quadMeanDiameter1;
	}

	public static record Limits(
			float maxLoreyHeight, float maxQuadMeanDiameter, float minDiameterHeight, float maxDiameterHeight
	) {
	};

	// EMP061
	public Limits getLimitsForHeightAndDiameter(String genus, Region region) {
		var coeMap = Utils.<MatrixMap2<String, Region, Coefficients>>expectParsedControl(
				controlMap, ControlKey.SPECIES_COMPONENT_SIZE_LIMIT, MatrixMap2.class
		);

		var coe = coeMap.get(genus, region);
		return new Limits(coe.getCoe(1), coe.getCoe(2), coe.getCoe(3), coe.getCoe(4));
	}

	// EMP070
	public void estimateBaseAreaByUtilization(
			BecDefinition bec, Coefficients quadMeanDiameterUtil, Coefficients baseAreaUtil, VdypSpecies spec
	) throws ProcessingException {
		final var coeMap = Utils.<MatrixMap3<Integer, String, String, Coefficients>>expectParsedControl(
				controlMap, ControlKey.UTIL_COMP_BA, MatrixMap3.class
		);

		float dq = quadMeanDiameterUtil.getCoe(UTIL_ALL);
		var b = Utils.utilizationVector();
		b.setCoe(0, baseAreaUtil.getCoe(UTIL_ALL));
		for (int i = 1; i < UTIL_LARGEST; i++) {
			var coe = coeMap.get(i, spec.getGenus(), bec.getGrowthBec().getAlias());

			float a0 = coe.getCoe(1);
			float a1 = coe.getCoe(2);

			float logit;
			if (i == 1) {
				logit = a0 + a1 * pow(dq, 0.25f);
			} else {
				logit = a0 + a1 * dq;
			}
			b.setCoe(i, b.getCoe(i - 1) * FloatMath.exponentRatio(logit));
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

	/**
	 * Estimate DQ by utilization class, see ipsjf120.doc
	 *
	 * @param bec
	 * @param quadMeanDiameterUtil
	 * @param spec
	 * @throws ProcessingException
	 */
	// EMP071
	public void estimateQuadMeanDiameterByUtilization(
			BecDefinition bec, Coefficients quadMeanDiameterUtil, VdypSpecies spec
	) throws ProcessingException {
		log.atTrace().setMessage("Estimate DQ by utilization class for {} in BEC {}.  DQ for all >7.5 is {}")
				.addArgument(spec.getGenus()).addArgument(bec.getName())
				.addArgument(quadMeanDiameterUtil.getCoe(UTIL_ALL));

		float quadMeanDiameter07 = quadMeanDiameterUtil.getCoe(UTIL_ALL);

		for (var uc : UTIL_CLASSES) {
			log.atDebug().setMessage("For util level {}").addArgument(uc.className);
			final var coeMap = Utils.<MatrixMap3<Integer, String, String, Coefficients>>expectParsedControl(
					controlMap, ControlKey.UTIL_COMP_DQ, MatrixMap3.class
			);
			var coe = coeMap.get(uc.index, spec.getGenus(), bec.getGrowthBec().getAlias());

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
				() -> UTIL_CLASSES.stream()
						.map(uc -> String.format("%s: %d", uc.className, quadMeanDiameterUtil.getCoe(uc.index)))
		);

	}

	// EMP091
	/**
	 * Updates wholeStemVolumeUtil with estimated values.
	 */
	public void estimateWholeStemVolume(
			UtilizationClass utilizationClass, float adjustCloseUtil, int volumeGroup, Float hlSp,
			Coefficients quadMeanDiameterUtil, Coefficients baseAreaUtil, Coefficients wholeStemVolumeUtil
	) throws ProcessingException {
		var dqSp = quadMeanDiameterUtil.getCoe(UTIL_ALL);
		final var wholeStemUtilizationComponentMap = Utils
				.<MatrixMap2<Integer, Integer, Optional<Coefficients>>>expectParsedControl(
						controlMap, ControlKey.UTIL_COMP_WS_VOLUME, MatrixMap2.class
				);
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
		}, x -> x < 0f, 0f);

		if (utilizationClass == UtilizationClass.ALL) {
			normalizeUtilizationComponents(wholeStemVolumeUtil);
		}

	}

	// EMP092
	/**
	 * Updates closeUtilizationVolumeUtil with estimated values.
	 *
	 * @throws ProcessingException
	 */
	public void estimateCloseUtilizationVolume(
			UtilizationClass utilizationClass, Coefficients aAdjust, int volumeGroup, float hlSp,
			Coefficients quadMeanDiameterUtil, Coefficients wholeStemVolumeUtil, Coefficients closeUtilizationVolumeUtil
	) throws ProcessingException {
		final var closeUtilizationCoeMap = Utils
				.<MatrixMap2<Integer, Integer, Optional<Coefficients>>>expectParsedControl(
						controlMap, ControlKey.CLOSE_UTIL_VOLUME, MatrixMap2.class
				);
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

	@FunctionalInterface
	static interface UtilizationProcessor {
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
	 * @throws ProcessingException
	 */
	static void estimateUtilization(
			Coefficients input, Coefficients output, UtilizationClass utilizationClass, UtilizationProcessor processor
	) throws ProcessingException {
		estimateUtilization(input, output, utilizationClass, processor, x -> false, 0f);
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
	static void estimateUtilization(
			Coefficients input, Coefficients output, UtilizationClass utilizationClass, UtilizationProcessor processor,
			Predicate<Float> skip, float defaultValue
	) throws ProcessingException {
		for (var uc : UTIL_CLASSES) {
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

			var result = processor.apply(uc, input.getCoe(uc.index));
			output.setCoe(uc.index, result);
		}
	}

	/**
	 * Estimate volume NET OF DECAY by (DBH) utilization classes
	 *
	 * @param utilizationClass
	 * @param aAdjust
	 * @param decayGroup
	 * @param lorieHeight
	 * @param ageBreastHeight
	 * @param quadMeanDiameterUtil
	 * @param closeUtilizationUtil
	 * @param closeUtilizationNetOfDecayUtil
	 * @throws ProcessingException
	 */
	public // EMP093
	void estimateNetDecayVolume(
			String genus, Region region, UtilizationClass utilizationClass, Coefficients aAdjust, int decayGroup,
			float lorieHeight, float ageBreastHeight, Coefficients quadMeanDiameterUtil,
			Coefficients closeUtilizationUtil, Coefficients closeUtilizationNetOfDecayUtil
	) throws ProcessingException {
		var dqSp = quadMeanDiameterUtil.getCoe(UTIL_ALL);
		final var netDecayCoeMap = Utils.<MatrixMap2<Integer, Integer, Optional<Coefficients>>>expectParsedControl(
				controlMap, ControlKey.VOLUME_NET_DECAY, MatrixMap2.class
		);
		final var decayModifierMap = Utils.<MatrixMap2<String, Region, Float>>expectParsedControl(
				controlMap, ModifierParser.CONTROL_KEY_MOD301_DECAY, MatrixMap2.class
		);

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
	 * Estimate utilization net of decay and waste
	 */
	public // EMP094
	void estimateNetDecayAndWasteVolume(
			Region region, UtilizationClass utilizationClass, Coefficients aAdjust, String genus, float lorieHeight,
			float ageBreastHeight, Coefficients quadMeanDiameterUtil, Coefficients closeUtilizationUtil,
			Coefficients closeUtilizationNetOfDecayUtil, Coefficients closeUtilizationNetOfDecayAndWasteUtil
	) throws ProcessingException {
		final var netDecayCoeMap = Utils.<Map<String, Coefficients>>expectParsedControl(
				controlMap, ControlKey.VOLUME_NET_DECAY_WASTE, Map.class
		);
		final var wasteModifierMap = Utils.<MatrixMap2<String, Region, Float>>expectParsedControl(
				controlMap, ControlKey.WASTE_MODIFIERS, MatrixMap2.class
		);

		estimateUtilization(
				closeUtilizationNetOfDecayUtil, closeUtilizationNetOfDecayAndWasteUtil, utilizationClass,
				(i, netDecay) -> {
					if (Float.isNaN(netDecay) || netDecay <= 0f) {
						return 0f;
					}

					Coefficients netWasteCoe = netDecayCoeMap.get(genus);
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

					float arg = a0 + a1 * frd + a3 * log(quadMeanDiameterUtil.getCoe(i.index)) + a4 * log(lorieHeight);

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
	 * Estimate utilization net of decay, waste, and breakage
	 *
	 * @throws ProcessingException
	 */
	public void estimateNetDecayWasteAndBreakageVolume(
			UtilizationClass utilizationClass, int breakageGroup, Coefficients quadMeanDiameterUtil,
			Coefficients closeUtilizationUtil, Coefficients closeUtilizationNetOfDecayAndWasteUtil,
			Coefficients closeUtilizationNetOfDecayWasteAndBreakageUtil
	) throws ProcessingException {
		final var netBreakageCoeMap = Utils
				.<Map<Integer, Coefficients>>expectParsedControl(controlMap, ControlKey.BREAKAGE, Map.class);
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
	 * Normalizes the utilization components 1-4 so they sum to the value of component UTIL_ALL
	 *
	 * @throws ProcessingException if the sum is not positive
	 */
	float normalizeUtilizationComponents(Coefficients components) throws ProcessingException {
		var sum = sumUtilizationComponents(components);
		var k = components.getCoe(UTIL_ALL) / sum;
		if (sum <= 0f) {
			throw new ProcessingException("Total volume " + sum + " was not positive.");
		}
		UTIL_CLASSES.forEach(uc -> components.setCoe(uc.index, components.getCoe(uc.index) * k));
		return k;
	}

	/**
	 * Sums the individual utilization components (1-4)
	 */
	float sumUtilizationComponents(Coefficients components) {
		return (float) UTIL_CLASSES.stream().mapToInt(x -> x.index).mapToDouble(components::getCoe).sum();
	}

	/**
	 * Sums the individual utilization components (1-4) and stores the results in coefficient UTIL_ALL
	 */
	float storeSumUtilizationComponents(Coefficients components) {
		var sum = sumUtilizationComponents(components);
		components.setCoe(UTIL_ALL, sum);
		return sum;
	}
}
