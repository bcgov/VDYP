package ca.bc.gov.nrs.vdyp.model;

import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.present;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.easymock.EasyMock;
import org.junit.jupiter.api.Test;

class VdypLayerTest {

	@Test
	void build() throws Exception {
		var result = VdypLayer.build(builder -> {
			builder.polygonIdentifier("Test");
			builder.layerType(LayerType.PRIMARY);

			builder.addSite(siteBuilder -> {
				siteBuilder.height(10f);
				siteBuilder.ageTotal(42f);
				siteBuilder.yearsToBreastHeight(2f);
				siteBuilder.siteGenus("PL");
				siteBuilder.siteCurveNumber(0);
			});

		});
		assertThat(result, hasProperty("polygonIdentifier", is("Test")));
		assertThat(result, hasProperty("layer", is(LayerType.PRIMARY)));
		assertThat(result, hasProperty("ageTotal", present(is(42f))));
		assertThat(result, hasProperty("yearsToBreastHeight", present(is(2f))));
		assertThat(result, hasProperty("height", present(is(10f))));
		assertThat(result, hasProperty("species", anEmptyMap()));
	}

	@Test
	void buildNoProperties() throws Exception {
		var ex = assertThrows(IllegalStateException.class, () -> VdypLayer.build(builder -> {
		}));
		assertThat(ex, hasProperty("message", allOf(containsString("polygonIdentifier"), containsString("layer"))));
	}

	@Test
	void buildForPolygon() throws Exception {

		var poly = VdypPolygon.build(builder -> {
			builder.polygonIdentifier("Test");
			builder.percentAvailable(50f);

			builder.forestInventoryZone("?");
			builder.biogeoclimaticZone("?");
		});

		var result = VdypLayer.build(poly, builder -> {
			builder.layerType(LayerType.PRIMARY);
			builder.addSite(siteBuilder -> {
				siteBuilder.height(10f);
				siteBuilder.ageTotal(42f);
				siteBuilder.yearsToBreastHeight(2f);
				siteBuilder.siteGenus("PL");
				siteBuilder.siteCurveNumber(0);
			});

		});

		assertThat(result, hasProperty("polygonIdentifier", is("Test")));
		assertThat(result, hasProperty("layer", is(LayerType.PRIMARY)));
		assertThat(result, hasProperty("ageTotal", present(is(42f))));
		assertThat(result, hasProperty("yearsToBreastHeight", present(is(2f))));
		assertThat(result, hasProperty("height", present(is(10f))));
		assertThat(result, hasProperty("species", anEmptyMap()));

		assertThat(poly.getLayers(), hasEntry(LayerType.PRIMARY, result));
	}

	@Test
	void buildAddSpecies() throws Exception {
		VdypSpecies mock = EasyMock.mock(VdypSpecies.class);
		EasyMock.expect(mock.getLayer()).andStubReturn(LayerType.PRIMARY);
		EasyMock.expect(mock.getGenus()).andStubReturn("B");
		EasyMock.replay(mock);
		var result = VdypLayer.build(builder -> {
			builder.polygonIdentifier("Test");
			builder.layerType(LayerType.PRIMARY);
			builder.addSite(siteBuilder -> {
				siteBuilder.height(10f);
				siteBuilder.ageTotal(42f);
				siteBuilder.yearsToBreastHeight(2f);
				siteBuilder.siteGenus("PL");
				siteBuilder.siteCurveNumber(0);
			});
			builder.addSpecies(specBuilder -> {
				specBuilder.genus("B");
				specBuilder.percentGenus(90f);
				specBuilder.volumeGroup(10);
				specBuilder.decayGroup(10);
				specBuilder.breakageGroup(10);
			});
		});
		assertThat(result, hasProperty("species", hasEntry(is("B"), hasProperty("genus", is("B")))));
	}

}
