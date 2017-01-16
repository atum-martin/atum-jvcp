/**
 * 
 */
package org.atum.jvcp.net.codec.http;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.ssl.SslContext;

/**
 * @author <a href="https://github.com/atum-martin">atum-martin</a>
 * @since 16 Dec 2016 00:49:04
 */
public class HttpPipeline extends ChannelInitializer<SocketChannel> {

	private final SslContext sslCtx = null;

	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		ChannelPipeline p = ch.pipeline();
		if (sslCtx != null) {
			p.addLast(sslCtx.newHandler(ch.alloc()));
		}
		p.addLast(new HttpRequestDecoder());
		p.addLast(new HttpResponseEncoder());
		p.addLast("aggregator", new HttpObjectAggregator(1048576));
		p.addLast(new HttpServerHandler());
	}

}
