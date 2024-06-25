package ca.bc.gov.nrs.vdyp.vri;

import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.closeTo;
import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.isPolyId;
import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.notPresent;
import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.present;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;
import org.junit.jupiter.params.provider.ValueSource;

import ca.bc.gov.nrs.vdyp.application.ApplicationTestUtils;
import ca.bc.gov.nrs.vdyp.application.StandProcessingException;
import ca.bc.gov.nrs.vdyp.application.VdypApplicationIdentifier;
import ca.bc.gov.nrs.vdyp.application.VdypStartApplication;
import ca.bc.gov.nrs.vdyp.common.ControlKey;
import ca.bc.gov.nrs.vdyp.common.Utils;
import ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexEquation;
import ca.bc.gov.nrs.vdyp.io.parse.coe.BasalAreaYieldParser;
import ca.bc.gov.nrs.vdyp.io.parse.coe.BaseAreaCoefficientParser;
import ca.bc.gov.nrs.vdyp.io.parse.coe.HLNonprimaryCoefficientParser;
import ca.bc.gov.nrs.vdyp.io.parse.coe.HLPrimarySpeciesEqnP1Parser;
import ca.bc.gov.nrs.vdyp.io.parse.coe.HLPrimarySpeciesEqnP2Parser;
import ca.bc.gov.nrs.vdyp.io.parse.coe.HLPrimarySpeciesEqnP3Parser;
import ca.bc.gov.nrs.vdyp.io.parse.coe.ModifierParser;
import ca.bc.gov.nrs.vdyp.io.parse.coe.QuadMeanDiameterCoefficientParser;
import ca.bc.gov.nrs.vdyp.io.parse.coe.QuadraticMeanDiameterYieldParser;
import ca.bc.gov.nrs.vdyp.io.parse.coe.SiteCurveParser;
import ca.bc.gov.nrs.vdyp.io.parse.coe.UpperBoundsParser;
import ca.bc.gov.nrs.vdyp.io.parse.coe.UpperCoefficientParser;
import ca.bc.gov.nrs.vdyp.io.parse.streaming.MockStreamingParser;
import ca.bc.gov.nrs.vdyp.io.parse.streaming.StreamingParser;
import ca.bc.gov.nrs.vdyp.io.parse.streaming.StreamingParserFactory;
import ca.bc.gov.nrs.vdyp.model.BecDefinition;
import ca.bc.gov.nrs.vdyp.model.BecLookup;
import ca.bc.gov.nrs.vdyp.model.Coefficients;
import ca.bc.gov.nrs.vdyp.model.LayerType;
import ca.bc.gov.nrs.vdyp.model.MatrixMap2Impl;
import ca.bc.gov.nrs.vdyp.model.PolygonMode;
import ca.bc.gov.nrs.vdyp.model.Region;
import ca.bc.gov.nrs.vdyp.test.MockFileResolver;
import ca.bc.gov.nrs.vdyp.test.TestUtils;
import ca.bc.gov.nrs.vdyp.test.VdypMatchers;
import ca.bc.gov.nrs.vdyp.vri.model.VriLayer;
import ca.bc.gov.nrs.vdyp.vri.model.VriPolygon;
import ca.bc.gov.nrs.vdyp.vri.model.VriSite;
import ca.bc.gov.nrs.vdyp.vri.model.VriSpecies;
import ca.bc.gov.nrs.vdyp.vri.test.VriTestUtils;

class VriStartTest {

	Map<String, Object> controlMap = new HashMap<>();

	ByteArrayOutputStream polyOut;
	ByteArrayOutputStream specOut;
	ByteArrayOutputStream utilOut;

	private MockFileResolver dummyInput() {
		controlMap.put(ControlKey.VRI_OUTPUT_VDYP_POLYGON.name(), "DUMMY1");
		controlMap.put(ControlKey.VRI_OUTPUT_VDYP_LAYER_BY_SPECIES.name(), "DUMMY2");
		controlMap.put(ControlKey.VRI_OUTPUT_VDYP_LAYER_BY_SP0_BY_UTIL.name(), "DUMMY3");

		MockFileResolver resolver = new MockFileResolver("Test");

		polyOut = new ByteArrayOutputStream();
		specOut = new ByteArrayOutputStream();
		utilOut = new ByteArrayOutputStream();

		resolver.addStream("DUMMY1", (OutputStream) polyOut);
		resolver.addStream("DUMMY2", (OutputStream) specOut);
		resolver.addStream("DUMMY3", (OutputStream) utilOut);
		return resolver;
	}

	@Nested
	class EstimateBaseAreaYield {
		@SuppressWarnings("resource")
		@Test
		void testCompute() throws StandProcessingException {
			Map<String, Object> controlMap = VriTestUtils.loadControlMap();
			VriStart app = new VriStart();
			ApplicationTestUtils.setControlMap(app, controlMap);

			var polygon = VriPolygon.build(pBuilder -> {
				pBuilder.polygonIdentifier("Test", 2024);
				pBuilder.biogeoclimaticZone("IDF");
				pBuilder.yieldFactor(1.0f);
				pBuilder.addLayer(lBuilder -> {
					lBuilder.layerType(LayerType.PRIMARY);
					lBuilder.crownClosure(57.8f);
					lBuilder.baseArea(66f);
					lBuilder.treesPerHectare(850f);
					lBuilder.utilization(7.5f);
					lBuilder.empiricalRelationshipParameterIndex(76);

					lBuilder.addSpecies(sBuilder -> {
						sBuilder.genus("B"); // 3
						sBuilder.percentGenus(2.99999993f);
					});
					lBuilder.addSpecies(sBuilder -> {
						sBuilder.genus("C"); // 4
						sBuilder.percentGenus(30.0000012f);
					});
					lBuilder.addSpecies(sBuilder -> {
						sBuilder.genus("H"); // 8
						sBuilder.percentGenus(48.9000022f);
					});
					lBuilder.addSpecies(sBuilder -> {
						sBuilder.genus("S"); // 15
						sBuilder.percentGenus(18.1000009f);
					});
				});
			});

			var species = polygon.getLayers().get(LayerType.PRIMARY).getSpecies().values();

			var bec = Utils.expectParsedControl(controlMap, ControlKey.BEC_DEF, BecLookup.class).get("IDF").get();

			float result = app.estimateBaseAreaYield(32f, 190.300003f, Optional.empty(), false, species, bec, 76);

			assertThat(result, closeTo(62.0858421f));
		}

		@SuppressWarnings("resource")
		@Test
		void testGetCoefficients() throws StandProcessingException {
			Map<String, Object> controlMap = VriTestUtils.loadControlMap();
			VriStart app = new VriStart();
			ApplicationTestUtils.setControlMap(app, controlMap);

			var polygon = VriPolygon.build(pBuilder -> {
				pBuilder.polygonIdentifier("Test", 2024);
				pBuilder.biogeoclimaticZone("IDF");
				pBuilder.yieldFactor(1.0f);
				pBuilder.addLayer(lBuilder -> {
					lBuilder.layerType(LayerType.PRIMARY);
					lBuilder.crownClosure(57.8f);
					lBuilder.baseArea(66f);
					lBuilder.treesPerHectare(850f);
					lBuilder.utilization(7.5f);
					lBuilder.empiricalRelationshipParameterIndex(76);

					lBuilder.addSpecies(sBuilder -> {
						sBuilder.genus("B"); // 3
						sBuilder.percentGenus(2.99999993f);
					});
					lBuilder.addSpecies(sBuilder -> {
						sBuilder.genus("C"); // 4
						sBuilder.percentGenus(30.0000012f);
					});
					lBuilder.addSpecies(sBuilder -> {
						sBuilder.genus("H"); // 8
						sBuilder.percentGenus(48.9000022f);
					});
					lBuilder.addSpecies(sBuilder -> {
						sBuilder.genus("S"); // 15
						sBuilder.percentGenus(18.1000009f);
					});
				});
			});

			var species = polygon.getLayers().get(LayerType.PRIMARY).getSpecies().values();

			var bec = Utils.expectParsedControl(controlMap, ControlKey.BEC_DEF, BecLookup.class).get("IDF").get();

			Coefficients result = app.estimateBaseAreaYieldCoefficients(species, bec);

			assertThat(
					result,
					VdypMatchers.coe(
							0, 7.29882717f, 0.934803009f, 7.22950029f, 0.478330702f, 0.00542420009f, 0f, -0.00899999961f
					)
			);
		}
	}

	@Nested
	class FindDefaultMode {

		@Test
		void testLowBA() throws Exception {
			var app = new VriStart();

			MockFileResolver resolver = dummyInput();

			controlMap.put(ControlKey.MINIMA.name(), Utils.constMap(map -> {
				map.put(VriControlParser.MINIMUM_BASE_AREA, 0f);
				map.put(VriControlParser.MINIMUM_HEIGHT, 6f);
				map.put(VriControlParser.MINIMUM_PREDICTED_BASE_AREA, 14f); // Set this high for test
			}));

			var baYieldMap = new MatrixMap2Impl<>(
					List.of("IDF"), List.of("B"),
					(k1, k2) -> new Coefficients(
							new float[] { -2.9907f + 11.2376f, 2.4562f - 3.2149f, 7.2295f + 0.0000f, 0.5074f + 0.3763f,
									-0.0150f + 0.030f, 0.0120f + -0.0364f, -0.0090f + 0.0000f },
							0
					)
			);
			controlMap.put(ControlKey.BA_YIELD.name(), baYieldMap);

			controlMap.put(ControlKey.BA_DQ_UPPER_BOUNDS.name(), Utils.constMap(map -> {
				map.put(76, new Coefficients(new float[] { 89.69f, 53.60f }, 1));
			}));

			app.init(resolver, controlMap);

			Optional<Float> ageTotal = Optional.of(200f);
			Optional<Float> yearsToBreastHeight = Optional.of(191f);
			Optional<Float> height = Optional.of(10f);
			Optional<Float> baseArea = Optional.of(30f);
			Optional<Float> treesPerHectare = Optional.of(300f);
			Optional<Float> percentForest = Optional.of(90f);

			Collection<VriSpecies> species = List.of(VriSpecies.build(builder -> {
				builder.polygonIdentifier("Test", 2024);
				builder.layerType(LayerType.PRIMARY);
				builder.genus("B");
				builder.percentGenus(100f);
			}));
			var bec = new BecDefinition("IDF", Region.INTERIOR, "Interior Douglas Fir");

			var result = app.findDefaultPolygonMode(
					ageTotal, yearsToBreastHeight, height, baseArea, treesPerHectare, percentForest, species, bec,
					Optional.of(76)
			);

			assertThat(result, is(PolygonMode.YOUNG));

			app.close();
		}

