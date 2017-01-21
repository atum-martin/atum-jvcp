/**
 * 
 */
package org.atum.jvcp.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * @author <a href="https://github.com/atum-martin">atum-martin</a>
 * @since 21 Jan 2017
 */
public class ChannelList {
	
	public ChannelList(){
		loadChannelList();
	}
	
	private Logger logger = Logger.getLogger(ChannelList.class);
	private Map<String, String> channelMap = new HashMap<String, String>();
	private static ChannelList singleton = null;
	
	public String getChannelName(int cardId,int serviceId){
		String channelId = Integer.toHexString(cardId)+":"+Integer.toHexString(serviceId);
		String channelName = channelMap.get(channelId);
		if(channelName == null){
			return Integer.toHexString(cardId)+":"+Integer.toHexString(serviceId);
		}
		return channelName;
	}
	
	public static ChannelList getSingleton(){
		if(singleton == null){
			singleton = new ChannelList();
		}
		return singleton;
	}

	private void loadChannelList() {
		InputStream is = this.getClass().getClassLoader().getResourceAsStream("channellist.txt");
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String line;
		int lineNo = 1;
		try {
			while((line = br.readLine()) != null){
				String[] parts = line.split("\\|");
				if(parts.length < 3)
					continue;
				int cardId = Integer.parseInt(parts[0].split(":")[0], 16);
				int serviceId = Integer.parseInt(parts[0].split(":")[1], 16);
				String provider = parts[1];
				String channelName = parts[2];
				String channelId = Integer.toHexString(cardId)+":"+Integer.toHexString(serviceId);
				channelMap.put(channelId, channelName);
				logger.info(channelId+" "+channelName);
				lineNo++;
			}
		} catch (ArrayIndexOutOfBoundsException e){
			logger.error("error parsing channellist on line: "+lineNo);
			e.printStackTrace();
		} catch (IOException e) {
			logger.error("error parsing channellist on line: "+lineNo);
			e.printStackTrace();
		}
	}
}
