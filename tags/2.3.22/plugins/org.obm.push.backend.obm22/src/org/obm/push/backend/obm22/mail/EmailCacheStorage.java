/* ***** BEGIN LICENSE BLOCK *****
 * Version: GPL 2.0
 *
 * The contents of this file are subject to the GNU General Public
 * License Version 2 or later (the "GPL").
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Initial Developer of the Original Code is
 *   MiniG.org project members
 *
 * ***** END LICENSE BLOCK ***** */

package org.obm.push.backend.obm22.mail;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;

import javax.transaction.UserTransaction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.minig.imap.InternalDate;
import org.minig.imap.SearchQuery;
import org.minig.imap.StoreClient;
import org.minig.obm.pool.OBMPoolActivator;
import org.obm.push.backend.BackendSession;
import org.obm.push.state.SyncState;

import fr.aliasource.utils.JDBCUtils;

/**
 * Implements cache update policy.
 * 
 * The following policy is used for cache updates : refresh each folder.
 * 
 * Folder refresh loads a list of cached uids, then this list is compared to the
 * uid list on the server.
 * 
 * @author adrienp
 * 
 */
public class EmailCacheStorage implements IEmailSync {

	protected Log logger;
	private String debugName;

	private MailChanges mailChangesCache;
	private Date lastSyncDate;
	private String lastSyncKey;

	public EmailCacheStorage() {
		this.logger = LogFactory.getLog(getClass());
		this.lastSyncDate = new Date(0);
		this.lastSyncKey = "";
	}

	public UserTransaction getUserTransaction() {
		return OBMPoolActivator.getDefault().getUserTransaction();
	}

	public MailChanges computeChanges(Set<Long> oldUids, Set<Long> fetched,
			Set<Long> lastUpdate) {
		Set<Long> removed = new HashSet<Long>();
		if (oldUids != null) {
			removed.addAll(oldUids);
			removed.removeAll(fetched);
		}

		Set<Long> updated = new HashSet<Long>();
		updated.addAll(lastUpdate);
		// updated.removeAll(old);

		MailChanges mc = new MailChanges();
		mc.setUpdated(updated);
		mc.setRemoved(removed);

		Calendar lastSync = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		lastSync.setTime(new Date());
		mc.setLastSync(lastSync.getTime());

		return mc;

	}

	private Set<Long> loadMailFromDb(BackendSession bs, Integer devId,
			Integer collectionId) {
		long time = System.currentTimeMillis();
		Set<Long> uids = new HashSet<Long>();

		Connection con = null;
		PreparedStatement ps = null;
		ResultSet evrs = null;
		try {
			con = OBMPoolActivator.getDefault().getConnection();
			ps = con.prepareStatement("SELECT mail_uid FROM opush_sync_mail WHERE collection_id=? and device_id=?");
			ps.setLong(1, collectionId);
			ps.setInt(2, devId);

			evrs = ps.executeQuery();
			while (evrs.next()) {
				Long uid = evrs.getLong("mail_uid");
				uids.add(uid);
			}
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			JDBCUtils.cleanup(con, ps, evrs);
		}

		if (logger.isDebugEnabled()) {
			time = System.currentTimeMillis() - time;
			logger.debug(debugName + " loadMailCache() in " + time + "ms.");
		}
		return uids;
	}

	private Set<Long> loadMailFromIMAP(BackendSession bs,
			StoreClient imapStore, Date lastUpdate) {
		long time = System.currentTimeMillis();
		Set<Long> mails = new HashSet<Long>();
		try {
			// imapStore.uidSearch return messages whose internal date
			// (disregarding time and timezone)
			// is within or later than the specified date.
			Collection<Long> uids = imapStore.uidSearch(new SearchQuery(
					lastUpdate));
			if (lastUpdate != null && !lastUpdate.equals(new Date(0))) {
				InternalDate[] tabID = imapStore.uidFetchInternalDate(uids);
				for (InternalDate id : tabID) {
					if (id.after(lastUpdate)) {
						mails.add(Long.valueOf(id.getUid()));
					}
				}
			} else {
				for (long uid : uids) {
					mails.add(Long.valueOf(uid));
				}
			}
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
		}
		if (logger.isDebugEnabled()) {
			time = System.currentTimeMillis() - time;
			logger.debug(debugName + " loadMailFromIMAP() in " + time + "ms.");
		}
		logger.info(mails.size() + " mails arrived since " + lastUpdate);
		return mails;
	}

