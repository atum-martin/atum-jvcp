package org.atum.jvcp.model;

import java.util.ArrayList;

import org.atum.jvcp.account.Account;
import org.atum.jvcp.net.NetworkConstants;
import org.atum.jvcp.net.codec.Packet;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author <a href="https://github.com/atum-martin">atum-martin</a>
 * @since 13 Dec 2016 23:56:31
 */
public abstract class CamSession {
	
	private PacketSenderInterface packetSender;
	private boolean isReader = false;
	private ChannelHandlerContext context;
	private EcmRequest lastRequest = null;

	private int packetCommandCode;
	private int packetSize;
	
	private long lastPing = System.currentTimeMillis();
	private CamProtocol protocol;
	
	public CamSession(ChannelHandlerContext context, CamProtocol protocol) {
		this.context = context;
		this.protocol = protocol;
	}

	public PacketSenderInterface getPacketSender(){
		return packetSender;
	}
	
	public void setPacketSender(PacketSenderInterface packetSender){
		this.packetSender = packetSender;
	}
	
	public boolean isReader() {
		return isReader;
	}
	
	public void setReader(boolean isReader){
		this.isReader = isReader;
	}
	
	public void setCurrentPacket(int cmdCode, int size) {
		this.packetCommandCode = cmdCode;
		this.packetSize = size;
	}
	
	public int getPacketCode(){
		return packetCommandCode;
	}
	
	public int getPacketSize(){
		return packetSize;
	}	
	
	public long getLastKeepalive(){
		return System.currentTimeMillis() - lastPing ;
	}
	
	public void keepAlive(){
		getPacketSender().writeKeepAlive();
		lastPing = System.currentTimeMillis();
	}

	public void setLastKeepAlive(long currentTimeMillis) {
		lastPing = currentTimeMillis;
	}
	
	public ChannelFuture write(Packet packet){
		return context.channel().writeAndFlush(packet);
	}	

	public ChannelHandlerContext getCtx() {
		return context;
	}
	

	public EcmRequest getLastRequest() {
		return lastRequest;
	}

	public void setLastRequest(EcmRequest lastRequest) {
		this.lastRequest = lastRequest;
	}
	
	public Account getAccount(){
		return this.getCtx().channel().attr(NetworkConstants.ACCOUNT).get();
	}
	
	public String toString(){
		if (isReader()){
			return "reader: "+getAccount()+" p: "+protocol;
		} else {
			return "client: "+getAccount()+" p: "+protocol;
		}
	}
	
	public abstract boolean hasCard(int cardId);

	/**
	 * @return
	 */
	public ArrayList<Integer> getGroups() {
		return getAccount().getGroups();
	}

}
