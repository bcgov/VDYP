package ca.bc.gov.nrs.vdyp.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public enum UtilizationClass {
	SMALL(-1, "<7.5 cm", 0f, 7.5f), //
	ALL(0, ">7.5 cm", 7.5f, 10000f), //
	U75TO125(1, "7.5 - 12.5 cm", 7.5f, 12.5f), //
	U125TO175(2, "12.5 - 17.5 cm", 12.5f, 17.5f), //
	U175TO225(3, "17.5 - 22.5 cm", 17.5f, 22.5f), //
	OVER225(4, ">22.5 cm", 22.5f, 10000f);

	public final int index;
	public final String className;
	public final float lowBound;
	public final float highBound;

	private Optional<UtilizationClass> next = Optional.empty();
	private Optional<UtilizationClass> previous = Optional.empty();

	public static final List<UtilizationClass> ALL_BUT_SMALL = Collections
			.unmodifiableList(new ArrayList<>(Arrays.asList(U75TO125, U125TO175, U175TO225, OVER225)));

	static {
		for (int i = 1; i < UtilizationClass.values().length; i++) {
			UtilizationClass.values()[i].previous = Optional.of(UtilizationClass.values()[i - 1]);
			UtilizationClass.values()[i - 1].next = Optional.of(UtilizationClass.values()[i]);
		}
	}

	UtilizationClass(int index, String className, float lowBound, float highBound) {
		this.index = index;
		this.className = className;
		this.lowBound = lowBound;
		this.highBound = highBound;
	}

	public static UtilizationClass getByIndex(String indexText) {
		try {
			return getByIndex(Integer.parseInt(indexText.strip()));
		} catch (NullPointerException | NumberFormatException e) {
			throw new IllegalArgumentException("UtilizationClass index " + indexText + " is not recognized");
		}
	}

	public static UtilizationClass getByIndex(int index) {

		for (UtilizationClass uc : values()) {
			if (uc.index == index)
				return uc;
		}

		throw new IllegalArgumentException("UtilizationClass index " + index + " is not recognized");
	}

	public Optional<UtilizationClass> next() {
		return this.next;
	}

	public Optional<UtilizationClass> previous() {
		return this.previous;
	}
}