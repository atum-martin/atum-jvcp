package org.atum.jvcp.net.codec.newcamd.io;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import org.apache.log4j.Logger;
import org.atum.jvcp.NewcamdServer;
import org.atum.jvcp.crypto.DESUtil;
import org.atum.jvcp.net.LoginDecoder;
import org.atum.jvcp.net.codec.LoginState;
import org.atum.jvcp.net.codec.newcamd.NewcamdClient;
import org.atum.jvcp.net.codec.newcamd.NewcamdPacket;

import static org.atum.jvcp.net.NetworkConstants.*;
import static org.atum.jvcp.net.codec.newcamd.NewcamdConstants.*;

/**
 * @author <a href="https://github.com/atum-martin">atum-martin</a>
 * @since 15 Dec 2016 23:51:26
 */
public class NewcamdClientLoginDecoder extends LoginDecoder {

	private Logger logger = Logger.getLogger(NewcamdClientLoginDecoder.class);
	private int packetDecodeFailedCounter = 0;
	
	public NewcamdClientLoginDecoder(NewcamdServer newcamdServer) {
		super(newcamdServer);
	}

	@Override
	protected void decode(ChannelHandlerContext context, ByteBuf buffer, List<Object> outStream) throws Exception {
		LoginState state = context.channel().attr(LOGIN_STATE).get();
		switch (state) {

		case ENCRYPTION:
			handleCrypto(context, buffer);
			break;
		case HANDSHAKE:
			handleHandshake(context, buffer);
			break;
		default:
			throw new IllegalStateException("Invalid state during login decoding.");
		}
	}

	public void init(ChannelHandlerContext context) {

	}

	private void handleCrypto(ChannelHandlerContext context, ByteBuf buffer) {
		if (buffer.readableBytes() < 14) {
			logger.debug("less than 14 bytes in crypto buffer");
			return;
		}
		ByteBuf random14 = buffer.readBytes(14);
		NewcamdClient client = (NewcamdClient) context.channel().attr(CAM_SESSION).get();
		byte[] desKey16 = DESUtil.desKeySpread((DESUtil.xorKey(client.getDesKey(), random14))); // loginKey
		client.setDesKey(desKey16);
		client.setPacketSender(new NewcamdPacketSender(client));
		
		context.channel().pipeline().addLast("packet-encoder", new NewcamdPacketEncoder());
		
		client.write(NewcamdPacketSender.createLoginPacket(client));
		
		context.channel().attr(LOGIN_STATE).set(LoginState.HANDSHAKE);
	}

	private void handleHandshake(ChannelHandlerContext context, ByteBuf buffer) {
		NewcamdClient client = (NewcamdClient) context.channel().attr(CAM_SESSION).get();
		NewcamdPacket packet = NewcamdPacketDecoder.parseBuffer(context, client, buffer);
		
		if(packet == null && packetDecodeFailedCounter++ > 2){
			context.channel().close();
			return;
		}
		
		if(packet.getCommand() != MSG_CLIENT_2_SERVER_LOGIN_ACK){
			logger.info("login failed for newcamd session: "+client);
			context.channel().close();
			return;
		}
		
		packet.getPayload().release();
		logger.info("login succeded for newcamd session: "+client);
		
		camServer.registerSession(client);
		client.setDesKey(DESUtil.desKeySpread(DESUtil.xorUserPass(client.getFirstDesKey(), DESUtil.cryptPassword(client.getAccount().getPassword()))));
		context.channel().pipeline().replace("login-header-decoder", "packet-decoder", new NewcamdPacketDecoder());
		
		client.write(NewcamdPacketSender.createCardDataReq());
	}

}
