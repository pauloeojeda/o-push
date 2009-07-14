package org.obm.push.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mortbay.util.ajax.Continuation;
import org.obm.push.backend.BackendSession;
import org.obm.push.backend.IBackend;
import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Handles the Provision cmd
 * 
 * @author tom
 * 
 */
public class SearchHandler implements IRequestHandler {

	private static final Log logger = LogFactory.getLog(SearchHandler.class);

	public SearchHandler(IBackend backend) {
	}

	@Override
	public void process(Continuation continuation, BackendSession bs,
			Document doc, Responder responder) {
		logger.info("process(" + bs.getLoginAtDomain() + "/" + bs.getDevType()
				+ ")");
		
		

		try {
			Document search = DOMUtils.createDoc(null, "Search");
			Element r = search.getDocumentElement();
			DOMUtils.createElementAndText(r, "Status", "1");
			Element resp = DOMUtils.createElement(r, "Response");
			Element store = DOMUtils.createElement(resp, "Store");
			DOMUtils.createElementAndText(store, "Status", "1");

			// TODO results go here
			//DOMUtils.createElementAndText(store, "Range", "0-0");
			
			DOMUtils.createElementAndText(store, "Total", "0");
			
			
			responder.sendResponse("Search", search);

		} catch (Exception e) {
			logger.error("Error creating search response");
		}

	}

}
