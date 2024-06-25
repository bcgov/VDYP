package ca.bc.gov.nrs.vdyp.vri;

import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.closeTo;
import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.notPresent;
import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.present;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import ca.bc.gov.nrs.vdyp.application.StandProcessingException;
import ca.bc.gov.nrs.vdyp.common.ControlKey;
import ca.bc.gov.nrs.vdyp.common.Utils;
import ca.bc.gov.nrs.vdyp.io.parse.coe.BecDefinitionParser;
import ca.bc.gov.nrs.vdyp.io.parse.coe.DefaultEquationNumberParser;
import ca.bc.gov.nrs.vdyp.io.parse.coe.EquationModifierParser;
import ca.bc.gov.nrs.vdyp.io.parse.coe.GenusDefinitionParser;
import ca.bc.gov.nrs.vdyp.io.parse.common.ResourceParseException;
import ca.bc.gov.nrs.vdyp.io.parse.streaming.MockStreamingParser;
import ca.bc.gov.nrs.vdyp.model.LayerType;
import ca.bc.gov.nrs.vdyp.model.PolygonIdentifier;
import ca.bc.gov.nrs.vdyp.test.MockFileResolver;
import ca.bc.gov.nrs.vdyp.test.TestUtils;
import ca.bc.gov.nrs.vdyp.vri.model.VriLayer;
import ca.bc.gov.nrs.vdyp.vri.model.VriPolygon;
import ca.bc.gov.nrs.vdyp.vri.model.VriSite;
import ca.bc.gov.nrs.vdyp.vri.model.VriSpecies;

class ParsersTogetherTest {

	Map<String, Object> controlMap = new HashMap<>();
	MockFileResolver resolver;
	IMocksControl mockControl = EasyMock.createControl();

	@BeforeEach
	void setUp() throws IOException, ResourceParseException {
		controlMap.put(ControlKey.VRI_OUTPUT_VDYP_POLYGON.name(), "DUMMY1");
		controlMap.put(ControlKey.VRI_OUTPUT_VDYP_LAYER_BY_SPECIES.name(), "DUMMY2");
		controlMap.put(ControlKey.VRI_OUTPUT_VDYP_LAYER_BY_SP0_BY_UTIL.name(), "DUMMY3");

		resolver = new MockFileResolver("Test");

		resolver.addStream("DUMMY1", (OutputStream) new ByteArrayOutputStream());
		resolver.addStream("DUMMY2", (OutputStream) new ByteArrayOutputStream());
		resolver.addStream("DUMMY3", (OutputStream) new ByteArrayOutputStream());

		controlMap.put(
				ControlKey.BEC_DEF.name(),
				new BecDefinitionParser().parse(TestUtils.class, "coe/Becdef.dat", controlMap)
		);
		controlMap.put(
				ControlKey.SP0_DEF.name(),
				new GenusDefinitionParser().parse(TestUtils.class, "coe/SP0DEF_v0.dat", controlMap)
		);
		controlMap.put(
				ControlKey.DEFAULT_EQ_NUM.name(),
				new DefaultEquationNumberParser().parse(TestUtils.class, "coe/GRPBA1.DAT", controlMap)
		);
		controlMap.put(
				ControlKey.EQN_MODIFIERS.name(),
				new EquationModifierParser().parse(TestUtils.class, "coe/GMODBA1.DAT", controlMap)
		);
	}

	@AfterEach
	void verifyMocks() {
		mockControl.verify();
	}

