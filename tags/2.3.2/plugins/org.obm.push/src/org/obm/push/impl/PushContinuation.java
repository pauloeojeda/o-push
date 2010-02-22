package org.obm.push.impl;

import javax.servlet.http.HttpServletRequest;

import org.mortbay.util.ajax.Continuation;
import org.obm.push.backend.BackendSession;
import org.obm.push.backend.IContinuation;

public class PushContinuation implements IContinuation {

	private Continuation c;
	private HttpServletRequest req;

	public PushContinuation(Continuation c, HttpServletRequest req) {
		this.c = c;
		this.req = req;
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

	@Override
	public void storeData(String regName, Object reg) {
		req.setAttribute(regName, reg);
	}
	
}
