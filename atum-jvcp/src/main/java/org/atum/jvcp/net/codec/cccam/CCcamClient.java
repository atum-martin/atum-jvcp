package org.atum.jvcp.net.codec.cccam;

import org.atum.jvcp.CCcamServer;
import org.atum.jvcp.account.Account;
import org.atum.jvcp.net.ClientConnector;
import org.atum.jvcp.net.NetworkConstants;
import org.atum.jvcp.net.codec.cccam.io.CCcamClientLoginDecoder;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author <a href="https://github.com/atum-martin">atum-martin</a>
 * @since 3 Dec 2016 14:57:43
 */

public class CCcamClient extends CCcamSession {

	public CCcamClient(ChannelHandlerContext context,CCcamServer server, CCcamCipher encrypter, CCcamCipher decrypter) {
		super(context, server, encrypter, decrypter);
	}

	public static CCcamClient connect(CCcamServer server, String host,int port, Account account){
		ClientConnector conn = new ClientConnector();
		
		Channel channel = conn.connect(host, port, account, new CCcamPipeline(server, CCcamClientLoginDecoder.class));
		CCcamClient client = new CCcamClient(channel.pipeline().firstContext(),server, new CCcamCipher(),new CCcamCipher());
		channel.attr(NetworkConstants.CAM_SESSION).set(client);
		client.setReader(true);
		server.registerSession(client);
		return client;
	}

}
