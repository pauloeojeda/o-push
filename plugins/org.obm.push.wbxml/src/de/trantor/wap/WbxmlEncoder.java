package de.trantor.wap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Hashtable;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * a class for converting ("binary encoding") XML to WBXML. Todo:
 * <ul>
 * <li>Add support for processing instructions
 * <li>Add support for tag and attribute tables
 * <li>Add support for WBXML extensions
 * </ul>
 */

public class WbxmlEncoder {

	class Handler extends DefaultHandler {
		public void startElement(String uri, String localName, String qName,
				Attributes attr) throws SAXException {

			try {
				boolean hasAttr = attr.getLength() != 0;

				buf.write(hasAttr ? Wbxml.LITERAL_AC : Wbxml.LITERAL_C); // attr
				// ?
				// content
				// ?

				writeStrT(qName);

				for (int i = 0; i < attr.getLength(); i++) {
					buf.write(Wbxml.LITERAL);
					writeStrT(attr.getQName(i));
					buf.write(Wbxml.STR_I);
					writeStrI(buf, attr.getValue(i));
				}

				if (hasAttr)
					buf.write(Wbxml.END);
			} catch (IOException e) {
				throw new SAXException(e);
			}
		}

		public void characters(char[] chars, int start, int len)
				throws SAXException {
			try {
				buf.write(Wbxml.STR_I);
				writeStrI(buf, new String(chars, start, len));
			} catch (IOException e) {
				throw new SAXException(e);
			}
		}

		public void endElement(String uri, String localName, String qName)
				throws SAXException {
			buf.write(Wbxml.END);
		}

	}

	OutputStream out;
	InputSource in;

	SAXParser parser;

	Hashtable<String, Integer> stringTable;

	ByteArrayOutputStream buf;
	ByteArrayOutputStream stringTableBuf;

	/**
	 * The constructor creates an internal document handler. The given parser is
	 * used
	 */

	public WbxmlEncoder() {
		try {
			System.setProperty("org.xml.sax.parser", "");
			parser = SAXParserFactory.newInstance().newSAXParser();

		} catch (Exception e) {
			throw new RuntimeException(e.toString());
		}
	}

	/**
	 * converts the XML data from the given SAX InputSource and writes the
	 * result to the given OutputStream
	 */

	public void convert(InputSource in, OutputStream out) throws SAXException,
			IOException {

		this.out = out;
		this.in = in;

		buf = new ByteArrayOutputStream();
		stringTable = new Hashtable<String, Integer>();
		stringTableBuf = new ByteArrayOutputStream();

		// perform conv.
		parser.parse(in, new Handler());

		// ok, write header

		out.write(0x01); // version
		out.write(0x01); // unknown or missing public identifier
		out.write(0x04); // iso-8859-1

		writeInt(out, stringTableBuf.size());

		// write StringTable

		stringTableBuf.writeTo(out);

		// write buf

		buf.writeTo(out);

		// ready!

		out.flush();
	}

	// internal methods

	void writeInt(OutputStream out, int i) throws IOException {
		byte[] buf = new byte[5];
		int idx = 0;

		do {
			buf[idx++] = (byte) (i & 0x7f);
			i = i >> 7;
		} while (i != 0);

		while (idx > 1) {
			out.write(buf[--idx] | 0x80);
		}
		out.write(buf[0]);
	}

	void writeStrI(OutputStream out, String s) throws IOException {
		for (int i = 0; i < s.length(); i++) {
			out.write((byte) s.charAt(i));
		}
		out.write(0);
	}

	void writeStrT(String s) throws IOException {

		Integer idx = stringTable.get(s);

		if (idx == null) {
			idx = new Integer(stringTableBuf.size());
			stringTable.put(s, idx);
			writeStrI(stringTableBuf, s);
			stringTableBuf.flush();
		}

		writeInt(buf, idx.intValue());
	}
}
