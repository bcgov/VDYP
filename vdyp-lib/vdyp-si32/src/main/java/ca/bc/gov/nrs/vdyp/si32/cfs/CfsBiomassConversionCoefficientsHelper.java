package ca.bc.gov.nrs.vdyp.si32.cfs;

import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;

public class CfsBiomassConversionCoefficientsHelper<MajorIndexType extends Enum<MajorIndexType>, MinorIndexType extends Enum<MinorIndexType>, ParamType extends Enum<ParamType>> {

	/* pp */ void validateCoefficientArray(
			Class<?> callingClass, CfsBiomassConversionCoefficientsDetails[][] array,
			Class<MajorIndexType> majorIndexType, Class<MinorIndexType> minorIndexType, Class<ParamType> paramListType
	) {

		try {
			if (array.length != ((int) majorIndexType.getMethod("size").invoke(null))) {
				throw new IllegalStateException(
						"CfsBiomassConversionCoefficientsForSpecies does not contain exactly one "
								+ " entry for each CfsBiomassConversionSupportedEcoZone"
				);
			}

			for (int i = 0; i < array.length; i++) {
				if (array[i].length != ((int) minorIndexType.getMethod("size").invoke(null))) {
					throw new IllegalStateException(
							MessageFormat.format(
									callingClass.getName() + " at index {0} does not contain exactly one "
											+ " entry for each CfsBiomassConversionSupportedSpecies",
									i
							)
					);
				}
			}

			int paramListSize = ((int) paramListType.getMethod("size").invoke(null));
			for (int i = 0; i < array.length; i++) {
				for (int j = 0; j < array[i].length; j++) {
					if (array[i][j].parms().length != paramListSize) {
						throw new IllegalStateException(
								MessageFormat.format(
										callingClass.getName()
												+ " at index {0} {1} contains {2} elements, but {3} were expected",
										i, j, array[i][j].parms().length, CfsLiveConversionParams.size()
								)
						);
					}
					if (array[i][j].containsData()) {
						for (int k = 0; k < paramListSize; k++) {
							if (array[i][j].parms()[k] == -9.0f) {
								throw new IllegalStateException(
										MessageFormat.format(
												callingClass.getName()
														+ " at index {0} {1} {2} is recorded as not containing data, but contains -9.0f",
												i, j, k
										)
								);
							}
						}
					} else {
						for (int k = 0; k < paramListSize; k++) {
							if (array[i][j].parms()[k] != -9.0f) {
								throw new IllegalStateException(
										MessageFormat.format(
												callingClass.getName()
														+ " at index {0} {1} {2} is recorded as not containing data, but contains {3}",
												i, j, k, array[i][j].parms()[k]
										)
								);
							}
						}
					}
				}
			}
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
				| SecurityException e) {
			throw new IllegalStateException(e);
		}
	}
}
