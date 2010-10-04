package org.obm.push.storage.jdbc;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import javax.transaction.UserTransaction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.minig.obm.pool.OBMPoolActivator;
import org.obm.push.backend.PIMDataType;
import org.obm.push.exception.CollectionNotFoundException;
import org.obm.push.state.SyncState;
import org.obm.push.store.ISyncStorage;
import org.obm.push.store.InvitationStatus;

import fr.aliasource.utils.IniFile;
import fr.aliasource.utils.JDBCUtils;

/**
 * Store device infos, id mappings & last sync dates into OBM database
 * 
 * 
 * @author tom
 * 
 */
public class SyncStorage implements ISyncStorage {

	private Map<String, Integer> devIdCache;
	private static final Log logger = LogFactory.getLog(SyncStorage.class);

	public SyncStorage() {
		this.devIdCache = new HashMap<String, Integer>();
	}

	public UserTransaction getUserTransaction() {
		return OBMPoolActivator.getDefault().getUserTransaction();
	}

	@Override
	public SyncState findStateForDevice(String devId, Integer collectionId) {
		int id = devIdCache.get(devId);
		SyncState ret = null;
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		try {
			con = OBMPoolActivator.getDefault().getConnection();
			ps = con
					.prepareStatement("SELECT sync_key, last_sync FROM opush_sync_state WHERE device_id=? AND collection_id=?");
			ps.setInt(1, id);
			ps.setInt(2, collectionId);
			rs = ps.executeQuery();
			if (rs.next()) {
				String key = rs.getString(1);
				long timeInMillis = rs.getTimestamp(2).getTime();
				String dataPath = getCollectionPath(collectionId, con);
				ret = new SyncState(dataPath);
				ret.setKey(key);
				cal.setTimeInMillis(timeInMillis);
				ret.setLastSync(cal.getTime());
			}
		} catch (Throwable se) {
			logger.error(se.getMessage(), se);
		} finally {
			JDBCUtils.cleanup(con, ps, rs);
		}
		return ret;
	}

