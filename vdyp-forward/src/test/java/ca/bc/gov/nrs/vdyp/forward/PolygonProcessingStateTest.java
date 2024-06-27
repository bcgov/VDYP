package ca.bc.gov.nrs.vdyp.forward;

import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.controlMapHasEntry;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.hamcrest.Matcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ca.bc.gov.nrs.vdyp.application.ProcessingException;
import ca.bc.gov.nrs.vdyp.common.ControlKey;
import ca.bc.gov.nrs.vdyp.forward.model.VdypLayerSpecies;
import ca.bc.gov.nrs.vdyp.forward.model.VdypPolygonDescription;
import ca.bc.gov.nrs.vdyp.forward.model.VdypPolygonLayer;
import ca.bc.gov.nrs.vdyp.forward.model.VdypSpeciesUtilization;
import ca.bc.gov.nrs.vdyp.forward.test.VdypForwardTestUtils;
import ca.bc.gov.nrs.vdyp.io.parse.common.ResourceParseException;
import ca.bc.gov.nrs.vdyp.io.parse.streaming.StreamingParser;
import ca.bc.gov.nrs.vdyp.io.parse.streaming.StreamingParserFactory;
import ca.bc.gov.nrs.vdyp.model.GenusDefinition;
import ca.bc.gov.nrs.vdyp.model.UtilizationClass;

class PolygonProcessingStateTest {

	private ForwardControlParser parser;
	private Map<String, Object> controlMap;

	private StreamingParser<VdypPolygonDescription> polygonDescriptionStream;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@BeforeEach
	void before() throws IOException, ResourceParseException {

		parser = new ForwardControlParser();
		controlMap = VdypForwardTestUtils.parse(parser, "VDYP.CTR");
		assertThat(
				controlMap,
				(Matcher) controlMapHasEntry(
						ControlKey.SP0_DEF, allOf(instanceOf(List.class), hasItem(instanceOf(GenusDefinition.class)))
				)
		);

		var polygonDescriptionStreamFactory = controlMap.get(ControlKey.FORWARD_INPUT_GROWTO.name());
		polygonDescriptionStream = ((StreamingParserFactory<VdypPolygonDescription>) polygonDescriptionStreamFactory)
				.get();
	}

	@Test
	void testConstruction() throws IOException, ResourceParseException, ProcessingException {

		assertThat(polygonDescriptionStream.hasNext(), is(true));
		var polygonDescription = polygonDescriptionStream.next();

		ForwardDataStreamReader reader = new ForwardDataStreamReader(controlMap);

		var polygon = reader.readNextPolygon(polygonDescription);

		VdypPolygonLayer pLayer = polygon.getPrimaryLayer();
		assertThat(pLayer, notNullValue());

		Bank pps = new Bank(pLayer, polygon.getBiogeoclimaticZone(), s -> true);

		int nSpecies = pLayer.getGenera().size();

		assertThat(pps, notNullValue());
		assertThat(pps.yearsAtBreastHeight.length, is(nSpecies + 1));
		assertThat(pps.ageTotals.length, is(nSpecies + 1));
		assertThat(pps.dominantHeights.length, is(nSpecies + 1));
		assertThat(pps.percentagesOfForestedLand.length, is(nSpecies + 1));
		assertThat(pps.siteIndices.length, is(nSpecies + 1));
		assertThat(pps.sp64Distributions.length, is(nSpecies + 1));
		assertThat(pps.speciesIndices.length, is(nSpecies + 1));
		assertThat(pps.speciesNames.length, is(nSpecies + 1));
		assertThat(pps.yearsToBreastHeight.length, is(nSpecies + 1));
		assertThat(pps.getNSpecies(), is(nSpecies));

		assertThat(pps.basalAreas.length, is(nSpecies + 1));
		for (int i = 0; i < nSpecies + 1; i++) {
			assertThat(pps.basalAreas[i].length, is(UtilizationClass.values().length));
		}
		assertThat(pps.closeUtilizationVolumes.length, is(nSpecies + 1));
		for (int i = 0; i < nSpecies + 1; i++) {
			assertThat(pps.closeUtilizationVolumes[i].length, is(UtilizationClass.values().length));
		}
		assertThat(pps.cuVolumesMinusDecay.length, is(nSpecies + 1));
		for (int i = 0; i < nSpecies + 1; i++) {
			assertThat(pps.cuVolumesMinusDecay[i].length, is(UtilizationClass.values().length));
		}
		assertThat(pps.cuVolumesMinusDecayAndWastage.length, is(nSpecies + 1));
		for (int i = 0; i < nSpecies + 1; i++) {
			assertThat(pps.cuVolumesMinusDecayAndWastage[i].length, is(UtilizationClass.values().length));
		}

		assertThat(pps.loreyHeights.length, is(nSpecies + 1));
		for (int i = 0; i < nSpecies + 1; i++) {
			assertThat(pps.loreyHeights[i].length, is(2));
		}
		assertThat(pps.quadMeanDiameters.length, is(nSpecies + 1));
		for (int i = 0; i < nSpecies + 1; i++) {
			assertThat(pps.quadMeanDiameters[i].length, is(UtilizationClass.values().length));
		}
		assertThat(pps.treesPerHectare.length, is(nSpecies + 1));
		for (int i = 0; i < nSpecies + 1; i++) {
			assertThat(pps.treesPerHectare[i].length, is(UtilizationClass.values().length));
		}
		assertThat(pps.wholeStemVolumes.length, is(nSpecies + 1));
		for (int i = 0; i < nSpecies + 1; i++) {
			assertThat(pps.wholeStemVolumes[i].length, is(UtilizationClass.values().length));
		}
	}

