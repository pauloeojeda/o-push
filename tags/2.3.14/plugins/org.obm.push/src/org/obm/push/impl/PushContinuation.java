package org.obm.push.impl;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mortbay.util.ajax.Continuation;
import org.mortbay.util.ajax.ContinuationSupport;
import org.obm.push.ActiveSyncServlet;
import org.obm.push.backend.BackendSession;
import org.obm.push.backend.CollectionChangeListener;
import org.obm.push.backend.IContinuation;
import org.obm.push.backend.IListenerRegistration;

public class PushContinuation implements IContinuation {

	private static int id = 0;
	@SuppressWarnings("unused")
	private static final Log logger = LogFactory
			.getLog(ActiveSyncServlet.class);

	private final static String KEY_BACKEND_SESSION = "key_backend_session";
	private final static String KEY_IS_ERROR = "key_is_error";
	private final static String KEY_STATUS_ERROR = "key_stauts_error";
	private final static String KEY_COLLECTION_CHANGE_LISTENER = "key_collection_change_listener";
	private final static String KEY_LISTENER_REGISTRATION = "key_listener_registration";
	private final static String KEY_ID_REQUEST = "key_id_request";
	
	private Continuation c;
	private Map<String, Object> params;

	@SuppressWarnings("unchecked")
	public PushContinuation(HttpServletRequest req) {
		this.c = ContinuationSupport.getContinuation(req, req);
		Object o = c.getObject();
		if (o != null && o instanceof Map && (c.isPending() || c.isResumed())) {
			params = (Map<String, Object>) o;
		} else {
			this.params = new HashMap<String, Object>();
			params.put(KEY_ID_REQUEST, id++);
			c.setObject(params);
		}
	}

	public int getReqId() {
		return (Integer)params.get(KEY_ID_REQUEST);
	}

	@Override
	public void resume() {
		c.resume();
	}

	@Override
	public void suspend(long msTimeout) {
		c.suspend(msTimeout);
	}

	@Override
	public void error(String status) {
		params.put(KEY_IS_ERROR, true);
		params.put(KEY_STATUS_ERROR, status);
	}

	@Override
	public BackendSession getBackendSession() {
		return (BackendSession) params.get(KEY_BACKEND_SESSION);
	}

	@Override
	public void setBackendSession(BackendSession bs) {
		params.put(KEY_BACKEND_SESSION, bs);
	}

	@Override
	public Boolean isError() {
		Object err = params.get(KEY_IS_ERROR);
		return err != null ? (Boolean) err : false;
	}

	@Override
	public String getErrorStatus() {
		return (String) params.get(KEY_STATUS_ERROR);
	}

	@Override
	public IListenerRegistration getListenerRegistration() {
		return (IListenerRegistration) params.get(KEY_LISTENER_REGISTRATION);
	}

	@Override
	public void setListenerRegistration(IListenerRegistration reg) {
		params.put(KEY_LISTENER_REGISTRATION, reg);
	}

	@Override
	public CollectionChangeListener getCollectionChangeListener() {
		return (CollectionChangeListener) params
				.get(KEY_COLLECTION_CHANGE_LISTENER);
	}

	@Override
	public void setCollectionChangeListener(CollectionChangeListener l) {
		params.put(KEY_COLLECTION_CHANGE_LISTENER, l);
	}

	@Override
	public Boolean isPending() {
		return c.isPending();
	}

	@Override
	public Boolean isResumed() {
		return c.isResumed();
	}

}
