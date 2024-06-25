package ca.bc.gov.nrs.vdyp.application;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormatSymbols;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Properties;

@SuppressWarnings({ "java:S100", "java:S116" })
public class VdypComponent {
	private static final String COPYRIGHT_HOLDER = "Government of British Columbia";

	private static final String BLANK = "";

	private final Properties properties;

	private final String COMPANY_NAME;
	private final String BINARY_PRODUCT;
	private final String BINARY_EXTENSION;
	private final String VERSION_MAJOR;
	private final String VERSION_MINOR;
	private final String VERSION_INC;
	private final String VERSION_BUILD;
	private final String VERSION_YEAR;
	private final String VERSION_MONTH;
	private final String VERSION_DAY;
	private final String COPYRIGHT_START;
	private final String COPYRIGHT_END;
	private final String VERSION_CONTROL_SYSTEM;
	private final String VERSION_CONTROL_VERSION;
	private final String BRANCH_NAME;
	private final String LAST_COMMIT_REFERENCE;
	private final String LAST_COMMIT_AUTHOR;
	private final String LAST_COMMIT_DATE;
	private final String BUILD_MACHINE;
	private final String ENV_COMPILER;
	private final String ENV_COMPILER_VER;
	private final String ENV_BUILD_CONFIG;
	private final String ENV_OS;
	private final String ENV_ARCH;

	public VdypComponent() {
		this(Thread.currentThread().getContextClassLoader());
	}

