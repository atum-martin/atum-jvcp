package org.atum.jvcp.net.codec.cccam.io;

import java.util.List;

import org.apache.log4j.Logger;
import org.atum.jvcp.CCcamServer;
import org.atum.jvcp.net.LoginDecoder;
import org.atum.jvcp.net.NetworkConstants;
import org.atum.jvcp.net.codec.LoginState;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class CCcamClientLoginDecoder extends LoginDecoder {

	private Logger logger = Logger.getLogger(CCcamClientLoginDecoder.class);
	
	public CCcamClientLoginDecoder(CCcamServer server) {
		super(server);
	}

	@Override
	public void init(ChannelHandlerContext firstContext) {
		firstContext.channel().attr(NetworkConstants.LOGIN_STATE).set(LoginState.HANDSHAKE);
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		logger.info("decoding client login handshake.");
		LoginState state = ctx.channel().attr(NetworkConstants.LOGIN_STATE).get();
		logger.info("processing state: " + state);
		switch (state) {
		case HANDSHAKE:
			handleHandshake(ctx, in);
			break;
		case HEADER:
			handleLoginHeader(ctx, in);
			break;
		case LOGIN_BLOCK_HEADER:
			handleLoginBlockHeader(ctx, in);
			break;
		default:
			throw new IllegalStateException("Invalid state during login decoding.");
		}
	}


	private void handleHandshake(ChannelHandlerContext ctx, ByteBuf in) {
		if (in.readableBytes() < 16) {
			logger.info("less than 16 bytes in client buffer");
			return;
		}
		byte[] secureRandom = new byte[16];
		in.readBytes(secureRandom);
		
		boolean isOscam = testOscamSha(secureRandom);
		boolean isMultiCs = testMultiCsSha(secureRandom);
		
		logger.info("Client sha tests: "+isOscam+" "+isMultiCs);
	}
	
	private void handleLoginHeader(ChannelHandlerContext ctx, ByteBuf in) {
		
	}
	
	private void handleLoginBlockHeader(ChannelHandlerContext ctx, ByteBuf in) {
		
	}

	private boolean testMultiCsSha(byte[] secureRandom) {
		int a = ((secureRandom[0] ^ 'M') + secureRandom[1] + secureRandom[2]) & 0xFF;
		int b = (secureRandom[4] + (secureRandom[5] ^ 'C') + secureRandom[6]) & 0xFF;
		int c = (secureRandom[8] + secureRandom[9] + (secureRandom[10] ^ 'S')) & 0xFF;
		return (a == secureRandom[3]) && (b == secureRandom[7]) && (c == secureRandom[11]);
	}

	private boolean testOscamSha(byte[] secureRandom) {
		int recv_sum = (secureRandom[14] << 8) | secureRandom[15];
		int sum = 0x1234;
		for(int i = 0; i < 14; i++)
		{
			sum += secureRandom[i] & 0xFF;
		}
		return recv_sum == sum;
	}




}
