package org.obm.push.backend.obm22.calendar;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.obm.push.backend.BackendSession;
import org.obm.push.backend.DataDelta;
import org.obm.push.backend.FolderType;
import org.obm.push.backend.ItemChange;
import org.obm.push.backend.MSAttendee;
import org.obm.push.backend.MSEvent;
import org.obm.push.backend.obm22.impl.ObmSyncBackend;
import org.obm.push.data.calendarenum.AttendeeStatus;
import org.obm.push.data.calendarenum.AttendeeType;
import org.obm.push.exception.ActiveSyncException;
import org.obm.push.store.ISyncStorage;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.calendar.CalendarInfo;
import org.obm.sync.calendar.Event;
import org.obm.sync.client.calendar.CalendarClient;
import org.obm.sync.items.EventChanges;

public class CalendarBackend extends ObmSyncBackend {

	public CalendarBackend(ISyncStorage storage) {
		super(storage);
	}

	private String getDefaultCalendarName(BackendSession bs) {
		return "obm:\\\\" + bs.getLoginAtDomain() + "\\calendar\\"
				+ bs.getLoginAtDomain();
	}

	public List<ItemChange> getHierarchyChanges(BackendSession bs) {
		List<ItemChange> ret = new LinkedList<ItemChange>();

		if (!bs.checkHint("hint.multipleCalendars", false)) {
			ItemChange ic = new ItemChange();
			String col = getDefaultCalendarName(bs);
			String serverId;
			try {
				serverId = getServerIdFor(bs.getDevId(), col, null);
			} catch (ActiveSyncException e) {
				serverId = createCollectionMapping(bs.getDevId(), col);
				ic.setIsNew(true);
			}
			ic.setServerId(serverId);
			ic.setParentId("0");
			ic.setDisplayName(bs.getLoginAtDomain() + " calendar");
			ic.setItemType(FolderType.DEFAULT_CALENDAR_FOLDER);
			ret.add(ic);
			return ret;
		}

		CalendarClient cc = getCalendarClient(bs);
		AccessToken token = cc.login(bs.getLoginAtDomain(), bs.getPassword(),
				"o-push");
		try {
			CalendarInfo[] cals = cc.listCalendars(token);

			int idx = bs.getLoginAtDomain().indexOf("@");
			String domain = bs.getLoginAtDomain().substring(idx);

			for (CalendarInfo ci : cals) {
				ItemChange ic = new ItemChange();
				String col = "obm:\\\\" + bs.getLoginAtDomain()
						+ "\\calendar\\" + ci.getUid() + domain;
				ic.setServerId(getServerIdFor(bs.getDevId(), col, null));
				ic.setParentId("0");
				ic.setDisplayName(ci.getMail() + " calendar");
				if (bs.getLoginAtDomain().equalsIgnoreCase(ci.getMail())) {
					ic.setItemType(FolderType.DEFAULT_CALENDAR_FOLDER);
				} else {
					ic.setItemType(FolderType.USER_CREATED_CALENDAR_FOLDER);
				}
				ret.add(ic);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		cc.logout(token);

		return ret;
	}

	public DataDelta getContentChanges(BackendSession bs, String collection) {
		List<ItemChange> addUpd = new LinkedList<ItemChange>();
		List<ItemChange> deletions = new LinkedList<ItemChange>();

		Date ls = bs.getState().getLastSync();
		CalendarClient cc = getCalendarClient(bs);
		AccessToken token = cc.login(bs.getLoginAtDomain(), bs.getPassword(),
				"o-push");
		String calendar = parseCalendarId(collection);
		try {
			EventChanges changes = null;
			if (bs.getState().isLastSyncFiltred()) {
				changes = cc.getSyncEventDate(token, calendar, ls);
			} else {
				changes = cc.getSync(token, calendar, ls);
			}
			Event[] evs = changes.getUpdated();
			for (Event e : evs) {
				if (e.getRecurrenceId() == null) {
					ItemChange change = addCalendarChange(bs.getDevId(),
							collection, e);
					addUpd.add(change);
				}
			}
			for (String del : changes.getRemoved()) {
				deletions.add(getDeletion(bs, collection, del));
			}

			bs.addUpdatedSyncDate(
					getCollectionIdFor(bs.getDevId(), collection), changes
							.getLastSync());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		cc.logout(token);
		logger.info("getContentChanges(" + calendar + ", " + collection
				+ ", lastSync: " + ls + ") => " + addUpd.size() + " entries.");
		return new DataDelta(addUpd, deletions);
	}

	private String parseCalendarId(String collectionId) {
		// parse obm:\\thomas@zz.com\calendar\sylvaing@zz.com
		logger.info("collectionId: " + collectionId);
		int slash = collectionId.lastIndexOf("\\");
		int at = collectionId.lastIndexOf("@");

		return collectionId.substring(slash + 1, at);
	}

	private ItemChange addCalendarChange(String deviceId, String collection,
			Event e) throws ActiveSyncException {
		ItemChange ic = new ItemChange();
		ic.setServerId(getServerIdFor(deviceId, collection, e.getUid()));
		MSEvent cal = new EventConverter().convertEvent(e);
		ic.setData(cal);
		return ic;
	}

	public String createOrUpdate(BackendSession bs, String collectionId,
			String serverId, String clientId, MSEvent data)
			throws ActiveSyncException {
		logger.info("createOrUpdate(" + bs.getLoginAtDomain() + ", "
				+ collectionId + ", " + serverId + ", " + clientId + ", "
				+ data.getSubject() + ")");
		CalendarClient cc = getCalendarClient(bs);
		AccessToken token = cc.login(bs.getLoginAtDomain(), bs.getPassword(),
				"o-push");
		String id = null;
		if (serverId != null) {
			id = serverId;
		}

		if (data.getAttendees() == null || data.getAttendees().isEmpty()) {
			String email = bs.getLoginAtDomain();
			try {
				email = cc.getUserEmail(token);
			} catch (Exception e) {
				logger.error("Error finding email: " + e.getMessage(), e);
			}
			List<MSAttendee> msal = new ArrayList<MSAttendee>(1);
			MSAttendee at = new MSAttendee();
			at.setEmail(email);
			at.setAttendeeStatus(AttendeeStatus.ACCEPT);
			at.setAttendeeType(AttendeeType.REQUIRED);
			msal.add(at);
			data.setAttendees(msal);
		}
		Event event = new EventConverter().convertEvent(data);
		if (id != null) {
			int idx = id.lastIndexOf(":");
			id = id.substring(idx + 1);
			try {
				event.setUid(id);
				cc.modifyEvent(token, parseCalendarId(collectionId), event,
						true);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		} else {
			try {
				id = cc
						.createEvent(token, parseCalendarId(collectionId),
								event);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
		cc.logout(token);

		return getServerIdFor(bs.getDevId(), collectionId, id);
	}

	public void delete(BackendSession bs, String collectionId, String serverId) {
		logger.info("delete serverId " + serverId);
		if (serverId != null) {
			int idx = serverId.indexOf(":");
			if (idx > 0) {
				String id = serverId.substring(idx + 1);
				CalendarClient bc = getCalendarClient(bs);
				AccessToken token = bc.login(bs.getLoginAtDomain(), bs
						.getPassword(), "o-push");
				try {
					bc.removeEvent(token, parseCalendarId(collectionId), id);
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
				bc.logout(token);
			}
		}
	}

	public String updateUserStatus(BackendSession bs, MSEvent data,
			AttendeeStatus status) {
		CalendarClient calCli = getCalendarClient(bs);
		AccessToken at = calCli.login(bs.getLoginAtDomain(), bs.getPassword(),
				"o-push");
		try {
			for (MSAttendee att : data.getAttendees()) {
				if (bs.getLoginAtDomain().equalsIgnoreCase(att.getEmail())) {
					att.setAttendeeStatus(status);
				}
			}

			int ar = bs.getLoginAtDomain().lastIndexOf("@");
			String calendar = bs.getLoginAtDomain().substring(0, ar);
			logger.info("update user status[" + status.toString()
					+ "] in calendar " + calendar);

			Event event = new EventConverter().convertEvent(data);
			event = calCli.modifyEvent(at, calendar, event, true);
			return getServerIdFor(bs.getDevId(), getDefaultCalendarName(bs)
					+ "", event.getUid());

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			calCli.logout(at);
		}
		return null;
	}
}
