package org.obm.push.impl;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.transform.TransformerException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mortbay.util.ajax.Continuation;
import org.obm.push.backend.BackendSession;
import org.obm.push.backend.IBackend;
import org.obm.push.utils.DOMUtils;
import org.obm.push.utils.FileUtils;
import org.obm.push.wbxml.WBXMLTools;
import org.w3c.dom.Document;

public abstract class WbxmlRequestHandler implements IRequestHandler {

	protected Log logger = LogFactory.getLog(getClass());
	protected IBackend backend;
	
	protected WbxmlRequestHandler(IBackend backend) {
		this.backend = backend;
	}

	@Override
	public void process(Continuation continuation, BackendSession bs,
			InputStream in, Responder responder) throws IOException {
		byte[] input = FileUtils.streamBytes(in, true);
		Document doc = null;
		try {
			doc = WBXMLTools.toXml(input);
		} catch (IOException e) {
			logger.error("Error parsing wbxml data.", e);
			return;
		}

		logger.info("from pda:");
		try {
			DOMUtils.logDom(doc);
		} catch (TransformerException e) {
		}

		process(continuation, bs, doc, responder);
	}

	protected abstract void process(Continuation continuation, BackendSession bs,
			Document doc, Responder responder);

}
