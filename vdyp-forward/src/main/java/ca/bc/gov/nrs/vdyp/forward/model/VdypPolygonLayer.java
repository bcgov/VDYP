package ca.bc.gov.nrs.vdyp.forward.model;

import java.util.Map;

import ca.bc.gov.nrs.vdyp.model.GenusDefinition;
import ca.bc.gov.nrs.vdyp.model.LayerType;

public class VdypPolygonLayer {

	// See IPSJF155.doc

	private final LayerType layerType; // LAYERG

	// Set after construction
	private VdypPolygon parent;
	private Map<GenusDefinition, VdypLayerSpecies> genus;

	public VdypPolygonLayer(LayerType layerType, VdypPolygon parent) {
		super();
		this.layerType = layerType;
		this.parent = parent;
	}

	LayerType getLayerType() {
		return layerType;
	}

	VdypPolygon getParent() {
		return parent;
	}

	public void setParent(VdypPolygon parent) {
		this.parent = parent;
	}

	public Map<GenusDefinition, VdypLayerSpecies> getGenus() {
		return genus;
	}

	public void setGenus(Map<GenusDefinition, VdypLayerSpecies> genus) {
		this.genus = genus;
	}
}
