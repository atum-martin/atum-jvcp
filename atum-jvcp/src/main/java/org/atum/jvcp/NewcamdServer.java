/**
 * 
 */
package org.atum.jvcp;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.atum.jvcp.model.CamSession;
import org.atum.jvcp.net.NettyBootstrap;
import org.atum.jvcp.net.codec.NetUtils;
import org.atum.jvcp.net.codec.newcamd.NewcamdPipeline;
import org.atum.jvcp.net.codec.newcamd.NewcamdSession;
import org.atum.jvcp.net.codec.newcamd.io.NewcamdServerLoginDecoder;

/**
 * @author <a href="https://github.com/atum-martin">atum-martin</a>
 * @since 13 Dec 2016 23:43:11
 */
public class NewcamdServer extends Thread implements CamServer {


	/**
	 * Instance of log4j logger
	 */
	@SuppressWarnings("unused")
	private Logger logger = Logger.getLogger(NewcamdServer.class);
	
	/**
	 * A list which contains all open Newcamd readers and clients.
	 */
	@SuppressWarnings("unused")
	private ArrayList<NewcamdSession> sessionList = new ArrayList<NewcamdSession>();
	
	/**
	 * Creates a new NewcamdServer server that will listen on a specified port.
	 * 
	 * @param port The port number the NewcamdServer server will bind to.
	 */
	public NewcamdServer(String name, int port) {	
		this.setName(name);
		NewcamdPipeline pipe = new NewcamdPipeline(this, NewcamdServerLoginDecoder.class);
		NettyBootstrap.listenTcp(pipe,port);
		this.start();	
	}

	/* (non-Javadoc)
	 * @see org.atum.jvcp.CamServer#registerSession(org.atum.jvcp.net.CamSession)
	 */
	public void registerSession(CamSession session) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * Returns a 14 byte key used for encrypting and decrypting information on a newcamd channel.
	 * @return A 14 byte key used for encrypting and decrypting information on a newcamd channel.
	 */
	public byte[] getDesKey() {
		//return new byte[]{ 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x10, 0x11, 0x12, 0x15, 0x14,};
		//return new byte[]{ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 15, 14,};
		final byte[] key = NetUtils.getBytesValue("01 02 03 04 05 06 07 08 09 10 11 12 13 14");
		return key;
	}
}
