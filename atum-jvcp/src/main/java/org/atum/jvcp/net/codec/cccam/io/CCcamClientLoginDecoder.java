package org.atum.jvcp.net.codec.cccam.io;

import java.util.List;

import org.apache.log4j.Logger;
import org.atum.jvcp.CCcamServer;
import org.atum.jvcp.net.LoginDecoder;
import org.atum.jvcp.net.NetworkConstants;
import org.atum.jvcp.net.codec.LoginState;
import org.atum.jvcp.net.codec.NetUtils;
import org.atum.jvcp.net.codec.cccam.CCcamCipher;
import org.atum.jvcp.net.codec.cccam.CCcamSession;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
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
		LoginState state = ctx.channel().attr(NetworkConstants.LOGIN_STATE).get();
		logger.info("processing client state: " + state);
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
		
		CCcamCipher.ccCamXOR(secureRandom);
		crypt.update(secureRandom);
		byte[] sha = crypt.digest();
		logger.debug("SHA cipher buffer len: "+sha.length);

		CCcamSession session = ctx.channel().attr(NetworkConstants.CCCAM_SESSION).get();
		
		CCcamCipher encrypter = session.getEncrypter();
		CCcamCipher decrypter = session.getDecrypter();
		
		decrypter.CipherInit(sha, sha.length);
		decrypter.decrypt(secureRandom, secureRandom.length);
		
		encrypter.CipherInit(secureRandom, secureRandom.length);
		encrypter.decrypt(sha, sha.length);
		
		ByteBuf out = Unpooled.buffer(20);
		out.writeBytes(sha);
		encrypter.encrypt(out);
		ctx.writeAndFlush(out);
		
		out = Unpooled.buffer(20);
		NetUtils.writeCCcamStr(out, "user99", 20);
		encrypter.encrypt(out);
		ctx.writeAndFlush(out);
		
		final String CCcamHash = "CCcam\0";
		byte[] password = "password789".getBytes();
		encrypter.encrypt(password,password.length);
		out = Unpooled.buffer(CCcamHash.length());
		out.writeBytes(CCcamHash.getBytes());
		encrypter.encrypt(out);
		ctx.writeAndFlush(out);
		
		ctx.channel().attr(NetworkConstants.LOGIN_STATE).set(LoginState.HEADER);

	}
	
	private void handleLoginHeader(ChannelHandlerContext ctx, ByteBuf in) {
		if (in.readableBytes() < 20) {
			logger.info("less than 16 bytes in client buffer");
			return;
		}
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
			sum += secureRandom[i];
		}
		return (recv_sum & 0xFF) == (sum & 0xFF);
	}




}
