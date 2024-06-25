package ca.bc.gov.nrs.vdyp.model;

public class StockingClassFactor {

	private final Character stk;
	private final Region region;
	private final float factor;
	private final int npctArea;

	public StockingClassFactor(Character stk, Region region, float factor, int npctArea) {
		super();
		this.stk = stk;
		this.region = region;
		this.factor = factor;
		this.npctArea = npctArea;
	}

	public Character getStk() {
		return stk;
	}

	public Region getRegion() {
		return region;
	}

	public float getFactor() {
		return factor;
	}

	public int getNpctArea() {
		return npctArea;
	}

}
