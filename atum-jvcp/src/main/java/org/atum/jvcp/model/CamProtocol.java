/**
 * 
 */
package org.atum.jvcp.model;

/**
 * @author <a href="https://github.com/atum-martin">atum-martin</a>
 * @since 11 Jan 2017
 */
public enum CamProtocol {

	CCCAM("CCcam"),
	NEWCAMD("Newcamd"),
	CCCAM_CACHE("CCcam/Cache"),
	NEWCAMD_CACHE("Newcamd/Cache"), 
	GHTTP("GHttp");
	
	private String proto;
	
	private CamProtocol(String proto){
		this.proto = proto;
	}
	
	public String getProtocol(){
		return proto;
	}
	
	public String toString(){
		return getProtocol();
	}
}
