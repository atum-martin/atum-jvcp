package org.atum.jvcp.net.codec.cccam;

import org.apache.log4j.Logger;
import org.atum.jvcp.net.NetworkConstants;
import org.atum.jvcp.net.codec.NetUtils;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class CCcamPacketEncoder extends MessageToByteEncoder<CCcamPacket> {

	private Logger logger = Logger.getLogger(CCcamPacketEncoder.class);

	@Override
	protected void encode(ChannelHandlerContext ctx, CCcamPacket msg, ByteBuf out) throws Exception {
		CCcamSession session = ctx.channel().attr(NetworkConstants.CCCAM_SESSION).get();
		int packetLength = 0;
		if(msg.getOut() != null){
			packetLength = msg.getOut().readableBytes();
		}
		byte[] buf = new byte[4+packetLength];
		logger.info("Encoding packet: "+msg.getCommand()+" "+buf.length);
		buf[0] = 0;
		buf[1] = (byte) msg.getCommand();
		buf[2] = (byte) (packetLength >> 8);
		buf[3] = (byte) (packetLength & 0xFF);
		if(packetLength != 0)
			NetUtils.readBuffer(msg.getOut(), buf, packetLength, 4);
		session.getEncrypter().encrypt(buf, buf.length);
		out.writeBytes(buf);
		
	}



}
