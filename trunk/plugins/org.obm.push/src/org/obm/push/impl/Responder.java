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

	public void sendResponse(Document doc) throws IOException {
		logger.info("to pda:");
		try {
			DOMUtils.logDom(doc);
		} catch (TransformerException e) {
		}
		byte[] wbxml = WBXMLTools.toWbxml(doc);
		ServletOutputStream out = resp.getOutputStream();
		out.write(wbxml);
		out.flush();
		out.close();
	}

}
