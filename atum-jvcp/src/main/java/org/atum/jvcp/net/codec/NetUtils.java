package org.atum.jvcp.net.codec;

import io.netty.buffer.ByteBuf;

public class NetUtils {

	public static void readBuffer(ByteBuf buffer, byte[] buf, int len) {
		readBuffer(buffer,buf,len,0);
	}

	public static void readBuffer(ByteBuf buffer, byte[] buf, int len, int offset) {
		for (int i = 0; i < len; i++) {
			buf[i+offset] = buffer.readByte();
		}
	}
	
	public static String readCCcamString(ByteBuf buffer, int len) {
		byte[] buf = new byte[len];
		for (int i = 0; i < len; i++) {
			buf[i] = buffer.readByte();
		}
		return toCCcamString(buf);
	}
	
	public static void putTriByte(ByteBuf buffer, int val) {
		buffer.writeByte(val >> 16);
		buffer.writeByte(val >> 8);
		buffer.writeByte(val & 0xFF);
	}
	
	public static void writeCCcamStr(ByteBuf buffer,String val,int len){
		buffer.writeBytes(val.getBytes());
		if(val.length() != len)
			buffer.writeBytes(new byte[len-val.length()]);
	}
	
	public static String toCCcamString(byte[] arr) {
		int len = findVal(arr, 0);
		byte[] newStr = new byte[len];
		System.arraycopy(arr, 0, newStr, 0, len);
		return new String(newStr);
	}

	private static int findVal(byte[] arr, int val) {
		for (int i = 0; i < arr.length; i++) {
			if (arr[i] == val) {
				return i;
			}
		}
		return arr.length;
	}
}
