package ca.bc.gov.nrs.vdyp.io.parse.coe;

import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.coe;
import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.mmHasEntry;
import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.notPresent;
import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.present;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import ca.bc.gov.nrs.vdyp.test.TestUtils;

class CloseUtilVolumeParserTest {

	@Test
	void testParseSimple() throws Exception {

		var parser = new CloseUtilVolumeParser();

		var is = TestUtils.makeInputStream("1   1    -7.425    0.0000   0.15032");

		Map<String, Object> controlMap = new HashMap<>();

		var result = parser.parse(is, controlMap);

		assertThat(result, mmHasEntry(present(coe(1, -7.425f, 0f, 0.15032f)), 1, 1));
		assertThat(result, mmHasEntry(notPresent(), 2, 1));
		assertThat(result, mmHasEntry(notPresent(), 1, 2));
		assertThat(result, mmHasEntry(notPresent(), 2, 2));
	}

	@Test
	void testParseUCIndexInSecondColumn() throws Exception {

		var parser = new CloseUtilVolumeParser();

		var is = TestUtils.makeInputStream(" 1  1    -7.425    0.0000   0.15032");

		Map<String, Object> controlMap = new HashMap<>();

		var result = parser.parse(is, controlMap);

		assertThat(result, mmHasEntry(present(coe(1, -7.425f, 0f, 0.15032f)), 1, 1));
		assertThat(result, mmHasEntry(notPresent(), 2, 1));
		assertThat(result, mmHasEntry(notPresent(), 1, 2));
		assertThat(result, mmHasEntry(notPresent(), 2, 2));
	}

	@Test
	void testParseTwoGroups() throws Exception {

		var parser = new CloseUtilVolumeParser();

		var is = TestUtils.makeInputStream(
				"1   1    -7.425    0.0000   0.15032", //
				"1   2    -9.326    0.6387   0.05382"
		);

		Map<String, Object> controlMap = new HashMap<>();

		var result = parser.parse(is, controlMap);

		assertThat(result, mmHasEntry(present(coe(1, -7.425f, 0f, 0.15032f)), 1, 1));
		assertThat(result, mmHasEntry(notPresent(), 2, 1));
		assertThat(result, mmHasEntry(present(coe(1, -9.326f, 0.6387f, 0.05382f)), 1, 2));
		assertThat(result, mmHasEntry(notPresent(), 2, 2));
	}

	@Test
	void testParseTwoClasses() throws Exception {

		var parser = new CloseUtilVolumeParser();

		var is = TestUtils.makeInputStream(
				"1   1    -7.425    0.0000   0.15032", //
				"2   1    -8.359    0.5124   0.05910"
		);

		Map<String, Object> controlMap = new HashMap<>();

		var result = parser.parse(is, controlMap);

		assertThat(result, mmHasEntry(present(coe(1, -7.425f, 0f, 0.15032f)), 1, 1));
		assertThat(result, mmHasEntry(present(coe(1, -8.359f, 0.5124f, 0.05910f)), 2, 1));
		assertThat(result, mmHasEntry(notPresent(), 1, 2));
		assertThat(result, mmHasEntry(notPresent(), 2, 2));
	}

	@Test
	void testParseTwoClassesWithBlankLine() throws Exception {

		var parser = new CloseUtilVolumeParser();

		var is = TestUtils.makeInputStream(
				"1   1    -7.425    0.0000   0.15032", //
				"", //
				"2   1    -8.359    0.5124   0.05910"
		);

		Map<String, Object> controlMap = new HashMap<>();

		var result = parser.parse(is, controlMap);

		assertThat(result, mmHasEntry(present(coe(1, -7.425f, 0f, 0.15032f)), 1, 1));
		assertThat(result, mmHasEntry(present(coe(1, -8.359f, 0.5124f, 0.05910f)), 2, 1));
		assertThat(result, mmHasEntry(notPresent(), 1, 2));
		assertThat(result, mmHasEntry(notPresent(), 2, 2));
	}

	@Test
	void testParseTwoClassesWithBlankKey() throws Exception {

		var parser = new CloseUtilVolumeParser();

		var is = TestUtils.makeInputStream(
				"1   1    -7.425    0.0000   0.15032", //
				"          4.444    4.4444   4.44444", //
				"2   1    -8.359    0.5124   0.05910"
		);

		Map<String, Object> controlMap = new HashMap<>();

		var result = parser.parse(is, controlMap);

		assertThat(result, mmHasEntry(present(coe(1, -7.425f, 0f, 0.15032f)), 1, 1));
		assertThat(result, mmHasEntry(present(coe(1, -8.359f, 0.5124f, 0.05910f)), 2, 1));
		assertThat(result, mmHasEntry(notPresent(), 1, 2));
		assertThat(result, mmHasEntry(notPresent(), 2, 2));
		assertFalse(result.any(opt -> opt.map(coe -> coe.getCoe(1) - 4.4444f > 0.0001).orElse(false)));
	}

	@Test
	void testParseTwoClassesWithZeroClass() throws Exception {

		var parser = new CloseUtilVolumeParser();

		var is = TestUtils.makeInputStream(
				"1   1    -7.425    0.0000   0.15032", //
				"0   1     4.444    4.4444   4.44444", //
				"2   1    -8.359    0.5124   0.05910"
		);

		Map<String, Object> controlMap = new HashMap<>();

		var result = parser.parse(is, controlMap);

		assertThat(result, mmHasEntry(present(coe(1, -7.425f, 0f, 0.15032f)), 1, 1));
		assertThat(result, mmHasEntry(present(coe(1, -8.359f, 0.5124f, 0.05910f)), 2, 1));
		assertThat(result, mmHasEntry(notPresent(), 1, 2));
		assertThat(result, mmHasEntry(notPresent(), 2, 2));
		assertFalse(result.any(opt -> opt.map(coe -> coe.getCoe(1) - 4.4444f > 0.0001).orElse(false)));
	}

	@Test
	void testParseTwoClassesWithZeroGroup() throws Exception {

		var parser = new CloseUtilVolumeParser();

		var is = TestUtils.makeInputStream(
				"1   1    -7.425    0.0000   0.15032", //
				"1   0     4.444    4.4444   4.44444", //
				"2   1    -8.359    0.5124   0.05910"
		);

		Map<String, Object> controlMap = new HashMap<>();

		var result = parser.parse(is, controlMap);

		assertThat(result, mmHasEntry(present(coe(1, -7.425f, 0f, 0.15032f)), 1, 1));
		assertThat(result, mmHasEntry(present(coe(1, -8.359f, 0.5124f, 0.05910f)), 2, 1));
		assertThat(result, mmHasEntry(notPresent(), 1, 2));
		assertThat(result, mmHasEntry(notPresent(), 2, 2));
		assertFalse(result.any(opt -> opt.map(coe -> coe.getCoe(1) - 4.4444f > 0.0001).orElse(false)));
	}
}
