package ca.bc.gov.nrs.vdyp.fip;

import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.assertEmpty;
import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.assertNext;
import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.hasSpecificEntry;
import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.isPolyId;
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

import ca.bc.gov.nrs.vdyp.common.ControlKey;
import ca.bc.gov.nrs.vdyp.fip.model.FipLayer;
import ca.bc.gov.nrs.vdyp.io.parse.streaming.StreamingParser;
import ca.bc.gov.nrs.vdyp.io.parse.streaming.StreamingParserFactory;
import ca.bc.gov.nrs.vdyp.model.LayerType;
import ca.bc.gov.nrs.vdyp.test.TestUtils;

class FipLayerParserTest {

	@Test
	void testParseEmpty() throws Exception {

		var parser = new FipLayerParser();

		Map<String, Object> controlMap = new HashMap<>();

		controlMap.put(ControlKey.FIP_INPUT_YIELD_LAYER.name(), "test.dat");
		TestUtils.populateControlMapBecReal(controlMap);

		var fileResolver = TestUtils.fileResolver("test.dat", TestUtils.makeInputStream(/* empty */));

		parser.modify(controlMap, fileResolver);

		var parserFactory = controlMap.get(ControlKey.FIP_INPUT_YIELD_LAYER.name());

		assertThat(parserFactory, instanceOf(StreamingParserFactory.class));

		@SuppressWarnings("unchecked")
		var stream = ((StreamingParserFactory<FipLayer>) parserFactory).get();

		assertThat(stream, instanceOf(StreamingParser.class));

		assertEmpty(stream);
	}

	@Test
	void testParseLayer() throws Exception {

		var parser = new FipLayerParser();

		Map<String, Object> controlMap = new HashMap<>();

		controlMap.put(ControlKey.FIP_INPUT_YIELD_LAYER.name(), "test.dat");
		TestUtils.populateControlMapGenusReal(controlMap);

		var fileResolver = TestUtils.fileResolver(
				"test.dat",
				TestUtils.makeInputStream(
						"01002 S000001 00     1970 1  55 35.3 35.0 87.4   D  D  1.0 0              13",
						"01002 S000001 00     1970 Z  55  0.0  0.0  0.0         0.0"
				)
		);

		parser.modify(controlMap, fileResolver);

		var parserFactory = controlMap.get(ControlKey.FIP_INPUT_YIELD_LAYER.name());

		assertThat(parserFactory, instanceOf(StreamingParserFactory.class));

		@SuppressWarnings("unchecked")
		var stream = ((StreamingParserFactory<Map<LayerType, FipLayer>>) parserFactory).get();

		assertThat(stream, instanceOf(StreamingParser.class));

		var layers = assertNext(stream);

		assertThat(layers, aMapWithSize(1));

		assertThat(
				layers,
				hasSpecificEntry(
						LayerType.PRIMARY, allOf(
								hasProperty("polygonIdentifier", isPolyId("01002 S000001 00", 1970)), //
								hasProperty("layerType", is(LayerType.PRIMARY)), //
								hasProperty("ageTotalSafe", is(55f)), //
								hasProperty("heightSafe", is(35.3f)), //
								hasProperty("siteIndex", present(is(35.0f))), //
								hasProperty("crownClosure", is(87.4f)), //
								hasProperty("siteGenus", present(is("D"))), //
								hasProperty("siteSpecies", present(is("D"))), //
								hasProperty("yearsToBreastHeightSafe", is(1.0f)), //
								hasProperty("stockingClass", present(is('0'))), //
								hasProperty("inventoryTypeGroup", notPresent()), //
								hasProperty("siteCurveNumber", present(is(13)))
						)
				)
		);

		assertEmpty(stream);
	}

	@Test
	void testParseTwoLayers() throws Exception {

		var parser = new FipLayerParser();

		Map<String, Object> controlMap = new HashMap<>();

		controlMap.put(ControlKey.FIP_INPUT_YIELD_LAYER.name(), "test.dat");
		TestUtils.populateControlMapGenusReal(controlMap);

		var fileResolver = TestUtils.fileResolver(
				"test.dat",
				TestUtils.makeInputStream(
						"01002 S000004 00     1970 V 195 45.2 22.3  4.0   B  B  9.4 2               8",
						"01002 S000004 00     1970 1  85 42.3 31.9 82.8   H  H  4.9 0              34",
						"01002 S000004 00     1970 Z  85  0.0  0.0  0.0         0.0"
				)
		);

		parser.modify(controlMap, fileResolver);

		var parserFactory = controlMap.get(ControlKey.FIP_INPUT_YIELD_LAYER.name());

		assertThat(parserFactory, instanceOf(StreamingParserFactory.class));

		@SuppressWarnings("unchecked")
		var stream = ((StreamingParserFactory<Map<LayerType, FipLayer>>) parserFactory).get();

		assertThat(stream, instanceOf(StreamingParser.class));

		var layers = assertNext(stream);

		assertThat(layers, aMapWithSize(2));

		assertThat(
				layers,
				hasSpecificEntry(
						LayerType.PRIMARY, allOf(
								hasProperty("polygonIdentifier", isPolyId("01002 S000004 00", 1970)), //
								hasProperty("layerType", is(LayerType.PRIMARY)), //
								hasProperty("ageTotalSafe", is(85f)), //
								hasProperty("heightSafe", is(42.3f)), //
								hasProperty("siteIndex", present(is(31.9f))), //
								hasProperty("crownClosure", is(82.8f)), //
								hasProperty("siteGenus", present(is("H"))), //
								hasProperty("siteSpecies", present(is("H"))), //
								hasProperty("yearsToBreastHeightSafe", is(4.9f)), //
								hasProperty("stockingClass", present(is('0'))), //
								hasProperty("inventoryTypeGroup", notPresent()), //
								hasProperty("siteCurveNumber", present(is(34)))
						)
				)
		);
		assertThat(
				layers,
				hasSpecificEntry(
						LayerType.VETERAN, allOf(
								hasProperty("polygonIdentifier", isPolyId("01002 S000004 00", 1970)), //
								hasProperty("layerType", is(LayerType.VETERAN)), //
								hasProperty("ageTotalSafe", is(195f)), //
								hasProperty("heightSafe", is(45.2f)), //
								hasProperty("siteIndex", present(is(22.3f))), //
								hasProperty("crownClosure", is(4.0f)), //
								hasProperty("siteGenus", present(is("B"))), //
								hasProperty("siteSpecies", present(is("B"))), //
								hasProperty("yearsToBreastHeightSafe", is(9.4f))
								// hasProperty("stockingClass", present(is("2"))),
								// hasProperty("siteCurveNumber", present(is(8)))
						)
				)
		);

		assertEmpty(stream);
	}

