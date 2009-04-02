package org.obm.push.backend.obm22.calendar;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.minig.obm.pool.IOBMConnection;
import org.minig.obm.pool.OBMPoolActivator;
import org.obm.push.backend.BackendSession;
import org.obm.push.backend.DataDelta;
import org.obm.push.backend.FolderType;
import org.obm.push.backend.ItemChange;
import org.obm.push.backend.MSEvent;
import org.obm.push.backend.obm22.impl.ObmDbHelper;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.calendar.Attendee;
import org.obm.sync.calendar.CalendarInfo;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.ParticipationRole;
import org.obm.sync.calendar.ParticipationState;
import org.obm.sync.client.calendar.CalendarClient;
import org.obm.sync.items.EventChanges;
import org.obm.sync.locators.CalendarLocator;

import fr.aliasource.utils.JDBCUtils;

public class CalendarBackend {

	private static final Log logger = LogFactory.getLog(CalendarBackend.class);

	private UIDMapper mapper;
	private String obmSyncHost;

	public CalendarBackend() {
		validateOBMConnection();
		this.mapper = new UIDMapper();
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

	private void locateObmSync(BackendSession bs) {
		Set<String> props = ObmDbHelper.findHost(bs, "sync", "obm_sync");
		if (props.isEmpty()) {
			obmSyncHost = "localhost";
			logger
					.warn("No host with obm_sync property found. Defauting to localhost");
		} else {
			obmSyncHost = props.iterator().next();
			logger.info("Using " + obmSyncHost + " as obm_sync host.");
		}
	}

	private void validateOBMConnection() {
		IOBMConnection con = OBMPoolActivator.getDefault().getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement("select now()");
			rs = ps.executeQuery();
			if (rs.next()) {
				logger.info("OBM Db connection is OK");
			}
		} catch (Exception e) {
			logger.error("OBM Db connection is broken: " + e.getMessage(), e);
		} finally {
			JDBCUtils.cleanup(con, ps, rs);
		}
	}

	public List<ItemChange> getHierarchyChanges(BackendSession bs) {
		List<ItemChange> ret = new LinkedList<ItemChange>();

		if (!bs.checkHint("hint.multipleCalendars", true)) {
			ItemChange ic = new ItemChange();
			ic.setServerId("obm:\\\\" + bs.getLoginAtDomain() + "\\calendar\\"
					+ bs.getLoginAtDomain());
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
				ic.setServerId("obm:\\\\" + bs.getLoginAtDomain() + "\\calendar\\"
						+ ci.getUid() + "@domain");
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
				ItemChange change = addCalendarChange(e);
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
		int slash = collectionId.lastIndexOf("\\");
		int at = collectionId.lastIndexOf("@");

		return collectionId.substring(slash + 1, at);
	}

	private ItemChange addCalendarChange(Event e) {
		ItemChange ic = new ItemChange();
		ic.setServerId(UIDMapper.UID_PREFIX + e.getUid());
		MSEvent cal = new EventConverter().convertEvent(e);
		cal.setUID(mapper.toDevice(ic.getServerId()));
		ic.setData(cal);
		return ic;
	}

	public String createOrUpdate(BackendSession bs, String collectionId,
			String serverId, MSEvent data) {
		logger.info("createOrUpdate(" + bs.getLoginAtDomain() + ", " + serverId
				+ ", " + data.getSubject() + ")");

		CalendarClient cc = getClient(bs);
		AccessToken token = cc.login(bs.getLoginAtDomain(), bs.getPassword(),
				"o-push");
		String device = data.getUID();
		String id = null;
		if (serverId != null) {
			id = serverId;
		} else if (data.getUID() != null && mapper.toOBM(data.getUID()) != null) {
			id = mapper.toOBM(data.getUID());
		}
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
			id = id.replace(UIDMapper.UID_PREFIX, "");
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
		String obm = UIDMapper.UID_PREFIX + id;
		mapper.addMapping(device, obm);
		return obm;
	}

	public void delete(BackendSession bs, String serverId) {
		// TODO Auto-generated method stub

	}

}
