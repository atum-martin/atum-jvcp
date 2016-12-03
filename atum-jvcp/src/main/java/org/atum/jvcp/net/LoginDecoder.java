package org.atum.jvcp.net;

import org.atum.jvcp.CCcamServer;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

public abstract class LoginDecoder extends ByteToMessageDecoder {

	protected CCcamServer cCcamServer;
	public abstract void init(ChannelHandlerContext firstContext);
	
	public LoginDecoder(CCcamServer cCcamServer) {
		this.cCcamServer = cCcamServer;
	}

}
