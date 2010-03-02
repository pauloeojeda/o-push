package org.obm.sync.push.client;

import java.io.InputStream;

import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Document;

public class TestGetItemEstimate extends AbstractPushTest {

	public void testGetItemEstimate() throws Exception {
		InputStream in = loadDataFile("GetItemEstimateRequest.xml");
		Document doc = DOMUtils.parse(in);
		Document ret = postXml("ItemEstimate", doc, "GetItemEstimate");
		assertNotNull(ret);
	}
	
	public void testGetItemEstimate2() throws Exception {
		InputStream in = loadDataFile("GetItemEstimateRequestError1.xml");
		Document doc = DOMUtils.parse(in);
		Document ret = postXml("ItemEstimate", doc, "GetItemEstimate");
		assertNotNull(ret);
		
		in = loadDataFile("SyncRequestError1.xml");
		doc = DOMUtils.parse(in);
		ret = postXml("AirSync", doc, "Sync");
		assertNotNull(ret);
	}
}
