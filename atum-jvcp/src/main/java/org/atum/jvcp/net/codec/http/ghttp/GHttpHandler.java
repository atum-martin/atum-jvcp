/**
 * 
 */
package org.atum.jvcp.net.codec.http.ghttp;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.atum.jvcp.CardServer;
import org.atum.jvcp.account.Account;
import org.atum.jvcp.account.AccountStore;
import org.atum.jvcp.model.CamSession;
import org.atum.jvcp.model.EcmRequest;
import org.atum.jvcp.net.NetworkConstants;
import org.atum.jvcp.net.codec.NetUtils;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpUtil;

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

	public void handleRequest(ChannelHandlerContext ctx, FullHttpRequest req, FullHttpResponse response) {
		int strIndex = ordinalIndexOf(req.uri(), "/", 3)+1;
		
		if(strIndex == 0){
			//error usecase invalid url.
			return;
		}
		if(!verifyUserCredentials(ctx, req)){
			return;
		}
		CamSession session = new GHttpSession(ctx, response);
		
		
		String apiCall = req.uri().substring(0, strIndex);
		logger.debug("ghttp handler: "+req.uri()+" "+apiCall);
		//no str switch in java 1.7
		if (apiCall.equals(API_CACHE_GET)) {
			handleCachePull(session, req, response);
		} else if (apiCall.equals(API_ECM_POST)) {
			handleEcmPost(session, req, response);
		} else if (apiCall.equals(API_FEEDER_POST)) {

		} else if (apiCall.equals(API_CAPMT)) {
			handleCapmtRequest(req, response);
		}
	}
	
	/**
	 * @param session
	 * @param req
	 * @param response
	 */
	private void handleCachePull(CamSession session, FullHttpRequest req, FullHttpResponse response) {
		String[] parts = req.uri().split("/");
		int offset = (parts[3].length() >= 6) ? 4 : 3;
		@SuppressWarnings("unused")
		int ecm0 = Integer.parseInt(parts[offset++], 16) & 0xFF;
		int cspHash = (int)Long.parseLong(parts[offset++], 16);
		EcmRequest ecm = CardServer.getInstance().getCache().peekCache(cspHash);
		if(ecm == null){
			logger.debug("no ecm found in cache: "+cspHash);
			session.getPacketSender().writeFailedEcm();
			return;
		}
		session.getPacketSender().writeEcmAnswer(ecm.getDcw());
	}

	private boolean verifyUserCredentials(ChannelHandlerContext ctx, FullHttpRequest req){
		String authDecoded = null;
		try {
			
			String auth = req.headers().get("Authorization");
			if(auth == null || !auth.toLowerCase().startsWith("basic ")){
				return false;
			}
			auth = auth.substring(6);//remove basic from start of string
			authDecoded = new String(Base64.getDecoder().decode(auth));
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
			ctx.channel().attr(NetworkConstants.ACCOUNT).set(acc);
		} catch(IndexOutOfBoundsException e){
			e.printStackTrace();
			return false;
		}
		logger.debug("user verified: "+authDecoded);
		return true;
	}

	/**
	 * @param session 
	 * @param req
	 * @param response
	 */
	@SuppressWarnings("unused")
	private void handleEcmPost(CamSession session, FullHttpRequest req, FullHttpResponse response) {
		if(!req.method().equals(HttpMethod.POST)){
			//throw error.
			return;
		}
		String[] parts = req.uri().split("/");
		int offset = (parts[3].length() >= 6) ? 4 : 3;
		int networkId = Integer.parseInt(parts[offset++], 16);
		int tid = Integer.parseInt(parts[offset++], 16);
		int pid = Integer.parseInt(parts[offset++], 16);
		int serviceId = Integer.parseInt(parts[offset++], 16);
		int cardId = Integer.parseInt(parts[offset++], 16);
		int provider = 0;
		if (parts.length > offset)
			provider = Integer.parseInt(parts[offset++], 16);
		
		int contentLength = (int) HttpUtil.getContentLength(req);
		byte[] ecm = new byte[contentLength];
		req.content().readBytes(ecm);
		logger.debug("ecm hex dump: "+NetUtils.bytesToString(ecm,0,ecm.length));
		CardServer.getInstance().handleClientEcmRequest(session, cardId, provider, 0, serviceId, ecm);
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

	@SuppressWarnings("unused")
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

	/**
	 * @return
	 */
	public static Map<String,GHttpHandler> getUrlMappings() {
		HashMap<String,GHttpHandler> urlMap = new HashMap<String,GHttpHandler>();
		GHttpHandler handler = new GHttpHandler();
		urlMap.put(API_CACHE_GET, handler);
		urlMap.put(API_ECM_POST, handler);
		urlMap.put(API_FEEDER_POST, handler);
		urlMap.put(API_CAPMT, handler);
		return urlMap;
	}
}
