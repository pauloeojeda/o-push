package org.obm.push.impl;

import java.io.IOException;
import java.io.InputStream;

import org.mortbay.util.ajax.Continuation;
import org.obm.push.backend.BackendSession;

/**
 * Interface to handle client ActiveSync requests
 * 
 * @author tom
 * 
 */
public interface IRequestHandler {

	public void process(Continuation continuation, BackendSession bs,
			InputStream in, Responder responder) throws IOException;

}
