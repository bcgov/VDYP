package ca.bc.gov.nrs.vdyp.forward;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.bc.gov.nrs.vdyp.application.ProcessingException;
import ca.bc.gov.nrs.vdyp.common.ControlKey;
import ca.bc.gov.nrs.vdyp.common_calculators.custom_exceptions.CurveErrorException;
import ca.bc.gov.nrs.vdyp.common_calculators.custom_exceptions.NoAnswerException;
import ca.bc.gov.nrs.vdyp.common_calculators.custom_exceptions.SpeciesErrorException;
import ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexEquation;
import ca.bc.gov.nrs.vdyp.forward.model.VdypEntity;
import ca.bc.gov.nrs.vdyp.forward.model.VdypPolygonDescription;
import ca.bc.gov.nrs.vdyp.forward.parsers.VdypPolygonDescriptionParser;
import ca.bc.gov.nrs.vdyp.forward.parsers.VdypPolygonParser;
import ca.bc.gov.nrs.vdyp.forward.parsers.VdypSpeciesParser;
import ca.bc.gov.nrs.vdyp.forward.parsers.VdypUtilizationParser;
import ca.bc.gov.nrs.vdyp.forward.test.VdypForwardTestUtils;
import ca.bc.gov.nrs.vdyp.io.parse.common.ResourceParseException;
import ca.bc.gov.nrs.vdyp.io.parse.streaming.StreamingParser;
import ca.bc.gov.nrs.vdyp.io.parse.streaming.StreamingParserFactory;
import ca.bc.gov.nrs.vdyp.model.CommonData;
import ca.bc.gov.nrs.vdyp.model.MatrixMap2Impl;
import ca.bc.gov.nrs.vdyp.model.Region;
import ca.bc.gov.nrs.vdyp.si32.site.SiteTool;
import ca.bc.gov.nrs.vdyp.test.TestUtils;

class GeneralForwardProcessingEngineTest {

	protected static final Logger logger = LoggerFactory.getLogger(GeneralForwardProcessingEngineTest.class);

	protected static ForwardControlParser parser;
	protected static Map<String, Object> controlMap;

	protected static StreamingParserFactory<VdypPolygonDescription> polygonDescriptionStreamFactory;
	protected static StreamingParser<VdypPolygonDescription> polygonDescriptionStream;

	protected static ForwardDataStreamReader forwardDataStreamReader;

	@SuppressWarnings("unchecked")
	@BeforeEach
	void beforeTest() throws IOException, ResourceParseException {
		parser = new ForwardControlParser();
		controlMap = VdypForwardTestUtils.parse(parser, "VDYP.CTR");

		polygonDescriptionStreamFactory = (StreamingParserFactory<VdypPolygonDescription>) controlMap
				.get(ControlKey.FORWARD_INPUT_GROWTO.name());
		polygonDescriptionStream = polygonDescriptionStreamFactory.get();

		forwardDataStreamReader = new ForwardDataStreamReader(controlMap);
	}

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

	@Test
	void testFindPrimarySpecies() throws IOException, ResourceParseException, ProcessingException {

		var polygonDescription = polygonDescriptionStream.next();
		var polygon = forwardDataStreamReader.readNextPolygon(polygonDescription);

		{
			ForwardProcessingState fps = new ForwardProcessingState(controlMap);
			fps.setPolygon(polygon);

			PolygonProcessingState pps = fps.getPolygonProcessingState();

			ForwardProcessingEngine.calculateCoverages(pps);
			ForwardProcessingEngine.determinePolygonRankings(pps, CommonData.PRIMARY_SPECIES_TO_COMBINE);

			assertThat(pps.getPrimarySpeciesIndex(), is(3));
			assertThat(pps.getSecondarySpeciesIndex(), is(4));
			assertThat(pps.getInventoryTypeGroup(), is(37));
		}
		{
			ForwardProcessingState fps = new ForwardProcessingState(controlMap);
			fps.setPolygon(polygon);

			PolygonProcessingState pps = fps.getPolygonProcessingState();

			var speciesToCombine = Arrays.asList(Arrays.asList(pps.wallet.speciesNames[3], pps.wallet.speciesNames[4]));

			ForwardProcessingEngine.calculateCoverages(pps);
			ForwardProcessingEngine.determinePolygonRankings(pps, speciesToCombine);

			// The test-specific speciesToCombine will combine 3 & 4 into 3 (leaving 4 at 0.0), promoting 2 to
			// secondary.
			assertThat(pps.getPrimarySpeciesIndex(), is(3));
			assertThat(pps.getSecondarySpeciesIndex(), is(2));
			assertThat(pps.getInventoryTypeGroup(), is(37));
		}
	}

