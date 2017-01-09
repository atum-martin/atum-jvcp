/**
 * 
 */
package org.atum.jvcp.net.codec.newcamd.io;

import org.apache.log4j.Logger;
import org.atum.jvcp.crypto.DESUtil;
import org.atum.jvcp.model.EcmRequest;
import org.atum.jvcp.model.PacketSenderInterface;
import org.atum.jvcp.net.codec.newcamd.NewcamdClient;
import org.atum.jvcp.net.codec.newcamd.NewcamdConstants;
import org.atum.jvcp.net.codec.newcamd.NewcamdPacket;
import org.atum.jvcp.net.codec.newcamd.NewcamdSession;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * @author <a href="https://github.com/atum-martin">atum-martin</a>
 * @since 21 Dec 2016 22:22:23
 */
public class NewcamdPacketSender implements PacketSenderInterface {

	private Logger logger = Logger.getLogger(NewcamdPacketSender.class);

	private NewcamdSession session;

	public NewcamdPacketSender(NewcamdSession session) {
		this.session = session;
	}
	
	public static NewcamdPacket createCardData(NewcamdSession session, int cardId){
		session.setCardId(cardId);
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
	
	public static NewcamdPacket createLoginPacket(NewcamdClient client){
		
		String pass = DESUtil.cryptPassword(client.getAccount().getPassword());
		int packetSize = client.getAccount().getUsername().length() + pass.length() + 2;
		ByteBuf payload = Unpooled.buffer(packetSize);
		payload.writeBytes(client.getAccount().getUsername().getBytes());
		payload.writeByte(0);
		payload.writeBytes(pass.getBytes());
		payload.writeByte(0);
		NewcamdPacket packet = new NewcamdPacket(NewcamdConstants.MSG_CLIENT_2_SERVER_LOGIN);
		packet.setPayload(payload);
		return packet;
	}

	public void writeKeepAlive() {
		
	}

	public void writeEcmAnswer(byte[] dcw) {
		logger.info("writing newcamd DCW ecm request answer.");
		NewcamdPacket packet = new NewcamdPacket(NewcamdConstants.MSG_SERVER_2_CLIENT_ECM);
		packet.setPayload(Unpooled.copiedBuffer(dcw));
		session.write(packet);
		
	}

	public void writeEcmRequest(EcmRequest req) {
		logger.info("sending newcamd ecm request.");
	}
}
