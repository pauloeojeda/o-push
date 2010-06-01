package org.obm.push.state;

import java.util.Date;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.push.backend.BackendSession;
import org.obm.push.exception.CollectionNotFoundException;
import org.obm.push.store.ISyncStorage;

public class StateMachine {

	@SuppressWarnings("unused")
	private static final Log logger = LogFactory.getLog(StateMachine.class);

	private ISyncStorage store;

	public StateMachine(ISyncStorage store) {
		this.store = store;
	}
	
	public SyncState getFolderSyncState(String deviceId, String collectionUrl, String syncKey) {
		try {
			return getSyncState(store.getCollectionMapping(deviceId, collectionUrl),syncKey);
		} catch (CollectionNotFoundException e) {
			SyncState ret = new SyncState(collectionUrl);
			ret.setKey(syncKey);
			if (!"0".equals(syncKey)) {
				ret.setLastSync(null);
			}
			return ret;
		}
	}

	public SyncState getSyncState(Integer collectionId, String syncKey) throws CollectionNotFoundException {
		SyncState ret = null;

		ret = store.findStateForKey(syncKey);
		if (ret == null) {
			ret = new SyncState(store.getCollectionPath(collectionId));
			ret.setKey(syncKey);
			if (!"0".equals(syncKey)) {
				ret.setLastSync(null);
			}
		}
		return ret;
	}

	public String allocateNewSyncKey(BackendSession bs, Integer collectionId,
			SyncState oldState) throws CollectionNotFoundException {
		SyncState newState = new SyncState(store.getCollectionPath(collectionId));
		Date nd = bs.getUpdatedSyncDate(collectionId);
		if (nd != null) {
			newState.setLastSync(bs.getUpdatedSyncDate(collectionId));
		}
		String newSk = UUID.randomUUID().toString();
		newState.setKey(newSk);
		store.updateState(bs.getDevId(), collectionId, oldState, newState);
		return newSk;
	}

}
