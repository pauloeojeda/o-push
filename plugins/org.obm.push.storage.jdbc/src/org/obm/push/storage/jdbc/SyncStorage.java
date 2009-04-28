package org.obm.push.storage.jdbc;

import org.obm.push.state.SyncState;
import org.obm.push.store.ISyncStorage;

public class SyncStorage implements ISyncStorage {

	@Override
	public SyncState findStateForDevice(String devId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SyncState findStateForKey(String syncKey) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getClientId(String deviceId, String serverId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getServerId(String deviceId, String clientId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean initDevice(String loginAtDomain, String deviceId,
			String deviceType) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void storeMapping(String deviceId, String clientId, String serverId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateState(String devId, SyncState state) {
		// TODO Auto-generated method stub
		
	}

}
