package org.obm.push.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.push.backend.IBackend;
import org.obm.push.backend.IHierarchyExporter;
import org.obm.push.backend.IHierarchyImporter;
import org.obm.push.backend.ItemChange;
import org.obm.push.backend.ServerId;
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
	public void process(ASParams p, Document doc, Responder responder) {
		logger.info("process(" + p.getUserId() + "/" + p.getDevType() + ")");
		String syncKey = DOMUtils.getElementText(doc.getDocumentElement(),
				"SyncKey");

		StateMachine sm = new StateMachine();
		SyncState state = sm.getSyncState(syncKey);
		String newSyncKey = sm.getNewSyncKey(syncKey);

		// look for Add, Modify, Remove

		Element changes = DOMUtils.getUniqueElement(doc.getDocumentElement(),
				"Changes");
		if (changes != null) {
			IHierarchyImporter importer = backend.getHierarchyImporter();
			importer.configure(state);

			Map<ServerId, String> idMap = new HashMap<ServerId, String>();

			NodeList nl = changes.getChildNodes();
			for (int i = 0; i < nl.getLength(); i++) {
				Element e = (Element) nl.item(i);
				String n = e.getNodeName();
				ServerId sid = null;
				SyncFolder sf = null;
				if (n.equals("Add") || n.equals("Modify")) {
					sf = folder(e);
					sid = importer.importFolderChange(sf);
				} else if (n.equals("Remove")) {
					sf = folder(e);
					sid = importer.importFolderDeletion(sf);
				}

				if (sid != null) {
					idMap.put(sid, sf.getClientId());
				}
			}
		}

		IHierarchyExporter exporter = backend.getHierarchyExporter();

		// dataClass, filterType, state, int, int
		exporter.configure(null, null, state, 0, 0);

		try {
			Document ret = DOMUtils.createDoc(null, "FolderSync");
			Element root = ret.getDocumentElement();
			DOMUtils.createElementAndText(root, "Status", "1");
			DOMUtils.createElementAndText(root, "SyncKey", newSyncKey);
			changes = DOMUtils.createElement(root, "Changes");

			exporter.synchronize();
			DOMUtils.createElementAndText(changes, "Count", exporter.getCount()
					+ "");
			List<ItemChange> changed = exporter.getChanged();
			for (ItemChange sf : changed) {
				Element add = DOMUtils.createElement(changes, "Add");
				encode(add, sf);
			}
			List<ItemChange> deleted = exporter.getDeleted();
			for (ItemChange sf : deleted) {
				Element remove = DOMUtils.createElement(changes, "Remove");
				encode(remove, sf);
			}

			responder.sendResponse("FolderHierarchy", ret);
			state = exporter.getState();
			sm.setSyncState(newSyncKey, state);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

	}

	private void encode(Element add, ItemChange sf) {
		DOMUtils.createElementAndText(add, "ServerId", sf.getServerId());
		DOMUtils.createElementAndText(add, "ParentId", sf.getParentId());
		DOMUtils.createElementAndText(add, "DisplayName", sf.getDisplayName());
		DOMUtils.createElementAndText(add, "Type", sf.getItemType()
				.asIntString());
	}

	private SyncFolder folder(Element e) {
		// TODO Auto-generated method stub
		return null;
	}
}
