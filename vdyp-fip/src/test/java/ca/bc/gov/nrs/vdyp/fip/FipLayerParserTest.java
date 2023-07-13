package ca.bc.gov.nrs.vdyp.fip;

import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.assertEmpty;
import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.assertNext;
import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.hasSpecificEntry;
import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.notPresent;
import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.present;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import ca.bc.gov.nrs.vdyp.fip.model.FipLayer;
import ca.bc.gov.nrs.vdyp.io.parse.BecDefinitionParserTest;
import ca.bc.gov.nrs.vdyp.io.parse.GenusDefinitionParserTest;
import ca.bc.gov.nrs.vdyp.io.parse.StreamingParser;
import ca.bc.gov.nrs.vdyp.io.parse.StreamingParserFactory;
import ca.bc.gov.nrs.vdyp.model.Layer;
import ca.bc.gov.nrs.vdyp.test.TestUtils;

public class FipLayerParserTest {

	@Test
	public void testParseEmpty() throws Exception {

		var parser = new FipLayerParser();

		Map<String, Object> controlMap = new HashMap<>();

		controlMap.put(FipLayerParser.CONTROL_KEY, "test.dat");
		BecDefinitionParserTest.populateControlMapReal(controlMap);

		var fileResolver = TestUtils.fileResolver("test.dat", TestUtils.makeStream(/* empty */));

		parser.modify(controlMap, fileResolver);

		var parserFactory = controlMap.get(FipLayerParser.CONTROL_KEY);

		assertThat(parserFactory, instanceOf(StreamingParserFactory.class));

		@SuppressWarnings("unchecked")
		var stream = ((StreamingParserFactory<FipLayer>) parserFactory).get();

		assertThat(stream, instanceOf(StreamingParser.class));

		assertEmpty(stream);
	}

	@Test
	public void testParseLayer() throws Exception {

		var parser = new FipLayerParser();

		Map<String, Object> controlMap = new HashMap<>();

		controlMap.put(FipLayerParser.CONTROL_KEY, "test.dat");
		GenusDefinitionParserTest.populateControlMapReal(controlMap);

		var fileResolver = TestUtils.fileResolver(
				"test.dat",
				TestUtils.makeStream(
						"01002 S000001 00     1970 1  55 35.3 35.0 87.4   D  D  1.0 0              13",
						"01002 S000001 00     1970 Z  55  0.0  0.0  0.0         0.0"
				)
		);

		parser.modify(controlMap, fileResolver);

		var parserFactory = controlMap.get(FipLayerParser.CONTROL_KEY);

		assertThat(parserFactory, instanceOf(StreamingParserFactory.class));

		@SuppressWarnings("unchecked")
		var stream = ((StreamingParserFactory<Map<Layer, FipLayer>>) parserFactory).get();

		assertThat(stream, instanceOf(StreamingParser.class));

		var layers = assertNext(stream);

		assertThat(layers, aMapWithSize(1));

		assertThat(
				layers,
				hasSpecificEntry(
						Layer.PRIMARY,
						allOf(
								hasProperty("polygonIdentifier", is("01002 S000001 00     1970")),
								hasProperty("layer", is(Layer.PRIMARY)), hasProperty("ageTotal", is(55f)),
								hasProperty("height", is(35.3f)), hasProperty("siteIndex", is(35.0f)),
								hasProperty("crownClosure", is(87.4f)), hasProperty("siteSp0", is("D")),
								hasProperty("siteSp64", is("D")), hasProperty("yearsToBreastHeight", is(1.0f)),
								hasProperty("stockingClass", present(is("0"))),
								hasProperty("inventoryTypeGroup", notPresent()),
								hasProperty("breastHeightAge", notPresent()),
								hasProperty("siteCurveNumber", present(is(13)))
						)
				)
		);

		assertEmpty(stream);
	}

	@Test
	public void testParseTwoLayers() throws Exception {

		var parser = new FipLayerParser();

		Map<String, Object> controlMap = new HashMap<>();

		controlMap.put(FipLayerParser.CONTROL_KEY, "test.dat");
		GenusDefinitionParserTest.populateControlMapReal(controlMap);

		var fileResolver = TestUtils.fileResolver(
				"test.dat",
				TestUtils.makeStream(
						"01002 S000004 00     1970 V 195 45.2 22.3  4.0   B  B  9.4 2               8",
						"01002 S000004 00     1970 1  85 42.3 31.9 82.8   H  H  4.9 0              34",
						"01002 S000004 00     1970 Z  85  0.0  0.0  0.0         0.0"
				)
		);

		parser.modify(controlMap, fileResolver);

		var parserFactory = controlMap.get(FipLayerParser.CONTROL_KEY);

		assertThat(parserFactory, instanceOf(StreamingParserFactory.class));

		@SuppressWarnings("unchecked")
		var stream = ((StreamingParserFactory<Map<Layer, FipLayer>>) parserFactory).get();

		assertThat(stream, instanceOf(StreamingParser.class));

		var layers = assertNext(stream);

		assertThat(layers, aMapWithSize(2));

		assertThat(
				layers,
				hasSpecificEntry(
						Layer.PRIMARY,
						allOf(
								hasProperty("polygonIdentifier", is("01002 S000004 00     1970")),
								hasProperty("layer", is(Layer.PRIMARY)), hasProperty("ageTotal", is(85f)),
								hasProperty("height", is(42.3f)), hasProperty("siteIndex", is(31.9f)),
								hasProperty("crownClosure", is(82.8f)), hasProperty("siteSp0", is("H")),
								hasProperty("siteSp64", is("H")), hasProperty("yearsToBreastHeight", is(4.9f)),
								hasProperty("stockingClass", present(is("0"))),
								hasProperty("inventoryTypeGroup", notPresent()),
								hasProperty("breastHeightAge", notPresent()),
								hasProperty("siteCurveNumber", present(is(34)))
						)
				)
		);
		assertThat(
				layers,
				hasSpecificEntry(
						Layer.VETERAN,
						allOf(
								hasProperty("polygonIdentifier", is("01002 S000004 00     1970")),
								hasProperty("layer", is(Layer.VETERAN)), hasProperty("ageTotal", is(195f)),
								hasProperty("height", is(45.2f)), hasProperty("siteIndex", is(22.3f)),
								hasProperty("crownClosure", is(4.0f)), hasProperty("siteSp0", is("B")),
								hasProperty("siteSp64", is("B")), hasProperty("yearsToBreastHeight", is(9.4f)),
								// hasProperty("stockingClass", present(is("2"))),
								hasProperty("inventoryTypeGroup", notPresent()),
								hasProperty("breastHeightAge", notPresent())
								// hasProperty("siteCurveNumber", present(is(8)))
						)
				)
		);

		assertEmpty(stream);
	}

