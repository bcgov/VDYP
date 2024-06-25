package ca.bc.gov.nrs.vdyp.io.write;

import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ca.bc.gov.nrs.vdyp.common.ControlKey;
import ca.bc.gov.nrs.vdyp.common.Utils;
import ca.bc.gov.nrs.vdyp.model.LayerType;
import ca.bc.gov.nrs.vdyp.model.PolygonMode;
import ca.bc.gov.nrs.vdyp.model.VdypLayer;
import ca.bc.gov.nrs.vdyp.model.VdypPolygon;
import ca.bc.gov.nrs.vdyp.model.VdypSpecies;
import ca.bc.gov.nrs.vdyp.test.MockFileResolver;
import ca.bc.gov.nrs.vdyp.test.TestUtils;
import ca.bc.gov.nrs.vdyp.test.TestUtils.MockOutputStream;
import ca.bc.gov.nrs.vdyp.test.VdypMatchers;

class VriAdjustInputWriterTest {

	MockOutputStream polyStream;
	MockOutputStream specStream;
	MockOutputStream utilStream;

	MockFileResolver fileResolver;

	Map<String, Object> controlMap;

	@BeforeEach
	void initStreams() {
		controlMap = new HashMap<String, Object>();
		TestUtils.populateControlMapGenusReal(controlMap);

		polyStream = new TestUtils.MockOutputStream("polygons");
		specStream = new TestUtils.MockOutputStream("species");
		utilStream = new TestUtils.MockOutputStream("utilization");

		controlMap.put(ControlKey.VRI_OUTPUT_VDYP_POLYGON.name(), "testPolygonFile");
		controlMap.put(ControlKey.VRI_OUTPUT_VDYP_LAYER_BY_SPECIES.name(), "testSpeciesFile");
		controlMap.put(ControlKey.VRI_OUTPUT_VDYP_LAYER_BY_SP0_BY_UTIL.name(), "testUtilizationFile");

		fileResolver = new MockFileResolver("TEST");
		fileResolver.addStream("testPolygonFile", polyStream);
		fileResolver.addStream("testSpeciesFile", specStream);
		fileResolver.addStream("testUtilizationFile", utilStream);

	}

	@Test
	void testClosesGivenStreams() throws IOException {

		var unit = new VriAdjustInputWriter(polyStream, specStream, utilStream, controlMap);

		unit.close();

		polyStream.assertClosed();
		specStream.assertClosed();
		utilStream.assertClosed();

		polyStream.assertContent(emptyString());
		specStream.assertContent(emptyString());
		utilStream.assertContent(emptyString());
	}

	@Test
	void testClosesOpenedStreams() throws IOException {

		var unit = new VriAdjustInputWriter(controlMap, fileResolver);

		unit.close();

		polyStream.assertClosed();
		specStream.assertClosed();
		utilStream.assertClosed();

		polyStream.assertContent(emptyString());
		specStream.assertContent(emptyString());
		utilStream.assertContent(emptyString());
	}

	@Test
	void testWritePolygon() throws IOException {
		try (var unit = new VriAdjustInputWriter(controlMap, fileResolver);) {

			VdypPolygon polygon = VdypPolygon.build(builder -> {

				builder.polygonIdentifier("082E004    615       1988");
				builder.percentAvailable(90f);
				builder.biogeoclimaticZone("IDF");
				builder.forestInventoryZone("D");
				builder.mode(PolygonMode.START);
			});
			var layer = VdypLayer.build(polygon, builder -> {
				builder.polygonIdentifier("082E004    615       1988");
				builder.layerType(LayerType.PRIMARY);

				builder.addSite(siteBuilder -> {
					siteBuilder.height(15f);
					siteBuilder.siteIndex(14.7f);
					siteBuilder.ageTotal(60f);
					siteBuilder.yearsToBreastHeight(8.5f);
					siteBuilder.siteGenus("PL");
					siteBuilder.siteCurveNumber(0);
				});
			});

			// FIXME Add to builder
			layer.setEmpericalRelationshipParameterIndex(Optional.of(119));
			layer.setInventoryTypeGroup(Optional.of(28));

			unit.writePolygon(polygon);
		}

		polyStream.assertContent(is("082E004    615       1988 IDF  D    90 28119  1\n"));
		specStream.assertContent(emptyString());
		utilStream.assertContent(emptyString());
	}

