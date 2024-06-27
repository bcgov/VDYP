package ca.bc.gov.nrs.vdyp.forward;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.IOException;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import ca.bc.gov.nrs.vdyp.application.ProcessingException;
import ca.bc.gov.nrs.vdyp.forward.model.VdypEntity;
import ca.bc.gov.nrs.vdyp.forward.parsers.VdypPolygonDescriptionParser;
import ca.bc.gov.nrs.vdyp.io.parse.common.ResourceParseException;
import ca.bc.gov.nrs.vdyp.model.LayerType;
import static ca.bc.gov.nrs.vdyp.model.UtilizationClass.*;
import static ca.bc.gov.nrs.vdyp.model.VolumeVariable.*;
import static ca.bc.gov.nrs.vdyp.model.SmallUtilizationClassVariable.*;

class SetCompatibilityVariablesForwardProcessingEngineTest extends AbstractForwardProcessingEngineTest {

	@Test
	void testSetCompatibilityVariables() throws ResourceParseException, IOException, ProcessingException {
		var testPolygonDescription = VdypPolygonDescriptionParser.parse("01002 S000001 00     1970");

		var reader = new ForwardDataStreamReader(controlMap);

		var polygon = reader.readNextPolygon(testPolygonDescription);

		ForwardProcessingEngine fpe = new ForwardProcessingEngine(controlMap);
		fpe.processPolygon(polygon, ForwardProcessingEngine.ExecutionStep.SET_COMPATIBILITY_VARIABLES);

		// These values have been verified against the FORTRAN implementation, allowing for minor
		// platform-specific differences.

		assertThat(
				fpe.fps.getPolygonProcessingState().volumeEquationGroups,
				Matchers.is(new int[] { VdypEntity.MISSING_INTEGER_VALUE, 12, 20, 25, 37, 66 })
		);
		assertThat(
				fpe.fps.getPolygonProcessingState().decayEquationGroups,
				Matchers.is(new int[] { VdypEntity.MISSING_INTEGER_VALUE, 7, 14, 19, 31, 54 })
		);
		assertThat(
				fpe.fps.getPolygonProcessingState().breakageEquationGroups,
				Matchers.is(new int[] { VdypEntity.MISSING_INTEGER_VALUE, 5, 6, 12, 17, 28 })
		);

		assertThat(
				fpe.fps.getPolygonProcessingState().getCVVolume(1, U75TO125, CLOSE_UTIL_VOL, LayerType.PRIMARY),
				is(0.0f)
		);
		assertThat(
				fpe.fps.getPolygonProcessingState().getCVVolume(1, U125TO175, CLOSE_UTIL_VOL, LayerType.PRIMARY),
				is(0.0f)
		);
		assertThat(
				fpe.fps.getPolygonProcessingState().getCVVolume(1, U175TO225, CLOSE_UTIL_VOL, LayerType.PRIMARY),
				is(0.0063979626f)
		);
		assertThat(
				fpe.fps.getPolygonProcessingState().getCVVolume(1, OVER225, CLOSE_UTIL_VOL, LayerType.PRIMARY),
				is(-0.00016450882f)
		);

		assertThat(
				fpe.fps.getPolygonProcessingState().getCVVolume(2, U75TO125, CLOSE_UTIL_VOL, LayerType.PRIMARY),
				is(-0.00024962425f)
		);
		assertThat(
				fpe.fps.getPolygonProcessingState().getCVVolume(2, U125TO175, CLOSE_UTIL_VOL, LayerType.PRIMARY),
				is(-0.00011026859f)
		);
		assertThat(
				fpe.fps.getPolygonProcessingState().getCVVolume(2, U175TO225, CLOSE_UTIL_VOL, LayerType.PRIMARY),
				is(-0.000006198883f)
		);
		assertThat(
				fpe.fps.getPolygonProcessingState().getCVVolume(2, OVER225, CLOSE_UTIL_VOL, LayerType.PRIMARY),
				is(0.000024557114f)
		);

		assertThat(
				fpe.fps.getPolygonProcessingState().getCVVolume(3, U75TO125, CLOSE_UTIL_VOL, LayerType.PRIMARY),
				is(0.0062299743f)
		);
		assertThat(
				fpe.fps.getPolygonProcessingState().getCVVolume(3, U125TO175, CLOSE_UTIL_VOL, LayerType.PRIMARY),
				is(0.0010375977f)
		);
		assertThat(
				fpe.fps.getPolygonProcessingState().getCVVolume(3, U175TO225, CLOSE_UTIL_VOL, LayerType.PRIMARY),
				is(0.0001244545f)
		);
		assertThat(
				fpe.fps.getPolygonProcessingState().getCVVolume(3, OVER225, CLOSE_UTIL_VOL, LayerType.PRIMARY),
				is(0.0000038146973f)
		);

		assertThat(
				fpe.fps.getPolygonProcessingState().getCVVolume(4, U75TO125, CLOSE_UTIL_VOL, LayerType.PRIMARY),
				is(-0.00013566017f)
		);
		assertThat(
				fpe.fps.getPolygonProcessingState().getCVVolume(4, U125TO175, CLOSE_UTIL_VOL, LayerType.PRIMARY),
				is(0.00033128262f)
		);
		assertThat(
				fpe.fps.getPolygonProcessingState().getCVVolume(4, U175TO225, CLOSE_UTIL_VOL, LayerType.PRIMARY),
				is(0.00021290779f)
		);
		assertThat(
				fpe.fps.getPolygonProcessingState().getCVVolume(4, OVER225, CLOSE_UTIL_VOL, LayerType.PRIMARY),
				is(-0.0000059604645f)
		);

		assertThat(
				fpe.fps.getPolygonProcessingState().getCVVolume(5, U75TO125, CLOSE_UTIL_VOL, LayerType.PRIMARY),
				is(-0.000882864f)
		);
		assertThat(
				fpe.fps.getPolygonProcessingState().getCVVolume(5, U125TO175, CLOSE_UTIL_VOL, LayerType.PRIMARY),
				is(-0.0002478361f)
		);
		assertThat(
				fpe.fps.getPolygonProcessingState().getCVVolume(5, U175TO225, CLOSE_UTIL_VOL, LayerType.PRIMARY),
				is(0.0008614063f)
		);
		assertThat(
				fpe.fps.getPolygonProcessingState().getCVVolume(5, OVER225, CLOSE_UTIL_VOL, LayerType.PRIMARY),
				is(-0.0000052452087f)
		);

		assertThat(
				fpe.fps.getPolygonProcessingState()
						.getCVVolume(1, U75TO125, CLOSE_UTIL_VOL_LESS_DECAY, LayerType.PRIMARY),
				is(0.0f)
		);
		assertThat(
				fpe.fps.getPolygonProcessingState()
						.getCVVolume(1, U125TO175, CLOSE_UTIL_VOL_LESS_DECAY, LayerType.PRIMARY),
				is(0.0f)
		);
		assertThat(
				fpe.fps.getPolygonProcessingState()
						.getCVVolume(1, U175TO225, CLOSE_UTIL_VOL_LESS_DECAY, LayerType.PRIMARY),
				is(-0.004629612f)
		);
		assertThat(
				fpe.fps.getPolygonProcessingState()
						.getCVVolume(1, OVER225, CLOSE_UTIL_VOL_LESS_DECAY, LayerType.PRIMARY),
				is(1.3446808E-4f)
		);

		assertThat(
				fpe.fps.getPolygonProcessingState()
						.getCVVolume(2, U75TO125, CLOSE_UTIL_VOL_LESS_DECAY, LayerType.PRIMARY),
				is(0.01768279f)
		);
		assertThat(
				fpe.fps.getPolygonProcessingState()
						.getCVVolume(2, U125TO175, CLOSE_UTIL_VOL_LESS_DECAY, LayerType.PRIMARY),
				is(0.0010006428f)
		);
		assertThat(
				fpe.fps.getPolygonProcessingState()
						.getCVVolume(2, U175TO225, CLOSE_UTIL_VOL_LESS_DECAY, LayerType.PRIMARY),
				is(2.2292137E-4f)
		);
		assertThat(
				fpe.fps.getPolygonProcessingState()
						.getCVVolume(2, OVER225, CLOSE_UTIL_VOL_LESS_DECAY, LayerType.PRIMARY),
				is(-1.9311905E-5f)
		);

		assertThat(
				fpe.fps.getPolygonProcessingState()
						.getCVVolume(3, U75TO125, CLOSE_UTIL_VOL_LESS_DECAY, LayerType.PRIMARY),
				is(0.0f)
		);
		assertThat(
				fpe.fps.getPolygonProcessingState()
						.getCVVolume(3, U125TO175, CLOSE_UTIL_VOL_LESS_DECAY, LayerType.PRIMARY),
				is(0.010708809f)
		);
		assertThat(
				fpe.fps.getPolygonProcessingState()
						.getCVVolume(3, U175TO225, CLOSE_UTIL_VOL_LESS_DECAY, LayerType.PRIMARY),
				is(7.638931E-4f)
		);
		assertThat(
				fpe.fps.getPolygonProcessingState()
						.getCVVolume(3, OVER225, CLOSE_UTIL_VOL_LESS_DECAY, LayerType.PRIMARY),
				is(-2.4795532E-5f)
		);

		assertThat(
				fpe.fps.getPolygonProcessingState()
						.getCVVolume(4, U75TO125, CLOSE_UTIL_VOL_LESS_DECAY, LayerType.PRIMARY),
				is(0.011518478f)
		);
		assertThat(
				fpe.fps.getPolygonProcessingState()
						.getCVVolume(4, U125TO175, CLOSE_UTIL_VOL_LESS_DECAY, LayerType.PRIMARY),
				is(-0.0010123253f)
		);
		assertThat(
				fpe.fps.getPolygonProcessingState()
						.getCVVolume(4, U175TO225, CLOSE_UTIL_VOL_LESS_DECAY, LayerType.PRIMARY),
				is(-9.3603134E-4f)
		);
		assertThat(
				fpe.fps.getPolygonProcessingState()
						.getCVVolume(4, OVER225, CLOSE_UTIL_VOL_LESS_DECAY, LayerType.PRIMARY),
				is(-5.4836273E-5f)
		);

		assertThat(
				fpe.fps.getPolygonProcessingState()
						.getCVVolume(5, U75TO125, CLOSE_UTIL_VOL_LESS_DECAY, LayerType.PRIMARY),
				is(0.0f)
		);
		assertThat(
				fpe.fps.getPolygonProcessingState()
						.getCVVolume(5, U125TO175, CLOSE_UTIL_VOL_LESS_DECAY, LayerType.PRIMARY),
				is(0.010197163f)
		);
		assertThat(
				fpe.fps.getPolygonProcessingState()
						.getCVVolume(5, U175TO225, CLOSE_UTIL_VOL_LESS_DECAY, LayerType.PRIMARY),
				is(-0.0014338493f)
		);
		assertThat(
				fpe.fps.getPolygonProcessingState()
						.getCVVolume(5, OVER225, CLOSE_UTIL_VOL_LESS_DECAY, LayerType.PRIMARY),
				is(-2.9087067E-5f)
		);

		assertThat(
				fpe.fps.getPolygonProcessingState()
						.getCVVolume(1, U75TO125, CLOSE_UTIL_VOL_LESS_DECAY_LESS_WASTAGE, LayerType.PRIMARY),
				is(0.0f)
		);
		assertThat(
				fpe.fps.getPolygonProcessingState()
						.getCVVolume(1, U125TO175, CLOSE_UTIL_VOL_LESS_DECAY_LESS_WASTAGE, LayerType.PRIMARY),
				is(0.0f)
		);
		assertThat(
				fpe.fps.getPolygonProcessingState()
						.getCVVolume(1, U175TO225, CLOSE_UTIL_VOL_LESS_DECAY_LESS_WASTAGE, LayerType.PRIMARY),
				is(0.035768032f)
		);
		assertThat(
				fpe.fps.getPolygonProcessingState()
						.getCVVolume(1, OVER225, CLOSE_UTIL_VOL_LESS_DECAY_LESS_WASTAGE, LayerType.PRIMARY),
				is(-0.0016698837f)
		);

		assertThat(
				fpe.fps.getPolygonProcessingState()
						.getCVVolume(2, U75TO125, CLOSE_UTIL_VOL_LESS_DECAY_LESS_WASTAGE, LayerType.PRIMARY),
				is(-0.16244507f)
		);
		assertThat(
				fpe.fps.getPolygonProcessingState()
						.getCVVolume(2, U125TO175, CLOSE_UTIL_VOL_LESS_DECAY_LESS_WASTAGE, LayerType.PRIMARY),
				is(-0.0045113564f)
		);
		assertThat(
				fpe.fps.getPolygonProcessingState()
						.getCVVolume(2, U175TO225, CLOSE_UTIL_VOL_LESS_DECAY_LESS_WASTAGE, LayerType.PRIMARY),
				is(-0.0030164719f)
		);
		assertThat(
				fpe.fps.getPolygonProcessingState()
						.getCVVolume(2, OVER225, CLOSE_UTIL_VOL_LESS_DECAY_LESS_WASTAGE, LayerType.PRIMARY),
				is(3.528595E-5f)
		);

		assertThat(
				fpe.fps.getPolygonProcessingState()
						.getCVVolume(3, U75TO125, CLOSE_UTIL_VOL_LESS_DECAY_LESS_WASTAGE, LayerType.PRIMARY),
				is(0.0f)
		);
		assertThat(
				fpe.fps.getPolygonProcessingState()
						.getCVVolume(3, U125TO175, CLOSE_UTIL_VOL_LESS_DECAY_LESS_WASTAGE, LayerType.PRIMARY),
				is(0.0f)
		);
		assertThat(
				fpe.fps.getPolygonProcessingState()
						.getCVVolume(3, U175TO225, CLOSE_UTIL_VOL_LESS_DECAY_LESS_WASTAGE, LayerType.PRIMARY),
				is(0.0f)
		);
		assertThat(
				fpe.fps.getPolygonProcessingState()
						.getCVVolume(3, OVER225, CLOSE_UTIL_VOL_LESS_DECAY_LESS_WASTAGE, LayerType.PRIMARY),
				is(4.1484833E-5f)
		);

		assertThat(
				fpe.fps.getPolygonProcessingState()
						.getCVVolume(4, U75TO125, CLOSE_UTIL_VOL_LESS_DECAY_LESS_WASTAGE, LayerType.PRIMARY),
				is(-0.13775301f)
		);
		assertThat(
				fpe.fps.getPolygonProcessingState()
						.getCVVolume(4, U125TO175, CLOSE_UTIL_VOL_LESS_DECAY_LESS_WASTAGE, LayerType.PRIMARY),
				is(0.005630493f)
		);
		assertThat(
				fpe.fps.getPolygonProcessingState()
						.getCVVolume(4, U175TO225, CLOSE_UTIL_VOL_LESS_DECAY_LESS_WASTAGE, LayerType.PRIMARY),
				is(0.0028266907f)
		);
		assertThat(
				fpe.fps.getPolygonProcessingState()
						.getCVVolume(4, OVER225, CLOSE_UTIL_VOL_LESS_DECAY_LESS_WASTAGE, LayerType.PRIMARY),
				is(3.7765503E-4f)
		);

		assertThat(
				fpe.fps.getPolygonProcessingState()
						.getCVVolume(5, U75TO125, CLOSE_UTIL_VOL_LESS_DECAY_LESS_WASTAGE, LayerType.PRIMARY),
				is(0.0f)
		);
		assertThat(
				fpe.fps.getPolygonProcessingState()
						.getCVVolume(5, U125TO175, CLOSE_UTIL_VOL_LESS_DECAY_LESS_WASTAGE, LayerType.PRIMARY),
				is(0.0f)
		);
		assertThat(
				fpe.fps.getPolygonProcessingState()
						.getCVVolume(5, U175TO225, CLOSE_UTIL_VOL_LESS_DECAY_LESS_WASTAGE, LayerType.PRIMARY),
				is(0.0f)
		);
		assertThat(
				fpe.fps.getPolygonProcessingState()
						.getCVVolume(5, OVER225, CLOSE_UTIL_VOL_LESS_DECAY_LESS_WASTAGE, LayerType.PRIMARY),
				is(5.378723E-4f)
		);

		assertThat(
				fpe.fps.getPolygonProcessingState().getCVVolume(1, U75TO125, WHOLE_STEM_VOL, LayerType.PRIMARY),
				is(0.0f)
		);
		assertThat(
				fpe.fps.getPolygonProcessingState().getCVVolume(1, U125TO175, WHOLE_STEM_VOL, LayerType.PRIMARY),
				is(3.684759E-4f)
		);
		assertThat(
				fpe.fps.getPolygonProcessingState().getCVVolume(1, U175TO225, WHOLE_STEM_VOL, LayerType.PRIMARY),
				is(0.0027964115f)
		);
		assertThat(
				fpe.fps.getPolygonProcessingState().getCVVolume(1, OVER225, WHOLE_STEM_VOL, LayerType.PRIMARY),
				is(-9.393692E-5f)
		);

		assertThat(
				fpe.fps.getPolygonProcessingState().getCVVolume(2, U75TO125, WHOLE_STEM_VOL, LayerType.PRIMARY),
				is(-3.0636787E-5f)
		);
		assertThat(
				fpe.fps.getPolygonProcessingState().getCVVolume(2, U125TO175, WHOLE_STEM_VOL, LayerType.PRIMARY),
				is(4.1246414E-5f)
		);
		assertThat(
				fpe.fps.getPolygonProcessingState().getCVVolume(2, U175TO225, WHOLE_STEM_VOL, LayerType.PRIMARY),
				is(-2.527237E-5f)
		);
		assertThat(
				fpe.fps.getPolygonProcessingState().getCVVolume(2, OVER225, WHOLE_STEM_VOL, LayerType.PRIMARY),
				is(9.536743E-7f)
		);

		assertThat(
				fpe.fps.getPolygonProcessingState().getCVVolume(3, U75TO125, WHOLE_STEM_VOL, LayerType.PRIMARY),
				is(0.001360178f)
		);
		assertThat(
				fpe.fps.getPolygonProcessingState().getCVVolume(3, U125TO175, WHOLE_STEM_VOL, LayerType.PRIMARY),
				is(4.8589706E-4f)
		);
		assertThat(
				fpe.fps.getPolygonProcessingState().getCVVolume(3, U175TO225, WHOLE_STEM_VOL, LayerType.PRIMARY),
				is(4.4107437E-5f)
		);
		assertThat(
				fpe.fps.getPolygonProcessingState().getCVVolume(3, OVER225, WHOLE_STEM_VOL, LayerType.PRIMARY),
				is(-1.1920929E-6f)
		);

		assertThat(
				fpe.fps.getPolygonProcessingState().getCVVolume(4, U75TO125, WHOLE_STEM_VOL, LayerType.PRIMARY),
				is(-5.042553E-5f)
		);
		assertThat(
				fpe.fps.getPolygonProcessingState().getCVVolume(4, U125TO175, WHOLE_STEM_VOL, LayerType.PRIMARY),
				is(5.4240227E-5f)
		);
		assertThat(
				fpe.fps.getPolygonProcessingState().getCVVolume(4, U175TO225, WHOLE_STEM_VOL, LayerType.PRIMARY),
				is(6.2704086E-5f)
		);
		assertThat(
				fpe.fps.getPolygonProcessingState().getCVVolume(4, OVER225, WHOLE_STEM_VOL, LayerType.PRIMARY),
				is(-2.169609E-5f)
		);

		assertThat(
				fpe.fps.getPolygonProcessingState().getCVVolume(5, U75TO125, WHOLE_STEM_VOL, LayerType.PRIMARY),
				is(1.3077259E-4f)
		);
		assertThat(
				fpe.fps.getPolygonProcessingState().getCVVolume(5, U125TO175, WHOLE_STEM_VOL, LayerType.PRIMARY),
				is(-1.21593475E-5f)
		);
		assertThat(
				fpe.fps.getPolygonProcessingState().getCVVolume(5, U175TO225, WHOLE_STEM_VOL, LayerType.PRIMARY),
				is(2.3770332E-4f)
		);
		assertThat(
				fpe.fps.getPolygonProcessingState().getCVVolume(5, OVER225, WHOLE_STEM_VOL, LayerType.PRIMARY),
				is(-1.2636185E-5f)
		);

		assertThat(
				fpe.fps.getPolygonProcessingState().getCVBasalArea(1, OVER225, LayerType.PRIMARY), is(-2.065301E-5f)
		);
		assertThat(
				fpe.fps.getPolygonProcessingState().getCVBasalArea(1, U125TO175, LayerType.PRIMARY), is(1.0924414E-5f)
		);
		assertThat(
				fpe.fps.getPolygonProcessingState().getCVBasalArea(1, U175TO225, LayerType.PRIMARY), is(1.0116026E-5f)
		);
		assertThat(
				fpe.fps.getPolygonProcessingState().getCVBasalArea(1, U75TO125, LayerType.PRIMARY), is(-3.9674342E-7f)
		);

		assertThat(
				fpe.fps.getPolygonProcessingState().getCVBasalArea(2, OVER225, LayerType.PRIMARY), is(-1.1444092E-5f)
		);
		assertThat(
				fpe.fps.getPolygonProcessingState().getCVBasalArea(2, U125TO175, LayerType.PRIMARY), is(5.00679E-6f)
		);
		assertThat(
				fpe.fps.getPolygonProcessingState().getCVBasalArea(2, U175TO225, LayerType.PRIMARY), is(9.596348E-6f)
		);
		assertThat(
				fpe.fps.getPolygonProcessingState().getCVBasalArea(2, U75TO125, LayerType.PRIMARY), is(6.660819E-6f)
		);

		assertThat(
				fpe.fps.getPolygonProcessingState().getCVBasalArea(3, OVER225, LayerType.PRIMARY), is(7.6293945E-6f)
		);
		assertThat(
				fpe.fps.getPolygonProcessingState().getCVBasalArea(3, U125TO175, LayerType.PRIMARY), is(-3.7066638E-6f)
		);
		assertThat(
				fpe.fps.getPolygonProcessingState().getCVBasalArea(3, U175TO225, LayerType.PRIMARY), is(-1.0579824E-5f)
		);
		assertThat(
				fpe.fps.getPolygonProcessingState().getCVBasalArea(3, U75TO125, LayerType.PRIMARY), is(-2.4344772E-6f)
		);

		assertThat(fpe.fps.getPolygonProcessingState().getCVBasalArea(4, OVER225, LayerType.PRIMARY), is(3.194809E-5f));
		assertThat(
				fpe.fps.getPolygonProcessingState().getCVBasalArea(4, U125TO175, LayerType.PRIMARY), is(-1.4662743E-5f)
		);
		assertThat(
				fpe.fps.getPolygonProcessingState().getCVBasalArea(4, U175TO225, LayerType.PRIMARY), is(-9.059906E-6f)
		);
		assertThat(
				fpe.fps.getPolygonProcessingState().getCVBasalArea(4, U75TO125, LayerType.PRIMARY), is(-8.106232E-6f)
		);

		assertThat(
				fpe.fps.getPolygonProcessingState().getCVBasalArea(5, OVER225, LayerType.PRIMARY), is(-4.673004E-5f)
		);
		assertThat(
				fpe.fps.getPolygonProcessingState().getCVBasalArea(5, U125TO175, LayerType.PRIMARY), is(8.821487E-6f)
		);
		assertThat(
				fpe.fps.getPolygonProcessingState().getCVBasalArea(5, U175TO225, LayerType.PRIMARY), is(2.8401613E-5f)
		);
		assertThat(
				fpe.fps.getPolygonProcessingState().getCVBasalArea(5, U75TO125, LayerType.PRIMARY), is(-4.954636E-7f)
		);

		assertThat(
				fpe.fps.getPolygonProcessingState().getCVQuadraticMeanDiameter(1, OVER225, LayerType.PRIMARY),
				is(-0.0054130554f)
		);
		assertThat(
				fpe.fps.getPolygonProcessingState().getCVQuadraticMeanDiameter(1, U125TO175, LayerType.PRIMARY),
				is(-0.011018753f)
		);
		assertThat(
				fpe.fps.getPolygonProcessingState().getCVQuadraticMeanDiameter(1, U175TO225, LayerType.PRIMARY),
				is(-0.0404377f)
		);
		assertThat(
				fpe.fps.getPolygonProcessingState().getCVQuadraticMeanDiameter(1, U75TO125, LayerType.PRIMARY),
				is(-0.018251419f)
		);

		assertThat(
				fpe.fps.getPolygonProcessingState().getCVQuadraticMeanDiameter(2, OVER225, LayerType.PRIMARY),
				is(7.209778E-4f)
		);
		assertThat(
				fpe.fps.getPolygonProcessingState().getCVQuadraticMeanDiameter(2, U125TO175, LayerType.PRIMARY),
				is(-2.0122528E-4f)
		);
		assertThat(
				fpe.fps.getPolygonProcessingState().getCVQuadraticMeanDiameter(2, U175TO225, LayerType.PRIMARY),
				is(6.0272217E-4f)
		);
		assertThat(
				fpe.fps.getPolygonProcessingState().getCVQuadraticMeanDiameter(2, U75TO125, LayerType.PRIMARY),
				is(-1.2207031E-4f)
		);

		assertThat(
				fpe.fps.getPolygonProcessingState().getCVQuadraticMeanDiameter(3, OVER225, LayerType.PRIMARY),
				is(-1.7929077E-4f)
		);
		assertThat(
				fpe.fps.getPolygonProcessingState().getCVQuadraticMeanDiameter(3, U125TO175, LayerType.PRIMARY),
				is(-0.008193016f)
		);
		assertThat(
				fpe.fps.getPolygonProcessingState().getCVQuadraticMeanDiameter(3, U175TO225, LayerType.PRIMARY),
				is(-0.0019302368f)
		);
		assertThat(
				fpe.fps.getPolygonProcessingState().getCVQuadraticMeanDiameter(3, U75TO125, LayerType.PRIMARY),
				is(-0.008533478f)
		);

		assertThat(
				fpe.fps.getPolygonProcessingState().getCVQuadraticMeanDiameter(4, OVER225, LayerType.PRIMARY),
				is(-0.0016155243f)
		);
		assertThat(
				fpe.fps.getPolygonProcessingState().getCVQuadraticMeanDiameter(4, U125TO175, LayerType.PRIMARY),
				is(-7.4863434E-4f)
		);
		assertThat(
				fpe.fps.getPolygonProcessingState().getCVQuadraticMeanDiameter(4, U175TO225, LayerType.PRIMARY),
				is(-0.0012569427f)
		);
		assertThat(
				fpe.fps.getPolygonProcessingState().getCVQuadraticMeanDiameter(4, U75TO125, LayerType.PRIMARY),
				is(1.9168854E-4f)
		);

		assertThat(
				fpe.fps.getPolygonProcessingState().getCVQuadraticMeanDiameter(5, OVER225, LayerType.PRIMARY),
				is(-0.002658844f)
		);
		assertThat(
				fpe.fps.getPolygonProcessingState().getCVQuadraticMeanDiameter(5, U125TO175, LayerType.PRIMARY),
				is(0.0011692047f)
		);
		assertThat(
				fpe.fps.getPolygonProcessingState().getCVQuadraticMeanDiameter(5, U175TO225, LayerType.PRIMARY),
				is(-0.005224228f)
		);
		assertThat(
				fpe.fps.getPolygonProcessingState().getCVQuadraticMeanDiameter(5, U75TO125, LayerType.PRIMARY),
				is(-6.6947937E-4f)
		);

		assertThat(fpe.fps.getPolygonProcessingState().getCVSmall(1, BASAL_AREA), is(-2.1831444E-7f));
		assertThat(fpe.fps.getPolygonProcessingState().getCVSmall(1, QUAD_MEAN_DIAMETER), is(0.0f));
		assertThat(fpe.fps.getPolygonProcessingState().getCVSmall(1, LOREY_HEIGHT), is(0.0f));
		assertThat(fpe.fps.getPolygonProcessingState().getCVSmall(1, WHOLE_STEM_VOLUME), is(0.0f));

		assertThat(fpe.fps.getPolygonProcessingState().getCVSmall(2, BASAL_AREA), is(-4.4927E-5f));
		assertThat(fpe.fps.getPolygonProcessingState().getCVSmall(2, QUAD_MEAN_DIAMETER), is(0.0023670197f));
		assertThat(fpe.fps.getPolygonProcessingState().getCVSmall(2, LOREY_HEIGHT), is(3.576278E-7f));
		assertThat(fpe.fps.getPolygonProcessingState().getCVSmall(2, WHOLE_STEM_VOLUME), is(0.0010275329f));

		assertThat(fpe.fps.getPolygonProcessingState().getCVSmall(3, BASAL_AREA), is(4.946138E-6f));
		assertThat(fpe.fps.getPolygonProcessingState().getCVSmall(3, QUAD_MEAN_DIAMETER), is(0.0f));
		assertThat(fpe.fps.getPolygonProcessingState().getCVSmall(3, LOREY_HEIGHT), is(-4.827988E-6f));
		assertThat(fpe.fps.getPolygonProcessingState().getCVSmall(3, WHOLE_STEM_VOLUME), is(0.0f));

		assertThat(fpe.fps.getPolygonProcessingState().getCVSmall(4, BASAL_AREA), is(0.0f));
		assertThat(fpe.fps.getPolygonProcessingState().getCVSmall(4, QUAD_MEAN_DIAMETER), is(0.0f));
		assertThat(fpe.fps.getPolygonProcessingState().getCVSmall(4, LOREY_HEIGHT), is(0.0f));
		assertThat(fpe.fps.getPolygonProcessingState().getCVSmall(4, WHOLE_STEM_VOLUME), is(0.0f));

		assertThat(fpe.fps.getPolygonProcessingState().getCVSmall(5, BASAL_AREA), is(3.4186523E-6f));
		assertThat(fpe.fps.getPolygonProcessingState().getCVSmall(5, QUAD_MEAN_DIAMETER), is(0.0f));
		assertThat(fpe.fps.getPolygonProcessingState().getCVSmall(5, LOREY_HEIGHT), is(1.800044E-5f));
		assertThat(fpe.fps.getPolygonProcessingState().getCVSmall(5, WHOLE_STEM_VOLUME), is(0.0f));
	}
}
