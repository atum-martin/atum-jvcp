package org.atum.jvcp.atum_jvcp;

import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.log4j.Logger;
import org.atum.jvcp.net.codec.cccam.CCcamCipher;

public class CCcamSession {

	
	protected OutputStream out = null;
	protected InputStream in = null;
	private Logger logger = Logger.getLogger(CCcamSession.class);
	
	protected CCcamCipher encrypter = new CCcamCipher();
	protected CCcamCipher decrypter = new CCcamCipher();
	protected MessageDigest crypt = null;
	
	public CCcamSession(InputStream in, OutputStream out) {
		this.in = in;
		this.out = out;
		try {
			crypt = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		if(true) return;
	}
}
