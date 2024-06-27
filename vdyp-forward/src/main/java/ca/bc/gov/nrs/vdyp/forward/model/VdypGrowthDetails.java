package ca.bc.gov.nrs.vdyp.forward.model;

import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;

/**
 * Control Variable values
 * <p>
 * 1st: Grow target
 * <ul>
 * <li>0: NO GROWTH
 * <li>-1: SEQ014 (specifies end yr, each poly)
 * <li>1 to 400: grow i yrs
 * <li>1920 to 2400: grow to yr i (A.D.)
 * </ul>
 * 2nd: Compatibility Variable output
 * <ul>
 * <li>0 None
 * <li>1 First yr only
 * <li>2 All yrs
 * <li>Note: Output can occur ONLY in yrs also selected by 3rd option
 * </ul>
 * 3rd: Compatibility Variable application
 * <ul>
 * <li>0: Do not apply
 * <li>1: All variables except Volume
 * <li>2: All variables (standard usage)
 * </ul>
 * 4th: Output Files
 * <ul>
 * <li>0: None
 * <li>1: First year
 * <li>2: First and last year
 * <li>3: All years
 * <li>4: First, 1st + 10, 1st + 20, ..., last
 * </ul>
 * 5th: Allow Compatibility Variable computations
 * <ul>
 * <li>0: Always
 * <li>1: Only when basis exceeds limits (.1 for vol, .01 BA for DQ)
 * </ul>
 * 6th: Update Site species and ITG during growth
 * <ul>
 * <li>0: No
 * <li>1: Yes (normal)
 * </ul>
 */
public class VdypGrowthDetails {

	private static final int MAX_CONTROL_VARIABLE_VALUES = 10;

	private enum ControlVariables {
		GROW_TARGET, COMPATIBILITY_VARIABLE_OUTPUT, COMPATIBILITY_VARIABLE_APPLICATION, OUTPUT_FILES,
		ALLOW_COMPATIBILITY_VARIABLE_CALCULATIONS, UPDATE_DURING_GROWTH
	}

	private Integer firstYear, currentYear, lastYear, yearCounter;

	private final int[] controlVariables = new int[10];

	public VdypGrowthDetails(Integer[] controlVariableValues) {
		int index = 0;

		if (controlVariableValues != null) {
			for (; index < Math.min(controlVariableValues.length, MAX_CONTROL_VARIABLE_VALUES); index++)
				this.controlVariables[index] = controlVariableValues[index];
		}

		for (; index < MAX_CONTROL_VARIABLE_VALUES; index++)
			this.controlVariables[index] = 0;
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

	public boolean allowCalculation(float value, float limit, BiFunction<Float, Float, Boolean> p) {
		int cvValue = controlVariables[ControlVariables.ALLOW_COMPATIBILITY_VARIABLE_CALCULATIONS.ordinal()];
		return cvValue == 0 && value > 0 || cvValue == 1 && p.apply(value, limit);
	}

	public boolean allowCalculation(BooleanSupplier p) {
		int cvValue = controlVariables[ControlVariables.ALLOW_COMPATIBILITY_VARIABLE_CALCULATIONS.ordinal()];
		return cvValue == 1 && p.getAsBoolean();
	}

	int getControlVariable(int elementNumber) {

		int index = elementNumber - 1;
		if (index < 0 || index > MAX_CONTROL_VARIABLE_VALUES) {
			throw new IllegalArgumentException(
					"Element number (" + elementNumber + ") is out of range - must be from 1 to "
							+ MAX_CONTROL_VARIABLE_VALUES
			);
		}

		return controlVariables[index];
	}
}
