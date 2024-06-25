package ca.bc.gov.nrs.vdyp.math;

import java.io.IOException;

public class FloatMath {

	private FloatMath() {
	}

	// wrap standard library double math functions to work with floats so equations
	// aren't littered with explicit casts

	public static float log(float f) {

		return (float) Math.log(f);
	}

	public static float exp(float f) {
		return (float) Math.exp(f);
	}

	public static float pow(float b, float e) {
		return (float) Math.pow(b, e);
	}

	public static float abs(float f) {
		return Math.abs(f);
	}

	public static float sqrt(float f) {
		return (float) Math.sqrt(f);
	}

	public static float floor(float f) {
		return (float) Math.floor(f);
	}

	public static float ceil(float f) {
		return (float) Math.ceil(f);
	}

	@SuppressWarnings("squid:S4274")
	public static float clamp(float x, float min, float max) {
		assert max >= min : "Maximum " + max + " was less than minimum " + min;
		if (x < min)
			return min;
		if (x > max)
			return max;
		return x;
	}

	public static float ratio(float arg, float radius) {
		if (arg < -radius) {
			return 0.0f;
		} else if (arg > radius) {
			return 1.0f;
		}
		return exp(arg) / (1.0f + exp(arg));
	}

	public static String toString(float[] vector) {
		var builder = new StringBuilder();
		try {
			toString(vector, builder);
		} catch (IOException e) {
			throw new IllegalStateException("StringBuilder should not throw IOException", e);
		}
		return builder.toString();
	}

	public static void toString(float[] vector, Appendable builder) throws IOException {
		builder.append("[");
		var first = true;
		for (var x : vector) {
			if (!first)
				builder.append(", ");
			first = false;
			builder.append(Float.toString(x));
		}
		builder.append("]");
	}
}
