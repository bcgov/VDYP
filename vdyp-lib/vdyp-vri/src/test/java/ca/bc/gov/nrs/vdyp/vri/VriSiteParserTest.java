package ca.bc.gov.nrs.vdyp.vri;

import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.assertEmpty;
import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.assertNext;
import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.closeTo;
import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.isPolyId;
import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.notPresent;
import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.present;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.iterableWithSize;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import ca.bc.gov.nrs.vdyp.common.ControlKey;
import ca.bc.gov.nrs.vdyp.io.parse.streaming.StreamingParser;
import ca.bc.gov.nrs.vdyp.io.parse.streaming.StreamingParserFactory;
import ca.bc.gov.nrs.vdyp.model.LayerType;
import ca.bc.gov.nrs.vdyp.test.TestUtils;
import ca.bc.gov.nrs.vdyp.vri.model.VriSite;

class VriSiteParserTest {

	@Test
	void testParseEmpty() throws Exception {

		var parser = new VriSiteParser();

		Map<String, Object> controlMap = new HashMap<>();

		controlMap.put(ControlKey.VRI_INPUT_YIELD_SPEC_DIST.name(), "test.dat");
		TestUtils.populateControlMapBecReal(controlMap);

		var fileResolver = TestUtils.fileResolver("test.dat", TestUtils.makeInputStream(/* empty */));

		parser.modify(controlMap, fileResolver);

		var parserFactory = controlMap.get(ControlKey.VRI_INPUT_YIELD_SPEC_DIST.name());

		assertThat(parserFactory, instanceOf(StreamingParserFactory.class));

		@SuppressWarnings("unchecked")
		var stream = ((StreamingParserFactory<VriSite>) parserFactory).get();

		assertThat(stream, instanceOf(StreamingParser.class));

		assertEmpty(stream);
	}

	@Test
	void testParseOneSite() throws Exception {

		var parser = new VriSiteParser();

		Map<String, Object> controlMap = new HashMap<>();

		controlMap.put(ControlKey.VRI_INPUT_YIELD_SPEC_DIST.name(), "test.dat");
		TestUtils.populateControlMapGenusReal(controlMap);

		var fileResolver = TestUtils.fileResolver(
				"test.dat",
				TestUtils.makeInputStream(
						"082F074/0071         2001 P 200 28.0 14.3        C CW 10.9          189.1 11",
						"082F074/0071         2001 Z   0  0.0  0.0"
				)
		);

		parser.modify(controlMap, fileResolver);

		var parserFactory = controlMap.get(ControlKey.VRI_INPUT_YIELD_SPEC_DIST.name());

		assertThat(parserFactory, instanceOf(StreamingParserFactory.class));

		@SuppressWarnings("unchecked")
		var stream = ((StreamingParserFactory<Collection<VriSite>>) parserFactory).get();

		assertThat(stream, instanceOf(StreamingParser.class));

		var sites = assertNext(stream);

		assertThat(sites, iterableWithSize(1));
		assertThat(
				sites.iterator().next(), allOf(
						hasProperty("ageTotal", present(closeTo(200.0f))), //
						hasProperty("height", present(closeTo(28.0f))), //
						hasProperty("siteIndex", present(closeTo(14.3f))), //
						hasProperty("siteGenus", is("C")), //
						hasProperty("siteSpecies", is("CW")), //
						hasProperty("yearsToBreastHeight", present(closeTo(10.9f))), //
						hasProperty("breastHeightAge", present(closeTo(189.1f))), //
						hasProperty("siteCurveNumber", present(is(11)))
				)
		);

		assertEmpty(stream);
	}

