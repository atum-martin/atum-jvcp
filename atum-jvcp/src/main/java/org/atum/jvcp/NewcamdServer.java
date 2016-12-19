/**
 * 
 */
package org.atum.jvcp;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.atum.jvcp.model.CamSession;
import org.atum.jvcp.net.NettyBootstrap;
import org.atum.jvcp.net.codec.newcamd.NewcamdPipeline;
import org.atum.jvcp.net.codec.newcamd.NewcamdServerLoginDecoder;
import org.atum.jvcp.net.codec.newcamd.NewcamdSession;

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
	 * @param port The port number the CCcam server will bind to.
	 */
	public NewcamdServer(int port) {	
		NewcamdPipeline pipe = new NewcamdPipeline(this, NewcamdServerLoginDecoder.class);
		NettyBootstrap.listen(pipe,port);
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
		return new byte[]{ 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x10, 0x11, 0x12, 0x15, 0x14,};
	}
}
