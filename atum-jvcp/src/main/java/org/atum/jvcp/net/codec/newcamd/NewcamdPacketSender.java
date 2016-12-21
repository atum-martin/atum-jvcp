/**
 * 
 */
package org.atum.jvcp.net.codec.newcamd;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * @author <a href="https://github.com/atum-martin">atum-martin</a>
 * @since 21 Dec 2016 22:22:23
 */
public class NewcamdPacketSender {

	public static NewcamdPacket createCardData(int cardId){
		byte[] data = new byte[23];
		data[0] = 0x02; // user id, not 1
		data[1] = (byte)((cardId >> 8) & 0xFF); // caId
		data[2] = (byte)(cardId & 0xFF);
	    // 3-10 = 0 (card number)
		data[11] = 1; // provider count
	    // ident & id = 0
		ByteBuf payload = Unpooled.buffer(data.length);
		payload.writeBytes(data);
		NewcamdPacket packet = new NewcamdPacket(NewcamdConstants.MSG_CARD_DATA);
		packet.setPayload(payload);
		return packet;
	}
}
