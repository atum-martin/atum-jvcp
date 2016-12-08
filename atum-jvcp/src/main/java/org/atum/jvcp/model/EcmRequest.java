package org.atum.jvcp.model;

import java.util.zip.Adler32;
import java.util.zip.Checksum;

public class EcmRequest {

	private int cardId;
	private Provider prov;
	private int shareId;
	private int serviceId;
	private byte[] ecm;
	private byte[] dcw = null;
	private long ecmHash;
	private long timestamp;

	public EcmRequest(int cardId,Provider prov,int shareId,int serviceId,byte[] ecm){
		this.setCardId(cardId);
		this.setProv(prov);
		this.setShareId(shareId);
		this.setServiceId(serviceId);
		this.setEcm(ecm);
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

	public void setEcm(byte[] ecm) {
		this.ecm = ecm;
		computeEcmHash();
	}

	/**
	 * CRC checksum for ECM. 
	 * Adler32 is a faster checksum implementation than CRC32.
	 * TODO: Implement simpler crc system as ecm length is always 16.
	 */
	private void computeEcmHash() {
		Checksum cksum = new Adler32();
		cksum.update(ecm, 0, ecm.length);
		ecmHash = cksum.getValue();
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
}
