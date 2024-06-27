package ca.bc.gov.nrs.vdyp.forward.model;

import java.text.MessageFormat;
import java.util.Optional;

import ca.bc.gov.nrs.vdyp.application.ProcessingException;
import ca.bc.gov.nrs.vdyp.forward.ForwardProcessingEngine;
import ca.bc.gov.nrs.vdyp.math.FloatMath;
import ca.bc.gov.nrs.vdyp.model.LayerType;
import ca.bc.gov.nrs.vdyp.model.UtilizationClass;

public class VdypSpeciesUtilization extends VdypEntity {

	// See IPSJF155.doc

	private VdypPolygonDescription polygonId; // POLYDESC
	private final LayerType layerType; // LAYERG
	private final int genusIndex; // ISP
	private final Optional<String> genus; // SP0
	private final UtilizationClass ucIndex; // J - utilization index

	// The following are not final because post construction the values
	// may be scaled by the scale method below.

	private float basalArea;
	private float liveTreesPerHectare;
	private float loreyHeight;
	private float wholeStemVolume;
	private float closeUtilizationVolume;
	private float cuVolumeMinusDecay;
	private float cuVolumeMinusDecayWastage;
	private float cuVolumeMinusDecayWastageBreakage;
	private float quadraticMeanDiameterAtBH;

	// Set after construction
	private VdypLayerSpecies parent;

	public VdypSpeciesUtilization(
			VdypPolygonDescription polygonId, LayerType layerType, Integer genusIndex, Optional<String> genus,
			UtilizationClass ucIndex, float basalArea, float liveTreesPerHectare, float loreyHeight,
			float wholeStemVolume, float closeUtilizationVolume, float cuVolumeMinusDecay,
			float cuVolumeMinusDecayWastage, float cuVolumeMinusDecayWastageBreakage, float quadraticMeanDiameterAtBH
	) {
		this.polygonId = polygonId;
		this.layerType = layerType;
		this.genusIndex = genusIndex;
		this.genus = genus;
		this.ucIndex = ucIndex;
		this.basalArea = basalArea;
		this.liveTreesPerHectare = liveTreesPerHectare;
		this.loreyHeight = loreyHeight;
		this.wholeStemVolume = wholeStemVolume;
		this.closeUtilizationVolume = closeUtilizationVolume;
		this.quadraticMeanDiameterAtBH = quadraticMeanDiameterAtBH;
		this.cuVolumeMinusDecay = cuVolumeMinusDecay;
		this.cuVolumeMinusDecayWastage = cuVolumeMinusDecayWastage;
		this.cuVolumeMinusDecayWastageBreakage = cuVolumeMinusDecayWastageBreakage;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append(polygonId).append(' ').append(layerType).append(' ').append(genusIndex).append(' ')
				.append(ucIndex.index);

		return sb.toString();
	}

	public void setParent(VdypLayerSpecies parent) {
		this.parent = parent;
	}

	public VdypPolygonDescription getPolygonId() {
		return polygonId;
	}

	public LayerType getLayerType() {
		return layerType;
	}

	public Integer getGenusIndex() {
		return genusIndex;
	}

	public Optional<String> getGenus() {
		return genus;
	}

	public UtilizationClass getUcIndex() {
		return ucIndex;
	}

	public float getBasalArea() {
		return basalArea;
	}

	public float getLiveTreesPerHectare() {
		return liveTreesPerHectare;
	}

	public float getLoreyHeight() {
		return loreyHeight;
	}

	public float getWholeStemVolume() {
		return wholeStemVolume;
	}

	public float getCloseUtilizationVolume() {
		return closeUtilizationVolume;
	}

	public float getCuVolumeMinusDecay() {
		return cuVolumeMinusDecay;
	}

	public float getCuVolumeMinusDecayWastage() {
		return cuVolumeMinusDecayWastage;
	}

	public float getCuVolumeMinusDecayWastageBreakage() {
		return cuVolumeMinusDecayWastageBreakage;
	}

	public float getQuadraticMeanDiameterAtBH() {
		return quadraticMeanDiameterAtBH;
	}

	public VdypLayerSpecies getParent() {
		return parent;
	}

	/**
	 * Implements VDYPGETU lines 224 - 229, in which the utilization- per-hectare values are scaled by the given factor
	 * - the % coverage of the primary layer.
	 *
	 * @param scalingFactor
	 */
	public void scale(float scalingFactor) {

		if (this.basalArea > 0) {
			this.basalArea *= scalingFactor;
		}
		if (this.liveTreesPerHectare > 0) {
			this.liveTreesPerHectare *= scalingFactor;
		}
		// lorey height is not a per-hectare value and therefore
		// is excluded from scaling.
		if (this.wholeStemVolume > 0) {
			this.wholeStemVolume *= scalingFactor;
		}
		if (this.closeUtilizationVolume > 0) {
			this.closeUtilizationVolume *= scalingFactor;
		}
		if (this.cuVolumeMinusDecay > 0) {
			this.cuVolumeMinusDecay *= scalingFactor;
		}
		if (this.cuVolumeMinusDecayWastage > 0) {
			this.cuVolumeMinusDecayWastage *= scalingFactor;
		}
		if (this.cuVolumeMinusDecayWastageBreakage > 0) {
			this.cuVolumeMinusDecayWastageBreakage *= scalingFactor;
		}
		// quadratic mean diameter is not a per-hectare value and
		// therefore not scaled.
	}

