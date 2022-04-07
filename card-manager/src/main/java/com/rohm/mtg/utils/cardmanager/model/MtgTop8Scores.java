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
	private static ExecutorService executor = Executors.newFixedThreadPool(50);

	private static synchronized IntegerProperty scoreFromMap(String cardName, MtgTop8Format format) {
		Map<MtgTop8Format, IntegerProperty> cardScores = mtgtop8Scores.get(cardName);
		if(cardScores == null) {
			cardScores = new HashMap<>();
			mtgtop8Scores.put(cardName, cardScores);
		}
		IntegerProperty score = cardScores.get(format);
		if(score == null) {
			score = new SimpleIntegerProperty(null, cardName+"@"+format, -1);
			cardScores.put(format, score);
		}
		return score;
	}

	public synchronized static IntegerProperty getScore(String cardName, List<CompLevel> compLevels, MtgTop8Format format, LocalDate startDate) {
		IntegerProperty score = scoreFromMap(cardName, format);
		// TODO adjust cardName before requesting
		if(score.get() == -1) {
			executor.execute(() -> {
				int result = -1;
				String fixedCardName = cardName;
				for(int i=0; i < 2 && result < 0;++i) {
					result = requestScore(fixedCardName, compLevels, format, startDate);
					if(result == SearchEndpoint.NO_MATCH) {
						// repeat request with adjusted name
						fixedCardName = cardName.split("//")[0].trim();
					}
				}
				score.set(result);
			});
		}
		return score;
	}

	private static int requestScore(String cardName, List<CompLevel> compLevels, MtgTop8Format format, LocalDate startDate) {
		int result = MtgTop8Api.search()
				.cards(cardName)
				.compLevel(compLevels)
				.sideboard(true)
				.mainboard(true)
				.format(format)
				.startdate(startDate)
				.get();
		return result;
	}

}
