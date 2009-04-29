package org.obm.push.store;

import org.obm.push.state.SyncState;

public interface ISyncStorage {

	void updateState(String devId, SyncState oldState, SyncState state);
	
	SyncState findStateForDevice(String devId);
	
	SyncState findStateForKey(String syncKey);
	
	boolean initDevice(String loginAtDomain, String deviceId, String deviceType);
	
	void storeMapping(String deviceId, String clientId, String serverId);
	
	String getServerId(String deviceId, String clientId);

	String getClientId(String deviceId, String serverId);

}
