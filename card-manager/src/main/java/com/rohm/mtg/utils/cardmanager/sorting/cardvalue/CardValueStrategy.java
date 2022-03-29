package com.rohm.mtg.utils.cardmanager.sorting.cardvalue;

import java.util.function.Function;

import com.rohm.mtg.utils.cardmanager.sorting.CompareOperator;
import com.rohm.mtg.utils.dragonshield.collection.CollectionCard;

public abstract class CardValueStrategy<T> {

	private String name;
	private Function<CollectionCard, T> converter;

	protected CardValueStrategy(String name, Function<CollectionCard, T> converter) {
		this.name = name;
		this.converter = converter;
	}

	@Override
	public String toString() {
		return name;
	}

	public T convert(CollectionCard card) {
		return converter.apply(card);
	}
	public abstract T parse(String inputText);

	protected abstract boolean compareWithOperator(T actualValue, T inputValue, CompareOperator operator);
	public boolean compareWithOperator(CollectionCard actualValue, String inputText, CompareOperator operator) {
		if(operator == null) {
			return true;
		}
		T parsed = parse(inputText);
		T cardValue = convert(actualValue);
		boolean compareResult = compareWithOperator(cardValue, parsed, operator);
		return compareResult;
	}

}
