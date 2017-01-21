/**
 * 
 */
package org.atum.jvcp.net.codec.http;

import org.atum.jvcp.model.CamProtocol;
import org.atum.jvcp.model.CamSession;

import io.netty.channel.ChannelHandlerContext;


/**
 * @author <a href="https://github.com/atum-martin">atum-martin</a>
 * @since 13 Dec 2016 23:43:11
 */
public class GHttpSession extends CamSession {

	
	public GHttpSession(ChannelHandlerContext context){
		super(context, CamProtocol.GHTTP);
	}

}
