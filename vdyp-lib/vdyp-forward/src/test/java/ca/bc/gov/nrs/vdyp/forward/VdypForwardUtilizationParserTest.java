package ca.bc.gov.nrs.vdyp.forward;

import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.assertEmpty;
import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.assertNext;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import ca.bc.gov.nrs.vdyp.common.ControlKey;
import ca.bc.gov.nrs.vdyp.forward.model.VdypSpeciesUtilization;
import ca.bc.gov.nrs.vdyp.forward.parsers.VdypUtilizationParser;
import ca.bc.gov.nrs.vdyp.io.parse.streaming.StreamingParser;
import ca.bc.gov.nrs.vdyp.io.parse.streaming.StreamingParserFactory;
import ca.bc.gov.nrs.vdyp.model.LayerType;
import ca.bc.gov.nrs.vdyp.model.UtilizationClass;
import ca.bc.gov.nrs.vdyp.test.TestUtils;

public class VdypForwardUtilizationParserTest {

	@Test
	void testParseEmpty() throws Exception {

		var parser = new VdypUtilizationParser();

		Map<String, Object> controlMap = new HashMap<>();

		controlMap.put(ControlKey.FORWARD_INPUT_VDYP_LAYER_BY_SP0_BY_UTIL.name(), "test.dat");
		TestUtils.populateControlMapBecReal(controlMap);

		var fileResolver = TestUtils.fileResolver("test.dat", TestUtils.makeInputStream(/* empty */));

		parser.modify(controlMap, fileResolver);

		var parserFactory = controlMap.get(ControlKey.FORWARD_INPUT_VDYP_LAYER_BY_SP0_BY_UTIL.name());

		assertThat(parserFactory, instanceOf(StreamingParserFactory.class));

		@SuppressWarnings("unchecked")
		var stream = ((StreamingParserFactory<VdypSpeciesUtilization>) parserFactory).get();

		assertThat(stream, instanceOf(StreamingParser.class));

		assertEmpty(stream);
	}

	@Test
	void testParseOneUtilization() throws Exception {

		var parser = new VdypUtilizationParser();

		Map<String, Object> controlMap = new HashMap<>();

		controlMap.put(ControlKey.FORWARD_INPUT_VDYP_LAYER_BY_SP0_BY_UTIL.name(), "test.dat");
		TestUtils.populateControlMapGenusReal(controlMap);

		var fileResolver = TestUtils.fileResolver(
				"test.dat",
				TestUtils.makeInputStream(
						"01002 S000001 00     1970 P  0    -1  0.01513     5.24   7.0166   0.0630   0.0000   0.0000   0.0000   0.0000   6.1",
						"01002 S000001 00     1970"
				)
		);

		parser.modify(controlMap, fileResolver);

		var parserFactory = controlMap.get(ControlKey.FORWARD_INPUT_VDYP_LAYER_BY_SP0_BY_UTIL.name());

		assertThat(parserFactory, instanceOf(StreamingParserFactory.class));

		@SuppressWarnings("unchecked")
		var stream = ((StreamingParserFactory<Collection<VdypSpeciesUtilization>>) parserFactory).get();

		assertThat(stream, instanceOf(StreamingParser.class));

		var utilizations = assertNext(stream);

		assertThat(utilizations, Matchers.hasSize(1));

		assertThat(
				utilizations,
				hasItem(
						allOf(
								hasProperty("polygonId", hasProperty("description", is("01002 S000001 00     1970"))),
								hasProperty("layerType", is(LayerType.PRIMARY)),
								hasProperty("genus", is(Optional.empty())),
								hasProperty("ucIndex", is(UtilizationClass.SMALL)),
								hasProperty("basalArea", is(0.01513f)), hasProperty("liveTreesPerHectare", is(5.24f)),
								hasProperty("loreyHeight", is(7.0166f)), hasProperty("wholeStemVolume", is(0.0630f)),
								hasProperty("closeUtilizationVolume", is(0.0f)),
								hasProperty("cuVolumeMinusDecay", is(0.0f)),
								hasProperty("cuVolumeMinusDecayWastage", is(0.0f)),
								hasProperty("cuVolumeMinusDecayWastageBreakage", is(0.0f)),
								hasProperty("genusIndex", is(0)), hasProperty("quadraticMeanDiameterAtBH", is(6.1f))
						)
				)
		);

		assertEmpty(stream);
	}

