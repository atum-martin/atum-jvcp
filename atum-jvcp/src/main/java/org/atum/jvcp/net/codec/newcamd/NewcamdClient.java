package org.atum.jvcp.net.codec.newcamd;

import org.atum.jvcp.NewcamdServer;
import org.atum.jvcp.account.Account;
import org.atum.jvcp.net.ClientConnector;
import org.atum.jvcp.net.NetworkConstants;
import org.atum.jvcp.net.codec.newcamd.io.NewcamdClientLoginDecoder;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author <a href="https://github.com/atum-martin">atum-martin</a>
 * @since 3 Dec 2016 14:57:43
 */

public class NewcamdClient extends NewcamdSession {

	public NewcamdClient(ChannelHandlerContext context,NewcamdServer server, byte[] desKey) {
		super(server, context, desKey);
	}

	public static NewcamdClient connect(NewcamdServer server, String host,int port, Account account, byte[] desKey){
		ClientConnector conn = new ClientConnector();
		
		Channel channel = conn.connect(host, port, account, new NewcamdPipeline(server, NewcamdClientLoginDecoder.class));
		NewcamdClient client = new NewcamdClient(channel.pipeline().firstContext(), server, desKey);
		channel.attr(NetworkConstants.CAM_SESSION).set(client);
		client.setReader(true);
		server.registerSession(client);
		return client;
	}
	
	public String toString(){
		return getAccount()+"@"+getCtx().channel().remoteAddress();
	}

}
