package org.atum.jvcp.model;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.Adler32;
import java.util.zip.Checksum;

import org.atum.jvcp.net.codec.cccam.CCcamSession;

public class EcmRequest {

	private int cardId;
	private Provider prov;
	private int shareId;
	private int serviceId;
	private byte[] ecm;
	private byte[] dcw = null;
	private int cspHash;
	private long timestamp;
	private List<CCcamSession> sessions = Collections.synchronizedList(new LinkedList<CCcamSession>());

	public EcmRequest(int cardId,Provider prov,int shareId,int serviceId,byte[] ecm, boolean computeHash){
		this.setCardId(cardId);
		this.setProv(prov);
		this.setShareId(shareId);
		this.setServiceId(serviceId);
		this.setEcm(ecm, computeHash);
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
	}

	public Provider getProv() {
		return prov;
	}

	public void setProv(Provider prov) {
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
	
	public long getTimestamp(){
		return timestamp;
	}

	public void setEcm(byte[] ecm, boolean computeHash) {
		this.ecm = ecm;
		if(computeHash)
			cspHash = computeEcmHash(ecm);
	}

	public static int computeEcmHash(byte[] ecm) {
		int hash = 0;
		for(int i = 3; i < ecm.length; i++){
			int em = (ecm[i] & 0xFF);
			hash = 31 * hash + em;
		}
		return hash;
	}

	public byte[] getDcw() {
		return dcw;
	}

	public void setDcw(byte[] dcw) {
		this.dcw = dcw;
	}
	
	public boolean hasAnswer(){
		return dcw != null;
	}
	
	public int getCspHash(){
		return cspHash;
	}
	
	@Override
	public int hashCode(){
		return (int) cspHash;
	}
	
	public boolean equals(EcmRequest req){
		if(req.hashCode() == this.hashCode()){
			return true;
		}
		return false;
	}

	public void fireActionListeners() {
		for(CCcamSession session : sessions){
			System.out.println("removing listener and firing event.");
			session.getPacketSender().writeEcmAnswer(dcw);
		}
	}

	public void setCspHash(int cspHash) {
		this.cspHash = cspHash;
	}

	public void addListener(CCcamSession session) {
		System.out.println("adding listener for ecm.");
		sessions.add(session);
	}
}
