package org.obm.push.impl;

import org.obm.push.backend.BackendSession;
import org.w3c.dom.Document;

public interface IRequestHandler {

	public void process(BackendSession bs, Document doc, Responder responder);

}
