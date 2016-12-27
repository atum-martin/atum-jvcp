package org.atum.jvcp.net.codec.cccam;

import io.netty.channel.ChannelHandlerContext;

import org.apache.log4j.Logger;
import org.atum.jvcp.CCcamServer;
import org.atum.jvcp.model.CamSession;
import org.atum.jvcp.model.EcmRequest;

/**
 * @author <a href="https://github.com/atum-martin">atum-martin</a>
 * @since 21 Nov 2016 22:16:25
 */

public class CCcamSession extends CamSession {

	@SuppressWarnings("unused")
	private Logger logger = Logger.getLogger(CCcamSession.class);
	
	private CCcamCipher encrypter;
	private CCcamCipher decrypter;
	private CCcamServer server;

	private String username;

	private long lastPing = System.currentTimeMillis();

	
	private EcmRequest lastRequest = null;
	
	public CCcamSession(ChannelHandlerContext context, CCcamServer server, CCcamCipher encrypter, CCcamCipher decrypter) {
		super(context);
		this.server = server;
		this.encrypter = encrypter;
		this.decrypter = decrypter;
	}

	public CCcamCipher getDecrypter() {
		return decrypter;
	}
	
	public CCcamCipher getEncrypter() {
		return encrypter;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getUsername() {
		return username;
	}
	
	public long getLastKeepalive(){
		return System.currentTimeMillis() - lastPing ;
	}
	
	public void keepAlive(){
		getPacketSender().writeKeepAlive();
		lastPing = System.currentTimeMillis();
	}

	public void setLastKeepAlive(long currentTimeMillis) {
		lastPing = currentTimeMillis;
	}

	public EcmRequest getLastRequest() {
		return lastRequest;
	}

	public void setLastRequest(EcmRequest lastRequest) {
		this.lastRequest = lastRequest;
	}

	public CCcamServer getServer() {
		return server;
	}
}
