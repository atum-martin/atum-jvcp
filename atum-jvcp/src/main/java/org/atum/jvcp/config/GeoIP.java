/**
 * 
 */
package org.atum.jvcp.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * @author <a href="https://github.com/atum-martin">atum-martin</a>
 * @since 4 Feb 2017
 */
public class GeoIP {

	private ArrayList<CountryId> geoIP = new ArrayList<CountryId>();

	public void readDB() {
		//http://geolite.maxmind.com/download/geoip/database/GeoIPCountryCSV.zip
		InputStream is = this.getClass().getClassLoader().getResourceAsStream("geoip.db.csv");
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String line;
		try {
			while ((line = br.readLine()) != null) {
				if(!line.contains(","))
					continue;
				String[] parts = line.replaceAll("\"", "").split(",");
				if(parts.length < 5)
					continue;
				long low = Long.parseLong(parts[2]);
				long high = Long.parseLong(parts[3]);
				String countryCode = parts[4];
				geoIP.add(new CountryId(low, high, countryCode));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		Collections.sort(geoIP, new Comparator<CountryId>(){
			public int compare(CountryId c1, CountryId c2) {
				if(c1.ipValHigh == c2.ipValHigh && c1.ipValLow == c1.ipValLow){
					return 0;
				} else if(c1.ipValHigh > c2.ipValHigh){
					return -1;
				} else {
					return 1;
				}
			}	
		});
	}

	public String getCountry(long ip) {
		int index = Collections.binarySearch(geoIP, ip);
		/*for (CountryId geo : geoIP) {
			if (ip >= geo.ipValLow && ip <= geo.ipValHigh) {
				return geo.countryCode;
			}
		}*/
		if(index <= -1)
			return null;
		return geoIP.get(index).countryCode;
	}

	public String getCountry(String ip) {
		return getCountry(ipToLong(ip));
	}

	public long ipToLong(String ipAddress) {

		String[] ipAddressInArray = ipAddress.split("\\.");
		long result = 0;
		for (int i = 0; i < ipAddressInArray.length; i++) {

			int power = 3 - i;
			int ip = Integer.parseInt(ipAddressInArray[i]);
			result += ip * Math.pow(256, power);

		}
		return result;
	}

	private class CountryId implements Comparable<Long> {
		private long ipValLow, ipValHigh;
		private String countryCode;

		public CountryId(long low, long high, String countryCode) {
			this.ipValLow = low;
			this.ipValHigh = high;
			this.countryCode = countryCode;
		}

		public int compareTo(Long ip) {
			if (ip >= ipValLow && ip <= ipValHigh) {
				return 0;
			} if(ip < ipValLow){
				return -1;
			} else {
				return 1;
			}
		}
	}
}
