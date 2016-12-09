/**
 * 
 */
package org.atum.jvcp.cache;

import org.atum.jvcp.model.EcmRequest;

/**
 * @author <a href="https://github.com/atum-martin">atum-martin</a>
 * @since 8 Dec 2016 21:00:28
 */
public interface CacheExchangeInterface {

	public EcmRequest peekCache(int ecmHash);
	public boolean addEntry(int ecmHash,EcmRequest dcw);
}
