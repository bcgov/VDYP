package ca.bc.gov.nrs.vdyp.fip;

import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.assertEmpty;
import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.assertNext;
import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.notPresent;
import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.present;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import ca.bc.gov.nrs.vdyp.fip.model.FipMode;
import ca.bc.gov.nrs.vdyp.fip.model.FipPolygon;
import ca.bc.gov.nrs.vdyp.io.parse.BecDefinitionParserTest;
import ca.bc.gov.nrs.vdyp.io.parse.StreamingParser;
import ca.bc.gov.nrs.vdyp.io.parse.StreamingParserFactory;
import ca.bc.gov.nrs.vdyp.test.TestUtils;
import ca.bc.gov.nrs.vdyp.test.VdypMatchers;

public class FipPolygonParserTest {

	@Test
	public void testParseEmpty() throws Exception {

		var parser = new FipPolygonParser();

		Map<String, Object> controlMap = new HashMap<>();

		controlMap.put(FipPolygonParser.CONTROL_KEY, "test.dat");
		BecDefinitionParserTest.populateControlMapReal(controlMap);

		var fileResolver = TestUtils.fileResolver("test.dat", TestUtils.makeStream(/* empty */));

		parser.modify(controlMap, fileResolver);

		var parserFactory = controlMap.get(FipPolygonParser.CONTROL_KEY);

		assertThat(parserFactory, instanceOf(StreamingParserFactory.class));

		@SuppressWarnings("unchecked")
		var stream = ((StreamingParserFactory<FipPolygon>) parserFactory).get();

		assertThat(stream, instanceOf(StreamingParser.class));

		assertEmpty(stream);
	}

	@Test
	public void testParsePolygon() throws Exception {

		var parser = new FipPolygonParser();

		Map<String, Object> controlMap = new HashMap<>();

		controlMap.put(FipPolygonParser.CONTROL_KEY, "test.dat");
		BecDefinitionParserTest.populateControlMapReal(controlMap);

		var fileResolver = TestUtils
				.fileResolver("test.dat", TestUtils.makeStream("Test Polygon              A CWH  90.0  2 BLAH  0.95"));

		parser.modify(controlMap, fileResolver);

		var parserFactory = controlMap.get(FipPolygonParser.CONTROL_KEY);

		assertThat(parserFactory, instanceOf(StreamingParserFactory.class));

		@SuppressWarnings("unchecked")
		var stream = ((StreamingParserFactory<FipPolygon>) parserFactory).get();

		assertThat(stream, instanceOf(StreamingParser.class));

		var poly = assertNext(stream);

		assertThat(poly, hasProperty("polygonIdentifier", is("Test Polygon")));
		assertThat(poly, hasProperty("forestInventoryZone", is("A")));
		assertThat(poly, hasProperty("biogeoclimaticZone", is("CWH")));
		assertThat(poly, hasProperty("percentAvailable", present(is(90.0f))));
		assertThat(poly, hasProperty("modeFip", present(is(FipMode.FIPYOUNG))));
		assertThat(poly, hasProperty("nonproductiveDescription", present(is("BLAH"))));
		assertThat(poly, hasProperty("yieldFactor", is(0.95f)));

		VdypMatchers.assertEmpty(stream);
	}

	@Test
	public void testParsePolygonWithBlanks() throws Exception {

		var parser = new FipPolygonParser();

		Map<String, Object> controlMap = new HashMap<>();

		controlMap.put(FipPolygonParser.CONTROL_KEY, "test.dat");
		BecDefinitionParserTest.populateControlMapReal(controlMap);

		var fileResolver = TestUtils
				.fileResolver("test.dat", TestUtils.makeStream("01002 S000001 00     1970 A CWH                    "));

		parser.modify(controlMap, fileResolver);

		var parserFactory = controlMap.get(FipPolygonParser.CONTROL_KEY);

		assertThat(parserFactory, instanceOf(StreamingParserFactory.class));

		@SuppressWarnings("unchecked")
		var stream = ((StreamingParserFactory<FipPolygon>) parserFactory).get();

		assertThat(stream, instanceOf(StreamingParser.class));

		var poly = assertNext(stream);

		assertThat(poly, hasProperty("polygonIdentifier", is("01002 S000001 00     1970")));
		assertThat(poly, hasProperty("forestInventoryZone", is("A")));
		assertThat(poly, hasProperty("biogeoclimaticZone", is("CWH")));
		assertThat(poly, hasProperty("percentAvailable", notPresent()));
		assertThat(poly, hasProperty("modeFip", notPresent()));
		assertThat(poly, hasProperty("nonproductiveDescription", notPresent()));
		assertThat(poly, hasProperty("yieldFactor", is(1.0f)));

		VdypMatchers.assertEmpty(stream);
	}

