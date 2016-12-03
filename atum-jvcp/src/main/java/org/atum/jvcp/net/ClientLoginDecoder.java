package org.atum.jvcp.net;

import org.atum.jvcp.CamServer;

import io.netty.handler.codec.ByteToMessageDecoder;

public abstract class ClientLoginDecoder extends ByteToMessageDecoder {
	private CamServer server;
	
	public ClientLoginDecoder(CamServer server){
		this.server = server;
	}
	
}
