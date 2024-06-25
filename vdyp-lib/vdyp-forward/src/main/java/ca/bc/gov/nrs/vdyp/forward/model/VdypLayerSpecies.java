package ca.bc.gov.nrs.vdyp.forward.model;

import java.util.Map;
import java.util.Optional;

import ca.bc.gov.nrs.vdyp.model.LayerType;
import ca.bc.gov.nrs.vdyp.model.GenusDistributionSet;
import ca.bc.gov.nrs.vdyp.model.UtilizationClass;

public class VdypLayerSpecies {

	// See IPSJF155.doc

	private final VdypPolygonDescription polygonId; // POLYDESC
	private final LayerType layerType; // LAYERG
	private final int genusIndex; // ISP
	private final Optional<String> genus; // SP0
	private final GenusDistributionSet speciesDistributions; // SP64DISTL1
	private final float siteIndex; // SI
	private final float dominantHeight; // HD
	private final float ageTotal; // AGETOT
	private final float ageAtBreastHeight; // AGEBH
	private final float yearsToBreastHeight; // YTBH
	private final Optional<Boolean> isPrimary; // ISITESP
	private final Integer siteCurveNumber; // SCN

	// Set after construction
	private VdypPolygonLayer parent;
	Optional<Map<UtilizationClass, VdypSpeciesUtilization>> utilizations;

	public VdypLayerSpecies(
			VdypPolygonDescription polygonId, LayerType layerType, int genusIndex, Optional<String> genus,
			GenusDistributionSet speciesDistributions, float siteIndex, float dominantHeight, float ageTotal,
			float ageAtBreastHeight, float yearsToBreastHeight, Optional<Boolean> isPrimary, Integer siteCurveNumber
	) {
		this.polygonId = polygonId;
		this.layerType = layerType;
		this.genusIndex = genusIndex;
		this.genus = genus;
		this.speciesDistributions = speciesDistributions;
		this.siteIndex = siteIndex;
		this.dominantHeight = dominantHeight;

		// From VDYPGETS.FOR, lines 235 onwards
		if (Float.isNaN(ageAtBreastHeight)) {
			if (ageTotal > 0.0 && yearsToBreastHeight > 0.0) {
				ageAtBreastHeight = ageTotal - yearsToBreastHeight;
				if (ageAtBreastHeight < 0.0)
					ageAtBreastHeight = Float.NaN;
			}
		} else if (Float.isNaN(ageTotal)) {
			if (ageAtBreastHeight > 0.0 && yearsToBreastHeight > 0.0)
				ageTotal = ageAtBreastHeight + yearsToBreastHeight;
		} else if (Float.isNaN(yearsToBreastHeight) && ageAtBreastHeight > 0.0 && ageTotal > ageAtBreastHeight) {
			yearsToBreastHeight = ageTotal - ageAtBreastHeight;
		}

		this.ageTotal = ageTotal;
		this.ageAtBreastHeight = ageAtBreastHeight;
		this.yearsToBreastHeight = yearsToBreastHeight;

		this.isPrimary = isPrimary;
		this.siteCurveNumber = siteCurveNumber;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append(polygonId).append(' ').append(layerType).append(' ').append(genusIndex);

		return sb.toString();
	}

	public void setParent(VdypPolygonLayer parent) {
		this.parent = parent;
	}

	public void setUtilizations(Optional<Map<UtilizationClass, VdypSpeciesUtilization>> utilizations) {
		this.utilizations = utilizations;
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

	public GenusDistributionSet getSpeciesDistributions() {
		return speciesDistributions;
	}

	public float getSiteIndex() {
		return siteIndex;
	}

	public float getDominantHeight() {
		return dominantHeight;
	}

	public float getAgeTotal() {
		return ageTotal;
	}

	public float getAgeAtBreastHeight() {
		return ageAtBreastHeight;
	}

	public float getYearsToBreastHeight() {
		return yearsToBreastHeight;
	}

	public Optional<Boolean> getIsPrimary() {
		return isPrimary;
	}

	public Integer getSiteCurveNumber() {
		return siteCurveNumber;
	}

	public VdypPolygonLayer getParent() {
		return parent;
	}

	public Optional<Map<UtilizationClass, VdypSpeciesUtilization>> getUtilizations() {
		return utilizations;
	}
}
