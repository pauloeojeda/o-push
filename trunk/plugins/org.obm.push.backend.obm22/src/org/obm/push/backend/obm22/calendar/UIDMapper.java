package org.obm.push.backend.obm22.calendar;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class UIDMapper {

	public static final String UID_PREFIX="obm-calendar-";
	
	private static final Log logger = LogFactory.getLog(UIDMapper.class);
	
	// obm, device
	private Map<String, String> toDevice;

	// device, obm
	private Map<String, String> toOBM;

	public UIDMapper() {
		toDevice = new HashMap<String, String>();
		toOBM = new HashMap<String, String>();
	}

	public String toDevice(String obm) {
		String ret = null;
		if (toDevice.containsKey(obm)) {
			ret = toDevice.get(obm);
		} else {
			ret = obm;
		}
		logger.info("toDevice("+obm+") => "+ret);
		return ret;
	}

	public String toOBM(String device) {
		if (device.startsWith(UID_PREFIX)) {
			return device;
		}
		if (toOBM.containsKey(device)) {
			return toOBM.get(device);
		}
		return null;
	}

	public void addMapping(String device, String obm) {
		logger.info("stored mapping "+obm+" => "+device);
		toDevice.put(obm, device);
		toOBM.put(device, obm);
	}

}
