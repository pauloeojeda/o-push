package org.obm.push.wbxml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import de.trantor.wap.WbxmlEncoder;
import de.trantor.wap.WbxmlParser;

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
		WbxmlParser parser = new WbxmlParser();
		parser.setTagTable(0, TagsTables.CP_0);
		parser.setTagTable(1, TagsTables.CP_1);
		parser.setTagTable(2, TagsTables.CP_2);
		parser.setTagTable(3, TagsTables.CP_3);
		parser.setTagTable(4, TagsTables.CP_4);
		parser.setTagTable(5, TagsTables.CP_5);
		parser.setTagTable(6, TagsTables.CP_6);
		parser.setTagTable(7, TagsTables.CP_7);
		parser.setTagTable(8, TagsTables.CP_8);
		parser.setTagTable(9, TagsTables.CP_9);
		parser.setTagTable(10, TagsTables.CP_10);
		parser.setTagTable(11, TagsTables.CP_11);
		parser.setTagTable(12, TagsTables.CP_12);
		parser.setTagTable(13, TagsTables.CP_13);
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

	public static byte[] toWbxml(Document doc) throws IOException {
		WbxmlEncoder encoder = new WbxmlEncoder();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			DOMUtils.serialise(doc, out);
			InputSource is = new InputSource(new ByteArrayInputStream(out.toByteArray()));
			out = new ByteArrayOutputStream();
			encoder.convert(is, out);
			byte[] ret = out.toByteArray();
			logger.info("return wbxml document with "+ret.length+" bytes.");
			return ret;
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

}
