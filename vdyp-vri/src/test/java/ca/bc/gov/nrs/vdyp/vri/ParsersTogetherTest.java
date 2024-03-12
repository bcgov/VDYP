package ca.bc.gov.nrs.vdyp.vri;

import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.closeTo;
import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.present;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import ca.bc.gov.nrs.vdyp.application.StandProcessingException;
import ca.bc.gov.nrs.vdyp.common.ControlKey;
import ca.bc.gov.nrs.vdyp.common.Utils;
import ca.bc.gov.nrs.vdyp.io.parse.common.ResourceParseException;
import ca.bc.gov.nrs.vdyp.io.parse.streaming.MockStreamingParser;
import ca.bc.gov.nrs.vdyp.model.LayerType;
import ca.bc.gov.nrs.vdyp.test.MockFileResolver;
import ca.bc.gov.nrs.vdyp.vri.model.VriLayer;
import ca.bc.gov.nrs.vdyp.vri.model.VriPolygon;
import ca.bc.gov.nrs.vdyp.vri.model.VriSite;
import ca.bc.gov.nrs.vdyp.vri.model.VriSpecies;

class ParsersTogetherTest {

	@Test
	void testPrimaryOnly() throws IOException, StandProcessingException, ResourceParseException {
		var app = new VriStart();

		var controlMap = new HashMap<String, Object>();

		controlMap.put(ControlKey.VRI_OUTPUT_VDYP_POLYGON.name(), "DUMMY1");
		controlMap.put(ControlKey.VRI_OUTPUT_VDYP_LAYER_BY_SPECIES.name(), "DUMMY2");
		controlMap.put(ControlKey.VRI_OUTPUT_VDYP_LAYER_BY_SP0_BY_UTIL.name(), "DUMMY3");

		final var polygonId = "Test";
		final var layerType = LayerType.PRIMARY;

		MockFileResolver resolver = new MockFileResolver("Test");
		resolver.addStream("DUMMY1", (OutputStream) new ByteArrayOutputStream());
		resolver.addStream("DUMMY2", (OutputStream) new ByteArrayOutputStream());
		resolver.addStream("DUMMY3", (OutputStream) new ByteArrayOutputStream());

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
		layerBuilder.crownClosure(0.95f);
		layerBuilder.utilization(0.6f);
		layerBuilder.baseArea(20);
		layerBuilder.treesPerHectare(300);
		layerStream.addValue(Collections.singletonMap(layerType, layerBuilder));

		speciesStream.addValue(Collections.singleton(VriSpecies.build(specBuilder -> {
			specBuilder.polygonIdentifier("Test");
			specBuilder.layerType(layerType);
			specBuilder.genus("W");
			specBuilder.percentGenus(100f);
		})));
		siteStream.addValue(Collections.singleton(VriSite.build(siteBuilder -> {
			siteBuilder.polygonIdentifier("Test");
			siteBuilder.layerType(layerType);
			siteBuilder.siteGenus("W");
			siteBuilder.siteSpecies("W");
		})));

		var result = app.getPolygon(polyStream, layerStream, speciesStream, siteStream);

		assertThat(result, hasProperty("layers", Matchers.aMapWithSize(2)));
		var primaryResult = result.getLayers().get(LayerType.PRIMARY);
		var veteranResult = result.getLayers().get(LayerType.VETERAN);
		assertThat(
				primaryResult, allOf(
						hasProperty("polygonIdentifier", is(polygonId)), //
						hasProperty("layer", is(LayerType.PRIMARY)), //
						hasProperty("crownClosure", is(0.95f)), //
						hasProperty("utilization", is(7.5f)), // Raised to minimum
						hasProperty("baseArea", present(is(20f))), //
						hasProperty("treesPerHectare", present(is(300f)))
				)
		);
		// Set to defaults, not that optional values should be present with a value of 0
		// instead of not present per the VDYP7 Fortran
		assertThat(
				veteranResult, allOf(
						hasProperty("polygonIdentifier", is(polygonId)), //
						hasProperty("layer", is(LayerType.VETERAN)), //
						hasProperty("crownClosure", is(0f)), //
						hasProperty("utilization", is(7.5f)), //
						hasProperty("baseArea", present(is(0f))), //
						hasProperty("treesPerHectare", present(is(0f)))
				)
		);

		app.close();
	}

