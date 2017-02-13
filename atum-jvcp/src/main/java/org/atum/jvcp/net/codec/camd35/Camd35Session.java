/**
 * 
 */
package org.atum.jvcp.net.codec.camd35;

import org.atum.jvcp.model.CamProtocol;
import org.atum.jvcp.model.CamSession;

import io.netty.channel.ChannelHandlerContext;

/**
 * @author <a href="https://github.com/atum-martin">atum-martin</a>
 * @since 3 Feb 2017
 */
public class Camd35Session extends CamSession {

	private boolean udp = true;
	
	/**
	 * @param context
	 * @param protocol
	 */
	public Camd35Session(ChannelHandlerContext context, CamProtocol protocol) {
		super(context, protocol); 
	}

	@Override
	public boolean hasCard(int cardId) {
		return false;
	}

	@Override
	public void unregister() {
		
	}

}
