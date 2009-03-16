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

	public void testDecodeSync() throws Exception {
		InputStream in = loadDataFile("contact_sync_wm61.wbxml");
		byte[] data = FileUtils.streamBytes(in, true);
		Document doc = WBXMLTools.toXml(data);
		DOMUtils.logDom(doc);
	}

	public void testDecodeIPhoneSync1() throws Exception {
		InputStream in = loadDataFile("iphone_sync1.wbxml");
		byte[] data = FileUtils.streamBytes(in, true);
		Document doc = WBXMLTools.toXml(data);
		DOMUtils.logDom(doc);
	}

	public void testDecodeIPhoneSync2() throws Exception {
		InputStream in = loadDataFile("iphone_sync2.wbxml");
		byte[] data = FileUtils.streamBytes(in, true);
		Document doc = WBXMLTools.toXml(data);
		DOMUtils.logDom(doc);
	}

	public void testDecodeIPhoneSync3() throws Exception {
		InputStream in = loadDataFile("iphone_sync3.wbxml");
		byte[] data = FileUtils.streamBytes(in, true);
		Document doc = WBXMLTools.toXml(data);
		DOMUtils.logDom(doc);
	}

	public void testDecodeEmailSync() throws Exception {
		InputStream in = loadDataFile("sync_request_wm61.wbxml");
		byte[] data = FileUtils.streamBytes(in, true);
		Document doc = WBXMLTools.toXml(data);
		DOMUtils.logDom(doc);
	}

	public void testExchangeFolderSync() throws Exception {
		InputStream in = loadDataFile("exchange_foldersync.wbxml");
		byte[] data = FileUtils.streamBytes(in, true);
		Document doc = WBXMLTools.toXml(data);
		DOMUtils.logDom(doc);
	}
}