	@Test
	void testPrimaryOnly() throws IOException, StandProcessingException, ResourceParseException {
		var app = new VriStart();

		final var polygonId = new PolygonIdentifier("Test", 2024);
		final var layerType = LayerType.PRIMARY;

		mockControl.replay();

		app.init(resolver, controlMap);

		var polyStream = new MockStreamingParser<VriPolygon>();
		var layerStream = new MockStreamingParser<Map<LayerType, VriLayer.Builder>>();
		var speciesStream = new MockStreamingParser<Collection<VriSpecies>>();
		var siteStream = new MockStreamingParser<Collection<VriSite>>();

		polyStream.addValue(VriPolygon.build(polyBuilder -> {
			polyBuilder.polygonIdentifier(polygonId);
			polyBuilder.percentAvailable(Optional.of(100.0f));
			polyBuilder.biogeoclimaticZone("IDF");
			polyBuilder.yieldFactor(0.9f);
		}));

		var layerBuilder = new VriLayer.Builder();
		layerBuilder.polygonIdentifier(polygonId);
		layerBuilder.layerType(layerType);
		layerBuilder.crownClosure(95f);
		layerBuilder.utilization(0.6f);
		layerBuilder.baseArea(20);
		layerBuilder.treesPerHectare(300);
		layerStream.addValue(Collections.singletonMap(layerType, layerBuilder));

		speciesStream.addValue(Collections.singleton(VriSpecies.build(specBuilder -> {
			specBuilder.polygonIdentifier(polygonId);
			specBuilder.layerType(layerType);
			specBuilder.genus("B");
			specBuilder.percentGenus(100f);
		})));
		siteStream.addValue(Collections.singleton(VriSite.build(siteBuilder -> {
			siteBuilder.polygonIdentifier(polygonId);
			siteBuilder.layerType(layerType);
			siteBuilder.siteGenus("B");
			siteBuilder.siteSpecies("B");
		})));

		var result = app.getPolygon(polyStream, layerStream, speciesStream, siteStream);

		assertThat(result, hasProperty("layers", Matchers.aMapWithSize(1)));
		var primaryResult = result.getLayers().get(LayerType.PRIMARY);
		var veteranResult = result.getLayers().get(LayerType.VETERAN);
		primaryResult.getPrimaryGenus();
		assertThat(
				primaryResult, allOf(
						hasProperty("polygonIdentifier", is(polygonId)), //
						hasProperty("layerType", is(LayerType.PRIMARY)), //
						hasProperty("crownClosure", is(95f)), //
						hasProperty("utilization", is(7.5f)), // Raised to minimum
						hasProperty("baseArea", present(is(20f))), //
						hasProperty("treesPerHectare", present(is(300f))), //
						hasProperty("primaryGenus", present(is("B"))), //
						hasProperty("secondaryGenus", notPresent())
				)
		);

		assertThat(veteranResult, nullValue());

		app.close();
	}

	@Test
	void testVeteranOnly() throws IOException, StandProcessingException, ResourceParseException {
		var app = new VriStart();

		final var polygonId = new PolygonIdentifier("Test", 2024);
		final var layerType = LayerType.VETERAN;

		mockControl.replay();

		app.init(resolver, controlMap);

		var polyStream = new MockStreamingParser<VriPolygon>();
		var layerStream = new MockStreamingParser<Map<LayerType, VriLayer.Builder>>();
		var speciesStream = new MockStreamingParser<Collection<VriSpecies>>();
		var siteStream = new MockStreamingParser<Collection<VriSite>>();

		polyStream.addValue(VriPolygon.build(polyBuilder -> {
			polyBuilder.polygonIdentifier(polygonId);
			polyBuilder.percentAvailable(Optional.of(100.0f));
			polyBuilder.biogeoclimaticZone("IDF");
			polyBuilder.yieldFactor(0.9f);
		}));

		var layerBuilder = new VriLayer.Builder();
		layerBuilder.polygonIdentifier(polygonId);
		layerBuilder.layerType(layerType);
		layerBuilder.crownClosure(95f);
		layerBuilder.utilization(0.6f);
		layerBuilder.baseArea(20);
		layerBuilder.treesPerHectare(300);
		layerStream.addValue(Collections.singletonMap(layerType, layerBuilder));

		speciesStream.addValue(Collections.singleton(VriSpecies.build(specBuilder -> {
			specBuilder.polygonIdentifier(polygonId);
			specBuilder.layerType(layerType);
			specBuilder.genus("B");
			specBuilder.percentGenus(100f);
		})));
		siteStream.addValue(Collections.singleton(VriSite.build(siteBuilder -> {
			siteBuilder.polygonIdentifier(polygonId);
			siteBuilder.layerType(layerType);
			siteBuilder.siteGenus("B");
			siteBuilder.siteSpecies("B");
		})));

		var result = app.getPolygon(polyStream, layerStream, speciesStream, siteStream);

		assertThat(result, hasProperty("layers", Matchers.aMapWithSize(1)));
		var primaryResult = result.getLayers().get(LayerType.PRIMARY);
		var veteranResult = result.getLayers().get(LayerType.VETERAN);
		assertThat(
				veteranResult, allOf(
						hasProperty("polygonIdentifier", is(polygonId)), //
						hasProperty("layerType", is(LayerType.VETERAN)), //
						hasProperty("crownClosure", is(95f)), //
						hasProperty("utilization", is(7.5f)), // Raised to minimum
						hasProperty("baseArea", present(is(20f))), //
						hasProperty("treesPerHectare", present(is(300f))), //
						hasProperty("primaryGenus", present(is("B"))), //
						hasProperty("secondaryGenus", notPresent())
				)
		);

		assertThat(primaryResult, nullValue());

		app.close();
	}