		@Test
		void testLowHeight() throws Exception {
			var app = new VriStart();

			MockFileResolver resolver = dummyInput();

			controlMap.put(ControlKey.MINIMA.name(), Utils.constMap(map -> {
				map.put(VriControlParser.MINIMUM_BASE_AREA, 0f);
				map.put(VriControlParser.MINIMUM_HEIGHT, 6f);
				map.put(VriControlParser.MINIMUM_PREDICTED_BASE_AREA, 2f);
			}));

			var baYieldMap = new MatrixMap2Impl<>(
					List.of("IDF"), List.of("B"),
					(k1, k2) -> new Coefficients(
							new float[] { -2.9907f + 11.2376f, 2.4562f - 3.2149f, 7.2295f + 0.0000f, 0.5074f + 0.3763f,
									-0.0150f + 0.030f, 0.0120f + -0.0364f, -0.0090f + 0.0000f },
							0
					)
			);
			controlMap.put(ControlKey.BA_YIELD.name(), baYieldMap);

			controlMap.put(ControlKey.BA_DQ_UPPER_BOUNDS.name(), Utils.constMap(map -> {
				map.put(76, new Coefficients(new float[] { 89.69f, 53.60f }, 1));
			}));

			app.init(resolver, controlMap);

			Optional<Float> ageTotal = Optional.of(200f);
			Optional<Float> yearsToBreastHeight = Optional.of(189f);
			Optional<Float> height = Optional.of(1f); // Set this low for test.
			Optional<Float> baseArea = Optional.of(30f);
			Optional<Float> treesPerHectare = Optional.of(300f);
			Optional<Float> percentForest = Optional.of(90f);

			Collection<VriSpecies> species = List.of(VriSpecies.build(builder -> {
				builder.polygonIdentifier("Test", 2024);
				builder.layerType(LayerType.PRIMARY);
				builder.genus("B");
				builder.percentGenus(100f);
			}));
			var bec = new BecDefinition("IDF", Region.INTERIOR, "Interior Douglas Fir");

			var result = app.findDefaultPolygonMode(
					ageTotal, yearsToBreastHeight, height, baseArea, treesPerHectare, percentForest, species, bec,
					Optional.of(76)
			);

			assertThat(result, is(PolygonMode.YOUNG));

			app.close();
		}

		@Test
		void testNoBA() throws Exception {
			var app = new VriStart();

			MockFileResolver resolver = dummyInput();

			controlMap.put(ControlKey.MINIMA.name(), Utils.constMap(map -> {
				map.put(VriControlParser.MINIMUM_BASE_AREA, 0f);
				map.put(VriControlParser.MINIMUM_HEIGHT, 6f);
				map.put(VriControlParser.MINIMUM_PREDICTED_BASE_AREA, 2f);
			}));

			var baYieldMap = new MatrixMap2Impl<>(
					List.of("IDF"), List.of("B"),
					(k1, k2) -> new Coefficients(
							new float[] { -2.9907f + 11.2376f, 2.4562f - 3.2149f, 7.2295f + 0.0000f, 0.5074f + 0.3763f,
									-0.0150f + 0.030f, 0.0120f + -0.0364f, -0.0090f + 0.0000f },
							0
					)
			);
			controlMap.put(ControlKey.BA_YIELD.name(), baYieldMap);

			controlMap.put(ControlKey.BA_DQ_UPPER_BOUNDS.name(), Utils.constMap(map -> {
				map.put(76, new Coefficients(new float[] { 89.69f, 53.60f }, 1));
			}));

			app.init(resolver, controlMap);

			Optional<Float> ageTotal = Optional.of(200f);
			Optional<Float> yearsToBreastHeight = Optional.of(189f);
			Optional<Float> height = Optional.of(20f);
			Optional<Float> baseArea = Optional.empty(); // Null this for the test
			Optional<Float> treesPerHectare = Optional.of(300f);
			Optional<Float> percentForest = Optional.of(90f);

			Collection<VriSpecies> species = List.of(VriSpecies.build(builder -> {
				builder.polygonIdentifier("Test", 2024);
				builder.layerType(LayerType.PRIMARY);
				builder.genus("B");
				builder.percentGenus(100f);
			}));
			var bec = new BecDefinition("IDF", Region.INTERIOR, "Interior Douglas Fir");

			var result = app.findDefaultPolygonMode(
					ageTotal, yearsToBreastHeight, height, baseArea, treesPerHectare, percentForest, species, bec,
					Optional.of(76)
			);

			assertThat(result, is(PolygonMode.YOUNG));

			app.close();
		}

		@Test
		void testNoTPH() throws Exception {
			var app = new VriStart();

			MockFileResolver resolver = dummyInput();

			controlMap.put(ControlKey.MINIMA.name(), Utils.constMap(map -> {
				map.put(VriControlParser.MINIMUM_BASE_AREA, 0f);
				map.put(VriControlParser.MINIMUM_HEIGHT, 6f);
				map.put(VriControlParser.MINIMUM_PREDICTED_BASE_AREA, 2f);
			}));

			var baYieldMap = new MatrixMap2Impl<>(
					List.of("IDF"), List.of("B"),
					(k1, k2) -> new Coefficients(
							new float[] { -2.9907f + 11.2376f, 2.4562f - 3.2149f, 7.2295f + 0.0000f, 0.5074f + 0.3763f,
									-0.0150f + 0.030f, 0.0120f + -0.0364f, -0.0090f + 0.0000f },
							0
					)
			);
			controlMap.put(ControlKey.BA_YIELD.name(), baYieldMap);

			controlMap.put(ControlKey.BA_DQ_UPPER_BOUNDS.name(), Utils.constMap(map -> {
				map.put(76, new Coefficients(new float[] { 89.69f, 53.60f }, 1));
			}));

			app.init(resolver, controlMap);

			Optional<Float> ageTotal = Optional.of(200f);
			Optional<Float> yearsToBreastHeight = Optional.of(189f);
			Optional<Float> height = Optional.of(20f);
			Optional<Float> baseArea = Optional.of(30f);
			Optional<Float> treesPerHectare = Optional.empty(); // Null this for the test
			Optional<Float> percentForest = Optional.of(90f);

			Collection<VriSpecies> species = List.of(VriSpecies.build(builder -> {
				builder.polygonIdentifier("Test", 2024);
				builder.layerType(LayerType.PRIMARY);
				builder.genus("B");
				builder.percentGenus(100f);
			}));
			var bec = new BecDefinition("IDF", Region.INTERIOR, "Interior Douglas Fir");

			var result = app.findDefaultPolygonMode(
					ageTotal, yearsToBreastHeight, height, baseArea, treesPerHectare, percentForest, species, bec,
					Optional.of(76)
			);

			assertThat(result, is(PolygonMode.YOUNG));

			app.close();
		}

		@Test
		void testLowRation() throws Exception {
			var app = new VriStart();

			MockFileResolver resolver = dummyInput();

			controlMap.put(ControlKey.MINIMA.name(), Utils.constMap(map -> {
				map.put(VriControlParser.MINIMUM_BASE_AREA, 13f);// Set this high
				map.put(VriControlParser.MINIMUM_HEIGHT, 0f);
				map.put(VriControlParser.MINIMUM_PREDICTED_BASE_AREA, 2f);
			}));

			var baYieldMap = new MatrixMap2Impl<>(
					List.of("IDF"), List.of("B"),
					(k1, k2) -> new Coefficients(
							new float[] { -2.9907f + 11.2376f, 2.4562f - 3.2149f, 7.2295f + 0.0000f, 0.5074f + 0.3763f,
									-0.0150f + 0.030f, 0.0120f + -0.0364f, -0.0090f + 0.0000f },
							0
					)
			);
			controlMap.put(ControlKey.BA_YIELD.name(), baYieldMap);

			controlMap.put(ControlKey.BA_DQ_UPPER_BOUNDS.name(), Utils.constMap(map -> {
				map.put(76, new Coefficients(new float[] { 89.69f, 53.60f }, 1));
			}));

			app.init(resolver, controlMap);

			Optional<Float> ageTotal = Optional.of(200f);
			Optional<Float> yearsToBreastHeight = Optional.of(189f);
			Optional<Float> height = Optional.of(20f);
			Optional<Float> baseArea = Optional.of(10f); // Set this low
			Optional<Float> treesPerHectare = Optional.of(300f);
			Optional<Float> percentForest = Optional.of(95f); // Set this high

			Collection<VriSpecies> species = List.of(VriSpecies.build(builder -> {
				builder.polygonIdentifier("Test", 2024);
				builder.layerType(LayerType.PRIMARY);
				builder.genus("B");
				builder.percentGenus(100f);
			}));
			var bec = new BecDefinition("IDF", Region.INTERIOR, "Interior Douglas Fir");

			var result = app.findDefaultPolygonMode(
					ageTotal, yearsToBreastHeight, height, baseArea, treesPerHectare, percentForest, species, bec,
					Optional.of(76)
			);

			assertThat(result, is(PolygonMode.YOUNG));

			app.close();
		}

		@Test
		void testStart() throws Exception {
			var app = new VriStart();

			MockFileResolver resolver = dummyInput();

			controlMap.put(ControlKey.MINIMA.name(), Utils.constMap(map -> {
				map.put(VriControlParser.MINIMUM_BASE_AREA, 0f);
				map.put(VriControlParser.MINIMUM_HEIGHT, 0f);
				map.put(VriControlParser.MINIMUM_PREDICTED_BASE_AREA, 2f);
			}));

			var baYieldMap = new MatrixMap2Impl<>(
					List.of("IDF"), List.of("B"),
					(k1, k2) -> new Coefficients(
							new float[] { -2.9907f + 11.2376f, 2.4562f - 3.2149f, 7.2295f + 0.0000f, 0.5074f + 0.3763f,
									-0.0150f + 0.030f, 0.0120f + -0.0364f, -0.0090f + 0.0000f },
							0
					)
			);
			controlMap.put(ControlKey.BA_YIELD.name(), baYieldMap);

			controlMap.put(ControlKey.BA_DQ_UPPER_BOUNDS.name(), Utils.constMap(map -> {
				map.put(76, new Coefficients(new float[] { 89.69f, 53.60f }, 1));
			}));

			app.init(resolver, controlMap);

			Optional<Float> ageTotal = Optional.of(200f);
			Optional<Float> yearsToBreastHeight = Optional.of(189f);
			Optional<Float> height = Optional.of(20f);
			Optional<Float> baseArea = Optional.of(30f);
			Optional<Float> treesPerHectare = Optional.of(300f);
			Optional<Float> percentForest = Optional.of(85f);

			Collection<VriSpecies> species = List.of(VriSpecies.build(builder -> {
				builder.polygonIdentifier("Test", 2024);
				builder.layerType(LayerType.PRIMARY);
				builder.genus("B");
				builder.percentGenus(100f);
			}));
			var bec = new BecDefinition("IDF", Region.INTERIOR, "Interior Douglas Fir");

			var result = app.findDefaultPolygonMode(
					ageTotal, yearsToBreastHeight, height, baseArea, treesPerHectare, percentForest, species, bec,
					Optional.of(76)
			);

			assertThat(result, is(PolygonMode.START));

			app.close();
		}
	}

