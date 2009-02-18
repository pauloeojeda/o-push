package org.obm.push.backend.obm22;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.push.backend.IExporter;
import org.obm.push.backend.IImporter;
import org.obm.push.backend.ItemChange;
import org.obm.push.backend.ItemType;
import org.obm.push.state.SyncState;

public class Exporter implements IExporter {

	private static final Log logger = LogFactory.getLog(Exporter.class);
	private IImporter imem;
	private SyncState state;

	@Override
	public void configure(IImporter imem, String dataClass, Integer filterType,
			SyncState state, int i, int j) {
		// TODO Auto-generated method stub
		logger.info("configure(imem, " + dataClass + ", " + filterType + ", "
				+ state + ", " + i + ", " + j + ")");
		this.imem = imem;
		this.state = state;
	}

	@Override
	public SyncState getState() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer getChangesCount() {
		return 1;
	}

	public void synchronize() {
		logger.info("synchronize");
		List<ItemChange> lic = getCalendarChanges(state.getLastSync());
		imem.addChanges(lic);
		lic = getCalendarDeletions(state.getLastSync());
		imem.addDeletions(lic);

		lic = getContactsChanges(state.getLastSync());
		imem.addChanges(lic);
		lic = getContactsDeletions(state.getLastSync());
		imem.addDeletions(lic);

		lic = getTasksChanges(state.getLastSync());
		imem.addChanges(lic);
		lic = getTasksDeletions(state.getLastSync());
		imem.addDeletions(lic);

		lic = getMailChanges(state.getLastSync());
		imem.addChanges(lic);
		lic = getMailDeletions(state.getLastSync());
		imem.addDeletions(lic);

	}

	private List<ItemChange> getContactsChanges(Date lastSync) {
		LinkedList<ItemChange> ret = new LinkedList<ItemChange>();
		// TODO Auto-generated method stub
		ItemChange ic = new ItemChange();
		ic.setServerId("obm://contacts/user@domain");
		ic.setParentId("0");
		ic.setDisplayName("OBM Contacts");
		ic.setItemType(ItemType.DEFAULT_CONTACTS_FOLDER);
		ret.add(ic);
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
		ItemChange ic = new ItemChange();
		ic.setServerId("obm://tasks/user@domain");
		ic.setParentId("0");
		ic.setDisplayName("OBM Tasks");
		ic.setItemType(ItemType.DEFAULT_TASKS_FOLDER);
		ret.add(ic);
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
		ItemChange ic = new ItemChange();
		ic.setServerId("obm://calendar/user@domain");
		ic.setParentId("0");
		ic.setDisplayName("OBM Calendar");
		ic.setItemType(ItemType.DEFAULT_CALENDAR_FOLDER);
		ret.add(ic);
		return ret;
	}

	private List<ItemChange> getCalendarDeletions(Date lastSync) {
		LinkedList<ItemChange> ret = new LinkedList<ItemChange>();
		// TODO Auto-generated method stub
		return ret;
	}

	private List<ItemChange> getMailChanges(Date lastSync) {
		LinkedList<ItemChange> ret = new LinkedList<ItemChange>();
		// TODO Auto-generated method stub
		ItemChange ic = new ItemChange();
		ic.setServerId("obm://mail/user@domain/INBOX");
		ic.setParentId("0");
		ic.setDisplayName("INBOX");
		ic.setItemType(ItemType.DEFAULT_INBOX_FOLDER);
		ret.add(ic);
		return ret;
	}

	private List<ItemChange> getMailDeletions(Date lastSync) {
		LinkedList<ItemChange> ret = new LinkedList<ItemChange>();
		// TODO Auto-generated method stub
		return ret;
	}

}
