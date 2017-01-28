package org.atum.jvcp.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.atum.jvcp.CardServer;
import org.atum.jvcp.net.codec.cccam.io.CCcamPacketDecoder;

/**
 * @author <a href="https://github.com/atum-martin">atum-martin</a>
 * @since 30 Nov 2016 23:34:34
 */

public class EcmRequest {

	private static final CardProfile DEFAULT_PROFILE = new CardProfile();

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(EcmRequest.class);
	
	private int cardId;
	private int prov;
	private int shareId;
	private int serviceId;
	private byte[] ecm;
	private byte[] dcw = null;
	private int cspHash;
	private long timestamp;
	private boolean sentToReaders = false;
	private CamSession session;
	private List<CamSession> sessions = Collections.synchronizedList(new LinkedList<CamSession>());
	private List<Integer> groups = new ArrayList<Integer>(3);
	private CardProfile profile = null;
	
	public EcmRequest(CamSession session, int cardId, int prov, int shareId, int serviceId, byte[] ecm, boolean computeHash) {
		this.setCardId(cardId);
		this.setProv(prov);
		this.session = session;
		this.setShareId(shareId);
		this.setServiceId(serviceId);
		this.setEcm(ecm, computeHash);
		//set default to group 1.
		
		for(int group : session.getGroups())
			groups.add(group);
		updateTimestamp();
	}

	/**
	 * Sets the timestamp of this ECM to NOW.
	 */
	private void updateTimestamp() {
		timestamp = System.currentTimeMillis();
	}

	public int getCardId() {
		return cardId;
	}

	public void setCardId(int cardId) {
		this.cardId = cardId;
		updateCardProfile();
	}
	
	public void updateCardProfile(){
		profile = CardServer.getProfiles().get(cardId);
		if(profile == null)
			profile = DEFAULT_PROFILE;
	}

	public int getProv() {
		return prov;
	}
	
	public CamSession getSessionReq() {
		return session;
	}

	public void setProv(int prov) {
		this.prov = prov;
	}

	public int getShareId() {
		return shareId;
	}

	public void setShareId(int shareId) {
		this.shareId = shareId;
	}

	public int getServiceId() {
		return serviceId;
	}

	public void setServiceId(int serviceId) {
		this.serviceId = serviceId;
	}

	public byte[] getEcm() {
		return ecm;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setEcm(byte[] ecm, boolean computeHash) {
		this.ecm = ecm;
		if (computeHash)
			cspHash = computeEcmHash(ecm);
	}

	/**
	 * This is based on String.hashCode(), Due to CSP only storing 16/19 of the
	 * ecm bytes within "customData" an offset is needed to only calculate the
	 * checksum of unique values rather than repeated variables like ecm length
	 * and ecm command.
	 * 
	 * @param ecm
	 *            - This should be 19 bytes in length and contain ecm command
	 *            and 2 bytes representing ecm length.
	 * @return A 4 byte integer hash of the 16 byte ecm.
	 */
	public static int computeEcmHash(byte[] ecm) {
		/*
		 * try { return new String(ecm, "ISO-8859-1").hashCode(); } catch
		 * (UnsupportedEncodingException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); } return 0;
		 */
		// oscam/trunk/module-cacheex.c
		// value used for hash is signed 4 byte integer.

		// This is based on String.hashCode(), Due to CSP only storing 16/19 of
		// the ecm bytes within "customData" an offset is needed to only
		// calculate the checksum of unique values rather than reapeat variables
		// like ecm length and ecm command.
		int hash = 0;
		for (int i = 3; i < ecm.length; i++) {
			int em = (ecm[i] & 0xFF);
			hash = (31 * hash + em);
		}
		return CCcamPacketDecoder.cspHashSwap(hash);
	}

	public byte[] getDcw() {
		return dcw;
	}

	public void setDcw(byte[] dcw) {
		this.dcw = dcw;
	}

	public boolean hasAnswer() {
		return dcw != null;
	}

	public int getCspHash() {
		return cspHash;
	}

	@Override
	public int hashCode() {
		return (int) cspHash;
	}

	public boolean equals(EcmRequest req) {
		if (req.hashCode() == this.hashCode()) {
			return true;
		}
		return false;
	}

	public void fireActionListeners() {
		for (CamSession session : sessions) {
			//logger.debug("removing listener and firing event.");
			session.getPacketSender().writeEcmAnswer(dcw);
		}
		sessions.clear();
	}

	public void setCspHash(int cspHash) {
		this.cspHash = cspHash;
	}

	public void addListener(CamSession session) {
		//logger.debug("adding listener for ecm.");
		sessions.add(session);
	}

	/**
	 * @return
	 */
	public int getSessionCount() {
		return sessions.size();
	}

	/**
	 * @return
	 */
	public String getSessionsStr() {
		StringBuilder build = new StringBuilder("{");
		for(CamSession session : sessions){
			build.append(session.getAccount()+", ");
		}
		build.append("}");
		return build.toString();
	}

	/**
	 * @return
	 */
	public List<Integer> getGroups() {
		return groups;
	}

	/**
	 * @param session2
	 */
	public void addGroups(CamSession session2) {
		for(int group : session2.getGroups()){
			if(!groups.contains(group)){
				groups.add(group);
			}
		}
	}

	public boolean wasSentToReaders() {
		return sentToReaders;
	}

	public void setSentToReaders() {
		sentToReaders = true;
	}

	public CardProfile getProfile() {
		return profile;
	}
}
