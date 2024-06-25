package ca.bc.gov.nrs.vdyp.forward;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import ca.bc.gov.nrs.vdyp.application.ProcessingException;
import ca.bc.gov.nrs.vdyp.common.ControlKey;
import ca.bc.gov.nrs.vdyp.forward.model.VdypPolygon;
import ca.bc.gov.nrs.vdyp.forward.model.VdypPolygonDescription;
import ca.bc.gov.nrs.vdyp.io.parse.common.ResourceParseException;
import ca.bc.gov.nrs.vdyp.io.parse.streaming.StreamingParserFactory;
import ca.bc.gov.nrs.vdyp.model.LayerType;
import ca.bc.gov.nrs.vdyp.model.UtilizationClass;
import ca.bc.gov.nrs.vdyp.test.TestUtils;

@SuppressWarnings({ "unchecked" })
class VdypForwardReadPolygonTest {

	@Test
	void testReadPolygons() throws Exception {

		var parser = new ForwardControlParser();
		Map<String, Object> controlMap = parse(parser, "VDYP.CTR");

		try {
			var polygonDescriptionStreamFactory = controlMap.get(ControlKey.FORWARD_INPUT_GROWTO.name());
			var polygonDescriptionStream = ((StreamingParserFactory<VdypPolygonDescription>) polygonDescriptionStreamFactory)
					.get();

			ForwardDataStreamReader reader = new ForwardDataStreamReader(controlMap);

			// Fetch the next polygon to process.
			List<VdypPolygon> polygons = new ArrayList<>();

			while (polygonDescriptionStream.hasNext()) {

				var polygonDescription = polygonDescriptionStream.next();

				var polygon = reader.readNextPolygon(polygonDescription);

				polygons.add(polygon);
			}

			assertThat(polygons, Matchers.hasSize(10));

			var polygon = polygons.get(0);

			assertThat(polygon.getDescription().toString(), is("01002 S000001 00(1970)"));

			var primaryLayer = polygon.getPrimaryLayer();
			{
				assertThat(primaryLayer, hasProperty("layerType", is(LayerType.PRIMARY)));
				assertThat(primaryLayer, hasProperty("defaultUtilizationMap"));
				assertThat(primaryLayer.getParent(), is(polygon));

				if (primaryLayer.getDefaultUtilizationMap().isPresent()) {
					var utilizationMap = primaryLayer.getDefaultUtilizationMap().get();

					assertThat(utilizationMap.size(), greaterThan(0));
					assertThat(utilizationMap.size(), lessThanOrEqualTo(UtilizationClass.values().length));
					for (UtilizationClass uc : UtilizationClass.values()) {
						assertThat(utilizationMap.get(uc), hasProperty("genusIndex", is(0)));
					}
				}

				var genusMap = primaryLayer.getGenus();
				assertThat(genusMap.size(), is(5));

				var genus = genusMap.values().iterator().next();

				assertThat(genus, hasProperty("parent", is(primaryLayer)));
				assertThat(genus, hasProperty("utilizations"));

				var genusUtilizationMap = genus.getUtilizations();

				if (genusUtilizationMap.isPresent()) {
					for (var u : genusUtilizationMap.get().values()) {
						assertThat(u, hasProperty("genusIndex", is(genus.getGenusIndex())));
						assertThat(u, hasProperty("parent", is(genus)));
					}
				}
			}

			var optionalVeteranLayer = polygon.getVeteranLayer();
			if (optionalVeteranLayer.isPresent()) {

				var veteranLayer = optionalVeteranLayer.get();

				if (veteranLayer.getDefaultUtilizationMap().isPresent()) {
					var utilizationMap = veteranLayer.getDefaultUtilizationMap().get();

					assertThat(utilizationMap.size(), greaterThan(0));
					assertThat(utilizationMap.size(), lessThanOrEqualTo(UtilizationClass.values().length));
					for (UtilizationClass uc : UtilizationClass.values()) {
						assertThat(utilizationMap.get(uc), hasProperty("genusIndex", is(0)));
					}
				}

				var genusMap = veteranLayer.getGenus();
				assertThat(genusMap.size(), is(5));

				var genus = genusMap.values().iterator().next();

				assertThat(genus, hasProperty("parent", is(veteranLayer)));
				assertThat(genus, hasProperty("utilizations"));

				var genusUtilizationMap = genus.getUtilizations();
				if (genusUtilizationMap.isPresent()) {
					for (var u : genusUtilizationMap.get().values()) {
						assertThat(u, hasProperty("genusIndex", is(genus.getGenusIndex())));
						assertThat(u, hasProperty("parent", is(genus)));
					}
				}
			}
		} catch (ResourceParseException | IOException e) {
			throw new ProcessingException(e);
		}
	}

	static InputStream addToEnd(InputStream is, String... lines) {
		var appendix = new ByteArrayInputStream(String.join("\r\n", lines).getBytes(StandardCharsets.US_ASCII));
		var result = new SequenceInputStream(is, appendix);
		return result;
	}

	static Map<String, ?> parseWithAppendix(ForwardControlParser parser, String... lines)
			throws IOException, ResourceParseException {

		Class<?> klazz = TestUtils.class;
		try (InputStream baseIs = klazz.getResourceAsStream("VDYP.CTR"); InputStream is = addToEnd(baseIs, lines);) {
			return parser.parse(is, TestUtils.fileResolver(klazz), new HashMap<>());
		}
	}

	Map<String, Object> parse(ForwardControlParser parser, String resourceName)
			throws IOException, ResourceParseException {

		Class<?> klazz = TestUtils.class;
		try (var is = klazz.getResourceAsStream(resourceName)) {

			return parser.parse(is, TestUtils.fileResolver(klazz), new HashMap<>());
		}
	}
}