	@Test
	void testApplyPercentAvailableToPrimaryLayer()
			throws IOException, StandProcessingException, ResourceParseException {
		var app = new VriStart();

		final var polygonId = new PolygonIdentifier("Test", 2024);
		final var layerType = LayerType.VETERAN;

		mockControl.replay();

		app.init(resolver, controlMap);

		var polyStream = new MockStreamingParser<VriPolygon>();
		var layerStream = new MockStreamingParser<Map<LayerType, VriLayer.Builder>>();
		var speciesStream = new MockStreamingParser<Collection<VriSpecies>>();
		var siteStream = new MockStreamingParser<Collection<VriSite>>();

		polyStream.addValue(VriPolygon.build(polyBuilder -> {
			polyBuilder.polygonIdentifier(polygonId);
			polyBuilder.percentAvailable(Optional.of(75.0f));
			polyBuilder.biogeoclimaticZone("IDF");
			polyBuilder.yieldFactor(0.9f);
		}));

		var layerBuilder1 = new VriLayer.Builder();
		layerBuilder1.polygonIdentifier(polygonId);
		layerBuilder1.layerType(LayerType.PRIMARY);
		layerBuilder1.crownClosure(95f);
		layerBuilder1.utilization(9f);
		layerBuilder1.baseArea(20);
		layerBuilder1.treesPerHectare(300);
		var layerBuilder2 = new VriLayer.Builder();
		layerBuilder2.polygonIdentifier(polygonId);
		layerBuilder2.layerType(LayerType.VETERAN);
		layerBuilder2.crownClosure(80f);
		layerBuilder2.utilization(8f);
		layerBuilder2.baseArea(30);
		layerBuilder2.treesPerHectare(200);
		layerStream.addValue(Utils.constMap(map -> {
			map.put(LayerType.PRIMARY, layerBuilder1);
			map.put(LayerType.VETERAN, layerBuilder2);
		}));

		speciesStream.addValue(Collections.singleton(VriSpecies.build(specBuilder -> {
			specBuilder.polygonIdentifier(polygonId);
			specBuilder.layerType(layerType);
			specBuilder.genus("B");
			specBuilder.percentGenus(100f);
		})));
		siteStream.addValue(Collections.singleton(VriSite.build(siteBuilder -> {
			siteBuilder.polygonIdentifier(polygonId);
			siteBuilder.layerType(layerType);
			siteBuilder.siteGenus("B");
			siteBuilder.siteSpecies("B");
		})));

		var result = app.getPolygon(polyStream, layerStream, speciesStream, siteStream);

		assertThat(result, hasProperty("layers", Matchers.aMapWithSize(2)));
		var primaryResult = result.getLayers().get(LayerType.PRIMARY);
		var veteranResult = result.getLayers().get(LayerType.VETERAN);
		assertThat(
				primaryResult, allOf(
						hasProperty("polygonIdentifier", is(polygonId)), //
						hasProperty("layerType", is(LayerType.PRIMARY)), //
						hasProperty("crownClosure", is(95f)), //
						hasProperty("utilization", is(9f)), hasProperty(
								"baseArea", //
								present(closeTo(20f * 0.75f))
						), // Apply Layer Percent Available
						hasProperty("treesPerHectare", present(closeTo(300f * 0.75f))) // Apply Layer Percent Available
				)
		);

		assertThat(
				veteranResult, allOf(
						hasProperty("polygonIdentifier", is(polygonId)), //
						hasProperty("layerType", is(LayerType.VETERAN)), //
						hasProperty("crownClosure", is(80f)), //
						hasProperty("utilization", is(8f)), //
						hasProperty("baseArea", present(is(30f))), // Don't Apply Layer Percent Available
						hasProperty("treesPerHectare", present(is(200f))) // Don't Apply Layer Percent Available
				)
		);

		app.close();
	}

