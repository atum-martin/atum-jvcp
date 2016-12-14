package org.atum.jvcp.net;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.atum.jvcp.CamServer;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

public abstract class LoginDecoder extends ByteToMessageDecoder {

	protected CamServer camServer;
	protected static MessageDigest crypt = null;
	public abstract void init(ChannelHandlerContext firstContext);
	
	public LoginDecoder(CamServer camServer) {
		this.camServer = camServer;
		try {
			if (crypt == null) {
				crypt = MessageDigest.getInstance("SHA-1");
			}
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

}
