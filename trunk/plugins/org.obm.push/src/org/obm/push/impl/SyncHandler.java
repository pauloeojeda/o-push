package org.obm.push.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.obm.push.backend.BackendSession;
import org.obm.push.backend.BodyPreference;
import org.obm.push.backend.DataDelta;
import org.obm.push.backend.FilterType;
import org.obm.push.backend.IApplicationData;
import org.obm.push.backend.IBackend;
import org.obm.push.backend.IContentsExporter;
import org.obm.push.backend.IContentsImporter;
import org.obm.push.backend.IContinuation;
import org.obm.push.backend.ItemChange;
import org.obm.push.backend.MSEmailBodyType;
import org.obm.push.backend.PIMDataType;
import org.obm.push.backend.SyncCollection;
import org.obm.push.data.CalendarDecoder;
import org.obm.push.data.ContactsDecoder;
import org.obm.push.data.EncoderFactory;
import org.obm.push.data.IDataDecoder;
import org.obm.push.data.IDataEncoder;
import org.obm.push.data.EmailDecoder;
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

public class SyncHandler extends WbxmlRequestHandler {

	public static final Integer SYNC_TRUNCATION_ALL = 9;

	private Map<String, IDataDecoder> decoders;

	private EncoderFactory encoders;

	public SyncHandler(IBackend backend) {
		super(backend);
		this.decoders = new HashMap<String, IDataDecoder>();
		decoders.put("Contacts", new ContactsDecoder());
		decoders.put("Calendar", new CalendarDecoder());
		decoders.put("Email", new EmailDecoder());
		this.encoders = new EncoderFactory();
	}

	@Override
	public void process(IContinuation continuation, BackendSession bs,
			Document doc, Responder responder) {
		logger.info("process(" + bs.getLoginAtDomain() + "/" + bs.getDevType()
				+ ")");

		StateMachine sm = new StateMachine(backend.getStore());

		NodeList nl = doc.getDocumentElement().getElementsByTagName(
				"Collection");
		LinkedList<SyncCollection> collections = new LinkedList<SyncCollection>();
		HashMap<String, String> processedClientIds = new HashMap<String, String>();
		for (int i = 0; i < nl.getLength(); i++) {
			Element col = (Element) nl.item(i);
			collections.add(processCollection(bs, sm, col, processedClientIds));
		}

		Document reply = null;
		try {
			reply = DOMUtils.createDoc(null, "Sync");
			Element root = reply.getDocumentElement();

			Element cols = DOMUtils.createElement(root, "Collections");

			for (SyncCollection c : collections) {
				if ("0".equals(c.getSyncKey())) {
					backend.resetCollection(bs, c.getCollectionId());
					bs.setState(new SyncState());
				}

				String syncKey = c.getSyncKey();
				SyncState st = sm.getSyncState(syncKey);

				SyncState oldClientSyncKey = bs.getLastClientSyncState(c
						.getCollectionId());
				if (oldClientSyncKey != null
						&& oldClientSyncKey.getKey().equals(syncKey)) {
					st.setLastSync(oldClientSyncKey.getLastSync());
				}

				Element ce = DOMUtils.createElement(cols, "Collection");
				if (c.getDataClass() != null) {
					DOMUtils
							.createElementAndText(ce, "Class", c.getDataClass());
				}

				if (!st.isValid()) {
					// invalid = true;
					DOMUtils.createElementAndText(ce, "CollectionId", c
							.getCollectionId().toString());
					DOMUtils.createElementAndText(ce, "Status",
							SyncStatus.INVALID_SYNC_KEY.asXmlValue());
					break;
				}

				Element sk = DOMUtils.createElement(ce, "SyncKey");
				DOMUtils.createElementAndText(ce, "CollectionId", c
						.getCollectionId().toString());
				DOMUtils.createElementAndText(ce, "Status", "1");
				if (!syncKey.equals("0")) {
					int col = c.getCollectionId();
					String colStr = backend.getStore().getCollectionString(col);
					IContentsExporter cex = backend.getContentsExporter(bs);
					cex.configure(bs, c.getDataClass(), c.getFilterType(), st,
							colStr);

					if (c.getFetchIds().size() == 0) {
						doUpdates(bs, c, ce, cex, processedClientIds);
					} else {
						// fetch
						doFetch(bs, c, ce, cex);
					}
				}
				bs.addLastClientSyncState(c.getCollectionId(), st);
				sk.setTextContent(sm.allocateNewSyncKey(bs,
						c.getCollectionId(), st));

			}
			responder.sendResponse("AirSync", reply);
		} catch (Exception e) {
			logger.error("Error creating Sync response", e);
		}
	}

