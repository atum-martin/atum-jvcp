package org.atum.jvcp.net.codec.cccam;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import org.apache.log4j.Logger;
import org.atum.jvcp.model.Card;
import org.atum.jvcp.model.EcmRequest;
import org.atum.jvcp.model.Provider;
import org.atum.jvcp.net.codec.NetUtils;
import org.atum.jvcp.net.codec.cccam.CCcamBuilds.CCcamBuild;

public class CCcamPacketSender {

	private CCcamSession session;
	private Logger logger = Logger.getLogger(CCcamSession.class);
	
	public CCcamPacketSender(CCcamSession session){
		this.session = session;
	}
	
	public void writeCard(Card card){
		int size = 20+(3*card.getProviders().length);
		ByteBuf out = Unpooled.buffer(size);
		//24+3*prov
		out.writeInt(card.getShare());
		out.writeInt((int) card.getNodeId());	
		out.writeShort(card.getCardId());
		out.writeByte(card.getHops());
		out.writeByte(card.getReshare());
		out.writeLong(card.getSerial());
		out.writeByte(card.getProviders().length);
		for(Provider prov : card.getProviders()){
			NetUtils.putTriByte(out, prov.getProviderId());
		}
		session.write(new CCcamPacket(CCcamConstants.MSG_NEW_CARD,out));
		
	}
	
	public void writeSrvData(){
		ByteBuf out = Unpooled.buffer(72);
		out.writeLong(837493L);
		CCcamBuild build = CCcamBuilds.getBuild("2.5.0");
		NetUtils.writeCCcamStr(out,build.getVersion(),32);
		NetUtils.writeCCcamStr(out,""+build.getBuildNum(),32);
		session.write(new CCcamPacket(CCcamConstants.MSG_SRV_DATA,out));
		logger.info("MSG_SRV_DATA: "+build.getVersion()+" "+build.getBuildNum());
		
		session.write(new CCcamPacket(CCcamConstants.MSG_CACHE_FILTER,Unpooled.buffer(482)));
	}
	
	public void writeCliData(){
		session.write(new CCcamPacket(CCcamConstants.MSG_CLI_DATA,null));
	}
	
	public void writeKeepAlive(){
		session.write(new CCcamPacket(CCcamConstants.MSG_KEEPALIVE,null));
	}
	
	public void writeEcmRequest(EcmRequest req){
		ByteBuf out = Unpooled.buffer(req.getEcm().length+12);
		out.writeShort(req.getCardId());
		out.writeInt(req.getProv().getProviderId());
		out.writeInt(req.getShareId());
		out.writeShort(req.getServiceId());
		out.writeBytes(req.getEcm());
		
		session.write(new CCcamPacket(CCcamConstants.MSG_CW_ECM,out));
	}
	
	public void writeEcmAnswer(byte[] dcw){
		ByteBuf out = Unpooled.buffer(16);
		out.writeBytes(dcw);
		session.write(new CCcamPacket(CCcamConstants.MSG_CW_ECM,out));
	}
	
}