	@Test
	void testPrimaryWithSmallComputedDiameter() throws IOException, StandProcessingException, ResourceParseException {
		var app = new VriStart();

		final var polygonId = new PolygonIdentifier("Test", 2024);
		final var layerType = LayerType.PRIMARY;

		mockControl.replay();

		app.init(resolver, controlMap);

		var polyStream = new MockStreamingParser<VriPolygon>();
		var layerStream = new MockStreamingParser<Map<LayerType, VriLayer.Builder>>();
		var speciesStream = new MockStreamingParser<Collection<VriSpecies>>();
		var siteStream = new MockStreamingParser<Collection<VriSite>>();

		polyStream.addValue(VriPolygon.build(polyBuilder -> {
			polyBuilder.polygonIdentifier(polygonId);
			polyBuilder.percentAvailable(Optional.of(100.0f));
			polyBuilder.biogeoclimaticZone("IDF");
			polyBuilder.yieldFactor(0.9f);
		}));

		var layerBuilder = new VriLayer.Builder();
		layerBuilder.polygonIdentifier(polygonId);
		layerBuilder.layerType(layerType);
		layerBuilder.crownClosure(95f);
		layerBuilder.utilization(0.6f);
		layerBuilder.baseArea(20);
		layerBuilder.treesPerHectare(300);
		layerStream.addValue(Collections.singletonMap(layerType, layerBuilder));

		speciesStream.addValue(Collections.singleton(VriSpecies.build(specBuilder -> {
			specBuilder.polygonIdentifier(polygonId);
			specBuilder.layerType(layerType);
			specBuilder.genus("B");
			specBuilder.percentGenus(100f);
		})));
		siteStream.addValue(Collections.singleton(VriSite.build(siteBuilder -> {
			siteBuilder.polygonIdentifier(polygonId);
			siteBuilder.layerType(layerType);
			siteBuilder.siteGenus("B");
			siteBuilder.siteSpecies("B");
		})));

		var result = app.getPolygon(polyStream, layerStream, speciesStream, siteStream);

		assertThat(result, hasProperty("layers", Matchers.aMapWithSize(1)));
		var primaryResult = result.getLayers().get(LayerType.PRIMARY);
		var veteranResult = result.getLayers().get(LayerType.VETERAN);
		assertThat(
				primaryResult, allOf(
						hasProperty("polygonIdentifier", is(polygonId)), //
						hasProperty("layerType", is(LayerType.PRIMARY)), //
						hasProperty("crownClosure", is(95f)), //
						hasProperty("utilization", is(7.5f)), // Raised to minimum
						hasProperty("baseArea", present(is(20f))), //
						hasProperty("treesPerHectare", present(is(300f)))
				)
		);
		assertThat(veteranResult, nullValue());

		app.close();
	}