	private void doUpdates(BackendSession bs, SyncCollection c, Element ce,
			IContentsExporter cex, HashMap<String, String> processedClientIds) {
		String col = backend.getStore()
				.getCollectionString(c.getCollectionId());
		DataDelta delta = null;
		if (bs.getUnSynchronizedItemChange(c.getCollectionId()).size() == 0) {
			delta = cex.getChanged(bs, c.getFilterType(), col);
		}
		List<ItemChange> changed = processWindowSize(c, delta, bs,
				processedClientIds);

		Element responses = DOMUtils.createElement(ce, "Responses");
		if (c.isMoreAvailable()) {
			// MoreAvailable has to be before Commands
			DOMUtils.createElement(ce, "MoreAvailable");
		}
		Element commands = DOMUtils.createElement(ce, "Commands");

		StringBuilder processedIds = new StringBuilder();
		for (Entry<String, String> k : processedClientIds.entrySet()) {
			processedIds.append(' ');
			if (k.getValue() != null) {
				processedIds.append(k.getValue());
			} else {
				processedIds.append(k.getValue());
			}
		}

		for (ItemChange ic : changed) {
			String clientId = processedClientIds.get(ic.getServerId());
			logger.info("processedIds:" + processedIds.toString()
					+ " ic.clientId: " + clientId + " ic.serverId: "
					+ ic.getServerId());

			if (clientId != null) {
				// Acks Add done by pda
				Element add = DOMUtils.createElement(responses, "Add");
				DOMUtils.createElementAndText(add, "ClientId", clientId);
				DOMUtils
						.createElementAndText(add, "ServerId", ic.getServerId());
				DOMUtils.createElementAndText(add, "Status", "1");
			} else if (processedClientIds.keySet().contains(ic.getServerId())) {
				// Change asked by device
				Element add = DOMUtils.createElement(responses, "Change");
				DOMUtils
						.createElementAndText(add, "ServerId", ic.getServerId());
				DOMUtils.createElementAndText(add, "Status", "1");
			} else {
				// New change done on server
				Element add = DOMUtils.createElement(commands, "Add");
				DOMUtils
						.createElementAndText(add, "ServerId", ic.getServerId());
				serializeChange(bs, add, c, ic);
			}
		}
		if (delta != null) {
			List<ItemChange> del = delta.getDeletions();
			for (ItemChange ic : del) {
				serializeDeletion(commands, ic);
			}
		}
		if (responses.getChildNodes().getLength() == 0) {
			responses.getParentNode().removeChild(responses);
		}
		if (commands.getChildNodes().getLength() == 0) {
			commands.getParentNode().removeChild(commands);
		}

	}

