package ca.bc.gov.nrs.vdyp.model;

import java.util.AbstractList;
import java.util.Collection;
import java.util.List;
import java.util.stream.DoubleStream;

import ca.bc.gov.nrs.vdyp.common.FloatBinaryOperator;
import ca.bc.gov.nrs.vdyp.common.FloatUnaryOperator;
import ca.bc.gov.nrs.vdyp.common.IndexedFloatBinaryOperator;
import ca.bc.gov.nrs.vdyp.common.IndexedFloatUnaryOperator;

/**
 * Fixed length list of floats that can be accessed using an offset index
 *
 * @author Kevin Smith, Vivid Solutions
 *
 */
public class Coefficients extends AbstractList<Float> implements List<Float> {
	private float[] coe;
	private int indexFrom;

	public Coefficients(float[] coe, int indexFrom) {
		this.coe = coe;
		this.indexFrom = indexFrom;
	}

	public Coefficients(List<Float> coe, int indexFrom) {

		this(listToArray(coe), indexFrom);
	}

	private static float[] listToArray(List<Float> coe) {
		float[] floatArray = new float[coe.size()];
		int i = 0;

		for (Float f : coe) {
			floatArray[i++] = (f != null ? f : Float.NaN);
		}
		return floatArray;
	}

	@Override
	public Float get(int i) {
		return coe[i];
	}

	public float getCoe(int i) {
		return coe[getRealIndex(i)];
	}

	public void setCoe(int i, float value) {
		coe[getRealIndex(i)] = value;
	}

	@Override
	public int size() {
		return coe.length;
	}

	@Override
	public boolean addAll(Collection<? extends Float> c) {
		return false;
	}

	/**
	 * Create a list of all the same float value
	 *
	 * @param size  number of elements
	 * @param value the value to repeat
	 * @return
	 */
	public static List<Float> sameSize(int size, float value) {
		return DoubleStream.generate(() -> value).limit(size).mapToObj(x -> (float) x).toList();
	}

	/**
	 * Create an empty (all 0.0) Coefficents object
	 *
	 * @param size
	 * @param indexFrom
	 * @return
	 */
	public static Coefficients empty(int size, int indexFrom) {
		return new Coefficients(sameSize(size, 0f), indexFrom);
	}

	/**
	 * Index of first coefficient
	 */
	public int getIndexFrom() {
		return indexFrom;
	}

	private int getRealIndex(int i) {
		var max = coe.length + indexFrom - 1;
		if (i < indexFrom || i > max) {
			throw new ArrayIndexOutOfBoundsException(
					"Coefficient index " + i + " out of bounds for [" + indexFrom + ":" + max + "]"
			);
		}
		return i - indexFrom;
	}

	/**
	 * Performs a pairwise operation in place with a compatible Coefficients object
	 *
	 * @param coe2 must have the same size and index offset
	 * @param op   operation to perform for each pair of coefficients
	 */
	public void pairwiseInPlace(Coefficients coe2, FloatBinaryOperator op) {
		pairwiseInPlace(coe2, (IndexedFloatBinaryOperator) op);
	}

	/**
	 * Performs a pairwise operation in place with a compatible Coefficients object
	 *
	 * @param coe2 must have the same size and index offset
	 * @param op   operation to perform for each pair of coefficients
	 */
	public void pairwiseInPlace(Coefficients coe2, IndexedFloatBinaryOperator op) {
		checkCompatible(coe2);
		int max = getIndexFrom() + size();
		for (int i = getIndexFrom(); i < max; i++) {
			setCoe(i, op.applyAsFloatWithIndex(getCoe(i), coe2.getCoe(i), i));
		}
	}

	private void checkCompatible(Coefficients coe2) throws IllegalArgumentException {
		if (coe2.getIndexFrom() != getIndexFrom()) {
			throw new IllegalArgumentException(
					"Expected Coefficients object indexed from " + getIndexFrom() + " but was indexed from "
							+ coe2.getIndexFrom()
			);
		}
		if (coe2.size() != size()) {
			throw new IllegalArgumentException(
					"Expected Coefficients object of size " + size() + " but was " + coe2.size()
			);
		}
	}

	/**
	 * Performs a pairwise operation with a compatible Coefficients object and returns the result.
	 *
	 * @param coe2 must have the same size and index offset
	 * @param op   operation to perform for each pair of coefficients
	 */
	public Coefficients pairwise(Coefficients coe2, FloatBinaryOperator op) {
		return pairwise(coe2, (IndexedFloatBinaryOperator) op);
	}

	/**
	 * Performs a pairwise operation with a compatible Coefficients object and returns the result.
	 *
	 * @param coe2 must have the same size and index offset
	 * @param op   operation to perform for each pair of coefficients
	 */
	public Coefficients pairwise(Coefficients coe2, IndexedFloatBinaryOperator op) {
		var result = new Coefficients(this, this.getIndexFrom());
		result.pairwiseInPlace(coe2, op);
		return result;
	}

	/**
	 * Perform the operation on each coefficient in place
	 *
	 * @param op
	 */
	public void scalarInPlace(FloatUnaryOperator op) {
		scalarInPlace((IndexedFloatUnaryOperator) op);
	}

	/**
	 * Perform the operation on one particular coefficient in place
	 *
	 * @param op
	 */
	public void scalarInPlace(int i, IndexedFloatUnaryOperator op) {
		setCoe(i, op.applyAsFloatWithIndex(getCoe(i), i));
	}

	/**
	 * Perform the operation on each coefficient in place
	 *
	 * @param op
	 */
	public void scalarInPlace(IndexedFloatUnaryOperator op) {
		int max = getIndexFrom() + size();
		for (int i = getIndexFrom(); i < max; i++) {
			scalarInPlace(i, op);
		}
	}

	/**
	 * Perform the operation on one particular coefficient in place
	 *
	 * @param op
	 */
	public void scalarInPlace(int i, FloatUnaryOperator op) {
		setCoe(i, op.applyAsFloat(getCoe(i)));
	}

	/**
	 * Perform the operation on each coefficient and return the result
	 *
	 * @param op
	 * @return
	 */
	public Coefficients scalar(IndexedFloatUnaryOperator op) {
		var result = new Coefficients(this, this.getIndexFrom());
		result.scalarInPlace(op);
		return result;
	}

	/**
	 * Perform the operation on each coefficient and return the result
	 *
	 * @param op
	 * @return
	 */
	public Coefficients scalar(FloatUnaryOperator op) {
		return scalar((IndexedFloatUnaryOperator) op);
	}

	/**
	 * Returns a view of this coefficients object indexed from the given value.
	 */
	public Coefficients reindex(int indexFrom) {
		return new Coefficients(this.coe, indexFrom);
	}
}
