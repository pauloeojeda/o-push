package org.obm.push.impl;

import org.obm.push.backend.BackendSession;
import org.obm.push.backend.IBackend;
import org.obm.push.backend.IContinuation;

/**
 * Handles the SendMail cmd
 * 
 * @author adrien
 * 
 */
public class SendMailHandler extends MailRequestHandler {

	public SendMailHandler(IBackend backend) {
		super(backend);
	}

	@Override
	public void process(IContinuation continuation, BackendSession bs,
			byte[] mailContent, Boolean saveInSent, ActiveSyncRequest request,
			Responder responder) {
		logger.info("process(" + bs.getLoginAtDomain() + "/" + bs.getDevType()
				+ ")");
		try{
		backend.getContentsImporter(null, bs).sendEmail(bs, mailContent, saveInSent);
		} catch (Throwable e) {
			logger.info(e.getMessage(), e);
		}
	}

}
