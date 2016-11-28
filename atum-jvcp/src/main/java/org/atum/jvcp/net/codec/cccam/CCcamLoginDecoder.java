package org.atum.jvcp.net.codec.cccam;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.List;
import java.util.Random;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import org.apache.log4j.Logger;
import org.atum.jvcp.CCcamServer;
import org.atum.jvcp.account.Account;
import org.atum.jvcp.account.AccountStore;
import org.atum.jvcp.net.NetworkConstants;
import org.atum.jvcp.net.codec.LoginState;
import org.atum.jvcp.net.codec.NetUtils;

/**
 *
 * @author Martin
 */
public class CCcamLoginDecoder extends ByteToMessageDecoder {

	private Logger logger = Logger.getLogger(CCcamLoginDecoder.class);
	private CCcamServer cCcamServer;
	private static Random r = new SecureRandom();
	private static MessageDigest crypt = null;

	private void getRandomBytes(byte[] data, int len) {
		for (int i = 0; i < len; i++) {
			data[i] = (byte) r.nextInt(127);
		}
	}

	public CCcamLoginDecoder(CCcamServer cCcamServer) {
		try {
			this.cCcamServer = cCcamServer;
			if (crypt == null) {
				crypt = MessageDigest.getInstance("SHA-1");
			}
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void decode(ChannelHandlerContext context, ByteBuf buffer, List<Object> outStream) throws Exception {
		LoginState state = context.channel().attr(NetworkConstants.LOGIN_STATE).get();
		logger.info("processing state: " + state);
		switch (state) {

		case SHA:
			// this shouldn't be getting hit.
			handleSHA(context);
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
		handleSHA(context);
	}

	private void handleSHA(ChannelHandlerContext context) {
		byte[] secureRandom = new byte[16];
		CCcamCipher encrypter = new CCcamCipher();
		CCcamCipher decrypter = new CCcamCipher();

		getRandomBytes(secureRandom, secureRandom.length);

		ByteBuf out = Unpooled.buffer(secureRandom.length);
		out.writeBytes(secureRandom);
		context.writeAndFlush(out);

		CCcamCipher.ccCamXOR(secureRandom);
		crypt.update(secureRandom);
		byte[] buf = crypt.digest();

		encrypter.CipherInit(buf, 20);
		encrypter.decrypt(secureRandom, 16);

		decrypter.CipherInit(secureRandom, 16);
		decrypter.encrypt(buf, 20);

		CCcamSession session = new CCcamSession(context, encrypter, decrypter);

		context.channel().attr(NetworkConstants.LOGIN_STATE).set(LoginState.HANDSHAKE);
		context.channel().attr(NetworkConstants.CCCAM_SESSION).set(session);

	}

	private void handleHandshake(ChannelHandlerContext context, ByteBuf buffer) {

		if (buffer.readableBytes() < 20) {
			logger.info("less than 20 bytes in buffer");
			return;
		}

		CCcamSession session = context.channel().attr(NetworkConstants.CCCAM_SESSION).get();

		byte[] shaCipher = new byte[20];

		NetUtils.readBuffer(buffer, shaCipher, 20);

		session.getDecrypter().decrypt(shaCipher, 20);

		context.channel().attr(NetworkConstants.LOGIN_STATE).set(LoginState.HEADER);

	}

	private void handleLoginHeader(ChannelHandlerContext context, ByteBuf buffer) {
		if (buffer.readableBytes() < 20) {
			logger.info("less than 20 bytes in buffer");
			return;
		}

		CCcamSession session = context.channel().attr(NetworkConstants.CCCAM_SESSION).get();

		byte[] usernameBuf = new byte[20];

		NetUtils.readBuffer(buffer, usernameBuf, 20);

		session.getDecrypter().decrypt(usernameBuf, 20);
		String username = NetUtils.toCCcamString(usernameBuf);
		session.setUsername(username);
		logger.info("Username: " + username);

		context.channel().attr(NetworkConstants.LOGIN_STATE).set(LoginState.LOGIN_BLOCK_HEADER);
	}

	private void handleLoginBlockHeader(ChannelHandlerContext context, ByteBuf buffer) {

		if (buffer.readableBytes() < 6) {
			logger.info("less than 6 bytes in buffer");
			return;
		}

		CCcamSession session = context.channel().attr(NetworkConstants.CCCAM_SESSION).get();

		byte[] passHash = new byte[6];
		NetUtils.readBuffer(buffer, passHash, 6);

		Account acc = AccountStore.getSingleton().getAccount(session.getUsername());
		byte[] passLookup = acc.getPassword().getBytes();

		session.getDecrypter().encrypt(passLookup, passLookup.length);
		session.getDecrypter().decrypt(passHash, 6);

		String passVerification = new String(passHash);
		final String CCcamHash = "CCcam\0";
		if (!CCcamHash.equals(passVerification)) {
			logger.info("password could not be verified.");
			return;
		}

		logger.info("password verified.");
		byte[] clientVerification = new byte[20];
		System.arraycopy(CCcamHash.getBytes(), 0, clientVerification, 0, CCcamHash.length());
		session.getEncrypter().encrypt(clientVerification, 20);

		ByteBuf buf = Unpooled.buffer(20);
		buf.writeBytes(clientVerification);
		context.writeAndFlush(buf);

		context.channel().attr(NetworkConstants.LOGIN_STATE).set(null);
		context.channel().pipeline().replace("login-header-decoder", "packet-decoder", new CCcamPacketDecoder());
		context.pipeline().addLast("packet-encoder", new CCcamPacketEncoder());
		
		CCcamPacketSender sender = new CCcamPacketSender(session);
		session.setPacketSender(sender);
		sender.writeCliData();
		sender.writeSrvData();
		Random ra = new Random();
		//for (int i = 0 ; i < 100; i++)
		//	sender.writeCard(new Card(0x963,ra.nextInt(),ra.nextInt()));
		
		
		
		cCcamServer.registerSession(session);
	}
	

	/*private void handleLoginBlock(ChannelHandlerContext context, ByteBuf buffer) {
		if (buffer.readableBytes() < 66) {
			logger.info("less than 66 bytes in buffer");
			return;
		}
		logger.info("buf size: "+buffer.readableBytes());
		byte[] junk = new byte[24];
		readBuffer(buffer, junk, junk.length);
		
		byte[] nodeId = new byte[8];
		readBuffer(buffer, nodeId, nodeId.length);
		
		byte[] remoteVersionByte = new byte[32];
		readBuffer(buffer, remoteVersionByte, remoteVersionByte.length);
		String remoteVersion = toCCcamString(remoteVersionByte);
		
		byte[] remoteBuildByte = new byte[32];
		readBuffer(buffer, remoteBuildByte, remoteBuildByte.length);
		String remoteBuild = toCCcamString(remoteBuildByte);
		
		logger.info(remoteVersion+" "+remoteBuild);
		
	}*/

}
