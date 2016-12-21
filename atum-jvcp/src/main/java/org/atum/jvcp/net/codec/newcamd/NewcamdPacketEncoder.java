/**
 * 
 */
package org.atum.jvcp.net.codec.newcamd;

import org.atum.jvcp.crypto.DESUtil;
import org.atum.jvcp.net.NetworkConstants;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @author <a href="https://github.com/atum-martin">atum-martin</a>
 * @since 21 Dec 2016 21:22:17
 */
public class NewcamdPacketEncoder extends MessageToByteEncoder<NewcamdPacket> {

	@Override
	protected void encode(ChannelHandlerContext ctx, NewcamdPacket msg, ByteBuf out) throws Exception {
		NewcamdSession session = (NewcamdSession) ctx.channel().attr(NetworkConstants.CAM_SESSION).get();
		int payloadCap = 0;
		if (msg.getPayload() != null)
			payloadCap = msg.getPayload().capacity();

		byte[] encryptedPayload = new byte[11 + payloadCap];
		msg.getHeaders().readBytes(encryptedPayload);
		encryptedPayload[11] = (byte) (msg.getCommand() & 0xFF);
		if (payloadCap != 0)
			msg.getPayload().readBytes(encryptedPayload, 12, payloadCap);

		encryptedPayload = DESUtil.desEncrypt(encryptedPayload, encryptedPayload.length, session.getDesKey(), NewcamdConstants.CWS_NETMSGSIZE);
		ByteBuf output = Unpooled.buffer(2 + encryptedPayload.length);
		output.writeShort(output.capacity());
		output.writeBytes(encryptedPayload);		
		
		out.writeBytes(output);
		output.release();
		
	}
}
