package ca.bc.gov.nrs.vdyp.fip.model;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import ca.bc.gov.nrs.vdyp.model.Layer;

public class FipLayer {

	static final String POLYGON_IDENTIFIER = "POLYGON_IDENTIFIER"; // POLYDESC
	static final String LAYER = "LAYER"; // LAYER
	static final String AGE_TOTAL = "AGE_TOTAL"; // AGETOT
	static final String HEIGHT = "HEIGHT"; // HT
	static final String SITE_INDEX = "SITE_INDEX"; // SI
	static final String CROWN_CLOSURE = "CROWN_CLOSURE"; // CC
	static final String SITE_SP0 = "SITE_SP0"; // SITESP0
	static final String SITE_SP64 = "SITE_SP64"; // SITESP64
	static final String YEARS_TO_BREAST_HEIGHT = "YEARS_TO_BREAST_HEIGHT"; // YTBH
	static final String INVENTORY_TYPE_GROUP = "INVENTORY_TYPE_GROUP"; // ITGFIP
	static final String BREAST_HEIGHT_AGE = "BREAST_HEIGHT_AGE"; // AGEBH

	String polygonIdentifier; // POLYDESC
	final Layer layer;
	float ageTotal;
	float height;
	float siteIndex;
	float crownClosure;
	String siteGenus;
	String siteSpecies;
	float yearsToBreastHeight;
	Optional<Integer> inventoryTypeGroup;
	Optional<Float> breastHeightAge;

	Map<String, FipSpecies> species = Collections.emptyMap();

	public FipLayer(
			String polygonIdentifier, Layer layer, float ageTotal, float height, float siteIndex, float crownClosure,
			String siteGenus, String siteSpecies, float yearsToBreastHeight, Optional<Integer> inventoryTypeGroup,
			Optional<Float> breastHeightAge
	) {
		super();
		this.polygonIdentifier = polygonIdentifier;
		this.layer = layer;
		this.ageTotal = ageTotal;
		this.height = height;
		this.siteIndex = siteIndex;
		this.crownClosure = crownClosure;
		this.siteGenus = siteGenus;
		this.siteSpecies = siteSpecies;
		this.yearsToBreastHeight = yearsToBreastHeight;
		this.inventoryTypeGroup = inventoryTypeGroup;
		this.breastHeightAge = breastHeightAge;
	}

	public String getPolygonIdentifier() {
		return polygonIdentifier;
	}

	public Layer getLayer() {
		return layer;
	}

	public float getAgeTotal() {
		return ageTotal;
	}

	public float getHeight() {
		return height;
	}

	public float getSiteIndex() {
		return siteIndex;
	}

	public float getCrownClosure() {
		return crownClosure;
	}

	public String getSiteSp0() {
		return siteGenus;
	}

	public String getSiteSp64() {
		return siteSpecies;
	}

	public float getYearsToBreastHeight() {
		return yearsToBreastHeight;
	}

	public Optional<Integer> getInventoryTypeGroup() {
		return inventoryTypeGroup;
	}

	public Optional<Float> getBreastHeightAge() {
		return breastHeightAge;
	}

	public void setPolygonIdentifier(String polygonIdentifier) {
		this.polygonIdentifier = polygonIdentifier;
	}

	public void setAgeTotal(float ageTotal) {
		this.ageTotal = ageTotal;
	}

	public void setHeight(float height) {
		this.height = height;
	}

	public void setSiteIndex(float siteIndex) {
		this.siteIndex = siteIndex;
	}

	public void setCrownClosure(float crownClosure) {
		this.crownClosure = crownClosure;
	}

	public void setSiteSp0(String sireSp0) {
		this.siteGenus = sireSp0;
	}

	public void setSiteSp64(String siteSp64) {
		this.siteSpecies = siteSp64;
	}

	public void setYearsToBreastHeight(float yearsToBreastHeight) {
		this.yearsToBreastHeight = yearsToBreastHeight;
	}

	public void setInventoryTypeGroup(Optional<Integer> inventoryTypeGroup) {
		this.inventoryTypeGroup = inventoryTypeGroup;
	}

	public void setBreastHeightAge(Optional<Float> breastHeightAge) {
		this.breastHeightAge = breastHeightAge;
	}

	public Map<String, FipSpecies> getSpecies() {
		return species;
	}

	public void setSpecies(Map<String, FipSpecies> species) {
		this.species = species;
	}

}
