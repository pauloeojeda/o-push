package org.obm.push.state;

import java.util.Calendar;
import java.util.Date;

/**
 * Stores the last sync date for a given sync key & collection
 * 
 * @author tom
 *
 */
public class SyncState {

	
	private Date lastSync;
	private String key;

	public SyncState() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, 1970);
		cal.set(Calendar.MONTH, Calendar.JANUARY);
		lastSync = cal.getTime();
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

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}
	
	
}
