package org.obm.push.backend.obm22;

import org.obm.push.backend.BackendSession;
import org.obm.push.backend.obm22.impl.ObmSyncBackend;
import org.obm.push.store.ISyncStorage;

public class FolderBackend extends ObmSyncBackend {

	protected FolderBackend(ISyncStorage storage) {
		super(storage);
	}

	public void synchronize(BackendSession bs) {
		getCollectionIdFor(bs.getDevId(), getColName(bs));
	}

	public int getServerIdFor(BackendSession bs) {
		return getCollectionIdFor(bs.getDevId(), getColName(bs));
	}
	
	private String getColName(BackendSession bs){
		return "obm:\\\\" + bs.getLoginAtDomain();
	}

}
