package org.atum.jvcp.net;

import org.atum.jvcp.model.CamSession;
import org.atum.jvcp.net.codec.LoginState;
import org.atum.jvcp.net.codec.PacketState;

import io.netty.util.AttributeKey;

/**
 * @author <a href="https://github.com/atum-martin">atum-martin</a>
 * @since 21 Nov 2016 22:16:25
 * 
 */

public class NetworkConstants {
	public static final AttributeKey<LoginState> LOGIN_STATE = AttributeKey.newInstance("LoginState");
	public static final AttributeKey<CamSession> CAM_SESSION = AttributeKey.newInstance("CAM_SESSION");
	public static final AttributeKey<PacketState> PACKET_STATE = AttributeKey.newInstance("PACKET_STATE");
}
