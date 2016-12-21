package org.atum.jvcp.net.codec.newcamd;

import java.util.List;

import org.apache.log4j.Logger;
import org.atum.jvcp.CardServer;
import org.atum.jvcp.crypto.DESUtil;
import org.atum.jvcp.model.EcmRequest;
import org.atum.jvcp.net.NetworkConstants;
import org.atum.jvcp.net.codec.NetUtils;
import org.atum.jvcp.net.codec.PacketState;
import org.atum.jvcp.net.codec.cccam.CCcamBuilds;
import org.atum.jvcp.net.codec.cccam.CCcamConstants;
import org.atum.jvcp.net.codec.cccam.CCcamSession;
import org.atum.jvcp.net.codec.cccam.CCcamBuilds.CCcamBuild;

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
		switch (packet.getCommand()) {
		
		default:
			logger.info("unhandled packet: " + packet.getCommand() + " " + packet.getSize());
			// payload.readBytes(size);
			break;
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
		int dataLength = decryptedPayload.readShort();
		if(dataLength != 0){
			if(dataLength != (packetSize-14)){
				loggerA.warn("Invalid packet size: "+dataLength+" "+packetSize);
			}
			ByteBuf payload = decryptedPayload.readBytes(dataLength);
			packet.setPayload(payload);
		}
		return packet;
	}
}
