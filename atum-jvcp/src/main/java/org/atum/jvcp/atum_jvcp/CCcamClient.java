package org.atum.jvcp.atum_jvcp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.log4j.Logger;

/**
 * A Client class which contains references to the cipher digests.
 * 
 * @since: 12/11/2016
 * @author atum-martin
 *
 */

public class CCcamClient extends CCcamSession {

	private Logger logger = Logger.getLogger(CCcamClient.class);
	
	public CCcamClient(InputStream in, OutputStream out) {
		super(in,out);
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
		String username = toCCcamString(usernameBuf);
		System.out.println(username);
		
		byte[] passHash = new byte[6];
		len = in.read(passHash, 0, 6);
		if (len != 6) {
			logger.info("pass less than 6 bytes in buffer");
			return;
		}
		
		byte[] passLookup = "john589746".getBytes();
		decrypter.encrypt(passLookup, passLookup.length);
		decrypter.decrypt(passHash, 6);
		
		String passVerification = new String(passHash);
		final String CCcamHash = "CCcam\0";
		if(!CCcamHash.equals(passVerification)){
			logger.info("password could not be verified.");
			return;
		}
		
		byte[] clientVerification = new byte[20];
		System.arraycopy(CCcamHash.getBytes(), 0, clientVerification, 0, CCcamHash.length());
		encrypter.encrypt(clientVerification, 20);
		out.write(clientVerification);
		out.flush();
	}
	
	public String toCCcamString(byte[] arr){
		int len = findVal(arr,0);
		byte[] newStr = new byte[len];
		System.arraycopy(arr, 0, newStr, 0, len);
		return new String(newStr);
	}
	
	private int findVal(byte[] arr, int val) {
		for(int i = 0; i < arr.length; i++){
			if(arr[i] == val){
				return i;
			}
		}
		return arr.length;
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
