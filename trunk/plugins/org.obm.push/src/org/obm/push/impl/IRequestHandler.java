package org.obm.push.impl;

import org.mortbay.util.ajax.Continuation;
import org.obm.push.backend.BackendSession;
import org.w3c.dom.Document;

public interface IRequestHandler {

	public void process(Continuation continuation, BackendSession bs, Document doc, Responder responder);

}
