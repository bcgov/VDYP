package ca.bc.gov.nrs.vdyp.vri;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import ca.bc.gov.nrs.vdyp.common.ControlKey;
import ca.bc.gov.nrs.vdyp.common.ValueOrMarker;
import ca.bc.gov.nrs.vdyp.vri.model.VriLayer;
import ca.bc.gov.nrs.vdyp.io.EndOfRecord;
import ca.bc.gov.nrs.vdyp.io.FileResolver;
import ca.bc.gov.nrs.vdyp.io.parse.common.LineParser;
import ca.bc.gov.nrs.vdyp.io.parse.common.ResourceParseException;
import ca.bc.gov.nrs.vdyp.io.parse.control.ControlMapValueReplacer;
import ca.bc.gov.nrs.vdyp.io.parse.streaming.AbstractStreamingParser;
import ca.bc.gov.nrs.vdyp.io.parse.streaming.GroupingStreamingParser;
import ca.bc.gov.nrs.vdyp.io.parse.streaming.StreamingParserFactory;
import ca.bc.gov.nrs.vdyp.io.parse.value.ValueParser;
import ca.bc.gov.nrs.vdyp.model.LayerType;

/**
 * Parses a file containing VRI Layer records into VriLayer.Builder objects. Returns Builders rather than finished
 * VriLayer objects because some additional manipulation is needed before adding them to their VriPolygon
 */
public class VriLayerParser
		implements ControlMapValueReplacer<StreamingParserFactory<Map<LayerType, VriLayer.Builder>>, String> {

	static final String LAYER = "LAYER"; // LAYER
	static final String CROWN_CLOSURE = "CROWN_CLOSURE"; // CC
	static final String BASE_AREA = "BASE_AREA"; // BA
	static final String TREES_PER_HECTARE = "TREES_PER_HECTARE"; // TPH
	static final String UTILIZATION = "UTILIZATION"; // UTL

	@Override
	public ControlKey getControlKey() {
		return ControlKey.VRI_INPUT_YIELD_LAYER;
	}

	@Override
	public StreamingParserFactory<Map<LayerType, VriLayer.Builder>>
			map(String fileName, FileResolver fileResolver, Map<String, Object> control)
					throws IOException, ResourceParseException {
		return () -> {
			var lineParser = new LineParser() //
					.strippedString(25, VriPolygonParser.POLYGON_IDENTIFIER) //
					.space(1) //
					.value(
							1, LAYER,
							ValueParser.valueOrMarker(
									ValueParser.LAYER,
									ValueParser.optionalSingleton("Z"::equals, EndOfRecord.END_OF_RECORD)
							)
					) //
					.value(6, CROWN_CLOSURE, ValueParser.FLOAT) //
					.value(9, BASE_AREA, ValueParser.SAFE_NONNEGATIVE_FLOAT) //
					.space(1) //
					.value(8, TREES_PER_HECTARE, ValueParser.SAFE_NONNEGATIVE_FLOAT) //
					.space(1) //
					.value(4, UTILIZATION, ValueParser.FLOAT);

			var is = fileResolver.resolveForInput(fileName);

			var delegateStream = new AbstractStreamingParser<ValueOrMarker<Optional<VriLayer.Builder>, EndOfRecord>>(
					is, lineParser, control
			) {

				@SuppressWarnings({ "unchecked" })
				@Override
				protected ValueOrMarker<Optional<VriLayer.Builder>, EndOfRecord> convert(Map<String, Object> entry) {
					var polygonId = (String) entry.get(VriPolygonParser.POLYGON_IDENTIFIER);
					var layer = (ValueOrMarker<Optional<LayerType>, EndOfRecord>) entry.get(LAYER);
					var crownClosure = (Float) entry.get(CROWN_CLOSURE);
					var baseArea = (Optional<Float>) entry.get(BASE_AREA);
					var treesPerHectare = (Optional<Float>) entry.get(TREES_PER_HECTARE);
					var utilization = (Float) entry.get(UTILIZATION);

					var vmBuilder = new ValueOrMarker.Builder<Optional<VriLayer.Builder>, EndOfRecord>();
					return layer.handle(l -> {
						var layerBuilder = new VriLayer.Builder();
						layerBuilder.polygonIdentifier(polygonId);
						layerBuilder.layerType(l.get());

						layerBuilder.crownClosure(crownClosure);
						layerBuilder.baseArea(baseArea);
						layerBuilder.treesPerHectare(treesPerHectare);
						layerBuilder.utilization(utilization);

						return vmBuilder.value(Optional.of(layerBuilder));

					}, vmBuilder::marker);
				}

			};

			return new GroupingStreamingParser<Map<LayerType, VriLayer.Builder>, ValueOrMarker<Optional<VriLayer.Builder>, EndOfRecord>>(
					delegateStream
			) {

				@Override
				protected boolean skip(ValueOrMarker<Optional<VriLayer.Builder>, EndOfRecord> nextChild) {
					return nextChild.getValue().map(x -> x.map(layer -> {
						return false;
					}).orElse(true)) // If the layer is not present (Unknown layer type) ignore
							.orElse(false); // If it's a marker, let it through so the stop method can see it.
				}

				@Override
				protected boolean stop(ValueOrMarker<Optional<VriLayer.Builder>, EndOfRecord> nextChild) {
					return nextChild.isMarker();
				}

				@Override
				protected Map<LayerType, VriLayer.Builder>
						convert(List<ValueOrMarker<Optional<VriLayer.Builder>, EndOfRecord>> children) {

					return children.stream().map(ValueOrMarker::getValue).map(Optional::get) // Should never be empty as
							// we've filtered out
							// markers
							.flatMap(Optional::stream) // Skip if empty (and unknown layer type)
							.collect(Collectors.toMap(x -> x.getLayerType().get(), x -> x));
				}

			};
		};
	}

	@Override
	public ValueParser<Object> getValueParser() {
		return FILENAME;
	}
}
