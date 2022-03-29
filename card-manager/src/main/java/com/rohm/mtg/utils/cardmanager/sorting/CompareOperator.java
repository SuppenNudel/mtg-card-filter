package com.rohm.mtg.utils.cardmanager.sorting;

import java.util.Arrays;
import java.util.List;

import com.rohm.mtg.utils.cardmanager.sorting.cardvalue.CardValueStrategy;
import com.rohm.mtg.utils.cardmanager.sorting.cardvalue.DoubleValueStrategy;
import com.rohm.mtg.utils.cardmanager.sorting.cardvalue.IntegerValueStrategy;
import com.rohm.mtg.utils.cardmanager.sorting.cardvalue.ListValueStrategy;
import com.rohm.mtg.utils.cardmanager.sorting.cardvalue.StringValueStrategy;

public enum CompareOperator {

	LESS_THAN("<", Arrays.asList(IntegerValueStrategy.class, DoubleValueStrategy.class)),
	EQUALS("=", Arrays.asList(StringValueStrategy.class, IntegerValueStrategy.class, DoubleValueStrategy.class, ListValueStrategy.class)),
	GREATER_THAN(">", Arrays.asList(IntegerValueStrategy.class, DoubleValueStrategy.class)),
	CONTAINS("contains", Arrays.asList(StringValueStrategy.class, ListValueStrategy.class));

	private String display;
	private List<Class<? extends CardValueStrategy>> applicable;

	private CompareOperator(String display, List<Class<? extends CardValueStrategy>> classes) {
		this.display = display;
		this.applicable = classes;
	}

	@Override
	public String toString() {
		return display;
	}

	public boolean isApplicable(Class<? extends CardValueStrategy> cardValueStrategy) {
		return applicable.contains(cardValueStrategy);
	}

}
