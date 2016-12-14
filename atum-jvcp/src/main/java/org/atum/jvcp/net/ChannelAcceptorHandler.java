package org.atum.jvcp.net;

import org.apache.log4j.Logger;


import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.ChannelHandler.Sharable;

/**
 * @author <a href="https://github.com/atum-martin">atum-martin</a>
 * @since 21 Nov 2016 22:16:25
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
