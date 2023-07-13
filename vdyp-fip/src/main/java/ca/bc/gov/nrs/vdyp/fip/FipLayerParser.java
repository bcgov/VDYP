package ca.bc.gov.nrs.vdyp.fip;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import ca.bc.gov.nrs.vdyp.common.ValueOrMarker;
import ca.bc.gov.nrs.vdyp.fip.model.FipLayer;
import ca.bc.gov.nrs.vdyp.fip.model.FipLayerPrimary;
import ca.bc.gov.nrs.vdyp.io.EndOfRecord;
import ca.bc.gov.nrs.vdyp.io.FileResolver;
import ca.bc.gov.nrs.vdyp.io.parse.AbstractStreamingParser;
import ca.bc.gov.nrs.vdyp.io.parse.ControlMapValueReplacer;
import ca.bc.gov.nrs.vdyp.io.parse.ControlledValueParser;
import ca.bc.gov.nrs.vdyp.io.parse.GroupingStreamingParser;
import ca.bc.gov.nrs.vdyp.io.parse.LineParser;
import ca.bc.gov.nrs.vdyp.io.parse.ResourceParseException;
import ca.bc.gov.nrs.vdyp.io.parse.StreamingParserFactory;
import ca.bc.gov.nrs.vdyp.io.parse.ValueParser;
import ca.bc.gov.nrs.vdyp.model.Layer;

public class FipLayerParser implements ControlMapValueReplacer<StreamingParserFactory<Map<Layer, FipLayer>>, String> {

	public static final String CONTROL_KEY = "FIP_LAYERS";

	static final String POLYGON_IDENTIFIER = "POLYGON_IDENTIFIER"; // POLYDESC
	static final String LAYER = "LAYER"; // LAYER
	static final String AGE_TOTAL = "AGE_TOTAL"; // AGETOT
	static final String HEIGHT = "HEIGHT"; // HT
	static final String SITE_INDEX = "SITE_INDEX"; // SI
	static final String CROWN_CLOSURE = "CROWN_CLOSURE"; // CC
	static final String SITE_SP0 = "SITE_SP0"; // SITESP0
	static final String SITE_SP64 = "SITE_SP64"; // SITESP64
	static final String YEARS_TO_BREAST_HEIGHT = "YEARS_TO_BREAST_HEIGHT"; // YTBH
	static final String STOCKING_CLASS = "STOCKING_CLASS"; // STK
	static final String INVENTORY_TYPE_GROUP = "INVENTORY_TYPE_GROUP"; // ITGFIP
	static final String BREAST_HEIGHT_AGE = "BREAST_HEIGHT_AGE"; // AGEBH
	static final String SITE_CURVE_NUMBER = "SITE_CURVE_NUMBER"; // SCN

	@Override
	public String getControlKey() {
		return CONTROL_KEY;
	}