	private void updateDbCache(BackendSession bs, Integer devId,
			Integer collectionId, final Set<Long> data) throws SQLException {
		Set<Long> toRemove = new HashSet<Long>();
		Set<Long> oldUids = loadMailFromDb(bs, devId, collectionId);
		if (oldUids != null) {
			toRemove.addAll(oldUids);
			toRemove.removeAll(data);
		}

		Set<Long> toInsert = new HashSet<Long>();
		toInsert.addAll(data);
		if (oldUids != null) {
			toInsert.removeAll(oldUids);
		}

		if (toRemove.size() == 0 && toInsert.size() == 0) {
			return;
		}

		PreparedStatement del = null;
		PreparedStatement insert = null;

		if (logger.isDebugEnabled()) {
			logger.debug(debugName + " should run a batch with "
					+ toRemove.size() + " deletions & " + toInsert.size()
					+ " insertions.");
		}
		Connection con = null;
		UserTransaction ut = getUserTransaction();
		try {
			ut.begin();
			con = OBMPoolActivator.getDefault().getConnection();
			del = con
					.prepareStatement("DELETE FROM opush_sync_mail WHERE collection_id=? AND device_id=? AND mail_uid=?");
			for (Long l : toRemove) {
				del.setInt(1, collectionId);
				del.setInt(2, devId);
				del.setInt(3, l.intValue());
				del.addBatch();
			}
			del.executeBatch();

			insert = con
					.prepareStatement("INSERT INTO opush_sync_mail (collection_id, device_id, mail_uid) VALUES (?, ?, ?)");
			for (Long l : toInsert) {
				insert.setInt(1, collectionId);
				insert.setInt(2, devId);
				insert.setInt(3, l.intValue());
				insert.addBatch();
			}
			insert.executeBatch();
			ut.commit();
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
			JDBCUtils.rollback(con);
		} finally {
			JDBCUtils.cleanup(null, del, null);
			JDBCUtils.cleanup(con, insert, null);
		}
	}

	@Override
	public MailChanges getSync(StoreClient imapStore, Integer devId,
			BackendSession bs, SyncState state, Integer collectionId)
			throws InterruptedException, SQLException {
		long time = System.currentTimeMillis();
		long ct = System.currentTimeMillis();
		Set<Long> memoryCache = loadMailFromDb(bs, devId, collectionId);
		ct = System.currentTimeMillis() - ct;

		long writeTime = System.currentTimeMillis();
		Set<Long> current = loadMailFromIMAP(bs, imapStore, null);
		if (!current.equals(memoryCache)) {
			updateDbCache(bs, devId, collectionId, current);
		} else if (state.getLastSync() != null && this.lastSyncDate != null
				&& !state.getLastSync().after(this.lastSyncDate)
				&& state.getKey().equals(this.lastSyncKey)) {
			return this.mailChangesCache;
		}
		writeTime = System.currentTimeMillis() - writeTime;

		long computeChangesTime = System.currentTimeMillis();
		Set<Long> lastUp = loadMailFromIMAP(bs, imapStore, 
				state.getLastSync());
		MailChanges sync = computeChanges(memoryCache, current, lastUp);
		this.mailChangesCache = sync;
		computeChangesTime = System.currentTimeMillis() - computeChangesTime;
		time = System.currentTimeMillis() - time;
		logger.info("[" + bs.getLoginAtDomain() + "]: collectionId ["
				+ collectionId + "] "
				+ (sync.getUpdated().size() + sync.getRemoved().size())
				+ " changes found. (" + time + "ms (loadCache: " + ct
				+ "ms, updCache: " + writeTime + "ms, computeChanges: "
				+ computeChangesTime + "ms))");

		memoryCache = current;
		this.lastSyncKey = state.getKey();
		this.lastSyncDate = state.getLastSync();
		return sync;
	}

	@Override
	public void deleteMessage(Integer devId, Integer collectionId, Long mailUid) {
		PreparedStatement del = null;
		if (logger.isDebugEnabled()) {
			logger.debug(debugName + " should run a batch with 1 deletions.");
		}
		Connection con = null;
		try {
			con = OBMPoolActivator.getDefault().getConnection();
			del = con
					.prepareStatement("DELETE FROM opush_sync_mail WHERE collection_id=? AND device_id=? AND mail_uid=?");
			del.setInt(1, collectionId);
			del.setInt(2, devId);
			del.setInt(3, mailUid.intValue());
			del.addBatch();

			del.executeBatch();
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			JDBCUtils.cleanup(con, del, null);
		}
	}

	@Override
	public void addMessage(Integer devId, Integer collectionId, Long mailUid) {
		if (logger.isDebugEnabled()) {
			logger.debug(debugName + " should run a batch with 1 insertions.");
		}
		Connection con = null;
		PreparedStatement insert = null;

		try {
			con = OBMPoolActivator.getDefault().getConnection();

			insert = con
					.prepareStatement("INSERT INTO opush_sync_mail (collection_id, device_id, mail_uid) VALUES (?, ?, ?)");
			insert.setInt(1, collectionId);
			insert.setInt(2, devId);
			insert.setInt(3, mailUid.intValue());
			insert.execute();
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			JDBCUtils.cleanup(con, insert, null);
		}
	}

}
