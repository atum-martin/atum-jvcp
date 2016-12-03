package org.atum.jvcp;

import org.atum.jvcp.net.codec.cccam.CCcamSession;

public abstract class CamServer extends Thread {

	public abstract void registerSession(CCcamSession session);
}
