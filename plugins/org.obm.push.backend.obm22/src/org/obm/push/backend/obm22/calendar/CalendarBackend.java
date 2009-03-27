package org.obm.push.backend.obm22.calendar;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.minig.obm.pool.IOBMConnection;
import org.minig.obm.pool.OBMPoolActivator;
import org.obm.push.backend.BackendSession;
import org.obm.push.backend.FolderType;
import org.obm.push.backend.ItemChange;
import org.obm.push.backend.MSEvent;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.calendar.CalendarInfo;
import org.obm.sync.calendar.Event;
import org.obm.sync.client.calendar.CalendarClient;
import org.obm.sync.items.EventChanges;
import org.obm.sync.locators.CalendarLocator;

import fr.aliasource.utils.JDBCUtils;

public class CalendarBackend {

	private static final Log logger = LogFactory.getLog(CalendarBackend.class);

	public CalendarBackend() {
		validateOBMConnection();
	}

	private CalendarClient getClient(BackendSession bs) {
		CalendarLocator cl = new CalendarLocator();
		CalendarClient calCli = cl
				.locate("http://10.0.0.5:8080/obm-sync/services");
		return calCli;
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

		CalendarClient cc = getClient(bs);
		AccessToken token = cc.login(bs.getLoginAtDomain(), bs.getPassword(),
				"o-push");
		try {
			CalendarInfo[] cals = cc.listCalendars(token);
			int i = 0;
			for (CalendarInfo ci : cals) {
				ItemChange ic = new ItemChange();
				ic.setServerId("obm://" + bs.getLoginAtDomain() + "/calendar/"
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

	public List<ItemChange> getContentChanges(BackendSession bs,
			String collectionId) {
		List<ItemChange> ret = new LinkedList<ItemChange>();

		Date ls = bs.getState().getLastSync();
		CalendarClient cc = getClient(bs);
		AccessToken token = cc.login(bs.getLoginAtDomain(), bs.getPassword(),
				"o-push");
		String calendar = parseCalendarId(collectionId);
		try {
			EventChanges changes = cc.getSync(token, calendar, ls);
			Event[] evs = changes.getUpdated();
			for (Event e : evs) {
				ret.add(addCalendarChange(e));
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		cc.logout(token);
		logger.info("getContentChanges(" + calendar + ", " + collectionId
				+ ") => " + ret.size() + " entries.");
		return ret;
	}

	private String parseCalendarId(String collectionId) {
		// parse obm://thomas@zz.com/calendar/sylvaing@zz.com
		int slash = collectionId.lastIndexOf("/");
		int at = collectionId.lastIndexOf("@");

		return collectionId.substring(slash + 1, at);
	}

	private ItemChange addCalendarChange(Event e) {
		ItemChange ic = new ItemChange();
		ic.setServerId("obm://calendar/" + e.getUid());
		MSEvent cal = new EventConverter().convertEvent(e);
		cal.setUID(ic.getServerId());
		ic.setData(cal);
		return ic;
	}

	public String createOrUpdate(BackendSession bs, String serverId,
			MSEvent data) {
		// TODO Auto-generated method stub
		logger.info("createOrUpdate("+bs.getLoginAtDomain()+", "+serverId+", "+data.getSubject()+")");
		
		return null;
	}

	public void delete(BackendSession bs, String serverId) {
		// TODO Auto-generated method stub
		
	}

}
