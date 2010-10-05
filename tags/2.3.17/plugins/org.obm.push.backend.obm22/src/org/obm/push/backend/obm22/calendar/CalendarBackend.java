package org.obm.push.backend.obm22.calendar;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.obm.push.backend.BackendSession;
import org.obm.push.backend.DataDelta;
import org.obm.push.backend.FolderType;
import org.obm.push.backend.IApplicationData;
import org.obm.push.backend.ItemChange;
import org.obm.push.backend.MSAttendee;
import org.obm.push.backend.MSEvent;
import org.obm.push.backend.MSTask;
import org.obm.push.backend.PIMDataType;
import org.obm.push.backend.obm22.impl.ObmSyncBackend;
import org.obm.push.data.calendarenum.AttendeeStatus;
import org.obm.push.data.calendarenum.AttendeeType;
import org.obm.push.exception.ActiveSyncException;
import org.obm.push.exception.ObjectNotFoundException;
import org.obm.push.state.SyncState;
import org.obm.push.store.ISyncStorage;
import org.obm.push.store.InvitationStatus;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.calendar.Attendee;
import org.obm.sync.calendar.CalendarInfo;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventType;
import org.obm.sync.calendar.ParticipationState;
import org.obm.sync.client.calendar.AbstractEventSyncClient;
import org.obm.sync.items.EventChanges;

public class CalendarBackend extends ObmSyncBackend {

	private Map<PIMDataType, ObmSyncCalendarConverter> converters;

	public CalendarBackend(ISyncStorage storage) {
		super(storage);
		converters = new HashMap<PIMDataType, ObmSyncCalendarConverter>(2);
		converters.put(PIMDataType.CALENDAR, new EventConverter());
		converters.put(PIMDataType.TASKS, new TodoConverter());
	}

