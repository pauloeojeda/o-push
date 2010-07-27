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

	private static final String POLL_QUERY = "		select "
			+ "		distinct "
			+ "		uo.userobm_login, d.domain_name, now() "
			+ "		from SyncedAddressbook sa "
			+ "		inner join UserObm uo on uo.userobm_id=sa.user_id "
			+ "		inner join Domain d on d.domain_id=uo.userobm_domain_id "
			+ "		inner join AddressBook ab on ab.id=sa.addressbook_id "
			+ "		where ab.timeupdate >= ? or ab.timecreate >= ? or sa.timestamp >= ? "
			+ "		; ";

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
			ps = con.prepareStatement(POLL_QUERY);
			ps.setTimestamp(idx++, ts);
			ps.setTimestamp(idx++, ts);
			ps.setTimestamp(idx++, ts);
			rs = ps.executeQuery();
			dbDate = fillChangedCollections(rs, changed, lastSync);
		} catch (Throwable t) {
			logger.error("Error running calendar poll query", t);
		} finally {
			JDBCUtils.cleanup(con, ps, rs);
		}

		if (logger.isInfoEnabled() && changed.size() > 0) {
			logger.info("changed collections: " + changed.size() + " dbDate: "
					+ dbDate);
		}

		return new ChangedCollections(dbDate, changed);
	}

	private Date fillChangedCollections(ResultSet rs,
			Set<SyncCollection> changed, Date lastSync) throws SQLException {
		Date ret = lastSync;
		int i = 0;
		while (rs.next()) {
			String login = rs.getString(1);
			String domain = rs.getString(2);
			ret = new Date(rs.getTimestamp(3).getTime());

			StringBuffer colName = new StringBuffer(255);
			colName.append("obm:\\\\");
			colName.append(login);
			colName.append('@');
			colName.append(domain);
			colName.append("\\contacts");

			SyncCollection sc = new SyncCollection();
			String s = colName.toString();
			sc.setCollectionPath(s);
			changed.add(sc);
			i++;
			if (logger.isInfoEnabled()) {
				logger.info("Detected contacts change for " + s);
			}
		}
		return ret;
	}

}
