package com.rohm.mtg.utils.cardmanager.model;

import org.junit.jupiter.api.Test;

import de.rohmio.mtg.scryfall.api.ScryfallApi;
import de.rohmio.mtg.scryfall.api.endpoints.factories.BulkDataEndpointFactory.Type;
import de.rohmio.mtg.scryfall.api.model.ScryfallError;

public class BulkDataTest {

	@Test
	public void test() throws ScryfallError {
		ScryfallApi.bulkData.bulkData(Type.ORACLE_CARDS, true).get();
	}

}
