/**
 * 
 */
package org.atum.jvcp.net.codec.http;

import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;

/**
 * @author <a href="https://github.com/atum-martin">atum-martin</a>
 * @since 13 Jan 2017 23:18:27
 */
public class GHttpHandler {

	protected static final String API_PREFIX = "/api";

	protected static final String API_CACHE_GET = API_PREFIX + "/c/";
	protected static final String API_ECM_POST = API_PREFIX + "/e/";
	protected static final String API_FEEDER_POST = API_PREFIX + "/f/";
	protected static final String API_CAPMT = API_PREFIX + "/p/";

	public void handleGetRequest(HttpRequest req, HttpResponse response) {
		String[] parts = req.uri().split("/");
		int offset = (parts[3].length() >= 6) ? 4 : 3;
		int networkId = Integer.parseInt(parts[offset++], 16);
		int tsId = Integer.parseInt(parts[offset++], 16);
		int serviceId = Integer.parseInt(parts[offset++], 16);
		int pidCount = Integer.parseInt(parts[offset++], 16);
		long namespace = 0L;
		if (parts.length > offset)
			namespace = Long.parseLong(parts[offset], 16);
	}
}