	VdypComponent(ClassLoader loader) {
		properties = loadProperties(loader);

		COMPANY_NAME = getProperty("COMPANY_NAME");
		BINARY_PRODUCT = getProperty("BINARY_PRODUCT");
		BINARY_EXTENSION = getProperty("BINARY_EXTENSION");
		VERSION_MAJOR = getProperty("VERSION_MAJOR");
		VERSION_MINOR = getProperty("VERSION_MINOR");
		VERSION_INC = getProperty("VERSION_INC");
		VERSION_BUILD = getProperty("VERSION_BUILD");
		VERSION_YEAR = getProperty("VERSION_YEAR");
		VERSION_MONTH = getProperty("VERSION_MONTH");
		VERSION_DAY = getProperty("VERSION_DAY");
		COPYRIGHT_START = getProperty("COPYRIGHT_START");
		COPYRIGHT_END = getProperty("COPYRIGHT_END");
		VERSION_CONTROL_SYSTEM = getProperty("VERSION_CONTROL_SYSTEM");
		VERSION_CONTROL_VERSION = getProperty("VERSION_CONTROL_VERSION");
		BRANCH_NAME = getProperty("BRANCH_NAME");
		LAST_COMMIT_REFERENCE = getProperty("LAST_COMMIT_REFERENCE");
		LAST_COMMIT_AUTHOR = getProperty("LAST_COMMIT_AUTHOR");
		LAST_COMMIT_DATE = getProperty("LAST_COMMIT_DATE");
		BUILD_MACHINE = getProperty("BUILD_MACHINE");
		ENV_COMPILER = getProperty("ENV_COMPILER");
		ENV_COMPILER_VER = getProperty("ENV_COMPILER_VER");
		ENV_BUILD_CONFIG = getProperty("ENV_BUILD_CONFIG");
		ENV_OS = getProperty("ENV_OS");
		ENV_ARCH = getProperty("ENV_ARCH");

		RESOURCE_COMMENTS = getRESOURCE_COMMENTS();
		RESOURCE_COMPANY_NAME = getRESOURCE_COMPANY_NAME();
		RESOURCE_VERSION_CSV = getRESOURCE_VERSION_CSV();
		RESOURCE_VERSION_DOT = getRESOURCE_VERSION_DOT();
		RESOURCE_VERSION_FMT = getRESOURCE_VERSION_FMT();
		RESOURCE_SHORT_VERSION = getRESOURCE_SHORT_VERSION();
		RESOURCE_FULL_VERSION = getRESOURCE_FULL_VERSION();
		RESOURCE_FULL_VERSION_ABBRV = getRESOURCE_FULL_VERSION_ABBRV();
		RESOURCE_VERSION_DATE = getRESOURCE_VERSION_DATE();
		RESOURCE_VERSION_YYYY = getRESOURCE_VERSION_YYYY();
		RESOURCE_VERSION_MM = getRESOURCE_VERSION_MM();
		RESOURCE_VERSION_MON = getRESOURCE_VERSION_MON();
		RESOURCE_VERSION_MONTH = getRESOURCE_VERSION_MONTH();
		RESOURCE_VERSION_DD = getRESOURCE_VERSION_DD();
		RESOURCE_VERSION_DATE_YYYY_MM_DD = getRESOURCE_VERSION_DATE_YYYY_MM_DD();
		RESOURCE_VERSION_DATE_YYYY_MON_DD = getRESOURCE_VERSION_DATE_YYYY_MON_DD();
		RESOURCE_VERSION_DATE_YYYY_MONTH_DD = getRESOURCE_VERSION_DATE_YYYY_MONTH_DD();
		RESOURCE_VERSION_MAJOR_NUM = getRESOURCE_VERSION_MAJOR_NUM();
		RESOURCE_VERSION_MINOR_NUM = getRESOURCE_VERSION_MINOR_NUM();
		RESOURCE_VERSION_INCR_NUM = getRESOURCE_VERSION_INCR_NUM();
		RESOURCE_VERSION_BUILD_NUM = getRESOURCE_VERSION_BUILD_NUM();
		RESOURCE_FILE_DESCRIPTION = getRESOURCE_FILE_DESCRIPTION();
		RESOURCE_FILE_VERSION = getRESOURCE_FILE_VERSION();
		RESOURCE_LEGAL_COPYRIGHT = getRESOURCE_LEGAL_COPYRIGHT();
		RESOURCE_COPYRIGHT_START_YEAR = getRESOURCE_COPYRIGHT_START_YEAR();
		RESOURCE_COPYRIGHT_END_YEAR = getRESOURCE_COPYRIGHT_END_YEAR();
		RESOURCE_COPYRIGHT_HOLDER = getRESOURCE_COPYRIGHT_HOLDER();
		RESOURCE_COPYRIGHT_FULL = getRESOURCE_COPYRIGHT_FULL();
		RESOURCE_LEGAL_TRADEMARKS = getRESOURCE_LEGAL_TRADEMARKS();
		RESOURCE_BINARY_NAME = getRESOURCE_BINARY_NAME();
		RESOURCE_BINARY_TYPE = getRESOURCE_BINARY_TYPE();
		RESOURCE_ORIG_FILENAME = getRESOURCE_ORIG_FILENAME();
		RESOURCE_PRIVATE_BUILD = getRESOURCE_PRIVATE_BUILD();
		RESOURCE_PRODUCT_NAME = getRESOURCE_PRODUCT_NAME();
		RESOURCE_PRODUCT_VERSION = getRESOURCE_PRODUCT_VERSION();
		RESOURCE_SPECIAL_BUILD = getRESOURCE_SPECIAL_BUILD();
		RESOURCE_VCS_SYSTEM_SPEC = getRESOURCE_VCS_SYSTEM_SPEC();
		RESOURCE_VCS_SYSTEM = getRESOURCE_VCS_SYSTEM();
		RESOURCE_VCS_VERSION = getRESOURCE_VCS_VERSION();
		RESOURCE_VCS = getRESOURCE_VCS();
		RESOURCE_VCS_BRANCH = getRESOURCE_VCS_BRANCH();
		RESOURCE_VCS_COMMIT_REF = getRESOURCE_VCS_COMMIT_REF();
		RESOURCE_VCS_COMMIT_AUTHOR = getRESOURCE_VCS_COMMIT_AUTHOR();
		RESOURCE_VCS_COMMIT_DATE = getRESOURCE_VCS_COMMIT_DATE();
		RESOURCE_BLD_MACHINE = getRESOURCE_BLD_MACHINE();
		RESOURCE_BLD_COMPILER = getRESOURCE_BLD_COMPILER();
		RESOURCE_BLD_COMPILER_VER = getRESOURCE_BLD_COMPILER_VER();
		RESOURCE_BLD_COMPILER_SPEC = getRESOURCE_BLD_COMPILER_SPEC();
		RESOURCE_BLD_CONFIG = getRESOURCE_BLD_CONFIG();
		RESOURCE_TARGET_OS = getRESOURCE_TARGET_OS();
		RESOURCE_TARGET_ARCH = getRESOURCE_TARGET_ARCH();
		RESOURCE_TARGET_ENV = getRESOURCE_TARGET_ENV();
		RESOURCE_COMPILE_TIMESTAMP = getRESOURCE_COMPILE_TIMESTAMP();
		RESOURCE_COMPILE_DATE = getRESOURCE_COMPILE_DATE();
		RESOURCE_COMPILE_TIME = getRESOURCE_COMPILE_TIME();
		AVERSION = getAVERSION();

	}

