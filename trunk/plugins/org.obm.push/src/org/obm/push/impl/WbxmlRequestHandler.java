package org.obm.push.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.transform.TransformerException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.push.backend.BackendSession;
import org.obm.push.backend.IBackend;
import org.obm.push.backend.IContinuation;
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
	public void process(IContinuation continuation, BackendSession bs,
			ActiveSyncRequest request, Responder responder) throws IOException {

		InputStream in = request.getInputStream();
		byte[] input = FileUtils.streamBytes(in, true);
		Document doc = null;

		if (input != null && input.length > 0) {
			try {
				doc = WBXMLTools.toXml(input);
			} catch (IOException e) {
				logger.error("Error parsing wbxml data.", e);
				return;
			}
		} else {
			logger.debug("empty wbxml command (valid for Ping & Sync)");
			// To reduce the amount of data sent in a Ping command request, the
			// server caches the heartbeat
			// interval and folder list. The client can omit the heartbeat
			// interval, the folder list, or both from
			// subsequent Ping requests if those parameters have not changed
			// from the previous Ping
			// request. If neither the heartbeat interval nor the folder list
			// has changed, the client can issue a
			// Ping request that does not contain an XML body. If the Ping
			// element is specified in an XML
			// request body, either the HeartbeatInterval element or the Folders
			// element or both MUST be
			// specified.
		}

		if (doc != null && logger.isInfoEnabled()) {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			try {
				DOMUtils.serialise(doc, out, true);
				logger.info("from pda:\n" + out.toString());
			} catch (TransformerException e) {
			}
		}

		process(continuation, bs, doc, request, responder);
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
	protected abstract void process(IContinuation continuation,
			BackendSession bs, Document doc, ActiveSyncRequest request,
			Responder responder);

}
