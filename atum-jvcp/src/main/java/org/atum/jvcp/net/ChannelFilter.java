package org.atum.jvcp.net;

import org.apache.log4j.Logger;
import org.atum.jvcp.model.CamSession;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelHandler.Sharable;

import static org.atum.jvcp.net.NetworkConstants.*;

/**
 * @author <a href="https://github.com/atum-martin">atum-martin</a>
 * @since 25 Nov 2016 23:49:09
 */

@Sharable
public class ChannelFilter extends ChannelInboundHandlerAdapter {

	private Logger logger = Logger.getLogger(ChannelAcceptorHandler.class);

	@Override
	public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
		logger.info("channelRegistered " + ctx.getClass().getName());
		
	}

	@Override
	public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
		logger.info("channelUnregistered " + ctx.getClass().getName());
		CamSession session = ctx.channel().attr(CAM_SESSION).get();
		session.unregister();
	}
}
