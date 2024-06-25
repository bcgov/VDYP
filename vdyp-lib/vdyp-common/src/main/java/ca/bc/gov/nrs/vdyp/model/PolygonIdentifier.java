package ca.bc.gov.nrs.vdyp.model;

import java.util.Objects;
import java.util.function.IntUnaryOperator;

public class PolygonIdentifier {

	private final String base;
	private final int year;

	public PolygonIdentifier(String base, int year) throws IllegalArgumentException {
		if (base.length() > BASE_LENGTH)
			throw new IllegalArgumentException("Polygon identifier base \"" + base + "\" is too long.");
		if (year < 1)
			throw new IllegalArgumentException("Polygon identifier year " + year + " must be positive.");
		this.base = base;
		this.year = year;
	}

	public static PolygonIdentifier split(String polygonIdentifier) throws IllegalArgumentException {
		if (polygonIdentifier.length() != ID_LENGTH)
			throw new IllegalArgumentException(
					"Polygon identifier \"" + polygonIdentifier + "\" must be exactly " + ID_LENGTH + " characters."
			);
		String base = polygonIdentifier.substring(0, BASE_LENGTH).trim();
		String year = polygonIdentifier.substring(BASE_LENGTH, ID_LENGTH).trim();

		return new PolygonIdentifier(base, Integer.parseInt(year));
	}

	public static final int ID_LENGTH = 25;
	public static final int YEAR_LENGTH = 4;
	public static final int BASE_LENGTH = ID_LENGTH - YEAR_LENGTH;

	public static final String FORMAT = "%-" + BASE_LENGTH + "s%" + YEAR_LENGTH + "d";

	public String getBase() {
		return base;
	}

	public int getYear() {
		return year;
	}

	@Override
	public String toString() {
		String result = FORMAT.formatted(base, year);
		assert (result.length() == ID_LENGTH);
		return result;
	}

	@Override
	public int hashCode() {
		return Objects.hash(base, year);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PolygonIdentifier other = (PolygonIdentifier) obj;
		return Objects.equals(base, other.base) && year == other.year;
	}

	public PolygonIdentifier forYear(int year) {
		return new PolygonIdentifier(getBase(), year);
	}

	public PolygonIdentifier forYear(IntUnaryOperator op) {
		return forYear(op.applyAsInt(getYear()));
	}
}
