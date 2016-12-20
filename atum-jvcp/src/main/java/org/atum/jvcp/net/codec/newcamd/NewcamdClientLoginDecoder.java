package org.atum.jvcp.net.codec.newcamd;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import org.apache.log4j.Logger;
import org.atum.jvcp.NewcamdServer;
import org.atum.jvcp.crypto.DESUtil;
import org.atum.jvcp.net.LoginDecoder;
import org.atum.jvcp.net.NetworkConstants;
import org.atum.jvcp.net.codec.LoginState;

/**
 * @author <a href="https://github.com/atum-martin">atum-martin</a>
 * @since 15 Dec 2016 23:51:26
 */
public class NewcamdClientLoginDecoder extends LoginDecoder {

	private Logger logger = Logger.getLogger(NewcamdClientLoginDecoder.class);

	public NewcamdClientLoginDecoder(NewcamdServer newcamdServer) {
		super(newcamdServer);
	}

	@Override
	protected void decode(ChannelHandlerContext context, ByteBuf buffer, List<Object> outStream) throws Exception {
		LoginState state = context.channel().attr(NetworkConstants.LOGIN_STATE).get();
		logger.info("processing state: " + state);
		switch (state) {

		case ENCRYPTION:
			handleCrypto(context, buffer);
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

	}

	private void handleCrypto(ChannelHandlerContext context, ByteBuf buffer) {
		if (buffer.readableBytes() < 14) {
			logger.debug("less than 14 bytes in crypto buffer");
			return;
		}
		ByteBuf random14 = buffer.readBytes(14);
		NewcamdServer server = (NewcamdServer) camServer;
		byte[] desKey16 = DESUtil.desKeySpread((DESUtil.xorKey(server.getDesKey(), random14))); // loginKey
		NewcamdSession session = new NewcamdSession(desKey16);

		//String password = DESUtil.cryptPassword(clientPassword);

		context.channel().attr(NetworkConstants.CAM_SESSION).set(session);
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
