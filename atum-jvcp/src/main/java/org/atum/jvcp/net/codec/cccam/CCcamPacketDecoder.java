package org.atum.jvcp.net.codec.cccam;

import java.util.List;

import org.apache.log4j.Logger;
import org.atum.jvcp.net.NetworkConstants;
import org.atum.jvcp.net.codec.NetUtils;
import org.atum.jvcp.net.codec.PacketState;
import org.atum.jvcp.net.codec.cccam.CCcamBuilds.CCcamBuild;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

public class CCcamPacketDecoder extends ByteToMessageDecoder {

	private Logger logger = Logger.getLogger(CCcamPacketDecoder.class);

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		CCcamSession session = ctx.channel().attr(NetworkConstants.CCCAM_SESSION).get();
		PacketState state = ctx.channel().attr(NetworkConstants.PACKET_STATE).get();
		if (state == null)
			state = PacketState.HEADER;
		switch (state) {
		case HEADER:

			ByteBuf command = in.readBytes(4);
			synchronized (session) {
				session.getDecrypter().decrypt(command);
			}
			command.readByte();
			int cmdCode = command.readByte() & 0xFF;
			int size = command.readShort();

			//logger.info("packet recieved: " + cmdCode + " " + size);
			session.setCurrentPacket(cmdCode, size);

			if (in.readableBytes() < size) {
				logger.info("packet payload too small: " + cmdCode + " " + size);
				ctx.channel().attr(NetworkConstants.PACKET_STATE).set(PacketState.PAYLOAD);
				return;
			}
		case PAYLOAD:
			if (state == PacketState.PAYLOAD) {
				logger.info("reconstructing fragmented packet: " + session.getPacketCode() + " " + session.getPacketSize());
			}
			
			if (in.readableBytes() < session.getPacketSize()) {
				return;
			}
			ctx.channel().attr(NetworkConstants.PACKET_STATE).set(PacketState.HEADER);
			ByteBuf payload = in.readBytes(session.getPacketSize());
			synchronized (session) {
				session.getDecrypter().decrypt(payload);
			}
			handlePacket(session, session.getPacketCode(), session.getPacketSize(), payload);
			break;
		}
	}

	private void handlePacket(CCcamSession session, int cmdCode, int size, ByteBuf payload) {
		switch (cmdCode) {
		case CCcamConstants.MSG_CLI_DATA:
			String username = NetUtils.readCCcamString(payload, 20);

			long nodeId = payload.readLong();
			@SuppressWarnings("unused")
			int flag = payload.readByte();
			String version = NetUtils.readCCcamString(payload, 32);
			int build = Integer.parseInt(NetUtils.readCCcamString(payload, 32));
			logger.info("MSG_CLI_DATA: " + version + " " + build + " " + username + " " + nodeId);
			CCcamBuild ccBuild = CCcamBuilds.getBuild(version);
			if (ccBuild != null && ccBuild.getBuildNum() == build) {
				logger.info("CCcam build verified: " + build);
			}
			break;
		case CCcamConstants.MSG_SRV_DATA:
			nodeId = payload.readLong();
			version = NetUtils.readCCcamString(payload, 32);
			build = Integer.parseInt(NetUtils.readCCcamString(payload, 32));
			logger.info("MSG_SRV_DATA: " + version + " " + build + " " + nodeId);
			break;
		
		case CCcamConstants.MSG_CACHE_PUSH:
			decodeCCcamCachePush(session, payload);
			break;
			
		case CCcamConstants.MSG_CW_ECM:
			decodeCCcamEcm(session, payload);
			break;
			
		case CCcamConstants.MSG_CARD_REMOVED:
			int cardId = payload.readInt();
			break;	

		default:
			logger.info("unhandled packet: " + cmdCode + " " + size);
			//payload.readBytes(size);
			break;
		}
	}

	private void decodeCCcamEcm(CCcamSession session, ByteBuf payload) {
		int cardId = payload.readShort();
		int provider = payload.readInt();
		int id = payload.readInt();
		int serviceId = payload.readShort();
		int ecmLength = payload.readableBytes();
		byte[] ecm = new byte[ecmLength];
		payload.readBytes(ecm);
	}

	private void decodeCCcamCachePush(CCcamSession session, ByteBuf payload) {
		int cardId = payload.readShort();
		int provider = payload.readInt();
		payload.readInt();
		int serviceId = payload.readShort();
		int ecmSize = payload.readShort();
		int returnCode = payload.readByte();
		payload.readByte();
		payload.readByte();
		payload.readByte();
		int cycleTime = payload.readByte(); 
		int ecm0 = payload.readByte();
		
		byte[] ecmd5 = new byte[16];
		payload.readBytes(ecmd5);
		int cspHash = payload.readInt();
		
		byte[] cw = new byte[16];
		payload.readBytes(cw);
		int nodeCount = payload.readByte()-1;
		long nodeId = payload.readLong();
		for(int i = 0; i < nodeCount; i++){
			long cacheNodeId = payload.readLong();
		}
		
	}

}