	@Test
	public void testParseMultiple() throws Exception {

		var parser = new FipPolygonParser();

		Map<String, Object> controlMap = new HashMap<>();

		controlMap.put(FipPolygonParser.CONTROL_KEY, "test.dat");
		BecDefinitionParserTest.populateControlMapReal(controlMap);

		var fileResolver = TestUtils.fileResolver(
				"test.dat",
				TestUtils.makeStream(
						"01002 S000001 00     1970 A CWH                1.00",
						"01002 S000002 00     1970 A CWH                1.00",
						"01002 S000003 00     1970 A CWH                1.00",
						"01002 S000004 00     1970 A CWH                1.00",
						"01003AS000001 00     1953 B CWH                1.00",
						"01003AS000003 00     1953 B CWH                1.00",
						"01004 S000002 00     1953 B CWH                1.00",
						"01004 S000036 00     1957 B CWH                1.00",
						"01004 S000037 00     1957 B CWH                1.00",
						"01004 S000038 00     1957 B CWH                1.00"
				)
		);

		parser.modify(controlMap, fileResolver);

		var parserFactory = controlMap.get(FipPolygonParser.CONTROL_KEY);

		assertThat(parserFactory, instanceOf(StreamingParserFactory.class));

		@SuppressWarnings("unchecked")
		var stream = ((StreamingParserFactory<FipPolygon>) parserFactory).get();

		assertThat(stream, instanceOf(StreamingParser.class));

		var poly = assertNext(stream);

		assertThat(poly, hasProperty("polygonIdentifier", is("01002 S000001 00     1970")));
		assertThat(poly, hasProperty("forestInventoryZone", is("A")));
		assertThat(poly, hasProperty("biogeoclimaticZone", is("CWH")));
		assertThat(poly, hasProperty("percentAvailable", notPresent()));
		assertThat(poly, hasProperty("modeFip", notPresent()));
		assertThat(poly, hasProperty("nonproductiveDescription", notPresent()));
		assertThat(poly, hasProperty("yieldFactor", is(1.0f)));

		poly = assertNext(stream);

		assertThat(poly, hasProperty("polygonIdentifier", is("01002 S000002 00     1970")));
		assertThat(poly, hasProperty("forestInventoryZone", is("A")));
		assertThat(poly, hasProperty("biogeoclimaticZone", is("CWH")));
		assertThat(poly, hasProperty("percentAvailable", notPresent()));
		assertThat(poly, hasProperty("modeFip", notPresent()));
		assertThat(poly, hasProperty("nonproductiveDescription", notPresent()));
		assertThat(poly, hasProperty("yieldFactor", is(1.0f)));

		poly = assertNext(stream);

		assertThat(poly, hasProperty("polygonIdentifier", is("01002 S000003 00     1970")));
		assertThat(poly, hasProperty("forestInventoryZone", is("A")));
		assertThat(poly, hasProperty("biogeoclimaticZone", is("CWH")));
		assertThat(poly, hasProperty("percentAvailable", notPresent()));
		assertThat(poly, hasProperty("modeFip", notPresent()));
		assertThat(poly, hasProperty("nonproductiveDescription", notPresent()));
		assertThat(poly, hasProperty("yieldFactor", is(1.0f)));

		poly = assertNext(stream);

		assertThat(poly, hasProperty("polygonIdentifier", is("01002 S000004 00     1970")));
		assertThat(poly, hasProperty("forestInventoryZone", is("A")));
		assertThat(poly, hasProperty("biogeoclimaticZone", is("CWH")));
		assertThat(poly, hasProperty("percentAvailable", notPresent()));
		assertThat(poly, hasProperty("modeFip", notPresent()));
		assertThat(poly, hasProperty("nonproductiveDescription", notPresent()));
		assertThat(poly, hasProperty("yieldFactor", is(1.0f)));

		poly = assertNext(stream);

		assertThat(poly, hasProperty("polygonIdentifier", is("01003AS000001 00     1953")));
		assertThat(poly, hasProperty("forestInventoryZone", is("B")));
		assertThat(poly, hasProperty("biogeoclimaticZone", is("CWH")));
		assertThat(poly, hasProperty("percentAvailable", notPresent()));
		assertThat(poly, hasProperty("modeFip", notPresent()));
		assertThat(poly, hasProperty("nonproductiveDescription", notPresent()));
		assertThat(poly, hasProperty("yieldFactor", is(1.0f)));

		poly = assertNext(stream);

		assertThat(poly, hasProperty("polygonIdentifier", is("01003AS000003 00     1953")));
		assertThat(poly, hasProperty("forestInventoryZone", is("B")));
		assertThat(poly, hasProperty("biogeoclimaticZone", is("CWH")));
		assertThat(poly, hasProperty("percentAvailable", notPresent()));
		assertThat(poly, hasProperty("modeFip", notPresent()));
		assertThat(poly, hasProperty("nonproductiveDescription", notPresent()));
		assertThat(poly, hasProperty("yieldFactor", is(1.0f)));

		poly = assertNext(stream);

		assertThat(poly, hasProperty("polygonIdentifier", is("01004 S000002 00     1953")));
		assertThat(poly, hasProperty("forestInventoryZone", is("B")));
		assertThat(poly, hasProperty("biogeoclimaticZone", is("CWH")));
		assertThat(poly, hasProperty("percentAvailable", notPresent()));
		assertThat(poly, hasProperty("modeFip", notPresent()));
		assertThat(poly, hasProperty("nonproductiveDescription", notPresent()));
		assertThat(poly, hasProperty("yieldFactor", is(1.0f)));

		poly = assertNext(stream);

		assertThat(poly, hasProperty("polygonIdentifier", is("01004 S000036 00     1957")));
		assertThat(poly, hasProperty("forestInventoryZone", is("B")));
		assertThat(poly, hasProperty("biogeoclimaticZone", is("CWH")));
		assertThat(poly, hasProperty("percentAvailable", notPresent()));
		assertThat(poly, hasProperty("modeFip", notPresent()));
		assertThat(poly, hasProperty("nonproductiveDescription", notPresent()));
		assertThat(poly, hasProperty("yieldFactor", is(1.0f)));

		poly = assertNext(stream);

		assertThat(poly, hasProperty("polygonIdentifier", is("01004 S000037 00     1957")));
		assertThat(poly, hasProperty("forestInventoryZone", is("B")));
		assertThat(poly, hasProperty("biogeoclimaticZone", is("CWH")));
		assertThat(poly, hasProperty("percentAvailable", notPresent()));
		assertThat(poly, hasProperty("modeFip", notPresent()));
		assertThat(poly, hasProperty("nonproductiveDescription", notPresent()));
		assertThat(poly, hasProperty("yieldFactor", is(1.0f)));

		poly = assertNext(stream);

		assertThat(poly, hasProperty("polygonIdentifier", is("01004 S000038 00     1957")));
		assertThat(poly, hasProperty("forestInventoryZone", is("B")));
		assertThat(poly, hasProperty("biogeoclimaticZone", is("CWH")));
		assertThat(poly, hasProperty("percentAvailable", notPresent()));
		assertThat(poly, hasProperty("modeFip", notPresent()));
		assertThat(poly, hasProperty("nonproductiveDescription", notPresent()));
		assertThat(poly, hasProperty("yieldFactor", is(1.0f)));

		VdypMatchers.assertEmpty(stream);
	}