	@Test
	void testCombinePercentages() {

		String[] speciesNames = new String[] { "AC", "B", "C", "D", "E", "F", "PW", "H", "PY", "L", "PA", "AT", "S",
				"MB", "Y", "PL" };
		float[] percentages = new float[] { 1.1f, 2.2f, 3.3f, 4.4f, 5.5f, 6.6f, 7.7f, 8.8f, 9.9f, 9.3f, 8.4f, 7.1f,
				6.3f, 8.4f, 9.4f, 1.6f };

		List<String> combineGroup;
		float[] testPercentages;

		combineGroup = List.of("C", "Y");
		testPercentages = Arrays.copyOf(percentages, percentages.length);

		ForwardProcessingEngine.combinePercentages(speciesNames, combineGroup, testPercentages);

		assertThat(testPercentages[2], is(0f));
		assertThat(testPercentages[14], is(12.7f));

		combineGroup = List.of("D", "PL");
		testPercentages = Arrays.copyOf(percentages, percentages.length);

		ForwardProcessingEngine.combinePercentages(speciesNames, combineGroup, testPercentages);

		assertThat(testPercentages[3], is(6.0f));
		assertThat(testPercentages[15], is(0.0f));
	}

	@Test
	void testCombinePercentagesOneGenusNotInCombinationList() {

		String[] speciesNames = new String[] { "AC", "C", "D", "E", "F", "PW", "H", "PY", "L", "PA", "AT", "S", "MB",
				"Y", "PL" };
		float[] percentages = new float[] { 1.1f, 3.3f, 4.4f, 5.5f, 6.6f, 7.7f, 8.8f, 9.9f, 9.3f, 8.4f, 7.1f, 6.3f,
				8.4f, 9.4f, 1.6f };

		List<String> combineGroup = List.of("B", "Y");
		float[] testPercentages = Arrays.copyOf(percentages, percentages.length);

		ForwardProcessingEngine.combinePercentages(speciesNames, combineGroup, testPercentages);

		assertThat(testPercentages[13], is(9.4f));
	}

	@Test
	void testCombinePercentagesBothGeneraNotInCombinationList() {

		String[] speciesNames = new String[] { "AC", "D", "E", "F", "PW", "H", "PY", "L", "PA", "AT", "S", "MB", "Y",
				"PL" };
		float[] percentages = new float[] { 1.1f, 4.4f, 5.5f, 6.6f, 7.7f, 8.8f, 9.9f, 9.3f, 8.4f, 7.1f, 6.3f, 8.4f,
				9.4f, 1.6f };

		List<String> combineGroup = List.of("B", "C");
		float[] testPercentages = Arrays.copyOf(percentages, percentages.length);

		ForwardProcessingEngine.combinePercentages(speciesNames, combineGroup, testPercentages);

		assertThat(testPercentages, is(percentages));
	}

	@Test
	void testCombinePercentagesBadCombinationList() {

		String[] speciesNames = new String[] { "AC", "D", "E", "F", "PW", "H", "PY", "L", "PA", "AT", "S", "MB", "Y",
				"PL" };
		float[] percentages = new float[] { 1.1f, 4.4f, 5.5f, 6.6f, 7.7f, 8.8f, 9.9f, 9.3f, 8.4f, 7.1f, 6.3f, 8.4f,
				9.4f, 1.6f };

		List<String> combineGroup = List.of("B", "C", "D");

		try {
			ForwardProcessingEngine.combinePercentages(speciesNames, combineGroup, percentages);
			fail();
		} catch (IllegalArgumentException e) {
			// expected
		}
	}

	@Test
	void testCombinePercentagesBadArrays() {

		String[] speciesNames = new String[] { "D", "E", "F", "PW", "H", "PY", "L", "PA", "AT", "S", "MB", "Y", "PL" };
		float[] percentages = new float[] { 1.1f, 4.4f, 5.5f, 6.6f, 7.7f, 8.8f, 9.9f, 9.3f, 8.4f, 7.1f, 6.3f, 8.4f,
				9.4f, 1.6f };

		List<String> combineGroup = List.of("B", "C");

		try {
			ForwardProcessingEngine.combinePercentages(speciesNames, combineGroup, percentages);
			fail();
		} catch (IllegalArgumentException e) {
			// expected
		}
	}