	private Properties loadProperties(ClassLoader loader) {
		Properties result = new Properties();

		try (InputStream stream = loader.getResourceAsStream("application.properties")) {
			if (stream == null) {
				throw new IllegalStateException("Could not find application.properties on classpath");
			}

			result.load(stream);
		} catch (IOException e) {
			throw new IllegalStateException("Could not load application properties", e);
		}

		return result;
	}

	private String getProperty(String propertyName) {
		return properties.getProperty(propertyName);
	}

	public final String RESOURCE_COMMENTS;
	public final String RESOURCE_COMPANY_NAME;
	public final String RESOURCE_VERSION_CSV;
	public final String RESOURCE_VERSION_DOT;
	public final String RESOURCE_VERSION_FMT;
	public final String RESOURCE_SHORT_VERSION;
	public final String RESOURCE_FULL_VERSION;
	public final String RESOURCE_FULL_VERSION_ABBRV;
	public final String RESOURCE_VERSION_DATE;
	public final String RESOURCE_VERSION_YYYY;
	public final String RESOURCE_VERSION_MM;
	public final String RESOURCE_VERSION_MON;
	public final String RESOURCE_VERSION_MONTH;
	public final String RESOURCE_VERSION_DD;
	public final String RESOURCE_VERSION_DATE_YYYY_MM_DD;
	public final String RESOURCE_VERSION_DATE_YYYY_MON_DD;
	public final String RESOURCE_VERSION_DATE_YYYY_MONTH_DD;
	public final String RESOURCE_VERSION_MAJOR_NUM;
	public final String RESOURCE_VERSION_MINOR_NUM;
	public final String RESOURCE_VERSION_INCR_NUM;
	public final String RESOURCE_VERSION_BUILD_NUM;
	public final String RESOURCE_FILE_DESCRIPTION;
	public final String RESOURCE_FILE_VERSION;
	public final String RESOURCE_LEGAL_COPYRIGHT;
	public final String RESOURCE_COPYRIGHT_START_YEAR;
	public final String RESOURCE_COPYRIGHT_END_YEAR;
	public final String RESOURCE_COPYRIGHT_HOLDER;
	public final String RESOURCE_COPYRIGHT_FULL;
	public final String RESOURCE_LEGAL_TRADEMARKS;
	public final String RESOURCE_BINARY_NAME;
	public final String RESOURCE_BINARY_TYPE;
	public final String RESOURCE_ORIG_FILENAME;
	public final String RESOURCE_PRIVATE_BUILD;
	public final String RESOURCE_PRODUCT_NAME;
	public final String RESOURCE_PRODUCT_VERSION;
	public final String RESOURCE_SPECIAL_BUILD;
	public final String RESOURCE_VCS_SYSTEM_SPEC;
	public final String RESOURCE_VCS_SYSTEM;
	public final String RESOURCE_VCS_VERSION;
	public final String RESOURCE_VCS;
	public final String RESOURCE_VCS_BRANCH;
	public final String RESOURCE_VCS_COMMIT_REF;
	public final String RESOURCE_VCS_COMMIT_AUTHOR;
	public final String RESOURCE_VCS_COMMIT_DATE;
	public final String RESOURCE_BLD_MACHINE;
	public final String RESOURCE_BLD_COMPILER;
	public final String RESOURCE_BLD_COMPILER_VER;
	public final String RESOURCE_BLD_COMPILER_SPEC;
	public final String RESOURCE_BLD_CONFIG;
	public final String RESOURCE_TARGET_OS;
	public final String RESOURCE_TARGET_ARCH;
	public final String RESOURCE_TARGET_ENV;
	public final String RESOURCE_COMPILE_TIMESTAMP;
	public final String RESOURCE_COMPILE_DATE;
	public final String RESOURCE_COMPILE_TIME;
	public final String AVERSION;

