package org.obm.push.backend.obm22;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.push.backend.BackendSession;
import org.obm.push.backend.DataDelta;
import org.obm.push.backend.FilterType;
import org.obm.push.backend.IContentsExporter;
import org.obm.push.backend.ItemChange;
import org.obm.push.backend.MSAttachementData;
import org.obm.push.backend.PIMDataType;
import org.obm.push.backend.obm22.calendar.CalendarBackend;
import org.obm.push.backend.obm22.contacts.ContactsBackend;
import org.obm.push.backend.obm22.mail.MailBackend;
import org.obm.push.state.SyncState;

public class ContentsExporter implements IContentsExporter {

	private static final Log logger = LogFactory.getLog(ContentsExporter.class);

	private MailBackend mailBackend;
	private CalendarBackend calBackend;

	private ContactsBackend contactsBackend;

	public ContentsExporter(MailBackend mailBackend,
			CalendarBackend calendarExporter, ContactsBackend contactsBackend) {
		super();
		this.mailBackend = mailBackend;
		this.calBackend = calendarExporter;
		this.contactsBackend = contactsBackend;
	}

	@Override
	public void configure(BackendSession bs, String dataClass,
			FilterType filterType, SyncState state, String collectionId) {
		logger.info("configure(" + dataClass + ", " + filterType + ", " + state
				+ ", " + collectionId + ")");
		if (collectionId == null) {
			logger.warn("null collection, skipping");
			return;
		}

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
		return new DataDelta(ret, ret);
	}

	private DataDelta getCalendarChanges(BackendSession bs, String collectionId) {
		return calBackend.getContentChanges(bs, collectionId);
	}

	private DataDelta getMailChanges(BackendSession bs, String collectionId) {
		return mailBackend.getContentChanges(bs, collectionId);
	}

	@Override
	public DataDelta getChanged(BackendSession bs, String collectionId) {
		logger.info("getChanged: " + bs + " collectionId: " + collectionId);
		DataDelta delta = null;
		switch (bs.getDataType()) {
		case CALENDAR:
			delta = getCalendarChanges(bs, collectionId);
			break;
		case CONTACTS:
			delta = getContactsChanges(bs, collectionId);
			break;
		case EMAIL:
			delta = getMailChanges(bs, collectionId);
			break;
		case TASKS:
			delta = getTasksChanges(bs);
			break;
		}

		return delta;
	}

	@Override
	public int getCount(BackendSession bs, String collectionId) {
		DataDelta dd = getChanged(bs, collectionId);
		return (dd.getChanges().size() + dd.getDeletions().size());
	}

	@Override
	public List<ItemChange> fetch(BackendSession bs, List<String> fetchServerIds) {
		LinkedList<ItemChange> changes = new LinkedList<ItemChange>();
		switch (bs.getDataType()) {
		case CALENDAR:
			break;
		case CONTACTS:
			break;
		case EMAIL:
			 changes.addAll(mailBackend.fetchItems(bs,fetchServerIds));
			break;
		case TASKS:
			break;

		}
		return changes;
	}
	
	@Override
	public Integer getDefaultCalendarId(BackendSession bs){
		return calBackend.getDefaultCalendarId(bs);
	}
	
	@Override
	public MSAttachementData getEmailAttachement(BackendSession bs,
			String attachmentId) {
		return mailBackend.getAttachment(bs, attachmentId);
	}
}
