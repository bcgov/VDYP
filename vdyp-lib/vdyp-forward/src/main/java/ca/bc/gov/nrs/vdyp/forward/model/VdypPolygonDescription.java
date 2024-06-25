package ca.bc.gov.nrs.vdyp.forward.model;

public class VdypPolygonDescription {

	// See IPSJF155.doc

	private final String description; // POLYDESC
	private final String name; // description, with year removed, trimmed
	private final Integer year; // derived - last four characters of POLYDESC

	public VdypPolygonDescription(String description, String name, Integer year) {
		this.description = description.trim();
		this.year = year;
		this.name = name.trim();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(name).append('(').append(year).append(')');
		return sb.toString();
	}

	public String getDescription() {
		return description;
	}

	public Integer getYear() {
		return year;
	}

	public String getName() {
		return name;
	}

	@Override
	public boolean equals(Object other) {
		return other instanceof VdypPolygonDescription that && this.name.equals(that.name);
	}

	@Override
	public int hashCode() {
		return description.hashCode();
	}
}
