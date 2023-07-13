package ca.bc.gov.nrs.vdyp.io.parse;

import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.mmHasEntry;
import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.present;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import ca.bc.gov.nrs.vdyp.test.TestUtils;

public class CoefficientParserTest {

	@Test
	public void testParseSimple() throws Exception {

		var parser = new CoefficientParser("TEST");

		var is = TestUtils.makeStream(
				"B1   A0 1  2.0028 -0.5343  1.3949 -0.3683 -0.3343  0.5699  0.2314  0.0528  0.2366 -0.3343  0.5076  0.5076  0.6680 -0.1353  1.2445 -0.4507"
		);

		Map<String, Object> controlMap = new HashMap<>();

		BecDefinitionParserTest.populateControlMap(controlMap);

		var result = parser.parse(is, controlMap);

		assertThat(result, mmHasEntry(present(is(2.0028f)), 0, "B1", (Integer) 1)); // COEs are 0 indexed, species are 1
																					// indexed
		assertThat(result, mmHasEntry(present(is(-0.5343f)), 0, "B1", (Integer) 2));

	}

	@Test
	public void testBadBec() throws Exception {

		var parser = new CoefficientParser("TEST");

		var is = TestUtils.makeStream(
				"BX   A0 0  2.0028 -0.5343  1.3949 -0.3683 -0.3343  0.5699  0.2314  0.0528  0.2366 -0.3343  0.5076  0.5076  0.6680 -0.1353  1.2445 -0.4507"
		);

		Map<String, Object> controlMap = new HashMap<>();

		BecDefinitionParserTest.populateControlMap(controlMap);

		assertThrows(ResourceParseLineException.class, () -> parser.parse(is, controlMap));

	}

	@Test
	public void testBadIndex() throws Exception {

		var parser = new CoefficientParser("TEST");

		var is = TestUtils.makeStream(
				"B1   AX 0  2.0028 -0.5343  1.3949 -0.3683 -0.3343  0.5699  0.2314  0.0528  0.2366 -0.3343  0.5076  0.5076  0.6680 -0.1353  1.2445 -0.4507"
		);

		Map<String, Object> controlMap = new HashMap<>();

		BecDefinitionParserTest.populateControlMap(controlMap);

		assertThrows(ResourceParseLineException.class, () -> parser.parse(is, controlMap));

	}

	@Test
	public void testParseDelta() throws Exception {

		var parser = new CoefficientParser("TEST");

		var is = TestUtils.makeStream(
				"B1   A0 2  2.0028 -0.5343  1.3949 -0.3683 -0.3343  0.5699  0.2314  0.0528  0.2366 -0.3343  0.5076  0.5076  0.6680 -0.1353  1.2445 -0.4507"
		);

		Map<String, Object> controlMap = new HashMap<>();

		BecDefinitionParserTest.populateControlMap(controlMap);

		var result = parser.parse(is, controlMap);

		assertThat(result, mmHasEntry(present(is(2.0028f)), 0, "B1", (Integer) 1));
		assertThat(result, mmHasEntry(present(is(2.0028f - 0.5343f)), 0, "B1", (Integer) 2));

	}

	@Test
	public void testParseFixed() throws Exception {

		var parser = new CoefficientParser("TEST");

		var is = TestUtils.makeStream(
				"B1   A0 0  2.0028 -0.5343  1.3949 -0.3683 -0.3343  0.5699  0.2314  0.0528  0.2366 -0.3343  0.5076  0.5076  0.6680 -0.1353  1.2445 -0.4507"
		);

		Map<String, Object> controlMap = new HashMap<>();

		BecDefinitionParserTest.populateControlMap(controlMap);

		var result = parser.parse(is, controlMap);

		assertThat(result, mmHasEntry(present(is(2.0028f)), 0, "B1", (Integer) 1));
		assertThat(result, mmHasEntry(present(is(2.0028f)), 0, "B1", (Integer) 2));

	}

}
