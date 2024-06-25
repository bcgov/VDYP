package ca.bc.gov.nrs.vdyp.io.parse.coe;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.bc.gov.nrs.vdyp.application.VdypApplicationIdentifier;
import ca.bc.gov.nrs.vdyp.common.ControlKey;
import ca.bc.gov.nrs.vdyp.common.FloatUnaryOperator;
import ca.bc.gov.nrs.vdyp.common.Utils;
import ca.bc.gov.nrs.vdyp.io.parse.common.LineParser;
import ca.bc.gov.nrs.vdyp.io.parse.common.ResourceParseException;
import ca.bc.gov.nrs.vdyp.io.parse.control.OptionalResourceControlMapModifier;
import ca.bc.gov.nrs.vdyp.io.parse.value.ValueParseException;
import ca.bc.gov.nrs.vdyp.io.parse.value.ValueParser;
import ca.bc.gov.nrs.vdyp.model.Coefficients;
import ca.bc.gov.nrs.vdyp.model.MatrixMap2;
import ca.bc.gov.nrs.vdyp.model.MatrixMap2Impl;
import ca.bc.gov.nrs.vdyp.model.MatrixMap3;
import ca.bc.gov.nrs.vdyp.model.NonprimaryHLCoefficients;
import ca.bc.gov.nrs.vdyp.model.Region;

public class ModifierParser implements OptionalResourceControlMapModifier {

	private static final Logger log = LoggerFactory.getLogger(ModifierParser.class);

	/**
	 * MatrixMap2 of Species ID, Region to Coefficients (1-3)
	 */
	public static final ControlKey CONTROL_KEY_MOD098_VETERAN_BQ = ControlKey.VETERAN_BQ;
	/**
	 * Boolean indicates HL modifiers are present
	 */
	public static final ControlKey CONTROL_KEY_MOD301_HL = ControlKey.HL_MODIFIERS;
	/**
	 * MatrixMap2 of Species ID, Region to Float
	 */
	public static final ControlKey CONTROL_KEY_MOD301_DECAY = ControlKey.DECAY_MODIFIERS;
	/**
	 * MatrixMap2 of Species ID, Region to Float
	 */
	public static final ControlKey CONTROL_KEY_MOD301_WASTE = ControlKey.WASTE_MODIFIERS;

	/**
	 * MatrixMap3<Integer, String, Region, Float
	 */
	public static final ControlKey CONTROL_KEY_MOD400_P1 = ControlKey.HL_PRIMARY_SP_EQN_P1;
	/**
	 * MatrixMap3<Integer, String, Region, Float
	 */
	public static final ControlKey CONTROL_KEY_MOD400_P2 = ControlKey.HL_PRIMARY_SP_EQN_P2;
	/**
	 * MatrixMap3<Integer, String, Region, Float
	 */
	public static final ControlKey CONTROL_KEY_MOD400_P3 = ControlKey.HL_PRIMARY_SP_EQN_P3;
	/**
	 * MatrixMap3<String, String, Region, NonprimaryHLCoefficients>
	 */
	public static final ControlKey CONTROL_KEY_MOD400_NONPRIMARY = ControlKey.HL_NONPRIMARY;

	public static final int MAX_MODS = 60;

	VdypApplicationIdentifier jprogram;

	static final int[] ipoint = { 1, 0, 2, 0, 0, 3, 4, 5, 0 };

