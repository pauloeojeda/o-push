package org.obm.push.impl;

import java.util.LinkedList;
import java.util.List;

import org.obm.push.backend.BackendSession;
import org.obm.push.backend.FilterType;
import org.obm.push.backend.IBackend;
import org.obm.push.backend.IContentsExporter;
import org.obm.push.backend.IContinuation;
import org.obm.push.backend.SyncCollection;
import org.obm.push.state.StateMachine;
import org.obm.push.state.SyncState;
import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class GetItemEstimateHandler extends WbxmlRequestHandler {

	public GetItemEstimateHandler(IBackend backend) {
		super(backend);
	}

	@Override
	public void process(IContinuation continuation, BackendSession bs,
			Document doc, Responder responder) {
		logger.info("process(" + bs.getLoginAtDomain() + "/" + bs.getDevType()
				+ ")");

		List<SyncCollection> cols = new LinkedList<SyncCollection>();

		NodeList collections = doc.getDocumentElement().getElementsByTagName(
				"Collection");
		for (int i = 0; i < collections.getLength(); i++) {
			Element ce = (Element) collections.item(i);
			String dataClass = DOMUtils.getElementText(ce, "Class");
			String filterType = DOMUtils.getElementText(ce, "FilterType");
			String syncKey = DOMUtils.getElementText(ce, "SyncKey");
			Element fid = DOMUtils.getUniqueElement(ce, "CollectionId");
			String collectionId = null;
			if (fid == null) {
				collectionId = Utils.getFolderId(bs.getDevId(), dataClass);
			} else {
				collectionId = fid.getTextContent();
			}
			SyncCollection sc = new SyncCollection();

			sc.setDataClass(dataClass);
			sc.setSyncKey(syncKey);
			sc.setCollectionId(Integer.parseInt(collectionId));
			sc.setFilterType(FilterType.getFilterType(filterType));
			cols.add(sc);
		}

		try {
			Document rep = DOMUtils.createDoc(null, "GetItemEstimate");
			Element root = rep.getDocumentElement();
			for (SyncCollection c : cols) {
				String collectionId = null;
				int col = -1;
				try {
					col = c.getCollectionId();
					collectionId = backend.getStore().getCollectionString(col);
				} catch (NumberFormatException nfe) {
				}
				Element response = DOMUtils.createElement(root, "Response");
				if (collectionId != null) {
					DOMUtils.createElementAndText(response, "Status", "1");
					Element ce = DOMUtils.createElement(response, "Collection");
					if (c.getDataClass() != null) {
						DOMUtils.createElementAndText(ce, "Class", c
								.getDataClass());
					}
					DOMUtils.createElementAndText(ce, "CollectionId", c
							.getCollectionId().toString());
					Element estim = DOMUtils.createElement(ce, "Estimate");
					StateMachine sm = new StateMachine(backend.getStore());
					SyncState state = sm.getSyncState(c.getSyncKey());
					IContentsExporter exporter = backend
							.getContentsExporter(bs);
					exporter.configure(bs, c.getDataClass(), c.getFilterType(),
							state, collectionId);
					int count = exporter.getCount(bs,c.getFilterType(), collectionId)
							+ bs.getUnSynchronizedItemChange(
									c.getCollectionId()).size();
					estim.setTextContent(count+"");
				} else {
					logger.warn("no mapping for collection with id "
							+ c.getCollectionId());
					// one collection id was invalid
					DOMUtils.createElementAndText(response, "Status", "2");
					break;
				}
			}
			responder.sendResponse("ItemEstimate", rep);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
}