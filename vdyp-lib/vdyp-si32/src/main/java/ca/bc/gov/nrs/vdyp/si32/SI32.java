package ca.bc.gov.nrs.vdyp.si32;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SI32 {
	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(SI32.class);

	public static String SiteTool_SINDEXErrorToString(int iSINDEXError) {
		String rtrnStr = null;

		switch (iSINDEXError) {
		case 0:
			rtrnStr = "SUCCESS";
			break;

		case SI32Errors.SI_ERR_LT13:
			rtrnStr = "SI_ERR_LT13";
			break;
		case SI32Errors.SI_ERR_GI_MIN:
			rtrnStr = "SI_ERR_GI_MIN";
			break;
		case SI32Errors.SI_ERR_GI_MAX:
			rtrnStr = "SI_ERR_GI_MAX";
			break;
		case SI32Errors.SI_ERR_NO_ANS:
			rtrnStr = "SI_ERR_NO_ANS";
			break;
		case SI32Errors.SI_ERR_CURVE:
			rtrnStr = "SI_ERR_CURVE";
			break;
		case SI32Errors.SI_ERR_CLASS:
			rtrnStr = "SI_ERR_CLASS";
			break;
		case SI32Errors.SI_ERR_FIZ:
			rtrnStr = "SI_ERR_FIZ";
			break;
		case SI32Errors.SI_ERR_CODE:
			rtrnStr = "SI_ERR_CODE";
			break;
		case SI32Errors.SI_ERR_GI_TOT:
			rtrnStr = "SI_ERR_GI_TOT";
			break;
		case SI32Errors.SI_ERR_SPEC:
			rtrnStr = "SI_ERR_SPEC";
			break;
		case SI32Errors.SI_ERR_AGE_TYPE:
			rtrnStr = "SI_ERR_AGE_TYPE";
			break;
		case SI32Errors.SI_ERR_ESTAB:
			rtrnStr = "SI_ERR_ESTAB";
			break;

		default:
			rtrnStr = "UNKNOWN";
			break;
		}

		return rtrnStr;
	}
}