package org.obm.push.impl;

import org.mortbay.util.ajax.Continuation;
import org.obm.push.backend.BackendSession;
import org.obm.push.backend.IContinuation;

public class PushContinuation implements IContinuation {

	private Continuation c;

	public PushContinuation(Continuation c) {
		this.c = c;
	}

	@Override
	public BackendSession getObject() {
		return (BackendSession) c.getObject();
	}

	@Override
	public void resume() {
		c.resume();
	}

	@Override
	public void setObject(BackendSession bs) {
		c.setObject(bs);
	}

	@Override
	public void suspend(long msTimeout) {
		c.suspend(msTimeout);
	}
	
}
