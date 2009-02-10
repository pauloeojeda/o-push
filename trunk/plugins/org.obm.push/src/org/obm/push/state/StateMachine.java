package org.obm.push.state;

import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class StateMachine {

	private static final Log logger = LogFactory.getLog(StateMachine.class);

	public SyncState getSyncState(String syncKey) {
		// TODO Auto-generated method stub
		logger.info("getSyncState(" + syncKey + ")");
		return new SyncState();
	}

	public String getNewSyncKey(String syncKey) {
		// TODO Auto-generated method stub
		return UUID.randomUUID().toString();
	}

	public void setSyncState(String newSyncKey, SyncState state) {
		// TODO Auto-generated method stub
		logger.info("setSyncState(" + newSyncKey + ", " + state + ")");
	}

}
