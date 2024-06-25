package ca.bc.gov.nrs.vdyp.vri;

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
import ca.bc.gov.nrs.vdyp.io.parse.streaming.StreamingParser;
import ca.bc.gov.nrs.vdyp.io.parse.streaming.StreamingParserFactory;
import ca.bc.gov.nrs.vdyp.model.LayerType;
import ca.bc.gov.nrs.vdyp.test.TestUtils;
import ca.bc.gov.nrs.vdyp.test.VdypMatchers;
import ca.bc.gov.nrs.vdyp.vri.model.VriLayer;

class VriLayerParserTest {

	@Test
	void testParseEmpty() throws Exception {

		var parser = new VriLayerParser();

		Map<String, Object> controlMap = new HashMap<>();

		controlMap.put(ControlKey.VRI_INPUT_YIELD_LAYER.name(), "test.dat");
		TestUtils.populateControlMapBecReal(controlMap);

		var fileResolver = TestUtils.fileResolver("test.dat", TestUtils.makeInputStream(/* empty */));

		parser.modify(controlMap, fileResolver);

		var parserFactory = controlMap.get(ControlKey.VRI_INPUT_YIELD_LAYER.name());

		assertThat(parserFactory, instanceOf(StreamingParserFactory.class));

		@SuppressWarnings("unchecked")
		var stream = ((StreamingParserFactory<Map<LayerType, VriLayer.Builder>>) parserFactory).get();

		assertThat(stream, instanceOf(StreamingParser.class));

		assertEmpty(stream);
	}

	@Test
	void testParsePrimaryLayer() throws Exception {

		var parser = new VriLayerParser();

		Map<String, Object> controlMap = new HashMap<>();

		controlMap.put(ControlKey.VRI_INPUT_YIELD_LAYER.name(), "test.dat");

		var fileResolver = TestUtils.fileResolver(
				"test.dat", TestUtils.makeInputStream(
						"082F074/0071         2001 P  57.8     66.0      850  7.5", //
						"082F074/0071         2001 Z"
				)
		);

		parser.modify(controlMap, fileResolver);

		var parserFactory = controlMap.get(ControlKey.VRI_INPUT_YIELD_LAYER.name());

		assertThat(parserFactory, instanceOf(StreamingParserFactory.class));

		@SuppressWarnings("unchecked")
		var stream = ((StreamingParserFactory<Map<LayerType, VriLayer.Builder>>) parserFactory).get();

		assertThat(stream, instanceOf(StreamingParser.class));

		var layers = assertNext(stream);

		assertThat(layers, aMapWithSize(1));

		assertThat(
				layers,
				hasSpecificEntry(
						LayerType.PRIMARY,
						VdypMatchers.builds(
								allOf(
										hasProperty("polygonIdentifier", isPolyId("082F074/0071", 2001)), //
										hasProperty("layerType", is(LayerType.PRIMARY)), //
										hasProperty("crownClosure", is(57.8f)), //
										hasProperty("baseArea", present(is(66.0f))), //
										hasProperty("treesPerHectare", present(is(850f))), //
										hasProperty("utilization", is(7.5f))
								)
						)
				)
		);

		assertEmpty(stream);
	}

	@Test
	void testParseVeteranLayer() throws Exception {

		var parser = new VriLayerParser();

		Map<String, Object> controlMap = new HashMap<>();

		controlMap.put(ControlKey.VRI_INPUT_YIELD_LAYER.name(), "test.dat");

		var fileResolver = TestUtils.fileResolver(
				"test.dat", TestUtils.makeInputStream(
						"082F074/0071         2001 V  57.8     66.0      850  7.5", //
						"082F074/0071         2001 Z"
				)
		);

		parser.modify(controlMap, fileResolver);

		var parserFactory = controlMap.get(ControlKey.VRI_INPUT_YIELD_LAYER.name());

		assertThat(parserFactory, instanceOf(StreamingParserFactory.class));

		@SuppressWarnings("unchecked")
		var stream = ((StreamingParserFactory<Map<LayerType, VriLayer.Builder>>) parserFactory).get();

		assertThat(stream, instanceOf(StreamingParser.class));

		var layers = assertNext(stream);

		assertThat(layers, aMapWithSize(1));

		assertThat(
				layers,
				hasSpecificEntry(
						LayerType.VETERAN,
						VdypMatchers.builds(
								allOf(
										hasProperty("polygonIdentifier", isPolyId("082F074/0071", 2001)), //
										hasProperty("layerType", is(LayerType.VETERAN)), //
										hasProperty("crownClosure", is(57.8f)), //
										hasProperty("baseArea", present(is(66.0f))), //
										hasProperty("treesPerHectare", present(is(850f))), //
										hasProperty("utilization", is(7.5f))
								)
						)
				)
		);

		assertEmpty(stream);
	}

	@Test
	void testParseTwoLayers() throws Exception {

		var parser = new VriLayerParser();

		Map<String, Object> controlMap = new HashMap<>();

		controlMap.put(ControlKey.VRI_INPUT_YIELD_LAYER.name(), "test.dat");
		TestUtils.populateControlMapGenusReal(controlMap);

		var fileResolver = TestUtils.fileResolver(
				"test.dat", TestUtils.makeInputStream(
						"082F074/0071         2001 P  57.8     66.0      850  7.5", //
						"082F074/0071         2001 V  30.0     -9.0       -9  7.5", //
						"082F074/0071         2001 Z"
				)
		);

		parser.modify(controlMap, fileResolver);

		var parserFactory = controlMap.get(ControlKey.VRI_INPUT_YIELD_LAYER.name());

		assertThat(parserFactory, instanceOf(StreamingParserFactory.class));

		@SuppressWarnings("unchecked")
		var stream = ((StreamingParserFactory<Map<LayerType, VriLayer.Builder>>) parserFactory).get();

		assertThat(stream, instanceOf(StreamingParser.class));

		var layers = assertNext(stream);

		assertThat(layers, aMapWithSize(2));

		assertThat(
				layers,
				hasSpecificEntry(
						LayerType.PRIMARY,
						VdypMatchers.builds(
								allOf(
										hasProperty("polygonIdentifier", isPolyId("082F074/0071", 2001)), //
										hasProperty("layerType", is(LayerType.PRIMARY)), //
										hasProperty("crownClosure", is(57.8f)), //
										hasProperty("baseArea", present(is(66.0f))), //
										hasProperty("treesPerHectare", present(is(850f))), //
										hasProperty("utilization", is(7.5f))
								)
						)
				)
		);
		assertThat(
				layers,
				hasSpecificEntry(
						LayerType.VETERAN,
						VdypMatchers.builds(
								allOf(
										hasProperty("polygonIdentifier", isPolyId("082F074/0071", 2001)), //
										hasProperty("layerType", is(LayerType.VETERAN)), //
										hasProperty("crownClosure", is(30.0f)), //
										hasProperty("baseArea", notPresent()), //
										hasProperty("treesPerHectare", notPresent()), //
										hasProperty("utilization", is(7.5f))
								)
						)
				)
		);

		assertEmpty(stream);
	}

}
