package org.atum.jvcp.net;

import org.apache.log4j.Logger;
import org.atum.jvcp.account.Account;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * @author <a href="https://github.com/atum-martin">atum-martin</a>
 * @since 3 Dec 2016 14:57:43
 */

public class ClientConnector {

	@SuppressWarnings("unused")
	private Logger logger = Logger.getLogger(ClientConnector.class);
	
	public Channel connect(String host, int port, Account account, ChannelInitializer<SocketChannel> pipeline) {
		EventLoopGroup group = new NioEventLoopGroup();
		try {
			Bootstrap b = new Bootstrap();
			b.group(group).channel(NioSocketChannel.class).handler(pipeline);

			try {
				Channel channel = b.connect(host, port).sync().channel();
				if(account != null){
					channel.attr(NetworkConstants.ACCOUNT).set(account);
				}
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
