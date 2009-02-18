package org.obm.push.backend;

import java.util.LinkedList;
import java.util.List;

public class ImportHierarchyChangesMem implements IImporter {

	private List<ItemChange> deletions;
	private List<ItemChange> changes;

	public ImportHierarchyChangesMem() {
		deletions = new LinkedList<ItemChange>();
		changes = new LinkedList<ItemChange>();
	}

	public int getCount() {
		return deletions.size() + changes.size();
	}

	public List<ItemChange> getChanged() {
		return changes;
	}

	public List<ItemChange> getDeleted() {
		return deletions;
	}

	@Override
	public void addChanges(List<ItemChange> lic) {
		changes.addAll(lic);
	}

	@Override
	public void addDeletions(List<ItemChange> lic) {
		deletions.addAll(lic);
	}

}
