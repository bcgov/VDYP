package ca.bc.gov.nrs.vdyp.io.parse.coe;

import java.util.Optional;

import ca.bc.gov.nrs.vdyp.common.ControlKey;
import ca.bc.gov.nrs.vdyp.io.parse.coe.base.SimpleCoefficientParser1;
import ca.bc.gov.nrs.vdyp.model.Coefficients;

/**
 * Parses a mapping from a Basal Area Group number to a list of ten coefficients. Each row contains
 * <ol>
 * <li>(cols 0-2) int - Basal Area Group number</li>
 * <li>(cols 3-12, 13-22, 23-32, ...) float * 10 - coefficient list</li>
 * </ol>
 * All lines are parsed. There is no provision for blank lines; all lines must have content. If fewer than ten
 * coefficients are supplied on a given line, the remainder default to 0.0f.
 * <p>
 * The result of the parse is a map from a Basal Area Group number to a (one-based) ten-element coefficient array.
 * <p>
 * Control index: 122
 * <p>
 * Example file: coe/GD23.coe
 *
 * @author Michael Junkin, Vivid Solutions
 * @see SimpleCoefficientParser1
 */
public class DqGrowthEmpiricalParser extends SimpleCoefficientParser1<Integer> {

	public static final int MAX_GROUPS = 40;

	protected static final Coefficients defaultCoefficients = new Coefficients(
			new float[] { 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f }, 1
	);

	public DqGrowthEmpiricalParser() {
		super(Integer.class, 1, ControlKey.DQ_GROWTH_EMPIRICAL);

		this.groupIndexKey(MAX_GROUPS)
				.coefficients(10, 9, Optional.of((index) -> defaultCoefficients), Optional.of((index) -> 0.0f));
	}
}
