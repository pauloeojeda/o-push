package org.obm.push.impl;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.push.backend.BackendSession;
import org.obm.push.backend.IBackend;
import org.obm.push.backend.IContentsExporter;
import org.obm.push.state.StateMachine;
import org.obm.push.state.SyncState;
import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class GetItemEstimateHandler implements IRequestHandler {

	private static final Log logger = LogFactory
			.getLog(GetItemEstimateHandler.class);

	private IBackend backend;

	public GetItemEstimateHandler(IBackend backend) {
		this.backend = backend;
	}

	@Override
	public void process(BackendSession bs, Document doc, Responder responder) {
		logger.info("process(" + bs.getLoginAtDomain() + "/" + bs.getDevType() + ")");

		List<SyncCollection> cols = new LinkedList<SyncCollection>();

		NodeList collections = doc.getDocumentElement().getElementsByTagName(
				"Collection");
		for (int i = 0; i < collections.getLength(); i++) {
			Element ce = (Element) collections.item(i);
			String dataClass = DOMUtils.getElementText(ce, "Class");
			Integer filterType = Integer.parseInt(DOMUtils.getElementText(ce,
					"FilterType"));
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
			sc.setCollectionId(collectionId);
			sc.setFilterType(filterType);
			cols.add(sc);
		}

		try {
			Document rep = DOMUtils.createDoc(null, "GetItemEstimate");
			Element root = rep.getDocumentElement();
			for (SyncCollection c : cols) {
				Element response = DOMUtils.createElement(root, "Response");
				DOMUtils.createElementAndText(response, "Status", "1");
				Element ce = DOMUtils.createElement(response, "Collection");
				DOMUtils.createElementAndText(ce, "Class", c.getDataClass());
				DOMUtils.createElementAndText(ce, "CollectionId", c
						.getCollectionId());
				Element estim = DOMUtils.createElement(ce, "Estimate");
				StateMachine sm = new StateMachine();
				SyncState state = sm.getSyncState(c.getSyncKey());
				IContentsExporter exporter = backend.getContentsExporter(bs);
				exporter.configure(c.getDataClass(), c.getFilterType(), state,
						0, 0);
				estim.setTextContent(exporter.getCount() + "");
			}
			responder.sendResponse("ItemEstimate", rep);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
}
