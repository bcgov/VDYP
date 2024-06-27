package ca.bc.gov.nrs.vdyp.common;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

import org.junit.jupiter.api.Test;

import ca.bc.gov.nrs.vdyp.application.ProcessingException;
import ca.bc.gov.nrs.vdyp.model.Coefficients;

class ReconcilationMethodsTest {

	@Test
	void testReconcilationWhenBasalAreaAllIsZero() throws ProcessingException {
		Coefficients basalAreaByUtilization = Utils.utilizationVector(0.0f);
		Coefficients quadMeanDiameterByUtilization = Utils.utilizationVector(0.0f);
		Coefficients treesPerHectareByUtilization = Utils.utilizationVector(0.0f);

		ReconcilationMethods.reconcileComponents(
				basalAreaByUtilization, treesPerHectareByUtilization, quadMeanDiameterByUtilization
		);
		assertThat(treesPerHectareByUtilization, contains(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f));
		assertThat(quadMeanDiameterByUtilization, contains(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f));
	}

	@Test
	void testReconcilationMode2() throws ProcessingException {
		Coefficients basalAreaByUtilization = Utils
				.utilizationVector(0.0f, 0.406989872f, 0.00509467721f, 0.0138180256f, 0.0231454968f, 0.364931673f);
		Coefficients quadMeanDiameterByUtilization = Utils
				.utilizationVector(0.0f, 31.5006275f, 10.0594692f, 14.9665585f, 19.9451237f, 46.0375214f);
		Coefficients treesPerHectareByUtilization = Utils
				.utilizationVector(0.0f, 5.22222185f, 0.64102751f, 0.785438597f, 0.740803719f, 2.19228911f);

		ReconcilationMethods.reconcileComponents(
				basalAreaByUtilization, treesPerHectareByUtilization, quadMeanDiameterByUtilization
		);

		// Results verified against FORTRAN VDYP7 run.
		assertThat(
				treesPerHectareByUtilization, contains(0.0f, 5.222222f, 0.7678731f, 0.94086f, 0.8873928f, 2.626096f)
		);
		assertThat(
				quadMeanDiameterByUtilization, contains(0.0f, 31.500628f, 9.191125f, 13.674629f, 18.22344f, 42.063515f)
		);
	}

	@Test
	void testReconcilationMode1() throws ProcessingException {
		Coefficients basalAreaByUtilization = Utils
				.utilizationVector(0.0f, 0.406989872f, 0.00509467721f, 0.0138180256f, 0.0231454968f, 0.364931673f);
		Coefficients quadMeanDiameterByUtilization = Utils
				.utilizationVector(0.0f, 31.5006275f, 10.0594692f, 14.9665585f, 19.9451237f, 46.0375214f);
		Coefficients treesPerHectareByUtilization = Utils
				.utilizationVector(0.0f, 14.0f, 0.64102751f, 0.785438597f, 0.740803719f, 2.19228911f);

		ReconcilationMethods.reconcileComponents(
				basalAreaByUtilization, treesPerHectareByUtilization, quadMeanDiameterByUtilization
		);

		assertThat(treesPerHectareByUtilization, contains(0.0f, 14.0f, 1.1531991f, 1.1259941f, 4.9625316f, 6.7582755f));
		assertThat(quadMeanDiameterByUtilization, contains(0.0f, 31.500628f, 7.5f, 12.5f, 17.5f, 22.5f));
	}
}
