/**
 * 
 */
package org.atum.jvcp;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.atum.jvcp.model.CamSession;
import org.atum.jvcp.net.codec.camd35.Camd35Session;

/**
 * @author <a href="https://github.com/atum-martin">atum-martin</a>
 * @since 13 Dec 2016 23:43:11
 */
public class Camd35Server extends Thread implements CamServer {


	/**
	 * Instance of log4j logger
	 */
	private Logger logger = Logger.getLogger(Camd35Server.class);
	
	/**
	 * A list which contains all open Camd35 readers and clients.
	 */
	private ArrayList<Camd35Session> sessionList = new ArrayList<Camd35Session>();
	
	/**
	 * Creates a new NewcamdServer server that will listen on a specified port.
	 * 
	 * @param port The port number the NewcamdServer server will bind to.
	 */
	public Camd35Server(String name, int port) {	
		this.setName(name);
		//NewcamdPipeline pipe = new NewcamdPipeline(this, NewcamdServerLoginDecoder.class);
		//NettyBootstrap.listenTcp(pipe,port);
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
		for(Camd35Session session : sessionList){
			if(session.getLastKeepalive() > 30000){
				session.keepAlive();
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.atum.jvcp.CamServer#registerSession(org.atum.jvcp.net.CamSession)
	 */
	public void registerSession(CamSession session) {
		/*synchronized(sessionList){
			sessionList.add((Camd35Session) session);
		}*/
	}

	public void addReaders(ArrayList<CamSession> readers) {
		synchronized (sessionList){
			for(CamSession session : sessionList){
				if(session.isReader())
					readers.add(session);
			}
		}
	}


	/**
	 * @param newcamdSession
	 */
	/*public void unregister(Camd35Session session) {
		if(session.isReader()){
			Camd35Session client = (Camd35Session) session;
			CardServer.registerReaderDisconnect(client);
		}
		logger.info("deregistering Camd35Session client: "+session);
		synchronized (sessionList){
			sessionList.remove(session);
		}
	}*/
}
