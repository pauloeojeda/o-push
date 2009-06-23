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
import org.obm.push.backend.obm22.contacts.ContactsBackend;
import org.obm.push.backend.obm22.mail.MailBackend;
import org.obm.push.state.SyncState;

public class ContentsExporter implements IContentsExporter {

	private static final Log logger = LogFactory.getLog(ContentsExporter.class);

	private MailBackend mailExporter;
	private CalendarBackend calBackend;

	private ContactsBackend contactsBackend;

	public ContentsExporter(MailBackend mailExporter,
			CalendarBackend calendarExporter, ContactsBackend contactsBackend) {
		super();
		this.mailExporter = mailExporter;
		this.calBackend = calendarExporter;
		this.contactsBackend = contactsBackend;
	}

	@Override
	public void configure(BackendSession bs, String dataClass,
			Integer filterType, SyncState state, String collectionId) {
		logger.info("configure(" + dataClass + ", " + filterType + ", " + state
				+ ", " + collectionId + ")");
		bs.setState(state);
		if (dataClass != null) {
			bs.setDataType(PIMDataType.valueOf(dataClass.toUpperCase()));
		} else if (collectionId.contains("\\calendar\\")) {
			bs.setDataType(PIMDataType.CALENDAR);
		} else if (collectionId.endsWith("\\contacts")) {
			bs.setDataType(PIMDataType.CONTACTS);
		} else {
			bs.setDataType(PIMDataType.EMAIL);
		}
	}

	@Override
	public SyncState getState(BackendSession bs) {
		return bs.getState();
	}

	private DataDelta getContactsChanges(BackendSession bs, String collectionId) {
		return contactsBackend.getContentChanges(bs, collectionId);
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
			delta = getContactsChanges(bs, collectionId);
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
