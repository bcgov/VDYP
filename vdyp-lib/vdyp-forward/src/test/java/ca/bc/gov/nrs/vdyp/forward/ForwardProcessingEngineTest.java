package ca.bc.gov.nrs.vdyp.forward;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.bc.gov.nrs.vdyp.application.ProcessingException;
import ca.bc.gov.nrs.vdyp.common.ControlKey;
import ca.bc.gov.nrs.vdyp.forward.model.VdypPolygonDescription;
import ca.bc.gov.nrs.vdyp.forward.test.VdypForwardTestUtils;
import ca.bc.gov.nrs.vdyp.io.parse.common.ResourceParseException;
import ca.bc.gov.nrs.vdyp.io.parse.streaming.StreamingParserFactory;

class ForwardProcessingEngineTest {

	private static final Logger logger = LoggerFactory.getLogger(ForwardProcessingEngineTest.class);

	@Test
	void test() throws IOException, ResourceParseException, ProcessingException {

		var parser = new ForwardControlParser();
		var controlMap = VdypForwardTestUtils.parse(parser, "VDYP.CTR");

		ForwardProcessingEngine fpe = new ForwardProcessingEngine(controlMap);

		assertThat(fpe.getBecLookup(), notNullValue());
		assertThat(fpe.getGenusDefinitionMap(), notNullValue());
		assertThat(fpe.getSiteCurveMap(), notNullValue());

		@SuppressWarnings("unchecked")
		var polygonDescriptionStreamFactory = (StreamingParserFactory<VdypPolygonDescription>) controlMap
				.get(ControlKey.FORWARD_INPUT_GROWTO.name());
		var polygonDescriptionStream = polygonDescriptionStreamFactory.get();

		var forwardDataStreamReader = new ForwardDataStreamReader(controlMap);

		// Fetch the next polygon to process.
		int nPolygonsProcessed = 0;
		while (polygonDescriptionStream.hasNext()) {

			var polygonDescription = polygonDescriptionStream.next();

			var polygon = forwardDataStreamReader.readNextPolygon(polygonDescription);

			fpe.processPolygon(polygon);

			nPolygonsProcessed += 1;
		}

		logger.info("{} polygons processed", nPolygonsProcessed);
	}
}
