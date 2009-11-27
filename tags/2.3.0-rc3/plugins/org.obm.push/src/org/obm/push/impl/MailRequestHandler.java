package org.obm.push.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

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

	@SuppressWarnings("unchecked")
	@Override
	public void process(IContinuation continuation, BackendSession bs,
			HttpServletRequest request, Responder responder) throws IOException {

		Enumeration params = request.getParameterNames();
		while (params.hasMoreElements()) {
			String p = (String) params.nextElement();
			logger
					.info("Parameter[" + p + ": " + request.getParameter(p)
							+ "]");
		}
		Boolean saveInSent = false;
		String sis = request.getParameter("SaveInSent");
		if (sis != null) {
			saveInSent = request.getParameter("SaveInSent").equalsIgnoreCase(
					"T");
		}

		InputStream in = request.getInputStream();
		byte[] mailContent = FileUtils.streamBytes(in, true);
		logger.info("Mail content:\n" + new String(mailContent));
		this.process(continuation, bs, mailContent, saveInSent, request,
				responder);
	}

	public abstract void process(IContinuation continuation, BackendSession bs,
			byte[] mailContent, Boolean saveInSent, HttpServletRequest request,
			Responder responder) throws IOException;

}
