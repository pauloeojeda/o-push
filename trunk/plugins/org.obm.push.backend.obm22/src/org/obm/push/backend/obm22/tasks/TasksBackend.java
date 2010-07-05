package org.obm.push.backend.obm22.tasks;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.obm.push.backend.BackendSession;
import org.obm.push.backend.DataDelta;
import org.obm.push.backend.FolderType;
import org.obm.push.backend.ItemChange;
import org.obm.push.backend.MSTask;
import org.obm.push.backend.obm22.impl.ObmSyncBackend;
import org.obm.push.exception.ActiveSyncException;
import org.obm.push.state.SyncState;
import org.obm.push.store.ISyncStorage;

public class TasksBackend extends ObmSyncBackend {

	private static Map<String, Set<String>> syncTasks;

	static {
		syncTasks = new HashMap<String, Set<String>>();
	}

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
		String serverId;
		try {
			serverId = getServerIdFor(bs.getDevId(), col, null);
		} catch (ActiveSyncException e) {
			serverId = createCollectionMapping(bs.getDevId(), col);
			ic.setIsNew(true);
		}
		ic.setServerId(serverId);
		ic.setParentId("0");
		ic.setDisplayName(bs.getLoginAtDomain() + " tasks");
		ic.setItemType(FolderType.DEFAULT_TASKS_FOLDER);
		ret.add(ic);
		return ret;

	}

	public String createOrUpdate(BackendSession bs, String collectionId,
			String serverId, String clientId, MSTask data)
			throws ActiveSyncException {
		Set<String> ids = syncTasks.get(bs.getDevId());
		if (ids == null) {
			ids = new HashSet<String>();
			syncTasks.put(bs.getDevId(), ids);
		}
		String itemId = getServerIdFor(bs.getDevId(), collectionId, clientId);
		ids.add(itemId);
		return itemId;
	}

	public DataDelta getContentChanges(BackendSession bs, SyncState state,
			String collection) {
		List<ItemChange> addUpd = new LinkedList<ItemChange>();
		List<ItemChange> deletions = new LinkedList<ItemChange>();
		Set<String> uids = syncTasks.get(bs.getDevId());
		if (uids != null) {
			for (String serverId : uids) {
				ItemChange ic = new ItemChange();
				ic.setServerId(serverId);
				ic.setData(null);
				addUpd.add(ic);
			}
			uids.clear();
		}
		return new DataDelta(addUpd, deletions);
	}

	public void delete(BackendSession bs, String serverId) {

	}

	public Collection<? extends ItemChange> fetchItems(BackendSession bs,
			List<String> fetchServerIds) {
		return null;
	}
}
