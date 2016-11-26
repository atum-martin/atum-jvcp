package org.atum.jvcp;

import java.util.ArrayList;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.atum.jvcp.account.AccountStore;
import org.atum.jvcp.net.NettyBootstrap;
import org.atum.jvcp.net.codec.cccam.CCcamSession;

/**
 * A basic socket acceptor which will decode the CCcam handshake and print the username sent.
 * 
 * @since: 22/11/2016
 * @author atum-martin
 *
 */

public class CCcamServer extends Thread {

	@SuppressWarnings("unused")
	private Logger logger = Logger.getLogger(CCcamServer.class);
	private ArrayList<CCcamSession> sessionList = new ArrayList<CCcamSession>();
	
	
	public CCcamServer(int port) {
		BasicConfigurator.configure();
		AccountStore.getSingleton();
		
		NettyBootstrap.listen(this,port);
		this.start();
	}
	
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

	private void sendKeepAlives() {
		for(CCcamSession session : sessionList){
			if(session.getLastKeepalive() > 30000){
				session.keepAlive();
			}
		}
	}

	public static void main(String[] args) {
		new CCcamServer(12000);
	}

	public void registerSession(CCcamSession session) {
		synchronized (sessionList){
			sessionList.add(session);
		}
	}

}
