package org.obm.push.impl;


import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.push.backend.BackendSession;
import org.obm.push.backend.IBackend;
import org.obm.push.backend.IContinuation;
import org.obm.push.backend.MSAttachementData;

/**
 * Handles the search cmd
 * 
 * @author tom
 * 
 */
public class GetAttachmentHandler implements IRequestHandler {

	protected Log logger = LogFactory.getLog(getClass());
	protected IBackend backend;

	public GetAttachmentHandler(IBackend backend) {
		this.backend = backend;
	}

	@Override
	public void process(IContinuation continuation, BackendSession bs,
			ActiveSyncRequest request, Responder responder) throws IOException {
		logger.info("process(" + bs.getLoginAtDomain() + "/" + bs.getDevType()
				+ ")");

		String AttachmentName = request.getParameter("AttachmentName");
		
		MSAttachementData attachment = backend.getContentsExporter(bs).getEmailAttachement(bs, AttachmentName);
		if(attachment != null){
			responder.sendResponseFile(attachment.getContentType(), attachment.getFile());
		} else {
			responder.sendError(500);
		}
		
	}
}