	public List<ItemChange> getHierarchyChanges(BackendSession bs) {
		List<ItemChange> ret = new LinkedList<ItemChange>();

		if (!bs.checkHint("hint.multipleCalendars", false)) {
			ItemChange ic = new ItemChange();
			String col = getDefaultCalendarName(bs);
			String serverId = "";
			try {
				Integer collectionId = getCollectionIdFor(bs.getDevId(), col);
				serverId = getServerIdFor(collectionId, null);
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
		// ADD EVENT FOLDER
		AbstractEventSyncClient cc = getCalendarClient(bs, null);
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
				Integer collectionId = getCollectionIdFor(bs.getDevId(), col);
				ic.setServerId(getServerIdFor(collectionId, null));
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

	public List<ItemChange> getHierarchyTaskChanges(BackendSession bs) {
		List<ItemChange> ret = new ArrayList<ItemChange>(1);
		ItemChange ic = new ItemChange();
		String col = "obm:\\\\" + bs.getLoginAtDomain() + "\\tasks\\"
				+ bs.getLoginAtDomain();
		String serverId;
		try {
			Integer collectionId = getCollectionIdFor(bs.getDevId(), col);
			serverId = getServerIdFor(collectionId, null);
		} catch (ActiveSyncException e) {
			serverId = createCollectionMapping(bs.getDevId(), col);
			ic.setIsNew(true);
		}
		ic.setServerId(serverId);
		ic.setParentId("0");
		ic.setDisplayName(bs.getLoginAtDomain() + " tasks");
		ic.setItemType(FolderType.DEFAULT_TASKS_FOLDER);
		ret.add(ic);
		return ret;
	}

	public DataDelta getContentChanges(BackendSession bs, SyncState state,
			Integer collectionId)
			throws ActiveSyncException {
		List<ItemChange> addUpd = new LinkedList<ItemChange>();
		List<ItemChange> deletions = new LinkedList<ItemChange>();

		Date ls = state.getLastSync();
		AbstractEventSyncClient cc = getCalendarClient(bs, state.getDataType());
		AccessToken token = cc.login(bs.getLoginAtDomain(), bs.getPassword(),
				"o-push");
		String collectionPath = getCollectionPathFor(collectionId);
		String calendar = parseCalendarName(collectionPath);
		try {
			EventChanges changes = null;
			if (state.isLastSyncFiltred()) {
				changes = cc.getSyncEventDate(token, calendar, ls);
			} else {
				changes = cc.getSync(token, calendar, ls);
			}
			Event[] evs = changes.getUpdated();
			String userEmail = cc.getUserEmail(token);
			for (Event e : evs) {
				boolean canAdd = true;
				for (Attendee att : e.getAttendees()) {
					if (userEmail.equals(att.getEmail())
							&& ParticipationState.DECLINED.equals(att
									.getState())) {
						logger
								.info("Event["
										+ e.getDatabaseId()
										+ "] The participation state is declined. The event will be deleted on phone");
						canAdd = false;
						deletions.add(getDeletion(collectionId, e.getUid()));
						break;
					}
				}
				if (canAdd && e.getRecurrenceId() == null) {
					ItemChange change = getCalendarChange(bs.getDevId(),
							collectionId, e);
					addUpd.add(change);
				}
			}
			for (String del : changes.getRemoved()) {
				deletions.add(getDeletion(collectionId, del));
			}
			bs.addUpdatedSyncDate(collectionId, changes.getLastSync());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		cc.logout(token);
		logger.info("getContentChanges(" + calendar + ", " + collectionPath
				+ ", lastSync: " + ls + ") => " + addUpd.size() + " entries.");
		DataDelta ret = new DataDelta(addUpd, deletions);
		filtreEvent(bs, state, collectionId, ret);
		return ret;
	}

	private void filtreEvent(BackendSession bs, SyncState state,
			Integer collectionId, DataDelta delta) {

		for (Iterator<ItemChange> it = delta.getChanges().iterator(); it
				.hasNext();) {
			ItemChange ic = it.next();
			MSEvent event = (MSEvent) ic.getData();

			if (!storage.isMostRecentInvitation(collectionId,
					event.getObmUID(), event.getDtStamp())) {
				logger
						.info("A more recent event or email is synchronized on phone. The event["
								+ event.getObmUID() + "] will not synced");
				it.remove();
			} else {
				storage.markToDeletedSyncedInvitation(collectionId, event
						.getObmUID());
				Boolean update = storage.haveEmailToDeleted(collectionId, event
						.getObmUID());
				if (update) {
					storage.createOrUpdateInvitation(collectionId, event
							.getObmUID(), event.getDtStamp(),
							InvitationStatus.EVENT_TO_SYNCED, null);
					try {
						this.createOrUpdate(bs, collectionId, getServerIdFor(collectionId, event.getObmUID()), event);
					} catch (ActiveSyncException e) {
						logger.error(e.getMessage(), e);
					}
					it.remove();
				} else {
						storage.createOrUpdateInvitation(collectionId, event
								.getObmUID(), event.getDtStamp(),
								InvitationStatus.EVENT_SYNCED, state.getKey());
				}
			}
		}

		try {
			List<String> eventToDeleted = storage.getEventToDeleted(
					collectionId, state.getKey());
			logger.info(eventToDeleted.size() + " event(s) will be deleted on the PDA");
			List<ItemChange> its = this.getDeletions(collectionId,
					eventToDeleted);
				storage.updateInvitationStatus(InvitationStatus.DELETED, state
						.getKey(), collectionId, eventToDeleted
						.toArray(new String[0]));
			delta.getDeletions().addAll(its);
		} catch (ActiveSyncException e) {
			logger.error(e.getMessage(), e);
		}
	}

	private String parseCalendarName(String collectionPath) {
		// parse obm:\\thomas@zz.com\calendar\sylvaing@zz.com
		logger.info("collectionPath: " + collectionPath);
		int slash = collectionPath.lastIndexOf("\\");
		int at = collectionPath.lastIndexOf("@");

		return collectionPath.substring(slash + 1, at);
	}

	private ItemChange getCalendarChange(String deviceId, Integer collectionId,
			Event e) throws ActiveSyncException {
		ItemChange ic = new ItemChange();
		ic.setServerId(getServerIdFor(collectionId, e.getUid()));
		IApplicationData ev = convertEvent(e);
		ic.setData(ev);
		return ic;
	}

	public String createOrUpdate(BackendSession bs, Integer collectionId,
			String serverId, IApplicationData data)
			throws ActiveSyncException {
		String collectionPath = getCollectionPathFor(collectionId);
		logger.info("createOrUpdate(" + bs.getLoginAtDomain() + ", "
				+ collectionPath + ", " + serverId + ")");
		AbstractEventSyncClient cc = getCalendarClient(bs, data.getType());
		AccessToken token = cc.login(bs.getLoginAtDomain(), bs.getPassword(),
				"o-push");
		String id = null;
		Event oldEvent = null;
		if (serverId != null) {
			int idx = serverId.lastIndexOf(":");
			id = serverId.substring(idx + 1);
			try {
				oldEvent = cc.getEventFromId(token, bs.getLoginAtDomain(), id);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}

		String email = bs.getLoginAtDomain();
		try {
			email = cc.getUserEmail(token);
		} catch (Exception e) {
			logger.error("Error finding email: " + e.getMessage(), e);
		}
		MSAttendee ownerAtt = new MSAttendee();
		ownerAtt.setEmail(email);
		ownerAtt.setAttendeeStatus(AttendeeStatus.ACCEPT);
		ownerAtt.setAttendeeType(AttendeeType.REQUIRED);
		Event event = converters.get(data.getType()).convert(oldEvent, data,
				ownerAtt);
		Attendee att = new Attendee();
		att.setEmail(email);
		if (id != null) {
			try {
				event.setUid(id);
				cc.modifyEvent(token, parseCalendarName(collectionPath), event,
						true);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		} else {
			try {
				id = cc.createEvent(token, parseCalendarName(collectionPath),
						event);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
		cc.logout(token);

		return getServerIdFor(collectionId, id);
	}

	public void delete(BackendSession bs, String collectionPath, String serverId) {
		logger.info("delete serverId " + serverId);
		if (serverId != null) {
			int idx = serverId.indexOf(":");
			if (idx > 0) {
				String id = serverId.substring(idx + 1);
				AbstractEventSyncClient bc = getCalendarClient(bs,
						PIMDataType.CALENDAR);
				AccessToken token = bc.login(bs.getLoginAtDomain(), bs
						.getPassword(), "o-push");
				try {
					Event evr = bc.getEventFromId(token, bs.getLoginAtDomain(),
							id);
					if (evr != null) {
						if (bs.getLoginAtDomain().equals(evr.getOwnerEmail())) {
							bc.removeEvent(token,
									parseCalendarName(collectionPath), id);
						} else {
							IApplicationData mser = convertEvent(evr);
							updateUserStatus(bs, mser, AttendeeStatus.DECLINE);
						}
					}
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
				bc.logout(token);
			}
		}
	}

	public String updateUserStatus(BackendSession bs, IApplicationData data,
			AttendeeStatus status) {
		AbstractEventSyncClient calCli = getCalendarClient(bs, data.getType());
		AccessToken at = calCli.login(bs.getLoginAtDomain(), bs.getPassword(),
				"o-push");
		try {
			if (data instanceof MSEvent) {
				MSEvent ev = (MSEvent) data;
				for (MSAttendee att : ev.getAttendees()) {
					if (bs.getLoginAtDomain().equalsIgnoreCase(att.getEmail())) {
						att.setAttendeeStatus(status);
					}
				}
			}

			int ar = bs.getLoginAtDomain().lastIndexOf("@");
			String calendar = bs.getLoginAtDomain().substring(0, ar);
			logger.info("update user status[" + status.toString()
					+ "] in calendar " + calendar);

			Event event = converters.get(data.getType()).convert(data);
			event = calCli.modifyEvent(at, calendar, event, true);
			Integer collectionId = getCollectionIdFor(bs.getDevId(),
					getDefaultCalendarName(bs));
			return getServerIdFor(collectionId, event.getUid());

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			calCli.logout(at);
		}
		return null;
	}

	public List<ItemChange> fetchItems(BackendSession bs,
			List<String> fetchServerIds) throws ObjectNotFoundException {
		List<ItemChange> ret = new LinkedList<ItemChange>();
		try {
			for (String serverId : fetchServerIds) {
				Integer id = getItemIdFor(serverId);
				if (id != null) {
					AbstractEventSyncClient calCli = getCalendarClient(bs,
							PIMDataType.CALENDAR);
					AccessToken token = calCli.login(bs.getLoginAtDomain(), bs
							.getPassword(), "o-push");

					Event e = calCli.getEventFromId(token, bs
							.getLoginAtDomain(), id.toString());
					ItemChange ic = new ItemChange();
					ic.setServerId(serverId);
					IApplicationData ev = convertEvent(e);
					ic.setData(ev);
					ret.add(ic);
				}
			}
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
			throw new ObjectNotFoundException();
		}
		return ret;
	}

	private IApplicationData convertEvent(Event e) {
		if (EventType.VTODO.equals(e.getType())) {
			return (MSTask) converters.get(PIMDataType.TASKS).convert(e);
		} else {
			return (MSEvent) converters.get(PIMDataType.CALENDAR).convert(e);
		}
	}
}
