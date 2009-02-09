package org.obm.push.wbxml;

import java.util.Stack;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.AttributeList;
import org.xml.sax.DocumentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

@SuppressWarnings("deprecation")
public class PushDocumentHandler implements DocumentHandler {

	private static final Log logger = LogFactory
			.getLog(PushDocumentHandler.class);

	private Document doc;
	private Stack<Element> elems;

	public PushDocumentHandler() {
		try {
			DocumentBuilder db = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();
			doc = db.newDocument();
			elems = new Stack<Element>();
		} catch (ParserConfigurationException e) {
			logger.error(e.getMessage(), e);
		}
	}

	@Override
	public void characters(char[] data, int off, int count) throws SAXException {
		Element cur = elems.peek();
		cur.setTextContent(new String(data, off, count));
	}

	@Override
	public void endDocument() throws SAXException {
		System.out.println("from pda: ");
		try {
			DOMUtils.logDom(doc);
		} catch (TransformerException e) {
			logger.error(e.getMessage(), e);
		}
	}

	@Override
	public void endElement(String arg0) throws SAXException {
		elems.pop();
	}

	@Override
	public void ignorableWhitespace(char[] arg0, int arg1, int arg2)
			throws SAXException {
	}

	@Override
	public void processingInstruction(String arg0, String arg1)
			throws SAXException {
	}

	@Override
	public void setDocumentLocator(Locator arg0) {
	}

	@Override
	public void startDocument() throws SAXException {
	}

	@Override
	public void startElement(String arg0, AttributeList arg1)
			throws SAXException {
		Element newE = doc.createElement(arg0);

		Element parent = null;
		if (!elems.isEmpty()) {
			parent = elems.peek();
		}

		if (parent != null) {
			parent.appendChild(newE);
		} else {
			doc.appendChild(newE);
		}
		elems.add(newE);

		for (int i = 0; i < arg1.getLength(); i++) {
			String att = arg1.getName(i);
			String val = arg1.getValue(i);
			newE.setAttribute(att, val);
		}
	}

}
