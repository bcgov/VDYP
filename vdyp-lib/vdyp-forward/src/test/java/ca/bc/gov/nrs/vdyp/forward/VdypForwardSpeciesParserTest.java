package ca.bc.gov.nrs.vdyp.forward;

import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.assertEmpty;
import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.assertNext;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import ca.bc.gov.nrs.vdyp.common.ControlKey;
import ca.bc.gov.nrs.vdyp.forward.model.VdypEntity;
import ca.bc.gov.nrs.vdyp.forward.model.VdypLayerSpecies;
import ca.bc.gov.nrs.vdyp.forward.parsers.VdypSpeciesParser;
import ca.bc.gov.nrs.vdyp.io.parse.streaming.StreamingParser;
import ca.bc.gov.nrs.vdyp.io.parse.streaming.StreamingParserFactory;
import ca.bc.gov.nrs.vdyp.model.LayerType;
import ca.bc.gov.nrs.vdyp.test.TestUtils;

class VdypForwardSpeciesParserTest {

	@Test
	void testParseEmpty() throws Exception {

		var parser = new VdypSpeciesParser();

		Map<String, Object> controlMap = new HashMap<>();

		controlMap.put(ControlKey.FORWARD_INPUT_VDYP_LAYER_BY_SPECIES.name(), "test.dat");
		TestUtils.populateControlMapBecReal(controlMap);

		var fileResolver = TestUtils.fileResolver("test.dat", TestUtils.makeInputStream(/* empty */));

		parser.modify(controlMap, fileResolver);

		var parserFactory = controlMap.get(ControlKey.FORWARD_INPUT_VDYP_LAYER_BY_SPECIES.name());

		assertThat(parserFactory, instanceOf(StreamingParserFactory.class));

		@SuppressWarnings("unchecked")
		var stream = ((StreamingParserFactory<VdypLayerSpecies>) parserFactory).get();

		assertThat(stream, instanceOf(StreamingParser.class));

		assertEmpty(stream);
	}

	@Test
	void testParseOneGenus() throws Exception {

		var parser = new VdypSpeciesParser();

		Map<String, Object> controlMap = new HashMap<>();

		controlMap.put(ControlKey.FORWARD_INPUT_VDYP_LAYER_BY_SPECIES.name(), "test.dat");
		TestUtils.populateControlMapGenusReal(controlMap);

		var fileResolver = TestUtils.fileResolver(
				"test.dat",
				TestUtils.makeInputStream(
						"01002 S000001 00     1970 P 15 S  S  100.0     0.0     0.0     0.0 -9.00 -9.00  -9.0  -9.0  -9.0 0 -9",
						"01002 S000001 00     1970"
				)
		);

		parser.modify(controlMap, fileResolver);

		var parserFactory = controlMap.get(ControlKey.FORWARD_INPUT_VDYP_LAYER_BY_SPECIES.name());

		assertThat(parserFactory, instanceOf(StreamingParserFactory.class));

		@SuppressWarnings("unchecked")
		var stream = ((StreamingParserFactory<Collection<VdypLayerSpecies>>) parserFactory).get();

		assertThat(stream, instanceOf(StreamingParser.class));

		var genera = assertNext(stream);

		assertThat(
				genera,
				hasItem(
						allOf(
								hasProperty("polygonId", hasProperty("description", is("01002 S000001 00     1970"))),
								hasProperty("layerType", is(LayerType.PRIMARY)), hasProperty("genusIndex", is(15)),
								hasProperty("genus", is(Optional.of("S"))),
								hasProperty(
										"speciesDistributions",
										hasProperty(
												"speciesDistributionMap",
												hasEntry(
														is(0),
														allOf(
																hasProperty("index", is(0)),
																hasProperty("genus", hasProperty("alias", is("S"))),
																hasProperty("percentage", is(100.0f))
														)
												)
										)
								), hasProperty("siteIndex", is(Float.NaN)),
								hasProperty("dominantHeight", is(Float.NaN)), hasProperty("ageTotal", is(Float.NaN)),
								hasProperty("ageAtBreastHeight", is(Float.NaN)),
								hasProperty("yearsToBreastHeight", is(Float.NaN)),
								hasProperty("isPrimary", is(Optional.of(false))),
								hasProperty("siteCurveNumber", is(VdypEntity.MISSING_INTEGER_VALUE))
						)
				)
		);

		assertEmpty(stream);
	}

