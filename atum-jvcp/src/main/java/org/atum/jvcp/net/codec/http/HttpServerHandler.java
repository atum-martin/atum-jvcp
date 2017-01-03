/**
 * 
 */
package org.atum.jvcp.net.codec.http;

import org.apache.log4j.Logger;
import org.atum.jvcp.html.HtmlResource;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;

import static io.netty.handler.codec.http.HttpHeaderNames.*;
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
		//String CONTENT = "<html><body><table><tr><td>"+CardServer.getCache().size()+"</td><td>"+CardServer.getPendingCache().size()+"</td></tr></table></body></html>";
		
		//response.headers().set(CONTENT_TYPE, "text/html");
		//response.headers().set(CONTENT_LENGTH, response.content().readableBytes());
		byte[] content = null;
		if(msg.uri().endsWith("html") || msg.uri().endsWith("/")){
			//String contentStr = header.getContent()+nav.getContent()+footer.getContent();
			//content = contentStr.getBytes();
			content = new byte[header.getContent().length+nav.getContent().length+footer.getContent().length];
			System.arraycopy(header.getContent(), 0, content, 0, header.getContent().length);
			System.arraycopy(nav.getContent(), 0, content, header.getContent().length, nav.getContent().length);
			System.arraycopy(footer.getContent(), 0, content, header.getContent().length+nav.getContent().length, footer.getContent().length);
		} else {
			String uri = msg.uri();
			if(uri.contains("?")){
				uri = uri.substring(0, uri.indexOf("?"));
			}
			content = (new HtmlResource("web"+uri)).getContent();
		}
		FullHttpResponse response = null;
		if(content == null){
			response = new DefaultFullHttpResponse(HTTP_1_1, NOT_FOUND);
		} else {
			response = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.wrappedBuffer(content));
		}
		response.headers().set(CONTENT_LENGTH, response.content().readableBytes());
		// Write the initial line and the header.
		ctx.writeAndFlush(response);
	}
	
	private static HtmlResource header = new HtmlResource("web/header.html");
	private static HtmlResource footer = new HtmlResource("web/footer.html");
	private static HtmlResource nav = new HtmlResource("web/navbar.html");

}
