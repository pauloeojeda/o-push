package org.obm.sync.push.client;

import java.io.InputStream;

import org.obm.push.utils.DOMUtils;
import org.obm.sync.push.client.utils.SyncKeyUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class TestEmailSync extends AbstractPushTest {

	public void testMailSync() throws Exception {
		optionsQuery();
		optionsQuery();

		InputStream in = loadDataFile("ProvisionRequest1.xml");
		Document doc = DOMUtils.parse(in);
		Document ret = postXml("Provision", doc, "Provision");
		assertNotNull(ret);

		in = loadDataFile("ProvisionRequest1.xml");
		doc = DOMUtils.parse(in);
		ret = postXml("Provision", doc, "Provision");
		assertNotNull(ret);
		
		String policyKey = DOMUtils.getElementText(ret.getDocumentElement(), "PolicyKey");
		in = loadDataFile("ProvisionRequest2.xml");
		doc = DOMUtils.parse(in);
		DOMUtils.getUniqueElement(doc.getDocumentElement(), "PolicyKey").setTextContent(policyKey);
		ret = postXml("Provision", doc, "Provision");
		assertNotNull(ret);
		

		in = loadDataFile("FolderSyncRequest.xml");
		doc = DOMUtils.parse(in);
		ret = postXml("FolderHierarchy", doc, "FolderSync");
		assertNotNull(ret);
		String folderSyncKey = SyncKeyUtils.getFolderSyncKey(ret);

		in = loadDataFile("FolderSyncRequest.xml");
		doc = DOMUtils.parse(in);
		SyncKeyUtils.appendFolderSyncKey(doc, folderSyncKey);
		ret = postXml("FolderHierarchy", doc, "FolderSync");
		assertNotNull(ret);

		in = loadDataFile("EmailSyncRequest.xml");
		doc = DOMUtils.parse(in);
		Element synckeyElem = DOMUtils.getUniqueElement(doc
				.getDocumentElement(), "SyncKey");
		synckeyElem.setTextContent("0");
		ret = postXml("AirSync", doc, "Sync");
		assertNotNull(ret);
		String sk = DOMUtils.getUniqueElement(ret.getDocumentElement(),
				"SyncKey").getTextContent();

		in = loadDataFile("EmailSyncRequest1.xml");
		doc = DOMUtils.parse(in);
		synckeyElem = DOMUtils.getUniqueElement(doc.getDocumentElement(),
				"AirSync:SyncKey");
		synckeyElem.setTextContent(sk);
		ret = postXml("ItemEstimate", doc, "GetItemEstimate");
		assertNotNull(ret);

		in = loadDataFile("EmailSyncRequest2.xml");
		doc = DOMUtils.parse(in);
		synckeyElem = DOMUtils.getUniqueElement(doc.getDocumentElement(),
				"SyncKey");
		synckeyElem.setTextContent(sk);
		ret = postXml("AirSync", doc, "Sync");
		assertNotNull(ret);

		NodeList nl = ret.getDocumentElement().getElementsByTagName(
				"ApplicationData");
		System.out.println("received " + nl.getLength()
				+ " events from server.");
		assertTrue(nl.getLength() > 0);

		// sk = DOMUtils.getUniqueElement(ret.getDocumentElement(), "SyncKey")
		// .getTextContent();
		// in = loadDataFile("EmailSyncRequest3.xml");
		// doc = DOMUtils.parse(in);
		// synckeyElem = DOMUtils.getUniqueElement(doc.getDocumentElement(),
		// "SyncKey");
		// synckeyElem.setTextContent(sk);
		// DOMUtils.logDom(doc);
		// ret = postXml("AirSync", doc, "Sync");
		// assertNotNull(ret);
		//
		// byte[] file = postGetAttachment("6%3a4%3a0");
		// assertNotNull(file);

	}

	public void testMailSync2() throws Exception {
		InputStream in = loadDataFile("FolderSyncRequest.xml");
		Document doc = DOMUtils.parse(in);
		Document ret = postXml("FolderHierarchy", doc, "FolderSync");
		assertNotNull(ret);

		in = loadDataFile("EmailSyncRequest.xml");
		doc = DOMUtils.parse(in);
		Element synckeyElem = DOMUtils.getUniqueElement(doc
				.getDocumentElement(), "SyncKey");
		synckeyElem.setTextContent("0");
		DOMUtils.logDom(doc);
		ret = postXml("AirSync", doc, "Sync");
		assertNotNull(ret);

		String sk = DOMUtils.getUniqueElement(ret.getDocumentElement(),
				"SyncKey").getTextContent();
		in = loadDataFile("GetItemEstimateRequestEmail.xml");
		doc = DOMUtils.parse(in);
		synckeyElem = DOMUtils.getUniqueElement(doc.getDocumentElement(),
				"AirSync:SyncKey");
		synckeyElem.setTextContent(sk);
		ret = postXml("ItemEstimate", doc, "GetItemEstimate");
		assertNotNull(ret);

		// sk = DOMUtils.getUniqueElement(ret.getDocumentElement(), "SyncKey")
		// .getTextContent();
		in = loadDataFile("EmailSyncRequest2.xml");
		doc = DOMUtils.parse(in);
		synckeyElem = DOMUtils.getUniqueElement(doc.getDocumentElement(),
				"SyncKey");
		synckeyElem.setTextContent(sk);
		DOMUtils.logDom(doc);
		ret = postXml("AirSync", doc, "Sync");
		assertNotNull(ret);

		NodeList nl = ret.getDocumentElement().getElementsByTagName(
				"ApplicationData");
		System.out.println("received " + nl.getLength()
				+ " events from server.");
		assertTrue(nl.getLength() > 0);

		sk = DOMUtils.getUniqueElement(ret.getDocumentElement(), "SyncKey")
				.getTextContent();
		in = loadDataFile("EmailSyncRequest2.xml");
		doc = DOMUtils.parse(in);
		synckeyElem = DOMUtils.getUniqueElement(doc.getDocumentElement(),
				"SyncKey");
		synckeyElem.setTextContent(sk);
		DOMUtils.logDom(doc);
		ret = postXml("AirSync", doc, "Sync");
		assertNotNull(ret);

		nl = ret.getDocumentElement().getElementsByTagName("ApplicationData");
		System.out.println("received " + nl.getLength()
				+ " events from server.");
		assertTrue(nl.getLength() == 0);

	}

	public void testMailSyncMultiBodyPref() throws Exception {
		InputStream in = loadDataFile("FolderSyncRequest.xml");
		Document doc = DOMUtils.parse(in);
		Document ret = postXml("FolderHierarchy", doc, "FolderSync");
		assertNotNull(ret);

		in = loadDataFile("EmailSyncRequest.xml");
		doc = DOMUtils.parse(in);
		Element synckeyElem = DOMUtils.getUniqueElement(doc
				.getDocumentElement(), "SyncKey");
		synckeyElem.setTextContent("0");
		DOMUtils.logDom(doc);
		ret = postXml("AirSync", doc, "Sync");
		assertNotNull(ret);

		String sk = DOMUtils.getUniqueElement(ret.getDocumentElement(),
				"SyncKey").getTextContent();
		in = loadDataFile("EmailSyncRequestMultipleBodyPref.xml");
		doc = DOMUtils.parse(in);
		synckeyElem = DOMUtils.getUniqueElement(doc.getDocumentElement(),
				"SyncKey");
		synckeyElem.setTextContent(sk);
		DOMUtils.logDom(doc);
		ret = postXml("AirSync", doc, "Sync");
		assertNotNull(ret);

	}
}
