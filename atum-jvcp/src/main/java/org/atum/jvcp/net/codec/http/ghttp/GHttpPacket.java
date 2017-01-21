/**
 * 
 */
package org.atum.jvcp.net.codec.http.ghttp;

import org.atum.jvcp.net.codec.Packet;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * @author <a href="https://github.com/atum-martin">atum-martin</a>
 * @since 21 Jan 2017
 */
public class GHttpPacket implements Packet {
	
	ByteBuf buf;
	
	public GHttpPacket(byte[] buf){
		this.buf = Unpooled.copiedBuffer(buf);
	}
}
