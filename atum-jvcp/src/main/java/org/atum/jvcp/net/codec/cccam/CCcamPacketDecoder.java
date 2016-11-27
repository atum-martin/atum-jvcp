package org.atum.jvcp.net.codec.cccam;

import java.util.List;

import org.apache.log4j.Logger;
import org.atum.jvcp.net.NetworkConstants;
import org.atum.jvcp.net.codec.NetUtils;
import org.atum.jvcp.net.codec.cccam.CCcamBuilds.CCcamBuild;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

public class CCcamPacketDecoder extends ByteToMessageDecoder {

	private Logger logger = Logger.getLogger(CCcamPacketDecoder.class);
	private static int index = 0;
	private static long dataSent = 0L;
	private static long packetsRecieved = 0;

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		CCcamSession session = ctx.channel().attr(NetworkConstants.CCCAM_SESSION).get();
		synchronized (session) {
			packetsRecieved++;
			
			ByteBuf command = in.readBytes(4);

			session.getDecrypter().decrypt(command);

			command.readByte();
			int cmdCode = command.readByte() & 0xFF;
			int size = command.readShort();
			
			dataSent += 4;
			dataSent += size;

			logger.info("packet recieved: " + cmdCode + " " + size);
			if(cmdCode == 99){
				session.getDecrypter().printValues();
				logger.info("packet: " + packetsRecieved + " " + dataSent);
			}
			
			try {
				ByteBuf payload = in.readBytes(size);
				session.getDecrypter().decrypt(payload);
				handlePacket(session, cmdCode, size, payload);
			} catch (Exception e) {
				if (index++ < 1)
					e.printStackTrace();
				ctx.channel().close();
			}
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

		default:
			logger.info("unhandled packet: " + cmdCode + " " + size);
			//payload.readBytes(size);
			break;
		}
	}

}
