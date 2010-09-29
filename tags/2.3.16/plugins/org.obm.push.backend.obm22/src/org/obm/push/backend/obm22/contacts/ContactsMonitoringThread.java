package org.obm.push.backend.obm22.contacts;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;

import org.obm.push.backend.ICollectionChangeListener;
import org.obm.push.backend.SyncCollection;
import org.obm.push.backend.obm22.impl.ChangedCollections;
import org.obm.push.backend.obm22.impl.MonitoringThread;
import org.obm.push.backend.obm22.impl.ObmSyncBackend;

import fr.aliasource.utils.JDBCUtils;

public class ContactsMonitoringThread extends MonitoringThread {

	private static final String CHANGED_UIDS = "		select "
			+ "		distinct sa.user_id, now() "
			+ "		from SyncedAddressbook sa "
			+ "		inner join AddressBook ab on ab.id=sa.addressbook_id "
			+ "		where ab.timeupdate >= ? or ab.timecreate >= ? or sa.timestamp >= ? ";

	private static final String POLL_QUERY = "		select "
			+ "		uo.userobm_login, d.domain_name "
			+ "		FROM UserObm uo "
			+ "		inner join Domain d on d.domain_id=uo.userobm_domain_id WHERE uo.userobm_id IN ";

	public ContactsMonitoringThread(ObmSyncBackend cb, long freqMs,
			Set<ICollectionChangeListener> ccls) {
		super(cb, freqMs, ccls);
	}

	@Override
	protected ChangedCollections getChangedCollections(Date lastSync) {

		Date dbDate = lastSync;
		Set<SyncCollection> changed = new HashSet<SyncCollection>();

		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		cal.setTimeInMillis(lastSync.getTime());
		Timestamp ts = new Timestamp(cal.getTimeInMillis());
		if (logger.isDebugEnabled()) {
			logger.debug("poll date is " + cal.getTime());
		}
		int idx = 1;
		try {
			con = newCon();
			ps = con.prepareStatement(CHANGED_UIDS);
			ps.setTimestamp(idx++, ts);
			ps.setTimestamp(idx++, ts);
			ps.setTimestamp(idx++, ts);
			rs = ps.executeQuery();
			dbDate = fillChangedCollections(con, rs, changed, lastSync);
		} catch (Throwable t) {
			logger.error("Error running changed uids query", t);
		} finally {
			JDBCUtils.cleanup(con, ps, rs);
		}

		if (logger.isInfoEnabled() && changed.size() > 0) {
			logger.info("changed collections: " + changed.size() + " dbDate: "
					+ dbDate);
		}

		return new ChangedCollections(dbDate, changed);
	}

	private Date fillChangedCollections(Connection con, ResultSet rs,
			Set<SyncCollection> changed, Date lastSync) throws SQLException {
		Date ret = lastSync;

		int i = 0;
		StringBuilder ids = new StringBuilder("(0");
		while (rs.next()) {
			int id = rs.getInt(1);
			ids.append(", " + id);
			if (i == 0) {
				ret = new Date(rs.getTimestamp(2).getTime());
			}
			i++;
		}
		ids.append(")");

		if (i > 0) {

			PreparedStatement ps = null;
			ResultSet res = null;
			try {
				ps = con.prepareStatement(POLL_QUERY + ids.toString());
				res = ps.executeQuery();
				while (res.next()) {
					StringBuilder colName = new StringBuilder(255);
					colName.append("obm:\\\\");
					colName.append(res.getString(1));
					colName.append('@');
					colName.append(res.getString(2));
					colName.append("\\contacts");

					SyncCollection sc = new SyncCollection();
					String s = colName.toString();
					sc.setCollectionPath(s);
					changed.add(sc);
					if (logger.isInfoEnabled()) {
						logger.info("Detected contacts change for " + s);
					}

				}

			} catch (Throwable t) {
				logger.error("Error running calendar poll query", t);
			} finally {
				JDBCUtils.cleanup(null, ps, res);
			}

		}

		return ret;
	}

}
