package org.atum.jvcp.net.codec.cccam;

import java.util.HashMap;
import java.util.Map;

public class CCcamBuilds {
	
	private static Map<String,CCcamBuild> builds = new HashMap<String,CCcamBuild>();
	
	public static CCcamBuild getBuild(String ver) {
		return builds.get(ver);
	}

	public enum CCcamBuild {

		CCCAM_2_3_0("2.3.0", 3367);

		private String version;
		private int buildNum;

		CCcamBuild(String version, int buildNum) {
			this.setVersion(version);
			this.setBuildNum(buildNum);
			CCcamBuilds.builds.put(version, this);
		}
		
		public String getVersion() {
			return version;
		}

		public void setVersion(String version) {
			this.version = version;
		}

		public int getBuildNum() {
			return buildNum;
		}

		public void setBuildNum(int buildNum) {
			this.buildNum = buildNum;
		}

	}
}
