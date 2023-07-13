package ca.bc.gov.nrs.vdyp.fip;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

import ca.bc.gov.nrs.vdyp.io.parse.HLCoefficientParser;
import ca.bc.gov.nrs.vdyp.io.parse.HLNonprimaryCoefficientParser;
import ca.bc.gov.nrs.vdyp.io.parse.LineParser;
import ca.bc.gov.nrs.vdyp.io.parse.OptionalResourceControlMapModifier;
import ca.bc.gov.nrs.vdyp.io.parse.ResourceParseException;
import ca.bc.gov.nrs.vdyp.io.parse.ResourceParser;
import ca.bc.gov.nrs.vdyp.io.parse.GenusDefinitionParser;
import ca.bc.gov.nrs.vdyp.io.parse.ValueParseException;
import ca.bc.gov.nrs.vdyp.io.parse.ValueParser;
import ca.bc.gov.nrs.vdyp.io.parse.VeteranBQParser;
import ca.bc.gov.nrs.vdyp.model.Coefficients;
import ca.bc.gov.nrs.vdyp.model.MatrixMap2;
import ca.bc.gov.nrs.vdyp.model.MatrixMap2Impl;
import ca.bc.gov.nrs.vdyp.model.MatrixMap3;
import ca.bc.gov.nrs.vdyp.model.NonprimaryHLCoefficients;
import ca.bc.gov.nrs.vdyp.model.Region;

public class ModifierParser implements OptionalResourceControlMapModifier {

	public static final String CONTROL_KEY = "MODIFIERS";

	/**
	 * MatrixMap2 of Species ID, Region to Coefficients (1-3)
	 */
	public static final String CONTROL_KEY_MOD098_VETERAN_BQ = VeteranBQParser.CONTROL_KEY;
	/**
	 * MatrixMap2 of Species ID, Region to Float
	 */
	public static final String CONTROL_KEY_MOD200_BA = "BA_MODIFIERS";
	/**
	 * MatrixMap2 of Species ID, Region to Float
	 */
	public static final String CONTROL_KEY_MOD200_DQ = "DQ_MODIFIERS";
	/**
	 * Boolean indicates HL modifiers are present
	 */
	public static final String CONTROL_KEY_MOD301_HL = "HL_MODIFIERS";
	/**
	 * MatrixMap2 of Species ID, Region to Float
	 */
	public static final String CONTROL_KEY_MOD301_DECAY = "DECAY_MODIFIERS";
	/**
	 * MatrixMap2 of Species ID, Region to Float
	 */
	public static final String CONTROL_KEY_MOD301_WASTE = "WASTE_MODIFIERS";

	/**
	 * MatrixMap3<Integer, String, Region, Float
	 */
	public static final String CONTROL_KEY_MOD400_P1 = HLCoefficientParser.CONTROL_KEY_P1;
	/**
	 * MatrixMap3<Integer, String, Region, Float
	 */
	public static final String CONTROL_KEY_MOD400_P2 = HLCoefficientParser.CONTROL_KEY_P2;
	/**
	 * MatrixMap3<Integer, String, Region, Float
	 */
	public static final String CONTROL_KEY_MOD400_P3 = HLCoefficientParser.CONTROL_KEY_P3;
	/**
	 * MatrixMap3<String, String, Region, NonprimaryHLCoefficients>
	 */
	public static final String CONTROL_KEY_MOD400_NONPRIMARY = HLNonprimaryCoefficientParser.CONTROL_KEY;

	public static final int MAX_MODS = 60;

	int jprogram;

	static final int[] ipoint = { 1, 0, 2, 0, 0, 3, 4, 5, 0 };

	public ModifierParser(int jprogram) {
		super();
		this.jprogram = jprogram;
	}