	@Test
	void testFindInventoryTypeGroup() throws ProcessingException {
		try {
			ForwardProcessingEngine.findInventoryTypeGroup("A", Optional.empty(), 80.0f);
			fail();
		} catch (ProcessingException e) {
			// expected
		}

		Optional<String> ac = Optional.of("AC");
		assertThrows(
				IllegalArgumentException.class, () -> ForwardProcessingEngine.findInventoryTypeGroup("AC", ac, 0.0f)
		);

		assertThat(ForwardProcessingEngine.findInventoryTypeGroup("AC", Optional.empty(), 80.0f), is(36));

		int[] expectedResults = new int[] {
				/*                  Secondary                         */
				/* AC  B  C  D  E  F PW  H PY  L PA AT  S MB  Y PL -- */
				/* AC */ 35, 35, 36, 36, 35, 35, 35, 35, 35, 35, 36, 35, 36, 35, 35, 35, //
				/*  B */ 20, 19, 20, 20, 20, 20, 19, 20, 20, 20, 20, 20, 20, 19, 20, 20, //
				/*  C */ 10, 11, 10, 10, 10, 10, 11, 10, 10, 10, 10, 11, 10, 10, 10, 10, //
				/*  D */ 38, 37, 37, 38, 37, 37, 37, 37, 37, 37, 38, 37, 38, 37, 37, 37, //
				/*  E */ 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, //
				/*  F */ 8, 3, 2, 8, 8, 7, 3, 6, 7, 5, 8, 4, 8, 2, 5, 8, //
				/* PW */ 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, //
				/*  H */ 13, 15, 14, 13, 13, 13, 13, 13, 13, 13, 13, 16, 13, 14, 13, 13, //
				/* PY */ 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, //
				/*  L */ 34, 34, 34, 34, 34, 33, 34, 34, 34, 34, 34, 34, 34, 34, 34, 34, //
				/* PA */ 31, 30, 30, 31, 31, 29, 29, 30, 29, 29, 31, 30, 31, 30, 28, 30, //
				/* AT */ 42, 41, 41, 42, 42, 41, 41, 41, 41, 41, 41, 41, 42, 41, 41, 41, //
				/*  S */ 26, 24, 23, 26, 26, 22, 22, 23, 22, 22, 22, 26, 26, 23, 25, 22, //
				/* MB */ 39, 39, 39, 39, 39, 39, 39, 39, 39, 39, 39, 39, 39, 39, 39, 39, //
				/*  Y */ 10, 11, 10, 10, 10, 10, 10, 11, 10, 10, 10, 10, 11, 10, 10, 10, //
				/* PL */ 31, 30, 30, 31, 31, 29, 29, 30, 29, 29, 28, 31, 30, 31, 30, 30 };

		int currentAnswerIndex = 0;

		for (String primaryGenus : CommonData.ITG_PURE.keySet()) {
			for (Optional<String> secondaryGenus : CommonData.ITG_PURE.keySet().stream()
					.filter(k -> !k.equals(primaryGenus)).map(k -> Optional.of(k)).collect(Collectors.toList())) {
				int itg = ForwardProcessingEngine.findInventoryTypeGroup(primaryGenus, secondaryGenus, 50.0f);
				assertThat(itg, is(expectedResults[currentAnswerIndex++]));
			}
			int itg = ForwardProcessingEngine.findInventoryTypeGroup(primaryGenus, Optional.empty(), 50.0f);
			assertThat(itg, is(expectedResults[currentAnswerIndex++]));
		}
	}

	@Test
	void testCalculateMissingSiteCurves() throws IOException, ResourceParseException, ProcessingException {

		buildSpeciesParserForStream(
				"testSpecies.dat", //
				"01002 S000001 00     1970 P  3 B  B  100.0     0.0     0.0     0.0 -9.00 -9.00  -9.0  -9.0  -9.0 0 -9", //
				"01002 S000001 00     1970 P  4 C  C  100.0     0.0     0.0     0.0 -9.00 -9.00  -9.0  -9.0  -9.0 0 -9", //
				"01002 S000001 00     1970 P  5 D  D  100.0     0.0     0.0     0.0 -9.00 -9.00  -9.0  62.9   9.1 0 -9", //
				"01002 S000001 00     1970 P  8 H  H  100.0     0.0     0.0     0.0 -9.00 -9.00  -9.0 253.9  11.1 0 -9", //
				"01002 S000001 00     1970 P 15 S  S  100.0     0.0     0.0     0.0 -9.00 -9.00  -9.0  -9.0  -9.0 0 -9", //
				"01002 S000001 00     1970"
		);

		var reader = new ForwardDataStreamReader(controlMap);

		var testPolygonDescription = VdypPolygonDescriptionParser.parse("01002 S000001 00     1970");
		var polygon = reader.readNextPolygon(testPolygonDescription);

		ForwardProcessingEngine fpe = new ForwardProcessingEngine(controlMap);
		fpe.processPolygon(polygon, ForwardProcessingEngine.ExecutionStep.CALCULATE_MISSING_SITE_CURVES);

		// Cannot check 0 since determinePolygonRankings has not been executed.
		assertThat(fpe.fps.getPolygonProcessingState().getSiteCurveNumber(1), is(12));
		assertThat(fpe.fps.getPolygonProcessingState().getSiteCurveNumber(2), is(2));
		assertThat(fpe.fps.getPolygonProcessingState().getSiteCurveNumber(3), is(42));
		assertThat(fpe.fps.getPolygonProcessingState().getSiteCurveNumber(4), is(10));
		assertThat(fpe.fps.getPolygonProcessingState().getSiteCurveNumber(5), is(10));
	}

