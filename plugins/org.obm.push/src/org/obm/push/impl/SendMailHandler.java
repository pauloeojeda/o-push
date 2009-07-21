package org.obm.push.impl;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mortbay.util.ajax.Continuation;
import org.obm.push.backend.BackendSession;
import org.obm.push.backend.IBackend;
import org.obm.push.utils.FileUtils;

/**
 * Handles the SendMail cmd
 * 
 * @author tom
 * 
 */
public class SendMailHandler implements IRequestHandler {

	private IBackend backend;
	private static final Log logger = LogFactory.getLog(SendMailHandler.class);

	public SendMailHandler(IBackend backend) {
		this.backend = backend;
	}

	@Override
	public void process(Continuation continuation, BackendSession bs,
			InputStream in, Responder responder) throws IOException {
		logger.info("process(" + bs.getLoginAtDomain() + "/" + bs.getDevType()
				+ ")");

		byte[] mailContent = FileUtils.streamBytes(in, true);

		logger.info("Mail content:\n" + new String(mailContent));

		logger.info("not implemented for backend "+backend);
		
		backend.sendMail(bs, mailContent);
	}

}
