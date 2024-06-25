package ca.bc.gov.nrs.vdyp.fip;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import ca.bc.gov.nrs.vdyp.common.ControlKey;
import ca.bc.gov.nrs.vdyp.common.ValueOrMarker;
import ca.bc.gov.nrs.vdyp.fip.model.FipLayer;
import ca.bc.gov.nrs.vdyp.fip.model.FipLayerPrimary;
import ca.bc.gov.nrs.vdyp.io.EndOfRecord;
import ca.bc.gov.nrs.vdyp.io.FileResolver;
import ca.bc.gov.nrs.vdyp.io.parse.common.LineParser;
import ca.bc.gov.nrs.vdyp.io.parse.common.ResourceParseException;
import ca.bc.gov.nrs.vdyp.io.parse.control.ControlMapValueReplacer;
import ca.bc.gov.nrs.vdyp.io.parse.streaming.AbstractStreamingParser;
import ca.bc.gov.nrs.vdyp.io.parse.streaming.GroupingStreamingParser;
import ca.bc.gov.nrs.vdyp.io.parse.streaming.StreamingParserFactory;
import ca.bc.gov.nrs.vdyp.io.parse.value.ControlledValueParser;
import ca.bc.gov.nrs.vdyp.io.parse.value.ValueParser;
import ca.bc.gov.nrs.vdyp.model.LayerType;