	private static final float MAX_ACCEPTABLE_BASAL_AREA_ERROR = 0.1f;
	private static final float[] CLASS_LOWER_BOUNDS = { 4.0f, 7.5f, 7.5f, 12.5f, 17.5f, 22.5f };
	private static final float[] CLASS_UPPER_BOUNDS = { 7.5f, 2000.0f, 12.5f, 17.5f, 22.5f, 2000.0f };
	private static final float DQ_EPS = 0.005f;

	/**
	 * Implements the logic in BANKIN2 (ICHECK == 2) adjusting the utilization values according to various rules.
	 *
	 * @throws ProcessingException when calculated values are out of range
	 */
	public void doPostCreateAdjustments() throws ProcessingException {

		resetOnMissingValues();

		adjustBasalAreaToMatchTreesPerHectare();

		doCalculateQuadMeanDiameter();
	}

	/**
	 * If either basalArea or liveTreesPerHectare is not positive, clear everything.
	 */
	private void resetOnMissingValues() {

		if (this.basalArea <= 0.0f || this.liveTreesPerHectare <= 0.0f) {
			this.basalArea = 0.0f;
			this.liveTreesPerHectare = 0.0f;
			// DO NOT zero-out the lorey height value.
			this.wholeStemVolume = 0.0f;
			this.closeUtilizationVolume = 0.0f;
			this.cuVolumeMinusDecay = 0.0f;
			this.cuVolumeMinusDecayWastage = 0.0f;
			this.cuVolumeMinusDecayWastageBreakage = 0.0f;
		}
	}

	/**
	 * Adjust Basal Area to match the Trees-Per-Hectare value.
	 *
	 * @throws ProcessingException
	 */
	private void adjustBasalAreaToMatchTreesPerHectare() throws ProcessingException {

		if (this.liveTreesPerHectare > 0.0f) {
			float basalAreaLowerBound = ForwardProcessingEngine
					.calculateBasalArea(CLASS_LOWER_BOUNDS[this.ucIndex.ordinal()] + DQ_EPS, this.liveTreesPerHectare);
			float basalAreaUpperBound = ForwardProcessingEngine
					.calculateBasalArea(CLASS_UPPER_BOUNDS[this.ucIndex.ordinal()] - DQ_EPS, this.liveTreesPerHectare);

			float basalAreaError = 0.0f;
			String message = null;

			if (this.basalArea < basalAreaLowerBound) {
				basalAreaError = FloatMath.abs(this.basalArea - basalAreaLowerBound);
				this.basalArea = basalAreaLowerBound;
				message = MessageFormat.format(
						"{0}: Error 6: basal area {1} is {2} below threshold, exceeding the maximum error {3}", this,
						this.basalArea, basalAreaError, MAX_ACCEPTABLE_BASAL_AREA_ERROR
				);
			} else if (this.basalArea > basalAreaUpperBound) {
				basalAreaError = FloatMath.abs(this.basalArea - basalAreaUpperBound);
				message = MessageFormat.format(
						"{0}: Error 6: basal area {1} is {2} above threshold, exceeding the maximum error {3}", this,
						this.basalArea, basalAreaError, MAX_ACCEPTABLE_BASAL_AREA_ERROR
				);
				this.basalArea = basalAreaUpperBound;
			}

			if (basalAreaError > MAX_ACCEPTABLE_BASAL_AREA_ERROR) {
				throw new ProcessingException(message);
			}
		}

	}

	/**
	 * Calculate QuadMeanDiameter for the given utilization.
	 *
	 * The value supplied in the input is IGNORED REPEAT IGNORED
	 *
	 * @throws ProcessingException
	 */
	private void doCalculateQuadMeanDiameter() throws ProcessingException {

		if (this.basalArea > 0.0f) {
			float qmd = ForwardProcessingEngine.calculateQuadMeanDiameter(basalArea, liveTreesPerHectare);

			if (qmd < CLASS_LOWER_BOUNDS[this.ucIndex.ordinal()]) {
				qmd = qmd + DQ_EPS;
				if (qmd /* is still */ < CLASS_LOWER_BOUNDS[this.ucIndex.ordinal()]) {
					throw new ProcessingException(
							MessageFormat.format(
									"{0}: Error 6: calculated quad-mean-diameter value {1} is below lower limit {2}",
									this, qmd, CLASS_LOWER_BOUNDS[this.ucIndex.ordinal()]
							)
					);
				}
			} else if (qmd > CLASS_UPPER_BOUNDS[this.ucIndex.ordinal()]) {
				qmd = qmd - DQ_EPS;
				if (qmd /* is still */ > CLASS_UPPER_BOUNDS[this.ucIndex.ordinal()]) {
					throw new ProcessingException(
							MessageFormat.format(
									"{0}: Error 6: calculated quad-mean-diameter value {1} is above upper limit {2}",
									this, qmd, CLASS_UPPER_BOUNDS[this.ucIndex.ordinal()]
							)
					);
				}
			}

			this.quadraticMeanDiameterAtBH = qmd;
		}
	}
}
