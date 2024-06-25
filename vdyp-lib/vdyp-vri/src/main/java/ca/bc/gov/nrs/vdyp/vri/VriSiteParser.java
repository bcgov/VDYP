package ca.bc.gov.nrs.vdyp.vri;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import ca.bc.gov.nrs.vdyp.common.ControlKey;
import ca.bc.gov.nrs.vdyp.common.Utils;
import ca.bc.gov.nrs.vdyp.common.ValueOrMarker;
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
import ca.bc.gov.nrs.vdyp.math.FloatMath;
import ca.bc.gov.nrs.vdyp.model.LayerType;
import ca.bc.gov.nrs.vdyp.vri.model.VriSite;

public class VriSiteParser implements ControlMapValueReplacer<StreamingParserFactory<Collection<VriSite>>, String> {

	static final String AGE_TOTAL = "AGE_TOTAL"; // AGETOT, VRISI/VR_TAGE
	static final String AGE_BREAST_HEIGHT = "AGE_BREAST_HEIGHT"; // AGEBH, VRISI/VR_BAGE
	static final String HEIGHT = "HEIGHT"; // HD, VRISI/VR_HD
	static final String SITE_INDEX = "SITE_INDEX"; // SI, VRISI/VR_SI
	static final String SITE_SP0 = "SITE_SP0"; // SITESP0, VRISIA/SITESP0
	static final String SITE_SP64 = "SITE_SP64"; // SITESP64, VRISIA/VR_SP64
	static final String YEARS_TO_BREAST_HEIGHT = "YEARS_TO_BREAST_HEIGHT"; // YTBH, VRISI/VR_YTBH
	static final String BREAST_HEIGHT_AGE = "BREAST_HEIGHT_AGE"; // AGEBH, VRISI/VR_BAGE
	static final String SITE_CURVE_NUMBER = "SITE_CURVE_NUMBER"; // SCN, VRISI/VR_SCN

	// TODO reduce code duplication with FipSpecies and VriSpecies

	@Override
	public ControlKey getControlKey() {
		return ControlKey.VRI_INPUT_YIELD_SPEC_DIST;
	}

	@Override
	public StreamingParserFactory<Collection<VriSite>>
			map(String fileName, FileResolver fileResolver, Map<String, Object> control)
					throws IOException, ResourceParseException {
		return () -> {
			var lineParser = new LineParser() {
			} //
					.strippedString(25, VriPolygonParser.POLYGON_IDENTIFIER) //
					.space(1) //
					.value(
							1, VriLayerParser.LAYER,
							ValueParser.valueOrMarker(
									ValueParser.LAYER,
									ValueParser.optionalSingleton("Z"::equals, EndOfRecord.END_OF_RECORD)
							)
					) //
					.value(4, AGE_TOTAL, ValueParser.SAFE_POSITIVE_FLOAT) //
					.value(5, HEIGHT, ValueParser.SAFE_POSITIVE_FLOAT) //
					.value(5, SITE_INDEX, ValueParser.SAFE_POSITIVE_FLOAT) //
					.space(8) //
					.value(2, SITE_SP0, ControlledValueParser.SPECIES) //
					.value(3, SITE_SP64, ValueParser.STRING) //
					.value(5, YEARS_TO_BREAST_HEIGHT, ValueParser.SAFE_POSITIVE_FLOAT) //
					.space(8) //
					.value(6, AGE_BREAST_HEIGHT, ValueParser.SAFE_NONNEGATIVE_FLOAT) //
					.integer(3, SITE_CURVE_NUMBER);

			var is = fileResolver.resolveForInput(fileName);

			var delegateStream = new AbstractStreamingParser<ValueOrMarker<Optional<VriSite>, EndOfRecord>>(
					is, lineParser, control
			) {

				@SuppressWarnings("unchecked")
				@Override
				protected ValueOrMarker<Optional<VriSite>, EndOfRecord> convert(Map<String, Object> entry)
						throws ResourceParseException {

					var layer = (ValueOrMarker<Optional<LayerType>, EndOfRecord>) entry.get(VriLayerParser.LAYER);

					var markerBuilder = new ValueOrMarker.Builder<Optional<VriSite>, EndOfRecord>();
					return layer.handle(s -> {

						final var polygon = (String) entry.get(VriPolygonParser.POLYGON_IDENTIFIER);

						final var siteGenus = (String) entry.get(SITE_SP0);
						final var siteSpecies = (String) entry.get(SITE_SP64);
						final var siteCurveNumber = (int) entry.get(SITE_CURVE_NUMBER);

						final var siteIndex = (Optional<Float>) entry.get(SITE_INDEX);

						final var height = (Optional<Float>) entry.get(HEIGHT);
						final var ageTotal = (Optional<Float>) entry.get(AGE_TOTAL);
						final var yearsToBreastHeight = (Optional<Float>) entry.get(YEARS_TO_BREAST_HEIGHT);
						final var breastHeightAge = (Optional<Float>) entry.get(AGE_BREAST_HEIGHT);

						return markerBuilder.value(s.map(layerType -> VriSite.build(siteBuilder -> {

							siteBuilder.polygonIdentifier(polygon);
							siteBuilder.layerType(s.get());

							siteBuilder.siteGenus(siteGenus);
							siteBuilder.siteSpecies(siteSpecies);
							siteBuilder.siteCurveNumber(siteCurveNumber);

							siteBuilder.siteIndex(siteIndex);

							siteBuilder.height(
									// Default height to 0.05 if the layer is primary, age total is within 0.6 of 1,
									// and site index is >=3. per VDYP7 Fortran, per memo JFmemo_20090329.doc
									height.or(() -> s.map(l -> l == LayerType.PRIMARY).orElse(false) && //
											ageTotal.map(at -> FloatMath.abs(at - 1f) < 0.6).orElse(false) && //
											siteIndex.map(si -> si >= 3f).orElse(false) //
													? Optional.of(0.05f) : Optional.empty()
									)
							);
							siteBuilder.ageTotal(ageTotal);
							siteBuilder.yearsToBreastHeight(yearsToBreastHeight);
							siteBuilder.breastHeightAge(
									// breastHeightAge can only be 0.0 if ageTotal and yearsToBreastHeight are
									// both present and within 0.5 of each other. Otherwise 0 means it's not
									// present. per VDYP7 Fortran
									breastHeightAge.filter(
											x -> x != 0f || Utils.mapBoth(
													ageTotal, yearsToBreastHeight,
													(at, ytbh) -> FloatMath.abs(at - ytbh) < 0.5
											).orElse(false)
									)
							);
						})));
					}, markerBuilder::marker);
				}

			};

			return new GroupingStreamingParser<Collection<VriSite>, ValueOrMarker<Optional<VriSite>, EndOfRecord>>(
					delegateStream
			) {

				@Override
				protected boolean skip(ValueOrMarker<Optional<VriSite>, EndOfRecord> nextChild) {
					return nextChild.getValue().map(Optional::isEmpty).orElse(false);
				}

				@Override
				protected boolean stop(ValueOrMarker<Optional<VriSite>, EndOfRecord> nextChild) {
					return nextChild.isMarker();
				}

				@Override
				protected Collection<VriSite> convert(List<ValueOrMarker<Optional<VriSite>, EndOfRecord>> children) {
					return children.stream().map(ValueOrMarker::getValue).map(Optional::get) // Should never be empty as
							// we've filtered out
							// markers
							.flatMap(Optional::stream) // Skip if empty (and unknown layer type)
							.toList();
				}

			};
		};
	}

	@Override
	public ValueParser<Object> getValueParser() {
		return FILENAME;
	}
}