	@Test
	void testCalculateMissingSiteCurvesNoSiteCurveData()
			throws IOException, ResourceParseException, ProcessingException {

		buildSpeciesParserForStream(
				"testSpecies.dat", //
				"01002 S000001 00     1970 P  3 B       0.0     0.0     0.0     0.0 -9.00 -9.00  -9.0  -9.0  -9.0 0 -9", //
				"01002 S000001 00     1970 P  4 C       0.0     0.0     0.0     0.0 13.40 -9.00  -9.0  -9.0  -9.0 0 -9", //
				"01002 S000001 00     1970 P  5 D       0.0     0.0     0.0     0.0 -9.00 -9.00  -9.0  62.9   9.1 0 -9", //
				"01002 S000001 00     1970 P  8 H       0.0     0.0     0.0     0.0 -9.00 -9.00  -9.0 253.9  11.1 0 -9", //
				"01002 S000001 00     1970 P 15 S       0.0     0.0     0.0     0.0 -9.00 -9.00  -9.0  -9.0  -9.0 0 -9", //
				"01002 S000001 00     1970"
		);

		var siteCurveMap = new MatrixMap2Impl<String, Region, SiteIndexEquation>(
				new ArrayList<String>(), new ArrayList<Region>(), (k1, k2) -> SiteIndexEquation.SI_NO_EQUATION
		);

		controlMap.put(ControlKey.SITE_CURVE_NUMBERS.name(), siteCurveMap);

		var reader = new ForwardDataStreamReader(controlMap);

		var testPolygonDescription = VdypPolygonDescriptionParser.parse("01002 S000001 00     1970");
		var polygon = reader.readNextPolygon(testPolygonDescription);

		ForwardProcessingEngine fpe = new ForwardProcessingEngine(controlMap);
		fpe.processPolygon(polygon, ForwardProcessingEngine.ExecutionStep.CALCULATE_MISSING_SITE_CURVES);

		// Cannot check 0 since determinePolygonRankings has not been executed.
		assertThat(fpe.fps.getPolygonProcessingState().getSiteCurveNumber(1), is(118));
		assertThat(fpe.fps.getPolygonProcessingState().getSiteCurveNumber(2), is(122));
		assertThat(fpe.fps.getPolygonProcessingState().getSiteCurveNumber(3), is(13));
		assertThat(fpe.fps.getPolygonProcessingState().getSiteCurveNumber(4), is(99));
		assertThat(fpe.fps.getPolygonProcessingState().getSiteCurveNumber(5), is(59));
	}

	@Test
	void testEstimateMissingSiteIndicesStep1() throws ProcessingException, IOException, ResourceParseException,
			CurveErrorException, SpeciesErrorException, NoAnswerException {

		buildPolygonParserForStream("testPolygon.dat", "01002 S000001 00     1970 CWH  A    99 37  1  1");

		buildSpeciesParserForStream(
				"testSpecies.dat", //
				"01002 S000001 00     1970 P  3 B  B  100.0     0.0     0.0     0.0 -9.00 -9.00  -9.0  -9.0  -9.0 0 -9", //
				"01002 S000001 00     1970 P  4 C  C  100.0     0.0     0.0     0.0 13.40 -9.00  -9.0  -9.0  -9.0 0 11", //
				"01002 S000001 00     1970 P  5 D  D  100.0     0.0     0.0     0.0 -9.00 -9.00  -9.0  -9.0  -9.0 0 99", //
				"01002 S000001 00     1970 P  8 H  H  100.0     0.0     0.0     0.0 -9.00 28.90 265.0 253.9  11.1 1 99", //
				"01002 S000001 00     1970 P 15 S  S  100.0     0.0     0.0     0.0 -9.00 -9.00  -9.0  -9.0  -9.0 0 -9", //
				"01002 S000001 00     1970"
		);

		buildUtilizationParserForStandardStream("testUtilizations.dat");

		var siteCurveMap = new MatrixMap2Impl<String, Region, SiteIndexEquation>(
				new ArrayList<String>(), new ArrayList<Region>(), (k1, k2) -> SiteIndexEquation.SI_NO_EQUATION
		);

		controlMap.put(ControlKey.SITE_CURVE_NUMBERS.name(), siteCurveMap);

		var reader = new ForwardDataStreamReader(controlMap);

		var testPolygonDescription = VdypPolygonDescriptionParser.parse("01002 S000001 00     1970");
		var polygon = reader.readNextPolygon(testPolygonDescription);

		ForwardProcessingEngine fpe = new ForwardProcessingEngine(controlMap);
		fpe.processPolygon(polygon, ForwardProcessingEngine.ExecutionStep.ESTIMATE_MISSING_SITE_INDICES);

		var sourceSiteCurve = SiteIndexEquation.SI_CWC_KURUCZ;
		var sourceSiteIndex = 13.4;
		var targetSiteCurve = SiteIndexEquation.SI_HWC_WILEYAC;
		double expectedValue = SiteTool
				.convertSiteIndexBetweenCurves(sourceSiteCurve, sourceSiteIndex, targetSiteCurve);

		assertThat(fpe.fps.getPolygonProcessingState().wallet.siteIndices[4], is((float) expectedValue));
	}