	@Test
	void testIgnoreIfNotPrimaryOrSecondary() throws Exception {

		var parser = new VriSiteParser();

		Map<String, Object> controlMap = new HashMap<>();

		controlMap.put(ControlKey.VRI_INPUT_YIELD_SPEC_DIST.name(), "test.dat");
		TestUtils.populateControlMapGenusReal(controlMap);

		var fileResolver = TestUtils.fileResolver(
				"test.dat",
				TestUtils.makeInputStream(
						"082F074/0071         2001 X 100 28.0 14.3        C CW 10.9          189.1 11",
						"082F074/0071         2001 P 200 28.0 14.3        C CW 10.9          189.1 11",
						"082F074/0071         2001 Z   0  0.0  0.0"
				)
		);

		parser.modify(controlMap, fileResolver);

		var parserFactory = controlMap.get(ControlKey.VRI_INPUT_YIELD_SPEC_DIST.name());

		assertThat(parserFactory, instanceOf(StreamingParserFactory.class));

		@SuppressWarnings("unchecked")
		var stream = ((StreamingParserFactory<Collection<VriSite>>) parserFactory).get();

		assertThat(stream, instanceOf(StreamingParser.class));

		var sites = assertNext(stream);

		assertThat(
				sites, containsInAnyOrder(
						allOf(
								hasProperty("ageTotal", present(closeTo(200.0f))), //
								hasProperty("height", present(closeTo(28.0f))), //
								hasProperty("siteIndex", present(closeTo(14.3f))), //
								hasProperty("siteGenus", is("C")), //
								hasProperty("siteSpecies", is("CW")), //
								hasProperty("yearsToBreastHeight", present(closeTo(10.9f))), //
								hasProperty("breastHeightAge", present(closeTo(189.1f))), //
								hasProperty("siteCurveNumber", present(is(11)))

						)
				)
		);

		assertEmpty(stream);
	}

	@Test
	void testParseTwoSites() throws Exception {

		var parser = new VriSiteParser();

		Map<String, Object> controlMap = new HashMap<>();

		controlMap.put(ControlKey.VRI_INPUT_YIELD_SPEC_DIST.name(), "test.dat");
		TestUtils.populateControlMapGenusReal(controlMap);

		var fileResolver = TestUtils.fileResolver(
				"test.dat",
				TestUtils.makeInputStream(
						"082F074/0071         2001 P 200 28.0 14.3        C CW 10.9          189.1 11",
						"082F074/0071         2001 P 200 32.0 14.6        H HW  9.7          190.3 37",
						"082F074/0071         2001 Z   0  0.0  0.0"
				)
		);

		parser.modify(controlMap, fileResolver);

		var parserFactory = controlMap.get(ControlKey.VRI_INPUT_YIELD_SPEC_DIST.name());

		assertThat(parserFactory, instanceOf(StreamingParserFactory.class));

		@SuppressWarnings("unchecked")
		var stream = ((StreamingParserFactory<Collection<VriSite>>) parserFactory).get();

		assertThat(stream, instanceOf(StreamingParser.class));

		var sites = assertNext(stream);

		assertThat(
				sites, containsInAnyOrder(
						allOf(
								hasProperty("ageTotal", present(closeTo(200.0f))), //
								hasProperty("height", present(closeTo(28.0f))), //
								hasProperty("siteIndex", present(closeTo(14.3f))), //
								hasProperty("siteGenus", is("C")), //
								hasProperty("siteSpecies", is("CW")), //
								hasProperty("yearsToBreastHeight", present(closeTo(10.9f))), //
								hasProperty("breastHeightAge", present(closeTo(189.1f))), //
								hasProperty("siteCurveNumber", present(is(11)))
						),
						allOf(
								hasProperty("ageTotal", present(closeTo(200.0f))), //
								hasProperty("height", present(closeTo(32.0f))), //
								hasProperty("siteIndex", present(closeTo(14.6f))), //
								hasProperty("siteGenus", is("H")), //
								hasProperty("siteSpecies", is("HW")), //
								hasProperty("yearsToBreastHeight", present(closeTo(9.7f))), //
								hasProperty("breastHeightAge", present(closeTo(190.3f))), //
								hasProperty("siteCurveNumber", present(is(37)))
						)
				)
		);

		assertEmpty(stream);

	}

