package ca.bc.gov.nrs.vdyp.io;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileSystemFileResolverTest {

	@TempDir
	Path testDir;

	@Test
	void testWorkingDirGetPath() throws Exception {
		var unit = new FileSystemFileResolver();

		var result = unit.toPath("test");

		assertThat(result.toAbsolutePath().toString(), equalTo(System.getProperty("user.dir") + "/test"));
	}

	@Test
	void testSpecifiedDirGetPath() throws Exception {
		var unit = new FileSystemFileResolver(testDir);

		var result = unit.toPath("test");

		assertThat(result.toAbsolutePath().toString(), equalTo(testDir.resolve("test").toString()));
	}

	@Test
	void testRelativeGetPath() throws Exception {
		var base = new FileSystemFileResolver(testDir);
		var unit = base.relative("rel");

		var result = unit.toPath("test");

		assertThat(result.toAbsolutePath().toString(), equalTo(testDir.resolve("rel").resolve("test").toString()));
	}

	@Test
	void testToString() throws Exception {
		var unit = new FileSystemFileResolver(testDir);

		assertThat(unit.toString("test"), equalTo("file:" + testDir.resolve("test").toString()));
	}

	@Test
	void testInput() throws Exception {
		var unit = new FileSystemFileResolver(testDir);
		try (var writer = Files.newBufferedWriter(testDir.resolve("test"))) {
			writer.write("blah");
		}
		try (var is = unit.resolveForInput("test")) {
			var result = is.readAllBytes();
			assertThat(result, equalTo("blah".getBytes()));
		}
	}

	@Test
	void testOutput() throws Exception {
		var unit = new FileSystemFileResolver(testDir);
		try (var os = unit.resolveForOutput("test")) {
			os.write("blah".getBytes());
		}
		try (var reader = Files.newBufferedReader(testDir.resolve("test"))) {
			var result = reader.readLine();

			assertThat(result, equalTo("blah"));
		}
	}
}
