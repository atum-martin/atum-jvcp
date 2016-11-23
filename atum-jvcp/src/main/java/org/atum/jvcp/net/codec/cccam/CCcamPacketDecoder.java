package org.atum.jvcp.net.codec.cccam;

import java.util.List;

import org.apache.log4j.Logger;
import org.atum.jvcp.net.NetworkConstants;
import org.atum.jvcp.net.codec.NetUtils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

public class CCcamPacketDecoder extends ByteToMessageDecoder {

	private Logger logger = Logger.getLogger(CCcamPacketDecoder.class);
	
	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		//cc_crypt(&cc->block[DECRYPT], buf, 4, DECRYPT);
		CCcamSession session = ctx.channel().attr(NetworkConstants.CCCAM_SESSION).get();
		byte[] command = new byte[4];
		NetUtils.readBuffer(in, command, command.length);
		session.getDecrypter().decrypt(command, command.length);
		
		int cmdCode = command[1];
		int size = (command[2] << 8) + command[3];
		
		logger.info("packet recieved: "+cmdCode+" "+size);
		
		byte[] payload = new byte[size];
		NetUtils.readBuffer(in, payload, payload.length);
		session.getDecrypter().decrypt(payload, payload.length);
		
		handlePacket(session, cmdCode, Unpooled.copiedBuffer(payload));
		
	}

	private void handlePacket(CCcamSession session, int cmdCode, ByteBuf payload) {
		switch (cmdCode) {
		case CCcamConstants.MSG_CLI_DATA:
			String username = NetUtils.readCCcamString(payload,20);
			
			long nodeId = payload.readLong();
			@SuppressWarnings("unused")
			int flag = payload.readByte();
			String version = NetUtils.readCCcamString(payload,32);
			String build = NetUtils.readCCcamString(payload,32);
			logger.info("MSG_CLI_DATA: "+version+" "+build+" "+username+" "+nodeId);
			break;
		case CCcamConstants.MSG_SRV_DATA:
			nodeId = payload.readLong();
			version = NetUtils.readCCcamString(payload,32);
			build = NetUtils.readCCcamString(payload,32);
			logger.info("MSG_SRV_DATA: "+version+" "+build+" "+nodeId);
			break;
		
		default:
			logger.info("unhandled packet: "+cmdCode+" "+payload.capacity());
			break;
		}
	}


}
