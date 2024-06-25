package ca.bc.gov.nrs.vdyp.io.parse.coe;

import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.coe;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import ca.bc.gov.nrs.vdyp.test.TestUtils;

class BySpeciesDqCoefficientParserTest {

	@Test
	void testParseSimple() throws Exception {

		var parser = new BySpeciesDqCoefficientParser();

		var is = TestUtils.makeInputStream(
				"A0 1 -0.65484 -0.48275 -0.75134  0.04482 -0.31195 -0.53012 -0.12645 -0.64668 -0.43538 -0.31134 -0.03435 -0.27833 -0.32476  0.10819 -0.38103 -0.12273",
				"A1 2  2.26389  0.19886 -0.25704  0.18579 -0.38547 -0.14115 -0.10146  0.09067  0.54304 -0.02947  0.08473 -0.39934  0.02206 -0.18235  0.01411 -0.21683",
				"A2 0  0.23162"
		);

		Map<String, Object> controlMap = new HashMap<>();
		TestUtils.populateControlMapGenusReal(controlMap);

		var result = parser.parse(is, controlMap);

		// "AC", "AT", "B", "C", "D", "E", "F", "H", "L", "MB", "PA", "PL", "PW", "PY",
		// "S", "Y"

		assertThat(result, aMapWithSize(16));

		// TODO: This is what VDYP7 does (confirmed with Debugger) but it looks wrong,
		// the much larger value of A1 for the first species suggests those for other
		// species should be considered modifiers. Possibly the test data file I'm using
		// is just old and newer ones
		// take this into account

		assertThat(result, hasEntry(is("AC"), coe(0, -0.65484f, 2.26389f, 0.23162f)));
		assertThat(result, hasEntry(is("AT"), coe(0, -0.48275f, 0.19886f, 0.23162f)));

	}

}
