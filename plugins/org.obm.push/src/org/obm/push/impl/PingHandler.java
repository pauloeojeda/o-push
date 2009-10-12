package org.obm.push.impl;

import java.util.HashSet;
import java.util.Set;

import org.obm.push.backend.BackendSession;
import org.obm.push.backend.CollectionChangeListener;
import org.obm.push.backend.IBackend;
import org.obm.push.backend.ICollectionChangeListener;
import org.obm.push.backend.IContinuation;
import org.obm.push.backend.IListenerRegistration;
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
/**
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

		long intervalSeconds = 0;
		if (doc == null) {
			logger
					.info("Empty Ping, reusing cached heartbeat & monitored folders");
			intervalSeconds = bs.getLastHeartbeat();
			// 5sec, why not ? a mobile device asking for <5sec is just stupid
			if (bs.getLastMonitored() == null
					|| bs.getLastMonitored().isEmpty() || intervalSeconds < 5) {
				logger.error("Don't know what to monitor, "
						+ "db table for storing ping params missing..."
						+ "interval: " + intervalSeconds + " toMonitor: "
						+ bs.getLastMonitored(), new RuntimeException());
			}
		} else {
			Element pr = doc.getDocumentElement();

			intervalSeconds = Long.parseLong(DOMUtils.getUniqueElement(pr,
					"HeartbeatInterval").getTextContent());

			Set<SyncCollection> toMonitor = new HashSet<SyncCollection>();
			NodeList folders = pr.getElementsByTagName("Folder");
			for (int i = 0; i < folders.getLength(); i++) {
				Element f = (Element) folders.item(i);
				SyncCollection sc = new SyncCollection();
				sc.setDataClass(DOMUtils.getElementText(f, "Class"));
				int id = Integer.parseInt(DOMUtils.getElementText(f, "Id"));
				sc.setCollectionId(id);
				toMonitor.add(sc);
			}
			// pda is allowed to only send the folder list on the first ping
			if (folders.getLength() > 0) {
				logger.warn("=========== setting monitored to "
						+ toMonitor.size());
				bs.setLastMonitored(toMonitor);
			}
			bs.setLastHeartbeat(intervalSeconds);
		}

		CollectionChangeListener l = new CollectionChangeListener(bs,
				continuation, bs.getLastMonitored());
		IListenerRegistration reg = backend.addChangeListener(l);
		continuation.storeData(ICollectionChangeListener.REG_NAME, reg);
		continuation.storeData(ICollectionChangeListener.LISTENER, l);
		logger.info("suspend for " + intervalSeconds + " seconds");
		synchronized (bs) {
			logger
					.warn("for testing purpose, we will only suspend for 40sec (to monitor: "
							+ bs.getLastMonitored() + ")");
			continuation.suspend(40 * 1000);
			// continuation.suspend(intervalSeconds * 1000);
		}
	}

	public void sendResponse(BackendSession bs, Responder responder,
			Set<SyncCollection> changedFolders) {
		Document ret = DOMUtils.createDoc(null, "Ping");

		fillResponse(ret.getDocumentElement(), changedFolders);
		try {
			responder.sendResponse("Ping", ret);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	private void fillResponse(Element element,
			Set<SyncCollection> changedFolders) {
		if (changedFolders == null || changedFolders.isEmpty()) {
			DOMUtils.createElementAndText(element, "Status",
					PingStatus.NO_CHANGES.toString());
		} else {
			DOMUtils.createElementAndText(element, "Status",
					PingStatus.CHANGES_OCCURED.toString());
			Element folders = DOMUtils.createElement(element, "Folders");
			for (SyncCollection sc : changedFolders) {
				DOMUtils.createElementAndText(folders, "Folder", sc
						.getCollectionId().toString());
			}
		}
	}

}
