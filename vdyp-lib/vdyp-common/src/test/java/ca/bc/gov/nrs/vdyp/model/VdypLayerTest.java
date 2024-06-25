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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Optional;

import org.easymock.EasyMock;
import org.junit.jupiter.api.Test;

import ca.bc.gov.nrs.vdyp.common.Utils;

class VdypLayerTest {

	@Test
	void build() throws Exception {
		var result = VdypLayer.build(builder -> {
			builder.polygonIdentifier("Test", 2024);
			builder.layerType(LayerType.PRIMARY);

			builder.addSite(siteBuilder -> {
				siteBuilder.height(10f);
				siteBuilder.ageTotal(42f);
				siteBuilder.yearsToBreastHeight(2f);
				siteBuilder.siteGenus("PL");
				siteBuilder.siteCurveNumber(0);
			});

		});
		assertThat(result, hasProperty("polygonIdentifier", isPolyId("Test", 2024)));
		assertThat(result, hasProperty("layerType", is(LayerType.PRIMARY)));
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
			builder.polygonIdentifier("Test", 2024);
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

		assertThat(result, hasProperty("polygonIdentifier", isPolyId("Test", 2024)));
		assertThat(result, hasProperty("layerType", is(LayerType.PRIMARY)));
		assertThat(result, hasProperty("ageTotal", present(is(42f))));
		assertThat(result, hasProperty("yearsToBreastHeight", present(is(2f))));
		assertThat(result, hasProperty("height", present(is(10f))));
		assertThat(result, hasProperty("species", anEmptyMap()));

		assertThat(poly.getLayers(), hasEntry(LayerType.PRIMARY, result));
	}

