package org.obm.push.impl;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.push.backend.IBackend;
import org.obm.push.backend.IImporter;
import org.obm.push.backend.SyncFolder;
import org.obm.push.state.StateMachine;
import org.obm.push.state.SyncState;
import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class FolderSyncHandler implements IRequestHandler {

	private static final Log logger = LogFactory
			.getLog(FolderSyncHandler.class);

	private IBackend backend;

	public FolderSyncHandler(IBackend backend) {
		this.backend = backend;
	}

	@Override
	public void process(ASParams p, Document doc, HttpServletResponse response) {
		logger.info("process(" + p.getUserId() + "/" + p.getDevType() + ")");
		String syncKey = DOMUtils.getElementText(doc.getDocumentElement(),
				"SyncKey");

		StateMachine sm = new StateMachine();
		SyncState state = sm.getSyncState(syncKey);
		String newSyncKey = sm.getNewSyncKey(syncKey);

		// look for Add, Modify, Remove
		IImporter importer = backend.getHierarchyImporter();
		importer.configure(state);

		Element changes = DOMUtils.getUniqueElement(doc.getDocumentElement(),
				"Changes");
		NodeList nl = changes.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			Element e = (Element) nl.item(i);
			String n = e.getNodeName();
			if (n.equals("Add") || n.equals("Modify")) {
				importer.importFolderChange(folder(e));
			} else if (n.equals("Remove")) {
				importer.importFolderDeletion(folder(e));
			}
		}
	}

	private SyncFolder folder(Element e) {
		// TODO Auto-generated method stub
		return null;
	}
}
