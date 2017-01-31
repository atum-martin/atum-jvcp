/**
 * 
 */
package org.atum.jvcp.model;

/**
 * @author <a href="https://github.com/atum-martin">atum-martin</a>
 * @since 26 Jan 2017 23:34:06
 */
public class CardProfile {

	private int cardId;
	private boolean allProviders = true;
	private int provider = 0;
	private int cacheWait = 0;
	
	public CardProfile(){
		cardId = 0;
	}

	public int getCardId() {
		return cardId;
	}
	
	public int getCacheWaitTime(){
		return cacheWait;
	}

	public void setCardId(int cardId) {
		this.cardId = cardId;
	}
	
	public int getProvider(){
		return provider;
	}
	
	public boolean allProviders(){
		return allProviders;
	}
}