	@Test
	void testFindsPrimaryGenusAndITG() throws IOException, StandProcessingException, ResourceParseException {
		var app = new VriStart();

		final var polygonId = new PolygonIdentifier("Test", 2024);
		final var layerType = LayerType.PRIMARY;

		mockControl.replay();

		app.init(resolver, controlMap);

		var polyStream = new MockStreamingParser<VriPolygon>();
		var layerStream = new MockStreamingParser<Map<LayerType, VriLayer.Builder>>();
		var speciesStream = new MockStreamingParser<Collection<VriSpecies>>();
		var siteStream = new MockStreamingParser<Collection<VriSite>>();

		polyStream.addValue(VriPolygon.build(polyBuilder -> {
			polyBuilder.polygonIdentifier(polygonId);
			polyBuilder.percentAvailable(Optional.of(100.0f));
			polyBuilder.biogeoclimaticZone("IDF");
			polyBuilder.yieldFactor(0.9f);
		}));

		layerStream.addValue(Utils.constMap(map -> {
			var layerBuilder = new VriLayer.Builder();
			layerBuilder.polygonIdentifier(polygonId);
			layerBuilder.layerType(LayerType.PRIMARY);
			layerBuilder.crownClosure(95f);
			layerBuilder.utilization(0.6f);
			layerBuilder.baseArea(20);
			layerBuilder.treesPerHectare(300);
			map.put(LayerType.PRIMARY, layerBuilder);

			layerBuilder = new VriLayer.Builder();
			layerBuilder.polygonIdentifier(polygonId);
			layerBuilder.layerType(LayerType.VETERAN);
			layerBuilder.crownClosure(85f);
			layerBuilder.utilization(0.7f);
			layerBuilder.baseArea(30);
			layerBuilder.treesPerHectare(200);
			map.put(LayerType.VETERAN, layerBuilder);
		}));

		speciesStream.addValue(List.of(VriSpecies.build(specBuilder -> {
			specBuilder.polygonIdentifier(polygonId);
			specBuilder.layerType(layerType);
			specBuilder.genus("B");
			specBuilder.percentGenus(80f);
		}), VriSpecies.build(specBuilder -> {
			specBuilder.polygonIdentifier(polygonId);
			specBuilder.layerType(layerType);
			specBuilder.genus("S");
			specBuilder.percentGenus(20f);
		})));
		siteStream.addValue(List.of(VriSite.build(siteBuilder -> {
			siteBuilder.polygonIdentifier(polygonId);
			siteBuilder.layerType(layerType);
			siteBuilder.siteGenus("B");
			siteBuilder.siteSpecies("B");
		}), VriSite.build(siteBuilder -> {
			siteBuilder.polygonIdentifier(polygonId);
			siteBuilder.layerType(layerType);
			siteBuilder.siteGenus("S");
			siteBuilder.siteSpecies("S");
		})));

		var result = app.getPolygon(polyStream, layerStream, speciesStream, siteStream);

		assertThat(result, hasProperty("layers", Matchers.aMapWithSize(2)));
		var primaryResult = result.getLayers().get(LayerType.PRIMARY);
		var veteranResult = result.getLayers().get(LayerType.VETERAN);
		assertThat(
				primaryResult, allOf(
						hasProperty("primaryGenus", present(is("B"))), //
						hasProperty("secondaryGenus", present(is("S"))), //
						hasProperty("inventoryTypeGroup", present(is(18))) // ITG for a pure (80%) B layer
				)
		);

		assertThat(
				veteranResult, allOf(
						// Veteran layer should not have primary genus or ITG
						hasProperty("primaryGenus", notPresent()), //
						hasProperty("secondaryGenus", notPresent()), //
						hasProperty("inventoryTypeGroup", notPresent())
				)
		);

		app.close();
	}

	@Test
	void testFindsGRPBA1() throws IOException, StandProcessingException, ResourceParseException {
		var app = new VriStart();

		final var polygonId = new PolygonIdentifier("Test", 2024);
		final var layerType = LayerType.PRIMARY;

		mockControl.replay();

		app.init(resolver, controlMap);

		var polyStream = new MockStreamingParser<VriPolygon>();
		var layerStream = new MockStreamingParser<Map<LayerType, VriLayer.Builder>>();
		var speciesStream = new MockStreamingParser<Collection<VriSpecies>>();
		var siteStream = new MockStreamingParser<Collection<VriSite>>();

		polyStream.addValue(VriPolygon.build(polyBuilder -> {
			polyBuilder.polygonIdentifier(polygonId);
			polyBuilder.percentAvailable(Optional.of(100.0f));
			polyBuilder.biogeoclimaticZone("IDF");
			polyBuilder.yieldFactor(0.9f);
		}));

		var layerBuilder = new VriLayer.Builder();
		layerBuilder.polygonIdentifier(polygonId);
		layerBuilder.layerType(layerType);
		layerBuilder.crownClosure(95f);
		layerBuilder.utilization(0.6f);
		layerBuilder.baseArea(20);
		layerBuilder.treesPerHectare(300);
		layerStream.addValue(Collections.singletonMap(layerType, layerBuilder));

		speciesStream.addValue(List.of(VriSpecies.build(specBuilder -> {
			specBuilder.polygonIdentifier(polygonId);
			specBuilder.layerType(layerType);
			specBuilder.genus("B");
			specBuilder.percentGenus(80f);
		}), VriSpecies.build(specBuilder -> {
			specBuilder.polygonIdentifier(polygonId);
			specBuilder.layerType(layerType);
			specBuilder.genus("S");
			specBuilder.percentGenus(20f);
		})));
		siteStream.addValue(List.of(VriSite.build(siteBuilder -> {
			siteBuilder.polygonIdentifier(polygonId);
			siteBuilder.layerType(layerType);
			siteBuilder.siteGenus("B");
			siteBuilder.siteSpecies("B");
		}), VriSite.build(siteBuilder -> {
			siteBuilder.polygonIdentifier(polygonId);
			siteBuilder.layerType(layerType);
			siteBuilder.siteGenus("S");
			siteBuilder.siteSpecies("S");
		})));

		var result = app.getPolygon(polyStream, layerStream, speciesStream, siteStream);

		assertThat(result, hasProperty("layers", Matchers.aMapWithSize(1)));
		var primaryResult = result.getLayers().get(LayerType.PRIMARY);
		var veteranResult = result.getLayers().get(LayerType.VETERAN);
		assertThat(primaryResult, allOf(hasProperty("empericalRelationshipParameterIndex", present(is(27)))));
		assertThat(veteranResult, nullValue());

		app.close();
		mockControl.verify();
	}

