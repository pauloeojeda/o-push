package org.obm.push.impl;

import java.util.HashSet;
import java.util.Set;

import org.obm.push.backend.BackendSession;
import org.obm.push.backend.IBackend;
import org.obm.push.backend.IContinuation;
import org.obm.push.backend.SyncCollection;
import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Handles the Provision cmd
 * 
 * @author tom
 * 
 */
public class PingHandler extends WbxmlRequestHandler {

	public PingHandler(IBackend backend) {
		super(backend);
	}

	@Override
	public void process(IContinuation continuation, BackendSession bs,
			Document doc, Responder responder) {
		logger.info("process(" + bs.getLoginAtDomain() + "/" + bs.getDevType()
				+ ")");

		Element pr = doc.getDocumentElement();

		long interval = Long.parseLong(DOMUtils.getUniqueElement(pr,
				"HeartbeatInterval").getTextContent());

		NodeList folders = pr.getElementsByTagName("Folder");
		Set<SyncCollection> toMonitor = new HashSet<SyncCollection>();
		for (int i = 0; i < folders.getLength(); i++) {
			Element f = (Element) folders.item(i);
			SyncCollection sc = new SyncCollection();
			sc.setDataClass(DOMUtils.getElementText(f, "Class"));
			int id = Integer.parseInt(DOMUtils.getElementText(f, "Id"));
			sc.setCollectionId(backend.getStore().getCollectionString(id));
			toMonitor.add(sc);
		}

		logger.info("suspend for " + interval + " seconds");
		backend.pollForChanges(continuation, bs, toMonitor, interval * 1000);
	}

	public void sendResponse(BackendSession bs, Responder responder) {
		Document ret = DOMUtils.createDoc(null, "Ping");

		fillResponse(ret.getDocumentElement(), bs.getChangedFolders());
		try {
			responder.sendResponse("Ping", ret);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	private void fillResponse(Element element,
			Set<SyncCollection> changedFolders) {
		if (changedFolders == null) {
			DOMUtils.createElementAndText(element, "Status", "1");
		} else {
			DOMUtils.createElementAndText(element, "Status", "2");
			DOMUtils.createElement(element, "Folders");
			for (SyncCollection sc : changedFolders) {
				DOMUtils.createElementAndText(element, "Folder", sc
						.getCollectionId());
			}
		}

		// TODO Auto-generated method stub

	}

}
