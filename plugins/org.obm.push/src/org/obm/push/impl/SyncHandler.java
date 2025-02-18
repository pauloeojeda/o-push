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

import org.mortbay.jetty.RetryRequest;
import org.obm.push.ActiveSyncServlet;
import org.obm.push.backend.BackendSession;
import org.obm.push.backend.BodyPreference;
import org.obm.push.backend.CollectionChangeListener;
import org.obm.push.backend.DataDelta;
import org.obm.push.backend.FilterType;
import org.obm.push.backend.IApplicationData;
import org.obm.push.backend.IBackend;
import org.obm.push.backend.IContentsExporter;
import org.obm.push.backend.IContentsImporter;
import org.obm.push.backend.IContinuation;
import org.obm.push.backend.IListenerRegistration;
import org.obm.push.backend.ItemChange;
import org.obm.push.backend.MSEmailBodyType;
import org.obm.push.backend.PIMDataType;
import org.obm.push.backend.SyncCollection;
import org.obm.push.data.IDataDecoder;
import org.obm.push.data.IDataEncoder;
import org.obm.push.exception.ActiveSyncException;
import org.obm.push.exception.CollectionNotFoundException;
import org.obm.push.exception.ObjectNotFoundException;
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
public class SyncHandler extends WbxmlRequestHandler implements
		IContinuationHandler {

	public static final Integer SYNC_TRUNCATION_ALL = 9;
	private static Map<Integer, IContinuation> waitContinuationCache;

	static {
		waitContinuationCache = new HashMap<Integer, IContinuation>();
	}

	public SyncHandler(IBackend backend) {
		super(backend);
	}

	@Override
	public void process(IContinuation continuation, BackendSession bs,
			Document doc, ActiveSyncRequest request, Responder responder) {
		logger.info("process(" + bs.getLoginAtDomain() + "/" + bs.getDevType()
				+ ")");
		try {
			StateMachine sm = new StateMachine(backend.getStore());

			Set<SyncCollection> collections = new HashSet<SyncCollection>();
			Map<String, String> processedClientIds = new HashMap<String, String>();
			String wait = "";
			if (doc != null) {
				Element query = doc.getDocumentElement();
				NodeList nl = query.getElementsByTagName("Collection");

				for (int i = 0; i < nl.getLength(); i++) {
					Element col = (Element) nl.item(i);
					SyncCollection collec = processCollection(bs, sm, col,
							processedClientIds);
					collections.add(collec);

				}
				bs.setLastSyncProcessedClientIds(processedClientIds);
				wait = DOMUtils.getElementText(query, "Wait");
				if (query.getElementsByTagName("Partial").getLength() > 0) {
					logger.info("Partial element has been found. "
							+ bs.getLastMonitored().size()
							+ " collection(s) are loaded from cache");
					if (bs.getLastMonitored() == null
							|| bs.getLastMonitored().size() == 0) {
						try {
							Document reply = null;
							reply = DOMUtils.createDoc(null, "Sync");
							Element root = reply.getDocumentElement();
							// Status 13
							// The client sent an empty or partial Sync request,
							// but the server is unable to process it. Please
							// resend the request with the full XML
							// TODO Cache in database last collections monitored
							DOMUtils.createElementAndText(root, "Status",
									SyncStatus.PARTIAL_REQUEST.asXmlValue());
							responder.sendResponse("AirSync", reply);
						} catch (Exception e) {
							logger.error("Error creating Sync response", e);
						}
						return;
					}
					collections.addAll(bs.getLastMonitored());
				}
			}

			if ((wait != null && wait.length() > 0) || doc == null) {
				if (collections.size() == 0) {
					logger.info(bs.getLastMonitored().size()
							+ " collection(s) are loaded from cache");
					collections.addAll(bs.getLastMonitored());
				}

				int secs = 0;
				try {
					secs = Integer.parseInt(wait) * 60;
				} catch (NumberFormatException e) {
				}
				if (secs == 0) {
					secs = bs.getLastWait();
				}

				// 59*60
				if (secs > 3540) {
					try {
						Document reply = null;
						reply = DOMUtils.createDoc(null, "Sync");
						Element root = reply.getDocumentElement();
						DOMUtils.createElementAndText(root, "Status",
								SyncStatus.WAIT_INTERVAL_OUT_OF_RANGE
										.asXmlValue());
						DOMUtils.createElementAndText(root, "Limit", "59");
						responder.sendResponse("AirSync", reply);
					} catch (Exception e) {
						logger.error("Error creating Sync response", e);
					}
					return;
				}
				logger.info("suspend for " + secs + "sec.");
				for (SyncCollection sc : collections) {
					String collectionPath = backend.getStore()
							.getCollectionPath(sc.getCollectionId());
					sc.setCollectionPath(collectionPath);
					PIMDataType dataClass = backend.getStore().getDataClass(
							collectionPath);
					// sc.setDataClass(dataClass) causes errors with wm6.1
					if ("email".equalsIgnoreCase(dataClass.toString())) {
						backend.startEmailMonitoring(bs, sc.getCollectionId());
						break;
					}
				}
				bs.setLastContinuationHandler(ActiveSyncServlet.SYNC_HANDLER);
				bs.setLastMonitored(collections);
				bs.setLastWait(secs);
				CollectionChangeListener l = new CollectionChangeListener(bs,
						continuation, collections);
				IListenerRegistration reg = backend.addChangeListener(l);
				continuation.setListenerRegistration(reg);
				continuation.setCollectionChangeListener(l);
				for (SyncCollection sc : collections) {
					waitContinuationCache.put(sc.getCollectionId(),
							continuation);
				}

				logger.info("suspend for " + secs + " seconds");
				synchronized (bs) {
					// logger
					// .warn("for testing purpose, we will only suspend for 40sec (to monitor: "
					// + bs.getLastMonitored() + ")");
					// continuation.suspend(10 * 1000);
					continuation.suspend(secs * 1000);
				}
			} else {
				processResponse(bs, responder, collections, false,
						processedClientIds, continuation);
			}
		} catch (CollectionNotFoundException ce) {
			sendError(responder, new HashSet<SyncCollection>(),
					SyncStatus.OBJECT_NOT_FOUND.asXmlValue(), continuation);
		} catch (ObjectNotFoundException oe) {
			sendError(responder, new HashSet<SyncCollection>(),
					SyncStatus.OBJECT_NOT_FOUND.asXmlValue(), continuation);
		} catch (ActiveSyncException e) {
			// sendError(responder, new HashSet<SyncCollection>(),
			// SyncStatus.SERVER_ERROR.asXmlValue(), continuation);
			logger.error(e.getMessage(), e);

		} catch (RetryRequest e) {
			// used by org.mortbay.util.ajax.Continuation
			throw e;
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
		}
	}

	private void doUpdates(BackendSession bs, SyncCollection c, Element ce,
			IContentsExporter cex, Map<String, String> processedClientIds)
			throws ActiveSyncException {
		DataDelta delta = null;
		if (bs.getUnSynchronizedItemChange(c.getCollectionId()).size() == 0) {
			delta = cex.getChanged(bs, c.getSyncState(), c.getFilterType(),
					c.getCollectionId());
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

		Set<ItemChange> unSyncdeleted = bs.getUnSynchronizedDeletedItemChange(c
				.getCollectionId());
		if (delta != null && delta.getDeletions() != null
				&& unSyncdeleted != null) {
			delta.getDeletions().addAll(unSyncdeleted);
			unSyncdeleted.clear();
		}

		if (delta != null && delta.getDeletions() != null) {
			for (ItemChange ic : delta.getDeletions()) {
				if (processedClientIds.containsKey(ic.getServerId())) {
					// changed.add(ic);
					bs.addUnSynchronizedDeletedItemChange(c.getCollectionId(),
							ic);
				} else {
					serializeDeletion(commands, ic);
				}
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
				DOMUtils.createElementAndText(add, "ServerId", ic.getServerId());
				DOMUtils.createElementAndText(add, "Status",
						SyncStatus.OK.asXmlValue());
			} else if (processedClientIds.keySet().contains(ic.getServerId())) {
				// Change asked by device
				Element add = DOMUtils.createElement(responses, "Change");
				DOMUtils.createElementAndText(add, "ServerId", ic.getServerId());
				DOMUtils.createElementAndText(add, "Status",
						SyncStatus.OK.asXmlValue());
			} else {
				// New change done on server
				Element add = DOMUtils.createElement(commands, "Add");
				DOMUtils.createElementAndText(add, "ServerId", ic.getServerId());
				serializeChange(bs, add, c, ic);
			}
			processedClientIds.remove(ic.getServerId());
		}

		// Send error for the remaining entry in the Map because the
		// client has requested the addition of a resource that already exists
		// on the server
		for (Entry<String, String> entry : processedClientIds.entrySet()) {
			if (entry != null
					&& entry.getKey() != null
					&& entry.getKey()
							.startsWith(c.getCollectionId().toString())) {
				Element add = DOMUtils.createElement(responses, "Add");
				DOMUtils.createElementAndText(add, "ServerId", entry.getKey());
				if (entry.getValue() != null) {
					DOMUtils.createElementAndText(add, "ClientId",
							entry.getValue());
				}
				// need send ok since we do not synchronize event with
				// ParticipationState need-action
				DOMUtils.createElementAndText(add, "Status",
						SyncStatus.OK.asXmlValue());
				// DOMUtils.createElementAndText(add, "Status",
				// SyncStatus.CONVERSATION_ERROR.asXmlValue());
			}
			processedClientIds.remove(entry.getKey());
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
			Map<String, String> processedClientIds) {
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
			IContentsExporter cex) throws ActiveSyncException {
		List<ItemChange> changed;
		changed = cex
				.fetch(bs, c.getSyncState().getDataType(), c.getFetchIds());
		Element commands = DOMUtils.createElement(ce, "Responses");
		for (ItemChange ic : changed) {
			Element add = DOMUtils.createElement(commands, "Fetch");
			DOMUtils.createElementAndText(add, "ServerId", ic.getServerId());
			DOMUtils.createElementAndText(add, "Status",
					SyncStatus.OK.asXmlValue());
			c.setTruncation(null);
			for (BodyPreference bp : c.getBodyPreferences().values()) {
				bp.setTruncationSize(null);
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
		IDataEncoder encoder = getEncoders().getEncoder(data,
				bs.getProtocolVersion());
		Element apData = DOMUtils.createElement(col, "ApplicationData");
		encoder.encode(bs, apData, data, c, true);
	}

	private SyncCollection decodeCollection(Element col)
			throws CollectionNotFoundException {
		SyncCollection collection = new SyncCollection();
		collection.setDataClass(DOMUtils.getElementText(col, "Class"));
		collection.setSyncKey(DOMUtils.getElementText(col, "SyncKey"));
		Element fid = DOMUtils.getUniqueElement(col, "CollectionId");
		if (fid != null) {
			try {
				collection.setCollectionId(Integer.parseInt(fid
						.getTextContent()));
			} catch (NumberFormatException e) {
				throw new CollectionNotFoundException();
			}

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
			NodeList bodyPreferences = col
					.getElementsByTagName("BodyPreference");

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

			if (bodyPreferences != null) {
				for (int i = 0; i < bodyPreferences.getLength(); i++) {
					Element bodyPreference = (Element) bodyPreferences.item(i);
					String truncationSize = DOMUtils.getElementText(
							bodyPreference, "TruncationSize");
					String type = DOMUtils.getElementText(bodyPreference,
							"Type");
					BodyPreference bp = new BodyPreference();
					// nokia n900 sets type without truncationsize
					if (truncationSize != null) {
						bp.setTruncationSize(Integer.parseInt(truncationSize));
					}
					bp.setType(MSEmailBodyType.getValueOf(Integer
							.parseInt(type)));
					collection.addBodyPreference(bp);
				}
			}
		}
		// TODO sync supported
		// TODO sync <deletesasmoves/>
		// TODO sync <getchanges/>
		// TODO sync options

		return collection;
	}

	private SyncCollection processCollection(BackendSession bs,
			StateMachine sm, Element col, Map<String, String> processedClientIds)
			throws ActiveSyncException {
		SyncCollection collection = decodeCollection(col);
		SyncState colState = sm.getSyncState(collection.getCollectionId(),
				collection.getSyncKey());
		collection.setSyncState(colState);

		// Disables last push request
		IContinuation cont = waitContinuationCache.get(collection
				.getCollectionId());
		if (cont != null) {
			cont.error(SyncStatus.NEED_RETRY.asXmlValue());
		}

		if (colState.isValid()) {
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
	 * @throws ActiveSyncException
	 */
	private void processModification(BackendSession bs,
			SyncCollection collection, IContentsImporter importer,
			Element modification, Map<String, String> processedClientIds)
			throws ActiveSyncException {
		Integer collectionId = collection.getCollectionId();
		String collectionPath = backend.getStore().getCollectionPath(
				collectionId);
		String modType = modification.getNodeName();
		logger.info("modType: " + modType);
		String serverId = DOMUtils.getElementText(modification, "ServerId");
		String clientId = DOMUtils.getElementText(modification, "ClientId");

		Element syncData = DOMUtils.getUniqueElement(modification,
				"ApplicationData");
		PIMDataType dataClass = backend.getStore().getDataClass(collectionPath);
		IDataDecoder dd = getDecoder(dataClass);
		IApplicationData data = null;
		if (dd != null) {
			if (syncData != null) {
				data = dd.decode(syncData);
			}
			if (modType.equals("Modify")) {
				importer.importMessageChange(bs, collectionId, serverId,
						clientId, data);
			} else if (modType.equals("Add") || modType.equals("Change")) {
				logger.info("processing Add/Change (srv: " + serverId
						+ ", cli:" + clientId + ")");

				String obmId = importer.importMessageChange(bs, collectionId,
						serverId, clientId, data);
				if (obmId != null) {
					if (clientId != null) {
						processedClientIds.put(obmId, clientId);
					}
					if (serverId != null) {
						processedClientIds.put(obmId, null);
					}
				}
			} else if (modType.equals("Delete")) {
				importer.importMessageDeletion(bs, dataClass, collectionPath,
						serverId, collection.isDeletesAsMoves());
			}
		} else {
			logger.error("no decoder for " + dataClass);
			if (modType.equals("Fetch")) {
				logger.info("adding id to fetch " + serverId);
				collection.getFetchIds().add(serverId);
			}
		}
	}

	@Override
	public void sendResponse(BackendSession bs, Responder responder,
			Set<SyncCollection> changedFolders, boolean sendHierarchyChange,
			IContinuation continuation) {
		Map<String, String> processedClientIds = new HashMap<String, String>(
				bs.getLastSyncProcessedClientIds());
		bs.setLastSyncProcessedClientIds(new HashMap<String, String>());
		processResponse(bs, responder, bs.getLastMonitored(),
				sendHierarchyChange, processedClientIds, continuation);

	}

	public void processResponse(BackendSession bs, Responder responder,
			Set<SyncCollection> changedFolders, boolean sendHierarchyChange,
			Map<String, String> processedClientIds, IContinuation continuation) {

		StateMachine sm = new StateMachine(backend.getStore());
		Document reply = null;
		try {
			reply = DOMUtils.createDoc(null, "Sync");
			Element root = reply.getDocumentElement();

			Element cols = DOMUtils.createElement(root, "Collections");

			for (SyncCollection c : changedFolders) {
				Element ce = DOMUtils.createElement(cols, "Collection");
				try {
					if ("0".equals(c.getSyncKey())) {
						backend.resetCollection(bs, c.getCollectionId());
					}

					String syncKey = c.getSyncKey();
					SyncState st = sm
							.getSyncState(c.getCollectionId(), syncKey);

					SyncState oldClientSyncKey = bs.getLastClientSyncState(c
							.getCollectionId());
					if (oldClientSyncKey != null
							&& oldClientSyncKey.getKey().equals(syncKey)) {
						st.setLastSync(oldClientSyncKey.getLastSync());
					}

					if (c.getDataClass() != null) {
						DOMUtils.createElementAndText(ce, "Class",
								c.getDataClass());
					}

					if (!st.isValid()) {
						DOMUtils.createElementAndText(ce, "CollectionId", c
								.getCollectionId().toString());
						DOMUtils.createElementAndText(ce, "Status",
								SyncStatus.INVALID_SYNC_KEY.asXmlValue());
						DOMUtils.createElementAndText(ce, "SyncKey", "0");
					} else {
						Element sk = DOMUtils.createElement(ce, "SyncKey");
						DOMUtils.createElementAndText(ce, "CollectionId", c
								.getCollectionId().toString());
						DOMUtils.createElementAndText(ce, "Status",
								SyncStatus.OK.asXmlValue());

						if (!syncKey.equals("0")) {
							IContentsExporter cex = backend
									.getContentsExporter(bs);

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
				} catch (CollectionNotFoundException e) {
					sendError(responder, new HashSet<SyncCollection>(),
							SyncStatus.OBJECT_NOT_FOUND.asXmlValue(),
							continuation);
				}
			}
			logger.info("Resp for requestId: " + continuation.getReqId());
			responder.sendResponse("AirSync", reply);
		} catch (Exception e) {
			logger.error("Error creating Sync response", e);
		}
	}

	@Override
	public void sendError(Responder responder,
			Set<SyncCollection> changedFolders, String errorStatus,
			IContinuation continuation) {
		Document ret = DOMUtils.createDoc(null, "Sync");
		Element root = ret.getDocumentElement();
		DOMUtils.createElementAndText(root, "Status", errorStatus.toString());

		try {
			logger.info("Resp for requestId: " + continuation.getReqId());
			responder.sendResponse("AirSync", ret);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

	}
}
