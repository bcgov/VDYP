package ca.bc.gov.nrs.vdyp.common;

/**
 * See {@link java.util.function.DoubleBinaryOperator}
 *
 * @author Kevin Smith, Vivid Solutions
 *
 */
@FunctionalInterface
public interface FloatBinaryOperator extends IndexedFloatBinaryOperator {
	/**
	 * Applies this operator to the given operands.
	 *
	 * @param left  the first operand
	 * @param right the second operand
	 * @return the operator result
	 */
	float applyAsFloat(float left, float right);

	@Override
	default float applyAsFloatWithIndex(float left, float right, int index) {
		return applyAsFloat(left, right);
	}
}
