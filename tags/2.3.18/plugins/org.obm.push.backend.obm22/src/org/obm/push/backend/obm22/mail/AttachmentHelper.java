package org.obm.push.backend.obm22.mail;

import java.util.HashMap;
import java.util.Map;

import org.minig.imap.impl.Base64;

/**
 * 
 * @author adrienp
 * 
 */
public class AttachmentHelper {

	public final static String COLLECTION_ID = "collectionId";
	public final static String MESSAGE_ID = "messageId";
	public final static String MIME_PART_ADDRESS = "mimePartAddress";
	public final static String CONTENT_TYPE = "contentType";
	public final static String CONTENT_TRANSFERE_ENCODING = "contentTransferEncoding";

	public static String getAttachmentId(String collectionId, String messageId,
			String mimePartAddress, String contentType,
			String contentTransferEncoding) {
		StringBuilder ct = Base64.encode(contentType);
		String ret = collectionId + "_" + messageId + "_" + mimePartAddress
				+ "_" + ct;
		if (contentTransferEncoding != null
				&& !contentTransferEncoding.isEmpty()) {
			StringBuilder cte = Base64.encode(contentTransferEncoding);
			ret += "_" + cte;
		}
		return ret;
	}

	public static Map<String, String> parseAttachmentId(String attachmentId) {
		String[] tab = attachmentId.split("_");
		if (tab.length < 4) {
			return null;
		}
		Map<String, String> data = new HashMap<String, String>();
		data.put(COLLECTION_ID, tab[0]);
		data.put(MESSAGE_ID, tab[1]);
		data.put(MIME_PART_ADDRESS, tab[2]);
		data.put(CONTENT_TYPE, new String(Base64.decode(tab[3]).array()));
		if(tab.length >=5){
			data.put(CONTENT_TRANSFERE_ENCODING, new String(Base64.decode(tab[4])
				.array()));
		}
		return data;
	}
}
