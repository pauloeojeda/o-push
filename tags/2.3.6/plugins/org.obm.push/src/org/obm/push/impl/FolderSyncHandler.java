package org.obm.push.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.push.backend.BackendSession;
import org.obm.push.backend.IBackend;
import org.obm.push.backend.IContinuation;
import org.obm.push.backend.IHierarchyExporter;
import org.obm.push.backend.IHierarchyImporter;
import org.obm.push.backend.ItemChange;
import org.obm.push.backend.ServerId;
import org.obm.push.backend.SyncFolder;
import org.obm.push.exception.CollectionNotFoundException;
import org.obm.push.state.StateMachine;
import org.obm.push.state.SyncState;
import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class FolderSyncHandler extends WbxmlRequestHandler {

	private static final Log logger = LogFactory
			.getLog(FolderSyncHandler.class);

	public FolderSyncHandler(IBackend backend) {
		super(backend);
	}

	@Override
	public void process(IContinuation continuation, BackendSession bs,
			Document doc, ActiveSyncRequest request, Responder responder) {
		logger.info("process(" + bs.getLoginAtDomain() + "/" + bs.getDevType()
				+ ")");
		String syncKey = DOMUtils.getElementText(doc.getDocumentElement(),
				"SyncKey");

		if ("0".equals(syncKey)) {
			backend.resetForFullSync(bs);
			bs.setState(new SyncState());
		}

		StateMachine sm = new StateMachine(backend.getStore());
		SyncState state = sm.getSyncState(syncKey);
		if (!state.isValid()) {
			sendError(responder, FolderSyncStatus.INVALID_SYNC_KEY);
			return;
		}
		// look for Add, Modify, Remove

		Element changes = DOMUtils.getUniqueElement(doc.getDocumentElement(),
				"Changes");
		if (changes != null) {
			IHierarchyImporter importer = backend.getHierarchyImporter(bs);
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

		IHierarchyExporter exporter = backend.getHierarchyExporter(bs);

		// dataClass, filterType, state, int, int
		exporter.configure(bs, null, null, state, 0, 0);

		try {
			Document ret = DOMUtils.createDoc(null, "FolderSync");
			Element root = ret.getDocumentElement();
			DOMUtils.createElementAndText(root, "Status", "1");
			Element sk = DOMUtils.createElement(root, "SyncKey");
			changes = DOMUtils.createElement(root, "Changes");

			// FIXME we know that we do not monitor hierarchy, so just respond
			// that nothing changed
			List<ItemChange> changed = exporter.getChanged(bs);
			if ("0".equals(syncKey)) {
				int cnt = exporter.getCount(bs);
				DOMUtils.createElementAndText(changes, "Count", cnt + "");

				for (ItemChange sf : changed) {
					Element add = DOMUtils.createElement(changes, "Add");
					encode(add, sf);
				}
				List<ItemChange> deleted = exporter.getDeleted(bs);
				for (ItemChange sf : deleted) {
					Element remove = DOMUtils.createElement(changes, "Remove");
					encode(remove, sf);
				}
			} else {
				DOMUtils.createElementAndText(changes, "Count", "0");
				for (ItemChange sf : changed) {
					if (sf.isNew()) {
						sendError(responder, FolderSyncStatus.INVALID_SYNC_KEY);
						return;
					}
				}
			}
			String newSyncKey = sm.allocateNewSyncKey(bs, exporter
					.getRootFolderId(bs), state);
			sk.setTextContent(newSyncKey);
			responder.sendResponse("FolderHierarchy", ret);
		} catch (CollectionNotFoundException e) {
			logger.error(e.getMessage(), e);
			sendError(responder, FolderSyncStatus.INVALID_SYNC_KEY);
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

	private void sendError(Responder resp, FolderSyncStatus status) {
		Document ret = DOMUtils.createDoc(null, "FolderSync");
		Element root = ret.getDocumentElement();
		DOMUtils.createElementAndText(root, "Status", status.asXmlValue());
		try {
			resp.sendResponse("FolderHierarchy", ret);
		} catch (IOException e) {
			logger.info(e.getMessage(), e);
		}
	}
}
