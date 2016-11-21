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
import org.atum.jvcp.atum_jvcp.account.Account;
import org.atum.jvcp.atum_jvcp.account.AccountStore;
import org.atum.jvcp.net.NetworkConstants;
import org.atum.jvcp.net.codec.LoginState;

/**
 *
 * @author Martin
 */
public class CCcamLoginDecoder extends ByteToMessageDecoder {

	private Logger logger = Logger.getLogger(CCcamLoginDecoder.class);
	private static Random r = new SecureRandom();
	private static MessageDigest crypt = null;

	private void getRandomBytes(byte[] data, int len) {
		for (int i = 0; i < len; i++) {
			data[i] = (byte) r.nextInt(127);
		}
	}

	public CCcamLoginDecoder() {
		try {
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
		case LOGIN_BLOCK:
			// handleLoginBlock(context, buffer);
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

		CCcamSession session = new CCcamSession(encrypter, decrypter);

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

		readBuffer(buffer, shaCipher, 20);

		session.getDecrypter().decrypt(shaCipher, 20);

		context.channel().attr(NetworkConstants.LOGIN_STATE).set(LoginState.HEADER);

	}

	private void readBuffer(ByteBuf buffer, byte[] buf, int len) {
		for (int i = 0; i < len; i++) {
			buf[i] = buffer.readByte();
		}
	}

	public String toCCcamString(byte[] arr) {
		int len = findVal(arr, 0);
		byte[] newStr = new byte[len];
		System.arraycopy(arr, 0, newStr, 0, len);
		return new String(newStr);
	}

	private int findVal(byte[] arr, int val) {
		for (int i = 0; i < arr.length; i++) {
			if (arr[i] == val) {
				return i;
			}
		}
		return arr.length;
	}

	private void handleLoginHeader(ChannelHandlerContext context, ByteBuf buffer) {
		if (buffer.readableBytes() < 20) {
			logger.info("less than 20 bytes in buffer");
			return;
		}

		CCcamSession session = context.channel().attr(NetworkConstants.CCCAM_SESSION).get();

		byte[] usernameBuf = new byte[20];

		readBuffer(buffer, usernameBuf, 20);

		session.getDecrypter().decrypt(usernameBuf, 20);
		String username = toCCcamString(usernameBuf);
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
		readBuffer(buffer, passHash, 6);

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

		context.channel().attr(NetworkConstants.LOGIN_STATE).set(LoginState.LOGIN_BLOCK);
	}

}
