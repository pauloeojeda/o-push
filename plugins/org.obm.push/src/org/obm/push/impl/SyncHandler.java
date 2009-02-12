package org.obm.push.impl;

import java.util.LinkedList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.push.backend.IApplicationData;
import org.obm.push.backend.IBackend;
import org.obm.push.backend.IContentsImporter;
import org.obm.push.data.IDataDecoder;
import org.obm.push.state.StateMachine;
import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class SyncHandler implements IRequestHandler {

	public static final Integer SYNC_TRUNCATION_ALL = 9;

	private static final Log logger = LogFactory.getLog(SyncHandler.class);

	private IBackend backend;

	public SyncHandler(IBackend backend) {
		this.backend = backend;
	}

	@Override
	public void process(ASParams p, Document doc, Responder responder) {
		logger.info("process(" + p.getUserId() + "/" + p.getDevType() + ")");

		StateMachine sm = new StateMachine();

		NodeList nl = doc.getDocumentElement().getElementsByTagName(
				"Collection");
		LinkedList<SyncCollection> collections = new LinkedList<SyncCollection>();
		for (int i = 0; i < nl.getLength(); i++) {
			Element col = (Element) nl.item(i);
			collections.add(processCollection(p, sm, col));
		}

		Document reply = null;
		try {
			reply = DOMUtils.createDoc(null, "Sync");
			Element root = reply.getDocumentElement();
			Element cols = DOMUtils.createElement(root, "Collections");
			
			for (SyncCollection c : collections) {
				c.setNewSyncKey(sm.getNewSyncKey(c.getSyncKey()));
				Element ce = DOMUtils.createElement(cols, "Collection");
				DOMUtils.createElementAndText(ce, "Class", c.getDataClass());
				DOMUtils.createElementAndText(ce, "SyncKey", c.getNewSyncKey());
				DOMUtils.createElementAndText(ce, "CollectionId", c.getCollectionId());
				DOMUtils.createElementAndText(ce, "Status", "1");
			}
			
			responder.sendResponse("AirSync", reply);
		} catch (Exception e) {
			logger.error("Error creating Sync response", e);
		}
	}

	private SyncCollection processCollection(ASParams p, StateMachine sm,
			Element col) {
		SyncCollection collection = new SyncCollection();
		collection.setDataClass(DOMUtils.getElementText(col, "Class"));
		collection.setSyncKey(DOMUtils.getElementText(col, "SyncKey"));
		Element fid = DOMUtils.getUniqueElement(col, "CollectionId");
		if (fid != null) {
			collection.setCollectionId(fid.getTextContent());
		}
		// TODO sync supported
		// TODO sync <deletesasmoves/>
		// TODO sync <getchanges/>
		// TODO sync max items
		// TODO sync options

		if (collection.getCollectionId() == null) {
			collection.setCollectionId(getFolderId(p.getDevId(), collection
					.getDataClass()));
		}

		collection.setSyncState(sm.getSyncState(collection.getSyncKey()));

		Element perform = DOMUtils.getUniqueElement(col, "Perform");

		if (perform != null) {
			// get our sync state for this collection
			IContentsImporter importer = backend.getContentsImporter(collection
					.getCollectionId());
			importer.configure(collection.getSyncState(), collection
					.getConflict());
			NodeList mod = perform.getChildNodes();
			for (int j = 0; j < mod.getLength(); j++) {
				Element modification = (Element) mod.item(j);
				processModification(collection, importer, modification);
			}
		}
		return collection;
	}

	private void processModification(SyncCollection collection,
			IContentsImporter importer, Element modification) {
		String modType = modification.getNodeName();
		String serverId = DOMUtils.getElementText(modification, "ServerId");
		String clientId = DOMUtils.getElementText(modification, "ClientId");
		Element syncData = DOMUtils.getUniqueElement(modification, "Data");
		String dataClass = collection.getDataClass();
		IDataDecoder dd = getDecoder(dataClass);
		IApplicationData data = null;
		if (dd != null) {
			data = dd.decode(syncData.getTextContent());
			if (modType.equals("Modify")) {
				if (data.isRead()) {
					importer.importMessageReadFlag(serverId, data.isRead());
				} else {
					importer.importMessageChange(serverId, data);
				}
				collection.setImportedChanges(true);
			} else if (modType.equals("Add")) {
				String id = importer.importMessageChange(null, data);
				if (clientId != null && id != null) {
					collection.getClientIds().put(clientId, id);
					collection.setImportedChanges(true);
				}
			} else if (modType.equals("Delete")) {
				if (collection.isDeletesAsMoves()) {
					String trash = backend.getWasteBasket();
					if (trash != null) {
						importer.importMessageMove(serverId, trash);
						collection.setImportedChanges(true);
					}
				} else {
					importer.importMessageDeletion(serverId);
					collection.setImportedChanges(true);
				}
			} else if (modType.equals("Fetch")) {
				collection.getFetchIds().add(serverId);
			}
			collection.setSyncState(importer.getState());
		} else {
			logger.info("no decoder for " + dataClass);
		}
	}

	private IDataDecoder getDecoder(String dataClass) {
		// TODO Auto-generated method stub
		return null;
	}

	private String getFolderId(String devId, Object object) {
		// TODO Auto-generated method stub
		return devId+"/"+object;
	}
}
