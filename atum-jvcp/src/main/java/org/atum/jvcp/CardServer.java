/**
 * 
 */
package org.atum.jvcp;

import java.util.Arrays;

import org.apache.log4j.Logger;
import org.atum.jvcp.cache.ClusteredCache;
import org.atum.jvcp.model.EcmRequest;
import org.atum.jvcp.model.Provider;
import org.atum.jvcp.net.codec.cccam.CCcamSession;

/**
 * @author <a href="https://github.com/atum-martin">atum-martin</a>
 * @since 8 Dec 2016 22:39:08
 */
public class CardServer {

	/**
	 * Instance of log4j logger
	 */
	private static Logger logger = Logger.getLogger(CCcamServer.class);

	private static ClusteredCache cache = new ClusteredCache();
	private static ClusteredCache pendingEcms = new ClusteredCache();
	
	private static final boolean DCW_CHECKING = true;

	/**
	 * Main method used to initialize a blank cccam server with clustered cache.
	 * 
	 * @param Not
	 *            used
	 */
	public static void main(String[] args) {
		new CCcamServer(12000);
	}

	public static ClusteredCache getCache() {
		return cache;
	}

	/**
	 * Returns a {@link ClusteredCache} used for queuing cache requests that
	 * were not found in {@link CardServer#cache} when the message was recieved.
	 * 
	 * @return Returns a {@link ClusteredCache} used for queuing cache requests
	 *         that were not found in {@link CardServer#cache} when the message
	 *         was recieved.
	 */
	public static ClusteredCache getPendingCache() {
		return pendingEcms;
	}

	public static boolean handleEcmAnswer(byte[] ecmd5, byte[] cw) {
		long ecmHash = EcmRequest.computeEcmHash(ecmd5);
		EcmRequest req = CardServer.getPendingCache().peekCache(ecmHash);
		if (req != null) {
			req.setDcw(cw);
			getCache().addEntry(ecmHash, req);
			getPendingCache().removeRequest(ecmHash);
			req.fireActionListeners();
			return true;
		} else {
			//EcmRequest either exists in cache or no pending requests have come in.
			//We want to check does the DCW differ from the cache entry.
			req = CardServer.getCache().peekCache(ecmHash);
		}
		
		if (req != null) {
			//this should never fail
			if (DCW_CHECKING && req.hasAnswer()) {
				if (!Arrays.equals(cw, req.getDcw())) {
					logger.warn("Duplicate ECM with different DCW.");
				}
			}
			return true;
		}
		return false;
	}
	
	public static EcmRequest createEcmRequest(int cardId, int provider, int shareId, int serviceId, byte[] ecm, long ecmHash){
		if(ecmHash == 0L){
			//no hash found compute it.
			ecmHash = EcmRequest.computeEcmHash(ecm);
		}
		EcmRequest answer = new EcmRequest(cardId, new Provider(provider), shareId, serviceId, ecm, false);
		answer.setEcmHash(ecmHash);
		getPendingCache().addEntry(ecmHash, answer);
		return answer;
	}

	public static EcmRequest handleEcmRequest(CCcamSession session, int cardId, int provider, int shareId, int serviceId, byte[] ecm) {
		long ecmHash = EcmRequest.computeEcmHash(ecm);
		EcmRequest answer = CardServer.getCache().peekCache(ecmHash);
		if (answer != null) {
			return answer;
		}
		answer = createEcmRequest(cardId, provider, shareId, serviceId, ecm, ecmHash);
		answer.addListener(session);
		return answer;
	}

}
