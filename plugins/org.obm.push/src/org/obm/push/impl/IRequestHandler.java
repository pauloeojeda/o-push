package org.obm.push.impl;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;

import org.obm.push.backend.BackendSession;
import org.obm.push.backend.IContinuation;

/**
 * Interface to handle client ActiveSync requests
 * 
 * @author tom
 * 
 */
public interface IRequestHandler {

	public void process(IContinuation continuation, BackendSession bs,
			HttpServletRequest request, Responder responder) throws IOException;

}