	@Test
	void testParseTwoPairsOfGenera() throws Exception {

		var parser = new VdypSpeciesParser();

		Map<String, Object> controlMap = new HashMap<>();

		controlMap.put(ControlKey.FORWARD_INPUT_VDYP_LAYER_BY_SPECIES.name(), "test.dat");
		TestUtils.populateControlMapGenusReal(controlMap);

		var fileResolver = TestUtils.fileResolver(
				"test.dat",
				TestUtils.makeInputStream(
						"01002 S000002 00     1970 P 15 S  S  100.0     0.0     0.0     0.0 -9.00 -9.00  -9.0  -9.0  -9.0 0 -9",
						"01002 S000002 00     1970 V  3 B  B   50.0 S  50.0     0.0     0.0 -9.00 -9.00  -9.0  -9.0  -9.0",
						"01002 S000002 00     1970",
						"01002 S000003 00     1970 P 15 S  S  100.0     0.0     0.0     0.0 -9.00 -9.00  -9.0  -9.0  -9.0 0 -9",
						"01002 S000003 00     1970 V  3 B  B   50.0 S  50.0     0.0     0.0 -9.00 -9.00  -9.0  -9.0  -9.0",
						"01002 S000003 00     1970"
				)
		);

		parser.modify(controlMap, fileResolver);

		var parserFactory = controlMap.get(ControlKey.FORWARD_INPUT_VDYP_LAYER_BY_SPECIES.name());

		assertThat(parserFactory, instanceOf(StreamingParserFactory.class));

		@SuppressWarnings("unchecked")
		var stream = ((StreamingParserFactory<Collection<VdypLayerSpecies>>) parserFactory).get();

		assertThat(stream, instanceOf(StreamingParser.class));

		var genera1 = assertNext(stream);
		assertThat(genera1, hasSize(2));

		var genera2 = assertNext(stream);
		assertThat(genera2, hasSize(2));

		assertEmpty(stream);
	}

	@Test
	void testParseComputesAgesWhenMissing() throws Exception {

		var parser = new VdypSpeciesParser();

		Map<String, Object> controlMap = new HashMap<>();

		controlMap.put(ControlKey.FORWARD_INPUT_VDYP_LAYER_BY_SPECIES.name(), "test.dat");
		TestUtils.populateControlMapGenusReal(controlMap);

		var fileResolver = TestUtils.fileResolver(
				"test.dat",
				TestUtils.makeInputStream(
						"01002 S000002 00     1970 P 15 S  S  100.0     0.0     0.0     0.0 -9.00 -9.00  20.0  12.0  -9.0 0 -9",
						"01002 S000002 00     1970 V  3 B  B   50.0 S  50.0     0.0     0.0 -9.00 -9.00  -9.0   8.0   4.0",
						"01002 S000002 00     1970 V  5 L  L   50.0 S  25.0 AC 15.0 B  10.0 -9.00 -9.00  14.0  -9.0   6.0 1",
						"01002 S000002 00     1970"
				)
		);

		parser.modify(controlMap, fileResolver);

		var parserFactory = controlMap.get(ControlKey.FORWARD_INPUT_VDYP_LAYER_BY_SPECIES.name());

		assertThat(parserFactory, instanceOf(StreamingParserFactory.class));

		@SuppressWarnings("unchecked")
		var stream = ((StreamingParserFactory<Collection<VdypLayerSpecies>>) parserFactory).get();

		assertThat(stream, instanceOf(StreamingParser.class));

		var genera = assertNext(stream);
		assertThat(genera, hasSize(3));

		assertThat(
				genera,
				hasItems(
						allOf(
								hasProperty("ageTotal", is(20.0f)), hasProperty("ageAtBreastHeight", is(12.0f)),
								hasProperty("yearsToBreastHeight", is(8.0f)),
								hasProperty("isPrimary", is(Optional.of(false)))
						),
						allOf(
								hasProperty(
										"speciesDistributions",
										hasProperty(
												"speciesDistributionMap",
												allOf(
														hasEntry(
																is(0),
																allOf(
																		hasProperty(
																				"genus", hasProperty("alias", is("B"))
																		), hasProperty("percentage", is(50.0f))
																)
														),
														hasEntry(
																is(1),
																allOf(
																		hasProperty(
																				"genus", hasProperty("alias", is("S"))
																		), hasProperty("percentage", is(50.0f))
																)
														)
												)
										)
								), hasProperty("ageTotal", is(12.0f)), hasProperty("ageAtBreastHeight", is(8.0f)),
								hasProperty("yearsToBreastHeight", is(4.0f)),
								hasProperty("isPrimary", is(Optional.empty()))
						),
						allOf(
								hasProperty(
										"speciesDistributions",
										hasProperty(
												"speciesDistributionMap",
												allOf(
														hasEntry(
																is(0),
																allOf(
																		hasProperty(
																				"genus", hasProperty("alias", is("L"))
																		), hasProperty("percentage", is(50.0f))
																)
														),
														hasEntry(
																is(1),
																allOf(
																		hasProperty(
																				"genus", hasProperty("alias", is("S"))
																		), hasProperty("percentage", is(25.0f))
																)
														),
														hasEntry(
																is(2),
																allOf(
																		hasProperty(
																				"genus", hasProperty("alias", is("AC"))
																		), hasProperty("percentage", is(15.0f))
																)
														),
														hasEntry(
																is(3),
																allOf(
																		hasProperty(
																				"genus", hasProperty("alias", is("B"))
																		), hasProperty("percentage", is(10.0f))
																)
														)
												)
										)
								), hasProperty("ageTotal", is(14.0f)), hasProperty("ageAtBreastHeight", is(8.0f)),
								hasProperty("yearsToBreastHeight", is(6.0f)),
								hasProperty("isPrimary", is(Optional.of(true)))
						)
				)
		);

		assertEmpty(stream);
	}