	@Test
	public void testParsePolygonZeroAsDefault() throws Exception {

		var parser = new FipPolygonParser();

		Map<String, Object> controlMap = new HashMap<>();

		controlMap.put(FipPolygonParser.CONTROL_KEY, "test.dat");
		BecDefinitionParserTest.populateControlMapReal(controlMap);

		var fileResolver = TestUtils
				.fileResolver("test.dat", TestUtils.makeStream("01002 S000001 00     1970 A CWH   0.0  0       0.00"));

		parser.modify(controlMap, fileResolver);

		var parserFactory = controlMap.get(FipPolygonParser.CONTROL_KEY);

		assertThat(parserFactory, instanceOf(StreamingParserFactory.class));

		@SuppressWarnings("unchecked")
		var stream = ((StreamingParserFactory<FipPolygon>) parserFactory).get();

		assertThat(stream, instanceOf(StreamingParser.class));

		var poly = assertNext(stream);

		assertThat(poly, hasProperty("polygonIdentifier", is("01002 S000001 00     1970")));
		assertThat(poly, hasProperty("forestInventoryZone", is("A")));
		assertThat(poly, hasProperty("biogeoclimaticZone", is("CWH")));
		assertThat(poly, hasProperty("percentAvailable", notPresent()));
		assertThat(poly, hasProperty("modeFip", notPresent()));
		assertThat(poly, hasProperty("nonproductiveDescription", notPresent()));
		assertThat(poly, hasProperty("yieldFactor", is(1.0f)));

		VdypMatchers.assertEmpty(stream);
	}

	@Test
	public void testParsePolygonNegativeAsDefault() throws Exception {

		var parser = new FipPolygonParser();

		Map<String, Object> controlMap = new HashMap<>();

		controlMap.put(FipPolygonParser.CONTROL_KEY, "test.dat");
		BecDefinitionParserTest.populateControlMapReal(controlMap);

		var fileResolver = TestUtils
				.fileResolver("test.dat", TestUtils.makeStream("01002 S000001 00     1970 A CWH  -1.0         -1.00"));

		parser.modify(controlMap, fileResolver);

		var parserFactory = controlMap.get(FipPolygonParser.CONTROL_KEY);

		assertThat(parserFactory, instanceOf(StreamingParserFactory.class));

		@SuppressWarnings("unchecked")
		var stream = ((StreamingParserFactory<FipPolygon>) parserFactory).get();

		assertThat(stream, instanceOf(StreamingParser.class));

		var poly = assertNext(stream);

		assertThat(poly, hasProperty("polygonIdentifier", is("01002 S000001 00     1970")));
		assertThat(poly, hasProperty("forestInventoryZone", is("A")));
		assertThat(poly, hasProperty("biogeoclimaticZone", is("CWH")));
		assertThat(poly, hasProperty("percentAvailable", notPresent()));
		assertThat(poly, hasProperty("modeFip", notPresent()));
		assertThat(poly, hasProperty("nonproductiveDescription", notPresent()));
		assertThat(poly, hasProperty("yieldFactor", is(1.0f)));

		VdypMatchers.assertEmpty(stream);
	}
}
