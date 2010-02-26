package org.obm.sync.push.client;

import java.io.InputStream;

import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class TestProvision extends AbstractPushTest {

	public void testSettingsProvisionProtocol121() throws Exception {
		optionsQuery();

		InputStream in = loadDataFile("ProvisionRequest1.xml");
		Document doc = DOMUtils.parse(in);
		Document ret = postXml("Provision", doc, "Provision");
		assertNotNull(ret);
	}

	public void testSettingsProvisionProtocol25() throws Exception {
		optionsQuery();

		InputStream in = loadDataFile("ProvisionRequest1Protocol2.5.xml");
		Document doc = DOMUtils.parse(in);
		Document ret = postXml("Provision", doc, "Provision", "0", "2.5");
		assertNotNull(ret);
		
		String policyKey = DOMUtils.getElementText(ret.getDocumentElement(), "PolicyKey");

		in = loadDataFile("ProvisionRequest2Protocol2.5.xml");
		doc = DOMUtils.parse(in);
		Element elemPolicy = DOMUtils.getUniqueElement(doc.getDocumentElement(), "PolicyKey");
		elemPolicy.setTextContent(policyKey);
		ret = postXml("Provision", doc, "Provision", "0", "2.5");
		
	}
}
