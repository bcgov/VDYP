package ca.bc.gov.nrs.vdyp.si32.cfs;

import java.text.MessageFormat;
import java.util.Arrays;

public record CfsBiomassConversionCoefficientsDetails(boolean containsData, float[] parms) {
	@Override
	public String toString() {
		return MessageFormat.format("ContainsData: {0}; parms: {1}", containsData, Arrays.toString(parms));
	}

	@Override
	public int hashCode() {
		return Boolean.valueOf(containsData).hashCode() * 17 + Arrays.hashCode(parms);
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof CfsBiomassConversionCoefficientsDetails that) {
			if (this.containsData != that.containsData) {
				return false;
			}
			if (this.parms.length != that.parms.length) {
				return false;
			}
			for (int i = 0; i < this.parms.length; i++) {
				if (this.parms[i] != that.parms[i])
					return false;
			}

			return true;
		} else {
			return false;
		}
	}
}
