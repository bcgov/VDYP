package ca.bc.gov.nrs.vdyp.application;

import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.causedBy;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

import org.easymock.EasyMock;
import org.junit.jupiter.api.Test;

import ca.bc.gov.nrs.vdyp.common.ControlKey;
import ca.bc.gov.nrs.vdyp.io.parse.common.ResourceParseException;
import ca.bc.gov.nrs.vdyp.io.parse.control.BaseControlParser;
import ca.bc.gov.nrs.vdyp.io.parse.streaming.StreamingParser;
import ca.bc.gov.nrs.vdyp.io.parse.streaming.StreamingParserFactory;
import ca.bc.gov.nrs.vdyp.test.MockFileResolver;
import ca.bc.gov.nrs.vdyp.test.TestUtils;

class VdypStartApplicationTest {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	void testInitWithoutErrors() throws IOException, ResourceParseException {
		BaseControlParser controlParser = EasyMock.createMock(BaseControlParser.class);
		MockFileResolver resolver = new MockFileResolver("Test");

		InputStream inputStream = TestUtils.makeInputStream("");
		resolver.addStream("testControl", inputStream);

		var controlMap = new HashMap<String, Object>();

		controlMap.put(ControlKey.VRI_OUTPUT_VDYP_POLYGON.name(), "DUMMY1");
		controlMap.put(ControlKey.VRI_OUTPUT_VDYP_LAYER_BY_SPECIES.name(), "DUMMY2");
		controlMap.put(ControlKey.VRI_OUTPUT_VDYP_LAYER_BY_SP0_BY_UTIL.name(), "DUMMY3");

		resolver.addStream("DUMMY1", new ByteArrayOutputStream());
		resolver.addStream("DUMMY2", new ByteArrayOutputStream());
		resolver.addStream("DUMMY3", new ByteArrayOutputStream());

		EasyMock.expect(
				controlParser.parse(EasyMock.anyObject(List.class), EasyMock.same(resolver), EasyMock.anyObject())
		).andReturn(controlMap);

		var app = new VdypStartApplication() {

			@Override
			protected BaseControlParser getControlFileParser() {
				return controlParser;
			}

			@Override
			public void process() throws ProcessingException {
				// Do Nothing
			}

			@Override
			public VdypApplicationIdentifier getId() {
				return VdypApplicationIdentifier.FIP_START;
			}
		};

		EasyMock.replay(controlParser);

		app.init(resolver, "testControl");
		assertThat(app.controlMap, is(controlMap));

		EasyMock.verify(controlParser);
		app.close();
	}