public class FipLayerParser
		implements ControlMapValueReplacer<StreamingParserFactory<Map<LayerType, FipLayer>>, String> {

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
	public ControlKey getControlKey() {
		return ControlKey.FIP_INPUT_YIELD_LAYER;
	}

	@Override
	public StreamingParserFactory<Map<LayerType, FipLayer>>
			map(String fileName, FileResolver fileResolver, Map<String, Object> control)
					throws IOException, ResourceParseException {
		return () -> {
			var lineParser = new LineParser() //
					.strippedString(25, FipPolygonParser.POLYGON_IDENTIFIER).space(1) //
					.value(
							1, LAYER,
							ValueParser.valueOrMarker(
									ValueParser.LAYER,
									ValueParser.optionalSingleton("Z"::equals, EndOfRecord.END_OF_RECORD)
							)
					) //
					.floating(4, AGE_TOTAL) //
					.floating(5, HEIGHT) //
					.floating(5, SITE_INDEX) //
					.floating(5, CROWN_CLOSURE) //
					.space(3) //
					.value(2, SITE_SP0, ControlledValueParser.optional(ValueParser.GENUS)) //
					.value(3, SITE_SP64, ControlledValueParser.optional(ValueParser.STRING)) //
					.floating(5, YEARS_TO_BREAST_HEIGHT) //
					.value(1, STOCKING_CLASS, ValueParser.optional(ValueParser.CHARACTER)) //
					.space(2) //
					.value(4, INVENTORY_TYPE_GROUP, ValueParser.optional(ValueParser.INTEGER)) //
					.space(1) //
					.value(6, BREAST_HEIGHT_AGE, ValueParser.optional(ValueParser.FLOAT)) //
					.value(3, SITE_CURVE_NUMBER, ValueParser.optional(ValueParser.INTEGER));

			var is = fileResolver.resolveForInput(fileName);

			var delegateStream = new AbstractStreamingParser<ValueOrMarker<Optional<FipLayer>, EndOfRecord>>(
					is, lineParser, control
			) {

				@SuppressWarnings({ "unchecked" })
				@Override
				protected ValueOrMarker<Optional<FipLayer>, EndOfRecord> convert(Map<String, Object> entry) {
					var polygonId = (String) entry.get(FipPolygonParser.POLYGON_IDENTIFIER);
					var layer = (ValueOrMarker<Optional<LayerType>, EndOfRecord>) entry.get(LAYER);
					var ageTotal = (float) entry.get(AGE_TOTAL);
					var height = (float) entry.get(HEIGHT);
					var siteIndex = (float) entry.get(SITE_INDEX);
					var crownClosure = (float) entry.get(CROWN_CLOSURE);
					var siteSp0 = (Optional<String>) entry.get(SITE_SP0);
					var siteSp64 = (Optional<String>) entry.get(SITE_SP64);
					var yearsToBreastHeight = (float) entry.get(YEARS_TO_BREAST_HEIGHT);
					var stockingClass = (Optional<Character>) entry.get(STOCKING_CLASS);
					var inventoryTypeGroup = (Optional<Integer>) entry.get(INVENTORY_TYPE_GROUP);
					var siteCurveNumber = (Optional<Integer>) entry.get(SITE_CURVE_NUMBER);

					var vmBuilder = new ValueOrMarker.Builder<Optional<FipLayer>, EndOfRecord>();
					return layer.handle(l -> {
						switch (l.orElse(null)) {
						case PRIMARY:
							FipLayerPrimary fipLayerPrimary = FipLayerPrimary.buildPrimary(flBuilder -> {
								flBuilder.polygonIdentifier(polygonId);

								flBuilder.addSite(siteBuilder -> {
									siteBuilder.ageTotal(ageTotal);
									siteBuilder.yearsToBreastHeight(yearsToBreastHeight);
									siteBuilder.height(height);
									siteBuilder.siteIndex(siteIndex);
									siteBuilder.siteGenus(siteSp0.get());
									siteBuilder.siteCurveNumber(siteCurveNumber);
									siteBuilder.siteSpecies(siteSp64.get());

								});

								flBuilder.crownClosure(crownClosure);

								flBuilder.stockingClass(stockingClass);
								flBuilder.inventoryTypeGroup(inventoryTypeGroup);
							});
							return vmBuilder.value(Optional.of(fipLayerPrimary));
						case VETERAN:
							FipLayer fipLayerVeteran = FipLayer.build(flBuilder -> {
								flBuilder.polygonIdentifier(polygonId);
								flBuilder.layerType(LayerType.VETERAN);
								flBuilder.addSite(siteBuilder -> {
									siteBuilder.ageTotal(ageTotal);
									siteBuilder.yearsToBreastHeight(yearsToBreastHeight);
									siteBuilder.height(height);
									siteBuilder.siteIndex(siteIndex);
									siteBuilder.siteGenus(siteSp0.get());
									siteBuilder.siteSpecies(siteSp64.get());
								});

								flBuilder.crownClosure(crownClosure);
							});

							return vmBuilder.value(Optional.of(fipLayerVeteran));

						default:
							return vmBuilder.value(Optional.empty());
						}
					}, vmBuilder::marker);
				}

			};

			return new GroupingStreamingParser<Map<LayerType, FipLayer>, ValueOrMarker<Optional<FipLayer>, EndOfRecord>>(
					delegateStream
			) {

				@Override
				protected boolean skip(ValueOrMarker<Optional<FipLayer>, EndOfRecord> nextChild) {
					return nextChild.getValue().map(x -> x.map(layer -> {
						// TODO log this
						// If the layer is present but has height or closure that's not positive, ignore
						return layer.getHeightSafe() <= 0f || layer.getCrownClosure() <= 0f;
					}).orElse(true)) // If the layer is not present (Unknown layer type) ignore
							.orElse(false); // If it's a marker, let it through so the stop method can see it.
				}

				@Override
				protected boolean stop(ValueOrMarker<Optional<FipLayer>, EndOfRecord> nextChild) {
					return nextChild.isMarker();
				}

				@Override
				protected Map<LayerType, FipLayer>
						convert(List<ValueOrMarker<Optional<FipLayer>, EndOfRecord>> children) {
					return children.stream().map(ValueOrMarker::getValue).map(Optional::get) // Should never be empty as
							// we've filtered out
							// markers
							.flatMap(Optional::stream) // Skip if empty (and unknown layer type)
							.collect(Collectors.toMap(FipLayer::getLayerType, x -> x));
				}

			};
		};
	}

	@Override
	public ValueParser<Object> getValueParser() {
		return FILENAME;
	}
}
