package org.obm.push.impl;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.push.backend.BackendSession;
import org.obm.push.backend.IApplicationData;
import org.obm.push.backend.IBackend;
import org.obm.push.backend.IContentsExporter;
import org.obm.push.backend.IContentsImporter;
import org.obm.push.backend.ItemChange;
import org.obm.push.backend.PIMDataType;
import org.obm.push.data.CalendarDecoder;
import org.obm.push.data.ContactsDecoder;
import org.obm.push.data.EncoderFactory;
import org.obm.push.data.IDataDecoder;
import org.obm.push.data.IDataEncoder;
import org.obm.push.state.StateMachine;
import org.obm.push.state.SyncState;
import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

//<?xml version="1.0" encoding="UTF-8"?>
//<Sync>
//<Collections>
//<Collection>
//<Class>Contacts</Class>
//<SyncKey>ff16677f-ee9c-42dc-a562-709f899c8d31</SyncKey>
//<CollectionId>obm://contacts/user@domain</CollectionId>
//<DeletesAsMoves/>
//<GetChanges/>
//<WindowSize>100</WindowSize>
//<Options>
//<Truncation>4</Truncation>
//<RTFTruncation>4</RTFTruncation>
//<Conflict>1</Conflict>
//</Options>
//</Collection>
//</Collections>
//</Sync>

public class SyncHandler implements IRequestHandler {

	public static final Integer SYNC_TRUNCATION_ALL = 9;

	private static final Log logger = LogFactory.getLog(SyncHandler.class);

	private IBackend backend;

	private Map<String, IDataDecoder> decoders;

	private EncoderFactory encoders;

	public SyncHandler(IBackend backend) {
		this.backend = backend;
		this.decoders = new HashMap<String, IDataDecoder>();
		decoders.put("Contacts", new ContactsDecoder());
		decoders.put("Calendar", new CalendarDecoder());
		this.encoders = new EncoderFactory();
	}

	@Override
	public void process(BackendSession bs, Document doc, Responder responder) {
		logger.info("process(" + bs.getLoginAtDomain() + "/" + bs.getDevType()
				+ ")");

		StateMachine sm = new StateMachine();

		NodeList nl = doc.getDocumentElement().getElementsByTagName(
				"Collection");
		LinkedList<SyncCollection> collections = new LinkedList<SyncCollection>();
		for (int i = 0; i < nl.getLength(); i++) {
			Element col = (Element) nl.item(i);
			collections.add(processCollection(bs, sm, col));
		}

		Document reply = null;
		try {
			reply = DOMUtils.createDoc(null, "Sync");
			Element root = reply.getDocumentElement();
			Element cols = DOMUtils.createElement(root, "Collections");

			for (SyncCollection c : collections) {
				String oldSyncKey = c.getSyncKey();
				SyncState st = sm.getSyncState(oldSyncKey);
				Element ce = DOMUtils.createElement(cols, "Collection");
				DOMUtils.createElementAndText(ce, "Class", c.getDataClass());
				c.setNewSyncKey(sm.getNewSyncKey(c.getSyncKey()));
				DOMUtils.createElementAndText(ce, "SyncKey", c.getNewSyncKey());
				DOMUtils.createElementAndText(ce, "CollectionId", c
						.getCollectionId());
				if (!st.isValid()) {
					DOMUtils.createElementAndText(ce, "Status",
							SyncStatus.INVALID_SYNC_KEY.asXmlValue());
				} else {
					DOMUtils.createElementAndText(ce, "Status", "1");
					if (!oldSyncKey.equals("0")) {
						IContentsExporter cex = backend.getContentsExporter(bs);
						cex.configure(bs, c.getDataClass(), c.getFilterType(), st,
								0, 0);

						List<ItemChange> changed = null;
						if (c.getFetchIds().size() == 0) {
							changed = cex.getChanged(bs, c.getCollectionId());
							logger.info("should send " + changed.size()
									+ " change(s).");
							Element commands = DOMUtils.createElement(ce,
									"Commands");

							for (ItemChange ic : changed) {
								Element add = DOMUtils.createElement(commands,
										"Add");
								DOMUtils.createElementAndText(add, "ServerId",
										ic.getServerId());
								serializeChange(add, c, ic);
							}

							List<ItemChange> del = cex.getDeleted(bs, c.getCollectionId());
							for (ItemChange ic : del) {
								serializeDeletion(ce, ic);
							}
						} else {
							// fetch
							changed = cex.fetch(bs, c.getFetchIds());
							Element commands = DOMUtils.createElement(ce,
									"Responses");
							for (ItemChange ic : changed) {
								Element add = DOMUtils.createElement(commands,
										"Fetch");
								DOMUtils.createElementAndText(add, "ServerId",
										ic.getServerId());
								DOMUtils.createElementAndText(add, "Status",
										"1");
								serializeChange(add, c, ic);
							}

						}
					}
				}
			}

			responder.sendResponse("AirSync", reply);
		} catch (Exception e) {
			logger.error("Error creating Sync response", e);
		}
	}