	@Nested
	class Process {
		@ParameterizedTest
		@EnumSource(value = PolygonMode.class, names = { "START", "YOUNG", "BATC", "BATN" })
		void testDontSkip(PolygonMode mode) throws Exception {
			var control = EasyMock.createControl();

			VriStart app = EasyMock.createMockBuilder(VriStart.class) //
					.addMockedMethod("processYoung") //
					.addMockedMethod("processBatc") //
					.addMockedMethod("processBatn") //
					.addMockedMethod("checkPolygon") //
					.createMock(control);

			MockFileResolver resolver = dummyInput();

			var poly = VriPolygon.build(pb -> {
				pb.polygonIdentifier("TestPoly", 2024);
				pb.biogeoclimaticZone("IDF");
				pb.yieldFactor(1.0f);
				pb.mode(mode);
			});

			var polyYoung = VriPolygon.build(pb -> {
				pb.polygonIdentifier("TestPolyYoung", 2024);
				pb.biogeoclimaticZone("IDF");
				pb.yieldFactor(1.0f);
				pb.mode(mode);
			});
			var polyBatc = VriPolygon.build(pb -> {
				pb.polygonIdentifier("TestPolyBatc", 2024);
				pb.biogeoclimaticZone("IDF");
				pb.yieldFactor(1.0f);
				pb.mode(mode);
			});
			var polyBatn = VriPolygon.build(pb -> {
				pb.polygonIdentifier("TestPolyBatn", 2024);
				pb.biogeoclimaticZone("IDF");
				pb.yieldFactor(1.0f);
				pb.mode(mode);
			});

			EasyMock.expect(app.checkPolygon(poly)).andReturn(mode).once();
			EasyMock.expect(app.processYoung(poly)).andReturn(polyYoung).times(0, 1);
			EasyMock.expect(app.processBatc(poly)).andReturn(polyBatc).times(0, 1);
			EasyMock.expect(app.processBatn(poly)).andReturn(polyBatn).times(0, 1);

			control.replay();

			app.init(resolver, controlMap);

			var result = app.processPolygon(0, poly);

			app.close();

			control.verify();
		}

		@ParameterizedTest
		@EnumSource(value = PolygonMode.class, mode = Mode.EXCLUDE, names = { "START", "YOUNG", "BATC", "BATN" })
		void testDoSkip(PolygonMode mode) throws Exception {
			var control = EasyMock.createControl();

			VriStart app = EasyMock.createMockBuilder(VriStart.class).addMockedMethod("checkPolygon")
					.createMock(control);

			MockFileResolver resolver = dummyInput();

			var poly = VriPolygon.build(pb -> {
				pb.polygonIdentifier("TestPoly", 2024);
				pb.biogeoclimaticZone("IDF");
				pb.yieldFactor(1.0f);
				pb.mode(mode);
			});

			// expect no calls

			control.replay();

			app.init(resolver, controlMap);

			var result = app.processPolygon(0, poly);

			assertThat(result, notPresent());

			app.close();

			control.verify();
		}

		@Test
		void testProcessEmpty() throws Exception {
			var control = EasyMock.createControl();

			VdypStartApplication<VriPolygon, VriLayer, VriSpecies, VriSite> app = EasyMock
					.createMockBuilder(VriStart.class)//
					.addMockedMethod("getVriWriter") //
					.addMockedMethod("checkPolygon") //
					.addMockedMethod("getPolygon") //
					.createMock(control);

			MockFileResolver resolver = dummyInput();

			StreamingParser<VriPolygon> polyStream = easyMockInputStreamFactory(
					controlMap, ControlKey.VRI_INPUT_YIELD_POLY, control
			);
			StreamingParser<Map<LayerType, VriLayer.Builder>> layerStream = easyMockInputStreamFactory(
					controlMap, ControlKey.VRI_INPUT_YIELD_LAYER, control
			);
			StreamingParser<Collection<VriSpecies>> specStream = easyMockInputStreamFactory(
					controlMap, ControlKey.VRI_INPUT_YIELD_SPEC_DIST, control
			);
			StreamingParser<Collection<VriSite>> siteStream = easyMockInputStreamFactory(
					controlMap, ControlKey.VRI_INPUT_YIELD_HEIGHT_AGE_SI, control
			);

			EasyMock.expect(polyStream.hasNext()).andReturn(false);

			// Expect no other calls

			polyStream.close();
			EasyMock.expectLastCall();
			layerStream.close();
			EasyMock.expectLastCall();
			specStream.close();
			EasyMock.expectLastCall();
			siteStream.close();
			EasyMock.expectLastCall();

			control.replay();

			app.init(resolver, controlMap);

			app.process();

			app.close();

			control.verify();
		}

		@Test
		void testProcessIgnored() throws Exception {
			var control = EasyMock.createControl();

			VriStart app = EasyMock.createMockBuilder(VriStart.class)//
					.addMockedMethod("getVriWriter") //
					.addMockedMethod("checkPolygon") //
					.addMockedMethod("getPolygon") //
					.createMock(control);

			MockFileResolver resolver = dummyInput();

			var poly = VriPolygon.build(pb -> {
				pb.polygonIdentifier("TestPoly", 2024);
				pb.biogeoclimaticZone("IDF");
				pb.yieldFactor(1.0f);
				pb.mode(PolygonMode.DONT_PROCESS);
			});

			StreamingParser<VriPolygon> polyStream = easyMockInputStreamFactory(
					controlMap, ControlKey.VRI_INPUT_YIELD_POLY, control
			);
			StreamingParser<Map<LayerType, VriLayer.Builder>> layerStream = easyMockInputStreamFactory(
					controlMap, ControlKey.VRI_INPUT_YIELD_LAYER, control
			);
			StreamingParser<Collection<VriSpecies>> specStream = easyMockInputStreamFactory(
					controlMap, ControlKey.VRI_INPUT_YIELD_SPEC_DIST, control
			);
			StreamingParser<Collection<VriSite>> siteStream = easyMockInputStreamFactory(
					controlMap, ControlKey.VRI_INPUT_YIELD_HEIGHT_AGE_SI, control
			);

			EasyMock.expect(polyStream.hasNext()).andReturn(true);
			EasyMock.expect(app.getPolygon(polyStream, layerStream, specStream, siteStream)).andReturn(poly);
			EasyMock.expect(polyStream.hasNext()).andReturn(false);

			// Expect no other calls

			polyStream.close();
			EasyMock.expectLastCall();
			layerStream.close();
			EasyMock.expectLastCall();
			specStream.close();
			EasyMock.expectLastCall();
			siteStream.close();
			EasyMock.expectLastCall();

			control.replay();

			app.init(resolver, controlMap);

			app.process();

			app.close();

			control.verify();
		}

	}

	<T> void mockInputStreamFactory(
			Map<String, Object> controlMap, ControlKey key, StreamingParser<T> stream, IMocksControl control
	) throws IOException {
		StreamingParserFactory<T> factory = control.createMock("factory_" + key.name(), StreamingParserFactory.class);

		EasyMock.expect(factory.get()).andReturn(stream);

		controlMap.put(key.name(), factory);
	}

	<T> StreamingParser<T>
			easyMockInputStreamFactory(Map<String, Object> controlMap, ControlKey key, IMocksControl control)
					throws IOException {
		StreamingParser<T> stream = control.createMock("stream_" + key.name(), StreamingParser.class);
		mockInputStreamFactory(controlMap, key, stream, control);
		return stream;
	}

	<T> MockStreamingParser<T>
			fillableMockInputStreamFactory(Map<String, Object> controlMap, ControlKey key, IMocksControl control)
					throws IOException {
		MockStreamingParser<T> stream = new MockStreamingParser<>();
		mockInputStreamFactory(controlMap, key, stream, control);
		return stream;
	}

	@Test
	void testFindSiteCurveNumber() throws Exception {
		var control = EasyMock.createControl();

		VriStart app = new VriStart();

		MockFileResolver resolver = dummyInput();

		TestUtils.populateControlMapGenusReal(controlMap);
		TestUtils.populateControlMapFromResource(controlMap, new SiteCurveParser(), "SIEQN.PRM");

		control.replay();

		app.init(resolver, controlMap);

		assertThat(app.findSiteCurveNumber(Region.COASTAL, "MB"), is(SiteIndexEquation.getByIndex(10)));
		assertThat(app.findSiteCurveNumber(Region.INTERIOR, "MB"), is(SiteIndexEquation.getByIndex(10)));

		assertThat(app.findSiteCurveNumber(Region.COASTAL, "B"), is(SiteIndexEquation.getByIndex(12)));
		assertThat(app.findSiteCurveNumber(Region.INTERIOR, "B"), is(SiteIndexEquation.getByIndex(42)));

		assertThat(app.findSiteCurveNumber(Region.COASTAL, "ZZZ", "B"), is(SiteIndexEquation.getByIndex(12)));
		assertThat(app.findSiteCurveNumber(Region.INTERIOR, "ZZZ", "B"), is(SiteIndexEquation.getByIndex(42)));

		assertThat(app.findSiteCurveNumber(Region.COASTAL, "YYY", "B"), is(SiteIndexEquation.getByIndex(42)));
		assertThat(app.findSiteCurveNumber(Region.INTERIOR, "YYY", "B"), is(SiteIndexEquation.getByIndex(06)));

		assertThrows(StandProcessingException.class, () -> app.findSiteCurveNumber(Region.COASTAL, "ZZZ"));
		assertThrows(StandProcessingException.class, () -> app.findSiteCurveNumber(Region.INTERIOR, "ZZZ"));

		app.close();

		control.verify();
	}

	@Nested
	class ProcessYoung {

