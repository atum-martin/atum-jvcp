package org.atum.jvcp;

import org.atum.jvcp.net.codec.cccam.CCcamSession;

/**
 * An interface which defines how a cam emulator should behave.
 * 
 * @since: 1/12/2016
 * @author atum-martin
 *
 */
public interface CamServer {
	/**
	 * Adds a {@link CCcamSession} to the list of sessions maintained as part of the CCcam server.
	 * @param session The instance of the session that should be added to the session list.
	 */
	public void registerSession(CCcamSession session);
}