	@Test
	void testVeteranOnly() throws IOException, StandProcessingException, ResourceParseException {
		var app = new VriStart();

		var controlMap = new HashMap<String, Object>();

		controlMap.put(ControlKey.VRI_OUTPUT_VDYP_POLYGON.name(), "DUMMY1");
		controlMap.put(ControlKey.VRI_OUTPUT_VDYP_LAYER_BY_SPECIES.name(), "DUMMY2");
		controlMap.put(ControlKey.VRI_OUTPUT_VDYP_LAYER_BY_SP0_BY_UTIL.name(), "DUMMY3");

		final var polygonId = "Test";
		final var layerType = LayerType.VETERAN;

		MockFileResolver resolver = new MockFileResolver("Test");
		resolver.addStream("DUMMY1", (OutputStream) new ByteArrayOutputStream());
		resolver.addStream("DUMMY2", (OutputStream) new ByteArrayOutputStream());
		resolver.addStream("DUMMY3", (OutputStream) new ByteArrayOutputStream());

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
		layerBuilder.crownClosure(0.95f);
		layerBuilder.utilization(0.6f);
		layerBuilder.baseArea(20);
		layerBuilder.treesPerHectare(300);
		layerStream.addValue(Collections.singletonMap(layerType, layerBuilder));

		speciesStream.addValue(Collections.singleton(VriSpecies.build(specBuilder -> {
			specBuilder.polygonIdentifier("Test");
			specBuilder.layerType(layerType);
			specBuilder.genus("W");
			specBuilder.percentGenus(100f);
		})));
		siteStream.addValue(Collections.singleton(VriSite.build(siteBuilder -> {
			siteBuilder.polygonIdentifier("Test");
			siteBuilder.layerType(layerType);
			siteBuilder.siteGenus("W");
			siteBuilder.siteSpecies("W");
		})));

		var result = app.getPolygon(polyStream, layerStream, speciesStream, siteStream);

		assertThat(result, hasProperty("layers", Matchers.aMapWithSize(2)));
		var primaryResult = result.getLayers().get(LayerType.PRIMARY);
		var veteranResult = result.getLayers().get(LayerType.VETERAN);
		assertThat(
				veteranResult, allOf(
						hasProperty("polygonIdentifier", is(polygonId)), //
						hasProperty("layer", is(LayerType.VETERAN)), //
						hasProperty("crownClosure", is(0.95f)), //
						hasProperty("utilization", is(7.5f)), // Raised to minimum
						hasProperty("baseArea", present(is(20f))), //
						hasProperty("treesPerHectare", present(is(300f)))
				)
		);
		// Set to defaults, not that optional values should be present with a value of 0
		// instead of not present per the VDYP7 Fortran
		assertThat(
				primaryResult, allOf(
						hasProperty("polygonIdentifier", is(polygonId)), //
						hasProperty("layer", is(LayerType.PRIMARY)), //
						hasProperty("crownClosure", is(0f)), //
						hasProperty("utilization", is(7.5f)), //
						hasProperty("baseArea", present(is(0f))), //
						hasProperty("treesPerHectare", present(is(0f)))
				)
		);
		app.close();
	}

	@Test
	void testApplyPercentAvailableToPrimaryLayer()
			throws IOException, StandProcessingException, ResourceParseException {
		var app = new VriStart();

		var controlMap = new HashMap<String, Object>();

		controlMap.put(ControlKey.VRI_OUTPUT_VDYP_POLYGON.name(), "DUMMY1");
		controlMap.put(ControlKey.VRI_OUTPUT_VDYP_LAYER_BY_SPECIES.name(), "DUMMY2");
		controlMap.put(ControlKey.VRI_OUTPUT_VDYP_LAYER_BY_SP0_BY_UTIL.name(), "DUMMY3");

		final var polygonId = "Test";
		final var layerType = LayerType.VETERAN;

		MockFileResolver resolver = new MockFileResolver("Test");
		resolver.addStream("DUMMY1", (OutputStream) new ByteArrayOutputStream());
		resolver.addStream("DUMMY2", (OutputStream) new ByteArrayOutputStream());
		resolver.addStream("DUMMY3", (OutputStream) new ByteArrayOutputStream());

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
		layerBuilder1.crownClosure(0.95f);
		layerBuilder1.utilization(9f);
		layerBuilder1.baseArea(20);
		layerBuilder1.treesPerHectare(300);
		var layerBuilder2 = new VriLayer.Builder();
		layerBuilder2.polygonIdentifier(polygonId);
		layerBuilder2.layerType(LayerType.VETERAN);
		layerBuilder2.crownClosure(0.8f);
		layerBuilder2.utilization(8f);
		layerBuilder2.baseArea(30);
		layerBuilder2.treesPerHectare(200);
		layerStream.addValue(Utils.constMap(map -> {
			map.put(LayerType.PRIMARY, layerBuilder1);
			map.put(LayerType.VETERAN, layerBuilder2);
		}));

		speciesStream.addValue(Collections.singleton(VriSpecies.build(specBuilder -> {
			specBuilder.polygonIdentifier("Test");
			specBuilder.layerType(layerType);
			specBuilder.genus("W");
			specBuilder.percentGenus(100f);
		})));
		siteStream.addValue(Collections.singleton(VriSite.build(siteBuilder -> {
			siteBuilder.polygonIdentifier("Test");
			siteBuilder.layerType(layerType);
			siteBuilder.siteGenus("W");
			siteBuilder.siteSpecies("W");
		})));

		var result = app.getPolygon(polyStream, layerStream, speciesStream, siteStream);

		assertThat(result, hasProperty("layers", Matchers.aMapWithSize(2)));
		var primaryResult = result.getLayers().get(LayerType.PRIMARY);
		var veteranResult = result.getLayers().get(LayerType.VETERAN);
		assertThat(
				primaryResult, allOf(
						hasProperty("polygonIdentifier", is(polygonId)), //
						hasProperty("layer", is(LayerType.PRIMARY)), //
						hasProperty("crownClosure", is(0.95f)), //
						hasProperty("utilization", is(9f)), hasProperty("baseArea", present(closeTo(20f * 0.75f))), // Apply
																													// Layer
																													// Percent
																													// Available
						hasProperty("treesPerHectare", present(closeTo(300f * 0.75f))) // Apply Layer Percent Available
				)
		);
		// Set to defaults, not that optional values should be present with a value of 0
		// instead of not present per the VDYP7 Fortran
		assertThat(
				veteranResult, allOf(
						hasProperty("polygonIdentifier", is(polygonId)), //
						hasProperty("layer", is(LayerType.VETERAN)), //
						hasProperty("crownClosure", is(0.8f)), //
						hasProperty("utilization", is(8f)), //
						hasProperty("baseArea", present(is(30f))), //
						hasProperty("treesPerHectare", present(is(200f)))
				)
		);
		app.close();
	}
}
