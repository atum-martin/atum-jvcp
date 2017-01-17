/**
 * 
 */
package org.atum.jvcp.net.codec.http;

import java.util.Base64;

import org.apache.log4j.Logger;
import org.atum.jvcp.CardServer;
import org.atum.jvcp.account.Account;
import org.atum.jvcp.account.AccountStore;
import org.atum.jvcp.model.CamSession;
import org.atum.jvcp.model.EcmRequest;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponse;

/**
 * @author <a href="https://github.com/atum-martin">atum-martin</a>
 * @since 13 Jan 2017 23:18:27
 */
public class GHttpHandler {

	protected static final String API_PREFIX = "/api";

	protected static final String API_CACHE_GET = API_PREFIX + "/c/";
	protected static final String API_ECM_POST = API_PREFIX + "/e/";
	protected static final String API_FEEDER_POST = API_PREFIX + "/f/";
	protected static final String API_CAPMT = API_PREFIX + "/p/";
	
	private Logger logger = Logger.getLogger(GHttpHandler.class);

	public void handleRequest(FullHttpRequest req, HttpResponse response) {
		int strIndex = ordinalIndexOf(req.uri(), "/", 3);
		if(strIndex == -1){
			//error usecase invalid url.
			return;
		}
		if(!verifyUserCredentials(req)){
			return;
		}
		String apiCall = req.uri().substring(0, strIndex);
		//no str switch in java 1.7
		if (apiCall.equals(API_CACHE_GET)) {

		} else if (apiCall.equals(API_ECM_POST)) {
			handleEcmPost(req, response);
		} else if (apiCall.equals(API_FEEDER_POST)) {

		} else if (apiCall.equals(API_CAPMT)) {
			handleCapmtRequest(req, response);
		}
	}
	
	private boolean verifyUserCredentials(FullHttpRequest req){
		try {
			
			String auth = req.headers().get("Authorization");
			if(auth == null || !auth.toLowerCase().startsWith("basic ")){
				return false;
			}
			auth = auth.substring(6);//remove basic from start of string
			String authDecoded = new String(Base64.getDecoder().decode(auth));
			int colon = authDecoded.indexOf(":");// format is user:password
			if(colon == -1){
				return false;
			}
			String user = authDecoded.substring(0, colon);
			String pass = authDecoded.substring(colon+1);
			Account acc = AccountStore.getSingleton().getAccount(user);
			if(!acc.getPassword().equals(pass)){
				return false;
			}
		} catch(IndexOutOfBoundsException e){
			e.printStackTrace();
		}
		return true;
	}

	/**
	 * @param req
	 * @param response
	 */
	private void handleEcmPost(FullHttpRequest req, HttpResponse response) {
		if(!req.method().equals(HttpMethod.POST)){
			//throw error.
			return;
		}
		String[] parts = req.uri().split("/");
		
		CamSession session = null;
		int offset = (parts[3].length() >= 6) ? 4 : 3;
		int networkId = Integer.parseInt(parts[offset++], 16);
		int tid = Integer.parseInt(parts[offset++], 16);
		int pid = Integer.parseInt(parts[offset++], 16);
		int serviceId = Integer.parseInt(parts[offset++], 16);
		int cardId = Integer.parseInt(parts[offset++], 16);
		int provider = 0;
		if (parts.length > offset)
			provider = Integer.parseInt(parts[offset++], 16);
		
		byte[] ecm = req.content().array();
		
		EcmRequest answer = CardServer.handleEcmRequest(session, cardId, provider, 0, serviceId, ecm);
		if(answer != null && answer.hasAnswer()){
			logger.info("handled client ECM: "+answer.getCspHash());
			session.getPacketSender().writeEcmAnswer(answer.getDcw());
		}
	}

	/**
	 * Taken from apache commons library. Used to determine the Nth index of a ordinal string.
	 * 
	 * @param str the string that will be searched for term substr
	 * @param substr the pattern used to match on.
	 * @param nth term you wish to obtain the index of.
	 * @return
	 */
	public static int ordinalIndexOf(String str, String substr, int n) {
		int pos = str.indexOf(substr);
		while (--n > 0 && pos != -1)
			pos = str.indexOf(substr, pos + 1);
		return pos;
	}

	public void handleCapmtRequest(FullHttpRequest req, HttpResponse response) {
		String[] parts = req.uri().split("/");
		int offset = (parts[3].length() >= 6) ? 4 : 3;
		int networkId = Integer.parseInt(parts[offset++], 16);
		int tsId = Integer.parseInt(parts[offset++], 16);
		int serviceId = Integer.parseInt(parts[offset++], 16);
		int pidCount = Integer.parseInt(parts[offset++], 16);
		long namespace = 0L;
		if (parts.length > offset)
			namespace = Long.parseLong(parts[offset], 16);

	}
}
