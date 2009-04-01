package org.obm.push.backend.obm22;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.push.backend.BackendSession;
import org.obm.push.backend.IHierarchyExporter;
import org.obm.push.backend.ItemChange;
import org.obm.push.backend.PIMDataType;
import org.obm.push.backend.obm22.calendar.CalendarBackend;
import org.obm.push.backend.obm22.mail.MailBackend;
import org.obm.push.state.SyncState;

public class HierarchyExporter implements IHierarchyExporter {

	private static final Log logger = LogFactory
			.getLog(HierarchyExporter.class);

	private MailBackend mailExporter;
	private CalendarBackend calendarExporter;

	public HierarchyExporter(MailBackend mailExporter,
			CalendarBackend calendarExporter) {
		this.mailExporter = mailExporter;
		this.calendarExporter = calendarExporter;
	}

	@Override
	public void configure(BackendSession bs, String dataClass,
			Integer filterType, SyncState state, int i, int j) {
		logger.info("configure(bs, " + dataClass + ", " + filterType + ", "
				+ state + ", " + i + ", " + j + ")");
		bs.setState(state);
		if (dataClass != null) {
			bs.setDataType(PIMDataType.valueOf(dataClass.toUpperCase()));
		} else {
			bs.setDataType(null);
		}
	}

	@Override
	public SyncState getState(BackendSession bs) {
		return bs.getState();
	}

	public void synchronize(BackendSession bs) {
		logger.info("synchronize");
		List<ItemChange> lic = getCalendarChanges(bs);
		addChanges(lic);
		lic = getCalendarDeletions(bs.getState().getLastSync());
		addDeletions(lic);

		lic = getContactsChanges(bs.getState().getLastSync());
		addChanges(lic);
		lic = getContactsDeletions(bs.getState().getLastSync());
		addDeletions(lic);

		lic = getTasksChanges(bs.getState().getLastSync());
		addChanges(lic);
		lic = getTasksDeletions(bs.getState().getLastSync());
		addDeletions(lic);

		lic = getMailChanges(bs);
		addChanges(lic);
		lic = getMailDeletions(bs.getState().getLastSync());
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

	private List<ItemChange> getCalendarChanges(BackendSession bs) {
		return calendarExporter.getHierarchyChanges(bs);
	}

	private List<ItemChange> getCalendarDeletions(Date lastSync) {
		LinkedList<ItemChange> ret = new LinkedList<ItemChange>();
		// TODO Auto-generated method stub
		return ret;
	}

	private List<ItemChange> getMailChanges(BackendSession bs) {
		return mailExporter.getHierarchyChanges(bs);
	}

	private List<ItemChange> getMailDeletions(Date lastSync) {
		LinkedList<ItemChange> ret = new LinkedList<ItemChange>();
		// TODO Auto-generated method stub
		return ret;
	}

	@Override
	public List<ItemChange> getChanged(BackendSession bs) {
		LinkedList<ItemChange> changes = new LinkedList<ItemChange>();
		changes.addAll(getCalendarChanges(bs));
		changes.addAll(getMailChanges(bs));
		return changes;
	}

	@Override
	public int getCount(BackendSession bs) {
		return getChanged(bs).size();
	}

	@Override
	public List<ItemChange> getDeleted(BackendSession bs) {
		return new LinkedList<ItemChange>();
	}

}
