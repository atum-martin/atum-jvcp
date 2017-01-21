/**
 * 
 */
package org.atum.jvcp.net.codec.http.ghttp;

import org.atum.jvcp.model.CamProtocol;
import org.atum.jvcp.model.CamSession;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpResponse;


/**
 * @author <a href="https://github.com/atum-martin">atum-martin</a>
 * @since 13 Dec 2016 23:43:11
 */
public class GHttpSession extends CamSession {

	public GHttpSession(ChannelHandlerContext context, FullHttpResponse response){
		super(context, CamProtocol.GHTTP);
		setPacketSender(new GHttpPacketSender(this, response));
	}

	public void writeAndFlush(FullHttpResponse response) {
		getCtx().writeAndFlush(response);
	}

}
