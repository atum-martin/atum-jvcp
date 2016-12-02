package org.atum.jvcp.cache;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class HashCache {
	
	private Map<Integer,CardCache> cache = new HashMap<Integer,CardCache>();
	
	public void setupCache(int cardId){
		
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
		// TODO Auto-generated method stub
		return 0;
	}

	public byte[] readCache(int cardId, int serviceId,byte[] ecm){
		ServiceCache service = cache.get(cardId).getService(serviceId);
		if(service == null)
			return null;
		long checksum = calculateChecksum(ecm);
		return service.getDCW(checksum);
		
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
		private Map<Long,ServiceCacheEntry> cache = new HashMap<Long,ServiceCacheEntry>();
		
		public byte[] getDCW(long ecmChk){
			return cache.get(ecmChk).getDCW();
		}
		
		public void putDCW(long ecmChk,ServiceCacheEntry entry){
			cache.put(ecmChk, entry);
		}
	}
	
	public class ServiceCacheEntry {
		private long ecmChecksum;
		private byte[] dcw;
		private long timestamp;
		
		public ServiceCacheEntry(long checksum, byte[] dcw) {
			this.timestamp = System.currentTimeMillis();
			this.dcw = dcw;
			this.ecmChecksum = checksum;
		}

		public byte[] getDCW() {
			return dcw;
		}
	}
}
