package org.obm.push.impl;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

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
			byte[] mailContent, Boolean saveInSent, HttpServletRequest request,
			Responder responder) throws IOException {
		logger.info("process(" + bs.getLoginAtDomain() + "/" + bs.getDevType()
				+ ")");

		backend.getContentsImporter(null, bs).sendEmail(bs, mailContent, saveInSent);
	}

}
