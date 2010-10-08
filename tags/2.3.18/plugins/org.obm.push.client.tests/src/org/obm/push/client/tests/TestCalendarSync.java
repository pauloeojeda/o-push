package org.obm.push.client.tests;

import java.io.InputStream;
import java.util.Random;

import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class TestCalendarSync extends AbstractPushTest {

	public void testSync() throws Exception {
		// InputStream in = loadDataFile("FolderSyncRequest.xml");
		// Document doc = DOMUtils.parse(in);
		// Document ret = postXml("FolderHierarchy", doc, "FolderSync");
		// assertNotNull(ret);

		// in = loadDataFile("CalSyncRequest.xml");
		// doc = DOMUtils.parse(in);
		// Element synckeyElem = DOMUtils.getUniqueElement(doc
		// .getDocumentElement(), "SyncKey");
		// synckeyElem.setTextContent("0");
		// DOMUtils.logDom(doc);
		// ret = postXml("AirSync", doc, "Sync");
		// assertNotNull(ret);

		// String sk = DOMUtils.getUniqueElement(ret.getDocumentElement(),
		// "SyncKey").getTextContent();

		InputStream in = loadDataFile("CalSyncRequest2.xml");
		Document doc = DOMUtils.parse(in);
		Element synckeyElem = DOMUtils.getUniqueElement(doc
				.getDocumentElement(), "SyncKey");
		synckeyElem.setTextContent("1115154113");
		DOMUtils.logDom(doc);
		Document ret = postXml("AirSync", doc, "Sync");
		assertNotNull(ret);

		// in = loadDataFile("FolderSyncRequest.xml");
		// doc = DOMUtils.parse(in);
		// DOMUtils.getUniqueElement(doc.getDocumentElement(), "SyncKey")
		// .setTextContent(sk);
		// ret = postXml("FolderHierarchy", doc, "FolderSync");
		// assertNotNull(ret);

		// NodeList nl = ret.getDocumentElement().getElementsByTagName(
		// "ApplicationData");
		// System.out.println("received " + nl.getLength()
		// + " events from server.");
		// assertTrue(nl.getLength() > 0);
		//
		//		
		// String sk1 = DOMUtils.getUniqueElement(ret.getDocumentElement(),
		// "SyncKey").getTextContent();
		//
		// in = loadDataFile("CalSyncRequest3.xml");
		// doc = DOMUtils.parse(in);
		// synckeyElem = DOMUtils.getUniqueElement(doc.getDocumentElement(),
		// "SyncKey");
		// synckeyElem.setTextContent(sk1);
		// DOMUtils.logDom(doc);
		// ret = postXml("AirSync", doc, "Sync");
		// assertNotNull(ret);

	}

	public void testSyncOldSyncKey() throws Exception {
		InputStream in = loadDataFile("FolderSyncRequest.xml");
		Document doc = DOMUtils.parse(in);
		Document ret = postXml("FolderHierarchy", doc, "FolderSync");
		assertNotNull(ret);

		in = loadDataFile("CalSyncRequest.xml");
		doc = DOMUtils.parse(in);
		Element synckeyElem = DOMUtils.getUniqueElement(doc
				.getDocumentElement(), "SyncKey");
		synckeyElem.setTextContent("0");
		DOMUtils.logDom(doc);
		ret = postXml("AirSync", doc, "Sync");
		assertNotNull(ret);

		String sk1 = DOMUtils.getUniqueElement(ret.getDocumentElement(),
				"SyncKey").getTextContent();

		in = loadDataFile("CalSyncRequest2.xml");
		doc = DOMUtils.parse(in);
		synckeyElem = DOMUtils.getUniqueElement(doc.getDocumentElement(),
				"SyncKey");
		synckeyElem.setTextContent(sk1);
		DOMUtils.logDom(doc);
		ret = postXml("AirSync", doc, "Sync");
		assertNotNull(ret);

		String sk2 = DOMUtils.getUniqueElement(ret.getDocumentElement(),
				"SyncKey").getTextContent();

		in = loadDataFile("CalSyncRequest2.xml");
		doc = DOMUtils.parse(in);
		synckeyElem = DOMUtils.getUniqueElement(doc.getDocumentElement(),
				"SyncKey");
		synckeyElem.setTextContent(sk2);
		DOMUtils.logDom(doc);
		ret = postXml("AirSync", doc, "Sync");

		in = loadDataFile("CalSyncRequest2.xml");
		doc = DOMUtils.parse(in);
		synckeyElem = DOMUtils.getUniqueElement(doc.getDocumentElement(),
				"SyncKey");
		synckeyElem.setTextContent(sk1);
		DOMUtils.logDom(doc);
		ret = postXml("AirSync", doc, "Sync");
		assertNotNull(ret);

		in = loadDataFile("FolderSyncRequest.xml");
		doc = DOMUtils.parse(in);
		ret = postXml("FolderHierarchy", doc, "FolderSync");
		assertNotNull(ret);

		in = loadDataFile("CalSyncRequest.xml");
		doc = DOMUtils.parse(in);
		synckeyElem = DOMUtils.getUniqueElement(doc
				.getDocumentElement(), "SyncKey");
		synckeyElem.setTextContent("0");
		DOMUtils.logDom(doc);
		ret = postXml("AirSync", doc, "Sync");
		assertNotNull(ret);

		in = loadDataFile("CalSyncRequest2.xml");
		doc = DOMUtils.parse(in);
		synckeyElem = DOMUtils.getUniqueElement(doc.getDocumentElement(),
				"SyncKey");
		synckeyElem.setTextContent(sk1);
		DOMUtils.logDom(doc);
		ret = postXml("AirSync", doc, "Sync");
		assertNotNull(ret);

	}

	public void testCalAdd() throws Exception {
		InputStream in = loadDataFile("FolderSyncRequest.xml");
		Document doc = DOMUtils.parse(in);
		Document ret = postXml("FolderHierarchy", doc, "FolderSync");
		assertNotNull(ret);

		in = loadDataFile("CalSyncRequest.xml");
		doc = DOMUtils.parse(in);
		Element synckeyElem = DOMUtils.getUniqueElement(doc
				.getDocumentElement(), "SyncKey");
		synckeyElem.setTextContent("0");
		DOMUtils.logDom(doc);
		ret = postXml("AirSync", doc, "Sync");
		assertNotNull(ret);

		String sk = DOMUtils.getUniqueElement(ret.getDocumentElement(),
				"SyncKey").getTextContent();

		in = loadDataFile("CalSyncAdd.xml");
		doc = DOMUtils.parse(in);
		synckeyElem = DOMUtils.getUniqueElement(doc.getDocumentElement(),
				"SyncKey");
		synckeyElem.setTextContent(sk);
		Element cliidElem = DOMUtils.getUniqueElement(doc.getDocumentElement(),
				"ClientId");
		cliidElem.setTextContent("" + new Random().nextInt(999999999));
		DOMUtils.logDom(doc);
		ret = postXml("AirSync", doc, "Sync");
		assertNotNull(ret);

		NodeList nl = ret.getDocumentElement().getElementsByTagName(
				"ApplicationData");
		System.out.println("received " + nl.getLength()
				+ " events from server.");
		assertTrue(nl.getLength() > 0);

	}

	public void testCalTwoAdd() throws Exception {
		InputStream in = loadDataFile("FolderSyncRequest.xml");
		Document doc = DOMUtils.parse(in);
		Document ret = postXml("FolderHierarchy", doc, "FolderSync");
		assertNotNull(ret);

		in = loadDataFile("CalSyncRequest.xml");
		doc = DOMUtils.parse(in);
		Element synckeyElem = DOMUtils.getUniqueElement(doc
				.getDocumentElement(), "SyncKey");
		synckeyElem.setTextContent("0");
		DOMUtils.logDom(doc);
		ret = postXml("AirSync", doc, "Sync");
		assertNotNull(ret);

		String sk = DOMUtils.getUniqueElement(ret.getDocumentElement(),
				"SyncKey").getTextContent();

		in = loadDataFile("CalSyncRequest2.xml");
		doc = DOMUtils.parse(in);
		synckeyElem = DOMUtils.getUniqueElement(doc.getDocumentElement(),
				"SyncKey");
		synckeyElem.setTextContent(sk);
		DOMUtils.logDom(doc);
		ret = postXml("AirSync", doc, "Sync");
		assertNotNull(ret);

		sk = DOMUtils.getUniqueElement(ret.getDocumentElement(), "SyncKey")
				.getTextContent();

		in = loadDataFile("CalSyncAdd.xml");
		doc = DOMUtils.parse(in);
		// synckeyElem = DOMUtils.getUniqueElement(doc.getDocumentElement(),
		// "SyncKey");
		// synckeyElem.setTextContent(sk);
		Element cliidElem = DOMUtils.getUniqueElement(doc.getDocumentElement(),
				"ClientId");
		cliidElem.setTextContent("999999999");
		for (int i = 0; i < 2; i++) {
			synckeyElem = DOMUtils.getUniqueElement(doc.getDocumentElement(),
					"SyncKey");
			synckeyElem.setTextContent(sk);
			DOMUtils.logDom(doc);
			ret = postXml("AirSync", doc, "Sync");
			assertNotNull(ret);
			sk = DOMUtils.getUniqueElement(ret.getDocumentElement(), "SyncKey")
					.getTextContent();
		}

		NodeList nl = ret.getDocumentElement().getElementsByTagName(
				"ApplicationData");
		System.out.println("received " + nl.getLength()
				+ " events from server.");
		assertTrue(nl.getLength() > 0);

	}

	public void testCalDelete() throws Exception {
		InputStream in = loadDataFile("FolderSyncRequest.xml");
		Document doc = DOMUtils.parse(in);
		Document ret = postXml25("FolderHierarchy", doc, "FolderSync");
		assertNotNull(ret);

		in = loadDataFile("CalSyncRequest.xml");
		doc = DOMUtils.parse(in);
		Element synckeyElem = DOMUtils.getUniqueElement(doc
				.getDocumentElement(), "SyncKey");
		synckeyElem.setTextContent("0");
		DOMUtils.logDom(doc);
		ret = postXml25("AirSync", doc, "Sync");
		assertNotNull(ret);

		String sk = DOMUtils.getUniqueElement(ret.getDocumentElement(),
				"SyncKey").getTextContent();

		in = loadDataFile("CalSyncDelete1.xml");
		doc = DOMUtils.parse(in);
		synckeyElem = DOMUtils.getUniqueElement(doc.getDocumentElement(),
				"SyncKey");
		synckeyElem.setTextContent(sk);
		Element cliidElem = DOMUtils.getUniqueElement(doc.getDocumentElement(),
				"ClientId");
		String clientId = "" + new Random().nextInt(999999999);
		cliidElem.setTextContent(clientId);
		DOMUtils.logDom(doc);
		ret = postXml25("AirSync", doc, "Sync");
		assertNotNull(ret);

		NodeList nl = ret.getDocumentElement().getElementsByTagName("Add");
		String servId = null;
		for (int i = 0; i < nl.getLength(); i++) {
			if (nl.item(i) instanceof Element) {
				Element elem = (Element) nl.item(i);
				String cliId = DOMUtils.getElementText(elem, "ClientId");
				if (clientId.equals(cliId)) {
					servId = DOMUtils.getElementText(elem, "ServerId");
					break;
				}
			}
		}
		if (servId == null) {
			fail();
		}
		sk = DOMUtils.getUniqueElement(ret.getDocumentElement(), "SyncKey")
				.getTextContent();

		in = loadDataFile("CalSyncDelete2.xml");
		doc = DOMUtils.parse(in);
		synckeyElem = DOMUtils.getUniqueElement(doc.getDocumentElement(),
				"SyncKey");
		synckeyElem.setTextContent(sk);
		Element servIdElem = DOMUtils.getUniqueElement(
				doc.getDocumentElement(), "ServerId");
		servIdElem.setTextContent(servId);
		DOMUtils.logDom(doc);
		ret = postXml25("AirSync", doc, "Sync");
		assertNotNull(ret);

	}

}