	@SuppressWarnings({ "rawtypes" })
	@Test
	void testInitNoControlFiles() throws IOException, ResourceParseException {
		BaseControlParser controlParser = EasyMock.createMock(BaseControlParser.class);
		MockFileResolver resolver = new MockFileResolver("Test");

		var app = new VdypStartApplication() {

			@Override
			protected BaseControlParser getControlFileParser() {
				return controlParser;
			}

			@Override
			public void process() throws ProcessingException {
				// Do Nothing
			}

			@Override
			public VdypApplicationIdentifier getId() {
				return VdypApplicationIdentifier.FIP_START;
			}
		};

		EasyMock.replay(controlParser);

		var ex = assertThrows(IllegalArgumentException.class, () -> app.init(resolver));
		assertThat(ex, hasProperty("message", is("At least one control file must be specified.")));

		EasyMock.verify(controlParser);
		app.close();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	void testGetStreamingParser() throws IOException, ResourceParseException, ProcessingException {
		BaseControlParser controlParser = EasyMock.createMock(BaseControlParser.class);
		StreamingParserFactory streamingParserFactory = EasyMock.createMock(StreamingParserFactory.class);
		StreamingParser streamingParser = EasyMock.createMock(StreamingParser.class);

		MockFileResolver resolver = new MockFileResolver("Test");
		var controlMap = new HashMap<String, Object>();

		controlMap.put(ControlKey.VRI_OUTPUT_VDYP_POLYGON.name(), "DUMMY1");
		controlMap.put(ControlKey.VRI_OUTPUT_VDYP_LAYER_BY_SPECIES.name(), "DUMMY2");
		controlMap.put(ControlKey.VRI_OUTPUT_VDYP_LAYER_BY_SP0_BY_UTIL.name(), "DUMMY3");
		controlMap.put(ControlKey.FIP_INPUT_YIELD_LAYER.name(), streamingParserFactory);

		resolver.addStream("DUMMY1", new ByteArrayOutputStream());
		resolver.addStream("DUMMY2", new ByteArrayOutputStream());
		resolver.addStream("DUMMY3", new ByteArrayOutputStream());

		EasyMock.expect(streamingParserFactory.get()).andReturn(streamingParser);

		var app = new VdypStartApplication() {

			@Override
			protected BaseControlParser getControlFileParser() {
				return controlParser;
			}

			@Override
			public void process() throws ProcessingException {
				// Do Nothing
			}

			@Override
			public VdypApplicationIdentifier getId() {
				return VdypApplicationIdentifier.FIP_START;
			}
		};

		EasyMock.replay(controlParser, streamingParserFactory, streamingParser);

		app.init(resolver, controlMap);

		var result = app.getStreamingParser(ControlKey.FIP_INPUT_YIELD_LAYER);

		assertThat(result, is(streamingParser));

		EasyMock.verify(controlParser, streamingParserFactory, streamingParser);

		app.close();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	void testGetStreamingParserMapEntryMissing() throws IOException, ResourceParseException, ProcessingException {
		BaseControlParser controlParser = EasyMock.createMock(BaseControlParser.class);

		MockFileResolver resolver = new MockFileResolver("Test");
		var controlMap = new HashMap<String, Object>();

		controlMap.put(ControlKey.VRI_OUTPUT_VDYP_POLYGON.name(), "DUMMY1");
		controlMap.put(ControlKey.VRI_OUTPUT_VDYP_LAYER_BY_SPECIES.name(), "DUMMY2");
		controlMap.put(ControlKey.VRI_OUTPUT_VDYP_LAYER_BY_SP0_BY_UTIL.name(), "DUMMY3");

		resolver.addStream("DUMMY1", new ByteArrayOutputStream());
		resolver.addStream("DUMMY2", new ByteArrayOutputStream());
		resolver.addStream("DUMMY3", new ByteArrayOutputStream());

		var app = new VdypStartApplication() {

			@Override
			protected BaseControlParser getControlFileParser() {
				return controlParser;
			}

			@Override
			public void process() throws ProcessingException {
				// Do Nothing
			}

			@Override
			public VdypApplicationIdentifier getId() {
				return VdypApplicationIdentifier.FIP_START;
			}
		};

		EasyMock.replay(controlParser);

		app.init(resolver, controlMap);

		var ex = assertThrows(
				ProcessingException.class, () -> app.getStreamingParser(ControlKey.FIP_INPUT_YIELD_LAYER)
		);
		assertThat(ex, hasProperty("message", is("Data file FIP_INPUT_YIELD_LAYER not specified in control map.")));

		EasyMock.verify(controlParser);

		app.close();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	void testGetStreamingParserErrorOpeningFile() throws IOException, ResourceParseException, ProcessingException {
		BaseControlParser controlParser = EasyMock.createMock(BaseControlParser.class);
		StreamingParserFactory streamingParserFactory = EasyMock.createMock(StreamingParserFactory.class);

		MockFileResolver resolver = new MockFileResolver("Test");
		var controlMap = new HashMap<String, Object>();

		controlMap.put(ControlKey.VRI_OUTPUT_VDYP_POLYGON.name(), "DUMMY1");
		controlMap.put(ControlKey.VRI_OUTPUT_VDYP_LAYER_BY_SPECIES.name(), "DUMMY2");
		controlMap.put(ControlKey.VRI_OUTPUT_VDYP_LAYER_BY_SP0_BY_UTIL.name(), "DUMMY3");
		controlMap.put(ControlKey.FIP_INPUT_YIELD_LAYER.name(), streamingParserFactory);

		resolver.addStream("DUMMY1", new ByteArrayOutputStream());
		resolver.addStream("DUMMY2", new ByteArrayOutputStream());
		resolver.addStream("DUMMY3", new ByteArrayOutputStream());

		IOException exception = new IOException("This is a Test");
		EasyMock.expect(streamingParserFactory.get()).andThrow(exception);

		var app = new VdypStartApplication() {

			@Override
			protected BaseControlParser getControlFileParser() {
				return controlParser;
			}

			@Override
			public void process() throws ProcessingException {
				// Do Nothing
			}

			@Override
			public VdypApplicationIdentifier getId() {
				return VdypApplicationIdentifier.FIP_START;
			}
		};

		EasyMock.replay(controlParser, streamingParserFactory);

		app.init(resolver, controlMap);

		var ex = assertThrows(
				ProcessingException.class, () -> app.getStreamingParser(ControlKey.FIP_INPUT_YIELD_LAYER)
		);
		assertThat(ex, hasProperty("message", is("Error while opening data file.")));
		assertThat(ex, causedBy(is(exception)));

		EasyMock.verify(controlParser, streamingParserFactory);

		app.close();
	}

}
