/**
 * 
 */
package org.atum.jvcp.net.codec.http;

import org.apache.log4j.Logger;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpRequest;

/**
 * @author <a href="https://github.com/atum-martin">atum-martin</a>
 * @since 16 Dec 2016 00:51:35
 */
public class HttpServerHandler extends SimpleChannelInboundHandler<HttpRequest> implements ChannelHandler {

	private Logger logger = Logger.getLogger(HttpServerHandler.class);
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, HttpRequest msg) throws Exception {
		//parse response for ghttp headers and respond.
		logger.info("http request: "+msg+" "+msg.uri());
	}


}
