/**
 * 
 */
package org.atum.jvcp.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
	private Map<Integer, List<Integer>> cardMap = new HashMap<Integer, List<Integer>>();
	private static ChannelList singleton = null;
	
	public static String getChannelName(int cardId,int serviceId){
		String channelId = Integer.toHexString(cardId)+":"+Integer.toHexString(serviceId);
		String channelName = getSingleton().channelMap.get(channelId);
		if(channelName == null){
			return channelId;
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
				if(parts.length < 3){
					if(line.contains(";")){
						handleCardMapping(line);
					}
					continue;
				}
				int cardId = Integer.parseInt(parts[0].split(":")[0], 16);
				int serviceId = Integer.parseInt(parts[0].split(":")[1], 16);
				@SuppressWarnings("unused")
				String provider = parts[1];
				String channelName = parts[2];
				
				addChannelMapping(cardId, serviceId, channelName);
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

	/**
	 * @param cardId
	 * @param serviceId
	 * @param channelName 
	 */
	private void addChannelMapping(int cardId, int serviceId, String channelName) {
		List<Integer> cardMapping = cardMap.get(cardId);
		addToMap(cardId, serviceId, channelName);
		if(cardMapping != null){
			for(int cardMappedId : cardMapping){
				addToMap(cardMappedId, serviceId, channelName);
			}
		}
	}
	
	public void addToMap(int cardId, int serviceId, String channelName){
		String channelId = Integer.toHexString(cardId)+":"+Integer.toHexString(serviceId);
		channelMap.put(channelId, channelName);
		logger.info(channelId+" "+channelName);
	}

	/**
	 * @param line
	 */
	private void handleCardMapping(String line) {
		String[] parts = line.split(";");
		int masterCardId = Integer.parseInt(parts[0], 16);
		List<Integer> cardList = cardMap.get(masterCardId);
		if(cardList == null){
			cardList = new ArrayList<Integer>();
			cardMap.put(masterCardId, cardList);
		}
		for(int i = 1; i < parts.length; i++){
			cardList.add(Integer.parseInt(parts[i], 16));
		}
		
	}
}
