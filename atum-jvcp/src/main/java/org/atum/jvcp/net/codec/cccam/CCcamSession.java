package org.atum.jvcp.net.codec.cccam;

import io.netty.channel.ChannelHandlerContext;

import org.apache.log4j.Logger;

public class CCcamSession {

	@SuppressWarnings("unused")
	private Logger logger = Logger.getLogger(CCcamSession.class);
	
	private ChannelHandlerContext context;
	private CCcamCipher encrypter;
	private CCcamCipher decrypter;

	private String username;
	private CCcamPacketSender packetSender;

	private long lastPing = System.currentTimeMillis();
	
	public CCcamSession(ChannelHandlerContext context, CCcamCipher encrypter, CCcamCipher decrypter) {
		this.context = context;
		this.encrypter = encrypter;
		this.decrypter = decrypter;
	}

	public CCcamCipher getDecrypter() {
		return decrypter;
	}
	
	public CCcamCipher getEncrypter() {
		return encrypter;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getUsername() {
		return username;
	}
	
	public void write(CCcamPacket cCcamPacket){
		context.channel().writeAndFlush(cCcamPacket);
	}

	public void setPacketSender(CCcamPacketSender packetSender) {
		this.packetSender = packetSender;
	}
	
	public long getLastKeepalive(){
		return System.currentTimeMillis() - lastPing ;
	}
	
	public void keepAlive(){
		packetSender.writeKeepAlive();
		lastPing = System.currentTimeMillis();
	}
}
