package org.obm.push.storage.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.minig.obm.pool.OBMPoolActivator;
import org.obm.push.state.SyncState;
import org.obm.push.store.ISyncStorage;

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
				ret = new SyncState();
				ret.setKey(rs.getString(1));
				cal.setTimeInMillis(rs.getTimestamp(2).getTime());
				ret.setLastSync(cal.getTime());
			}
		} catch (SQLException se) {
			logger.error(se.getMessage(), se);
		} finally {
			JDBCUtils.cleanup(con, ps, null);
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
					.prepareStatement("SELECT device_id, last_sync FROM opush_sync_state WHERE sync_key=?");
			ps.setString(1, syncKey);

			rs = ps.executeQuery();
			if (rs.next()) {
				ret = new SyncState();
				ret.setKey(syncKey);
				ret.setLastSync(rs.getTimestamp(2));
			}
		} catch (SQLException se) {
			logger.error(se.getMessage(), se);
		} finally {
			JDBCUtils.cleanup(con, ps, null);
		}
		return ret;
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
		try {
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
					return false;
				}
			} else {
				id = rs.getInt(1);
			}
			devIdCache.put(deviceId, id);
		} catch (SQLException se) {
			logger.error(se.getMessage(), se);
			return false;
		} finally {
			JDBCUtils.cleanup(con, ps, rs);
		}

		return true;
	}

	public boolean syncAuthorized(String loginAtDomain, String deviceId) {
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
		} catch (SQLException se) {
			logger.error(se.getMessage(), se);
		} finally {
			JDBCUtils.cleanup(con, ps, null);
		}
		if(!hasSyncPerm){
			logger.info(loginAtDomain+" isn't authorized to synchronize in OBM-UI");
		}
		return hasSyncPerm;
	}

	@Override
	public void updateState(String devId, Integer collectionId,
			SyncState oldState, SyncState state) {
		int id = devIdCache.get(devId);

		Connection con = null;
		PreparedStatement ps = null;

		try {
			con = OBMPoolActivator.getDefault().getConnection();
			// con.setAutoCommit(false);
			ps = con
					.prepareStatement("DELETE FROM opush_sync_state WHERE device_id=? AND collection_id=?");
			ps.setInt(1, id);
			ps.setInt(2, collectionId);
			ps.executeUpdate();

			ps.close();
			ps = con
					.prepareStatement("INSERT INTO opush_sync_state (sync_key, device_id, last_sync, collection_id) VALUES (?, ?, ?, ?)");
			ps.setString(1, state.getKey());
			ps.setInt(2, id);
			ps.setTimestamp(3, new Timestamp(state.getLastSync().getTime()));
			ps.setInt(4, collectionId);
			ps.executeUpdate();
			// con.commit();
		} catch (SQLException se) {
			logger.error(se.getMessage(), se);
			// JDBCUtils.rollback(con);
		} finally {
			JDBCUtils.cleanup(con, ps, null);
		}
	}

	@Override
	public Integer getCollectionMapping(String deviceId, String collection) {
		int id = devIdCache.get(deviceId);
		Integer ret = null;

		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			con = OBMPoolActivator.getDefault().getConnection();
			// con.setAutoCommit(false);
			ps = con
					.prepareStatement("SELECT id FROM opush_folder_mapping WHERE device_id=? AND collection=?");
			ps.setInt(1, id);
			ps.setString(2, collection);
			rs = ps.executeQuery();

			if (rs.next()) {
				ret = rs.getInt(1);
			} else {
				rs.close();
				rs = null;
				ps.close();

				ps = con
						.prepareStatement("INSERT INTO opush_folder_mapping (device_id, collection) VALUES (?, ?)");
				ps.setInt(1, id);
				ps.setString(2, collection);
				ps.executeUpdate();
				ret = OBMPoolActivator.getDefault().lastInsertId(con);
			}

			// con.commit();
		} catch (SQLException se) {
			logger.error(se.getMessage(), se);
			// JDBCUtils.rollback(con);
		} finally {
			JDBCUtils.cleanup(con, ps, rs);
		}
		return ret;
	}

	@Override
	public String getCollectionString(Integer collectionId) {
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
		} catch (SQLException se) {
			logger.error(se.getMessage(), se);
		} finally {
			JDBCUtils.cleanup(con, ps, rs);
		}
		return ret;
	}

	@Override
	public String getDataClass(String collectionId) {
		// TODO add mail & tasks
		if (collectionId.contains("\\calendar\\")) {
			return "Calendar";
		} else if (collectionId.contains("\\contacts")) {
			return "Contacts";
		} else if (collectionId.contains("\\email\\")) {
			return "Email";
		} else if (collectionId.contains("\\task\\")) {
			return "Task";
		} else {
			return "Folder";
		}
	}

	@Override
	public void resetForFullSync(String devId) {
		int id = devIdCache.get(devId);

		Connection con = null;
		PreparedStatement ps = null;

		try {
			con = OBMPoolActivator.getDefault().getConnection();
			// con.setAutoCommit(false);
			ps = con
					.prepareStatement("DELETE FROM opush_sync_state WHERE device_id=?");
			ps.setInt(1, id);
			ps.executeUpdate();

			ps = con
					.prepareStatement("DELETE FROM opush_sync_mail WHERE device_id=?");
			ps.setInt(1, id);
			ps.executeUpdate();

			// con.commit();
			logger.warn("mappings & states cleared for full sync of device "
					+ devId);
		} catch (Exception e) {
			// JDBCUtils.rollback(con);
			logger.error(e.getMessage(), e);
		} finally {
			JDBCUtils.cleanup(con, ps, null);
		}
	}

	@Override
	public void resetCollection(String devId, Integer collectionId) {
		int id = devIdCache.get(devId);

		Connection con = null;
		PreparedStatement ps = null;

		try {
			con = OBMPoolActivator.getDefault().getConnection();
			// con.setAutoCommit(false);
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

			// con.commit();
			logger.warn("mappings & states cleared for sync of collection "+collectionId+" of device "
					+ devId);
		} catch (Exception e) {
			// JDBCUtils.rollback(con);
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
		} catch (SQLException se) {
			logger.error(se.getMessage(), se);
		} finally {
			JDBCUtils.cleanup(con, ps, null);
		}
		return ret;
	}

}
