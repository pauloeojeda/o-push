package org.obm.push.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.push.backend.BackendSession;
import org.obm.push.backend.IBackend;
import org.obm.push.backend.IContinuation;
import org.w3c.dom.Document;

/**
 * Handles the Provision cmd
 * 
 * @author tom
 * 
 */
public class SettingsHandler extends WbxmlRequestHandler {

	private static final Log logger = LogFactory.getLog(SettingsHandler.class);

	public SettingsHandler(IBackend backend) {
		super(backend);
	}

	@Override
	public void process(IContinuation continuation, BackendSession bs,
			Document doc, ActiveSyncRequest request, Responder responder) {
		logger.info("process(" + bs.getLoginAtDomain() + "/" + bs.getDevType()
				+ ")");

		try {
			// send back the original document
			responder.sendResponse("Settings", doc);
		} catch (Exception e) {
			logger.error("Error creating provision response");
		}

	}

}
