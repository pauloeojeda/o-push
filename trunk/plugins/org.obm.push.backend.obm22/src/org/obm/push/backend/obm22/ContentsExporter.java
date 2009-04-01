package org.obm.push.backend.obm22;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.push.backend.BackendSession;
import org.obm.push.backend.DataDelta;
import org.obm.push.backend.IContentsExporter;
import org.obm.push.backend.ItemChange;
import org.obm.push.backend.PIMDataType;
import org.obm.push.backend.obm22.calendar.CalendarBackend;
import org.obm.push.backend.obm22.mail.MailBackend;
import org.obm.push.state.SyncState;

public class ContentsExporter implements IContentsExporter {

	private static final Log logger = LogFactory.getLog(ContentsExporter.class);

	private MailBackend mailExporter;
	private CalendarBackend calBackend;

	public ContentsExporter(MailBackend mailExporter,
			CalendarBackend calendarExporter) {
		super();
		this.mailExporter = mailExporter;
		this.calBackend = calendarExporter;
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

	private DataDelta getContactsChanges(BackendSession bs) {
		LinkedList<ItemChange> ret = new LinkedList<ItemChange>();
		// TODO Auto-generated method stub
		return new DataDelta(ret, ret);
	}

	private DataDelta getTasksChanges(BackendSession bs) {
		LinkedList<ItemChange> ret = new LinkedList<ItemChange>();
		// TODO Auto-generated method stub
		return new DataDelta(ret, ret);
	}

	private DataDelta getCalendarChanges(BackendSession bs, String collectionId) {
		return calBackend.getContentChanges(bs, collectionId);
	}

	private DataDelta getMailChanges(BackendSession bs) {
		return mailExporter.getContentChanges(bs);
	}

	@Override
	public DataDelta getChanged(BackendSession bs, String collectionId) {
		DataDelta delta = null;
		switch (bs.getDataType()) {
		case CALENDAR:
			delta = getCalendarChanges(bs, collectionId);
			break;
		case CONTACTS:
			delta = getContactsChanges(bs);
			break;
		case EMAIL:
			delta = getMailChanges(bs);
			break;
		case TASKS:
			delta = getTasksChanges(bs);
			break;
		}

		return delta;
	}

	@Override
	public int getCount(BackendSession bs, String collectionId) {
		return getChanged(bs, collectionId).getChanges().size();
	}

	@Override
	public List<ItemChange> fetch(BackendSession bs, List<String> fetchIds) {
		LinkedList<ItemChange> changes = new LinkedList<ItemChange>();
		switch (bs.getDataType()) {
			case CALENDAR:
				break;
			case CONTACTS:
				break;
			case EMAIL:
				changes.addAll(mailExporter.fetchItems(fetchIds));
				break;
			case TASKS:
				break;
		
		}
		return changes;
	}

}
