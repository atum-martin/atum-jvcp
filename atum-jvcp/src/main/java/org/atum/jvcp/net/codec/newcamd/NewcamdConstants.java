/**
 * 
 */
package org.atum.jvcp.net.codec.newcamd;

/**
 * Constants ripped from CSP newcamd constants by Bowman.
 * User: bowman
 * Date: Oct 9, 2005
 * Time: 5:22:44 AM
 */
public class NewcamdConstants {

	  public static final int CWS_NETMSGSIZE = 240;
	  public static final int CWS_FIRSTCMDNO = 0xE0;

	  public static final int EXT_OSD_MESSAGE = 0xD1;
	  public static final int EXT_ADD_CARD = 0xD3;
	  public static final int EXT_REMOVE_CARD = 0xD4;
	  public static final int EXT_GET_VERSION = 0xD6;
	  public static final int EXT_SID_LIST = 0xD7;

	  public static final int
	      MSG_CLIENT_2_SERVER_LOGIN = CWS_FIRSTCMDNO,
	      MSG_CLIENT_2_SERVER_LOGIN_ACK = CWS_FIRSTCMDNO + 1,
	      MSG_CLIENT_2_SERVER_LOGIN_NAK = CWS_FIRSTCMDNO + 2,
	      MSG_CARD_DATA_REQ = CWS_FIRSTCMDNO + 3,
	      MSG_CARD_DATA = CWS_FIRSTCMDNO + 4,
	      MSG_SERVER_2_CLIENT_NAME = CWS_FIRSTCMDNO + 5,
	      MSG_SERVER_2_CLIENT_NAME_ACK = CWS_FIRSTCMDNO + 6,
	      MSG_SERVER_2_CLIENT_NAME_NAK = CWS_FIRSTCMDNO + 7,
	      MSG_SERVER_2_CLIENT_LOGIN = CWS_FIRSTCMDNO + 8,
	      MSG_SERVER_2_CLIENT_LOGIN_ACK = CWS_FIRSTCMDNO + 9,
	      MSG_SERVER_2_CLIENT_LOGIN_NAK = CWS_FIRSTCMDNO + 10,
	      MSG_ADMIN = CWS_FIRSTCMDNO + 11,
	      MSG_ADMIN_ACK = CWS_FIRSTCMDNO + 12,
	      MSG_ADMIN_LOGIN = CWS_FIRSTCMDNO + 13,
	      MSG_ADMIN_LOGIN_ACK = CWS_FIRSTCMDNO + 14,
	      MSG_ADMIN_LOGIN_NAK = CWS_FIRSTCMDNO + 15,
	      MSG_ADMIN_COMMAND = CWS_FIRSTCMDNO + 16,
	      MSG_ADMIN_COMMAND_ACK = CWS_FIRSTCMDNO + 17,
	      MSG_ADMIN_COMMAND_NAK = CWS_FIRSTCMDNO + 18,
	      MSG_KEEPALIVE = CWS_FIRSTCMDNO + 0x1D;
	  
	public static final int MSG_SERVER_2_CLIENT_ECM = 0x80;
}
