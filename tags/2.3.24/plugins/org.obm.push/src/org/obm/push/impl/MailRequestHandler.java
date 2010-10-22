package org.obm.push.impl;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.push.backend.BackendSession;
import org.obm.push.backend.IBackend;
import org.obm.push.backend.IContinuation;
import org.obm.push.utils.FileUtils;

public abstract class MailRequestHandler implements IRequestHandler {

	protected Log logger = LogFactory.getLog(getClass());
	protected IBackend backend;

	protected MailRequestHandler(IBackend backend) {
		this.backend = backend;
	}

	@Override
	public void process(IContinuation continuation, BackendSession bs,
			ActiveSyncRequest request, Responder responder) throws IOException {
		try {
			Boolean saveInSent = false;
			String sis = request.getParameter("SaveInSent");
			if (sis != null) {
				saveInSent = sis.equalsIgnoreCase("T");
			}
			InputStream in = request.getInputStream();
			byte[] mailContent = FileUtils.streamBytes(in, true);
			if (logger.isDebugEnabled()) {
				logger.debug("Mail content:\n" + new String(mailContent));
			}
			this.process(continuation, bs, mailContent, saveInSent, request,
					responder);
		} catch (Throwable e) {
			logger.error("Error while sending mail.", e);
		}
	}

	public abstract void process(IContinuation continuation, BackendSession bs,
			byte[] mailContent, Boolean saveInSent, ActiveSyncRequest request,
			Responder responder) throws IOException;

}
