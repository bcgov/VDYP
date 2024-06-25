package ca.bc.gov.nrs.vdyp.forward.model;

import java.util.Optional;

import ca.bc.gov.nrs.vdyp.model.LayerType;
import ca.bc.gov.nrs.vdyp.model.UtilizationClass;

public class VdypSpeciesUtilization extends VdypEntity {

	// See IPSJF155.doc

	private final VdypPolygonDescription polygonId; // POLYDESC
	private final LayerType layerType; // LAYERG
	private final int genusIndex; // ISP
	private final Optional<String> genus; // SP0
	private final UtilizationClass ucIndex; // J - utilization index
	private final float basalArea;
	private final float liveTreesPerHectare;
	private final float loreyHeight;
	private final float wholeStemVolume;
	private final float closeUtilizationVolume;
	private final float cuVolumeMinusDecay;
	private final float cuVolumeMinusDecayWastage;
	private final float cuVolumeMinusDecayWastageBreakage;
	private final float quadraticMeanDiameterAtBH;

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
		this.cuVolumeMinusDecay = cuVolumeMinusDecay;
		this.cuVolumeMinusDecayWastage = cuVolumeMinusDecayWastage;
		this.cuVolumeMinusDecayWastageBreakage = cuVolumeMinusDecayWastageBreakage;
		this.quadraticMeanDiameterAtBH = quadraticMeanDiameterAtBH;
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
}