	public ModifierParser(VdypApplicationIdentifier jprogram) {
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

		final var vetBqMap = Utils.<MatrixMap2<String, Region, Coefficients>>parsedControl(
				control, ControlKey.VETERAN_BQ, MatrixMap2.class
		);

		final var baMap = Utils.<MatrixMap2<String, Region, Float>>expectParsedControl(
				control, ControlKey.BA_MODIFIERS, MatrixMap2.class
		);
		final var dqMap = Utils.<MatrixMap2<String, Region, Float>>expectParsedControl(
				control, ControlKey.DQ_MODIFIERS, MatrixMap2.class
		);

		final var decayMap = Utils.<MatrixMap2<String, Region, Float>>expectParsedControl(
				control, CONTROL_KEY_MOD301_DECAY, MatrixMap2.class
		);
		final var wasteMap = Utils.<MatrixMap2<String, Region, Float>>expectParsedControl(
				control, CONTROL_KEY_MOD301_WASTE, MatrixMap2.class
		);

		final var hlP1Map = Utils.<MatrixMap2<String, Region, Coefficients>>expectParsedControl(
				control, CONTROL_KEY_MOD400_P1, MatrixMap2.class
		);
		final var hlP2Map = Utils.<MatrixMap2<String, Region, Coefficients>>expectParsedControl(
				control, CONTROL_KEY_MOD400_P2, MatrixMap2.class
		);
		final var hlP3Map = Utils.<MatrixMap2<String, Region, Coefficients>>expectParsedControl(
				control, CONTROL_KEY_MOD400_P3, MatrixMap2.class
		);
		final var hlNPMap = Utils.<MatrixMap3<String, String, Region, NonprimaryHLCoefficients>>expectParsedControl(
				control, CONTROL_KEY_MOD400_NONPRIMARY, MatrixMap3.class
		);

		parser.parse(data, control, (entry, result, line) -> {
			int sequence = (int) entry.get("sequence");

			if (!modIsForProgram(entry))
				return result;

			if (sequence == 98) {
				modify98(control, vetBqMap, entry);
			} else if (sequence >= 200 && sequence <= 299) {
				modify200(control, baMap, dqMap, entry, sequence);
			} else if (sequence >= 300 && sequence <= 399) {
				modify300(control, decayMap, wasteMap, entry, sequence);
			} else if (sequence >= 400 && sequence <= 499) {
				modify400(control, hlP1Map, hlP2Map, hlP3Map, hlNPMap, entry, sequence);
			} else {
				log.atWarn().setMessage("Unexpected modifier sequence: {}").addArgument(sequence);
				// Unexpected sequence in modifier file
			}
			return result;
		}, control);

	}

	private void modify400(
			Map<String, Object> control, final MatrixMap2<String, Region, Coefficients> hlP1Map,
			final MatrixMap2<String, Region, Coefficients> hlP2Map,
			final MatrixMap2<String, Region, Coefficients> hlP3Map,
			final MatrixMap3<String, String, Region, NonprimaryHLCoefficients> hlNPMap, Map<String, Object> entry,
			int sequence
	) throws ValueParseException {
		// Modifiers are per region, for the specified species, multiply existing
		// coefficients
		var sp0Index = sequence - 400;
		var sp0Aliases = getSpeciesByIndex(sp0Index, control);
		var mods = getMods(4, entry);

		for (var sp0Alias : sp0Aliases) {

			modsByRegions(mods, 0, (m, r) -> {
				var coe = hlP1Map.get(sp0Alias, r);
				coe.scalarInPlace(1, (FloatUnaryOperator) x -> x * m);
				coe.scalarInPlace(2, (FloatUnaryOperator) x -> x * m);
			});
			modsByRegions(mods, 0, (m, r) -> {
				var coe = hlP2Map.get(sp0Alias, r);
				coe.scalarInPlace(1, (FloatUnaryOperator) x -> x * m);
			});
			modsByRegions(mods, 0, (m, r) -> {
				var coe = hlP3Map.get(sp0Alias, r);
				coe.scalarInPlace(1, (FloatUnaryOperator) x -> {
					if (x > 0.0f && x < 1.0e06f) {
						return x * m;
					}
					return x;
				});
			});
			for (var primarySp : GenusDefinitionParser.getSpeciesAliases(control)) {
				modsByRegions(mods, 2, (m, r) -> {
					var coe = hlNPMap.get(sp0Alias, primarySp, r);
					if (coe.getEquationIndex() == 1) {
						coe.scalarInPlace(1, (FloatUnaryOperator) x -> x * m);
					}
				});
			}
		}
	}

