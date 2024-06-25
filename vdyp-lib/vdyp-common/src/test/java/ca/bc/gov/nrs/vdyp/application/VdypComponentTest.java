package ca.bc.gov.nrs.vdyp.application;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.jupiter.api.Test;

class VdypComponentTest {

	@Test
	void testLoadProperties() {
		var unit = new VdypComponent();

		assertThat(unit.RESOURCE_COMMENTS, notNullValue());
		assertThat(unit.RESOURCE_COMPANY_NAME, notNullValue());
		assertThat(unit.RESOURCE_VERSION_CSV, notNullValue());
		assertThat(unit.RESOURCE_VERSION_DOT, notNullValue());
		assertThat(unit.RESOURCE_VERSION_FMT, notNullValue());
		assertThat(unit.RESOURCE_SHORT_VERSION, notNullValue());
		assertThat(unit.RESOURCE_FULL_VERSION, notNullValue());
		assertThat(unit.RESOURCE_FULL_VERSION_ABBRV, notNullValue());
		assertThat(unit.RESOURCE_VERSION_DATE, notNullValue());
		assertThat(unit.RESOURCE_VERSION_YYYY, notNullValue());
		assertThat(unit.RESOURCE_VERSION_MM, notNullValue());
		assertThat(unit.RESOURCE_VERSION_MON, notNullValue());
		assertThat(unit.RESOURCE_VERSION_MONTH, notNullValue());
		assertThat(unit.RESOURCE_VERSION_DD, notNullValue());
		assertThat(unit.RESOURCE_VERSION_DATE_YYYY_MM_DD, notNullValue());
		assertThat(unit.RESOURCE_VERSION_DATE_YYYY_MON_DD, notNullValue());
		assertThat(unit.RESOURCE_VERSION_DATE_YYYY_MONTH_DD, notNullValue());
		assertThat(unit.RESOURCE_VERSION_MAJOR_NUM, notNullValue());
		assertThat(unit.RESOURCE_VERSION_MINOR_NUM, notNullValue());
		assertThat(unit.RESOURCE_VERSION_INCR_NUM, notNullValue());
		assertThat(unit.RESOURCE_VERSION_BUILD_NUM, notNullValue());
		assertThat(unit.RESOURCE_FILE_DESCRIPTION, notNullValue());
		assertThat(unit.RESOURCE_FILE_VERSION, notNullValue());
		assertThat(unit.RESOURCE_LEGAL_COPYRIGHT, notNullValue());
		assertThat(unit.RESOURCE_COPYRIGHT_START_YEAR, notNullValue());
		assertThat(unit.RESOURCE_COPYRIGHT_END_YEAR, notNullValue());
		assertThat(unit.RESOURCE_COPYRIGHT_HOLDER, notNullValue());
		assertThat(unit.RESOURCE_COPYRIGHT_FULL, notNullValue());
		assertThat(unit.RESOURCE_LEGAL_TRADEMARKS, notNullValue());
		assertThat(unit.RESOURCE_BINARY_NAME, notNullValue());
		assertThat(unit.RESOURCE_BINARY_TYPE, notNullValue());
		assertThat(unit.RESOURCE_ORIG_FILENAME, notNullValue());
		assertThat(unit.RESOURCE_PRIVATE_BUILD, notNullValue());
		assertThat(unit.RESOURCE_PRODUCT_NAME, notNullValue());
		assertThat(unit.RESOURCE_PRODUCT_VERSION, notNullValue());
		assertThat(unit.RESOURCE_SPECIAL_BUILD, notNullValue());
		assertThat(unit.RESOURCE_VCS_SYSTEM_SPEC, notNullValue());
		assertThat(unit.RESOURCE_VCS_SYSTEM, notNullValue());
		assertThat(unit.RESOURCE_VCS_VERSION, notNullValue());
		assertThat(unit.RESOURCE_VCS, notNullValue());
		assertThat(unit.RESOURCE_VCS_BRANCH, notNullValue());
		assertThat(unit.RESOURCE_VCS_COMMIT_REF, notNullValue());
		assertThat(unit.RESOURCE_VCS_COMMIT_AUTHOR, notNullValue());
		assertThat(unit.RESOURCE_VCS_COMMIT_DATE, notNullValue());
		assertThat(unit.RESOURCE_BLD_MACHINE, notNullValue());
		assertThat(unit.RESOURCE_BLD_COMPILER, notNullValue());
		assertThat(unit.RESOURCE_BLD_COMPILER_VER, notNullValue());
		assertThat(unit.RESOURCE_BLD_COMPILER_SPEC, notNullValue());
		assertThat(unit.RESOURCE_BLD_CONFIG, notNullValue());
		assertThat(unit.RESOURCE_TARGET_OS, notNullValue());
		assertThat(unit.RESOURCE_TARGET_ARCH, notNullValue());
		assertThat(unit.RESOURCE_TARGET_ENV, notNullValue());
		assertThat(unit.RESOURCE_COMPILE_TIMESTAMP, notNullValue());
		assertThat(unit.RESOURCE_COMPILE_DATE, notNullValue());
		assertThat(unit.RESOURCE_COMPILE_TIME, notNullValue());
		assertThat(unit.AVERSION, notNullValue());
	}

}