	private List<ItemChange> processWindowSize(SyncCollection c,
			DataDelta delta, BackendSession bs,
			HashMap<String, String> processedClientIds) {
		List<ItemChange> changed = new ArrayList<ItemChange>();
		if (delta != null) {
			changed.addAll(delta.getChanges());
		}
		changed.addAll(bs.getUnSynchronizedItemChange(c.getCollectionId()));

		bs.getUnSynchronizedItemChange(c.getCollectionId()).clear();
		logger.info("should send " + changed.size() + " change(s)");
		int changeItem = changed.size() - c.getWindowSize();
		logger.info("WindowsSize value is " + c.getWindowSize() + ", "
				+ (changeItem < 0 ? 0 : changeItem) + " changes will not send");

		if (changed.size() <= c.getWindowSize()) {
			return changed;
		}

		int nbChangeByMobile = processedClientIds.size();
		Set<ItemChange> changeByMobile = new HashSet<ItemChange>();
		// Find changes ask by the device
		for (Iterator<ItemChange> it = changed.iterator(); it.hasNext();) {
			ItemChange ic = it.next();
			if (processedClientIds.get(ic.getServerId()) != null
					|| processedClientIds.keySet().contains(ic.getServerId())) {
				changeByMobile.add(ic);
				it.remove();
			}
			if (nbChangeByMobile == changeByMobile.size()) {
				break;
			}
		}

		int changedSize = changed.size();
		for (int i = c.getWindowSize(); i < changedSize; i++) {
			ItemChange ic = changed.get(changed.size() - 1);
			bs.addUnSynchronizedItemChange(c.getCollectionId(), ic);
			changed.remove(ic);
		}

		changed.addAll(changeByMobile);
		c.setMoreAvailable(true);
		return changed;
	}

	private void doFetch(BackendSession bs, SyncCollection c, Element ce,
			IContentsExporter cex) {
		List<ItemChange> changed;
		changed = cex.fetch(bs, c.getFetchIds());
		Element commands = DOMUtils.createElement(ce, "Responses");
		for (ItemChange ic : changed) {
			Element add = DOMUtils.createElement(commands, "Fetch");
			DOMUtils.createElementAndText(add, "ServerId", ic.getServerId());
			DOMUtils.createElementAndText(add, "Status", "1");
			c.setTruncation(null);
			if (c.getBodyPreference() != null) {
				c.getBodyPreference().setTruncationSize(null);
			}
			serializeChange(bs, add, c, ic);
		}
	}

	private void serializeDeletion(Element commands, ItemChange ic) {
		Element del = DOMUtils.createElement(commands, "Delete");
		DOMUtils.createElementAndText(del, "ServerId", ic.getServerId());
	}

	private void serializeChange(BackendSession bs, Element col,
			SyncCollection c, ItemChange ic) {
		IApplicationData data = ic.getData();
		IDataEncoder encoder = encoders.getEncoder(data, bs
				.getProtocolVersion());
		Element apData = DOMUtils.createElement(col, "ApplicationData");
		encoder.encode(bs, apData, data, c, true);
	}

