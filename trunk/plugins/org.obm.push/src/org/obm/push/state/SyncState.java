package org.obm.push.state;

import java.util.Date;

/**
 * Stores the last sync date for a given sync key & collection
 * 
 * @author tom
 *
 */
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

	/**
	 * @return true if we matched the SyncKey to a sync date
	 */
	public boolean isValid() {
		return lastSync != null;
	}
	
	
}
