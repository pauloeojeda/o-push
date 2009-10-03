package org.obm.push.impl;

import java.io.ByteArrayOutputStream;
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

/**
 * Abstract class for handling client requests with a Content-Type set to
 * <code>application/vnd.ms-sync.wbxml</code>
 * 
 * @author tom
 * 
 */
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

		if (logger.isInfoEnabled()) {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			try {
				DOMUtils.serialise(doc, out, true);
				logger.info("from pda:\n" + out.toString());
			} catch (TransformerException e) {
			}
		}

		process(continuation, bs, doc, responder);
	}

	/**
	 * Handles the client request. The wbxml was already decoded and is
	 * available in the doc parameter.
	 * 
	 * @param continuation
	 * @param bs
	 * @param doc
	 *            the decoded wbxml document.
	 * @param responder
	 */
	protected abstract void process(Continuation continuation,
			BackendSession bs, Document doc, Responder responder);

}
