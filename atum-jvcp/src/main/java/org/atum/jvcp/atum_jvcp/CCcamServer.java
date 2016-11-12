package org.atum.jvcp.atum_jvcp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

import org.apache.log4j.Logger;

/**
 * A basic socket acceptor which will decode the CCcam handshake and print the username sent.
 * 
 * @since: 12/11/2016
 * @author atum-martin
 *
 */

public class CCcamServer {

	private OutputStream out = null;
	private InputStream in = null;
	private Logger logger = Logger.getLogger(CCcamServer.class);
	private Random r = new SecureRandom();
	
	public CCcamServer(int port) {
		try {
			ServerSocket acceptor = new ServerSocket(port);
			Socket s = null;
			while ((s = acceptor.accept()) != null) {
				try {
					newCCcamConn(s);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			acceptor.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void newCCcamConn(Socket s) throws IOException, NoSuchAlgorithmException {
		CCcamClient client = new CCcamClient(in,out);
		logger.info("new client connected "+client);
		byte[] secureRandom = new byte[16];
		getRandomBytes(secureRandom, 16);
		client.loginHandshake(secureRandom);
		
	}

	private void getRandomBytes(byte[] data, int len) {
		for (int i = 0; i < len; i++)
			data[i] = (byte) r.nextInt(127);
	}

	public static void main(String[] args) {
		new CCcamServer(12000);
	}

}
