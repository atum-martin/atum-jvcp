/**
 * 
 */
package org.atum.jvcp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.atum.jvcp.account.AccountStore;
import org.atum.jvcp.cache.ClusteredCache;
import org.atum.jvcp.config.ReaderConfig;
import org.atum.jvcp.model.CamSession;
import org.atum.jvcp.model.EcmRequest;
import org.atum.jvcp.model.Provider;
import org.atum.jvcp.net.NettyBootstrap;
import org.atum.jvcp.net.codec.http.HttpPipeline;

/**
 * @author <a href="https://github.com/atum-martin">atum-martin</a>
 * @since 8 Dec 2016 23:29:32
 */
public class CardServer {

	/**
	 * Instance of log4j logger
	 */
	private static Logger logger = Logger.getLogger(CardServer.class);

	private static ClusteredCache cache = new ClusteredCache();
	private static ClusteredCache pendingEcms = new ClusteredCache();
	private static ArrayList<CamSession> readers = new ArrayList<CamSession>();
	private static int readerRoundRobin = 0;
	
	private static final boolean DCW_CHECKING = true;

	/**
	 * Main method used to initialize a blank cccam server with clustered cache.
	 * 
	 * @param args Not used
	 */
	@SuppressWarnings("unused")
	public static void main(String[] args) {
		BasicConfigurator.configure();
		AccountStore.getSingleton();
		CCcamServer server1 = new CCcamServer("cccam-server1", 12000);
		NewcamdServer server2 = new NewcamdServer("newcamd-server1", 12001);
		NettyBootstrap.listenTcp(new HttpPipeline(),8080);
		ReaderConfig config = new ReaderConfig(new CamServer[]{server1, server2,});
		addAllReaders(server1, readers);
		addAllReaders(server2, readers);
	}

	/**
	 * @param server1
	 * @param readers2
	 */
	private static void addAllReaders(CamServer server1, ArrayList<CamSession> readers2) {
		server1.addReaders(readers);
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

	public static boolean handleEcmAnswer(CamSession session, int cspHash, byte[] cw, int cardId, int serviceId) {
		//long ecmHash = EcmRequest.computeEcmHash(cspHash);
		//logger.info("ECM cache entry for: "+cspHash);
		EcmRequest req = CardServer.getPendingCache().peekCache(cspHash);
		if (req != null) {
			req.setDcw(cw);
			getCache().addEntry(cspHash, req);
			getPendingCache().removeRequest(cspHash);
			logger.info("cache hit for "+Integer.toHexString(req.getCardId())+":"+Integer.toHexString(req.getServiceId())+" by "+session+" for "+req.getSessionsStr()+" sessions.");

			req.fireActionListeners();
			//logger.info("Cache push hit on: "+Integer.toHexString(cardId)+":"+Integer.toHexString(serviceId));
			//logger.info("cache hit for pending dcw dump: "+NetUtils.bytesToString(cw,0,cw.length));
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
					//logger.warn("Duplicate ECM with different DCW. "+cspHash+" "+sum(cw));
				}
			}
			return true;
		}
		return false;
	}

	public static EcmRequest createEcmRequest(CamSession session, int cardId, int provider, int shareId, int serviceId, byte[] ecm, int cspHash, boolean cache){
		if(cspHash == 0L){
			//no hash found compute it.
			cspHash = EcmRequest.computeEcmHash(ecm);
		}
		EcmRequest answer = CardServer.getPendingCache().peekCache(cspHash);
		if(answer != null && !cache){
			answer.addGroups(session);
			return answer;
		}
		answer = new EcmRequest(session, cardId, new Provider(provider), shareId, serviceId, ecm, false);
		answer.setCspHash(cspHash);
		if(!cache){
			sendEcmToReader(answer);
			getPendingCache().addEntry(cspHash, answer);
		}
		return answer;
	}

	/**
	 * @param answer
	 */
	private static boolean sendEcmToReader(EcmRequest req) {
		if(readers.size() == 0)
			return false;
		List<CamSession> filteredReader = filterReaders(req);
		CamSession session = filteredReader.get(readerRoundRobin++ % filteredReader.size());
		if (session == null){
			logger.error("no reader found for ecm req: "+req);
			return false;
		}
		session.setLastRequest(req);
		session.getPacketSender().writeEcmRequest(req);
		return true;
	}

	/**
	 * @param req
	 * @return
	 */
	private static List<CamSession> filterReaders(EcmRequest req) {
		ArrayList<CamSession> filtered = new ArrayList<CamSession>();
		for(CamSession session : readers){
			for(int group : req.getGroups()){
				if(session.getGroups().contains(group)){
					filtered.add(session);
				}
			}
		}
		return filtered;
	}

	public static EcmRequest handleEcmRequest(CamSession session, int cardId, int provider, int shareId, int serviceId, byte[] ecm) {
		int cspHash = EcmRequest.computeEcmHash(ecm);
		EcmRequest answer = CardServer.getCache().peekCache(cspHash);
		if (answer != null) {
			session.setLastRequest(answer);
			return answer;
		}
		answer = createEcmRequest(session, cardId, provider, shareId, serviceId, ecm, cspHash, false);
		session.setLastRequest(answer);
		answer.addListener(session);
		return answer;
	}

}
