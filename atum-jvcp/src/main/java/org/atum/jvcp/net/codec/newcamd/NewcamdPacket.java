/**
 * 
 */
package org.atum.jvcp.net.codec.newcamd;

import org.atum.jvcp.net.codec.Packet;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * @author <a href="https://github.com/atum-martin">atum-martin</a>
 * @since 20 Dec 2016 21:53:06
 */
public class NewcamdPacket implements Packet {
	
	private int command;
	private ByteBuf headers;
	private ByteBuf payload = null;
	private transient int strReaderIndex = 0;

	public NewcamdPacket(int command) {
		this.command = command;
		this.headers = Unpooled.buffer(10);
		headers.writeBytes(new byte[10]);
	}

	/**
	 * @param command
	 * @param headers
	 */
	public NewcamdPacket(int command, ByteBuf headers) {
		this.command = command;
		this.headers = headers;
	}

	public void setHeader(int index,int value){
		headers.setByte(index, value);
	}
	
	public void setHeaderShort(int index,int value){
		headers.setShort(index, value);
	}
	
	public void setPayload(ByteBuf payload){
		this.payload = payload;
		payload.resetReaderIndex();
	}
	
	public ByteBuf getPayload(){
		return payload;
	}

	public int getCommand() {
		return command;
	}
	
	public boolean isEcm(){
		return command == 0x80 || command == 0x81;
	}
	
	public boolean isEmm(){
		return command >= 0x82 && command <= 0x8F;
	}
	
	public boolean isDcw(){
		return isEcm() && (payload == null || payload.capacity() == 16);
	}
	
	public String readStr(){
		StringBuilder build = new StringBuilder();
		for(; strReaderIndex < payload.capacity(); strReaderIndex++){
			char c = (char) payload.getByte(strReaderIndex);
			if(c == 0){
				strReaderIndex++;
				return build.toString();
			}
			build.append(c);
		}
		return build.toString();
	}

	/**
	 * @return
	 */
	public int getSize() {
		return payload == null ? 0 : payload.capacity();
	}

	public ByteBuf getHeaders() {
		return headers;
	}
}