	@Test
	void testWriteSpecies() throws IOException {
		try (var unit = new VriAdjustInputWriter(controlMap, fileResolver);) {

			var layer = VdypLayer.build(builder -> {
				builder.polygonIdentifier("082E004    615       1988");
				builder.layerType(LayerType.PRIMARY);

				builder.addSite(siteBuilder -> {
					siteBuilder.height(15f);
					siteBuilder.siteIndex(14.7f);
					siteBuilder.ageTotal(60f);
					siteBuilder.yearsToBreastHeight(8.5f);
					siteBuilder.siteGenus("PL");
					siteBuilder.siteCurveNumber(0);
				});
			});

			var species = VdypSpecies.build(layer, builder -> {
				builder.genus("PL");
				builder.addSpecies("PL", 100f);

				builder.percentGenus(100f);
				builder.volumeGroup(0);
				builder.decayGroup(0);
				builder.breakageGroup(0);
			});

			unit.writeSpecies(layer, species);
		}
		specStream.assertContent(
				is(
						"082E004    615       1988 P 12 PL PL 100.0     0.0     0.0     0.0 14.70 15.00  60.0  51.5   8.5 1  0\n"
				)
		);
		polyStream.assertContent(emptyString());
		utilStream.assertContent(emptyString());
	}

	@Test
	void testWriteUtilizationForLayer() throws IOException {
		try (var unit = new VriAdjustInputWriter(controlMap, fileResolver);) {

			var layer = VdypLayer.build(builder -> {
				builder.polygonIdentifier("082E004    615       1988");
				builder.layerType(LayerType.PRIMARY);

				builder.addSite(siteBuilder -> {
					siteBuilder.height(15f);
					siteBuilder.siteIndex(14.7f);
					siteBuilder.ageTotal(60f);
					siteBuilder.yearsToBreastHeight(8.5f);
					siteBuilder.siteGenus("PL");
					siteBuilder.siteCurveNumber(0);
				});
			});

			@SuppressWarnings("unused")
			var species = VdypSpecies.build(layer, builder -> {
				builder.genus("PL");
				builder.addSpecies("PL", 100f);

				builder.percentGenus(100f);
				builder.volumeGroup(0);
				builder.decayGroup(0);
				builder.breakageGroup(0);
			});

			layer.setBaseAreaByUtilization(
					Utils.utilizationVector(0.02865f, 19.97867f, 6.79731f, 8.54690f, 3.63577f, 0.99869f)
			);
			layer.setTreesPerHectareByUtilization(
					Utils.utilizationVector(9.29f, 1485.82f, 834.25f, 509.09f, 123.56f, 18.92f)
			);
			layer.setLoreyHeightByUtilization(Utils.heightVector(7.8377f, 13.0660f));

			layer.setWholeStemVolumeByUtilization(
					Utils.utilizationVector(0.1077f, 117.9938f, 33.3680f, 52.4308f, 25.2296f, 6.9654f)
			);
			layer.setCloseUtilizationVolumeByUtilization(
					Utils.utilizationVector(0f, 67.7539f, 2.4174f, 36.8751f, 22.0156f, 6.4459f)
			);
			layer.setCloseUtilizationVolumeNetOfDecayByUtilization(
					Utils.utilizationVector(0f, 67.0665f, 2.3990f, 36.5664f, 21.7930f, 6.3080f)
			);
			layer.setCloseUtilizationVolumeNetOfDecayAndWasteByUtilization(
					Utils.utilizationVector(0f, 66.8413f, 2.3951f, 36.4803f, 21.7218f, 6.2442f)
			);
			layer.setCloseUtilizationVolumeNetOfDecayWasteAndBreakageByUtilization(
					Utils.utilizationVector(0f, 65.4214f, 2.3464f, 35.7128f, 21.2592f, 6.1030f)
			);

			layer.setQuadraticMeanDiameterByUtilization(Utils.utilizationVector(4f, 4f, 4f, 4f, 4f, 4f)); // Should be
																											// ignored
																											// and
																											// computed
																											// from
																											// BA and
																											// TPH

			unit.writeUtilization(layer, layer);
		}
		utilStream.assertContent(
				VdypMatchers.hasLines(
						"082E004    615       1988 P  0    -1  0.02865     9.29   7.8377   0.1077   0.0000   0.0000   0.0000   0.0000   6.3",
						"082E004    615       1988 P  0     0 19.97867  1485.82  13.0660 117.9938  67.7539  67.0665  66.8413  65.4214  13.1",
						"082E004    615       1988 P  0     1  6.79731   834.25  -9.0000  33.3680   2.4174   2.3990   2.3951   2.3464  10.2",
						"082E004    615       1988 P  0     2  8.54690   509.09  -9.0000  52.4308  36.8751  36.5664  36.4803  35.7128  14.6",
						"082E004    615       1988 P  0     3  3.63577   123.56  -9.0000  25.2296  22.0156  21.7930  21.7218  21.2592  19.4",
						"082E004    615       1988 P  0     4  0.99869    18.92  -9.0000   6.9654   6.4459   6.3080   6.2442   6.1030  25.9"
				)
		);
		polyStream.assertContent(emptyString());
		specStream.assertContent(emptyString());
	}