	@Override
	public void modify(Map<String, Object> control, InputStream data) throws ResourceParseException, IOException {
		// Modifiers, IPSJF155-Appendix XII

		// RD_E198

		defaultModify(control);

		var parser = new LineParser() {

			@Override
			public boolean isStopSegment(List<String> entry) {
				return !entry.isEmpty() && entry.get(0).equals("999");
			}

			@Override
			public boolean isIgnoredSegment(List<String> entry) {
				var sequence = entry.get(0);
				try {
					return sequence.isBlank() || ValueParser.INTEGER.parse(sequence) == 0;
				} catch (ValueParseException e) {
					return false;
				}
			}

			@Override
			public boolean isIgnoredLine(String line) {
				return line.isBlank();
			}

		}.integer(3, "sequence").multiValue(6, 2, "programs", ValueParser.LOGICAL)
				.multiValue(10, 6, "mods", ValueParser.optional(ValueParser.FLOAT));

		final MatrixMap2<String, Region, Coefficients> vetBqMap = ResourceParser
				.expectParsedControl(control, VeteranBQParser.CONTROL_KEY, MatrixMap2.class);

		@SuppressWarnings("unchecked")
		final MatrixMap2<String, Region, Float> baMap = ResourceParser
				.expectParsedControl(control, CONTROL_KEY_MOD200_BA, MatrixMap2.class);
		@SuppressWarnings("unchecked")
		final MatrixMap2<String, Region, Float> dqMap = ResourceParser
				.expectParsedControl(control, CONTROL_KEY_MOD200_DQ, MatrixMap2.class);

		@SuppressWarnings("unchecked")
		final MatrixMap2<String, Region, Float> decayMap = ResourceParser
				.expectParsedControl(control, CONTROL_KEY_MOD301_DECAY, MatrixMap2.class);
		@SuppressWarnings("unchecked")
		final MatrixMap2<String, Region, Float> wasteMap = ResourceParser
				.expectParsedControl(control, CONTROL_KEY_MOD301_WASTE, MatrixMap2.class);

		final MatrixMap2<String, Region, Coefficients> hlP1Map = ResourceParser
				.expectParsedControl(control, CONTROL_KEY_MOD400_P1, MatrixMap2.class);
		final MatrixMap2<String, Region, Coefficients> hlP2Map = ResourceParser
				.expectParsedControl(control, CONTROL_KEY_MOD400_P2, MatrixMap2.class);
		final MatrixMap2<String, Region, Coefficients> hlP3Map = ResourceParser
				.expectParsedControl(control, CONTROL_KEY_MOD400_P3, MatrixMap2.class);
		final MatrixMap3<String, String, Region, NonprimaryHLCoefficients> hlNPMap = ResourceParser
				.expectParsedControl(control, CONTROL_KEY_MOD400_NONPRIMARY, MatrixMap3.class);

		parser.parse(data, control, (entry, result) -> {
			int sequence = (int) entry.get("sequence");

			if (!modIsForProgram(entry))
				return result;

			if (sequence == 98) {
				// If modifiers are per region, for each species, multiply the first coefficient
				// for veteran BQ by the region appropriate modifier.
				vetBqMap.get(CONTROL_KEY, null);
				var mods = getMods(2, entry);
				var sp0Aliases = GenusDefinitionParser.getSpeciesAliases(control);
				for (var sp0Alias : sp0Aliases) {
					final float coastalMod = mods.get(0);
					final float interiorMod = mods.get(1);

					if (coastalMod != 0.0) {
						var coe = vetBqMap.get(sp0Alias, Region.COASTAL).get();
						coe.modifyCoe(1, x -> x * coastalMod);
					}
					if (interiorMod != 0.0) {
						var coe = vetBqMap.get(sp0Alias, Region.INTERIOR).get();
						coe.modifyCoe(1, x -> x * interiorMod);
					}
				}
			} else if (sequence >= 200 && sequence <= 299) {
				// Modifiers are per region for BA and DQ, for each species, set the modifier
				// map
				var mods = getMods(4, entry);
				var sp0Index = sequence - 200;
				var sp0Aliases = getSpeciesByIndex(sp0Index, control);
				for (var sp0Alias : sp0Aliases) {
					modsByRegions(mods, 0, (m, r) -> baMap.put(sp0Alias, r, m));
					modsByRegions(mods, 2, (m, r) -> dqMap.put(sp0Alias, r, m));
				}
			} else if (sequence >= 300 && sequence <= 399) {
				// Modifiers are per region for Decay and Waste, for the specified species, set
				// the
				// modifier map
				var sp0Index = sequence - 300;
				var sp0Aliases = getSpeciesByIndex(sp0Index, control);
				var mods = getMods(4, entry);

				for (var sp0Alias : sp0Aliases) {
					modsByRegions(mods, 0, (m, r) -> decayMap.put(sp0Alias, r, m));
					modsByRegions(mods, 2, (m, r) -> wasteMap.put(sp0Alias, r, m));
				}
			} else if (sequence >= 400 && sequence <= 499) {
				// Modifiers are per region, for the specified species, multiply existing
				// coefficients
				var sp0Index = sequence - 400;
				var sp0Aliases = getSpeciesByIndex(sp0Index, control);
				var mods = getMods(4, entry);

				for (var sp0Alias : sp0Aliases) {
					modsByRegions(mods, 0, (m, r) -> hlP1Map.get(sp0Alias, r).ifPresent(coe -> {
						coe.modifyCoe(1, x -> x * m);
						coe.modifyCoe(2, x -> x * m);
					}));
					modsByRegions(mods, 0, (m, r) -> hlP2Map.get(sp0Alias, r).ifPresent(coe -> {
						coe.modifyCoe(1, x -> x * m);
					}));
					modsByRegions(mods, 0, (m, r) -> hlP3Map.get(sp0Alias, r).ifPresent(coe -> {
						coe.modifyCoe(1, x -> {
							if (x > 0.0f && x < 1.0e06f) {
								return x * m;
							}
							return x;
						});
					}));
					for (var primarySp : GenusDefinitionParser.getSpeciesAliases(control)) {
						modsByRegions(mods, 2, (m, r) -> hlNPMap.get(sp0Alias, primarySp, r).ifPresent(coe -> {
							if (coe.getEquationIndex() == 1) {
								coe.modifyCoe(1, x -> x * m);
							}
						}));
					}
				}
			} else {
				// Unexpected sequence in modifier file
			}
			return result;
		}, control);

	}

