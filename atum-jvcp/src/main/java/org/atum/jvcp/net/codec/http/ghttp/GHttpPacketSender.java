/**
 * 
 */
package org.atum.jvcp.net.codec.http.ghttp;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;

import org.atum.jvcp.model.EcmRequest;
import org.atum.jvcp.model.PacketSenderInterface;

import io.netty.handler.codec.http.FullHttpResponse;

/**
 * @author <a href="https://github.com/atum-martin">atum-martin</a>
 * @since 21 Jan 2017
 */
public class GHttpPacketSender implements PacketSenderInterface {

	private GHttpSession session;
	private FullHttpResponse response;
	
	public GHttpPacketSender(GHttpSession session, FullHttpResponse response) {
		this.session = session;
		this.response = response;
	}
	
	/* (non-Javadoc)
	 * @see org.atum.jvcp.model.PacketSenderInterface#writeKeepAlive()
	 */
	public void writeKeepAlive() {
		
	}

	/* (non-Javadoc)
	 * @see org.atum.jvcp.model.PacketSenderInterface#writeEcmAnswer(byte[])
	 */
	public void writeEcmAnswer(byte[] dcw) {
		response.content().writeBytes(dcw);
		response.headers().set("content-type", "application/octet-stream");
		response.headers().set(CONTENT_LENGTH, dcw.length);
		session.writeAndFlush(response);
		System.out.println("sending ecm answer: "+response);
		//session.write(new GHttpPacket(dcw));
	}

	/* (non-Javadoc)
	 * @see org.atum.jvcp.model.PacketSenderInterface#writeEcmRequest(org.atum.jvcp.model.EcmRequest)
	 */
	public void writeEcmRequest(EcmRequest req) {
		
	}

	/* (non-Javadoc)
	 * @see org.atum.jvcp.model.PacketSenderInterface#writeFailedEcm()
	 */
	public void writeFailedEcm() {
		// TODO Auto-generated method stub
		
	}

}
