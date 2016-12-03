package org.atum.jvcp.model;

import org.atum.jvcp.cache.HashCache;

public class Card {
	
	private int shareId, remoteId, cardId;

	public Card(int cardId, int shareId, int remoteId) {
		this.cardId = cardId;
		this.shareId = shareId;
		this.remoteId = remoteId;
		HashCache.getSingleton().setupCache(cardId);
	}

	public int getShare() {
		return shareId;
	}

	public int getHops() {
		return 0;
	}

	public long getNodeId() {
		return remoteId;
	}

	public int getCardId() {
		return cardId;
	}

	public Provider[] getProviders() {
		return new Provider[]{new Provider(),};
	}

	public long getSerial() {
		return 0;
	}

	public int getReshare() {
		return 1;
	}

}
