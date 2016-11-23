package org.atum.jvcp.net.codec.cccam;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import org.atum.jvcp.model.Card;
import org.atum.jvcp.model.Provider;
import org.atum.jvcp.net.codec.NetUtils;

public class CCcamPacketSender {

	private CCcamSession session;
	
	public CCcamPacketSender(CCcamSession session){
		this.session = session;
	}
	
	public void writeCard(Card card){
		int size = 24+3*card.getProviders().length;
		ByteBuf out = Unpooled.buffer(size);
		//24+3*prov
		out.writeInt(card.getShare());
		out.writeByte(card.getHops());
		out.writeLong(card.getNodeId());
		out.writeShort(card.getCardId());
		out.writeByte(card.getProviders().length);
		out.writeLong(card.getSerial());
		for(Provider prov : card.getProviders()){
			NetUtils.putTriByte(out, prov.getProviderId());
		}
		session.write(out);
		
	}
	
}
