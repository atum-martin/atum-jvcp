package org.atum.jvcp.model;

/**
 * @author <a href="https://github.com/atum-martin">atum-martin</a>
 * @since 14 Dec 2016 22:40:15
 */
public interface PacketSenderInterface {

	public void writeKeepAlive();
	public void writeEcmAnswer(byte[] dcw);
	public void writeEcmRequest(EcmRequest req);
	public void writeFailedEcm();
	public void writeCard(Card card);

}
