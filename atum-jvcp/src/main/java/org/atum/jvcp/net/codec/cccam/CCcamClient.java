package org.atum.jvcp.net.codec.cccam;

import org.atum.jvcp.CCcamServer;
import org.atum.jvcp.net.ClientConnector;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

public class CCcamClient extends CCcamSession {

	public CCcamClient(ChannelHandlerContext context, CCcamCipher encrypter, CCcamCipher decrypter) {
		super(context, encrypter, decrypter);
	}

	public static CCcamClient connect(CCcamServer server, String host,int port){
		ClientConnector conn = new ClientConnector();
		
		Channel channel = conn.connect(host, port, new CCcamPipeline(server, CCcamClientLoginDecoder.class));
		CCcamClient client = new CCcamClient(channel.pipeline().firstContext(),null,null);
		return client;
	}
}
