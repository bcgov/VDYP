package ca.bc.gov.nrs.vdyp.si32.cfs;

/**
 * A two-dimensional array indexed by {@link CfsBiomassConversionSupportedEcoZone} and then
 * {@link CfsBiomassConversionSupportedGenera} giving the dead biomass conversion coefficients for that Eco Zone and
 * Genus. Each array element is a record indicating whether it "contains data" - that is, has meaningful values and, if
 * so, an array of floats indexed by {@link CfsDeadConversionParams}.
 * <p>
 * The values are derived from
 * <ul>
 * <li>'C Conversion Factors Initializers' column of the
 * <li>'DeadBiomassParams' table found on the
 * <li>'Derived C Species Table' tab in the
 * <li>'BC_Inventory_updates_by_CBMv2bs.xlsx' located in the
 * <li>'Documents/CFS-Biomass' folder.
 * </ul>
 */
public class CfsBiomassConversionCoefficientsDead {

	public static CfsBiomassConversionCoefficientsDetails
			get(int cfsSupportedEcoZoneIndex, int cfsSupportedGeneraIndex) {
		return array[cfsSupportedEcoZoneIndex][cfsSupportedGeneraIndex];
	}

	private static final CfsBiomassConversionCoefficientsDetails[][] array = new CfsBiomassConversionCoefficientsDetails[][] {
			{ //
					new CfsBiomassConversionCoefficientsDetails(
							true,
							new float[] { 0.04000000f, 0.04100000f, 0.05300000f, 0.08900000f, 0.13500000f,
									108.00000000f, 263.50000000f, 495.50000000f, 934.00000000f }
					),
					new CfsBiomassConversionCoefficientsDetails(
							true,
							new float[] { 0.66700000f, 0.66700000f, 0.33100000f, 0.29600000f, 0.19200000f, 44.50000000f,
									102.00000000f, 195.00000000f, 399.00000000f }
					),
					new CfsBiomassConversionCoefficientsDetails(
							true,
							new float[] { 0.33800000f, 0.22700000f, 0.18100000f, 0.14900000f, 0.07400000f,
									368.00000000f, 762.00000000f, 1340.00000000f, 2140.00000000f }
					),
					new CfsBiomassConversionCoefficientsDetails(
							false,
							new float[] { -9.00000000f, -9.00000000f, -9.00000000f, -9.00000000f, -9.00000000f,
									-9.00000000f, -9.00000000f, -9.00000000f, -9.00000000f }
					),
					new CfsBiomassConversionCoefficientsDetails(
							true,
							new float[] { 0.39400000f, 0.01700000f, 0.06700000f, 0.02300000f, 0.12400000f, 26.50000000f,
									69.00000000f, 141.00000000f, 269.00000000f }
					),
					new CfsBiomassConversionCoefficientsDetails(
							true,
							new float[] { 0.21800000f, 0.20000000f, 0.15100000f, 0.15300000f, 0.13800000f, 89.00000000f,
									177.50000000f, 291.00000000f, 533.00000000f }
					),
					new CfsBiomassConversionCoefficientsDetails(
							false,
							new float[] { -9.00000000f, -9.00000000f, -9.00000000f, -9.00000000f, -9.00000000f,
									-9.00000000f, -9.00000000f, -9.00000000f, -9.00000000f }
					),
					new CfsBiomassConversionCoefficientsDetails(
							true,
							new float[] { 0.30700000f, 0.16000000f, 0.21000000f, 0.18900000f, 0.14700000f,
									301.50000000f, 560.50000000f, 911.50000000f, 1414.50000000f }
					),
					new CfsBiomassConversionCoefficientsDetails(
							true,
							new float[] { 0.17500000f, 0.20100000f, 0.20100000f, 0.18500000f, 0.27400000f,
									116.00000000f, 210.50000000f, 314.00000000f, 569.00000000f }
					),
					new CfsBiomassConversionCoefficientsDetails(
							true,
							new float[] { 0.04000000f, 0.04100000f, 0.05300000f, 0.08900000f, 0.13500000f,
									108.00000000f, 263.50000000f, 495.50000000f, 934.00000000f }
					),
					new CfsBiomassConversionCoefficientsDetails(
							true,
							new float[] { 0.22800000f, 0.21000000f, 0.03300000f, 0.19700000f, 0.13100000f, 59.00000000f,
									123.50000000f, 228.50000000f, 431.50000000f }
					),
					new CfsBiomassConversionCoefficientsDetails(
							false,
							new float[] { -9.00000000f, -9.00000000f, -9.00000000f, -9.00000000f, -9.00000000f,
									-9.00000000f, -9.00000000f, -9.00000000f, -9.00000000f }
					),
					new CfsBiomassConversionCoefficientsDetails(
							false,
							new float[] { -9.00000000f, -9.00000000f, -9.00000000f, -9.00000000f, -9.00000000f,
									-9.00000000f, -9.00000000f, -9.00000000f, -9.00000000f }
					),
					new CfsBiomassConversionCoefficientsDetails(
							true,
							new float[] { 0.25200000f, 0.09000000f, 0.15400000f, 0.13100000f, 0.08100000f,
									115.00000000f, 327.50000000f, 583.00000000f, 879.00000000f }
					),
					new CfsBiomassConversionCoefficientsDetails(
							false,
							new float[] { -9.00000000f, -9.00000000f, -9.00000000f, -9.00000000f, -9.00000000f,
									-9.00000000f, -9.00000000f, -9.00000000f, -9.00000000f }
					),
					new CfsBiomassConversionCoefficientsDetails(
							false,
							new float[] { -9.00000000f, -9.00000000f, -9.00000000f, -9.00000000f, -9.00000000f,
									-9.00000000f, -9.00000000f, -9.00000000f, -9.00000000f }
					),
					new CfsBiomassConversionCoefficientsDetails(
							false,
							new float[] { -9.00000000f, -9.00000000f, -9.00000000f, -9.00000000f, -9.00000000f,
									-9.00000000f, -9.00000000f, -9.00000000f, -9.00000000f }
					),
					new CfsBiomassConversionCoefficientsDetails(
							true,
							new float[] { 0.04000000f, 0.04100000f, 0.05300000f, 0.08900000f, 0.13500000f,
									108.00000000f, 263.50000000f, 495.50000000f, 934.00000000f }
					),
					new CfsBiomassConversionCoefficientsDetails(
							true,
							new float[] { 0.25200000f, 0.09000000f, 0.15400000f, 0.13100000f, 0.08100000f,
									115.00000000f, 327.50000000f, 583.00000000f, 879.00000000f }
					),
					new CfsBiomassConversionCoefficientsDetails(
							true,
							new float[] { 0.04000000f, 0.04100000f, 0.05300000f, 0.08900000f, 0.13500000f,
									108.00000000f, 263.50000000f, 495.50000000f, 934.00000000f }
					) },
			{ //
					new CfsBiomassConversionCoefficientsDetails(
							true,
							new float[] { 0.11000000f, 0.06100000f, 0.04900000f, 0.07200000f, 0.10100000f, 79.50000000f,
									177.50000000f, 301.50000000f, 534.50000000f }
					),
					new CfsBiomassConversionCoefficientsDetails(
							true,
							new float[] { 0.32000000f, 0.29900000f, 0.37700000f, 0.47900000f, 0.35000000f,
									140.00000000f, 264.00000000f, 409.50000000f, 685.50000000f }
					),
					new CfsBiomassConversionCoefficientsDetails(
							true,
							new float[] { 0.33800000f, 0.22700000f, 0.18100000f, 0.14900000f, 0.07400000f,
									368.00000000f, 762.00000000f, 1340.00000000f, 2140.00000000f }
					),
					new CfsBiomassConversionCoefficientsDetails(
							false,
							new float[] { -9.00000000f, -9.00000000f, -9.00000000f, -9.00000000f, -9.00000000f,
									-9.00000000f, -9.00000000f, -9.00000000f, -9.00000000f }
					),
					new CfsBiomassConversionCoefficientsDetails(
							true,
							new float[] { 0.00000000f, 0.12000000f, 0.00800000f, 0.05100000f, 0.10000000f, 15.50000000f,
									41.00000000f, 120.50000000f, 298.00000000f }
					),
					new CfsBiomassConversionCoefficientsDetails(
							true,
							new float[] { 0.21800000f, 0.20000000f, 0.15100000f, 0.15300000f, 0.13800000f, 89.00000000f,
									177.50000000f, 291.00000000f, 533.00000000f }
					),
					new CfsBiomassConversionCoefficientsDetails(
							false,
							new float[] { -9.00000000f, -9.00000000f, -9.00000000f, -9.00000000f, -9.00000000f,
									-9.00000000f, -9.00000000f, -9.00000000f, -9.00000000f }
					),
					new CfsBiomassConversionCoefficientsDetails(
							true,
							new float[] { 0.30700000f, 0.16000000f, 0.21000000f, 0.18900000f, 0.14700000f,
									301.50000000f, 560.50000000f, 911.50000000f, 1414.50000000f }
					),
					new CfsBiomassConversionCoefficientsDetails(
							true,
							new float[] { 0.17500000f, 0.20100000f, 0.20100000f, 0.18500000f, 0.27400000f,
									116.00000000f, 210.50000000f, 314.00000000f, 569.00000000f }
					),
					new CfsBiomassConversionCoefficientsDetails(
							true,
							new float[] { 0.11000000f, 0.06100000f, 0.04900000f, 0.07200000f, 0.10100000f, 79.50000000f,
									177.50000000f, 301.50000000f, 534.50000000f }
					),
					new CfsBiomassConversionCoefficientsDetails(
							true,
							new float[] { 0.08900000f, 0.06500000f, 0.06200000f, 0.06100000f, 0.16500000f, 85.50000000f,
									179.00000000f, 273.50000000f, 500.50000000f }
					),
					new CfsBiomassConversionCoefficientsDetails(
							false,
							new float[] { -9.00000000f, -9.00000000f, -9.00000000f, -9.00000000f, -9.00000000f,
									-9.00000000f, -9.00000000f, -9.00000000f, -9.00000000f }
					),
					new CfsBiomassConversionCoefficientsDetails(
							false,
							new float[] { -9.00000000f, -9.00000000f, -9.00000000f, -9.00000000f, -9.00000000f,
									-9.00000000f, -9.00000000f, -9.00000000f, -9.00000000f }
					),
					new CfsBiomassConversionCoefficientsDetails(
							true,
							new float[] { 0.14200000f, 0.13100000f, 0.11100000f, 0.14000000f, 0.12100000f,
									217.50000000f, 451.50000000f, 675.00000000f, 901.00000000f }
					),
					new CfsBiomassConversionCoefficientsDetails(
							false,
							new float[] { -9.00000000f, -9.00000000f, -9.00000000f, -9.00000000f, -9.00000000f,
									-9.00000000f, -9.00000000f, -9.00000000f, -9.00000000f }
					),
					new CfsBiomassConversionCoefficientsDetails(
							false,
							new float[] { -9.00000000f, -9.00000000f, -9.00000000f, -9.00000000f, -9.00000000f,
									-9.00000000f, -9.00000000f, -9.00000000f, -9.00000000f }
					),
					new CfsBiomassConversionCoefficientsDetails(
							false,
							new float[] { -9.00000000f, -9.00000000f, -9.00000000f, -9.00000000f, -9.00000000f,
									-9.00000000f, -9.00000000f, -9.00000000f, -9.00000000f }
					),
					new CfsBiomassConversionCoefficientsDetails(
							true,
							new float[] { 0.11000000f, 0.06100000f, 0.04900000f, 0.07200000f, 0.10100000f, 79.50000000f,
									177.50000000f, 301.50000000f, 534.50000000f }
					),
					new CfsBiomassConversionCoefficientsDetails(
							true,
							new float[] { 0.14200000f, 0.13100000f, 0.11100000f, 0.14000000f, 0.12100000f,
									217.50000000f, 451.50000000f, 675.00000000f, 901.00000000f }
					),
					new CfsBiomassConversionCoefficientsDetails(
							true,
							new float[] { 0.11000000f, 0.06100000f, 0.04900000f, 0.07200000f, 0.10100000f, 79.50000000f,
									177.50000000f, 301.50000000f, 534.50000000f }
					) },
			{ //
					new CfsBiomassConversionCoefficientsDetails(
							true,
							new float[] { 0.15600000f, 0.10800000f, 0.14500000f, 0.11000000f, 0.10400000f, 45.00000000f,
									126.50000000f, 236.00000000f, 415.00000000f }
					),
					new CfsBiomassConversionCoefficientsDetails(
							true,
							new float[] { 0.66700000f, 0.66700000f, 0.33100000f, 0.29600000f, 0.19200000f, 44.50000000f,
									102.00000000f, 195.00000000f, 399.00000000f }
					),
					new CfsBiomassConversionCoefficientsDetails(
							true,
							new float[] { 0.33800000f, 0.22700000f, 0.18100000f, 0.14900000f, 0.07400000f,
									368.00000000f, 762.00000000f, 1340.00000000f, 2140.00000000f }
					),
					new CfsBiomassConversionCoefficientsDetails(
							false,
							new float[] { -9.00000000f, -9.00000000f, -9.00000000f, -9.00000000f, -9.00000000f,
									-9.00000000f, -9.00000000f, -9.00000000f, -9.00000000f }
					),
					new CfsBiomassConversionCoefficientsDetails(
							true,
							new float[] { 0.02800000f, 0.07200000f, 0.02800000f, 0.10100000f, 0.02500000f, 79.00000000f,
									175.00000000f, 290.50000000f, 422.00000000f }
					),
					new CfsBiomassConversionCoefficientsDetails(
							true,
							new float[] { 0.21800000f, 0.20000000f, 0.15100000f, 0.15300000f, 0.13800000f, 89.00000000f,
									177.50000000f, 291.00000000f, 533.00000000f }
					),
					new CfsBiomassConversionCoefficientsDetails(
							false,
							new float[] { -9.00000000f, -9.00000000f, -9.00000000f, -9.00000000f, -9.00000000f,
									-9.00000000f, -9.00000000f, -9.00000000f, -9.00000000f }
					),
					new CfsBiomassConversionCoefficientsDetails(
							true,
							new float[] { 0.13800000f, 0.15900000f, 0.22300000f, 0.23700000f, 0.20400000f,
									392.00000000f, 826.50000000f, 1268.50000000f, 1861.50000000f }
					),
					new CfsBiomassConversionCoefficientsDetails(
							true,
							new float[] { 0.17500000f, 0.20100000f, 0.20100000f, 0.18500000f, 0.27400000f,
									116.00000000f, 210.50000000f, 314.00000000f, 569.00000000f }
					),
					new CfsBiomassConversionCoefficientsDetails(
							true,
							new float[] { 0.18500000f, 0.07400000f, 0.11500000f, 0.18600000f, 0.09600000f,
									348.00000000f, 520.50000000f, 699.00000000f, 1022.50000000f }
					),
					new CfsBiomassConversionCoefficientsDetails(
							true,
							new float[] { 0.08500000f, 0.11100000f, 0.10700000f, 0.15100000f, 0.09800000f, 65.50000000f,
									134.50000000f, 208.00000000f, 357.50000000f }
					),
					new CfsBiomassConversionCoefficientsDetails(
							false,
							new float[] { -9.00000000f, -9.00000000f, -9.00000000f, -9.00000000f, -9.00000000f,
									-9.00000000f, -9.00000000f, -9.00000000f, -9.00000000f }
					),
					new CfsBiomassConversionCoefficientsDetails(
							false,
							new float[] { -9.00000000f, -9.00000000f, -9.00000000f, -9.00000000f, -9.00000000f,
									-9.00000000f, -9.00000000f, -9.00000000f, -9.00000000f }
					),
					new CfsBiomassConversionCoefficientsDetails(
							true,
							new float[] { 0.29700000f, 0.18700000f, 0.15000000f, 0.14500000f, 0.16000000f, 78.00000000f,
									159.50000000f, 256.50000000f, 467.50000000f }
					),
					new CfsBiomassConversionCoefficientsDetails(
							false,
							new float[] { -9.00000000f, -9.00000000f, -9.00000000f, -9.00000000f, -9.00000000f,
									-9.00000000f, -9.00000000f, -9.00000000f, -9.00000000f }
					),
					new CfsBiomassConversionCoefficientsDetails(
							false,
							new float[] { -9.00000000f, -9.00000000f, -9.00000000f, -9.00000000f, -9.00000000f,
									-9.00000000f, -9.00000000f, -9.00000000f, -9.00000000f }
					),
					new CfsBiomassConversionCoefficientsDetails(
							false,
							new float[] { -9.00000000f, -9.00000000f, -9.00000000f, -9.00000000f, -9.00000000f,
									-9.00000000f, -9.00000000f, -9.00000000f, -9.00000000f }
					),
					new CfsBiomassConversionCoefficientsDetails(
							true,
							new float[] { 0.24200000f, 0.27900000f, 0.12000000f, 0.16300000f, 0.16900000f,
									160.00000000f, 287.00000000f, 414.00000000f, 634.50000000f }
					),
					new CfsBiomassConversionCoefficientsDetails(
							true,
							new float[] { 0.29700000f, 0.18700000f, 0.15000000f, 0.14500000f, 0.16000000f, 78.00000000f,
									159.50000000f, 256.50000000f, 467.50000000f }
					),
					new CfsBiomassConversionCoefficientsDetails(
							true,
							new float[] { 0.15600000f, 0.10800000f, 0.14500000f, 0.11000000f, 0.10400000f, 45.00000000f,
									126.50000000f, 236.00000000f, 415.00000000f }
					) },
			{ //
					new CfsBiomassConversionCoefficientsDetails(
							true,
							new float[] { 0.13100000f, 0.11200000f, 0.09300000f, 0.06300000f, 0.07900000f,
									157.00000000f, 345.50000000f, 663.00000000f, 1263.50000000f }
					),
					new CfsBiomassConversionCoefficientsDetails(
							true,
							new float[] { 0.20900000f, 0.18800000f, 0.21500000f, 0.27600000f, 0.23300000f,
									304.00000000f, 803.00000000f, 1331.00000000f, 2010.00000000f }
					),
					new CfsBiomassConversionCoefficientsDetails(
							true,
							new float[] { 0.12400000f, 0.18800000f, 0.27100000f, 0.28000000f, 0.22000000f,
									455.00000000f, 892.50000000f, 1425.00000000f, 2174.50000000f }
					),
					new CfsBiomassConversionCoefficientsDetails(
							false,
							new float[] { -9.00000000f, -9.00000000f, -9.00000000f, -9.00000000f, -9.00000000f,
									-9.00000000f, -9.00000000f, -9.00000000f, -9.00000000f }
					),
					new CfsBiomassConversionCoefficientsDetails(
							true,
							new float[] { 0.45200000f, 0.01400000f, 0.08300000f, 0.04200000f, 0.16400000f,
									166.50000000f, 249.00000000f, 333.00000000f, 511.50000000f }
					),
					new CfsBiomassConversionCoefficientsDetails(
							true,
							new float[] { 0.21800000f, 0.22400000f, 0.17500000f, 0.20300000f, 0.17200000f,
									239.50000000f, 467.50000000f, 754.00000000f, 1360.50000000f }
					),
					new CfsBiomassConversionCoefficientsDetails(
							false,
							new float[] { -9.00000000f, -9.00000000f, -9.00000000f, -9.00000000f, -9.00000000f,
									-9.00000000f, -9.00000000f, -9.00000000f, -9.00000000f }
					),
					new CfsBiomassConversionCoefficientsDetails(
							true,
							new float[] { 0.13800000f, 0.15900000f, 0.22300000f, 0.23700000f, 0.20400000f,
									392.00000000f, 826.50000000f, 1268.50000000f, 1861.50000000f }
					),
					new CfsBiomassConversionCoefficientsDetails(
							true,
							new float[] { 0.17500000f, 0.20100000f, 0.20100000f, 0.18500000f, 0.27400000f,
									116.00000000f, 210.50000000f, 314.00000000f, 569.00000000f }
					),
					new CfsBiomassConversionCoefficientsDetails(
							true,
							new float[] { 0.18500000f, 0.07400000f, 0.11500000f, 0.18600000f, 0.09600000f,
									348.00000000f, 520.50000000f, 699.00000000f, 1022.50000000f }
					),
					new CfsBiomassConversionCoefficientsDetails(
							true,
							new float[] { 0.00500000f, 0.09300000f, 0.08800000f, 0.12200000f, 0.15900000f, 38.50000000f,
									125.00000000f, 241.50000000f, 441.50000000f }
					),
					new CfsBiomassConversionCoefficientsDetails(
							false,
							new float[] { -9.00000000f, -9.00000000f, -9.00000000f, -9.00000000f, -9.00000000f,
									-9.00000000f, -9.00000000f, -9.00000000f, -9.00000000f }
					),
					new CfsBiomassConversionCoefficientsDetails(
							false,
							new float[] { -9.00000000f, -9.00000000f, -9.00000000f, -9.00000000f, -9.00000000f,
									-9.00000000f, -9.00000000f, -9.00000000f, -9.00000000f }
					),
					new CfsBiomassConversionCoefficientsDetails(
							true,
							new float[] { 0.16900000f, 0.20500000f, 0.19700000f, 0.18300000f, 0.14700000f,
									433.00000000f, 838.00000000f, 1233.50000000f, 1882.50000000f }
					),
					new CfsBiomassConversionCoefficientsDetails(
							false,
							new float[] { -9.00000000f, -9.00000000f, -9.00000000f, -9.00000000f, -9.00000000f,
									-9.00000000f, -9.00000000f, -9.00000000f, -9.00000000f }
					),
					new CfsBiomassConversionCoefficientsDetails(
							false,
							new float[] { -9.00000000f, -9.00000000f, -9.00000000f, -9.00000000f, -9.00000000f,
									-9.00000000f, -9.00000000f, -9.00000000f, -9.00000000f }
					),
					new CfsBiomassConversionCoefficientsDetails(
							false,
							new float[] { -9.00000000f, -9.00000000f, -9.00000000f, -9.00000000f, -9.00000000f,
									-9.00000000f, -9.00000000f, -9.00000000f, -9.00000000f }
					),
					new CfsBiomassConversionCoefficientsDetails(
							true,
							new float[] { 0.24200000f, 0.27900000f, 0.12000000f, 0.16300000f, 0.16900000f,
									160.00000000f, 287.00000000f, 414.00000000f, 634.50000000f }
					),
					new CfsBiomassConversionCoefficientsDetails(
							true,
							new float[] { 0.13800000f, 0.15900000f, 0.22300000f, 0.23700000f, 0.20400000f,
									392.00000000f, 826.50000000f, 1268.50000000f, 1861.50000000f }
					),
					new CfsBiomassConversionCoefficientsDetails(
							true,
							new float[] { 0.13100000f, 0.11200000f, 0.09300000f, 0.06300000f, 0.07900000f,
									157.00000000f, 345.50000000f, 663.00000000f, 1263.50000000f }
					) },
			{ //
					new CfsBiomassConversionCoefficientsDetails(
							true,
							new float[] { 0.08800000f, 0.12600000f, 0.09800000f, 0.08100000f, 0.09200000f, 89.00000000f,
									181.50000000f, 301.50000000f, 676.00000000f }
					),
					new CfsBiomassConversionCoefficientsDetails(
							true,
							new float[] { 0.32000000f, 0.29900000f, 0.37700000f, 0.47900000f, 0.35000000f,
									140.00000000f, 264.00000000f, 409.50000000f, 685.50000000f }
					),
					new CfsBiomassConversionCoefficientsDetails(
							true,
							new float[] { 0.33800000f, 0.22700000f, 0.18100000f, 0.14900000f, 0.07400000f,
									368.00000000f, 762.00000000f, 1340.00000000f, 2140.00000000f }
					),
					new CfsBiomassConversionCoefficientsDetails(
							false,
							new float[] { -9.00000000f, -9.00000000f, -9.00000000f, -9.00000000f, -9.00000000f,
									-9.00000000f, -9.00000000f, -9.00000000f, -9.00000000f }
					),
					new CfsBiomassConversionCoefficientsDetails(
							true,
							new float[] { 0.29900000f, 0.10900000f, 0.10300000f, 0.08500000f, 0.11800000f, 65.00000000f,
									136.50000000f, 218.50000000f, 388.50000000f }
					),
					new CfsBiomassConversionCoefficientsDetails(
							true,
							new float[] { 0.21800000f, 0.20000000f, 0.15100000f, 0.15300000f, 0.13800000f, 89.00000000f,
									177.50000000f, 291.00000000f, 533.00000000f }
					),
					new CfsBiomassConversionCoefficientsDetails(
							false,
							new float[] { -9.00000000f, -9.00000000f, -9.00000000f, -9.00000000f, -9.00000000f,
									-9.00000000f, -9.00000000f, -9.00000000f, -9.00000000f }
					),
					new CfsBiomassConversionCoefficientsDetails(
							true,
							new float[] { 0.30700000f, 0.16000000f, 0.21000000f, 0.18900000f, 0.14700000f,
									301.50000000f, 560.50000000f, 911.50000000f, 1414.50000000f }
					),
					new CfsBiomassConversionCoefficientsDetails(
							true,
							new float[] { 0.17500000f, 0.20100000f, 0.20100000f, 0.18500000f, 0.27400000f,
									116.00000000f, 210.50000000f, 314.00000000f, 569.00000000f }
					),
					new CfsBiomassConversionCoefficientsDetails(
							true,
							new float[] { 0.18500000f, 0.07400000f, 0.11500000f, 0.18600000f, 0.09600000f,
									348.00000000f, 520.50000000f, 699.00000000f, 1022.50000000f }
					),
					new CfsBiomassConversionCoefficientsDetails(
							true,
							new float[] { 0.17400000f, 0.10600000f, 0.09700000f, 0.11600000f, 0.12600000f, 77.50000000f,
									175.50000000f, 289.00000000f, 507.50000000f }
					),
					new CfsBiomassConversionCoefficientsDetails(
							false,
							new float[] { -9.00000000f, -9.00000000f, -9.00000000f, -9.00000000f, -9.00000000f,
									-9.00000000f, -9.00000000f, -9.00000000f, -9.00000000f }
					),
					new CfsBiomassConversionCoefficientsDetails(
							false,
							new float[] { -9.00000000f, -9.00000000f, -9.00000000f, -9.00000000f, -9.00000000f,
									-9.00000000f, -9.00000000f, -9.00000000f, -9.00000000f }
					),
					new CfsBiomassConversionCoefficientsDetails(
							true,
							new float[] { 0.20200000f, 0.21000000f, 0.26800000f, 0.27300000f, 0.18900000f,
									202.50000000f, 380.50000000f, 575.50000000f, 869.00000000f }
					),
					new CfsBiomassConversionCoefficientsDetails(
							false,
							new float[] { -9.00000000f, -9.00000000f, -9.00000000f, -9.00000000f, -9.00000000f,
									-9.00000000f, -9.00000000f, -9.00000000f, -9.00000000f }
					),
					new CfsBiomassConversionCoefficientsDetails(
							false,
							new float[] { -9.00000000f, -9.00000000f, -9.00000000f, -9.00000000f, -9.00000000f,
									-9.00000000f, -9.00000000f, -9.00000000f, -9.00000000f }
					),
					new CfsBiomassConversionCoefficientsDetails(
							false,
							new float[] { -9.00000000f, -9.00000000f, -9.00000000f, -9.00000000f, -9.00000000f,
									-9.00000000f, -9.00000000f, -9.00000000f, -9.00000000f }
					),
					new CfsBiomassConversionCoefficientsDetails(
							true,
							new float[] { 0.24200000f, 0.27900000f, 0.12000000f, 0.16300000f, 0.16900000f,
									160.00000000f, 287.00000000f, 414.00000000f, 634.50000000f }
					),
					new CfsBiomassConversionCoefficientsDetails(
							true,
							new float[] { 0.21800000f, 0.20000000f, 0.15100000f, 0.15300000f, 0.13800000f, 89.00000000f,
									177.50000000f, 291.00000000f, 533.00000000f }
					),
					new CfsBiomassConversionCoefficientsDetails(
							true,
							new float[] { 0.08800000f, 0.12600000f, 0.09800000f, 0.08100000f, 0.09200000f, 89.00000000f,
									181.50000000f, 301.50000000f, 676.00000000f }
					) } };

	static {
		new CfsBiomassConversionCoefficientsHelper<CfsBiomassConversionSupportedEcoZone, CfsBiomassConversionSupportedGenera, CfsDeadConversionParams>()
				.validateCoefficientArray(
						CfsBiomassConversionCoefficientsDead.class, array, CfsBiomassConversionSupportedEcoZone.class,
						CfsBiomassConversionSupportedGenera.class, CfsDeadConversionParams.class
				);
	}
}
