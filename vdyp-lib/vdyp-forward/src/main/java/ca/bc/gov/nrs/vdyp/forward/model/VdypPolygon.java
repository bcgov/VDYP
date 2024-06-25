package ca.bc.gov.nrs.vdyp.forward.model;

import java.util.Optional;

import ca.bc.gov.nrs.vdyp.model.BecDefinition;

public class VdypPolygon extends VdypEntity {

	// See IPSJF155.doc

	private final VdypPolygonDescription description; // POLYDESC
	private final BecDefinition biogeoclimaticZone; // BEC
	private final Character forestInventoryZone; // FIZ
	private final float percentForestLand; // PCTFLAND
	private final Optional<Integer> inventoryTypeGroup; // ITG
	private final Optional<Integer> basalAreaGroup; // GRPBA1
	private final Optional<FipMode> fipMode; // MODE

	// Set after construction
	private VdypPolygonLayer primaryLayer;
	private Optional<VdypPolygonLayer> veteranLayer;

	public VdypPolygon(
			VdypPolygonDescription vdypPolygonDescription, BecDefinition bec, Character fizId, float percentForestLand,
			Optional<Integer> inventoryTypeGroup, Optional<Integer> basalAreaGroup, Optional<FipMode> fipMode
	) {
		this.description = vdypPolygonDescription;
		this.biogeoclimaticZone = bec;
		this.forestInventoryZone = fizId;

		// VDYPGETP lines 146-154
		if (percentForestLand <= 0.0)
			this.percentForestLand = 90.0f;
		else
			this.percentForestLand = percentForestLand;

		this.inventoryTypeGroup = inventoryTypeGroup;
		this.basalAreaGroup = basalAreaGroup;
		this.fipMode = fipMode;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append(description.getName()).append('(').append(description.getYear()).append(')');

		return sb.toString();
	}

	public void setPrimaryLayer(VdypPolygonLayer primaryLayer) {
		this.primaryLayer = primaryLayer;
	}

	public void setVeteranLayer(Optional<VdypPolygonLayer> veteranLayer) {
		this.veteranLayer = veteranLayer;
	}

	public VdypPolygonDescription getDescription() {
		return description;
	}

	public String getName() {
		return description.getName();
	}

	public Integer getYear() {
		return description.getYear();
	}

	public BecDefinition getBiogeoclimaticZone() {
		return biogeoclimaticZone;
	}

	public Character getForestInventoryZone() {
		return forestInventoryZone;
	}

	public float getPercentForestLand() {
		return percentForestLand;
	}

	public Optional<Integer> getInventoryTypeGroup() {
		return inventoryTypeGroup;
	}

	public Optional<Integer> getBasalAreaGroup() {
		return basalAreaGroup;
	}

	public Optional<FipMode> getFipMode() {
		return fipMode;
	}

	public VdypPolygonLayer getPrimaryLayer() {
		return primaryLayer;
	}

	public Optional<VdypPolygonLayer> getVeteranLayer() {
		return veteranLayer;
	}
}
