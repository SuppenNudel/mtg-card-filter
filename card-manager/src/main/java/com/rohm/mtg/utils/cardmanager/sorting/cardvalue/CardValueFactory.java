package com.rohm.mtg.utils.cardmanager.sorting.cardvalue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.rohm.mtg.utils.dragonshield.collection.CollectionCard;

import de.rohmio.mtg.scryfall.api.ScryfallApi;
import de.rohmio.mtg.scryfall.api.endpoints.factories.BulkDataEndpointFactory;
import de.rohmio.mtg.scryfall.api.model.CardObject;
import de.rohmio.mtg.scryfall.api.model.ScryfallError;
import de.rohmio.mtg.scryfall.api.model.enums.Color;

public class CardValueFactory {

	public static Map<String, CardObject> scryfallCards = load();

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

	public static CardObject getScryfallCard(String cardName) {
		return scryfallCards.get(cardName);
	}

	public static final CardValueStrategy<Integer> quantity = new IntegerValueStrategy("quantity", "#", CollectionCard::getQuantity);
	public static final CardValueStrategy<String> setCode = new StringValueStrategy("setcode", "Set Code", card -> {
		String setCode = card.getSetCode();
		// Guild Kit Workaround
		return setCode.startsWith("GK") ? setCode.replaceAll("_.+$", "") : setCode;
	});
	public static final CardValueStrategy<String> cardNumber = new StringValueStrategy("cardnumber", "Card Number", CollectionCard::getCardNumber);
	public static final CardValueStrategy<String> printing = new StringValueStrategy("printing", "Printing", CollectionCard::getPrinting);
	public static final CardValueStrategy<String> language = new StringValueStrategy("language", "Language", CollectionCard::getLanguage);
	public static final CardValueStrategy<Double> price = new DoubleValueStrategy("price", "Price", CollectionCard::getAvg);
	public static final CardValueStrategy<List<String>> type = new ListValueStrategy<>("type", "Type", card -> Arrays.asList(scryfallCards.get(card.getCardName()).getType_line().toLowerCase().split(" ")), str -> str);
	public static final CardValueStrategy<Double> cmc = new DoubleValueStrategy("cmc", "CMC", card -> scryfallCards.get(card.getCardName()).getCmc());
	public static final CardValueStrategy<Integer> edhrecRank = new IntegerValueStrategy("edhrec", "EDHREC Rank", card -> scryfallCards.get(card.getCardName()).getEdhrec_rank());
	public static final CardValueStrategy<String> name = new StringValueStrategy("cardname", "Card Name", card -> {
		try {
			return scryfallCards.get(card.getCardName()).getName();
		} catch (NullPointerException e) {
			System.err.println("No scryfall card for: " + card);
		}
		return card.getCardName();
	});
	public static final CardValueStrategy<String> folder = new StringValueStrategy("folder", "Folder", CollectionCard::getFolderName);
	public static final CardValueStrategy<List<Color>> colorId = new ListValueStrategy<>("colorid", "Color Id", card -> scryfallCards.get(card.getCardName()).getColor_identity(), s -> {
		try {
			Color valueOf = Color.valueOf(s.toUpperCase());
			return valueOf;
		} catch (IllegalArgumentException e) {
			return null;
		}
	});

	public static final CardValueStrategy<Double> priceBought = new DoubleValueStrategy("pricebought", "\"Price Bought\"", CollectionCard::getPriceBought);
	public static final CardValueStrategy<Double> priceAvg = new DoubleValueStrategy("priceavg", "Price Average", CollectionCard::getAvg);
	public static final CardValueStrategy<Double> priceLow = new DoubleValueStrategy("pricelow", "Price Low", CollectionCard::getLow);
	public static final CardValueStrategy<Double> priceTrend = new DoubleValueStrategy("price, trend", "Price Trend", CollectionCard::getTrend);

	public static final CardValueStrategy<String> setName = new StringValueStrategy("setname", "Set Name", CollectionCard::getSetName);

	public static final Map<String, CardValueStrategy<? extends Object>> allCardValueStrategies = new Supplier<Map<String, CardValueStrategy<? extends Object>>>() {
		@Override
		public Map<String, CardValueStrategy<? extends Object>> get() {
			Map<String, CardValueStrategy<? extends Object>> map = new HashMap<>();
			map.put(price.getKey(), price);
			map.put(type.getKey(), type);
			map.put(cmc.getKey(), cmc);
			map.put(edhrecRank.getKey(), edhrecRank);
			map.put(name.getKey(), name);
			map.put(folder.getKey(), folder);
			map.put(colorId.getKey(), colorId);
			map.put(priceBought.getKey(), priceBought);
			map.put(priceAvg.getKey(), priceAvg);
			map.put(priceLow.getKey(), priceLow);
			map.put(priceTrend.getKey(), priceTrend);
			map.put(setName.getKey(), setName);
			map.put(setCode.getKey(), setCode);
			map.put(language.getKey(), language);
			map.put(printing.getKey(), printing);
			map.put(cardNumber.getKey(), cardNumber);
			return map;
		}
	}.get();
//	public static final CardValueStrategy<List<Integer>> score = new ListValueStrategy<>("Staple Score", card -> {
//		String cardName = card.getCardName();
//		List<CompLevel> compLevels = UserConfig.getConfig().getList(UserConfigKey.COMP_LEVELS, CompLevel.class);
//		List<MtgTop8Format> formats = UserConfig.getConfig().getList(UserConfigKey.FORMATS, MtgTop8Format.class);
//		Integer monthsAgo = UserConfig.getConfig().get(UserConfigKey.TIME_PERIOD);
//		ChronoUnit timeUnit = UserConfig.getConfig().get(UserConfigKey.TIME_UNIT);
//		LocalDate startDate = LocalDate.now().minus(monthsAgo, timeUnit);
//		List<Integer> scores = new ArrayList<>();
//		for (MtgTop8Format format : formats) {
//			IntegerProperty score = MtgTop8Scores.getScore(cardName, compLevels, format, startDate);
//			scores.add(score.get());
//		}
//		return scores;
//	}, Integer::parseInt);

}
