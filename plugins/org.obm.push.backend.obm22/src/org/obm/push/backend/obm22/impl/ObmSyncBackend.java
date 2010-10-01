package org.obm.push.backend.obm22.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.minig.obm.pool.OBMPoolActivator;
import org.obm.locator.client.LocatorClient;
import org.obm.push.backend.BackendSession;
import org.obm.push.backend.ItemChange;
import org.obm.push.backend.PIMDataType;
import org.obm.push.exception.ActiveSyncException;
import org.obm.push.exception.CollectionNotFoundException;
import org.obm.push.store.ISyncStorage;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.client.book.BookClient;
import org.obm.sync.client.calendar.AbstractEventSyncClient;
import org.obm.sync.client.calendar.CalendarClient;
import org.obm.sync.locators.AddressBookLocator;
import org.obm.sync.locators.CalendarLocator;
import org.obm.sync.locators.TaskLocator;

import fr.aliasource.utils.JDBCUtils;

public class ObmSyncBackend {

	protected Log logger = LogFactory.getLog(getClass());

	protected String obmSyncHost;
	protected ISyncStorage storage;

	protected ObmSyncBackend(ISyncStorage storage) {
		validateOBMConnection();
		this.storage = storage;
	}

	protected void locateObmSync(String loginAtDomain) {
		obmSyncHost = new LocatorClient().locateHost("sync/obm_sync",
				loginAtDomain);
		logger.info("Using " + obmSyncHost + " as obm_sync host.");
	}

	protected CalendarClient getCalendarClient(BackendSession bs) {
		return (CalendarClient) getCalendarClient(bs, PIMDataType.CALENDAR);
	}

	protected AbstractEventSyncClient getCalendarClient(BackendSession bs,
			PIMDataType type) {

		if (obmSyncHost == null) {
			locateObmSync(bs.getLoginAtDomain());
		}

		AbstractEventSyncClient cli = null;
		if (PIMDataType.TASKS.equals(type)) {
			TaskLocator tl = new TaskLocator();
			cli = tl
					.locate("http://" + obmSyncHost + ":8080/obm-sync/services");
		} else {
			CalendarLocator cl = new CalendarLocator();
			cli = cl
					.locate("http://" + obmSyncHost + ":8080/obm-sync/services");
		}

		return cli;
	}

	protected BookClient getBookClient(BackendSession bs) {
		AddressBookLocator abl = new AddressBookLocator();
		if (obmSyncHost == null) {
			locateObmSync(bs.getLoginAtDomain());
		}
		BookClient bookCli = abl.locate("http://" + obmSyncHost
				+ ":8080/obm-sync/services");
		return bookCli;
	}

	protected ItemChange getDeletion(Integer collectionId, String del)
			throws ActiveSyncException {
		ItemChange ic = new ItemChange();
		ic.setServerId(getServerIdFor(collectionId, del));
		return ic;
	}

	protected List<ItemChange> getDeletions(Integer collectionId, Collection<? extends Object> uids)
			throws ActiveSyncException {
		List<ItemChange> deletions = new LinkedList<ItemChange>();
		for (Object uid : uids) {
			deletions.add(getDeletion(collectionId, uid.toString()));
		}
		return deletions;
	}

	public boolean validatePassword(String loginAtDomain, String password) {
		CalendarLocator cl = new CalendarLocator();
		if (obmSyncHost == null) {
			locateObmSync(loginAtDomain);
		}
		CalendarClient cc = cl.locate("http://" + obmSyncHost
				+ ":8080/obm-sync/services");
		AccessToken token = cc.login(loginAtDomain, password, "o-push");
		Boolean valid = true;
		if (token == null || token.getSessionId() == null) {
			logger
					.info(loginAtDomain
							+ " can't log on obm-sync. The username or password isn't valid");
			valid = false;
		}
		cc.logout(token);
		return valid;
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

	protected String getDefaultCalendarName(BackendSession bs) {
		return "obm:\\\\" + bs.getLoginAtDomain() + "\\calendar\\"
				+ bs.getLoginAtDomain();
	}

	public Integer getCollectionIdFor(String deviceId, String collection)
			throws CollectionNotFoundException {
		return storage.getCollectionMapping(deviceId, collection);
	}

	public String getCollectionPathFor(Integer collectionId)
			throws ActiveSyncException {
		return storage.getCollectionPath(collectionId);
	}

	public String getServerIdFor(Integer collectionId, String clientId)
			throws ActiveSyncException {
		StringBuilder sb = new StringBuilder(10);
		sb.append(collectionId);
		if (clientId != null) {
			sb.append(':');
			sb.append(clientId);
		}
		return sb.toString();
	}

	protected Integer getItemIdFor(String serverId) {
		int idx = serverId.lastIndexOf(":");
		return Integer.parseInt(serverId.substring(idx + 1));
	}

	public int getDevId(String deviceId) {
		return storage.getDevId(deviceId);
	}

	public String createCollectionMapping(String devId, String col) {
		return storage.addCollectionMapping(devId, col).toString();
	}

}
