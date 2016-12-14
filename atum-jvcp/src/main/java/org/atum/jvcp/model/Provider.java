package org.atum.jvcp.model;

/**
 * @author <a href="https://github.com/atum-martin">atum-martin</a>
 * @since 23 Nov 2016 00:05:46
 */

public class Provider {

	private int providerId;
	
	public Provider(int providerId) {
		this.providerId = providerId;
	}

	public int getProviderId(){
		return providerId;
	}
}
