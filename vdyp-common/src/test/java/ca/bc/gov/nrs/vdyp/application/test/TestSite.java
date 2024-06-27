package ca.bc.gov.nrs.vdyp.application.test;

import java.util.Optional;
import java.util.function.Consumer;

import ca.bc.gov.nrs.vdyp.model.BaseVdypSite;
import ca.bc.gov.nrs.vdyp.model.LayerType;
import ca.bc.gov.nrs.vdyp.model.PolygonIdentifier;

public class TestSite extends BaseVdypSite {

	protected TestSite(
			PolygonIdentifier polygonIdentifier, LayerType layerType, String siteGenus,
			Optional<Integer> siteCurveNumber, Optional<Float> siteIndex, Optional<Float> height,
			Optional<Float> ageTotal, Optional<Float> yearsToBreastHeight
	) {
		super(
				polygonIdentifier, layerType, siteGenus, siteCurveNumber, siteIndex, height, ageTotal,
				yearsToBreastHeight
		);
	}

	public static TestSite build(Consumer<TestSite.Builder> config) {
		var builder = new Builder();
		config.accept(builder);
		return builder.build();
	}

	public static class Builder extends BaseVdypSite.Builder<TestSite> {

		protected Optional<String> siteSpecies = Optional.empty();

		@Override
		protected TestSite doBuild() {
			return new TestSite(
					this.polygonIdentifier.get(), //
					this.layerType.get(), //
					this.siteGenus.get(), //
					this.siteCurveNumber, //
					this.siteIndex, //
					this.height, //
					this.ageTotal, //
					this.yearsToBreastHeight //
			);
		}
	}
}