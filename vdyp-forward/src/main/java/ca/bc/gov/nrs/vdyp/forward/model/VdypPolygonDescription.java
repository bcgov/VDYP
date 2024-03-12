package ca.bc.gov.nrs.vdyp.forward.model;

public class VdypPolygonDescription {

	// See IPSJF155.doc

	private final String description; // POLYDESC
	private final Integer year; // derived - last four characters of POLYDESC

	public VdypPolygonDescription(String description, Integer year) {
		super();
		this.description = description;
		this.year = year;
	}

	public String getDescription() {
		return description;
	}

	public Integer getYear() {
		return year;
	}
}
