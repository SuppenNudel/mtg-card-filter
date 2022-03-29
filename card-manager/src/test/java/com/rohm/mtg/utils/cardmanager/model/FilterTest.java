package com.rohm.mtg.utils.cardmanager.model;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.rohm.mtg.utils.dragonshield.DragonShieldReaderFactory;
import com.rohm.mtg.utils.dragonshield.DragonShieldReaderFactory.CollectionReader;
import com.rohm.mtg.utils.dragonshield.collection.CollectionCard;

import de.rohmio.mtg.scryfall.api.ScryfallApi;
import de.rohmio.mtg.scryfall.api.endpoints.factories.BulkDataEndpointFactory;
import de.rohmio.mtg.scryfall.api.model.CardObject;
import de.rohmio.mtg.scryfall.api.model.ScryfallError;
import de.rohmio.mtg.scryfall.api.model.enums.Color;

public class FilterTest {

	private static final File allFoldersFile = new File("src/test/resources/all-folders.csv");

	private static List<CollectionCard> collectionCards;
//	private static List<CardObject> scryfallCards;
	private static Map<String, CardObject> scryfallCards;

	@BeforeAll
	public static void init() throws IOException, ScryfallError {
		CollectionReader collectionReader = DragonShieldReaderFactory.collection(allFoldersFile);
		collectionCards = collectionReader.getCards(false);
		System.out.println("loaded "+collectionCards.size()+" collection entries");
		scryfallCards = ScryfallApi.bulkData.bulkData(BulkDataEndpointFactory.Type.ORACLE_CARDS, false)
		.save(true).useCache(true).get()
		.stream()
		.collect(Collectors.toMap(CardObject::getName, c -> c, (a, b) -> a));
		System.out.println("loaded "+scryfallCards.size()+" scryfall cards");
	}

	@Test
	public void whiteOneDropCreatures() throws IOException {
		collectionCards.stream()
		.filter(collCard -> !collCard.getFolderName().equals("White ⚪"))
		.filter(collCard -> !collCard.getFolderName().contains("Staples"))
		.map(collCard -> {
			String cardName = collCard.getCardName().replace('-', ' ');
			CardObject scryfallCard = scryfallCards.get(cardName);
			Pair pair = new Pair(collCard, scryfallCard);
			return pair;
		})
		.filter(p -> p.getScryfallCard() != null)
		.filter(p -> Double.valueOf(1).equals(p.getScryfallCard().getCmc()))
		.filter(p -> p.getScryfallCard().getColor_identity().size() == 1)
		.filter(p -> p.getScryfallCard().getColor_identity().contains(Color.WHITE))
		.filter(p -> p.getScryfallCard().getType_line().contains("Creature"))
		.sorted((o1, o2) -> o1.getCollCard().getFolderName().compareTo(o2.getCollCard().getFolderName()))
		.forEach(p -> System.out.println(p.getCollCard()));
	}

	private class Pair {
		private CollectionCard collectionCard;
		private CardObject scryfallCard;
		private Pair(CollectionCard collectionCard, CardObject scryfallCard) {
			this.collectionCard = collectionCard;
			this.scryfallCard = scryfallCard;
		}
		public CollectionCard getCollCard() {
			return collectionCard;
		}
		public CardObject getScryfallCard() {
			return scryfallCard;
		}
	}

}
