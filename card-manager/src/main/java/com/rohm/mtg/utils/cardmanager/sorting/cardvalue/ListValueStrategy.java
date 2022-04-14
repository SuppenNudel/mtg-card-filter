package com.rohm.mtg.utils.cardmanager.sorting.cardvalue;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.rohm.mtg.utils.cardmanager.sorting.CompareOperator;
import com.rohm.mtg.utils.dragonshield.collection.CollectionCard;

public class ListValueStrategy<T> extends CardValueStrategy<List<T>> {

	private Function<String, T> tConverter;

	public ListValueStrategy(String key, String name, Function<CollectionCard, List<T>> converter, Function<String, T> tConverter) {
		super(key, name, converter);
		this.tConverter = tConverter;
	}

	@Override
	public List<T> parse(String inputText) {
		List<String> asList = Arrays.asList(inputText.toLowerCase().split("[,; ]"));
		List<T> arrayList = asList.stream().map(str -> tConverter.apply(str)).filter(t -> t != null).collect(Collectors.toList());
		return arrayList;
	}

	@Override
	protected boolean compareWithOperator(List<T> actualValue, List<T> inputValue, CompareOperator operator) {
		switch (operator) {
		case EQUALS: return inputValue.containsAll(actualValue) && actualValue.size() == inputValue.size();
		case CONTAINS: return actualValue.containsAll(inputValue);
		default:
			throw new RuntimeException("Not a legal list comparison");
		}
	}

}
