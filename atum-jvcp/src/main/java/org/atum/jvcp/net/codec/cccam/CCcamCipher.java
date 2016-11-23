package org.atum.jvcp.net.codec.cccam;

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

}
