package ca.bc.gov.nrs.vdyp.forward.parsers;

import java.util.List;

import ca.bc.gov.nrs.vdyp.forward.model.VdypGrowthDetails;
import ca.bc.gov.nrs.vdyp.io.parse.value.ValueParseException;
import ca.bc.gov.nrs.vdyp.io.parse.value.ValueParser;

public class VdypControlVariableParser implements ValueParser<VdypGrowthDetails> {

	@Override
	public VdypGrowthDetails parse(String string) throws ValueParseException {

		if (string == null) {
			throw new ValueParseException(null, "VdypControlVariableParser: supplied string is null");
		}
		if (string.trim().length() == 0) {
			throw new ValueParseException(
					string, "VdypControlVariableParser: supplied string \"" + string + "\" is empty"
			);
		}

		var parser = ValueParser.list(ValueParser.INTEGER);
		List<Integer> controlVariableValues = parser.parse(string);

		if (controlVariableValues.isEmpty()) {
			throw new ValueParseException(
					string, "VdypControlVariableParser: supplied string \"" + string + "\" is empty"
			);
		}

		var a = new Integer[controlVariableValues.size()];

		var details = new VdypGrowthDetails(controlVariableValues.toArray(a));

		var yearCounter = a[0];
		if (yearCounter != -1 && (yearCounter < 0 || yearCounter > 400 && yearCounter < 1920)) {
			throw new ValueParseException(
					Integer.toString(yearCounter),
					"VdypControlVariableParser: yearCounter value \"" + yearCounter + "\" is out of range"
			);
		}

		details.setYearCounter(yearCounter);

		return details;
	}
}