	@Test
	void buildAddSpecies() throws Exception {
		VdypSpecies mock = EasyMock.mock(VdypSpecies.class);
		EasyMock.expect(mock.getLayerType()).andStubReturn(LayerType.PRIMARY);
		EasyMock.expect(mock.getGenus()).andStubReturn("B");
		EasyMock.replay(mock);
		var result = VdypLayer.build(builder -> {
			builder.polygonIdentifier("Test", 2024);
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

	@Test
	void buildAdapt() throws Exception {

		var control = EasyMock.createControl();

		BaseVdypLayer<?, ?> toCopy = control.createMock(BaseVdypLayer.class);

		EasyMock.expect(toCopy.getPolygonIdentifier()).andStubReturn(new PolygonIdentifier("Test", 2024));
		EasyMock.expect(toCopy.getLayerType()).andStubReturn(LayerType.PRIMARY);
		EasyMock.expect(toCopy.getInventoryTypeGroup()).andStubReturn(Optional.of(12));

		control.replay();

		var result = VdypLayer.build(builder -> {

			builder.adapt(toCopy);

		});
		assertThat(result, hasProperty("polygonIdentifier", isPolyId("Test", 2024)));
		assertThat(result, hasProperty("layerType", is(LayerType.PRIMARY)));
		assertThat(result, hasProperty("species", anEmptyMap()));
		assertThat(result, hasProperty("inventoryTypeGroup", present(is(12))));

		control.verify();

	}

	@Test
	void buildAdaptSites() throws Exception {

		var control = EasyMock.createControl();

		BaseVdypLayer<BaseVdypSpecies, BaseVdypSite> toCopy = control.createMock(BaseVdypLayer.class);
		BaseVdypSite siteToCopy = control.createMock(BaseVdypSite.class);

		EasyMock.expect(toCopy.getPolygonIdentifier()).andStubReturn(new PolygonIdentifier("Test", 2024));
		EasyMock.expect(siteToCopy.getPolygonIdentifier()).andStubReturn(new PolygonIdentifier("Test", 2024));
		EasyMock.expect(toCopy.getLayerType()).andStubReturn(LayerType.PRIMARY);
		EasyMock.expect(siteToCopy.getLayerType()).andStubReturn(LayerType.PRIMARY);
		EasyMock.expect(toCopy.getInventoryTypeGroup()).andStubReturn(Optional.of(12));
		EasyMock.expect(toCopy.getSites())
				.andStubReturn(new LinkedHashMap<>(Collections.singletonMap("B", siteToCopy)));
		EasyMock.expect(siteToCopy.getSiteGenus()).andStubReturn("B");
		EasyMock.expect(siteToCopy.getAgeTotal()).andStubReturn(Optional.of(30f));
		EasyMock.expect(siteToCopy.getYearsToBreastHeight()).andStubReturn(Optional.of(4f));
		EasyMock.expect(siteToCopy.getHeight()).andStubReturn(Optional.of(20f));
		EasyMock.expect(siteToCopy.getSiteIndex()).andStubReturn(Optional.of(19.7f));
		EasyMock.expect(siteToCopy.getSiteCurveNumber()).andStubReturn(Optional.of(13));

		control.replay();

		var result = VdypLayer.build(builder -> {

			builder.adapt(toCopy);
			builder.adaptSites(toCopy, (siteBuilder, site) -> {
				// Do Nothing
			});

		});
		assertThat(result, hasProperty("sites", hasEntry(is("B"), hasProperty("siteGenus", is("B")))));
		var resultSite = result.getSite().get();
		assertThat(resultSite, hasProperty("polygonIdentifier", isPolyId("Test", 2024)));
		assertThat(resultSite, hasProperty("layerType", is(LayerType.PRIMARY)));
		assertThat(resultSite, hasProperty("siteGenus", is("B")));
		assertThat(resultSite, hasProperty("ageTotal", present(is(30f))));
		assertThat(resultSite, hasProperty("yearsToBreastHeight", present(is(4f))));
		assertThat(resultSite, hasProperty("height", present(is(20f))));
		assertThat(resultSite, hasProperty("siteIndex", present(is(19.7f))));
		assertThat(resultSite, hasProperty("siteCurveNumber", present(is(13))));

		control.verify();

	}

	@Test
	void buildCopySites() throws Exception {

		var toCopy = VdypLayer.build(builder -> {
			builder.polygonIdentifier("Test", 2024);
			builder.layerType(LayerType.PRIMARY);

			builder.addSite(siteBuilder -> {
				siteBuilder.height(10f);
				siteBuilder.ageTotal(42f);
				siteBuilder.yearsToBreastHeight(2f);
				siteBuilder.siteGenus("PL");
				siteBuilder.siteIndex(19.7f);
				siteBuilder.siteCurveNumber(13);
			});

		});

		var result = VdypLayer.build(builder -> {

			builder.adapt(toCopy);
			builder.adaptSites(toCopy, (siteBuilder, site) -> {
				// Do Nothing
			});

		});
		assertThat(result, hasProperty("polygonIdentifier", isPolyId("Test", 2024)));
		assertThat(result, hasProperty("layerType", is(LayerType.PRIMARY)));
		assertThat(result, hasProperty("ageTotal", present(is(42f))));
		assertThat(result, hasProperty("yearsToBreastHeight", present(is(2f))));
		assertThat(result, hasProperty("height", present(is(10f))));
		assertThat(result, hasProperty("species", anEmptyMap()));

		assertThat(result, hasProperty("sites", hasEntry(is("PL"), hasProperty("siteGenus", is("PL")))));
		var resultSite = result.getSite().get();
		assertThat(resultSite, hasProperty("polygonIdentifier", isPolyId("Test", 2024)));
		assertThat(resultSite, hasProperty("layerType", is(LayerType.PRIMARY)));
		assertThat(resultSite, hasProperty("siteGenus", is("PL")));
		assertThat(resultSite, hasProperty("ageTotal", present(is(42f))));
		assertThat(resultSite, hasProperty("yearsToBreastHeight", present(is(2f))));
		assertThat(resultSite, hasProperty("height", present(is(10f))));
		assertThat(resultSite, hasProperty("siteIndex", present(is(19.7f))));
		assertThat(resultSite, hasProperty("siteCurveNumber", present(is(13))));

	}

	@Test
	void buildAdaptSpecies() throws Exception {

		var control = EasyMock.createControl();

		BaseVdypLayer<BaseVdypSpecies, BaseVdypSite> toCopy = control.createMock(BaseVdypLayer.class);
		BaseVdypSpecies speciesToCopy = control.createMock(BaseVdypSpecies.class);

		EasyMock.expect(toCopy.getPolygonIdentifier()).andStubReturn(new PolygonIdentifier("Test", 2024));
		EasyMock.expect(speciesToCopy.getPolygonIdentifier()).andStubReturn(new PolygonIdentifier("Test", 2024));
		EasyMock.expect(toCopy.getLayerType()).andStubReturn(LayerType.PRIMARY);
		EasyMock.expect(speciesToCopy.getLayerType()).andStubReturn(LayerType.PRIMARY);
		EasyMock.expect(toCopy.getInventoryTypeGroup()).andStubReturn(Optional.of(12));
		EasyMock.expect(toCopy.getSpecies())
				.andStubReturn(new LinkedHashMap<>(Collections.singletonMap("B", speciesToCopy)));
		EasyMock.expect(speciesToCopy.getGenus()).andStubReturn("B");
		EasyMock.expect(speciesToCopy.getPercentGenus()).andStubReturn(100f);
		EasyMock.expect(speciesToCopy.getFractionGenus()).andStubReturn(1f);
		EasyMock.expect(speciesToCopy.getSpeciesPercent()).andStubReturn(Utils.constMap(map -> {
			map.put("BL", 75f);
			map.put("BX", 25f);
		}));

		control.replay();

		var result = VdypLayer.build(builder -> {

			builder.adapt(toCopy);
			builder.adaptSpecies(toCopy, (siteBuilder, site) -> {
				siteBuilder.volumeGroup(1);
				siteBuilder.decayGroup(2);
				siteBuilder.breakageGroup(3);
			});

		});
		assertThat(result, hasProperty("species", hasEntry(is("B"), hasProperty("genus", is("B")))));
		var resultSpecies = result.getSpecies().get("B");
		assertThat(resultSpecies, hasProperty("polygonIdentifier", isPolyId("Test", 2024)));
		assertThat(resultSpecies, hasProperty("layerType", is(LayerType.PRIMARY)));
		assertThat(resultSpecies, hasProperty("genus", is("B")));
		assertThat(resultSpecies, hasProperty("percentGenus", is(100f)));
		assertThat(resultSpecies, hasProperty("fractionGenus", is(1f)));
		assertThat(
				resultSpecies,
				hasProperty("speciesPercent", allOf(hasEntry(is("BL"), is(75f)), hasEntry(is("BX"), is(25f))))
		);

		control.verify();

	}

	@Test
	void buildCopySpecies() throws Exception {

		var toCopy = VdypLayer.build(builder -> {
			builder.polygonIdentifier("Test", 2024);
			builder.layerType(LayerType.PRIMARY);

			builder.addSpecies(speciesBuilder -> {
				speciesBuilder.genus("B");
				speciesBuilder.percentGenus(100f);
				speciesBuilder.fractionGenus(1f);
				speciesBuilder.volumeGroup(1);
				speciesBuilder.decayGroup(2);
				speciesBuilder.breakageGroup(3);
				speciesBuilder.addSpecies("BL", 75f);
				speciesBuilder.addSpecies("BX", 25f);
			});

		});

		var result = VdypLayer.build(builder -> {

			builder.copy(toCopy);
			builder.copySpecies(toCopy, (speciesBuilder, species) -> {
				// Do Nothing
			});

		});

		assertThat(result, hasProperty("species", hasEntry(is("B"), hasProperty("genus", is("B")))));
		var resultSpecies = result.getSpecies().get("B");
		assertThat(resultSpecies, hasProperty("polygonIdentifier", isPolyId("Test", 2024)));
		assertThat(resultSpecies, hasProperty("layerType", is(LayerType.PRIMARY)));
		assertThat(resultSpecies, hasProperty("genus", is("B")));
		assertThat(resultSpecies, hasProperty("percentGenus", is(100f)));
		assertThat(resultSpecies, hasProperty("fractionGenus", is(1f)));
		assertThat(resultSpecies, hasProperty("volumeGroup", is(1)));
		assertThat(resultSpecies, hasProperty("decayGroup", is(2)));
		assertThat(resultSpecies, hasProperty("breakageGroup", is(3)));
		assertThat(
				resultSpecies,
				hasProperty("speciesPercent", allOf(hasEntry(is("BL"), is(75f)), hasEntry(is("BX"), is(25f))))
		);

	}

}
