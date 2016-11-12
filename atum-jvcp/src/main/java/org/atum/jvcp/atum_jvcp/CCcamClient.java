package org.atum.jvcp.atum_jvcp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.log4j.Logger;

/**
 * A Client class which contains references to the cipher digests.
 * 
 * @since: 12/11/2016
 * @author atum-martin
 *
 */

public class CCcamClient {

	private OutputStream out = null;
	private InputStream in = null;
	private Logger logger = Logger.getLogger(CCcamClient.class);
	
	private CCcamCipher encrypter = new CCcamCipher();
	private CCcamCipher decrypter = new CCcamCipher();
	private MessageDigest crypt = null;
	
	public CCcamClient(InputStream in, OutputStream out) {
		this.in = in;
		this.out = out;
		
		try {
			crypt = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

	public void loginHandshake(byte[] secureRandom) throws IOException {
		out.write(secureRandom, 0, secureRandom.length);
		out.flush();
		CCcamCipher.ccCamXOR(secureRandom);
		crypt.update(secureRandom);
		byte[] buf = crypt.digest();
		
		encrypter.CipherInit(buf, 20);
		encrypter.decrypt(secureRandom, 16);

		decrypter.CipherInit(secureRandom, 16);
		decrypter.encrypt(buf, 20);

		byte[] shaCipher = new byte[20];
		int len = in.read(shaCipher, 0, 20);
		if (len != 20) {
			logger.info("less than 20 bytes in buffer");
			return;
		}
		decrypter.decrypt(shaCipher, 20);

		byte[] usernameBuf = new byte[20];

		len = in.read(usernameBuf, 0, 20);
		if (len != 20) {
			logger.info("name less than 20 bytes in buffer");
			return;
		}
		decrypter.decrypt(usernameBuf, 20);
		String username = new String(usernameBuf);
		System.out.println(username);

	}
	
	@SuppressWarnings("unused")
	private void printArr(byte[] data) {
		System.out.println("byte arr:");
		for (byte b : data) {
			System.out.print(", " + b);
		}
		System.out.print('\n');
	}
}
