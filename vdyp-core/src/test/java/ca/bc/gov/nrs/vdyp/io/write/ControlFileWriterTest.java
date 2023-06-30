package ca.bc.gov.nrs.vdyp.io.write;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayOutputStream;

import org.junit.jupiter.api.Test;

public class ControlFileWriterTest {

	@Test
	public void testWriteEntry() throws Exception {

		try (var output = new ByteArrayOutputStream(); var writer = new ControlFileWriter(output);) {

			writer.writeEntry(1, "Value");

			assertThat(
					output.toString(ControlFileWriter.CHARSET),
					equalTo("001 Value                                             \r\n")
			);

		}
	}

	@Test
	public void testWriteEntryWithComment() throws Exception {

		try (var output = new ByteArrayOutputStream(); var writer = new ControlFileWriter(output);) {

			writer.writeEntry(1, "Value", "Comment");

			assertThat(
					output.toString(ControlFileWriter.CHARSET),
					equalTo("001 Value                                             Comment\r\n")
			);

		}
	}

	@Test
	public void testWriteExtendedEntry() throws Exception {

		try (var output = new ByteArrayOutputStream(); var writer = new ControlFileWriter(output);) {

			writer.writeEntry(1, "Long Value.........................................");

			assertThat(
					output.toString(ControlFileWriter.CHARSET),
					equalTo(
							"001XLong Value.........................................                                                                     \r\n"
					)
			);

		}
	}

	@Test
	public void testWriteExtendedEntryWithComment() throws Exception {

		try (var output = new ByteArrayOutputStream(); var writer = new ControlFileWriter(output);) {

			writer.writeEntry(1, "Long Value.........................................", "Comment");

			assertThat(
					output.toString(ControlFileWriter.CHARSET),
					equalTo(
							"001XLong Value.........................................                                                                     Comment\r\n"
					)
			);

		}
	}

	@Test
	public void testWriteComment() throws Exception {

		try (var output = new ByteArrayOutputStream(); var writer = new ControlFileWriter(output);) {

			writer.writeComment("Comment");

			assertThat(output.toString(ControlFileWriter.CHARSET), equalTo("    Comment\r\n"));

		}
	}

	@Test
	public void testValidateIndex() throws Exception {

		try (var output = new ByteArrayOutputStream(); var writer = new ControlFileWriter(output);) {

			writer.writeEntry(1, "Low");
			writer.writeEntry(200, "High");
			assertThrows(IllegalArgumentException.class, () -> {
				writer.writeEntry(0, "Anything");
			});
			assertThrows(IllegalArgumentException.class, () -> {
				writer.writeEntry(-1, "Anything");
			});
			assertThrows(IllegalArgumentException.class, () -> {
				writer.writeEntry(201, "Anything");
			});

			assertThat(
					output.toString(ControlFileWriter.CHARSET),
					equalTo(
							"001 Low                                               \r\n200 High                                              \r\n"
					)
			);

		}
	}

	@Test
	public void testValidateValue() throws Exception {

		try (var output = new ByteArrayOutputStream(); var writer = new ControlFileWriter(output);) {

			writer.writeEntry(1, "");
			writer.writeEntry(
					2,
					"012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789"
			);
			assertThrows(IllegalArgumentException.class, () -> {
				writer.writeEntry(
						3,
						"012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789X"
				);
			});
			assertThrows(IllegalArgumentException.class, () -> {
				writer.writeEntry(4, "No ! Bang");
			});
			assertThrows(IllegalArgumentException.class, () -> {
				writer.writeEntry(5, "No \r\n windows newline");
			});
			assertThrows(IllegalArgumentException.class, () -> {
				writer.writeEntry(6, "No \n POSIX newline");
			});

			assertThat(
					output.toString(ControlFileWriter.CHARSET),
					equalTo(
							"001                                                   \r\n002X012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789\r\n"
					)
			);

		}
	}

	@Test
	public void testValidateComment() throws Exception {

		try (var output = new ByteArrayOutputStream(); var writer = new ControlFileWriter(output);) {

			writer.writeEntry(1, "Value", "");
			writer.writeEntry(2, "Value", "Comment");
			writer.writeEntry(3, "Value", "! Comment");
			writer.writeComment("");
			writer.writeComment("Comment");
			writer.writeComment("! Comment");
			assertThrows(IllegalArgumentException.class, () -> {
				writer.writeEntry(4, "Value", "Comment with \r\n Windows newline");
			});
			assertThrows(IllegalArgumentException.class, () -> {
				writer.writeEntry(5, "Value", "Comment with \n POSIX newline");
			});
			// Could write comments with line breaks as multiple comment lines
			assertThrows(IllegalArgumentException.class, () -> {
				writer.writeComment("Comment with \r\n Windows newline");
			});
			assertThrows(IllegalArgumentException.class, () -> {
				writer.writeComment("Comment with \n POSIX newline");
			});

			assertThat(
					output.toString(ControlFileWriter.CHARSET),
					equalTo(
							"001 Value                                             \r\n"
									+ "002 Value                                             Comment\r\n"
									+ "003 Value                                             ! Comment\r\n" + "    \r\n"
									+ "    Comment\r\n" + "    ! Comment\r\n"
					)
			);

		}
	}

}
