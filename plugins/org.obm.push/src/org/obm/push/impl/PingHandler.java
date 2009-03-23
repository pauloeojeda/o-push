package org.obm.push.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.push.backend.BackendSession;
import org.obm.push.backend.IBackend;
import org.obm.push.backend.ItemChange;
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
		logger.info("process(" + bs.getLoginAtDomain() + "/" + bs.getDevType()
				+ ")");

		Element pr = doc.getDocumentElement();

		try {
			Document ret = DOMUtils.createDoc(null, "Ping");

			List<ItemChange> changes = backend.waitForChanges(bs);
			fillResponse(pr, changes);
			responder.sendResponse("Provision", ret);

		} catch (Exception e) {
			logger.error("Error creating provision response");
		}

	}

	private void fillResponse(Element pr, List<ItemChange> changes) {
		// TODO Auto-generated method stub
		
	}

}
