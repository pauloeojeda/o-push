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
	
	public void testProvisionRequest() throws Exception {
		decode("provisionRequest.wbxml");
	}
	public void testProvisionResponse() throws Exception {
		decode("provisionResponse.wbxml");
	}
	public void testProvisionResponse2() throws Exception {
		decode("provisionResponse2.wbxml");
	}
}