	@Test
	void testWriteUtilizationZeroBaseArea() throws IOException {
		try (var unit = new VriAdjustInputWriter(controlMap, fileResolver);) {

			var layer = VdypLayer.build(builder -> {
				builder.polygonIdentifier("082E004    615       1988");
				builder.layerType(LayerType.PRIMARY);

				builder.addSite(siteBuilder -> {
					siteBuilder.height(15f);
					siteBuilder.siteIndex(14.7f);
					siteBuilder.ageTotal(60f);
					siteBuilder.yearsToBreastHeight(8.5f);
					siteBuilder.siteGenus("PL");
					siteBuilder.siteCurveNumber(0);
				});
			});

			var species = VdypSpecies.build(layer, builder -> {
				builder.genus("PL");
				builder.addSpecies("PL", 100f);

				builder.percentGenus(100f);
				builder.volumeGroup(0);
				builder.decayGroup(0);
				builder.breakageGroup(0);
			});

			species.setBaseAreaByUtilization(
					Utils.utilizationVector(0.02865f, 19.97867f, 6.79731f, 8.54690f, 3.63577f, 0f)
			);
			species.setTreesPerHectareByUtilization(
					Utils.utilizationVector(9.29f, 1485.82f, 834.25f, 509.09f, 123.56f, 18.92f)
			);
			species.setLoreyHeightByUtilization(Utils.heightVector(7.8377f, 13.0660f));

			species.setWholeStemVolumeByUtilization(
					Utils.utilizationVector(0.1077f, 117.9938f, 33.3680f, 52.4308f, 25.2296f, 6.9654f)
			);
			species.setCloseUtilizationVolumeByUtilization(
					Utils.utilizationVector(0f, 67.7539f, 2.4174f, 36.8751f, 22.0156f, 6.4459f)
			);
			species.setCloseUtilizationVolumeNetOfDecayByUtilization(
					Utils.utilizationVector(0f, 67.0665f, 2.3990f, 36.5664f, 21.7930f, 6.3080f)
			);
			species.setCloseUtilizationVolumeNetOfDecayAndWasteByUtilization(
					Utils.utilizationVector(0f, 66.8413f, 2.3951f, 36.4803f, 21.7218f, 6.2442f)
			);
			species.setCloseUtilizationVolumeNetOfDecayWasteAndBreakageByUtilization(
					Utils.utilizationVector(0f, 65.4214f, 2.3464f, 35.7128f, 21.2592f, 6.1030f)
			);

			species.setQuadraticMeanDiameterByUtilization(Utils.utilizationVector(4f, 4f, 4f, 4f, 4f, 4f)); // Should be
																											// ignored
																											// and
																											// computed
																											// from
																											// BA and
																											// TPH

			unit.writeUtilization(layer, species);
		}
		utilStream.assertContent(
				VdypMatchers.hasLines(
						"082E004    615       1988 P 12 PL -1  0.02865     9.29   7.8377   0.1077   0.0000   0.0000   0.0000   0.0000   6.3",
						"082E004    615       1988 P 12 PL  0 19.97867  1485.82  13.0660 117.9938  67.7539  67.0665  66.8413  65.4214  13.1",
						"082E004    615       1988 P 12 PL  1  6.79731   834.25  -9.0000  33.3680   2.4174   2.3990   2.3951   2.3464  10.2",
						"082E004    615       1988 P 12 PL  2  8.54690   509.09  -9.0000  52.4308  36.8751  36.5664  36.4803  35.7128  14.6",
						"082E004    615       1988 P 12 PL  3  3.63577   123.56  -9.0000  25.2296  22.0156  21.7930  21.7218  21.2592  19.4",
						"082E004    615       1988 P 12 PL  4  0.00000    18.92  -9.0000   6.9654   6.4459   6.3080   6.2442   6.1030  -9.0" // DQ
																																				// should
																																				// be
																																				// -9
				)
		);
		polyStream.assertContent(emptyString());
		specStream.assertContent(emptyString());
	}

