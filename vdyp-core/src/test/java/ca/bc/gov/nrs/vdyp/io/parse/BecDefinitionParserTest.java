package ca.bc.gov.nrs.vdyp.io.parse;

import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.hasBec;
import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.present;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasProperty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.Test;

import ca.bc.gov.nrs.vdyp.model.BecDefinition;
import ca.bc.gov.nrs.vdyp.model.BecLookup;
import ca.bc.gov.nrs.vdyp.model.Region;

public class BecDefinitionParserTest {

	@Test
	public void testParse() throws Exception {
		var parser = new BecDefinitionParser();

		var result = parser.parse(ControlFileParserTest.class, "coe/Becdef.dat", Collections.emptyMap());

		assertThat(
				result,
				hasBec(
						"AT",
						present(
								allOf(
										hasProperty("alias", equalTo("AT")),
										hasProperty("region", equalTo(Region.INTERIOR)),
										hasProperty("name", equalTo("Alpine Tundra"))
								)
						)
				)
		);
		assertThat(
				result,
				hasBec(
						"BG",
						present(
								allOf(
										hasProperty("alias", equalTo("BG")),
										hasProperty("region", equalTo(Region.INTERIOR)),
										hasProperty("name", equalTo("Bunchgrass"))
								)
						)
				)
		);
		assertThat(
				result,
				hasBec(
						"BWBS",
						present(
								allOf(
										hasProperty("alias", equalTo("BWBS")),
										hasProperty("region", equalTo(Region.INTERIOR)),
										hasProperty("name", equalTo("Boreal White and Black Spruce"))
								)
						)
				)
		);
		assertThat(
				result,
				hasBec(
						"CDF",
						present(
								allOf(
										hasProperty("alias", equalTo("CDF")),
										hasProperty("region", equalTo(Region.COASTAL)),
										hasProperty("name", equalTo("Coastal Dougfir"))
								)
						)
				)
		);
		assertThat(
				result,
				hasBec(
						"CWH",
						present(
								allOf(
										hasProperty("alias", equalTo("CWH")),
										hasProperty("region", equalTo(Region.COASTAL)),
										hasProperty("name", equalTo("Coastal Western Hemlock"))
								)
						)
				)
		);
		assertThat(
				result,
				hasBec(
						"ESSF",
						present(
								allOf(
										hasProperty("alias", equalTo("ESSF")),
										hasProperty("region", equalTo(Region.INTERIOR)),
										hasProperty("name", equalTo("Englemann Sruce -SubAlpine Fir"))
								)
						)
				)
		);
		assertThat(
				result,
				hasBec(
						"ICH",
						present(
								allOf(
										hasProperty("alias", equalTo("ICH")),
										hasProperty("region", equalTo(Region.INTERIOR)),
										hasProperty("name", equalTo("Interior Cedar-Hemlock"))
								)
						)
				)
		);
		assertThat(
				result,
				hasBec(
						"IDF",
						present(
								allOf(
										hasProperty("alias", equalTo("IDF")),
										hasProperty("region", equalTo(Region.INTERIOR)),
										hasProperty("name", equalTo("Interior DougFir"))
								)
						)
				)
		);
		assertThat(
				result,
				hasBec(
						"MH",
						present(
								allOf(
										hasProperty("alias", equalTo("MH")),
										hasProperty("region", equalTo(Region.COASTAL)),
										hasProperty("name", equalTo("Mountain Hemlock"))
								)
						)
				)
		);
		assertThat(
				result,
				hasBec(
						"MS",
						present(
								allOf(
										hasProperty("alias", equalTo("MS")),
										hasProperty("region", equalTo(Region.INTERIOR)),
										hasProperty("name", equalTo("Montane Spruce"))
								)
						)
				)
		);
		assertThat(
				result,
				hasBec(
						"PP",
						present(
								allOf(
										hasProperty("alias", equalTo("PP")),
										hasProperty("region", equalTo(Region.INTERIOR)),
										hasProperty("name", equalTo("Ponderosa Pine"))
								)
						)
				)
		);
		assertThat(
				result,
				hasBec(
						"SBPS",
						present(
								allOf(
										hasProperty("alias", equalTo("SBPS")),
										hasProperty("region", equalTo(Region.INTERIOR)),
										hasProperty("name", equalTo("SubBoreal Pine-Spruce"))
								)
						)
				)
		);
		assertThat(
				result,
				hasBec(
						"SBS",
						present(
								allOf(
										hasProperty("alias", equalTo("SBS")),
										hasProperty("region", equalTo(Region.INTERIOR)),
										hasProperty("name", equalTo("SubBoreal Spruce"))
								)
						)
				)
		);
		assertThat(
				result,
				hasBec(
						"SWB",
						present(
								allOf(
										hasProperty("alias", equalTo("SWB")),
										hasProperty("region", equalTo(Region.INTERIOR)),
										hasProperty("name", equalTo("Spruce-Willow-Birch"))
								)
						)
				)
		);
	}

	/**
	 * Add a mock control map entry for BEC parse results with species "B1" and "B2"
	 * for Coastal and Interior Regions respectively
	 */
	public static void populateControlMap(Map<String, Object> controlMap) {
		populateControlMap(controlMap, "B1", "B2");
	}

	/**
	 * Add a mock control map entry for SP0 parse results. Alternates assigning to
	 * Coastal and Interior regions, starting with Coastal.
	 */
	public static void populateControlMap(Map<String, Object> controlMap, String... aliases) {

		List<BecDefinition> becs = new ArrayList<>();

		int i = 0;
		for (var alias : aliases) {
			becs.add(new BecDefinition(alias, Region.values()[i % 2], "Test " + alias, 2, 2, 2));
			i++;
		}

		controlMap.put(BecDefinitionParser.CONTROL_KEY, new BecLookup(becs));
	}

}
