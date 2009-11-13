package org.obm.push.backend.obm22.impl;

import java.util.Date;
import java.util.Set;

import org.obm.push.backend.SyncCollection;

public class ChangedCollections {

	private Date lastSync;
	private Set<SyncCollection> changed;
	
	public ChangedCollections(Date lastSync, Set<SyncCollection> changed) {
		this.lastSync = lastSync;
		this.changed = changed;
	}

	
	public Date getLastSync() {
		return lastSync;
	}

	public Set<SyncCollection> getChanged() {
		return changed;
	}
	
	
	
}
