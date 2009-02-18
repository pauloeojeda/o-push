package org.obm.push.wbxml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.push.utils.DOMUtils;
import org.obm.push.wbxml.parsers.WbxmlEncoder;
import org.obm.push.wbxml.parsers.WbxmlParser;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


/**
 * Wbxml convertion tools
 * 
 * @author tom
 * 
 */
public class WBXMLTools {

	private static final Log logger = LogFactory.getLog(WBXMLTools.class);

	/**
	 * Transforms a wbxml byte array into the corresponding DOM representation
	 * 
	 * @param wbxml
	 * @return
	 * @throws IOException
	 */
	public static Document toXml(byte[] wbxml) throws IOException {

		storeWbxml(wbxml);

		WbxmlParser parser = new WbxmlParser();
		parser.setTagTable(0x0, TagsTables.CP_0); // AirSync
		parser.setTagTable(0x6, TagsTables.CP_6); // ItemEstimate
		parser.setTagTable(0x7, TagsTables.CP_7); // FolderHierarchy
		parser.switchPage(0);
		PushDocumentHandler pdh = new PushDocumentHandler();
		parser.setDocumentHandler(pdh);
		try {
			parser.parse(new ByteArrayInputStream(wbxml));
			return pdh.getDocument();
		} catch (SAXException e) {
			logger.error(e.getMessage(), e);
			throw new IOException(e.getMessage());
		}

	}

	private static void storeWbxml(byte[] wbxml) {
		try {
			File tmp = File.createTempFile("debug_", ".wbxml");
			FileOutputStream fout = new FileOutputStream(tmp);
			fout.write(wbxml);
			fout.close();
			logger.info("received wbxml logged to " + tmp.getAbsolutePath());
		} catch (Throwable t) {
			t.printStackTrace();
		}

	}

	public static byte[] toWbxml(String defaultNamespace, Document doc)
			throws IOException {
		WbxmlEncoder encoder = new WbxmlEncoder(defaultNamespace);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			DOMUtils.serialise(doc, out);
			InputSource is = new InputSource(new ByteArrayInputStream(out
					.toByteArray()));
			out = new ByteArrayOutputStream();
			encoder.convert(is, out);
			byte[] ret = out.toByteArray();
			storeWbxml(ret);

			// logger.info("reconverted version");
			// DOMUtils.logDom(toXml(ret));

			return ret;
		} catch (Exception e) {
			throw new IOException(e);
		}

	}

}
