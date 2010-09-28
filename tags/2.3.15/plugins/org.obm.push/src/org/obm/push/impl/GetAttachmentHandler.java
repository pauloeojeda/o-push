package org.obm.push.impl;


import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.push.backend.BackendSession;
import org.obm.push.backend.IBackend;
import org.obm.push.backend.IContinuation;
import org.obm.push.backend.MSAttachementData;
import org.obm.push.exception.ObjectNotFoundException;

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
		
		MSAttachementData attachment;
		try {
			attachment = backend.getContentsExporter(bs).getEmailAttachement(bs, AttachmentName);
			responder.sendResponseFile(attachment.getContentType(), attachment.getFile());
		} catch (ObjectNotFoundException e) {
			responder.sendError(500);
		}
	}
}
