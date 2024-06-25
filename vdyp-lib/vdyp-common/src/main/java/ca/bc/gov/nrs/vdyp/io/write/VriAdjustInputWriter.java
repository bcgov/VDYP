package ca.bc.gov.nrs.vdyp.io.write;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Stream;

import ca.bc.gov.nrs.vdyp.common.ControlKey;
import ca.bc.gov.nrs.vdyp.common.Utils;
import ca.bc.gov.nrs.vdyp.common_calculators.BaseAreaTreeDensityDiameter;
import ca.bc.gov.nrs.vdyp.io.FileResolver;
import ca.bc.gov.nrs.vdyp.io.parse.coe.GenusDefinitionParser;
import ca.bc.gov.nrs.vdyp.model.BaseVdypSpecies;
import ca.bc.gov.nrs.vdyp.model.PolygonMode;
import ca.bc.gov.nrs.vdyp.model.LayerType;
import ca.bc.gov.nrs.vdyp.model.UtilizationClass;
import ca.bc.gov.nrs.vdyp.model.VdypLayer;
import ca.bc.gov.nrs.vdyp.model.VdypPolygon;
import ca.bc.gov.nrs.vdyp.model.VdypSpecies;
import ca.bc.gov.nrs.vdyp.model.VdypUtilizationHolder;

/**
 * Write files to be input into VRI Adjust.
 */
public class VriAdjustInputWriter implements Closeable {

	private Map<String, Object> controlMap;
	private OutputStream polygonFile;
	private OutputStream speciesFile;
	private OutputStream utilizationFile;

	static final String POLY_IDENTIFIER_FORMAT = "%-25s";
	static final String LAYER_TYPE_FORMAT = "%-1s";
	static final String SPEC_IDENTIFIER_FORMAT = "%-2s";
	static final String SPEC_INDEX_FORMAT = "%2d";
	static final String SPEC_DIST_FORMAT = "%-3s%5.1f";
	static final String DISTANCE_FORMAT = "%6.2f";
	static final String AGE_FORMAT = "%6.1f";

	static final float EMPTY_FLOAT = -9f;
	static final int EMPTY_INT = -9;

	// FORMAT(A25, 1x,A4,1x,A1,I6, I3, I3 , I3 )
	static final String POLY_FORMAT = POLY_IDENTIFIER_FORMAT + " %-4s %1s%6d%3d%3d%3d\n";

	// FORMAT(A25,1x,A1,1x,I2,1x,A2,1x,A32,2f6.2,3f6.1,I2,I3)
	static final String SPEC_FORMAT = POLY_IDENTIFIER_FORMAT + " " + LAYER_TYPE_FORMAT + " " + SPEC_INDEX_FORMAT + " "
			+ SPEC_IDENTIFIER_FORMAT + " " + SPEC_DIST_FORMAT.repeat(4) + DISTANCE_FORMAT.repeat(2)
			+ AGE_FORMAT.repeat(3) + "%2d%3d\n";

	// 082E004 615 1988 P 0 0 19.97867 1485.82 13.0660 117.9938 67.7539 67.0665
	// 66.8413 65.4214 13.1
	// FORMAT (A25, 1x, A1, 1x, I2, 1x, A2, I3,
	// 1 F9.5, F9.2, F9.4, 5F9.4, F6.1)
	static final String UTIL_FORMAT = POLY_IDENTIFIER_FORMAT + " " + LAYER_TYPE_FORMAT + " " + SPEC_INDEX_FORMAT + " "
			+ SPEC_IDENTIFIER_FORMAT + "%3d%9.5f%9.2f%9.4f%9.4f%9.4f%9.4f%9.4f%9.4f%6.1f\n";

	static final String END_RECORD_FORMAT = POLY_IDENTIFIER_FORMAT + "  \n";

	/**
	 * Create a writer for VRI Adjust input files using provided OutputStreams. The Streams will be closed when the
	 * writer is closed.
	 *
	 * @param polygonFile
	 * @param speciesFile
	 * @param utilizationFile
	 * @param controlMap
	 */
	public VriAdjustInputWriter(
			OutputStream polygonFile, OutputStream speciesFile, OutputStream utilizationFile,
			Map<String, Object> controlMap
	) {
		this.controlMap = controlMap;
		this.polygonFile = polygonFile;
		this.speciesFile = speciesFile;
		this.utilizationFile = utilizationFile;
	}