	private void modify300(
			Map<String, Object> control, final MatrixMap2<String, Region, Float> decayMap,
			final MatrixMap2<String, Region, Float> wasteMap, Map<String, Object> entry, int sequence
	) throws ValueParseException {
		// Modifiers are per region for Decay and Waste, for the specified species, set
		// the modifier map
		var sp0Index = sequence - 300;
		var sp0Aliases = getSpeciesByIndex(sp0Index, control);
		var mods = getMods(4, entry);

		for (var sp0Alias : sp0Aliases) {
			modsByRegions(mods, 0, (m, r) -> decayMap.put(sp0Alias, r, m));
			modsByRegions(mods, 2, (m, r) -> wasteMap.put(sp0Alias, r, m));
		}
	}

	private void modify200(
			Map<String, Object> control, final MatrixMap2<String, Region, Float> baMap,
			final MatrixMap2<String, Region, Float> dqMap, Map<String, Object> entry, int sequence
	) throws ValueParseException {
		// Modifiers are per region for BA and DQ, for each species, set the modifier
		// map
		var mods = getMods(4, entry);
		var sp0Index = sequence - 200;
		var sp0Aliases = getSpeciesByIndex(sp0Index, control);
		for (var sp0Alias : sp0Aliases) {
			modsByRegions(mods, 0, (m, r) -> baMap.put(sp0Alias, r, m));
			modsByRegions(mods, 2, (m, r) -> dqMap.put(sp0Alias, r, m));
		}
	}

	private void modify98(
			Map<String, Object> control, final Optional<MatrixMap2<String, Region, Coefficients>> vetBqMap,
			Map<String, Object> entry
	) throws ValueParseException {
		// If modifiers are per region, for each species, multiply the first coefficient
		// for veteran BQ by the region appropriate modifier.
		var mods = getMods(2, entry);
		var sp0Aliases = GenusDefinitionParser.getSpeciesAliases(control);
		for (var sp0Alias : sp0Aliases) {
			final float coastalMod = mods.get(0);
			final float interiorMod = mods.get(1);

			vetBqMap.ifPresent(map -> {
				if (coastalMod != 0.0) {
					var coe = map.get(sp0Alias, Region.COASTAL);
					coe.scalarInPlace(1, (FloatUnaryOperator) x -> x * coastalMod);
				}
				if (interiorMod != 0.0) {
					var coe = map.get(sp0Alias, Region.INTERIOR);
					coe.scalarInPlace(1, (FloatUnaryOperator) x -> x * interiorMod);
				}
			});
		}
	}

	void modsByRegions(List<Float> mods, int offset, BiConsumer<Float, Region> modifier) {
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
		var index = ipoint[this.jprogram.getJProgramNumber() - 1];
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
	public ControlKey getControlKey() {
		return ControlKey.MODIFIER_FILE;
	}

	@Override
	public void defaultModify(Map<String, Object> control) {
		var spAliases = GenusDefinitionParser.getSpeciesAliases(control);
		var regions = Arrays.asList(Region.values());

		var baModifiers = new MatrixMap2Impl<String, Region, Float>(spAliases, regions, (k1, k2) -> 1f);
		control.put(ControlKey.BA_MODIFIERS.name(), baModifiers);

		var dqModifiers = new MatrixMap2Impl<String, Region, Float>(spAliases, regions, (k1, k2) -> 1f);
		control.put(ControlKey.DQ_MODIFIERS.name(), dqModifiers);

		var decayModifiers = new MatrixMap2Impl<String, Region, Float>(spAliases, regions, (k1, k2) -> 0f);
		control.put(CONTROL_KEY_MOD301_DECAY.name(), decayModifiers);

		var wasteModifiers = new MatrixMap2Impl<String, Region, Float>(spAliases, regions, (k1, k2) -> 0f);
		control.put(CONTROL_KEY_MOD301_WASTE.name(), wasteModifiers);
	}

}
