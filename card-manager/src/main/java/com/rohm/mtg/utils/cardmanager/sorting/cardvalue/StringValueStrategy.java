package com.rohm.mtg.utils.cardmanager.sorting.cardvalue;

import java.util.function.Function;

import com.rohm.mtg.utils.cardmanager.sorting.CompareOperator;
import com.rohm.mtg.utils.dragonshield.collection.CollectionCard;

public class StringValueStrategy extends CardValueStrategy<String> {

	public StringValueStrategy(String key, String name, Function<CollectionCard, String> converter) {
		super(key, name, converter);
	}

	@Override
	public String parse(String inputText) {
		return inputText.toLowerCase();
	}

	@Override
	protected boolean compareWithOperator(String actualValue, String inputValue, CompareOperator operator) {
		actualValue = actualValue.toLowerCase();
		switch (operator) {
		case EQUALS: return actualValue.equals(inputValue);
		case CONTAINS: return actualValue.contains(inputValue);
		default:
			throw new RuntimeException("Not a legal string comparison");
		}
	}

}
