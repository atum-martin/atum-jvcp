package org.atum.jvcp.net;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class ClientConnector {

	public Channel connect(String host, int port, ChannelInitializer<SocketChannel> clazz) {
		EventLoopGroup group = new NioEventLoopGroup();
		try {
			Bootstrap b = new Bootstrap();
			b.group(group).channel(NioSocketChannel.class).handler(clazz);

			try {
				Channel channel = b.connect(host, port).sync().channel();
				return channel;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return null;
		} finally {
			group.shutdownGracefully();
		}
	}
}
