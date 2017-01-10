package org.atum.jvcp.net.codec.cccam.io;

import java.util.List;

import org.apache.log4j.Logger;
import org.atum.jvcp.CCcamServer;
import org.atum.jvcp.account.Account;
import org.atum.jvcp.net.LoginDecoder;
import org.atum.jvcp.net.codec.LoginState;
import org.atum.jvcp.net.codec.NetUtils;
import org.atum.jvcp.net.codec.cccam.CCcamCipher;
import org.atum.jvcp.net.codec.cccam.CCcamSession;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

import static org.atum.jvcp.net.NetworkConstants.*;

/**
 * @author <a href="https://github.com/atum-martin">atum-martin</a>
 * @since 3 Dec 2016 14:57:43
 */

public class CCcamClientLoginDecoder extends LoginDecoder {

	private Logger logger = Logger.getLogger(CCcamClientLoginDecoder.class);
	
	
	public CCcamClientLoginDecoder(CCcamServer server) {
		super(server);
	}

	@Override
	public void init(ChannelHandlerContext firstContext) {
		firstContext.channel().attr(LOGIN_STATE).set(LoginState.HANDSHAKE);
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		LoginState state = ctx.channel().attr(LOGIN_STATE).get();
		logger.debug("processing client state: " + state);
		switch (state) {
		case HANDSHAKE:
			handleHandshake(ctx, in);
			break;
		case HEADER:
			handleLoginHeader(ctx, in);
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
		
		logger.info("Client SHA tests: "+isOscam+" "+isMultiCs);
		
		CCcamCipher.ccCamXOR(secureRandom);
		crypt.update(secureRandom);
		byte[] sha = crypt.digest();
		logger.debug("SHA cipher buffer len: "+sha.length);

		CCcamSession session = (CCcamSession) ctx.channel().attr(CAM_SESSION).get();
		
		CCcamCipher encrypter = session.getEncrypter();
		CCcamCipher decrypter = session.getDecrypter();
		
		decrypter.CipherInit(sha, sha.length);
		decrypter.decrypt(secureRandom, secureRandom.length);
		
		encrypter.CipherInit(secureRandom, secureRandom.length);
		encrypter.decrypt(sha, sha.length);
		
		Account account = session.getAccount();
		
		ByteBuf out = Unpooled.buffer(20);
		out.writeBytes(sha);
		encrypter.encrypt(out);
		ctx.writeAndFlush(out);
		
		out = Unpooled.buffer(20);
		NetUtils.writeCCcamStr(out, account.getUsername(), 20);
		encrypter.encrypt(out);
		ctx.writeAndFlush(out);
		
		final String CCcamHash = "CCcam\0";
		byte[] password = account.getPassword().getBytes();
		encrypter.encrypt(password,password.length);
		out = Unpooled.buffer(CCcamHash.length());
		out.writeBytes(CCcamHash.getBytes());
		encrypter.encrypt(out);
		ctx.writeAndFlush(out);
		
		ctx.channel().attr(LOGIN_STATE).set(LoginState.HEADER);

	}
	
	private void handleLoginHeader(ChannelHandlerContext ctx, ByteBuf in) {
		if (in.readableBytes() < 20) {
			logger.info("less than 16 bytes in client buffer");
			return;
		}
		CCcamSession session = (CCcamSession) ctx.channel().attr(CAM_SESSION).get();
		ByteBuf passHash = in.readBytes(20);
		session.getDecrypter().decrypt(passHash);
		String passwordVerification = NetUtils.readCCcamString(passHash, 20);
		final String CCcamHash = "CCcam";
		if(passwordVerification.equals(CCcamHash)){
			logger.info("password verified.");
		} else {
			logger.info("password verification failed: "+passwordVerification);
		}
		
		CCcamPacketSender sender = new CCcamPacketSender(session);
		session.setPacketSender(sender);
		
		ctx.channel().attr(LOGIN_STATE).set(null);
		ctx.channel().pipeline().replace("login-header-decoder", "packet-decoder", new CCcamPacketDecoder());
		ctx.channel().pipeline().addLast("packet-encoder", new CCcamPacketEncoder());
		
		sender.writeCliData();
	}

	private boolean testMultiCsSha(byte[] secureRandom) {
		int a = ((secureRandom[0] ^ 'M') + secureRandom[1] + secureRandom[2]) & 0xFF;
		int b = (secureRandom[4] + (secureRandom[5] ^ 'C') + secureRandom[6]) & 0xFF;
		int c = (secureRandom[8] + secureRandom[9] + (secureRandom[10] ^ 'S')) & 0xFF;
		return (a == secureRandom[3]) && (b == secureRandom[7]) && (c == secureRandom[11]);
	}

	private boolean testOscamSha(byte[] data) {
		for(int i = 0; i < 4; i++){
			if((data[12+i] & 0xFF) != ((data[i] + data[4 + i] + data[8 + i]) & 0xFF))
				return false;
		}
		return true;
	}




}