	@ParameterizedTest
	@CsvSource(
		{ //
				"20.0, 200.0, 88.0, 25.0, 20.0, 200.0", // If BA and TPH are present for Veteran layer, do nothing
				",     200.0, 88.0, 25.0, 22.0,   0.0", // If BA is missing, set TPH to 0 and BA ot the BA of the
				// primary layer times the CC
				",     200.0, 88.0,     , 44.0,   0.0", // If BA is missing for vet and prime, set BA to half the CC
				"20.0,      , 88.0, 25.0, 22.0,   0.0", // If TPH is missing, set TPH to 0 and BA ot the BA of the
				// primary layer times the CC
				"20.0,      , 88.0,     , 44.0,   0.0", // If TPH is missing for vet and prime, set BA to half the
				// CC
				"0.0,  200.0, 88.0, 25.0, 22.0,   0.0", // If BA is not positive, set TPH to 0 and BA ot the BA of
				// the primary layer times the CC
				"0.0,  200.0, 88.0,     , 44.0,   0.0", // If BA is not positive for vet and prime, set BA to half
				// the CC
				"20.0,   0.0, 88.0, 25.0, 22.0,   0.0", // If TPH is not positive, set TPH to 0 and BA ot the BA of
				// the primary layer times the CC
				"20.0,   0.0, 88.0,     , 44.0,   0.0", // If TPH is not positive for vet and prime, set BA to half
														// the CC
		}
	)

	void testDefaultBaAndTphForVeteran(
			Float vetBaseArea, Float vetTreesPerHectare, float vetCrownClosure, Float primeBaseArea,
			float expectedBaseArea, float expectedTreesPerHectare
	) throws IOException, StandProcessingException, ResourceParseException {
		var app = new VriStart();

		final var polygonId = new PolygonIdentifier("Test", 2024);
		final var layerType = LayerType.VETERAN;

		mockControl.replay();

		app.init(resolver, controlMap);

		var polyStream = new MockStreamingParser<VriPolygon>();
		var layerStream = new MockStreamingParser<Map<LayerType, VriLayer.Builder>>();
		var speciesStream = new MockStreamingParser<Collection<VriSpecies>>();
		var siteStream = new MockStreamingParser<Collection<VriSite>>();

		polyStream.addValue(VriPolygon.build(polyBuilder -> {
			polyBuilder.polygonIdentifier(polygonId);
			polyBuilder.percentAvailable(Optional.of(75.0f));
			polyBuilder.biogeoclimaticZone("IDF");
			polyBuilder.yieldFactor(0.9f);
		}));

		var layerBuilder1 = new VriLayer.Builder();
		layerBuilder1.polygonIdentifier(polygonId);
		layerBuilder1.layerType(LayerType.PRIMARY);
		layerBuilder1.crownClosure(95f);
		layerBuilder1.utilization(9f);
		layerBuilder1.baseArea(Optional.ofNullable(primeBaseArea));
		layerBuilder1.treesPerHectare(300);
		var layerBuilder2 = new VriLayer.Builder();
		layerBuilder2.polygonIdentifier(polygonId);
		layerBuilder2.layerType(LayerType.VETERAN);
		layerBuilder2.crownClosure(vetCrownClosure);
		layerBuilder2.utilization(8f);
		layerBuilder2.baseArea(Optional.ofNullable(vetBaseArea));
		layerBuilder2.treesPerHectare(Optional.ofNullable(vetTreesPerHectare));
		layerStream.addValue(Utils.constMap(map -> {
			map.put(LayerType.PRIMARY, layerBuilder1);
			map.put(LayerType.VETERAN, layerBuilder2);
		}));

		speciesStream.addValue(Collections.singleton(VriSpecies.build(specBuilder -> {
			specBuilder.polygonIdentifier(polygonId);
			specBuilder.layerType(layerType);
			specBuilder.genus("B");
			specBuilder.percentGenus(100f);
		})));
		siteStream.addValue(Collections.singleton(VriSite.build(siteBuilder -> {
			siteBuilder.polygonIdentifier(polygonId);
			siteBuilder.layerType(layerType);
			siteBuilder.siteGenus("B");
			siteBuilder.siteSpecies("B");
		})));

		var result = app.getPolygon(polyStream, layerStream, speciesStream, siteStream);

		assertThat(result, hasProperty("layers", Matchers.aMapWithSize(2)));
		var veteranResult = result.getLayers().get(LayerType.VETERAN);
		assertThat(
				veteranResult, allOf(
						hasProperty("polygonIdentifier", is(polygonId)), //
						hasProperty("layerType", is(LayerType.VETERAN)), //
						hasProperty("crownClosure", is(88f)), //
						hasProperty("utilization", is(8f)), //
						hasProperty("baseArea", present(closeTo(expectedBaseArea))),
						hasProperty("treesPerHectare", present(closeTo(expectedTreesPerHectare)))
				)
		);

		app.close();
	}