	@Test
	void testWritePolygonWithChildren() throws IOException {
		try (var unit = new VriAdjustInputWriter(controlMap, fileResolver);) {

			VdypPolygon polygon = VdypPolygon.build(builder -> {

				builder.polygonIdentifier("082E004    615       1988");
				builder.percentAvailable(90f);
				builder.biogeoclimaticZone("IDF");
				builder.forestInventoryZone("D");
				builder.mode(PolygonMode.START);
			});

			var layer = VdypLayer.build(polygon, builder -> {
				builder.layerType(LayerType.PRIMARY);

				builder.addSite(siteBuilder -> {
					siteBuilder.height(15f);
					siteBuilder.siteIndex(14.7f);
					siteBuilder.ageTotal(60f);
					siteBuilder.yearsToBreastHeight(8.5f);
					siteBuilder.siteGenus("PL");
					siteBuilder.siteCurveNumber(0);
				});
			});

			var species = VdypSpecies.build(layer, builder -> {
				builder.genus("PL");
				builder.addSpecies("PL", 100f);

				builder.percentGenus(100f);
				builder.volumeGroup(0);
				builder.decayGroup(0);
				builder.breakageGroup(0);
			});

			// fixme add to builder
			layer.setEmpericalRelationshipParameterIndex(Optional.of(119));
			layer.setInventoryTypeGroup(Optional.of(28));

			layer.setBaseAreaByUtilization(
					Utils.utilizationVector(0.02865f, 19.97867f, 6.79731f, 8.54690f, 3.63577f, 0.99869f)
			);
			layer.setTreesPerHectareByUtilization(
					Utils.utilizationVector(9.29f, 1485.82f, 834.25f, 509.09f, 123.56f, 18.92f)
			);
			layer.setLoreyHeightByUtilization(Utils.heightVector(7.8377f, 13.0660f));

			layer.setWholeStemVolumeByUtilization(
					Utils.utilizationVector(0.1077f, 117.9938f, 33.3680f, 52.4308f, 25.2296f, 6.9654f)
			);
			layer.setCloseUtilizationVolumeByUtilization(
					Utils.utilizationVector(0f, 67.7539f, 2.4174f, 36.8751f, 22.0156f, 6.4459f)
			);
			layer.setCloseUtilizationVolumeNetOfDecayByUtilization(
					Utils.utilizationVector(0f, 67.0665f, 2.3990f, 36.5664f, 21.7930f, 6.3080f)
			);
			layer.setCloseUtilizationVolumeNetOfDecayAndWasteByUtilization(
					Utils.utilizationVector(0f, 66.8413f, 2.3951f, 36.4803f, 21.7218f, 6.2442f)
			);
			layer.setCloseUtilizationVolumeNetOfDecayWasteAndBreakageByUtilization(
					Utils.utilizationVector(0f, 65.4214f, 2.3464f, 35.7128f, 21.2592f, 6.1030f)
			);

			layer.setQuadraticMeanDiameterByUtilization(Utils.utilizationVector(4f, 4f, 4f, 4f, 4f, 4f)); // Should be
																											// ignored
																											// and
																											// computed
																											// from
																											// BA and
																											// TPH

			species.setBaseAreaByUtilization(
					Utils.utilizationVector(0.02865f, 19.97867f, 6.79731f, 8.54690f, 3.63577f, 0f)
			);
			species.setTreesPerHectareByUtilization(
					Utils.utilizationVector(9.29f, 1485.82f, 834.25f, 509.09f, 123.56f, 18.92f)
			);
			species.setLoreyHeightByUtilization(Utils.heightVector(7.8377f, 13.0660f));

			species.setWholeStemVolumeByUtilization(
					Utils.utilizationVector(0.1077f, 117.9938f, 33.3680f, 52.4308f, 25.2296f, 6.9654f)
			);
			species.setCloseUtilizationVolumeByUtilization(
					Utils.utilizationVector(0f, 67.7539f, 2.4174f, 36.8751f, 22.0156f, 6.4459f)
			);
			species.setCloseUtilizationVolumeNetOfDecayByUtilization(
					Utils.utilizationVector(0f, 67.0665f, 2.3990f, 36.5664f, 21.7930f, 6.3080f)
			);
			species.setCloseUtilizationVolumeNetOfDecayAndWasteByUtilization(
					Utils.utilizationVector(0f, 66.8413f, 2.3951f, 36.4803f, 21.7218f, 6.2442f)
			);
			species.setCloseUtilizationVolumeNetOfDecayWasteAndBreakageByUtilization(
					Utils.utilizationVector(0f, 65.4214f, 2.3464f, 35.7128f, 21.2592f, 6.1030f)
			);

			species.setQuadraticMeanDiameterByUtilization(Utils.utilizationVector(4f, 4f, 4f, 4f, 4f, 4f)); // Should be
																											// ignored
																											// and
																											// computed
																											// from
																											// BA and
																											// TPH

			unit.writePolygonWithSpeciesAndUtilization(polygon);
		}
		polyStream.assertContent(is("082E004    615       1988 IDF  D    90 28119  1\n"));
		utilStream.assertContent(
				VdypMatchers.hasLines(
						"082E004    615       1988 P  0    -1  0.02865     9.29   7.8377   0.1077   0.0000   0.0000   0.0000   0.0000   6.3",
						"082E004    615       1988 P  0     0 19.97867  1485.82  13.0660 117.9938  67.7539  67.0665  66.8413  65.4214  13.1",
						"082E004    615       1988 P  0     1  6.79731   834.25  -9.0000  33.3680   2.4174   2.3990   2.3951   2.3464  10.2",
						"082E004    615       1988 P  0     2  8.54690   509.09  -9.0000  52.4308  36.8751  36.5664  36.4803  35.7128  14.6",
						"082E004    615       1988 P  0     3  3.63577   123.56  -9.0000  25.2296  22.0156  21.7930  21.7218  21.2592  19.4",
						"082E004    615       1988 P  0     4  0.99869    18.92  -9.0000   6.9654   6.4459   6.3080   6.2442   6.1030  25.9",
						"082E004    615       1988 P 12 PL -1  0.02865     9.29   7.8377   0.1077   0.0000   0.0000   0.0000   0.0000   6.3",
						"082E004    615       1988 P 12 PL  0 19.97867  1485.82  13.0660 117.9938  67.7539  67.0665  66.8413  65.4214  13.1",
						"082E004    615       1988 P 12 PL  1  6.79731   834.25  -9.0000  33.3680   2.4174   2.3990   2.3951   2.3464  10.2",
						"082E004    615       1988 P 12 PL  2  8.54690   509.09  -9.0000  52.4308  36.8751  36.5664  36.4803  35.7128  14.6",
						"082E004    615       1988 P 12 PL  3  3.63577   123.56  -9.0000  25.2296  22.0156  21.7930  21.7218  21.2592  19.4",
						"082E004    615       1988 P 12 PL  4  0.00000    18.92  -9.0000   6.9654   6.4459   6.3080   6.2442   6.1030  -9.0",
						"082E004    615       1988  "
				)
		);
		specStream.assertContent(
				VdypMatchers.hasLines(
						"082E004    615       1988 P 12 PL PL 100.0     0.0     0.0     0.0 14.70 15.00  60.0  51.5   8.5 1  0",
						"082E004    615       1988  "
				)
		);
	}
}
