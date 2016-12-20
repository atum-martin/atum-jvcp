package org.atum.jvcp.model;

/**
 * @author <a href="https://github.com/atum-martin">atum-martin</a>
 * @since 13 Dec 2016 23:56:31
 */
public class CamSession {
	
	private PacketSenderInterface packetSender;
	private boolean isReader = false;

	private int packetCommandCode;
	private int packetSize;
	
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
}
