package org.obm.push.client.tests;

import java.io.InputStream;

import org.obm.push.utils.DOMUtils;
import org.obm.sync.push.client.Collection;
import org.obm.sync.push.client.Folder;
import org.obm.sync.push.client.FolderSyncResponse;
import org.obm.sync.push.client.FolderType;
import org.obm.sync.push.client.SyncResponse;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class TestEmailSync extends OPClientTests {

	public void testMailSync() throws Exception {
		testOptions();
		FolderSyncResponse fsr = testInitialFolderSync();
		Folder inbox = fsr.getFolders().get(FolderType.DEFAULT_INBOX_FOLDER);
		SyncResponse syncResp = testInitialSync(inbox);
		
		InputStream in = null;
		Document doc = null;
		
		in = loadDataFile("GetItemEstimateRequestEmail.xml");
		doc = DOMUtils.parse(in);
		replace(doc, inbox, syncResp);
		Document estimateRet = postXml("ItemEstimate", doc, "GetItemEstimate");
		assertNotNull(estimateRet);
		
		
		in = loadDataFile("EmailSyncRequest.xml");
		doc = DOMUtils.parse(in);
		replace(doc, inbox, syncResp);
		syncResp = testSync(doc);
		Collection colInbox = syncResp.getCollection(inbox.getServerId());
		assertNotNull(colInbox);
		System.out.println("received " + colInbox.getAdds().size()
				+ " email from server.");
		assertTrue(colInbox.getAdds().size() > 0);

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
		testOptions();
		FolderSyncResponse fsr = testInitialFolderSync();
		Folder inbox = fsr.getFolders().get(FolderType.DEFAULT_INBOX_FOLDER);
		SyncResponse syncResp = testInitialSync(inbox);
		
		InputStream in = null;
		Document doc = null;
//		Document ret = null;
		
		in = loadDataFile("GetItemEstimateRequestEmail.xml");
		doc = DOMUtils.parse(in);
		replace(doc, inbox, syncResp);
		Document estimateRet = postXml("ItemEstimate", doc, "GetItemEstimate");
		assertNotNull(estimateRet);
		
		
		in = loadDataFile("EmailSyncRequest.xml");
		doc = DOMUtils.parse(in);
		replace(doc, inbox, syncResp);
		syncResp = testSync(doc);
		assertNotNull(syncResp);
		Collection colInbox = syncResp.getCollection(inbox.getServerId());
		assertNotNull(colInbox);
		System.out.println("received " + colInbox.getAdds().size()
				+ " email from server.");
		assertTrue(colInbox.getAdds().size() > 0);

		in = loadDataFile("EmailSyncRequest.xml");
		doc = DOMUtils.parse(in);
		replace(doc, inbox, syncResp);
		syncResp = testSync(doc);
		colInbox = syncResp.getCollection(inbox.getServerId());
		assertNotNull(colInbox);
		System.out.println("received " + colInbox.getAdds().size()
				+ " email from server.");
		assertEquals(0, colInbox.getAdds().size());

	}
	
	public void testMailChangeRead() throws Exception {
		testOptions();
		FolderSyncResponse fsr = testInitialFolderSync();
		Folder inbox = fsr.getFolders().get(FolderType.DEFAULT_INBOX_FOLDER);
		SyncResponse syncResp = testInitialSync(inbox);
		
		InputStream in = null;
		Document doc = null;
		Document ret = null;
		
		in = loadDataFile("GetItemEstimateRequestEmail.xml");
		doc = DOMUtils.parse(in);
		replace(doc, inbox, syncResp);
		ret = postXml("ItemEstimate", doc, "GetItemEstimate");
		assertNotNull(ret);
		
		in = loadDataFile("EmailSyncRequest.xml");
		doc = DOMUtils.parse(in);
		replace(doc, inbox, syncResp);
		syncResp = testSync(doc);
		assertNotNull(syncResp);
		Collection colInbox = syncResp.getCollection(inbox.getServerId());
		assertNotNull(colInbox);
		System.out.println("received " + colInbox.getAdds().size()
				+ " email from server.");
		assertTrue(colInbox.getAdds().size() > 0);
		String serverId = colInbox.getAdds().get(0).getServerId();
		
		in = loadDataFile("EmailSyncReadRequest.xml");
		doc = DOMUtils.parse(in);
		replace(doc, inbox, syncResp);
		DOMUtils.getUniqueElement(doc.getDocumentElement(), "ServerId").setTextContent(serverId);
		syncResp = testSync(doc);
		syncResp = testSync(doc);
		assertNotNull(syncResp);
		colInbox = syncResp.getCollection(inbox.getServerId());
		assertNotNull(colInbox);
		System.out.println("received " + colInbox.getAdds().size()
				+ " email from server.");
		assertEquals(0, colInbox.getAdds().size());

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
