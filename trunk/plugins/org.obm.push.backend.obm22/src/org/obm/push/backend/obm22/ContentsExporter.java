package org.obm.push.backend.obm22;

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

	private List<ItemChange> getContactsChanges(BackendSession bs) {
		LinkedList<ItemChange> ret = new LinkedList<ItemChange>();
		// TODO Auto-generated method stub
		return ret;
	}

	private List<ItemChange> getContactsDeletions(BackendSession bs, String collectionId) {
		LinkedList<ItemChange> ret = new LinkedList<ItemChange>();
		// TODO Auto-generated method stub
		return ret;
	}

	private List<ItemChange> getTasksChanges(BackendSession bs) {
		LinkedList<ItemChange> ret = new LinkedList<ItemChange>();
		// TODO Auto-generated method stub
		return ret;
	}

	private List<ItemChange> getTasksDeletions(BackendSession bs, String collectionId) {
		LinkedList<ItemChange> ret = new LinkedList<ItemChange>();
		// TODO Auto-generated method stub
		return ret;
	}

	private List<ItemChange> getCalendarChanges(BackendSession bs, String collectionId) {
		return calendarExporter.getContentChanges(bs, collectionId);
	}

	private List<ItemChange> getCalendarDeletions(BackendSession bs, String collectionId) {
		LinkedList<ItemChange> ret = new LinkedList<ItemChange>();
		// TODO Auto-generated method stub
		return ret;
	}

	private List<ItemChange> getMailChanges(BackendSession bs) {
		return mailExporter.getContentChanges(bs);
	}

	private List<ItemChange> getMailDeletions(BackendSession bs, String collectionId) {
		LinkedList<ItemChange> ret = new LinkedList<ItemChange>();
		// TODO Auto-generated method stub
		return ret;
	}

	@Override
	public List<ItemChange> getChanged(BackendSession bs, String collectionId) {
		LinkedList<ItemChange> changes = new LinkedList<ItemChange>();
		switch (bs.getDataType()) {
		case CALENDAR:
			changes.addAll(getCalendarChanges(bs, collectionId));
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
	public int getCount(BackendSession bs, String collectionId) {
		return getChanged(bs, collectionId).size();
	}

	@Override
	public List<ItemChange> getDeleted(BackendSession bs, String collectionId) {
		LinkedList<ItemChange> changes = new LinkedList<ItemChange>();
		switch (bs.getDataType()) {
		case CALENDAR:
			changes.addAll(getCalendarDeletions(bs, collectionId));
			break;
		case CONTACT:
			changes.addAll(getContactsDeletions(bs, collectionId));
			break;
		case EMAIL:
			changes.addAll(getMailDeletions(bs, collectionId));
			break;
		case TASK:
			changes.addAll(getTasksDeletions(bs, collectionId));
			break;
		}

		return changes;
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
