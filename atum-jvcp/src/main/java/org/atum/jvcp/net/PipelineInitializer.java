package org.atum.jvcp.net;

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


	private final ChannelAcceptorHandler ACCECPTOR_HANDLER = new ChannelAcceptorHandler();
	
	public PipelineInitializer(){
	}

	@Override
	protected void initChannel(SocketChannel channel) throws Exception {
		final ChannelPipeline pipeline = channel.pipeline();

		channel.attr(NetworkConstants.LOGIN_STATE).set(LoginState.SHA);
		
		CCcamLoginDecoder decoder = new CCcamLoginDecoder();

		pipeline.addLast("timeout", new IdleStateHandler(10000, 0, 0));
		pipeline.addLast("login-header-decoder", decoder);
		// pipeline.addLast("packet-encoder", new PacketEncoder());

		pipeline.addLast("channel-handler", ACCECPTOR_HANDLER);
		
		decoder.init(pipeline.firstContext());
	}

}