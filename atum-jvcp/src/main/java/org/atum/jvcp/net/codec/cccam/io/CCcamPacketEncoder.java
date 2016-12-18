package org.atum.jvcp.net.codec.cccam.io;

import org.apache.log4j.Logger;
import org.atum.jvcp.net.NetworkConstants;
import org.atum.jvcp.net.codec.cccam.CCcamPacket;
import org.atum.jvcp.net.codec.cccam.CCcamSession;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @author <a href="https://github.com/atum-martin">atum-martin</a>
 * @since 24 Nov 2016 22:40:08
 */

public class CCcamPacketEncoder extends MessageToByteEncoder<CCcamPacket> {

	@SuppressWarnings("unused")
	private Logger logger = Logger.getLogger(CCcamPacketEncoder.class);

	@Override
	protected void encode(ChannelHandlerContext ctx, CCcamPacket msg, ByteBuf out) throws Exception {
		CCcamSession session = (CCcamSession) ctx.channel().attr(NetworkConstants.CAM_SESSION).get();
		int packetLength = 0;
		if(msg.getOut() != null){
			packetLength = msg.getOut().readableBytes();
		}
		ByteBuf unencBuf = Unpooled.buffer(4+packetLength);
		unencBuf.writeByte(0);
		unencBuf.writeByte(msg.getCommand());
		unencBuf.writeShort(packetLength);
		//unencBuf.writeByte(packetLength >> 8);
		//unencBuf.writeByte(packetLength & 0xFF);
		if(packetLength != 0)
			unencBuf.writeBytes(msg.getOut());
		session.getEncrypter().encrypt(unencBuf);
		out.writeBytes(unencBuf);
		unencBuf.release();
	}



}
