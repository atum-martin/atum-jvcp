package org.atum.jvcp.net.codec;

import io.netty.buffer.ByteBuf;

public class NetUtils {

	public static void readBuffer(ByteBuf buffer, byte[] buf, int len) {
		for (int i = 0; i < len; i++) {
			buf[i] = buffer.readByte();
		}
	}
}