	@Test
	void testSetCopy() throws IOException, ResourceParseException, ProcessingException {

		assertThat(polygonDescriptionStream.hasNext(), is(true));
		var polygonDescription = polygonDescriptionStream.next();

		ForwardDataStreamReader reader = new ForwardDataStreamReader(controlMap);

		var polygon = reader.readNextPolygon(polygonDescription);

		VdypPolygonLayer pLayer = polygon.getPrimaryLayer();
		assertThat(pLayer, notNullValue());

		Bank pps = new Bank(pLayer, polygon.getBiogeoclimaticZone(), s -> true);

		verifyProcessingStateMatchesLayer(pps, pLayer);

		Bank ppsCopy = pps.copy();

		verifyProcessingStateMatchesLayer(ppsCopy, pLayer);
	}

	@Test
	void testRemoveSmallLayers() throws IOException, ResourceParseException, ProcessingException {

		assertThat(polygonDescriptionStream.hasNext(), is(true));
		var polygonDescription = polygonDescriptionStream.next();
		ForwardDataStreamReader reader = new ForwardDataStreamReader(controlMap);

		var polygon = reader.readNextPolygon(polygonDescription);

		VdypPolygonLayer pLayer = polygon.getPrimaryLayer();
		assertThat(pLayer, notNullValue());

		Bank bank1 = new Bank(
				pLayer, polygon.getBiogeoclimaticZone(),
				s -> s.getUtilizations().isPresent()
						? s.getUtilizations().get().get(UtilizationClass.ALL).getBasalArea() >= 0.5 : true
		);

		// the filter should have removed genus B (index 3) since it's ALL basal area is below 0.5
		assertThat(bank1.getNSpecies(), is(pLayer.getGenera().size() - 1));
		assertThat(bank1.speciesIndices, is(new int[] { 0, 4, 5, 8, 15 }));

		Bank bank2 = new Bank(
				pLayer, polygon.getBiogeoclimaticZone(),
				s -> s.getUtilizations().isPresent()
						? s.getUtilizations().get().get(UtilizationClass.ALL).getBasalArea() >= 100.0 : true
		);

		// the filter should have removed all genera.
		assertThat(bank2.getNSpecies(), is(0));
		assertThat(bank2.speciesIndices, is(new int[] { 0 }));
	}

