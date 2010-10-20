package org.obm.push.backend.obm22.mail;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.minig.imap.IMAPException;
import org.minig.imap.IdleClient;
import org.minig.imap.idle.IIdleCallback;
import org.minig.imap.idle.IdleLine;
import org.minig.imap.idle.IdleTag;
import org.obm.locator.client.LocatorClient;
import org.obm.push.backend.BackendSession;
import org.obm.push.backend.ICollectionChangeListener;
import org.obm.push.backend.SyncCollection;
import org.obm.push.backend.obm22.impl.ChangedCollections;
import org.obm.push.backend.obm22.impl.ObmSyncBackend;
import org.obm.push.backend.obm22.impl.PushNotification;
import org.obm.push.exception.ActiveSyncException;

public class EmailMonitoringThread implements IIdleCallback {

	/**
	 * SynchronizedSet, all accesses should be synchronized
	 */
	private Set<ICollectionChangeListener> ccls;

	protected Log logger = LogFactory.getLog(getClass());
	protected ObmSyncBackend backend;
	protected String imapHost;
	private BackendSession bs;
	private String collectionName;
	private Boolean remainConnected;  

	private IdleClient store;

	public EmailMonitoringThread(ObmSyncBackend cb,
			Set<ICollectionChangeListener> ccls, BackendSession bs,
			Integer collectionId) throws ActiveSyncException {
		remainConnected = false;
		this.ccls = Collections.synchronizedSet(ccls);
		this.backend = cb;
		this.bs = bs;
		collectionName = backend.getCollectionPathFor(collectionId);
	}

	public synchronized void startIdle() throws IMAPException {
		if (store == null) {
			store = getIdleClient(bs);
			store.login(EmailManager.getInstance().getActivateTLS());
			store.select(EmailManager.getInstance().parseMailBoxName(bs,
					collectionName));
			store.startIdle(this);
		}
		remainConnected = true;
		logger.info("Start email push monitoring for collection[ "
				+ collectionName + "]");
	}
	
	public synchronized void stopIdle() {
		if (store != null) {
			store.stopIdle();
			store.logout();
			store = null;
		}
		remainConnected = false;
		logger.info("Stop email push monitoring for collection[ "
				+ collectionName + "]");
	}

	private Set<SyncCollection> getChangedCollections(BackendSession session,
			ChangedCollections cols, Set<SyncCollection> monitoredCollections) {
		Set<SyncCollection> ret = new HashSet<SyncCollection>();

		for (SyncCollection sc : cols.getChanged()) {
			try {
				int id = backend.getCollectionIdFor(session.getDevId(), sc
						.getCollectionPath());
				sc.setCollectionId(id);
				logger.info("processing sc: id: " + sc.getCollectionId()
						+ " name: " + sc.getCollectionPath());
				if (monitoredCollections.contains(sc)) {
					logger.info("******** PUSH " + sc.getCollectionId()
							+ " name: " + sc.getCollectionPath() + " ********");
					ret.add(sc);
				} else {
					logger.info("** " + sc.getCollectionId()
							+ " modified but nobody cares **");
					for (SyncCollection mon : monitoredCollections) {
						logger.info("   * monitored: " + mon.getCollectionId());
					}
				}
			} catch (ActiveSyncException e) {
				logger.error("getChangedError", e);
			}
		}

		for (SyncCollection toPush : ret) {
			try {
				String colName = toPush.getCollectionPath();
				int collectionId = backend.getCollectionIdFor(session
						.getDevId(), colName);
				toPush.setCollectionId(collectionId);
			} catch (ActiveSyncException e) {
				logger.error("getChangedError", e);
			}

		}

		return ret;
	}

	@Override
	public synchronized void receive(IdleLine line) {
		if (line != null) {
			if ((IdleTag.EXISTS.equals(line.getTag()) || IdleTag.FETCH
					.equals(line.getTag()))) {
				stopIdle();
				Set<SyncCollection> lsc = new HashSet<SyncCollection>();

				SyncCollection sc = new SyncCollection();
				String s = collectionName;
				sc.setCollectionPath(s);
				lsc.add(sc);

				Calendar cal = Calendar
						.getInstance(TimeZone.getTimeZone("GMT"));
				cal.setTime(new Date());

				ChangedCollections cols = new ChangedCollections(cal.getTime(),
						lsc);
				logger.info("DB lastSync: " + cal.getTime());

				LinkedList<PushNotification> toNotify = new LinkedList<PushNotification>();
				for (ICollectionChangeListener ccl : ccls) {
					Set<SyncCollection> monitoredCollections = ccl
							.getMonitoredCollections();
					Set<SyncCollection> changes = getChangedCollections(ccl
							.getSession(), cols, monitoredCollections);
					if (!changes.isEmpty()) {
						this.stopIdle();
						toNotify.add(new PushNotification(changes, ccl));
					}
				}
				for (PushNotification pn : toNotify) {
					pn.emit();
				}
			}
		}
	}

	private IdleClient getIdleClient(BackendSession bs) {
		if (imapHost == null) {
			locateImap(bs);
		}
		String login = bs.getLoginAtDomain();
		boolean useDomain = EmailManager.getInstance().getLoginWithDomain();
		if (!useDomain) {
			int at = login.indexOf("@");
			if (at > 0) {
				login = login.substring(0, at);
			}
		}
		logger.info("creating idleClient with login: " + login
				+ " (loginWithDomain: " + useDomain + ")");
		IdleClient idleCli = new IdleClient(imapHost, 143, login, bs
				.getPassword());
		return idleCli;
	}

	private void locateImap(BackendSession bs) {
		imapHost = new LocatorClient().locateHost("mail/imap_frontend", bs
				.getLoginAtDomain());
		logger.info("Using " + imapHost + " as imap host.");
	}

	@Override
	public synchronized void disconnectedCallBack() {
		if(store != null){
			try{
				stopIdle();
			} catch (Throwable e) {
				logger.error(e.getMessage(),e );
			}
		}
		if(remainConnected){
			try {
				startIdle();
			} catch (IMAPException e) {
				logger.error("SEND ERROR TO PDA",e );
				//TODO SEND ERROR TO PDA
			}	
		}
	}
}