		@Test
		void testBasic() throws Exception {
			var control = EasyMock.createControl();

			VriStart app = EasyMock.createMockBuilder(VriStart.class).createMock(control);

			MockFileResolver resolver = dummyInput();

			TestUtils.populateControlMapGenusReal(controlMap);
			TestUtils.populateControlMapBecReal(controlMap);
			controlMap.put(ControlKey.MINIMA.name(), Utils.constMap(map -> {
				map.put(VriControlParser.MINIMUM_BASE_AREA, 0f);
				map.put(VriControlParser.MINIMUM_HEIGHT, 6f);
				map.put(VriControlParser.MINIMUM_PREDICTED_BASE_AREA, 2f);
			}));
			TestUtils.populateControlMapFromResource(controlMap, new BasalAreaYieldParser(), "YLDBA407.COE");
			TestUtils.populateControlMapFromResource(controlMap, new UpperBoundsParser(), "PCT_407.coe");

			var poly = VriPolygon.build(pb -> {
				pb.polygonIdentifier("082F074/0142", 1997);
				pb.forestInventoryZone(" ");
				pb.biogeoclimaticZone("IDF");
				pb.yieldFactor(1.0f);
				pb.mode(PolygonMode.YOUNG);

				pb.addLayer(lb -> {
					lb.layerType(LayerType.PRIMARY);
					lb.baseArea(Optional.empty());
					lb.treesPerHectare(Optional.empty());
					lb.primaryGenus("F");
					lb.inventoryTypeGroup(3);
					lb.crownClosure(30);
					lb.utilization(7.5f);

					lb.empiricalRelationshipParameterIndex(61);

					lb.addSpecies(spb -> {
						spb.genus("B");
						spb.percentGenus(10);
						spb.addSpecies("BL", 100);
					});
					lb.addSpecies(spb -> {
						spb.genus("C");
						spb.percentGenus(20);
						spb.addSpecies("CW", 100);
					});
					lb.addSpecies(spb -> {
						spb.genus("F");
						spb.percentGenus(30);
						spb.addSpecies("FD", 100);
					});
					lb.addSpecies(spb -> {
						spb.genus("H");
						spb.percentGenus(30);
						spb.addSpecies("HW", 100);
					});
					lb.addSpecies(spb -> {
						spb.genus("S");
						spb.percentGenus(10);
						spb.addSpecies("S", 100);
					});

					lb.addSite(sib -> {
						sib.siteGenus("B");
						sib.siteSpecies("BL");
					});
					lb.addSite(sib -> {
						sib.siteGenus("C");
						sib.siteCurveNumber(11);
						sib.siteSpecies("CW");
					});
					lb.addSite(sib -> {
						sib.siteGenus("F");
						sib.siteSpecies("FD");
						sib.siteCurveNumber(23);
						sib.siteIndex(19.7f);
						sib.height(7.6f);
						sib.yearsToBreastHeight(9);
						sib.breastHeightAge(15);
						sib.ageTotal(24);
					});
					lb.addSite(sib -> {
						sib.siteGenus("H");
						sib.siteSpecies("HW");
						sib.siteCurveNumber(37);
					});
					lb.addSite(sib -> {
						sib.siteGenus("S");
						sib.siteSpecies("S");
						sib.siteCurveNumber(71);
					});
				});
			});

			control.replay();

			app.init(resolver, controlMap);

			// Run the process

			var result = assertDoesNotThrow(() -> app.processYoung(poly));

			// Assertions

			final var forPolygon = hasProperty("polygonIdentifier", isPolyId("082F074/0142", 1997));
			final var forPrimeLayer = both(forPolygon).and(hasProperty("layerType", is(LayerType.PRIMARY)));

			assertThat(result, forPolygon);
			assertThat(result, hasProperty("mode", present(is(PolygonMode.BATN))));

			assertThat(
					result, hasProperty("layers", allOf(aMapWithSize(1), hasEntry(is(LayerType.PRIMARY), anything())))
			);
			var resultPrimaryLayer = result.getLayers().get(LayerType.PRIMARY);

			assertThat(resultPrimaryLayer, forPrimeLayer);

			assertThat(
					resultPrimaryLayer, hasProperty(
							"sites", allOf(
									aMapWithSize(5), //
									hasSite(
											is("B"), is("BL"),
											forPrimeLayer.and(hasProperty("siteCurveNumber", notPresent()))
									), //
									hasSite(
											is("C"), is("CW"),
											forPrimeLayer.and(hasProperty("siteCurveNumber", present(is(11))))
									), //
									hasSite(
											is("F"), is("FD"),
											forPrimeLayer.and(hasProperty("siteCurveNumber", present(is(23))))
									), //
									hasSite(
											is("H"), is("HW"),
											forPrimeLayer.and(hasProperty("siteCurveNumber", present(is(37))))
									), //
									hasSite(
											is("S"), is("S"),
											forPrimeLayer.and(hasProperty("siteCurveNumber", present(is(71))))
									)
							)
					)
			);

			assertThat(
					resultPrimaryLayer, hasProperty(
							"species", allOf(
									aMapWithSize(5), //
									hasSpecies(is("B"), is("BL"), closeTo(10), forPrimeLayer), //
									hasSpecies(is("C"), is("CW"), closeTo(20), forPrimeLayer), //
									hasSpecies(is("F"), is("FD"), closeTo(30), forPrimeLayer), //
									hasSpecies(is("H"), is("HW"), closeTo(30), forPrimeLayer), //
									hasSpecies(is("S"), is("S"), closeTo(10), forPrimeLayer)
							)
					)
			);

			for (var nonPrimaryGenus : List.of("B", "C", "H", "S")) {
				var nonPrimarySite = resultPrimaryLayer.getSites().get(nonPrimaryGenus);
				assertThat(
						nonPrimarySite, allOf(
								hasProperty("siteIndex", notPresent()), //
								hasProperty("height", notPresent()), //
								hasProperty("ageTotal", notPresent()), //
								hasProperty("yearsToBreastHeight", notPresent()), //
								hasProperty("breastHeightAge", notPresent())
						)
				);
			}

			var primarySite = resultPrimaryLayer.getPrimarySite().get();

			assertThat(
					primarySite, allOf(
							hasProperty("siteGenus", is("F")), //
							hasProperty("siteIndex", present(closeTo(19.7f))), //
							hasProperty("height", present(closeTo(7.6f))), //
							hasProperty("ageTotal", present(closeTo(24f))), //
							hasProperty("yearsToBreastHeight", present(closeTo(9f))), //
							hasProperty("breastHeightAge", present(closeTo(15f)))
					)
			);

			app.close();

			control.verify();
		}

		@Test
		void testLowPercentAvailable() throws Exception {
			var control = EasyMock.createControl();

			VriStart app = EasyMock.createMockBuilder(VriStart.class).createMock(control);

			MockFileResolver resolver = dummyInput();

			TestUtils.populateControlMapGenusReal(controlMap);
			TestUtils.populateControlMapBecReal(controlMap);
			controlMap.put(ControlKey.MINIMA.name(), Utils.constMap(map -> {
				map.put(VriControlParser.MINIMUM_BASE_AREA, 0f);
				map.put(VriControlParser.MINIMUM_HEIGHT, 6f);
				map.put(VriControlParser.MINIMUM_PREDICTED_BASE_AREA, 10f); // Set this high
			}));
			TestUtils.populateControlMapFromResource(controlMap, new BasalAreaYieldParser(), "YLDBA407.COE");
			TestUtils.populateControlMapFromResource(controlMap, new UpperBoundsParser(), "PCT_407.coe");

			// Target BA should be 11.11111111 due to low PCTFLAND

			var poly = VriPolygon.build(pb -> {
				pb.polygonIdentifier("082F074/0142", 1997);
				pb.forestInventoryZone(" ");
				pb.biogeoclimaticZone("IDF");
				pb.yieldFactor(1.0f);
				pb.mode(PolygonMode.YOUNG);

				pb.percentAvailable(9); // Set this less than 10

				pb.addLayer(lb -> {
					lb.layerType(LayerType.PRIMARY);
					lb.baseArea(Optional.empty());
					lb.treesPerHectare(Optional.empty());
					lb.primaryGenus("F");
					lb.inventoryTypeGroup(3);
					lb.crownClosure(30);
					lb.utilization(7.5f);

					lb.empiricalRelationshipParameterIndex(61);

					lb.addSpecies(spb -> {
						spb.genus("B");
						spb.percentGenus(10);
						spb.addSpecies("BL", 100);
					});
					lb.addSpecies(spb -> {
						spb.genus("C");
						spb.percentGenus(20);
						spb.addSpecies("CW", 100);
					});
					lb.addSpecies(spb -> {
						spb.genus("F");
						spb.percentGenus(30);
						spb.addSpecies("FD", 100);
					});
					lb.addSpecies(spb -> {
						spb.genus("H");
						spb.percentGenus(30);
						spb.addSpecies("HW", 100);
					});
					lb.addSpecies(spb -> {
						spb.genus("S");
						spb.percentGenus(10);
						spb.addSpecies("S", 100);
					});

					lb.addSite(sib -> {
						sib.siteGenus("B");
						sib.siteSpecies("BL");
					});
					lb.addSite(sib -> {
						sib.siteGenus("C");
						sib.siteCurveNumber(11);
						sib.siteSpecies("CW");
					});
					lb.addSite(sib -> {
						sib.siteGenus("F");
						sib.siteSpecies("FD");
						sib.siteCurveNumber(23);
						sib.siteIndex(19.7f);
						sib.height(7.6f);
						sib.yearsToBreastHeight(9);
						sib.breastHeightAge(15);
						sib.ageTotal(24);
					});
					lb.addSite(sib -> {
						sib.siteGenus("H");
						sib.siteSpecies("HW");
						sib.siteCurveNumber(37);
					});
					lb.addSite(sib -> {
						sib.siteGenus("S");
						sib.siteSpecies("S");
						sib.siteCurveNumber(71);
					});
				});
			});

			control.replay();

			app.init(resolver, controlMap);

			// Run the process

			var result = assertDoesNotThrow(() -> app.processYoung(poly));

			// Assertions

			final var forPolygon = hasProperty("polygonIdentifier", isPolyId("082F074/0142", 1999));
			final var forPrimeLayer = both(forPolygon).and(hasProperty("layerType", is(LayerType.PRIMARY)));

			assertThat(result, forPolygon);
			assertThat(result, hasProperty("mode", present(is(PolygonMode.BATN))));

			assertThat(
					result, hasProperty("layers", allOf(aMapWithSize(1), hasEntry(is(LayerType.PRIMARY), anything())))
			);
			var resultPrimaryLayer = result.getLayers().get(LayerType.PRIMARY);

			assertThat(resultPrimaryLayer, forPrimeLayer);

			assertThat(
					resultPrimaryLayer, hasProperty(
							"sites", allOf(
									aMapWithSize(5), //
									hasSite(
											is("B"), is("BL"),
											forPrimeLayer.and(hasProperty("siteCurveNumber", notPresent()))
									), //
									hasSite(
											is("C"), is("CW"),
											forPrimeLayer.and(hasProperty("siteCurveNumber", present(is(11))))
									), //
									hasSite(
											is("F"), is("FD"),
											forPrimeLayer.and(hasProperty("siteCurveNumber", present(is(23))))
									), //
									hasSite(
											is("H"), is("HW"),
											forPrimeLayer.and(hasProperty("siteCurveNumber", present(is(37))))
									), //
									hasSite(
											is("S"), is("S"),
											forPrimeLayer.and(hasProperty("siteCurveNumber", present(is(71))))
									)
							)
					)
			);

			assertThat(
					resultPrimaryLayer, hasProperty(
							"species", allOf(
									aMapWithSize(5), //
									hasSpecies(is("B"), is("BL"), closeTo(10), forPrimeLayer), //
									hasSpecies(is("C"), is("CW"), closeTo(20), forPrimeLayer), //
									hasSpecies(is("F"), is("FD"), closeTo(30), forPrimeLayer), //
									hasSpecies(is("H"), is("HW"), closeTo(30), forPrimeLayer), //
									hasSpecies(is("S"), is("S"), closeTo(10), forPrimeLayer)
							)
					)
			);

			for (var nonPrimaryGenus : List.of("B", "C", "H", "S")) {
				var nonPrimarySite = resultPrimaryLayer.getSites().get(nonPrimaryGenus);
				assertThat(
						nonPrimarySite, allOf(
								hasProperty("siteIndex", notPresent()), //
								hasProperty("height", notPresent()), //
								hasProperty("ageTotal", notPresent()), //
								hasProperty("yearsToBreastHeight", notPresent()), //
								hasProperty("breastHeightAge", notPresent())
						)
				);
			}

			var primarySite = resultPrimaryLayer.getPrimarySite().get();

			assertThat(
					primarySite, allOf(
							hasProperty("siteGenus", is("F")), //
							hasProperty("siteIndex", present(closeTo(19.7f))), //
							hasProperty("height", present(closeTo(8.43922043f))), //
							hasProperty("ageTotal", present(closeTo(26f))), //
							hasProperty("yearsToBreastHeight", present(closeTo(9f))), //
							hasProperty("breastHeightAge", present(closeTo(17f)))
					)
			);

			app.close();

			control.verify();
		}