	<T> void modsByRegions(List<Float> mods, int offset, BiConsumer<Float, Region> modifier) {
		assert mods.size() % 2 == 0;
		assert offset % 2 == 0;
		assert mods.size() - offset >= 2;

		var regions = Region.values();

		for (int i = 0; i < regions.length; i++) {
			modifier.accept(mods.get(offset + i), regions[i]);
		}
	}

	boolean modIsForProgram(Map<String, Object> entry) {
		@SuppressWarnings("unchecked")
		var programs = (List<Boolean>) entry.get("programs");
		var index = ipoint[this.jprogram - 1];
		if (index <= 0)
			throw new IllegalStateException("JProgram " + this.jprogram + " mapped to " + index);
		return programs.get(index - 1);
	}

	List<String> getSpeciesByIndex(int index, Map<String, Object> control) {
		if (index == 0) {
			return GenusDefinitionParser.getSpeciesAliases(control);
		}
		return List.of(GenusDefinitionParser.getSpeciesByIndex(index, control).getAlias());
	}

	List<Float> getMods(int num, Map<String, Object> entry) throws ValueParseException {
		@SuppressWarnings("unchecked")
		var raw = (List<Optional<Float>>) entry.get("mods");
		return getMods(num, raw);
	}

	<T> List<T> getMods(int num, List<Optional<T>> raw) throws ValueParseException {
		var it = raw.iterator();

		var result = new ArrayList<T>(num);
		for (int i = 0; i < num; i++) {
			result.add(
					it.next().orElseThrow(() -> new ValueParseException("", "Expected " + num + " modifier values"))
			);
		}
		// Possibly log a warning if there are extra unused values
		return result;
	}

	@Override
	public String getControlKey() {
		return CONTROL_KEY;
	}

	@Override
	public void defaultModify(Map<String, Object> control) {
		var spAliases = GenusDefinitionParser.getSpeciesAliases(control);
		var regions = Arrays.asList(Region.values());

		var baModifiers = new MatrixMap2Impl<String, Region, Float>(spAliases, regions);
		baModifiers.setAll(1.0f);
		control.put(CONTROL_KEY_MOD200_BA, baModifiers);

		var dqModifiers = new MatrixMap2Impl<String, Region, Float>(spAliases, regions);
		dqModifiers.setAll(1.0f);
		control.put(CONTROL_KEY_MOD200_DQ, dqModifiers);

		var decayModifiers = new MatrixMap2Impl<String, Region, Float>(spAliases, regions);
		decayModifiers.setAll(0.0f);
		control.put(CONTROL_KEY_MOD301_DECAY, decayModifiers);

		var wasteModifiers = new MatrixMap2Impl<String, Region, Float>(spAliases, regions);
		wasteModifiers.setAll(0.0f);
		control.put(CONTROL_KEY_MOD301_WASTE, wasteModifiers);
	}

}
