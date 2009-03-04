package org.obm.push.backend.obm22;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.push.backend.IContentsExporter;
import org.obm.push.backend.ItemChange;
import org.obm.push.backend.ItemType;
import org.obm.push.state.SyncState;

public class ContentsExporter implements IContentsExporter {

	private static final Log logger = LogFactory.getLog(ContentsExporter.class);
	private SyncState state;

	@Override
	public void configure(String dataClass, Integer filterType, SyncState state,
			int i, int j) {
		logger.info("configure(" + dataClass + ", " + filterType + ", "
				+ state + ", " + i + ", " + j + ")");
		this.state = state;
	}

	@Override
	public SyncState getState() {
		return state;
	}

	public void synchronize() {
		logger.info("synchronize");
		List<ItemChange> lic = getCalendarChanges(state.getLastSync());
		addChanges(lic);
		lic = getCalendarDeletions(state.getLastSync());
		addDeletions(lic);

		lic = getContactsChanges(state.getLastSync());
		addChanges(lic);
		lic = getContactsDeletions(state.getLastSync());
		addDeletions(lic);

		lic = getTasksChanges(state.getLastSync());
		addChanges(lic);
		lic = getTasksDeletions(state.getLastSync());
		addDeletions(lic);

		lic = getMailChanges(state.getLastSync());
		addChanges(lic);
		lic = getMailDeletions(state.getLastSync());
		addDeletions(lic);
	}

	private void addDeletions(List<ItemChange> lic) {
		// TODO Auto-generated method stub
		
	}

	private void addChanges(List<ItemChange> lic) {
		// TODO Auto-generated method stub
		
	}

	private List<ItemChange> getContactsChanges(Date lastSync) {
		LinkedList<ItemChange> ret = new LinkedList<ItemChange>();
		// TODO Auto-generated method stub
		return ret;
	}

	private List<ItemChange> getContactsDeletions(Date lastSync) {
		LinkedList<ItemChange> ret = new LinkedList<ItemChange>();
		// TODO Auto-generated method stub
		return ret;
	}

	private List<ItemChange> getTasksChanges(Date lastSync) {
		LinkedList<ItemChange> ret = new LinkedList<ItemChange>();
		// TODO Auto-generated method stub
		return ret;
	}

	private List<ItemChange> getTasksDeletions(Date lastSync) {
		LinkedList<ItemChange> ret = new LinkedList<ItemChange>();
		// TODO Auto-generated method stub
		return ret;
	}

	private List<ItemChange> getCalendarChanges(Date lastSync) {
		LinkedList<ItemChange> ret = new LinkedList<ItemChange>();
		// TODO Auto-generated method stub
		return ret;
	}

	private List<ItemChange> getCalendarDeletions(Date lastSync) {
		LinkedList<ItemChange> ret = new LinkedList<ItemChange>();
		// TODO Auto-generated method stub
		return ret;
	}

	private List<ItemChange> getMailChanges(Date lastSync) {
		LinkedList<ItemChange> ret = new LinkedList<ItemChange>();
		
		// FIXME fake data
		ItemChange ic = new ItemChange();
		ic.setDisplayName("Mail subject");
		ic.setServerId("358");
		ic.setItemType(ItemType.DEFAULT_TASKS_FOLDER);
		ic.setParentId("0");
		ret.add(ic);
		
		// TODO Auto-generated method stub
		return ret;
	}

	private List<ItemChange> getMailDeletions(Date lastSync) {
		LinkedList<ItemChange> ret = new LinkedList<ItemChange>();
		// TODO Auto-generated method stub
		return ret;
	}

	@Override
	public List<ItemChange> getChanged() {
		LinkedList<ItemChange> changes = new LinkedList<ItemChange>();
		changes.addAll(getMailChanges(getState().getLastSync()));
		return changes;
	}

	@Override
	public int getCount() {
		return getChanged().size();
	}

	@Override
	public List<ItemChange> getDeleted() {
		return new LinkedList<ItemChange>();
	}

}
