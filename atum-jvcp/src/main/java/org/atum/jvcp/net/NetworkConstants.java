package org.atum.jvcp.net;

import org.atum.jvcp.net.codec.LoginState;
import org.atum.jvcp.net.codec.cccam.CCcamSession;

import io.netty.util.AttributeKey;

public class NetworkConstants {
	public static final AttributeKey<LoginState> LOGIN_STATE = AttributeKey.newInstance("LoginState");
	public static final AttributeKey<CCcamSession> CCCAM_SESSION = AttributeKey.newInstance("CCCAM_SESSION");
}
