package org.atum.jvcp.net;

import org.apache.log4j.Logger;
import org.atum.jvcp.net.codec.cccam.io.CCcamClientLoginDecoder;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.ChannelHandler.Sharable;
/**
*
* @author Martin
*/
@Sharable
public class ChannelAcceptorHandler extends SimpleChannelInboundHandler<Object> {

	private Logger logger = Logger.getLogger(ChannelAcceptorHandler.class);
	
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Object obj) throws Exception {
		logger.debug("channelRead0 "+ctx.getClass().getName());
	}
	

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		logger.debug("channelInactive "+ctx.getClass().getName());
	}

}
