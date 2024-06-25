package ca.bc.gov.nrs.vdyp.model;

import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.isPolyId;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.easymock.EasyMock;
import org.junit.jupiter.api.Test;

class VdypPolygonTest {

	@Test
	void build() throws Exception {
		var result = VdypPolygon.build(builder -> {
			builder.polygonIdentifier("Test", 2024);
			builder.percentAvailable(90f);

			builder.forestInventoryZone("?");
			builder.biogeoclimaticZone("?");
		});
		assertThat(result, hasProperty("polygonIdentifier", isPolyId("Test", 2024)));
		assertThat(result, hasProperty("percentAvailable", is(90f)));
		assertThat(result, hasProperty("layers", anEmptyMap()));
	}

	@Test
	void buildNoProperties() throws Exception {
		var ex = assertThrows(IllegalStateException.class, () -> VdypPolygon.build(builder -> {
		}));
		assertThat(
				ex,
				hasProperty("message", allOf(containsString("polygonIdentifier"), containsString("percentAvailable")))
		);
	}

	@Test
	void buildAddLayer() throws Exception {
		VdypLayer mock = EasyMock.mock(VdypLayer.class);
		EasyMock.expect(mock.getLayerType()).andStubReturn(LayerType.PRIMARY);
		EasyMock.replay(mock);
		var result = VdypPolygon.build(builder -> {
			builder.polygonIdentifier("Test", 2024);
			builder.percentAvailable(90f);

			builder.forestInventoryZone("?");
			builder.biogeoclimaticZone("?");

			builder.addLayer(mock);
		});
		assertThat(result, hasProperty("polygonIdentifier", isPolyId("Test", 2024)));
		assertThat(result, hasProperty("percentAvailable", is(90f)));
		assertThat(result, hasProperty("layers", hasEntry(LayerType.PRIMARY, mock)));
	}

	@Test
	void buildAddLayerSubBuild() throws Exception {
		var result = VdypPolygon.build(builder -> {
			builder.polygonIdentifier("Test", 2024);
			builder.percentAvailable(90f);

			builder.forestInventoryZone("?");
			builder.biogeoclimaticZone("?");

			builder.addLayer(layerBuilder -> {
				layerBuilder.layerType(LayerType.PRIMARY);
			});
		});
		assertThat(result, hasProperty("polygonIdentifier", isPolyId("Test", 2024)));
		assertThat(result, hasProperty("percentAvailable", is(90f)));
		assertThat(result, hasProperty("layers", hasEntry(is(LayerType.PRIMARY), anything())));
		var resultLayer = result.getLayers().get(LayerType.PRIMARY);

		assertThat(resultLayer, hasProperty("polygonIdentifier", isPolyId("Test", 2024)));
		assertThat(resultLayer, hasProperty("layerType", is(LayerType.PRIMARY)));
	}

	@Test
	void copyWithoutLayers() throws Exception {
		var toCopy = VdypPolygon.build(builder -> {
			builder.polygonIdentifier("Test", 2024);
			builder.percentAvailable(90f);

			builder.forestInventoryZone("Z");
			builder.biogeoclimaticZone("IDF");

			builder.addLayer(layerBuilder -> {
				layerBuilder.layerType(LayerType.PRIMARY);
			});
		});

		var result = VdypPolygon.build(builder -> {
			builder.copy(toCopy);
		});
		assertThat(result, hasProperty("polygonIdentifier", isPolyId("Test", 2024)));
		assertThat(result, hasProperty("percentAvailable", is(90f)));
		assertThat(result, hasProperty("forestInventoryZone", is("Z")));
		assertThat(result, hasProperty("biogeoclimaticZone", is("IDF")));
		assertThat(result, hasProperty("layers", anEmptyMap()));
	}

	@Test
	void copyWithLayers() throws Exception {
		var toCopy = VdypPolygon.build(builder -> {
			builder.polygonIdentifier("Test", 2024);
			builder.percentAvailable(90f);

			builder.forestInventoryZone("Z");
			builder.biogeoclimaticZone("IDF");

			builder.addLayer(layerBuilder -> {
				layerBuilder.layerType(LayerType.PRIMARY);
			});
		});

		var result = VdypPolygon.build(builder -> {
			builder.copy(toCopy);
			builder.copyLayers(toCopy, (layerBuilder, layer) -> {
				// Do nothing
			});
		});
		assertThat(result, hasProperty("polygonIdentifier", isPolyId("Test", 2024)));
		assertThat(result, hasProperty("percentAvailable", is(90f)));
		assertThat(result, hasProperty("forestInventoryZone", is("Z")));
		assertThat(result, hasProperty("biogeoclimaticZone", is("IDF")));
		assertThat(result, hasProperty("layers", hasEntry(is(LayerType.PRIMARY), anything())));
		var resultLayer = result.getLayers().get(LayerType.PRIMARY);

		assertThat(resultLayer, hasProperty("polygonIdentifier", isPolyId("Test", 2024)));
		assertThat(resultLayer, hasProperty("layerType", is(LayerType.PRIMARY)));
	}

}
