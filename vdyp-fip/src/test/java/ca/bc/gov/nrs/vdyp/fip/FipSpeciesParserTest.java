package ca.bc.gov.nrs.vdyp.fip;

import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.assertEmpty;
import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.assertNext;
import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.hasSpecificEntry;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import ca.bc.gov.nrs.vdyp.fip.model.FipSpecies;
import ca.bc.gov.nrs.vdyp.io.parse.BecDefinitionParserTest;
import ca.bc.gov.nrs.vdyp.io.parse.GenusDefinitionParserTest;
import ca.bc.gov.nrs.vdyp.io.parse.StreamingParser;
import ca.bc.gov.nrs.vdyp.io.parse.StreamingParserFactory;
import ca.bc.gov.nrs.vdyp.model.Layer;
import ca.bc.gov.nrs.vdyp.test.TestUtils;

public class FipSpeciesParserTest {

	@Test
	public void testParseEmpty() throws Exception {

		var parser = new FipSpeciesParser();

		Map<String, Object> controlMap = new HashMap<>();

		controlMap.put(FipSpeciesParser.CONTROL_KEY, "test.dat");
		BecDefinitionParserTest.populateControlMapReal(controlMap);

		var fileResolver = TestUtils.fileResolver("test.dat", TestUtils.makeStream(/* empty */));

		parser.modify(controlMap, fileResolver);

		var parserFactory = controlMap.get(FipSpeciesParser.CONTROL_KEY);

		assertThat(parserFactory, instanceOf(StreamingParserFactory.class));

		@SuppressWarnings("unchecked")
		var stream = ((StreamingParserFactory<FipSpecies>) parserFactory).get();

		assertThat(stream, instanceOf(StreamingParser.class));

		assertEmpty(stream);
	}

	@Test
	public void testParseOneGenus() throws Exception {

		var parser = new FipSpeciesParser();

		Map<String, Object> controlMap = new HashMap<>();

		controlMap.put(FipSpeciesParser.CONTROL_KEY, "test.dat");
		GenusDefinitionParserTest.populateControlMapReal(controlMap);

		var fileResolver = TestUtils.fileResolver(
				"test.dat",
				TestUtils.makeStream(
						"01002 S000001 00     1970 1 B  100.0B  100.0     0.0     0.0     0.0",
						"01002 S000001 00     1970 Z      0.0     0.0     0.0     0.0     0.0"
				)
		);

		parser.modify(controlMap, fileResolver);

		var parserFactory = controlMap.get(FipSpeciesParser.CONTROL_KEY);

		assertThat(parserFactory, instanceOf(StreamingParserFactory.class));

		@SuppressWarnings("unchecked")
		var stream = ((StreamingParserFactory<Collection<FipSpecies>>) parserFactory).get();

		assertThat(stream, instanceOf(StreamingParser.class));

		var genera = assertNext(stream);

		assertThat(
				genera,
				containsInAnyOrder(
						allOf(
								hasProperty("polygonIdentifier", is("01002 S000001 00     1970")),
								hasProperty("layer", is(Layer.PRIMARY)), hasProperty("genus", is("B")),
								hasProperty("percentGenus", is(100.0f)),
								hasProperty("speciesPercent", allOf(aMapWithSize(1), hasSpecificEntry("B", is(100.0f))))
						)
				)
		);

		assertEmpty(stream);
	}