		@Test
		void testIncreaseYear() throws Exception {
			var control = EasyMock.createControl();

			VriStart app = EasyMock.createMockBuilder(VriStart.class).createMock(control);

			MockFileResolver resolver = dummyInput();

			TestUtils.populateControlMapGenusReal(controlMap);
			TestUtils.populateControlMapBecReal(controlMap);
			controlMap.put(ControlKey.MINIMA.name(), Utils.constMap(map -> {
				map.put(VriControlParser.MINIMUM_BASE_AREA, 0f);
				map.put(VriControlParser.MINIMUM_HEIGHT, 6f);
				map.put(VriControlParser.MINIMUM_PREDICTED_BASE_AREA, 2f);
			}));
			TestUtils.populateControlMapFromResource(controlMap, new BasalAreaYieldParser(), "YLDBA407.COE");
			TestUtils.populateControlMapFromResource(controlMap, new UpperBoundsParser(), "PCT_407.coe");

			var poly = VriPolygon.build(pb -> {
				pb.polygonIdentifier("082F074/0142", 1997);
				pb.forestInventoryZone(" ");
				pb.biogeoclimaticZone("IDF");
				pb.yieldFactor(1.0f);
				pb.mode(PolygonMode.YOUNG);
				pb.percentAvailable(85f);

				pb.addLayer(lb -> {
					lb.layerType(LayerType.PRIMARY);
					lb.baseArea(Optional.empty());
					lb.treesPerHectare(Optional.empty());
					lb.primaryGenus("F");
					lb.inventoryTypeGroup(3);
					lb.crownClosure(30);
					lb.utilization(7.5f);

					lb.empiricalRelationshipParameterIndex(61);
					lb.inventoryTypeGroup(3);

					lb.addSpecies(spb -> {
						spb.genus("B");
						spb.percentGenus(10);
						spb.addSpecies("BL", 100);
					});
					lb.addSpecies(spb -> {
						spb.genus("C");
						spb.percentGenus(20);
						spb.addSpecies("CW", 100);
					});
					lb.addSpecies(spb -> {
						spb.genus("F");
						spb.percentGenus(30);
						spb.addSpecies("FD", 100);
					});
					lb.addSpecies(spb -> {
						spb.genus("H");
						spb.percentGenus(30);
						spb.addSpecies("HW", 100);
					});
					lb.addSpecies(spb -> {
						spb.genus("S");
						spb.percentGenus(10);
						spb.addSpecies("S", 100);
					});

					lb.addSite(sib -> {
						sib.siteGenus("B");
						sib.siteSpecies("BL");
					});
					lb.addSite(sib -> {
						sib.siteGenus("C");
						sib.siteCurveNumber(11);
						sib.siteSpecies("CW");
					});
					lb.addSite(sib -> {
						sib.siteGenus("F");
						sib.siteSpecies("FD");
						sib.siteCurveNumber(23);
						sib.siteIndex(19.7f);
						sib.height(6f); // Set this low so we have to increment year
						sib.yearsToBreastHeight(9);
						sib.breastHeightAge(15);
						sib.ageTotal(24);
					});
					lb.addSite(sib -> {
						sib.siteGenus("H");
						sib.siteSpecies("HW");
						sib.siteCurveNumber(37);
					});
					lb.addSite(sib -> {
						sib.siteGenus("S");
						sib.siteSpecies("S");
						sib.siteCurveNumber(71);
					});
				});
			});

			control.replay();

			app.init(resolver, controlMap);

			// Run the process

			var result = assertDoesNotThrow(() -> app.processYoung(poly));

			// Assertions

			final var forPolygon = hasProperty("polygonIdentifier", isPolyId("082F074/0142", 2001));
			final var forPrimeLayer = both(forPolygon).and(hasProperty("layerType", is(LayerType.PRIMARY)));

			assertThat(result, forPolygon);
			assertThat(result, hasProperty("mode", present(is(PolygonMode.BATN))));

			assertThat(
					result, hasProperty("layers", allOf(aMapWithSize(1), hasEntry(is(LayerType.PRIMARY), anything())))
			);
			var resultPrimaryLayer = result.getLayers().get(LayerType.PRIMARY);

			assertThat(resultPrimaryLayer, forPrimeLayer);

			assertThat(
					resultPrimaryLayer, hasProperty(
							"sites", allOf(
									aMapWithSize(5), //
									hasSite(
											is("B"), is("BL"),
											forPrimeLayer.and(hasProperty("siteCurveNumber", notPresent()))
									), //
									hasSite(
											is("C"), is("CW"),
											forPrimeLayer.and(hasProperty("siteCurveNumber", present(is(11))))
									), //
									hasSite(
											is("F"), is("FD"),
											forPrimeLayer.and(hasProperty("siteCurveNumber", present(is(23))))
									), //
									hasSite(
											is("H"), is("HW"),
											forPrimeLayer.and(hasProperty("siteCurveNumber", present(is(37))))
									), //
									hasSite(
											is("S"), is("S"),
											forPrimeLayer.and(hasProperty("siteCurveNumber", present(is(71))))
									)
							)
					)
			);

			assertThat(
					resultPrimaryLayer, hasProperty(
							"species", allOf(
									aMapWithSize(5), //
									hasSpecies(is("B"), is("BL"), closeTo(10), forPrimeLayer), //
									hasSpecies(is("C"), is("CW"), closeTo(20), forPrimeLayer), //
									hasSpecies(is("F"), is("FD"), closeTo(30), forPrimeLayer), //
									hasSpecies(is("H"), is("HW"), closeTo(30), forPrimeLayer), //
									hasSpecies(is("S"), is("S"), closeTo(10), forPrimeLayer)
							)
					)
			);

			for (var nonPrimaryGenus : List.of("B", "C", "H", "S")) {
				var nonPrimarySite = resultPrimaryLayer.getSites().get(nonPrimaryGenus);
				assertThat(
						nonPrimarySite, allOf(
								hasProperty("siteIndex", notPresent()), //
								hasProperty("height", notPresent()), //
								hasProperty("ageTotal", notPresent()), //
								hasProperty("yearsToBreastHeight", notPresent()), //
								hasProperty("breastHeightAge", notPresent())
						)
				);
			}

			var primarySite = resultPrimaryLayer.getPrimarySite().get();

			assertThat(
					primarySite, allOf(
							hasProperty("siteGenus", is("F")), //
							hasProperty("siteIndex", present(closeTo(19.7f))), //
							hasProperty("height", present(closeTo(7.6620512f))), //
							hasProperty("ageTotal", present(closeTo(28f))), //
							hasProperty("yearsToBreastHeight", present(closeTo(9f))), //
							hasProperty("breastHeightAge", present(closeTo(19f)))
					)
			);

			app.close();

			control.verify();
		}

		@Test
		void testTooOld() throws Exception {
			var control = EasyMock.createControl();

			VriStart app = new VriStart();

			MockFileResolver resolver = dummyInput();

			var poly = VriPolygon.build(pb -> {
				pb.polygonIdentifier(TestUtils.polygonId("TestPolygon", 1899));
				pb.biogeoclimaticZone("IDF");
				pb.yieldFactor(1.0f);
				pb.mode(PolygonMode.YOUNG);
			});

			control.replay();

			app.init(resolver, controlMap);

			var ex = assertThrows(StandProcessingException.class, () -> app.processYoung(poly));

			assertThat(ex, hasProperty("message", is("Year for YOUNG stand should be at least 1900 but was 1899")));

			app.close();

			control.verify();
		}

	}

	@Nested
	class ProcessBatc {
		@Test
		void testNoVeteran() throws Exception {
			var control = EasyMock.createControl();

			VriStart app = EasyMock.createMockBuilder(VriStart.class).createMock(control);

			MockFileResolver resolver = dummyInput();

			TestUtils.populateControlMapGenusReal(controlMap);
			TestUtils.populateControlMapBecReal(controlMap);

			TestUtils.populateControlMapFromResource(controlMap, new BaseAreaCoefficientParser(), "REGBA25.coe");
			TestUtils
					.populateControlMapFromResource(controlMap, new QuadMeanDiameterCoefficientParser(), "REGDQ26.coe");

			TestUtils.populateControlMapFromResource(controlMap, new HLPrimarySpeciesEqnP1Parser(), "REGYHLP.COE");
			TestUtils.populateControlMapFromResource(controlMap, new HLPrimarySpeciesEqnP2Parser(), "REGYHLPA.COE");
			TestUtils.populateControlMapFromResource(controlMap, new HLPrimarySpeciesEqnP3Parser(), "REGYHLPB.DAT");
			TestUtils.populateControlMapFromResource(controlMap, new HLNonprimaryCoefficientParser(), "REGHL.COE");

			TestUtils.populateControlMapFromResource(controlMap, new UpperCoefficientParser(), "UPPERB02.COE");

			TestUtils.populateControlMapFromResource(
					controlMap, new ModifierParser(VdypApplicationIdentifier.VRI_START), "mod19813.prm"
			);

			var poly = VriPolygon.build(pb -> {
				pb.polygonIdentifier("082F074/0142", 1997);
				pb.forestInventoryZone(" ");
				pb.biogeoclimaticZone("IDF");
				pb.yieldFactor(1.0f);
				pb.mode(PolygonMode.BATC);

				pb.addLayer(lb -> {
					lb.layerType(LayerType.PRIMARY);
					lb.baseArea(Optional.empty());
					lb.treesPerHectare(Optional.empty());
					lb.primaryGenus("F");
					lb.inventoryTypeGroup(3);
					lb.crownClosure(30);
					lb.utilization(7.5f);

					lb.empiricalRelationshipParameterIndex(61);

					lb.addSpecies(spb -> {
						spb.genus("B");
						spb.percentGenus(10);
						spb.addSpecies("BL", 100);
					});
					lb.addSpecies(spb -> {
						spb.genus("C");
						spb.percentGenus(20);
						spb.addSpecies("CW", 100);
					});
					lb.addSpecies(spb -> {
						spb.genus("F");
						spb.percentGenus(30);
						spb.addSpecies("FD", 100);
					});
					lb.addSpecies(spb -> {
						spb.genus("H");
						spb.percentGenus(30);
						spb.addSpecies("HW", 100);
					});
					lb.addSpecies(spb -> {
						spb.genus("S");
						spb.percentGenus(10);
						spb.addSpecies("S", 100);
					});

					lb.addSite(sib -> {
						sib.siteGenus("B");
						sib.siteSpecies("BL");
					});
					lb.addSite(sib -> {
						sib.siteGenus("C");
						sib.siteCurveNumber(11);
						sib.siteSpecies("CW");
					});
					lb.addSite(sib -> {
						sib.siteGenus("F");
						sib.siteSpecies("FD");
						sib.siteCurveNumber(23);
						sib.siteIndex(19.7f);
						sib.height(7.6f);
						sib.yearsToBreastHeight(9);
						sib.breastHeightAge(15);
						sib.ageTotal(24);
					});
					lb.addSite(sib -> {
						sib.siteGenus("H");
						sib.siteSpecies("HW");
						sib.siteCurveNumber(37);
					});
					lb.addSite(sib -> {
						sib.siteGenus("S");
						sib.siteSpecies("S");
						sib.siteCurveNumber(71);
					});
				});
			});

			control.replay();

			app.init(resolver, controlMap);

			// Run the process

			var result = assertDoesNotThrow(() -> app.processBatc(poly));

			// Assertions

			final var forPolygon = hasProperty("polygonIdentifier", isPolyId("082F074/0142", 1997));
			final var forPrimeLayer = both(forPolygon).and(hasProperty("layerType", is(LayerType.PRIMARY)));

			assertThat(result, forPolygon);
			assertThat(result, hasProperty("mode", present(is(PolygonMode.BATC))));

			assertThat(
					result, hasProperty("layers", allOf(aMapWithSize(1), hasEntry(is(LayerType.PRIMARY), anything())))
			);
			var resultPrimaryLayer = result.getLayers().get(LayerType.PRIMARY);

			assertThat(resultPrimaryLayer, forPrimeLayer);

			assertThat(resultPrimaryLayer, hasProperty("baseArea", present(closeTo(0.72882f))));
			assertThat(resultPrimaryLayer, hasProperty("treesPerHectare", present(closeTo(122.3f))));

			assertThat(
					resultPrimaryLayer, hasProperty(
							"sites", allOf(
									aMapWithSize(5), //
									hasSite(
											is("B"), is("BL"),
											forPrimeLayer.and(hasProperty("siteCurveNumber", notPresent()))
									), //
									hasSite(
											is("C"), is("CW"),
											forPrimeLayer.and(hasProperty("siteCurveNumber", present(is(11))))
									), //
									hasSite(
											is("F"), is("FD"),
											forPrimeLayer.and(hasProperty("siteCurveNumber", present(is(23))))
									), //
									hasSite(
											is("H"), is("HW"),
											forPrimeLayer.and(hasProperty("siteCurveNumber", present(is(37))))
									), //
									hasSite(
											is("S"), is("S"),
											forPrimeLayer.and(hasProperty("siteCurveNumber", present(is(71))))
									)
							)
					)
			);

			assertThat(
					resultPrimaryLayer, hasProperty(
							"species", allOf(
									aMapWithSize(5), //
									hasSpecies(is("B"), is("BL"), closeTo(10), forPrimeLayer), //
									hasSpecies(is("C"), is("CW"), closeTo(20), forPrimeLayer), //
									hasSpecies(is("F"), is("FD"), closeTo(30), forPrimeLayer), //
									hasSpecies(is("H"), is("HW"), closeTo(30), forPrimeLayer), //
									hasSpecies(is("S"), is("S"), closeTo(10), forPrimeLayer)
							)
					)
			);

			for (var nonPrimaryGenus : List.of("B", "C", "H", "S")) {
				var nonPrimarySite = resultPrimaryLayer.getSites().get(nonPrimaryGenus);
				assertThat(
						nonPrimarySite, allOf(
								hasProperty("siteIndex", notPresent()), //
								hasProperty("height", notPresent()), //
								hasProperty("ageTotal", notPresent()), //
								hasProperty("yearsToBreastHeight", notPresent()), //
								hasProperty("breastHeightAge", notPresent())
						)
				);
			}

			var primarySite = resultPrimaryLayer.getPrimarySite().get();

			assertThat(
					primarySite, allOf(
							hasProperty("siteGenus", is("F")), //
							hasProperty("siteIndex", present(closeTo(19.7f))), //
							hasProperty("height", present(closeTo(7.6f))), //
							hasProperty("ageTotal", present(closeTo(24f))), //
							hasProperty("yearsToBreastHeight", present(closeTo(9f))), //
							hasProperty("breastHeightAge", present(closeTo(15f)))
					)
			);

			app.close();

			control.verify();
		}

