package ca.bc.gov.nrs.vdyp.model;

import java.text.MessageFormat;
import java.util.Objects;

import org.apache.commons.lang3.ObjectUtils;

public class GenusDistribution implements Comparable<GenusDistribution> {
	private final int index;
	private final GenusDefinition genus;
	private final float percentage;

	/**
	 * Construct a GenusDistribution.
	 *
	 * @param index      the genus's index.
	 * @param genus      the genus. Must not be null.
	 * @param percentage the distribution percentage. Must be non null and in the range 0 .. 100.
	 */
	public GenusDistribution(int index, GenusDefinition genus, Float percentage) {
		if (genus == null) {
			throw new IllegalArgumentException(MessageFormat.format("Genus for index {0} is missing", genus));
		}
		if (percentage == null || percentage < 0 || percentage > 100) {
			throw new IllegalArgumentException(
					MessageFormat
							.format("Percentage value {0} for index {1} must be between 0 and 100", index, percentage)
			);
		}

		this.index = index;
		this.genus = genus;
		this.percentage = percentage;
	}

	public int getIndex() {
		return index;
	}

	public GenusDefinition getGenus() {
		return genus;
	}

	public Float getPercentage() {
		return percentage;
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof GenusDistribution that) {
			return this.index == that.index && Objects.equals(this.genus, that.genus)
					&& this.percentage == that.percentage;
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return ( (index * 17) + genus.hashCode()) * 17 + Float.valueOf(percentage).hashCode();
	}

	@Override
	public int compareTo(GenusDistribution that) {
		if (that != null) {
			int cr = this.index - that.index;
			if (cr == 0) {
				cr = ObjectUtils.compare(this.genus, that.genus);
				if (cr == 0)
					cr = ObjectUtils.compare(this.percentage, that.percentage);
			}

			return cr;
		} else {
			return 1 /* null is always less than not null */;
		}
	}
}
