package org.obm.push.impl;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.push.backend.BackendSession;
import org.obm.push.backend.IBackend;
import org.obm.push.backend.IContentsImporter;
import org.obm.push.backend.IContinuation;
import org.obm.push.backend.MoveItem;
import org.obm.push.backend.PIMDataType;
import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Handles the MoveItems cmd
 * 
 * @author adrienp
 * 
 */
public class MoveItemsHandler extends WbxmlRequestHandler {

	private static final Log logger = LogFactory.getLog(MoveItemsHandler.class);

	public MoveItemsHandler(IBackend backend) {
		super(backend);
	}

	// <?xml version="1.0" encoding="UTF-8"?>
	// <MoveItems>
	// <Move>
	// <SrcMsgId>56:340</SrcMsgId>
	// <SrcFldId>56</SrcFldId>
	// <DstFldId>57</DstFldId>
	// </Move>
	// <Move>
	// <SrcMsgId>56:339</SrcMsgId>
	// <SrcFldId>56</SrcFldId>
	// <DstFldId>57</DstFldId>
	// </Move>
	// </MoveItems>
	@Override
	protected void process(IContinuation continuation, BackendSession bs,
			Document doc, ActiveSyncRequest request, Responder responder) {
		logger.info("process(" + bs.getLoginAtDomain() + "/" + bs.getDevType()
				+ ")");
		NodeList moves = doc.getDocumentElement().getElementsByTagName("Move");
		List<MoveItem> moveItems = new LinkedList<MoveItem>();
		for (int i = 0; i < moves.getLength(); i++) {
			Element mv = (Element) moves.item(i);

			String srcMsgId = DOMUtils.getElementText(mv, "SrcMsgId");
			String srcFldId = DOMUtils.getElementText(mv, "SrcFldId");
			String dstFldId = DOMUtils.getElementText(mv, "DstFldId");

			MoveItem mi = new MoveItem(srcMsgId, srcFldId, dstFldId);
			moveItems.add(mi);
		}
		try {
			Document reply = DOMUtils.createDoc(null, "MoveItems");
			Element root = reply.getDocumentElement();
			for (MoveItem item : moveItems) {
				String srcCollection = null;
				String dstCollection = null;
				Integer srcCollectionId = null;
				Integer dstCollectionId = null;
				String retDstId = "";
				int i = item.getSourceMessageId().indexOf(":");
				if (i > -1) {
					String mailUid = item.getSourceMessageId().substring(i + i);
					retDstId = item.getDestinationFolderId() + ":" + mailUid;
				}

				try {
					srcCollectionId = Integer
							.parseInt(item.getSourceFolderId());
					srcCollection = backend.getStore().getCollectionPath(
							srcCollectionId);
					dstCollectionId = Integer.parseInt(item
							.getDestinationFolderId());
					dstCollection = backend.getStore().getCollectionPath(
							dstCollectionId);
				} catch (NumberFormatException nfe) {
				}
				Element response = DOMUtils.createElement(root, "Response");

				if (srcCollectionId == null) {
					DOMUtils.createElementAndText(response, "Status", "1");
				} else if (srcCollectionId == null) {
					DOMUtils.createElementAndText(response, "Status", "2");
				} else if (srcCollectionId.equals(dstCollectionId)) {
					DOMUtils.createElementAndText(response, "Status", "4");
				} else {
					IContentsImporter importer = backend
							.getContentsImporter(Integer.parseInt(item
									.getDestinationFolderId()), bs);
					String dataClass = backend.getStore().getDataClass(
							srcCollection);
					String newDstId = importer.importMoveItem(bs, PIMDataType
							.valueOf(dataClass.toUpperCase()), srcCollection,
							dstCollection, item.getSourceMessageId());
					if (newDstId == null || "".equals(newDstId)) {
						// DOMUtils.createElementAndText(response, "Status",
						// "5");
						// SEND SYNC OK
						DOMUtils.createElementAndText(response, "Status", "3");
					} else {
						DOMUtils.createElementAndText(response, "Status", "3");
						retDstId = newDstId;

					}
					DOMUtils.createElementAndText(response, "DstMsgId",
							retDstId);
					DOMUtils.createElementAndText(response, "SrcMsgId", item
							.getSourceMessageId());
				}
				responder.sendResponse("Move", reply);
			}
		} catch (Exception e) {
			logger.info("Error creating Sync response", e);
		}
	}
}
