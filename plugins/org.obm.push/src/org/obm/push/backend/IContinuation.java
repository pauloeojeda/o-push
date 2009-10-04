package org.obm.push.backend;

public interface IContinuation {

	void setObject(BackendSession bs);

	BackendSession getObject();

	void suspend(long msTimeout);

	void resume();
}
