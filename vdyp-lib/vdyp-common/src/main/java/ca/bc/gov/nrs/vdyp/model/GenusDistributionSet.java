package ca.bc.gov.nrs.vdyp.model;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import ca.bc.gov.nrs.vdyp.io.parse.common.InvalidGenusDistributionSet;

public class GenusDistributionSet implements Comparable<GenusDistributionSet> {

	private Map<Integer, GenusDistribution> genusDistributionMap = new HashMap<>();
	private int maxIndex;

	public GenusDistributionSet(int maxIndex, List<GenusDistribution> sdList) {

		try {
			validate(maxIndex, sdList);
		} catch (InvalidGenusDistributionSet e) {
			throw new IllegalArgumentException(e);
		}

		for (GenusDistribution sd : sdList) {
			genusDistributionMap.put(sd.getIndex(), sd);
		}

		this.maxIndex = maxIndex;
	}

	private GenusDistributionSet(GenusDistributionSet other) {
		genusDistributionMap = new HashMap<>();

		for (var e : other.genusDistributionMap.entrySet()) {
			genusDistributionMap.put(
					e.getKey(),
					new GenusDistribution(
							e.getValue().getIndex(), e.getValue().getGenus(), e.getValue().getPercentage()
					)
			);
		}
	}

	public Map<Integer, GenusDistribution> getSpeciesDistributionMap() {
		return Collections.unmodifiableMap(genusDistributionMap);
	}

	public Optional<GenusDistribution> getSpeciesDistribution(int index) {
		if (index > maxIndex) {
			throw new IllegalArgumentException(
					MessageFormat.format("Index {0} exceeds the maximum value {1}", index, maxIndex)
			);
		}
		if (genusDistributionMap.containsKey(index))
			return Optional.of(genusDistributionMap.get(index));
		else
			return Optional.empty();
	}

	public GenusDistributionSet copy() {
		return new GenusDistributionSet(this);
	}

	public static void validate(int maxIndex, List<GenusDistribution> gdList) throws InvalidGenusDistributionSet {
		if (gdList == null || gdList.size() == 0) {
			throw new InvalidGenusDistributionSet("GenusDistributionSet does not have the required first entry");
		}

		Set<GenusDefinition> generaSeen = new HashSet<>();
		Set<Integer> indicesSeen = new HashSet<>();

		float distributionTotal = 0.0f;

		for (GenusDistribution gd : gdList) {
			if (generaSeen.contains(gd.getGenus())) {
				throw new InvalidGenusDistributionSet(
						MessageFormat
								.format("Species {0} appears more than once in GenusDistributionSet", gd.getGenus())
				);
			}
			if (indicesSeen.contains(gd.getIndex())) {
				throw new InvalidGenusDistributionSet(
						MessageFormat.format("Index {0} appears more than once in GenusDistributionSet", gd.getIndex())
				);
			}
			if (gd.getIndex() < 0 || gd.getIndex() > maxIndex) {
				throw new InvalidGenusDistributionSet(
						MessageFormat.format(
								"Index {0} is out of range - acceptable values are between 0 and {1}, inclusive",
								gd.getIndex(), maxIndex
						)
				);
			}
			generaSeen.add(gd.getGenus());
			indicesSeen.add(gd.getIndex());
			distributionTotal += gd.getPercentage();
		}

		if (distributionTotal > 100.05f || distributionTotal < 99.95) {
			throw new InvalidGenusDistributionSet(
					MessageFormat
							.format("Sum {0} of species distributions is not within 0.05 of 100.0", distributionTotal)
			);
		}
	}

	@Override
	public int hashCode() {
		return genusDistributionMap.hashCode() * 17 + maxIndex;
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof GenusDistributionSet that) {
			return compareTo(that) == 0;
		} else {
			return false;
		}
	}

	@Override
	public int compareTo(GenusDistributionSet that) {
		if (that != null) {
			if (that.genusDistributionMap.size() != genusDistributionMap.size()) {
				return genusDistributionMap.size() - that.genusDistributionMap.size();
			}

			for (GenusDistribution sd : this.genusDistributionMap.values()) {
				if (that.genusDistributionMap.containsKey(sd.getIndex())) {
					var result = sd.compareTo(that.genusDistributionMap.get(sd.getIndex()));
					if (result != 0) {
						return result;
					}
				}
			}

			return 0;
		} else {
			// null is less than non-null, per ObjectUtils.compare()
			return 1;
		}
	}
}