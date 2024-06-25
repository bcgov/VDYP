package ca.bc.gov.nrs.vdyp.model;

import java.text.MessageFormat;
import java.util.Objects;
import java.util.Optional;

import ca.bc.gov.nrs.vdyp.common.Utils;

public class GenusDefinition extends AliasedEntity implements Comparable<GenusDefinition> {

	private final Optional<Integer> preference;

	@SuppressWarnings("java:S2789")
	public GenusDefinition(String alias, Optional<Integer> preference, String name) {
		super(alias, name);

		if (preference == null) {
			throw new IllegalArgumentException(
					MessageFormat.format(
							"GenusDefinition preference may not be null; provide Optional.empty() instead (alias {0})",
							alias
					)
			);
		}
		this.preference = preference;
	}

	public Optional<Integer> getPreference() {
		return preference;
	}

	@Override
	public int hashCode() {
		return ( (this.getAlias().hashCode() * 17) + this.getName().hashCode()) * 17 + preference.orElse(0);
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof GenusDefinition that) {
			return Objects.equals(this.preference, that.preference) && super.equals(that);
		} else {
			return false;
		}
	}

	@Override
	public int compareTo(GenusDefinition that) {
		if (that != null) {
			int aliasCompareResult = this.getAlias().compareTo(that.getAlias());
			if (aliasCompareResult == 0) {
				int nameCompareResult = this.getName().compareTo(that.getName());
				if (nameCompareResult == 0) {
					return Utils.compareOptionals(this.preference, that.preference);
				} else {
					return nameCompareResult;
				}
			} else {
				return aliasCompareResult;
			}
		} else {
			return 1 /* null is always less than not null */;
		}
	}
}
