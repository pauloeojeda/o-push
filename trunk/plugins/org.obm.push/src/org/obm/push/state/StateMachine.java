package org.obm.push.state;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.push.backend.BackendSession;

public class StateMachine {

	@SuppressWarnings("unused")
	private static final Log logger = LogFactory.getLog(StateMachine.class);

	private static Map<String, SyncState> devIdStore;
	private static Map<String, SyncState> syncKeyStore;

	static {
		devIdStore = new HashMap<String, SyncState>();
		syncKeyStore = new HashMap<String, SyncState>();
	}

	public StateMachine() {
	}

	public SyncState getSyncState(String syncKey) {
		SyncState ret = null;
		if (syncKeyStore.containsKey(syncKey)) {
			ret = syncKeyStore.get(syncKey);
		} else {
			ret = new SyncState();
			ret.setKey(syncKey);
		}
		return ret;
	}

	public String allocateNewSyncKey(BackendSession bs) {
		SyncState oldState = devIdStore.get(bs.getDevId());
		if (oldState != null) {
			syncKeyStore.remove(oldState.getKey());
		}

		SyncState newState = new SyncState();
		Date nd = bs.getUpdatedSyncDate();
		if (nd != null) {
			newState.setLastSync(bs.getUpdatedSyncDate());
		}
		String newSk = UUID.randomUUID().toString();
		newState.setKey(newSk);
		syncKeyStore.put(newSk, newState);
		devIdStore.put(bs.getDevId(), newState);
		return newSk;
	}

}
