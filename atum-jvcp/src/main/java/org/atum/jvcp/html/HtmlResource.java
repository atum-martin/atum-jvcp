/**
 * 
 */
package org.atum.jvcp.html;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author <a href="https://github.com/atum-martin">atum-martin</a>
 * @since 28 Dec 2016
 */
public class HtmlResource {
	
	private String content;
	
	public HtmlResource(String source){
		StringBuilder builder = new StringBuilder();
		System.out.println("loading resource: "+source);
		InputStream is = this.getClass().getClassLoader().getResourceAsStream(source);
		if(is == null){
			this.content = null;
			return;
		}
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String line;
		try {
			while((line = br.readLine()) != null){
				builder.append(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.content = builder.toString();
	}
	
	public String getContent(){
		return content;
	}
}
