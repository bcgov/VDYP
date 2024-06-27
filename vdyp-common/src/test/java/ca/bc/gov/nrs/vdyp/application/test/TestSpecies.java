package ca.bc.gov.nrs.vdyp.application.test;

import java.util.function.Consumer;

import ca.bc.gov.nrs.vdyp.model.BaseVdypSpecies;
import ca.bc.gov.nrs.vdyp.model.LayerType;
import ca.bc.gov.nrs.vdyp.model.PolygonIdentifier;

public class TestSpecies extends BaseVdypSpecies {

	protected TestSpecies(PolygonIdentifier polygonIdentifier, LayerType layerType, String genus, float percentGenus) {
		super(polygonIdentifier, layerType, genus, percentGenus);
	}

	public static TestSpecies build(Consumer<TestSpecies.Builder> config) {
		var builder = new Builder();
		config.accept(builder);
		return builder.build();
	}

	public static class Builder extends BaseVdypSpecies.Builder<TestSpecies> {

		@Override
		protected TestSpecies doBuild() {
			return new TestSpecies(polygonIdentifier.get(), layerType.get(), genus.get(), percentGenus.get());
		}

	}
}