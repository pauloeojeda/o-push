package org.obm.push;

import java.util.HashMap;
import java.util.Map;

public class PushConfiguration {

	private Map<String, String> conf;

	public PushConfiguration() {
		conf = new HashMap<String, String>();
	}

	String getConfValue(String confKey) {
		return conf.get(confKey);
	}

}