	@ParameterizedTest
	@CsvSource(
		{ //
				",     200.0", // If BA is missing
				"20.0,      ", // If TPH is missing
				"0.0,  200.0", // If BA is not positive
				"20.0,   0.0", // If TPH is not positive
		}
	)
	void testDefaultBaAndTphForVeteranWhenZeroCrownClosure(Float vetBaseArea, Float vetTreesPerHectare)
			throws IOException, StandProcessingException, ResourceParseException {
		var app = new VriStart();

		final var polygonId = new PolygonIdentifier("Test", 2024);
		final var layerType = LayerType.VETERAN;

		mockControl.replay();

		app.init(resolver, controlMap);

		var polyStream = new MockStreamingParser<VriPolygon>();
		var layerStream = new MockStreamingParser<Map<LayerType, VriLayer.Builder>>();
		var speciesStream = new MockStreamingParser<Collection<VriSpecies>>();
		var siteStream = new MockStreamingParser<Collection<VriSite>>();

		polyStream.addValue(VriPolygon.build(polyBuilder -> {
			polyBuilder.polygonIdentifier(polygonId);
			polyBuilder.percentAvailable(Optional.of(75.0f));
			polyBuilder.biogeoclimaticZone("IDF");
			polyBuilder.yieldFactor(0.9f);
		}));

		var layerBuilder1 = new VriLayer.Builder();
		layerBuilder1.polygonIdentifier(polygonId);
		layerBuilder1.layerType(LayerType.PRIMARY);
		layerBuilder1.crownClosure(95f);
		layerBuilder1.utilization(9f);
		layerBuilder1.baseArea(25);
		layerBuilder1.treesPerHectare(300);
		var layerBuilder2 = new VriLayer.Builder();
		layerBuilder2.polygonIdentifier(polygonId);
		layerBuilder2.layerType(LayerType.VETERAN);
		layerBuilder2.crownClosure(0.0f); // Set to zero
		layerBuilder2.utilization(8f);
		layerBuilder2.baseArea(Optional.ofNullable(vetBaseArea));
		layerBuilder2.treesPerHectare(Optional.ofNullable(vetTreesPerHectare));
		layerStream.addValue(Utils.constMap(map -> {
			map.put(LayerType.PRIMARY, layerBuilder1);
			map.put(LayerType.VETERAN, layerBuilder2);
		}));

		speciesStream.addValue(Collections.singleton(VriSpecies.build(specBuilder -> {
			specBuilder.polygonIdentifier(polygonId);
			specBuilder.layerType(layerType);
			specBuilder.genus("B");
			specBuilder.percentGenus(100f);
		})));
		siteStream.addValue(Collections.singleton(VriSite.build(siteBuilder -> {
			siteBuilder.polygonIdentifier(polygonId);
			siteBuilder.layerType(layerType);
			siteBuilder.siteGenus("B");
			siteBuilder.siteSpecies("B");
		})));

		var ex = assertThrows(
				StandProcessingException.class, () -> app.getPolygon(polyStream, layerStream, speciesStream, siteStream)
		);

		assertThat(ex, hasProperty("message", is("Expected a positive crown closure for veteran layer but was 0.0")));

		app.close();
	}

}
