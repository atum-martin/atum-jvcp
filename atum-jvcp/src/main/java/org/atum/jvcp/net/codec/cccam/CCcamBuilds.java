package org.atum.jvcp.net.codec.cccam;

import java.util.HashMap;
import java.util.Map;

public class CCcamBuilds {
	
	private static Map<String,CCcamBuild> builds = new HashMap<String,CCcamBuild>();
	
	static {
		//This call is added to initialize all enumerated values into the builds map.
		CCcamBuild.CCCAM_2_0_11.getBuildNum();
	}
	
	
	public static CCcamBuild getBuild(String ver) {
		return builds.get(ver);
	}

	public enum CCcamBuild {
		CCCAM_2_0_11("2.0.11", 2892),
		CCCAM_2_1_1("2.1.1", 2971),
		CCCAM_2_1_2("2.1.2", 3094),
		CCCAM_2_1_3("2.1.3", 3165),
		CCCAM_2_1_4("2.1.4", 3191),
		CCCAM_2_2_0("2.2.0", 3290),
		CCCAM_2_2_1("2.2.1", 3316),
		CCCAM_2_3_0("2.3.0", 3367),
		CCCAM_2_5_0("2.5.0", 3692);

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