	@Test
	public void testParseTwoGenera() throws Exception {

		var parser = new FipSpeciesParser();

		Map<String, Object> controlMap = new HashMap<>();

		controlMap.put(FipSpeciesParser.CONTROL_KEY, "test.dat");
		GenusDefinitionParserTest.populateControlMapReal(controlMap);

		var fileResolver = TestUtils.fileResolver(
				"test.dat",
				TestUtils.makeStream(
						"01002 S000001 00     1970 1 B   75.0B  100.0     0.0     0.0     0.0",
						"01002 S000001 00     1970 1 C   25.0C  100.0     0.0     0.0     0.0",
						"01002 S000001 00     1970 Z      0.0     0.0     0.0     0.0     0.0"
				)
		);

		parser.modify(controlMap, fileResolver);

		var parserFactory = controlMap.get(FipSpeciesParser.CONTROL_KEY);

		assertThat(parserFactory, instanceOf(StreamingParserFactory.class));

		@SuppressWarnings("unchecked")
		var stream = ((StreamingParserFactory<Collection<FipSpecies>>) parserFactory).get();

		assertThat(stream, instanceOf(StreamingParser.class));

		var genera = assertNext(stream);

		assertThat(
				genera,
				containsInAnyOrder(
						allOf(
								hasProperty("polygonIdentifier", is("01002 S000001 00     1970")),
								hasProperty("layer", is(Layer.PRIMARY)), hasProperty("genus", is("B")),
								hasProperty("percentGenus", is(75.0f)),
								hasProperty("speciesPercent", allOf(aMapWithSize(1), hasSpecificEntry("B", is(100.0f))))
						),
						allOf(
								hasProperty("polygonIdentifier", is("01002 S000001 00     1970")),
								hasProperty("layer", is(Layer.PRIMARY)), hasProperty("genus", is("C")),
								hasProperty("percentGenus", is(25.0f)),
								hasProperty("speciesPercent", allOf(aMapWithSize(1), hasSpecificEntry("C", is(100.0f))))
						)
				)
		);

		assertEmpty(stream);

	}

	@Test
	public void testParseTwoLayers() throws Exception {

		var parser = new FipSpeciesParser();

		Map<String, Object> controlMap = new HashMap<>();

		controlMap.put(FipSpeciesParser.CONTROL_KEY, "test.dat");
		GenusDefinitionParserTest.populateControlMapReal(controlMap);

		var fileResolver = TestUtils.fileResolver(
				"test.dat",
				TestUtils.makeStream(
						"01002 S000001 00     1970 1 B  100.0B  100.0     0.0     0.0     0.0",
						"01002 S000001 00     1970 V B  100.0B  100.0     0.0     0.0     0.0",
						"01002 S000001 00     1970 Z      0.0     0.0     0.0     0.0     0.0"
				)
		);

		parser.modify(controlMap, fileResolver);

		var parserFactory = controlMap.get(FipSpeciesParser.CONTROL_KEY);

		assertThat(parserFactory, instanceOf(StreamingParserFactory.class));

		@SuppressWarnings("unchecked")
		var stream = ((StreamingParserFactory<Collection<FipSpecies>>) parserFactory).get();

		assertThat(stream, instanceOf(StreamingParser.class));

		var genera = assertNext(stream);

		assertThat(
				genera,
				containsInAnyOrder(
						allOf(
								hasProperty("polygonIdentifier", is("01002 S000001 00     1970")),
								hasProperty("layer", is(Layer.PRIMARY)), hasProperty("genus", is("B")),
								hasProperty("percentGenus", is(100.0f)),
								hasProperty("speciesPercent", allOf(aMapWithSize(1), hasSpecificEntry("B", is(100.0f))))
						),
						allOf(
								hasProperty("polygonIdentifier", is("01002 S000001 00     1970")),
								hasProperty("layer", is(Layer.VETERAN)), hasProperty("genus", is("B")),
								hasProperty("percentGenus", is(100.0f)),
								hasProperty("speciesPercent", allOf(aMapWithSize(1), hasSpecificEntry("B", is(100.0f))))
						)
				)
		);

		assertEmpty(stream);
	}

