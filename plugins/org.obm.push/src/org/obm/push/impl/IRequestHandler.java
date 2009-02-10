package org.obm.push.impl;

import org.w3c.dom.Document;

public interface IRequestHandler {

	public void process(ASParams p, Document doc, Responder responder);

}
