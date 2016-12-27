package org.atum.jvcp.net.codec.newcamd.io;

import java.util.List;

import org.apache.log4j.Logger;
import org.atum.jvcp.CardServer;
import org.atum.jvcp.crypto.DESUtil;
import org.atum.jvcp.model.EcmRequest;
import org.atum.jvcp.net.NetworkConstants;
import org.atum.jvcp.net.codec.PacketState;
import org.atum.jvcp.net.codec.newcamd.NewcamdConstants;
import org.atum.jvcp.net.codec.newcamd.NewcamdPacket;
import org.atum.jvcp.net.codec.newcamd.NewcamdSession;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

/**
 * @author <a href="https://github.com/atum-martin">atum-martin</a>
 * @since 22 Nov 2016 22:23:11
 */

public class NewcamdPacketDecoder extends ByteToMessageDecoder {

	private Logger logger = Logger.getLogger(NewcamdPacketDecoder.class);
	private static Logger loggerA = Logger.getLogger(NewcamdPacketDecoder.class);
	
	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		NewcamdSession session = (NewcamdSession) ctx.channel().attr(NetworkConstants.CAM_SESSION).get();
		NewcamdPacket packet = parseBuffer(ctx, session, in);
		if(packet == null){
			return;
		}
		handlePacket(session, packet);
	}

	private void handlePacket(NewcamdSession session, NewcamdPacket packet) {
		if(packet.isEcm()){
			decodeEcm(session, packet);
			return;
		}
		if(packet.isEmm()){
			decodeEmm(session, packet);
			return;
		}
		switch (packet.getCommand()) {
		case NewcamdConstants.MSG_CARD_DATA_REQ:
			logger.info("newcamd MSG_CARD_DATA_REQ decode: "+session.isReader());
			break;
		case NewcamdConstants.MSG_KEEPALIVE:
			handleKeepalive(session, packet);
			break;
		default:
			logger.info("unhandled packet: " + packet.getCommand() + " " + packet.getSize());
			// payload.readBytes(size);
			break;
		}
	}
	
	private void decodeEmm(NewcamdSession session, NewcamdPacket packet) {
		logger.info("newcamd emm decode: "+session.isReader());
	}

	private void handleKeepalive(NewcamdSession session, NewcamdPacket packet) {
		logger.info("newcamd keepalive decode: "+session.isReader());
	}

	private void decodeEcm(NewcamdSession session, NewcamdPacket packet) {
		if(packet.isDcw()){
			logger.info("newcamd ecm dcw decode: "+session.isReader());
			return;
		}
		logger.info("newcamd ecm decode: "+session.isReader());
		byte[] ecm = new byte[packet.getSize()+3];
		ecm[0] = (byte) packet.getCommand();
		ecm[1] = (byte) (packet.getSize() >> 8);
		ecm[2] = (byte) (packet.getSize() & 0xFF);
		packet.getPayload().readBytes(ecm, 3, packet.getSize());
		
		//long cspHash = EcmRequest.computeEcmHash(ecm);
		EcmRequest answer = CardServer.handleEcmRequest(session, session.getCardId(), 0, 0, 0, ecm);
		if(answer != null && answer.hasAnswer()){
			logger.info("handled client ECM: "+answer.getCspHash());
			//logger.info("dcw dump: "+bytesToString(answer.getDcw(),0,answer.getDcw().length));
			session.getPacketSender().writeEcmAnswer(answer.getDcw());
		}
	}

	public static NewcamdPacket parseBuffer(ChannelHandlerContext ctx, NewcamdSession session, ByteBuf in) {
		PacketState state = ctx.channel().attr(NetworkConstants.PACKET_STATE).get();
		if (state == null)
			state = PacketState.HEADER;
		switch (state) {
		case HEADER:
			if (in.readableBytes() < 2) {
				return null;
			}
			int size = in.readShort();
			session.setCurrentPacket(-1, size);

			if (in.readableBytes() < size) {
				ctx.channel().attr(NetworkConstants.PACKET_STATE).set(PacketState.PAYLOAD);
				return null;
			}
		case PAYLOAD:

			if (in.readableBytes() < session.getPacketSize()) {
				return null;
			}
			ctx.channel().attr(NetworkConstants.PACKET_STATE).set(PacketState.HEADER);
			byte[] payload = new byte[session.getPacketSize()];
			in.readBytes(payload);

			ByteBuf decryptedPayload = DESUtil.desDecrypt(payload, session.getPacketSize(), session.getDesKey());
			if(decryptedPayload == null){
				//invalid DES key.
				return null;
			} 
			return parseDecryptedBuffer(decryptedPayload, decryptedPayload.capacity());
		}
		return null;
	}

	/**
	 * @param decryptedPayload
	 * @param packetSize
	 * @return
	 */
	private static NewcamdPacket parseDecryptedBuffer(ByteBuf decryptedPayload, int packetSize) {
		ByteBuf headers = decryptedPayload.readBytes(10);
		int commandCode = decryptedPayload.readByte() & 0xFF;
		NewcamdPacket packet = new NewcamdPacket(commandCode, headers);
		//int dataLength = decryptedPayload.readShort();
		int dataLength = (decryptedPayload.readByte() & 0x0F) * 256 + (decryptedPayload.readByte() & 0xFF);
		if(dataLength != 0){
			if(dataLength != (packetSize-14)){
				loggerA.warn("Invalid packet size: "+dataLength+" "+packetSize+" "+commandCode);
			}
			ByteBuf payload = decryptedPayload.readBytes(dataLength);
			packet.setPayload(payload);
		}
		return packet;
	}
}
