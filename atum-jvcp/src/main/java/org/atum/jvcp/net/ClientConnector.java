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

	private Logger logger = Logger.getLogger(ClientConnector.class);
	
	public Channel connect(String host, int port, ChannelInitializer<SocketChannel> clazz) {
		EventLoopGroup group = new NioEventLoopGroup();
		try {
			Bootstrap b = new Bootstrap();
			b.group(group).channel(NioSocketChannel.class).handler(clazz);

			try {
				logger.debug("connceting netty: "+host+" "+clazz.getClass().getName());
				Channel channel = b.connect(host, port).sync().channel();
				logger.debug("connceted netty: "+host+" "+clazz.getClass().getName()+" "+channel.isActive()+" "+channel.pipeline().last());
				
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
