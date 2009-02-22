package org.obm.sync.push.client;

import java.io.InputStream;

import org.obm.push.utils.DOMUtils;
import org.obm.push.utils.FileUtils;
import org.obm.push.wbxml.WBXMLTools;
import org.w3c.dom.Document;

public class TestWbxmlEncode extends AbstractPushTest {

	public void testEncode() throws Exception {
		InputStream in = loadDataFile("FolderSyncRequest.xml");
		Document doc = DOMUtils.parse(in);
		byte[] data = WBXMLTools.toWbxml("FolderHierarchy", doc);
		assertNotNull(data);
	}

	public void testDecode() throws Exception {
		InputStream in = loadDataFile("foldersync_wm61.wbxml");
		byte[] data = FileUtils.streamBytes(in, true);
		Document doc = WBXMLTools.toXml(data);
		DOMUtils.logDom(doc);
	}
}
