package ca.bc.gov.nrs.vdyp.si32.enumerations;

public interface SI32Enum<T> {
	/**
	 * @return the integer value of the enumeration entry. For a given enumeration, this is normally the sequence n, n +
	 *         1, n + 2, ... where n is either 0 or 1, but it doesn't have to be.
	 */
	public int getIndex();

	/**
	 * @return the index of the enumeration entry. This is the relative position (zero-based) of the entry in the
	 *         enumeration, ignoring any "housekeeping" entries (such as Unknown and the like.)
	 *
	 * @throws IllegalArgumentException if this is a housekeeping entry.
	 */
	int getOffset();

	/**
	 * @return the text identifying the enumeration entry. This is normally the enum constant excluding the prefix
	 *         common to all. This cannot be called on "housekeeping" entries.
	 *
	 * @throws IllegalArgumentException if this is a housekeeping entry.
	 */
	String getText();
}
