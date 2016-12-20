/**
 * 
 */
package org.atum.jvcp.net.codec.newcamd;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * @author <a href="https://github.com/atum-martin">atum-martin</a>
 * @since 20 Dec 2016 21:53:06
 */
public class NewcamdPacket {
	
	private int command;
	private ByteBuf headers = Unpooled.buffer(10);
	private ByteBuf payload = null;

	public NewcamdPacket(int command) {
		this.command = command;
	}

	public void setHeader(int index,int value){
		headers.setByte(index, value);
	}
	
	public void setPayload(ByteBuf payload){
		this.payload = payload;
	}
	
	public ByteBuf getPayload(){
		return payload;
	}

	public int getCommand() {
		return command;
	}
}
