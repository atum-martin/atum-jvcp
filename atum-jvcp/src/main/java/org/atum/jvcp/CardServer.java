/**
 * 
 */
package org.atum.jvcp;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.atum.jvcp.account.AccountStore;
import org.atum.jvcp.cache.ClusteredCache;
import org.atum.jvcp.config.ChannelList;
import org.atum.jvcp.config.ReaderConfig;
import org.atum.jvcp.model.CamClient;
import org.atum.jvcp.model.CamSession;
import org.atum.jvcp.model.CardProfile;
import org.atum.jvcp.model.EcmRequest;
import org.atum.jvcp.net.NettyBootstrap;
import org.atum.jvcp.net.codec.http.HttpPipeline;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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
	private static Vector<CamClient> disconnectedReaders = new Vector<CamClient>();
	private static int readerRoundRobin = 0;

	private static Map<Integer,CardProfile> profiles;

	private static final boolean DCW_CHECKING = true;

	/**
	 * Main method used to initialize a blank cccam server with clustered cache.
	 * 
	 * @param args
	 *            Not used
	 */
	@SuppressWarnings("unused")
	public static void main(String[] args) {
		BasicConfigurator.configure();
		Logger.getRootLogger().setLevel(Level.INFO);

		ChannelList.getSingleton();
		AccountStore.getSingleton();
		CardServer server = new CardServer();
		profiles = server.loadProfiles();
		CCcamServer server1 = new CCcamServer("cccam-server1", 12000);
		NewcamdServer server2 = new NewcamdServer("newcamd-server1", 12001);
		NettyBootstrap.listenTcp(new HttpPipeline(), 8080);
		ReaderConfig config = new ReaderConfig(new CamServer[] { server1, server2, });
		addAllReaders(server1, readers);
		addAllReaders(server2, readers);
		spawnReaderMonitorThread();
		while(true){
			try {
				fireCacheWaitTimeout();
			} catch (Exception e){
				e.printStackTrace();
			}
			try {
				Thread.sleep(5);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private static void spawnReaderMonitorThread() {
		new Thread(){
			public void run(){
				List<CamClient> cleanup = new LinkedList<CamClient>();
				while(true){
					long time = System.currentTimeMillis();
					
					for(CamClient client : disconnectedReaders){
						if(time - client.getLastDisconnect() > 500){
							client.connect();
							if(client.getLastDisconnect() == 0){
								cleanup.add(client);
							}
						}
					}
					for(CamClient client : cleanup){
						disconnectedReaders.remove(client);
					}
					disconnectedReaders.clear();
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}.start();	
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
		// long ecmHash = EcmRequest.computeEcmHash(cspHash);
		// logger.info("ECM cache entry for: "+cspHash);
		EcmRequest req = CardServer.getPendingCache().peekCache(cspHash);
		if (req != null) {
			req.setDcw(cw);
			getCache().addEntry(cspHash, req);
			getPendingCache().removeRequest(cspHash);
			logger.info("ecm answer for " + ChannelList.getChannelName(req.getCardId(), req.getServiceId()) + " by " + session + " for " + req.getSessionsStr()
					+ " sessions.");

			req.fireActionListeners();
			// logger.info("Cache push hit on: "+Integer.toHexString(cardId)+":"+Integer.toHexString(serviceId));
			// logger.info("cache hit for pending dcw dump: "+NetUtils.bytesToString(cw,0,cw.length));
			return true;
		} else {
			// EcmRequest either exists in cache or no pending requests have
			// come in.
			// We want to check does the DCW differ from the cache entry.
			req = CardServer.getCache().peekCache(cspHash);
		}

		if (req != null) {
			// this should never fail
			if (DCW_CHECKING && req.hasAnswer()) {
				if (!Arrays.equals(cw, req.getDcw())) {
					// logger.warn("Duplicate ECM with different DCW. "+cspHash+" "+sum(cw));
				}
			}
			return true;
		}
		return false;
	}

	public static void fireCacheWaitTimeout() {
		long now = System.currentTimeMillis();
		synchronized (pendingEcms) {
			for (Object o : pendingEcms.entrySet()) {
				@SuppressWarnings("unchecked")
				Entry<Long, EcmRequest> e = (Entry<Long, EcmRequest>) o;
				EcmRequest req = e.getValue();
				
				if(req.wasSentToReaders()){
					continue;
				}
				int cacheWait = req.getProfile().getCacheWaitTime();
				long timeDiff = now - req.getTimestamp();
				if (timeDiff < cacheWait) {
					// map is ordered no entry after this point will have
					// expired
					break;
				}
				logger.info("cache wait timeout: "+timeDiff+"ms "+req);
				if (!sendEcmToReader(req)) {
					logger.error("no reader found for ecm req: " + req);
				}
			}
		}
	}
	
	public Map<Integer,CardProfile> loadProfiles(){
		Gson gson = new GsonBuilder().create();
		InputStream is = this.getClass().getClassLoader().getResourceAsStream("profiles.json");
		CardProfile[] profiles = gson.fromJson(new InputStreamReader(is), CardProfile[].class);
		HashMap<Integer,CardProfile> profileMap = new HashMap<Integer,CardProfile>();
		for(CardProfile profile : profiles){
			if(profile != null){
				int cardId = Integer.parseInt(profile.getCardId()+"", 16);
				profile.setCardId(cardId);
				profileMap.put(cardId, profile);
			}
		}
		return profileMap;
	}

	public static EcmRequest createEcmRequest(CamSession session, int cardId, int provider, int shareId, int serviceId, byte[] ecm, int cspHash, boolean cache) {
		if (cspHash == 0L) {
			// no hash found compute it.
			cspHash = EcmRequest.computeEcmHash(ecm);
		}
		EcmRequest request = CardServer.getPendingCache().peekCache(cspHash);
		if (request != null && !cache) {
			request.addGroups(session);
			return request;
		}
		request = new EcmRequest(session, cardId, provider, shareId, serviceId, ecm, false);
		request.setCspHash(cspHash);
		if (!cache) {
			getPendingCache().addEntry(cspHash, request);
			if(request.getProfile().getCacheWaitTime() <= 0)
				sendEcmToReader(request);
		} else {
			request.setSentToReaders();
		}
		return request;
	}

	/**
	 * @param answer
	 */
	private static boolean sendEcmToReader(EcmRequest req) {
		if (readers.size() == 0)
			return false;
		List<CamSession> filteredReader = filterReaders(req);
		if (filteredReader.size() == 0)
			return false;
		req.setSentToReaders();
		CamSession session = filteredReader.get(readerRoundRobin++ % filteredReader.size());
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
		for (CamSession session : readers) {
			// Remove readers that do not support that cardId.
			if (!session.hasCard(req.getCardId())) {
				continue;
			}
			for (int group : req.getGroups()) {
				logger.debug("filterReaders: attempting match: " + group + " " + listToStr(session.getGroups()));
				if (session.getGroups().contains(group)) {
					logger.debug("filterReaders: found match: " + group);
					filtered.add(session);
				}
			}
		}
		return filtered;
	}

	/**
	 * @param groups
	 * @return
	 */
	private static String listToStr(ArrayList<Integer> groups) {
		StringBuilder build = new StringBuilder("[");
		for (int group : groups) {
			build.append(group);
			build.append(", ");
		}
		build.append("]");
		return build.toString();
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

	/**
	 * @param session
	 * @param cardId
	 * @param provider
	 * @param i
	 * @param serviceId
	 * @param ecm
	 */
	public static void handleClientEcmRequest(CamSession session, int cardId, int provider, int shareId, int serviceId, byte[] ecm) {
		EcmRequest answer = handleEcmRequest(session, cardId, provider, 0, serviceId, ecm);
		if (answer != null && answer.hasAnswer()) {
			logger.info("cache hit for " + session + " " + ChannelList.getChannelName(cardId, serviceId));
			session.getPacketSender().writeEcmAnswer(answer.getDcw());
		}
	}

	public static Map<Integer,CardProfile> getProfiles() {
		return profiles;
	}

	public static void registerReaderDisconnect(CamClient client) {
		if(!disconnectedReaders.contains(client)){
			disconnectedReaders.add(client);
		}
	}

}