	@Test
	void testParseTwoLayers() throws Exception {

		var parser = new VriSiteParser();

		Map<String, Object> controlMap = new HashMap<>();

		controlMap.put(ControlKey.VRI_INPUT_YIELD_SPEC_DIST.name(), "test.dat");
		TestUtils.populateControlMapGenusReal(controlMap);

		var fileResolver = TestUtils.fileResolver(
				"test.dat",
				TestUtils.makeInputStream(
						"082F074/0071         2001 P 200 28.0 14.3        C CW 10.9          189.1 11",
						"082F074/0071         2001 S 200 32.0 14.6        H HW  9.7          190.3 37",
						"082F074/0071         2001 Z   0  0.0  0.0"
				)
		);

		parser.modify(controlMap, fileResolver);

		var parserFactory = controlMap.get(ControlKey.VRI_INPUT_YIELD_SPEC_DIST.name());

		assertThat(parserFactory, instanceOf(StreamingParserFactory.class));

		@SuppressWarnings("unchecked")
		var stream = ((StreamingParserFactory<Collection<VriSite>>) parserFactory).get();

		assertThat(stream, instanceOf(StreamingParser.class));

		var sites = assertNext(stream);

		assertThat(
				sites, containsInAnyOrder(
						allOf(
								hasProperty("layerType", is(LayerType.PRIMARY)), //
								hasProperty("ageTotal", present(closeTo(200.0f))), //
								hasProperty("height", present(closeTo(28.0f))), //
								hasProperty("siteIndex", present(closeTo(14.3f))), //
								hasProperty("siteGenus", is("C")), //
								hasProperty("siteSpecies", is("CW")), //
								hasProperty("yearsToBreastHeight", present(closeTo(10.9f))), //
								hasProperty("breastHeightAge", present(closeTo(189.1f))), //
								hasProperty("siteCurveNumber", present(is(11)))
						),
						allOf(
								hasProperty("layerType", is(LayerType.SECONDARY)), //
								hasProperty("ageTotal", present(closeTo(200.0f))), //
								hasProperty("height", present(closeTo(32.0f))), //
								hasProperty("siteIndex", present(closeTo(14.6f))), //
								hasProperty("siteGenus", is("H")), //
								hasProperty("siteSpecies", is("HW")), //
								hasProperty("yearsToBreastHeight", present(closeTo(9.7f))), //
								hasProperty("breastHeightAge", present(closeTo(190.3f))), //
								hasProperty("siteCurveNumber", present(is(37)))
						)
				)
		);

		assertEmpty(stream);
	}

	@Test
	void testParseTwoPolygons() throws Exception {

		var parser = new VriSiteParser();

		Map<String, Object> controlMap = new HashMap<>();

		controlMap.put(ControlKey.VRI_INPUT_YIELD_SPEC_DIST.name(), "test.dat");
		TestUtils.populateControlMapGenusReal(controlMap);

		var fileResolver = TestUtils.fileResolver(
				"test.dat",
				TestUtils.makeInputStream(
						"082F074/0072         2002 P 200 28.0 14.3        C CW 10.9          189.1 11",
						"082F074/0072         2002 Z   0  0.0  0.0",
						"082F074/0071         2001 P 200 32.0 14.6        H HW  9.7          190.3 37",
						"082F074/0071         2001 Z   0  0.0  0.0"
				)
		);

		parser.modify(controlMap, fileResolver);

		var parserFactory = controlMap.get(ControlKey.VRI_INPUT_YIELD_SPEC_DIST.name());

		assertThat(parserFactory, instanceOf(StreamingParserFactory.class));

		@SuppressWarnings("unchecked")
		var stream = ((StreamingParserFactory<Collection<VriSite>>) parserFactory).get();

		assertThat(stream, instanceOf(StreamingParser.class));

		var sites = assertNext(stream);

		assertThat(sites, iterableWithSize(1));
		assertThat(
				sites.iterator().next(), allOf(
						hasProperty("polygonIdentifier", isPolyId("082F074/0072", 2002)), //
						hasProperty("layerType", is(LayerType.PRIMARY)), //
						hasProperty("ageTotal", present(closeTo(200.0f))), //
						hasProperty("height", present(closeTo(28.0f))), //
						hasProperty("siteIndex", present(closeTo(14.3f))), //
						hasProperty("siteGenus", is("C")), //
						hasProperty("siteSpecies", is("CW")), //
						hasProperty("yearsToBreastHeight", present(closeTo(10.9f))), //
						hasProperty("breastHeightAge", present(closeTo(189.1f))), //
						hasProperty("siteCurveNumber", present(is(11)))
				)
		);

		sites = assertNext(stream);

		assertThat(sites, iterableWithSize(1));
		assertThat(
				sites.iterator().next(), allOf(
						hasProperty("polygonIdentifier", isPolyId("082F074/0071", 2001)), //
						hasProperty("layerType", is(LayerType.PRIMARY)), //
						hasProperty("ageTotal", present(closeTo(200.0f))), //
						hasProperty("height", present(closeTo(32.0f))), //
						hasProperty("siteIndex", present(closeTo(14.6f))), //
						hasProperty("siteGenus", is("H")), //
						hasProperty("siteSpecies", is("HW")), //
						hasProperty("yearsToBreastHeight", present(closeTo(9.7f))), //
						hasProperty("breastHeightAge", present(closeTo(190.3f))), //
						hasProperty("siteCurveNumber", present(is(37)))
				)

		);

		assertEmpty(stream);
	}

