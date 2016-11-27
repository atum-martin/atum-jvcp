package org.atum.jvcp.net;

import org.atum.jvcp.CCcamServer;
import org.atum.jvcp.net.codec.LoginState;
import org.atum.jvcp.net.codec.cccam.CCcamLoginDecoder;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

/**
 *
 * @author Martin
 */
@Sharable
public class PipelineInitializer extends ChannelInitializer<SocketChannel> {


	private static final ChannelAcceptorHandler ACCECPTOR_HANDLER = new ChannelAcceptorHandler();
	private static final ChannelFilter filter = new ChannelFilter();
	private CCcamServer cCcamServer;
	
	public PipelineInitializer(CCcamServer cCcamServer){
		this.cCcamServer = cCcamServer;
	}

	@Override
	protected void initChannel(SocketChannel channel) throws Exception {
		final ChannelPipeline pipeline = channel.pipeline();

		channel.attr(NetworkConstants.LOGIN_STATE).set(LoginState.SHA);
		
		CCcamLoginDecoder decoder = new CCcamLoginDecoder(cCcamServer);

		pipeline.addLast("filter", filter);
		pipeline.addLast("timeout", new IdleStateHandler(10000, 0, 0));
		pipeline.addLast("login-header-decoder", decoder);

		pipeline.addLast("channel-handler", ACCECPTOR_HANDLER);
		
		decoder.init(pipeline.firstContext());
	}

}
