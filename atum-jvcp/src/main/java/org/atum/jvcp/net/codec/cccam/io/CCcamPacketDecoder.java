package org.atum.jvcp.net.codec.cccam.io;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.log4j.Logger;
import org.atum.jvcp.CardServer;
import org.atum.jvcp.model.Card;
import org.atum.jvcp.model.EcmRequest;
import org.atum.jvcp.net.NetworkConstants;
import org.atum.jvcp.net.codec.NetUtils;
import org.atum.jvcp.net.codec.PacketState;
import org.atum.jvcp.net.codec.cccam.CCcamBuilds;
import org.atum.jvcp.net.codec.cccam.CCcamCipher;
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

public class CCcamPacketDecoder extends ByteToMessageDecoder {

	private Logger logger = Logger.getLogger(CCcamPacketDecoder.class);

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		CCcamSession session = (CCcamSession) ctx.channel().attr(NetworkConstants.CAM_SESSION).get();
		PacketState state = ctx.channel().attr(NetworkConstants.PACKET_STATE).get();
		if (state == null)
			state = PacketState.HEADER;
		switch (state) {
		case HEADER:
			if(in.readableBytes() < 4){
				return;
			}
			ByteBuf command = in.readBytes(4);
			synchronized (session) {
				session.getDecrypter().decrypt(command);
			}
			int ecmIdx = command.readByte();
			int cmdCode = command.readByte() & 0xFF;
			int size = command.readShort();
			command.release();
			//logger.info("packet recieved: " + cmdCode + " " + size);
			session.setCurrentPacket(cmdCode, size, ecmIdx);

			if (in.readableBytes() < size) {
				//logger.info("packet payload too small: " + cmdCode + " " + size);
				ctx.channel().attr(NetworkConstants.PACKET_STATE).set(PacketState.PAYLOAD);
				return;
			}
		case PAYLOAD:
			/*if (state == PacketState.PAYLOAD) {
				logger.info("reconstructing fragmented packet: " + session.getPacketCode() + " " + session.getPacketSize());
			}*/
			
			if (in.readableBytes() < session.getPacketSize()) {
				return;
			}
			ctx.channel().attr(NetworkConstants.PACKET_STATE).set(PacketState.HEADER);
			ByteBuf payload = in.readBytes(session.getPacketSize());
			synchronized (session) {
				session.getDecrypter().decrypt(payload);
			}
			handlePacket(session, session.getPacketCode(), session.getPacketSize(), payload);
			payload.release();
			break;
		}
	}

	private void handlePacket(CCcamSession session, int cmdCode, int size, ByteBuf payload) {
		switch (cmdCode) {
		case CCcamConstants.MSG_CLI_DATA:
			if(size < 93){
				return;
			}
			String username = NetUtils.readCCcamString(payload, 20);

			byte[] nodeId = new byte[8];
			payload.readBytes(nodeId);
			session.setNodeId(nodeId);
			@SuppressWarnings("unused")
			int flag = payload.readByte();
			String version = NetUtils.readCCcamString(payload, 32);
			int build = Integer.parseInt(NetUtils.readCCcamString(payload, 32));
			logger.info("decoding MSG_CLI_DATA: " + version + " " + build + " " + username + " " + nodeId);
			CCcamBuild ccBuild = CCcamBuilds.getBuild(version);
			if (ccBuild != null && ccBuild.getBuildNum() == build) {
				logger.info("CCcam build verified: " + build);
			}
			break;
		case CCcamConstants.MSG_SRV_DATA:
			byte[] servNodeId = new byte[8];
			payload.readBytes(servNodeId);
			session.setNodeId(servNodeId);
			version = NetUtils.readCCcamString(payload, 32);
			build = Integer.parseInt(NetUtils.readCCcamString(payload, 32));
			logger.info("MSG_SRV_DATA: " + version + " " + build + " " + servNodeId);
			break;
		
		case CCcamConstants.MSG_CACHE_PUSH:
			decodeCCcamCachePush(session, payload);
			break;
			
		case CCcamConstants.MSG_CW_ECM:
			decodeCCcamEcm(session, payload, size);
			break;
			
		case CCcamConstants.MSG_CARD_REMOVED:
			@SuppressWarnings("unused")
			int cardId = payload.readInt();
			break;	
			
		case CCcamConstants.MSG_NEW_CARD:
			decodeCCcamNewCard(session, payload);
			break;	
			
		case CCcamConstants.MSG_KEEPALIVE:
			session.setLastKeepAlive(System.currentTimeMillis());
			break;

		default:
			logger.info("unhandled packet: " + cmdCode + " " + size);
			//payload.readBytes(size);
			break;
		}
	}

	@SuppressWarnings("unused")
	private void decodeCCcamNewCard(CCcamSession session, ByteBuf payload) {
		int length = payload.readableBytes();
		int shareId = payload.readInt();
		int nodeId = payload.readInt();
		int cardId = payload.readShort();
		int hopCount = payload.readByte();
		int reshare = payload.readByte();
		long serial = payload.readLong();
		int providersLen = payload.readByte();
		
		int[] providers = new int[providersLen];
		for(int i = 0; i < providersLen; i++){
			int providerId = NetUtils.readTriByte(payload);
			providers[i] = providerId;
			payload.readInt();
		}
		int nodeCount = payload.readByte();
		for(int i = 0; i < nodeCount; i++){
			long nodeShareId = payload.readLong();
		}
		
		Card card = new Card(cardId, shareId, nodeId, providers, hopCount, reshare);
		session.addCard(card);
		logger.info("Decoded new caid: 0x"+Integer.toHexString(cardId)+" "+length+" "+nodeCount+" "+session.getCountryCode());
	}

	@SuppressWarnings("unused")
	private void decodeCCcamEcm(CCcamSession session, ByteBuf payload, int size) {
		
		//This value needs to be used instead of getLastRequest
		int ecmIdx = session.getEcmIdx();
		if(session.isReader()){
			byte[] dcw = new byte[16];
			payload.readBytes(dcw);
			
			CCcamCipher.cc_crypt_cw(
					session.getServer().getNodeId(), 
					session.getLastRequest().getShareId(), 
					dcw);	
			
			session.getDecrypter().decrypt( Arrays.copyOf(dcw,16), 16);
			
			logger.debug("dcw dump: "+NetUtils.bytesToString(dcw,0,dcw.length));
			
			if(!CardServer.getInstance().handleEcmAnswer(session,session.getLastRequest().getCspHash(), dcw, -1, -1)){
				//answer was not handled. No entry existed in any cache.
			}
			return;
		}
		final int CCCAM_ECM_HEADER_LENGTH = 13;
		//REQUEST ECM
		int cardId = payload.readShort();
		int provider = payload.readInt();
		//shareId
		int shareId = payload.readInt();
		int serviceId = payload.readShort();
		int ecmLength = payload.readByte() & 0xFF;
		byte[] ecm = new byte[size - CCCAM_ECM_HEADER_LENGTH];
		payload.readBytes(ecm);
		
		long cspHash = EcmRequest.computeEcmHash(ecm);
		//logger.info("requested client ECM: "+cspHash);
		CardServer.getInstance().handleClientEcmRequest(session, cardId, provider, shareId, serviceId, ecm);

	}

	public static int cspHashSwap(long n){
		return (int) (((n & 0xFFL) << 24L) | ((n & 0xFF00L) << 8L) | ((n & 0xFF0000L) >> 8L) | ((n & 0xFF000000L) >> 24L));
	}
	
	DescriptiveStatistics stats = new DescriptiveStatistics();

	private void decodeCCcamCachePush(CCcamSession session, ByteBuf payload) {
		int cardId = payload.readShort();
		int provider = payload.readInt();
		payload.readInt();
		int serviceId = payload.readShort();
		int ecmSize = payload.readShort();
		int returnCode = payload.readByte();
		/*
ECM rc codes:
#define E_FOUND         0
#define E_CACHE1        1
#define E_CACHE2        2
#define E_CACHEEX       3
		 */
		payload.readByte();
		payload.readByte();
		payload.readByte();
		int cycleTime = payload.readByte(); 
		int ecm0 = payload.readByte();//this is byte 20
		
		byte[] ecmMD5 = new byte[16];
		payload.readBytes(ecmMD5);
		int cspHash = payload.readInt();
		
		byte[] cw = new byte[16];
		payload.readBytes(cw);
		int nodeCount = payload.readByte()-1;
		long nodeId = payload.readLong();
		for(int i = 0; i < nodeCount; i++){
			long cacheNodeId = payload.readLong();
		}
		
		testCW(cw);
		//logger.debug(Integer.toHexString(cardId)+":"+Integer.toHexString(serviceId));
		
		if(!CardServer.getInstance().handleEcmAnswer(session, cspHash, cw, cardId, serviceId)){
			//answer was not handled. No entry existed in any cache.
			EcmRequest req = CardServer.getInstance().createEcmRequest(session, cardId, provider, (int) nodeId, serviceId,  new byte[1], cspHash, true);
			req.setDcw(cw);
			req.setEcmLength(ecmSize);
			req.setEcmMD5(ecmMD5);
			req.setCacheNodeCount(nodeCount);
			CardServer.getInstance().getCache().addEntry(req.getCspHash(), req);
		}
		
		
	}

	/**
	 * @param cw
	 */
	private void testCW(byte[] cw) {
		boolean test1 = false;
		boolean test2 = false;
		for(int i = 0; i < 8; i++){
			if(cw[i] != 0)
				test1 = true;
			if(cw[8+i] != 0)
				test2 = true;
		}
		boolean failed = (test1 && test2) || (!test1 && !test2);
		//if(failed)
		//	logger.info("Bad cache push recieved "+NetUtils.bytesToString(cw, 0, 16));
	}
	
}
