package org.atum.jvcp.net.codec.cccam;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import org.apache.log4j.Logger;

public class CCcamSession {

	@SuppressWarnings("unused")
	private Logger logger = Logger.getLogger(CCcamSession.class);
	
	private ChannelHandlerContext context;
	private CCcamCipher encrypter;
	private CCcamCipher decrypter;
	
	private String username;
	
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
	
	public void write(ByteBuf buf){
		context.writeAndFlush(buf);
	}
}
