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
				MoveItemsStatus status = null;
				try {
					dstCollectionId = Integer.parseInt(item
							.getDestinationFolderId());
					dstCollection = backend.getStore().getCollectionPath(
							dstCollectionId);
				} catch (Throwable nfe) {
					status = MoveItemsStatus.INVALID_DESTINATION_COLLECTION_ID;
				}
				
				try {
					srcCollectionId = Integer
							.parseInt(item.getSourceFolderId());
					srcCollection = backend.getStore().getCollectionPath(
							srcCollectionId);
				} catch (Throwable nfe) {
					status = MoveItemsStatus.INVALID_SOURCE_COLLECTION_ID;
				}
				
				Element response = DOMUtils.createElement(root, "Response");

				if (status == null && srcCollectionId.equals(dstCollectionId)) {
					status = MoveItemsStatus.SAME_SOURCE_AND_DESTINATION_COLLECTION_ID;
				}

				if (status == null) {
					try {
						IContentsImporter importer = backend
								.getContentsImporter(Integer.parseInt(item
										.getDestinationFolderId()), bs);
						PIMDataType dataClass = backend.getStore()
								.getDataClass(srcCollection);
						String newDstId = importer.importMoveItem(bs,
								dataClass, srcCollection, dstCollection, item
										.getSourceMessageId());
						DOMUtils.createElementAndText(response, "Status",
								MoveItemsStatus.SUCCESS.asXmlValue());
						DOMUtils.createElementAndText(response, "SrcMsgId",
								item.getSourceMessageId());
						DOMUtils.createElementAndText(response, "DstMsgId",
								newDstId);
					} catch (Exception e) {
						DOMUtils.createElementAndText(response, "SrcMsgId",
								item.getSourceMessageId());
						DOMUtils.createElementAndText(response, "Status",
								MoveItemsStatus.SERVER_ERROR.asXmlValue());
					}
				} else {
					DOMUtils.createElementAndText(response, "SrcMsgId", item
							.getSourceMessageId());
					DOMUtils.createElementAndText(response, "Status", status
							.asXmlValue());
				}
			}
			responder.sendResponse("Move", reply);
		} catch (Throwable e) {
			logger.info("Error creating Sync response", e);
		}
	}
}