	private String getAVERSION() {
		return RESOURCE_SHORT_VERSION + " " + RESOURCE_VERSION_DATE;
	}

	private String getRESOURCE_COMMENTS() {
		return RESOURCE_BINARY_NAME + " " + RESOURCE_FULL_VERSION + " " + RESOURCE_VERSION_DATE;
	}

	private String getRESOURCE_COMPANY_NAME() {
		return COMPANY_NAME;
	}

	private String getRESOURCE_VERSION_CSV() {
		return VERSION_MAJOR + "," + VERSION_MINOR + "," + VERSION_INC + "," + VERSION_BUILD;
	}

	private String getRESOURCE_VERSION_DOT() {
		return VERSION_MAJOR + "." + VERSION_MINOR + "." + VERSION_INC + "." + VERSION_BUILD;
	}

	private String getRESOURCE_VERSION_FMT() {
		return VERSION_MAJOR + ", " + VERSION_MINOR + ", " + VERSION_INC + ", " + VERSION_BUILD;
	}

	private String getRESOURCE_SHORT_VERSION() {
		// VERSION_INC is one-based.
		return VERSION_MAJOR + ", " + VERSION_MINOR + (Character.toString('a' + (Integer.parseInt(VERSION_INC))));
	}

	private String getRESOURCE_FULL_VERSION() {
		return getRESOURCE_SHORT_VERSION() + " (Build " + VERSION_BUILD + ")";
	}

	private String getRESOURCE_FULL_VERSION_ABBRV() {
		return getRESOURCE_SHORT_VERSION() + " " + VERSION_BUILD;
	}

	private String getRESOURCE_VERSION_DATE() {
		return VERSION_YEAR + "-"
				+ new DateFormatSymbols().getShortMonths()[Integer.parseInt(VERSION_MONTH) - 1].substring(0, 3) + "-"
				+ VERSION_DAY;
	}

	private String getRESOURCE_VERSION_YYYY() {
		return VERSION_YEAR;
	}

	private String getRESOURCE_VERSION_MM() {
		return VERSION_MONTH.length() == 1 ? "0" + VERSION_MONTH : VERSION_MONTH;
	}

	private String getRESOURCE_VERSION_MON() {
		return new DateFormatSymbols().getShortMonths()[Integer.parseInt(VERSION_MONTH) - 1].substring(0, 3);
	}

	private String getRESOURCE_VERSION_MONTH() {
		return new DateFormatSymbols().getMonths()[Integer.parseInt(VERSION_MONTH) - 1];
	}

	private String getRESOURCE_VERSION_DD() {
		return VERSION_DAY.length() == 1 ? "0" + VERSION_DAY : VERSION_DAY;
	}

	private String getRESOURCE_VERSION_DATE_YYYY_MM_DD() {
		return getRESOURCE_VERSION_YYYY() + "-" + getRESOURCE_VERSION_MM() + getRESOURCE_VERSION_DD();
	}

	private String getRESOURCE_VERSION_DATE_YYYY_MON_DD() {
		return getRESOURCE_VERSION_YYYY() + "-" + getRESOURCE_VERSION_MON() + getRESOURCE_VERSION_DD();
	}

	private String getRESOURCE_VERSION_DATE_YYYY_MONTH_DD() {
		return getRESOURCE_VERSION_YYYY() + "-" + getRESOURCE_VERSION_MONTH() + getRESOURCE_VERSION_DD();
	}

	private String getRESOURCE_VERSION_MAJOR_NUM() {
		return VERSION_MAJOR;
	}

	private String getRESOURCE_VERSION_MINOR_NUM() {
		return VERSION_MINOR;
	}

	private String getRESOURCE_VERSION_INCR_NUM() {
		return VERSION_INC;
	}

	private String getRESOURCE_VERSION_BUILD_NUM() {
		return VERSION_BUILD;
	}

	private String getRESOURCE_FILE_DESCRIPTION() {
		return BINARY_PRODUCT + " " + RESOURCE_SHORT_VERSION + " (Compiler: " + ENV_COMPILER + ", Build: "
				+ ENV_BUILD_CONFIG + ", Branch: " + BRANCH_NAME + ", Commit: " + LAST_COMMIT_REFERENCE;
	}

