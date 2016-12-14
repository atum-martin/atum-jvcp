package org.atum.jvcp.net.codec.cccam;

import org.atum.jvcp.CCcamServer;
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

	public CCcamClient(ChannelHandlerContext context, CCcamCipher encrypter, CCcamCipher decrypter) {
		super(context, encrypter, decrypter);
	}

	public static CCcamClient connect(CCcamServer server, String host,int port){
		ClientConnector conn = new ClientConnector();
		
		Channel channel = conn.connect(host, port, new CCcamPipeline(server, CCcamClientLoginDecoder.class));
		CCcamClient client = new CCcamClient(channel.pipeline().firstContext(),new CCcamCipher(),new CCcamCipher());
		channel.attr(NetworkConstants.CAM_SESSION).set(client);
		return client;
	}

}
