package org.obm.push.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.mortbay.jetty.HttpHeaderValues;
import org.mortbay.jetty.HttpHeaders;
import org.obm.push.backend.BackendSession;
import org.obm.push.backend.BodyPreference;
import org.obm.push.backend.IBackend;
import org.obm.push.backend.IContentsExporter;
import org.obm.push.backend.IContinuation;
import org.obm.push.backend.ItemChange;
import org.obm.push.backend.MSAttachementData;
import org.obm.push.backend.MSEmailBodyType;
import org.obm.push.backend.SyncCollection;
import org.obm.push.data.EmailEncoder;
import org.obm.push.exception.ActiveSyncException;
import org.obm.push.exception.ObjectNotFoundException;
import org.obm.push.search.StoreName;
import org.obm.push.utils.DOMUtils;
import org.obm.push.utils.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Handles the ItemOperations cmd
 * 
 * @author adrienp
 * 
 */
public class ItemOperationsHandler extends WbxmlRequestHandler {

	public ItemOperationsHandler(IBackend backend) {
		super(backend);
	}

	@Override
	public void process(IContinuation continuation, BackendSession bs,
			Document doc, ActiveSyncRequest request, Responder responder) {
		logger.info("process(" + bs.getLoginAtDomain() + "/" + bs.getDevType()
				+ ")");
		boolean multipart = false;
		boolean gzip = false;
		if ("T".equals(request.getHeader("MS-ASAcceptMultiPart"))
				|| "T"
						.equalsIgnoreCase(request
								.getParameter("AcceptMultiPart"))) {
			multipart = true;
		}
		if (request.getHeader(HttpHeaders.ACCEPT_ENCODING).contains(
				HttpHeaderValues.GZIP)) {
			gzip = true;
		}
		try {
			Element fetch = DOMUtils.getUniqueElement(doc.getDocumentElement(),
					"Fetch");
			StoreName store = StoreName.getValue(DOMUtils.getElementText(fetch,
					"Store"));
			Document ret = DOMUtils.createDoc(null, "ItemOperations");
			List<InputStream> items = new LinkedList<InputStream>();
			if (StoreName.Mailbox.equals(store)) {
				processMailboxFetch(bs, responder, fetch, multipart, ret, items);
				if (multipart) {
					responder.sendMSSyncMultipartResponse("ItemOperations", ret,
							items, gzip);
				} else {
					responder.sendResponse("ItemOperations", ret);
				}
			} else {
				logger.error("ItemOperations is not implemented for store "+store);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	private void processMailboxFetch(BackendSession bs, Responder responder,
			Element fetch, boolean multipart, Document ret,
			List<InputStream> items) throws IOException {
		IContentsExporter exporter = backend.getContentsExporter(bs);
		String reference = DOMUtils.getElementText(fetch, "FileReference");
		String collectionId = DOMUtils.getElementText(fetch, "CollectionId");
		String serverId = DOMUtils.getElementText(fetch, "ServerId");
		Integer type = Integer.getInteger(DOMUtils
				.getElementText(fetch, "Type"));
		if (reference != null) {
			processFileReferenceFetch(bs, exporter, fetch, reference,
					multipart, ret, items);
		} else if (collectionId != null && serverId != null) {
			processMailFetch(bs, exporter, multipart, collectionId, serverId,
					type, ret, items);
		}
	}

	private void processMailFetch(BackendSession bs,
			IContentsExporter exporter, boolean multipart, String collectionId,
			String serverId, Integer type, Document ret, List<InputStream> items) {
		List<String> fetchIds = new ArrayList<String>(1);
		fetchIds.add(serverId);
		ItemOperationsStatus status = ItemOperationsStatus.SUCCESS;
		List<ItemChange> ic = null;
		try {
			ic = exporter.fetchMails(bs, fetchIds);
			if (ic.size() == 0) {
				status = ItemOperationsStatus.DOCUMENT_LIBRARY_NOT_FOUND;
			}
		} catch (ActiveSyncException e) {
			status = ItemOperationsStatus.DOCUMENT_LIBRARY_NOT_FOUND;
		}

		Element root = ret.getDocumentElement();
		DOMUtils.createElementAndText(root, "Status",
				ItemOperationsStatus.SUCCESS.asXmlValue());
		Element resp = DOMUtils.createElement(root, "Response");
		Element fetchResp = DOMUtils.createElement(resp, "Fetch");
		DOMUtils.createElementAndText(fetchResp, "Status", status.asXmlValue());
		DOMUtils.createElementAndText(fetchResp, "AirSync:ServerId", serverId);
		DOMUtils.createElementAndText(fetchResp, "AirSync:CollectionId",
				collectionId);
		if (ItemOperationsStatus.SUCCESS.equals(status)) {
			Element dataElem = DOMUtils.createElement(fetchResp, "Data");
			EmailEncoder ee = new EmailEncoder();
			SyncCollection c = new SyncCollection();
			c.setCollectionId(Integer.parseInt(collectionId));
			BodyPreference bp = new BodyPreference();
			bp.setType(MSEmailBodyType.getValueOf(type));
			c.addBodyPreference(bp);
			ee.encode(bs, dataElem, ic.get(0).getData(), c, true);
			if (multipart) {
				Element data = DOMUtils.getUniqueElement(dataElem, "Data");
				String dataValue = "";
				if (data != null) {
					dataValue = data.getTextContent();
					Element pData = (Element) data.getParentNode();
					pData.removeChild(data);
					data = null;
					DOMUtils.createElementAndText(pData, "Part", "1");
				}
				items.add(new ByteArrayInputStream(dataValue.getBytes()));
			}
		}
	}

	private void processFileReferenceFetch(BackendSession bs,
			IContentsExporter exporter, Element fetch, String reference,
			boolean multipart, Document ret, List<InputStream> items) {
		ItemOperationsStatus status = ItemOperationsStatus.SUCCESS;
		MSAttachementData data = null;
		try {
			data = exporter.getEmailAttachement(bs, reference);
		} catch (ObjectNotFoundException e) {
			status = ItemOperationsStatus.MAILBOX_INVALID_ATTACHMENT_ID;
		}

		String attch = "";
		if (data != null) {
			try {
				byte[] att = FileUtils.streamBytes(data.getFile(), true);
				attch = new String(att);
				items.add(new ByteArrayInputStream(att));
			} catch (Throwable e) {
				status = ItemOperationsStatus.MAILBOX_ITEM_FAILED_CONVERSATION;
			}
		}
		Element root = ret.getDocumentElement();
		DOMUtils.createElementAndText(root, "Status",
				ItemOperationsStatus.SUCCESS.asXmlValue());
		Element resp = DOMUtils.createElement(root, "Response");
		Element fetchResp = DOMUtils.createElement(resp, "Fetch");
		DOMUtils.createElementAndText(fetchResp, "Status", status.asXmlValue());
		if (ItemOperationsStatus.SUCCESS.equals(status)) {
			DOMUtils.createElementAndText(fetchResp,
					"AirSyncBase:FileReference", reference);
			Element properties = DOMUtils
					.createElement(fetchResp, "Properties");
			DOMUtils.createElementAndText(properties,
					"AirSyncBase:ContentType", data.getContentType());
			if (!multipart) {
				DOMUtils.createElementAndText(properties, "Data", attch);
			} else {
				DOMUtils.createElementAndText(properties, "Part", "1");
			}
		}
	}
}