	private SyncCollection processCollection(BackendSession bs,
			StateMachine sm, Element col,
			HashMap<String, String> processedClientIds) {
		SyncCollection collection = new SyncCollection();
		collection.setDataClass(DOMUtils.getElementText(col, "Class"));
		collection.setSyncKey(DOMUtils.getElementText(col, "SyncKey"));
		Element fid = DOMUtils.getUniqueElement(col, "CollectionId");
		if (fid != null) {
			collection.setCollectionId(Integer.parseInt(fid.getTextContent()));
		}

		Element wse = DOMUtils.getUniqueElement(col, "WindowSize");
		if (wse != null) {
			collection.setWindowSize(Integer.parseInt(wse.getTextContent()));
		}

		Element option = DOMUtils.getUniqueElement(col, "Options");
		if (option != null) {
			String filterType = DOMUtils.getElementText(option, "FilterType");
			String truncation = DOMUtils.getElementText(option, "Truncation");

			String mimeSupport = DOMUtils.getElementText(option, "MIMESupport");
			String mimeTruncation = DOMUtils.getElementText(option,
					"MIMETruncation");
			String conflict = DOMUtils.getElementText(option, "Conflict");
			Element bodyPreference = DOMUtils.getUniqueElement(col,
					"BodyPreference");

			if (conflict != null) {
				collection.setConflict(Integer.parseInt(conflict));
			}
			if (filterType != null) {
				collection.setFilterType(FilterType.getFilterType(filterType));
			}
			if (mimeSupport != null) {
				collection.setMimeSupport(Integer.parseInt(mimeSupport));
			}
			if (mimeTruncation != null) {
				collection.setMimeTruncation(Integer.parseInt(mimeTruncation));
			}
			if (truncation != null) {
				collection.setTruncation(Integer.parseInt(truncation));
			}

			if (bodyPreference != null) {
				String truncationSize = DOMUtils.getElementText(bodyPreference,
						"TruncationSize");
				String type = DOMUtils.getElementText(bodyPreference, "Type");
				BodyPreference bp = new BodyPreference();
				bp.setTruncationSize(Integer.parseInt(truncationSize));
				bp.setType(MSEmailBodyType.getValueOf(Integer.parseInt(type)));
				collection.setBodyPreference(bp);
			}

		}
		// TODO sync supported
		// TODO sync <deletesasmoves/>
		// TODO sync <getchanges/>
		// TODO sync options

		SyncState oldColState = sm.getSyncState(collection.getSyncKey());
		collection.setSyncState(oldColState);
		if (oldColState.isValid()) {
			Element perform = DOMUtils.getUniqueElement(col, "Commands");

			if (perform != null) {
				NodeList fetchs = perform.getElementsByTagName("Fetch");
				List<String> fetchIds = new LinkedList<String>();
				for (int i = 0; i < fetchs.getLength(); i++) {
					Element fetch = (Element) fetchs.item(i);
					fetchIds.add(DOMUtils.getElementText(fetch, "ServerId"));
				}
				collection.setFetchIds(fetchIds);
				// get our sync state for this collection
				IContentsImporter importer = backend.getContentsImporter(
						collection.getCollectionId(), bs);
				importer.configure(bs, collection.getSyncState(), collection
						.getConflict());
				NodeList mod = perform.getChildNodes();
				for (int j = 0; j < mod.getLength(); j++) {
					Element modification = (Element) mod.item(j);
					processModification(bs, collection, importer, modification,
							processedClientIds);
				}
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
	 * @param processedClientIds
	 */
	private void processModification(BackendSession bs,
			SyncCollection collection, IContentsImporter importer,
			Element modification, HashMap<String, String> processedClientIds) {
		int col = collection.getCollectionId();
		String collectionId = backend.getStore().getCollectionString(col);
		String modType = modification.getNodeName();
		logger.info("modType: " + modType);
		String serverId = DOMUtils.getElementText(modification, "ServerId");
		String clientId = DOMUtils.getElementText(modification, "ClientId");

		Element syncData = DOMUtils.getUniqueElement(modification,
				"ApplicationData");
		String dataClass = backend.getStore().getDataClass(collectionId);
		IDataDecoder dd = getDecoder(dataClass);
		IApplicationData data = null;
		if (dd != null) {
			if (syncData != null) {
				data = dd.decode(syncData);
			}
			if (modType.equals("Modify")) {
				if (data.isRead()) {
					importer.importMessageReadFlag(bs, serverId, data.isRead());
				} else {
					importer.importMessageChange(bs, collectionId, serverId,
							clientId, data);
				}
			} else if (modType.equals("Add") || modType.equals("Change")) {
				logger.info("processing Add/Change (srv: " + serverId
						+ ", cli:" + clientId + ")");

				String obmId = importer.importMessageChange(bs, collectionId,
						serverId, clientId, data);
				if (clientId != null) {
					processedClientIds.put(obmId, clientId);
				}
				if (serverId != null) {
					processedClientIds.put(obmId, null);
				}
			} else if (modType.equals("Delete")) {
				if (collection.isDeletesAsMoves()) {
					String trash = backend.getWasteBasket();
					if (trash != null) {
						importer.importMessageMove(bs, serverId, trash);
					}
				} else {
					importer.importMessageDeletion(bs, PIMDataType
							.valueOf(dataClass.toUpperCase()), collectionId,
							serverId);
				}
			}
		} else {
			logger.error("no decoder for " + dataClass);
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
