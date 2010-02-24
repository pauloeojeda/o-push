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

	private IdleClient store;

	public EmailMonitoringThread(ObmSyncBackend cb,
			Set<ICollectionChangeListener> ccls, BackendSession bs,
			Integer collectionId) {
		this.ccls = Collections.synchronizedSet(ccls);
		this.backend = cb;
		this.bs = bs;
		collectionName = backend.getCollectionNameFor(collectionId);
	}

	public void startIdle() throws IMAPException {
		if (store == null) {
			store = getIdleClient(bs);
			store.login();
			store.select(EmailManager.getInstance().parseMailBoxName(bs,
					collectionName));
		}
		store.startIdle();
		logger.info("Start email push monitoring for collection[ "
				+ collectionName + "]");
	}

	private Set<SyncCollection> getChangedCollections(BackendSession session,
			ChangedCollections cols, Set<SyncCollection> monitoredCollections) {
		Set<SyncCollection> ret = new HashSet<SyncCollection>();

		for (SyncCollection sc : cols.getChanged()) {
			int id = backend.getCollectionIdFor(session.getDevId(), sc
					.getCollectionName());
			sc.setCollectionId(id);
			logger.info("processing sc: id: " + sc.getCollectionId()
					+ " name: " + sc.getCollectionName());
			if (monitoredCollections.contains(sc)) {
				logger.info("******** PUSH " + sc.getCollectionId() + " name: "
						+ sc.getCollectionName() + " ********");
				ret.add(sc);
			} else {
				logger.info("** " + sc.getCollectionId()
						+ " modified but nobody cares **");
				for (SyncCollection mon : monitoredCollections) {
					logger.info("   * monitored: " + mon.getCollectionId());
				}
			}
		}

		for (SyncCollection toPush : ret) {
			String colName = toPush.getCollectionName();
			int collectionId = backend.getCollectionIdFor(session.getDevId(),
					colName);
			toPush.setCollectionId(collectionId);
		}

		return ret;
	}

	public void stopIdle() {
		if (store != null) {
			store.stopIdle();
			store.logout();
			store = null;
		}
		logger.info("Stop email push monitoring for collection[ "
				+ collectionName + "]");
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
				sc.setCollectionName(s);
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
			} else if (IdleTag.BYE.equals(line.getTag())) {
				try {
					logger.info("Disconnect from IMAP[Timeout]");
					startIdle();
				} catch (IMAPException e) {
					logger.error(e.getMessage(), e);
				}
			}
		}
	}

	private IdleClient getIdleClient(BackendSession bs) {
		if (imapHost == null) {
			locateImap(bs);
		}
		IdleClient idleCli = new IdleClient(imapHost, 143, bs
				.getLoginAtDomain(), bs.getPassword(), this);
		return idleCli;
	}

	private void locateImap(BackendSession bs) {
		imapHost = new LocatorClient().locateHost("mail/imap", bs
				.getLoginAtDomain());
		logger.info("Using " + imapHost + " as imap host.");
	}

}