	@Override
	public SyncState findStateForKey(String syncKey) {

		SyncState ret = null;
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			con = OBMPoolActivator.getDefault().getConnection();
			ps = con
					.prepareStatement("SELECT device_id, last_sync, collection_id FROM opush_sync_state WHERE sync_key=?");
			ps.setString(1, syncKey);

			rs = ps.executeQuery();
			if (rs.next()) {
				Timestamp lastSync = rs.getTimestamp("last_sync");
				ret = new SyncState(getCollectionPath(rs
						.getInt("collection_id"), con));
				ret.setKey(syncKey);
				ret.setLastSync(lastSync);
			}
		} catch (Throwable se) {
			logger.error(se.getMessage(), se);
		} finally {
			JDBCUtils.cleanup(con, ps, rs);
		}
		return ret;
	}

	@Override
	public long findLastHearbeat(String devId) {
		int id = devIdCache.get(devId);
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			con = OBMPoolActivator.getDefault().getConnection();
			ps = con
					.prepareStatement("SELECT last_heartbeat FROM opush_ping_heartbeat WHERE device_id=?");
			ps.setInt(1, id);

			rs = ps.executeQuery();
			if (rs.next()) {
				return rs.getLong("last_heartbeat");
			}
		} catch (Throwable se) {
			logger.error(se.getMessage(), se);
		} finally {
			JDBCUtils.cleanup(con, ps, null);
		}
		return 0L;
	}

	@Override
	public synchronized void updateLastHearbeat(String devId, long hearbeat) {
		int id = devIdCache.get(devId);

		Connection con = null;
		PreparedStatement ps = null;
		UserTransaction ut = getUserTransaction();
		try {
			ut.begin();
			con = OBMPoolActivator.getDefault().getConnection();
			ps = con
					.prepareStatement("DELETE FROM opush_ping_heartbeat WHERE device_id=? ");
			ps.setInt(1, id);
			ps.executeUpdate();

			ps.close();
			ps = con
					.prepareStatement("INSERT INTO opush_ping_heartbeat (device_id, last_heartbeat) VALUES (?, ?)");
			ps.setInt(1, id);
			ps.setLong(2, hearbeat);
			ps.executeUpdate();
			ut.commit();
		} catch (Throwable se) {
			logger.error(se.getMessage(), se);
			JDBCUtils.rollback(ut);
		} finally {
			JDBCUtils.cleanup(con, ps, null);
		}
	}

	@Override
	public boolean initDevice(String loginAtDomain, String deviceId,
			String deviceType) {
		String[] parts = loginAtDomain.split("@");
		String login = parts[0].toLowerCase();
		String domain = parts[1].toLowerCase();

		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		int id = 0;
		boolean ret = true;
		UserTransaction ut = getUserTransaction();
		try {
			ut.begin();
			con = OBMPoolActivator.getDefault().getConnection();
			ps = con
					.prepareStatement("SELECT id FROM opush_device "
							+ "INNER JOIN UserObm ON owner=userobm_id "
							+ "INNER JOIN Domain ON userobm_domain_id=domain_id "
							+ "WHERE identifier=? AND type=? AND lower(userobm_login)=? AND lower(domain_name)=?");
			ps.setString(1, deviceId);
			ps.setString(2, deviceType);
			ps.setString(3, login);
			ps.setString(4, domain);
			rs = ps.executeQuery();
			if (!rs.next()) {
				rs.close();
				ps.close();

				ps = con
						.prepareStatement("INSERT INTO opush_device (identifier, type, owner) "
								+ "SELECT ?, ?, userobm_id FROM UserObm "
								+ "INNER JOIN Domain ON userobm_domain_id=domain_id "
								+ "WHERE lower(userobm_login)=? AND lower(domain_name)=?");
				ps.setString(1, deviceId);
				ps.setString(2, deviceType);
				ps.setString(3, login);
				ps.setString(4, domain);
				int insert = ps.executeUpdate();
				if (insert > 0) {
					id = OBMPoolActivator.getDefault().lastInsertId(con);
				} else {
					logger
							.warn("did not insert any row in device table for device "
									+ deviceType
									+ " of "
									+ login
									+ " @ "
									+ domain);
					ret = false;
				}
			} else {
				id = rs.getInt(1);
			}
			devIdCache.put(deviceId, id);
			ut.commit();
		} catch (Throwable se) {
			logger.error(se.getMessage(), se);
			JDBCUtils.rollback(ut);
			ret = false;
		} finally {
			JDBCUtils.cleanup(con, ps, rs);
		}
		return ret;
	}

	public boolean syncAuthorized(String loginAtDomain, String deviceId) {
		IniFile ini = new IniFile("/etc/opush/sync_perms.ini") {
			@Override
			public String getCategory() {
				return null;
			}
		};
		String syncperm = ini.getData().get("allow.unknown.pda");

		if ("true".equals(syncperm)) {
			return true;
		}

		String[] parts = loginAtDomain.split("@");
		String login = parts[0].toLowerCase();
		String domain = parts[1].toLowerCase();
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		boolean hasSyncPerm = false;
		try {
			con = OBMPoolActivator.getDefault().getConnection();
			ps = con
					.prepareStatement("SELECT policy FROM opush_sync_perms "
							+ "INNER JOIN UserObm u ON owner=userobm_id "
							+ "INNER JOIN Domain d ON userobm_domain_id=domain_id "
							+ "INNER JOIN opush_device od ON device_id=id "
							+ "WHERE od.identifier=? AND lower(u.userobm_login)=? AND lower(d.domain_name)=?");
			ps.setString(1, deviceId);
			ps.setString(2, login);
			ps.setString(3, domain);

			rs = ps.executeQuery();
			if (rs.next()) {
				hasSyncPerm = true;
			}
		} catch (Throwable se) {
			logger.error(se.getMessage(), se);
		} finally {
			JDBCUtils.cleanup(con, ps, null);
		}
		if (!hasSyncPerm) {
			logger.info(loginAtDomain
					+ " isn't authorized to synchronize in OBM-UI");
		}
		return hasSyncPerm;
	}

	@Override
	public synchronized void updateState(String devId, Integer collectionId,
			SyncState oldState, SyncState state) {
		int id = devIdCache.get(devId);
		Connection con = null;
		PreparedStatement ps = null;
		UserTransaction ut = getUserTransaction();
		try {
			ut.begin();
			con = OBMPoolActivator.getDefault().getConnection();
			// ps = con
			// .prepareStatement("DELETE FROM opush_sync_state WHERE device_id=? AND collection_id=?");
			// ps.setInt(1, id);
			// ps.setInt(2, collectionId);
			// ps.executeUpdate();
			//
			// ps.close();

			ps = con
					.prepareStatement("INSERT INTO opush_sync_state (sync_key, device_id, last_sync, collection_id) VALUES (?, ?, ?, ?)");
			ps.setString(1, state.getKey());
			ps.setInt(2, id);
			ps.setTimestamp(3, new Timestamp(state.getLastSync().getTime()));
			ps.setInt(4, collectionId);
			ps.executeUpdate();
			ut.commit();
		} catch (Throwable se) {
			logger.error(se.getMessage(), se);
			JDBCUtils.rollback(ut);
		} finally {
			JDBCUtils.cleanup(con, ps, null);
		}
	}

	@Override
	public Integer getCollectionMapping(String deviceId, String collection)
			throws CollectionNotFoundException {
		int id = devIdCache.get(deviceId);
		Integer ret = null;

		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			con = OBMPoolActivator.getDefault().getConnection();
			ps = con
					.prepareStatement("SELECT id FROM opush_folder_mapping WHERE device_id=? AND collection=?");
			ps.setInt(1, id);
			ps.setString(2, collection);
			rs = ps.executeQuery();

			if (rs.next()) {
				ret = rs.getInt(1);
			}
		} catch (Throwable se) {
			logger.error(se.getMessage(), se);
		} finally {
			JDBCUtils.cleanup(con, ps, rs);
		}
		if (ret == null) {
			throw new CollectionNotFoundException();
		}
		return ret;
	}

	public Integer addCollectionMapping(String deviceId, String collection) {
		int id = devIdCache.get(deviceId);
		Integer ret = null;
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = OBMPoolActivator.getDefault().getConnection();
			ps = con
					.prepareStatement("INSERT INTO opush_folder_mapping (device_id, collection) VALUES (?, ?)");
			ps.setInt(1, id);
			ps.setString(2, collection);
			ps.executeUpdate();
			ret = OBMPoolActivator.getDefault().lastInsertId(con);
		} catch (Throwable e) {
			logger.info(e.getMessage(), e);
		} finally {
			JDBCUtils.cleanup(con, ps, null);
		}
		return ret;
	}

	@Override
	public String getCollectionPath(Integer collectionId)
			throws CollectionNotFoundException {
		String ret = null;

		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			con = OBMPoolActivator.getDefault().getConnection();
			ps = con
					.prepareStatement("SELECT collection FROM opush_folder_mapping WHERE id=?");
			ps.setInt(1, collectionId);
			rs = ps.executeQuery();

			if (rs.next()) {
				ret = rs.getString(1);
			}
		} catch (Throwable se) {
			logger.error(se.getMessage(), se);
		} finally {
			JDBCUtils.cleanup(con, ps, rs);
		}
		if (ret == null) {
			throw new CollectionNotFoundException("Collection with id["
					+ collectionId + "] can not be found.");
		}
		return ret;
	}

	private String getCollectionPath(Integer collectionId, Connection con)
			throws CollectionNotFoundException {
		String ret = null;

		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = con
					.prepareStatement("SELECT collection FROM opush_folder_mapping WHERE id=?");
			ps.setInt(1, collectionId);
			rs = ps.executeQuery();

			if (rs.next()) {
				ret = rs.getString(1);
			}
		} catch (Throwable se) {
			logger.error(se.getMessage(), se);
		} finally {
			JDBCUtils.cleanup(null, ps, rs);
		}
		if (ret == null) {
			throw new CollectionNotFoundException();
		}
		return ret;
	}

	@Override
	public PIMDataType getDataClass(String collectionPath) {
		if (collectionPath.contains("\\calendar\\")) {
			return PIMDataType.CALENDAR;
		} else if (collectionPath.contains("\\contacts")) {
			return PIMDataType.CONTACTS;
		} else if (collectionPath.contains("\\email\\")) {
			return PIMDataType.EMAIL;
		} else if (collectionPath.contains("\\tasks\\")) {
			return PIMDataType.TASKS;
		} else {
			return PIMDataType.FOLDER;
		}
	}

	@Override
	public synchronized void resetForFullSync(String devId) {
		int id = devIdCache.get(devId);
		Connection con = null;
		PreparedStatement ps = null;
		UserTransaction ut = getUserTransaction();
		try {
			ut.begin();
			con = OBMPoolActivator.getDefault().getConnection();
			ps = con
					.prepareStatement("DELETE FROM opush_folder_mapping WHERE device_id=?");
			ps.setInt(1, id);
			ps.executeUpdate();

			ps = con
					.prepareStatement("DELETE FROM opush_sync_state WHERE device_id=?");
			ps.setInt(1, id);
			ps.executeUpdate();

			ps = con
					.prepareStatement("DELETE FROM opush_sync_mail WHERE device_id=?");
			ps.setInt(1, id);
			ps.executeUpdate();

			ut.commit();
			logger.warn("mappings & states cleared for full sync of device "
					+ devId);
		} catch (Throwable e) {
			JDBCUtils.rollback(ut);
			logger.error(e.getMessage(), e);
		} finally {
			JDBCUtils.cleanup(con, ps, null);
		}
	}

	@Override
	public synchronized void resetCollection(String devId, Integer collectionId) {
		int id = devIdCache.get(devId);

		Connection con = null;
		PreparedStatement ps = null;
		UserTransaction ut = getUserTransaction();
		try {
			ut.begin();
			con = OBMPoolActivator.getDefault().getConnection();
			ps = con
					.prepareStatement("DELETE FROM opush_sync_state WHERE device_id=? AND collection_id=?");
			ps.setInt(1, id);
			ps.setInt(2, collectionId);
			ps.executeUpdate();

			ps = con
					.prepareStatement("DELETE FROM opush_sync_mail WHERE device_id=? AND collection_id=?");
			ps.setInt(1, id);
			ps.setInt(2, collectionId);
			ps.executeUpdate();

			ut.commit();
			logger.warn("mappings & states cleared for sync of collection "
					+ collectionId + " of device " + devId);
		} catch (Throwable e) {
			JDBCUtils.rollback(ut);
			logger.error(e.getMessage(), e);
		} finally {
			JDBCUtils.cleanup(con, ps, null);
		}
	}

	@Override
	public Integer getDevId(String devId) {
		return devIdCache.get(devId);
	}

	@Override
	public Set<Integer> getAllCollectionId(String devId) {
		Set<Integer> ret = new HashSet<Integer>();
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		int id = devIdCache.get(devId);
		try {
			con = OBMPoolActivator.getDefault().getConnection();
			ps = con
					.prepareStatement("SELECT id FROM opush_folder_mapping WHERE device_id=?");
			ps.setInt(1, id);
			rs = ps.executeQuery();
			while (rs.next()) {
				ret.add(rs.getInt("id"));
			}
		} catch (Throwable se) {
			logger.error(se.getMessage(), se);
		} finally {
			JDBCUtils.cleanup(con, ps, null);
		}
		return ret;
	}

	@Override
	public Boolean isMostRecentInvitation(Integer eventCollectionId,
			String eventUid, Date dtStamp) {
		Boolean ret = true;
		String calQuery = "SELECT mail_uid"
				+ " FROM opush_invitation_mapping WHERE event_collection_id=? AND event_uid=? AND dtstamp > ?";
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			con = OBMPoolActivator.getDefault().getConnection();
			ps = con.prepareStatement(calQuery);
			ps.setInt(1, eventCollectionId);
			ps.setString(2, eventUid);
			ps.setTimestamp(3, new Timestamp(dtStamp.getTime()));
			rs = ps.executeQuery();
			if (rs.next()) {
				logger.info(rs.getLong("mail_uid"));
				ret = false;
			}
		} catch (Throwable se) {
			logger.error(se.getMessage(), se);
		} finally {
			JDBCUtils.cleanup(con, ps, rs);
		}
		return ret;
	}

	@Override
	public void markToDeletedSyncedInvitation(Integer eventCollectionId,
			String eventUid) {
		Connection con = null;
		PreparedStatement ps = null;
		UserTransaction ut = getUserTransaction();
		try {
			ut.begin();
			con = OBMPoolActivator.getDefault().getConnection();
			String up = "UPDATE opush_invitation_mapping SET status=?, dtstamp=dtstamp WHERE event_collection_id=? AND event_uid=? AND status=?";
			ps = con.prepareStatement(up);
			ps.setString(1, InvitationStatus.EMAIL_TO_DELETED.toString());
			ps.setInt(2, eventCollectionId);
			ps.setString(3, eventUid);
			ps.setString(4, InvitationStatus.EMAIL_SYNCED.toString());
			ps.executeUpdate();

			ps = con.prepareStatement(up);
			ps.setString(1, InvitationStatus.EVENT_TO_DELETED.toString());
			ps.setInt(2, eventCollectionId);
			ps.setString(3, eventUid);
			ps.setString(4, InvitationStatus.EVENT_SYNCED.toString());
			ps.executeUpdate();

			ut.commit();
		} catch (Throwable se) {
			JDBCUtils.rollback(ut);
			logger.error(se.getMessage(), se);
		} finally {
			JDBCUtils.cleanup(con, ps, null);
		}
	}

	@Override
	public Boolean haveEmailToDeleted(Integer eventCollectionId, String eventUid) {
		return haveInvitationToDeleted(eventCollectionId, eventUid,
				InvitationStatus.EMAIL_TO_DELETED);
	}

	@Override
	public Boolean haveEventToDeleted(Integer eventCollectionId, String eventUid) {
		return haveInvitationToDeleted(eventCollectionId, eventUid,
				InvitationStatus.EVENT_TO_DELETED);
	}

	private Boolean haveInvitationToDeleted(Integer eventCollectionId,
			String eventUid, InvitationStatus status) {
		Boolean ret = false;
		String calQuery = "SELECT status"
				+ " FROM opush_invitation_mapping WHERE event_collection_id=? AND event_uid=? AND status=?";
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			con = OBMPoolActivator.getDefault().getConnection();
			ps = con.prepareStatement(calQuery);
			int i = 1;
			ps.setInt(i++, eventCollectionId);
			ps.setString(i++, eventUid);
			ps.setString(i++, status.toString());

			rs = ps.executeQuery();
			if (rs.next()) {
				ret = true;
			}
		} catch (Throwable se) {
			logger.error(se.getMessage(), se);
		} finally {
			JDBCUtils.cleanup(con, ps, rs);
		}
		return ret;
	}

	public void createOrUpdateInvitation(Integer eventCollectionId,
			String eventUid, Date dtStamp, InvitationStatus status,
			String syncKey) {
		this.createOrUpdateInvitation(eventCollectionId, eventUid, null, null,
				dtStamp, status, syncKey);
	}

	public void createOrUpdateInvitation(Integer eventCollectionId,
			String eventUid, Integer emailCollectionId, Long emailUid,
			Date dtStamp, InvitationStatus status, String syncKey) {
		StringBuilder calQuery = new StringBuilder(
				"SELECT status"
						+ " FROM opush_invitation_mapping WHERE event_collection_id=? AND event_uid=? AND ");
		if (emailCollectionId != null) {
			calQuery.append(" mail_collection_id=? ");
		} else {
			calQuery.append(" mail_collection_id IS NULL ");
		}
		calQuery.append(" AND ");
		if (emailUid != null) {
			calQuery.append(" mail_uid=? ");
		} else {
			calQuery.append(" mail_uid IS NULL ");
		}
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			con = OBMPoolActivator.getDefault().getConnection();
			ps = con.prepareStatement(calQuery.toString());
			int i = 1;
			ps.setInt(i++, eventCollectionId);
			ps.setString(i++, eventUid);

			if (emailCollectionId != null) {
				ps.setInt(i++, emailCollectionId);
			}
			if (emailUid != null) {
				ps.setLong(i++, emailUid);
			}
			rs = ps.executeQuery();
			if (rs.next()) {
				updateSyncedInvitation(con, eventCollectionId, eventUid,
						emailCollectionId, emailUid, dtStamp, status, syncKey);
			} else {
				createInvitation(con, eventCollectionId, eventUid,
						emailCollectionId, emailUid, dtStamp, status, syncKey);
			}
		} catch (Throwable se) {
			logger.error(se.getMessage(), se);
		} finally {
			JDBCUtils.cleanup(con, ps, rs);
		}
	}

	private void updateSyncedInvitation(Connection con,
			Integer eventCollectionId, String eventUid,
			Integer emailCollectionId, Long emailUid, Date dtStamp,
			InvitationStatus status, String synkKey) {
		PreparedStatement ps = null;
		UserTransaction ut = getUserTransaction();
		try {
			ut.begin();
			con = OBMPoolActivator.getDefault().getConnection();
			StringBuilder query = new StringBuilder("UPDATE opush_invitation_mapping SET status=?, dtstamp=?, sync_key=? WHERE event_collection_id=? AND event_uid=? "); 
			if(emailCollectionId != null){
				query.append(" AND mail_collection_id=? ");
			} else {
				query.append(" AND mail_collection_id IS NULL ");
			}
			if(emailUid != null){
				query.append(" AND mail_uid=? ");
			} else {
				query.append(" AND mail_uid IS NULL ");
			}
			ps = con
					.prepareStatement(query.toString());
			
			ps.setString(1, status.toString());
			ps.setTimestamp(2, new Timestamp(dtStamp.getTime()));
			ps.setString(3, synkKey);
			ps.setInt(4, eventCollectionId);
			ps.setString(5, eventUid);
			if (emailCollectionId != null) {
				ps.setInt(6, emailCollectionId);
			} 
			if (emailUid != null) {
				ps.setLong(7, emailUid);
			} 

			ps.execute();
			ut.commit();
		} catch (Throwable se) {
			try {
				ut.rollback();
			} catch (Exception e) {
				logger.error("Error while rolling-back", e);
			}
			logger.error(se.getMessage(), se);
		} finally {
			JDBCUtils.cleanup(null, ps, null);
		}
	}

	private void createInvitation(Connection con, Integer eventCollectionId,
			String eventUid, Integer emailCollectionId, Long emailUid,
			Date dtStamp, InvitationStatus status, String syncKey)
			throws SQLException {
		PreparedStatement ps = null;
		try {

			ps = con
					.prepareStatement("INSERT into opush_invitation_mapping (mail_collection_id, mail_uid, event_collection_id, event_uid, dtstamp, status, sync_key) VALUES (?,?,?,?,?,?,?)");
			int i = 1;
			if (emailCollectionId != null) {
				ps.setInt(i++, emailCollectionId);
			} else {
				ps.setNull(i++, Types.INTEGER);
			}
			if (emailUid != null) {
				ps.setLong(i++, emailUid);
			} else {
				ps.setNull(i++, Types.INTEGER);
			}

			ps.setInt(i++, eventCollectionId);
			ps.setString(i++, eventUid);

			ps.setTimestamp(i++, new Timestamp(dtStamp.getTime()));

			ps.setString(i++, status.toString());
			ps.setString(i++, syncKey);
			ps.execute();
		} finally {
			JDBCUtils.cleanup(null, ps, null);
		}
	}

	@Override
	public void updateInvitationStatus(InvitationStatus status, String syncKey,
			Integer eventCollectionId, Integer emailCollectionId,
			Long... emailUids) {
		if (emailUids != null && emailUids.length > 0) {
			Connection con = null;
			PreparedStatement ps = null;
			UserTransaction ut = getUserTransaction();
			String uids = buildEmailUid(emailUids);
			try {
				ut.begin();
				con = OBMPoolActivator.getDefault().getConnection();
				ps = con
						.prepareStatement("UPDATE opush_invitation_mapping SET status=?, sync_key=?, dtstamp=dtstamp WHERE event_collection_id=? AND mail_collection_id=? AND mail_uid IN ("
								+ uids + ")");
				ps.setString(1, status.toString());
				ps.setString(2, syncKey);
				ps.setInt(3, eventCollectionId);
				ps.setInt(4, emailCollectionId);
				ps.execute();
				ut.commit();
			} catch (Throwable se) {
				try {
					ut.rollback();
				} catch (Exception e) {
					logger.error("Error while rolling-back", e);
				}
				logger.error(se.getMessage(), se);
			} finally {
				JDBCUtils.cleanup(con, ps, null);
			}
		}
	}

	@Override
	public void updateInvitationStatus(InvitationStatus status, String syncKey,
			Integer eventCollectionId, String... eventUids) {
		if (eventUids != null && eventUids.length > 0) {
			Connection con = null;
			PreparedStatement ps = null;
			UserTransaction ut = getUserTransaction();
			String uids = buildEventUid(eventUids);
			try {
				ut.begin();
				con = OBMPoolActivator.getDefault().getConnection();
				ps = con
						.prepareStatement("UPDATE opush_invitation_mapping SET status=?, sync_key=?, dtstamp=dtstamp WHERE mail_collection_id IS NULL AND mail_uid IS NULL AND event_collection_id=? AND event_uid IN ("
								+ uids + ")");
				ps.setString(1, status.toString());
				ps.setString(2, syncKey);
				ps.setInt(3, eventCollectionId);
				ps.execute();
				ut.commit();
			} catch (Throwable se) {
				try {
					ut.rollback();
				} catch (Exception e) {
					logger.error("Error while rolling-back", e);
				}
				logger.error(se.getMessage(), se);
			} finally {
				JDBCUtils.cleanup(con, ps, null);
			}
		}
	}

	@Override
	public List<Long> getEmailToSynced(Integer emailCollectionId, String syncKey) {
		return getEmail(emailCollectionId, syncKey,
				InvitationStatus.EMAIL_SYNCED, InvitationStatus.EMAIL_TO_SYNCED);
	}

	@Override
	public List<Long> getEmailToDeleted(Integer emailCollectionId,
			String syncKey) {
		return getEmail(emailCollectionId, syncKey, InvitationStatus.DELETED,
				InvitationStatus.EMAIL_TO_DELETED);
	}

	private List<Long> getEmail(Integer emailCollectionId, String syncKey,
			InvitationStatus status, InvitationStatus statusAction) {
		List<Long> ret = new ArrayList<Long>();
		String calQuery = "SELECT mail_uid"
				+ " FROM opush_invitation_mapping WHERE mail_collection_id=? AND status=? OR ( sync_key=? AND status=? )";
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			con = OBMPoolActivator.getDefault().getConnection();
			ps = con.prepareStatement(calQuery);
			ps.setInt(1, emailCollectionId);
			ps.setString(2, statusAction.toString());
			ps.setString(3, syncKey);
			ps.setString(4, status.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				ret.add(rs.getLong("mail_uid"));
			}
		} catch (Throwable se) {
			logger.error(se.getMessage(), se);
		} finally {
			JDBCUtils.cleanup(con, ps, rs);
		}
		return ret;
	}

	@Override
	public List<String> getEventToSynced(Integer eventCollectionId,
			String syncKey) {
		return getEvent(eventCollectionId, syncKey,
				InvitationStatus.EVENT_SYNCED, InvitationStatus.EVENT_TO_SYNCED);
	}

	@Override
	public List<String> getEventToDeleted(Integer eventCollectionId,
			String syncKey) {
		return getEvent(eventCollectionId, syncKey, InvitationStatus.DELETED,
				InvitationStatus.EVENT_TO_DELETED);
	}

	private List<String> getEvent(Integer eventCollectionId, String syncKey,
			InvitationStatus status, InvitationStatus statusAction) {
		List<String> ret = new ArrayList<String>();
		String calQuery = "SELECT event_uid"
				+ " FROM opush_invitation_mapping WHERE event_collection_id=? AND status=? OR ( sync_key=? AND status=? )";
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			con = OBMPoolActivator.getDefault().getConnection();
			ps = con.prepareStatement(calQuery);
			ps.setInt(1, eventCollectionId);
			ps.setString(2, statusAction.toString());
			ps.setString(3, syncKey);
			ps.setString(4, status.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				ret.add("" + rs.getInt("event_uid"));
			}
		} catch (Throwable se) {
			logger.error(se.getMessage(), se);
		} finally {
			JDBCUtils.cleanup(con, ps, rs);
		}
		return ret;
	}

	private String buildEmailUid(Long[] emailUids) {
		StringBuilder sb = new StringBuilder();
		sb.append("0");
		for (Long l : emailUids) {
			sb.append(",");
			sb.append(l);
		}
		return sb.toString();
	}

	private String buildEventUid(String[] eventUids) {
		StringBuilder sb = new StringBuilder();
		sb.append("'0'");
		for (String s : eventUids) {
			sb.append(",");
			sb.append("'");
			sb.append(s);
			sb.append("'");
		}
		return sb.toString();
	}

	public Object getJdbcObject(String value, String type) {
		if ("PGSQL".equals(type)) {
			try {
				Object o = Class.forName("org.postgresql.util.PGobject")
						.newInstance();
				Method setType = o.getClass()
						.getMethod("setType", String.class);
				Method setValue = o.getClass().getMethod("setValue",
						String.class);

				setType.invoke(o, "vpartstat");
				setValue.invoke(o, toString());
				return o;
			} catch (Throwable e) {
				e.printStackTrace();
			}
			return null;
		} else {
			return value;
		}
	}

	@Override
	public void removeInvitationStatus(Integer eventCollectionId,
			Integer emailCollectionId, Long emailUid) {
		Connection con = null;
		PreparedStatement ps = null;
		UserTransaction ut = getUserTransaction();
		try {
			ut.begin();
			con = OBMPoolActivator.getDefault().getConnection();
			ps = con
					.prepareStatement("DELETE FROM opush_invitation_mapping "
							+ "WHERE event_collection_id=? AND mail_collection_id=? AND mail_uid=?");
			ps.setInt(1, eventCollectionId);
			ps.setInt(2, emailCollectionId);
			ps.setLong(3, emailUid);
			ps.execute();
			ut.commit();
		} catch (Throwable se) {
			try {
				ut.rollback();
			} catch (Exception e) {
				logger.error("Error while rolling-back", e);
			}
			logger.error(se.getMessage(), se);
		} finally {
			JDBCUtils.cleanup(con, ps, null);
		}
	}
}
