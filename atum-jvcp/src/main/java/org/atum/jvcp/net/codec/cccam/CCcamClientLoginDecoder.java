package org.atum.jvcp.net.codec.cccam;

import java.util.List;

import org.atum.jvcp.CCcamServer;
import org.atum.jvcp.net.LoginDecoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class CCcamClientLoginDecoder extends LoginDecoder {

	public CCcamClientLoginDecoder(CCcamServer server) {
		super(server);
	}

	@Override
	public void init(ChannelHandlerContext firstContext) {

	}

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		
	}

}
