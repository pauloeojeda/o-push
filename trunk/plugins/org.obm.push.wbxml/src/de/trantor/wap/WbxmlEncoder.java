package de.trantor.wap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Hashtable;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * a class for converting ("binary encoding") XML to WBXML. Todo:
 * <ul>
 * <li>Add support for processing instructions
 * <li>Add support for tag and attribute tables
 * <li>Add support for WBXML extensions
 * </ul>
 */

public class WbxmlEncoder {

	private SAXParser parser;
	private Hashtable<String, Integer> stringTable;
	private ByteArrayOutputStream buf;
	private String defaultNamespace;

	/**
	 * The constructor creates an internal document handler. The given parser is
	 * used
	 */

	public WbxmlEncoder(String defaultNamespace) {
		this.defaultNamespace = defaultNamespace;
		try {
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

		buf = new ByteArrayOutputStream();

		// perform conv.
		parser.parse(in, new EncoderHandler(this, buf, defaultNamespace));

		// ok, write header

		out.write(0x03); // version
		out.write(0x01); // unknown or missing public identifier
		out.write(0x6a); // iso-8859-1
		out.write(0x00); // no string table

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
			throw new IOException("unknown elem in mapping table: " + s);
		}

		writeInt(buf, idx.intValue());
	}
}
