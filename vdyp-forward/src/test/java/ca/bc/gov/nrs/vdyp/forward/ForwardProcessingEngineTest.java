package ca.bc.gov.nrs.vdyp.forward;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.jupiter.api.Test;

import ca.bc.gov.nrs.vdyp.application.ProcessingException;
import ca.bc.gov.nrs.vdyp.io.parse.common.ResourceParseException;

class ForwardProcessingEngineTest extends AbstractForwardProcessingEngineTest {

	@Test
	void test() throws IOException, ResourceParseException, ProcessingException {

		ForwardProcessingEngine fpe = new ForwardProcessingEngine(controlMap);

		assertThat(fpe.fps.getBecLookup(), notNullValue());
		assertThat(fpe.fps.getGenusDefinitionMap(), notNullValue());
		assertThat(fpe.fps.getSiteCurveMap(), notNullValue());

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
