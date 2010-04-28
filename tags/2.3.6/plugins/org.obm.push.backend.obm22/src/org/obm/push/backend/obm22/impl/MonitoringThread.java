package org.obm.push.backend.obm22.impl;

import java.sql.Connection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.minig.obm.pool.OBMPoolActivator;
import org.obm.push.backend.BackendSession;
import org.obm.push.backend.ICollectionChangeListener;
import org.obm.push.backend.SyncCollection;
import org.obm.push.exception.ActiveSyncException;

public abstract class MonitoringThread implements Runnable {

	/**
	 * SynchronizedSet, all accesses should be synchronized
	 */
	private Set<ICollectionChangeListener> ccls;
	private boolean stopped;

	protected Log logger = LogFactory.getLog(getClass());
	private long freqMillisec;
	protected ObmSyncBackend backend;

	protected MonitoringThread(ObmSyncBackend backend, long freqMillisec,
			Set<ICollectionChangeListener> ccls) {
		this.freqMillisec = freqMillisec;
		this.stopped = false;
		this.ccls = ccls;
		this.backend = backend;
	}

	@Override
	public void run() {
		Date lastSync = new Date();
		ChangedCollections cols = getChangedCollections(lastSync);
		lastSync = cols.getLastSync();
		logger.info("DB lastSync: " + lastSync);
		while (!stopped) {
			try {
				Thread.sleep(freqMillisec);
			} catch (InterruptedException e) {
				stopped = true;
				continue;
			}
			synchronized (ccls) {
				if (ccls.isEmpty()) {
					continue;
				}
			}
			cols = getChangedCollections(lastSync);
			lastSync = cols.getLastSync();
			LinkedList<PushNotification> toNotify = new LinkedList<PushNotification>();
			synchronized (ccls) {
				for (ICollectionChangeListener ccl : ccls) {
					Set<SyncCollection> monitoredCollections = ccl
							.getMonitoredCollections();
					Set<SyncCollection> changes = getChangedCollections(ccl
							.getSession(), cols, monitoredCollections);
					if (!changes.isEmpty()) {
						toNotify.add(new PushNotification(changes, ccl));
					}
				}
			}
			for (PushNotification pn : toNotify) {
				pn.emit();
			}
		}
	}

	private Set<SyncCollection> getChangedCollections(BackendSession session,
			ChangedCollections cols, Set<SyncCollection> monitoredCollections) {
		Set<SyncCollection> ret = new HashSet<SyncCollection>();

		for (SyncCollection sc : cols.getChanged()) {
			int id;
			try {
				id = backend.getCollectionIdFor(session.getDevId(), sc
						.getCollectionPath());
				sc.setCollectionId(id);
				logger.info("processing sc: id: " + sc.getCollectionId()
						+ " name: " + sc.getCollectionPath());
				if (monitoredCollections.contains(sc)) {
					logger.info("******** PUSH " + sc.getCollectionId() + " name: "
							+ sc.getCollectionPath() + " ********");
					ret.add(sc);
				} else {
					logger.info("** " + sc.getCollectionId()
							+ " modified but nobody cares **");
					for (SyncCollection mon : monitoredCollections) {
						logger.info("   * monitored: " + mon.getCollectionId());
					}
				}
			} catch (ActiveSyncException e) {
			}
			
		}

		for (SyncCollection toPush : ret) {
			try {
				String colName = toPush.getCollectionPath();
				int collectionId = backend.getCollectionIdFor(session.getDevId(),
						colName);
				toPush.setCollectionId(collectionId);
			} catch (ActiveSyncException e) {
			}
			
		}

		return ret;
	}

	protected abstract ChangedCollections getChangedCollections(Date lastSync);

	protected Connection newCon() {
		return OBMPoolActivator.getDefault().getConnection();
	}

	public void stop() {
		this.stopped = true;
	}

}
