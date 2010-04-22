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
	
	public void testSettingsProvisionProtocol120() throws Exception {
		optionsQuery();

		InputStream in = loadDataFile("ProvisionRequest1.xml");
		Document doc = DOMUtils.parse(in);
		Document ret = postXml("Provision", doc, "Provision",null,"12.0", false);
		assertNotNull(ret);
	}

	public void testSettingsProvisionProtocol25() throws Exception {
		optionsQuery();

		InputStream in = loadDataFile("ProvisionRequest1Protocol2.5.xml");
		Document doc = DOMUtils.parse(in);
		System.out.println("From test:");
		DOMUtils.logDom(doc);
		Document ret = postXml("Provision", doc, "Provision", "0", "2.5", false);
		assertNotNull(ret);
		
		String policyKey = DOMUtils.getElementText(ret.getDocumentElement(), "PolicyKey");

		in = loadDataFile("ProvisionRequest2Protocol2.5.xml");
		doc = DOMUtils.parse(in);
		Element elemPolicy = DOMUtils.getUniqueElement(doc.getDocumentElement(), "PolicyKey");
		elemPolicy.setTextContent(policyKey);
		System.out.println("From test:");
		DOMUtils.logDom(doc);
		ret = postXml("Provision", doc, "Provision", "0", "2.5", false);
		
		policyKey = DOMUtils.getElementText(ret.getDocumentElement(), "PolicyKey");

		in = loadDataFile("ProvisionRequest2Protocol2.5.xml");
		doc = DOMUtils.parse(in);
		elemPolicy = DOMUtils.getUniqueElement(doc.getDocumentElement(), "PolicyKey");
		elemPolicy.setTextContent(policyKey);
		System.out.println("From test:");
		DOMUtils.logDom(doc);
		ret = postXml("Provision", doc, "Provision", "0", "2.5", false);
		Element policy = DOMUtils.getUniqueElement(ret.getDocumentElement(), "Policy");
		String status = DOMUtils.getElementText(policy, "Status");
		assertEquals("5", status);
		
	}
}
