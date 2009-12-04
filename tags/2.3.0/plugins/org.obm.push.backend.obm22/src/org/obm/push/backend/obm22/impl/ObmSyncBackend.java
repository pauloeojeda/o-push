package org.obm.push.backend.obm22.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.minig.obm.pool.OBMPoolActivator;
import org.obm.locator.client.LocatorClient;
import org.obm.push.backend.BackendSession;
import org.obm.push.backend.ItemChange;
import org.obm.push.store.ISyncStorage;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.client.calendar.CalendarClient;
import org.obm.sync.locators.CalendarLocator;

import fr.aliasource.utils.JDBCUtils;

public class ObmSyncBackend {

	protected Log logger = LogFactory.getLog(getClass());

	protected String obmSyncHost;
	private ISyncStorage storage;

	protected ObmSyncBackend(ISyncStorage storage) {
		validateOBMConnection();
		this.storage = storage;
	}

	protected void locateObmSync(String loginAtDomain) {
		obmSyncHost = new LocatorClient().locateHost("sync/obm_sync", loginAtDomain);
		logger.info("Using " + obmSyncHost + " as obm_sync host.");
	}
	
	protected CalendarClient getCalendarClient(BackendSession bs) {
		CalendarLocator cl = new CalendarLocator();
		if (obmSyncHost == null) {
			locateObmSync(bs.getLoginAtDomain());
		}
		CalendarClient calCli = cl.locate("http://" + obmSyncHost
				+ ":8080/obm-sync/services");
		return calCli;
	}

	protected ItemChange getDeletion(BackendSession bs, String collection,
			String del) {
		ItemChange ic = new ItemChange();
		ic.setServerId(getServerIdFor(bs.getDevId(), collection, del));
		return ic;
	}
	
	public boolean validatePassword(String loginAtDomain, String password) {
		CalendarLocator cl = new CalendarLocator();
		if (obmSyncHost == null) {
			locateObmSync(loginAtDomain);
		}
		CalendarClient cc = cl.locate("http://" + obmSyncHost
				+ ":8080/obm-sync/services");
		AccessToken token = cc.login(loginAtDomain, password,
				"o-push");
		if(token == null || token.getSessionId() == null){
			logger.info(loginAtDomain+" can't log on obm-sync. The username or password isn't valid" );
			return false;
		}
		return true;
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

	public Integer getCollectionIdFor(String deviceId, String collection) {
		return storage.getCollectionMapping(deviceId, collection);
	}
	
	public String getCollectionNameFor(Integer collectionId) {
		return storage.getCollectionString(collectionId);
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

	public int getDevId(String deviceId) {
		return storage.getDevId(deviceId);
	}
}
