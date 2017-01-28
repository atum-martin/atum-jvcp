package org.atum.jvcp.net.codec.cccam;

import org.apache.log4j.Logger;
import org.atum.jvcp.CCcamServer;
import org.atum.jvcp.CardServer;
import org.atum.jvcp.account.Account;
import org.atum.jvcp.model.CamClient;
import org.atum.jvcp.net.ClientConnector;
import org.atum.jvcp.net.NetworkConstants;
import org.atum.jvcp.net.codec.cccam.io.CCcamClientLoginDecoder;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author <a href="https://github.com/atum-martin">atum-martin</a>
 * @since 3 Dec 2016 14:57:43
 */

public class CCcamClient extends CCcamSession implements CamClient {

	private String host;
	private int port;
	private Account account;
	CCcamServer server;
	private long disconnectTime = 0;
	private Logger logger = Logger.getLogger(CCcamClient.class);
	
	public CCcamClient(CCcamServer server, String host, int port, Account account, ChannelHandlerContext context, CCcamCipher encrypter, CCcamCipher decrypter) {
		super(context, server, encrypter, decrypter);
		this.host = host;
		this.port = port;
		this.server = server;
		this.account = account;
	}

	public static CCcamClient connect(CCcamServer server, String host,int port, Account account){
		ChannelHandlerContext ctx = null;
		CCcamClient client = new CCcamClient(server, host, port, account, ctx, new CCcamCipher(),new CCcamCipher());
		client.setReader(true);
		client.connect();
		if(client.getCtx() == null){
			CardServer.registerReaderDisconnect(client);
		}
		return client;
	}

	public void connect() {
		logger.info("Reconnecting CCcam reader: "+host+":"+port+" "+account);
		Channel channel = ClientConnector.connect(host, port, account, new CCcamPipeline(server, CCcamClientLoginDecoder.class));
		if(channel != null){
			this.setCtx(channel.pipeline().firstContext());
			disconnectTime = 0;
			channel.attr(NetworkConstants.CAM_SESSION).set(this);
			server.registerSession(this);
		} else {
			disconnect();
		}
	}
	
	public void disconnect() {
		disconnectTime  = System.currentTimeMillis();
	}

	public long getLastDisconnect() {
		return disconnectTime;
	}

}