	@Test
	void testIgnoreLayerIfHeightZero() throws Exception {

		var parser = new FipLayerParser();

		Map<String, Object> controlMap = new HashMap<>();

		controlMap.put(ControlKey.FIP_INPUT_YIELD_LAYER.name(), "test.dat");
		TestUtils.populateControlMapGenusReal(controlMap);

		var fileResolver = TestUtils.fileResolver(
				"test.dat",
				TestUtils.makeInputStream(
						"01002 S000004 00     1970 V 195 45.2 22.3  0.0   B  B  9.4 2               8",
						"01002 S000004 00     1970 1  85 42.3 31.9 82.8   H  H  4.9 0              34",
						"01002 S000004 00     1970 Z  85  0.0  0.0  0.0         0.0"
				)
		);

		parser.modify(controlMap, fileResolver);

		var parserFactory = controlMap.get(ControlKey.FIP_INPUT_YIELD_LAYER.name());

		assertThat(parserFactory, instanceOf(StreamingParserFactory.class));

		@SuppressWarnings("unchecked")
		var stream = ((StreamingParserFactory<Map<LayerType, FipLayer>>) parserFactory).get();

		assertThat(stream, instanceOf(StreamingParser.class));

		var layers = assertNext(stream);

		assertThat(layers, aMapWithSize(1));

		assertThat(
				layers,
				hasSpecificEntry(
						LayerType.PRIMARY, allOf(
								hasProperty("polygonIdentifier", isPolyId("01002 S000004 00", 1970)), //
								hasProperty("layerType", is(LayerType.PRIMARY)), //
								hasProperty("ageTotalSafe", is(85f)), //
								hasProperty("heightSafe", is(42.3f)), //
								hasProperty("siteIndex", present(is(31.9f))), //
								hasProperty("crownClosure", is(82.8f)), //
								hasProperty("siteGenus", present(is("H"))), //
								hasProperty("siteSpecies", present(is("H"))), //
								hasProperty("yearsToBreastHeightSafe", is(4.9f)), //
								hasProperty("stockingClass", present(is('0'))), //
								hasProperty("inventoryTypeGroup", notPresent()), //
								hasProperty("siteCurveNumber", present(is(34)))
						)
				)
		);

		assertEmpty(stream);
	}

	@Test
	void testIgnoreLayerIfCrownClosureZero() throws Exception {

		var parser = new FipLayerParser();

		Map<String, Object> controlMap = new HashMap<>();

		controlMap.put(ControlKey.FIP_INPUT_YIELD_LAYER.name(), "test.dat");
		TestUtils.populateControlMapGenusReal(controlMap);

		var fileResolver = TestUtils.fileResolver(
				"test.dat",
				TestUtils.makeInputStream(
						"01002 S000004 00     1970 V 195  0.0 22.3  4.0   B  B  9.4 2               8",
						"01002 S000004 00     1970 1  85 42.3 31.9 82.8   H  H  4.9 0              34",
						"01002 S000004 00     1970 Z  85  0.0  0.0  0.0         0.0"
				)
		);

		parser.modify(controlMap, fileResolver);

		var parserFactory = controlMap.get(ControlKey.FIP_INPUT_YIELD_LAYER.name());

		assertThat(parserFactory, instanceOf(StreamingParserFactory.class));

		@SuppressWarnings("unchecked")
		var stream = ((StreamingParserFactory<Map<LayerType, FipLayer>>) parserFactory).get();

		assertThat(stream, instanceOf(StreamingParser.class));

		var layers = assertNext(stream);

		assertThat(layers, aMapWithSize(1));

		assertThat(
				layers,
				hasSpecificEntry(
						LayerType.PRIMARY, allOf(
								hasProperty("polygonIdentifier", isPolyId("01002 S000004 00", 1970)), //
								hasProperty("layerType", is(LayerType.PRIMARY)), //
								hasProperty("ageTotalSafe", is(85f)), //
								hasProperty("heightSafe", is(42.3f)), //
								hasProperty("siteIndex", present(is(31.9f))), //
								hasProperty("crownClosure", is(82.8f)), //
								hasProperty("siteGenus", present(is("H"))), //
								hasProperty("siteSpecies", present(is("H"))), //
								hasProperty("yearsToBreastHeightSafe", is(4.9f)),
								hasProperty("stockingClass", present(is('0'))), //
								hasProperty("inventoryTypeGroup", notPresent()), //
								hasProperty("siteCurveNumber", present(is(34)))
						)
				)
		);

		assertEmpty(stream);
	}

}