		@Test
		void testWithVeteran() throws Exception {
			var control = EasyMock.createControl();

			VriStart app = EasyMock.createMockBuilder(VriStart.class).createMock(control);

			MockFileResolver resolver = dummyInput();

			TestUtils.populateControlMapGenusReal(controlMap);
			TestUtils.populateControlMapBecReal(controlMap);

			TestUtils.populateControlMapFromResource(controlMap, new BaseAreaCoefficientParser(), "REGBA25.coe");
			TestUtils
					.populateControlMapFromResource(controlMap, new QuadMeanDiameterCoefficientParser(), "REGDQ26.coe");

			TestUtils.populateControlMapFromResource(controlMap, new HLPrimarySpeciesEqnP1Parser(), "REGYHLP.COE");
			TestUtils.populateControlMapFromResource(controlMap, new HLPrimarySpeciesEqnP2Parser(), "REGYHLPA.COE");
			TestUtils.populateControlMapFromResource(controlMap, new HLPrimarySpeciesEqnP3Parser(), "REGYHLPB.DAT");
			TestUtils.populateControlMapFromResource(controlMap, new HLNonprimaryCoefficientParser(), "REGHL.COE");

			TestUtils.populateControlMapFromResource(controlMap, new UpperCoefficientParser(), "UPPERB02.COE");

			TestUtils.populateControlMapFromResource(
					controlMap, new ModifierParser(VdypApplicationIdentifier.VRI_START), "mod19813.prm"
			);

			var poly = VriPolygon.build(pb -> {
				pb.polygonIdentifier("082F074/0142", 1997);
				pb.forestInventoryZone(" ");
				pb.biogeoclimaticZone("IDF");
				pb.yieldFactor(1.0f);
				pb.mode(PolygonMode.BATC);

				pb.addLayer(lb -> {
					lb.layerType(LayerType.PRIMARY);
					lb.baseArea(Optional.empty());
					lb.treesPerHectare(Optional.empty());
					lb.primaryGenus("F");
					lb.inventoryTypeGroup(3);
					lb.crownClosure(30);
					lb.utilization(7.5f);

					lb.empiricalRelationshipParameterIndex(61);

					lb.addSpecies(spb -> {
						spb.genus("B");
						spb.percentGenus(10);
						spb.addSpecies("BL", 100);
					});
					lb.addSpecies(spb -> {
						spb.genus("C");
						spb.percentGenus(20);
						spb.addSpecies("CW", 100);
					});
					lb.addSpecies(spb -> {
						spb.genus("F");
						spb.percentGenus(30);
						spb.addSpecies("FD", 100);
					});
					lb.addSpecies(spb -> {
						spb.genus("H");
						spb.percentGenus(30);
						spb.addSpecies("HW", 100);
					});
					lb.addSpecies(spb -> {
						spb.genus("S");
						spb.percentGenus(10);
						spb.addSpecies("S", 100);
					});

					lb.addSite(sib -> {
						sib.siteGenus("B");
						sib.siteSpecies("BL");
					});
					lb.addSite(sib -> {
						sib.siteGenus("C");
						sib.siteCurveNumber(11);
						sib.siteSpecies("CW");
					});
					lb.addSite(sib -> {
						sib.siteGenus("F");
						sib.siteSpecies("FD");
						sib.siteCurveNumber(23);
						sib.siteIndex(19.7f);
						sib.height(7.6f);
						sib.yearsToBreastHeight(9);
						sib.breastHeightAge(15);
						sib.ageTotal(24);
					});
					lb.addSite(sib -> {
						sib.siteGenus("H");
						sib.siteSpecies("HW");
						sib.siteCurveNumber(37);
					});
					lb.addSite(sib -> {
						sib.siteGenus("S");
						sib.siteSpecies("S");
						sib.siteCurveNumber(71);
					});
				});
				pb.addLayer(lb -> {
					lb.layerType(LayerType.VETERAN);
					lb.baseArea(Optional.empty());
					lb.treesPerHectare(Optional.empty());
					lb.primaryGenus("F");
					lb.inventoryTypeGroup(3);
					lb.crownClosure(30);
					lb.utilization(7.5f);

					lb.empiricalRelationshipParameterIndex(61);

					lb.addSpecies(spb -> {
						spb.genus("B");
						spb.percentGenus(10);
						spb.addSpecies("BL", 100);
					});
					lb.addSpecies(spb -> {
						spb.genus("C");
						spb.percentGenus(20);
						spb.addSpecies("CW", 100);
					});
					lb.addSpecies(spb -> {
						spb.genus("F");
						spb.percentGenus(30);
						spb.addSpecies("FD", 100);
					});
					lb.addSpecies(spb -> {
						spb.genus("H");
						spb.percentGenus(30);
						spb.addSpecies("HW", 100);
					});
					lb.addSpecies(spb -> {
						spb.genus("S");
						spb.percentGenus(10);
						spb.addSpecies("S", 100);
					});

					lb.addSite(sib -> {
						sib.siteGenus("B");
						sib.siteSpecies("BL");
					});
					lb.addSite(sib -> {
						sib.siteGenus("C");
						sib.siteCurveNumber(11);
						sib.siteSpecies("CW");
					});
					lb.addSite(sib -> {
						sib.siteGenus("F");
						sib.siteSpecies("FD");
						sib.siteCurveNumber(23);
						sib.siteIndex(19.7f);
						sib.height(7.6f);
						sib.yearsToBreastHeight(9);
						sib.breastHeightAge(15);
						sib.ageTotal(24);
					});
					lb.addSite(sib -> {
						sib.siteGenus("H");
						sib.siteSpecies("HW");
						sib.siteCurveNumber(37);
					});
					lb.addSite(sib -> {
						sib.siteGenus("S");
						sib.siteSpecies("S");
						sib.siteCurveNumber(71);
					});
				});
			});

			control.replay();

			app.init(resolver, controlMap);

			// Run the process

			var result = assertDoesNotThrow(() -> app.processBatc(poly));

			// Assertions

			final var forPolygon = hasProperty("polygonIdentifier", isPolyId("082F074/0142", 1997));
			final var forPrimeLayer = both(forPolygon).and(hasProperty("layerType", is(LayerType.PRIMARY)));
			final var forVeteranLayer = both(forPolygon).and(hasProperty("layerType", is(LayerType.VETERAN)));

			assertThat(result, forPolygon);
			assertThat(result, hasProperty("mode", present(is(PolygonMode.BATC))));

			assertThat(
					result,
					hasProperty(
							"layers",
							allOf(
									aMapWithSize(2), hasEntry(is(LayerType.PRIMARY), anything()),
									hasEntry(is(LayerType.VETERAN), anything())
							)
					)
			);
			var resultPrimaryLayer = result.getLayers().get(LayerType.PRIMARY);
			var resultVeteranLayer = result.getLayers().get(LayerType.VETERAN);

			assertThat(resultPrimaryLayer, forPrimeLayer);
			assertThat(resultVeteranLayer, forVeteranLayer);

			assertThat(resultPrimaryLayer, hasProperty("baseArea", present(closeTo(0.72324f))));
			assertThat(resultPrimaryLayer, hasProperty("treesPerHectare", present(closeTo(121.6f))));

			assertThat(resultVeteranLayer, hasProperty("baseArea", present(closeTo(0.80981f))));

			for (var nonPrimaryGenus : List.of("B", "C", "H", "S")) {
				var nonPrimarySite = resultPrimaryLayer.getSites().get(nonPrimaryGenus);
				assertThat(
						nonPrimarySite, allOf(
								hasProperty("siteIndex", notPresent()), //
								hasProperty("height", notPresent()), //
								hasProperty("ageTotal", notPresent()), //
								hasProperty("yearsToBreastHeight", notPresent()), //
								hasProperty("breastHeightAge", notPresent())
						)
				);
			}

			var primarySite = resultPrimaryLayer.getPrimarySite().get();

			assertThat(
					primarySite, allOf(
							hasProperty("siteGenus", is("F")), //
							hasProperty("siteIndex", present(closeTo(19.7f))), //
							hasProperty("height", present(closeTo(7.6f))), //
							hasProperty("ageTotal", present(closeTo(24f))), //
							hasProperty("yearsToBreastHeight", present(closeTo(9f))), //
							hasProperty("breastHeightAge", present(closeTo(15f)))
					)
			);

			app.close();

			control.verify();
		}
	}