	@Test
	void testEstimateMissingSiteIndicesStep2() throws ProcessingException, IOException, ResourceParseException,
			CurveErrorException, SpeciesErrorException, NoAnswerException {

		var targetDescription = VdypPolygonDescriptionParser.parse("01004 S000037 00     1957");
		var polygon = forwardDataStreamReader.readNextPolygon(targetDescription);

		ForwardProcessingEngine fpe = new ForwardProcessingEngine(controlMap);
		fpe.processPolygon(polygon, ForwardProcessingEngine.ExecutionStep.ESTIMATE_MISSING_SITE_INDICES);

		var sourceSiteCurve = SiteIndexEquation.SI_CWC_KURUCZ;
		var sourceSiteIndex = 13.4;
		var targetSiteCurve = SiteIndexEquation.SI_CWC_BARKER;
		double expectedValue = SiteTool
				.convertSiteIndexBetweenCurves(sourceSiteCurve, sourceSiteIndex, targetSiteCurve);

		assertThat(fpe.fps.getPolygonProcessingState().wallet.siteIndices[1], is((float) expectedValue));
	}

	@Test
	void testEstimateMissingYearsToBreastHeightValues()
			throws ProcessingException, IOException, ResourceParseException {

		var testPolygonDescription = VdypPolygonDescriptionParser.parse("01002 S000001 00     1970");

		buildSpeciesParserForStream(
				"testSpecies.dat", //
				"01002 S000001 00     1970 P  3 B  B  100.0     0.0     0.0     0.0 -9.00 -9.00  15.0  11.0  -9.0 0 -9", //
				"01002 S000001 00     1970 P  4 C  C  100.0     0.0     0.0     0.0 -9.00 -9.00  -9.0  -9.0  -9.0 0 -9", //
				"01002 S000001 00     1970 P  5 D  D  100.0     0.0     0.0     0.0 35.00 35.30  55.0  54.0   1.0 1 13", //
				"01002 S000001 00     1970 P  8 H  H  100.0     0.0     0.0     0.0 -9.00 -9.00  -9.0  -9.0  -9.0 0 -9", //
				"01002 S000001 00     1970 P 15 S  S  100.0     0.0     0.0     0.0 -9.00 -9.00  -9.0  -9.0  -9.0 0 -9", //
				"01002 S000001 00     1970"
		);

		var reader = new ForwardDataStreamReader(controlMap);

		var polygon = reader.readNextPolygon(testPolygonDescription);

		ForwardProcessingEngine fpe = new ForwardProcessingEngine(controlMap);
		fpe.processPolygon(
				polygon, ForwardProcessingEngine.ExecutionStep.ESTIMATE_MISSING_YEARS_TO_BREAST_HEIGHT_VALUES
		);

		assertThat(
				fpe.fps.getPolygonProcessingState().wallet.yearsToBreastHeight,
				is(new float[] { 0.0f, 4.0f, 4.6f, 1.0f, 5.0f, 5.0f })
		);
	}

