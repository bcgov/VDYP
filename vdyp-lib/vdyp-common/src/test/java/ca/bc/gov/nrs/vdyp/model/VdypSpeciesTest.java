package ca.bc.gov.nrs.vdyp.model;

import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.isPolyId;
import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.present;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class VdypSpeciesTest {

	@Test
	void build() throws Exception {
		var result = VdypSpecies.build(builder -> {
			builder.polygonIdentifier("Test", 2024);
			builder.layerType(LayerType.PRIMARY);
			builder.genus("B");
			builder.percentGenus(50f);
			builder.volumeGroup(1);
			builder.decayGroup(2);
			builder.breakageGroup(3);
		});
		assertThat(result, hasProperty("polygonIdentifier", isPolyId("Test", 2024)));
		assertThat(result, hasProperty("layerType", is(LayerType.PRIMARY)));
		assertThat(result, hasProperty("genus", is("B")));
		assertThat(result, hasProperty("percentGenus", is(50f)));
		assertThat(result, hasProperty("volumeGroup", is(1)));
		assertThat(result, hasProperty("decayGroup", is(2)));
		assertThat(result, hasProperty("breakageGroup", is(3)));
		assertThat(result, hasProperty("speciesPercent", anEmptyMap()));
	}

	@Test
	void buildNoProperties() throws Exception {
		var ex = assertThrows(IllegalStateException.class, () -> VdypSpecies.build(builder -> {
		}));
		assertThat(
				ex,
				hasProperty(
						"message",
						allOf(
								containsString("polygonIdentifier"), containsString("layer"), containsString("genus"),
								containsString("percentGenus"), containsString("volumeGroup"),
								containsString("decayGroup")
						)
				)
		);
	}

	@Test
	void buildForLayer() throws Exception {

		var layer = VdypLayer.build(builder -> {
			builder.polygonIdentifier("Test", 2024);
			builder.layerType(LayerType.PRIMARY);
		});

		var result = VdypSpecies.build(layer, builder -> {
			builder.polygonIdentifier("Test", 2024);
			builder.genus("B");
			builder.percentGenus(50f);
			builder.volumeGroup(1);
			builder.decayGroup(2);
			builder.breakageGroup(3);
			builder.addSite(siteBuilder -> {
				siteBuilder.siteCurveNumber(0);
				siteBuilder.ageTotal(42f);
				siteBuilder.yearsToBreastHeight(2f);
				siteBuilder.height(10f);
			});
		});

		assertThat(result, hasProperty("polygonIdentifier", isPolyId("Test", 2024)));
		assertThat(result, hasProperty("layerType", is(LayerType.PRIMARY)));
		assertThat(result, hasProperty("genus", is("B")));
		assertThat(result, hasProperty("percentGenus", is(50f)));
		assertThat(result, hasProperty("volumeGroup", is(1)));
		assertThat(result, hasProperty("decayGroup", is(2)));
		assertThat(result, hasProperty("breakageGroup", is(3)));
		assertThat(result, hasProperty("speciesPercent", anEmptyMap()));

		assertThat(layer.getSpecies(), hasEntry("B", result));
	}

	@Test
	void buildAddSpeciesPercent() throws Exception {
		var result = VdypSpecies.build(builder -> {
			builder.polygonIdentifier("Test", 2024);
			builder.layerType(LayerType.PRIMARY);
			builder.genus("B");
			builder.percentGenus(50f);
			builder.volumeGroup(1);
			builder.decayGroup(2);
			builder.breakageGroup(3);
			builder.addSpecies("B", 100f);
		});
		assertThat(result, hasProperty("speciesPercent", hasEntry("B", 100f)));
	}

	@Test
	void adaptSite() throws Exception {
		var toCopy = VdypSpecies.build(builder -> {
			builder.polygonIdentifier("Test", 2024);
			builder.layerType(LayerType.PRIMARY);
			builder.genus("B");
			builder.percentGenus(50f);
			builder.volumeGroup(1);
			builder.decayGroup(2);
			builder.breakageGroup(3);
			builder.addSpecies("B", 100f);
			builder.addSite(ib -> {
				ib.ageTotal(42);
				ib.yearsToBreastHeight(5);
				ib.siteCurveNumber(2);
				ib.siteIndex(5.5f);
			});
		});
		var result = VdypSpecies.build(builder -> {
			builder.adapt(toCopy);

			builder.volumeGroup(1);
			builder.decayGroup(2);
			builder.breakageGroup(3);

			builder.adaptSiteFrom(toCopy, (ib, siteToCopy) -> {
			});
		});

		var siteResult = result.getSite().get();

		assertThat(siteResult, hasProperty("polygonIdentifier", isPolyId("Test", 2024)));
		assertThat(siteResult, hasProperty("layerType", is(LayerType.PRIMARY)));
		assertThat(siteResult, hasProperty("siteGenus", is("B")));
		assertThat(siteResult, hasProperty("yearsToBreastHeight", present(is(5f))));
		assertThat(siteResult, hasProperty("ageTotal", present(is(42f))));
		assertThat(siteResult, hasProperty("siteCurveNumber", present(is(2))));
		assertThat(siteResult, hasProperty("siteIndex", present(is(5.5f))));
	}

}
