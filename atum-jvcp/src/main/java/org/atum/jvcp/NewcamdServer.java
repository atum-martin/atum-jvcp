/**
 * 
 */
package org.atum.jvcp;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.atum.jvcp.net.NettyBootstrap;
import org.atum.jvcp.net.codec.cccam.CCcamPipeline;
import org.atum.jvcp.net.codec.cccam.CCcamSession;
import org.atum.jvcp.net.codec.cccam.io.CCcamServerLoginDecoder;
import org.atum.jvcp.net.codec.newcamd.NewcamdPipeline;
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
	private ArrayList<NewcamdSession> sessionList = new ArrayList<NewcamdSession>();
	
	/**
	 * Creates a new CCcam server that will listen on a specified port.
	 * 
	 * @param port The port number the CCcam server will bind to.
	 */
	public NewcamdServer(int port) {	
		NettyBootstrap.listen(new NewcamdPipeline(this, NewcamdServerLoginDecoder.class),port);
		this.start();	
	}

	/* (non-Javadoc)
	 * @see org.atum.jvcp.CamServer#registerSession(org.atum.jvcp.net.codec.cccam.CCcamSession)
	 */
	public void registerSession(CCcamSession session) {
		// TODO Auto-generated method stub
		
	}
}
