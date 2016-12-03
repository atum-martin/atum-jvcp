package org.atum.jvcp.cache;

import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import org.atum.jvcp.net.codec.cccam.CCcamSession;

public class HashCache {
	
	private Map<Integer,CardCache> cache = new HashMap<Integer,CardCache>();
	private static HashCache singleton;
	
	public void setupCache(int cardId){
		CardCache card = cache.get(cardId);
		if(card == null){
			cache.put(cardId, new CardCache());
		}
	}
	
	public static HashCache getSingleton(){
		if(singleton == null)
			singleton = new HashCache();
		return singleton;
	}

	public void pushCache(int cardId,int serviceId,byte[] ecm, byte[] dcw){
		long checksum = calculateChecksum(ecm);
		ServiceCacheEntry entry = new ServiceCacheEntry(checksum,dcw);
		ServiceCache service = cache.get(cardId).getService(serviceId);
		if(service == null)
			service = cache.get(cardId).createService(serviceId);
		service.putDCW(checksum, entry);
	}
	
	private long calculateChecksum(byte[] ecm) {
		Checksum checksum = new CRC32();
		checksum.update(ecm, 0, ecm.length);
		return checksum.getValue();
	}

	public byte[] readCache(int cardId, int serviceId,byte[] ecm){
		ServiceCache service = cache.get(cardId).getService(serviceId);
		if(service == null)
			return null;
		long checksum = calculateChecksum(ecm);
		return service.getDCW(checksum);
		
	}
	

	public void addListener(int cardId, int serviceId, byte[] ecm, CCcamSession session) {
		ServiceCache service = cache.get(cardId).getService(serviceId);
		if(service == null)
			return;
		long checksum = calculateChecksum(ecm);
		service.addListener(checksum,session);
	}
	
	public class CardCache {
		private Map<Integer,ServiceCache> cache = new HashMap<Integer,ServiceCache>();
		
		public ServiceCache getService(int serviceId){
			return cache.get(serviceId);
		}

		public ServiceCache createService(int serviceId) {
			ServiceCache entry = new ServiceCache();
			cache.put(serviceId, entry);
			return entry;
		}
	}
	
	public class ServiceCache {
		private Map<Long,ServiceCacheEntry> cache = new Hashtable<Long,ServiceCacheEntry>();
		private List<ServiceCacheEntry> listeners = new LinkedList<ServiceCacheEntry>();
		
		public byte[] getDCW(long ecmChk){
			return cache.get(ecmChk).getDCW();
		}
		
		public void addListener(long checksum, CCcamSession session) {
			ServiceCacheEntry entry = new ServiceCacheEntry(checksum,null);
			entry.setSession(session);
			listeners.add(entry);
		}

		public void putDCW(long ecmChk,ServiceCacheEntry entry){
			cache.put(ecmChk, entry);
			fireListeners(ecmChk, entry);
		}
		
		private void fireListeners(long ecmChk,ServiceCacheEntry dcw) {
			if(listeners.size() == 0)
				return;
			Iterator<ServiceCacheEntry> it = listeners.iterator();
			int index = 0;
			while(it.hasNext()){
				ServiceCacheEntry lis = it.next();
				if(lis.getEcmChk() == ecmChk){
					listeners.remove(index);
					return;
				}
				index++;
			}
		}

		public void cleanupCache(long currentTime,long timeout){
			Collection<ServiceCacheEntry> entries = cache.values();
			for(ServiceCacheEntry entry : entries){
				if(currentTime - entry.timestamp > timeout){
					cache.remove(entry.ecmChecksum);
				}
			}
		}
	}
	
	public class ServiceCacheEntry {
		private long ecmChecksum;
		private byte[] dcw;
		private long timestamp;
		private CCcamSession session = null;
		
		public ServiceCacheEntry(long checksum, byte[] dcw) {
			this.timestamp = System.currentTimeMillis();
			this.dcw = dcw;
			this.ecmChecksum = checksum;
		}

		public long getEcmChk() {
			return ecmChecksum;
		}

		public void setSession(CCcamSession session){
			this.session = session;
		}
		
		public CCcamSession getSession(){
			return session;
		}
		
		public byte[] getDCW() {
			return dcw;
		}
	}

}
