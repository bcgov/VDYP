package ca.bc.gov.nrs.vdyp.io.parse.coe;

import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.hasBec;
import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.present;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collections;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import ca.bc.gov.nrs.vdyp.model.Region;
import ca.bc.gov.nrs.vdyp.test.TestUtils;

class BecDefinitionParserTest {

	@Test
	void testParse() throws Exception {
		var parser = new BecDefinitionParser();

		var result = parser.parse(TestUtils.class, "coe/Becdef.dat", Collections.emptyMap());

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
		assertThat(result.get("AT").get().getGrowthBec(), sameInstance(result.get("ESSF").get()));
		assertThat(result.get("AT").get().getDecayBec(), sameInstance(result.get("AT").get()));
		assertThat(result.get("AT").get().getVolumeBec(), sameInstance(result.get("AT").get()));
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
		assertThat(result.get("BG").get().getGrowthBec(), sameInstance(result.get("ESSF").get()));
		assertThat(result.get("BG").get().getDecayBec(), sameInstance(result.get("BG").get()));
		assertThat(result.get("BG").get().getVolumeBec(), sameInstance(result.get("ESSF").get()));
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
		assertThat(result.get("BWBS").get().getGrowthBec(), sameInstance(result.get("BWBS").get()));
		assertThat(result.get("BWBS").get().getDecayBec(), sameInstance(result.get("BWBS").get()));
		assertThat(result.get("BWBS").get().getVolumeBec(), sameInstance(result.get("BWBS").get()));
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
		assertThat(result.get("CDF").get().getGrowthBec(), sameInstance(result.get("CDF").get()));
		assertThat(result.get("CDF").get().getDecayBec(), sameInstance(result.get("CDF").get()));
		assertThat(result.get("CDF").get().getVolumeBec(), sameInstance(result.get("CDF").get()));
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
		assertThat(result.get("CWH").get().getGrowthBec(), sameInstance(result.get("CWH").get()));
		assertThat(result.get("CWH").get().getDecayBec(), sameInstance(result.get("CWH").get()));
		assertThat(result.get("CWH").get().getVolumeBec(), sameInstance(result.get("CWH").get()));
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
		assertThat(result.get("ESSF").get().getGrowthBec(), sameInstance(result.get("ESSF").get()));
		assertThat(result.get("ESSF").get().getDecayBec(), sameInstance(result.get("ESSF").get()));
		assertThat(result.get("ESSF").get().getVolumeBec(), sameInstance(result.get("ESSF").get()));
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
		assertThat(result.get("ICH").get().getGrowthBec(), sameInstance(result.get("ICH").get()));
		assertThat(result.get("ICH").get().getDecayBec(), sameInstance(result.get("ICH").get()));
		assertThat(result.get("ICH").get().getVolumeBec(), sameInstance(result.get("ICH").get()));
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
		assertThat(result.get("IDF").get().getGrowthBec(), sameInstance(result.get("IDF").get()));
		assertThat(result.get("IDF").get().getDecayBec(), sameInstance(result.get("IDF").get()));
		assertThat(result.get("IDF").get().getVolumeBec(), sameInstance(result.get("IDF").get()));
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
		assertThat(result.get("MH").get().getGrowthBec(), sameInstance(result.get("MH").get()));
		assertThat(result.get("MH").get().getDecayBec(), sameInstance(result.get("MH").get()));
		assertThat(result.get("MH").get().getVolumeBec(), sameInstance(result.get("MH").get()));
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
		assertThat(result.get("MS").get().getGrowthBec(), sameInstance(result.get("MS").get()));
		assertThat(result.get("MS").get().getDecayBec(), sameInstance(result.get("MS").get()));
		assertThat(result.get("MS").get().getVolumeBec(), sameInstance(result.get("MS").get()));
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
		assertThat(result.get("PP").get().getGrowthBec(), sameInstance(result.get("PP").get()));
		assertThat(result.get("PP").get().getDecayBec(), sameInstance(result.get("PP").get()));
		assertThat(result.get("PP").get().getVolumeBec(), sameInstance(result.get("PP").get()));
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
		assertThat(result.get("SBPS").get().getGrowthBec(), sameInstance(result.get("SBPS").get()));
		assertThat(result.get("SBPS").get().getDecayBec(), sameInstance(result.get("SBPS").get()));
		assertThat(result.get("SBPS").get().getVolumeBec(), sameInstance(result.get("SBPS").get()));
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
		assertThat(result.get("SBS").get().getGrowthBec(), sameInstance(result.get("SBS").get()));
		assertThat(result.get("SBS").get().getDecayBec(), sameInstance(result.get("SBS").get()));
		assertThat(result.get("SBS").get().getVolumeBec(), sameInstance(result.get("SBS").get()));
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
		assertThat(result.get("SWB").get().getGrowthBec(), sameInstance(result.get("SWB").get()));
		assertThat(result.get("SWB").get().getDecayBec(), sameInstance(result.get("SWB").get()));
		assertThat(result.get("SWB").get().getVolumeBec(), sameInstance(result.get("SWB").get()));
	}

	@Test
	void testParseNoDefault() throws Exception {
		var parser = new BecDefinitionParser();
		try (var is = TestUtils.makeInputStream("")) {
			var ex = assertThrows(IllegalStateException.class, () -> parser.parse(is, Collections.emptyMap()));
			assertThat(ex, hasProperty("message", Matchers.is("Could not find default BEC ESSF")));
		}
	}

}
