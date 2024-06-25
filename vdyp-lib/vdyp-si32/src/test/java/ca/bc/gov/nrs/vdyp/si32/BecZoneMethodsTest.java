package ca.bc.gov.nrs.vdyp.si32;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.Test;

import ca.bc.gov.nrs.vdyp.si32.bec.BecZone;
import ca.bc.gov.nrs.vdyp.si32.bec.BecZoneMethods;
import ca.bc.gov.nrs.vdyp.si32.vdyp.SP64Name;

class BecZoneMethodsTest {

	@Test
	void test_SiteTool_MoFBiomassCoefficient() {
		assertThat(
				BecZoneMethods.mofBiomassCoefficient(BecZone.AT.getText(), SP64Name.A.getText()),
				equalTo(BecZoneMethods.mofBiomassCoeffs[SP64Name.A.getOffset()][BecZone.AT.getOffset()])
		);
		assertThat(
				BecZoneMethods.mofBiomassCoefficient("at", "a"),
				equalTo(BecZoneMethods.mofBiomassCoeffs[SP64Name.A.getOffset()][BecZone.AT.getOffset()])
		);
		assertThat(BecZoneMethods.mofBiomassCoefficient("??", "??"), equalTo(-1.0f));
		assertThat(BecZoneMethods.mofBiomassCoefficient(SP64Name.A.getText(), "??"), equalTo(-1.0f));
	}

	@Test
	void test_SiteTool_IndexToBecZone() {
		assertThat(BecZoneMethods.becZoneToIndex(BecZone.AT.getText()), equalTo(BecZone.AT));
		assertThat(BecZoneMethods.becZoneToIndex(null), equalTo(BecZone.UNKNOWN));
	}

	@Test
	void test_SiteTool_BECZoneToCode() {
		assertThat(BecZoneMethods.becZoneToCode(BecZone.AT), equalTo(BecZone.AT.getText()));
		assertThat(BecZoneMethods.becZoneToCode(null), equalTo(BecZoneMethods.UNKNOWN_BEC_ZONE_TEXT));
	}

	@Test
	void test_VDYP_MofBiomassCoefficient() {
		assertThat(BecZoneMethods.mofBiomassCoefficient(BecZone.AT.getText(), SP64Name.A.getText()), equalTo(0.75226f));
		assertThat(BecZoneMethods.mofBiomassCoefficient(null, SP64Name.A.getText()), equalTo(-1.0f));
		assertThat(BecZoneMethods.mofBiomassCoefficient(BecZone.AT.getText(), null), equalTo(-1.0f));
	}
}