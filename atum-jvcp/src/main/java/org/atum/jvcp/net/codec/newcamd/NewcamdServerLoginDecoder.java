package org.atum.jvcp.net.codec.newcamd;

import java.security.SecureRandom;
import java.util.List;
import java.util.Random;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

import org.apache.log4j.Logger;
import org.atum.jvcp.NewcamdServer;
import org.atum.jvcp.account.Account;
import org.atum.jvcp.account.AccountStore;
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
		
		NewcamdServer server = (NewcamdServer) camServer;
		byte[] desKey16 = DESUtil.desKeySpread(DESUtil.xorKey(server.getDesKey(), random));
		NewcamdSession session = new NewcamdSession(desKey16);
		
		context.channel().attr(NetworkConstants.CAM_SESSION).set(session);
		context.channel().attr(NetworkConstants.LOGIN_STATE).set(LoginState.HANDSHAKE);
	}

	private void handleHandshake(ChannelHandlerContext context, ByteBuf buffer) {

		if (buffer.readableBytes() < 13) {
			logger.debug("less than 13 bytes in handshake buffer");
			return;
		}
		NewcamdSession session = (NewcamdSession) context.channel().attr(NetworkConstants.CAM_SESSION).get();
		NewcamdPacket loginPacket = NewcamdPacketDecoder.parseBuffer(context, session, buffer);
		if(loginPacket == null || loginPacket.getCommand() != NewcamdConstants.MSG_CLIENT_2_SERVER_LOGIN){
			logger.info("newcamd client with invalid command code "+loginPacket.getCommand());
			context.channel().close();
			return;
		}
		String username = loginPacket.readStr();
		String cryptedPass = loginPacket.readStr();
		logger.info("newcamd login: "+username);
		Account acc = AccountStore.getSingleton().getAccount(username);
		if(acc == null/* || !DESUtil.checkPassword(acc.getPassword(), cryptedPass)*/){
			logger.info("newcamd invalid pass ");
			context.channel().close();
			return;
		}
		
		context.channel().pipeline().replace("login-header-decoder", "packet-decoder", new NewcamdPacketDecoder());
		context.channel().pipeline().addLast("packet-encoder", new NewcamdPacketEncoder());
		
		byte[] desKey = ((NewcamdServer) camServer).getDesKey();
		desKey = DESUtil.xorUserPass(desKey, cryptedPass);
        desKey = DESUtil.desKeySpread(desKey);
        session.setDesKey(desKey);
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