	@Test
	void testCopyConstructor() throws IOException, ResourceParseException, ProcessingException {

		assertThat(polygonDescriptionStream.hasNext(), is(true));
		var polygonDescription = polygonDescriptionStream.next();
		ForwardDataStreamReader reader = new ForwardDataStreamReader(controlMap);

		var polygon = reader.readNextPolygon(polygonDescription);

		VdypPolygonLayer pLayer = polygon.getPrimaryLayer();
		assertThat(pLayer, notNullValue());

		Bank pps = new Bank(pLayer, polygon.getBiogeoclimaticZone(), s -> true);

		Bank ppsCopy = new Bank(pps);

		verifyProcessingStateMatchesLayer(ppsCopy, pLayer);
	}

	private void verifyProcessingStateMatchesLayer(Bank pps, VdypPolygonLayer layer) {

		List<Integer> sortedSpIndices = layer.getGenera().keySet().stream().sorted().toList();

		for (int i = 0; i < sortedSpIndices.size(); i++) {

			int arrayIndex = i + 1;

			VdypLayerSpecies genus = layer.getGenera().get(sortedSpIndices.get(i));
			verifyProcessingStateSpeciesMatchesSpecies(pps, arrayIndex, genus);

			if (genus.getUtilizations().isPresent()) {
				verifyProcessingStateSpeciesUtilizationsMatchesUtilizations(
						pps, arrayIndex, genus.getUtilizations().get()
				);
			}
		}

		if (layer.getDefaultUtilizationMap().isPresent()) {
			verifyProcessingStateSpeciesUtilizationsMatchesUtilizations(pps, 0, layer.getDefaultUtilizationMap().get());
		}
	}

	private void verifyProcessingStateSpeciesUtilizationsMatchesUtilizations(
			Bank pps, int spIndex, Map<UtilizationClass, VdypSpeciesUtilization> map
	) {

		for (UtilizationClass uc : UtilizationClass.values()) {
			VdypSpeciesUtilization u = map.get(uc);

			assertThat(pps.basalAreas[spIndex][uc.index + 1], is(u.getBasalArea()));
			assertThat(pps.closeUtilizationVolumes[spIndex][uc.index + 1], is(u.getCloseUtilizationVolume()));
			assertThat(pps.cuVolumesMinusDecay[spIndex][uc.index + 1], is(u.getCuVolumeMinusDecay()));
			assertThat(pps.cuVolumesMinusDecayAndWastage[spIndex][uc.index + 1], is(u.getCuVolumeMinusDecayWastage()));
			if (uc.index <= 0) {
				assertThat(pps.loreyHeights[spIndex][uc.index + 1], is(u.getLoreyHeight()));
			}
			assertThat(pps.quadMeanDiameters[spIndex][uc.index + 1], is(u.getQuadraticMeanDiameterAtBH()));
			assertThat(pps.treesPerHectare[spIndex][uc.index + 1], is(u.getLiveTreesPerHectare()));
			assertThat(pps.wholeStemVolumes[spIndex][uc.index + 1], is(u.getWholeStemVolume()));
		}
	}

	private void verifyProcessingStateSpeciesMatchesSpecies(Bank pps, int index, VdypLayerSpecies species) {
		assertThat(pps.yearsAtBreastHeight[index], is(species.getAgeAtBreastHeight()));
		assertThat(pps.ageTotals[index], is(species.getAgeTotal()));
		assertThat(pps.dominantHeights[index], is(species.getDominantHeight()));
		assertThat(pps.siteIndices[index], is(species.getSiteIndex()));
		assertThat(pps.sp64Distributions[index], is(species.getSpeciesDistributions()));
		assertThat(pps.speciesIndices[index], is(species.getGenusIndex()));
		assertThat(pps.speciesNames[index], is(species.getGenus()));
		assertThat(pps.yearsToBreastHeight[index], is(species.getYearsToBreastHeight()));
	}
}
