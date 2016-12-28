/**
 * 
 */
package org.atum.jvcp.net.codec.http;

import org.apache.log4j.Logger;
import org.atum.jvcp.CardServer;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;

import static io.netty.handler.codec.http.HttpHeaders.Names.*;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.*;

/**
 * @author <a href="https://github.com/atum-martin">atum-martin</a>
 * @since 16 Dec 2016 00:51:35
 */
public class HttpServerHandler extends SimpleChannelInboundHandler<HttpRequest> implements ChannelHandler {

	private Logger logger = Logger.getLogger(HttpServerHandler.class);

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, HttpRequest msg) throws Exception {
		// parse response for ghttp headers and respond.
		//logger.info("http request: " + msg + " " + msg.uri());
		String CONTENT = "<html><body><table><tr><td>"+CardServer.getCache().size()+"</td><td>"+CardServer.getPendingCache().size()+"</td></tr></table></body></html>";
		FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.wrappedBuffer(CONTENT.getBytes()));
		response.headers().set(CONTENT_TYPE, "text/html");
		response.headers().set(CONTENT_LENGTH, response.content().readableBytes());

		// Write the initial line and the header.
		ctx.writeAndFlush(response);
	}

}
