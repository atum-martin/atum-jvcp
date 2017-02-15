/**
 * 
 */
package org.atum.jvcp.html;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author <a href="https://github.com/atum-martin">atum-martin</a>
 * @since 28 Dec 2016
 */
public class HtmlResource {
	
	private byte[] content;
	
	public HtmlResource(String source){
		//System.out.println("loading resource: "+source);
		InputStream is = this.getClass().getClassLoader().getResourceAsStream(source);
		if(is == null){
			this.content = null;
			return;
		}
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();

		int bytesRead;
		byte[] data = new byte[16384];

		try {
			while ((bytesRead = is.read(data, 0, data.length)) != -1) {
			  buffer.write(data, 0, bytesRead);
			}
			buffer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.content = buffer.toByteArray();
	}
	
	public byte[] getContent(){
		return content;
	}
}
