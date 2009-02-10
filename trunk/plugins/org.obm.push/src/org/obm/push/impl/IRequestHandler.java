package org.obm.push.impl;

import javax.servlet.http.HttpServletResponse;

import org.w3c.dom.Document;

public interface IRequestHandler {

	public void process(ASParams p, Document doc, HttpServletResponse response);

}
