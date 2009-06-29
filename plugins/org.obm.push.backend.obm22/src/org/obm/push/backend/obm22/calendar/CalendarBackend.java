package org.obm.push.backend.obm22.calendar;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.obm.push.backend.BackendSession;
import org.obm.push.backend.DataDelta;
import org.obm.push.backend.FolderType;
import org.obm.push.backend.ItemChange;
import org.obm.push.backend.MSEvent;
import org.obm.push.backend.obm22.impl.ObmSyncBackend;
import org.obm.push.store.ISyncStorage;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.calendar.Attendee;
import org.obm.sync.calendar.CalendarInfo;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.ParticipationRole;
import org.obm.sync.calendar.ParticipationState;
import org.obm.sync.client.calendar.CalendarClient;
import org.obm.sync.items.EventChanges;
import org.obm.sync.locators.CalendarLocator;

public class CalendarBackend extends ObmSyncBackend {

	public CalendarBackend(ISyncStorage storage) {
		super(storage);
	}

	private CalendarClient getClient(BackendSession bs) {

		CalendarLocator cl = new CalendarLocator();
		if (obmSyncHost == null) {
			locateObmSync(bs);
		}
		CalendarClient calCli = cl.locate("http://" + obmSyncHost
				+ ":8080/obm-sync/services");
		return calCli;
	}

	public List<ItemChange> getHierarchyChanges(BackendSession bs) {
		List<ItemChange> ret = new LinkedList<ItemChange>();

		if (!bs.checkHint("hint.multipleCalendars", true)) {
			ItemChange ic = new ItemChange();
			String col = "obm:\\\\" + bs.getLoginAtDomain() + "\\calendar\\"
					+ bs.getLoginAtDomain();
			ic.setServerId(mapper.getClientIdFor(bs.getDevId(), col, null));
			ic.setParentId("0");
			ic.setDisplayName(bs.getLoginAtDomain());
			ic.setItemType(FolderType.DEFAULT_CALENDAR_FOLDER);
			ret.add(ic);
			return ret;
		}

		CalendarClient cc = getClient(bs);
		AccessToken token = cc.login(bs.getLoginAtDomain(), bs.getPassword(),
				"o-push");
		try {
			CalendarInfo[] cals = cc.listCalendars(token);
			int i = 0;
			for (CalendarInfo ci : cals) {
				ItemChange ic = new ItemChange();
				String col = "obm:\\\\" + bs.getLoginAtDomain()
						+ "\\calendar\\" + ci.getUid() + "@domain";
				ic.setServerId(mapper.getClientIdFor(bs.getDevId(), col, null));
				ic.setParentId("0");
				ic.setDisplayName(ci.getMail());
				if (i++ == 0) {
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

	public DataDelta getContentChanges(BackendSession bs, String collectionId) {
		List<ItemChange> addUpd = new LinkedList<ItemChange>();
		List<ItemChange> deletions = new LinkedList<ItemChange>();

		Date ls = bs.getState().getLastSync();
		CalendarClient cc = getClient(bs);
		AccessToken token = cc.login(bs.getLoginAtDomain(), bs.getPassword(),
				"o-push");
		String calendar = parseCalendarId(collectionId);
		try {
			EventChanges changes = cc.getSync(token, calendar, ls);
			Event[] evs = changes.getUpdated();
			for (Event e : evs) {
				ItemChange change = addCalendarChange(bs.getDevId(),
						collectionId, e);
				addUpd.add(change);
			}
			bs.setUpdatedSyncDate(changes.getLastSync());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		cc.logout(token);
		logger.info("getContentChanges(" + calendar + ", " + collectionId
				+ ") => " + addUpd.size() + " entries.");
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
			Event e) {
		ItemChange ic = new ItemChange();
		ic.setServerId(mapper.getClientIdFor(deviceId, collection, e.getUid()));
		MSEvent cal = new EventConverter().convertEvent(e);
		String clientId = mapper.toDevice(deviceId, ic.getServerId());
		if (!ic.getServerId().equals(clientId)) {
			int idx = clientId.lastIndexOf(":");
			clientId = clientId.substring(idx + 1);
			ic.setClientId(clientId);
		}
		ic.setData(cal);
		return ic;
	}

	public String createOrUpdate(BackendSession bs, String collectionId,
			String serverId, String clientId, MSEvent data) {
		logger.info("createOrUpdate(" + bs.getLoginAtDomain() + ", "
				+ collectionId + ", " + serverId + ", " + clientId + ", "
				+ data.getSubject() + ")");

		CalendarClient cc = getClient(bs);
		AccessToken token = cc.login(bs.getLoginAtDomain(), bs.getPassword(),
				"o-push");
		String id = null;
		if (serverId != null) {
			id = serverId;
		}

		// disabled for nokia tests
		// else if (clientId != null) {
		// id = mapper.toOBM(bs.getDevId(), mapper.getClientIdFor(bs
		// .getDevId(), collectionId, clientId));
		// }
		
		Event event = new EventConverter().convertEvent(data);
		if (event.getAttendees().isEmpty()) {
			String email = bs.getLoginAtDomain();
			try {
				email = cc.getUserEmail(token);
			} catch (Exception e) {
				logger.error("Error finding email: " + e.getMessage(), e);
			}
			Attendee at = new Attendee();
			at.setEmail(email);
			at.setRequired(ParticipationRole.REQ);
			at.setState(ParticipationState.ACCEPTED);
			at.setDisplayName(email);
			event.addAttendee(at);
		}
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
		String obm = mapper.getClientIdFor(bs.getDevId(), collectionId, id);
		if (clientId != null) {
			mapper.addMapping(bs.getDevId(), mapper.getClientIdFor(bs
					.getDevId(), collectionId, clientId), obm);
		}
		return obm;
	}

	public void delete(BackendSession bs, String collectionId, String serverId) {
		logger.info("delete serverId "+serverId);
		if (serverId != null) {
			int idx = serverId.indexOf(":");
			if (idx > 0) {
				String id = serverId.substring(idx+1);
				CalendarClient bc = getClient(bs);
				AccessToken token = bc.login(bs.getLoginAtDomain(), bs.getPassword(),
						"o-push");
				try {
					bc.removeEvent(token, parseCalendarId(collectionId), id);
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
				bc.logout(token);
			}
		}
	}

}