	@Test
	void testCalculateDominantHeightAgeSiteIndex() throws ProcessingException, IOException, ResourceParseException {

		var testPolygonDescription = VdypPolygonDescriptionParser.parse("01002 S000001 00     1970");

		buildSpeciesParserForStream(
				"testSpecies.dat", //
				// Polygon            Year L? x G  S1 %    S2 %    S3 %    S4 %     Sidx  domH   age  agebh y2bh P? SC
				"01002 S000001 00     1970 P  3 B  B  100.0     0.0     0.0     0.0 -9.00 -9.00  15.0  11.0  -9.0 0 -9", //
				"01002 S000001 00     1970 P  4 C  C  100.0     0.0     0.0     0.0 34.00 -9.00  -9.0  -9.0  -9.0 0 -9", //
				"01002 S000001 00     1970 P  5 D  D  100.0     0.0     0.0     0.0 -9.00 -9.00  -9.0  -9.0  -9.0 1 13", //
				"01002 S000001 00     1970 P  8 H  H  100.0     0.0     0.0     0.0 -9.00 -9.00  -9.0  -9.0  -9.0 0 -9", //
				"01002 S000001 00     1970 P 15 S  S  100.0     0.0     0.0     0.0 -9.00 -9.00  -9.0  -9.0  -9.0 0 -9", //
				"01002 S000001 00     1970"
		);

		buildUtilizationParserForStandardStream("testUtilizations.dat");

		var reader = new ForwardDataStreamReader(controlMap);

		var polygon = reader.readNextPolygon(testPolygonDescription);

		ForwardProcessingEngine fpe = new ForwardProcessingEngine(controlMap);
		fpe.processPolygon(polygon, ForwardProcessingEngine.ExecutionStep.CALCULATE_DOMINANT_HEIGHT_AGE_SITE_INDEX);

		assertThat(fpe.fps.getPolygonProcessingState().getPrimarySpeciesDominantHeight(), is(35.31195f));
		assertThat(fpe.fps.getPolygonProcessingState().getPrimarySpeciesSiteIndex(), is(34.0f));
		assertThat(fpe.fps.getPolygonProcessingState().getPrimarySpeciesTotalAge(), is(15.0f));
		assertThat(fpe.fps.getPolygonProcessingState().getPrimarySpeciesAgeAtBreastHeight(), is(14.0f));
		assertThat(fpe.fps.getPolygonProcessingState().getPrimarySpeciesAgeToBreastHeight(), is(1.0f));
	}

	@Test
	void testSetEquationGroups() throws ResourceParseException, IOException, ProcessingException {
		var testPolygonDescription = VdypPolygonDescriptionParser.parse("01002 S000001 00     1970");

		var reader = new ForwardDataStreamReader(controlMap);

		var polygon = reader.readNextPolygon(testPolygonDescription);

		ForwardProcessingState fps = new ForwardProcessingState(controlMap);
		fps.setPolygon(polygon);

		assertThat(
				fps.getPolygonProcessingState().volumeEquationGroups,
				Matchers.is(new int[] { VdypEntity.MISSING_INTEGER_VALUE, 12, 20, 25, 37, 66 })
		);
		assertThat(
				fps.getPolygonProcessingState().decayEquationGroups,
				Matchers.is(new int[] { VdypEntity.MISSING_INTEGER_VALUE, 7, 14, 19, 31, 54 })
		);
		assertThat(
				fps.getPolygonProcessingState().breakageEquationGroups,
				Matchers.is(new int[] { VdypEntity.MISSING_INTEGER_VALUE, 5, 6, 12, 17, 28 })
		);
	}

	@Test
	void testCalculateDominantHeightAgeSiteIndexNoSecondary()
			throws ProcessingException, IOException, ResourceParseException {

		var testPolygonDescription = VdypPolygonDescriptionParser.parse("01002 S000001 00     1970");

		buildSpeciesParserForStream(
				"testSpecies.dat", //
				"01002 S000001 00     1970 P  4 C  C  100.0     0.0     0.0     0.0 34.00 -9.00  22.0  -9.0  -9.0 0 -9", //
				"01002 S000001 00     1970"
		);

		buildUtilizationParserForStream(
				"testUtilizations.dat", //
				"01002 S000001 00     1970 P  0    -1  0.01513     5.24   7.0166   0.0630   0.0000   0.0000   0.0000   0.0000   6.1", //
				"01002 S000001 00     1970 P  0     0 44.93259   595.32  30.9724 620.9775 592.2023 580.1681 577.6229 549.0159  31.0", //
				"01002 S000001 00     1970 P  0     1  0.53100    64.82  -9.0000   2.5979   0.3834   0.3794   0.3788   0.3623  10.2", //
				"01002 S000001 00     1970 P  0     2  1.27855    71.93  -9.0000   9.1057   6.9245   6.8469   6.8324   6.5384  15.0", //
				"01002 S000001 00     1970 P  0     3  2.33020    73.60  -9.0000  22.4019  20.1244  19.8884  19.8375  18.9555  20.1", //
				"01002 S000001 00     1970 P  0     4 40.79285   384.98  -9.0000 586.8720 564.7699 553.0534 550.5741 523.1597  36.7", //
				"01002 S000001 00     1970 P  4 C  -1  0.01243     4.40   6.4602   0.0507   0.0000   0.0000   0.0000   0.0000   6.0", //
				"01002 S000001 00     1970 P  4 C   0  5.04597    83.46  22.9584  43.4686  39.4400  36.2634  35.2930  32.9144  27.7", //
				"01002 S000001 00     1970 P  4 C   1  0.12822    16.12  -9.0000   0.6027   0.1116   0.1094   0.1090   0.1035  10.1", //
				"01002 S000001 00     1970 P  4 C   2  0.31003    17.87  -9.0000   1.9237   1.4103   1.3710   1.3628   1.2915  14.9", //
				"01002 S000001 00     1970 P  4 C   3  0.51339    16.55  -9.0000   3.8230   3.3162   3.2127   3.1859   3.0076  19.9", //
				"01002 S000001 00     1970 P  4 C   4  4.09434    32.92  -9.0000  37.1192  34.6019  31.5703  30.6352  28.5119  39.8", //
				"01002 S000001 00     1970"
		);

		var reader = new ForwardDataStreamReader(controlMap);

		var polygon = reader.readNextPolygon(testPolygonDescription);

		ForwardProcessingEngine fpe = new ForwardProcessingEngine(controlMap);
		fpe.processPolygon(polygon, ForwardProcessingEngine.ExecutionStep.CALCULATE_DOMINANT_HEIGHT_AGE_SITE_INDEX);

		assertThat(fpe.fps.getPolygonProcessingState().getPrimarySpeciesDominantHeight(), is(22.950302f));
		assertThat(fpe.fps.getPolygonProcessingState().getPrimarySpeciesSiteIndex(), is(34.0f));
		assertThat(fpe.fps.getPolygonProcessingState().getPrimarySpeciesTotalAge(), is(22.0f));
		assertThat(fpe.fps.getPolygonProcessingState().getPrimarySpeciesAgeAtBreastHeight(), is(Float.NaN));
		assertThat(fpe.fps.getPolygonProcessingState().getPrimarySpeciesAgeToBreastHeight(), is(4.7f));
	}

