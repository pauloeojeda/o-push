package org.obm.push.store;

import org.obm.push.state.SyncState;

public interface ISyncStorage {

	void updateState(String devId, String collectionId, SyncState oldState,
			SyncState state);

	SyncState findStateForDevice(String devId, String collectionId);

	SyncState findStateForKey(String syncKey);

	/**
	 * Stores device informations for the given user. Returns <code>true</code>
	 * if the device is allowed to synchronize.
	 * 
	 * @param loginAtDomain
	 * @param deviceId
	 * @param deviceType
	 * @return
	 */
	boolean initDevice(String loginAtDomain, String deviceId, String deviceType);

	void storeMapping(String deviceId, String clientId, String serverId);

	String getServerId(String deviceId, String clientId);

	String getClientId(String deviceId, String serverId);

	/**
	 * Fetches the id associated with a given collection id string. Creates a
	 * new one if missing.
	 * 
	 * @param deviceId
	 * @param collectionId
	 * @return
	 */
	Integer getCollectionMapping(String deviceId, String collectionId);
	
	String getCollectionString(int collectionId);

	String getDataClass(String collectionId);

	void resetForFullSync(String devId);

}
