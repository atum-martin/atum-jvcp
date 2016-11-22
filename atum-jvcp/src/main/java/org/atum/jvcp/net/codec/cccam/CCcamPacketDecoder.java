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
	
	//Taken from Oscam CCcam implementation.
	@SuppressWarnings("unused")
	private static final int MSG_CLI_DATA = 0,
			MSG_CW_ECM = 1,
			MSG_EMM_ACK = 2,
			MSG_CARD_REMOVED = 4,
			MSG_CMD_05 = 5,
			MSG_KEEPALIVE = 6,
			MSG_NEW_CARD = 7,
			MSG_SRV_DATA = 8,
			MSG_CMD_0A = 0x0a,
			MSG_CMD_0B = 0x0b,
			MSG_CMD_0C = 0x0c, // CCCam 2.2.x fake client checks
			MSG_CMD_0D = 0x0d, // "
			MSG_CMD_0E = 0x0e, // "
			MSG_NEW_CARD_SIDINFO = 0x0f,
			MSG_SLEEPSEND = 0x80, //Sleepsend support
			MSG_CACHE_PUSH = 0x81, //CacheEx Cache-Push In/Out
			MSG_CACHE_FILTER = 0x82, //CacheEx Cache-Filter Request
			MSG_CW_NOK1 = 0xfe, //Node no more available
			MSG_CW_NOK2 = 0xff, //No decoding
			MSG_NO_HEADER = 0xffff;
	
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
		case MSG_CLI_DATA:
			String username = NetUtils.readCCcamString(payload,20);
			
			long nodeId = payload.readLong();
			int flag = payload.readByte();
			String version = NetUtils.readCCcamString(payload,32);
			String build = NetUtils.readCCcamString(payload,32);
			logger.info("CLI data: "+version+" "+build+" "+username+" "+nodeId);
			break;
		default:
			logger.info("unhandled packet: "+cmdCode+" "+payload.capacity());
			break;
		}
	}


}
