package org.atum.jvcp.net.codec;

/**
 * @author <a href="https://github.com/atum-martin">atum-martin</a>
 * @since 21 Nov 2016 22:16:25
 */

public enum LoginState {

	HANDSHAKE,
	HEADER,
	LOGIN_BLOCK_HEADER,
	LOGIN_BLOCK,
	LOGGED_IN, 
	ENCRYPTION
}
