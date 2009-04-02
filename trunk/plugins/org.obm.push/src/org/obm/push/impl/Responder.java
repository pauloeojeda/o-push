package org.obm.push.impl;

import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.push.utils.DOMUtils;
import org.obm.push.wbxml.WBXMLTools;
import org.w3c.dom.Document;

public class Responder {

	private HttpServletResponse resp;
	private static final Log logger = LogFactory.getLog(Responder.class);

	public Responder(HttpServletResponse resp) {
		this.resp = resp;
	}

	public void sendResponse(String defaultNamespace, Document doc)
			throws IOException {
		resp.setContentType("application/vnd.ms-sync.wbxml");
		logger.info("to pda:");
		try {
			DOMUtils.logDom(doc);
		} catch (TransformerException e) {
		}
		byte[] wbxml = WBXMLTools.toWbxml(defaultNamespace, doc);
		resp.setContentLength(wbxml.length);
		ServletOutputStream out = resp.getOutputStream();
		out.write(wbxml);
		out.flush();
		out.close();
	}

}
