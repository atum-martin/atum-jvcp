package org.atum.jvcp.net.codec.cccam.io;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

import org.apache.log4j.Logger;
import org.atum.jvcp.model.Card;
import org.atum.jvcp.model.EcmRequest;
import org.atum.jvcp.model.PacketSenderInterface;
import org.atum.jvcp.model.Provider;
import org.atum.jvcp.net.codec.NetUtils;
import org.atum.jvcp.net.codec.cccam.CCcamBuilds;
import org.atum.jvcp.net.codec.cccam.CCcamConstants;
import org.atum.jvcp.net.codec.cccam.CCcamPacket;
import org.atum.jvcp.net.codec.cccam.CCcamSession;
import org.atum.jvcp.net.codec.cccam.CCcamBuilds.CCcamBuild;
import org.atum.jvcp.net.codec.cccam.CCcamCipher;

/**
 * @author <a href="https://github.com/atum-martin">atum-martin</a>
 * @since 23 Nov 2016 00:05:46
 */

public class CCcamPacketSender implements PacketSenderInterface {

	private CCcamSession session;
	private Logger logger = Logger.getLogger(CCcamSession.class);

	public CCcamPacketSender(CCcamSession session) {
		this.session = session;
	}

	public void writeCard(Card card) {
		int size = 22 + (7 * card.getProviders().length) + ( 8 * 1/*card.getNodeCount()*/);
		ByteBuf out = Unpooled.buffer(size);
		out.writeInt(card.getShare());
		out.writeInt((int) card.getNodeId());
		out.writeShort(card.getCardId());
		out.writeByte(card.getHops());
		out.writeByte(card.getReshare());
		out.writeLong(card.getSerial());
		out.writeByte(card.getProviders().length);
		for (Provider prov : card.getProviders()) {
			NetUtils.putTriByte(out, prov.getProviderId());
			out.writeInt(0);
		}
		//node count
		out.writeByte(1);
		out.writeLong(card.getNodeId());
		session.write(new CCcamPacket(CCcamConstants.MSG_NEW_CARD, out));

	}

	public void writeSrvData() {
		ByteBuf out = Unpooled.buffer(72);
		out.writeBytes(session.getServer().getNodeId());
		CCcamBuild build = CCcamBuilds.getBuild("2.0.11");
		NetUtils.writeCCcamStr(out, build.getVersion(), 32);
		NetUtils.writeCCcamStr(out, "" + build.getBuildNum(), 32);
		session.write(new CCcamPacket(CCcamConstants.MSG_SRV_DATA, out));
		logger.info("MSG_SRV_DATA: " + build.getVersion() + " " + build.getBuildNum());

		session.write(new CCcamPacket(CCcamConstants.MSG_CACHE_FILTER, Unpooled.buffer(482)));
	}

	public void writeCliData() {
		// session.write(new CCcamPacket(CCcamConstants.MSG_CLI_DATA,null));

		ByteBuf out = Unpooled.buffer(93);
		NetUtils.writeCCcamStr(out, "user99", 20);
		out.writeBytes(session.getServer().getNodeId());
		out.writeByte(0);
		CCcamBuild build = CCcamBuilds.getBuild("2.0.11");
		NetUtils.writeCCcamStr(out, build.getVersion(), 32);
		NetUtils.writeCCcamStr(out, "" + build.getBuildNum(), 32);
		logger.info("sending MSG_CLI_DATA: " + build.getVersion() + " " + build.getBuildNum());
		session.write(new CCcamPacket(CCcamConstants.MSG_CLI_DATA, out));

	}

	public void writeKeepAlive() {
		session.write(new CCcamPacket(CCcamConstants.MSG_KEEPALIVE, null));
	}

	public void writeEcmRequest(EcmRequest req) {
		ByteBuf out = Unpooled.buffer(req.getEcm().length + 13);
		out.writeShort(req.getCardId());
		out.writeInt(req.getProv().getProviderId());
		out.writeInt(req.getShareId());
		out.writeShort(req.getServiceId());
		out.writeByte(req.getEcm().length);
		out.writeBytes(req.getEcm());
		session.setLastRequest(req);
		session.write(new CCcamPacket(CCcamConstants.MSG_CW_ECM, out));
	}

	public void writeEcmAnswer(byte[] dcw) {
		final byte[] sendDcw = new byte[dcw.length];
		System.arraycopy(dcw, 0, sendDcw, 0, dcw.length);
		ByteBuf out = Unpooled.buffer(16);
		CCcamCipher.cc_crypt_cw(
				session.getNodeId(), 
				session.getLastRequest().getShareId(), 
				sendDcw);	
		out.writeBytes(sendDcw);		
		
		session.write(new CCcamPacket(CCcamConstants.MSG_CW_ECM, out)).addListener(new ChannelFutureListener() {
			public void operationComplete(ChannelFuture future) throws Exception {
				session.getEncrypter().encrypt(sendDcw, 16);
			}
		});
	}

}
