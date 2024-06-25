package ca.bc.gov.nrs.vdyp.forward.model;

public class VdypGrowthDetails {

	private static final int N_VTROL_ELEMENTS = 10;

	private Integer firstYear, currentYear, lastYear, yearCounter;
	private final int[] vtrol = new int[10];

	public VdypGrowthDetails(Integer[] integers) {
		int index = 0;

		if (integers != null) {
			for (; index < Math.min(integers.length, N_VTROL_ELEMENTS); index++)
				this.vtrol[index] = integers[index];
		}

		for (; index < N_VTROL_ELEMENTS; index++)
			this.vtrol[index] = 0;
	}

	public Integer getFirstYear() {
		return firstYear;
	}

	public void setFirstYear(Integer firstYear) {
		this.firstYear = firstYear;
	}

	public Integer getCurrentYear() {
		return currentYear;
	}

	public void setCurrentYear(Integer currentYear) {
		this.currentYear = currentYear;
	}

	public Integer getLastYear() {
		return lastYear;
	}

	public void setLastYear(Integer lastYear) {
		this.lastYear = lastYear;
	}

	public Integer getYearCounter() {
		return yearCounter;
	}

	public void setYearCounter(Integer yearCounter) {
		this.yearCounter = yearCounter;
	}

	public int getVtrol(int elementNumber) {

		int index = elementNumber - 1;
		if (index < 0 || index > N_VTROL_ELEMENTS) {
			throw new IllegalArgumentException(
					"Element number (" + elementNumber + ") is out of range - must be from 1 to " + N_VTROL_ELEMENTS
			);
		}

		return vtrol[index];
	}
}
