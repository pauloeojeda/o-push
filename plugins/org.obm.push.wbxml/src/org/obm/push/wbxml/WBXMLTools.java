package org.obm.push.wbxml;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.xml.sax.SAXException;

import de.trantor.wap.WbxmlParser;

public class WBXMLTools {

	public static void toXml(byte[] wbxml) throws IOException {
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
		parser.setDocumentHandler(new PushDocumentHandler());
		try {
			parser.parse(new ByteArrayInputStream(wbxml));
		} catch (SAXException e) {
			e.printStackTrace();
			throw new IOException(e.getMessage());
		}
	
	}


}
