package org.atum.jvcp.model;

public class EcmRequest {

	private int cardId;
	private Provider prov;
	private int shareId;
	private int serviceId;
	private byte[] ecm;
	private byte[] dcw = null;

	public EcmRequest(int cardId,Provider prov,int shareId,int serviceId,byte[] ecm){
		this.setCardId(cardId);
		this.setProv(prov);
		this.setShareId(shareId);
		this.setServiceId(serviceId);
		this.setEcm(ecm);
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

	public void setEcm(byte[] ecm) {
		this.ecm = ecm;
	}

	public byte[] getDcw() {
		return dcw;
	}

	public void setDcw(byte[] dcw) {
		this.dcw = dcw;
	}
}
