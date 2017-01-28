package org.atum.jvcp.net.codec.newcamd;

import org.atum.jvcp.NewcamdServer;
import org.atum.jvcp.account.Account;
import org.atum.jvcp.model.CamClient;
import org.atum.jvcp.net.ClientConnector;
import org.atum.jvcp.net.NetworkConstants;
import org.atum.jvcp.net.codec.newcamd.io.NewcamdClientLoginDecoder;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author <a href="https://github.com/atum-martin">atum-martin</a>
 * @since 3 Dec 2016 14:57:43
 */

public class NewcamdClient extends NewcamdSession implements CamClient {

	private String host;
	private int port;
	private Account account;
	NewcamdServer server;
	
	public NewcamdClient(String host, int port, Account account, ChannelHandlerContext context,NewcamdServer server, byte[] desKey) {
		super(server, context, desKey);
		this.host = host;
		this.port = port;
		this.account = account;
		this.server = server;
	}

	public static NewcamdClient connect(NewcamdServer server, String host,int port, Account account, byte[] desKey){
		ChannelHandlerContext ctx = null;
		NewcamdClient client = new NewcamdClient(host, port, account, ctx, server, desKey);
		client.setReader(true);
		client.connect();
		return client;
	}
	
	public String toString(){
		return getAccount()+"@"+getCtx().channel().remoteAddress();
	}

	public void connect() {
		Channel channel = ClientConnector.connect(host, port, account, new NewcamdPipeline(server, NewcamdClientLoginDecoder.class));
		if(channel != null){
			this.setCtx(channel.pipeline().firstContext());
			channel.attr(NetworkConstants.CAM_SESSION).set(this);
			server.registerSession(this);
		}
	}

}
