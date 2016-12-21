/**
 * 
 */
package org.atum.jvcp.net.codec.newcamd.io;

import org.atum.jvcp.net.codec.newcamd.NewcamdConstants;
import org.atum.jvcp.net.codec.newcamd.NewcamdPacket;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * @author <a href="https://github.com/atum-martin">atum-martin</a>
 * @since 21 Dec 2016 22:22:23
 */
public class NewcamdPacketSender {

	public static NewcamdPacket createCardData(int cardId){
		ByteBuf payload = Unpooled.buffer(23);
		payload.writeByte(0x02);//user id
		payload.writeShort(cardId);
		payload.writeLong(0L);//card number, unique identified
		payload.writeByte(1);//1 provider.
		//each provider is 11 bytes long.
		payload.writeBytes(new byte[11]);
		NewcamdPacket packet = new NewcamdPacket(NewcamdConstants.MSG_CARD_DATA);
		packet.setPayload(payload);
		return packet;
	}
}
