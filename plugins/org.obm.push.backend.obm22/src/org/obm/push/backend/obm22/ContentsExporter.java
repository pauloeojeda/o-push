package org.obm.push.backend.obm22;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.push.backend.BackendSession;
import org.obm.push.backend.IContentsExporter;
import org.obm.push.backend.ItemChange;
import org.obm.push.backend.PIMDataType;
import org.obm.push.backend.obm22.calendar.CalendarExporter;
import org.obm.push.backend.obm22.mail.MailExporter;
import org.obm.push.state.SyncState;

public class ContentsExporter implements IContentsExporter {

	private static final Log logger = LogFactory.getLog(ContentsExporter.class);

	private MailExporter mailExporter;
	private CalendarExporter calendarExporter;

	public ContentsExporter(MailExporter mailExporter,
			CalendarExporter calendarExporter) {
		super();
		this.mailExporter = mailExporter;
		this.calendarExporter = calendarExporter;
	}

	@Override
	public void configure(BackendSession bs, String dataClass,
			Integer filterType, SyncState state, int i, int j) {
		logger.info("configure(" + dataClass + ", " + filterType + ", " + state
				+ ", " + i + ", " + j + ")");
		bs.setState(state);
		bs.setDataType(PIMDataType.valueOf(dataClass.toUpperCase()));
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

		lic = getContactsChanges(bs);
		addChanges(lic);
		lic = getContactsDeletions(bs.getState().getLastSync());
		addDeletions(lic);

		lic = getTasksChanges(bs);
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

	private List<ItemChange> getContactsChanges(BackendSession bs) {
		LinkedList<ItemChange> ret = new LinkedList<ItemChange>();
		// TODO Auto-generated method stub
		return ret;
	}

	private List<ItemChange> getContactsDeletions(Date lastSync) {
		LinkedList<ItemChange> ret = new LinkedList<ItemChange>();
		// TODO Auto-generated method stub
		return ret;
	}

	private List<ItemChange> getTasksChanges(BackendSession bs) {
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
		return calendarExporter.getContentChanges(bs);
	}

	private List<ItemChange> getCalendarDeletions(Date lastSync) {
		LinkedList<ItemChange> ret = new LinkedList<ItemChange>();
		// TODO Auto-generated method stub
		return ret;
	}

	private List<ItemChange> getMailChanges(BackendSession bs) {
		return mailExporter.getContentChanges(bs);
	}

	private List<ItemChange> getMailDeletions(Date lastSync) {
		LinkedList<ItemChange> ret = new LinkedList<ItemChange>();
		// TODO Auto-generated method stub
		return ret;
	}

	@Override
	public List<ItemChange> getChanged(BackendSession bs) {
		LinkedList<ItemChange> changes = new LinkedList<ItemChange>();
		// TODO switch dataClass
		switch (bs.getDataType()) {
		case CALENDAR:
			changes.addAll(getCalendarChanges(bs));
			break;
		case CONTACT:
			changes.addAll(getContactsChanges(bs));
			break;
		case EMAIL:
			changes.addAll(getMailChanges(bs));
			break;
		case TASK:
			changes.addAll(getTasksChanges(bs));
			break;
		}

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

	@Override
	public List<ItemChange> fetch(BackendSession bs, List<String> fetchIds) {
		LinkedList<ItemChange> changes = new LinkedList<ItemChange>();
		switch (bs.getDataType()) {
			case CALENDAR:
				break;
			case CONTACT:
				break;
			case EMAIL:
				changes.addAll(mailExporter.fetchItems(fetchIds));
				break;
			case TASK:
				break;
		
		}
		return changes;
	}

}
