package org.obm.push.backend.obm22.tasks;

import java.util.LinkedList;
import java.util.List;

import org.obm.push.backend.BackendSession;
import org.obm.push.backend.FolderType;
import org.obm.push.backend.ItemChange;
import org.obm.push.backend.obm22.impl.ObmSyncBackend;
import org.obm.push.store.ISyncStorage;

public class TasksBackend extends ObmSyncBackend {

	public TasksBackend(ISyncStorage storage) {
		super(storage);
	}

	private String getDefaultCalendarName(BackendSession bs) {
		return "obm:\\\\" + bs.getLoginAtDomain() + "\\tasks\\"
				+ bs.getLoginAtDomain();
	}

	public List<ItemChange> getHierarchyChanges(BackendSession bs) {
		List<ItemChange> ret = new LinkedList<ItemChange>();

		ItemChange ic = new ItemChange();
		String col = getDefaultCalendarName(bs);
		ic.setServerId(getServerIdFor(bs.getDevId(), col, null));
		ic.setParentId("0");
		ic.setDisplayName(bs.getLoginAtDomain()+" tasks");
		ic.setItemType(FolderType.DEFAULT_TASKS_FOLDER);
		ret.add(ic);
		return ret;

	}

//	public DataDelta getContentChanges(BackendSession bs, String collectionId) {
//		List<ItemChange> addUpd = new LinkedList<ItemChange>();
//		List<ItemChange> deletions = new LinkedList<ItemChange>();
//		return new DataDelta(addUpd, deletions);
//	}
}
