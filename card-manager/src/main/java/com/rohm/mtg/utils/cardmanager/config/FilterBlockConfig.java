package com.rohm.mtg.utils.cardmanager.config;

import com.rohm.mtg.utils.cardmanager.sorting.CompareOperator;

public class FilterBlockConfig {

	private boolean not;
	private String userInput;
	private String valueStrategyKey;
	private CompareOperator compareOperator;

	public FilterBlockConfig(String userInput, String valueStrategyKey, CompareOperator compareOperator, boolean not) {
		this.userInput = userInput;
		this.valueStrategyKey = valueStrategyKey;
		this.compareOperator = compareOperator;
		this.not = not;
	}

	public String getUserInput() {
		return userInput;
	}
	public String getValueStrategyKey() {
		return valueStrategyKey;
	}
	public CompareOperator getCompareOperator() {
		return compareOperator;
	}
	public boolean isNot() {
		return not;
	}

}
