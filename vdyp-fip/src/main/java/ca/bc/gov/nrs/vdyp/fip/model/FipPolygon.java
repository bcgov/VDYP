package ca.bc.gov.nrs.vdyp.fip.model;

import java.util.Optional;

public class FipPolygon {

	String polygonIdentifier; // POLYDESC
	String forestInventoryZone; // FIZ
	String biogeoclimaticZone; // BEC
	Optional<Float> percentAvailable; // PCTFLAND
	Optional<FipMode> modeFip; // MODEfip
	Optional<String> nonproductiveDescription; // NPDESC
	float yieldFactor; // YLDFACT

	public FipPolygon(
			String polygonIdentifier, String fiz, String becIdentifier, Optional<Float> percentAvailable,
			Optional<FipMode> modeFip, Optional<String> nonproductiveDescription, float yieldFactor
	) {
		super();
		this.polygonIdentifier = polygonIdentifier;
		this.forestInventoryZone = fiz;
		this.biogeoclimaticZone = becIdentifier;
		this.percentAvailable = percentAvailable;
		this.modeFip = modeFip;
		this.nonproductiveDescription = nonproductiveDescription;
		this.yieldFactor = yieldFactor;
	}

	public String getPolygonIdentifier() {
		return polygonIdentifier;
	}

	public void setPolygonIdentifier(String polygonIdentifier) {
		this.polygonIdentifier = polygonIdentifier;
	}

	public String getForestInventoryZone() {
		return forestInventoryZone;
	}

	public void setForestInventoryZone(String forestInventoryZone) {
		this.forestInventoryZone = forestInventoryZone;
	}

	public String getBiogeoclimaticZone() {
		return biogeoclimaticZone;
	}

	public void setBiogeoclimaticZone(String biogeoclimaticZone) {
		this.biogeoclimaticZone = biogeoclimaticZone;
	}

	public Optional<Float> getPercentAvailable() {
		return percentAvailable;
	}

	public void setPercentAvailable(Optional<Float> percentAvailable) {
		this.percentAvailable = percentAvailable;
	}

	public Optional<FipMode> getModeFip() {
		return modeFip;
	}

	public void setModeFip(Optional<FipMode> modeFip) {
		this.modeFip = modeFip;
	}

	public Optional<String> getNonproductiveDescription() {
		return nonproductiveDescription;
	}

	public void setNonproductiveDescription(Optional<String> nonproductiveDescription) {
		this.nonproductiveDescription = nonproductiveDescription;
	}

	public float getYieldFactor() {
		return yieldFactor;
	}

	public void setYieldFactor(float yieldFactor) {
		this.yieldFactor = yieldFactor;
	}

}
