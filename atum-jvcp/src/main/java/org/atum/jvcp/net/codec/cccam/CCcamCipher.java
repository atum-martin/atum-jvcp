package org.atum.jvcp.net.codec.cccam;

import io.netty.buffer.ByteBuf;

/**
 * A class which represents encrypting and decrypting of a CCcam payload. Based
 * on the work of the Oscam team.
 * 
 * @since: 22/11/2016
 * @author atum-martin
 *
 */
public class CCcamCipher {

	private int state = 0;
	private int sum = 0;
	private int counter = 0;
	private int[] keytable = new int[256];

	enum Mode {
		DECRYPT, ENCRYPT;
	}

	public void CipherInit(byte[] key, int len) {
		int i;
		for (i = 0; i < 256; i++) {
			keytable[i] = i;
		}
		int j = 0;
		for (i = 0; i < 256; i++) {
			j = (j + key[i % len] + keytable[i]) & 0xff;
			int temp = keytable[i];
			keytable[i] = keytable[j];
			keytable[j] = temp;
		}

		state = key[0];
		counter = 0;
		sum = 0;
	}

	public static void ccCamXOR(byte[] data) {
		byte[] cccam = "CCcam".getBytes();
		for (byte i = 0; i < 8; i++) {
			data[8 + i] = (byte) (i * data[i]);
			if (i < 5)
				data[i] ^= cccam[i];
		}
	}

	public void decrypt(byte[] data, int len) {
		cipher(data, len, Mode.DECRYPT);
	}

	public void encrypt(byte[] data, int len) {
		cipher(data, len, Mode.ENCRYPT);
	}

	public void decrypt(ByteBuf in) {
		cipher(in, Mode.DECRYPT);
	}

	public void encrypt(ByteBuf in) {
		cipher(in, Mode.ENCRYPT);
	}

	public void cipher(byte[] data, int len, Mode mode) {
		for (int i = 0; i < len; i++) {
			counter = ++counter & 0xFF;
			sum = (sum + keytable[counter]) & 0xFF;

			byte temp = (byte) keytable[counter];
			keytable[counter] = keytable[sum];
			keytable[sum] = temp;

			byte z = data[i];
			data[i] = (byte) (z ^ keytable[keytable[counter & 0xFF] + keytable[sum] & 0xFF] ^ state);
			if (mode == Mode.DECRYPT)
				z = data[i];
			state = (state ^ z) & 0xFF;
		}
	}

	public void cipher(ByteBuf in, Mode mode) {
		int length = in.readableBytes();
		for (int i = 0; i < length; i++) {
			counter = ++counter & 0xFF;
			sum = (sum + keytable[counter]) & 0xFF;

			byte temp = (byte) keytable[counter];
			keytable[counter] = keytable[sum];
			keytable[sum] = temp;

			byte z = in.readByte();
			byte write = (byte) (z ^ keytable[keytable[counter & 0xFF] + keytable[sum] & 0xFF] ^ state);
			in.setByte(i, write);
			if (mode == Mode.DECRYPT)
				z = write;

			state = (state ^ z) & 0xFF;
		}
		in.resetReaderIndex();
		// in.resetWriterIndex();
	}

	/**
	 * Encrypts CW sent in an ECM answer, ripped from CCcam CSP implementation.
	 * 
	 * @param nodeid
	 * @param shareid
	 * @param cws
	 */
	///////////////////////////////////////////////////////////////////////////////
	// node_id : client nodeid, the sender of the ECM Request(big endian)
	// card_id : local card_id for the server
	public static void cc_crypt_cw(byte[] nodeid, int shareId, byte[] cws) {
		byte tmp;
		byte i;
		byte n;
		int cardId = shareId;
		byte[] nod = new byte[8];
		// int card_id = (shareid[0] << 24) | (shareid[1] << 16) | (shareid[2]
		// << 8) | (shareid[3]);
		for (i = 0; i < 8; i++)
			nod[i] = nodeid[7-i];
		for (i = 0; i < 16; i++) {
			if (i % 2 == 1) {
				if (i != 15)
					n = (byte) (((nod[i >> 1] >> 4) & 0x0f) | ((nod[(i >> 1) + 1] << 4) & 0xf0));
				else
					n = (byte) ((nod[i >> 1] >> 4) & 0x0f);
			} else
				n = (byte) (nod[i >> 1]);
			tmp = (byte) (cws[i] ^ n);
			if (i % 2 == 1)
				tmp ^= -1;
			cws[i] = (byte) ((cardId >> (2 * i)) ^ tmp & 0xff);
		}
	}
}
