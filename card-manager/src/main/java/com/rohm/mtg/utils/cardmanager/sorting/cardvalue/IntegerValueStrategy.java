package com.rohm.mtg.utils.cardmanager.sorting.cardvalue;

import java.util.function.Function;

import com.rohm.mtg.utils.cardmanager.sorting.CompareOperator;
import com.rohm.mtg.utils.dragonshield.collection.CollectionCard;

public class IntegerValueStrategy extends CardValueStrategy<Integer> {

	public IntegerValueStrategy(String key, String name, Function<CollectionCard, Integer> converter) {
		super(key, name, converter);
	}

	@Override
	public Integer parse(String inputText) {
		if(inputText == null) {
			return null;
		}
		try {
			return Integer.parseInt(inputText);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	@Override
	protected boolean compareWithOperator(Integer actualValue, Integer inputValue, CompareOperator operator) {
		int compare = Integer.compare(actualValue, inputValue);
		switch (operator) {
		case EQUALS: return compare == 0;
		case LESS_THAN: return compare < 0;
		case GREATER_THAN: return compare > 0;
		default:
			throw new RuntimeException("Not a legal string comparison");
		}
	}

}
