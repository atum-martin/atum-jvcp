/**
 * 
 */
package org.atum.jvcp;

import java.util.Arrays;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.atum.jvcp.account.AccountStore;
import org.atum.jvcp.cache.ClusteredCache;
import org.atum.jvcp.model.EcmRequest;
import org.atum.jvcp.model.Provider;
import org.atum.jvcp.net.codec.cccam.CCcamSession;

/**
 * @author <a href="https://github.com/atum-martin">atum-martin</a>
 * @since 8 Dec 2016 23:29:32
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
	 * @param Not used
	 */
	public static void main(String[] args) {
		BasicConfigurator.configure();
		AccountStore.getSingleton();
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

	public static boolean handleEcmAnswer(long cspHash, byte[] cw) {
		//long ecmHash = EcmRequest.computeEcmHash(cspHash);
		//logger.info("ECM cache entry for: "+cspHash);
		EcmRequest req = CardServer.getPendingCache().peekCache(cspHash);
		if (req != null) {
			req.setDcw(cw);
			getCache().addEntry(cspHash, req);
			getPendingCache().removeRequest(cspHash);
			req.fireActionListeners();
			return true;
		} else {
			//EcmRequest either exists in cache or no pending requests have come in.
			//We want to check does the DCW differ from the cache entry.
			req = CardServer.getCache().peekCache(cspHash);
		}
		
		if (req != null) {
			//this should never fail
			if (DCW_CHECKING && req.hasAnswer()) {
				if (!Arrays.equals(cw, req.getDcw())) {
					logger.warn("Duplicate ECM with different DCW. "+cspHash+" "+sum(cw));
				}
			}
			return true;
		}
		return false;
	}
	
	private static int sum(byte[] ecmd5) {
		int sum = 0;
		for(byte b : ecmd5){
			sum += b;
		}
		return sum;
	}

	public static EcmRequest createEcmRequest(int cardId, int provider, int shareId, int serviceId, byte[] ecm, long cspHash){
		if(cspHash == 0L){
			//no hash found compute it.
			cspHash = EcmRequest.computeEcmHash(ecm);
		}
		EcmRequest answer = new EcmRequest(cardId, new Provider(provider), shareId, serviceId, ecm, false);
		answer.setCspHash(cspHash);
		getPendingCache().addEntry(cspHash, answer);
		return answer;
	}

	public static EcmRequest handleEcmRequest(CCcamSession session, int cardId, int provider, int shareId, int serviceId, byte[] ecm) {
		long cspHash = EcmRequest.computeEcmHash(ecm);
		EcmRequest answer = CardServer.getCache().peekCache(cspHash);
		if (answer != null) {
			return answer;
		}
		answer = createEcmRequest(cardId, provider, shareId, serviceId, ecm, cspHash);
		answer.addListener(session);
		return answer;
	}

}
