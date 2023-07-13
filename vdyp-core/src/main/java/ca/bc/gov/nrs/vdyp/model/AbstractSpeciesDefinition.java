package ca.bc.gov.nrs.vdyp.model;

public class AbstractSpeciesDefinition {

	final String alias;
	final String name;

	protected AbstractSpeciesDefinition(String alias, String name) {
		this.alias = alias;
		this.name = name;
	}

	/**
	 * @return The short alias for the species
	 */
	public String getAlias() {
		return alias;
	}

	/**
	 * @return The full, human readable name for the species
	 */
	public String getName() {
		return name;
	}
}