	private void buildPolygonParserForStream(String fileName, String... streamContent)
			throws ResourceParseException, IOException {

		controlMap.put(ControlKey.FORWARD_INPUT_VDYP_POLY.name(), fileName);
		var polygonParser = new VdypPolygonParser();
		var polygonFileResolver = TestUtils.fileResolver(fileName, TestUtils.makeInputStream(streamContent));

		polygonParser.modify(controlMap, polygonFileResolver);
	}

	private void buildSpeciesParserForStream(String fileName, String... streamContent)
			throws ResourceParseException, IOException {

		controlMap.put(ControlKey.FORWARD_INPUT_VDYP_LAYER_BY_SPECIES.name(), fileName);
		var speciesParser = new VdypSpeciesParser();
		var speciesFileResolver = TestUtils.fileResolver(fileName, TestUtils.makeInputStream(streamContent));

		speciesParser.modify(controlMap, speciesFileResolver);
	}

	private void buildUtilizationParserForStream(String fileName, String... streamContent)
			throws ResourceParseException, IOException {

		controlMap.put(ControlKey.FORWARD_INPUT_VDYP_LAYER_BY_SP0_BY_UTIL.name(), fileName);
		var utilizationsParser = new VdypUtilizationParser();
		var utilizationsFileResolver = TestUtils.fileResolver(fileName, TestUtils.makeInputStream(streamContent));

		utilizationsParser.modify(controlMap, utilizationsFileResolver);
	}