	@Test
	public void testParseTwoPolygons() throws Exception {

		var parser = new FipSpeciesParser();

		Map<String, Object> controlMap = new HashMap<>();

		controlMap.put(FipSpeciesParser.CONTROL_KEY, "test.dat");
		GenusDefinitionParserTest.populateControlMapReal(controlMap);

		var fileResolver = TestUtils.fileResolver(
				"test.dat",
				TestUtils.makeStream(
						"01002 S000001 00     1970 1 B  100.0B  100.0     0.0     0.0     0.0",
						"01002 S000001 00     1970 Z      0.0     0.0     0.0     0.0     0.0",
						"01002 S000002 00     1970 1 B  100.0B  100.0     0.0     0.0     0.0",
						"01002 S000002 00     1970 Z      0.0     0.0     0.0     0.0     0.0"
				)
		);

		parser.modify(controlMap, fileResolver);

		var parserFactory = controlMap.get(FipSpeciesParser.CONTROL_KEY);

		assertThat(parserFactory, instanceOf(StreamingParserFactory.class));

		@SuppressWarnings("unchecked")
		var stream = ((StreamingParserFactory<Collection<FipSpecies>>) parserFactory).get();

		assertThat(stream, instanceOf(StreamingParser.class));

		var genera = assertNext(stream);

		assertThat(
				genera,
				containsInAnyOrder(
						allOf(
								hasProperty("polygonIdentifier", is("01002 S000001 00     1970")),
								hasProperty("layer", is(Layer.PRIMARY)), hasProperty("genus", is("B")),
								hasProperty("percentGenus", is(100.0f)),
								hasProperty("speciesPercent", allOf(aMapWithSize(1), hasSpecificEntry("B", is(100.0f))))
						)
				)
		);

		genera = assertNext(stream);

		assertThat(
				genera,
				containsInAnyOrder(
						allOf(
								hasProperty("polygonIdentifier", is("01002 S000002 00     1970")),
								hasProperty("layer", is(Layer.PRIMARY)), hasProperty("genus", is("B")),
								hasProperty("percentGenus", is(100.0f)),
								hasProperty("speciesPercent", allOf(aMapWithSize(1), hasSpecificEntry("B", is(100.0f))))
						)
				)
		);

		assertEmpty(stream);
	}

	@Test
	public void testParseMutipleSpecies() throws Exception {

		var parser = new FipSpeciesParser();

		Map<String, Object> controlMap = new HashMap<>();

		controlMap.put(FipSpeciesParser.CONTROL_KEY, "test.dat");
		GenusDefinitionParserTest.populateControlMapReal(controlMap);

		var fileResolver = TestUtils.fileResolver(
				"test.dat",
				TestUtils.makeStream(
						"01002 S000001 00     1970 1 B  100.0B1  75.0B2  10.0B3   8.0B4   7.0",
						"01002 S000001 00     1970 Z      0.0     0.0     0.0     0.0     0.0"
				)
		);

		parser.modify(controlMap, fileResolver);

		var parserFactory = controlMap.get(FipSpeciesParser.CONTROL_KEY);

		assertThat(parserFactory, instanceOf(StreamingParserFactory.class));

		@SuppressWarnings("unchecked")
		var stream = ((StreamingParserFactory<Collection<FipSpecies>>) parserFactory).get();

		assertThat(stream, instanceOf(StreamingParser.class));

		var genera = assertNext(stream);

		assertThat(
				genera,
				containsInAnyOrder(
						allOf(
								hasProperty("polygonIdentifier", is("01002 S000001 00     1970")),
								hasProperty("layer", is(Layer.PRIMARY)), hasProperty("genus", is("B")),
								hasProperty("percentGenus", is(100.0f)),
								hasProperty(
										"speciesPercent",
										allOf(
												aMapWithSize(4),
												allOf(
														hasSpecificEntry("B1", is(75.0f)),
														hasSpecificEntry("B2", is(10.0f)),
														hasSpecificEntry("B3", is(8.0f)),
														hasSpecificEntry("B4", is(7.0f))
												)
										)
								)
						)
				)
		);

		assertEmpty(stream);
	}

}
