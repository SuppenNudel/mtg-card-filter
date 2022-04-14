package com.rohm.mtg.utils.cardmanager.model;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.rohm.mtg.utils.dragonshield.DragonShieldReaderFactory;
import com.rohm.mtg.utils.dragonshield.DragonShieldReaderFactory.CollectionReader;
import com.rohm.mtg.utils.dragonshield.collection.CollectionCard;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class DragonShield {

	private static CollectionReader collectionReader;

	private static ObservableList<CollectionCard> collectionCards = FXCollections.observableArrayList();

	public static ObservableList<CollectionCard> getCollection() {
		return collectionCards;
	}

	public static ObservableList<CollectionCard> loadCollection(File file, boolean forceLoad) throws IOException {
		collectionReader = DragonShieldReaderFactory.collection(file);
		List<CollectionCard> cards = collectionReader.getCards(true);
		collectionCards.setAll(cards);
		return collectionCards;
	}

}