	private void buildUtilizationParserForStandardStream(String fileName) throws ResourceParseException, IOException {

		buildUtilizationParserForStream(
				fileName, //
				// Polygon            Year P? i S  uc ba         tph      lh     ws       cu       cu-d     cu-d-w   cu-d-w-b  qmd
				"01002 S000001 00     1970 P  0    -1  0.01513     5.24   7.0166   0.0630   0.0000   0.0000   0.0000   0.0000   6.1",
				"01002 S000001 00     1970 P  0     0 44.93259   595.32  30.9724 620.9775 592.2023 580.1681 577.6229 549.0159  31.0",
				"01002 S000001 00     1970 P  0     1  0.53100    64.82  -9.0000   2.5979   0.3834   0.3794   0.3788   0.3623  10.2",
				"01002 S000001 00     1970 P  0     2  1.27855    71.93  -9.0000   9.1057   6.9245   6.8469   6.8324   6.5384  15.0",
				"01002 S000001 00     1970 P  0     3  2.33020    73.60  -9.0000  22.4019  20.1244  19.8884  19.8375  18.9555  20.1",
				"01002 S000001 00     1970 P  0     4 40.79285   384.98  -9.0000 586.8720 564.7699 553.0534 550.5741 523.1597  36.7",
				"01002 S000001 00     1970 P  3 B  -1  0.00000     0.00   8.0272   0.0000   0.0000   0.0000   0.0000   0.0000   6.1",
				"01002 S000001 00     1970 P  3 B   0  0.40292     5.16  36.7553   6.2098   5.9592   5.8465   5.8163   5.5177  31.5",
				"01002 S000001 00     1970 P  3 B   1  0.00502     0.76  -9.0000   0.0185   0.0009   0.0009   0.0009   0.0009   9.2",
				"01002 S000001 00     1970 P  3 B   2  0.01363     0.93  -9.0000   0.0757   0.0498   0.0497   0.0496   0.0475  13.7",
				"01002 S000001 00     1970 P  3 B   3  0.02284     0.88  -9.0000   0.1748   0.1521   0.1514   0.1512   0.1445  18.2",
				"01002 S000001 00     1970 P  3 B   4  0.36143     2.60  -9.0000   5.9408   5.7564   5.6446   5.6146   5.3249  42.1",
				"01002 S000001 00     1970 P  4 C  -1  0.01243     4.40   6.4602   0.0507   0.0000   0.0000   0.0000   0.0000   6.0",
				"01002 S000001 00     1970 P  4 C   0  5.04597    83.46  22.9584  43.4686  39.4400  36.2634  35.2930  32.9144  27.7",
				"01002 S000001 00     1970 P  4 C   1  0.12822    16.12  -9.0000   0.6027   0.1116   0.1094   0.1090   0.1035  10.1",
				"01002 S000001 00     1970 P  4 C   2  0.31003    17.87  -9.0000   1.9237   1.4103   1.3710   1.3628   1.2915  14.9",
				"01002 S000001 00     1970 P  4 C   3  0.51339    16.55  -9.0000   3.8230   3.3162   3.2127   3.1859   3.0076  19.9",
				"01002 S000001 00     1970 P  4 C   4  4.09434    32.92  -9.0000  37.1192  34.6019  31.5703  30.6352  28.5119  39.8",
				"01002 S000001 00     1970 P  5 D  -1  0.00155     0.47  10.6033   0.0078   0.0000   0.0000   0.0000   0.0000   6.5",
				"01002 S000001 00     1970 P  5 D   0 29.30249   287.70  33.7440 459.5233 444.0844 436.5280 435.2818 413.6949  36.0",
				"01002 S000001 00     1970 P  5 D   1  0.01412     1.64  -9.0000   0.1091   0.0571   0.0566   0.0565   0.0541  10.5",
				"01002 S000001 00     1970 P  5 D   2  0.05128     2.69  -9.0000   0.5602   0.5048   0.5007   0.5005   0.4783  15.6",
				"01002 S000001 00     1970 P  5 D   3  0.45736    13.82  -9.0000   6.0129   5.6414   5.5975   5.5948   5.3383  20.5",
				"01002 S000001 00     1970 P  5 D   4 28.77972   269.56  -9.0000 452.8412 437.8810 430.3732 429.1300 407.8242  36.9",
				"01002 S000001 00     1970 P  8 H  -1  0.00000     0.00   7.5464   0.0000   0.0000   0.0000   0.0000   0.0000  -9.0",
				"01002 S000001 00     1970 P  8 H   0  5.81006   167.90  22.7704  55.8878  49.8291  49.0742  48.8550  46.6828  21.0",
				"01002 S000001 00     1970 P  8 H   1  0.36138    43.57  -9.0000   1.7385   0.1925   0.1913   0.1911   0.1834  10.3",
				"01002 S000001 00     1970 P  8 H   2  0.82449    45.99  -9.0000   5.8666   4.4155   4.3846   4.3789   4.2023  15.1",
				"01002 S000001 00     1970 P  8 H   3  1.07566    33.93  -9.0000   9.6521   8.5752   8.5019   8.4827   8.1397  20.1",
				"01002 S000001 00     1970 P  8 H   4  3.54853    44.42  -9.0000  38.6306  36.6459  35.9963  35.8023  34.1574  31.9",
				"01002 S000001 00     1970 P 15 S  -1  0.00115     0.36   8.2003   0.0045   0.0000   0.0000   0.0000   0.0000   6.3",
				"01002 S000001 00     1970 P 15 S   0  4.37115    51.10  32.0125  55.8879  52.8895  52.4561  52.3768  50.2060  33.0",
				"01002 S000001 00     1970 P 15 S   1  0.02225     2.73  -9.0000   0.1291   0.0213   0.0212   0.0212   0.0204  10.2",
				"01002 S000001 00     1970 P 15 S   2  0.07911     4.46  -9.0000   0.6795   0.5440   0.5410   0.5406   0.5189  15.0",
				"01002 S000001 00     1970 P 15 S   3  0.26095     8.43  -9.0000   2.7391   2.4396   2.4250   2.4229   2.3254  19.9",
				"01002 S000001 00     1970 P 15 S   4  4.00883    35.49  -9.0000  52.3402  49.8846  49.4689  49.3920  47.3414  37.9",
				"01002 S000001 00     1970  "
		);
	}
}
