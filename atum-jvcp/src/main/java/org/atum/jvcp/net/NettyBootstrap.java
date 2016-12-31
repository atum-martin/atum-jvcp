package org.atum.jvcp.net;

import org.apache.log4j.Logger;

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
		listen(pipeline, port, NioServerSocketChannel.class);
	}
	
	public static void listenUdp(ChannelInitializer<SocketChannel> pipeline, int port) {
		listen(pipeline, port, NioDatagramChannel.class);
	}
	
	public static void listen(ChannelInitializer<SocketChannel> pipeline, int port, Class clazz) {
		//port = 8080;
 
		Logger logger = Logger.getLogger(NettyBootstrap.class);

		EventLoopGroup loopGroup = new NioEventLoopGroup();

		ServerBootstrap bootstrap = new ServerBootstrap();

		bootstrap.group(loopGroup).channel(clazz).childHandler(pipeline).bind(port).syncUninterruptibly();

		logger.info("Server listening on port: " + port);
		
		//@SuppressWarnings("unused")
		//CCcamClient client = CCcamClient.connect(cCcamServer, "127.0.0.1", 12000);
		
	}

}
