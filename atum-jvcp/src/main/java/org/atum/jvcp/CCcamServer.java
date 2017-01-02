package org.atum.jvcp;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.atum.jvcp.model.CamSession;
import org.atum.jvcp.model.EcmRequest;
import org.atum.jvcp.net.NettyBootstrap;
import org.atum.jvcp.net.codec.cccam.CCcamPipeline;
import org.atum.jvcp.net.codec.cccam.CCcamSession;
import org.atum.jvcp.net.codec.cccam.io.CCcamServerLoginDecoder;

/**
 * The main class of the CCcam application. Contains references to created sessions.
 * 
 * @since 12 Nov 2016 20:31:49
 * @author <a href="https://github.com/atum-martin">atum-martin</a>
 *
 */

public class CCcamServer extends Thread implements CamServer {

	/**
	 * Instance of log4j logger
	 */
	@SuppressWarnings("unused")
	private Logger logger = Logger.getLogger(CCcamServer.class);
	
	/**
	 * A list which contains all open CCcam readers and clients.
	 */
	private ArrayList<CCcamSession> sessionList = new ArrayList<CCcamSession>();
	
	/**
	 * Creates a new CCcam server that will listen on a specified port.
	 * 
	 * @param port The port number the CCcam server will bind to.
	 */
	public CCcamServer(String name, int port) {	
		this.setName(name);
		NettyBootstrap.listenTcp(new CCcamPipeline(this, CCcamServerLoginDecoder.class),port);
		this.start();	
	}
	
	/**
	 * Session keep alive thread.
	 */
	public void run(){
		while(true){
			synchronized(sessionList){
				sendKeepAlives();
			}
			try {
				Thread.sleep(200L);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Loops through all sessions checking when the last packet was sent.
	 * If this time exceeds 30 seconds send a keep alive packet. 
	 */
	private void sendKeepAlives() {
		for(CCcamSession session : sessionList){
			if(session.getLastKeepalive() > 30000){
				session.keepAlive();
			}
		}
	}

	/**
	 * Creates a default instance of CCcam server that listens on port 12000.
	 * @param args Not used.
	 */
	public static void main(String[] args) {
		new CCcamServer("cccam-server1",12000);
	}

	/**
	 * Adds a {@link CCcamSession} to the list of sessions maintained as part of the CCcam server.
	 * @param session The instance of the session that should be added to the session list.
	 */
	public void registerSession(CamSession session) {
		synchronized (sessionList){
			sessionList.add((CCcamSession) session);
		}
	}
	
	public void sendEcmToReaders(EcmRequest req){
		synchronized (sessionList){
			for(CamSession session : sessionList){
				session.getPacketSender().writeEcmRequest(req);
			}
		}
	}

	public byte[] getNodeId() {
		return new byte[]{0x12, 0x21, 0x45, 0x1A, 0x07, 0x19, 0x12, 0x12, };
	}

}
