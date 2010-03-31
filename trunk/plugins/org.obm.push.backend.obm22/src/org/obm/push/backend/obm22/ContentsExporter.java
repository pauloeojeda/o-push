package org.obm.push.backend.obm22;

import java.util.ArrayList;
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
import org.obm.push.backend.SearchResult;
import org.obm.push.backend.StoreName;
import org.obm.push.backend.obm22.calendar.CalendarBackend;
import org.obm.push.backend.obm22.contacts.ContactsBackend;
import org.obm.push.backend.obm22.mail.MailBackend;
import org.obm.push.exception.ActiveSyncException;
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
		} else if (collectionId.contains("\\tasks")) {
			bs.setDataType(PIMDataType.TASKS);
		} else {
			bs.setDataType(PIMDataType.EMAIL);
		}
	}

	private void proccessFilterType(BackendSession bs, FilterType filterType) {
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

			if (bs.getState().getLastSync() != null
					&& cal.getTime().after(bs.getState().getLastSync())) {
				bs.getState().setLastSync(cal.getTime());
				bs.getState().setLastSyncFiltred(true);
			}
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
	public DataDelta getChanged(BackendSession bs, FilterType filterType,
			String collectionId) {
		DataDelta delta = null;
		switch (bs.getDataType()) {
		case CALENDAR:
			proccessFilterType(bs, filterType);
			logger.info("getChanged: " + bs.getState().getLastSync());
			delta = getCalendarChanges(bs, collectionId);
			break;
		case CONTACTS:
			delta = getContactsChanges(bs, collectionId);
			break;
		case EMAIL:
			proccessFilterType(bs, filterType);
			logger.info("getChanged: " + bs.getState().getLastSync());
			delta = getMailChanges(bs, collectionId);
			break;
		case TASKS:
			delta = getTasksChanges(bs);
			break;
		}
		logger.info("Get changed from " + bs.getState().getLastSync()
				+ " on collectionId[" + collectionId + "]");
		return delta;
	}

	@Override
	public int getCount(BackendSession bs, FilterType filterType,
			String collectionId) {
		DataDelta dd = getChanged(bs, filterType, collectionId);
		return (dd.getChanges().size() + dd.getDeletions().size());
	}

	@Override
	public List<ItemChange> fetch(BackendSession bs, List<String> fetchServerIds)
			throws ActiveSyncException {
		LinkedList<ItemChange> changes = new LinkedList<ItemChange>();
		switch (bs.getDataType()) {
		case CALENDAR:
			break;
		case CONTACTS:
			break;
		case EMAIL:
			changes.addAll(mailBackend.fetchItems(bs, fetchServerIds));
			break;
		case TASKS:
			break;

		}
		return changes;
	}

	@Override
	public MSAttachementData getEmailAttachement(BackendSession bs,
			String attachmentId) {
		return mailBackend.getAttachment(bs, attachmentId);
	}

	@Override
	public boolean validatePassword(String loginAtDomain, String password) {
		return calBackend.validatePassword(loginAtDomain, password);
	}

	@Override
	public List<SearchResult> search(BackendSession bs, StoreName storeName, String query,
			Integer rangeLower, Integer rangeUpper) {
		switch (storeName) {
		case GAL:
			return contactsBackend.search(bs, query,rangeLower, rangeUpper);	
		case DocumentLibrary:
			break;
		case Mailbox:
			break;
		default:
			break;
		}
		return new ArrayList<SearchResult>(0);
	}
}
