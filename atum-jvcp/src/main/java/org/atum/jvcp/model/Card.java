package org.atum.jvcp.model;

import org.atum.jvcp.cache.HashCache;

/**
 * A java POJO which represents a card being used as a reader in a conditional
 * access module.
 * 
 * @author <a href="https://github.com/atum-martin">atum-martin</a>
 * @since 04/12/2016
 *
 */

public class Card {

	/**
	 * A unique identifier for this card that can be used across multiple nodes
	 * to determine what cards have already been sent an ecm request.
	 */
	private int shareId;

	/**
	 * The nodeId the card belongs to.
	 */
	private int remoteId;

	/**
	 * The cardId. Each provider generally has 1 card ID for their service at
	 * any one point in time.
	 */
	private int cardId;

	/**
	 * The constructor for a card object. If this is a previously encountered
	 * card ID the necessary cache mappings will be created now.
	 * 
	 * @param cardId
	 *            The cardId this POJO should represent.
	 * @param shareId
	 *            {@link shareId}
	 * @param remoteId
	 *            The nodeId the card belongs to.
	 */
	public Card(int cardId, int shareId, int remoteId) {
		this.cardId = cardId;
		this.shareId = shareId;
		this.remoteId = remoteId;
		HashCache.getSingleton().setupCache(cardId);
	}

	/**
	 * Returns the integer value shareId.
	 * 
	 * @return An integer representing a unique identifier for this card.
	 */
	public int getShare() {
		return shareId;
	}

	/**
	 * Returns the lowest node hop count for this card.
	 * 
	 * @return An integer between 0-10
	 */
	public int getHops() {
		return 0;
	}

	/**
	 * Returns the nodeId this card belongs to.
	 * 
	 * @return A long between {@link Long#MAX_VALUE} and {@link Long#MIN_VALUE}
	 */
	public long getNodeId() {
		return remoteId;
	}

	/**
	 * An integer value representing a proividers card ID. <a
	 * href="http://www.streamboard.tv/oscam/wiki/CardsList">Card List</a>
	 * 
	 * @return An integer value representing the card identifier.
	 */
	public int getCardId() {
		return cardId;
	}

	/**
	 * Returns an array of providers associated with this card.
	 * 
	 * @return An array of providers associated with this card.
	 */
	public Provider[] getProviders() {
		return new Provider[] { new Provider(), };
	}

	/**
	 * An integer value for the serial value of the card reader. This is rarely
	 * set.
	 * 
	 * @return An integer value for the serial value of the card reader.
	 */
	public long getSerial() {
		return 0;
	}

	/**
	 * A CCcam field that represents how this reader can be shared with
	 * connected clients.
	 * 
	 * @return An integer value that decreases by one for each node this card is
	 *         sent to.
	 */
	public int getReshare() {
		return 1;
	}

}
