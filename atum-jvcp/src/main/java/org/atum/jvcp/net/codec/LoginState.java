package org.atum.jvcp.net.codec;

public enum LoginState {

	HANDSHAKE,
	HEADER,
	LOGIN_BLOCK_HEADER,
	LOGIN_BLOCK,
	LOGGED_IN, 
	SHA
}