	private String getRESOURCE_FILE_VERSION() {
		return RESOURCE_VERSION_FMT;
	}

	private String getRESOURCE_LEGAL_COPYRIGHT() {
		return "Copyright \0xa9 " + COPYRIGHT_START + " - " + COPYRIGHT_END;
	}

	private String getRESOURCE_COPYRIGHT_START_YEAR() {
		return COPYRIGHT_START;
	}

	private String getRESOURCE_COPYRIGHT_END_YEAR() {
		return COPYRIGHT_END;
	}

	private String getRESOURCE_COPYRIGHT_HOLDER() {
		return COPYRIGHT_HOLDER;
	}

	private String getRESOURCE_COPYRIGHT_FULL() {
		return RESOURCE_LEGAL_COPYRIGHT + ", " + RESOURCE_COPYRIGHT_HOLDER;
	}

	private String getRESOURCE_LEGAL_TRADEMARKS() {
		return BLANK;
	}

	private String getRESOURCE_BINARY_NAME() {
		return BINARY_PRODUCT;
	}

	private String getRESOURCE_BINARY_TYPE() {
		return BINARY_EXTENSION;
	}

	private String getRESOURCE_ORIG_FILENAME() {
		return BINARY_PRODUCT + "." + BINARY_EXTENSION;
	}

	private String getRESOURCE_PRIVATE_BUILD() {
		return BLANK;
	}

	private String getRESOURCE_PRODUCT_NAME() {
		return COMPANY_NAME + " " + BINARY_PRODUCT;
	}

	private String getRESOURCE_PRODUCT_VERSION() {
		return RESOURCE_VERSION_FMT;
	}

	private String getRESOURCE_SPECIAL_BUILD() {
		return BLANK;
	}

	private String getRESOURCE_VCS_SYSTEM_SPEC() {
		return VERSION_CONTROL_SYSTEM + " " + VERSION_CONTROL_VERSION;
	}

	private String getRESOURCE_VCS_SYSTEM() {
		return VERSION_CONTROL_SYSTEM;
	}

	private String getRESOURCE_VCS_VERSION() {
		return VERSION_CONTROL_VERSION;
	}

	private String getRESOURCE_VCS() {
		return VERSION_CONTROL_SYSTEM + " " + VERSION_CONTROL_VERSION;
	}

	private String getRESOURCE_VCS_BRANCH() {
		return BRANCH_NAME;
	}

	private String getRESOURCE_VCS_COMMIT_REF() {
		return LAST_COMMIT_REFERENCE;
	}

	private String getRESOURCE_VCS_COMMIT_AUTHOR() {
		return LAST_COMMIT_AUTHOR;
	}

	private String getRESOURCE_VCS_COMMIT_DATE() {
		return LAST_COMMIT_DATE;
	}

	private String getRESOURCE_BLD_MACHINE() {
		return BUILD_MACHINE;
	}

	private String getRESOURCE_BLD_COMPILER() {
		return ENV_COMPILER;
	}

	private String getRESOURCE_BLD_COMPILER_VER() {
		return ENV_COMPILER_VER;
	}

	private String getRESOURCE_BLD_COMPILER_SPEC() {
		return RESOURCE_BLD_COMPILER + " " + RESOURCE_BLD_COMPILER_VER;
	}

	private String getRESOURCE_BLD_CONFIG() {
		return ENV_BUILD_CONFIG;
	}

	private String getRESOURCE_TARGET_OS() {
		return ENV_OS;
	}

	private String getRESOURCE_TARGET_ARCH() {
		return ENV_ARCH;
	}

	private String getRESOURCE_TARGET_ENV() {
		return RESOURCE_TARGET_OS + " " + RESOURCE_TARGET_ARCH;
	}

	private String getRESOURCE_COMPILE_TIMESTAMP() {
		return LocalDateTime.now().toString();
	}

	private String getRESOURCE_COMPILE_DATE() {
		return LocalDate.now().toString();
	}

	private String getRESOURCE_COMPILE_TIME() {
		return LocalTime.now().toString();
	}
}
