package org.atum.jvcp.net.codec.cccam;

public class CCcamConstants {

	//Taken from Oscam CCcam implementation.s
	public static final int MSG_CLI_DATA = 0,
			MSG_CW_ECM = 1,
			MSG_EMM_ACK = 2,
			MSG_CARD_REMOVED = 4,
			MSG_CMD_05 = 5,
			MSG_KEEPALIVE = 6,
			MSG_NEW_CARD = 7,
			MSG_SRV_DATA = 8,
			MSG_CMD_0A = 0x0a,
			MSG_CMD_0B = 0x0b,
			MSG_CMD_0C = 0x0c, // CCCam 2.2.x fake client checks
			MSG_CMD_0D = 0x0d, // "
			MSG_CMD_0E = 0x0e, // "
			MSG_NEW_CARD_SIDINFO = 0x0f,
			MSG_SLEEPSEND = 0x80, //Sleepsend support
			MSG_CACHE_PUSH = 0x81, //CacheEx Cache-Push In/Out
			MSG_CACHE_FILTER = 0x82, //CacheEx Cache-Filter Request
			MSG_CW_NOK1 = 0xfe, //Node no more available
			MSG_CW_NOK2 = 0xff, //No decoding
			MSG_NO_HEADER = 0xffff;
}
