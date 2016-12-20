package org.atum.jvcp.net.codec.newcamd;

import java.security.SecureRandom;
import java.util.List;
import java.util.Random;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

import org.apache.log4j.Logger;
import org.atum.jvcp.NewcamdServer;
import org.atum.jvcp.crypto.DESUtil;
import org.atum.jvcp.net.LoginDecoder;
import org.atum.jvcp.net.NetworkConstants;
import org.atum.jvcp.net.codec.LoginState;

/**
 * @author <a href="https://github.com/atum-martin">atum-martin</a>
 * @since 13 Dec 2016 23:43:11
 */
public class NewcamdServerLoginDecoder extends LoginDecoder {

	private Logger logger = Logger.getLogger(NewcamdServerLoginDecoder.class);
	
	private static Random r = new SecureRandom();

	private void fillRandomBytes(byte[] data, int len) {
		for (int i = 0; i < len; i++) {
			data[i] = (byte) r.nextInt(127);
		}
	}

	
	public NewcamdServerLoginDecoder(NewcamdServer newcamdServer) {
		super(newcamdServer);
	}

	@Override
	protected void decode(ChannelHandlerContext context, ByteBuf buffer, List<Object> outStream) throws Exception {
		LoginState state = context.channel().attr(NetworkConstants.LOGIN_STATE).get();
		logger.info("processing state: " + state);
		switch (state) {

		case ENCRYPTION:
			// this shouldn't be getting hit.
			handleCrypto(context);
			break;
		case HANDSHAKE:
			handleHandshake(context, buffer);
			break;
		case HEADER:
			handleLoginHeader(context, buffer);
			break;
		case LOGIN_BLOCK_HEADER:
			handleLoginBlockHeader(context, buffer);
			break;
		default:
			throw new IllegalStateException("Invalid state during login decoding.");
		}
	}

	public void init(ChannelHandlerContext context) {
		handleCrypto(context);
	}

	private void handleCrypto(ChannelHandlerContext context) {
		byte[] random = new byte[14];
		fillRandomBytes(random,random.length);
		ByteBuf out = Unpooled.buffer(random.length);
		out.writeBytes(random);
		context.writeAndFlush(out);
		context.channel().attr(NetworkConstants.LOGIN_STATE).set(LoginState.HANDSHAKE);
	}

	private void handleHandshake(ChannelHandlerContext context, ByteBuf buffer) {

		if (buffer.readableBytes() < 20) {
			logger.debug("less than 20 bytes in buffer");
			return;
		}
		
	}

	private void handleLoginHeader(ChannelHandlerContext context, ByteBuf buffer) {
		if (buffer.readableBytes() < 20) {
			logger.debug("less than 20 bytes in buffer");
			return;
		}

		context.channel().attr(NetworkConstants.LOGIN_STATE).set(LoginState.LOGIN_BLOCK_HEADER);
	}

	private void handleLoginBlockHeader(ChannelHandlerContext context, ByteBuf buffer) {

	}


}