	/**
	 * Create a writer for VRI Adjust input files configured using the given control map.
	 *
	 * @param polygonFile
	 * @param speciesFile
	 * @param utilizationFile
	 * @param controlMap
	 */
	public VriAdjustInputWriter(Map<String, Object> controlMap, FileResolver resolver) throws IOException {
		this(
				getOutputStream(controlMap, resolver, ControlKey.VRI_OUTPUT_VDYP_POLYGON.name()),
				getOutputStream(controlMap, resolver, ControlKey.VRI_OUTPUT_VDYP_LAYER_BY_SPECIES.name()),
				getOutputStream(controlMap, resolver, ControlKey.VRI_OUTPUT_VDYP_LAYER_BY_SP0_BY_UTIL.name()),
				controlMap
		);
	}

	static OutputStream getOutputStream(Map<String, Object> controlMap, FileResolver resolver, String key)
			throws IOException {
		String fileName = Utils.expectParsedControl(controlMap, key, String.class);
		return resolver.resolveForOutput(fileName);
	}

	// V7W_AIP
	/**
	 * Write a polygon record to the polygon file
	 *
	 * @param polygon
	 * @throws IOException
	 */
	void writePolygon(VdypPolygon polygon) throws IOException {
		writeFormat(
				polygonFile, //
				POLY_FORMAT, //

				polygon.getPolygonIdentifier(), //
				polygon.getBiogeoclimaticZone(), //
				polygon.getForestInventoryZone(), //

				polygon.getPercentAvailable().intValue(), //
				polygon.getLayers().get(LayerType.PRIMARY).getInventoryTypeGroup().orElse(EMPTY_INT), //
				polygon.getLayers().get(LayerType.PRIMARY).getEmpiricalRelationshipParameterIndex().orElse(EMPTY_INT), //
				polygon.getMode().orElse(PolygonMode.START).getCode()
		);
	}

	/**
	 * Write a species record to the species file
	 *
	 * @param layer
	 * @param spec
	 * @throws IOException
	 */
	void writeSpecies(VdypLayer layer, VdypSpecies spec) throws IOException {

		// Ensure we have a list of 4 distribution entries
		var specDistributionEntries = Stream.concat(
				spec.getSpeciesPercent().entrySet().stream().sorted(Utils.compareUsing(Entry::getValue)),
				Stream.generate(() -> new AbstractMap.SimpleEntry<String, Float>("", 0f))
		).limit(4).toList();
		// 082E004 615 1988 P 9 L LW 100.0 0.0 0.0 0.0 -9.00 -9.00 -9.0 -9.0 -9.0 0 -9
		var specIndex = GenusDefinitionParser.getIndex(spec.getGenus(), controlMap);
		boolean isSiteSpec = layer.getSiteGenus().map(spec.getGenus()::equals).orElse(false);
		writeFormat(
				speciesFile, //
				SPEC_FORMAT, //

				spec.getPolygonIdentifier(), //
				spec.getLayerType().getAlias(), //

				specIndex.orElse(0), //
				spec.getGenus(), //

				specDistributionEntries.get(0).getKey(), //
				specDistributionEntries.get(0).getValue(), //
				specDistributionEntries.get(1).getKey(), //
				specDistributionEntries.get(1).getValue(), //
				specDistributionEntries.get(2).getKey(), //
				specDistributionEntries.get(2).getValue(), //
				specDistributionEntries.get(3).getKey(), //
				specDistributionEntries.get(3).getValue(), //

				layer.getSiteIndex().filter(x -> isSiteSpec).orElse(EMPTY_FLOAT), //
				layer.getHeight().filter(x -> isSiteSpec).orElse(EMPTY_FLOAT), //
				layer.getAgeTotal().filter(x -> isSiteSpec).orElse(EMPTY_FLOAT), //
				layer.getBreastHeightAge().filter(x -> isSiteSpec).orElse(EMPTY_FLOAT), //
				layer.getYearsToBreastHeight().filter(x -> isSiteSpec).orElse(EMPTY_FLOAT), //
				layer.getSiteGenus().filter(x -> isSiteSpec).map(id -> id.equals(spec.getGenus())).orElse(false) ? 1
						: 0, //
				layer.getSiteCurveNumber().filter(x -> isSiteSpec).orElse(EMPTY_INT)

		);

	}

