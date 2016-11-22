package org.atum.jvcp.net.codec;

import io.netty.buffer.ByteBuf;

public class NetUtils {

	public static void readBuffer(ByteBuf buffer, byte[] buf, int len) {
		for (int i = 0; i < len; i++) {
			buf[i] = buffer.readByte();
		}
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
