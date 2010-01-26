package org.obm.push.impl;

import java.io.IOException;

import org.obm.push.backend.BackendSession;
import org.obm.push.backend.IBackend;
import org.obm.push.backend.IContinuation;

/**
 * Handles the SmartReply cmd
 * 
 * @author adrien
 * 
 */
public class SmartReplyHandler extends MailRequestHandler {

	public SmartReplyHandler(IBackend backend) {
		super(backend);
	}

	@Override
	public void process(IContinuation continuation, BackendSession bs,
			byte[] mailContent, Boolean saveInSent, ActiveSyncRequest request,
			Responder responder) throws IOException {
		logger.info("process(" + bs.getLoginAtDomain() + "/" + bs.getDevType()
				+ ")");

		String collectionId = request.getParameter("CollectionId");
		String serverId = request.getParameter("ItemId");

		backend.getContentsImporter(null, bs).replyEmail(bs, mailContent,
				saveInSent, collectionId, serverId);
	}
}
