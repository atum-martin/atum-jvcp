/**
 * 
 */
package org.atum.jvcp.config;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.atum.jvcp.CCcamServer;
import org.atum.jvcp.CamServer;
import org.atum.jvcp.NewcamdServer;
import org.atum.jvcp.account.Account;
import org.atum.jvcp.net.codec.NetUtils;
import org.atum.jvcp.net.codec.cccam.CCcamClient;
import org.atum.jvcp.net.codec.newcamd.NewcamdClient;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * @author <a href="https://github.com/atum-martin">atum-martin</a>
 * @since 2 Jan 2017
 */
public class ReaderConfig {

	/**
	 * The instance of the log4j {@link Logger}.
	 */
	private Logger logger = Logger.getLogger(ReaderConfig.class);
	private ArrayList<CCcamServer> cccamServers = new ArrayList<CCcamServer>();
	private ArrayList<NewcamdServer> newcamdsServers = new ArrayList<NewcamdServer>();

	public ReaderConfig(CamServer[] servers) {
		setupServers(servers);
		Gson gson = new GsonBuilder().create();
		InputStream is = this.getClass().getClassLoader().getResourceAsStream("readers.json");
		JvcpReader[] readers = gson.fromJson(new InputStreamReader(is), JvcpReader[].class);
		for (JvcpReader reader : readers) {
			if (reader != null) {
				if (!verifyReaderBasics(reader)) {
					logger.error("reader failed basic validation: "+reader);
					continue;
				}
				logger.info("loaded reader: " + reader.protocol + "://" + reader.host + ":" + reader.port + " " + reader.username);

				if (reader.protocol.equalsIgnoreCase("cccam")) {
					CCcamServer server = getCCcamServer(reader.server);
					if (server == null)
						continue;
					CCcamClient.connect(server, reader.host, reader.port, new Account(reader.username, reader.password));
				} else if (reader.protocol.equalsIgnoreCase("newcamd")) {
					NewcamdServer server = getNewcamdServer(reader.server);
					if (server == null)
						continue;
					if (reader.desKey == null) {
						logger.error("no desKey set for newcamd reader: " + reader.name);
						continue;
					}
					NewcamdClient.connect(server, reader.host, reader.port, new Account(reader.username, reader.password), NetUtils.getBytesValue(reader.desKey));
				}
			}
		}
	}

	private boolean verifyReaderBasics(JvcpReader reader) {
		if(reader.host == null)
			return false;
		if(reader.name == null)
			return false;
		if(reader.protocol == null)
			return false;
		if(reader.username == null)
			return false;
		if(reader.password == null)
			return false;
		if(reader.port == 0)
			return false;
		return true;
	}

	private CCcamServer getCCcamServer(String serverName) {
		if (cccamServers.size() == 0) {
			logger.error("no server found to bind reader to. Missing server name: " + serverName);
			return null;
		}
		if (serverName == null) {
			return cccamServers.get(0);
		}
		for (CCcamServer serv : cccamServers) {
			if (serv.getName().equalsIgnoreCase(serverName)) {
				return serv;
			}
		}
		return cccamServers.get(0);
	}

	private NewcamdServer getNewcamdServer(String serverName) {
		if (newcamdsServers.size() == 0) {
			logger.error("no server found to bind reader to. Missing server name: " + serverName);
			return null;
		}
		if (serverName == null) {
			return newcamdsServers.get(0);
		}
		for (NewcamdServer serv : newcamdsServers) {
			if (serv.getName().equalsIgnoreCase(serverName)) {
				return serv;
			}
		}
		return newcamdsServers.get(0);
	}

	private void setupServers(CamServer[] servers) {
		for (int i = 0; i < servers.length; i++) {
			if (servers[i] instanceof CCcamServer) {
				cccamServers.add((CCcamServer) servers[i]);
			}
			if (servers[i] instanceof NewcamdServer) {
				newcamdsServers.add((NewcamdServer) servers[i]);
			}
		}
	}

	class JvcpReader {
		private String name;
		private String username;
		private String password;
		private String protocol;
		private String host;
		private String server = null;
		private int port;

		/** protocol specific settings: */
		private String desKey = null;
	}
}
