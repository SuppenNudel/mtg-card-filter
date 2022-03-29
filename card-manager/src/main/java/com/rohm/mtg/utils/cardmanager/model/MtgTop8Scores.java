package com.rohm.mtg.utils.cardmanager.model;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.rohmio.mtg.mtgtop8.api.MtgTop8Api;
import de.rohmio.mtg.mtgtop8.api.endpoints.SearchEndpoint;
import de.rohmio.mtg.mtgtop8.api.model.CompLevel;
import de.rohmio.mtg.mtgtop8.api.model.MtgTop8Format;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class MtgTop8Scores {

	private static Map<String, Map<MtgTop8Format, IntegerProperty>> mtgtop8Scores = new HashMap<>();
	private static ExecutorService executor = Executors.newFixedThreadPool(20);

	private static synchronized IntegerProperty scoreFromMap(String cardName, MtgTop8Format format) {
		Map<MtgTop8Format, IntegerProperty> formatScores = mtgtop8Scores.get(cardName);
		if(formatScores == null) {
			formatScores = new HashMap<>();
			mtgtop8Scores.put(cardName, formatScores);
		}
		IntegerProperty score = formatScores.get(format);
		if(score == null) {
			score = new SimpleIntegerProperty(-1);
			formatScores.put(format, score);
		}
		return score;
	}

	public static IntegerProperty getScore(String cardName, List<CompLevel> compLevels, MtgTop8Format format, LocalDate startDate) {
		return getScore(cardName, compLevels, format, startDate, true);
	}

	private static IntegerProperty getScore(String cardName, List<CompLevel> compLevels, MtgTop8Format format, LocalDate startDate, boolean retry) {
		IntegerProperty score = scoreFromMap(cardName, format);
		executor.execute(() -> {
			int result = MtgTop8Api.search()
				.cards(cardName)
				.compLevel(compLevels)
				.sideboard(true)
				.mainboard(true)
				.format(format)
				.startdate(startDate)
				.get();
			score.set(result);
			if(result == SearchEndpoint.NO_MATCH) {
				// repeat request with adjusted name
				getScore(cardName.split("//")[0].trim(), compLevels, format, startDate, false);
			}
		});
		return score;
	}

}
