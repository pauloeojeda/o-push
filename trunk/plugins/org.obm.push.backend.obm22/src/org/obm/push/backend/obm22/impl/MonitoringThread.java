package org.obm.push.backend.obm22.impl;

import java.sql.Connection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.minig.obm.pool.OBMPoolActivator;
import org.obm.push.backend.ICollectionChangeListener;
import org.obm.push.backend.SyncCollection;

public abstract class MonitoringThread implements Runnable {

	/**
	 * SynchronizedSet, all accesses should be synchronized
	 */
	private Set<ICollectionChangeListener> ccls;
	private boolean stopped;

	protected Log logger = LogFactory.getLog(getClass());
	private long freqMillisec;

	protected MonitoringThread(long freqMillisec, Set<ICollectionChangeListener> ccls) {
		this.freqMillisec = freqMillisec;
		this.stopped = false;
		this.ccls = ccls;
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
					Set<SyncCollection> changes = getMonitoredCollections(cols,
							monitoredCollections);
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

	private Set<SyncCollection> getMonitoredCollections(
			ChangedCollections cols, Set<SyncCollection> monitoredCollections) {
		Set<SyncCollection> ret = new HashSet<SyncCollection>();
		// TODO Auto-generated method stub

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
