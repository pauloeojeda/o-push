package org.obm.push.backend.obm22.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.push.store.ISyncStorage;

public class UIDMapper {

	public static final String UID_CAL_PREFIX = "obm-calendar-";
	public static final String UID_BOOK_PREFIX = "obm-contacts-";

	@SuppressWarnings("unused")
	private static final Log logger = LogFactory.getLog(UIDMapper.class);

	private ISyncStorage storage;

	public UIDMapper(ISyncStorage storage) {
		this.storage = storage;
	}

	public String toDevice(String deviceId, String obm) {
		String ret = storage.getClientId(deviceId, obm);
		if (ret == null) {
			ret = obm;
		}
		return ret;
	}

	public String toOBM(String deviceId, String clientId) {
		if (clientId.startsWith(UID_CAL_PREFIX)) {
			return clientId;
		}
		return storage.getServerId(deviceId, clientId);
	}

	public void addMapping(String deviceId, String clientId, String obm) {
		storage.storeMapping(deviceId, clientId, obm);
	}
	
	public String getClientIdFor(String deviceId, String collection, String clientId) {
		int folderId = storage.getCollectionMapping(deviceId, collection);
		StringBuilder sb = new StringBuilder(10);
		sb.append(folderId);
		if (clientId != null) {
			sb.append(':');
			sb.append(clientId);
		}
		return sb.toString();
	}

}
