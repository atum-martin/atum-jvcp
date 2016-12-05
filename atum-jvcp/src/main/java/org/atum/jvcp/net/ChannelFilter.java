package org.atum.jvcp.net;

import org.apache.log4j.Logger;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelHandler.Sharable;

@Sharable
public class ChannelFilter extends ChannelInboundHandlerAdapter {

	private Logger logger = Logger.getLogger(ChannelAcceptorHandler.class);

	@Override
	public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
		logger.debug("channelRegistered " + ctx.getClass().getName());
	}

	@Override
	public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
		logger.debug("channelUnregistered " + ctx.getClass().getName());
	}
}
