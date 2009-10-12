package org.obm.push.backend.obm22.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.minig.obm.pool.OBMPoolActivator;
import org.obm.push.backend.BackendSession;
import org.obm.push.backend.ItemChange;
import org.obm.push.store.ISyncStorage;

import fr.aliasource.utils.JDBCUtils;

public class ObmSyncBackend {

	protected Log logger = LogFactory.getLog(getClass());

	protected String obmSyncHost;
	private ISyncStorage storage;

	protected ObmSyncBackend(ISyncStorage storage) {
		validateOBMConnection();
		this.storage = storage;
	}

	protected void locateObmSync(BackendSession bs) {
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

	protected ItemChange getDeletion(BackendSession bs, String collection,
			String del) {
		ItemChange ic = new ItemChange();
		ic.setServerId(getServerIdFor(bs.getDevId(), collection, del));
		return ic;
	}

	private void validateOBMConnection() {
		Connection con = OBMPoolActivator.getDefault().getConnection();
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
	
	public int getCollectionIdFor(String deviceId, String collection) {
		return storage.getCollectionMapping(deviceId, collection);
	}

	public String getServerIdFor(String deviceId, String collection,
			String clientId) {
		int folderId = storage.getCollectionMapping(deviceId, collection);
		StringBuilder sb = new StringBuilder(10);
		sb.append(folderId);
		if (clientId != null) {
			sb.append(':');
			sb.append(clientId);
		}
		return sb.toString();
	}

}
