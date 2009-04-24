package org.obm.push.backend.obm22.impl;

import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mortbay.util.ajax.Continuation;
import org.obm.push.backend.BackendSession;
import org.obm.push.backend.SyncCollection;
import org.obm.push.backend.obm22.OBMBackend;

public class PollingThread implements Runnable {

	private BackendSession bs;

	private OBMBackend backend;

	private Continuation continuation;

	private long msTimeout;

	private static final Log logger = LogFactory.getLog(PollingThread.class);

	public PollingThread(BackendSession bs, Set<SyncCollection> toMonitor,
			OBMBackend obmBackend, Continuation c, long msTimeout) {
		this.bs = bs;
		this.backend = obmBackend;
		this.continuation = c;
		this.msTimeout = msTimeout;
	}

	@Override
	public void run() {
		logger.info("sleeping in polling thread");
		try {
			Thread.sleep(msTimeout);
		} catch (InterruptedException e) {
		}
		backend.onChangeFound(continuation, bs);
	}

}