	@Override
	public StreamingParserFactory<Map<Layer, FipLayer>>
			map(String fileName, FileResolver fileResolver, Map<String, Object> control)
					throws IOException, ResourceParseException {
		return () -> {
			var lineParser = new LineParser().strippedString(25, POLYGON_IDENTIFIER).space(1).value(
					1, LAYER,
					ValueParser.valueOrMarker(
							ValueParser.LAYER, ValueParser.optionalSingleton("Z"::equals, EndOfRecord.END_OF_RECORD)
					)
			).floating(4, AGE_TOTAL).floating(5, HEIGHT).floating(5, SITE_INDEX).floating(5, CROWN_CLOSURE).space(3)
					.value(2, SITE_SP0, ControlledValueParser.optional(ValueParser.GENUS))
					.value(3, SITE_SP64, ControlledValueParser.optional(ValueParser.STRING))
					.floating(5, YEARS_TO_BREAST_HEIGHT)
					.value(1, STOCKING_CLASS, ValueParser.optional(ValueParser.STRING)).space(2)
					.value(4, INVENTORY_TYPE_GROUP, ValueParser.optional(ValueParser.INTEGER)).space(1)
					.value(6, BREAST_HEIGHT_AGE, ValueParser.optional(ValueParser.FLOAT))
					.value(3, SITE_CURVE_NUMBER, ValueParser.optional(ValueParser.INTEGER));

			var is = fileResolver.resolve(fileName);

			var delegateStream = new AbstractStreamingParser<ValueOrMarker<Optional<FipLayer>, EndOfRecord>>(
					is, lineParser, control
			) {

				@SuppressWarnings("unchecked")
				@Override
				protected ValueOrMarker<Optional<FipLayer>, EndOfRecord> convert(Map<String, Object> entry) {
					var polygonId = (String) entry.get(POLYGON_IDENTIFIER);
					var layer = (ValueOrMarker<Optional<Layer>, EndOfRecord>) entry.get(LAYER);
					var ageTotal = (float) entry.get(AGE_TOTAL);
					var height = (float) entry.get(HEIGHT);
					var siteIndex = (float) entry.get(SITE_INDEX);
					var crownClosure = (float) entry.get(CROWN_CLOSURE);
					var siteSp0 = (Optional<String>) entry.get(SITE_SP0);
					var siteSp64 = (Optional<String>) entry.get(SITE_SP64);
					var yearsToBreastHeight = (float) entry.get(YEARS_TO_BREAST_HEIGHT);
					var stockingClass = (Optional<String>) entry.get(STOCKING_CLASS);
					var inventoryTypeGroup = (Optional<Integer>) entry.get(INVENTORY_TYPE_GROUP);
					var breastHeightAge = (Optional<Float>) entry.get(BREAST_HEIGHT_AGE);
					var siteCurveNumber = (Optional<Integer>) entry.get(SITE_CURVE_NUMBER);

					var builder = new ValueOrMarker.Builder<Optional<FipLayer>, EndOfRecord>();
					var result = layer.handle(l -> {
						switch (l.orElse(null)) {
						case PRIMARY:
							return builder.value(
									Optional.of(
											new FipLayerPrimary(
													polygonId, ageTotal, height, siteIndex, crownClosure, siteSp0.get(),
													siteSp64.get(), yearsToBreastHeight, stockingClass,
													inventoryTypeGroup, breastHeightAge, siteCurveNumber
											)
									)
							);
						case VETERAN:
							return builder.value(
									Optional.of(
											new FipLayer(
													polygonId, Layer.VETERAN, ageTotal, height, siteIndex, crownClosure,
													siteSp0.get(), siteSp64.get(), yearsToBreastHeight,
													inventoryTypeGroup, breastHeightAge
											)
									)
							);

						default:
							return builder.value(Optional.empty());
						}
					}, m -> {
						return builder.marker(m);
					});
					return result;
				}

			};

			return new GroupingStreamingParser<Map<Layer, FipLayer>, ValueOrMarker<Optional<FipLayer>, EndOfRecord>>(
					delegateStream
			) {

				@Override
				protected boolean skip(ValueOrMarker<Optional<FipLayer>, EndOfRecord> nextChild) {
					return nextChild.getValue().map(x -> x.map(layer -> {
						// TODO log this
						// If the layer is present but has height or closure that's not positive, ignore
						return layer.getHeight() <= 0f || layer.getCrownClosure() <= 0f;
					}).orElse(true)) // If the layer is not present (Unknown layer type) ignore
							.orElse(false); // If it's a marker, let it through so the stop method can see it.
				}

				@Override
				protected boolean stop(ValueOrMarker<Optional<FipLayer>, EndOfRecord> nextChild) {
					return nextChild.isMarker();
				}

				@Override
				protected Map<Layer, FipLayer> convert(List<ValueOrMarker<Optional<FipLayer>, EndOfRecord>> children) {
					return children.stream().map(ValueOrMarker::getValue).map(Optional::get) // Should never be empty as
																								// we've filtered out
																								// markers
							.flatMap(Optional::stream) // Skip if empty (and unknown layer type)
							.collect(Collectors.toMap(FipLayer::getLayer, x -> x));
				}

			};
		};
	}
}
