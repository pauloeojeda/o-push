package org.obm.push.backend;

import java.util.List;

public class DataDelta {
	
	private List<ItemChange> changes;
	private List<ItemChange> deletions;

	public DataDelta(List<ItemChange> changes, List<ItemChange> deletions) {
		this.changes = changes;
		this.deletions = deletions;
	}

	public List<ItemChange> getChanges() {
		return changes;
	}

	public List<ItemChange> getDeletions() {
		return deletions;
	}
	
	

}
