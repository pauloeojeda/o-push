package org.obm.push.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.push.backend.BackendSession;
import org.obm.push.backend.IBackend;
import org.obm.push.provisioning.Policy;
import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Handles the Provision cmd
 * 
 * @author tom
 * 
 */
public class PingHandler implements IRequestHandler {

	private static final Log logger = LogFactory.getLog(PingHandler.class);

	private IBackend backend;

	public PingHandler(IBackend backend) {
		this.backend = backend;
	}

	@Override
	public void process(BackendSession bs, Document doc, Responder responder) {
		logger.info("process(" + bs.getLoginAtDomain() + "/" + bs.getDevType() + ")");

		Element pr = doc.getDocumentElement();
//		int folderCount = Integer.parseInt(DOMUtils.getElementText(pr, "Folders"));
//		NodeList folders = pr.getElementsByTagName("Folder");
		
		
		try {
			Document ret = DOMUtils.createDoc(null, "Ping");

			serializePolicy(pr, backend.getDevicePolicy(bs));
			responder.sendResponse("Provision", ret);

		} catch (Exception e) {
			logger.error("Error creating provision response");
		}

	}

	private void serializePolicy(Element provDoc, Policy p) {
		// TODO Auto-generated method stub

	}
}
