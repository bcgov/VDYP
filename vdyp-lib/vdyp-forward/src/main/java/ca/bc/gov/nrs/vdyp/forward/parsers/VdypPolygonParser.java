package ca.bc.gov.nrs.vdyp.forward.parsers;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.bc.gov.nrs.vdyp.common.ControlKey;
import ca.bc.gov.nrs.vdyp.common.Utils;
import ca.bc.gov.nrs.vdyp.common_calculators.ForestInventoryZone;
import ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexForestInventoryZone;
import ca.bc.gov.nrs.vdyp.forward.model.FipMode;
import ca.bc.gov.nrs.vdyp.forward.model.VdypPolygon;
import ca.bc.gov.nrs.vdyp.io.FileResolver;
import ca.bc.gov.nrs.vdyp.io.parse.common.LineParser;
import ca.bc.gov.nrs.vdyp.io.parse.common.ResourceParseException;
import ca.bc.gov.nrs.vdyp.io.parse.control.ControlMapValueReplacer;
import ca.bc.gov.nrs.vdyp.io.parse.streaming.AbstractStreamingParser;
import ca.bc.gov.nrs.vdyp.io.parse.streaming.StreamingParserFactory;
import ca.bc.gov.nrs.vdyp.io.parse.value.ControlledValueParser;
import ca.bc.gov.nrs.vdyp.io.parse.value.ValueParser;
import ca.bc.gov.nrs.vdyp.model.BecDefinition;
import ca.bc.gov.nrs.vdyp.model.BecLookup;

public class VdypPolygonParser implements ControlMapValueReplacer<Object, String> {

	private static final Logger logger = LoggerFactory.getLogger(VdypPolygonParser.class);

	private static final Float DEFAULT_FORESTED_LAND_PERCENTAGE = 90.0f;

	private static final String DESCRIPTION = "DESCRIPTION"; // POLYDESC
	private static final String BIOGEOCLIMATIC_ZONE = "BIOGEOCLIMATIC_ZONE"; // BEC
	private static final String FOREST_INVENTORY_ZONE = "FOREST_INVENTORY_ZONE"; // FIZ
	private static final String PERCENT_FOREST_LAND = "PERCENT_FOREST_LAND"; // PCTFLAND
	private static final String INVENTORY_TYPE_GROUP = "INVENTORY_TYPE_GROUP"; // ITG
	private static final String BASAL_AREA_GROUP = "BASAL_AREA_GROUP"; // GRPBA1
	private static final String FIP_MODE = "FIP_MODE"; // MODEfip

	@Override
	public ControlKey getControlKey() {
		return ControlKey.FORWARD_INPUT_VDYP_POLY;
	}

	@Override
	public StreamingParserFactory<VdypPolygon>
			map(String fileName, FileResolver fileResolver, Map<String, Object> control)
					throws IOException, ResourceParseException {
		return () -> {
			var lineParser = new LineParser() {
				@Override
				public boolean isStopLine(String line) {
					return line.substring(0, Math.min(25, line.length())).trim().length() == 0;
				}
			}.strippedString(25, DESCRIPTION).space(1).value(4, BIOGEOCLIMATIC_ZONE, ControlledValueParser.BEC).space(1)
					.value(1, FOREST_INVENTORY_ZONE, ValueParser.CHARACTER) // TODO: add ValueParser.FIZ
					.value(6, PERCENT_FOREST_LAND, ValueParser.FLOAT)
					.value(3, INVENTORY_TYPE_GROUP, ControlledValueParser.optional(ValueParser.INTEGER))
					.value(3, BASAL_AREA_GROUP, ValueParser.optional(ValueParser.INTEGER))
					.value(3, FIP_MODE, ValueParser.optional(ValueParser.INTEGER));

			var is = fileResolver.resolveForInput(fileName);

			return new AbstractStreamingParser<VdypPolygon>(is, lineParser, control) {

				@Override
				protected VdypPolygon convert(Map<String, Object> entry) throws ResourceParseException {
					var descriptionText = (String) entry.get(DESCRIPTION);
					var becAlias = (String) entry.get(BIOGEOCLIMATIC_ZONE);
					var fizId = (Character) entry.get(FOREST_INVENTORY_ZONE);
					var percentForestLand = (Float) entry.get(PERCENT_FOREST_LAND);
					var inventoryTypeGroup = Utils.<Integer>optSafe(entry.get(INVENTORY_TYPE_GROUP));
					var basalAreaGroup = Utils.<Integer>optSafe(entry.get(BASAL_AREA_GROUP));
					var fipMode = Utils.<Integer>optSafe(entry.get(FIP_MODE));

					BecLookup becLookup = (BecLookup) control.get(ControlKey.BEC_DEF.name());
					BecDefinition bec = becLookup.get(becAlias)
							.orElseThrow(() -> new ResourceParseException(becAlias + " is not a recognized BEC alias"));

					if (ForestInventoryZone.toRegion(fizId) == SiteIndexForestInventoryZone.FIZ_UNKNOWN) {
						throw new ResourceParseException(
								"Forest Inventory Zone " + fizId
										+ " is not a recognized FIZ (only 'A' ... 'L' are supported)"
						);
					}

					var description = VdypPolygonDescriptionParser.parse(descriptionText);

					if (percentForestLand <= 0.0) {
						// VDYPGETP.for lines 146 - 154
						logger.warn(
								MessageFormat.format(
										"Polygon {0} percent-forested-land value {1} is <= 0.0; replacing with default {2}",
										description.getName(), percentForestLand, DEFAULT_FORESTED_LAND_PERCENTAGE
								)
						);
						percentForestLand = DEFAULT_FORESTED_LAND_PERCENTAGE;
					}

					return new VdypPolygon(
							description, bec, fizId, percentForestLand, inventoryTypeGroup, basalAreaGroup,
							fipMode.flatMap(FipMode::getByCode)
					);
				}
			};
		};
	}

	@Override
	public ValueParser<Object> getValueParser() {
		return FILENAME;
	}
}
