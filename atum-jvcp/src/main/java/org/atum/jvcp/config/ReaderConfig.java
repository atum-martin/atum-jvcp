/**
 * 
 */
package org.atum.jvcp.config;

import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.atum.jvcp.CCcamServer;
import org.atum.jvcp.CamServer;
import org.atum.jvcp.NewcamdServer;
import org.atum.jvcp.account.Account;
import org.atum.jvcp.net.codec.cccam.CCcamClient;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * @author <a href="https://github.com/atum-martin">atum-martin</a>
 * @since 2 Jan 2017
 */
public class ReaderConfig {

	public static void main(String[] args) {
		BasicConfigurator.configure();
		new ReaderConfig(null);
	}

	/**
	 * The instance of the log4j {@link Logger}.
	 */
	private Logger logger = Logger.getLogger(ReaderConfig.class);
	private CCcamServer cccamServer = null;
	private NewcamdServer newcamdsServer = null;

	public ReaderConfig(CamServer[] servers) {
		setupServers(servers);
		Gson gson = new GsonBuilder().create();
		InputStream is = this.getClass().getClassLoader().getResourceAsStream("readers.json");
		JvcpReader[] readers = gson.fromJson(new InputStreamReader(is), JvcpReader[].class);
		for (JvcpReader reader : readers) {
			if (reader != null) {
				logger.info("loaded reader: " + reader.protocol + "://" + reader.host + ":" + reader.port + " " + reader.username);
				if (reader.protocol.equalsIgnoreCase("cccam")) {
					CCcamClient.connect(cccamServer, reader.host, reader.port, new Account(reader.username, reader.password));
				}
			}
		}
	}

	private void setupServers(CamServer[] servers) {
		for (int i = 0; i < servers.length; i++) {
			if (cccamServer == null && servers[i] instanceof CCcamServer) {
				cccamServer = (CCcamServer) servers[i];
			}
			if (newcamdsServer == null && servers[i] instanceof NewcamdServer) {
				newcamdsServer = (NewcamdServer) servers[i];
			}
		}
	}

	class JvcpReader {
		private String name;
		private String username;
		private String password;
		private String protocol;
		private String host;
		private int port;
	}
}
