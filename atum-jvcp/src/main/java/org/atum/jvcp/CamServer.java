package org.atum.jvcp;

import org.atum.jvcp.net.CamSession;

/**
 * An interface which defines how a cam emulator should behave.
 * 
 * @since 3 Dec 2016 14:57:43
 * @author <a href="https://github.com/atum-martin">atum-martin</a>
 *
 */
public interface CamServer {
	/**
	 * Adds a {@link CamSession} to the list of sessions maintained as part of the CCcam server.
	 * @param session The instance of the session that should be added to the session list.
	 */
	public void registerSession(CamSession session);
}