	private void serializeDeletion(Element commands, ItemChange ic) {
		// TODO Auto-generated method stub

	}

	private void serializeChange(Element col, SyncCollection c, ItemChange ic) {
		IApplicationData data = ic.getData();
		IDataEncoder encoder = encoders.getEncoder(data);
		Element apData = DOMUtils.createElement(col, "ApplicationData");
		encoder.encode(apData, data);
	}

	private SyncCollection processCollection(BackendSession bs,
			StateMachine sm, Element col) {
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
			collection.setCollectionId(Utils.getFolderId(bs.getDevId(),
					collection.getDataClass()));
		}

		collection.setSyncState(sm.getSyncState(collection.getSyncKey()));

		// Element perform = DOMUtils.getUniqueElement(col, "Perform");
		Element perform = DOMUtils.getUniqueElement(col, "Commands");

		if (perform != null) {
			// get our sync state for this collection
			IContentsImporter importer = backend.getContentsImporter(collection
					.getCollectionId(), bs);
			importer.configure(bs, collection.getSyncState(), collection
					.getConflict());
			NodeList mod = perform.getChildNodes();
			for (int j = 0; j < mod.getLength(); j++) {
				Element modification = (Element) mod.item(j);
				processModification(bs, collection, importer, modification);
			}
		}
		return collection;
	}

	/**
	 * Handles modifications requested by mobile device
	 * 
	 * @param bs
	 * @param collection
	 * @param importer
	 * @param modification
	 */
	private void processModification(BackendSession bs, SyncCollection collection,
			IContentsImporter importer, Element modification) {
		String modType = modification.getNodeName();
		String serverId = DOMUtils.getElementText(modification, "ServerId");
		String clientId = DOMUtils.getElementText(modification, "ClientId");
		Element syncData = DOMUtils.getUniqueElement(modification,
				"ApplicationData");
		String dataClass = collection.getDataClass();
		IDataDecoder dd = getDecoder(dataClass);
		IApplicationData data = null;
		if (dd != null) {
			data = dd.decode(syncData);
			if (modType.equals("Modify")) {
				if (data.isRead()) {
					importer.importMessageReadFlag(bs, serverId, data.isRead());
				} else {
					importer.importMessageChange(bs, collection.getCollectionId(), serverId, data);
				}
			} else if (modType.equals("Add") || modType.equals("Change")) {
				String id = importer.importMessageChange(bs, collection.getCollectionId(), serverId, data);
				if (clientId != null && id != null) {
					collection.getClientIds().put(clientId, id);
				}
			} else if (modType.equals("Delete")) {
				if (collection.isDeletesAsMoves()) {
					String trash = backend.getWasteBasket();
					if (trash != null) {
						importer.importMessageMove(bs, serverId, trash);
					}
				} else {
					importer.importMessageDeletion(bs, PIMDataType.valueOf(dataClass), serverId);
				}
			}
		} else {
			logger.info("no decoder for " + dataClass);
			if (modType.equals("Fetch")) {
				logger.info("adding id to fetch " + serverId);
				collection.getFetchIds().add(serverId);
			}
		}
		collection.setSyncState(importer.getState(bs));
	}

	private IDataDecoder getDecoder(String dataClass) {
		return decoders.get(dataClass);
	}
}
