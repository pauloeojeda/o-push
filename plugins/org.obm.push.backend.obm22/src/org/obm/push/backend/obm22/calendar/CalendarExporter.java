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
import org.obm.push.backend.MSEvent;
import org.obm.push.backend.FolderType;
import org.obm.push.backend.ItemChange;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.calendar.Event;
import org.obm.sync.client.calendar.CalendarClient;
import org.obm.sync.items.EventChanges;
import org.obm.sync.locators.CalendarLocator;

import fr.aliasource.utils.JDBCUtils;

public class CalendarExporter {

	private static final Log logger = LogFactory.getLog(CalendarExporter.class);

	public CalendarExporter() {
		validateOBMConnection();
	}

	private CalendarClient getClient(BackendSession bs) {
		CalendarLocator cl = new CalendarLocator();
		CalendarClient calCli = cl.locate("http://10.0.0.5/obm-sync/services");
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
			logger.error("OBM Db connection is broken: "+ e.getMessage(), e);
		} finally {
			JDBCUtils.cleanup(con, ps, rs);
		}
	}

	public List<ItemChange> getHierarchyChanges(BackendSession bs) {
		List<ItemChange> ret = new LinkedList<ItemChange>();

		ItemChange ic = new ItemChange();
		ic.setServerId("obm://" + bs.getLoginAtDomain() + "/calendar/"
				+ bs.getLoginAtDomain());
		ic.setParentId("0");
		ic.setDisplayName(bs.getLoginAtDomain());
		ic.setItemType(FolderType.DEFAULT_CALENDAR_FOLDER);
		ret.add(ic);

		ic = new ItemChange();
		ic.setServerId("obm://thomas@zz.com/calendar/sylvaing@zz.com");
		ic.setParentId("0");
		ic.setDisplayName("sylvaing@zz.com");
		ic.setItemType(FolderType.USER_CREATED_CALENDAR_FOLDER);
		ret.add(ic);

		return ret;
	}

	public List<ItemChange> getContentChanges(BackendSession bs, String  collectionId) {
		List<ItemChange> ret = new LinkedList<ItemChange>();
		
		Date ls = bs.getState().getLastSync();
		CalendarClient cc = getClient(bs);
		AccessToken token = cc.login(bs.getLoginAtDomain(), bs.getPassword(), "o-push");
		int at = bs.getLoginAtDomain().indexOf("@");
		String calendar = bs.getLoginAtDomain().substring(0, at);
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
		return ret;
	}

	private ItemChange addCalendarChange(Event e) {
		ItemChange ic = new ItemChange();
		ic.setServerId("358");
		MSEvent cal = new EventConverter().convertEvent(e);
		ic.setData(cal);
		return ic;
	}

}
