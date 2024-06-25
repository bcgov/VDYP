package ca.bc.gov.nrs.vdyp.model;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class SingleSiteLayer<S extends BaseVdypSpecies, I extends BaseVdypSite> extends BaseVdypLayer<S, I> {

	protected SingleSiteLayer(
			PolygonIdentifier polygonIdentifier, LayerType layer, Optional<Integer> inventoryTypeGroup
	) {
		super(polygonIdentifier, layer, inventoryTypeGroup);
	}

	public Optional<I> getSite() {
		var entries = getSites().entrySet();
		if (entries.size() == 1) {
			return Optional.of(entries.iterator().next().getValue());
		}
		return Optional.empty();
	}

	public Optional<Float> getSiteIndex() {
		return getSite().flatMap(BaseVdypSite::getSiteIndex);
	}

	public Optional<Integer> getSiteCurveNumber() {
		return getSite().flatMap(BaseVdypSite::getSiteCurveNumber);
	}

	public Optional<String> getSiteGenus() {
		return getSite().map(BaseVdypSite::getSiteGenus);
	}

	public Optional<Float> getHeight() {
		return getSite().flatMap(BaseVdypSite::getHeight);
	}

	public Optional<Float> getAgeTotal() {
		return getSite().flatMap(BaseVdypSite::getAgeTotal);
	}

	public Optional<Float> getYearsToBreastHeight() {
		return getSite().flatMap(BaseVdypSite::getYearsToBreastHeight);
	}

	@Override
	public LinkedHashMap<String, I> getSites() {
		// Make sure callers can push additional sites into the layer.
		// Should be quick as we know there's only ever one entry.

		// TODO Consider broadening to Map in general at Parent class, and only refine
		// to LinkedHashMap for modules that have more than one and need odered access
		var result = new LinkedHashMap<String, I>();
		result.putAll(super.getSites());
		return result;
	}

	@Override
	public void setSites(Map<String, I> sites) {
		if (sites.size() > 1) {
			throw new IllegalArgumentException(
					"Layer type " + this.getClass().getSimpleName() + " can only have one site"
			);
		}
		super.setSites(sites);
	}

	@Override
	public void setSites(Collection<I> sites) {
		if (sites.size() > 1) {
			throw new IllegalArgumentException(
					"Layer type " + this.getClass().getSimpleName() + " can only have one site"
			);
		}
		super.setSites(sites);
	}

}
