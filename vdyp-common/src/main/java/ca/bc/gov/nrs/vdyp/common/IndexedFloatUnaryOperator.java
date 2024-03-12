package ca.bc.gov.nrs.vdyp.common;

/**
 * Applies a floating point operator for a particular indexed location in a sequence
 *
 * @author Kevin Smith, Vivid Solutions
 *
 */
@FunctionalInterface
public interface IndexedFloatUnaryOperator {
	/**
	 * Applies this operator to the given operand.
	 *
	 * @param value the operand
	 * @param index the index
	 * @return the operator result
	 */
	float applyAsFloatWithIndex(float value, int index);
}
