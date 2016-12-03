package org.atum.jvcp.net;

import org.apache.log4j.Logger;
import org.atum.jvcp.CCcamServer;
import org.atum.jvcp.net.codec.cccam.CCcamLoginDecoder;
import org.atum.jvcp.net.codec.cccam.CCcamPipeline;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class NettyBootstrap {

	public static void listen(CCcamServer cCcamServer, int port) {

		Logger logger = Logger.getLogger(NettyBootstrap.class);

		EventLoopGroup loopGroup = new NioEventLoopGroup();

		ServerBootstrap bootstrap = new ServerBootstrap();

		bootstrap.group(loopGroup).channel(NioServerSocketChannel.class).childHandler(new CCcamPipeline(cCcamServer, CCcamLoginDecoder.class)).bind(port).syncUninterruptibly();

		logger.info("Server listening on port: " + port);
	}

}
