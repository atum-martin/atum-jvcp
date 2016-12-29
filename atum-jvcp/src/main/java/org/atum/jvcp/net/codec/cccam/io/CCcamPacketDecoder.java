package org.atum.jvcp.net.codec.cccam.io;

import java.util.List;

import org.apache.log4j.Logger;
import org.atum.jvcp.CardServer;
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
			command.readByte();
			int cmdCode = command.readByte() & 0xFF;
			int size = command.readShort();

			//logger.info("packet recieved: " + cmdCode + " " + size);
			session.setCurrentPacket(cmdCode, size);

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
			nodeId = new byte[8];
			payload.readBytes(nodeId);
			session.setNodeId(nodeId);
			version = NetUtils.readCCcamString(payload, 32);
			build = Integer.parseInt(NetUtils.readCCcamString(payload, 32));
			logger.info("MSG_SRV_DATA: " + version + " " + build + " " + nodeId);
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
		int providers = payload.readByte();
		for(int i = 0; i < providers; i++){
			int providerId = NetUtils.readTriByte(payload);
			payload.readInt();
		}
		int nodeCount = payload.readByte();
		for(int i = 0; i < nodeCount; i++){
			long nodeShareId = payload.readLong();
		}
		
		logger.info("Decoded new caid: 0x"+Integer.toHexString(cardId)+" "+length+" "+providers+" "+nodeCount);
	}

	@SuppressWarnings("unused")
	private void decodeCCcamEcm(CCcamSession session, ByteBuf payload, int size) {
		if(session.isReader()){
			byte[] dcw = new byte[16];
			payload.readBytes(dcw);
			logger.info("Recieved DCW");
			
			logger.info("dcw dump: "+bytesToString(dcw,0,dcw.length));
			
			if(!CardServer.handleEcmAnswer(session.getLastRequest().getCspHash(), dcw, -1, -1)){
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
		//NetUtils.readBuffer(payload, ecm, ecm.length, 0);
		payload.readBytes(ecm);
		//payload.readBytes(ecm);
		logger.info("ecm hex dump: "+bytesToString(ecm,0,ecm.length));
		logger.debug("ecm req: "+Integer.toHexString(ecmLength)+" "+Integer.toHexString(size - CCCAM_ECM_HEADER_LENGTH)+" " +Integer.toHexString(cardId)+":"+Integer.toHexString(serviceId));
		
		/*
		byte[] dcw = HashCache.getSingleton().readCache(cardId, serviceId, ecm);
		if(dcw != null){
			session.getPacketSender().writeEcmAnswer(dcw);
		}
		HashCache.getSingleton().addListener(cardId, serviceId, ecm, session);*/
		long cspHash = EcmRequest.computeEcmHash(ecm);
		logger.info("requested client ECM: "+cspHash);
		EcmRequest answer = CardServer.handleEcmRequest(session, cardId, provider, shareId, serviceId, ecm);
		if(answer != null && answer.hasAnswer()){
			logger.info("handled client ECM: "+answer.getCspHash());
			logger.info("dcw dump: "+bytesToString(answer.getDcw(),0,answer.getDcw().length));
			session.getPacketSender().writeEcmAnswer(answer.getDcw());
		}
	}
	
	  public static String bytesToString(byte[] bytes, int offs, int len) {
		    StringBuffer sb = new StringBuffer();
		    String bt;
		    if(len > bytes.length) len = bytes.length;
		    for(int i = 0; i < len && (i + offs < bytes.length); i++) {
		      bt = Integer.toHexString(bytes[offs + i] & 0xFF);
		      if(bt.length() == 1) sb.append('0');
		      sb.append(bt);
		      sb.append(' ');
		    }
		    return sb.toString().trim().toUpperCase();
		  }
	/*
	#define CSP_HASH_SWAP(n) (((((uint32_t)(n) & 0xFF)) << 24) | \
            ((((uint32_t)(n) & 0xFF00)) << 8) | \
            ((((uint32_t)(n) & 0xFF0000)) >> 8) | \
            ((((uint32_t)(n) & 0xFF000000)) >> 24))
	*/
	public static int cspHashSwap(long n){
		return (int) (((n & 0xFFL) << 24L) | ((n & 0xFF00L) << 8L) | ((n & 0xFF0000L) >> 8L) | ((n & 0xFF000000L) >> 24L));
	}

	@SuppressWarnings("unused")
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
		
		//logger.debug(Integer.toHexString(cardId)+":"+Integer.toHexString(serviceId));
		
		if(!CardServer.handleEcmAnswer(cspHash, cw, cardId, serviceId)){
			//answer was not handled. No entry existed in any cache.
			EcmRequest req = CardServer.createEcmRequest(cardId, provider, (int) nodeId, serviceId,  new byte[1], cspHash, true);
			req.setDcw(cw);
			CardServer.getCache().addEntry(req.getCspHash(), req);
		}
		//HashCache.getSingleton().pushCache(cardId, serviceId, ecmd5, cw);
		
		
	}

	public static long getUnsignedInt(int x) {
		return x;
	    //return x & 0x00000000ffffffffL;
	}
	
}