	@Test
	void testParseTwoUtilizations() throws Exception {

		var parser = new VdypUtilizationParser();

		Map<String, Object> controlMap = new HashMap<>();

		controlMap.put(ControlKey.FORWARD_INPUT_VDYP_LAYER_BY_SP0_BY_UTIL.name(), "test.dat");
		TestUtils.populateControlMapGenusReal(controlMap);

		var fileResolver = TestUtils.fileResolver(
				"test.dat",
				TestUtils.makeInputStream(
						"01002 S000001 00     1970 P  0    -1  0.01513     5.24   7.0166   0.0630   0.0000   0.0000   0.0000   0.0000   6.1",
						"01002 S000001 00     1970 P  0    -1  0.01513     5.24   7.0166   0.0630   1.2343   0.0000   0.0000   0.0000   6.1",
						"01002 S000001 00     1970"
				)
		);

		parser.modify(controlMap, fileResolver);

		var parserFactory = controlMap.get(ControlKey.FORWARD_INPUT_VDYP_LAYER_BY_SP0_BY_UTIL.name());

		assertThat(parserFactory, instanceOf(StreamingParserFactory.class));

		@SuppressWarnings("unchecked")
		var stream = ((StreamingParserFactory<Collection<VdypSpeciesUtilization>>) parserFactory).get();

		assertThat(stream, instanceOf(StreamingParser.class));

		var utilizations = assertNext(stream);

		assertThat(utilizations, Matchers.hasSize(2));

		assertThat(
				utilizations,
				hasItems(
						allOf(
								hasProperty("polygonId", hasProperty("description", is("01002 S000001 00     1970"))),
								hasProperty("layerType", is(LayerType.PRIMARY)),
								hasProperty("genus", is(Optional.empty())),
								hasProperty("ucIndex", is(UtilizationClass.SMALL)),
								hasProperty("basalArea", is(0.01513f)), hasProperty("liveTreesPerHectare", is(5.24f)),
								hasProperty("loreyHeight", is(7.0166f)), hasProperty("wholeStemVolume", is(0.0630f)),
								hasProperty("closeUtilizationVolume", is(0.0f)),
								hasProperty("cuVolumeMinusDecay", is(0.0f)),
								hasProperty("cuVolumeMinusDecayWastage", is(0.0f)),
								hasProperty("cuVolumeMinusDecayWastageBreakage", is(0.0f)),
								hasProperty("genusIndex", is(0)), hasProperty("quadraticMeanDiameterAtBH", is(6.1f))
						),
						allOf(
								hasProperty("polygonId", hasProperty("description", is("01002 S000001 00     1970"))),
								hasProperty("layerType", is(LayerType.PRIMARY)),
								hasProperty("genus", is(Optional.empty())),
								hasProperty("ucIndex", is(UtilizationClass.SMALL)),
								hasProperty("basalArea", is(0.01513f)), hasProperty("liveTreesPerHectare", is(5.24f)),
								hasProperty("loreyHeight", is(7.0166f)), hasProperty("wholeStemVolume", is(0.0630f)),
								hasProperty("closeUtilizationVolume", is(1.2343f)),
								hasProperty("cuVolumeMinusDecay", is(0.0f)),
								hasProperty("cuVolumeMinusDecayWastage", is(0.0f)),
								hasProperty("cuVolumeMinusDecayWastageBreakage", is(0.0f)),
								hasProperty("genusIndex", is(0)), hasProperty("quadraticMeanDiameterAtBH", is(6.1f))
						)
				)
		);

		assertEmpty(stream);
	}
}
