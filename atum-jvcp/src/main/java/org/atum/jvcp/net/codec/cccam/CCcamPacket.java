package org.atum.jvcp.net.codec.cccam;

import io.netty.buffer.ByteBuf;

/**
 * @author <a href="https://github.com/atum-martin">atum-martin</a>
 * @since 24 Nov 2016 22:40:08
 */

public class CCcamPacket {

	private int command;
	private ByteBuf out;

	public CCcamPacket(int command, ByteBuf out) {
		this.command = command;
		this.out = out;
	}

	public ByteBuf getOut() {
		return out;
	}

	public int getCommand() {
		return command;
	}

}
