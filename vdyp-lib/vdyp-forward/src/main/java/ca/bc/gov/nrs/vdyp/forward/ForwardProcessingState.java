package ca.bc.gov.nrs.vdyp.forward;

import java.text.MessageFormat;

import ca.bc.gov.nrs.vdyp.common.GenusDefinitionMap;
import ca.bc.gov.nrs.vdyp.forward.model.VdypPolygon;
import ca.bc.gov.nrs.vdyp.model.LayerType;

class ForwardProcessingState {

	/** An "instance" is the processing state of one polygon */
	private static final int MAX_INSTANCES = 3;

	/**
	 * We allocate one record for the Primary Layer and one record for the Veteran Layer of each instance.
	 */
	private static final int MAX_RECORDS = 2 * MAX_INSTANCES;

	private final GenusDefinitionMap genusDefinitionMap;
	private final PolygonProcessingState[] banks;
	private PolygonProcessingState active;

	public ForwardProcessingState(GenusDefinitionMap genusDefinitionMap) {

		this.genusDefinitionMap = genusDefinitionMap;
		banks = new PolygonProcessingState[MAX_RECORDS];
	}

	public void setStartingState(VdypPolygon polygon) {
		// Move the primary layer of the given polygon to bank zero.
		banks[0] = new PolygonProcessingState(genusDefinitionMap, polygon.getPrimaryLayer());
	}

	public void setActive(LayerType layerType, int instanceNumber) {
		active = banks[toIndex(layerType, instanceNumber)].copy();
	}

	public void storeActive(LayerType layerType, int instanceNumber) {
		banks[toIndex(layerType, instanceNumber)] = active.copy();
	}

	public void transfer(LayerType layerType, int fromInstanceNumber, int toInstanceNumber) {
		banks[toIndex(layerType, toInstanceNumber)] = banks[toIndex(layerType, fromInstanceNumber)].copy();
	}

	public PolygonProcessingState getActive() {
		return active;
	}

	public PolygonProcessingState getBank(LayerType layerType, int instanceNumber) {
		return banks[toIndex(layerType, instanceNumber)];
	}

	private static int toIndex(LayerType layerType, int instanceNumber) {
		return toLayerIndex(layerType) * MAX_INSTANCES + instanceNumber;
	}

	private static int toLayerIndex(LayerType layerType) {
		switch (layerType) {
		case PRIMARY:
			return 0;
		case VETERAN:
			return 1;
		default:
			throw new IllegalStateException(MessageFormat.format("Unsupported LayerType {0}", layerType));
		}
	}
}