package org.atum.jvcp.net;

import org.apache.log4j.Logger;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * @author <a href="https://github.com/atum-martin">atum-martin</a>
 * @since 21 Nov 2016 22:16:25
 */

public class NettyBootstrap {

	public static void listenTcp(ChannelInitializer<SocketChannel> pipeline, int port) {
		listen(pipeline, port, NioServerSocketChannel.class, new ServerBootstrap());
	}
	
	public static void listenUdp(ChannelInitializer<SocketChannel> pipeline, int port) {
		listenUdp(pipeline, port, new Bootstrap());
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void listen(ChannelInitializer<SocketChannel> pipeline, int port, Class clazz, ServerBootstrap bootstrap) {
		//port = 8080;
 
		Logger logger = Logger.getLogger(NettyBootstrap.class);

		EventLoopGroup loopGroup = new NioEventLoopGroup();	

		bootstrap.group(loopGroup).channel(clazz).childHandler(pipeline).bind(port).syncUninterruptibly();

		logger.info("TCP Server listening on port: " + port);
		
	}
	
	public static void listenUdp(ChannelInitializer<SocketChannel> pipeline, int port,Bootstrap bootstrap) {
		//port = 8080;
 
		Logger logger = Logger.getLogger(NettyBootstrap.class);

		EventLoopGroup loopGroup = new NioEventLoopGroup();	

		bootstrap.group(loopGroup).channel(NioDatagramChannel.class).handler(pipeline).bind(port).syncUninterruptibly();

		logger.info("UDP Server listening on port: " + port);
		
	}

}
