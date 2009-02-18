package org.obm.push.impl;

public class Utils {

	public static String getFolderId(String devId, String dataClass) {
		return devId + "/" + dataClass;
	}

}