	@Nested
	class ProcessBatn {

		@Test
		void testNoVeteran() throws Exception {
			var control = EasyMock.createControl();

			VriStart app = new VriStart();

			MockFileResolver resolver = dummyInput();

			TestUtils.populateControlMapGenusReal(controlMap);
			TestUtils.populateControlMapBecReal(controlMap);

			TestUtils.populateControlMapFromResource(controlMap, new BaseAreaCoefficientParser(), "REGBA25.coe");
			TestUtils
					.populateControlMapFromResource(controlMap, new QuadMeanDiameterCoefficientParser(), "REGDQ26.coe");

			TestUtils.populateControlMapFromResource(controlMap, new HLPrimarySpeciesEqnP1Parser(), "REGYHLP.COE");
			TestUtils.populateControlMapFromResource(controlMap, new HLPrimarySpeciesEqnP2Parser(), "REGYHLPA.COE");
			TestUtils.populateControlMapFromResource(controlMap, new HLPrimarySpeciesEqnP3Parser(), "REGYHLPB.DAT");
			TestUtils.populateControlMapFromResource(controlMap, new HLNonprimaryCoefficientParser(), "REGHL.COE");

			TestUtils.populateControlMapFromResource(controlMap, new UpperCoefficientParser(), "UPPERB02.COE");

			TestUtils.populateControlMapFromResource(controlMap, new BasalAreaYieldParser(), "YLDBA407.COE");
			TestUtils.populateControlMapFromResource(controlMap, new QuadraticMeanDiameterYieldParser(), "YLDDQ45.COE");
			TestUtils.populateControlMapFromResource(controlMap, new UpperBoundsParser(), "PCT_407.coe");

			TestUtils.populateControlMapFromResource(
					controlMap, new ModifierParser(VdypApplicationIdentifier.VRI_START), "mod19813.prm"
			);

			var poly = VriPolygon.build(pb -> {
				pb.polygonIdentifier("082F074/0142", 1997);
				pb.forestInventoryZone(" ");
				pb.biogeoclimaticZone("IDF");
				pb.yieldFactor(1.0f);
				pb.mode(PolygonMode.BATN);

				pb.addLayer(lb -> {
					lb.layerType(LayerType.PRIMARY);
					lb.baseArea(Optional.empty());
					lb.treesPerHectare(Optional.empty());
					lb.primaryGenus("F");
					lb.inventoryTypeGroup(3);
					lb.crownClosure(30);
					lb.utilization(7.5f);

					lb.empiricalRelationshipParameterIndex(61);

					lb.addSpecies(spb -> {
						spb.genus("B");
						spb.percentGenus(10);
						spb.addSpecies("BL", 100);
					});
					lb.addSpecies(spb -> {
						spb.genus("C");
						spb.percentGenus(20);
						spb.addSpecies("CW", 100);
					});
					lb.addSpecies(spb -> {
						spb.genus("F");
						spb.percentGenus(30);
						spb.addSpecies("FD", 100);
					});
					lb.addSpecies(spb -> {
						spb.genus("H");
						spb.percentGenus(30);
						spb.addSpecies("HW", 100);
					});
					lb.addSpecies(spb -> {
						spb.genus("S");
						spb.percentGenus(10);
						spb.addSpecies("S", 100);
					});

					lb.addSite(sib -> {
						sib.siteGenus("B");
						sib.siteSpecies("BL");
					});
					lb.addSite(sib -> {
						sib.siteGenus("C");
						sib.siteCurveNumber(11);
						sib.siteSpecies("CW");
					});
					lb.addSite(sib -> {
						sib.siteGenus("F");
						sib.siteSpecies("FD");
						sib.siteCurveNumber(23);
						sib.siteIndex(19.7f);
						sib.height(7.6f);
						sib.yearsToBreastHeight(9);
						sib.breastHeightAge(15);
						sib.ageTotal(24);
					});
					lb.addSite(sib -> {
						sib.siteGenus("H");
						sib.siteSpecies("HW");
						sib.siteCurveNumber(37);
					});
					lb.addSite(sib -> {
						sib.siteGenus("S");
						sib.siteSpecies("S");
						sib.siteCurveNumber(71);
					});
				});
			});

			control.replay();

			app.init(resolver, controlMap);

			// Run the process

			var result = assertDoesNotThrow(() -> app.processBatn(poly));

			// Assertions

			final var forPolygon = hasProperty("polygonIdentifier", isPolyId("082F074/0142", 1997));
			final var forPrimeLayer = both(forPolygon).and(hasProperty("layerType", is(LayerType.PRIMARY)));

			assertThat(result, forPolygon);
			assertThat(result, hasProperty("mode", present(is(PolygonMode.BATN))));

			assertThat(
					result, hasProperty("layers", allOf(aMapWithSize(1), hasEntry(is(LayerType.PRIMARY), anything())))
			);
			var resultPrimaryLayer = result.getLayers().get(LayerType.PRIMARY);

			assertThat(resultPrimaryLayer, forPrimeLayer);

			assertThat(resultPrimaryLayer, hasProperty("baseArea", present(closeTo(6.34290648f))));
			assertThat(resultPrimaryLayer, hasProperty("treesPerHectare", present(closeTo(748.402222f))));

			assertThat(
					resultPrimaryLayer, hasProperty(
							"sites", allOf(
									aMapWithSize(5), //
									hasSite(
											is("B"), is("BL"),
											forPrimeLayer.and(hasProperty("siteCurveNumber", notPresent()))
									), //
									hasSite(
											is("C"), is("CW"),
											forPrimeLayer.and(hasProperty("siteCurveNumber", present(is(11))))
									), //
									hasSite(
											is("F"), is("FD"),
											forPrimeLayer.and(hasProperty("siteCurveNumber", present(is(23))))
									), //
									hasSite(
											is("H"), is("HW"),
											forPrimeLayer.and(hasProperty("siteCurveNumber", present(is(37))))
									), //
									hasSite(
											is("S"), is("S"),
											forPrimeLayer.and(hasProperty("siteCurveNumber", present(is(71))))
									)
							)
					)
			);

			assertThat(
					resultPrimaryLayer, hasProperty(
							"species", allOf(
									aMapWithSize(5), //
									hasSpecies(is("B"), is("BL"), closeTo(10), forPrimeLayer), //
									hasSpecies(is("C"), is("CW"), closeTo(20), forPrimeLayer), //
									hasSpecies(is("F"), is("FD"), closeTo(30), forPrimeLayer), //
									hasSpecies(is("H"), is("HW"), closeTo(30), forPrimeLayer), //
									hasSpecies(is("S"), is("S"), closeTo(10), forPrimeLayer)
							)
					)
			);

			for (var nonPrimaryGenus : List.of("B", "C", "H", "S")) {
				var nonPrimarySite = resultPrimaryLayer.getSites().get(nonPrimaryGenus);
				assertThat(
						nonPrimarySite, allOf(
								hasProperty("siteIndex", notPresent()), //
								hasProperty("height", notPresent()), //
								hasProperty("ageTotal", notPresent()), //
								hasProperty("yearsToBreastHeight", notPresent()), //
								hasProperty("breastHeightAge", notPresent())
						)
				);
			}

			var primarySite = resultPrimaryLayer.getPrimarySite().get();

			assertThat(
					primarySite, allOf(
							hasProperty("siteGenus", is("F")), //
							hasProperty("siteIndex", present(closeTo(19.7f))), //
							hasProperty("height", present(closeTo(7.6f))), //
							hasProperty("ageTotal", present(closeTo(24f))), //
							hasProperty("yearsToBreastHeight", present(closeTo(9f))), //
							hasProperty("breastHeightAge", present(closeTo(15f)))
					)
			);

			app.close();

			control.verify();
		}

