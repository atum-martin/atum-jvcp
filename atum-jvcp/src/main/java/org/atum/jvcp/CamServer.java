package org.atum.jvcp;

import org.atum.jvcp.net.codec.cccam.CCcamSession;

public interface CamServer {

	public void registerSession(CCcamSession session);
}
