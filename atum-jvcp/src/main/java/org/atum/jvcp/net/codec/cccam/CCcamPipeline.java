package org.atum.jvcp.net.codec.cccam;

import org.atum.jvcp.CCcamServer;
import org.atum.jvcp.net.ChannelAcceptorHandler;
import org.atum.jvcp.net.ChannelFilter;
import org.atum.jvcp.net.LoginDecoder;
import org.atum.jvcp.net.NetworkConstants;
import org.atum.jvcp.net.codec.LoginState;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

public class CCcamPipeline extends ChannelInitializer<SocketChannel> {

	private Class<? extends LoginDecoder> decoder;
	private CCcamServer cCcamServer;
	private static final ChannelAcceptorHandler ACCECPTOR_HANDLER = new ChannelAcceptorHandler();
	private static final ChannelFilter filter = new ChannelFilter();
	
	public CCcamPipeline(CCcamServer cCcamServer, Class<? extends LoginDecoder> decoder) {
		this.decoder = decoder;
		this.cCcamServer = cCcamServer;
	}

	@Override
	protected void initChannel(SocketChannel channel) throws Exception {
		final ChannelPipeline pipeline = channel.pipeline();

		channel.attr(NetworkConstants.LOGIN_STATE).set(LoginState.SHA);

		pipeline.addLast("filter", filter);
		pipeline.addLast("timeout", new IdleStateHandler(10000, 0, 0));
		LoginDecoder instance = decoder.getConstructor(CCcamServer.class).newInstance(cCcamServer);
		pipeline.addLast("login-header-decoder", instance);

		pipeline.addLast("channel-handler", ACCECPTOR_HANDLER);
		
		instance.init(pipeline.firstContext());
	}

}