	@Test
	public void testIgnoreLayerIfHeightZero() throws Exception {

		var parser = new FipLayerParser();

		Map<String, Object> controlMap = new HashMap<>();

		controlMap.put(FipLayerParser.CONTROL_KEY, "test.dat");
		GenusDefinitionParserTest.populateControlMapReal(controlMap);

		var fileResolver = TestUtils.fileResolver(
				"test.dat",
				TestUtils.makeStream(
						"01002 S000004 00     1970 V 195 45.2 22.3  0.0   B  B  9.4 2               8",
						"01002 S000004 00     1970 1  85 42.3 31.9 82.8   H  H  4.9 0              34",
						"01002 S000004 00     1970 Z  85  0.0  0.0  0.0         0.0"
				)
		);

		parser.modify(controlMap, fileResolver);

		var parserFactory = controlMap.get(FipLayerParser.CONTROL_KEY);

		assertThat(parserFactory, instanceOf(StreamingParserFactory.class));

		@SuppressWarnings("unchecked")
		var stream = ((StreamingParserFactory<Map<Layer, FipLayer>>) parserFactory).get();

		assertThat(stream, instanceOf(StreamingParser.class));

		var layers = assertNext(stream);

		assertThat(layers, aMapWithSize(1));

		assertThat(
				layers,
				hasSpecificEntry(
						Layer.PRIMARY,
						allOf(
								hasProperty("polygonIdentifier", is("01002 S000004 00     1970")),
								hasProperty("layer", is(Layer.PRIMARY)), hasProperty("ageTotal", is(85f)),
								hasProperty("height", is(42.3f)), hasProperty("siteIndex", is(31.9f)),
								hasProperty("crownClosure", is(82.8f)), hasProperty("siteSp0", is("H")),
								hasProperty("siteSp64", is("H")), hasProperty("yearsToBreastHeight", is(4.9f)),
								hasProperty("stockingClass", present(is("0"))),
								hasProperty("inventoryTypeGroup", notPresent()),
								hasProperty("breastHeightAge", notPresent()),
								hasProperty("siteCurveNumber", present(is(34)))
						)
				)
		);

		assertEmpty(stream);
	}

	@Test
	public void testIgnoreLayerIfCrownClosureZero() throws Exception {

		var parser = new FipLayerParser();

		Map<String, Object> controlMap = new HashMap<>();

		controlMap.put(FipLayerParser.CONTROL_KEY, "test.dat");
		GenusDefinitionParserTest.populateControlMapReal(controlMap);

		var fileResolver = TestUtils.fileResolver(
				"test.dat",
				TestUtils.makeStream(
						"01002 S000004 00     1970 V 195  0.0 22.3  4.0   B  B  9.4 2               8",
						"01002 S000004 00     1970 1  85 42.3 31.9 82.8   H  H  4.9 0              34",
						"01002 S000004 00     1970 Z  85  0.0  0.0  0.0         0.0"
				)
		);

		parser.modify(controlMap, fileResolver);

		var parserFactory = controlMap.get(FipLayerParser.CONTROL_KEY);

		assertThat(parserFactory, instanceOf(StreamingParserFactory.class));

		@SuppressWarnings("unchecked")
		var stream = ((StreamingParserFactory<Map<Layer, FipLayer>>) parserFactory).get();

		assertThat(stream, instanceOf(StreamingParser.class));

		var layers = assertNext(stream);

		assertThat(layers, aMapWithSize(1));

		assertThat(
				layers,
				hasSpecificEntry(
						Layer.PRIMARY,
						allOf(
								hasProperty("polygonIdentifier", is("01002 S000004 00     1970")),
								hasProperty("layer", is(Layer.PRIMARY)), hasProperty("ageTotal", is(85f)),
								hasProperty("height", is(42.3f)), hasProperty("siteIndex", is(31.9f)),
								hasProperty("crownClosure", is(82.8f)), hasProperty("siteSp0", is("H")),
								hasProperty("siteSp64", is("H")), hasProperty("yearsToBreastHeight", is(4.9f)),
								hasProperty("stockingClass", present(is("0"))),
								hasProperty("inventoryTypeGroup", notPresent()),
								hasProperty("breastHeightAge", notPresent()),
								hasProperty("siteCurveNumber", present(is(34)))
						)
				)
		);

		assertEmpty(stream);
	}

}
