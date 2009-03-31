package org.obm.push.state;

import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.push.backend.BackendSession;

public class StateMachine {

	private static final Log logger = LogFactory.getLog(StateMachine.class);

	public SyncState getSyncState(String syncKey) {
		// TODO Auto-generated method stub
		logger.info("getSyncState(" + syncKey + ")");
		return new SyncState();
	}

	public String allocateNewSyncKey(BackendSession bs) {
		// TODO Auto-generated method stub
		return UUID.randomUUID().toString();
	}


}
