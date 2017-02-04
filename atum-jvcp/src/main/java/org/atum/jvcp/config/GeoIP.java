/**
 * 
 */
package org.atum.jvcp.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;

/**
 * @author <a href="https://github.com/atum-martin">atum-martin</a>
 * @since 4 Feb 2017
 */
public class GeoIP {

	private LinkedList<CountryId> geoIP = new LinkedList<CountryId>();

	public void readDB() {
		InputStream is = this.getClass().getClassLoader().getResourceAsStream("geoip.db.csv");
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String line;
		try {
			while ((line = br.readLine()) != null) {
				String[] parts = line.replaceAll("\"", "").split(",");
				long low = Long.parseLong(parts[2]);
				long high = Long.parseLong(parts[3]);
				String countryCode = parts[4];
				geoIP.add(new CountryId(low, high, countryCode));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getCountry(long ip) {
		for (CountryId geo : geoIP) {
			if (ip >= geo.low && ip <= geo.high) {
				return geo.countryCode;
			}
		}
		return null;
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

	private class CountryId {
		private long low, high;
		private String countryCode;

		public CountryId(long low, long high, String countryCode) {
			this.low = low;
			this.high = high;
			this.countryCode = countryCode;
		}
	}
}