	@Test
	void testBreastHeightAgeZeroAndTotalEmpty() throws Exception {

		var parser = new VriSiteParser();

		Map<String, Object> controlMap = new HashMap<>();

		controlMap.put(ControlKey.VRI_INPUT_YIELD_SPEC_DIST.name(), "test.dat");
		TestUtils.populateControlMapGenusReal(controlMap);

		var fileResolver = TestUtils.fileResolver(
				"test.dat",
				TestUtils.makeInputStream(
						"082F074/0071         2001 P  -9 28.0 14.3        C CW 10.9            0.0 11",
						"082F074/0071         2001 Z   0  0.0  0.0"
				)
		);

		parser.modify(controlMap, fileResolver);

		var parserFactory = controlMap.get(ControlKey.VRI_INPUT_YIELD_SPEC_DIST.name());

		assertThat(parserFactory, instanceOf(StreamingParserFactory.class));

		@SuppressWarnings("unchecked")
		var stream = ((StreamingParserFactory<Collection<VriSite>>) parserFactory).get();

		assertThat(stream, instanceOf(StreamingParser.class));

		var sites = assertNext(stream);

		assertThat(sites, iterableWithSize(1));
		assertThat(
				sites.iterator().next(), allOf(
						hasProperty("ageTotal", notPresent()), //
						hasProperty("breastHeightAge", notPresent())
				)
		);

		assertEmpty(stream);
	}

	@Test
	void testBreastHeightAgeZeroAndNotCloseToExpected() throws Exception {

		var parser = new VriSiteParser();

		Map<String, Object> controlMap = new HashMap<>();

		controlMap.put(ControlKey.VRI_INPUT_YIELD_SPEC_DIST.name(), "test.dat");
		TestUtils.populateControlMapGenusReal(controlMap);

		var fileResolver = TestUtils.fileResolver(
				"test.dat", TestUtils.makeInputStream(
						// YTBH differs from Age Total by more than 0.5
						"082F074/0071         2001 P  20 28.0 14.3        C CW 19.4            0.0 11",
						"082F074/0071         2001 Z   0  0.0  0.0"
				)
		);

		parser.modify(controlMap, fileResolver);

		var parserFactory = controlMap.get(ControlKey.VRI_INPUT_YIELD_SPEC_DIST.name());

		assertThat(parserFactory, instanceOf(StreamingParserFactory.class));

		@SuppressWarnings("unchecked")
		var stream = ((StreamingParserFactory<Collection<VriSite>>) parserFactory).get();

		assertThat(stream, instanceOf(StreamingParser.class));

		var sites = assertNext(stream);

		assertThat(sites, iterableWithSize(1));
		assertThat(
				sites.iterator().next(), allOf(
						hasProperty("ageTotal", present(closeTo(20f))), //
						hasProperty("breastHeightAge", notPresent())
				)
		);

		assertEmpty(stream);
	}

