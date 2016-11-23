package org.atum.jvcp;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.atum.jvcp.account.AccountStore;
import org.atum.jvcp.net.NettyBootstrap;

/**
 * A basic socket acceptor which will decode the CCcam handshake and print the username sent.
 * 
 * @since: 22/11/2016
 * @author atum-martin
 *
 */

public class CCcamServer {

	@SuppressWarnings("unused")
	private Logger logger = Logger.getLogger(CCcamServer.class);
	
	public CCcamServer(int port) {
		BasicConfigurator.configure();
		AccountStore.getSingleton();
		
		NettyBootstrap.listen(port);
	}

	public static void main(String[] args) {
		new CCcamServer(12000);
	}

}
