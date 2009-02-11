package org.obm.push.wbxml.parsers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

import org.obm.push.wbxml.TagsTables;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

class EncoderHandler extends DefaultHandler {

	private WbxmlEncoder we;
	private ByteArrayOutputStream buf;
	private String defaultNamespace;
	private String currentXmlns;

	public EncoderHandler(WbxmlEncoder we, ByteArrayOutputStream buf,
			String defaultNamespace) throws IOException {
		this.defaultNamespace = defaultNamespace;
		this.we = we;
		this.buf = buf;
		switchToNs(defaultNamespace);
		currentXmlns = defaultNamespace;
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
				qName = qName.substring(qName.indexOf(":")+1);
			}

			if (!newNs.equals(currentXmlns)) {
				switchToNs(newNs);
			}
			currentXmlns = newNs;

//			buf.write(Wbxml.LITERAL_C);
//			we.writeStrT(qName);
			we.writeElement(qName);
		} catch (IOException e) {
			throw new SAXException(e);
		}
	}

	private void switchToNs(String newNs) throws IOException {
		// TODO Auto-generated method stub
		Map<String, Integer> table = TagsTables.getElementMappings(newNs);
		we.setStringTable(table);
		we.switchPage(TagsTables.NAMESPACES_IDS.get(newNs));
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