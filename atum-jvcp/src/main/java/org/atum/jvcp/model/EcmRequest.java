package org.atum.jvcp.model;

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
	private long ecmHash;
	private long timestamp;
	private List<CCcamSession> sessions = new LinkedList<CCcamSession>();

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
			ecmHash = computeEcmHash(ecm);
	}

	/**
	 * CRC checksum for ECM. 
	 * Adler32 is a faster checksum implementation than CRC32.
	 * TODO: Implement simpler crc system as ecm length is always 16.
	 */
	public static long computeEcmHash(byte[] ecm) {
		Checksum cksum = new Adler32();
		cksum.update(ecm, 0, ecm.length);
		return cksum.getValue();
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
	
	public long getEcmHash(){
		return ecmHash;
	}
	
	@Override
	public int hashCode(){
		return (int) ecmHash;
	}
	
	public boolean equals(EcmRequest req){
		if(req.hashCode() == this.hashCode()){
			return true;
		}
		return false;
	}

	public void fireActionListeners() {
		for(CCcamSession session : sessions){
			session.getPacketSender().writeEcmAnswer(dcw);
		}
	}

	public void setEcmHash(long ecmHash2) {
		this.ecmHash = ecmHash2;
	}
}
