package org.obm.push.storage.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.minig.obm.pool.OBMPoolActivator;
import org.obm.push.state.SyncState;
import org.obm.push.store.ISyncStorage;

import fr.aliasource.utils.JDBCUtils;

public class SyncStorage implements ISyncStorage {

	private Map<String, Integer> devIdCache;
	private static final Log logger = LogFactory.getLog(SyncStorage.class);

	public SyncStorage() {
		this.devIdCache = new HashMap<String, Integer>();
	}

	@Override
	public SyncState findStateForDevice(String devId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SyncState findStateForKey(String syncKey) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getClientId(String deviceId, String serverId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getServerId(String deviceId, String clientId) {
		// TODO Auto-generated method stub
		return null;
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
		// TODO Auto-generated method stub

	}

	@Override
	public void updateState(String devId, SyncState state) {
		// TODO Auto-generated method stub

	}

}
