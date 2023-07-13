package ca.bc.gov.nrs.vdyp.fip.model;

import java.util.Optional;

import ca.bc.gov.nrs.vdyp.model.Layer;

public class FipLayerPrimary extends FipLayer {

	static final String SITE_CURVE_NUMBER = "SITE_CURVE_NUMBER"; // SCN
	static final String STOCKING_CLASS = "STOCKING_CLASS"; // STK

	Optional<Integer> siteCurveNumber; // TODO Confirm if these should be required instead of optional if we know it's
										// a Primary layer.
	Optional<String> stockingClass;

	public FipLayerPrimary(
			String polygonIdentifier, float ageTotal, float height, float siteIndex, float crownClosure,
			String siteGenus, String siteSpecies, float yearsToBreastHeight, Optional<String> stockingClass,
			Optional<Integer> inventoryTypeGroup, Optional<Float> breastHeightAge, Optional<Integer> siteCurveNumber
	) {
		super(
				polygonIdentifier, Layer.PRIMARY, ageTotal, height, siteIndex, crownClosure, siteGenus, siteSpecies,
				yearsToBreastHeight, inventoryTypeGroup, breastHeightAge
		);
		this.siteCurveNumber = siteCurveNumber;
		this.stockingClass = stockingClass;
	}

	public Optional<Integer> getSiteCurveNumber() {
		return siteCurveNumber;
	}

	public void setSiteCurveNumber(Optional<Integer> siteCurveNumber) {
		this.siteCurveNumber = siteCurveNumber;
	}

	public Optional<String> getStockingClass() {
		return stockingClass;
	}

	public void setStockingClass(Optional<String> stockingClass) {
		this.stockingClass = stockingClass;
	}

}
