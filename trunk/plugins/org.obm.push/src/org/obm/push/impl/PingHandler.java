package org.obm.push.impl;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.push.backend.BackendSession;
import org.obm.push.backend.IBackend;
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
public class PingHandler implements IRequestHandler {

	private static final Log logger = LogFactory.getLog(PingHandler.class);

	private IBackend backend;

	public PingHandler(IBackend backend) {
		this.backend = backend;
	}

	@Override
	public void process(BackendSession bs, Document doc, Responder responder) {
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
			sc.setCollectionId(DOMUtils.getElementText(f, "Id"));
			toMonitor.add(sc);
		}

		try {
			Document ret = DOMUtils.createDoc(null, "Ping");

			Set<SyncCollection> changedFolders = backend.waitForChanges(bs,
					toMonitor, interval);
			fillResponse(ret.getDocumentElement(), changedFolders);
			responder.sendResponse("Ping", ret);

		} catch (Exception e) {
			logger.error("Error creating provision response");
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
