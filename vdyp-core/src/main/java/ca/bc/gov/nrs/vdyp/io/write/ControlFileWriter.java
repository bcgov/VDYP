package ca.bc.gov.nrs.vdyp.io.write;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

import ca.bc.gov.nrs.vdyp.io.parse.ControlFileParser;

/**
 * Writer for control files
 *
 * @author Kevin Smith, Vivid Solutions
 *
 */
public class ControlFileWriter implements Closeable {
	private final OutputStream os;

	static final Charset CHARSET = StandardCharsets.US_ASCII;
	public static final String NEW_LINE = "\r\n"; // Always use Window line separator to write
	public static final Pattern NEW_LINE_PATTERN = Pattern.compile("[\r\n]");
	public static final String NORMAL_FORMAT = "%03d %-50s%s" + NEW_LINE;
	public static final String EXTENDED_FORMAT = "%03dX%-120s%s" + NEW_LINE;
	public static final String COMMENT_FORMAT = "    %s" + NEW_LINE;

	public ControlFileWriter(OutputStream os) {
		super();
		this.os = os;
	}

	/**
	 * Write an entry for a control file
	 *
	 * @param index   Control file index (1 to 200 inclusive)
	 * @param control Value of the control (Max 120 characters, May not contain '!'
	 *                or newlines)
	 * @param comment A human readable comment (May not contain newlines)
	 * @throws IOException
	 * @throws IllegalArgumentException
	 */
	public void writeEntry(int index, String control, String comment) throws IOException {
		if (index < 1) {
			throw new IllegalArgumentException("Control file index " + index + " is less than 1");
		}
		if (index > 200) {
			throw new IllegalArgumentException("Control file index " + index + " is greater than 200");
		}
		// There's no mechanism for extending past 120 so simply disallow this
		if (control.length() > ControlFileParser.CONTROL_LENGTH_EXTENDED) {
			throw new IllegalArgumentException(
					"Control file value is longer than " + ControlFileParser.CONTROL_LENGTH_EXTENDED
			);
		}
		// There's no mechanism for escaping so simply disallow this.
		if (control.contains(ControlFileParser.COMMENT_MARKER)) {
			throw new IllegalArgumentException(
					"Control file value contains a comment marker (" + ControlFileParser.COMMENT_MARKER + ")"
			);
		}
		// There's no mechanism for escaping so simply disallow this.
		if (NEW_LINE_PATTERN.matcher(control).find()) {
			throw new IllegalArgumentException("Control file value contains a line break");
		}
		// Could use additional comment lines instead
		if (NEW_LINE_PATTERN.matcher(comment).find()) {
			throw new IllegalArgumentException("Control file comment contains a line break");
		}

		String format = control.length() > ControlFileParser.CONTROL_LENGTH ? EXTENDED_FORMAT : NORMAL_FORMAT;

		os.write(String.format(format, index, control, comment).getBytes(CHARSET));
	}

	/**
	 * Write an entry for a control file
	 *
	 * @param index   Control file index (1 to 200 inclusive)
	 * @param control Value of the control (Max 120 characters, May not contain '!'
	 *                or newlines)
	 * @throws IOException
	 * @throws IllegalArgumentException
	 */
	public void writeEntry(int index, String control) throws IOException {
		writeEntry(index, control, "");
	}

	/**
	 * Write a comment line for a control file
	 *
	 * @param comment A human readable comment (May not contain newlines)
	 * @throws IOException
	 * @throws IllegalArgumentException
	 */
	public void writeComment(String comment) throws IOException {
		// Could use additional comment lines instead
		if (NEW_LINE_PATTERN.matcher(comment).find()) {
			throw new IllegalArgumentException("Control file comment contains a line break");
		}

		os.write(String.format(COMMENT_FORMAT, comment).getBytes(CHARSET));
	}

	@Override
	public void close() throws IOException {
		os.close();
	}

}
