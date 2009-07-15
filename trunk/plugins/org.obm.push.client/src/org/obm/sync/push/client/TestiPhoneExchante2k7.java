package org.obm.sync.push.client;

import java.io.InputStream;

import org.obm.push.utils.DOMUtils;
import org.obm.push.utils.FileUtils;
import org.obm.push.wbxml.WBXMLTools;
import org.w3c.dom.Document;

public class TestiPhoneExchante2k7 extends AbstractPushTest {

	private void decode(String fileName) throws Exception {
		InputStream in = loadDataFile(fileName);
		byte[] data = FileUtils.streamBytes(in, true);
		Document doc = WBXMLTools.toXml(data);
		DOMUtils.logDom(doc);
	}
	
	public void testProvisionRequest1() throws Exception {
		System.err.println("iphone request 1");
		decode("iphoneProvReq1.wbxml");
	}
	public void testProvisionResponse1() throws Exception {
		System.err.println("exchange response 1");
		decode("ex2k7provResp1.wbxml");
	}
	public void testProvisionRequest2() throws Exception {
		System.err.println("iphone request 2");
		decode("iphoneProvReq2.wbxml");
	}
	public void testProvisionResponse2() throws Exception {
		System.err.println("exchange response 2");
		decode("ex2k7provResp2.wbxml");
	}
}
