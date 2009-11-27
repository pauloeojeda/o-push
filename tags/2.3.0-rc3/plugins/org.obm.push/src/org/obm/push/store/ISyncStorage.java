package org.obm.push.store;

import java.util.Set;

import org.obm.push.state.SyncState;

public interface ISyncStorage {

	void updateState(String devId, Integer collectionId, SyncState oldState,
			SyncState state);

	SyncState findStateForDevice(String devId, Integer collectionId);

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

	/**
	 * Fetches the id associated with a given collection id string. Creates a
	 * new one if missing.
	 * 
	 * @param deviceId
	 * @param collectionId
	 * @return
	 */
	Integer getCollectionMapping(String deviceId, String collectionId);
	
	String getCollectionString(Integer collectionId);

	String getDataClass(String collectionId);

	void resetForFullSync(String devId);
	
	Integer getDevId(String deviceId);

	Set<Integer> getAllCollectionId(String devId);
	
	void resetCollection(String devId, Integer collectionId);

}
