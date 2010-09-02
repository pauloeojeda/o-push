package org.obm.push.backend.obm22;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

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
import org.obm.push.exception.ActiveSyncException;
import org.obm.push.exception.ObjectNotFoundException;
import org.obm.push.state.SyncState;

public class ContentsExporter implements IContentsExporter {

	private static final Log logger = LogFactory.getLog(ContentsExporter.class);

	private MailBackend mailBackend;
	private CalendarBackend calBackend;
	private ContactsBackend contactsBackend;
//	private TasksBackend tasksBackend;

	public ContentsExporter(MailBackend mailBackend,
			CalendarBackend calendarExporter, ContactsBackend contactsBackend) {
		super();
		this.mailBackend = mailBackend;
		this.calBackend = calendarExporter;
		this.contactsBackend = contactsBackend;
//		this.tasksBackend = tasksBackend;
	}

	private void proccessFilterType(SyncState state, FilterType filterType) {
		if (filterType != null) {
			// FILTER_BY_NO_INCOMPLETE_TASKS;//8
			Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
			cal.set(Calendar.HOUR, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);

			switch (filterType) {
			case ONE_DAY_BACK:
				cal
						.set(Calendar.DAY_OF_YEAR, cal
								.get(Calendar.DAY_OF_YEAR) - 1);
				break;
			case THREE_DAYS_BACK:
				cal
						.set(Calendar.DAY_OF_YEAR, cal
								.get(Calendar.DAY_OF_YEAR) - 3);
				break;
			case ONE_WEEK_BACK:
				cal.set(Calendar.WEEK_OF_YEAR,
						cal.get(Calendar.WEEK_OF_YEAR) - 1);
				break;
			case TWO_WEEKS_BACK:
				cal.set(Calendar.WEEK_OF_YEAR,
						cal.get(Calendar.WEEK_OF_YEAR) - 2);
				break;
			case ONE_MONTHS_BACK:
				cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) - 1);
				break;
			case THREE_MONTHS_BACK:
				cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) - 3);
				break;
			case SIX_MONTHS_BACK:
				cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) - 3);
				break;
			default:
			case ALL_ITEMS:
				cal.setTimeInMillis(0);
				break;
			}

			if (state.getLastSync() != null
					&& cal.getTime().after(state.getLastSync())) {
				state.setLastSync(cal.getTime());
				state.setLastSyncFiltred(true);
			}
		}
	}

	private DataDelta getContactsChanges(BackendSession bs, SyncState state,
			String collectionId) {
		return contactsBackend.getContentChanges(bs, state, collectionId);
	}

	private DataDelta getTasksChanges(BackendSession bs, SyncState state,
			String collectionId) {
		return this.calBackend.getContentChanges(bs, state, collectionId);
	}

	private DataDelta getCalendarChanges(BackendSession bs, SyncState state,
			String collectionId) {
		return calBackend.getContentChanges(bs, state, collectionId);
	}

	private DataDelta getMailChanges(BackendSession bs, SyncState state,
			String collectionId) {
		return mailBackend.getContentChanges(bs, state, collectionId);
	}

	@Override
	public DataDelta getChanged(BackendSession bs, SyncState state,
			FilterType filterType, String collectionId) {
		DataDelta delta = null;
		switch (state.getDataType()) {
		case CALENDAR:
			proccessFilterType(state, filterType);
			logger.info("getChanged: " + state.getLastSync());
			delta = getCalendarChanges(bs, state, collectionId);
			break;
		case CONTACTS:
			delta = getContactsChanges(bs, state, collectionId);
			break;
		case EMAIL:
			proccessFilterType(state, filterType);
			logger.info("getChanged: " + state.getLastSync());
			delta = getMailChanges(bs, state, collectionId);
			break;
		case TASKS:
			delta = getTasksChanges(bs, state, collectionId);
			break;
		}
		logger.info("Get changed from " + state.getLastSync()
				+ " on collectionId[" + collectionId + "]");
		return delta;
	}

	@Override
	public int getCount(BackendSession bs, SyncState state,
			FilterType filterType, String collectionId) {
		DataDelta dd = getChanged(bs, state, filterType, collectionId);
		return dd.getChanges().size();
	}

	@Override
	public List<ItemChange> fetch(BackendSession bs, PIMDataType getDataType,
			List<String> fetchServerIds) throws ActiveSyncException {
		LinkedList<ItemChange> changes = new LinkedList<ItemChange>();
		switch (getDataType) {
		case CALENDAR:
			changes.addAll(calBackend.fetchItems(bs, fetchServerIds));
			break;
		case CONTACTS:
			changes.addAll(contactsBackend.fetchItems(bs, fetchServerIds));
			break;
		case EMAIL:
			changes.addAll(mailBackend.fetchItems(bs, fetchServerIds));
			break;
		case TASKS:
			 changes.addAll(calBackend.fetchItems(bs, fetchServerIds));
			break;
		}
		return changes;
	}

	@Override
	public MSAttachementData getEmailAttachement(BackendSession bs,
			String attachmentId) throws ObjectNotFoundException {
		return mailBackend.getAttachment(bs, attachmentId);
	}

	@Override
	public boolean validatePassword(String loginAtDomain, String password) {
		return calBackend.validatePassword(loginAtDomain, password);
	}
}
