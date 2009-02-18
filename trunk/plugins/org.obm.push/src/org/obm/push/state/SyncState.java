package org.obm.push.state;

import java.util.Date;

public class SyncState {

	
	private Date lastSync;

	public SyncState() {
		
	}
	
	public Date getLastSync() {
		return lastSync;
	}

	public void setLastSync(Date lastSync) {
		this.lastSync = lastSync;
	}
	
	
}
