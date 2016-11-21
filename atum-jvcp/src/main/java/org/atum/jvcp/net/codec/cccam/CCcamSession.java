package org.atum.jvcp.net.codec.cccam;

import org.apache.log4j.Logger;

public class CCcamSession {

	private Logger logger = Logger.getLogger(CCcamSession.class);
	
	protected CCcamCipher encrypter;
	protected CCcamCipher decrypter;
	
	public CCcamSession(CCcamCipher encrypter, CCcamCipher decrypter) {
		this.encrypter = encrypter;
		this.decrypter = decrypter;
	}

	public CCcamCipher getDecrypter() {
		return decrypter;
	}
	
	public CCcamCipher getEncrypter() {
		return encrypter;
	}
}
