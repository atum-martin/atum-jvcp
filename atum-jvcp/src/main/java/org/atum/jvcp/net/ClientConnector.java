package org.atum.jvcp.net;

import org.apache.log4j.Logger;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class ClientConnector {

	@SuppressWarnings("unused")
	private Logger logger = Logger.getLogger(ClientConnector.class);
	
	public Channel connect(String host, int port, ChannelInitializer<SocketChannel> pipeline) {
		EventLoopGroup group = new NioEventLoopGroup();
		try {
			Bootstrap b = new Bootstrap();
			b.group(group).channel(NioSocketChannel.class).handler(pipeline);

			try {
				Channel channel = b.connect(host, port).sync().channel();
				return channel;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return null;
		} catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
}
