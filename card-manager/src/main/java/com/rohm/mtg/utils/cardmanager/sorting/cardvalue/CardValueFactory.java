package com.rohm.mtg.utils.cardmanager.sorting.cardvalue;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.rohm.mtg.utils.dragonshield.collection.CollectionCard;

import de.rohmio.mtg.scryfall.api.ScryfallApi;
import de.rohmio.mtg.scryfall.api.endpoints.factories.BulkDataEndpointFactory;
import de.rohmio.mtg.scryfall.api.model.CardObject;
import de.rohmio.mtg.scryfall.api.model.ScryfallError;
import de.rohmio.mtg.scryfall.api.model.enums.Color;

public class CardValueFactory {

	private static Map<String, CardObject> scryfallCards = load();

	private static Map<String, CardObject> load() {
		try {
			Map<String, CardObject> collect = ScryfallApi.bulkData.bulkData(BulkDataEndpointFactory.Type.ORACLE_CARDS, true)
			.save(true).useCache(true).get()
			.stream()
			.collect(Collectors.toMap(/* key */ card -> {
				String cardName = card.getName();
				// check for multiple variants
//				if(card.getCollector_number().matches("[0-9]+\\D$")) {
//					System.out.println(card);
//				}
				/* TODO Unquenchable Fury -> real card and "Defeat a God"
				 * Big Furry Monster -> two sides
				 */
				// check for token
				if(card.getType_line().toLowerCase().contains("token")) {
					cardName = String.format("%s Token/%s/%s", cardName, card.getSet().toUpperCase(), card.getCollector_number());
				}
				return cardName;
			}, /* value */ c -> c, /* merge */ (a, b) -> {
				System.out.println(a);
				System.out.println(b);
				return a;
			}));
			// TODO workaround until dragonshield fixes name
			collect.put("Lurrus of the Dream Den", collect.get("Lurrus of the Dream-Den"));
			return collect;
		} catch (ScryfallError e) {
			e.printStackTrace();
		}
		return null;
	}

	public static final CardValueStrategy<Integer> quantity = new IntegerValueStrategy("#", CollectionCard::getQuantity);
	public static final CardValueStrategy<String> setCode = new StringValueStrategy("Set Code", card -> {
		String setCode = card.getSetCode();
		return setCode.startsWith("GK") ? setCode.replaceAll("_.+$", "") : setCode;
	});
	public static final CardValueStrategy<String> cardNumber = new StringValueStrategy("Card Number", CollectionCard::getCardNumber);
	public static final CardValueStrategy<String> printing = new StringValueStrategy("Printing", CollectionCard::getPrinting);
	public static final CardValueStrategy<String> language = new StringValueStrategy("Language", CollectionCard::getLanguage);
	public static final CardValueStrategy<Double> price = new DoubleValueStrategy("Price", CollectionCard::getAvg);
	public static final CardValueStrategy<List<String>> type = new ListValueStrategy<>("Type", card -> Arrays.asList(scryfallCards.get(card.getCardName()).getType_line().toLowerCase().split(" ")), str -> str);
	public static final CardValueStrategy<Double> cmc = new DoubleValueStrategy("CMC", card -> scryfallCards.get(card.getCardName()).getCmc());
	public static final CardValueStrategy<Integer> edhrecRank = new IntegerValueStrategy("EDHREC Rank", card -> scryfallCards.get(card.getCardName()).getEdhrec_rank());
	public static final CardValueStrategy<String> name = new StringValueStrategy("Card Name", card -> {
		try {
			return scryfallCards.get(card.getCardName()).getName();
		} catch (NullPointerException e) {
			System.err.println("No scryfall card for: " + card);
		}
		return card.getCardName();
	});
	public static final CardValueStrategy<String> folder = new StringValueStrategy("Folder", CollectionCard::getFolderName);
	public static final CardValueStrategy<List<Color>> colorId = new ListValueStrategy<>("Color Id", card -> scryfallCards.get(card.getCardName()).getColor_identity(), s -> {
		try {
			Color valueOf = Color.valueOf(s.toUpperCase());
			return valueOf;
		} catch (IllegalArgumentException e) {
			return null;
		}
	});

	public static final List<CardValueStrategy<? extends Object>> allCardValues = Arrays.asList(price, type, cmc, edhrecRank, name, folder, colorId);
//	public static final ListValueStrategy<CollectionCard, List<Integer>> score = new ListValueStrategy<>("Staple Score", card -> MtgTop8Scores.getScore(card.getCardName()));

}