		@Test
		void testWithVeteran() throws Exception {
			var control = EasyMock.createControl();

			VriStart app = EasyMock.createMockBuilder(VriStart.class).createMock(control);

			MockFileResolver resolver = dummyInput();

			TestUtils.populateControlMapGenusReal(controlMap);
			TestUtils.populateControlMapBecReal(controlMap);

			TestUtils.populateControlMapFromResource(controlMap, new BaseAreaCoefficientParser(), "REGBA25.coe");
			TestUtils
					.populateControlMapFromResource(controlMap, new QuadMeanDiameterCoefficientParser(), "REGDQ26.coe");

			TestUtils.populateControlMapFromResource(controlMap, new HLPrimarySpeciesEqnP1Parser(), "REGYHLP.COE");
			TestUtils.populateControlMapFromResource(controlMap, new HLPrimarySpeciesEqnP2Parser(), "REGYHLPA.COE");
			TestUtils.populateControlMapFromResource(controlMap, new HLPrimarySpeciesEqnP3Parser(), "REGYHLPB.DAT");
			TestUtils.populateControlMapFromResource(controlMap, new HLNonprimaryCoefficientParser(), "REGHL.COE");

			TestUtils.populateControlMapFromResource(controlMap, new UpperCoefficientParser(), "UPPERB02.COE");

			TestUtils.populateControlMapFromResource(controlMap, new BasalAreaYieldParser(), "YLDBA407.COE");
			TestUtils.populateControlMapFromResource(controlMap, new QuadraticMeanDiameterYieldParser(), "YLDDQ45.COE");
			TestUtils.populateControlMapFromResource(controlMap, new UpperBoundsParser(), "PCT_407.coe");

			TestUtils.populateControlMapFromResource(
					controlMap, new ModifierParser(VdypApplicationIdentifier.VRI_START), "mod19813.prm"
			);

			var poly = VriPolygon.build(pb -> {
				pb.polygonIdentifier("082F074/0142", 1997);
				pb.forestInventoryZone(" ");
				pb.biogeoclimaticZone("IDF");
				pb.yieldFactor(1.0f);
				pb.mode(PolygonMode.BATN);

				pb.addLayer(lb -> {
					lb.layerType(LayerType.PRIMARY);
					lb.baseArea(Optional.empty());
					lb.treesPerHectare(Optional.empty());
					lb.primaryGenus("F");
					lb.inventoryTypeGroup(3);
					lb.crownClosure(30);
					lb.utilization(7.5f);

					lb.empiricalRelationshipParameterIndex(61);

					lb.addSpecies(spb -> {
						spb.genus("B");
						spb.percentGenus(10);
						spb.addSpecies("BL", 100);
					});
					lb.addSpecies(spb -> {
						spb.genus("C");
						spb.percentGenus(20);
						spb.addSpecies("CW", 100);
					});
					lb.addSpecies(spb -> {
						spb.genus("F");
						spb.percentGenus(30);
						spb.addSpecies("FD", 100);
					});
					lb.addSpecies(spb -> {
						spb.genus("H");
						spb.percentGenus(30);
						spb.addSpecies("HW", 100);
					});
					lb.addSpecies(spb -> {
						spb.genus("S");
						spb.percentGenus(10);
						spb.addSpecies("S", 100);
					});

					lb.addSite(sib -> {
						sib.siteGenus("B");
						sib.siteSpecies("BL");
					});
					lb.addSite(sib -> {
						sib.siteGenus("C");
						sib.siteCurveNumber(11);
						sib.siteSpecies("CW");
					});
					lb.addSite(sib -> {
						sib.siteGenus("F");
						sib.siteSpecies("FD");
						sib.siteCurveNumber(23);
						sib.siteIndex(19.7f);
						sib.height(7.6f);
						sib.yearsToBreastHeight(9);
						sib.breastHeightAge(15);
						sib.ageTotal(24);
					});
					lb.addSite(sib -> {
						sib.siteGenus("H");
						sib.siteSpecies("HW");
						sib.siteCurveNumber(37);
					});
					lb.addSite(sib -> {
						sib.siteGenus("S");
						sib.siteSpecies("S");
						sib.siteCurveNumber(71);
					});
				});
				pb.addLayer(lb -> {
					lb.layerType(LayerType.VETERAN);
					lb.baseArea(5f);
					lb.treesPerHectare(Optional.empty());
					lb.primaryGenus("F");
					lb.inventoryTypeGroup(3);
					lb.crownClosure(30);
					lb.utilization(7.5f);

					lb.empiricalRelationshipParameterIndex(61);

					lb.addSpecies(spb -> {
						spb.genus("B");
						spb.percentGenus(10);
						spb.addSpecies("BL", 100);
					});
					lb.addSpecies(spb -> {
						spb.genus("C");
						spb.percentGenus(20);
						spb.addSpecies("CW", 100);
					});
					lb.addSpecies(spb -> {
						spb.genus("F");
						spb.percentGenus(30);
						spb.addSpecies("FD", 100);
					});
					lb.addSpecies(spb -> {
						spb.genus("H");
						spb.percentGenus(30);
						spb.addSpecies("HW", 100);
					});
					lb.addSpecies(spb -> {
						spb.genus("S");
						spb.percentGenus(10);
						spb.addSpecies("S", 100);
					});

					lb.addSite(sib -> {
						sib.siteGenus("B");
						sib.siteSpecies("BL");
					});
					lb.addSite(sib -> {
						sib.siteGenus("C");
						sib.siteCurveNumber(11);
						sib.siteSpecies("CW");
					});
					lb.addSite(sib -> {
						sib.siteGenus("F");
						sib.siteSpecies("FD");
						sib.siteCurveNumber(23);
						sib.siteIndex(19.7f);
						sib.height(7.6f);
						sib.yearsToBreastHeight(9);
						sib.breastHeightAge(15);
						sib.ageTotal(24);
					});
					lb.addSite(sib -> {
						sib.siteGenus("H");
						sib.siteSpecies("HW");
						sib.siteCurveNumber(37);
					});
					lb.addSite(sib -> {
						sib.siteGenus("S");
						sib.siteSpecies("S");
						sib.siteCurveNumber(71);
					});
				});
			});

			control.replay();

			app.init(resolver, controlMap);

			// Run the process

			var result = assertDoesNotThrow(() -> app.processBatn(poly));

			// Assertions

			final var forPolygon = hasProperty("polygonIdentifier", isPolyId("082F074/0142", 1997));
			final var forPrimeLayer = both(forPolygon).and(hasProperty("layerType", is(LayerType.PRIMARY)));
			final var forVeteranLayer = both(forPolygon).and(hasProperty("layerType", is(LayerType.VETERAN)));

			assertThat(result, forPolygon);
			assertThat(result, hasProperty("mode", present(is(PolygonMode.BATN))));

			assertThat(
					result,
					hasProperty(
							"layers",
							allOf(
									aMapWithSize(2), hasEntry(is(LayerType.PRIMARY), anything()),
									hasEntry(is(LayerType.VETERAN), anything())
							)
					)
			);
			var resultPrimaryLayer = result.getLayers().get(LayerType.PRIMARY);
			var resultVeteranLayer = result.getLayers().get(LayerType.VETERAN);

			assertThat(resultPrimaryLayer, forPrimeLayer);
			assertThat(resultVeteranLayer, forVeteranLayer);

			assertThat(resultPrimaryLayer, hasProperty("baseArea", present(closeTo(6.06380272f))));
			assertThat(resultPrimaryLayer, hasProperty("treesPerHectare", present(closeTo(715.977112f))));

			assertThat(resultVeteranLayer, hasProperty("baseArea", present(closeTo(5f))));

			for (var nonPrimaryGenus : List.of("B", "C", "H", "S")) {
				var nonPrimarySite = resultPrimaryLayer.getSites().get(nonPrimaryGenus);
				assertThat(
						nonPrimarySite, allOf(
								hasProperty("siteIndex", notPresent()), //
								hasProperty("height", notPresent()), //
								hasProperty("ageTotal", notPresent()), //
								hasProperty("yearsToBreastHeight", notPresent()), //
								hasProperty("breastHeightAge", notPresent())
						)
				);
			}

			var primarySite = resultPrimaryLayer.getPrimarySite().get();

			assertThat(
					primarySite, allOf(
							hasProperty("siteGenus", is("F")), //
							hasProperty("siteIndex", present(closeTo(19.7f))), //
							hasProperty("height", present(closeTo(7.6f))), //
							hasProperty("ageTotal", present(closeTo(24f))), //
							hasProperty("yearsToBreastHeight", present(closeTo(9f))), //
							hasProperty("breastHeightAge", present(closeTo(15f)))
					)
			);

			app.close();

			control.verify();
		}
	}

	@Nested
	class EstimateQuadMeanDiameterYield {

		@Test
		void testCompute() throws StandProcessingException {

			controlMap = VriTestUtils.loadControlMap();
			VriStart app = new VriStart();
			ApplicationTestUtils.setControlMap(app, controlMap);

			var polygon = VriPolygon.build(pBuilder -> {
				pBuilder.polygonIdentifier("Test", 2024);
				pBuilder.biogeoclimaticZone("IDF");
				pBuilder.yieldFactor(1.0f);
				pBuilder.addLayer(lBuilder -> {
					lBuilder.layerType(LayerType.PRIMARY);
					lBuilder.crownClosure(57.8f);
					lBuilder.utilization(7.5f);
					lBuilder.empiricalRelationshipParameterIndex(61);

					lBuilder.addSpecies(sBuilder -> {
						sBuilder.genus("B"); // 3
						sBuilder.percentGenus(10f);
					});
					lBuilder.addSpecies(sBuilder -> {
						sBuilder.genus("C"); // 4
						sBuilder.percentGenus(20f);
					});
					lBuilder.addSpecies(sBuilder -> {
						sBuilder.genus("F"); // 4
						sBuilder.percentGenus(30f);
					});
					lBuilder.addSpecies(sBuilder -> {
						sBuilder.genus("H"); // 8
						sBuilder.percentGenus(30f);
					});
					lBuilder.addSpecies(sBuilder -> {
						sBuilder.genus("S"); // 15
						sBuilder.percentGenus(10f);
					});

					lBuilder.primaryGenus("F");
				});
			});

			var species = polygon.getLayers().get(LayerType.PRIMARY).getSpecies().values();

			var bec = Utils.expectParsedControl(controlMap, ControlKey.BEC_DEF, BecLookup.class).get("IDF").get();

			float result = app.estimateQuadMeanDiameterYield(7.6f, 15f, Optional.empty(), species, bec, 61);

			assertThat(result, closeTo(10.3879938f));
		}

		@ParameterizedTest
		@ValueSource(floats = { 0f, -1f, -Float.MIN_VALUE, -Float.MAX_VALUE, Float.NEGATIVE_INFINITY })
		void testBreastHeightAgeLow(float breastHeightAge) throws StandProcessingException {
			controlMap = VriTestUtils.loadControlMap();
			VriStart app = new VriStart();
			ApplicationTestUtils.setControlMap(app, controlMap);

			var polygon = VriPolygon.build(pBuilder -> {
				pBuilder.polygonIdentifier("Test", 2024);
				pBuilder.biogeoclimaticZone("IDF");
				pBuilder.yieldFactor(1.0f);
				pBuilder.addLayer(lBuilder -> {
					lBuilder.layerType(LayerType.PRIMARY);
					lBuilder.crownClosure(57.8f);
					lBuilder.utilization(7.5f);
					lBuilder.empiricalRelationshipParameterIndex(61);

					lBuilder.addSpecies(sBuilder -> {
						sBuilder.genus("B"); // 3
						sBuilder.percentGenus(10f);
					});
					lBuilder.addSpecies(sBuilder -> {
						sBuilder.genus("C"); // 4
						sBuilder.percentGenus(20f);
					});
					lBuilder.addSpecies(sBuilder -> {
						sBuilder.genus("F"); // 4
						sBuilder.percentGenus(30f);
					});
					lBuilder.addSpecies(sBuilder -> {
						sBuilder.genus("H"); // 8
						sBuilder.percentGenus(30f);
					});
					lBuilder.addSpecies(sBuilder -> {
						sBuilder.genus("S"); // 15
						sBuilder.percentGenus(10f);
					});

					lBuilder.primaryGenus("F");
				});
			});

			var species = polygon.getLayers().get(LayerType.PRIMARY).getSpecies().values();

			var bec = Utils.expectParsedControl(controlMap, ControlKey.BEC_DEF, BecLookup.class).get("IDF").get();

			var ex = assertThrows(
					StandProcessingException.class,
					() -> app.estimateQuadMeanDiameterYield(7.6f, breastHeightAge, Optional.empty(), species, bec, 61)
			);

			assertThat(ex, hasProperty("message", endsWith(Float.toString(breastHeightAge))));

		}

	}

	/**
	 * Matches a species entry with the given genus and a single species entry of 100%
	 *
	 * @param genus
	 * @param species
	 * @param percent
	 * @return
	 */
	@SuppressWarnings("unchecked")
	static Matcher<Map<? extends String, ? extends VriSpecies>> hasSpecies(
			Matcher<String> genus, Matcher<String> species, Matcher<Float> percent,
			Matcher<? super VriSpecies> additional
	) {
		return hasEntry(
				genus, //
				both(hasProperty("genus", genus))
						.and(hasProperty("speciesPercent", allOf(aMapWithSize(1), hasEntry(species, is(100f)))))
						.and(hasProperty("percentGenus", percent))//
						.and((Matcher<? super Object>) additional)
		);
	}

	@SuppressWarnings("unchecked")
	static Matcher<Map<? extends String, ? extends VriSite>>
			hasSite(Matcher<String> genus, Matcher<String> species, Matcher<? super VriSite> additional) {
		return hasEntry(
				genus, //
				both(hasProperty("siteGenus", genus)) //
						.and(hasProperty("siteSpecies", species)) //
						.and((Matcher<? super Object>) additional)
		);
	}
}
