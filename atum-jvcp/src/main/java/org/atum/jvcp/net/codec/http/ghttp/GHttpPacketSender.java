/**
 * 
 */
package org.atum.jvcp.net.codec.http.ghttp;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpResponseStatus.SERVICE_UNAVAILABLE;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import org.apache.log4j.Logger;
import org.atum.jvcp.model.EcmRequest;
import org.atum.jvcp.model.PacketSenderInterface;
import org.atum.jvcp.net.codec.NetUtils;

import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;

/**
 * @author <a href="https://github.com/atum-martin">atum-martin</a>
 * @since 21 Jan 2017
 */
public class GHttpPacketSender implements PacketSenderInterface {
	
	private Logger logger = Logger.getLogger(GHttpPacketSender.class);

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
		//case sentistive in oscam, failure for this header to be set will result in ghttp not working.
		response.headers().set("Content-Length", dcw.length);
		session.writeAndFlush(response);
		logger.debug("dcw dump: "+NetUtils.bytesToString(dcw,0,dcw.length));
		
		logger.debug("sending ecm answer: "+response);
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
		session.writeAndFlush(new DefaultFullHttpResponse(HTTP_1_1, SERVICE_UNAVAILABLE));
	}

}