	@Test
	void testParseTwoGenera() throws Exception {

		var parser = new VdypSpeciesParser();

		Map<String, Object> controlMap = new HashMap<>();

		controlMap.put(ControlKey.FORWARD_INPUT_VDYP_LAYER_BY_SPECIES.name(), "test.dat");
		TestUtils.populateControlMapGenusReal(controlMap);

		var fileResolver = TestUtils.fileResolver(
				"test.dat",
				TestUtils.makeInputStream(
						"01002 S000002 00     1970 P 15 S  S  100.0     0.0     0.0     0.0 -9.00 -9.00  -9.0  -9.0  -9.0 0 -9",
						"01002 S000002 00     1970 V  3 B  B   50.0 S  50.0     0.0     0.0 -9.00 -9.00  -9.0  -9.0  -9.0",
						"01002 S000002 00     1970"
				)
		);

		parser.modify(controlMap, fileResolver);

		var parserFactory = controlMap.get(ControlKey.FORWARD_INPUT_VDYP_LAYER_BY_SPECIES.name());

		assertThat(parserFactory, instanceOf(StreamingParserFactory.class));

		@SuppressWarnings("unchecked")
		var stream = ((StreamingParserFactory<Collection<VdypLayerSpecies>>) parserFactory).get();

		assertThat(stream, instanceOf(StreamingParser.class));

		var genera = assertNext(stream);

		assertThat(
				genera,
				hasItems(
						allOf(
								hasProperty("polygonId", hasProperty("description", is("01002 S000002 00     1970"))),
								hasProperty("layerType", is(LayerType.PRIMARY)), hasProperty("genusIndex", is(15)),
								hasProperty("genus", is(Optional.of("S"))),
								hasProperty(
										"speciesDistributions",
										hasProperty(
												"speciesDistributionMap",
												hasEntry(
														is(0),
														allOf(
																hasProperty("index", is(0)),
																hasProperty("genus", hasProperty("alias", is("S"))),
																hasProperty("percentage", is(100.0f))
														)
												)
										)
								), hasProperty("siteIndex", is(Float.NaN)),
								hasProperty("dominantHeight", is(Float.NaN)), hasProperty("ageTotal", is(Float.NaN)),
								hasProperty("ageAtBreastHeight", is(Float.NaN)),
								hasProperty("yearsToBreastHeight", is(Float.NaN)),
								hasProperty("isPrimary", is(Optional.of(false))),
								hasProperty("siteCurveNumber", is(VdypEntity.MISSING_INTEGER_VALUE))
						),
						allOf(
								hasProperty("polygonId", hasProperty("description", is("01002 S000002 00     1970"))),
								hasProperty("layerType", is(LayerType.VETERAN)), hasProperty("genusIndex", is(3)),
								hasProperty("genus", is(Optional.of("B"))),
								hasProperty(
										"speciesDistributions",
										hasProperty(
												"speciesDistributionMap",
												allOf(
														hasEntry(
																is(0),
																allOf(
																		hasProperty("index", is(0)),
																		hasProperty(
																				"genus", hasProperty("alias", is("B"))
																		), hasProperty("percentage", is(50.0f))
																)
														),
														hasEntry(
																is(1),
																allOf(
																		hasProperty("index", is(1)),
																		hasProperty(
																				"genus", hasProperty("alias", is("S"))
																		), hasProperty("percentage", is(50.0f))
																)
														)
												)
										)
								), hasProperty("siteIndex", is(Float.NaN)),
								hasProperty("dominantHeight", is(Float.NaN)), hasProperty("ageTotal", is(Float.NaN)),
								hasProperty("ageAtBreastHeight", is(Float.NaN)),
								hasProperty("yearsToBreastHeight", is(Float.NaN)),
								hasProperty("isPrimary", is(Optional.empty())),
								hasProperty("siteCurveNumber", is(VdypEntity.MISSING_INTEGER_VALUE))
						)
				)
		);

		assertEmpty(stream);
	}
}
