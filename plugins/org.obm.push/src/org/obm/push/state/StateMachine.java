package org.obm.push.state;

import java.util.Date;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.push.backend.BackendSession;
import org.obm.push.store.ISyncStorage;

public class StateMachine {

	@SuppressWarnings("unused")
	private static final Log logger = LogFactory.getLog(StateMachine.class);

	private ISyncStorage store;

	public StateMachine(ISyncStorage store) {
		this.store = store;
	}

	public SyncState getSyncState(String syncKey) {
		SyncState ret = null;

		ret = store.findStateForKey(syncKey);
		if (ret == null) {
			ret = new SyncState();
			ret.setKey(syncKey);
		}
		return ret;
	}

	public String allocateNewSyncKey(BackendSession bs, SyncState oldState) {
		SyncState newState = new SyncState();
		Date nd = bs.getUpdatedSyncDate();
		if (nd != null) {
			newState.setLastSync(bs.getUpdatedSyncDate());
		}
		String newSk = UUID.randomUUID().toString();
		newState.setKey(newSk);
		store.updateState(bs.getDevId(), oldState, newState);
		return newSk;
	}

}