	/**
	 * Write the utilization records for a layer or species to the utilization file.
	 *
	 * @param layer
	 * @param utils
	 * @throws IOException
	 */
	// V7W_AIU Internalized loop over utilization classes
	void writeUtilization(VdypLayer layer, VdypUtilizationHolder utils) throws IOException {
		Optional<String> specId = Optional.empty();
		if (utils instanceof VdypSpecies spec) {
			specId = Optional.of(spec.getGenus());
		}

		Optional<Integer> specIndex = specId.flatMap(id -> GenusDefinitionParser.getIndex(id, controlMap));

		for (var uc : UtilizationClass.values()) {
			Optional<Float> height = Optional.empty();
			if (uc.index < 1) {
				height = Optional.of(utils.getLoreyHeightByUtilization().getCoe(uc.index)).filter(x -> x > 0f);
			}
			Optional<Float> quadMeanDiameter = Optional.empty();
			if (utils.getBaseAreaByUtilization().getCoe(uc.index) > 0) {
				quadMeanDiameter = Optional.of(
						BaseAreaTreeDensityDiameter.quadMeanDiameter(
								utils.getBaseAreaByUtilization().getCoe(uc.index),
								utils.getTreesPerHectareByUtilization().getCoe(uc.index)
						)
				);
			}

			writeFormat(
					utilizationFile, //
					UTIL_FORMAT, //

					layer.getPolygonIdentifier(), //
					layer.getLayerType().getAlias(), //

					specIndex.orElse(0), //
					specId.orElse("  "), //

					uc.index,

					utils.getBaseAreaByUtilization().getCoe(uc.index), //
					utils.getTreesPerHectareByUtilization().getCoe(uc.index), //
					height.orElse(EMPTY_FLOAT), //

					utils.getWholeStemVolumeByUtilization().getCoe(uc.index), //
					utils.getCloseUtilizationVolumeByUtilization().getCoe(uc.index), //
					utils.getCloseUtilizationVolumeNetOfDecayByUtilization().getCoe(uc.index), //
					utils.getCloseUtilizationVolumeNetOfDecayAndWasteByUtilization().getCoe(uc.index), //
					utils.getCloseUtilizationVolumeNetOfDecayWasteAndBreakageByUtilization().getCoe(uc.index), //

					quadMeanDiameter.orElse(layer.getLayerType() == LayerType.PRIMARY ? //
							EMPTY_FLOAT : 0f
					) // FIXME: VDYP7 is being inconsistent. Should consider using -9 for both.
			);
		}
	}

	/**
	 * Output a polygon and its children.
	 *
	 * @param polygon
	 * @throws IOException
	 */
	// VDYP_OUT when JPROGRAM = 1 (FIPSTART) or 3 (VRISTART)
	public void writePolygonWithSpeciesAndUtilization(VdypPolygon polygon) throws IOException {

		writePolygon(polygon);
		for (var layer : polygon.getLayers().values()) {
			writeUtilization(layer, layer);
			List<VdypSpecies> specs = new ArrayList<>(layer.getSpecies().size());
			specs.addAll(layer.getSpecies().values());
			specs.sort(Utils.compareUsing(BaseVdypSpecies::getGenus));
			for (var species : specs) {
				writeSpecies(layer, species);
				writeUtilization(layer, species);
			}
		}
		writeSpeciesEndRecord(polygon);
		writeUtilizationEndRecord(polygon);
	}

	private void writeEndRecord(OutputStream os, VdypPolygon polygon) throws IOException {
		writeFormat(os, END_RECORD_FORMAT, polygon.getPolygonIdentifier());
	}

	private void writeUtilizationEndRecord(VdypPolygon polygon) throws IOException {
		writeEndRecord(utilizationFile, polygon);
	}

	private void writeSpeciesEndRecord(VdypPolygon polygon) throws IOException {
		writeEndRecord(speciesFile, polygon);
	}

	void writeFormat(OutputStream os, String format, Object... params) throws IOException {
		os.write(String.format(format, params).getBytes());
	}

	@Override
	public void close() throws IOException {
		polygonFile.close();
		speciesFile.close();
		utilizationFile.close();
	}
}
