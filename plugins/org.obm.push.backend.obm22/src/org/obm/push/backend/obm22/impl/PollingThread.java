package org.obm.push.backend.obm22.impl;

import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.push.backend.BackendSession;
import org.obm.push.backend.IContinuation;
import org.obm.push.backend.SyncCollection;
import org.obm.push.backend.obm22.OBMBackend;

/**
 * Check data sources for changes and notifies the backend if anything changed.
 * 
 * @author tom
 * 
 */
public class PollingThread implements Runnable {

	private BackendSession bs;

	private OBMBackend backend;

	private IContinuation continuation;

	private long msTimeout;

	private static final Log logger = LogFactory.getLog(PollingThread.class);

	public PollingThread(BackendSession bs, Set<SyncCollection> toMonitor,
			OBMBackend obmBackend, IContinuation c, long msTimeout) {
		this.bs = bs;
		this.backend = obmBackend;
		this.continuation = c;
		this.msTimeout = msTimeout;
	}

	@Override
	public void run() {
		logger.info("sleeping in polling thread");
		try {
			Thread.sleep(Math.max(5000, msTimeout - 5000));
		} catch (InterruptedException e) {
		}
		backend.onChangeFound(continuation, bs);
	}

}
