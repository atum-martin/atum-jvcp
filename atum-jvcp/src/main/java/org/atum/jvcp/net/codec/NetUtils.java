package org.atum.jvcp.net.codec;

import io.netty.buffer.ByteBuf;

/**
 * @author <a href="https://github.com/atum-martin">atum-martin</a>
 * @since 22 Nov 2016 22:23:11
 */

public class NetUtils {
	
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

	public static byte[] getBytesValue(String byteStr) {
		String[] parts = byteStr.split(" ");
		byte[] bytes = new byte[parts.length];
		for (int i = 0; i < parts.length; i++) {
			if (parts[i].length() != 2)
				throw new NumberFormatException("Bad byte value '" + parts[i] + "' in '" + i + "' (must be 2 digit hex value)");
			try {
				int tmp = Integer.parseInt(parts[i], 16);
				if (tmp < 0 || tmp > 255)
					throw new NumberFormatException();
				bytes[i] = (byte) (tmp & 0xFF);
			} catch (NumberFormatException e) {
				throw new NumberFormatException("Bad byte value '" + parts[i] + "' in '" + i + "' (must be 00-FF)");
			}
		}
		return bytes;
	}

	public static int readTriByte(ByteBuf payload) {
		return (payload.readByte() << 16 | payload.readByte() << 8 | payload.readByte() & 0xFF);
	}
}
