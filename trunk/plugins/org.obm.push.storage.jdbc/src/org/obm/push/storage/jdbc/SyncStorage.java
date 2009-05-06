package org.obm.push.storage.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

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
	public SyncState findStateForDevice(String devId, String collectionId) {
		int id = devIdCache.get(devId);

		SyncState ret = null;
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			con = OBMPoolActivator.getDefault().getConnection();
			ps = con
					.prepareStatement("SELECT sync_key, last_sync FROM sync_state WHERE device_id=? AND collection=?");
			ps.setInt(1, id);
			ps.setString(2, collectionId);
			rs = ps.executeQuery();
			if (rs.next()) {
				ret = new SyncState();
				ret.setKey(rs.getString(1));
				ret.setLastSync(rs.getDate(2));
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
					.prepareStatement("SELECT device_id, last_sync FROM sync_state WHERE sync_key=?");
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
	public String getClientId(String deviceId, String serverId) {
		int id = devIdCache.get(deviceId);

		String ret = null;
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			con = OBMPoolActivator.getDefault().getConnection();
			ps = con
					.prepareStatement("SELECT client_id FROM id_mapping WHERE device_id=? AND server_id=?");
			ps.setInt(1, id);
			ps.setString(2, serverId);
			rs = ps.executeQuery();
			if (rs.next()) {
				ret = rs.getString(1);
			}
		} catch (SQLException se) {
			logger.error(se.getMessage(), se);
		} finally {
			JDBCUtils.cleanup(con, ps, null);
		}
		return ret;
	}

	@Override
	public String getServerId(String deviceId, String clientId) {
		int id = devIdCache.get(deviceId);

		String ret = null;
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			con = OBMPoolActivator.getDefault().getConnection();
			ps = con
					.prepareStatement("SELECT server_id FROM id_mapping WHERE device_id=? AND client_id=?");
			ps.setInt(1, id);
			ps.setString(2, clientId);
			rs = ps.executeQuery();
			if (rs.next()) {
				ret = rs.getString(1);
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
		String login = parts[0];
		String domain = parts[1];

		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		int id = 0;
		try {
			con = OBMPoolActivator.getDefault().getConnection();
			ps = con
					.prepareStatement("SELECT id FROM device "
							+ "INNER JOIN UserObm ON owner=userobm_id "
							+ "INNER JOIN Domain ON userobm_domain_id=domain_id "
							+ "WHERE identifier=? AND type=? AND userobm_login=? AND domain_name=?");
			ps.setString(1, deviceId);
			ps.setString(2, deviceType);
			ps.setString(3, login);
			ps.setString(4, domain);
			rs = ps.executeQuery();
			if (!rs.next()) {
				rs.close();
				ps.close();

				ps = con
						.prepareStatement("INSERT INTO device (identifier, type, owner) "
								+ "SELECT ?, ?, userobm_id FROM UserObm "
								+ "INNER JOIN Domain ON userobm_domain_id=domain_id "
								+ "WHERE userobm_login=? AND domain_name=?");
				ps.setString(1, deviceId);
				ps.setString(2, deviceType);
				ps.setString(3, login);
				ps.setString(4, domain);
				int insert = ps.executeUpdate();
				if (insert > 0) {
					id = JDBCUtils.lastInsertId(con);
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

	@Override
	public void storeMapping(String deviceId, String clientId, String serverId) {
		int id = devIdCache.get(deviceId);

		Connection con = null;
		PreparedStatement ps = null;

		try {
			con = OBMPoolActivator.getDefault().getConnection();
			ps = con
					.prepareStatement("INSERT INTO id_mapping (device_id, client_id, server_id) VALUES (?, ?, ?)");
			ps.setInt(1, id);
			ps.setString(2, clientId);
			ps.setString(3, serverId);
			ps.executeUpdate();
		} catch (SQLException se) {
			logger.error(se.getMessage(), se);
		} finally {
			JDBCUtils.cleanup(con, ps, null);
		}
	}

	@Override
	public void updateState(String devId, String collectionId, SyncState oldState, SyncState state) {
		int id = devIdCache.get(devId);

		Connection con = null;
		PreparedStatement ps = null;

		try {
			con = OBMPoolActivator.getDefault().getConnection();
			con.setAutoCommit(false);
			ps = con
					.prepareStatement("DELETE FROM sync_state WHERE device_id=? AND collection=?");
			ps.setInt(1, devIdCache.get(devId));
			ps.setString(2, collectionId);
			ps.executeUpdate();

			ps.close();
			ps = con
					.prepareStatement("INSERT INTO sync_state (sync_key, device_id, last_sync, collection) VALUES (?, ?, ?, ?)");
			ps.setString(1, state.getKey());
			ps.setInt(2, id);
			ps.setTimestamp(3, new Timestamp(state.getLastSync().getTime()));
			ps.setString(4, collectionId);
			ps.executeUpdate();
			con.commit();
		} catch (SQLException se) {
			logger.error(se.getMessage(), se);
			JDBCUtils.rollback(con);
		} finally {
			JDBCUtils.cleanup(con, ps, null);
		}

	}

}
