package ca.bc.gov.nrs.vdyp.application.test;

import java.util.Optional;
import java.util.function.Consumer;

import ca.bc.gov.nrs.vdyp.model.BaseVdypPolygon;
import ca.bc.gov.nrs.vdyp.model.PolygonIdentifier;
import ca.bc.gov.nrs.vdyp.model.PolygonMode;

public class TestPolygon extends BaseVdypPolygon<TestLayer, Optional<Float>, TestSpecies, TestSite> {

	public static TestPolygon build(Consumer<TestPolygon.Builder> config) {
		var builder = new Builder();
		config.accept(builder);
		return builder.build();
	}

	protected TestPolygon(
			PolygonIdentifier polygonIdentifier, Optional<Float> percentAvailable, String fiz, String becIdentifier,
			Optional<PolygonMode> mode
	) {
		super(polygonIdentifier, percentAvailable, fiz, becIdentifier, mode);
	}

	public static class Builder extends
			BaseVdypPolygon.Builder<TestPolygon, TestLayer, Optional<Float>, TestSpecies, TestSite, TestLayer.Builder, TestSpecies.Builder, TestSite.Builder> {

		@Override
		protected TestLayer.Builder getLayerBuilder() {
			var builder = new TestLayer.Builder();
			return builder;
		}

		@Override
		protected TestPolygon doBuild() {
			return (new TestPolygon(
					polygonIdentifier.get(), //
					percentAvailable.flatMap(x -> x), //
					forestInventoryZone.get(), //
					biogeoclimaticZone.get(), //
					mode //
			));

		}

	}

}