	@Test
	void testBreastHeightAgeZeroAndIsCloseToExpected() throws Exception {

		var parser = new VriSiteParser();

		Map<String, Object> controlMap = new HashMap<>();

		controlMap.put(ControlKey.VRI_INPUT_YIELD_SPEC_DIST.name(), "test.dat");
		TestUtils.populateControlMapGenusReal(controlMap);

		var fileResolver = TestUtils.fileResolver(
				"test.dat", TestUtils.makeInputStream(
						// YTBH differs from Age Total by less than 0.5
						"082F074/0071         2001 P  20 28.0 14.3        C CW 19.6            0.0 11",
						"082F074/0071         2001 Z   0  0.0  0.0"
				)
		);

		parser.modify(controlMap, fileResolver);

		var parserFactory = controlMap.get(ControlKey.VRI_INPUT_YIELD_SPEC_DIST.name());

		assertThat(parserFactory, instanceOf(StreamingParserFactory.class));

		@SuppressWarnings("unchecked")
		var stream = ((StreamingParserFactory<Collection<VriSite>>) parserFactory).get();

		assertThat(stream, instanceOf(StreamingParser.class));

		var sites = assertNext(stream);

		assertThat(sites, iterableWithSize(1));
		assertThat(
				sites.iterator().next(), allOf(
						hasProperty("ageTotal", present(closeTo(20f))), //
						hasProperty("breastHeightAge", present(closeTo(0f)))
				)
		);

		assertEmpty(stream);
	}

	@Test
	void testDefaultHeight() throws Exception {

		var parser = new VriSiteParser();

		Map<String, Object> controlMap = new HashMap<>();

		controlMap.put(ControlKey.VRI_INPUT_YIELD_SPEC_DIST.name(), "test.dat");
		TestUtils.populateControlMapGenusReal(controlMap);

		var fileResolver = TestUtils.fileResolver(
				"test.dat", TestUtils.makeInputStream(
						// height empty, ageTotal within 0.6 of 1, siteIndex >=3
						"082F074/0071         2001 P   1 -9.0 14.3        C CW 19.6            0.0 11",
						"082F074/0071         2001 Z   0  0.0  0.0"
				)
		);

		parser.modify(controlMap, fileResolver);

		var parserFactory = controlMap.get(ControlKey.VRI_INPUT_YIELD_SPEC_DIST.name());

		assertThat(parserFactory, instanceOf(StreamingParserFactory.class));

		@SuppressWarnings("unchecked")
		var stream = ((StreamingParserFactory<Collection<VriSite>>) parserFactory).get();

		assertThat(stream, instanceOf(StreamingParser.class));

		var sites = assertNext(stream);

		assertThat(sites, iterableWithSize(1));
		assertThat(sites.iterator().next(), allOf(hasProperty("height", present(closeTo(0.05f)))));

		assertEmpty(stream);
	}

	@ParameterizedTest
	@ValueSource(
			strings = { //
					"082F074/0071         2001 P 1.7 -9.0 14.3        C CW 19.6            0.0 11", // Total Age High
					"082F074/0071         2001 P 0.3 -9.0 14.3        C CW 19.6            0.0 11", // Total Age Low
					"082F074/0071         2001 P   1 -9.0  2.9        C CW 19.6            0.0 11" // Site Index Low

			}
	)
	void testNoDefaultHeight() throws Exception {

		var parser = new VriSiteParser();

		Map<String, Object> controlMap = new HashMap<>();

		controlMap.put(ControlKey.VRI_INPUT_YIELD_SPEC_DIST.name(), "test.dat");
		TestUtils.populateControlMapGenusReal(controlMap);

		var fileResolver = TestUtils.fileResolver(
				"test.dat",
				TestUtils.makeInputStream(
						"082F074/0071         2001 P 1.7 -9.0 14.3        C CW 19.6            0.0 11",
						"082F074/0071         2001 Z   0  0.0  0.0"
				)
		);

		parser.modify(controlMap, fileResolver);

		var parserFactory = controlMap.get(ControlKey.VRI_INPUT_YIELD_SPEC_DIST.name());

		assertThat(parserFactory, instanceOf(StreamingParserFactory.class));

		@SuppressWarnings("unchecked")
		var stream = ((StreamingParserFactory<Collection<VriSite>>) parserFactory).get();

		assertThat(stream, instanceOf(StreamingParser.class));

		var sites = assertNext(stream);

		assertThat(sites, iterableWithSize(1));
		assertThat(sites.iterator().next(), allOf(hasProperty("height", notPresent())));

		assertEmpty(stream);
	}

}
