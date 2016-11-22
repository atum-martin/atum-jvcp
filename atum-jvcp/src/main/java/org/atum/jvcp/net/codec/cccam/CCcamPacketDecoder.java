package org.atum.jvcp.net.codec.cccam;

import java.util.List;

import org.apache.log4j.Logger;
import org.atum.jvcp.net.NetworkConstants;
import org.atum.jvcp.net.codec.NetUtils;

import io.netty.buffer.ByteBuf;
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
		
	}


}
