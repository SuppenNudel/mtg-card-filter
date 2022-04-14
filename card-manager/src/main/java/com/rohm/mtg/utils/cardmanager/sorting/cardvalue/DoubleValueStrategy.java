package com.rohm.mtg.utils.cardmanager.sorting.cardvalue;

import java.util.function.Function;

import com.rohm.mtg.utils.cardmanager.sorting.CompareOperator;
import com.rohm.mtg.utils.dragonshield.collection.CollectionCard;

public class DoubleValueStrategy extends CardValueStrategy<Double> {

	public DoubleValueStrategy(String key, String name, Function<CollectionCard, Double> converter) {
		super(key, name, converter);
	}

	@Override
	public Double parse(String inputText) {
		if(inputText.isEmpty()) {
			return null;
		}
		try {
			return Double.parseDouble(inputText);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	@Override
	protected boolean compareWithOperator(Double actualValue, Double inputValue, CompareOperator operator) {
		int compare = Double.compare(actualValue, inputValue);
		switch (operator) {
		case EQUALS: return compare == 0;
		case GREATER_THAN: return compare > 0;
		case LESS_THAN: return compare < 0;
		default:
			throw new RuntimeException("Not a legal double comparison");
		}
	}

}
