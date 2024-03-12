package ca.bc.gov.nrs.vdyp.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.any;
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
			builder.polygonIdentifier("Test");
			builder.percentAvailable(90f);

			builder.forestInventoryZone("?");
			builder.biogeoclimaticZone("?");
		});
		assertThat(result, hasProperty("polygonIdentifier", is("Test")));
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
		EasyMock.expect(mock.getLayer()).andStubReturn(LayerType.PRIMARY);
		EasyMock.replay(mock);
		var result = VdypPolygon.build(builder -> {
			builder.polygonIdentifier("Test");
			builder.percentAvailable(90f);

			builder.forestInventoryZone("?");
			builder.biogeoclimaticZone("?");

			builder.addLayer(mock);
		});
		assertThat(result, hasProperty("polygonIdentifier", is("Test")));
		assertThat(result, hasProperty("percentAvailable", is(90f)));
		assertThat(result, hasProperty("layers", hasEntry(LayerType.PRIMARY, mock)));
	}

	@Test
	void buildBuildLayer() throws Exception {
		var result = VdypPolygon.build(builder -> {
			builder.polygonIdentifier("Test");
			builder.percentAvailable(90f);

			builder.forestInventoryZone("?");
			builder.biogeoclimaticZone("?");

			builder.buildLayer(layerBuilder -> {
				layerBuilder.layerType(LayerType.PRIMARY);
				layerBuilder.addSite(siteBuilder -> {
					siteBuilder.siteGenus("B");
					siteBuilder.siteCurveNumber(0);
					siteBuilder.ageTotal(10f);
					siteBuilder.yearsToBreastHeight(1f);
					siteBuilder.height(5f);
				});
			});
		});
		assertThat(result, hasProperty("polygonIdentifier", is("Test")));
		assertThat(result, hasProperty("percentAvailable", is(90f)));
		assertThat(result, hasProperty("layers", hasEntry(is(LayerType.PRIMARY), any(VdypLayer.class))));
	}

}
