package com.rohm.mtg.utils.cardmanager.model;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import de.rohmio.mtg.mtgtop8.api.MtgTop8Api;
import de.rohmio.mtg.mtgtop8.api.model.CompLevel;
import de.rohmio.mtg.mtgtop8.api.model.MtgTop8Format;

public class TestStapleScan {

	@Test
	public void test() {
		String cardName = "Abiding Grace";
		List<CompLevel> compLevels = Arrays.asList(CompLevel.COMPETITIVE, CompLevel.MAJOR, CompLevel.PROFESSIONAL);
		MtgTop8Format format = MtgTop8Format.MODERN;
		LocalDate startDate = LocalDate.now().minusMonths(12);
		System.out.println(startDate);
		Integer score = MtgTop8Api.search()
			.cards(cardName)
			.compLevel(compLevels)
			.sideboard(true)
			.mainboard(true)
			.format(format)
			.startdate(startDate)
			.get();
		System.out.println(score);
	}

}
