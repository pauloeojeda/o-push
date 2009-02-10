package de.trantor.wap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

class EncoderHandler extends DefaultHandler {

	private WbxmlEncoder we;
	private ByteArrayOutputStream buf;
	private String defaultNamespace;
	private String currentXmlns;

	public EncoderHandler(WbxmlEncoder we, ByteArrayOutputStream buf, String defaultNamespace) {
		this.defaultNamespace = defaultNamespace;
		this.we = we;
		this.buf = buf;
	}

	public void startElement(String uri, String localName, String qName,
			Attributes attr) throws SAXException {

		try {
			// TODO handle namespace here !!!
			String newNs = null;
			if (!qName.contains(":")) {
				newNs = defaultNamespace;
			} else {
				newNs = qName.substring(0, qName.indexOf(":"));
			}
			
			if (!newNs.equals(currentXmlns)) {
				switchToNs(newNs);
			}
			currentXmlns = newNs;
			
			buf.write(Wbxml.LITERAL_C); 
			we.writeStrT(qName);
		} catch (IOException e) {
			throw new SAXException(e);
		}
	}

	private void switchToNs(String newNs) {
		// TODO Auto-generated method stub
		
	}

	public void characters(char[] chars, int start, int len)
			throws SAXException {
		try {
			buf.write(Wbxml.STR_I);
			we.writeStrI(buf, new String(chars, start, len));
		} catch (IOException e) {
			throw new SAXException(e);
		}
	}

	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		buf.write(Wbxml.END);
	}

}