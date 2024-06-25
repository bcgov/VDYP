package ca.bc.gov.nrs.vdyp.forward.parsers;

import java.util.List;

import ca.bc.gov.nrs.vdyp.forward.model.VdypGrowthDetails;
import ca.bc.gov.nrs.vdyp.io.parse.value.ValueParseException;
import ca.bc.gov.nrs.vdyp.io.parse.value.ValueParser;

public class VdypVtrolParser implements ValueParser<VdypGrowthDetails> {

	@Override
	public VdypGrowthDetails parse(String string) throws ValueParseException {

		if (string == null) {
			throw new ValueParseException(null, "VdypVtrolParser: supplied string is null");
		}
		if (string.trim().length() == 0) {
			throw new ValueParseException(string, "VdypVtrolParser: supplied string \"" + string + "\" is empty");
		}

		var parser = ValueParser.list(ValueParser.INTEGER);
		List<Integer> vtrol = parser.parse(string);

		var a = new Integer[vtrol.size()];
		var details = new VdypGrowthDetails(vtrol.toArray(a));

		if (a.length == 0) {
			throw new ValueParseException(string, "VdypVtrolParser: supplied string \"" + string + "\" is empty");
		}

		var yearCounter = a[0];
		if (yearCounter != -1 && (yearCounter < 0 || yearCounter > 400 && yearCounter < 1920)) {
			throw new ValueParseException(
					Integer.toString(yearCounter),
					"VdypVtrolParser: yearCounter value \"" + yearCounter + "\" is out of range"
			);
		}

		details.setYearCounter(yearCounter);

		return details;
	}
}
