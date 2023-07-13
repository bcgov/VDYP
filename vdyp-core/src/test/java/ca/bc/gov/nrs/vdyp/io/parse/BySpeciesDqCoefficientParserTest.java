package ca.bc.gov.nrs.vdyp.io.parse;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import ca.bc.gov.nrs.vdyp.test.TestUtils;

public class BySpeciesDqCoefficientParserTest {

	@Test
	public void testParseSimple() throws Exception {

		var parser = new BySpeciesDqCoefficientParser();

		var is = TestUtils.makeStream(
				"A0 1 -0.65484 -0.48275 -0.75134  0.04482 -0.31195 -0.53012 -0.12645 -0.64668 -0.43538 -0.31134 -0.03435 -0.27833 -0.32476  0.10819 -0.38103 -0.12273",
				"A1 2  2.26389  0.19886 -0.25704  0.18579 -0.38547 -0.14115 -0.10146  0.09067  0.54304 -0.02947  0.08473 -0.39934  0.02206 -0.18235  0.01411 -0.21683",
				"A2 0  0.23162"
		);

		Map<String, Object> controlMap = new HashMap<>();

		var result = parser.parse(is, controlMap);

		assertThat(
				result,
				contains(
						contains(
								-0.65484f, -0.48275f, -0.75134f, 0.04482f, -0.31195f, -0.53012f, -0.12645f, -0.64668f,
								-0.43538f, -0.31134f, -0.03435f, -0.27833f, -0.32476f, 0.10819f, -0.38103f, -0.12273f
						),
						contains(
								2.26389f, 0.19886f, -0.25704f, 0.18579f, -0.38547f, -0.14115f, -0.10146f, 0.09067f,
								0.54304f, -0.02947f, 0.08473f, -0.39934f, 0.02206f, -0.18235f, 0.01411f, -0.21683f
						),
						contains(
								0.23162f, 0.23162f, 0.23162f, 0.23162f, 0.23162f, 0.23162f, 0.23162f, 0.23162f,
								0.23162f, 0.23162f, 0.23162f, 0.23162f, 0.23162f, 0.23162f, 0.23162f, 0.23162f
						)
				)
		);
	}

}
