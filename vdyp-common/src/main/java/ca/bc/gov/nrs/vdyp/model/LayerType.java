package ca.bc.gov.nrs.vdyp.model;

import java.util.Collections;
import java.util.List;

public enum LayerType {
	/**
	 * The primary layer
	 */
	PRIMARY("P"),

	/**
	 * The parser is aware of this but it is never implemented
	 */
	SECONDARY("S"),

	/**
	 * An older layer than the primary layer, also called the "overstory"
	 */
	VETERAN("V");

	public static final List<LayerType> ALL_USED = Collections.unmodifiableList(List.of(PRIMARY, VETERAN));

	private final String alias;

	private LayerType(String alias) {
		this.alias = alias;
	}

	public String getAlias() {
		return alias;
	}
}
