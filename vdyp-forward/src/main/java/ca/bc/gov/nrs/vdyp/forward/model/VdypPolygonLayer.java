package ca.bc.gov.nrs.vdyp.forward.model;

import java.util.Map;
import java.util.Optional;

import ca.bc.gov.nrs.vdyp.model.LayerType;
import ca.bc.gov.nrs.vdyp.model.UtilizationClass;

public class VdypPolygonLayer extends VdypEntity {

	// See IPSJF155.doc

	private final LayerType layerType; // LAYERG

	private final VdypPolygon parent;
	private final Map<Integer, VdypLayerSpecies> genera;
	private final Optional<Map<UtilizationClass, VdypSpeciesUtilization>> defaultUtilizationMap;

	public VdypPolygonLayer(
			LayerType layerType, VdypPolygon parent, Map<Integer, VdypLayerSpecies> genera,
			Optional<Map<UtilizationClass, VdypSpeciesUtilization>> defaultUtilizationMap
	) {
		this.layerType = layerType;
		this.parent = parent;
		this.genera = genera;
		this.defaultUtilizationMap = defaultUtilizationMap;
	}

	public LayerType getLayerType() {
		return layerType;
	}

	public VdypPolygon getParent() {
		return parent;
	}

	public Map<Integer, VdypLayerSpecies> getGenera() {
		return genera;
	}

	public Optional<Map<UtilizationClass, VdypSpeciesUtilization>> getDefaultUtilizationMap() {
		return defaultUtilizationMap;
	}

	@Override
	public String toString() {
		return layerType.toString();
	}
}
