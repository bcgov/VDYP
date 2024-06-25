package ca.bc.gov.nrs.vdyp.model;

import java.util.Objects;

public abstract class AliasedEntity {

	private final String alias;
	private final String name;

	protected AliasedEntity(String alias, String name) {
		this.alias = alias;
		this.name = name;
	}

	/**
	 * @return The short alias for the entity
	 */
	public String getAlias() {
		return alias;
	}

	/**
	 * @return The full, human readable name for the entity
	 */
	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return String.format("%s (%s)", getAlias(), getName());
	}

	@Override
	public int hashCode() {
		return alias.hashCode() * 17 + name.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof AliasedEntity that) {
			return Objects.equals(this.alias, that.alias) && Objects.equals(this.name, that.name);
		} else {
			return false;
		}
	}
}
