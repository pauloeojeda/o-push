package org.obm.push.impl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.push.backend.BackendSession;
import org.obm.push.backend.IBackend;
import org.obm.push.backend.IContentsExporter;
import org.obm.push.backend.IContentsImporter;
import org.obm.push.backend.IContinuation;
import org.obm.push.backend.ItemChange;
import org.obm.push.backend.MSEmail;
import org.obm.push.backend.MSEvent;
import org.obm.push.backend.PIMDataType;
import org.obm.push.data.calendarenum.AttendeeStatus;
import org.obm.push.data.email.MeetingResponse;
import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Handles the MeetingResponse cmd
 * 
 * @author adrienp
 * 
 */
public class MeetingResponseHandler extends WbxmlRequestHandler {

	private static final Log logger = LogFactory
			.getLog(MeetingResponseHandler.class);

	public MeetingResponseHandler(IBackend backend) {
		super(backend);
	}

	// <?xml version="1.0" encoding="UTF-8"?>
	// <MeetingResponse>
	// <Request>
	// <UserResponse>1</UserResponse>
	// <CollectionId>62</CollectionId>
	// <ReqId>62:379</ReqId>
	// </Request>
	// </MeetingResponse>

	@Override
	protected void process(IContinuation continuation, BackendSession bs,
			Document doc, ActiveSyncRequest request, Responder responder) {
		logger.info("process(" + bs.getLoginAtDomain() + "/" + bs.getDevType()
				+ ")");
		NodeList requests = doc.getDocumentElement().getElementsByTagName(
				"Request");

		Set<MeetingResponse> items = new HashSet<MeetingResponse>();

		for (int i = 0; i < requests.getLength(); i++) {
			Element req = (Element) requests.item(i);
			String userResponse = DOMUtils.getElementText(req, "UserResponse");
			String collectionId = DOMUtils.getElementText(req, "CollectionId");
			String reqId = DOMUtils.getElementText(req, "ReqId");
			String longId = DOMUtils.getElementText(req, "LongId");

			MeetingResponse mr = new MeetingResponse();

			if (collectionId != null && !collectionId.isEmpty()) {
				mr.setCollectionId(Integer.parseInt(collectionId));
			}
			mr.setReqId(reqId);
			mr.setLongId(longId);

			if ("1".equals(userResponse)) {
				mr.setUserResponse(AttendeeStatus.ACCEPT);
			} else if ("2".equals(userResponse)) {
				mr.setUserResponse(AttendeeStatus.TENTATIVE);
			} else if ("3".equals(userResponse)) {
				mr.setUserResponse(AttendeeStatus.DECLINE);
			} else {
				mr.setUserResponse(AttendeeStatus.TENTATIVE);
			}

			items.add(mr);
		}

		try {
			bs.setDataType(PIMDataType.EMAIL);
			Document reply = DOMUtils.createDoc(null, "MeetingResponse");
			Element root = reply.getDocumentElement();
			for (MeetingResponse item : items) {

				IContentsExporter exporter = backend.getContentsExporter(bs);
				List<ItemChange> lit = exporter.fetch(bs, Arrays.asList(item
						.getReqId()));
				ItemChange ic = null;
				if (lit.size() > 0) {
					ic = lit.get(0);
				}

				Element response = DOMUtils.createElement(root, "Result");
				if (ic == null || ic.getData() == null) {
					DOMUtils.createElementAndText(response, "Status", "3");
				} else {
					MSEvent invitation = ((MSEmail) ic.getData())
							.getInvitation();
					if (invitation == null) {
						DOMUtils.createElementAndText(response, "Status", "2");
					} else {
						IContentsImporter importer = backend
								.getContentsImporter(item.getCollectionId(), bs);
						String calId = importer.importCalendarUserStatus(bs,
								invitation, item.getUserResponse());
						DOMUtils.createElementAndText(response, "Status", "1");

						if (!AttendeeStatus.DECLINE.equals(item
								.getUserResponse())) {
							DOMUtils.createElementAndText(response, "CalId",
									calId);
						}
					}
				}
				DOMUtils.createElementAndText(response, "ReqId", item
						.getReqId());

				responder.sendResponse("MeetingResponse", reply);
			}
		} catch (Exception e) {
			logger.info("Error creating Sync response", e);
		}
	}
}
